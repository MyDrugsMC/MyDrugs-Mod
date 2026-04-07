package org.mydrugs.mydrugs.machine.handler;

import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.IntPredicate;

public class RestrictedFluidHandler extends SyncedFluidStacksHandler {
    private final IntPredicate canInsert;
    private final IntPredicate canExtract;

    public RestrictedFluidHandler(
            int tankCount,
            int capacity,
            Runnable onChanged,
            IntPredicate canInsert,
            IntPredicate canExtract
    ) {
        super(tankCount, capacity, onChanged);
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return this.canInsert.test(index) ? super.insert(index, resource, amount, transaction) : 0;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return this.canExtract.test(index) ? super.extract(index, resource, amount, transaction) : 0;
    }
}