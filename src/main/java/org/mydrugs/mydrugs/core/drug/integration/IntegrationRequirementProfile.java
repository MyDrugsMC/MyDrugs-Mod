package org.mydrugs.mydrugs.core.drug.integration;

import org.mydrugs.mydrugs.core.drug.DrugId;

public record IntegrationRequirementProfile(
        DrugId drugId,
        IntegrationRequirementType type,
        float requiredPeakExposure,
        float maxCurrentAddiction,
        float requiredLifetimeDose,
        float requiredRecoveryProgress,
        int requiredCleanDoseStreak,
        boolean currentAddictionRelevant,
        int requiredPsychedelicReflections,
        int requiredSafePsychedelicUses,
        long recentBadTripBlockTicks
) {
    public IntegrationRequirementProfile(
            DrugId drugId,
            IntegrationRequirementType type,
            float requiredPeakExposure,
            float maxCurrentAddiction,
            float requiredLifetimeDose,
            float requiredRecoveryProgress,
            int requiredCleanDoseStreak,
            boolean currentAddictionRelevant
    ) {
        this(drugId, type, requiredPeakExposure, maxCurrentAddiction, requiredLifetimeDose,
                requiredRecoveryProgress, requiredCleanDoseStreak, currentAddictionRelevant, 0, 0, 0L);
    }

    public boolean usesCleanDoseStreak() {
        return type == IntegrationRequirementType.CLEAN_PSYCHEDELIC_STREAK;
    }

    public boolean requiresRecoveryProgress() {
        return requiredRecoveryProgress > 0.0F;
    }

    public boolean requiresLifetimeDose() {
        return requiredLifetimeDose > 0.0F;
    }

    public boolean requiresPsychedelicReflection() {
        return requiredPsychedelicReflections > 0;
    }

    public boolean requiresSafePsychedelicUse() {
        return requiredSafePsychedelicUses > 0;
    }

    public boolean blocksRecentBadTrips() {
        return recentBadTripBlockTicks > 0L;
    }
}
