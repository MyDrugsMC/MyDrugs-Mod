package org.mydrugs.mydrugs.gas;

public final class CompositeGasHandler implements IGasHandler {
    private final GasTank[] tanks;

    public CompositeGasHandler(GasTank... tanks) {
        this.tanks = tanks;
    }

    @Override
    public int getTanks() {
        return tanks.length;
    }

    @Override
    public GasStack getGasInTank(int tank) {
        if (tank < 0 || tank >= tanks.length) return GasStack.EMPTY;
        return tanks[tank].getGasInTank(0);
    }

    @Override
    public long getTankCapacity(int tank) {
        if (tank < 0 || tank >= tanks.length) return 0;
        return tanks[tank].getTankCapacity(0);
    }

    @Override
    public boolean isGasValid(int tank, GasStack stack) {
        if (tank < 0 || tank >= tanks.length) return false;
        return tanks[tank].isGasValid(0, stack);
    }

    @Override
    public long fill(GasStack resource, boolean simulate) {
        long remaining = resource.amount();
        for (GasTank tank : tanks) {
            if (remaining <= 0) break;
            long inserted = tank.fill(resource.withAmount(remaining), simulate);
            remaining -= inserted;
        }
        return resource.amount() - remaining;
    }

    @Override
    public long fill(int tank, GasStack resource, boolean simulate) {
        if (tank < 0 || tank >= tanks.length) return 0;
        return tanks[tank].fill(0, resource, simulate);
    }

    @Override
    public GasStack drain(long amount, boolean simulate) {
        for (GasTank tank : tanks) {
            GasStack drained = tank.drain(amount, simulate);
            if (!drained.isEmpty()) return drained;
        }
        return GasStack.EMPTY;
    }

    @Override
    public GasStack drain(int tank, long amount, boolean simulate) {
        if (tank < 0 || tank >= tanks.length) return GasStack.EMPTY;
        return tanks[tank].drain(0, amount, simulate);
    }
}
