package org.mydrugs.mydrugs.dimension.inner;

public final class InnerChunkSampleCache {
    private final InnerTerrain.Sample[][] samples = new InnerTerrain.Sample[16][16];

    private InnerChunkSampleCache() {
    }

    public static InnerChunkSampleCache build(int centerX, int centerZ, int chunkMinX, int chunkMinZ) {
        InnerChunkSampleCache cache = new InnerChunkSampleCache();
        for (int localX = 0; localX < 16; localX++) {
            int worldX = chunkMinX + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                cache.samples[localX][localZ] = InnerTerrain.sample(centerX, centerZ, worldX, chunkMinZ + localZ);
            }
        }
        return cache;
    }

    public InnerTerrain.Sample sample(int localX, int localZ) {
        return samples[localX][localZ];
    }
}
