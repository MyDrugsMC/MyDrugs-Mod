package org.mydrugs.mydrugs.dimension.inner;

public record InnerGenerationMetrics(
        int processedChunks,
        int enqueuedChunks,
        int placedBlocks,
        int skippedBlocks,
        long elapsedMillis,
        long sampleComputes,
        long groveComputes,
        long sceneComputes,
        long maxChunkMillis,
        long[] perBuilderNanos
) {
    static final long[] NO_BUILDER_NANOS = new long[0];

    public static final InnerGenerationMetrics EMPTY =
            new InnerGenerationMetrics(0, 0, 0, 0, 0L);

    public InnerGenerationMetrics(
            int processedChunks,
            int enqueuedChunks,
            int placedBlocks,
            int skippedBlocks,
            long elapsedMillis
    ) {
        this(processedChunks, enqueuedChunks, placedBlocks, skippedBlocks, elapsedMillis,
                0L, 0L, 0L, 0L, NO_BUILDER_NANOS);
    }

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
                + ", blocks_per_sec=" + String.format(java.util.Locale.ROOT, "%.2f", blocksPerSecond)
                + ", samples=" + sampleComputes
                + ", groves=" + groveComputes
                + ", scenes=" + sceneComputes
                + ", max_chunk_ms=" + maxChunkMillis
                + builderDebug();
    }

    private String builderDebug() {
        java.util.List<String> names = InnerVisualFeatureBuilders.builderOrderForTest();
        if (perBuilderNanos.length != names.size()) {
            return "";
        }
        StringBuilder out = new StringBuilder(", builders_ms=[");
        for (int i = 0; i < perBuilderNanos.length; i++) {
            if (i > 0) {
                out.append(' ');
            }
            out.append(names.get(i))
                    .append('=')
                    .append(String.format(java.util.Locale.ROOT, "%.1f", perBuilderNanos[i] / 1_000_000.0D));
        }
        return out.append(']').toString();
    }
}
