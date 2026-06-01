package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;

final class InnerChunkRebuilder {
    private InnerChunkRebuilder() {
    }

    static void recreateChunk(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            ChunkPos chunkPos,
            InnerPlacement.MutablePlacementCount count
    ) {
        InnerChunkSampleCache cache = InnerChunkSampleCache.build(
                island.centerX(),
                island.centerZ(),
                chunkPos.getMinBlockX(),
                chunkPos.getMinBlockZ()
        );
        clearChunk(level, chunkPos, cache, count);
        rebuildTerrain(level, chunkPos, cache, count);
    }

    // Destructive recreate clears from world floor through both the expected generated band and
    // the column's current occupied height, so stale tall trees/spikes do not survive a rebuild.
    private static final int CLEAR_HEADROOM = 48;
    private static final int CLEAR_HEIGHTMAP_MARGIN = 16;

    private static void clearChunk(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState air = Blocks.AIR.defaultBlockState();
        int floorY = level.getMinY();
        int ceilY = level.getMaxY() - 1;
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        for (int localX = 0; localX < 16; localX++) {
            int worldX = minX + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = minZ + localZ;
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                int lo = clearLowYForTest(floorY);
                int currentSurfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ);
                int hi = clearHighYForTest(sample.visualTopY(), currentSurfaceY, ceilY);
                for (int y = lo; y <= hi; y++) {
                    pos.set(worldX, y, worldZ);
                    if (level.getBlockState(pos).isAir()) {
                        continue;
                    }
                    level.setBlock(pos, air, InnerDimensionConstants.RECREATE_UPDATE_FLAGS);
                    count.recordPlaced();
                }
            }
        }
    }

    static int clearLowYForTest(int floorY) {
        return floorY;
    }

    static int clearHighYForTest(int sampledVisualTopY, int currentSurfaceY, int ceilY) {
        int expectedGeneratedTop = sampledVisualTopY + CLEAR_HEADROOM;
        int occupiedTop = currentSurfaceY + CLEAR_HEIGHTMAP_MARGIN;
        return Math.min(ceilY, Math.max(expectedGeneratedTop, occupiedTop));
    }

    private static void rebuildTerrain(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        for (int localX = 0; localX < 16; localX++) {
            int worldX = minX + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = minZ + localZ;
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                if (!sample.land()) {
                    continue;
                }
                for (int y = sample.bottomY(); y <= sample.topY(); y++) {
                    if (y < level.getMinY() || y >= level.getMaxY() || InnerTerrain.caveAir(sample, worldX, y, worldZ)) {
                        continue;
                    }
                    pos.set(worldX, y, worldZ);
                    BlockState state = InnerTerrain.stateFor(sample, worldX, y, worldZ);
                    if (level.getBlockState(pos).equals(state)) {
                        continue;
                    }
                    level.setBlock(pos, state, InnerDimensionConstants.RECREATE_UPDATE_FLAGS);
                    count.recordPlaced();
                }
                InnerLakeBuilder.fillLakeColumn(level, pos, sample, worldX, worldZ, count);
            }
        }
        InnerVisualFeatureBuilders.placeOverlayFeatures(level, chunkPos, cache, count);
    }
}
