package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

final class InnerTransitionSceneBuilder {
    // Demoted (was 0.24 / 0.32): the palette dither in stateFor now does most of the edge work.
    private static final double TRANSITION_BASE_CHANCE = 0.10D;
    private static final double TRANSITION_STRENGTH_CHANCE = 0.16D;

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
        if (!cache.anyTransition()) {
            return;
        }
        int islandCenterX = InnerTerrain.slotCenter(minX + 8);
        int islandCenterZ = InnerTerrain.slotCenter(minZ + 8);
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        int placed = 0;
        boolean thresholdMarkersPlaced = false;
        for (int localZ = 1; localZ < 16 && placed < 8; localZ += 3) {
            for (int localX = 1; localX < 16 && placed < 8; localX += 3) {
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                if (!thresholdMarkersPlaced && isPathThresholdColumn(sample)) {
                    long thresholdHash = InnerNoise.mix64(seed
                            ^ 0x7448_7253L
                            ^ (long) worldX * 0x41C6_4E6DL
                            ^ (long) worldZ * 0x6595_27F5L);
                    if ((thresholdHash & 7L) == 0L) {
                        buildPathThreshold(worldX, worldZ, sample, setter);
                        thresholdMarkersPlaced = true;
                    }
                }
                if (!canHostTransitionScene(sample)) {
                    continue;
                }
                InnerSceneSample scene = cache.scene(localX, localZ);
                if (scene.type() != InnerSceneType.TRANSITION_GARDEN && sample.transitionStrength() < 0.40D) {
                    continue;
                }
                long hash = InnerNoise.mix64(seed
                        ^ (long) worldX * 0x19D3_55A7L
                        ^ (long) worldZ * 0x68E7_1D31L);
                // Demoted: region edges are handled mostly by the dithered palette blend in
                // InnerTerrain.stateFor; this standalone cluster is now a rare accent on top.
                if ((hash & 1023L) > (TRANSITION_BASE_CHANCE + sample.transitionStrength() * TRANSITION_STRENGTH_CHANCE) * 1024.0D) {
                    continue;
                }
                buildTransitionCluster(worldX, worldZ, sample, hash, setter);
                placed++;
            }
        }
    }

    /** A path column crossing the heart of a region boundary — the natural place for a gate. */
    private static boolean isPathThresholdColumn(InnerTerrain.Sample sample) {
        return sample.land()
                && sample.path()
                && sample.pathStrength() > 0.50D
                && sample.transitionZone()
                && sample.transitionStrength() > 0.50D
                && !sample.lake()
                && !sample.hole()
                && sample.distanceFromCenter() > InnerDimensionConstants.CORE_RADIUS + 36.0D
                && InnerTransitionPalette.hasExplicitHybrid(sample.primaryDrug(), sample.secondaryDrug());
    }

    /**
     * Paired threshold markers flanking the path where it crosses a region boundary (P10):
     * two short two-block pillars in the hybrid palette, one per side, so walking between
     * regions reads as passing through a gate.
     */
    private static void buildPathThreshold(int x, int z, InnerTerrain.Sample sample, BlockSetter setter) {
        int y = sample.topY() + 1;
        BlockState pillar = InnerTransitionPalette.surfaceAccent(sample);
        BlockState cap = InnerTransitionPalette.glowAccent(sample);
        for (int side = -2; side <= 2; side += 4) {
            setter.set(new BlockPos(x + side, y, z), pillar);
            setter.set(new BlockPos(x + side, y + 1, z), cap);
            setter.set(new BlockPos(x, y, z + side), pillar);
            setter.set(new BlockPos(x, y + 1, z + side), cap);
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
