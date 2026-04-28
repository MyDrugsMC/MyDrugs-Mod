package org.mydrugs.mydrugs.pipe;

import com.mojang.serialization.Codec;

public enum PipeTier {
    BASIC(1, 10, 100, 100),
    FAST(4, 10, 500, 500);

    public static final Codec<PipeTier> CODEC = Codec.STRING.xmap(PipeTier::valueOf, PipeTier::name);

    private final int itemAmount;
    private final int itemIntervalTicks;
    private final int fluidAmountPerTick;
    private final long gasAmountPerTick;

    PipeTier(int itemAmount, int itemIntervalTicks, int fluidAmountPerTick, long gasAmountPerTick) {
        this.itemAmount = itemAmount;
        this.itemIntervalTicks = itemIntervalTicks;
        this.fluidAmountPerTick = fluidAmountPerTick;
        this.gasAmountPerTick = gasAmountPerTick;
    }

    public int itemAmount() {
        return this.itemAmount;
    }

    public int itemIntervalTicks() {
        return this.itemIntervalTicks;
    }

    public int fluidAmountPerTick() {
        return this.fluidAmountPerTick;
    }

    public long gasAmountPerTick() {
        return this.gasAmountPerTick;
    }
}
