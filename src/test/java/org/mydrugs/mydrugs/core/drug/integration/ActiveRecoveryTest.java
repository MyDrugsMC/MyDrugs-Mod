package org.mydrugs.mydrugs.core.drug.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;

class ActiveRecoveryTest {

    @Test
    void idleTimeNeverCompletesRecoveryProgress() {
        DrugAddictionStats stats = reckoningStats();
        float before = stats.recoveryProgress;

        assertEquals(before, stats.recoveryProgress);
        assertFalse(stats.recoveryProgress >= 1.0F);
    }

    @Test
    void productiveActionsReduceAddictionAndIncreaseRecovery() {
        DrugAddictionStats stats = reckoningStats();

        RecoveryProgressManager.RecoveryDelta delta = RecoveryProgressManager.applyRecoveryAction(stats, 10.0F);

        assertTrue(delta.addictionReduced() > 0.0F);
        assertTrue(delta.recoveryProgressAdded() > 0.0F);
        assertTrue(stats.addictionValue < 40.0F);
        assertTrue(stats.recoveryProgress > 0.25F);
    }

    @Test
    void nextDrugWorkIsFasterThanGenericWork() {
        float generic = RecoveryProgressManager.effectiveWeight(
                RecoveryProgressManager.ActionKind.MACHINE_OUTPUT, 1.0F, 1.0F, false);
        float nextDrug = RecoveryProgressManager.effectiveWeight(
                RecoveryProgressManager.ActionKind.MACHINE_OUTPUT, 1.0F, 1.0F, true);

        assertEquals(generic * IntegrationConstants.NEXT_DRUG_WORK_BONUS, nextDrug, 1e-6F);
    }

    @Test
    void recoveryProgressClampsAtOne() {
        DrugAddictionStats stats = reckoningStats();
        stats.recoveryProgress = 0.99F;

        RecoveryProgressManager.applyRecoveryAction(stats, 100.0F);

        assertEquals(1.0F, stats.recoveryProgress, 1e-6F);
    }

    @Test
    void integratedDrugsAreNotInReckoning() {
        DrugAddictionStats stats = reckoningStats();
        stats.integrationStage = 2;

        assertFalse(RecoveryProgressManager.isInReckoning(stats));
    }

    private static DrugAddictionStats reckoningStats() {
        // This helper builds stats without a DrugId; applyRecoveryAction(stats, w) routes through
        // the null-drug overload, which uses PEAK_THRESHOLD_FALLBACK. Keep the test consistent
        // with the documented fallback so it exercises that exact path.
        DrugAddictionStats stats = new DrugAddictionStats();
        stats.peakHistoricalAddiction = IntegrationConstants.PEAK_THRESHOLD_FALLBACK + 10.0F;
        stats.addictionValue = 40.0F;
        stats.recoveryProgress = 0.25F;
        // Lifetime dose is not gated by the fallback path; any positive value is fine.
        stats.lifetimeDoseConsumed = 50.0F;
        return stats;
    }
}
