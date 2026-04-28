package org.mydrugs.mydrugs.pipe.network;

import org.mydrugs.mydrugs.pipe.PipeTier;

public record NetworkThroughputBudget(int itemAmount, int itemIntervalTicks, int fluidPerTick, long gasPerTick) {
    public static NetworkThroughputBudget from(PipeTier tier) {
        return new NetworkThroughputBudget(
                tier.itemAmount(),
                tier.itemIntervalTicks(),
                tier.fluidAmountPerTick(),
                tier.gasAmountPerTick()
        );
    }
}
