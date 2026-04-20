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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.drying.DryingRecipe;

import java.util.Optional;

public final class DryingRackBlockEntity extends BlockEntity {
    public static final int SLOT_COUNT = 4;

    private final NonNullList<ItemStack> stacks = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final int[] progress = new int[SLOT_COUNT];
    private final int[] maxProgress = new int[SLOT_COUNT];

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_RACK.get(), pos, state);
    }

    public ItemStack getStack(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;
        return this.stacks.get(slot);
    }

    public boolean canInsert(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT) return false;
        if (stack.isEmpty()) return false;
        if (!this.stacks.get(slot).isEmpty()) return false;
        return this.getRecipe(stack).isPresent();
    }

    public boolean insert(int slot, ItemStack stack) {
        if (!this.canInsert(slot, stack)) return false;

        this.stacks.set(slot, stack.copy());
        this.progress[slot] = 0;
        this.maxProgress[slot] = 0;
        this.setChanged();
        this.sync();
        return true;
    }

    public ItemStack extract(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return ItemStack.EMPTY;

        ItemStack stack = this.stacks.get(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;

        this.stacks.set(slot, ItemStack.EMPTY);
        this.progress[slot] = 0;
        this.maxProgress[slot] = 0;
        this.setChanged();
        this.sync();
        return stack;
    }

    private static boolean canTransformWholeStack(ItemStack inputStack, DryingRecipe recipe) {
        ItemStack output = recipe.result().copy();
        int resultCount = output.getCount() * inputStack.getCount();
        return resultCount <= output.getMaxStackSize();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity be) {
        boolean dirty = false;
        boolean shouldSync = false;

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
                if (be.progress[slot] != 0 || be.maxProgress[slot] != 0) {
                    be.progress[slot] = 0;
                    be.maxProgress[slot] = 0;
                    dirty = true;
                }
                continue;
            }

            DryingRecipe recipe = match.get().value();

            if (!canTransformWholeStack(stack, recipe)) {
                be.progress[slot] = 0;
                be.maxProgress[slot] = recipe.dryTime();
                dirty = true;
                continue;
            }

            be.maxProgress[slot] = recipe.dryTime();
            be.progress[slot]++;
            dirty = true;

            if (be.progress[slot] >= be.maxProgress[slot]) {
                ItemStack output = recipe.result().copy();
                output.setCount(output.getCount() * stack.getCount());

                be.stacks.set(slot, output);
                be.progress[slot] = 0;
                be.maxProgress[slot] = recipe.dryTime();

                shouldSync = true;
            }
        }

        if (dirty) {
            be.setChanged();
        }

        if (shouldSync) {
            be.sync();
        }
    }

    private Optional<RecipeHolder<DryingRecipe>> getRecipe(ItemStack stack) {
        if (!(this.level instanceof ServerLevel serverLevel) || stack.isEmpty()) {
            return Optional.empty();
        }

        return serverLevel.recipeAccess().getRecipeFor(
                ModRecipeTypes.DRYING.get(),
                new SingleRecipeInput(stack),
                serverLevel
        );
    }

    public void sync() {
        if (this.level == null || this.level.isClientSide()) return;
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        for (int i = 0; i < SLOT_COUNT; i++) {
            this.stacks.set(i, ItemStack.EMPTY);
            this.progress[i] = 0;
            this.maxProgress[i] = 0;
        }

        for (ValueInput child : input.childrenListOrEmpty("items")) {
            int slot = child.getIntOr("slot", -1);
            ItemStack stack = child.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);

            if (slot >= 0 && slot < SLOT_COUNT && !stack.isEmpty()) {
                this.stacks.set(slot, stack);
            }
        }

        int[] loadedProgress = input.getIntArray("progress").orElse(new int[SLOT_COUNT]);
        int[] loadedMax = input.getIntArray("max_progress").orElse(new int[SLOT_COUNT]);

        System.arraycopy(loadedProgress, 0, this.progress, 0, Math.min(loadedProgress.length, SLOT_COUNT));
        System.arraycopy(loadedMax, 0, this.maxProgress, 0, Math.min(loadedMax.length, SLOT_COUNT));
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ValueOutput.ValueOutputList items = output.childrenList("items");

        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = this.stacks.get(i);
            if (stack.isEmpty()) continue;

            ValueOutput child = items.addChild();
            child.putInt("slot", i);
            child.store("stack", ItemStack.CODEC, stack);
        }

        if (items.isEmpty()) {
            output.discard("items");
        }

        output.putIntArray("progress", this.progress);
        output.putIntArray("max_progress", this.maxProgress);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);

        if (this.level == null || this.level.isClientSide()) return;

        for (ItemStack stack : this.stacks) {
            if (!stack.isEmpty()) {
                Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        super.onDataPacket(connection, input);
    }
}