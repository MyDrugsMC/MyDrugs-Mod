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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatFluidStack;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatRecipe;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatRecipeInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MixingVatBlockEntity extends BlockEntity {
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
        return firstInputFluidAmount();
    }

    public float getVisualFluidRatio() {
        return Math.min(1.0f, getVisualFluidAmount() / (float) FLUID_CAPACITY);
    }

    public boolean hasPendingResult() {
        return !resultItem.isEmpty() || (resultFluidId != null && resultFluidAmount > 0);
    }

    public boolean hasContentsToMix() {
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
        currentStirs = 0;
        requiredStirs = 0;
    }

    public boolean insertOneItem(ItemStack held) {
        if (held.isEmpty() || hasPendingResult()) return false;

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

        return Math.min(requestedAmount, freeSpace);
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

    public boolean tryInsertFluidFromHeld(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty() || hasPendingResult()) return false;

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

    private Optional<RecipeHolder<MixingVatRecipe>> getCurrentRecipe(ServerLevel level) {
        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.MIXING_VAT.get(),
                new MixingVatRecipeInput(currentInputList(), currentFluidList()),
                level
        );
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

    private void craft(MixingVatRecipe recipe) {
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
    }

    public boolean stirOnce() {
        if (level == null || level.isClientSide()) {
            return false;
        }

        if (hasPendingResult()) {
            return false;
        }

        Optional<RecipeHolder<MixingVatRecipe>> recipeHolder = getCurrentRecipe((ServerLevel) level);
        if (recipeHolder.isEmpty()) {
            resetMixingProgress();
            notifyUpdate();
            return false;
        }

        MixingVatRecipe recipe = recipeHolder.get().value();

        if (recipe.requiresHeat() && !isHeated()) {
            return false;
        }

        requiredStirs = recipe.requiredStirs();

        currentStirs++;
        stirAnimationTicks = STIR_ANIMATION_TICKS;

        if (currentStirs >= requiredStirs) {
            craft(recipe);
            currentStirs = 0;
            requiredStirs = 0;
        }

        notifyUpdate();
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

        output.putInt("current_stirs", currentStirs);
        output.putInt("required_stirs", requiredStirs);
        output.putInt("stir_animation_ticks", stirAnimationTicks);
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

        currentStirs = input.getIntOr("current_stirs", 0);
        requiredStirs = input.getIntOr("required_stirs", 0);
        stirAnimationTicks = input.getIntOr("stir_animation_ticks", 0);
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
}