package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Carves narrow watercourses through low valleys so water connects downhill rather than only sitting
 * in isolated lake bowls (Section 4a).
 *
 * <p>Deterministic and per-column (no path tracing): a river is the set of columns near a zero
 * crossing of a low-frequency noise field — a continuous winding line — restricted to low valley
 * ground. Because valleys are where the terrain is already lowest, these channels naturally run
 * toward and between lakes and the coast, where they meet existing water. Each river column has its
 * surface replaced with a still water source and the block above cleared, leaving a flanked ribbon
 * of water rather than a flooding torrent.
 */
final class InnerRiverBuilder {
    private static final long RIVER_SALT = 0x52_4956_4552L;
    private static final double RIVER_SCALE = 200.0D;
    // |noise| below this (near a zero crossing) is river — small value keeps channels narrow.
    private static final double RIVER_HALF_WIDTH = 0.06D;
    // Only carve in low valley ground so rivers descend and connect basins.
    private static final int RIVER_MAX_TOP_Y = InnerDimensionConstants.BASE_Y + 8;

    private InnerRiverBuilder() {
    }

    static void placeInitialRivers(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        placeRivers(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), (pos, state) -> {
            if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
                return;
            }
            if (!chunkPos.equals(new ChunkPos(pos))) {
                return;
            }
            chunk.setBlockState(pos, state, 2);
        });
    }

    static void placeOverlayRivers(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeRivers(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
                (pos, state) -> InnerPlacement.safeSet(level, pos, state, true, count));
    }

    private static void placeRivers(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        int centerX = InnerTerrain.slotCenter(minX + 8);
        int centerZ = InnerTerrain.slotCenter(minZ + 8);
        long seed = InnerTerrain.seedForSlot(centerX, centerZ);
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                if (!isRiver(sample, seed, worldX, worldZ)) {
                    continue;
                }
                int y = sample.topY();
                setter.set(new BlockPos(worldX, y, worldZ), Blocks.WATER.defaultBlockState());
                setter.set(new BlockPos(worldX, y + 1, worldZ), Blocks.AIR.defaultBlockState());
            }
        }
    }

    /** Pure, deterministic river predicate. Exposed for testing. */
    static boolean isRiver(InnerTerrain.Sample sample, long seed, int worldX, int worldZ) {
        if (!sample.land()
                || sample.lake()
                || sample.hole()
                || sample.path()
                || sample.scar()
                || sample.topY() > RIVER_MAX_TOP_Y
                || sample.distanceFromCenter() < InnerDimensionConstants.CORE_RADIUS + 40.0D) {
            return false;
        }
        double n = InnerNoise.fbm(seed + RIVER_SALT, worldX, worldZ, RIVER_SCALE, 4);
        return Math.abs(n) < RIVER_HALF_WIDTH;
    }

    private interface BlockSetter {
        void set(BlockPos pos, BlockState state);
    }
}
