package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachments;
import org.mydrugs.mydrugs.energy.PsyCurrentMachines;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.machine.manual.ManualMachineSpeedHelper;
import org.mydrugs.mydrugs.machine.manual.ManualMachineType;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferAttachments;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatFluidStack;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatRecipe;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatRecipeInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MixingVatBlockEntity extends BlockEntity implements MachineStatusProvider {
    public static final int MAX_ITEM_TYPES = 4;
    public static final int FLUID_CAPACITY = 4000;
    public static final int STIR_ANIMATION_TICKS = 8;
    private final NonNullList<ItemStack> inputItems = NonNullList.withSize(MAX_ITEM_TYPES, ItemStack.EMPTY);
    @Nullable
    private ResourceLocation inputFluid1Id = null;
    private int inputFluid1Amount = 0;
    @Nullable
    private ResourceLocation inputFluid2Id = null;
    private int inputFluid2Amount = 0;
    private ItemStack resultItem = ItemStack.EMPTY;
    @Nullable
    private ResourceLocation resultFluidId = null;
    private int resultFluidAmount = 0;
    private int progress = 0;
    private int maxProgress = 100;
    private int currentStirs = 0;
    private int requiredStirs = 0;
    private int stirAnimationTicks = 0;
    private int pendingFumeTicks = 0;
    private double fractionalWork = 0.0D;
    private MachineStatus machineStatus = MachineStatus.IDLE;

    public MixingVatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIXING_VAT.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MixingVatBlockEntity be) {
        if (be.stirAnimationTicks > 0) {
            be.stirAnimationTicks--;
            if (!level.isClientSide()) {
                be.setChanged();
            }
        }

        if (!level.isClientSide() && (level.getGameTime() % 20L == 0L)
                && be.getVisualFluidAmount() > 0 && be.isHeated()) {
            AABB interior = new AABB(
                    pos.getX() + 0.1875, pos.getY() + 0.1875, pos.getZ() + 0.1875,
                    pos.getX() + 0.8125, pos.getY() + 1.0,    pos.getZ() + 0.8125
            );
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, interior)) {
                if (entity.fireImmune()) continue;
                entity.hurt(level.damageSources().inFire(), 2.0F);
                entity.igniteForSeconds(2);
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            tickStreetCookHazard(serverLevel, pos, be);
            be.tickAutomation(serverLevel);
        }
    }

    private static void tickStreetCookHazard(ServerLevel level, BlockPos pos, MixingVatBlockEntity be) {
        if (be.pendingFumeTicks > 0) {
            be.pendingFumeTicks--;
            if (be.pendingFumeTicks == 0) {
                StreetCookFumeHazard.vent(level, pos);
            }
            return;
        }
        if (!StreetCookFumeHazard.isEnabled()) return;
        if (level.getGameTime() % 20L != 0L) return;
        if (!be.isHeated()) return;
        Optional<RecipeHolder<MixingVatRecipe>> holder = be.getCurrentRecipe(level);
        if (holder.isEmpty()) return;
        if (!StreetCookFumeHazard.recipeProducesCrudeMethSlurry(holder.get().value())) return;
        double chance = StreetCookFumeHazard.VENT_BASE_CHANCE_PER_SECOND * StreetCookFumeHazard.intensity();
        if (level.random.nextDouble() < chance) {
            StreetCookFumeHazard.telegraph(level, pos);
            be.pendingFumeTicks = StreetCookFumeHazard.TELEGRAPH_TICKS;
            be.setChanged();
        }
    }

    public List<ItemStack> getVisualItems() {
        if (!resultItem.isEmpty()) {
            return List.of(resultItem);
        }

        List<ItemStack> list = new ArrayList<>();
        for (ItemStack stack : inputItems) {
            if (!stack.isEmpty()) {
                list.add(stack);
            }
        }
        return list;
    }

    @Nullable
    private ResourceLocation firstInputFluidId() {
        if (inputFluid1Id != null && inputFluid1Amount > 0) {
            return inputFluid1Id;
        }
        if (inputFluid2Id != null && inputFluid2Amount > 0) {
            return inputFluid2Id;
        }
        return null;
    }

    private int firstInputFluidAmount() {
        if (inputFluid1Id != null && inputFluid1Amount > 0) {
            return inputFluid1Amount;
        }
        if (inputFluid2Id != null && inputFluid2Amount > 0) {
            return inputFluid2Amount;
        }
        return 0;
    }

    public int getTotalInputFluidAmount() {
        return inputFluid1Amount + inputFluid2Amount;
    }

    @Nullable
    public ResourceLocation getVisualFluidId() {
        if (resultFluidId != null && resultFluidAmount > 0) {
            return resultFluidId;
        }
        return firstInputFluidId();
    }

    public int getVisualFluidAmount() {
        if (resultFluidId != null && resultFluidAmount > 0) {
            return resultFluidAmount;
        }
        return getTotalInputFluidAmount();
    }

    public float getVisualFluidRatio() {
        return Math.min(1.0f, getVisualFluidAmount() / (float) FLUID_CAPACITY);
    }

    public boolean hasPendingResult() {
        return !resultItem.isEmpty() || (resultFluidId != null && resultFluidAmount > 0);
    }

    private boolean hasBlockingResult() {
        return !resultItem.isEmpty();
    }

    private boolean hasResultFluid() {
        return resultFluidId != null && resultFluidAmount > 0;
    }

    public boolean hasContentsToMix() {
        if (hasResultFluid()) return true;
        if (getTotalInputFluidAmount() > 0) return true;

        for (ItemStack stack : inputItems) {
            if (!stack.isEmpty()) return true;
        }

        return false;
    }

    public int getCurrentStirs() {
        return currentStirs;
    }

    public int getRequiredStirs() {
        return requiredStirs;
    }

    public float getStirAnimationProgress(float partialTick) {
        if (stirAnimationTicks <= 0) {
            return 0.0f;
        }

        float elapsed = (STIR_ANIMATION_TICKS - stirAnimationTicks) + partialTick;
        return Math.min(1.0f, elapsed / (float) STIR_ANIMATION_TICKS);
    }

    public boolean isStirAnimationActive() {
        return stirAnimationTicks > 0;
    }

    public boolean isHeated() {
        if (level == null) return false;
        BlockPos below = worldPosition.below();
        net.minecraft.world.level.block.state.BlockState belowState = level.getBlockState(below);
        Block belowBlock = belowState.getBlock();

        if (belowBlock == Blocks.FIRE || belowBlock == Blocks.SOUL_FIRE) {
            return true;
        }

        if ((belowBlock == Blocks.CAMPFIRE || belowBlock == Blocks.SOUL_CAMPFIRE)
                && belowState.getValue(BlockStateProperties.LIT)) {
            return true;
        }

        return false;
    }

    private void resetMixingProgress() {
        progress = 0;
        maxProgress = 100;
        currentStirs = 0;
        requiredStirs = 0;
        fractionalWork = 0.0D;
    }

    public boolean insertOneItem(ItemStack held) {
        if (held.isEmpty() || hasBlockingResult()) return false;

        for (int i = 0; i < inputItems.size(); i++) {
            ItemStack existing = inputItems.get(i);
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, held) && existing.getCount() < existing.getMaxStackSize()) {
                existing.grow(1);
                resetMixingProgress();
                notifyUpdate();
                return true;
            }
        }

        for (int i = 0; i < inputItems.size(); i++) {
            if (inputItems.get(i).isEmpty()) {
                ItemStack inserted = held.copyWithCount(1);
                inputItems.set(i, inserted);
                resetMixingProgress();
                notifyUpdate();
                return true;
            }
        }

        return false;
    }

    public int insertWholeStack(ItemStack held) {
        if (held.isEmpty() || hasBlockingResult()) return 0;

        int inserted = 0;
        int remaining = held.getCount();

        for (int i = 0; i < inputItems.size() && remaining > 0; i++) {
            ItemStack existing = inputItems.get(i);
            if (existing.isEmpty()) continue;
            if (!ItemStack.isSameItemSameComponents(existing, held)) continue;

            int free = existing.getMaxStackSize() - existing.getCount();
            if (free <= 0) continue;

            int moved = Math.min(free, remaining);
            existing.grow(moved);
            inserted += moved;
            remaining -= moved;
        }

        for (int i = 0; i < inputItems.size() && remaining > 0; i++) {
            if (!inputItems.get(i).isEmpty()) continue;

            int moved = Math.min(held.getMaxStackSize(), remaining);
            inputItems.set(i, held.copyWithCount(moved));
            inserted += moved;
            remaining -= moved;
        }

        if (inserted > 0) {
            resetMixingProgress();
            notifyUpdate();
        }
        return inserted;
    }

    private int getInsertableAmount(ResourceLocation incomingId, int requestedAmount) {
        if (requestedAmount <= 0) {
            return 0;
        }

        int freeSpace = FLUID_CAPACITY - getTotalInputFluidAmount();
        if (freeSpace <= 0) {
            return 0;
        }

        boolean matchesExisting =
                (inputFluid1Amount > 0 && incomingId.equals(inputFluid1Id)) ||
                        (inputFluid2Amount > 0 && incomingId.equals(inputFluid2Id));

        boolean hasEmptyTank = inputFluid1Amount <= 0 || inputFluid2Amount <= 0;

        if (!matchesExisting && !hasEmptyTank) {
            return 0;
        }

        int insertable = Math.min(requestedAmount, freeSpace);
        while (insertable > 0 && !canPromoteResultFluidAfterInsert(incomingId, insertable)) {
            insertable--;
        }
        return insertable;
    }

    private int getSlotInsertableAmount(int slot, ResourceLocation incomingId, int requestedAmount) {
        if (requestedAmount <= 0 || slot < 0 || slot > 1) {
            return 0;
        }

        int existingAmount = slot == 0 ? inputFluid1Amount : inputFluid2Amount;
        int freeSpace = FLUID_CAPACITY - getTotalInputFluidAmount();
        if (freeSpace <= 0) {
            return 0;
        }

        int insertable = Math.min(requestedAmount, freeSpace);
        while (insertable > 0 && !canPromoteResultFluidAfterSlotInsert(slot, incomingId, insertable)) {
            insertable--;
        }

        int slotCapacity = FLUID_CAPACITY - existingAmount;
        return Math.min(insertable, slotCapacity);
    }

    private void addInputFluid(ResourceLocation incomingId, int amount) {
        if (amount <= 0) {
            return;
        }

        if (inputFluid1Amount > 0 && incomingId.equals(inputFluid1Id)) {
            inputFluid1Amount += amount;
            return;
        }

        if (inputFluid2Amount > 0 && incomingId.equals(inputFluid2Id)) {
            inputFluid2Amount += amount;
            return;
        }

        if (inputFluid1Amount <= 0) {
            inputFluid1Id = incomingId;
            inputFluid1Amount = amount;
            return;
        }

        if (inputFluid2Amount <= 0) {
            inputFluid2Id = incomingId;
            inputFluid2Amount = amount;
        }
    }

    private boolean canPromoteResultFluidAfterInsert(ResourceLocation incomingId, int incomingAmount) {
        if (!hasResultFluid()) {
            return true;
        }

        FluidSnapshot tank1 = new FluidSnapshot(inputFluid1Id, inputFluid1Amount);
        FluidSnapshot tank2 = new FluidSnapshot(inputFluid2Id, inputFluid2Amount);

        if (incomingAmount > 0) {
            if (tank1.amount() > 0 && incomingId.equals(tank1.id())) {
                tank1 = new FluidSnapshot(tank1.id(), tank1.amount() + incomingAmount);
            } else if (tank2.amount() > 0 && incomingId.equals(tank2.id())) {
                tank2 = new FluidSnapshot(tank2.id(), tank2.amount() + incomingAmount);
            } else if (tank1.amount() <= 0) {
                tank1 = new FluidSnapshot(incomingId, incomingAmount);
            } else if (tank2.amount() <= 0) {
                tank2 = new FluidSnapshot(incomingId, incomingAmount);
            } else {
                return false;
            }
        }

        return canFitResultFluidInInputTanks(tank1, tank2);
    }

    private boolean canPromoteResultFluidAfterSlotInsert(int slot, ResourceLocation incomingId, int incomingAmount) {
        if (!hasResultFluid()) {
            return true;
        }

        FluidSnapshot tank1 = new FluidSnapshot(inputFluid1Id, inputFluid1Amount);
        FluidSnapshot tank2 = new FluidSnapshot(inputFluid2Id, inputFluid2Amount);

        if (incomingAmount > 0) {
            if (slot == 0) {
                tank1 = new FluidSnapshot(incomingId, tank1.amount() + incomingAmount);
            } else if (slot == 1) {
                tank2 = new FluidSnapshot(incomingId, tank2.amount() + incomingAmount);
            } else {
                return false;
            }
        }

        return canFitResultFluidInInputTanks(tank1, tank2);
    }

    private boolean canFitResultFluidInInputTanks(FluidSnapshot tank1, FluidSnapshot tank2) {
        if (!hasResultFluid()) {
            return true;
        }
        if (tank1.amount() + tank2.amount() + resultFluidAmount > FLUID_CAPACITY) {
            return false;
        }
        return (tank1.amount() > 0 && resultFluidId.equals(tank1.id()))
                || (tank2.amount() > 0 && resultFluidId.equals(tank2.id()))
                || tank1.amount() <= 0
                || tank2.amount() <= 0;
    }


    public boolean tryFillCoffeeCup(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty() || !held.is(ModItems.CUP.get())) {
            return false;
        }

        ItemStack filled = filledCupForResultFluid();
        if (filled.isEmpty() || resultFluidAmount < 250) {
            return false;
        }

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        if (held.isEmpty()) {
            player.setItemInHand(hand, filled);
        } else if (!player.getInventory().add(filled)) {
            player.drop(filled, false);
        }
        resultFluidAmount -= 250;
        if (resultFluidAmount <= 0) {
            resultFluidAmount = 0;
            resultFluidId = null;
        }
        resetMixingProgress();
        notifyUpdate();
        return true;
    }

    private ItemStack filledCupForResultFluid() {
        if (resultFluidId == null) {
            return ItemStack.EMPTY;
        }

        if (resultFluidId.equals(ModFluids.rl("coffee"))) {
            return new ItemStack(ModItems.COFFEE_CUP.get());
        }

        if (resultFluidId.equals(ModFluids.rl("herbal_tea"))) {
            return new ItemStack(ModItems.HERBAL_TEA.get());
        }

        return ItemStack.EMPTY;
    }

    public boolean tryInsertFluidFromHeld(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty() || hasBlockingResult()) return false;

        if (player.getAbilities().instabuild && held.getItem() instanceof GlassBottleItem) {
            ResourceLocation incomingId = GlassBottleItem.getStoredFluidId(held);
            int containedAmount = GlassBottleItem.getStoredAmount(held);

            if (incomingId == null || containedAmount <= 0) {
                return false;
            }

            int moved = getInsertableAmount(incomingId, containedAmount);
            if (moved <= 0) {
                return false;
            }

            addInputFluid(incomingId, moved);
            resetMixingProgress();
            notifyUpdate();
            return true;
        }

        ItemAccess access = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        var handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) return false;

        FluidResource resource = handler.getResource(0);
        int containedAmount = handler.getAmountAsInt(0);

        if (resource.isEmpty() || containedAmount <= 0) {
            return false;
        }

        ResourceLocation incomingId = BuiltInRegistries.FLUID.getKey(resource.getFluid());
        if (incomingId == null) return false;

        int requested = getInsertableAmount(incomingId, containedAmount);
        if (requested <= 0) {
            return false;
        }

        try (var tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, requested, tx);
            if (extracted <= 0) {
                return false;
            }

            tx.commit();
            addInputFluid(incomingId, extracted);
        }

        resetMixingProgress();
        notifyUpdate();
        return true;
    }

    private int firstNonEmptyInputFluidTank() {
        if (inputFluid1Id != null && inputFluid1Amount > 0) {
            return 1;
        }
        if (inputFluid2Id != null && inputFluid2Amount > 0) {
            return 2;
        }
        return 0;
    }

    @Nullable
    private ResourceLocation getInputFluidId(int tank) {
        return switch (tank) {
            case 1 -> inputFluid1Id;
            case 2 -> inputFluid2Id;
            default -> null;
        };
    }

    private int getInputFluidAmount(int tank) {
        return switch (tank) {
            case 1 -> inputFluid1Amount;
            case 2 -> inputFluid2Amount;
            default -> 0;
        };
    }

    public boolean tryExtractFluidToHeld(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty()) return false;

        ResourceLocation sourceId;
        int sourceAmount;
        boolean extractingResult = false;
        int inputTank = 0;

        if (resultFluidId != null && resultFluidAmount > 0) {
            sourceId = resultFluidId;
            sourceAmount = resultFluidAmount;
            extractingResult = true;
        } else {
            inputTank = firstNonEmptyInputFluidTank();
            if (inputTank == 0) {
                return false;
            }

            sourceId = getInputFluidId(inputTank);
            sourceAmount = getInputFluidAmount(inputTank);
            if (sourceId == null || sourceAmount <= 0) {
                return false;
            }
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(sourceId);
        if (fluid == null || fluid == Fluids.EMPTY) return false;

        if (player.getAbilities().instabuild && held.getItem() instanceof GlassBottleItem) {
            if (!GlassBottleItem.isFluidBottlable(fluid)) {
                return false;
            }

            ItemStack filledBottle = held.copyWithCount(1);
            int moved = GlassBottleItem.fill(
                    filledBottle,
                    sourceId,
                    Math.min(sourceAmount, GlassBottleItem.CAPACITY_MB)
            );

            if (moved <= 0) {
                return false;
            }

            if (!player.getInventory().add(filledBottle)) {
                return false;
            }

            removeFromVat(extractingResult, inputTank, moved);
            resetMixingProgress();
            notifyUpdate();
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
            return true;
        }

        ItemAccess access = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        var handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) return false;

        FluidResource resource = FluidResource.of(fluid);

        int transferred;
        try (var tx = Transaction.openRoot()) {
            transferred = handler.insert(resource, sourceAmount, tx);
            if (transferred <= 0) {
                return false;
            }

            tx.commit();
        }

        removeFromVat(extractingResult, inputTank, transferred);
        resetMixingProgress();
        notifyUpdate();
        return true;
    }

    public boolean takeResultItem(Player player) {
        if (resultItem.isEmpty()) return false;

        ItemStack toGive = resultItem.copy();
        resultItem = ItemStack.EMPTY;

        if (!player.addItem(toGive)) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, toGive);
        }

        resetMixingProgress();
        notifyUpdate();
        return true;
    }

    public boolean takeFirstIngredientItem(Player player) {
        for (int i = MAX_ITEM_TYPES - 1; i >= 0; i--) {
            ItemStack item = inputItems.get(i);

            if (item.isEmpty()) continue;

            ItemStack toGive = item.copy();

            if (!player.addItem(toGive)) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, toGive);
            }

            inputItems.set(i, ItemStack.EMPTY);

            resetMixingProgress();
            notifyUpdate();

            return true;
        }
        return false;
    }

    private void notifyUpdate() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private List<ItemStack> currentInputList() {
        List<ItemStack> list = new ArrayList<>();
        for (ItemStack stack : inputItems) {
            if (!stack.isEmpty()) {
                list.add(stack);
            }
        }
        return list;
    }

    private List<MixingVatFluidStack> currentFluidList() {
        List<MixingVatFluidStack> list = new ArrayList<>();

        if (inputFluid1Id != null && inputFluid1Amount > 0) {
            list.add(new MixingVatFluidStack(inputFluid1Id, inputFluid1Amount));
        }

        if (inputFluid2Id != null && inputFluid2Amount > 0) {
            list.add(new MixingVatFluidStack(inputFluid2Id, inputFluid2Amount));
        }

        return list;
    }

    private List<MixingVatFluidStack> currentFluidListWithResultFluid() {
        List<MixingVatFluidStack> list = new ArrayList<>();
        boolean resultMerged = false;

        if (inputFluid1Id != null && inputFluid1Amount > 0) {
            int amount = inputFluid1Amount;
            if (hasResultFluid() && resultFluidId.equals(inputFluid1Id)) {
                amount += resultFluidAmount;
                resultMerged = true;
            }
            list.add(new MixingVatFluidStack(inputFluid1Id, amount));
        }

        if (inputFluid2Id != null && inputFluid2Amount > 0) {
            int amount = inputFluid2Amount;
            if (!resultMerged && hasResultFluid() && resultFluidId.equals(inputFluid2Id)) {
                amount += resultFluidAmount;
                resultMerged = true;
            }
            list.add(new MixingVatFluidStack(inputFluid2Id, amount));
        }

        if (hasResultFluid() && !resultMerged) {
            list.add(new MixingVatFluidStack(resultFluidId, resultFluidAmount));
        }

        return list;
    }

    private Optional<RecipeHolder<MixingVatRecipe>> getCurrentRecipe(ServerLevel level) {
        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.MIXING_VAT.get(),
                new MixingVatRecipeInput(currentInputList(), currentFluidList()),
                level
        );
    }

    private Optional<RecipeHolder<MixingVatRecipe>> getMixableRecipe(ServerLevel level) {
        if (hasResultFluid()) {
            return getRecipeUsingResultFluid(level);
        }
        return getCurrentRecipe(level);
    }

    private Optional<RecipeHolder<MixingVatRecipe>> getRecipeUsingResultFluid(ServerLevel level) {
        if (!hasResultFluid() || !canPromoteResultFluidAfterInsert(resultFluidId, 0)) {
            return Optional.empty();
        }

        MixingVatRecipeInput input = new MixingVatRecipeInput(currentInputList(), currentFluidListWithResultFluid());
        for (RecipeHolder<MixingVatRecipe> holder : level.recipeAccess()
                .recipeMap()
                .byType(ModRecipeTypes.MIXING_VAT.get())) {
            MixingVatRecipe recipe = holder.value();
            if (recipeRequiresResultFluid(recipe) && recipe.matches(input, level)) {
                return Optional.of(holder);
            }
        }

        return Optional.empty();
    }

    private boolean recipeRequiresResultFluid(MixingVatRecipe recipe) {
        return hasResultFluid()
                && recipe.requiredFluids().stream().anyMatch(required -> required.fluid().equals(resultFluidId));
    }

    private boolean promoteResultFluidToInput() {
        if (!hasResultFluid()) {
            return true;
        }
        if (!canPromoteResultFluidAfterInsert(resultFluidId, 0)) {
            return false;
        }

        addInputFluid(resultFluidId, resultFluidAmount);
        resultFluidId = null;
        resultFluidAmount = 0;
        return true;
    }

    private void consumeOneMatchingItem(net.minecraft.world.item.crafting.Ingredient ingredient) {
        for (int i = 0; i < inputItems.size(); i++) {
            ItemStack stack = inputItems.get(i);
            if (!stack.isEmpty() && ingredient.test(stack)) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    inputItems.set(i, ItemStack.EMPTY);
                }
                return;
            }
        }
    }

    private boolean consumeInputFluid(MixingVatFluidStack required) {
        if (inputFluid1Amount >= required.amount()
                && required.fluid().equals(inputFluid1Id)) {
            inputFluid1Amount -= required.amount();
            if (inputFluid1Amount <= 0) {
                inputFluid1Amount = 0;
                inputFluid1Id = null;
            }
            return true;
        }

        if (inputFluid2Amount >= required.amount()
                && required.fluid().equals(inputFluid2Id)) {
            inputFluid2Amount -= required.amount();
            if (inputFluid2Amount <= 0) {
                inputFluid2Amount = 0;
                inputFluid2Id = null;
            }
            return true;
        }

        return false;
    }

    private void craft(RecipeHolder<MixingVatRecipe> holder) {
        MixingVatRecipe recipe = holder.value();
        for (var ingredient : recipe.requiredItems()) {
            consumeOneMatchingItem(ingredient);
        }

        for (var requiredFluid : recipe.requiredFluids()) {
            consumeInputFluid(requiredFluid);
        }

        resultItem = recipe.resultItem().copy();

        if (recipe.resultFluid().isPresent()) {
            var fluid = recipe.resultFluid().get();
            resultFluidId = fluid.fluid();
            resultFluidAmount = fluid.amount();
        } else {
            resultFluidId = null;
            resultFluidAmount = 0;
        }
        org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(
                this,
                Optional.of(holder.id().location()),
                org.mydrugs.mydrugs.machine.MachineCompletionHelper.itemId(recipe.resultItem()),
                recipe.resultFluid().map(MixingVatFluidStack::fluid),
                Optional.empty(),
                Optional.empty()
        );
    }

    public boolean stirOnce(Player player) {
        if (level == null || level.isClientSide()) {
            return false;
        }

        if (hasBlockingResult() || isStirAnimationActive()) {
            return false;
        }

        Optional<RecipeHolder<MixingVatRecipe>> recipeHolder = getMixableRecipe((ServerLevel) level);
        if (recipeHolder.isEmpty()) {
            resetMixingProgress();
            notifyUpdate();
            return false;
        }

        MixingVatRecipe recipe = recipeHolder.get().value();

        if (recipe.requiresHeat() && !isHeated()) {
            return false;
        }

        if (hasResultFluid() && !promoteResultFluidToInput()) {
            return false;
        }

        float speed = player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                ? ManualMachineSpeedHelper.getSpeedMultiplier(serverPlayer, ManualMachineType.MIXING_VAT)
                : 1.0F;
        addRecipeWork(recipeHolder.get(), 20.0D * speed);
        stirAnimationTicks = STIR_ANIMATION_TICKS;

        notifyUpdate();
        return true;
    }

    private void tickAutomation(ServerLevel level) {
        if (!MachineEnergyAttachments.get(this).hasAutomationUpgrade()) {
            setMachineStatus(MachineStatus.IDLE);
            return;
        }
        if (hasBlockingResult()) {
            setMachineStatus(MachineStatus.OUTPUT_SLOT_FULL);
            return;
        }
        Optional<RecipeHolder<MixingVatRecipe>> recipeHolder = getMixableRecipe(level);
        if (recipeHolder.isEmpty()) {
            if (hasContentsToMix()) {
                setMachineStatus(MachineStatus.NO_MATCHING_RECIPE);
            } else {
                setMachineStatus(MachineStatus.IDLE);
            }
            return;
        }
        MixingVatRecipe recipe = recipeHolder.get().value();
        if (recipe.requiresHeat() && !isHeated()) {
            setMachineStatus(MachineStatus.NOT_ENOUGH_HEAT);
            return;
        }
        prepareProgressFor(recipe);
        if (!PsyCurrentMachines.tryUseAutomationCurrentTick(this)) {
            setMachineStatus(MachineStatus.NOT_ENOUGH_ENERGY);
            return;
        }
        if (hasResultFluid() && !promoteResultFluidToInput()) {
            setMachineStatus(MachineStatus.OUTPUT_TANK_FULL);
            return;
        }
        addRecipeWork(recipeHolder.get(), 1.0D);
        if (!isStirAnimationActive()) {
            stirAnimationTicks = STIR_ANIMATION_TICKS;
        }
        setMachineStatus(MachineStatus.RUNNING);
        MachineSync.syncIfDue(this, 10);
    }

    private void prepareProgressFor(MixingVatRecipe recipe) {
        int requiredWork = Math.max(1, recipe.requiredStirs() * 20);
        if (this.maxProgress != requiredWork || this.requiredStirs != recipe.requiredStirs()) {
            this.maxProgress = requiredWork;
            this.requiredStirs = recipe.requiredStirs();
            this.progress = Math.min(this.progress, this.maxProgress);
            updateCurrentStirsFromWork();
        }
    }

    private void addRecipeWork(RecipeHolder<MixingVatRecipe> holder, double work) {
        MixingVatRecipe recipe = holder.value();
        prepareProgressFor(recipe);
        this.fractionalWork += Math.max(0.0D, work);
        int wholeWork = (int) this.fractionalWork;
        if (wholeWork <= 0) {
            return;
        }
        this.fractionalWork -= wholeWork;
        this.progress += wholeWork;
        updateCurrentStirsFromWork();
        if (this.progress >= this.maxProgress) {
            craft(holder);
            resetMixingProgress();
        }
    }

    private void updateCurrentStirsFromWork() {
        this.currentStirs = Math.min(this.requiredStirs, this.progress / 20);
    }

    @Override
    public MachineStatus getMachineStatus() {
        return this.machineStatus;
    }

    private boolean setMachineStatus(MachineStatus status) {
        if (this.machineStatus == status) {
            return false;
        }
        this.machineStatus = status;
        MachineSync.syncIfDue(this, 10);
        return true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        for (int i = 0; i < inputItems.size(); i++) {
            ItemStack stack = inputItems.get(i);
            if (!stack.isEmpty()) {
                output.store("input_item_" + i, ItemStack.CODEC, stack);
            }
        }

        output.putString("input_fluid_1", inputFluid1Id == null ? "" : inputFluid1Id.toString());
        output.putInt("input_fluid_1_amount", inputFluid1Amount);

        output.putString("input_fluid_2", inputFluid2Id == null ? "" : inputFluid2Id.toString());
        output.putInt("input_fluid_2_amount", inputFluid2Amount);

        if (!resultItem.isEmpty()) {
            output.store("result_item", ItemStack.CODEC, resultItem);
        }

        output.putString("result_fluid", resultFluidId == null ? "" : resultFluidId.toString());
        output.putInt("result_fluid_amount", resultFluidAmount);

        output.putInt("progress", progress);
        output.putInt("max_progress", maxProgress);
        output.putDouble("fractional_work", fractionalWork);

        output.putInt("current_stirs", currentStirs);
        output.putInt("required_stirs", requiredStirs);
        output.putInt("stir_animation_ticks", stirAnimationTicks);
        output.putInt("pending_fume_ticks", pendingFumeTicks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        for (int i = 0; i < inputItems.size(); i++) {
            inputItems.set(i, input.read("input_item_" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        }

        String inFluid1 = input.getStringOr("input_fluid_1", "");
        inputFluid1Id = inFluid1.isEmpty() ? null : ResourceLocation.parse(inFluid1);
        inputFluid1Amount = input.getIntOr("input_fluid_1_amount", 0);

        String inFluid2 = input.getStringOr("input_fluid_2", "");
        inputFluid2Id = inFluid2.isEmpty() ? null : ResourceLocation.parse(inFluid2);
        inputFluid2Amount = input.getIntOr("input_fluid_2_amount", 0);

        if (inputFluid1Id == null && inputFluid1Amount == 0 && inputFluid2Id == null && inputFluid2Amount == 0) {
            String oldFluid = input.getStringOr("input_fluid", "");
            inputFluid1Id = oldFluid.isEmpty() ? null : ResourceLocation.parse(oldFluid);
            inputFluid1Amount = input.getIntOr("input_fluid_amount", 0);
        }

        resultItem = input.read("result_item", ItemStack.CODEC).orElse(ItemStack.EMPTY);

        String outFluid = input.getStringOr("result_fluid", "");
        resultFluidId = outFluid.isEmpty() ? null : ResourceLocation.parse(outFluid);
        resultFluidAmount = input.getIntOr("result_fluid_amount", 0);

        progress = input.getIntOr("progress", 0);
        maxProgress = input.getIntOr("max_progress", 100);
        fractionalWork = input.getDoubleOr("fractional_work", 0.0D);

        currentStirs = input.getIntOr("current_stirs", 0);
        requiredStirs = input.getIntOr("required_stirs", 0);
        stirAnimationTicks = input.getIntOr("stir_animation_ticks", 0);
        pendingFumeTicks = input.getIntOr("pending_fume_ticks", 0);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);

        if (level instanceof ServerLevel serverLevel) {
            for (ItemStack stack : inputItems) {
                if (!stack.isEmpty()) {
                    Containers.dropItemStack(serverLevel, pos.getX(), pos.getY(), pos.getZ(), stack);
                }
            }

            if (!resultItem.isEmpty()) {
                Containers.dropItemStack(serverLevel, pos.getX(), pos.getY(), pos.getZ(), resultItem);
            }
        }
    }

    private void removeFromVat(boolean extractingResult, int inputTank, int amount) {
        if (amount <= 0) {
            return;
        }

        if (extractingResult) {
            resultFluidAmount -= amount;
            if (resultFluidAmount <= 0) {
                resultFluidAmount = 0;
                resultFluidId = null;
            }
            return;
        }

        if (inputTank == 1) {
            inputFluid1Amount -= amount;
            if (inputFluid1Amount <= 0) {
                inputFluid1Amount = 0;
                inputFluid1Id = null;
            }
            return;
        }

        if (inputTank == 2) {
            inputFluid2Amount -= amount;
            if (inputFluid2Amount <= 0) {
                inputFluid2Amount = 0;
                inputFluid2Id = null;
            }
        }
    }

    public ResourceHandler<ItemResource> getItemCapability(@Nullable net.minecraft.core.Direction side) {
        return new VatItemResourceHandler();
    }

    public ResourceHandler<FluidResource> getFluidCapability(@Nullable net.minecraft.core.Direction side) {
        return new VatFluidResourceHandler();
    }

    private final class VatItemResourceHandler implements ResourceHandler<ItemResource> {
        private final List<ItemJournal> journals = List.of(
                new ItemJournal(0), new ItemJournal(1), new ItemJournal(2), new ItemJournal(3), new ItemJournal(4)
        );

        @Override
        public int size() {
            return MAX_ITEM_TYPES + 1;
        }

        @Override
        public ItemResource getResource(int slot) {
            Objects.checkIndex(slot, size());
            ItemStack stack = slot < MAX_ITEM_TYPES ? inputItems.get(slot) : resultItem;
            return ItemResource.of(stack);
        }

        @Override
        public long getAmountAsLong(int slot) {
            Objects.checkIndex(slot, size());
            return slot < MAX_ITEM_TYPES ? inputItems.get(slot).getCount() : resultItem.getCount();
        }

        @Override
        public long getCapacityAsLong(int slot, ItemResource resource) {
            Objects.checkIndex(slot, size());
            if (slot >= MAX_ITEM_TYPES) {
                return resource.isEmpty() ? 64 : resource.getMaxStackSize();
            }
            return resource.isEmpty() ? 64 : resource.getMaxStackSize();
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            Objects.checkIndex(slot, size());
            return slot < MAX_ITEM_TYPES && !resource.isEmpty() && !hasBlockingResult();
        }

        @Override
        public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            Objects.checkIndex(slot, size());
            if (!isValid(slot, resource) || amount <= 0) {
                return 0;
            }
            ItemStack existing = inputItems.get(slot);
            if (!existing.isEmpty() && !resource.matches(existing)) {
                return 0;
            }
            int inserted = Math.min(amount, Math.min(resource.getMaxStackSize(), 64) - existing.getCount());
            if (inserted <= 0) {
                return 0;
            }
            journals.get(slot).updateSnapshots(transaction);
            inputItems.set(slot, resource.toStack(existing.getCount() + inserted));
            resetMixingProgress();
            return inserted;
        }

        @Override
        public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            Objects.checkIndex(slot, size());
            if (slot != MAX_ITEM_TYPES || resource.isEmpty() || amount <= 0 || resultItem.isEmpty() || !resource.matches(resultItem)) {
                return 0;
            }
            int extracted = Math.min(amount, resultItem.getCount());
            journals.get(slot).updateSnapshots(transaction);
            resultItem.shrink(extracted);
            if (resultItem.isEmpty()) {
                resultItem = ItemStack.EMPTY;
            }
            resetMixingProgress();
            return extracted;
        }

        private final class ItemJournal extends SnapshotJournal<ItemStack> {
            private final int slot;

            private ItemJournal(int slot) {
                this.slot = slot;
            }

            @Override
            protected ItemStack createSnapshot() {
                return this.slot < MAX_ITEM_TYPES ? inputItems.get(this.slot).copy() : resultItem.copy();
            }

            @Override
            protected void revertToSnapshot(ItemStack snapshot) {
                if (this.slot < MAX_ITEM_TYPES) {
                    inputItems.set(this.slot, snapshot.copy());
                } else {
                    resultItem = snapshot.copy();
                }
            }

            @Override
            protected void onRootCommit(ItemStack originalState) {
                MachineTransferAttachments.markCapabilityChanged(MixingVatBlockEntity.this);
                notifyUpdate();
            }
        }
    }

    private final class VatFluidResourceHandler implements ResourceHandler<FluidResource> {
        private final List<FluidJournal> journals = List.of(new FluidJournal(0), new FluidJournal(1), new FluidJournal(2));

        @Override
        public int size() {
            return 3;
        }

        @Override
        public FluidResource getResource(int slot) {
            Objects.checkIndex(slot, size());
            ResourceLocation id = fluidIdForSlot(slot);
            if (id == null) {
                return FluidResource.EMPTY;
            }
            Fluid fluid = BuiltInRegistries.FLUID.getValue(id);
            return fluid == null || fluid == Fluids.EMPTY ? FluidResource.EMPTY : FluidResource.of(fluid);
        }

        @Override
        public long getAmountAsLong(int slot) {
            Objects.checkIndex(slot, size());
            return switch (slot) {
                case 0 -> inputFluid1Amount;
                case 1 -> inputFluid2Amount;
                case 2 -> resultFluidAmount;
                default -> 0;
            };
        }

        @Override
        public long getCapacityAsLong(int slot, FluidResource resource) {
            Objects.checkIndex(slot, size());
            return FLUID_CAPACITY;
        }

        @Override
        public boolean isValid(int slot, FluidResource resource) {
            Objects.checkIndex(slot, size());
            return slot < 2 && !resource.isEmpty() && !hasBlockingResult();
        }

        @Override
        public int insert(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            Objects.checkIndex(slot, size());
            if (!isValid(slot, resource) || amount <= 0) {
                return 0;
            }
            ResourceLocation incomingId = BuiltInRegistries.FLUID.getKey(resource.getFluid());
            if (incomingId == null) {
                return 0;
            }
            ResourceLocation existingId = fluidIdForSlot(slot);
            int existingAmount = slot == 0 ? inputFluid1Amount : inputFluid2Amount;
            if (existingAmount > 0 && !incomingId.equals(existingId)) {
                return 0;
            }
            int inserted = getSlotInsertableAmount(slot, incomingId, amount);
            if (inserted <= 0) {
                return 0;
            }
            journals.get(slot).updateSnapshots(transaction);
            if (slot == 0) {
                inputFluid1Id = incomingId;
                inputFluid1Amount += inserted;
            } else {
                inputFluid2Id = incomingId;
                inputFluid2Amount += inserted;
            }
            resetMixingProgress();
            return inserted;
        }

        @Override
        public int extract(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            Objects.checkIndex(slot, size());
            if (slot != 2 || resource.isEmpty() || amount <= 0 || resultFluidId == null || resultFluidAmount <= 0) {
                return 0;
            }
            FluidStack stored = fluidStack(resultFluidId, resultFluidAmount);
            if (stored.isEmpty() || !resource.matches(stored)) {
                return 0;
            }
            int extracted = Math.min(amount, resultFluidAmount);
            journals.get(slot).updateSnapshots(transaction);
            resultFluidAmount -= extracted;
            if (resultFluidAmount <= 0) {
                resultFluidAmount = 0;
                resultFluidId = null;
            }
            resetMixingProgress();
            return extracted;
        }

        @Nullable
        private ResourceLocation fluidIdForSlot(int slot) {
            return switch (slot) {
                case 0 -> inputFluid1Amount > 0 ? inputFluid1Id : null;
                case 1 -> inputFluid2Amount > 0 ? inputFluid2Id : null;
                case 2 -> resultFluidAmount > 0 ? resultFluidId : null;
                default -> null;
            };
        }

        private FluidStack fluidStack(ResourceLocation id, int amount) {
            Fluid fluid = BuiltInRegistries.FLUID.getValue(id);
            return fluid == null || fluid == Fluids.EMPTY || amount <= 0 ? FluidStack.EMPTY : new FluidStack(fluid, amount);
        }

        private final class FluidJournal extends SnapshotJournal<FluidSnapshot> {
            private final int slot;

            private FluidJournal(int slot) {
                this.slot = slot;
            }

            @Override
            protected FluidSnapshot createSnapshot() {
                return switch (this.slot) {
                    case 0 -> new FluidSnapshot(inputFluid1Id, inputFluid1Amount);
                    case 1 -> new FluidSnapshot(inputFluid2Id, inputFluid2Amount);
                    default -> new FluidSnapshot(resultFluidId, resultFluidAmount);
                };
            }

            @Override
            protected void revertToSnapshot(FluidSnapshot snapshot) {
                if (this.slot == 0) {
                    inputFluid1Id = snapshot.id();
                    inputFluid1Amount = snapshot.amount();
                } else if (this.slot == 1) {
                    inputFluid2Id = snapshot.id();
                    inputFluid2Amount = snapshot.amount();
                } else {
                    resultFluidId = snapshot.id();
                    resultFluidAmount = snapshot.amount();
                }
            }

            @Override
            protected void onRootCommit(FluidSnapshot originalState) {
                MachineTransferAttachments.markCapabilityChanged(MixingVatBlockEntity.this);
                notifyUpdate();
            }
        }
    }

    private record FluidSnapshot(@Nullable ResourceLocation id, int amount) {
    }
}
