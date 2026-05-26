package org.mydrugs.mydrugs.energy;

/**
 * Constants for Psy Current — the renamed, reworked machine energy. Psy Current (unit "PC") powers
 * machine automation. It is produced by the Distillate Engine, never by burning drugs directly.
 */
public final class PsyCurrentConstants {
    public static final int DEFAULT_MACHINE_CAPACITY = 10_000;
    /** Psy Current a powered machine consumes per tick. Kept at 1 to preserve existing balance. */
    public static final int DEFAULT_MACHINE_CURRENT_PER_TICK = 1;

    // --- Distillate Engine ---
    public static final int ENGINE_CURRENT_CAPACITY = 100_000;
    public static final int ENGINE_MIN_RADIUS = 1;
    public static final int ENGINE_MAX_RADIUS = 8;
    public static final int ENGINE_TARGET_RESCAN_INTERVAL_TICKS = 100;
    public static final int ENGINE_OVERLOAD_COOLDOWN_TICKS = 600;
    public static final int ENGINE_OVERLOAD_COOL_INTERVAL_TICKS = 20;
    public static final int ENGINE_OVERLOAD_COOL_AMOUNT = 3;
    public static final int ENGINE_MAX_STRAIN = 100;
    public static final int ENGINE_STRAIN_TICK_INTERVAL = 20;
    public static final int ENGINE_IDLE_STRAIN_DECAY_INTERVAL_TICKS = 80;
    public static final int ENGINE_IDLE_STRAIN_DECAY = 1;
    public static final int ENGINE_STRAIN_VENT_DECAY = 3;
    public static final int ENGINE_CURRENT_REGULATOR_STRAIN_REDUCTION = 1;
    public static final int ENGINE_AREA_PREVIEW_TICKS = 20 * 30;

    // --- Distillate fuel table ---
    public static final int LUCID_EXTRACT_CURRENT = 12_000;
    public static final int LUCID_EXTRACT_DURATION_TICKS = 600;
    public static final int LUCID_EXTRACT_STRAIN_ON_START = 0;
    public static final int LUCID_EXTRACT_STRAIN_PER_SECOND = 0;

    public static final int CALMING_RESIN_CURRENT = 8_000;
    public static final int CALMING_RESIN_DURATION_TICKS = 500;
    public static final int CALMING_RESIN_STRAIN_ON_START = -18;
    public static final int CALMING_RESIN_STRAIN_PER_SECOND = -1;

    public static final int REDLINE_FUEL_CURRENT = 24_000;
    public static final int REDLINE_FUEL_DURATION_TICKS = 600;
    public static final int REDLINE_FUEL_STRAIN_ON_START = 12;
    public static final int REDLINE_FUEL_STRAIN_PER_SECOND = 4;

    public static final int OVERDRIVE_FUEL_CURRENT = 48_000;
    public static final int OVERDRIVE_FUEL_DURATION_TICKS = 800;
    public static final int OVERDRIVE_FUEL_STRAIN_ON_START = 24;
    public static final int OVERDRIVE_FUEL_STRAIN_PER_SECOND = 7;

    public static final int UNSTABLE_ESSENCE_CURRENT = 18_000;
    public static final int UNSTABLE_ESSENCE_DURATION_TICKS = 400;
    public static final int UNSTABLE_ESSENCE_STRAIN_ON_START = 20;
    public static final int UNSTABLE_ESSENCE_STRAIN_PER_SECOND = 5;

    public static final int DREAM_RESIDUE_DEBUG_FUEL_CURRENT = 6_000;
    public static final int DREAM_RESIDUE_DEBUG_FUEL_DURATION_TICKS = 300;
    public static final int DREAM_RESIDUE_DEBUG_FUEL_STRAIN_ON_START = 25;
    public static final int DREAM_RESIDUE_DEBUG_FUEL_STRAIN_PER_SECOND = 6;

    public static final int MYCELIAL_INSIGHT_DEBUG_FUEL_CURRENT = 6_000;
    public static final int MYCELIAL_INSIGHT_DEBUG_FUEL_DURATION_TICKS = 400;
    public static final int MYCELIAL_INSIGHT_DEBUG_FUEL_STRAIN_ON_START = -12;
    public static final int MYCELIAL_INSIGHT_DEBUG_FUEL_STRAIN_PER_SECOND = -1;

    public static final int UNSTABLE_PULSE_STEP_TICKS = 10;
    public static final int[] UNSTABLE_PULSE_WEIGHTS = {0, 2, 1, 3, 0, 4, 1, 5};

    private PsyCurrentConstants() {
    }
}
