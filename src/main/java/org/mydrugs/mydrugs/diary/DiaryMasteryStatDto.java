package org.mydrugs.mydrugs.diary;

public record DiaryMasteryStatDto(
        String recipeId,
        String displayName,
        int completed,
        int failed,
        float speedMultiplier,
        float instabilityReduction,
        int removedActions,
        float timingWindowBonus,
        float actionTimeoutBonus,
        String rankKey,
        String nextBenefitKey
) {
}
