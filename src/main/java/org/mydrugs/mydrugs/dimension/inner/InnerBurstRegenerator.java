package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;

final class InnerBurstRegenerator {
    private InnerBurstRegenerator() {
    }

    static InnerGenerationMetrics recreateOwnerNow(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island
    ) {
        InnerOverlayQueue.cancel(island.owner());
        int[] enqueued = {0};
        InnerOverlayQueue.forEachRecreateChunkCoordinate(
                island.centerX(),
                island.centerZ(),
                (chunkX, chunkZ) -> enqueued[0]++
        );

        long started = System.currentTimeMillis();
        int[] processed = {0};
        int[] placed = {0};
        int[] skipped = {0};
        InnerOverlayQueue.forEachRecreateChunkCoordinate(
                island.centerX(),
                island.centerZ(),
                (chunkX, chunkZ) -> {
                    ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                    level.getChunk(chunkX, chunkZ);
                    InnerPlacement.PlacementCount count = InnerOverlayQueue.recreateAndDecorateChunkNow(level, island, chunkPos);
                    processed[0]++;
                    placed[0] += count.placed();
                    skipped[0] += count.skipped();
                }
        );
        long elapsed = Math.max(0L, System.currentTimeMillis() - started);
        InnerGenerationMetrics metrics = new InnerGenerationMetrics(
                processed[0],
                enqueued[0],
                placed[0],
                skipped[0],
                elapsed
        );
        InnerDimensionSavedData.get(level).updateOverlayMetrics(
                island.owner(),
                "burst_recreate " + metrics.toDebugString()
        );
        return metrics;
    }
}
