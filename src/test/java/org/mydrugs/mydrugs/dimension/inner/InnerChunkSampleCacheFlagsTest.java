package org.mydrugs.mydrugs.dimension.inner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Builder early-out flags must be conservative supersets: whenever a column satisfies a
 * builder-relevant predicate, the corresponding chunk flag must be true. (A flag may be true
 * without any qualifying column — that only costs a scan — but a false negative would silently
 * drop features.)
 */
class InnerChunkSampleCacheFlagsTest {

    @Test
    void flagsAreNeverFalseWhenAColumnQualifies() {
        // Sweep a band of chunks crossing core, paths, lakes, scars, transition bands and coast.
        for (int chunkX = -40; chunkX <= 40; chunkX += 5) {
            for (int chunkZ = -40; chunkZ <= 40; chunkZ += 8) {
                assertFlagsConservative(chunkX << 4, chunkZ << 4);
            }
        }
    }

    private static void assertFlagsConservative(int minX, int minZ) {
        InnerChunkSampleCache cache = InnerChunkSampleCache.build(0, 0, minX, minZ);
        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                InnerTerrain.Sample s = cache.sample(localX, localZ);
                String at = "chunk(" + minX + "," + minZ + ") local(" + localX + "," + localZ + ")";
                if (s.land()) {
                    assertTrue(cache.anyLand(), "anyLand " + at);
                    assertTrue(cache.minLandTopY() <= s.topY(), "minLandTopY " + at);
                }
                if (s.land() && s.lake()) {
                    assertTrue(cache.anyLake(), "anyLake " + at);
                }
                if (s.scar() || s.hole()) {
                    assertTrue(cache.anyScarOrHole(), "anyScarOrHole " + at);
                }
                if (s.transitionZone()) {
                    assertTrue(cache.anyTransition(), "anyTransition " + at);
                }
                if (s.land() && (s.treeZone() || s.plantPatch()
                        || s.shoreStrength() > 0.0D || s.wetlandStrength() > 0.0D)) {
                    assertTrue(cache.anyGroveGround(), "anyGroveGround " + at);
                }
                if (s.satellite()) {
                    assertTrue(cache.anySatellite(), "anySatellite " + at);
                }
                if (s.spikeField() || s.holeStrength() > 0.10D || s.satellite()) {
                    assertTrue(cache.anySpikeCandidate(), "anySpikeCandidate " + at);
                }
                assertTrue(cache.maxPathStrength() >= s.pathStrength(), "maxPathStrength " + at);
                assertTrue(cache.maxCliffStrength() >= s.cliffStrength(), "maxCliffStrength " + at);
            }
        }
    }

    @Test
    void groveAndSceneAccessorsMatchDirectSamplerCalls() {
        int minX = 35 << 4;
        int minZ = 2 << 4;
        InnerChunkSampleCache cache = InnerChunkSampleCache.build(0, 0, minX, minZ);
        long seed = InnerTerrain.seedForSlot(0, 0);
        for (int localX = 0; localX < 16; localX += 3) {
            for (int localZ = 0; localZ < 16; localZ += 3) {
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                InnerGroveSample direct = InnerGroveSampler.sample(seed, 0, 0, minX + localX, minZ + localZ, sample);
                assertEquals(direct, cache.grove(localX, localZ), "grove memo must be value-identical");
                InnerSceneSample directScene =
                        InnerSceneSampler.sample(seed, 0, 0, minX + localX, minZ + localZ, sample, direct);
                assertEquals(directScene, cache.scene(localX, localZ), "scene memo must be value-identical");
                assertSame(cache.grove(localX, localZ), cache.grove(localX, localZ), "grove must be memoized");
                assertNotNull(cache.scene(localX, localZ));
            }
        }
    }
}
