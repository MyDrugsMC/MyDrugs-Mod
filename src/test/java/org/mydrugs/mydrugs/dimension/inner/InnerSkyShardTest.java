package org.mydrugs.mydrugs.dimension.inner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Sky-shard field guarantees (A1): determinism, band/annulus bounds, presence. */
class InnerSkyShardTest {

    @Test
    void shardsExistSomewhereAndStayInsideTheirBand() {
        int shardColumns = 0;
        for (int x = -1500; x <= 1500; x += 11) {
            for (int z = -1500; z <= 1500; z += 13) {
                InnerTerrain.Sample sample = InnerTerrain.sample(0, 0, x, z);
                if (!sample.skyLand()) {
                    continue;
                }
                shardColumns++;
                assertTrue(sample.skyBottomY() < sample.skyTopY(),
                        "inverted sky band at " + x + "," + z);
                assertTrue(sample.skyBottomY() >= InnerSkyShardSampler.MIN_ALTITUDE - 24,
                        "shard hangs too low at " + x + "," + z + ": " + sample.skyBottomY());
                assertTrue(sample.skyTopY() <= InnerSkyShardSampler.MAX_TOP,
                        "shard tops out too high at " + x + "," + z + ": " + sample.skyTopY());
                if (sample.land()) {
                    assertTrue(sample.skyBottomY() >= sample.topY() + InnerSkyShardSampler.GROUND_CLEARANCE,
                            "shard must float clear of the ground at " + x + "," + z
                                    + " (ground=" + sample.topY() + " shardBottom=" + sample.skyBottomY() + ")");
                }
            }
        }
        assertTrue(shardColumns > 50, "expected a meaningful shard field, found " + shardColumns + " columns");
    }

    @Test
    void noShardsOverTheSanctuary() {
        for (int x = -InnerDimensionConstants.CORE_RADIUS; x <= InnerDimensionConstants.CORE_RADIUS; x += 7) {
            for (int z = -InnerDimensionConstants.CORE_RADIUS; z <= InnerDimensionConstants.CORE_RADIUS; z += 9) {
                if (Math.hypot(x, z) > InnerDimensionConstants.CORE_RADIUS) {
                    continue;
                }
                assertFalse(InnerTerrain.sample(0, 0, x, z).skyLand(),
                        "shard over the sanctuary at " + x + "," + z);
            }
        }
    }

    @Test
    void shardSamplingIsDeterministic() {
        for (int x = 200; x <= 1200; x += 37) {
            InnerTerrain.Sample a = InnerTerrain.sample(0, 0, x, -x / 2);
            InnerTerrain.Sample b = InnerTerrain.sample(0, 0, x, -x / 2);
            assertEquals(a.sky(), b.sky(), "sky sample must be deterministic at x=" + x);
        }
    }

    @Test
    void wholeIsletSharesOneRegionIdentity() {
        // Find a shard column, then walk its neighbourhood: all connected shard columns within
        // islet radius must report the same drug identity.
        for (int x = -1400; x <= 1400; x += 17) {
            for (int z = -1400; z <= 1400; z += 19) {
                InnerTerrain.Sample sample = InnerTerrain.sample(0, 0, x, z);
                if (!sample.skyLand() || sample.sky().strength() < 0.8D) {
                    continue;
                }
                // Near the islet centre — neighbours inside the same islet share the identity.
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        InnerTerrain.Sample n = InnerTerrain.sample(0, 0, x + dx, z + dz);
                        if (n.skyLand()) {
                            assertEquals(sample.sky().drug(), n.sky().drug(),
                                    "islet mixes identities at " + (x + dx) + "," + (z + dz));
                        }
                    }
                }
                return; // one islet checked is enough
            }
        }
    }
}
