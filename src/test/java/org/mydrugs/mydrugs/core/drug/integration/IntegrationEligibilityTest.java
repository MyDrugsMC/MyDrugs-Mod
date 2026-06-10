package org.mydrugs.mydrugs.core.drug.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.core.drug.DrugId;

class IntegrationEligibilityTest {

    @Test
    void thresholdsGateEligibilityIndependently() {
        PlayerAddictionStats stats = eligibleStats();

        assertTrue(IntegrationService.evaluate(stats, DrugId.COFFEE).eligible());

        IntegrationRequirementProfile coffeeProfile = IntegrationRequirements.profile(DrugId.COFFEE);
        stats.getDrugStats(DrugId.COFFEE).peakHistoricalAddiction = coffeeProfile.requiredPeakExposure() - 0.01F;
        assertFalse(IntegrationService.evaluate(stats, DrugId.COFFEE).peakMet());
        assertFalse(IntegrationService.evaluate(stats, DrugId.COFFEE).eligible());

        stats = eligibleStats();
        stats.getDrugStats(DrugId.COFFEE).addictionValue = coffeeProfile.maxCurrentAddiction() + 0.01F;
        assertFalse(IntegrationService.evaluate(stats, DrugId.COFFEE).lowAddictionMet());
        assertFalse(IntegrationService.evaluate(stats, DrugId.COFFEE).eligible());

        stats = eligibleStats();
        stats.getDrugStats(DrugId.COFFEE).recoveryProgress = 0.999F;
        assertFalse(IntegrationService.evaluate(stats, DrugId.COFFEE).recoveryMet());
        assertFalse(IntegrationService.evaluate(stats, DrugId.COFFEE).eligible());

        stats = eligibleStats();
        stats.getDrugStats(DrugId.COFFEE).lifetimeDoseConsumed = coffeeProfile.requiredLifetimeDose() - 0.01F;
        assertFalse(IntegrationService.evaluate(stats, DrugId.COFFEE).lifetimeDoseMet());
        assertFalse(IntegrationService.evaluate(stats, DrugId.COFFEE).eligible());
    }

    @Test
    void psychedelicCleanDoseStreakReplacesAddictionPeak() {
        PlayerAddictionStats stats = new PlayerAddictionStats();
        DrugAddictionStats lsd = stats.getOrCreateDrugStats(DrugId.LSD);
        lsd.cleanIntegrationDoseStreak = 4;
        lsd.cleanPsychedelicReflectionCount = 1;
        lsd.cleanPsychedelicSafeUseCount = 1;

        assertFalse(IntegrationService.evaluate(stats, DrugId.LSD).cleanDoseStreakMet());
        assertFalse(IntegrationService.evaluate(stats, DrugId.LSD).eligible());

        lsd.cleanIntegrationDoseStreak = 5;

        assertTrue(IntegrationService.evaluate(stats, DrugId.LSD).cleanDoseStreakMet());
        assertTrue(IntegrationService.evaluate(stats, DrugId.LSD).eligible());
    }

    @Test
    void psychedelicIntegrationRequiresReflectionSafeUseAndNoRecentBadTrip() {
        PlayerAddictionStats stats = new PlayerAddictionStats();
        DrugAddictionStats lsd = stats.getOrCreateDrugStats(DrugId.LSD);
        lsd.cleanIntegrationDoseStreak = 5;

        IntegrationService.EligibilityResult noReflection = IntegrationService.evaluate(stats, DrugId.LSD, 100_000L);
        assertTrue(noReflection.cleanDoseStreakMet());
        assertFalse(noReflection.psychedelicReflectionMet());
        assertFalse(noReflection.safePsychedelicUseMet());
        assertFalse(noReflection.eligible());

        lsd.cleanPsychedelicReflectionCount = 1;
        lsd.cleanPsychedelicSafeUseCount = 1;
        assertTrue(IntegrationService.evaluate(stats, DrugId.LSD, 100_000L).eligible());

        lsd.lastBadTripGameTime = 99_000L;
        IntegrationService.EligibilityResult recentBadTrip = IntegrationService.evaluate(stats, DrugId.LSD, 100_000L);
        assertFalse(recentBadTrip.noRecentBadTripMet());
        assertFalse(recentBadTrip.eligible());

        assertTrue(IntegrationService.evaluate(stats, DrugId.LSD,
                99_000L + IntegrationConstants.PSYCHEDELIC_BAD_TRIP_BLOCK_TICKS).eligible());
    }

