package org.mydrugs.mydrugs.core;

public enum Effect {
    NAUSEA(EffectType.INGAME_EFFECT),
    SLOWNESS(EffectType.INGAME_EFFECT),
    CHROMATIC_DREAM(EffectType.SHADER),
    ACID_WARP(EffectType.SHADER),
    VOID_PULSE(EffectType.SHADER),
    FOG(EffectType.SHADER);

    private final EffectType effectType;

    Effect(EffectType effectType) {
        this.effectType = effectType;
    }

    public EffectType getEffectType() {
        return effectType;
    }
}
