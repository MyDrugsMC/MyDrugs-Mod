package org.mydrugs.mydrugs.blocks.entity;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationService;
import org.mydrugs.mydrugs.dimension.InnerDimensionService;

/**
 * Reasons a Psychotrope Resonator ritual can refuse or fail. Lives outside
 * {@link PsychotropeResonatorBlockEntity} so it can be exercised by unit tests without
 * triggering the BE's Minecraft-bootstrap-dependent static initializer.
 */
public enum PsychotropeResonatorFailureReason {
    NONE(0, "screen.mydrugs.psychotrope_resonator.failure.none"),
    BUSY(1, "message.mydrugs.resonator.busy"),
    REQUIRES_LYSERGIC(2, "message.mydrugs.resonator.requires_lysergic"),
    MISSING_DREAM_RESIDUE(3, "message.mydrugs.resonator.missing_dream_residue"),
    MISSING_CALMING_RESIN(4, "message.mydrugs.resonator.missing_calming_resin"),
    MISSING_DIARY(5, "message.mydrugs.resonator.missing_diary"),
    MISSING_RECOVERY_ROOM(6, "message.mydrugs.resonator.missing_recovery_room"),
    MISSING_DRUG_KNOWLEDGE(7, "message.mydrugs.resonator.missing_drug_knowledge"),
    PEAK_TOO_LOW(8, "message.mydrugs.resonator.peak_too_low"),
    ADDICTION_TOO_HIGH(9, "message.mydrugs.resonator.addiction_too_high"),
    RECOVERY_INCOMPLETE(10, "message.mydrugs.resonator.recovery_incomplete"),
    LIFETIME_DOSE_LOW(11, "message.mydrugs.resonator.lifetime_dose_low"),
    CLEAN_STREAK_LOW(12, "message.mydrugs.resonator.clean_streak_low"),
    MISSING_CORE(13, "message.mydrugs.resonator.missing_integration_core"),
    MISSING_MATERIAL(14, "message.mydrugs.resonator.missing_integration_material"),
    ALREADY_INTEGRATED(15, "message.mydrugs.resonator.already_integrated"),
    NO_ELIGIBLE_DRUG(16, "message.mydrugs.resonator.no_eligible_drug"),
    INTEGRATION_LOST(17, "message.mydrugs.resonator.integration_lost"),
    DREAM_ALIGNMENT_MISSING(18, "message.mydrugs.inner_dimension.requires_dream_alignment"),
    INTEGRATION_MISSING(19, "message.mydrugs.inner_dimension.requires_integration"),
    INNER_DIMENSION_UNAVAILABLE(20, "message.mydrugs.inner_dimension.unavailable"),
    DREAM_ALREADY_ALIGNED(21, "message.mydrugs.resonator.dream_already_aligned");

    private final int networkId;
    private final String translationKey;

    PsychotropeResonatorFailureReason(int networkId, String translationKey) {
        this.networkId = networkId;
        this.translationKey = translationKey;
    }

    public int networkId() {
        return networkId;
    }

    public String translationKey() {
        return translationKey;
    }

    public static PsychotropeResonatorFailureReason byNetworkId(int id) {
        for (PsychotropeResonatorFailureReason reason : values()) {
            if (reason.networkId == id) {
                return reason;
            }
        }
        return NONE;
    }

    /**
     * Maps an {@link IntegrationService.EligibilityResult} to the first failing reason in the same
     * priority order used by the Resonator's validation cascade. Returns {@code null} when the
     * result is fully eligible. Lives here (not on the BE) so unit tests can exercise the cascade
     * without triggering the BE's Minecraft-bootstrap-dependent static initializer.
     */
    public static @Nullable PsychotropeResonatorFailureReason firstEligibilityFailure(
            IntegrationService.EligibilityResult eligibility
    ) {
        if (eligibility == null) {
            return NO_ELIGIBLE_DRUG;
        }
        if (eligibility.alreadyIntegrated()) return ALREADY_INTEGRATED;
        if (!eligibility.cleanDoseStreakMet()) return CLEAN_STREAK_LOW;
        if (!eligibility.peakMet()) return PEAK_TOO_LOW;
        if (!eligibility.lowAddictionMet()) return ADDICTION_TOO_HIGH;
        if (!eligibility.lifetimeDoseMet()) return LIFETIME_DOSE_LOW;
        if (!eligibility.recoveryMet()) return RECOVERY_INCOMPLETE;
        return null;
    }

    public static PsychotropeResonatorFailureReason fromOpenStatus(InnerDimensionService.OpenStatus status) {
        return switch (status) {
            case READY -> NONE;
            case MISSING_INTEGRATION -> INTEGRATION_MISSING;
            case MISSING_DREAM_ALIGNMENT -> DREAM_ALIGNMENT_MISSING;
            case UNAVAILABLE -> INNER_DIMENSION_UNAVAILABLE;
        };
    }
}
