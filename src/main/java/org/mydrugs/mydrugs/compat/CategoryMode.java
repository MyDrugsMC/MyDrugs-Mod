package org.mydrugs.mydrugs.compat;

public enum CategoryMode {
    NORMAL(1, 1),
    LARGE(3, 2);

    private final int mul;
    private final int div;

    CategoryMode(int mul, int div) {
        this.mul = mul;
        this.div = div;
    }

    public int scale(int value) {
        return value * mul / div;
    }

    public boolean isLarge() {
        return this == LARGE;
    }
}