package org.mydrugs.mydrugs.dimension.inner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Invariants for the naturalised glow rework (Section 2). Operates purely on the deterministic
 * placement decision ({@link InnerGlowBuilder#modeFor} / {@link InnerGlowBuilder#glowYFor} /
 * {@link InnerGlowBuilder#placesSolidCube}) so it needs no Minecraft bootstrap — exactly like
 * {@code InnerTerrainTest}, which samples terrain directly.
 */
class InnerGlowPlacementTest {
    private static final int CENTER_X = 0;
    private static final int CENTER_Z = 0;

    @Test
    void solidGlowIsNeverPlacedProudOfTheSurface() {
        int rockColumns = 0;
        int plantColumns = 0;
        int ambientColumns = 0;
        for (int x = -480; x <= 480; x += 8) {
            for (int z = -480; z <= 480; z += 8) {
                InnerTerrain.Sample sample = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
                if (!sample.land()) {
                    continue;
                }
                InnerGlowBuilder.Mode mode = InnerGlowBuilder.modeFor(sample);
                int y = InnerGlowBuilder.glowYFor(mode, sample);
                if (InnerGlowBuilder.placesSolidCube(mode)) {
                    // The core rule: a solid emissive cube only ever sits flush (topY) or below.
                    assertTrue(y <= sample.topY(),
                            "solid glow placed proud of surface at " + x + "," + z
                                    + " mode=" + mode + " y=" + y + " topY=" + sample.topY());
                }
                switch (mode) {
                    case EMBEDDED_ROCK -> {
                        assertEquals(sample.topY(), y, "embedded node must be flush at topY");
                        rockColumns++;
                    }
                    case GLOW_PLANT -> {
                        assertEquals(sample.topY() + 1, y, "glow plant sits one above the surface");
                        plantColumns++;
                    }
                    case AMBIENT_FILL -> {
                        assertTrue(y > sample.topY(), "ambient light fill floats above the surface");
                        ambientColumns++;
                    }
                    default -> {
                    }
                }
            }
        }
        // Sanity: the scan actually exercised the naturalised modes.
        assertTrue(rockColumns + plantColumns + ambientColumns > 0, "no glowable land columns scanned");
    }

    @Test
    void flatFeaturelessGroundNeverGetsASolidCube() {
        boolean checkedAny = false;
        for (int x = -480; x <= 480; x += 8) {
            for (int z = -480; z <= 480; z += 8) {
                InnerTerrain.Sample sample = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
                if (!sample.land() || !isFlatFeatureless(sample)) {
                    continue;
                }
                checkedAny = true;
                InnerGlowBuilder.Mode mode = InnerGlowBuilder.modeFor(sample);
                assertFalse(InnerGlowBuilder.placesSolidCube(mode),
                        "flat featureless ground got a solid cube at " + x + "," + z + " mode=" + mode);
            }
        }
        assertTrue(checkedAny, "expected to find some flat featureless ground");
    }

    @Test
    void lakeContextGlowLandsOnTheLakeFloorNotTheShore() {
        boolean foundLake = false;
        for (int x = -480; x <= 480 && !foundLake; x += 4) {
            for (int z = -480; z <= 480; z += 4) {
                InnerTerrain.Sample sample = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
                if (InnerGlowBuilder.modeFor(sample) != InnerGlowBuilder.Mode.LAKE_BED) {
                    continue;
                }
                int y = InnerGlowBuilder.glowYFor(InnerGlowBuilder.Mode.LAKE_BED, sample);
                assertEquals(sample.lakeFloorY(), y, "lake glow must land on the lake floor");
                assertTrue(y <= sample.topY(), "lake floor glow must not sit on/above the shore surface");
                assertTrue(sample.lake(), "LAKE_BED mode requires a lake column");
                foundLake = true;
                break;
            }
        }
        // Not all slots contain a lake; only assert behaviour when one is present.
    }

    @Test
    void glowDecisionIsDeterministic() {
        for (int x = -300; x <= 300; x += 17) {
            for (int z = -300; z <= 300; z += 19) {
                InnerTerrain.Sample a = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
                InnerTerrain.Sample b = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
                assertEquals(InnerGlowBuilder.modeFor(a), InnerGlowBuilder.modeFor(b),
                        "glow mode must be deterministic at " + x + "," + z);
            }
        }
    }

    private static boolean isFlatFeatureless(InnerTerrain.Sample sample) {
        return !sample.lake()
                && !sample.scar()
                && !sample.hole()
                && !sample.treeZone()
                && !sample.plantPatch()
                && sample.cliffStrength() <= 0.30D
                && sample.slopeHint() <= 0.40D
                && sample.wetlandStrength() <= 0.20D
                && sample.shoreStrength() <= 0.20D;
    }
}
