package org.mydrugs.mydrugs.diary;

public record DiaryMasteryStatDto(
        String recipeId,
        String displayName,
        int completed,
        int failed,
        float speedMultiplier,
        float instabilityReduction
) {
}
