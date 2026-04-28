package org.mydrugs.mydrugs.gas;

public interface IGasHandler {
    int getTanks();

    GasStack getGasInTank(int tank);

    long getTankCapacity(int tank);

    boolean isGasValid(int tank, GasStack stack);

    long fill(GasStack stack, boolean simulate);

    GasStack drain(long amount, boolean simulate);

    default long fill(int tank, GasStack stack, boolean simulate) {
        if (tank != 0) {
            return 0;
        }
        return fill(stack, simulate);
    }

    default GasStack drain(int tank, long amount, boolean simulate) {
        if (tank != 0) {
            return GasStack.EMPTY;
        }
        return drain(amount, simulate);
    }
}
