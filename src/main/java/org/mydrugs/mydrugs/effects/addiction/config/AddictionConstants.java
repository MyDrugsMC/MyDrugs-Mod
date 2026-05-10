package org.mydrugs.mydrugs.effects.addiction.config;

public final class AddictionConstants {
    private AddictionConstants() {}

    // --- Safe Zone ---
    public static final int SAFE_ZONE_RADIUS = 8;
    public static final float SAFE_ZONE_RECOVERY_THRESHOLD = 0.40F;
    public static final long SAFE_ZONE_RECOVERY_INTERVAL_TICKS = 200L;

    // --- Combat / Social detection ---
    public static final int COMBAT_DETECTION_TICKS = 200;
    public static final double COMPANION_DETECTION_RADIUS = 12.0D;

    // --- Stress targets (StressManager) ---
    public static final float STRESS_BASELINE = 0.10F;
    public static final float STRESS_SEVERITY_SCALE = 0.35F;
    public static final float STRESS_NIGHT_BONUS = 0.03F;
    public static final float STRESS_COMBAT_BONUS = 0.12F;
    public static final float STRESS_ALONE_BONUS = 0.02F;
    public static final float STRESS_SAFE_ZONE_REDUCTION = 0.08F;
    public static final float STRESS_DIARY_REDUCTION = 0.10F;
    public static final float STRESS_HEADPHONES_REDUCTION = 0.10F;
    public static final float STRESS_CANNABIS_REDUCTION = 0.06F;
    public static final float STRESS_HIGH_DOSE_BONUS = 0.04F;
    public static final float STRESS_BAD_TRIP_DOSE_BONUS = 0.22F;
    public static final float STRESS_OVERDOSE_DOSE_BONUS = 0.35F;
    public static final float STRESS_RISE_RATE = 0.003F;
    public static final float STRESS_FALL_RATE = 0.006F;
    public static final float STRESS_HEADPHONES_EXTRA_FALL_RATE = 0.004F;
    public static final float STRESS_CANNABIS_EXTRA_FALL_RATE = 0.002F;
    public static final float STRESS_DAMAGE_BONUS_PER_HP = 0.01F;
    public static final float STRESS_DAMAGE_FLAT_BONUS = 0.08F;
    public static final float STRESS_PSY_MIXER_FAILURE_SPIKE_SCALE = 0.08F;

    // --- Bad trip state (BadTripManager) ---
    public static final float BAD_TRIP_THRESHOLD_AT_VERY_HIGH = 0.30F;
    public static final float BAD_TRIP_THRESHOLD_AT_OVERDOSE = 0.01F;
    public static final float BAD_TRIP_STOP_HYSTERESIS = 0.05F;
    public static final float BAD_TRIP_STRESS_PRESSURE_RANGE = 0.30F;
    public static final float BAD_TRIP_STRESS_PRESSURE_WEIGHT = 0.60F;
    public static final float BAD_TRIP_DOSE_PRESSURE_WEIGHT = 0.40F;
    public static final float BAD_TRIP_STRONG_THRESHOLD = 0.35F;
    public static final float BAD_TRIP_VIOLENT_THRESHOLD = 0.70F;
    public static final int BAD_TRIP_SYMPTOM_REROLL_TICKS = 100;
    public static final float BAD_TRIP_SYMPTOM_INTENSITY_MIN = 0.80F;
    public static final float BAD_TRIP_SYMPTOM_INTENSITY_MAX = 1.00F;
    public static final int BAD_TRIP_EFFECT_REFRESH_TICKS = 20;
    public static final int BAD_TRIP_EFFECT_DURATION_TICKS = 45;

    // --- Stress damage (StressDamageManager) ---
    public static final float STRESS_DAMAGE_START_THRESHOLD = 0.90F;
    public static final float STRESS_DAMAGE_END_THRESHOLD = 1.00F;
    public static final float STRESS_MAX_DAMAGE_PER_SECOND = 0.50F;

    // --- Drug use stress relief ---
    public static final float STRESS_RELIEF_ON_CONSUME = 0.03F;

    // --- Recovery item relief ---
    public static final float RELIEF_DIARY = 0.18F;
    public static final float RELIEF_HERBAL_TEA = 0.12F;
    public static final float RELIEF_CALMING_MIXTURE = 0.18F;
    public static final float RELIEF_SLEEPING_AID = 0.10F;
    public static final float RELIEF_ON_WAKE_UP = 0.12F;
    public static final float RELIEF_THERAPIST = 0.15F;
}