    @Test
    void alreadyIntegratedDrugIsNotEligibleAgain() {
        PlayerAddictionStats stats = eligibleStats();
        stats.getDrugStats(DrugId.COFFEE).integrationStage = DrugAddictionStats.INTEGRATION_STAGE_INTEGRATED;

        IntegrationService.EligibilityResult result = IntegrationService.evaluate(stats, DrugId.COFFEE);

        assertTrue(result.alreadyIntegrated());
        assertFalse(result.eligible());
    }

    @Test
    void integratedDrugStatsAreNeverPrunedWhenEmpty() {
        PlayerAddictionStats stats = new PlayerAddictionStats();
        DrugAddictionStats coffee = stats.getOrCreateDrugStats(DrugId.COFFEE);
        coffee.integrationStage = DrugAddictionStats.INTEGRATION_STAGE_INTEGRATED;

        assertFalse(stats.removeDrugStatsIfEmpty(DrugId.COFFEE));
        assertTrue(stats.getAllDrugStats().containsKey(DrugId.COFFEE));
    }

    @Test
    void emptyNonIntegratedDrugStatsArePruned() {
        PlayerAddictionStats stats = new PlayerAddictionStats();
        stats.getOrCreateDrugStats(DrugId.COFFEE);

        assertTrue(stats.removeDrugStatsIfEmpty(DrugId.COFFEE));
        assertFalse(stats.getAllDrugStats().containsKey(DrugId.COFFEE));
    }

    @Test
    void integrationFieldsSurviveDeathCopy() {
        PlayerAddictionStats source = new PlayerAddictionStats();
        DrugAddictionStats sourceCoffee = source.getOrCreateDrugStats(DrugId.COFFEE);
        sourceCoffee.integrationStage = DrugAddictionStats.INTEGRATION_STAGE_INTEGRATED;
        sourceCoffee.integratedAtGameTime = 42L;
        sourceCoffee.recoveryProgress = 1.0F;
        sourceCoffee.cleanIntegrationDoseStreak = 3;
        sourceCoffee.cleanPsychedelicReflectionCount = 1;
        sourceCoffee.cleanPsychedelicSafeUseCount = 1;
        sourceCoffee.lastBadTripGameTime = 40L;

        PlayerAddictionStats copy = new PlayerAddictionStats();
        copy.copyFrom(source, true, RandomSource.create(1L));

        DrugAddictionStats copiedCoffee = copy.getDrugStats(DrugId.COFFEE);
        assertTrue(copiedCoffee != null && copiedCoffee.isIntegrated());
        assertTrue(copiedCoffee.integratedAtGameTime == 42L);
        assertTrue(copiedCoffee.recoveryProgress == 1.0F);
        assertTrue(copiedCoffee.cleanIntegrationDoseStreak == 3);
        assertTrue(copiedCoffee.cleanPsychedelicReflectionCount == 1);
        assertTrue(copiedCoffee.cleanPsychedelicSafeUseCount == 1);
        assertTrue(copiedCoffee.lastBadTripGameTime == 40L);
    }

    private static PlayerAddictionStats eligibleStats() {
        PlayerAddictionStats stats = new PlayerAddictionStats();
        DrugAddictionStats coffee = stats.getOrCreateDrugStats(DrugId.COFFEE);
        IntegrationRequirementProfile profile = IntegrationRequirements.profile(DrugId.COFFEE);
        coffee.peakHistoricalAddiction = profile.requiredPeakExposure();
        coffee.addictionValue = profile.maxCurrentAddiction();
        coffee.recoveryProgress = 1.0F;
        coffee.lifetimeDoseConsumed = profile.requiredLifetimeDose();
        return stats;
    }
}
