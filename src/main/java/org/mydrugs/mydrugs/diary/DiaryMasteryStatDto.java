package org.mydrugs.mydrugs.diary;

public record DiaryMasteryStatDto(
        String recipeId,
        int completed,
        int failed,
        float speedMultiplier,
        float instabilityReduction
) {
}
