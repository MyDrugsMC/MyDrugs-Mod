package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import net.minecraft.util.Mth;

public final class PsyMixerRitualEngine {
    public static final int INPUT_COOLDOWN_TICKS = 5;
    public static final int FEEDBACK_TICKS = 28;
    public static final int GOLDEN_ZONE_COUNT = 3;
    public static final float START_RESONANCE = 0.25F;
    public static final float MIN_INSTABILITY = 0.02F;
    public static final float MAX_INSTABILITY = 0.95F;
    private static final float TWO_PI = (float) (Math.PI * 2.0);
    private static final float ZONE_DRIFT_AMPLITUDE = 0.115F;
    private static final float ZONE_SIZE_AMPLITUDE = 0.045F;
    private static final float ZONE_SIZE_PHASE_OFFSET = (float) (Math.PI / 4.0);
    private static final float ZONE_WIDTH_SCALE = 0.60F;

    private PsyMixerRitualEngine() {
    }

    public static float phase(int progress, int ritualMaxTime) {
        if (ritualMaxTime <= 0) {
            return 0.0F;
        }
        float p = ((float) progress / ritualMaxTime) * 4.0F;
        return p - (float) Math.floor(p);
    }

    public static float targetPhase(PsyMixerRitualFocus focus, int progress, int ritualMaxTime, float motionScale) {
        float wave = oscillator(progress, ritualMaxTime, motionScale);
        return wrapUnit(focus.targetPhase() + (float) Math.sin(wave) * ZONE_DRIFT_AMPLITUDE);
    }

    public static float timingWindow(float baseWindow, int progress, int ritualMaxTime, float sizeScale) {
        if (baseWindow >= 1.0F) {
            return 1.0F;
        }
        float wave = oscillator(progress, ritualMaxTime, sizeScale) + ZONE_SIZE_PHASE_OFFSET;
        float sizeOffset = (float) Math.cos(wave) * ZONE_SIZE_AMPLITUDE;
        return Mth.clamp((baseWindow + sizeOffset) * ZONE_WIDTH_SCALE, 0.035F, 1.0F);
    }

    public static JudgementResult judge(float phase, float targetPhase, float timingWindow, int streak) {
        float distance = nearestGoldenZoneDistance(phase, targetPhase);
        float halfWindow = Math.max(0.015F, timingWindow / 2.0F);
        PsyMixerRitualJudgement judgement;

        if (distance <= halfWindow * 0.22F) {
            judgement = PsyMixerRitualJudgement.PERFECT;
        } else if (distance <= halfWindow * 0.55F) {
            judgement = PsyMixerRitualJudgement.GREAT;
        } else if (distance <= halfWindow) {
            judgement = PsyMixerRitualJudgement.GOOD;
        } else if (distance <= halfWindow * 1.65F) {
            judgement = PsyMixerRitualJudgement.NEAR;
        } else {
            judgement = PsyMixerRitualJudgement.MISS;
        }

        float accuracy = Mth.clamp(1.0F - (distance / halfWindow), 0.0F, 1.0F);
        int streakBonus = judgement.isHit() ? Math.min(6, Math.max(0, streak) / 2) : 0;
        return new JudgementResult(judgement, distance, accuracy, judgement.progressBonus() + streakBonus);
    }

    public static float goldenZonePhase(float firstTargetPhase, int zoneIndex) {
        return wrapUnit(firstTargetPhase + zoneIndex / (float) GOLDEN_ZONE_COUNT);
    }

    public static float nearestGoldenZoneDistance(float phase, float firstTargetPhase) {
        float best = 1.0F;
        for (int i = 0; i < GOLDEN_ZONE_COUNT; i++) {
            best = Math.min(best, wrappedDistance(phase, goldenZonePhase(firstTargetPhase, i)));
        }
        return best;
    }

    private static float oscillator(int progress, int ritualMaxTime, float scale) {
        if (ritualMaxTime <= 0) {
            return 0.0F;
        }
        float normalized = Mth.clamp(progress / (float) ritualMaxTime, 0.0F, 1.0F);
        return normalized * TWO_PI * Math.max(0.05F, scale);
    }

    private static float wrapUnit(float value) {
        return value - (float) Math.floor(value);
    }

    public static float wrappedDistance(float a, float b) {
        float diff = Math.abs(a - b);
        return Math.min(diff, 1.0F - diff);
    }

    public static float applyInstability(float current, PsyMixerRitualJudgement judgement, int streak) {
        float streakRelief = judgement.isHit() ? Math.min(0.015F, Math.max(0, streak) * 0.0025F) : 0.0F;
        return Mth.clamp(current + judgement.instabilityDelta() - streakRelief, MIN_INSTABILITY, MAX_INSTABILITY);
    }

    public static float applyResonance(float current, PsyMixerRitualJudgement judgement, int streak) {
        float streakBonus = judgement.isHit() ? Math.min(0.08F, Math.max(0, streak) * 0.01F) : 0.0F;
        return Mth.clamp(current + judgement.resonanceDelta() + streakBonus, 0.0F, 1.0F);
    }

    public static float finalInstability(float currentInstability, float resonance) {
        return Mth.clamp(currentInstability - resonance * 0.28F, MIN_INSTABILITY, MAX_INSTABILITY);
    }

    public record JudgementResult(
            PsyMixerRitualJudgement judgement,
            float distance,
            float accuracy,
            int progressBonus
    ) {
    }
}
