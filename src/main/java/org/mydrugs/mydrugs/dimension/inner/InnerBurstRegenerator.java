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
        long[] maxChunkNanos = {0L};
        // Profile the whole burst: the resulting non-zero sampler counters in the chat output
        // double as proof that a freshly built jar is actually running.
        InnerGenerationProfiler.begin();
        InnerGenerationProfiler.Counters counters;
        try {
            // Two passes, mirroring the queued recreate: rebuild ALL terrain first, then decorate.
            // A single interleaved pass placed the multi-chunk sanctuary disc while later chunks
            // were still being cleared, erasing the half that landed in not-yet-rebuilt chunks —
            // the recreate "semicircle" bug. Decorating only after every chunk's terrain is final
            // lets the full disc survive.
            InnerOverlayQueue.forEachRecreateChunkCoordinate(
                    island.centerX(),
                    island.centerZ(),
                    (chunkX, chunkZ) -> {
                        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                        level.getChunk(chunkX, chunkZ);
                        long chunkStart = System.nanoTime();
                        InnerPlacement.PlacementCount count = InnerOverlayQueue.rebuildChunkTerrainNow(level, island, chunkPos);
                        maxChunkNanos[0] = Math.max(maxChunkNanos[0], System.nanoTime() - chunkStart);
                        processed[0]++;
                        placed[0] += count.placed();
                        skipped[0] += count.skipped();
                    }
            );
            InnerOverlayQueue.forEachRecreateChunkCoordinate(
                    island.centerX(),
                    island.centerZ(),
                    (chunkX, chunkZ) -> {
                        ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
                        long chunkStart = System.nanoTime();
                        InnerPlacement.PlacementCount count = InnerOverlayQueue.decorateChunkNow(level, island, chunkPos);
                        maxChunkNanos[0] = Math.max(maxChunkNanos[0], System.nanoTime() - chunkStart);
                        processed[0]++;
                        placed[0] += count.placed();
                        skipped[0] += count.skipped();
                    }
            );
        } finally {
            counters = InnerGenerationProfiler.end();
        }
        long elapsed = Math.max(0L, System.currentTimeMillis() - started);
        InnerGenerationMetrics metrics = counters == null
                ? new InnerGenerationMetrics(processed[0], enqueued[0], placed[0], skipped[0], elapsed)
                : new InnerGenerationMetrics(
                        processed[0],
                        enqueued[0],
                        placed[0],
                        skipped[0],
                        elapsed,
                        counters.sampleComputes,
                        counters.groveComputes,
                        counters.sceneComputes,
                        maxChunkNanos[0] / 1_000_000L,
                        counters.builderNanos.clone()
                );
        InnerDimensionSavedData.get(level).updateOverlayMetrics(
                island.owner(),
                "burst_recreate " + metrics.toDebugString()
        );
        return metrics;
    }
}
