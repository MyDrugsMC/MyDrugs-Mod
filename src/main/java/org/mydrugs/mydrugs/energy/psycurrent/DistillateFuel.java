package org.mydrugs.mydrugs.energy.psycurrent;

import net.minecraft.world.item.Item;
import org.mydrugs.mydrugs.energy.PsyCurrentConstants;

public record DistillateFuel(
        Item item,
        DistillateFuelType type,
        int totalCurrent,
        int durationTicks,
        int strainOnStart,
        int strainPerSecond,
        boolean pulsing
) {
    public DistillateFuel {
        totalCurrent = Math.max(0, totalCurrent);
        durationTicks = Math.max(1, durationTicks);
    }

    public int outputForTick(int elapsedTicks) {
        int base = Math.max(1, this.totalCurrent / this.durationTicks);
        if (!this.pulsing) {
            return base;
        }

        int step = Math.floorMod(elapsedTicks / PsyCurrentConstants.UNSTABLE_PULSE_STEP_TICKS,
                PsyCurrentConstants.UNSTABLE_PULSE_WEIGHTS.length);
        return base * PsyCurrentConstants.UNSTABLE_PULSE_WEIGHTS[step];
    }
}
