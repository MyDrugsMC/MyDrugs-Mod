package org.mydrugs.mydrugs.core.drug.integration;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.core.drug.DrugId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 3 / Issue B: psychedelics (LSD, MUSHROOMS) use a clean dose streak instead of an
 * addiction-recovery arc and therefore never enter {@link RecoveryProgressManager}'s reckoning
 * loop. The eligibility promotion (stage 0 -> 1) for them MUST be triggered from the addiction
 * consumption path, not from the recovery loop.
 *
 * <p>This test pins that architectural property so a future change that moves the streak logic
 * out of {@code AddictionManager} doesn't silently re-break the staging + notification UX.
 */
class PsychedelicReckoningTest {

    @Test
    void lsdAndMushroomsUseCleanDoseStreak() {
        IntegrationRequirementProfile lsd = IntegrationRequirements.profile(DrugId.LSD);
        IntegrationRequirementProfile mushrooms = IntegrationRequirements.profile(DrugId.MUSHROOMS);
        assertTrue(lsd.usesCleanDoseStreak(), "LSD must be a clean-streak drug");
        assertTrue(mushrooms.usesCleanDoseStreak(), "MUSHROOMS must be a clean-streak drug");
        assertFalse(lsd.requiresRecoveryProgress(), "LSD must not require recovery progress");
        assertFalse(mushrooms.requiresRecoveryProgress(), "MUSHROOMS must not require recovery progress");
    }

    @Test
    void psychedelicsAreNeverInReckoning() {
        // RecoveryProgressManager.onProductiveAction iterates only drugs where isInReckoning is
        // true. If this assertion ever flips, the AddictionManager-side staging hook becomes
        // redundant — and the test should be revisited intentionally rather than reflexively.
        DrugAddictionStats lsdStats = new DrugAddictionStats();
        lsdStats.cleanIntegrationDoseStreak = 5;
        lsdStats.lifetimeDoseConsumed = 999.0F;
        assertFalse(RecoveryProgressManager.isInReckoning(DrugId.LSD, lsdStats),
                "Psychedelics with full clean streak still must not be reckoning subjects");

        DrugAddictionStats mushroomStats = new DrugAddictionStats();
        mushroomStats.cleanIntegrationDoseStreak = 5;
        assertFalse(RecoveryProgressManager.isInReckoning(DrugId.MUSHROOMS, mushroomStats),
                "MUSHROOMS clean-streak completion must not promote it through the reckoning loop");
    }

    @Test
    void cleanStreakNeedsReflectionAndSafeUseBeforeEligibility() {
        // Psychedelics still avoid normal addiction recovery, but the streak alone is no longer
        // enough: at least one reflection and one safe-setting experience must be recorded too.
        var stats = new org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats();
        DrugAddictionStats lsd = stats.getOrCreateDrugStats(DrugId.LSD);
        lsd.cleanIntegrationDoseStreak = 5;
        assertFalse(IntegrationService.evaluate(stats, DrugId.LSD).eligible());

        lsd.cleanPsychedelicReflectionCount = 1;
        lsd.cleanPsychedelicSafeUseCount = 1;
        assertTrue(IntegrationService.evaluate(stats, DrugId.LSD).eligible());
    }
}
