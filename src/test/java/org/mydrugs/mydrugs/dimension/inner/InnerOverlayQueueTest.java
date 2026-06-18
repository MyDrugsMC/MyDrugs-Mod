package org.mydrugs.mydrugs.dimension.inner;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

class InnerOverlayQueueTest {

    @Test
    void queueDeduplicatesChunksBeforeProcessing() {
        int[][] chunks = {
                {0, 0},
                {0, 0},
                {1, 0},
                {1, 0},
                {0, 1}
        };

        assertEquals(3, InnerOverlayQueue.deduplicatedChunkCoordinateCountForTest(chunks));
    }

    @Test
    void processedChunksCanBeQueuedAgain() {
        assertTrue(InnerOverlayQueue.processedChunkCanBeRequeuedForTest());
    }

    @Test
    void loadedChunksAreNotStarvedBehindUnloadedOnes() {
        // Three unloaded chunks sit in front of two loaded ones; both loaded chunks must still be
        // processed this tick, and the unloaded chunks are preserved (re-queued), not consumed.
        int[][] chunks = {{0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 0}};
        boolean[] loaded = {false, false, false, true, true};

        int[] result = InnerOverlayQueue.loadedFirstSchedulingForTest(chunks, loaded, 6, 64);

        assertEquals(2, result[0], "both loaded chunks should be processed");
        assertEquals(3, result[1], "the three unloaded chunks should be deferred");
        assertEquals(3, result[2], "deferred chunks remain queued; processed chunks leave the queue");
    }

    @Test
    void processingIsBoundedByPerTickBudget() {
        int[][] chunks = {{0, 0}, {1, 0}, {2, 0}, {3, 0}, {4, 0}, {5, 0}, {6, 0}, {7, 0}};
        boolean[] loaded = {true, true, true, true, true, true, true, true};

        int[] result = InnerOverlayQueue.loadedFirstSchedulingForTest(chunks, loaded, 6, 64);

        assertEquals(6, result[0], "no more than the per-tick budget is processed");
        assertEquals(0, result[1], "no deferrals when every chunk is loaded");
        assertEquals(2, result[2], "the over-budget chunks stay queued for next tick");
    }

    @Test
    void scanCapBoundsUnloadedExaminationWithoutLoadingChunks() {
        // A long run of unloaded chunks ahead of a single loaded one: the scan cap stops the hunt
        // so the tick stays bounded (no endless spinning), and nothing is dropped or force-loaded.
        int[][] chunks = new int[101][2];
        boolean[] loaded = new boolean[101];
        for (int i = 0; i < 100; i++) {
            chunks[i] = new int[] {i, 0};
            loaded[i] = false;
        }
        chunks[100] = new int[] {100, 0};
        loaded[100] = true;

        int[] result = InnerOverlayQueue.loadedFirstSchedulingForTest(chunks, loaded, 6, 10);

        assertEquals(0, result[0], "the loaded chunk is beyond the scan cap, so none processed this tick");
        assertEquals(10, result[1], "exactly the scan cap's worth of unloaded chunks are examined");
        assertEquals(101, result[2], "no chunk is dropped; the queue merely rotates");
    }

    @Test
    void roundRobinGivesLaterOwnersTurnsWithLowPerTickBudget() {
        boolean[][] queues = {
                {true, true, true, true},
                {true, true, true, true}
        };

        int[] processed = InnerOverlayQueue.roundRobinOwnerSchedulingForTest(queues, 1, 64, 4);

        assertArrayEquals(new int[] {2, 2}, processed,
                "a one-chunk tick budget should rotate between owners instead of draining the first owner");
    }

    @Test
    void unloadedOwnerQueueDoesNotStarveLoadedOwnerQueue() {
        boolean[][] queues = {
                {false, false, false, false},
                {true, true}
        };

        int[] processed = InnerOverlayQueue.roundRobinOwnerSchedulingForTest(queues, 1, 64, 1);

        assertArrayEquals(new int[] {0, 1}, processed,
                "unloaded chunks for one owner should not consume another owner's processing turn");
    }

    @Test
    void roundRobinCursorSurvivesOwnerCompletion() {
        boolean[][] queues = {
                {true},
                {true, true}
        };

        int[] processed = InnerOverlayQueue.roundRobinOwnerSchedulingForTest(queues, 1, 64, 3);

        assertArrayEquals(new int[] {1, 2}, processed,
                "removing a completed owner should not reset or break later owner progress");
    }

