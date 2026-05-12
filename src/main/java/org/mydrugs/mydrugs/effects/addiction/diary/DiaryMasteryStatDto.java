package org.mydrugs.mydrugs.effects.addiction.diary;

public record DiaryMasteryStatDto(
        String recipeId,
        int completed,
        int failed,
        float speedMultiplier,
        float instabilityReduction
) {
}
