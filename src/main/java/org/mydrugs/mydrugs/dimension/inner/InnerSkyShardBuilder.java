package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Dresses the floating sky-shards (A1): crown flora or crystal teeth on top, and hanging
 * "drip" tails below the deepest tip — 1-3 blocks of strata ending in a glow node, so every
 * islet reads as a slow drop of the world falling upward. Same initial/overlay symmetry as
 * every other builder: decisions are pure functions of the cached samples.
 */
final class InnerSkyShardBuilder {
    private static final long DRIP_SALT = 0x4452_4950L;

    private InnerSkyShardBuilder() {
    }

    static void placeInitialSkyShardDetails(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        placeSkyShardDetails(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), (pos, state) -> {
            if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
                return;
            }
            if (!chunkPos.equals(new ChunkPos(pos))) {
                return;
            }
            chunk.setBlockState(pos, state, 2);
        });
    }

    static void placeOverlaySkyShardDetails(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeSkyShardDetails(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
                (pos, state) -> InnerPlacement.safeSet(level, pos, state, true, count));
    }

    private static void placeSkyShardDetails(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
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
                    setter.set(new BlockPos(worldX, sky.topY() + 1, worldZ), crown);
                }

                // Hanging drip below the deepest part of the islet only (strength near centre).
                if (sky.strength() > 0.70D && (hash & 7L) == 0L) {
                    int dripLength = 1 + (int) ((hash >>> 8) & 3L); // 1..4
                    for (int i = 1; i <= dripLength; i++) {
                        BlockState drip = i == dripLength
                                ? profile.nodeState()
                                : profile.subsurfaceBlock();
                        setter.set(new BlockPos(worldX, sky.bottomY() - i, worldZ), drip);
                    }
                }
            }
        }
    }

    private interface BlockSetter {
        void set(BlockPos pos, BlockState state);
    }
}
