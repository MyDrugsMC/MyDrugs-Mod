package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.mydrugs.mydrugs.blocks.FluidPumpBlock;
import org.mydrugs.mydrugs.blocks.FluidPumpLoggedFluid;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;

public class FluidPumpBlockEntity extends BlockEntity {
    private static final int MAX_MANUAL_CREDIT = 5000;

    private int manualCredit;
    private final CreditJournal creditJournal = new CreditJournal();
    private final PumpFluidHandler fluidHandler = new PumpFluidHandler();

    public FluidPumpBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PUMP.get(), pos, state);
    }

    /**
     * Manual crank behavior:
     * - First tries to push directly into the block above.
     * - If the block above refuses insertion, like the current pipe no-op handler,
     *   stores temporary manual credit so pipes can pull it on their own tick.
     *
     * This prevents infinite passive pumping while still allowing manual pumping.
     */
    public int pumpOnce(ServerLevel level, Direction outputDirection, int amount) {
        if (outputDirection != Direction.UP || amount <= 0 || !this.hasCrank() || this.sourceResource().isEmpty()) {
            return 0;
        }

        int directlyPushed = this.tryPushUp(level, amount);
        int remaining = amount - directlyPushed;

        if (remaining <= 0) {
            return directlyPushed;
        }

        int credited = this.addManualCredit(remaining);
        return directlyPushed + credited;
    }

    /**
     * Expose a fluid capability upward so pipes can pull only manually-created credit.
     * This is NOT infinite anymore.
     */
    public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
        return side == Direction.UP ? this.fluidHandler : null;
    }

    private int tryPushUp(ServerLevel level, int amount) {
        ResourceHandler<FluidResource> target = level.getCapability(
                Capabilities.Fluid.BLOCK,
                this.worldPosition.above(),
                Direction.DOWN
        );

        if (target == null) {
            return 0;
        }

        FluidResource resource = this.sourceResource();
        if (resource.isEmpty()) {
            return 0;
        }

        int remaining = amount;
        int insertedTotal = 0;

        for (int slot = 0; slot < target.size() && remaining > 0; slot++) {
            try (Transaction tx = Transaction.openRoot()) {
                int inserted = target.insert(slot, resource, remaining, tx);
                if (inserted > 0) {
                    tx.commit();
                    insertedTotal += inserted;
                    remaining -= inserted;
                }
            }
        }

        return insertedTotal;
    }

    private int addManualCredit(int amount) {
        if (amount <= 0 || this.sourceResource().isEmpty()) {
            return 0;
        }

        int accepted = Math.min(amount, MAX_MANUAL_CREDIT - this.manualCredit);
        if (accepted <= 0) {
            return 0;
        }

        this.manualCredit += accepted;
        this.markUpdated();
        return accepted;
    }

    private boolean hasCrank() {
        return this.getBlockState().getValue(FluidPumpBlock.CRANK);
    }

    private FluidResource sourceResource() {
        FluidPumpLoggedFluid logged = this.getBlockState().getValue(FluidPumpBlock.LOGGED_FLUID);
        return logged == FluidPumpLoggedFluid.EMPTY ? FluidResource.EMPTY : FluidResource.of(logged.sourceFluid());
    }

    private void markUpdated() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("manual_credit", this.manualCredit);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.manualCredit = Math.max(0, input.getIntOr("manual_credit", 0));
    }

    private final class PumpFluidHandler implements ResourceHandler<FluidResource> {
        @Override
        public int size() {
            return 1;
        }

        @Override
        public FluidResource getResource(int slot) {
            if (slot != 0 || manualCredit <= 0 || sourceResource().isEmpty()) {
                return FluidResource.EMPTY;
            }
            return sourceResource();
        }

        @Override
        public long getAmountAsLong(int slot) {
            if (slot != 0 || sourceResource().isEmpty()) {
                return 0;
            }
            return manualCredit;
        }

        @Override
        public long getCapacityAsLong(int slot, FluidResource resource) {
            if (slot != 0 || resource.isEmpty() || !resource.equals(sourceResource())) {
                return 0;
            }
            return MAX_MANUAL_CREDIT;
        }

        @Override
        public boolean isValid(int slot, FluidResource resource) {
            return slot == 0 && !resource.isEmpty() && resource.equals(sourceResource());
        }

        @Override
        public int insert(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public int extract(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            if (slot != 0 || amount <= 0 || !resource.equals(sourceResource()) || manualCredit <= 0) {
                return 0;
            }

            int extracted = Math.min(amount, manualCredit);
            if (extracted <= 0) {
                return 0;
            }

            creditJournal.updateSnapshots(transaction);
            manualCredit -= extracted;
            return extracted;
        }
    }

    private final class CreditJournal extends SnapshotJournal<Integer> {
        @Override
        protected Integer createSnapshot() {
            return manualCredit;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            manualCredit = snapshot;
        }

        @Override
        protected void onRootCommit(Integer originalState) {
            markUpdated();
        }
    }
}