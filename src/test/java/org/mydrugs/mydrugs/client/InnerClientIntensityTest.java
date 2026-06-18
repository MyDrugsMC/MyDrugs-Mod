package org.mydrugs.mydrugs.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pure-logic coverage for the central client intensity scalars. These cores take the reduced-motion
 * flag directly so they exercise the policy without a running client / loaded config.
 */
class InnerClientIntensityTest {
    @Test
    void normalMotionLeavesEveryScalarAtFullStrength() {
        assertEquals(1.0D, InnerClientIntensity.particleScale(false), 0.0D);
        assertEquals(1.0D, InnerClientIntensity.skyMotionScale(false), 0.0D);
        assertEquals(1.0D, InnerClientIntensity.screenEffectScale(false), 0.0D);
    }

    @Test
    void reducedMotionDampsMotionWithoutErasingParticlesOrStings() {
        // Sky drift fully freezes...
        assertEquals(0.0D, InnerClientIntensity.skyMotionScale(true), 0.0D);
        // ...but particles and screen stings are only thinned, never removed (stay > 0).
        double particles = InnerClientIntensity.particleScale(true);
        double sting = InnerClientIntensity.screenEffectScale(true);
        assertTrue(particles > 0.0D && particles < 1.0D,
                "reduced-motion particle scale should thin but keep particles, was " + particles);
        assertTrue(sting > 0.0D && sting < 1.0D,
                "reduced-motion screen-effect scale should soften but keep stings, was " + sting);
    }

    @Test
    void reducedMotionScalarsMatchTheDocumentedConstants() {
        assertEquals(InnerClientIntensity.REDUCED_PARTICLE_SCALE,
                InnerClientIntensity.particleScale(true), 0.0D);
        assertEquals(InnerClientIntensity.REDUCED_SKY_MOTION_SCALE,
                InnerClientIntensity.skyMotionScale(true), 0.0D);
        assertEquals(InnerClientIntensity.REDUCED_SCREEN_EFFECT_SCALE,
                InnerClientIntensity.screenEffectScale(true), 0.0D);
    }

    @Test
    void everyScalarStaysWithinUnitRange() {
        for (boolean reduced : new boolean[]{false, true}) {
            assertWithinUnit("particleScale", InnerClientIntensity.particleScale(reduced));
            assertWithinUnit("skyMotionScale", InnerClientIntensity.skyMotionScale(reduced));
            assertWithinUnit("screenEffectScale", InnerClientIntensity.screenEffectScale(reduced));
        }
    }

    private static void assertWithinUnit(String name, double value) {
        assertTrue(value >= 0.0D && value <= 1.0D, name + " out of [0,1]: " + value);
    }
}
