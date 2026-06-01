package org.mydrugs.mydrugs.dimension.inner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
    void fullRecreateQueuesWholeOwnerArea() {
        assertTrue(InnerOverlayQueue.fullRecreateChunkCountForTest(0, 0) > 40_000);
    }

    @Test
    void destructiveQueueCannotAbsorbOverlayAppends() {
        assertTrue(InnerOverlayQueue.destructiveQueueIsSeparateFromOverlayQueueForTest());
    }
}
