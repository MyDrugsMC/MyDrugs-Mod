package org.mydrugs.mydrugs.core.drug.runtime;

import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.strategy.RouteEffectProfile;

public final class ActiveDrugEffect {
    private static final float BENEFICIAL_SECOND_DOSE_BOOST = 0.35F;
    private static final float BENEFICIAL_LATER_DOSE_BOOST = 0.12F;
    private static final float BENEFICIAL_VISIBLE_CAP = 1.35F;
    private static final float HARMFUL_SECOND_DOSE_BOOST = 0.70F;
    private static final float HARMFUL_LATER_DOSE_BOOST = 0.45F;
    private static final float HARMFUL_VISIBLE_CAP = 2.25F;
    private static final float MAX_RISK_PRESSURE = 8.0F;

    private final EffectType type;
    private float intensity;
    private float riskPressure;
    private int remainingTicks;
    private int ageTicks;
    private int baselineDurationTicks;
    private int fadeTicksRemaining;
    private int fadeDurationTicks;
    private RouteEffectProfile profile;
    private int overlappingDoses;

    public ActiveDrugEffect(EffectType type, float intensity, int remainingTicks) {
        this(type, intensity, remainingTicks, RouteEffectProfile.immediate());
    }

    public ActiveDrugEffect(EffectType type, float intensity, int remainingTicks, RouteEffectProfile profile) {
        this.type = type;
        this.intensity = Math.max(0.0F, intensity);
        this.riskPressure = this.intensity;
        this.remainingTicks = Math.max(0, remainingTicks);
        this.baselineDurationTicks = this.remainingTicks;
        this.fadeDurationTicks = fadeDurationFor(this.remainingTicks);
        this.profile = profile == null ? RouteEffectProfile.immediate() : profile;
        this.overlappingDoses = this.remainingTicks > 0 ? 1 : 0;
    }

    public EffectType type() {
        return type;
    }

    public float intensity() {
        return this.intensity * activePhaseScale() * fadeScale();
    }

    public float baseIntensity() {
        return intensity;
    }

    public float riskPressure() {
        return riskPressure;
    }

    public int remainingTicks() {
        return remainingTicks + fadeTicksRemaining;
    }

    /** Raw active-phase ticks remaining, excluding the fade tail. Used for persistence. */
    public int activeTicks() {
        return remainingTicks;
    }

    public int ageTicks() {
        return ageTicks;
    }

    public int baselineDurationTicks() {
        return baselineDurationTicks;
    }

    public int fadeTicksRemaining() {
        return fadeTicksRemaining;
    }

    public int fadeDurationTicks() {
        return fadeDurationTicks;
    }

    public RouteEffectProfile profile() {
        return profile;
    }

    public int overlappingDoses() {
        return overlappingDoses;
    }

    public boolean isFading() {
        return this.remainingTicks <= 0 && this.fadeTicksRemaining > 0;
    }

    public Phase phase() {
        if (isFading()) {
            return Phase.FADING;
        }
        if (!profile.hasPhaseCurve() || remainingTicks <= 0) {
            return Phase.PEAK;
        }

        int total = activeDurationTotal();
        int onset = Math.min(profile.onsetTicks(), Math.max(0, total - 1));
        if (onset > 0 && ageTicks < onset) {
            return Phase.ONSET;
        }

        int comedownStart = comedownStart(total, onset);
        if (ageTicks >= comedownStart) {
            return Phase.COMEDOWN;
        }
        return Phase.PEAK;
    }

    public MergeResult merge(float incomingIntensity, int incomingDuration) {
        return merge(incomingIntensity, incomingDuration, RouteEffectProfile.immediate(), false, 1.0F);
    }

