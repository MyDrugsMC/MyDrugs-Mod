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
        double seconds = elapsedMillis <= 0L ? 0.0D : elapsedMillis / 1000.0D;
        double chunksPerSecond = seconds <= 0.0D ? 0.0D : processedChunks / seconds;
        double blocksPerSecond = seconds <= 0.0D ? 0.0D : placedBlocks / seconds;
        return "processed=" + processedChunks
                + ", enqueued=" + enqueuedChunks
                + ", placed=" + placedBlocks
                + ", skipped=" + skippedBlocks
                + ", elapsed_ms=" + elapsedMillis
                + ", chunks_per_sec=" + String.format(java.util.Locale.ROOT, "%.2f", chunksPerSecond)
                + ", blocks_per_sec=" + String.format(java.util.Locale.ROOT, "%.2f", blocksPerSecond);
    }
}
