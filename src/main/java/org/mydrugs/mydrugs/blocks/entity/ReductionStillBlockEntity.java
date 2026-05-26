package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.menu.ReductionStillMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.reduction_still.ReductionStillRecipe;
import org.mydrugs.mydrugs.recipes.reduction_still.ReductionStillRecipeInput;

import java.util.Optional;

public class ReductionStillBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int SLOT_CUTTINGS = 0;
    public static final int SLOT_SOLVENT = 1;
    public static final int SLOT_EXTRACT_OUTPUT = 2;
    public static final int SLOT_PULP_OUTPUT = 3;
    public static final int SLOT_COUNT = 4;

    public enum Status {
        IDLE_NO_CUTTINGS,
        IDLE_NO_SOLVENT,
        IDLE_OUTPUT_BLOCKED,
        IDLE_NEED_MORE_CUTTINGS,
        WORKING
    }

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private int maxProgress = 600;
    private Status status = Status.IDLE_NO_CUTTINGS;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> status.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = Math.max(1, value);
                case 2 -> status = Status.values()[Mth.clamp(value, 0, Status.values().length - 1)];
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public ReductionStillBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REDUCTION_STILL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ReductionStillBlockEntity be) {
        Optional<RecipeHolder<ReductionStillRecipe>> match = be.getRecipe();
        if (match.isEmpty()) {
            be.updateStatusIdle();
            if (be.progress != 0) {
                be.progress = 0;
                be.markDirtyAndSync();
            }
            return;
        }

        ReductionStillRecipe recipe = match.get().value();
        be.maxProgress = recipe.work();

        if (be.items.get(SLOT_CUTTINGS).getCount() < recipe.cuttingsPerBatch()) {
            be.status = Status.IDLE_NEED_MORE_CUTTINGS;
            if (be.progress != 0) {
                be.progress = 0;
            }
            be.markDirtyAndSync();
            return;
        }

        if (!be.canFitResults(recipe)) {
            be.status = Status.IDLE_OUTPUT_BLOCKED;
            be.markDirtyAndSync();
            return;
        }

        be.status = Status.WORKING;
        be.progress = Math.min(be.progress + 1, be.maxProgress);
        if (be.progress >= be.maxProgress) {
            be.craft(recipe);
        }
        if (level.getGameTime() % 4L == 0L) {
            be.markDirtyAndSync();
        }
    }

    private void updateStatusIdle() {
        ItemStack cuttings = items.get(SLOT_CUTTINGS);
        ItemStack solvent = items.get(SLOT_SOLVENT);
        if (cuttings.isEmpty()) {
            status = Status.IDLE_NO_CUTTINGS;
        } else if (solvent.isEmpty() || !isWaterBucket(solvent)) {
            status = Status.IDLE_NO_SOLVENT;
        } else {
            status = Status.IDLE_NO_CUTTINGS;
        }
    }

    private static boolean isWaterBucket(ItemStack stack) {
        return stack.is(Items.WATER_BUCKET);
    }

    private Optional<RecipeHolder<ReductionStillRecipe>> getRecipe() {
        if (!(this.level instanceof ServerLevel serverLevel)) return Optional.empty();
        ItemStack cuttings = items.get(SLOT_CUTTINGS);
        ItemStack solvent = items.get(SLOT_SOLVENT);
        if (cuttings.isEmpty() || solvent.isEmpty()) return Optional.empty();
        return serverLevel.recipeAccess().getRecipeFor(
                ModRecipeTypes.REDUCTION_STILL.get(),
                new ReductionStillRecipeInput(cuttings, solvent),
                serverLevel
        );
    }

    private boolean canFitResults(ReductionStillRecipe recipe) {
        return canAccept(SLOT_EXTRACT_OUTPUT, recipe.extractResult())
                && canAccept(SLOT_PULP_OUTPUT, recipe.pulpResult());
    }

    private boolean canAccept(int slot, ItemStack stack) {
        if (stack.isEmpty()) return true;
        ItemStack existing = items.get(slot);
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, stack)) return false;
        return existing.getCount() + stack.getCount() <= Math.min(existing.getMaxStackSize(), getMaxStackSize());
    }

    private void insertOutput(int slot, ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemStack existing = items.get(slot);
        if (existing.isEmpty()) {
            items.set(slot, stack.copy());
        } else {
            existing.grow(stack.getCount());
        }
    }

    private void craft(ReductionStillRecipe recipe) {
        items.get(SLOT_CUTTINGS).shrink(recipe.cuttingsPerBatch());
        ItemStack solvent = items.get(SLOT_SOLVENT);
        if (isWaterBucket(solvent)) {
            items.set(SLOT_SOLVENT, new ItemStack(Items.BUCKET));
        } else {
            solvent.shrink(1);
        }
        insertOutput(SLOT_EXTRACT_OUTPUT, recipe.extractResult().copy());
        insertOutput(SLOT_PULP_OUTPUT, recipe.pulpResult().copy());
        progress = 0;
        org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(this);
    }

    public ContainerData getData() {
        return data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mydrugs.reduction_still");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ReductionStillMenu(containerId, inventory, this, this.data, ContainerLevelAccess.create(player.level(), this.worldPosition));
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) setChanged();
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < SLOT_COUNT) {
            items.set(slot, stack);
            setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) return false;
        return switch (slot) {
            case SLOT_CUTTINGS -> true;
            case SLOT_SOLVENT -> isWaterBucket(stack);
            default -> false;
        };
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) output.store("item_" + i, ItemStack.CODEC, items.get(i));
        }
        output.putInt("progress", progress);
        output.putInt("max_progress", maxProgress);
        output.putInt("status", status.ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < items.size(); i++) {
            items.set(i, input.read("item_" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        }
        progress = input.getIntOr("progress", 0);
        maxProgress = input.getIntOr("max_progress", 600);
        int statusOrdinal = input.getIntOr("status", 0);
        status = Status.values()[Mth.clamp(statusOrdinal, 0, Status.values().length - 1)];
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null || level.isClientSide()) return;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
    }

    private void markDirtyAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
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
}
