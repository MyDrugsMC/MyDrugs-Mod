package org.mydrugs.mydrugs.dimension.inner;

public record InnerGenerationMetrics(
        int processedChunks,
        int enqueuedChunks,
        int placedBlocks,
        int skippedBlocks,
        long elapsedMillis
) {
    public static final InnerGenerationMetrics EMPTY =
            new InnerGenerationMetrics(0, 0, 0, 0, 0L);

    public String toDebugString() {
        return "processed=" + processedChunks
                + ", enqueued=" + enqueuedChunks
                + ", placed=" + placedBlocks
                + ", skipped=" + skippedBlocks
                + ", elapsed_ms=" + elapsedMillis;
    }
}
