package org.mydrugs.mydrugs.machine.handler;

import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class OutputOnlyFluidHandler extends SyncedFluidStacksHandler {
    public OutputOnlyFluidHandler(int tankCount, int capacity, Runnable onChanged) {
        super(tankCount, capacity, onChanged);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }
}