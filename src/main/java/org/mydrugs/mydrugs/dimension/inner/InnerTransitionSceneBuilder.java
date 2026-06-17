package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class InnerTransitionSceneBuilder {
    // Demoted (was 0.24 / 0.32): the palette dither in stateFor now does most of the edge work.
    private static final double TRANSITION_BASE_CHANCE = 0.10D;
    private static final double TRANSITION_STRENGTH_CHANCE = 0.16D;
    private static final int MARGIN = 2;

    private InnerTransitionSceneBuilder() {
    }

    static void place(InnerBlockSink sink, InnerChunkSampleCache cache, int minX, int minZ) {
        int islandCenterX = cache.islandCenterX();
        int islandCenterZ = cache.islandCenterZ();
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        int placed = 0;
        boolean thresholdMarkersPlaced = false;
        for (int worldZ = minZ - MARGIN; worldZ < minZ + 16 + MARGIN && placed < 8; worldZ++) {
            if (!InnerChunkSampleCache.chunkLocalCandidate(worldZ, 1, 16, 3)) {
                continue;
            }
            for (int worldX = minX - MARGIN; worldX < minX + 16 + MARGIN && placed < 8; worldX++) {
                if (!InnerChunkSampleCache.chunkLocalCandidate(worldX, 1, 16, 3)) {
                    continue;
                }
                InnerTerrain.Sample sample = cache.sampleAt(worldX, worldZ);
                if (!thresholdMarkersPlaced && isPathThresholdColumn(sample)) {
                    long thresholdHash = InnerNoise.mix64(seed
                            ^ 0x7448_7253L
                            ^ (long) worldX * 0x41C6_4E6DL
                            ^ (long) worldZ * 0x6595_27F5L);
                    if ((thresholdHash & 7L) == 0L) {
                        buildPathThreshold(worldX, worldZ, sample, sink);
                        thresholdMarkersPlaced = true;
                    }
                }
                if (!canHostTransitionScene(sample)) {
                    continue;
                }
                InnerGroveSample grove = cache.groveAt(worldX, worldZ, sample);
                InnerSceneSample scene = cache.sceneAt(worldX, worldZ, sample, grove);
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
                buildTransitionCluster(worldX, worldZ, sample, hash, sink);
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
    private static void buildPathThreshold(int x, int z, InnerTerrain.Sample sample, InnerBlockSink sink) {
        int y = sample.topY() + 1;
        BlockState pillar = InnerTransitionPalette.surfaceAccent(sample);
        BlockState cap = InnerTransitionPalette.glowAccent(sample);
        for (int side = -2; side <= 2; side += 4) {
            sink.setBlock(new BlockPos(x + side, y, z), pillar, true);
            sink.setBlock(new BlockPos(x + side, y + 1, z), cap, true);
            sink.setBlock(new BlockPos(x, y, z + side), pillar, true);
            sink.setBlock(new BlockPos(x, y + 1, z + side), cap, true);
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
            InnerBlockSink sink
    ) {
        int y = sample.lake() ? sample.lakeSurfaceY() : sample.topY();
        BlockState path = InnerTransitionPalette.pathAccent(sample);
        BlockState glow = InnerTransitionPalette.glowAccent(sample);
        BlockState flora = InnerTransitionPalette.floraAccent(sample);
        sink.setBlock(new BlockPos(x, y, z), path, true);
        if (sample.pathStrength() < 0.58D && !sample.lake()) {
            sink.setBlock(new BlockPos(x + 1, y, z), InnerTransitionPalette.surfaceAccent(sample), true);
            sink.setBlock(new BlockPos(x, y, z + 1), InnerTransitionPalette.shoreAccent(sample), true);
        }
        if ((hash & 3L) != 0L) {
            sink.setBlock(new BlockPos(x, y + 1, z), flora, true);
        }
        if (((hash >>> 4) & 3L) == 0L || sample.transitionStrength() > 0.42D) {
            if (glow.getBlock() == Blocks.LANTERN || glow.getBlock() == Blocks.SOUL_LANTERN) {
                sink.setBlock(new BlockPos(x - 1, y, z), path, true);
                sink.setBlock(new BlockPos(x - 1, y + 1, z), glow, true);
            } else {
                sink.setBlock(new BlockPos(x - 1, y, z), glow, true);
            }
        }
    }
}
