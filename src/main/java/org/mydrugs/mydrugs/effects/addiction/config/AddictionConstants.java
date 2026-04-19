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
    public static final float STRESS_BASE = 0.15F;
    public static final float STRESS_SEVERITY_SCALE = 0.75F;
    public static final float STRESS_NIGHT_BONUS = 0.05F;
    public static final float STRESS_COMBAT_BONUS = 0.15F;
    public static final float STRESS_ALONE_BONUS = 0.08F;
    public static final float STRESS_SAFE_ZONE_REDUCTION = 0.08F;
    public static final float STRESS_DIARY_REDUCTION = 0.10F;
    public static final float STRESS_HEADPHONES_REDUCTION = 0.08F;
    public static final float STRESS_LERP_RATE = 0.08F;
    public static final float STRESS_DAMAGE_BONUS_PER_HP = 0.01F;
    public static final float STRESS_DAMAGE_FLAT_BONUS = 0.08F;

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
