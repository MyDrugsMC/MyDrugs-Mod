package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;

public final class CopperDrumBlockEntity extends BlockEntity {
    public static final int CAPACITY_MB = 8000;

    private final StoredFluidTank tank = new StoredFluidTank(CAPACITY_MB, this::sync);
    private final DrumFluidHandler fluidHandler = new DrumFluidHandler();

    public CopperDrumBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COPPER_DRUM.get(), pos, state);
    }

    @Nullable
    public ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        return this.fluidHandler;
    }

    public FluidStack getVisualFluid() {
        return this.tank.getFluid();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.tank.save(output, "tank");
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.tank.load(input, "tank");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private final class DrumFluidHandler implements ResourceHandler<FluidResource> {
        private final DrumJournal journal = new DrumJournal();

        @Override
        public int size() {
            return 1;
        }

        @Override
        public FluidResource getResource(int slot) {
            if (slot != 0 || tank.getFluid().isEmpty()) {
                return FluidResource.EMPTY;
            }
            return FluidResource.of(tank.getFluid().getFluid());
        }

        @Override
        public long getAmountAsLong(int slot) {
            return slot == 0 ? tank.getAmount() : 0;
        }

        @Override
        public long getCapacityAsLong(int slot, FluidResource resource) {
            return slot == 0 ? CAPACITY_MB : 0;
        }

        @Override
        public boolean isValid(int slot, FluidResource resource) {
            return slot == 0 && !resource.isEmpty();
        }

        @Override
        public int insert(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            if (!isValid(slot, resource) || amount <= 0) {
                return 0;
            }
            int addable = tank.getAddableAmount(resource.toStack(amount));
            if (addable <= 0) {
                return 0;
            }
            this.journal.updateSnapshots(transaction);
            FluidStack stored = tank.getFluid();
            if (stored.isEmpty()) {
                tank.setFluidSilent(resource.toStack(addable));
            } else {
                tank.setFluidSilent(stored.copyWithAmount(stored.getAmount() + addable));
            }
            return addable;
        }

        @Override
        public int extract(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            if (slot != 0 || resource.isEmpty() || amount <= 0 || tank.getFluid().isEmpty() || !resource.matches(tank.getFluid())) {
                return 0;
            }
            int extracted = Math.min(amount, tank.getAmount());
            if (extracted <= 0) {
                return 0;
            }
            this.journal.updateSnapshots(transaction);
            int remaining = tank.getAmount() - extracted;
            tank.setFluidSilent(remaining <= 0 ? FluidStack.EMPTY : tank.getFluid().copyWithAmount(remaining));
            return extracted;
        }
    }

    private final class DrumJournal extends SnapshotJournal<FluidStack> {
        @Override
        protected FluidStack createSnapshot() {
            return tank.getFluid().copy();
        }

        @Override
        protected void revertToSnapshot(FluidStack snapshot) {
            tank.setFluidSilent(snapshot);
        }

        @Override
        protected void onRootCommit(FluidStack originalState) {
            tank.markChanged();
            sync();
        }
    }
}
