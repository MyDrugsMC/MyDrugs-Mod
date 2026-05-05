package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.drying.DryingRecipe;

import java.util.Optional;

public class CoffeeDryingMatBlockEntity extends BlockEntity {
    public static final int SLOT_COUNT = 9;
    private final NonNullList<ItemStack> stacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final int[] progress = new int[SLOT_COUNT];
    private final int[] maxProgress = new int[SLOT_COUNT];

    public CoffeeDryingMatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COFFEE_DRYING_MAT.get(), pos, state);
    }

    public ItemStack getStack(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? stacks.get(slot) : ItemStack.EMPTY;
    }

    public boolean canInsert(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT || stack.isEmpty() || !stacks.get(slot).isEmpty()) return false;
        if (this.level == null || this.level.isClientSide()) {
            return stack.is(ModItems.WET_COFFEE_BEAN.get());
        }
        return getRecipe(stack).isPresent();
    }

    public boolean insert(int slot, ItemStack stack) {
        if (!canInsert(slot, stack)) return false;
        stacks.set(slot, stack.copyWithCount(1));
        progress[slot] = 0;
        maxProgress[slot] = 0;
        markDirtyAndSync();
        return true;
    }

    public ItemStack extract(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;
        ItemStack stack = stacks.get(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        stacks.set(slot, ItemStack.EMPTY);
        progress[slot] = 0;
        maxProgress[slot] = 0;
        markDirtyAndSync();
        return stack;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CoffeeDryingMatBlockEntity be) {
        boolean dirty = false;
        boolean sync = false;
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = be.stacks.get(slot);
            if (stack.isEmpty()) {
                if (be.progress[slot] != 0 || be.maxProgress[slot] != 0) {
                    be.progress[slot] = 0;
                    be.maxProgress[slot] = 0;
                    dirty = true;
                }
                continue;
            }
            Optional<RecipeHolder<DryingRecipe>> match = be.getRecipe(stack);
            if (match.isEmpty()) {
                be.progress[slot] = 0;
                be.maxProgress[slot] = 0;
                dirty = true;
                continue;
            }
            DryingRecipe recipe = match.get().value();
            be.maxProgress[slot] = recipe.dryTime();
            be.progress[slot]++;
            dirty = true;
            if (be.progress[slot] >= be.maxProgress[slot]) {
                be.stacks.set(slot, recipe.result().copy());
                be.progress[slot] = 0;
                sync = true;
                org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(be);
            }
        }
        if (dirty) be.setChanged();
        if (sync || (dirty && level.getGameTime() % 40L == 0L)) be.sync();
    }

    private Optional<RecipeHolder<DryingRecipe>> getRecipe(ItemStack stack) {
        if (!(this.level instanceof ServerLevel serverLevel) || stack.isEmpty()) return Optional.empty();
        return serverLevel.recipeAccess().getRecipeFor(ModRecipeTypes.DRYING.get(), new SingleRecipeInput(stack), serverLevel);
    }

    private void sync() {
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    private void markDirtyAndSync() {
        setChanged();
        sync();
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < SLOT_COUNT; i++) {
            stacks.set(i, input.read("stack_" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        }
        int[] loadedProgress = input.getIntArray("progress").orElse(new int[SLOT_COUNT]);
        int[] loadedMax = input.getIntArray("max_progress").orElse(new int[SLOT_COUNT]);
        System.arraycopy(loadedProgress, 0, progress, 0, Math.min(loadedProgress.length, SLOT_COUNT));
        System.arraycopy(loadedMax, 0, maxProgress, 0, Math.min(loadedMax.length, SLOT_COUNT));
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!stacks.get(i).isEmpty()) output.store("stack_" + i, ItemStack.CODEC, stacks.get(i));
        }
        output.putIntArray("progress", progress);
        output.putIntArray("max_progress", maxProgress);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null || level.isClientSide()) return;
        for (ItemStack stack : stacks) if (!stack.isEmpty()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries) { return this.saveWithoutMetadata(registries); }
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override
    public void onDataPacket(Connection connection, ValueInput input) { super.onDataPacket(connection, input); }
}
