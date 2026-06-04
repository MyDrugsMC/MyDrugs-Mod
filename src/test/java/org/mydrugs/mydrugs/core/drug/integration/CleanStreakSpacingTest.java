package org.mydrugs.mydrugs.core.drug.integration;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Phase 3 / Issue C: the clean dose streak rewards <em>disciplined</em> use, not rapid spamming.
 * A second psychedelic dose closer than {@link IntegrationConstants#MIN_CLEAN_STREAK_SPACING_TICKS}
 * resets the streak to zero. Properly spaced doses still increment.
 */
class CleanStreakSpacingTest {

    private static final long SPACING = IntegrationConstants.MIN_CLEAN_STREAK_SPACING_TICKS;

    @Test
    void firstEverDoseIncrementsToOne() {
        DrugAddictionStats stats = new DrugAddictionStats();
        // prevLastUseTime = 0 means "no prior use ever". Should count as clean.
        IntegrationService.onCleanStreakUse(stats, 100L, 0L, SPACING);
        assertEquals(1, stats.cleanIntegrationDoseStreak);
    }

    @Test
    void properlySpacedDosesAccumulate() {
        DrugAddictionStats stats = new DrugAddictionStats();
        long t = 1000L;
        for (int i = 1; i <= 5; i++) {
            long prev = i == 1 ? 0L : t - SPACING - 1;
            IntegrationService.onCleanStreakUse(stats, t, prev, SPACING);
            t += SPACING + 1;
        }
        assertEquals(5, stats.cleanIntegrationDoseStreak);
    }

    @Test
    void underSpacedDoseResetsToZero() {
        DrugAddictionStats stats = new DrugAddictionStats();
        stats.cleanIntegrationDoseStreak = 4;
        stats.cleanPsychedelicReflectionCount = 1;
        stats.cleanPsychedelicSafeUseCount = 1;
        long prev = 50_000L;
        long current = prev + SPACING - 1; // 1 tick under the gate
        IntegrationService.onCleanStreakUse(stats, current, prev, SPACING);
        assertEquals(0, stats.cleanIntegrationDoseStreak);
        assertEquals(0, stats.cleanPsychedelicReflectionCount);
        assertEquals(0, stats.cleanPsychedelicSafeUseCount);
    }

    @Test
    void exactlyAtSpacingThresholdResetsToZero() {
        // Strict "< SPACING" gate: dose at exactly SPACING ticks later counts as clean.
        DrugAddictionStats stats = new DrugAddictionStats();
        stats.cleanIntegrationDoseStreak = 4;
        long prev = 1000L;
        long current = prev + SPACING;
        IntegrationService.onCleanStreakUse(stats, current, prev, SPACING);
        assertEquals(5, stats.cleanIntegrationDoseStreak);
    }

    @Test
    void streakCapsAt999() {
        DrugAddictionStats stats = new DrugAddictionStats();
        stats.cleanIntegrationDoseStreak = 999;
        IntegrationService.onCleanStreakUse(stats, 100_000L, 0L, SPACING);
        assertEquals(999, stats.cleanIntegrationDoseStreak);
    }

    @Test
    void nullStatsIsNoOp() {
        // Defensive: should not throw.
        IntegrationService.onCleanStreakUse(null, 100L, 0L, SPACING);
    }
}
