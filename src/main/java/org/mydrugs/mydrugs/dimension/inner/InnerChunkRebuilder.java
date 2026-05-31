package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
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
        clearChunk(level, chunkPos, count);
        rebuildTerrain(level, island, chunkPos, count);
    }

    private static void clearChunk(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerPlacement.MutablePlacementCount count
    ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        BlockState air = Blocks.AIR.defaultBlockState();
        int minY = level.getMinY();
        int maxY = level.getMaxY() - 1;
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        for (int localX = 0; localX < 16; localX++) {
            int worldX = minX + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = minZ + localZ;
                for (int y = minY; y <= maxY; y++) {
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

    private static void rebuildTerrain(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            ChunkPos chunkPos,
            InnerPlacement.MutablePlacementCount count
    ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        for (int localX = 0; localX < 16; localX++) {
            int worldX = minX + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = minZ + localZ;
                InnerTerrain.Sample sample = InnerTerrain.sample(island.centerX(), island.centerZ(), worldX, worldZ);
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
            }
        }
    }
}
