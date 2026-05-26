package org.mydrugs.mydrugs.diary;

public record DiaryMemory(
        long day,
        String textKey,
        String titleKey,
        String targetNodeId,
        String guideTarget,
        String iconItemId
) {
    public DiaryMemory {
        day = Math.max(1L, day);
        textKey = normalize(textKey);
        titleKey = normalize(titleKey);
        targetNodeId = normalize(targetNodeId);
        guideTarget = normalize(guideTarget);
        iconItemId = normalize(iconItemId);
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
