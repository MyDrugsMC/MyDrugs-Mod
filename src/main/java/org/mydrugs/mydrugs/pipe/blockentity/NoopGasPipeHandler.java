package org.mydrugs.mydrugs.pipe.blockentity;

import org.mydrugs.mydrugs.gas.GasStack;
import org.mydrugs.mydrugs.gas.IGasHandler;

final class NoopGasPipeHandler implements IGasHandler {
    static final NoopGasPipeHandler INSTANCE = new NoopGasPipeHandler();

    private NoopGasPipeHandler() {
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public GasStack getGasInTank(int tank) {
        return GasStack.EMPTY;
    }

    @Override
    public long getTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isGasValid(int tank, GasStack stack) {
        return false;
    }

    @Override
    public long fill(GasStack stack, boolean simulate) {
        return 0;
    }

    @Override
    public long fill(int tank, GasStack stack, boolean simulate) {
        return 0;
    }

    @Override
    public GasStack drain(long amount, boolean simulate) {
        return GasStack.EMPTY;
    }

    @Override
    public GasStack drain(int tank, long amount, boolean simulate) {
        return GasStack.EMPTY;
    }
}
