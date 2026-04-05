package org.mydrugs.mydrugs.gas;

public final class GasStack {
    public static final GasStack EMPTY = new GasStack(null, 0);

    private final GasType type;
    private final long amount;

    private GasStack(GasType type, long amount) {
        this.type = type;
        this.amount = amount;
    }

    public static GasStack of(GasType type, long amount) {
        if (type == null || amount <= 0) {
            return EMPTY;
        }
        return new GasStack(type, amount);
    }

    public GasType type() {
        return type;
    }

    public long amount() {
        return amount;
    }

    public boolean isEmpty() {
        return type == null || amount <= 0;
    }

    public boolean is(GasType other) {
        return !isEmpty() && type.id().equals(other.id());
    }

    public boolean sameGas(GasStack other) {
        return !this.isEmpty()
                && !other.isEmpty()
                && this.type.id().equals(other.type.id());
    }

    public GasStack withAmount(long newAmount) {
        return GasStack.of(type, newAmount);
    }
}