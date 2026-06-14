package org.mydrugs.mydrugs.dimension.inner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Density, cap and determinism guarantees for the constellation glow pass (P8). */
class InnerGlowConstellationTest {

    @Test
    void glowNeverExceedsThePerChunkCap() {
        for (int chunkX = -40; chunkX <= 40; chunkX += 3) {
            for (int chunkZ = -40; chunkZ <= 40; chunkZ += 5) {
                int count = InnerGlowBuilder.glowDecisionCountForTest(0, 0, chunkX << 4, chunkZ << 4);
                assertTrue(count <= InnerDimensionConstants.MAX_GLOW_DETAILS_PER_CHUNK,
                        "glow cap exceeded at chunk " + chunkX + "," + chunkZ + ": " + count);
            }
        }
    }

    @Test
    void glowDecisionsAreDeterministicPerChunk() {
        for (int chunkX = -20; chunkX <= 20; chunkX += 7) {
            int minX = chunkX << 4;
            int minZ = (chunkX * 3) << 4;
            assertEquals(
                    InnerGlowBuilder.glowDecisionCountForTest(0, 0, minX, minZ),
                    InnerGlowBuilder.glowDecisionCountForTest(0, 0, minX, minZ),
                    "glow decisions must be deterministic at " + chunkX);
        }
    }

    @Test
    void someChunksHostConstellationsAndMostStayDark() {
        int litChunks = 0;
        int scanned = 0;
        for (int chunkX = -48; chunkX <= 48; chunkX += 2) {
            for (int chunkZ = -48; chunkZ <= 48; chunkZ += 2) {
                scanned++;
                if (InnerGlowBuilder.glowDecisionCountForTest(0, 0, chunkX << 4, chunkZ << 4) > 0) {
                    litChunks++;
                }
            }
        }
        assertTrue(litChunks > 0, "expected at least some constellations on the island");
        assertTrue(litChunks < scanned / 2,
                "constellations should be composed accents, not blanket lighting (" + litChunks + "/" + scanned + ")");
    }
}
