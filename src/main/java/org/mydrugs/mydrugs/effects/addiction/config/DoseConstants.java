package org.mydrugs.mydrugs.effects.addiction.config;

/**
 * Tunables for the dose system. Doses are floating-point "units" — one drug
 * consumption typically adds 1.0 to the target dose of the drug's category.
 */
public final class DoseConstants {
    private DoseConstants() {}

    // --- Alcohol path thresholds ---
    public static final float DRUNK_THRESHOLD = 3.0F;
    public static final float VERY_DRUNK_THRESHOLD = 6.0F;
    public static final float ETHYLIC_COMA_THRESHOLD = 10.0F;

    // --- Drug path thresholds ---
    public static final float HIGH_THRESHOLD = 3.0F;
    public static final float VERY_HIGH_THRESHOLD = 6.0F;
    public static final float OVERDOSE_THRESHOLD = 10.0F;

    // --- Absorption ---
    /** Fallback absorption time when no strategy-specific entry exists. */
    public static final int DEFAULT_ABSORPTION_TICKS = 600; // 30 s

    // --- Decay ---
    /**
     * Dose decays linearly at a constant rate regardless of how high it is:
     * dose=10 drains in 20 min, dose=3 drains in 6 min, etc. 0.5 per minute.
     * 0.5 / (60s * 20 ticks) = 1/2400 per tick.
     */
    public static final float DOSE_DECAY_PER_TICK = 1.0F / 2400.0F;

    // --- Overdose death timer ---
    public static final int OVERDOSE_DEATH_TICKS = 600; // 30 s

    // --- Antidote ---
    public static final float ANTIDOTE_DOSE_REDUCTION = 2.0F;
    public static final int ANTIDOTE_USE_TICKS = 60; // 3 s
}
