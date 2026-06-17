package org.mydrugs.mydrugs.dimension.inner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Note: ChunkPos cannot be constructed in a pure unit test (its static init pulls in the
// not-bootstrapped MC registries), so dedup is exercised through the coordinate-based helper.
class InnerOverlayQueueDedupTest {

    @Test
    void duplicateCoordinatesAreCollapsed() {
        int[][] coords = {
                {0, 0},
                {0, 0},
                {5, -2},
                {5, -2},
                {5, -3}
        };
        assertEquals(3, InnerOverlayQueue.deduplicatedChunkCoordinateCountForTest(coords));
    }

    @Test
    void fullRecreateChunkCountIsPositiveAndDeterministic() {
        int a = InnerOverlayQueue.fullRecreateChunkCountForTest(0, 0);
        int b = InnerOverlayQueue.fullRecreateChunkCountForTest(0, 0);
        assertEquals(a, b, "full recreate chunk count must be deterministic");
        assertTrue(a > 0, "full recreate must cover at least one chunk");
    }

    @Test
    void aProcessedChunkCanBeRequeued() {
        assertTrue(InnerOverlayQueue.processedChunkCanBeRequeuedForTest(),
                "a chunk removed from the queued set must be re-addable");
    }

    @Test
    void destructiveAndOverlayQueuesStaySeparate() {
        assertTrue(InnerOverlayQueue.destructiveQueueIsSeparateFromOverlayQueueForTest());
    }

    @Test
    void decoratedLoadGuardSkipsAndInvalidates() {
        assertTrue(InnerOverlayQueue.decoratedLoadGuardSkipsAndInvalidatesForTest());
    }

    @Test
    void decoratedLoadGuardIsBounded() {
        assertTrue(InnerOverlayQueue.decoratedLoadGuardIsBoundedForTest());
    }

    @Test
    void overlayProcessingMarksChunkDecorated() {
        // Overlay runs the full decoration pass, so it must not be left un-marked (which would let
        // the lazy on-load guard re-enqueue and re-decorate it endlessly).
        assertTrue(InnerOverlayQueue.overlayPhaseMarksDecoratedForTest(),
                "overlay processing must mark the chunk decorated");
    }

    @Test
    void recreateTerrainPhaseDoesNotFalselyMarkDecorated() {
        // The terrain-rebuild phase only clears/rebuilds terrain; structures are placed in a later
        // phase, so a terrain-phase chunk must not yet count as decorated.
        assertFalse(InnerOverlayQueue.recreateTerrainPhaseMarksDecoratedForTest(),
                "recreate terrain phase must not mark the chunk decorated");
    }

    @Test
    void recreateDecoratePhaseMarksChunkDecorated() {
        assertTrue(InnerOverlayQueue.recreateDecoratePhaseMarksDecoratedForTest(),
                "recreate decorate phase must mark the chunk decorated");
    }

    @Test
    void recreateInFlightSuppressesLazyDecoration() {
        // A full recreate decorates the whole area itself; lazy per-chunk decoration during it would
        // duplicate that work, so the on-load guard must suppress while the recreate is queued.
        assertTrue(InnerOverlayQueue.recreateInFlightSuppressesLazyDecorationForTest());
    }
}
