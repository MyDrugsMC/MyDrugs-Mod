package org.mydrugs.mydrugs.effects.addiction.diary;

public enum DiaryEntryType {
    AUTO,
    CUSTOM;

    public static DiaryEntryType parse(String s) {
        if (s == null) return AUTO;
        try {
            return DiaryEntryType.valueOf(s);
        } catch (IllegalArgumentException e) {
            return AUTO;
        }
    }
}
