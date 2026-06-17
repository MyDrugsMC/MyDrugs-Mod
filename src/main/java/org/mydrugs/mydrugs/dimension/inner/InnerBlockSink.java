package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Single placement target for the inner-dimension visual builders, so each builder can be written
 * once and run on either the worldgen thread (chunk fill) or the server thread (overlay rebuild)
 * without duplicating its logic. The two implementations preserve the exact semantics each path
 * used before this abstraction existed:
 *
 * <ul>
 *   <li>{@link #forChunk} writes straight into a {@link ChunkAccess} during generation, guarded to
 *       the chunk's own column range and Y bounds (writes that fall outside the chunk are dropped —
 *       the neighbouring chunk's deterministic pass fills them in);</li>
 *   <li>{@link #forLevel} delegates to {@link InnerPlacement#safeSet}, so the overlay attempt
 *       budget, replace/skip rules and placed/skipped counting are unchanged.</li>
 * </ul>
 */
interface InnerBlockSink {
    /**
     * Place {@code state} at {@code pos}.
     *
     * @param replaceExisting whether already-generated terrain may be overwritten (maps to
     *                        {@link InnerPlacement#safeSet}'s {@code allowTerrainReplace}); ignored
     *                        by the chunk-fill sink, which always writes onto fresh terrain.
     * @return {@code true} if the block was placed.
     */
    boolean setBlock(BlockPos pos, BlockState state, boolean replaceExisting);

    /** Worldgen-thread sink writing into the chunk currently being generated. */
    static InnerBlockSink forChunk(ChunkAccess chunk) {
        return new ChunkBlockSink(chunk);
    }

    /** Server-thread sink delegating to {@link InnerPlacement#safeSet} with LIVE_OVERLAY mode. */
    static InnerBlockSink forLevel(ServerLevel level, InnerPlacement.MutablePlacementCount count) {
        return new LevelBlockSink(level, count, InnerPlacement.PlacementMode.LIVE_OVERLAY);
    }

    /** Server-thread sink delegating to {@link InnerPlacement#safeSet} with the given mode. */
    static InnerBlockSink forLevel(ServerLevel level, InnerPlacement.MutablePlacementCount count, InnerPlacement.PlacementMode mode) {
        return new LevelBlockSink(level, count, mode);
    }
}

final class ChunkBlockSink implements InnerBlockSink {
    private final ChunkAccess chunk;
    private final ChunkPos chunkPos;

    ChunkBlockSink(ChunkAccess chunk) {
        this.chunk = chunk;
        this.chunkPos = chunk.getPos();
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, boolean replaceExisting) {
        if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
            return false;
        }
        if (!chunkPos.equals(new ChunkPos(pos))) {
            // Belt-and-suspenders only: builders scan a margin around the chunk so neighbour
            // chunks independently re-derive the blocks they own, instead of relying on this
            // chunk's off-boundary writes to populate them.
            return false;
        }
        chunk.setBlockState(pos, state, 2);
        return true;
    }
}

final class LevelBlockSink implements InnerBlockSink {
    private final ServerLevel level;
    private final InnerPlacement.MutablePlacementCount count;
    private final InnerPlacement.PlacementMode mode;

    LevelBlockSink(ServerLevel level, InnerPlacement.MutablePlacementCount count, InnerPlacement.PlacementMode mode) {
        this.level = level;
        this.count = count;
        this.mode = mode;
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, boolean replaceExisting) {
        return InnerPlacement.safeSet(level, pos, state, replaceExisting, count, mode);
    }
}
