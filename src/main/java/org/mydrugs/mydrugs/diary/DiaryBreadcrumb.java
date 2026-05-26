package org.mydrugs.mydrugs.diary;

public record DiaryBreadcrumb(
        String textKey,
        String clearHintKey,
        String explicitHintKey,
        String guideTarget,
        String iconItemId,
        DiarySpoilerLevel spoilerLevel
) {
    public DiaryBreadcrumb {
        textKey = normalize(textKey);
        clearHintKey = normalize(clearHintKey);
        explicitHintKey = normalize(explicitHintKey);
        guideTarget = normalize(guideTarget);
        iconItemId = normalize(iconItemId);
        if (spoilerLevel == null) {
            spoilerLevel = DiarySpoilerLevel.VAGUE;
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
