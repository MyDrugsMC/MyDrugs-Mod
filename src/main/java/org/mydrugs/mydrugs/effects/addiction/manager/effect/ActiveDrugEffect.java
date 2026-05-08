package org.mydrugs.mydrugs.effects.addiction.manager.effect;

import org.mydrugs.mydrugs.core.drug.effect.EffectType;

public final class ActiveDrugEffect {
    private final EffectType type;
    private float intensity;
    private int remainingTicks;
    private int fadeTicksRemaining;
    private int fadeDurationTicks;

    public ActiveDrugEffect(EffectType type, float intensity, int remainingTicks) {
        this.type = type;
        this.intensity = Math.max(0.0F, intensity);
        this.remainingTicks = Math.max(0, remainingTicks);
        this.fadeDurationTicks = fadeDurationFor(this.remainingTicks);
    }

    public EffectType type() {
        return type;
    }

    public float intensity() {
        if (this.remainingTicks > 0 || this.fadeDurationTicks <= 0) {
            return this.intensity;
        }
        return this.intensity * Math.clamp(this.fadeTicksRemaining / (float) this.fadeDurationTicks, 0.0F, 1.0F);
    }

    public float baseIntensity() {
        return intensity;
    }

    public int remainingTicks() {
        return remainingTicks + fadeTicksRemaining;
    }

    public int fadeTicksRemaining() {
        return fadeTicksRemaining;
    }

    public int fadeDurationTicks() {
        return fadeDurationTicks;
    }

    public boolean isFading() {
        return this.remainingTicks <= 0 && this.fadeTicksRemaining > 0;
    }

    public void merge(float incomingIntensity, int incomingDuration) {
        this.intensity = Math.max(this.intensity, Math.max(0.0F, incomingIntensity));
        int duration = Math.max(0, incomingDuration);
        if (duration > this.remainingTicks) {
            this.remainingTicks = duration;
            this.fadeDurationTicks = fadeDurationFor(duration);
            this.fadeTicksRemaining = 0;
        } else if (this.isFading()) {
            this.remainingTicks = duration;
            this.fadeDurationTicks = fadeDurationFor(duration);
            this.fadeTicksRemaining = 0;
        }
    }

    public boolean tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
            if (remainingTicks <= 0) {
                fadeTicksRemaining = fadeDurationTicks;
            }
            return false;
        }

        if (fadeTicksRemaining > 0) {
            fadeTicksRemaining--;
        }
        return fadeTicksRemaining <= 0;
    }

    public ActiveDrugEffect copy() {
        ActiveDrugEffect copy = new ActiveDrugEffect(type, intensity, remainingTicks);
        copy.fadeTicksRemaining = this.fadeTicksRemaining;
        copy.fadeDurationTicks = this.fadeDurationTicks;
        return copy;
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
}
