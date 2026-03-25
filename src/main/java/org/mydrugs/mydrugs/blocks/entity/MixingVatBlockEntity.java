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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatRecipe;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatRecipeInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MixingVatBlockEntity extends BlockEntity {
    public static final int MAX_ITEM_TYPES = 4;
    public static final int FLUID_CAPACITY = 4000;

    private final NonNullList<ItemStack> inputItems = NonNullList.withSize(MAX_ITEM_TYPES, ItemStack.EMPTY);

    @Nullable
    private ResourceLocation inputFluidId = null;
    private int inputFluidAmount = 0;

    private ItemStack resultItem = ItemStack.EMPTY;

    @Nullable
    private ResourceLocation resultFluidId = null;
    private int resultFluidAmount = 0;

    private int progress = 0;
    private int maxProgress = 100;

    public static final int STIR_ANIMATION_TICKS = 8;

    private int currentStirs = 0;
    private int requiredStirs = 0;
    private int stirAnimationTicks = 0;

    public MixingVatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIXING_VAT.get(), pos, state);
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
    public ResourceLocation getVisualFluidId() {
        if (resultFluidId != null && resultFluidAmount > 0) {
            return resultFluidId;
        }
        return inputFluidAmount > 0 ? inputFluidId : null;
    }

    public int getVisualFluidAmount() {
        if (resultFluidId != null && resultFluidAmount > 0) {
            return resultFluidAmount;
        }
        return inputFluidAmount;
    }

    public float getVisualFluidRatio() {
        return Math.min(1.0f, getVisualFluidAmount() / (float) FLUID_CAPACITY);
    }

    public boolean hasPendingResult() {
        return !resultItem.isEmpty() || (resultFluidId != null && resultFluidAmount > 0);
    }

    public boolean hasContentsToMix() {
        if (inputFluidAmount > 0) return true;

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

    public boolean tryInsertFluidFromHeld(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty() || hasPendingResult()) return false;

        // Creative-mode special case for your custom bottle:
        // fill the vat, but do not drain the held bottle.
        if (player.getAbilities().instabuild && held.getItem() instanceof GlassBottleItem) {
            ResourceLocation incomingId = GlassBottleItem.getStoredFluidId(held);
            int containedAmount = GlassBottleItem.getStoredAmount(held);

            if (incomingId == null || containedAmount <= 0) {
                return false;
            }

            if (inputFluidAmount > 0 && !incomingId.equals(inputFluidId)) {
                return false;
            }

            int freeSpace = FLUID_CAPACITY - inputFluidAmount;
            if (freeSpace <= 0) {
                return false;
            }

            int moved = Math.min(containedAmount, freeSpace);
            if (moved <= 0) {
                return false;
            }

            inputFluidId = incomingId;
            inputFluidAmount += moved;

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

        if (inputFluidAmount > 0 && !incomingId.equals(inputFluidId)) {
            return false;
        }

        int freeSpace = FLUID_CAPACITY - inputFluidAmount;
        if (freeSpace <= 0) {
            return false;
        }

        int requested = Math.min(containedAmount, freeSpace);

        try (var tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, requested, tx);
            if (extracted <= 0) {
                return false;
            }

            tx.commit();
            inputFluidId = incomingId;
            inputFluidAmount += extracted;
        }

        resetMixingProgress();
        notifyUpdate();
        return true;
    }

    public boolean tryExtractFluidToHeld(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty()) return false;

        ResourceLocation sourceId;
        int sourceAmount;
        boolean extractingResult = false;

        if (resultFluidId != null && resultFluidAmount > 0) {
            sourceId = resultFluidId;
            sourceAmount = resultFluidAmount;
            extractingResult = true;
        } else if (inputFluidId != null && inputFluidAmount > 0) {
            sourceId = inputFluidId;
            sourceAmount = inputFluidAmount;
        } else {
            return false;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(sourceId);
        if (fluid == null || fluid == Fluids.EMPTY) return false;

        // Creative-mode special case for your custom bottle:
        // keep the original bottle in hand, give a filled copy instead.
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

            removeFromVat(extractingResult, moved);
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

        removeFromVat(extractingResult, transferred);
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

    private Optional<RecipeHolder<MixingVatRecipe>> getCurrentRecipe(ServerLevel level) {
        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.MIXING_VAT.get(),
                new MixingVatRecipeInput(currentInputList(), inputFluidId, inputFluidAmount),
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

    private void craft(MixingVatRecipe recipe) {
        for (var ingredient : recipe.requiredItems()) {
            consumeOneMatchingItem(ingredient);
        }

        recipe.fluidInput().ifPresent(required -> {
            inputFluidAmount -= required.amount();
            if (inputFluidAmount <= 0) {
                inputFluidAmount = 0;
                inputFluidId = null;
            }
        });

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

    public static void tick(Level level, BlockPos pos, BlockState state, MixingVatBlockEntity be) {
        if (be.stirAnimationTicks > 0) {
            be.stirAnimationTicks--;
            if (!level.isClientSide()) {
                be.setChanged();
            }
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

        output.putString("input_fluid", inputFluidId == null ? "" : inputFluidId.toString());
        output.putInt("input_fluid_amount", inputFluidAmount);

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

        String inFluid = input.getStringOr("input_fluid", "");
        inputFluidId = inFluid.isEmpty() ? null : ResourceLocation.parse(inFluid);
        inputFluidAmount = input.getIntOr("input_fluid_amount", 0);

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

    private void removeFromVat(boolean extractingResult, int amount) {
        if (extractingResult) {
            resultFluidAmount -= amount;
            if (resultFluidAmount <= 0) {
                resultFluidAmount = 0;
                resultFluidId = null;
            }
        } else {
            inputFluidAmount -= amount;
            if (inputFluidAmount <= 0) {
                inputFluidAmount = 0;
                inputFluidId = null;
            }
        }
    }
}