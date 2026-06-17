package org.mydrugs.mydrugs.dimension.inner;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for InnerOverlayQueue admin/debug status formatting.
 */
class InnerOverlayQueueStatusTest {

    private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void queueProgressDescribeIncludesBlockCountsWhenNonZero() {
        var progress = new InnerOverlayQueue.QueueProgress(
                OWNER, "overlay", "decorate",
                100, 50, 50, 2,
                1234, 56, 0L, 30L
        );
        String desc = progress.describe();
        assertTrue(desc.contains("type=overlay"));
        assertTrue(desc.contains("phase=decorate"));
        assertTrue(desc.contains("progress=50/100"));
        assertTrue(desc.contains("50%"));
        assertTrue(desc.contains("blocks_placed=1234"));
        assertTrue(desc.contains("skipped=56"));
        assertTrue(desc.contains("pending=50"));
        assertTrue(desc.contains("unloaded_deferrals_total=2"));
        assertTrue(desc.contains("elapsed=30s"));
    }

    @Test
    void queueProgressDescribeOmitsBlockCountsWhenZero() {
        var progress = new InnerOverlayQueue.QueueProgress(
                OWNER, "overlay", "decorate",
                10, 5, 5, 0,
                0, 0, 0L, 0L
        );
        String desc = progress.describe();
        assertFalse(desc.contains("blocks_placed"));
        assertFalse(desc.contains("skipped="));
        assertFalse(desc.contains("elapsed="));
        assertFalse(desc.contains("max_chunk_ms"));
    }

    @Test
    void queueProgressShowsStuckFlagWhenDeferredHighAndProcessedLow() {
        var stuck = new InnerOverlayQueue.QueueProgress(
                OWNER, "full_recreate", "terrain",
                200, 2, 198, 8,
                0, 0, 0L, 0L
        );
        assertTrue(stuck.appearsStuckOnUnloaded());
        String desc = stuck.describe();
        assertTrue(desc.contains("STUCK: mostly unloaded chunks"));

        var notStuck = new InnerOverlayQueue.QueueProgress(
                OWNER, "full_recreate", "terrain",
                200, 50, 150, 3,
                0, 0, 0L, 0L
        );
        assertFalse(notStuck.appearsStuckOnUnloaded());
    }

    @Test
    void queueProgressShowsSlowChunkTimeWhenPresent() {
        var progress = new InnerOverlayQueue.QueueProgress(
                OWNER, "full_recreate", "terrain",
                100, 50, 50, 0,
                5000, 100, 1200L, 45L
        );
        String desc = progress.describe();
        assertTrue(desc.contains("max_chunk_ms=1200"));
        assertTrue(desc.contains("elapsed=45s"));
    }

    @Test
    void queueProgressElapsedFormatsMinutesWhenOver60Seconds() {
        var progress = new InnerOverlayQueue.QueueProgress(
                OWNER, "full_recreate", "decorate",
                100, 90, 10, 0,
                8000, 200, 350L, 125L
        );
        String desc = progress.describe();
        assertTrue(desc.contains("elapsed=2m5s"));
    }

    @Test
    void queueProgressPercentCompleteHandlesZeroTotal() {
        var progress = new InnerOverlayQueue.QueueProgress(
                OWNER, "overlay", "decorate",
                0, 0, 0, 0,
                0, 0, 0L, 0L
        );
        assertEquals(0, progress.percentComplete());
    }

    @Test
    void queueProgressPercentCompleteHandlesMidProgress() {
        var progress = new InnerOverlayQueue.QueueProgress(
                OWNER, "full_recreate", "terrain",
                200, 75, 125, 0,
                0, 0, 0L, 0L
        );
        assertEquals(37, progress.percentComplete());
    }

    @Test
    void queueProgressPercentCompleteCapsAt100() {
        var progress = new InnerOverlayQueue.QueueProgress(
                OWNER, "full_recreate", "terrain",
                100, 120, 0, 0,
                0, 0, 0L, 0L
        );
        assertEquals(100, progress.percentComplete());
    }
}
