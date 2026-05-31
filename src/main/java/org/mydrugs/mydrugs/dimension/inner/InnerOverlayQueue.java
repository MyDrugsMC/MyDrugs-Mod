package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.InnerDimensions;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class InnerOverlayQueue {
    private static final Map<UUID, QueueState> QUEUES = new LinkedHashMap<>();
    private static final Map<UUID, InnerGenerationMetrics> LAST_METRICS = new LinkedHashMap<>();

    private InnerOverlayQueue() {
    }

    public static void enqueueEntryRefresh(InnerDimensionSavedData.IslandState island) {
        if (island == null || island.owner() == null) {
            return;
        }
        ChunkCollector chunks = new ChunkCollector();
        addSquare(chunks, island.centerX() >> 4, island.centerZ() >> 4, 2);
        appendQueue(island.owner(), chunks);
    }

    public static void enqueueIntegrationAwakening(InnerDimensionSavedData.IslandState island, DrugId drugId) {
        if (island == null || island.owner() == null || drugId == null) {
            return;
        }
        ChunkCollector chunks = new ChunkCollector();
        addSquare(chunks, island.centerX() >> 4, island.centerZ() >> 4, 1);
        addLandmarkPatch(chunks, island, drugId, 4);
        addPathChunks(chunks, island, drugId);
        appendQueue(island.owner(), chunks);
    }

    public static InnerRefreshJob enqueueOwnerOverlayRefresh(InnerDimensionSavedData.IslandState island) {
        if (island == null || island.owner() == null) {
            return InnerRefreshJob.empty();
        }
        ChunkCollector chunks = new ChunkCollector();
        addSquare(chunks, island.centerX() >> 4, island.centerZ() >> 4, 2);
        for (DrugId drugId : CuratedDrugChain.ORDER) {
            addLandmarkPatch(chunks, island, drugId, 4);
            if (island.integratedDrugs().contains(drugId)) {
                addPathChunks(chunks, island, drugId);
            }
        }
        boolean replaced = QUEUES.put(island.owner(), new QueueState(chunks.chunks(), chunks.keys(), QueueMode.OVERLAY)) != null;
        return new InnerRefreshJob(island.owner(), chunks.size(), replaced);
    }

    public static InnerRefreshJob enqueueOwnerFullRecreate(InnerDimensionSavedData.IslandState island) {
        if (island == null || island.owner() == null) {
            return InnerRefreshJob.empty();
        }
        ChunkCollector chunks = new ChunkCollector();
        addRecreateChunks(chunks, island);
        boolean replaced = QUEUES.put(island.owner(), new QueueState(chunks.chunks(), chunks.keys(), QueueMode.FULL_RECREATE)) != null;
        return new InnerRefreshJob(island.owner(), chunks.size(), replaced);
    }

    /**
     * B1: enqueue a single freshly-loaded chunk for idempotent decoration. Placement is gated by
     * the marker system (B2) and {@code safeSet}, so re-processing an already-dressed chunk is a
     * no-op. Appends to the owner's existing overlay queue or starts a new one.
     */
    public static void enqueueChunkDecoration(InnerDimensionSavedData.IslandState island, ChunkPos chunkPos) {
        if (island == null || island.owner() == null || chunkPos == null) {
            return;
        }
        ChunkCollector chunks = new ChunkCollector();
        chunks.add(chunkPos);
        appendQueue(island.owner(), chunks);
    }

    public static boolean cancel(UUID owner) {
        return QUEUES.remove(owner) != null;
    }

    public static String queueStatus(UUID owner) {
        QueueState state = QUEUES.get(owner);
        if (state == null) {
            InnerGenerationMetrics metrics = LAST_METRICS.getOrDefault(owner, InnerGenerationMetrics.EMPTY);
            return "Inner Dimension overlay queue idle. Last metrics: " + metrics.toDebugString();
        }
        return "Inner Dimension overlay queue owner=" + owner
                + " mode=" + state.mode.id
                + " remaining=" + state.remaining()
                + ", processed=" + state.processedChunks
                + ", placed=" + state.placedBlocks
                + ", skipped=" + state.skippedBlocks;
    }

    public static String lastMetricsFor(UUID owner) {
        return LAST_METRICS.getOrDefault(owner, InnerGenerationMetrics.EMPTY).toDebugString();
    }

    public static int remainingFor(UUID owner) {
        QueueState state = QUEUES.get(owner);
        return state == null ? 0 : state.remaining();
    }

    public static int deduplicatedChunkCountForTest(List<ChunkPos> chunks) {
        ChunkCollector collector = new ChunkCollector();
        for (ChunkPos chunk : chunks) {
            collector.add(chunk);
        }
        return collector.size();
    }

    public static int deduplicatedChunkCoordinateCountForTest(int[][] chunks) {
        Set<Long> keys = new LinkedHashSet<>();
        for (int[] chunk : chunks) {
            if (chunk.length >= 2) {
                keys.add(chunkKey(chunk[0], chunk[1]));
            }
        }
        return keys.size();
    }

    /**
     * B1: when a chunk loads in the inner dimension, enqueue it for decoration so the owner's
     * island is dressed regardless of how the player arrived (no reliance on an entry/integration
     * event having fired). The event fires before the chunk is promoted to FULL, so we must not
     * touch the level here — enqueueing only defers the work to {@link #onLevelTick}.
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(InnerDimensions.INNER_LEVEL)) {
            return;
        }
        ChunkPos chunkPos = event.getChunk().getPos();
        if (!InnerTerrain.chunkMayHaveLand(chunkPos.getMinBlockX(), chunkPos.getMinBlockZ())) {
            return;
        }
        int centerX = InnerTerrain.slotCenter(chunkPos.getMiddleBlockX());
        int centerZ = InnerTerrain.slotCenter(chunkPos.getMiddleBlockZ());
        InnerDimensionSavedData.IslandState island = InnerDimensionSavedData.get(level).findIslandBySlot(centerX, centerZ);
        if (island != null) {
            enqueueChunkDecoration(island, chunkPos);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(InnerDimensions.INNER_LEVEL)) {
            return;
        }
        if (QUEUES.isEmpty()) {
            return;
        }

        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        int chunksLeftThisTick = InnerDimensionConstants.OVERLAY_CHUNKS_PER_TICK;
        var iterator = QUEUES.entrySet().iterator();
        while (iterator.hasNext() && chunksLeftThisTick > 0) {
            Map.Entry<UUID, QueueState> entry = iterator.next();
            QueueState state = entry.getValue();
            InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(entry.getKey());
            int queueBudget = state.mode == QueueMode.FULL_RECREATE
                    ? Math.min(chunksLeftThisTick, InnerDimensionConstants.RECREATE_CHUNKS_PER_TICK)
                    : chunksLeftThisTick;
            while (queueBudget > 0 && !state.chunks.isEmpty()) {
                ChunkPos chunkPos = state.pollNext();
                level.getChunk(chunkPos.x, chunkPos.z);
                InnerPlacement.PlacementCount count = placeChunk(level, island, chunkPos, state.mode);
                state.processedChunks++;
                state.placedBlocks += count.placed();
                state.skippedBlocks += count.skipped();
                chunksLeftThisTick--;
                queueBudget--;
            }
            if (state.chunks.isEmpty()) {
                long elapsed = Math.max(0L, System.currentTimeMillis() - state.startedMillis);
                InnerGenerationMetrics metrics = new InnerGenerationMetrics(
                        state.processedChunks,
                        state.enqueuedChunks,
                        state.placedBlocks,
                        state.skippedBlocks,
                        elapsed
                );
                LAST_METRICS.put(entry.getKey(), metrics);
                data.updateOverlayMetrics(entry.getKey(), metrics.toDebugString());
                iterator.remove();
            }
        }
    }

    private static InnerPlacement.PlacementCount placeChunk(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            ChunkPos chunkPos,
            QueueMode mode
    ) {
        InnerPlacement.MutablePlacementCount count = new InnerPlacement.MutablePlacementCount();
        // B4: memoize terrain samples for the duration of this single-threaded pass so the
        // rebuilder, sanctuary/landmark builders, decorator and surfaceTop share one lookup
        // per column instead of recomputing the warped sample each time.
        InnerTerrain.beginCachePass();
        try {
            if (mode == QueueMode.FULL_RECREATE) {
                InnerChunkRebuilder.recreateChunk(level, island, chunkPos, count);
            }
            int centerChunkX = island.centerX() >> 4;
            int centerChunkZ = island.centerZ() >> 4;
            if (chunkPos.x == centerChunkX && chunkPos.z == centerChunkZ) {
                InnerSanctuaryBuilder.placeCenterSanctuary(level, island, count);
            }

            InnerDecorator.decoratePathChunk(level, chunkPos, count);
            for (DrugId drugId : CuratedDrugChain.ORDER) {
                BlockPos landmark = InnerRegionMap.landmarkFor(island.centerX(), island.centerZ(), drugId);
                ChunkPos landmarkChunk = new ChunkPos(landmark);
                int dx = Math.abs(chunkPos.x - landmarkChunk.x);
                int dz = Math.abs(chunkPos.z - landmarkChunk.z);
                if (dx > 4 || dz > 4) {
                    continue;
                }
                boolean unlocked = island.integratedDrugs().contains(drugId);
                if (chunkPos.x == landmarkChunk.x && chunkPos.z == landmarkChunk.z) {
                    InnerLandmarkBuilder.placeLandmark(level, island, drugId, unlocked, count);
                }
                if (unlocked) {
                    InnerDecorator.decorateRegionAwakening(level, chunkPos, drugId, true, count);
                }
            }
        } finally {
            InnerTerrain.endCachePass();
        }
        return count.freeze();
    }

    private static void appendQueue(UUID owner, ChunkCollector chunks) {
        QUEUES.compute(owner, (id, existing) -> existing == null
                ? new QueueState(chunks.chunks(), chunks.keys(), QueueMode.OVERLAY)
                : existing.append(chunks));
    }

    private static void addSquare(ChunkCollector chunks, int centerChunkX, int centerChunkZ, int radius) {
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                chunks.add(new ChunkPos(centerChunkX + dx, centerChunkZ + dz));
            }
        }
    }

    private static void addLandmarkPatch(
            ChunkCollector chunks,
            InnerDimensionSavedData.IslandState island,
            DrugId drugId,
            int radius
    ) {
        ChunkPos landmarkChunk = new ChunkPos(InnerRegionMap.landmarkFor(island.centerX(), island.centerZ(), drugId));
        addSquare(chunks, landmarkChunk.x, landmarkChunk.z, radius);
    }

    private static void addPathChunks(
            ChunkCollector chunks,
            InnerDimensionSavedData.IslandState island,
            DrugId drugId
    ) {
        BlockPos landmark = InnerRegionMap.landmarkFor(island.centerX(), island.centerZ(), drugId);
        int dx = landmark.getX() - island.centerX();
        int dz = landmark.getZ() - island.centerZ();
        int steps = Math.max(Math.abs(dx), Math.abs(dz)) / 8;
        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0.0D : i / (double) steps;
            int x = island.centerX() + (int) Math.round(dx * t);
            int z = island.centerZ() + (int) Math.round(dz * t);
            chunks.add(new ChunkPos(x >> 4, z >> 4));
        }
    }

    private static void addRecreateChunks(ChunkCollector chunks, InnerDimensionSavedData.IslandState island) {
        addRecreateChunks(chunks, island.centerX(), island.centerZ());
    }

    private static void addRecreateChunks(ChunkCollector chunks, int centerX, int centerZ) {
        forEachRecreateChunkCoordinate(centerX, centerZ, (chunkX, chunkZ) -> chunks.add(new ChunkPos(chunkX, chunkZ)));
    }

    private static void forEachRecreateChunkCoordinate(int centerX, int centerZ, BiConsumer<Integer, Integer> consumer) {
        int centerChunkX = centerX >> 4;
        int centerChunkZ = centerZ >> 4;
        int radius = InnerDimensionConstants.FULL_RECREATE_CHUNK_RADIUS;
        long radiusSq = (long) InnerDimensionConstants.FULL_RECREATE_BLOCK_RADIUS
                * InnerDimensionConstants.FULL_RECREATE_BLOCK_RADIUS;
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int chunkCenterX = ((centerChunkX + dx) << 4) + 8;
                int chunkCenterZ = ((centerChunkZ + dz) << 4) + 8;
                int worldDx = chunkCenterX - centerX;
                int worldDz = chunkCenterZ - centerZ;
                if ((long) worldDx * worldDx + (long) worldDz * worldDz <= radiusSq) {
                    consumer.accept(centerChunkX + dx, centerChunkZ + dz);
                }
            }
        }
    }

    public static int fullRecreateChunkCountForTest(int centerX, int centerZ) {
        Set<Long> keys = new LinkedHashSet<>();
        forEachRecreateChunkCoordinate(centerX, centerZ, (chunkX, chunkZ) -> keys.add(chunkKey(chunkX, chunkZ)));
        return keys.size();
    }

    public static boolean processedChunkCanBeRequeuedForTest() {
        long key = chunkKey(4, -7);
        Set<Long> queued = new LinkedHashSet<>();
        queued.add(key);
        queued.remove(key);
        return queued.add(key);
    }

    private static long chunkKey(ChunkPos chunkPos) {
        return chunkKey(chunkPos.x, chunkPos.z);
    }

    private static long chunkKey(int x, int z) {
        return ((long) x & 0xffffffffL) | (((long) z & 0xffffffffL) << 32);
    }

    private static final class ChunkCollector {
        private final ArrayDeque<ChunkPos> chunks = new ArrayDeque<>();
        private final Set<Long> keys = new LinkedHashSet<>();

        void add(ChunkPos chunkPos) {
            if (keys.add(chunkKey(chunkPos))) {
                chunks.add(chunkPos);
            }
        }

        int size() {
            return chunks.size();
        }

        ArrayDeque<ChunkPos> chunks() {
            return new ArrayDeque<>(chunks);
        }

        Set<Long> keys() {
            return new LinkedHashSet<>(keys);
        }
    }

    private enum QueueMode {
        OVERLAY("overlay"),
        FULL_RECREATE("full_recreate");

        private final String id;

        QueueMode(String id) {
            this.id = id;
        }
    }

    private static final class QueueState {
        private final ArrayDeque<ChunkPos> chunks;
        private final Set<Long> queued;
        private final QueueMode mode;
        private final long startedMillis;
        private int enqueuedChunks;
        private int processedChunks;
        private int placedBlocks;
        private int skippedBlocks;

        private QueueState(ArrayDeque<ChunkPos> chunks, Set<Long> queued, QueueMode mode) {
            this.chunks = chunks;
            this.queued = queued;
            this.mode = mode;
            this.enqueuedChunks = chunks.size();
            this.startedMillis = System.currentTimeMillis();
        }

        QueueState append(ChunkCollector extra) {
            int added = 0;
            for (ChunkPos chunkPos : extra.chunks) {
                if (queued.add(chunkKey(chunkPos))) {
                    chunks.add(chunkPos);
                    added++;
                }
            }
            enqueuedChunks += added;
            return this;
        }

        ChunkPos pollNext() {
            ChunkPos chunkPos = chunks.removeFirst();
            queued.remove(chunkKey(chunkPos));
            return chunkPos;
        }

        int remaining() {
            return chunks.size();
        }
    }
}
