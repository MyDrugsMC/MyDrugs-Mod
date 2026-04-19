package org.mydrugs.mydrugs.core.drug.effect;

public class DrugEffect {
    private final EffectType effectType;
    private final int baseDuration;
    private final int basePotency;

    public DrugEffect(EffectType type) {
        this(type, 10 * 20);
    }

    public DrugEffect(EffectType type, int baseDuration) {
        this(type, baseDuration, 1);
    }

    public DrugEffect(EffectType type, int baseDuration, int basePotency) {
        this.effectType = type;
        this.baseDuration = baseDuration;
        this.basePotency = basePotency;
    }

    public int getBaseDuration() {
        return baseDuration;
    }

    public int getBasePotency() {
        return basePotency;
    }

    public EffectType getEffectType() {
        return effectType;
    }
}