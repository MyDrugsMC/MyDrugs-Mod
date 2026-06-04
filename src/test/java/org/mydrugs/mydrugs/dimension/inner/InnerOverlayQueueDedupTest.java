package org.mydrugs.mydrugs.dimension.inner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
