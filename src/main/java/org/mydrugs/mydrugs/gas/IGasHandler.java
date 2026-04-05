package org.mydrugs.mydrugs.gas;

public interface IGasHandler {
    int getTanks();

    GasStack getGasInTank(int tank);

    long getTankCapacity(int tank);

    boolean isGasValid(int tank, GasStack stack);

    long fill(GasStack stack, boolean simulate);

    GasStack drain(long amount, boolean simulate);
}