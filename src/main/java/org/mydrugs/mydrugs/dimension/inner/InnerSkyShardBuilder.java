package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Dresses the floating sky-shards (A1): crown flora or crystal teeth on top, and hanging
 * "drip" tails below the deepest tip — 1-3 blocks of strata ending in a glow node, so every
 * islet reads as a slow drop of the world falling upward. Same initial/overlay symmetry as
 * every other builder: decisions are pure functions of the cached samples.
 */
final class InnerSkyShardBuilder {
    private static final long DRIP_SALT = 0x4452_4950L;
    private static final int MARGIN = 0;

    private InnerSkyShardBuilder() {
    }

    static void place(InnerBlockSink sink, InnerChunkSampleCache cache, int minX, int minZ) {
        if (!cache.anySkyLand()) {
            return;
        }
        long seed = cache.seed();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                InnerSkyShardSample sky = sample.sky();
                if (!sky.land()) {
                    continue;
                }
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                long hash = InnerNoise.mix64(seed + DRIP_SALT
                        ^ (long) worldX * 0x5851_F42DL
                        ^ (long) worldZ * 0x1405_7B7EL);
                InnerTerrainProfile profile = InnerTerrainProfile.forDrug(sky.drug());

                // Crown: flora on soft islets, crystal teeth on crystalline ones.
                BlockState crown = InnerSkyShardSampler.crownState(sky, profile, hash);
                if (crown != null) {
                    sink.setBlock(new BlockPos(worldX, sky.topY() + 1, worldZ), crown, true);
                }

                // Hanging drip below the deepest part of the islet only (strength near centre).
                if (sky.strength() > 0.70D && (hash & 7L) == 0L) {
                    int dripLength = 1 + (int) ((hash >>> 8) & 3L); // 1..4
                    for (int i = 1; i <= dripLength; i++) {
                        BlockState drip = i == dripLength
                                ? profile.nodeState()
                                : profile.subsurfaceBlock();
                        sink.setBlock(new BlockPos(worldX, sky.bottomY() - i, worldZ), drip, true);
                    }
                }
            }
        }
    }
}