    @Test
    void fullRecreateQueuesWholeOwnerArea() {
        assertTrue(InnerOverlayQueue.fullRecreateChunkCountForTest(0, 0) > 40_000);
    }

    @Test
    void destructiveQueueCannotAbsorbOverlayAppends() {
        assertTrue(InnerOverlayQueue.destructiveQueueIsSeparateFromOverlayQueueForTest());
    }

    @Test
    void landmarkChunkCacheIsKeyedByIslandCenter() {
        assertTrue(InnerOverlayQueue.landmarkChunkCacheIsCenterKeyedForTest());
    }

    @Test
    void awakeningWaveEnqueuesEveryRenderedCorridorChunk() {
        // The wandering spoke corridor bends away from a straight centre->landmark line. The
        // awakening enqueue must cover every chunk the rendered corridor (pathStrength > spoke
        // tolerance) actually occupies, or the Phase 8 outward wave leaves gaps in the bent middle.
        for (DrugId drug : CuratedDrugChain.ORDER) {
            Set<Long> enqueued = InnerOverlayQueue.pathChunkKeysForTest(0, 0, drug);
            Set<Long> rendered = InnerOverlayQueue.renderedPathChunkKeysForTest(0, 0, drug);
            assertTrue(!rendered.isEmpty(), drug + " corridor produced no rendered chunks");
            for (Long chunkKey : rendered) {
                assertTrue(enqueued.contains(chunkKey),
                        drug + " awakening wave misses rendered corridor chunk key " + chunkKey);
            }
        }
    }

    @Test
    void fullRecreateTotalWorkCountsBothPasses() {
        // A recreate runs a terrain pass and a decorate pass over the same chunks, so its reported
        // total work is twice the enqueued chunk count; overlay is a single pass.
        assertEquals(200, InnerOverlayQueue.totalWorkUnitsForTest(true, 100));
        assertEquals(100, InnerOverlayQueue.totalWorkUnitsForTest(false, 100));
        assertEquals(0, InnerOverlayQueue.totalWorkUnitsForTest(true, 0));
    }

    @Test
    void recreatePhaseIdReflectsLifecycle() {
        assertEquals("terrain", InnerOverlayQueue.phaseIdForTest(true, false));
        assertEquals("decorate", InnerOverlayQueue.phaseIdForTest(true, true));
        assertEquals("overlay", InnerOverlayQueue.phaseIdForTest(false, false));
    }

    @Test
    void recreateDecorationThreadsRecreateModeToStructureBuilders() throws Exception {
        String queue = Files.readString(Path.of(
                "src/main/java/org/mydrugs/mydrugs/dimension/inner/InnerOverlayQueue.java"
        ));
        assertTrue(queue.contains("decorateChunk(level, island, chunkPos, cache, count, InnerPlacement.PlacementMode.RECREATE)"));
        assertTrue(queue.contains("InnerSanctuaryBuilder.placeCenterSanctuary(level, island, count, mode)"));
        assertTrue(queue.contains("InnerVaults.placeVaultChests(level, data, island, chunkPos, count, mode)"));
        assertTrue(queue.contains("InnerSkyShardLootBuilder.placeRewards(level, data, island, chunkPos, cache, count, mode)"));
        assertTrue(queue.contains("InnerLandmarkBuilder.placeLandmark(level, island, drugId, unlocked, count, mode)"));
    }

    @Test
    void liveOverlayDecorationKeepsLiveOverlayMode() throws Exception {
        String queue = Files.readString(Path.of(
                "src/main/java/org/mydrugs/mydrugs/dimension/inner/InnerOverlayQueue.java"
        ));
        assertTrue(queue.contains("decorateChunk(level, island, chunkPos, cache, count, InnerPlacement.PlacementMode.LIVE_OVERLAY)"));
    }

    @Test
    void recreateTerrainFeaturePassUsesRecreateMode() throws Exception {
        String rebuilder = Files.readString(Path.of(
                "src/main/java/org/mydrugs/mydrugs/dimension/inner/InnerChunkRebuilder.java"
        ));
        assertTrue(rebuilder.contains("InnerPlacement.PlacementMode.RECREATE"));
    }

