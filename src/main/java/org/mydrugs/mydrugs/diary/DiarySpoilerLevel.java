package org.mydrugs.mydrugs.diary;

import java.util.Locale;

public enum DiarySpoilerLevel {
    VAGUE,
    CLEAR,
    EXPLICIT;

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static DiarySpoilerLevel bySerializedName(String name) {
        if (name == null || name.isBlank()) {
            return VAGUE;
        }
        String normalized = name.toLowerCase(Locale.ROOT);
        for (DiarySpoilerLevel level : values()) {
            if (level.serializedName().equals(normalized)) {
                return level;
            }
        }
        return VAGUE;
    }
}
