package org.mydrugs.mydrugs.recovery;

import java.util.Locale;

public enum RecoverySessionStage {
    NONE,
    ARRIVE,
    GROUND,
    REFLECT,
    RETURN;

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static RecoverySessionStage bySerializedName(String name) {
        if (name == null || name.isBlank()) {
            return NONE;
        }
        try {
            return RecoverySessionStage.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
