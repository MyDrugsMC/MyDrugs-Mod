package org.mydrugs.mydrugs.diary;

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