    public MergeResult merge(float incomingIntensity, int incomingDuration, RouteEffectProfile incomingProfile,
                             boolean harmful, float riskMultiplier) {
        float clampedIncoming = Math.max(0.0F, incomingIntensity);
        int duration = Math.max(0, incomingDuration);
        float before = this.intensity();
        int previousActive = this.remainingTicks;
        boolean overlapping = previousActive > 0 || this.fadeTicksRemaining > 0;
        if (!overlapping) {
            this.ageTicks = 0;
            this.overlappingDoses = 1;
            this.intensity = clampedIncoming;
            this.riskPressure = clampedIncoming * Math.max(0.0F, riskMultiplier);
        } else {
            this.overlappingDoses = Math.max(1, this.overlappingDoses + 1);
            float boostFactor = stackingBoost(harmful, this.overlappingDoses);
            float visibleCap = Math.max(this.intensity, clampedIncoming * (harmful ? HARMFUL_VISIBLE_CAP : BENEFICIAL_VISIBLE_CAP));
            this.intensity = Math.min(visibleCap, Math.max(this.intensity, clampedIncoming) + clampedIncoming * boostFactor);
            this.riskPressure = Math.min(MAX_RISK_PRESSURE,
                    this.riskPressure + clampedIncoming * Math.max(0.0F, riskMultiplier) * riskBoost(harmful, this.overlappingDoses));
        }

        if (incomingProfile != null && incomingProfile.hasPhaseCurve()) {
            this.profile = incomingProfile;
        }

        if (duration > 0) {
            this.baselineDurationTicks = Math.max(this.baselineDurationTicks, duration);
            int refreshBonus = overlapping ? Math.max(20, duration / 5) : 0;
            int ceiling = Math.max(duration, this.baselineDurationTicks + this.baselineDurationTicks / 2);
            this.remainingTicks = Math.min(ceiling, Math.max(this.remainingTicks, duration) + refreshBonus);
            this.fadeDurationTicks = fadeDurationFor(this.remainingTicks);
            this.fadeTicksRemaining = 0;
        } else if (this.isFading()) {
            this.remainingTicks = 0;
            this.fadeTicksRemaining = 0;
        }

        return new MergeResult(
                overlapping,
                this.overlappingDoses >= 3,
                this.remainingTicks > previousActive,
                before,
                this.intensity(),
                this.riskPressure,
                this.overlappingDoses
        );
    }

    public TickResult tick() {
        Phase before = phase();
        boolean expired = false;
        boolean fadeStarted = false;

        if (remainingTicks > 0) {
            ageTicks++;
            remainingTicks--;
            if (remainingTicks <= 0) {
                fadeTicksRemaining = fadeDurationTicks;
                fadeStarted = fadeTicksRemaining > 0;
            }
        } else if (fadeTicksRemaining > 0) {
            fadeTicksRemaining--;
        }

        if (remainingTicks <= 0 && fadeTicksRemaining <= 0) {
            expired = true;
        }
        Phase after = phase();
        return new TickResult(expired, before, after, fadeStarted);
    }

    /**
     * Drains the active phase of this effect by `extraTicks`, beyond the normal 1-tick decay.
     * Respects fade behavior and never goes negative.
     */
    public void drain(int extraTicks) {
        if (extraTicks <= 0) return;
        if (remainingTicks > 0) {
            remainingTicks = Math.max(0, remainingTicks - extraTicks);
            if (remainingTicks == 0 && fadeTicksRemaining <= 0) {
                fadeTicksRemaining = fadeDurationTicks;
            }
        } else if (fadeTicksRemaining > 0) {
            fadeTicksRemaining = Math.max(0, fadeTicksRemaining - extraTicks);
        }
    }

    public ActiveDrugEffect copy() {
        ActiveDrugEffect copy = new ActiveDrugEffect(type, intensity, remainingTicks, profile);
        copy.riskPressure = this.riskPressure;
        copy.ageTicks = this.ageTicks;
        copy.baselineDurationTicks = this.baselineDurationTicks;
        copy.fadeTicksRemaining = this.fadeTicksRemaining;
        copy.fadeDurationTicks = this.fadeDurationTicks;
        copy.overlappingDoses = this.overlappingDoses;
        return copy;
    }

    /**
     * Rebuilds an effect from persisted state. All inputs are clamped/validated so corrupt or
     * out-of-range save data cannot produce a poisoned runtime entry.
     */
    public static ActiveDrugEffect restore(EffectType type, float baseIntensity, int activeTicks,
                                            int fadeTicksRemaining, int fadeDurationTicks) {
        return restore(type, baseIntensity, activeTicks, 0, Math.max(activeTicks, 0),
                fadeTicksRemaining, fadeDurationTicks, RouteEffectProfile.immediate(), baseIntensity, 1);
    }

