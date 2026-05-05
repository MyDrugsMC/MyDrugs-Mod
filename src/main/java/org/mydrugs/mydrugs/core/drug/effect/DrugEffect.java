package org.mydrugs.mydrugs.core.drug.effect;

public class DrugEffect {
    private final EffectType effectType;
    private final int baseDuration;
    private final float baseIntensity;

    public DrugEffect(EffectType type) {
        this(type, 10 * 20);
    }

    public DrugEffect(EffectType type, int baseDuration) {
        this(type, baseDuration, 1.0F);
    }

    public DrugEffect(EffectType type, int baseDuration, int basePotency) {
        this(type, baseDuration, (float) basePotency);
    }

    public DrugEffect(EffectType type, int baseDuration, float baseIntensity) {
        this.effectType = type;
        this.baseDuration = baseDuration;
        this.baseIntensity = baseIntensity;
    }

    public int getBaseDuration() {
        return baseDuration;
    }

    /**
     * Legacy compatibility for old integer-amplifier code paths. New drug effects should use
     * {@link #getBaseIntensity()} and never translate this value to a vanilla potion amplifier.
     */
    @Deprecated
    public int getBasePotency() {
        return Math.round(baseIntensity);
    }

    public float getBaseIntensity() {
        return baseIntensity;
    }

    public EffectType getEffectType() {
        return effectType;
    }
}
