package org.mydrugs.mydrugs.core.drug.dose;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.addiction.config.DoseConstants;

/**
 * Maps a {@link ConsumptionStrategy} to how long (in ticks) the active dose
 * takes to rise from 0 to the consumed target dose. Keeps the ConsumptionStrategy
 * interface (external library) untouched — adjust values here or override per-item.
 */
public final class AbsorptionTimes {
    private AbsorptionTimes() {}

    public static int forStrategy(@Nullable ConsumptionStrategy strategy) {
        if (strategy == null) return DoseConstants.DEFAULT_ABSORPTION_TICKS;

        return switch (strategy.getClass().getSimpleName()) {
            case "InjectingStrategy" -> 40;   // 2 s  — near-instant
            case "SniffingStrategy"  -> 100;  // 5 s
            case "SmokingStrategy"   -> 200;  // 10 s
            case "EatingStrategy"    -> 900;  // 45 s — slowest
            default -> DoseConstants.DEFAULT_ABSORPTION_TICKS;
        };
    }
}
