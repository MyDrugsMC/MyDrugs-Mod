package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import com.mojang.serialization.Codec;

import java.util.List;
import java.util.Locale;

public enum PsyMixerRitualLevel {
    NONE("none", List.of()),
    SIMPLE("simple", List.of(PsyMixerRitualAction.TIMING_RING)),
    FOCUSED("focused", List.of(PsyMixerRitualAction.LOOK_AT_CORE, PsyMixerRitualAction.TIMING_RING, PsyMixerRitualAction.SNEAK)),
    GREAT("great", List.of(PsyMixerRitualAction.JUMP, PsyMixerRitualAction.WALK_RING, PsyMixerRitualAction.LOOK_AT_CORE, PsyMixerRitualAction.TIMING_RING, PsyMixerRitualAction.SNEAK)),
    MASTER("master", List.of(PsyMixerRitualAction.WALK_RING, PsyMixerRitualAction.LOOK_AT_CORE, PsyMixerRitualAction.TIMING_RING, PsyMixerRitualAction.STAND_STILL, PsyMixerRitualAction.TIMING_RING, PsyMixerRitualAction.SNEAK));

    public static final Codec<PsyMixerRitualLevel> CODEC = Codec.STRING.xmap(
            PsyMixerRitualLevel::bySerializedName,
            PsyMixerRitualLevel::serializedName
    );

    private final String serializedName;
    private final List<PsyMixerRitualAction> defaultActions;

    PsyMixerRitualLevel(String serializedName, List<PsyMixerRitualAction> defaultActions) {
        this.serializedName = serializedName;
        this.defaultActions = List.copyOf(defaultActions);
    }

    public String serializedName() {
        return serializedName;
    }

    public List<PsyMixerRitualAction> defaultActions() {
        return defaultActions;
    }

    public static PsyMixerRitualLevel bySerializedName(String name) {
        if (name == null) {
            return NONE;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (PsyMixerRitualLevel level : values()) {
            if (level.serializedName.equals(normalized)) {
                return level;
            }
        }
        return NONE;
    }
}
