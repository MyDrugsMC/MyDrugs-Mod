package org.mydrugs.mydrugs.effects.addiction.util;


import org.mydrugs.mydrugs.core.drug.AddictionCategoryConfig;
import org.mydrugs.mydrugs.effects.addiction.data.WithdrawalPhase;

public final class AddictionMath {
    private AddictionMath() {
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float mapRange(float value, float inMin, float inMax, float outMin, float outMax) {
        if (inMax - inMin == 0.0F) return outMin;
        float t = clamp((value - inMin) / (inMax - inMin), 0.0F, 1.0F);
        return outMin + (outMax - outMin) * t;
    }

    public static float smoothstep(float x) {
        x = clamp(x, 0.0F, 1.0F);
        return x * x * (3.0F - 2.0F * x);
    }

    public static float computeAddictionGain(float dose, AddictionCategoryConfig cfg, float geneticFactor, float tolerance) {
        return dose * cfg.addictionRate() * geneticFactor * (1.0F - tolerance);
    }

    public static float computeToleranceGain(float dose, AddictionCategoryConfig cfg, float addictionNorm) {
        return dose * cfg.toleranceGainRate() * (0.65F + 0.35F * addictionNorm);
    }

    public static float computeToleranceDecayPerSecond(AddictionCategoryConfig cfg, float resilience, boolean sleeping, boolean inSafeZone) {
        float rate = cfg.toleranceDecayPerSecond();
        rate *= (0.75F + resilience * 0.75F);
        if (sleeping) rate *= 1.50F;
        if (inSafeZone) rate *= 1.15F;
        return rate;
    }

    public static float computeAddictionRecoveryPerSecond(AddictionCategoryConfig cfg, float resilience, boolean inTherapyWindow, boolean inSafeZone) {
        float rate = cfg.addictionRecoveryPerSecond();
        rate *= (0.40F + resilience * 0.80F);
        if (inTherapyWindow) rate *= 1.25F;
        if (inSafeZone) rate *= 1.10F;
        return rate;
    }

    public static WithdrawalPhase getPhase(long abstinence, AddictionCategoryConfig cfg) {
        long onset = cfg.withdrawalOnsetTicks();
        long risingEnd = onset + cfg.risingTicks();
        long peakEnd = risingEnd + cfg.peakTicks();
        long recoveryEnd = peakEnd + cfg.recoveryTicks();

        if (abstinence < onset) return WithdrawalPhase.NONE;
        if (abstinence < risingEnd) return WithdrawalPhase.RISING;
        if (abstinence < peakEnd) return WithdrawalPhase.PEAK;
        if (abstinence < recoveryEnd) return WithdrawalPhase.RECOVERY;
        return WithdrawalPhase.NONE;
    }

    public static float computePhaseFactor(long abstinence, AddictionCategoryConfig cfg) {
        long onset = cfg.withdrawalOnsetTicks();
        long risingEnd = onset + cfg.risingTicks();
        long peakEnd = risingEnd + cfg.peakTicks();
        long recoveryEnd = peakEnd + cfg.recoveryTicks();

        if (abstinence < onset) return 0.0F;

        if (abstinence < risingEnd) {
            float t = (float) (abstinence - onset) / (float) cfg.risingTicks();
            return smoothstep(t);
        }

        if (abstinence < peakEnd) {
            return 1.0F;
        }

        if (abstinence < recoveryEnd) {
            float t = (float) (abstinence - peakEnd) / (float) cfg.recoveryTicks();
            return 1.0F - smoothstep(t);
        }

        return 0.0F;
    }

    public static float computeContextMultiplier(boolean night, float stress, int companions, boolean inSafeZone, boolean inCombat) {
        float nightMultiplier = night ? 1.15F : 1.0F;
        float stressMultiplier = 1.0F + stress * 0.30F;
        float socialMultiplier = 1.0F - Math.min(0.25F, companions * 0.05F);
        float safeZoneMultiplier = inSafeZone ? 0.85F : 1.0F;
        float triggerMultiplier = inCombat ? 1.20F : 1.0F;
        return nightMultiplier * stressMultiplier * socialMultiplier * safeZoneMultiplier * triggerMultiplier;
    }

    public static float computeWithdrawalTarget(float addictionNorm, float phaseFactor, float contextMultiplier, float resilience, AddictionCategoryConfig cfg) {
        float base = 100.0F
                * addictionNorm
                * cfg.withdrawalIntensity()
                * phaseFactor
                * contextMultiplier
                * (1.15F - 0.55F * resilience);

        return clamp(base, 0.0F, 100.0F);
    }

    public static float computeWithdrawalResponseRate(float addictionNorm) {
        return 0.08F + addictionNorm * 0.12F;
    }

    public static float computeWithdrawalRecoveryRate(float resilience, boolean sleeping, boolean inSafeZone, boolean companionsNearby, boolean calmWindow) {
        float rate = 0.20F + resilience * 0.45F;
        if (sleeping) rate *= 2.50F;
        if (inSafeZone) rate *= 1.30F;
        if (companionsNearby) rate *= 1.15F;
        if (calmWindow) rate *= 1.20F;
        return rate;
    }

    public static float computeEffectiveRelief(float baseRelief, float tolerance) {
        float value = baseRelief * (1.0F - tolerance * 0.65F);
        return Math.max(baseRelief * 0.25F, value);
    }

    public static float computeGlobalSeverity(float maxSeverity, float avgSeverity) {
        return clamp(maxSeverity * 0.70F + avgSeverity * 0.30F, 0.0F, 1.0F);
    }

    public static int computeInsomniaDelayTicks(float severity) {
        return (int) (clamp(severity, 0.0F, 1.0F) * 20.0F * 20.0F);
    }
}