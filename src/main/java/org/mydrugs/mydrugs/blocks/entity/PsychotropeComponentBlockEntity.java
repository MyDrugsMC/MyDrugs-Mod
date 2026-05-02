package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.core.drug.DrugHolder;
import org.mydrugs.mydrugs.fluids.FluidTypesEx;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;

public final class PsychotropeComponentBlockEntity extends BlockEntity {
    public static final int ITEM_CAPACITY = 64;
    public static final int FLUID_CAPACITY = 1000;

    private final StoredFluidTank fluidTank = new StoredFluidTank(FLUID_CAPACITY, this::sync, stack -> !stack.isEmpty() && FluidTypesEx.getDrugModel(stack.getFluid()) != null);
    private final ResourceHandler<ItemResource> itemHandler = new ComponentItemHandler();
    private final ResourceHandler<FluidResource> fluidHandler = new ComponentFluidHandler();
    private ItemStack item = ItemStack.EMPTY;
    private long lastInputChangedTick;

    public PsychotropeComponentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PSYCHOTROPE_COMPONENT.get(), pos, state);
    }

    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        return this.itemHandler;
    }

    public ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        return this.fluidHandler;
    }

    public ItemStack getStoredItem() {
        return this.item;
    }

    public void setStoredItem(ItemStack stack) {
        this.item = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(Math.min(stack.getCount(), ITEM_CAPACITY));
        markInputChanged();
        sync();
    }

    public StoredFluidTank fluidTank() {
        return this.fluidTank;
    }

    public long lastInputChangedTick() {
        return this.lastInputChangedTick;
    }

    public boolean hasDrugInput() {
        return isDrugItem(this.item) || FluidTypesEx.getDrugModel(this.fluidTank.getFluid().getFluid()) != null;
    }

    public static boolean isDrugItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof DrugHolder;
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private void markInputChanged() {
        this.lastInputChangedTick = this.level == null ? this.lastInputChangedTick + 1L : this.level.getGameTime();
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.item = input.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.fluidTank.load(input, "fluid");
        this.lastInputChangedTick = input.getLongOr("last_input_changed_tick", 0L);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.item.isEmpty()) {
            output.store("item", ItemStack.CODEC, this.item);
        }
        this.fluidTank.save(output, "fluid");
        output.putLong("last_input_changed_tick", this.lastInputChangedTick);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        super.onDataPacket(connection, input);
    }

    private final class ComponentItemHandler implements ResourceHandler<ItemResource> {
        private final SnapshotJournal<ItemStack> journal = new SnapshotJournal<>() {
            @Override
            protected ItemStack createSnapshot() {
                return item.copy();
            }

            @Override
            protected void revertToSnapshot(ItemStack snapshot) {
                item = snapshot.copy();
            }

            @Override
            protected void onRootCommit(ItemStack originalState) {
                markInputChanged();
                sync();
            }
        };

        @Override
        public int size() {
            return 1;
        }

        @Override
        public ItemResource getResource(int slot) {
            return slot == 0 ? ItemResource.of(item) : ItemResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int slot) {
            return slot == 0 ? item.getCount() : 0;
        }

        @Override
        public long getCapacityAsLong(int slot, ItemResource resource) {
            return slot == 0 && isValid(slot, resource) ? Math.min(ITEM_CAPACITY, resource.getMaxStackSize()) : 0;
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return slot == 0 && !resource.isEmpty() && isDrugItem(resource.toStack(1));
        }

        @Override
        public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (!isValid(slot, resource)) {
                return 0;
            }
            if (!item.isEmpty() && !resource.matches(item)) {
                return 0;
            }
            int current = item.getCount();
            int capacity = (int) getCapacityAsLong(slot, resource);
            int inserted = Math.min(amount, capacity - current);
            if (inserted <= 0) {
                return 0;
            }
            this.journal.updateSnapshots(transaction);
            item = resource.toStack(current + inserted);
            return inserted;
        }

        @Override
        public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }

    private final class ComponentFluidHandler implements ResourceHandler<FluidResource> {
        private final SnapshotJournal<FluidStack> journal = new SnapshotJournal<>() {
            @Override
            protected FluidStack createSnapshot() {
                return fluidTank.getFluid();
            }

            @Override
            protected void revertToSnapshot(FluidStack snapshot) {
                fluidTank.setFluidSilent(snapshot);
            }

            @Override
            protected void onRootCommit(FluidStack originalState) {
                markInputChanged();
                fluidTank.markChanged();
            }
        };

        @Override
        public int size() {
            return 1;
        }

        @Override
        public FluidResource getResource(int slot) {
            FluidStack stored = fluidTank.getFluid();
            return slot == 0 && !stored.isEmpty() ? FluidResource.of(stored.getFluid()) : FluidResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int slot) {
            return slot == 0 ? fluidTank.getAmount() : 0;
        }

        @Override
        public long getCapacityAsLong(int slot, FluidResource resource) {
            return slot == 0 && isValid(slot, resource) ? fluidTank.capacity() : 0;
        }

        @Override
        public boolean isValid(int slot, FluidResource resource) {
            return slot == 0 && !resource.isEmpty() && FluidTypesEx.getDrugModel(resource.getFluid()) != null;
        }

        @Override
        public int insert(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
            if (!isValid(slot, resource)) {
                return 0;
            }

            FluidStack incoming = resource.toStack(amount);
            int inserted = fluidTank.getAddableAmount(incoming);
            if (inserted <= 0) {
                return 0;
            }

            this.journal.updateSnapshots(transaction);
            FluidStack stored = fluidTank.getFluid();
            if (stored.isEmpty()) {
                fluidTank.setFluidSilent(incoming.copyWithAmount(inserted));
            } else {
                fluidTank.setFluidSilent(stored.copyWithAmount(stored.getAmount() + inserted));
            }
            return inserted;
        }

        @Override
        public int extract(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }
}
