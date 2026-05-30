package org.mydrugs.mydrugs.dimension.inner.v7;

public record InnerV7GenerationMetrics(
        int queuedChunks,
        int processedChunks,
        int placedBlocks,
        int skippedBlocks,
        long elapsedMillis
) {
    public static final InnerV7GenerationMetrics EMPTY = new InnerV7GenerationMetrics(0, 0, 0, 0, 0L);

    public String toDebugString() {
        return "queued=" + queuedChunks
                + ", processed=" + processedChunks
                + ", placed=" + placedBlocks
                + ", skipped=" + skippedBlocks
                + ", ms=" + elapsedMillis;
    }
}
