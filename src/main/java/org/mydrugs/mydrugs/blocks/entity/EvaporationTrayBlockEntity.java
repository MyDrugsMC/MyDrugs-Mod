package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.evaporation_tray.EvaporationTrayRecipe;
import org.mydrugs.mydrugs.recipes.evaporation_tray.EvaporationTrayRecipeInput;

import java.util.Optional;

public class EvaporationTrayBlockEntity extends BlockEntity {
    public static final int FLUID_CAPACITY = 4000;

    @Nullable
    private ResourceLocation inputFluidId = null;
    private int inputFluidAmount = 0;

    private ItemStack resultItem = ItemStack.EMPTY;

    private int progress = 0;
    private int maxProgress = 200;

    public EvaporationTrayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EVAPORATION_TRAY.get(), pos, state);
    }

    @Nullable
    public ResourceLocation getVisualFluidId() {
        return inputFluidId;
    }

    public int getVisualFluidAmount() {
        return inputFluidAmount;
    }

    public float getVisualFluidRatio() {
        return Math.min(1.0f, inputFluidAmount / (float) FLUID_CAPACITY);
    }

    public ItemStack getVisualItem() {
        return resultItem.copy();
    }

    public boolean hasResult() {
        return !resultItem.isEmpty();
    }

    private void resetProgress() {
        progress = 0;
        maxProgress = 200;
    }

    private void notifyUpdate() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private int getInsertableAmount(ResourceLocation incomingId, int requestedAmount) {
        if (requestedAmount <= 0) {
            return 0;
        }

        if (hasResult()) {
            return 0;
        }

        int freeSpace = FLUID_CAPACITY - inputFluidAmount;
        if (freeSpace <= 0) {
            return 0;
        }

        if (inputFluidAmount > 0 && !incomingId.equals(inputFluidId)) {
            return 0;
        }

        return Math.min(requestedAmount, freeSpace);
    }

    private void addInputFluid(ResourceLocation incomingId, int amount) {
        if (amount <= 0) {
            return;
        }

        if (inputFluidAmount <= 0 || inputFluidId == null) {
            inputFluidId = incomingId;
            inputFluidAmount = amount;
            return;
        }

        if (incomingId.equals(inputFluidId)) {
            inputFluidAmount += amount;
        }
    }

    public boolean tryInsertFluidFromHeld(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty() || level == null || hasResult()) {
            return false;
        }

        ItemAccess access = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        var handler = access.getCapability(Capabilities.Fluid.ITEM);

        if (handler == null || handler.size() <= 0) {
            return false;
        }

        FluidResource resource = handler.getResource(0);
        int containedAmount = handler.getAmountAsInt(0);

        if (resource.isEmpty() || containedAmount <= 0) {
            return false;
        }

        ResourceLocation incomingId = BuiltInRegistries.FLUID.getKey(resource.getFluid());
        if (incomingId == null) {
            return false;
        }

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

        resetProgress();
        notifyUpdate();
        return true;
    }

    public boolean tryTakeResult(Player player) {
        if (resultItem.isEmpty()) {
             return false;
        }

        if (level == null || level.isClientSide()) {
            return true;
        }

        ItemStack toGive = resultItem.copy();
        resultItem = ItemStack.EMPTY;

        if (!player.addItem(toGive)) {
            Containers.dropItemStack(level,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5,
                    toGive);
        }

        resetProgress();
        notifyUpdate();
        return true;
    }

    private boolean canAcceptOutput(ItemStack stack) {
        if (resultItem.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameComponents(resultItem, stack)) {
            return false;
        }

        return resultItem.getCount() + stack.getCount() <= resultItem.getMaxStackSize();
    }

    private Optional<RecipeHolder<EvaporationTrayRecipe>> getCurrentRecipe(ServerLevel serverLevel) {
        if (inputFluidId == null || inputFluidAmount <= 0) {
            return Optional.empty();
        }

        return serverLevel.recipeAccess().getRecipeFor(
                ModRecipeTypes.EVAPORATION_TRAY.get(),
                new EvaporationTrayRecipeInput(inputFluidId, inputFluidAmount),
                serverLevel
        );
    }

    private void consumeInputFluid(int amount) {
        inputFluidAmount -= amount;
        if (inputFluidAmount <= 0) {
            inputFluidAmount = 0;
            inputFluidId = null;
        }
    }

    private void craft(EvaporationTrayRecipe recipe) {
        consumeInputFluid(recipe.inputAmount());

        ItemStack result = recipe.result().copy();
        if (resultItem.isEmpty()) {
            resultItem = result;
        } else {
            resultItem.grow(result.getCount());
        }

        resetProgress();
        notifyUpdate();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EvaporationTrayBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (be.hasResult()) {
            return;
        }

        if (be.inputFluidId == null || be.inputFluidAmount <= 0) {
            if (be.progress != 0) {
                be.resetProgress();
                be.notifyUpdate();
            }
            return;
        }

        Optional<RecipeHolder<EvaporationTrayRecipe>> recipeHolder = be.getCurrentRecipe(serverLevel);
        if (recipeHolder.isEmpty()) {
            if (be.progress != 0) {
                be.resetProgress();
                be.notifyUpdate();
            }
            return;
        }

        EvaporationTrayRecipe recipe = recipeHolder.get().value();
        if (!be.canAcceptOutput(recipe.result())) {
            if (be.progress != 0) {
                be.resetProgress();
                be.notifyUpdate();
            }
            return;
        }

        be.maxProgress = recipe.processingTime();
        be.progress++;

        if (be.progress >= be.maxProgress) {
            be.craft(recipe);
        } else {
            be.setChanged();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putString("input_fluid", inputFluidId == null ? "" : inputFluidId.toString());
        output.putInt("input_fluid_amount", inputFluidAmount);

        if (!resultItem.isEmpty()) {
            output.store("result_item", ItemStack.CODEC, resultItem);
        }

        output.putInt("progress", progress);
        output.putInt("max_progress", maxProgress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        String fluid = input.getStringOr("input_fluid", "");
        inputFluidId = fluid.isEmpty() ? null : ResourceLocation.parse(fluid);
        inputFluidAmount = input.getIntOr("input_fluid_amount", 0);

        resultItem = input.read("result_item", ItemStack.CODEC).orElse(ItemStack.EMPTY);

        progress = input.getIntOr("progress", 0);
        maxProgress = input.getIntOr("max_progress", 200);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);

        if (level instanceof ServerLevel serverLevel && !resultItem.isEmpty()) {
            Containers.dropItemStack(serverLevel,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    resultItem.copy());
        }
    }
}