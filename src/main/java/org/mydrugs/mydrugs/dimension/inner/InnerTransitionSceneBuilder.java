package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

final class InnerTransitionSceneBuilder {
    private InnerTransitionSceneBuilder() {
    }

    static void placeInitialTransitionScenes(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        placeTransitionScenes(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), (pos, state) -> {
            if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
                return;
            }
            if (!chunkPos.equals(new ChunkPos(pos))) {
                return;
            }
            chunk.setBlockState(pos, state, 2);
        });
    }

    static void placeOverlayTransitionScenes(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeTransitionScenes(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
                (pos, state) -> InnerPlacement.safeSet(level, pos, state, true, count));
    }

    private static void placeTransitionScenes(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        int islandCenterX = InnerTerrain.slotCenter(minX + 8);
        int islandCenterZ = InnerTerrain.slotCenter(minZ + 8);
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        int placed = 0;
        for (int localZ = 1; localZ < 16 && placed < 8; localZ += 3) {
            for (int localX = 1; localX < 16 && placed < 8; localX += 3) {
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                if (!canHostTransitionScene(sample)) {
                    continue;
                }
                InnerGroveSample grove = InnerGroveSampler.sample(seed, islandCenterX, islandCenterZ, worldX, worldZ, sample);
                InnerSceneSample scene = InnerSceneSampler.sample(seed, islandCenterX, islandCenterZ, worldX, worldZ, sample, grove);
                if (scene.type() != InnerSceneType.TRANSITION_GARDEN && sample.transitionStrength() < 0.40D) {
                    continue;
                }
                long hash = InnerNoise.mix64(seed
                        ^ (long) worldX * 0x19D3_55A7L
                        ^ (long) worldZ * 0x68E7_1D31L);
                if ((hash & 1023L) > (0.24D + sample.transitionStrength() * 0.32D) * 1024.0D) {
                    continue;
                }
                buildTransitionCluster(worldX, worldZ, sample, hash, setter);
                placed++;
            }
        }
    }

    private static boolean canHostTransitionScene(InnerTerrain.Sample sample) {
        return sample.land()
                && sample.transitionZone()
                && sample.transitionStrength() > 0.24D
                && !sample.hole()
                && sample.distanceFromCenter() > InnerDimensionConstants.CORE_RADIUS + 36.0D
                && InnerTransitionPalette.hasExplicitHybrid(sample.primaryDrug(), sample.secondaryDrug());
    }

    private static void buildTransitionCluster(
            int x,
            int z,
            InnerTerrain.Sample sample,
            long hash,
            BlockSetter setter
    ) {
        int y = sample.lake() ? sample.lakeSurfaceY() : sample.topY();
        BlockState path = InnerTransitionPalette.pathAccent(sample);
        BlockState glow = InnerTransitionPalette.glowAccent(sample);
        BlockState flora = InnerTransitionPalette.floraAccent(sample);
        setter.set(new BlockPos(x, y, z), path);
        if (sample.pathStrength() < 0.58D && !sample.lake()) {
            setter.set(new BlockPos(x + 1, y, z), InnerTransitionPalette.surfaceAccent(sample));
            setter.set(new BlockPos(x, y, z + 1), InnerTransitionPalette.shoreAccent(sample));
        }
        if ((hash & 3L) != 0L) {
            setter.set(new BlockPos(x, y + 1, z), flora);
        }
        if (((hash >>> 4) & 3L) == 0L || sample.transitionStrength() > 0.42D) {
            if (glow.getBlock() == Blocks.LANTERN || glow.getBlock() == Blocks.SOUL_LANTERN) {
                setter.set(new BlockPos(x - 1, y, z), path);
                setter.set(new BlockPos(x - 1, y + 1, z), glow);
            } else {
                setter.set(new BlockPos(x - 1, y, z), glow);
            }
        }
    }

    private interface BlockSetter {
        void set(BlockPos pos, BlockState state);
    }
}
