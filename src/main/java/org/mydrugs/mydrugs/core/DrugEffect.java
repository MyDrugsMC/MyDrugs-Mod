package org.mydrugs.mydrugs.core;

public class DrugEffect {
    private final EffectType effectType;

    public DrugEffect(EffectType effectType) {
        this.effectType = effectType;
    }

    public EffectType getEffectType() {
        return effectType;
    }
}
