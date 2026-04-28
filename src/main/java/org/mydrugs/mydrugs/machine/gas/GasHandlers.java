package org.mydrugs.mydrugs.machine.gas;

import org.mydrugs.mydrugs.gas.GasStack;
import org.mydrugs.mydrugs.gas.IGasHandler;

public final class GasHandlers {
    private GasHandlers() {
    }

    public static IGasHandler inputOnly(IGasHandler delegate) {
        return new IGasHandler() {
            @Override
            public int getTanks() {
                return delegate.getTanks();
            }

            @Override
            public GasStack getGasInTank(int tank) {
                return delegate.getGasInTank(tank);
            }

            @Override
            public long getTankCapacity(int tank) {
                return delegate.getTankCapacity(tank);
            }

            @Override
            public boolean isGasValid(int tank, GasStack stack) {
                return delegate.isGasValid(tank, stack);
            }

            @Override
            public long fill(GasStack stack, boolean simulate) {
                return delegate.fill(stack, simulate);
            }

            @Override
            public long fill(int tank, GasStack stack, boolean simulate) {
                return delegate.fill(tank, stack, simulate);
            }

            @Override
            public GasStack drain(long amount, boolean simulate) {
                return GasStack.EMPTY;
            }

            @Override
            public GasStack drain(int tank, long amount, boolean simulate) {
                return GasStack.EMPTY;
            }
        };
    }

    public static IGasHandler outputOnly(IGasHandler delegate) {
        return new IGasHandler() {
            @Override
            public int getTanks() {
                return delegate.getTanks();
            }

            @Override
            public GasStack getGasInTank(int tank) {
                return delegate.getGasInTank(tank);
            }

            @Override
            public long getTankCapacity(int tank) {
                return delegate.getTankCapacity(tank);
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
                return delegate.drain(amount, simulate);
            }

            @Override
            public GasStack drain(int tank, long amount, boolean simulate) {
                return delegate.drain(tank, amount, simulate);
            }
        };
    }
}
