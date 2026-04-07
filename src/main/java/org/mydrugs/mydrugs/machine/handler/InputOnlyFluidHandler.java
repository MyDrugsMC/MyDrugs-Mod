package org.mydrugs.mydrugs.machine.handler;

import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class InputOnlyFluidHandler extends SyncedFluidStacksHandler {
    public InputOnlyFluidHandler(int tankCount, int capacity, Runnable onChanged) {
        super(tankCount, capacity, onChanged);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }
}