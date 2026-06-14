package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.InnerDimensions;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class InnerOverlayQueue {
    private static final Map<UUID, QueueState> OVERLAY_QUEUES = new LinkedHashMap<>();
    private static final Map<UUID, QueueState> RECREATE_QUEUES = new LinkedHashMap<>();
    private static final Map<UUID, InnerGenerationMetrics> LAST_METRICS = new LinkedHashMap<>();
    private static final int DECORATED_CHUNK_CAP_PER_OWNER = 8_192;
    private static final Map<UUID, LinkedHashMap<Long, Boolean>> DECORATED_CHUNKS = new LinkedHashMap<>();

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
        enqueueIntegrationAwakening(island, drugId, false);
    }

    /**
     * Enqueue the integration awakening. When {@code centerOutward} is true (the owning player is
     * present in the dimension — Phase 8), the collected chunks are ordered by distance from the
     * island centre so the existing per-tick overlay budget reveals them as a visible outward wave
     * radiating from the sanctuary. The reveal is still paced by {@code OVERLAY_CHUNKS_PER_TICK} —
     * this only changes the order, never the per-tick chunk cap.
     */
    public static void enqueueIntegrationAwakening(InnerDimensionSavedData.IslandState island, DrugId drugId, boolean centerOutward) {
        if (island == null || island.owner() == null || drugId == null) {
            return;
        }
        ChunkCollector chunks = new ChunkCollector();
        addSquare(chunks, island.centerX() >> 4, island.centerZ() >> 4, 1);
        addLandmarkPatch(chunks, island, drugId, 4);
        addPathChunks(chunks, island, drugId);
        if (centerOutward) {
            chunks.sortByDistanceFromChunk(island.centerX() >> 4, island.centerZ() >> 4);
        }
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
        boolean replaced = OVERLAY_QUEUES.put(island.owner(), new QueueState(chunks.chunks(), chunks.keys(), QueueMode.OVERLAY)) != null;
        return new InnerRefreshJob(island.owner(), chunks.size(), replaced);
    }

    public static InnerRefreshJob enqueueOwnerFullRecreate(InnerDimensionSavedData.IslandState island) {
        if (island == null || island.owner() == null) {
            return InnerRefreshJob.empty();
        }
        invalidateDecorated(island.owner());
        ChunkCollector chunks = new ChunkCollector();
        addRecreateChunks(chunks, island);
        boolean replaced = RECREATE_QUEUES.put(island.owner(), new QueueState(chunks.chunks(), chunks.keys(), QueueMode.FULL_RECREATE)) != null;
        return new InnerRefreshJob(island.owner(), chunks.size(), replaced);
    }

    /**
     * Enqueue a freshly-loaded chunk for idempotent decoration. Placement is gated by markers and
     * {@code safeSet}, so re-processing an already-dressed chunk is a no-op.
     */
    public static void enqueueChunkDecoration(InnerDimensionSavedData.IslandState island, ChunkPos chunkPos) {
        if (island == null || island.owner() == null || chunkPos == null) {
            return;
        }
        if (!shouldQueueLoadedChunk(island.owner(), chunkKey(chunkPos))) {
            return;
        }
        ChunkCollector chunks = new ChunkCollector();
        chunks.add(chunkPos);
        appendQueue(island.owner(), chunks);
    }

    public static void invalidateDecorated(UUID owner) {
        if (owner != null) {
            DECORATED_CHUNKS.remove(owner);
        }
    }

    /**
     * Clear the static per-owner queue and metrics maps when the server stops, so stale state
     * does not leak across world loads in singleplayer. All access to these maps (enqueue*,
     * onLevelTick, onChunkLoad, here) happens on the server thread.
     */
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        OVERLAY_QUEUES.clear();
        RECREATE_QUEUES.clear();
        LAST_METRICS.clear();
        DECORATED_CHUNKS.clear();
        org.mydrugs.mydrugs.entity.InnerDemonSpawnManager.clearInnerAmbientState();
        org.mydrugs.mydrugs.entity.InnerWildlifeManager.clearAll();
    }

    public static boolean cancel(UUID owner) {
        return OVERLAY_QUEUES.remove(owner) != null | RECREATE_QUEUES.remove(owner) != null;
    }

    public static String queueStatus(UUID owner) {
        QueueState recreate = RECREATE_QUEUES.get(owner);
        QueueState overlay = OVERLAY_QUEUES.get(owner);
        if (recreate == null && overlay == null) {
            InnerGenerationMetrics metrics = LAST_METRICS.getOrDefault(owner, InnerGenerationMetrics.EMPTY);
            return "Inner Dimension overlay queue idle. Last metrics: " + metrics.toDebugString();
        }
        return "Inner Dimension queue owner=" + owner
                + stateDebug(" destructive", recreate)
                + stateDebug(" overlay", overlay);
    }

    public static String lastMetricsFor(UUID owner) {
        return LAST_METRICS.getOrDefault(owner, InnerGenerationMetrics.EMPTY).toDebugString();
    }

    private static String stateDebug(String label, QueueState state) {
        if (state == null) {
            return label + "=idle";
        }
        return label
                + "[mode=" + state.mode.id
                + ", remaining=" + state.remaining()
                + ", processed=" + state.processedChunks
                + ", placed=" + state.placedBlocks
                + ", skipped=" + state.skippedBlocks
                + "]";
    }

    public static int remainingFor(UUID owner) {
        QueueState recreate = RECREATE_QUEUES.get(owner);
        QueueState overlay = OVERLAY_QUEUES.get(owner);
        return (recreate == null ? 0 : recreate.remaining())
                + (overlay == null ? 0 : overlay.remaining());
    }

    static int deduplicatedChunkCoordinateCountForTest(int[][] chunks) {
        Set<Long> keys = new LinkedHashSet<>();
        for (int[] chunk : chunks) {
            if (chunk.length >= 2) {
                keys.add(chunkKey(chunk[0], chunk[1]));
            }
        }
        return keys.size();
    }

    /**
     * When a chunk loads in the inner dimension, enqueue it for decoration so the owner's
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
        if (OVERLAY_QUEUES.isEmpty() && RECREATE_QUEUES.isEmpty()) {
            return;
        }

        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        int chunksLeftThisTick = InnerDimensionConstants.OVERLAY_CHUNKS_PER_TICK;
        // Soft time budget: never raises the chunk caps, only stops early once at least one
        // chunk has been processed, so heavy chunks cannot stack into a tick spike.
        long deadlineNanos = System.nanoTime()
                + InnerDimensionConstants.OVERLAY_TICK_BUDGET_MS * 1_000_000L;
        chunksLeftThisTick = processQueues(level, data, RECREATE_QUEUES, chunksLeftThisTick, true, deadlineNanos);
        if (chunksLeftThisTick > 0) {
            processQueues(level, data, OVERLAY_QUEUES, chunksLeftThisTick, false, deadlineNanos);
        }
    }

    private static int processQueues(
            ServerLevel level,
            InnerDimensionSavedData data,
            Map<UUID, QueueState> queues,
            int chunksLeftThisTick,
            boolean destructive,
            long deadlineNanos
    ) {
        int startingBudget = chunksLeftThisTick;
        var iterator = queues.entrySet().iterator();
        while (iterator.hasNext() && chunksLeftThisTick > 0) {
            Map.Entry<UUID, QueueState> entry = iterator.next();
            if (!destructive && RECREATE_QUEUES.containsKey(entry.getKey())) {
                continue;
            }
            QueueState state = entry.getValue();
            InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(entry.getKey());
            int queueBudget = destructive
                    ? Math.min(chunksLeftThisTick, InnerDimensionConstants.RECREATE_CHUNKS_PER_TICK)
                    : chunksLeftThisTick;
            while (queueBudget > 0 && !state.chunks.isEmpty()) {
                ChunkPos chunkPos = state.pollNext();
                if (!level.hasChunk(chunkPos.x, chunkPos.z)) {
                    state.defer(chunkPos);
                    chunksLeftThisTick--;
                    queueBudget--;
                    continue;
                }
                InnerGenerationProfiler.begin();
                long chunkStartNanos = System.nanoTime();
                InnerPlacement.PlacementCount count;
                try {
                    count = processChunk(level, island, chunkPos, state);
                } finally {
                    state.absorb(InnerGenerationProfiler.end(), System.nanoTime() - chunkStartNanos);
                }
                markDecorated(entry.getKey(), chunkPos);
                state.processedChunks++;
                state.placedBlocks += count.placed();
                state.skippedBlocks += count.skipped();
                chunksLeftThisTick--;
                queueBudget--;
                // Tick-time budget: stop early (this tick only) once at least one chunk was done.
                if (chunksLeftThisTick < startingBudget && System.nanoTime() > deadlineNanos) {
                    if (state.exhausted()) {
                        completeQueue(data, entry.getKey(), state);
                        iterator.remove();
                    }
                    return 0;
                }
            }
            if (state.exhausted()) {
                completeQueue(data, entry.getKey(), state);
                iterator.remove();
            }
        }
        return chunksLeftThisTick;
    }

    private static void completeQueue(InnerDimensionSavedData data, UUID owner, QueueState state) {
        long elapsed = Math.max(0L, System.currentTimeMillis() - state.startedMillis);
        InnerGenerationMetrics metrics = new InnerGenerationMetrics(
                state.processedChunks,
                state.enqueuedChunks,
                state.placedBlocks,
                state.skippedBlocks,
                elapsed,
                state.sampleComputes,
                state.groveComputes,
                state.sceneComputes,
                state.maxChunkMillis,
                state.perBuilderNanos.clone()
        );
        LAST_METRICS.put(owner, metrics);
        data.updateOverlayMetrics(owner, state.mode.id + " " + metrics.toDebugString());
    }

    /**
     * Dispatch one chunk of work. A FULL_RECREATE queue runs in two phases: first every chunk's
     * terrain is rebuilt (collecting the chunk for the decoration phase), then — once the terrain
     * deque drains — the same chunks are re-walked to place structures onto now-stable terrain.
     * OVERLAY queues are single-phase and never clear terrain.
     */
    private static InnerPlacement.PlacementCount processChunk(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            ChunkPos chunkPos,
            QueueState state
    ) {
        if (state.mode == QueueMode.FULL_RECREATE) {
            if (state.inDecoratePhase()) {
                return decorateChunkNow(level, island, chunkPos);
            }
            InnerPlacement.PlacementCount count = rebuildChunkTerrainNow(level, island, chunkPos);
            state.recordForDecorate(chunkPos);
            return count;
        }
        return placeOverlayChunk(level, island, chunkPos);
    }

    /**
     * Non-destructive overlay pass for a single chunk: re-runs the idempotent visual feature
     * builders and structural decoration on terrain that already exists. Used by the OVERLAY
     * queue (entry/awakening refreshes). Never clears or rebuilds terrain.
     */
    private static InnerPlacement.PlacementCount placeOverlayChunk(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            ChunkPos chunkPos
    ) {
        InnerPlacement.MutablePlacementCount count = new InnerPlacement.MutablePlacementCount();
        InnerTerrain.beginCachePass();
        try {
            InnerChunkSampleCache cache = buildCache(island, chunkPos);
            InnerVisualFeatureBuilders.placeOverlayFeatures(level, chunkPos, cache, count);
            decorateChunk(level, island, chunkPos, cache, count);
        } finally {
            InnerTerrain.endCachePass();
        }
        return count.freeze();
    }

    /**
     * Destructive terrain phase for a single chunk: clears the column and rebuilds terrain plus
     * the shared visual feature pass. Structural decoration (sanctuary, landmarks, vaults, path
     * scenes) is deliberately NOT done here — those structures span several chunks and a later
     * neighbour's terrain clear would erase the half written into a not-yet-rebuilt chunk. They
     * run in a second {@link #decorateChunkNow} pass once every chunk's terrain is final, so a
     * recreate can never leave a half-built sanctuary (the burst "semicircle" bug).
     */
    static InnerPlacement.PlacementCount rebuildChunkTerrainNow(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            ChunkPos chunkPos
    ) {
        InnerPlacement.MutablePlacementCount count = new InnerPlacement.MutablePlacementCount();
        InnerTerrain.beginCachePass();
        try {
            InnerChunkRebuilder.recreateChunk(level, island, chunkPos, count);
        } finally {
            InnerTerrain.endCachePass();
        }
        return count.freeze();
    }

    /**
     * Structure/decoration phase for a single chunk, run after all terrain is final. Idempotent
     * via markers and {@code safeSet}, so multi-chunk structures (sanctuary disc, landmark
     * shrines) are written whole onto stable terrain and never clipped by a neighbour's rebuild.
     */
    static InnerPlacement.PlacementCount decorateChunkNow(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            ChunkPos chunkPos
    ) {
        InnerPlacement.MutablePlacementCount count = new InnerPlacement.MutablePlacementCount();
        InnerTerrain.beginCachePass();
        try {
            InnerChunkSampleCache cache = buildCache(island, chunkPos);
            decorateChunk(level, island, chunkPos, cache, count);
        } finally {
            InnerTerrain.endCachePass();
        }
        return count.freeze();
    }

    private static InnerChunkSampleCache buildCache(
            InnerDimensionSavedData.IslandState island,
            ChunkPos chunkPos
    ) {
        return InnerChunkSampleCache.build(
                island.centerX(),
                island.centerZ(),
                chunkPos.getMinBlockX(),
                chunkPos.getMinBlockZ()
        );
    }

    private static void decorateChunk(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        int centerChunkX = island.centerX() >> 4;
        int centerChunkZ = island.centerZ() >> 4;
        if (chunkPos.x == centerChunkX && chunkPos.z == centerChunkZ) {
            InnerSanctuaryBuilder.placeCenterSanctuary(level, island, count);
        }

        InnerDecorator.decoratePathChunk(level, chunkPos, cache, count);
        InnerDecorator.decorateAmbientChunk(level, chunkPos, cache, count);
        InnerVaults.placeVaultChests(level, InnerDimensionSavedData.get(level), island, chunkPos, count);
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
                InnerDecorator.decorateRegionAwakening(
                        level,
                        chunkPos,
                        drugId,
                        true,
                        island.integratedCount(),
                        cache,
                        count
                );
            }
        }
    }

    private static void appendQueue(UUID owner, ChunkCollector chunks) {
        OVERLAY_QUEUES.compute(owner, (id, existing) -> existing == null
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

    static void forEachRecreateChunkCoordinate(int centerX, int centerZ, BiConsumer<Integer, Integer> consumer) {
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

    static int fullRecreateChunkCountForTest(int centerX, int centerZ) {
        Set<Long> keys = new LinkedHashSet<>();
        forEachRecreateChunkCoordinate(centerX, centerZ, (chunkX, chunkZ) -> keys.add(chunkKey(chunkX, chunkZ)));
        return keys.size();
    }

    static boolean processedChunkCanBeRequeuedForTest() {
        long key = chunkKey(4, -7);
        Set<Long> queued = new LinkedHashSet<>();
        queued.add(key);
        queued.remove(key);
        return queued.add(key);
    }

    static boolean destructiveQueueIsSeparateFromOverlayQueueForTest() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-00000000d00d");
        try {
            RECREATE_QUEUES.put(owner, new QueueState(new ArrayDeque<>(), new LinkedHashSet<>(), QueueMode.FULL_RECREATE));
            appendQueue(owner, new ChunkCollector());
            QueueState recreate = RECREATE_QUEUES.get(owner);
            QueueState overlay = OVERLAY_QUEUES.get(owner);
            return recreate != null
                    && overlay != null
                    && recreate.mode == QueueMode.FULL_RECREATE
                    && overlay.mode == QueueMode.OVERLAY;
        } finally {
            RECREATE_QUEUES.remove(owner);
            OVERLAY_QUEUES.remove(owner);
        }
    }

    static boolean decoratedLoadGuardSkipsAndInvalidatesForTest() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-00000000dec0");
        long key = chunkKey(4, -7);
        try {
            invalidateDecorated(owner);
            boolean initiallyAllowed = shouldQueueLoadedChunk(owner, key);
            markDecorated(owner, key);
            boolean skippedAfterMark = !shouldQueueLoadedChunk(owner, key);
            invalidateDecorated(owner);
            boolean allowedAfterInvalidate = shouldQueueLoadedChunk(owner, key);
            return initiallyAllowed && skippedAfterMark && allowedAfterInvalidate;
        } finally {
            invalidateDecorated(owner);
        }
    }

    static boolean decoratedLoadGuardIsBoundedForTest() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-00000000dec1");
        try {
            invalidateDecorated(owner);
            for (int i = 0; i <= DECORATED_CHUNK_CAP_PER_OWNER; i++) {
                markDecorated(owner, chunkKey(i, -i));
            }
            LinkedHashMap<Long, Boolean> decorated = DECORATED_CHUNKS.get(owner);
            return decorated != null
                    && decorated.size() == DECORATED_CHUNK_CAP_PER_OWNER
                    && !isDecorated(owner, chunkKey(0, 0))
                    && isDecorated(owner, chunkKey(DECORATED_CHUNK_CAP_PER_OWNER, -DECORATED_CHUNK_CAP_PER_OWNER));
        } finally {
            invalidateDecorated(owner);
        }
    }

    private static boolean shouldQueueLoadedChunk(UUID owner, long key) {
        return !isDecorated(owner, key);
    }

    private static boolean isDecorated(UUID owner, long key) {
        LinkedHashMap<Long, Boolean> chunks = DECORATED_CHUNKS.get(owner);
        return chunks != null && Boolean.TRUE.equals(chunks.get(key));
    }

    private static void markDecorated(UUID owner, ChunkPos chunkPos) {
        markDecorated(owner, chunkKey(chunkPos));
    }

    private static void markDecorated(UUID owner, long key) {
        if (owner != null) {
            decoratedChunks(owner).put(key, Boolean.TRUE);
        }
    }

    private static LinkedHashMap<Long, Boolean> decoratedChunks(UUID owner) {
        return DECORATED_CHUNKS.computeIfAbsent(owner, ignored -> new LinkedHashMap<>(16, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Boolean> eldest) {
                return size() > DECORATED_CHUNK_CAP_PER_OWNER;
            }
        });
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

        /** Reorder so nearer-to-centre chunks are polled first (the Phase 8 outward wave). */
        void sortByDistanceFromChunk(int centerChunkX, int centerChunkZ) {
            java.util.List<ChunkPos> sorted = new java.util.ArrayList<>(chunks);
            sorted.sort(java.util.Comparator.comparingLong(c -> {
                long dx = (long) c.x - centerChunkX;
                long dz = (long) c.z - centerChunkZ;
                return dx * dx + dz * dz;
            }));
            chunks.clear();
            chunks.addAll(sorted);
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
        private long sampleComputes;
        private long groveComputes;
        private long sceneComputes;
        private long maxChunkMillis;
        private final long[] perBuilderNanos = new long[InnerVisualFeatureBuilders.builderCount()];
        // FULL_RECREATE two-phase state: chunks whose terrain was rebuilt, awaiting decoration.
        private final ArrayDeque<ChunkPos> decoratePending = new ArrayDeque<>();
        private boolean decoratePhase;

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

        void absorb(InnerGenerationProfiler.Counters counters, long chunkNanos) {
            maxChunkMillis = Math.max(maxChunkMillis, chunkNanos / 1_000_000L);
            if (counters == null) {
                return;
            }
            sampleComputes += counters.sampleComputes;
            groveComputes += counters.groveComputes;
            sceneComputes += counters.sceneComputes;
            for (int i = 0; i < perBuilderNanos.length && i < counters.builderNanos.length; i++) {
                perBuilderNanos[i] += counters.builderNanos[i];
            }
        }

        ChunkPos pollNext() {
            ChunkPos chunkPos = chunks.removeFirst();
            queued.remove(chunkKey(chunkPos));
            return chunkPos;
        }

        void defer(ChunkPos chunkPos) {
            if (queued.add(chunkKey(chunkPos))) {
                chunks.addLast(chunkPos);
            }
        }

        boolean inDecoratePhase() {
            return decoratePhase;
        }

        /** Remember a chunk whose terrain is now final so it can be decorated in the second phase. */
        void recordForDecorate(ChunkPos chunkPos) {
            decoratePending.add(chunkPos);
        }

        /**
         * True once there is no more work. For a FULL_RECREATE queue, draining the terrain deque
         * flips the queue into its decoration phase (re-loading the rebuilt chunks) rather than
         * completing, so structures are placed only after every chunk's terrain is final.
         */
        boolean exhausted() {
            if (!chunks.isEmpty()) {
                return false;
            }
            if (mode == QueueMode.FULL_RECREATE && !decoratePhase && !decoratePending.isEmpty()) {
                decoratePhase = true;
                queued.clear();
                for (ChunkPos chunkPos : decoratePending) {
                    if (queued.add(chunkKey(chunkPos))) {
                        chunks.add(chunkPos);
                    }
                }
                decoratePending.clear();
                return false;
            }
            return true;
        }

        int remaining() {
            return chunks.size() + decoratePending.size();
        }
    }
}
