package org.mydrugs.mydrugs.diary;

public record DiaryBlocker(
        String type,
        String textKey,
        String clearHintKey,
        String explicitHintKey,
        int count,
        long lastSeenGameTime,
        DiarySpoilerLevel spoilerLevel
) {
    public DiaryBlocker {
        type = normalize(type);
        textKey = normalize(textKey);
        clearHintKey = normalize(clearHintKey);
        explicitHintKey = normalize(explicitHintKey);
        count = Math.max(1, count);
        lastSeenGameTime = Math.max(0L, lastSeenGameTime);
        if (spoilerLevel == null) {
            spoilerLevel = DiarySpoilerLevel.CLEAR;
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
