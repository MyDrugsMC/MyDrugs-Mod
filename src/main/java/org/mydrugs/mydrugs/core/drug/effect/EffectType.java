package org.mydrugs.mydrugs.core.drug.effect;

public enum EffectType {
    NAUSEA(EffectCategory.MINECRAFT_EFFECT),
    SLOWNESS(EffectCategory.MINECRAFT_EFFECT),
    CHROMATIC_DREAM(EffectCategory.SHADER),
    ACID_WARP(EffectCategory.SHADER),
    VOID_PULSE(EffectCategory.SHADER),
    FOG(EffectCategory.SHADER),
    IRIDESCENT_HAZE(EffectCategory.SHADER),
    LUCID_DREAM(EffectCategory.SHADER),
    MELT_REALITY(EffectCategory.SHADER),
    VELVET_ECHO(EffectCategory.SHADER),
    EVENT_HORIZON(EffectCategory.SHADER),
    NEON_CELLS(EffectCategory.SHADER),
    OPAL_WAVE(EffectCategory.SHADER),
    QUANTUM_FLOWER(EffectCategory.SHADER),
    COSMIC_TUNNEL(EffectCategory.SHADER),
    FRACTAL_WARP(EffectCategory.SHADER),
    LIQUID_CHROMA(EffectCategory.SHADER),
    MELTING_REALITY(EffectCategory.SHADER),
    AURORA_RIBBONS(EffectCategory.SHADER),
    SPECTRAL_POSTER(EffectCategory.SHADER),
    HEARTBEAT(EffectCategory.SOUND_EFFECT),
    DRUNK_VISION(EffectCategory.SHADER);

    private final EffectCategory category;

    EffectType(EffectCategory category) {
        this.category = category;
    }

    public EffectCategory getCategory() {
        return category;
    }
}

