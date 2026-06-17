package org.mydrugs.mydrugs.dimension.inner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class InnerBuilderParityTest {
    private static final int CENTER_X = 0;
    private static final int CENTER_Z = 0;

    @Test
    void treeBuilderNeighborChunkSelectsBorderAnchorIndependently() {
        for (int chunkZ = -54; chunkZ <= 54; chunkZ++) {
            for (int chunkX = -54; chunkX <= 54; chunkX++) {
                int minX = chunkX << 4;
                int minZ = chunkZ << 4;
                InnerChunkSampleCache ownerCache = buildCache(minX, minZ);

                for (int worldZ = minZ; worldZ < minZ + 16; worldZ++) {
                    for (int worldX = minX + 14; worldX < minX + 16; worldX++) {
                        if (!selectedTreeAnchor(ownerCache, minX, minZ, worldX, worldZ)) {
                            continue;
                        }

                        int eastMinX = minX + 16;
                        InnerChunkSampleCache eastCache = buildCache(eastMinX, minZ);
                        assertTrue(InnerTreeBuilder.scansTreeAnchorForTest(eastMinX, minZ, worldX, worldZ),
                                "east neighbor did not scan owner chunk tree anchor at " + worldX + "," + worldZ);
                        assertTrue(selectedTreeAnchor(eastCache, eastMinX, minZ, worldX, worldZ),
                                "east neighbor did not independently select owner chunk tree anchor at " + worldX + "," + worldZ);
                        return;
                    }
                }
            }
        }
        fail("expected at least one deterministic selected tree anchor within the east border margin");
    }

    private static InnerChunkSampleCache buildCache(int minX, int minZ) {
        InnerTerrain.beginCachePass();
        try {
            return InnerChunkSampleCache.build(CENTER_X, CENTER_Z, minX, minZ);
        } finally {
            InnerTerrain.endCachePass();
        }
    }

    private static boolean selectedTreeAnchor(InnerChunkSampleCache cache, int minX, int minZ, int worldX, int worldZ) {
        InnerTerrain.beginCachePass();
        try {
            return InnerTreeBuilder.selectedTreeAnchorForTest(cache, minX, minZ, worldX, worldZ);
        } finally {
            InnerTerrain.endCachePass();
        }
    }
}
