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
        boolean currentAddictionRelevant
) {
    public boolean usesCleanDoseStreak() {
        return type == IntegrationRequirementType.CLEAN_PSYCHEDELIC_STREAK;
    }

    public boolean requiresRecoveryProgress() {
        return requiredRecoveryProgress > 0.0F;
    }

    public boolean requiresLifetimeDose() {
        return requiredLifetimeDose > 0.0F;
    }
}
