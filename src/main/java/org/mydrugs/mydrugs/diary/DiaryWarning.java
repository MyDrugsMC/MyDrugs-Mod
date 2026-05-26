package org.mydrugs.mydrugs.diary;

public record DiaryWarning(
        String textKey,
        String clearHintKey,
        String iconItemId,
        int severity
) {
    public DiaryWarning {
        textKey = normalize(textKey);
        clearHintKey = normalize(clearHintKey);
        iconItemId = normalize(iconItemId);
        severity = Math.max(0, Math.min(3, severity));
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