    public static ActiveDrugEffect restore(EffectType type, float baseIntensity, int activeTicks, int ageTicks,
                                            int baselineDurationTicks, int fadeTicksRemaining, int fadeDurationTicks,
                                            RouteEffectProfile profile, float riskPressure, int overlappingDoses) {
        ActiveDrugEffect effect = new ActiveDrugEffect(type, baseIntensity, Math.max(0, activeTicks), profile);
        effect.ageTicks = Math.max(0, ageTicks);
        effect.baselineDurationTicks = Math.max(effect.remainingTicks, baselineDurationTicks);
        effect.fadeDurationTicks = Math.max(0, fadeDurationTicks);
        effect.fadeTicksRemaining = Math.clamp(fadeTicksRemaining, 0, effect.fadeDurationTicks);
        effect.riskPressure = Math.clamp(riskPressure, 0.0F, MAX_RISK_PRESSURE);
        effect.overlappingDoses = Math.max(effect.remainingTicks > 0 ? 1 : 0, overlappingDoses);
        return effect;
    }

    /** True when the effect has no active ticks and no fade tail left, i.e. it should not be restored. */
    public boolean isExpired() {
        return remainingTicks <= 0 && fadeTicksRemaining <= 0;
    }

    private float activePhaseScale() {
        if (!profile.hasPhaseCurve() || remainingTicks <= 0) {
            return 1.0F;
        }

        int total = activeDurationTotal();
        int onset = Math.min(profile.onsetTicks(), Math.max(0, total - 1));
        if (onset > 0 && ageTicks < onset) {
            return Math.clamp(ageTicks / (float) onset, 0.0F, 1.0F);
        }

        int comedownStart = comedownStart(total, onset);
        if (ageTicks >= comedownStart && total > comedownStart) {
            return Math.clamp((total - ageTicks) / (float) (total - comedownStart), 0.0F, 1.0F);
        }

        return 1.0F;
    }

    private float fadeScale() {
        if (this.remainingTicks > 0 || this.fadeDurationTicks <= 0) {
            return 1.0F;
        }
        return Math.clamp(this.fadeTicksRemaining / (float) this.fadeDurationTicks, 0.0F, 1.0F);
    }

    private int activeDurationTotal() {
        return Math.max(1, Math.max(baselineDurationTicks, ageTicks + remainingTicks));
    }

    private int comedownStart(int total, int onset) {
        int remainingAfterOnset = Math.max(0, total - onset);
        int comedown = Math.min(profile.comedownTicks(), remainingAfterOnset);
        int peak = Math.min(profile.peakTicks(), Math.max(0, remainingAfterOnset - comedown));
        return Math.clamp(Math.max(onset + peak, total - comedown), onset, total);
    }

    private static float stackingBoost(boolean harmful, int overlappingDoses) {
        if (overlappingDoses <= 1) {
            return 1.0F;
        }
        if (overlappingDoses == 2) {
            return harmful ? HARMFUL_SECOND_DOSE_BOOST : BENEFICIAL_SECOND_DOSE_BOOST;
        }
        return harmful ? HARMFUL_LATER_DOSE_BOOST : BENEFICIAL_LATER_DOSE_BOOST;
    }

    private static float riskBoost(boolean harmful, int overlappingDoses) {
        if (overlappingDoses <= 1) {
            return 0.5F;
        }
        return harmful ? 1.45F : 1.0F + Math.min(1.0F, overlappingDoses * 0.12F);
    }

    private static int fadeDurationFor(int duration) {
        if (duration <= 0) {
            return 0;
        }
        if (duration <= 20) {
            return Math.max(4, duration / 2);
        }
        return Math.clamp(duration / 4, 20, 80);
    }

    public enum Phase {
        ONSET,
        PEAK,
        COMEDOWN,
        FADING
    }

    public record MergeResult(boolean overlapped, boolean overstacked, boolean refreshed, float previousIntensity,
                              float newIntensity, float riskPressure, int overlappingDoses) {
    }

    public record TickResult(boolean expired, Phase previousPhase, Phase currentPhase, boolean fadeStarted) {
        public boolean phaseChanged() {
            return previousPhase != currentPhase;
        }
    }
}
