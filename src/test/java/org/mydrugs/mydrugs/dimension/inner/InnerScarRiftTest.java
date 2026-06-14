package org.mydrugs.mydrugs.dimension.inner;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Scar rifts (A3): deep walkable canyons on the strongest scar lines, never on paths. */
class InnerScarRiftTest {

    @Test
    void strongScarsCarveRealDepth() {
        int riftColumns = 0;
        int deepest = 0;
        for (int x = -1200; x <= 1200; x += 7) {
            for (int z = -1200; z <= 1200; z += 9) {
                InnerTerrain.Sample sample = InnerTerrain.sample(0, 0, x, z);
                if (!sample.land() || sample.scarStrength() <= 0.70D) {
                    continue;
                }
                riftColumns++;
                // Compare against an unscarred neighbour estimate: the rift floor must sit well
                // below the base shelf the surrounding terrain rests on.
                int depthBelowBase = InnerDimensionConstants.BASE_Y - sample.topY();
                deepest = Math.max(deepest, depthBelowBase);
            }
        }
        assertTrue(riftColumns > 10, "expected rift columns on the island, found " + riftColumns);
        assertTrue(deepest >= 6, "rifts should cut visibly below the base shelf, deepest=" + deepest);
    }

    @Test
    void pathsNeverFallIntoARift() {
        for (int x = -1200; x <= 1200; x += 5) {
            for (int z = -1200; z <= 1200; z += 11) {
                InnerTerrain.Sample sample = InnerTerrain.sample(0, 0, x, z);
                if (sample.path() && sample.pathStrength() > 0.42D) {
                    // The corridor's raised, walkable grade must survive even on strong scar lines.
                    assertTrue(sample.topY() >= InnerDimensionConstants.BASE_Y,
                            "path corridor carved below grade at " + x + "," + z + " topY=" + sample.topY());
                }
            }
        }
    }
}
