package org.mydrugs.mydrugs.addiction.network;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum DrugEffectCueKind {
    ROUTE_CONSUMED,
    EFFECT_STARTED,
    EFFECT_REFRESHED,
    EFFECT_PEAKED,
    EFFECT_COMEDOWN,
    EFFECT_EXPIRED,
    EFFECT_OVERSTACKED,
    BURST_READY,
    BURST_ACTIVE,
    BURST_ENDED,
    DASH_SUCCESS,
    DASH_NO_EFFECT,
    DASH_COOLDOWN,
    DASH_INVALID,
    INPUT_FAIL;

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public boolean isWarning() {
        return switch (this) {
            case EFFECT_OVERSTACKED, DASH_NO_EFFECT, DASH_COOLDOWN, DASH_INVALID, INPUT_FAIL -> true;
            default -> false;
        };
    }

    public static @Nullable DrugEffectCueKind bySerializedNameOrNull(String name) {
        if (name == null) {
            return null;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (DrugEffectCueKind kind : values()) {
            if (kind.serializedName().equals(normalized)) {
                return kind;
            }
        }
        return null;
    }
}