    @Test
    void queueProgressReportsMeaningfulFields() {
        InnerOverlayQueue.QueueProgress progress = new InnerOverlayQueue.QueueProgress(
                new java.util.UUID(0L, 7L), "full_recreate", "terrain", 200, 50, 150, 3, 400, 20, 850L, 12L);

        assertEquals(25, progress.percentComplete());
        String describe = progress.describe();
        assertTrue(describe.contains("type=full_recreate"), describe);
        assertTrue(describe.contains("phase=terrain"), describe);
        assertTrue(describe.contains("progress=50/200"), describe);
        assertTrue(describe.contains("pending=150"), describe);
        assertTrue(describe.contains("unloaded_deferrals_total=3"), describe);
        assertTrue(describe.contains("blocks_placed=400"), describe);
        assertTrue(describe.contains("skipped=20"), describe);
        assertTrue(describe.contains("max_chunk_ms=850"), describe);
    }

    @Test
    void emptyQueueProgressIsNotDivideByZero() {
        InnerOverlayQueue.QueueProgress progress = new InnerOverlayQueue.QueueProgress(
                new java.util.UUID(0L, 1L), "overlay", "overlay", 0, 0, 0, 0, 0, 0, 0L, 0L);
        assertEquals(0, progress.percentComplete());
        assertFalse(progress.describe().contains("blocks_placed"));
    }

    @Test
    void recreateProgressSnapshotIsExposed() {
        assertTrue(InnerOverlayQueue.recreateProgressSnapshotForTest());
    }

    @Test
    void persistedRecreateMarkerRestoresQueue() {
        assertTrue(InnerOverlayQueue.persistedRecreateRestoresQueueForTest());
    }

    @Test
    void completingRecreateClearsPersistedMarker() {
        assertTrue(InnerOverlayQueue.completingRecreateClearsPersistedMarkerForTest());
    }

    @Test
    void queueStatusReportsPersistedRecreateMarker() {
        assertTrue(InnerOverlayQueue.queueStatusReportsPersistedRecreateForTest());
    }

    @Test
    void cancellationReportsAndClearsQueues() {
        assertTrue(InnerOverlayQueue.cancelReportsAndClearsForTest());
    }

    @Test
    void logoutCancelsOverlayButPreservesFullRecreate() {
        assertTrue(InnerOverlayQueue.logoutCancelsOverlayButPreservesRecreateForTest());
    }

    @Test
    void logoutStatusKeepsActiveRecreateVisible() {
        assertTrue(InnerOverlayQueue.logoutQueueStatusKeepsRecreateVisibleForTest());
    }

    @Test
    void cancelSummaryDescribesAbandonedWork() {
        InnerOverlayQueue.CancelSummary cancelled = new InnerOverlayQueue.CancelSummary(true, false, 42, 0);
        assertTrue(cancelled.cancelledAnything());
        assertTrue(cancelled.describe().contains("destructive recreate (42 chunk-units abandoned)"),
                cancelled.describe());
        assertTrue(cancelled.describe().contains("explicitly abandoned"), cancelled.describe());

        InnerOverlayQueue.CancelSummary overlay = new InnerOverlayQueue.CancelSummary(false, true, 0, 7);
        assertTrue(overlay.describe().contains("overlay refresh (7 chunk-units abandoned)"), overlay.describe());
        assertTrue(overlay.describe().contains("regenerated"), overlay.describe());

        InnerOverlayQueue.CancelSummary none = new InnerOverlayQueue.CancelSummary(false, false, 0, 0);
        assertFalse(none.cancelledAnything());
        assertTrue(none.describe().contains("No active"), none.describe());
    }

    @Test
    void cancelOnIdleOwnerIsSafeAndReportsNoWork() {
        // Unconditional cancel on logout or dimension change must be safe
        // even when the owner has no active queues.
        var randomOwner = new java.util.UUID(0L, 999L);
        assertFalse(InnerOverlayQueue.cancel(randomOwner));
        InnerOverlayQueue.CancelSummary summary = InnerOverlayQueue.cancelWithSummary(randomOwner);
        assertFalse(summary.cancelledAnything());
        assertFalse(summary.cancelledRecreate());
        assertFalse(summary.cancelledOverlay());
        assertEquals(0, summary.recreatePendingDropped());
        assertEquals(0, summary.overlayPendingDropped());
    }
}
