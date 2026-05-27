package org.mydrugs.mydrugs.diary;

public record DiaryIntegrationProgressDto(
        String drugId,
        String traitKey,
        String rewardKey,
        String roleplayKey,
        String materialItemId,
        String requirementType,
        boolean knowledgeUnlocked,
        boolean peakMet,
        boolean lowAddictionMet,
        boolean recoveryMet,
        boolean lifetimeDoseMet,
        boolean cleanDoseStreakMet,
        boolean diaryContext,
        boolean recoveryRoom,
        boolean materialInInventory,
        boolean integrationCoreInInventory,
        boolean alreadyIntegrated,
        float peakCurrent,
        float peakRequired,
        float addictionCurrent,
        float addictionMax,
        float recoveryProgress,
        float recoveryRequired,
        float lifetimeDose,
        float lifetimeDoseRequired,
        int cleanDoseStreak,
        int cleanDoseStreakRequired
) {
}
