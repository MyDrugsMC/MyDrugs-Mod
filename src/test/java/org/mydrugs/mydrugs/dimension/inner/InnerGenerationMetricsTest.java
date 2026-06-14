package org.mydrugs.mydrugs.dimension.inner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InnerGenerationMetricsTest {

    @Test
    void shortConstructorDefaultsExtendedCounters() {
        InnerGenerationMetrics metrics = new InnerGenerationMetrics(1, 2, 3, 4, 5L);
        assertEquals(0L, metrics.sampleComputes());
        assertEquals(0L, metrics.groveComputes());
        assertEquals(0L, metrics.sceneComputes());
        assertEquals(0L, metrics.maxChunkMillis());
        assertEquals(0, metrics.perBuilderNanos().length);
    }

    @Test
    void debugStringExposesSamplerCountersAndBuilderTimings() {
        long[] builderNanos = new long[InnerVisualFeatureBuilders.builderCount()];
        builderNanos[0] = 2_500_000L;
        InnerGenerationMetrics metrics = new InnerGenerationMetrics(
                10, 12, 100, 5, 250L, 2_560L, 640L, 640L, 7L, builderNanos);
        String debug = metrics.toDebugString();
        assertTrue(debug.contains("samples=2560"), debug);
        assertTrue(debug.contains("groves=640"), debug);
        assertTrue(debug.contains("scenes=640"), debug);
        assertTrue(debug.contains("max_chunk_ms=7"), debug);
        assertTrue(debug.contains("builders_ms=[lake_details=2.5"), debug);
    }

    @Test
    void debugStringOmitsBuilderTimingsWhenNotCollected() {
        String debug = InnerGenerationMetrics.EMPTY.toDebugString();
        assertTrue(debug.contains("samples=0"), debug);
        assertFalse(debug.contains("builders_ms"), debug);
    }

    @Test
    void profilerCountsOnlyWhileActiveAndClearsOnEnd() {
        assertNull(InnerGenerationProfiler.end(), "no pass should be active initially");

        InnerGenerationProfiler.countSample(); // inactive: must be a no-op
        InnerGenerationProfiler.begin();
        InnerGenerationProfiler.countSample();
        InnerGenerationProfiler.countSample();
        InnerGenerationProfiler.countGrove();
        InnerGenerationProfiler.countScene();
        InnerGenerationProfiler.recordBuilderNanos(0, 1_000L);
        InnerGenerationProfiler.recordBuilderNanos(0, 500L);
        InnerGenerationProfiler.recordBuilderNanos(InnerVisualFeatureBuilders.builderCount() + 5, 99L);

        InnerGenerationProfiler.Counters counters = InnerGenerationProfiler.end();
        assertNotNull(counters);
        assertEquals(2L, counters.sampleComputes);
        assertEquals(1L, counters.groveComputes);
        assertEquals(1L, counters.sceneComputes);
        assertEquals(1_500L, counters.builderNanos[0]);
        assertEquals(InnerVisualFeatureBuilders.builderCount(), counters.builderNanos.length);

        assertNull(InnerGenerationProfiler.end(), "end must clear the active pass");
    }

    @Test
    void overlayStepListMatchesBuilderOrder() {
        assertEquals(InnerVisualFeatureBuilders.builderOrderForTest().size(),
                InnerVisualFeatureBuilders.overlayStepCountForTest());
    }
}
