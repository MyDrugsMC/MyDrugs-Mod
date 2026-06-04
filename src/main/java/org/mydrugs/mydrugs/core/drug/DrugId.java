package org.mydrugs.mydrugs.core.drug;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public enum DrugId {
    WEED,
    HASH,
    METH,
    COCAINE,
    CRACK,
    LSD,
    MUSHROOMS,
    ALCOHOL,
    TOBACCO,
    COFFEE;

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public int networkId() {
        return switch (this) {
            case WEED -> 1;
            case HASH -> 2;
            case METH -> 3;
            case COCAINE -> 4;
            case CRACK -> 5;
            case LSD -> 7;
            case MUSHROOMS -> 8;
            case ALCOHOL -> 18;
            case TOBACCO -> 21;
            case COFFEE -> 22;
        };
    }

    public static Optional<DrugId> bySerializedName(String name) {
        return Optional.ofNullable(bySerializedNameOrNull(name));
    }

    public static @Nullable DrugId bySerializedNameOrNull(String name) {
        if (name == null) {
            return null;
        }

        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (DrugId id : values()) {
            if (id.serializedName().equals(normalized)) {
                return id;
            }
        }
        return null;
    }

    public static @Nullable DrugId byNetworkId(int networkId) {
        for (DrugId id : values()) {
            if (id.networkId() == networkId) {
                return id;
            }
        }
        return null;
    }
}
