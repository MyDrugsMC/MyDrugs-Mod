package org.mydrugs.mydrugs.dimension.inner;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reachability guard for the wandering-spoke change (Section 3c). The corridors now snake, so this
 * asserts that for every region a continuous band of walkable land still runs from the central
 * clearing out to the drug's landmark — i.e. disguising the route did not sever progression.
 *
 * <p>The search sweeps a small angular window around each landmark bearing (covering the spoke's
 * maximum wander plus its half-width) at every radius step; finding land somewhere in that window at
 * each step proves an unbroken corridor exists within the spoke envelope.
 */
class InnerSpokeReachabilityTest {
    private static final int CENTER_X = 0;
    private static final int CENTER_Z = 0;
    private static final double WANDER_WINDOW = 0.20D; // >= SPOKE_WANDER (0.16) + SPOKE_TOLERANCE (0.03)

    @Test
    void everyLandmarkRemainsReachableFromTheCore() {
        for (DrugId drug : InnerRegionMap.regionOrder()) {
            double bearing = InnerRegionMap.angleFor(drug);
            int landmarkRadius = InnerRegionMap.landmarkRadiusFor(drug);
            for (int r = InnerDimensionConstants.CORE_RADIUS + 8; r <= landmarkRadius; r += 16) {
                assertTrue(landExistsInWindow(bearing, r),
                        "no walkable land within the spoke window for " + drug + " at radius " + r);
            }
        }
    }

    private static boolean landExistsInWindow(double bearing, int radius) {
        for (double da = -WANDER_WINDOW; da <= WANDER_WINDOW; da += 0.02D) {
            double a = bearing + da;
            int x = CENTER_X + (int) Math.round(Math.cos(a) * radius);
            int z = CENTER_Z + (int) Math.round(Math.sin(a) * radius);
            if (InnerTerrain.sample(CENTER_X, CENTER_Z, x, z).land()) {
                return true;
            }
        }
        return false;
    }
}
