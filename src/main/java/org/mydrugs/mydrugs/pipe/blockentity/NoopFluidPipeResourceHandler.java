package org.mydrugs.mydrugs.pipe.blockentity;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

final class NoopFluidPipeResourceHandler implements ResourceHandler<FluidResource> {
    static final NoopFluidPipeResourceHandler INSTANCE = new NoopFluidPipeResourceHandler();

    private NoopFluidPipeResourceHandler() {
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int slot) {
        return FluidResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int slot) {
        return 0;
    }

    @Override
    public long getCapacityAsLong(int slot, FluidResource resource) {
        return 0;
    }

    @Override
    public boolean isValid(int slot, FluidResource resource) {
        return false;
    }

    @Override
    public int insert(int slot, FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int slot, FluidResource resource, int amount, TransactionContext transaction) {
        return 0;
    }
}
