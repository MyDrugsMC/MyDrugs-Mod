package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;

import java.util.ArrayList;
import java.util.List;

/**
 * Bridges {@link StoredFluidTank} (the mod's internal fluid storage) to NeoForge's
 * {@link ResourceHandler<FluidResource>} capability so external pipes can interact with it.
 */
public final class StoredFluidTankResourceHandler implements ResourceHandler<FluidResource> {
    private final StoredFluidTank[] tanks;
    private final BlockEntity blockEntity;
    private final List<TankJournal> journals;

    public StoredFluidTankResourceHandler(BlockEntity blockEntity, StoredFluidTank... tanks) {
        this.tanks = tanks;
        this.blockEntity = blockEntity;
        this.journals = new ArrayList<>(tanks.length);
        for (StoredFluidTank tank : tanks) {
            this.journals.add(new TankJournal(tank));
        }
    }

    @Override
    public int size() {
        return tanks.length;
    }

    @Override
    public FluidResource getResource(int slot) {
        FluidStack stored = tanks[slot].getFluid();
        return stored.isEmpty() ? FluidResource.EMPTY : FluidResource.of(stored.getFluid());
    }

    @Override
    public long getAmountAsLong(int slot) {
        return tanks[slot].getAmount();
    }

    @Override
    public long getCapacityAsLong(int slot, FluidResource resource) {
        return tanks[slot].capacity();
    }

    @Override
    public boolean isValid(int slot, FluidResource resource) {
        return !resource.isEmpty();
    }

    @Override
    public int insert(int slot, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount <= 0) return 0;

        StoredFluidTank tank = tanks[slot];
        FluidStack incoming = resource.toStack(amount);
        int addable = tank.getAddableAmount(incoming);
        if (addable <= 0) return 0;

        journals.get(slot).updateSnapshots(transaction);
        FluidStack stored = tank.getFluid();
        if (stored.isEmpty()) {
            tank.setFluidSilent(incoming.copyWithAmount(addable));
        } else {
            tank.setFluidSilent(stored.copyWithAmount(stored.getAmount() + addable));
        }
        return addable;
    }

    @Override
    public int extract(int slot, FluidResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount <= 0) return 0;

        StoredFluidTank tank = tanks[slot];
        FluidStack stored = tank.getFluid();
        if (stored.isEmpty() || !resource.matches(stored)) return 0;

        int extracted = Math.min(amount, stored.getAmount());
        if (extracted <= 0) return 0;

        journals.get(slot).updateSnapshots(transaction);
        int remaining = stored.getAmount() - extracted;
        tank.setFluidSilent(remaining <= 0 ? FluidStack.EMPTY : stored.copyWithAmount(remaining));
        return extracted;
    }

    private final class TankJournal extends SnapshotJournal<FluidStack> {
        private final StoredFluidTank tank;

        TankJournal(StoredFluidTank tank) {
            this.tank = tank;
        }

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
            MachineTransferAttachments.markCapabilityChanged(blockEntity);
        }
    }
}
