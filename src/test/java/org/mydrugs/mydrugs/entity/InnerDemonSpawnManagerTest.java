package org.mydrugs.mydrugs.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnerDemonSpawnManagerTest {

    @Test
    void completedTrialAndRestoredScarEachLowerAmbientDanger() {
        double raw = 0.9D;
        double none = InnerDemonSpawnManager.effectiveAmbientDanger(raw, false, false);
        double trial = InnerDemonSpawnManager.effectiveAmbientDanger(raw, true, false);
        double scar = InnerDemonSpawnManager.effectiveAmbientDanger(raw, false, true);
        double both = InnerDemonSpawnManager.effectiveAmbientDanger(raw, true, true);

        assertEquals(raw, none, 1.0E-9, "no relief should leave danger unchanged");
        assertTrue(trial < none, "a completed trial must lower danger");
        assertTrue(scar < none, "a restored scar must lower danger");
        assertTrue(both < trial && both < scar, "the two reliefs must compound");
    }

    @Test
    void ambientSpawnChanceIsZeroAtOrBelowGateAndClampedAbove() {
        // At/below the danger gate there is no chance at all.
        assertEquals(0.0F, InnerDemonSpawnManager.ambientSpawnChance(0.0D), 1.0E-6F);
        assertEquals(0.0F, InnerDemonSpawnManager.ambientSpawnChance(0.45D), 1.0E-6F);
        // Above the gate the chance is positive and rises with danger, but never exceeds the cap.
        float mid = InnerDemonSpawnManager.ambientSpawnChance(0.7D);
        assertTrue(mid > 0.0F && mid <= 0.5F, "mid danger should give a bounded positive chance");
        assertTrue(InnerDemonSpawnManager.ambientSpawnChance(5.0D) <= 0.5F,
                "the ambient chance must stay clamped to its cap even at extreme danger");
    }

    @Test
    void minimumDistanceIsRejected() {
        assertFalse(InnerDemonSpawnManager.isSpawnGeometryAcceptableForTest(
                6.0D * 6.0D,
                64,
                0,
                256,
                true,
                true,
                true,
                false,
                true,
                true
        ));
    }

    @Test
    void lavaAndFluidAreRejected() {
        assertFalse(InnerDemonSpawnManager.isSpawnGeometryAcceptableForTest(
                9.0D * 9.0D,
                64,
                0,
                256,
                true,
                true,
                true,
                false,
                false,
                true
        ));
        assertFalse(InnerDemonSpawnManager.isSpawnGeometryAcceptableForTest(
                9.0D * 9.0D,
                64,
                0,
                256,
                true,
                true,
                true,
                false,
                true,
                false
        ));
    }

    @Test
    void blockedHeadroomIsRejected() {
        assertFalse(InnerDemonSpawnManager.isSpawnGeometryAcceptableForTest(
                9.0D * 9.0D,
                64,
                0,
                256,
                true,
                false,
                true,
                false,
                true,
                true
        ));
    }

    @Test
    void validFloorAndOpenSpaceAreAccepted() {
        assertTrue(InnerDemonSpawnManager.isSpawnGeometryAcceptableForTest(
                9.0D * 9.0D,
                64,
                0,
                256,
                true,
                true,
                true,
                false,
                true,
                true
        ));
    }
}
