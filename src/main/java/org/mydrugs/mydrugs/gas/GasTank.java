package org.mydrugs.mydrugs.gas;

import java.util.Objects;
import java.util.function.Predicate;

public class GasTank implements IGasHandler {
    private final long capacity;
    private final Predicate<GasType> validator;
    private final Runnable onChanged;

    private GasStack stored = GasStack.EMPTY;

    public GasTank(long capacity, Predicate<GasType> validator, Runnable onChanged) {
        this.capacity = capacity;
        this.validator = Objects.requireNonNull(validator);
        this.onChanged = Objects.requireNonNull(onChanged);
    }

    public GasType getGasType() {
        return stored.type();
    }

    public long getAmount() {
        return stored.amount();
    }

    public boolean isEmpty() {
        return stored.isEmpty();
    }

    public void loadStored(GasType type, long amount) {
        long clamped = Math.max(0, Math.min(capacity, amount));
        this.stored = GasStack.of(type, clamped);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public GasStack getGasInTank(int tank) {
        if (tank != 0) {
            return GasStack.EMPTY;
        }
        return stored;
    }

    @Override
    public long getTankCapacity(int tank) {
        if (tank != 0) {
            return 0;
        }
        return capacity;
    }

    @Override
    public boolean isGasValid(int tank, GasStack stack) {
        if (tank != 0 || stack == null || stack.isEmpty()) {
            return false;
        }
        return validator.test(stack.type());
    }

    @Override
    public long fill(GasStack resource, boolean simulate) {
        if (resource == null || resource.isEmpty()) {
            return 0;
        }
        if (!validator.test(resource.type())) {
            return 0;
        }

        if (stored.isEmpty()) {
            long inserted = Math.min(capacity, resource.amount());
            if (!simulate && inserted > 0) {
                stored = GasStack.of(resource.type(), inserted);
                onChanged.run();
            }
            return inserted;
        }

        if (!stored.sameGas(resource)) {
            return 0;
        }

        long space = capacity - stored.amount();
        if (space <= 0) {
            return 0;
        }

        long inserted = Math.min(space, resource.amount());
        if (!simulate && inserted > 0) {
            stored = stored.withAmount(stored.amount() + inserted);
            onChanged.run();
        }
        return inserted;
    }

    @Override
    public long fill(int tank, GasStack stack, boolean simulate) {
        return tank == 0 ? fill(stack, simulate) : 0;
    }

    @Override
    public GasStack drain(long amount, boolean simulate) {
        if (amount <= 0 || stored.isEmpty()) {
            return GasStack.EMPTY;
        }

        long extracted = Math.min(amount, stored.amount());
        GasStack drained = stored.withAmount(extracted);

        if (!simulate) {
            long remaining = stored.amount() - extracted;
            stored = remaining <= 0 ? GasStack.EMPTY : stored.withAmount(remaining);
            onChanged.run();
        }

        return drained;
    }

    @Override
    public GasStack drain(int tank, long amount, boolean simulate) {
        return tank == 0 ? drain(amount, simulate) : GasStack.EMPTY;
    }
}
