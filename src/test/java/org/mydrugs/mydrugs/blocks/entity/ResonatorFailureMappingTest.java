package org.mydrugs.mydrugs.blocks.entity;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorFailureReason;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationService.EligibilityResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Regression test: when a ritual is interrupted mid-cast (e.g. addictionValue ticks back above
 * maxCurrentAddiction), the Resonator must surface the *specific* failed requirement to the
 * player, not the catch-all INTEGRATION_LOST. Pinning the eligibility cascade also defends
 * against a future change that re-walls a curated drug behind a new requirement without
 * mapping it to its own PsychotropeResonatorFailureReason.
 */
class ResonatorFailureMappingTest {

    @Test
    void fullyEligibleResultHasNoFailure() {
        EligibilityResult eligible = new EligibilityResult(true, true, true, true, true, true, false);
        assertNull(PsychotropeResonatorFailureReason.firstEligibilityFailure(eligible));
    }

    @Test
    void alreadyIntegratedMapsBeforeOtherChecks() {
        EligibilityResult result = new EligibilityResult(false, true, true, true, true, true, true);
        assertEquals(PsychotropeResonatorFailureReason.ALREADY_INTEGRATED,
                PsychotropeResonatorFailureReason.firstEligibilityFailure(result));
    }

    @Test
    void cleanDoseStreakLowMapsToCleanStreakLow() {
        EligibilityResult result = new EligibilityResult(false, true, true, true, true, false, false);
        assertEquals(PsychotropeResonatorFailureReason.CLEAN_STREAK_LOW,
                PsychotropeResonatorFailureReason.firstEligibilityFailure(result));
    }

    @Test
    void peakLowMapsToPeakTooLow() {
        EligibilityResult result = new EligibilityResult(false, false, true, true, true, true, false);
        assertEquals(PsychotropeResonatorFailureReason.PEAK_TOO_LOW,
                PsychotropeResonatorFailureReason.firstEligibilityFailure(result));
    }

    @Test
    void addictionTooHighMapsToAddictionTooHigh() {
        // This is the canonical mid-ritual regression: addictionValue ticked back above the gate.
        // Previously surfaced as INTEGRATION_LOST; must now surface as ADDICTION_TOO_HIGH.
        EligibilityResult result = new EligibilityResult(false, true, false, true, true, true, false);
        assertEquals(PsychotropeResonatorFailureReason.ADDICTION_TOO_HIGH,
                PsychotropeResonatorFailureReason.firstEligibilityFailure(result));
    }

    @Test
    void lifetimeDoseLowMapsToLifetimeDoseLow() {
        EligibilityResult result = new EligibilityResult(false, true, true, true, false, true, false);
        assertEquals(PsychotropeResonatorFailureReason.LIFETIME_DOSE_LOW,
                PsychotropeResonatorFailureReason.firstEligibilityFailure(result));
    }

    @Test
    void recoveryIncompleteMapsToRecoveryIncomplete() {
        EligibilityResult result = new EligibilityResult(false, true, true, false, true, true, false);
        assertEquals(PsychotropeResonatorFailureReason.RECOVERY_INCOMPLETE,
                PsychotropeResonatorFailureReason.firstEligibilityFailure(result));
    }

    @Test
    void nullEligibilityFallsBackToNoEligibleDrug() {
        assertEquals(PsychotropeResonatorFailureReason.NO_ELIGIBLE_DRUG,
                PsychotropeResonatorFailureReason.firstEligibilityFailure(null));
    }
}
