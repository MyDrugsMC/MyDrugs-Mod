package org.mydrugs.mydrugs.core.drug.effect;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public enum EffectType {
    CUSTOM_NAUSEA(EffectCategory.CAMERA),
    BLUR(EffectCategory.VISUAL),
    GAMMA_BOOST(EffectCategory.SHADER),
    LOW_LIGHT_VISION(EffectCategory.VISUAL),
    BRIGHTNESS_BOOST(EffectCategory.VISUAL),
    MOVEMENT_SPEED(EffectCategory.MOVEMENT),
    MOVEMENT_SLOWDOWN(EffectCategory.MOVEMENT),
    MINING_SPEED(EffectCategory.MINING),
    DAMAGE_RESISTANCE(EffectCategory.STAT_MODIFIER),
    CAMERA_SWAY(EffectCategory.CAMERA),
    TREMOR(EffectCategory.CAMERA),
    STUMBLE(EffectCategory.INPUT),
    INPUT_FAIL(EffectCategory.INPUT),
    VOMIT(EffectCategory.SERVER_ACTION),
    CONFUSION(EffectCategory.CAMERA),
    FOCUS(EffectCategory.STAT_MODIFIER),

    // Deprecated compatibility names. They are routed through the custom runtime, never vanilla potion effects.
    @Deprecated NAUSEA(EffectCategory.CAMERA),
    @Deprecated SLOWNESS(EffectCategory.MOVEMENT),

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
    HEARTBEAT(EffectCategory.SOUND),
    DRUNK_VISION(EffectCategory.SHADER);

    private final EffectCategory category;

    EffectType(EffectCategory category) {
        this.category = category;
    }

    public EffectCategory getCategory() {
        return category;
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Optional<EffectType> bySerializedName(String name) {
        return Optional.ofNullable(bySerializedNameOrNull(name));
    }

    public static @Nullable EffectType bySerializedNameOrNull(String name) {
        if (name == null) {
            return null;
        }

        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (EffectType type : values()) {
            if (type.serializedName().equals(normalized)) {
                return type;
            }
        }
        return null;
    }
}
