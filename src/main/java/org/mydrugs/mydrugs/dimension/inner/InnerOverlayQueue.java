package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.InnerDimensions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class InnerOverlayQueue {
    private static final Map<UUID, QueueState> OVERLAY_QUEUES = new LinkedHashMap<>();
    private static final Map<UUID, QueueState> RECREATE_QUEUES = new LinkedHashMap<>();
    private static final QueueCursor OVERLAY_CURSOR = new QueueCursor();
    private static final QueueCursor RECREATE_CURSOR = new QueueCursor();
    private static final Map<UUID, InnerGenerationMetrics> LAST_METRICS = new LinkedHashMap<>();
    private static final int DECORATED_CHUNK_CAP_PER_OWNER = 8_192;
    /**
     * Per-owner set of chunks whose <em>decoration</em> phase has actually run (overlay pass or a
     * recreate's second decorate pass). It is strictly a dedup guard for the lazy on-chunk-load
     * path ({@link #enqueueChunkDecoration}); a chunk must never be recorded here after a recreate's
     * terrain-only rebuild, or the guard would suppress the decoration it still needs.
     */
    private static final Map<UUID, LinkedHashMap<Long, Boolean>> DECORATED_CHUNKS = new LinkedHashMap<>();
    private static final int LANDMARK_CHUNK_CACHE_CAP = 256;
    private static final Map<Long, InnerChunkCollector.LandmarkChunk[]> LANDMARK_CHUNKS_BY_CENTER =
            new LinkedHashMap<>(16, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, InnerChunkCollector.LandmarkChunk[]> eldest) {
                    return size() > LANDMARK_CHUNK_CACHE_CAP;
                }
            };

    private InnerOverlayQueue() {
    }

    public static void enqueueEntryRefresh(InnerDimensionSavedData.IslandState island) {
        if (island == null || island.owner() == null) {
            return;
        }
        InnerChunkCollector.ChunkCollector chunks = new InnerChunkCollector.ChunkCollector();
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
        InnerChunkCollector.ChunkCollector chunks = new InnerChunkCollector.ChunkCollector();
        addSquare(chunks, island.centerX() >> 4, island.centerZ() >> 4, 1);
        addLandmarkPatch(chunks, island, drugId, 4);
        addPathChunks(chunks, island, drugId);
        if (centerOutward) {
            chunks.sortByDistanceFromChunk(island.centerX() >> 4, island.centerZ() >> 4);
        }
        appendQueue(island.owner(), chunks);
    }

    public static void enqueueTrialCompletion(InnerDimensionSavedData.IslandState island, DrugId drugId) {
        if (island == null || island.owner() == null || drugId == null) {
            return;
        }
        InnerChunkCollector.ChunkCollector chunks = new InnerChunkCollector.ChunkCollector();
        addSquare(chunks, island.centerX() >> 4, island.centerZ() >> 4, 1);
        addLandmarkPatch(chunks, island, drugId, 1);
        appendQueue(island.owner(), chunks);
    }

    public static InnerRefreshJob enqueueOwnerOverlayRefresh(InnerDimensionSavedData.IslandState island) {
        if (island == null || island.owner() == null) {
            return InnerRefreshJob.empty();
        }
        InnerChunkCollector.ChunkCollector chunks = new InnerChunkCollector.ChunkCollector();
        addSquare(chunks, island.centerX() >> 4, island.centerZ() >> 4, 2);
        for (DrugId drugId : CuratedDrugChain.ORDER) {
            addLandmarkPatch(chunks, island, drugId, 4);
            if (island.hasIntegrated(drugId)) {
                addPathChunks(chunks, island, drugId);
            }
        }
        boolean replaced = OVERLAY_QUEUES.put(island.owner(), new QueueState(chunks.chunks(), chunks.keys(), QueueMode.OVERLAY)) != null;
        return new InnerRefreshJob(island.owner(), chunks.size(), replaced);
    }

    /**
     * Queue a destructive full rebuild of the owner's whole island area (terrain pass, then a
     * decorate pass). The queue is intentionally <em>not</em> persisted: it can hold tens of
     * thousands of chunk coordinates, so saved data stores only a compact "recreate incomplete"
     * marker that can requeue the full deterministic pass after restart. Recreate processing never mutates
     * gameplay-critical island state (center, slot, integrations, progress markers, trials, dream
     * alignment); only visual overlay markers are cleared at enqueue, and
     * {@link InnerDimensionSavedData#clearOverlayMarkers} preserves progress markers. If the server
     * stops mid-recreate, already-rebuilt terrain remains but the persisted marker requeues a full
     * safe pass on the next Inner Dimension tick. Progress is observable via {@link #recreateProgress}
     * / {@link #queueStatus} and can be explicitly
     * abandoned via {@link #cancelWithSummary}.
     */
    public static InnerRefreshJob enqueueOwnerFullRecreate(InnerDimensionSavedData.IslandState island) {
        if (island == null || island.owner() == null) {
            return InnerRefreshJob.empty();
        }
        invalidateDecorated(island.owner());
        InnerChunkCollector.ChunkCollector chunks = new InnerChunkCollector.ChunkCollector();
        addRecreateChunks(chunks, island);
        boolean replaced = RECREATE_QUEUES.put(island.owner(), new QueueState(chunks.chunks(), chunks.keys(), QueueMode.FULL_RECREATE)) != null;
        return new InnerRefreshJob(island.owner(), chunks.size(), replaced);
    }

    public static InnerRefreshJob enqueueOwnerFullRecreate(
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island
    ) {
        InnerRefreshJob job = enqueueOwnerFullRecreate(island);
        if (data != null && job.enqueuedChunks() > 0) {
            data.markRecreateJobPending(job.owner());
        }
        return job;
    }

    /**
     * Enqueue a freshly-loaded chunk for idempotent decoration. Placement is gated by markers and
     * {@code safeSet}, so re-processing an already-dressed chunk is a no-op.
     */
    public static void enqueueChunkDecoration(InnerDimensionSavedData.IslandState island, ChunkPos chunkPos) {
        if (island == null || island.owner() == null || chunkPos == null) {
            return;
        }
        if (!shouldQueueLoadedChunk(island.owner(), InnerChunkCollector.chunkKey(chunkPos))) {
            return;
        }
        InnerChunkCollector.ChunkCollector chunks = new InnerChunkCollector.ChunkCollector();
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
        OVERLAY_CURSOR.clear();
        RECREATE_CURSOR.clear();
        LAST_METRICS.clear();
        DECORATED_CHUNKS.clear();
        LANDMARK_CHUNKS_BY_CENTER.clear();
        InnerTerrain.clearAllSatelliteCenters();
        org.mydrugs.mydrugs.entity.InnerDemonSpawnManager.clearInnerAmbientState();
        org.mydrugs.mydrugs.entity.InnerWildlifeManager.clearAll();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        UUID owner = player.getUUID();
        // Overlay work only affects player-visible decoration and can be safely abandoned when the
        // owner leaves. Full recreate queues are destructive terrain rebuilds; once started, they
        // must remain queued so the server can finish rebuilding/decorating the island area.
        cancelOverlayForLogout(owner);
        boolean recreateActive = RECREATE_QUEUES.containsKey(owner);
        if (!recreateActive) {
            LAST_METRICS.remove(owner);
            DECORATED_CHUNKS.remove(owner);
        }

        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return;
        }
        ServerLevel innerLevel = server.getLevel(InnerDimensions.INNER_LEVEL);
        if (innerLevel == null) {
            return;
        }
        InnerDimensionSavedData.IslandState island = InnerDimensionSavedData.get(innerLevel).island(owner);
        if (island != null) {
            clearIslandStaticCaches(island.centerX(), island.centerZ());
        }
    }

    /**
     * Abandon any queued work for {@code owner} and report what was dropped. This is an explicit
     * admin/debug operation: overlay cancellation is visually self-healing, but cancelling a full
     * recreate after terrain work has begun may leave mixed old/new terrain until another recreate
     * completes. No saved island state is touched here, so persisted progress remains valid.
     */
    public static CancelSummary cancelWithSummary(UUID owner) {
        QueueState recreate = RECREATE_QUEUES.remove(owner);
        QueueState overlay = OVERLAY_QUEUES.remove(owner);
        RECREATE_CURSOR.drop(owner);
        OVERLAY_CURSOR.drop(owner);
        return new CancelSummary(
                recreate != null,
                overlay != null,
                recreate == null ? 0 : recreate.pendingWork(),
                overlay == null ? 0 : overlay.pendingWork()
        );
    }

    public static CancelSummary cancelWithSummary(InnerDimensionSavedData data, UUID owner) {
        boolean persistedRecreate = data != null && data.pendingRecreateJob(owner) != null;
        CancelSummary summary = cancelWithSummary(owner);
        if (data != null && (summary.cancelledRecreate() || persistedRecreate)) {
            data.clearRecreateJob(owner);
        }
        if (!persistedRecreate || summary.cancelledRecreate()) {
            return summary;
        }
        return new CancelSummary(true, summary.cancelledOverlay(), 0, summary.overlayPendingDropped());
    }

    public static boolean cancel(UUID owner) {
        return cancelWithSummary(owner).cancelledAnything();
    }

    /** Logout cleanup: only abandon overlay work; destructive recreate queues continue server-side. */
    private static CancelSummary cancelOverlayForLogout(UUID owner) {
        QueueState overlay = OVERLAY_QUEUES.remove(owner);
        OVERLAY_CURSOR.drop(owner);
        return new CancelSummary(false, overlay != null, 0, overlay == null ? 0 : overlay.pendingWork());
    }

    /** Structured progress for the owner's destructive recreate queue, if one is active. */
    public static Optional<QueueProgress> recreateProgress(UUID owner) {
        QueueState recreate = RECREATE_QUEUES.get(owner);
        return recreate == null ? Optional.empty() : Optional.of(recreate.progress(owner));
    }

    public static String queueStatus(UUID owner) {
        return queueStatus(null, owner);
    }

    public static String queueStatus(InnerDimensionSavedData data, UUID owner) {
        QueueState recreate = RECREATE_QUEUES.get(owner);
        QueueState overlay = OVERLAY_QUEUES.get(owner);
        if (recreate == null && overlay == null) {
            InnerDimensionSavedData.RecreateJobState persisted = data == null ? null : data.pendingRecreateJob(owner);
            if (persisted != null) {
                return "Inner Dimension queue owner=" + owner
                        + "\n  destructive: incomplete persisted "
                        + persisted.type()
                        + " phase="
                        + persisted.phase()
                        + " schema="
                        + persisted.schemaVersion()
                        + " (will resume by replaying the full recreate pass).";
            }
            InnerGenerationMetrics metrics = LAST_METRICS.getOrDefault(owner, InnerGenerationMetrics.EMPTY);
            if (metrics.equals(InnerGenerationMetrics.EMPTY)) {
                return "Inner Dimension queue idle for owner=" + owner + " (no previous activity).";
            }
            return "Inner Dimension queue idle for owner=" + owner
                    + ". Last metrics: " + metrics.toDebugString();
        }
        StringBuilder out = new StringBuilder("Inner Dimension queue owner=").append(owner).append('\n');
        if (recreate != null) {
            out.append("  destructive: ").append(recreate.progress(owner).describe()).append('\n');
        }
        if (overlay != null) {
            out.append("  overlay:     ").append(overlay.progress(owner).describe()).append('\n');
        }
        return out.toString().stripTrailing();
    }

    private static String formatSeconds(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return minutes + "m" + (remainingSeconds > 0 ? remainingSeconds + "s" : "");
    }

    public static String lastMetricsFor(UUID owner) {
        return LAST_METRICS.getOrDefault(owner, InnerGenerationMetrics.EMPTY).toDebugString();
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
                keys.add(InnerChunkCollector.chunkKey(chunk[0], chunk[1]));
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
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        restorePersistedRecreateQueues(data);
        if (OVERLAY_QUEUES.isEmpty() && RECREATE_QUEUES.isEmpty()) {
            return;
        }

        int chunksLeftThisTick = InnerDimensionConstants.OVERLAY_CHUNKS_PER_TICK;
        // Soft time budget: never raises the chunk caps, only stops early once at least one
        // chunk has been processed, so heavy chunks cannot stack into a tick spike.
        long deadlineNanos = System.nanoTime()
                + InnerDimensionConstants.OVERLAY_TICK_BUDGET_MS * 1_000_000L;
        chunksLeftThisTick = processQueues(level, data, RECREATE_QUEUES, RECREATE_CURSOR, chunksLeftThisTick, true, deadlineNanos);
        if (chunksLeftThisTick > 0) {
            processQueues(level, data, OVERLAY_QUEUES, OVERLAY_CURSOR, chunksLeftThisTick, false, deadlineNanos);
        }
    }

    private static int processQueues(
            ServerLevel level,
            InnerDimensionSavedData data,
            Map<UUID, QueueState> queues,
            QueueCursor cursor,
            int chunksLeftThisTick,
            boolean destructive,
            long deadlineNanos
    ) {
        int startingBudget = chunksLeftThisTick;
        ArrayList<UUID> owners = activeOwners(queues, destructive);
        if (owners.isEmpty()) {
            cursor.clear();
            return chunksLeftThisTick;
        }

        int ownerIndex = cursor.startIndex(owners);
        int visitsWithoutProgress = 0;
        Set<UUID> visitedThisTick = destructive ? new LinkedHashSet<>() : null;
        Map<UUID, Integer> scanBudgets = new HashMap<>();
        while (chunksLeftThisTick > 0
                && !owners.isEmpty()
                && (!destructive || visitedThisTick.size() < owners.size())
                && (destructive || visitsWithoutProgress < owners.size())) {
            if (ownerIndex >= owners.size()) {
                ownerIndex = 0;
            }
            UUID owner = owners.get(ownerIndex);
            ownerIndex = (ownerIndex + 1) % owners.size();
            if (destructive) {
                visitedThisTick.add(owner);
            }

            QueueState state = queues.get(owner);
            if (state == null || (!destructive && RECREATE_QUEUES.containsKey(owner))) {
                visitsWithoutProgress++;
                continue;
            }

            InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
            int queueBudget = destructive
                    ? Math.min(chunksLeftThisTick, InnerDimensionConstants.RECREATE_CHUNKS_PER_TICK)
                    : overlayOwnerBudget(chunksLeftThisTick, owners, queues);
            // Unloaded chunks are held aside (not re-queued) for the duration of this queue's pass,
            // so each chunk is examined at most once per tick and a loaded chunk behind a run of
            // unloaded ones can still be reached. Skipping an unloaded chunk costs only the bounded
            // scan budget, never the per-tick processing budget, so unloaded work cannot starve
            // loaded work in this or any other queue.
            ArrayDeque<ChunkPos> deferredThisPass = new ArrayDeque<>();
            int[] scanBudget = {
                    scanBudgets.computeIfAbsent(owner,
                            ignored -> Math.min(state.chunks.size(), InnerDimensionConstants.MAX_UNLOADED_CHUNK_SCANS_PER_TICK))
            };
            int processedBefore = state.processedChunks;
            while (queueBudget > 0 && scanBudget[0] > 0) {
                ChunkPos chunkPos = pollNextLoaded(state, level, deferredThisPass, scanBudget);
                if (chunkPos == null) {
                    // No loaded chunk reachable within this tick's scan budget; stop hunting.
                    break;
                }
                InnerGenerationProfiler.begin();
                long chunkStartNanos = System.nanoTime();
                ChunkProcessResult result;
                try {
                    result = processChunk(level, island, chunkPos, state);
                } finally {
                    state.absorb(InnerGenerationProfiler.end(), System.nanoTime() - chunkStartNanos);
                }
                // Mark decorated only once the chunk's decoration phase has actually run — never
                // after a recreate's terrain-only rebuild — so the lazy on-load guard cannot
                // suppress a chunk that still needs its structures placed.
                if (result.decorated()) {
                    markDecorated(owner, chunkPos);
                }
                state.processedChunks++;
                state.placedBlocks += result.count().placed();
                state.skippedBlocks += result.count().skipped();
                chunksLeftThisTick--;
                queueBudget--;
                // Tick-time budget: stop early (this tick only) once at least one chunk was done.
                if (chunksLeftThisTick < startingBudget && System.nanoTime() > deadlineNanos) {
                    requeueDeferred(state, deferredThisPass);
                    scanBudgets.put(owner, scanBudget[0]);
                    if (completeOrTrackQueue(data, owner, state)) {
                        queues.remove(owner);
                    }
                    cursor.set(nextActiveOwner(owners, ownerIndex, queues, destructive));
                    return 0;
                }
            }
            requeueDeferred(state, deferredThisPass);
            scanBudgets.put(owner, scanBudget[0]);
            if (completeOrTrackQueue(data, owner, state)) {
                queues.remove(owner);
            }
            if (state.processedChunks > processedBefore) {
                visitsWithoutProgress = 0;
            } else {
                visitsWithoutProgress++;
            }
        }
        cursor.set(nextActiveOwner(owners, ownerIndex, queues, destructive));
        return chunksLeftThisTick;
    }

    private static ArrayList<UUID> activeOwners(Map<UUID, QueueState> queues, boolean destructive) {
        ArrayList<UUID> owners = new ArrayList<>(queues.size());
        for (UUID owner : queues.keySet()) {
            if (isActiveOwner(owner, queues, destructive)) {
                owners.add(owner);
            }
        }
        return owners;
    }

    private static boolean isActiveOwner(UUID owner, Map<UUID, QueueState> queues, boolean destructive) {
        return queues.containsKey(owner) && (destructive || !RECREATE_QUEUES.containsKey(owner));
    }

    private static int activeOwnerCount(List<UUID> owners, Map<UUID, QueueState> queues, boolean destructive) {
        int count = 0;
        for (UUID owner : owners) {
            if (isActiveOwner(owner, queues, destructive)) {
                count++;
            }
        }
        return count;
    }

    private static int overlayOwnerBudget(int chunksLeftThisTick, List<UUID> owners, Map<UUID, QueueState> queues) {
        return activeOwnerCount(owners, queues, false) <= 1 ? chunksLeftThisTick : 1;
    }

    private static UUID nextActiveOwner(List<UUID> owners, int startIndex, Map<UUID, QueueState> queues, boolean destructive) {
        if (owners.isEmpty()) {
            return null;
        }
        for (int offset = 0; offset < owners.size(); offset++) {
            UUID owner = owners.get((startIndex + offset) % owners.size());
            if (isActiveOwner(owner, queues, destructive)) {
                return owner;
            }
        }
        return null;
    }

    private static void restorePersistedRecreateQueues(InnerDimensionSavedData data) {
        for (InnerDimensionSavedData.RecreateJobState job : data.pendingRecreateJobs()) {
            UUID owner = job.owner();
            if (owner == null || !job.fullRecreate() || RECREATE_QUEUES.containsKey(owner)) {
                continue;
            }
            InnerDimensionSavedData.IslandState island = data.island(owner);
            if (island == null) {
                continue;
            }
            invalidateDecorated(owner);
            data.updateRecreateJobPhase(owner, "terrain");
            InnerChunkCollector.ChunkCollector chunks = new InnerChunkCollector.ChunkCollector();
            addRecreateChunks(chunks, island);
            RECREATE_QUEUES.put(owner, new QueueState(chunks.chunks(), chunks.keys(), QueueMode.FULL_RECREATE));
            MyDrugs.getLOGGER().warn(
                    "Restored incomplete Inner Dimension full recreate for owner {} from saved marker; replaying the full pass safely",
                    owner
            );
        }
    }

    /**
     * Poll the next already-loaded chunk from {@code state}, setting any unloaded chunks aside in
     * {@code deferred} (so they are not re-examined this tick) and spending {@code scanBudget} for
     * each skip. Returns the next loaded chunk, or {@code null} when none is reachable before the
     * scan budget runs out or the queue empties. Only inspects chunk residency — never loads one.
     */
    private static ChunkPos pollNextLoaded(
            QueueState state,
            ServerLevel level,
            ArrayDeque<ChunkPos> deferred,
            int[] scanBudget
    ) {
        while (!state.chunks.isEmpty()) {
            ChunkPos chunkPos = state.pollNext();
            if (level.hasChunk(chunkPos.x, chunkPos.z)) {
                return chunkPos;
            }
            deferred.add(chunkPos);
            state.cumulativeUnloadedDeferrals++;
            if (--scanBudget[0] <= 0) {
                return null;
            }
        }
        return null;
    }

    /** Re-queue chunks set aside as unloaded this tick so they survive for a later tick. */
    private static void requeueDeferred(QueueState state, ArrayDeque<ChunkPos> deferred) {
        for (ChunkPos chunkPos : deferred) {
            state.defer(chunkPos);
        }
        deferred.clear();
    }

    private static boolean completeOrTrackQueue(InnerDimensionSavedData data, UUID owner, QueueState state) {
        boolean wasDecoratePhase = state.inDecoratePhase();
        if (state.exhausted()) {
            completeQueue(data, owner, state);
            return true;
        }
        if (state.mode == QueueMode.FULL_RECREATE && !wasDecoratePhase && state.inDecoratePhase()) {
            data.updateRecreateJobPhase(owner, "decorate");
        }
        return false;
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
        if (state.mode == QueueMode.FULL_RECREATE) {
            data.clearRecreateJob(owner);
        }
    }

    /**
     * Dispatch one chunk of work. A FULL_RECREATE queue runs in two phases: first every chunk's
     * terrain is rebuilt (collecting the chunk for the decoration phase), then — once the terrain
     * deque drains — the same chunks are re-walked to place structures onto now-stable terrain.
     * OVERLAY queues are single-phase and never clear terrain.
     */
    private static ChunkProcessResult processChunk(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            ChunkPos chunkPos,
            QueueState state
    ) {
        boolean decorated = phaseDecoratesChunk(state.mode, state.inDecoratePhase());
        if (state.mode == QueueMode.FULL_RECREATE) {
            if (state.inDecoratePhase()) {
                return new ChunkProcessResult(decorateChunkNow(level, island, chunkPos), decorated);
            }
            InnerPlacement.PlacementCount count = rebuildChunkTerrainNow(level, island, chunkPos);
            state.recordForDecorate(chunkPos);
            return new ChunkProcessResult(count, decorated);
        }
        return new ChunkProcessResult(placeOverlayChunk(level, island, chunkPos), decorated);
    }

    /** Outcome of processing one chunk: blocks placed, and whether the decoration phase ran. */
    private record ChunkProcessResult(InnerPlacement.PlacementCount count, boolean decorated) {
    }

    /**
     * Whether processing a chunk in the given phase performs full decoration (sanctuary, landmarks,
     * vaults, path scenes) and so should mark the chunk decorated. Overlay chunks always decorate;
     * a {@link QueueMode#FULL_RECREATE} chunk decorates only in its second (decorate) phase, never
     * in the first terrain-rebuild phase.
     */
    private static boolean phaseDecoratesChunk(QueueMode mode, boolean inDecoratePhase) {
        return mode != QueueMode.FULL_RECREATE || inDecoratePhase;
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
            decorateChunk(level, island, chunkPos, cache, count, InnerPlacement.PlacementMode.LIVE_OVERLAY);
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
            decorateChunk(level, island, chunkPos, cache, count, InnerPlacement.PlacementMode.RECREATE);
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
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        int centerChunkX = island.centerX() >> 4;
        int centerChunkZ = island.centerZ() >> 4;
        if (chunkPos.x == centerChunkX && chunkPos.z == centerChunkZ) {
            InnerSanctuaryBuilder.placeCenterSanctuary(level, island, count, mode);
        }

        InnerDecorator.decoratePathChunk(level, chunkPos, cache, count, mode);
        InnerDecorator.decorateAmbientChunk(level, chunkPos, cache, count, mode);
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        InnerVaults.placeVaultChests(level, data, island, chunkPos, count, mode);
        InnerSkyShardLootBuilder.placeRewards(level, data, island, chunkPos, cache, count, mode);
        InnerChunkCollector.LandmarkChunk[] landmarkChunks = landmarkChunksForIsland(island);
        for (int i = 0; i < CuratedDrugChain.ORDER.size(); i++) {
            DrugId drugId = CuratedDrugChain.ORDER.get(i);
            InnerChunkCollector.LandmarkChunk landmarkChunk = landmarkChunks[i];
            int dx = Math.abs(chunkPos.x - landmarkChunk.x());
            int dz = Math.abs(chunkPos.z - landmarkChunk.z());
            if (dx > 4 || dz > 4) {
                continue;
            }
            boolean unlocked = island.hasIntegrated(drugId);
            if (chunkPos.x == landmarkChunk.x() && chunkPos.z == landmarkChunk.z()) {
                InnerLandmarkBuilder.placeLandmark(level, island, drugId, unlocked, count, mode);
            }
            if (unlocked) {
                    InnerDecorator.decorateRegionAwakening(
                            level,
                            chunkPos,
                            drugId,
                            true,
                            island.integratedDrugCount(),
                            cache,
                            count,
                            mode
                    );
            }
        }
    }

    private static void appendQueue(UUID owner, InnerChunkCollector.ChunkCollector chunks) {
        OVERLAY_QUEUES.compute(owner, (id, existing) -> existing == null
                ? new QueueState(chunks.chunks(), chunks.keys(), QueueMode.OVERLAY)
                : existing.append(chunks));
    }

    private static void addSquare(InnerChunkCollector.ChunkCollector chunks, int centerChunkX, int centerChunkZ, int radius) {
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                chunks.add(new ChunkPos(centerChunkX + dx, centerChunkZ + dz));
            }
        }
    }

    private static void addLandmarkPatch(
            InnerChunkCollector.ChunkCollector chunks,
            InnerDimensionSavedData.IslandState island,
            DrugId drugId,
            int radius
    ) {
        InnerChunkCollector.LandmarkChunk landmarkChunk = landmarkChunkFor(island, drugId);
        addSquare(chunks, landmarkChunk.x(), landmarkChunk.z(), radius);
    }

    private static InnerChunkCollector.LandmarkChunk landmarkChunkFor(InnerDimensionSavedData.IslandState island, DrugId drugId) {
        int index = CuratedDrugChain.ORDER.indexOf(drugId);
        return landmarkChunksForIsland(island)[index < 0 ? 0 : index];
    }

    private static InnerChunkCollector.LandmarkChunk[] landmarkChunksForIsland(InnerDimensionSavedData.IslandState island) {
        return landmarkChunksForCenter(island.centerX(), island.centerZ());
    }

    private static InnerChunkCollector.LandmarkChunk[] landmarkChunksForCenter(int centerX, int centerZ) {
        return LANDMARK_CHUNKS_BY_CENTER.computeIfAbsent(InnerChunkCollector.centerKey(centerX, centerZ), ignored -> {
            InnerChunkCollector.LandmarkChunk[] chunks = new InnerChunkCollector.LandmarkChunk[CuratedDrugChain.ORDER.size()];
            for (int i = 0; i < CuratedDrugChain.ORDER.size(); i++) {
                BlockPos landmark = InnerRegionMap.landmarkFor(centerX, centerZ, CuratedDrugChain.ORDER.get(i));
                chunks[i] = new InnerChunkCollector.LandmarkChunk(landmark.getX() >> 4, landmark.getZ() >> 4);
            }
            return chunks;
        });
    }

    /**
     * Enqueue the chunks the wandering spoke corridor actually occupies for the Phase 8 awakening
     * wave. A straight centre→landmark line missed the bent middle of the corridor (the route
     * snakes via {@link InnerTerrain#corridorCenterAngle}), leaving gaps in the revealed path.
     * We instead reproduce the corridor geometry exactly: walk the radius out to the landmark and,
     * at each step, sweep the full lateral envelope the corridor spans at that radius. Dedup is
     * handled by {@link InnerChunkCollector.ChunkCollector}.
     */
    private static void addPathChunks(
            InnerChunkCollector.ChunkCollector chunks,
            InnerDimensionSavedData.IslandState island,
            DrugId drugId
    ) {
        forEachPathChunkCoordinate(island.centerX(), island.centerZ(), drugId,
                (chunkX, chunkZ) -> chunks.add(new ChunkPos(chunkX, chunkZ)));
    }

    /**
     * Walk the spoke corridor for {@code drugId} and emit every chunk coordinate it covers (each
     * distinct chunk once). The corridor is swept on a one-block (radius, lateral-offset) grid
     * over its full angular support, so the emitted chunks are a superset of every chunk the
     * rendered corridor occupies — there is no sampling gap to leave a hole in the awakening wave.
     * Geometry mirrors {@link InnerTerrain#pathStrength} via the shared
     * {@link InnerTerrain#corridorCenterAngle} helper, so the enqueue and the rendered corridor
     * stay in lockstep. The dense inner loop is cheap arithmetic and runs on integration, not per
     * tick; internal dedup keeps the emitted chunk count (and any allocation by the consumer) small.
     */
    private static void forEachPathChunkCoordinate(
            int centerX,
            int centerZ,
            DrugId drugId,
            BiConsumer<Integer, Integer> consumer
    ) {
        long seed = InnerTerrain.seedForSlot(centerX, centerZ);
        double halfWidth = InnerTerrain.spokeCorridorHalfWidth();
        double corridorStart = InnerTerrain.corridorStartRadius();
        double landmarkRadius = InnerRegionMap.landmarkRadiusFor(drugId);
        Set<Long> seen = new LinkedHashSet<>();
        for (double r = corridorStart; r <= landmarkRadius; r += 1.0D) {
            double target = InnerTerrain.corridorCenterAngle(seed, r, drugId);
            double reach = halfWidth * r; // the corridor spans angular +/- halfWidth at this radius
            for (double offset = -reach; offset <= reach; offset += 1.0D) {
                double angle = target + offset / r;
                int chunkX = (centerX + (int) Math.round(Math.cos(angle) * r)) >> 4;
                int chunkZ = (centerZ + (int) Math.round(Math.sin(angle) * r)) >> 4;
                if (seen.add(InnerChunkCollector.chunkKey(chunkX, chunkZ))) {
                    consumer.accept(chunkX, chunkZ);
                }
            }
        }
    }

    /** Chunk keys the awakening wave enqueues for a drug's spoke corridor (test visibility). */
    static Set<Long> pathChunkKeysForTest(int centerX, int centerZ, DrugId drugId) {
        Set<Long> keys = new LinkedHashSet<>();
        forEachPathChunkCoordinate(centerX, centerZ, drugId,
                (chunkX, chunkZ) -> keys.add(InnerChunkCollector.chunkKey(chunkX, chunkZ)));
        return keys;
    }

    /**
     * Chunk keys the rendered corridor actually occupies: every chunk holding a column whose
     * {@link InnerTerrain#pathStrength} exceeds the spoke tolerance, scanned densely across the
     * corridor's full angular support. The enqueued set (above) must be a superset of this.
     */
    static Set<Long> renderedPathChunkKeysForTest(int centerX, int centerZ, DrugId drugId) {
        long seed = InnerTerrain.seedForSlot(centerX, centerZ);
        double threshold = InnerTerrain.spokeCorridorHalfWidth(); // SPOKE_TOLERANCE
        double landmarkRadius = InnerRegionMap.landmarkRadiusFor(drugId);
        Set<Long> keys = new LinkedHashSet<>();
        for (double r = InnerDimensionConstants.CORE_RADIUS; r <= landmarkRadius; r += 1.0D) {
            double target = InnerTerrain.corridorCenterAngle(seed, r, drugId);
            double reach = threshold * r; // angular support of pathStrength is |off|/r < tolerance
            for (double off = -reach; off <= reach; off += 1.0D) {
                double angle = target + off / r;
                if (InnerTerrain.pathStrengthForTest(seed, r, angle, drugId) > threshold) {
                    int x = centerX + (int) Math.round(Math.cos(angle) * r);
                    int z = centerZ + (int) Math.round(Math.sin(angle) * r);
                    keys.add(InnerChunkCollector.chunkKey(x >> 4, z >> 4));
                }
            }
        }
        return keys;
    }

    private static void addRecreateChunks(InnerChunkCollector.ChunkCollector chunks, InnerDimensionSavedData.IslandState island) {
        addRecreateChunks(chunks, island.centerX(), island.centerZ());
    }

    private static void addRecreateChunks(InnerChunkCollector.ChunkCollector chunks, int centerX, int centerZ) {
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
        forEachRecreateChunkCoordinate(centerX, centerZ, (chunkX, chunkZ) -> keys.add(InnerChunkCollector.chunkKey(chunkX, chunkZ)));
        return keys.size();
    }

    /**
     * Simulate one tick of the loaded-first scheduling policy in {@link #pollNextLoaded} /
     * {@link #requeueDeferred}. Operates on chunk-key {@code long}s rather than {@link ChunkPos}
     * because the {@code ChunkPos} class cannot be initialized in the unit-test JVM (no Minecraft
     * bootstrap); the loop below is a faithful mirror of the production policy. Returns
     * {@code [processed, deferred, remaining]} so tests can assert that loaded chunks are reached
     * even when behind unloaded ones, that processing is bounded by {@code processBudget}, that the
     * scan cap bounds how many unloaded chunks are examined, and that deferred chunks are preserved
     * (re-queued, never dropped or force-loaded).
     */
    static int[] loadedFirstSchedulingForTest(int[][] chunkCoords, boolean[] loadedFlags, int processBudget, int scanCap) {
        ArrayDeque<Long> deque = new ArrayDeque<>();
        Set<Long> seen = new LinkedHashSet<>();
        Map<Long, Boolean> loadedByKey = new HashMap<>();
        for (int i = 0; i < chunkCoords.length; i++) {
            long key = InnerChunkCollector.chunkKey(chunkCoords[i][0], chunkCoords[i][1]);
            if (seen.add(key)) {
                deque.add(key);
            }
            loadedByKey.put(key, loadedFlags[i]);
        }
        ArrayDeque<Long> deferred = new ArrayDeque<>();
        int[] scanBudget = {Math.min(deque.size(), scanCap)};
        int processed = 0;
        int deferredCount = 0;
        int budget = processBudget;
        scheduling:
        while (budget > 0) {
            Long loadedKey = null;
            while (!deque.isEmpty()) {
                long key = deque.removeFirst();
                if (Boolean.TRUE.equals(loadedByKey.get(key))) {
                    loadedKey = key;
                    break;
                }
                deferred.add(key);
                deferredCount++;
                if (--scanBudget[0] <= 0) {
                    break scheduling;
                }
            }
            if (loadedKey == null) {
                break;
            }
            processed++;
            budget--;
        }
        deque.addAll(deferred); // re-queue chunks set aside as unloaded, preserving them
        return new int[] {processed, deferredCount, deque.size()};
    }

    /**
     * Pure JVM mirror of overlay owner scheduling. Each owner's boolean array is its chunk queue:
     * {@code true} chunks are loaded and processable, {@code false} chunks are deferred. Returns
     * processed chunk counts per owner after {@code ticks} simulated ticks.
     */
    static int[] roundRobinOwnerSchedulingForTest(boolean[][] loadedByOwner, int processBudget, int scanCap, int ticks) {
        ArrayList<ArrayDeque<Integer>> queues = new ArrayList<>(loadedByOwner.length);
        for (boolean[] ownerChunks : loadedByOwner) {
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            for (int chunk = 0; chunk < ownerChunks.length; chunk++) {
                queue.add(chunk);
            }
            queues.add(queue);
        }

        int[] processed = new int[loadedByOwner.length];
        int cursor = 0;
        for (int tick = 0; tick < ticks; tick++) {
            int chunksLeft = processBudget;
            int[] scanBudgets = new int[queues.size()];
            for (int owner = 0; owner < queues.size(); owner++) {
                scanBudgets[owner] = Math.min(queues.get(owner).size(), scanCap);
            }

            int ownerIndex = nextSimulatedOwnerIndex(queues, cursor);
            if (ownerIndex < 0) {
                break;
            }
            int visitsWithoutProgress = 0;
            while (chunksLeft > 0 && visitsWithoutProgress < queues.size()) {
                if (ownerIndex >= queues.size()) {
                    ownerIndex = 0;
                }
                int owner = ownerIndex;
                ownerIndex = (ownerIndex + 1) % queues.size();

                ArrayDeque<Integer> queue = queues.get(owner);
                if (queue.isEmpty()) {
                    visitsWithoutProgress++;
                    continue;
                }

                int ownerBudget = activeSimulatedOwnerCount(queues) <= 1 ? chunksLeft : 1;
                int processedBefore = processed[owner];
                if (scanBudgets[owner] > 0) {
                    ArrayDeque<Integer> deferred = new ArrayDeque<>();
                    scheduling:
                    while (ownerBudget > 0) {
                        Integer loadedChunk = null;
                        while (!queue.isEmpty()) {
                            int chunk = queue.removeFirst();
                            if (loadedByOwner[owner][chunk]) {
                                loadedChunk = chunk;
                                break;
                            }
                            deferred.add(chunk);
                            if (--scanBudgets[owner] <= 0) {
                                break scheduling;
                            }
                        }
                        if (loadedChunk == null) {
                            break;
                        }
                        processed[owner]++;
                        chunksLeft--;
                        ownerBudget--;
                    }
                    queue.addAll(deferred);
                }

                if (processed[owner] > processedBefore) {
                    visitsWithoutProgress = 0;
                } else {
                    visitsWithoutProgress++;
                }
            }

            int next = nextSimulatedOwnerIndex(queues, ownerIndex);
            cursor = next < 0 ? 0 : next;
        }
        return processed;
    }

    private static int activeSimulatedOwnerCount(ArrayList<ArrayDeque<Integer>> queues) {
        int count = 0;
        for (ArrayDeque<Integer> queue : queues) {
            if (!queue.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private static int nextSimulatedOwnerIndex(ArrayList<ArrayDeque<Integer>> queues, int startIndex) {
        if (queues.isEmpty()) {
            return -1;
        }
        for (int offset = 0; offset < queues.size(); offset++) {
            int index = (startIndex + offset) % queues.size();
            if (!queues.get(index).isEmpty()) {
                return index;
            }
        }
        return -1;
    }

    static boolean processedChunkCanBeRequeuedForTest() {
        long key = InnerChunkCollector.chunkKey(4, -7);
        Set<Long> queued = new LinkedHashSet<>();
        queued.add(key);
        queued.remove(key);
        return queued.add(key);
    }

    static boolean destructiveQueueIsSeparateFromOverlayQueueForTest() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-00000000d00d");
        try {
            RECREATE_QUEUES.put(owner, new QueueState(new ArrayDeque<>(), new LinkedHashSet<>(), QueueMode.FULL_RECREATE));
            appendQueue(owner, new InnerChunkCollector.ChunkCollector());
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

    static int totalWorkUnitsForTest(boolean fullRecreate, int enqueuedChunks) {
        return totalWorkUnits(fullRecreate, enqueuedChunks);
    }

    static String phaseIdForTest(boolean fullRecreate, boolean decoratePhase) {
        return phaseId(fullRecreate, decoratePhase);
    }

    /** A recreate queue snapshot reports owner/type/phase and is removed from the active map. */
    static boolean recreateProgressSnapshotForTest() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-00000000c0de");
        try {
            RECREATE_QUEUES.put(owner, new QueueState(new ArrayDeque<>(), new LinkedHashSet<>(), QueueMode.FULL_RECREATE));
            Optional<QueueProgress> progress = recreateProgress(owner);
            return progress.isPresent()
                    && progress.get().owner().equals(owner)
                    && progress.get().queueType().equals("full_recreate")
                    && progress.get().phase().equals("terrain");
        } finally {
            RECREATE_QUEUES.remove(owner);
        }
    }

    static boolean persistedRecreateRestoresQueueForTest() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-00000000c0df");
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        try {
            InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
            data.markRecreateJobPending(owner);
            InnerDimensionSavedData.RecreateJobState job = data.pendingRecreateJob(owner);
            return job != null
                    && job.fullRecreate()
                    && fullRecreateChunkCountForTest(island.centerX(), island.centerZ()) > 0
                    && queueStatus(data, owner).contains("will resume");
        } finally {
            RECREATE_QUEUES.remove(owner);
            RECREATE_CURSOR.drop(owner);
        }
    }

    static boolean completingRecreateClearsPersistedMarkerForTest() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-00000000c0e0");
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        QueueState state = new QueueState(new ArrayDeque<>(), new LinkedHashSet<>(), QueueMode.FULL_RECREATE);
        data.getOrCreateIsland(owner);
        data.markRecreateJobPending(owner);
        completeQueue(data, owner, state);
        return data.pendingRecreateJob(owner) == null;
    }

    static boolean queueStatusReportsPersistedRecreateForTest() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-00000000c0e1");
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        try {
            data.getOrCreateIsland(owner);
            data.markRecreateJobPending(owner);
            String status = queueStatus(data, owner);
            return status.contains("incomplete persisted full_recreate")
                    && status.contains("will resume");
        } finally {
            RECREATE_QUEUES.remove(owner);
            OVERLAY_QUEUES.remove(owner);
        }
    }

    /** Cancelling reports which queues were active and clears both from the live maps. */
    static boolean cancelReportsAndClearsForTest() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-00000000cafe");
        try {
            RECREATE_QUEUES.put(owner, new QueueState(new ArrayDeque<>(), new LinkedHashSet<>(), QueueMode.FULL_RECREATE));
            OVERLAY_QUEUES.put(owner, new QueueState(new ArrayDeque<>(), new LinkedHashSet<>(), QueueMode.OVERLAY));
            CancelSummary summary = cancelWithSummary(owner);
            boolean clearedMaps = !RECREATE_QUEUES.containsKey(owner) && !OVERLAY_QUEUES.containsKey(owner);
            CancelSummary second = cancelWithSummary(owner);
            return summary.cancelledRecreate()
                    && summary.cancelledOverlay()
                    && summary.cancelledAnything()
                    && clearedMaps
                    && !second.cancelledAnything();
        } finally {
            RECREATE_QUEUES.remove(owner);
            OVERLAY_QUEUES.remove(owner);
        }
    }

    static boolean logoutCancelsOverlayButPreservesRecreateForTest() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-00000000b10c");
        try {
            RECREATE_QUEUES.put(owner, new QueueState(new ArrayDeque<>(), new LinkedHashSet<>(), QueueMode.FULL_RECREATE));
            OVERLAY_QUEUES.put(owner, new QueueState(new ArrayDeque<>(), new LinkedHashSet<>(), QueueMode.OVERLAY));
            CancelSummary summary = cancelOverlayForLogout(owner);
            return summary.cancelledOverlay()
                    && !summary.cancelledRecreate()
                    && RECREATE_QUEUES.containsKey(owner)
                    && !OVERLAY_QUEUES.containsKey(owner);
        } finally {
            RECREATE_QUEUES.remove(owner);
            OVERLAY_QUEUES.remove(owner);
        }
    }

    static boolean logoutQueueStatusKeepsRecreateVisibleForTest() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-00000000b10d");
        try {
            RECREATE_QUEUES.put(owner, new QueueState(new ArrayDeque<>(), new LinkedHashSet<>(), QueueMode.FULL_RECREATE));
            OVERLAY_QUEUES.put(owner, new QueueState(new ArrayDeque<>(), new LinkedHashSet<>(), QueueMode.OVERLAY));
            cancelOverlayForLogout(owner);
            String status = queueStatus(owner);
            return status.contains("destructive:")
                    && status.contains("type=full_recreate")
                    && !status.contains("overlay:");
        } finally {
            RECREATE_QUEUES.remove(owner);
            OVERLAY_QUEUES.remove(owner);
        }
    }

    static boolean decoratedLoadGuardSkipsAndInvalidatesForTest() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-00000000dec0");
        long key = InnerChunkCollector.chunkKey(4, -7);
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

    /** Overlay chunks run full decoration, so processing one must mark it decorated. */
    static boolean overlayPhaseMarksDecoratedForTest() {
        return phaseDecoratesChunk(QueueMode.OVERLAY, false);
    }

    /** A recreate's terrain-rebuild phase does not decorate, so it must not mark decorated. */
    static boolean recreateTerrainPhaseMarksDecoratedForTest() {
        return phaseDecoratesChunk(QueueMode.FULL_RECREATE, false);
    }

    /** A recreate's second (decorate) phase does decorate, so it must mark decorated. */
    static boolean recreateDecoratePhaseMarksDecoratedForTest() {
        return phaseDecoratesChunk(QueueMode.FULL_RECREATE, true);
    }

    /** While a recreate is in flight for an owner, the lazy on-load guard must suppress enqueues. */
    static boolean recreateInFlightSuppressesLazyDecorationForTest() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-00000000dec2");
        long key = InnerChunkCollector.chunkKey(3, 5);
        try {
            invalidateDecorated(owner);
            RECREATE_QUEUES.remove(owner);
            boolean allowedWhenIdle = shouldQueueLoadedChunk(owner, key);
            RECREATE_QUEUES.put(owner, new QueueState(new ArrayDeque<>(), new LinkedHashSet<>(), QueueMode.FULL_RECREATE));
            boolean suppressedDuringRecreate = !shouldQueueLoadedChunk(owner, key);
            RECREATE_QUEUES.remove(owner);
            boolean allowedAfterRecreate = shouldQueueLoadedChunk(owner, key);
            return allowedWhenIdle && suppressedDuringRecreate && allowedAfterRecreate;
        } finally {
            RECREATE_QUEUES.remove(owner);
            invalidateDecorated(owner);
        }
    }

    static boolean decoratedLoadGuardIsBoundedForTest() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-00000000dec1");
        try {
            invalidateDecorated(owner);
            for (int i = 0; i <= DECORATED_CHUNK_CAP_PER_OWNER; i++) {
                markDecorated(owner, InnerChunkCollector.chunkKey(i, -i));
            }
            LinkedHashMap<Long, Boolean> decorated = DECORATED_CHUNKS.get(owner);
            return decorated != null
                    && decorated.size() == DECORATED_CHUNK_CAP_PER_OWNER
                    && !isDecorated(owner, InnerChunkCollector.chunkKey(0, 0))
                    && isDecorated(owner, InnerChunkCollector.chunkKey(DECORATED_CHUNK_CAP_PER_OWNER, -DECORATED_CHUNK_CAP_PER_OWNER));
        } finally {
            invalidateDecorated(owner);
        }
    }

    static boolean landmarkChunkCacheIsCenterKeyedForTest() {
        LANDMARK_CHUNKS_BY_CENTER.clear();
        InnerChunkCollector.LandmarkChunk[] first = landmarkChunksForCenter(0, 0);
        InnerChunkCollector.LandmarkChunk[] second = landmarkChunksForCenter(0, 0);
        InnerChunkCollector.LandmarkChunk[] other = landmarkChunksForCenter(InnerDimensionConstants.SLOT_SPACING, 0);
        boolean cached = first == second && first != other;
        clearIslandStaticCaches(0, 0);
        InnerChunkCollector.LandmarkChunk[] afterClear = landmarkChunksForCenter(0, 0);
        LANDMARK_CHUNKS_BY_CENTER.clear();
        return cached && first != afterClear;
    }

    /**
     * Whether a freshly-loaded chunk should be enqueued for lazy decoration. Skipped when the chunk
     * is already decorated, or when its owner has a full recreate in flight — the recreate rebuilds
     * and decorates the whole island area itself, so a parallel lazy pass would be redundant work
     * (the recreate marks the chunk decorated when its decorate phase reaches it).
     */
    private static boolean shouldQueueLoadedChunk(UUID owner, long key) {
        return !RECREATE_QUEUES.containsKey(owner) && !isDecorated(owner, key);
    }

    private static boolean isDecorated(UUID owner, long key) {
        LinkedHashMap<Long, Boolean> chunks = DECORATED_CHUNKS.get(owner);
        return chunks != null && Boolean.TRUE.equals(chunks.get(key));
    }

    /** Record a chunk as decorated. Call only after the decoration phase has actually run. */
    private static void markDecorated(UUID owner, ChunkPos chunkPos) {
        markDecorated(owner, InnerChunkCollector.chunkKey(chunkPos));
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

    private static void clearIslandStaticCaches(int centerX, int centerZ) {
        LANDMARK_CHUNKS_BY_CENTER.remove(InnerChunkCollector.centerKey(centerX, centerZ));
        InnerTerrain.clearSatelliteCenters(centerX, centerZ);
    }

    private enum QueueMode {
        OVERLAY("overlay"),
        FULL_RECREATE("full_recreate");

        private final String id;

        QueueMode(String id) {
            this.id = id;
        }
    }

    private static final class QueueCursor {
        private UUID nextOwner;

        int startIndex(List<UUID> owners) {
            if (nextOwner != null) {
                int index = owners.indexOf(nextOwner);
                if (index >= 0) {
                    return index;
                }
            }
            return 0;
        }

        void set(UUID owner) {
            nextOwner = owner;
        }

        void drop(UUID owner) {
            if (owner != null && owner.equals(nextOwner)) {
                nextOwner = null;
            }
        }

        void clear() {
            nextOwner = null;
        }
    }

    /**
     * Immutable, admin-visible snapshot of one queue's progress. Computed on demand from a live
     * {@link QueueState}; never persisted. {@code totalChunks} counts chunk-work units, so a
     * full recreate (terrain pass + decorate pass) reports twice its enqueued chunk count.
     */
    public record QueueProgress(
            UUID owner,
            String queueType,
            String phase,
            int totalChunks,
            int processedChunks,
            int pendingChunks,
            int cumulativeUnloadedDeferrals,
            int placedBlocks,
            int skippedBlocks,
            long maxChunkMillis,
            long elapsedSeconds
    ) {
        public int percentComplete() {
            if (totalChunks <= 0) {
                return processedChunks > 0 ? 100 : 0;
            }
            return (int) Math.min(100L, (100L * processedChunks) / totalChunks);
        }

        /** Whether the queue appears stuck on unloaded chunks (many cumulative deferrals vs low processed). */
        public boolean appearsStuckOnUnloaded() {
            return processedChunks <= 2 && cumulativeUnloadedDeferrals >= 4;
        }

        public String describe() {
            StringBuilder out = new StringBuilder();
            out.append("type=").append(queueType)
                    .append(" phase=").append(phase)
                    .append(" progress=").append(processedChunks).append('/').append(totalChunks)
                    .append(" (").append(percentComplete()).append("%)");
            if (placedBlocks > 0 || skippedBlocks > 0) {
                out.append(" blocks_placed=").append(placedBlocks)
                        .append(" skipped=").append(skippedBlocks);
            }
            out.append(" pending=").append(pendingChunks)
                    .append(" unloaded_deferrals_total=").append(cumulativeUnloadedDeferrals);
            if (appearsStuckOnUnloaded()) {
                out.append(" [STUCK: mostly unloaded chunks]");
            }
            if (elapsedSeconds > 0) {
                out.append(" elapsed=").append(formatSeconds(elapsedSeconds));
            }
            if (maxChunkMillis > 0) {
                out.append(" max_chunk_ms=").append(maxChunkMillis);
            }
            return out.toString();
        }
    }

    /** Result of cancelling an owner's queues: which were active and how much work was abandoned. */
    public record CancelSummary(
            boolean cancelledRecreate,
            boolean cancelledOverlay,
            int recreatePendingDropped,
            int overlayPendingDropped
    ) {
        public boolean cancelledAnything() {
            return cancelledRecreate || cancelledOverlay;
        }

        public String describe() {
            if (!cancelledAnything()) {
                return "No active Inner Dimension queue to cancel.";
            }
            StringBuilder out = new StringBuilder("Cancelled Inner Dimension");
            if (cancelledRecreate) {
                out.append(" destructive recreate (")
                        .append(recreatePendingDropped)
                        .append(" chunk-units abandoned)");
            }
            if (cancelledOverlay) {
                out.append(cancelledRecreate ? " and" : "")
                        .append(" overlay refresh (")
                        .append(overlayPendingDropped)
                        .append(" chunk-units abandoned)");
            }
            if (cancelledRecreate) {
                out.append(". Destructive recreate was explicitly abandoned; rebuilt terrain remains and another recreate should be run if a complete pass is still required.");
            } else {
                out.append(". Overlay work can be regenerated by later refresh or chunk-load decoration.");
            }
            return out.toString();
        }
    }

    /** Total chunk-work units a queue performs: a full recreate runs two passes, overlay one. */
    private static int totalWorkUnits(boolean fullRecreate, int enqueuedChunks) {
        return fullRecreate ? enqueuedChunks * 2 : enqueuedChunks;
    }

    /** Human-readable lifecycle phase id for a queue. */
    private static String phaseId(boolean fullRecreate, boolean decoratePhase) {
        if (!fullRecreate) {
            return "overlay";
        }
        return decoratePhase ? "decorate" : "terrain";
    }

    private static final class QueueState {
        private final ArrayDeque<ChunkPos> chunks;
        private final Set<Long> queued;
        private final QueueMode mode;
        private final long startedMillis;
        private int enqueuedChunks;
        private int processedChunks;
        // Cumulative count of unloaded-chunk skips over this queue's life. This is a churn signal,
        // not the current number of unavailable pending chunks.
        private int cumulativeUnloadedDeferrals;
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

        QueueState append(InnerChunkCollector.ChunkCollector extra) {
            int added = 0;
            for (ChunkPos chunkPos : extra.chunks) {
                if (queued.add(InnerChunkCollector.chunkKey(chunkPos))) {
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
            queued.remove(InnerChunkCollector.chunkKey(chunkPos));
            return chunkPos;
        }

        void defer(ChunkPos chunkPos) {
            if (queued.add(InnerChunkCollector.chunkKey(chunkPos))) {
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
                    if (queued.add(InnerChunkCollector.chunkKey(chunkPos))) {
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

        /** Total chunk-work units this queue performs across all of its phases. */
        int totalWork() {
            return totalWorkUnits(mode == QueueMode.FULL_RECREATE, enqueuedChunks);
        }

        /** Remaining chunk-work units, including the not-yet-started decorate pass of a recreate. */
        int pendingWork() {
            return Math.max(0, totalWork() - processedChunks);
        }

        QueueProgress progress(UUID owner) {
            return new QueueProgress(
                    owner,
                    mode.id,
                    phaseId(mode == QueueMode.FULL_RECREATE, decoratePhase),
                    totalWork(),
                    processedChunks,
                    pendingWork(),
                    cumulativeUnloadedDeferrals,
                    placedBlocks,
                    skippedBlocks,
                    maxChunkMillis,
                    (System.currentTimeMillis() - startedMillis) / 1000L
            );
        }
    }
}
