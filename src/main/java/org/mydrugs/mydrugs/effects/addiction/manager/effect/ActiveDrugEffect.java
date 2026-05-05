package org.mydrugs.mydrugs.effects.addiction.manager.effect;

import org.mydrugs.mydrugs.core.drug.effect.EffectType;

public final class ActiveDrugEffect {
    private final EffectType type;
    private float intensity;
    private int remainingTicks;

    public ActiveDrugEffect(EffectType type, float intensity, int remainingTicks) {
        this.type = type;
        this.intensity = Math.max(0.0F, intensity);
        this.remainingTicks = Math.max(0, remainingTicks);
    }

    public EffectType type() {
        return type;
    }

    public float intensity() {
        return intensity;
    }

    public int remainingTicks() {
        return remainingTicks;
    }

    public void merge(float incomingIntensity, int incomingDuration) {
        this.intensity = Math.max(this.intensity, Math.max(0.0F, incomingIntensity));
        this.remainingTicks = Math.max(this.remainingTicks, Math.max(0, incomingDuration));
    }

    public boolean tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
        return remainingTicks <= 0;
    }

    public ActiveDrugEffect copy() {
        return new ActiveDrugEffect(type, intensity, remainingTicks);
    }
}
