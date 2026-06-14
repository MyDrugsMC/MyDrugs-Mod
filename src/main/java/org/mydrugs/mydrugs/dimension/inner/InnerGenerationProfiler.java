package org.mydrugs.mydrugs.dimension.inner;

/**
 * Thread-confined counters for server-side generation passes. All hooks are no-ops unless a
 * pass is active on the current thread, so worldgen-thread chunk fills pay only a ThreadLocal
 * read per counted event. Never used to influence generation — observation only.
 */
final class InnerGenerationProfiler {
    private static final ThreadLocal<Counters> ACTIVE = new ThreadLocal<>();

    private InnerGenerationProfiler() {
    }

    static void begin() {
        ACTIVE.set(new Counters());
    }

    /** Ends the active pass and returns its counters, or {@code null} if no pass was active. */
    static Counters end() {
        Counters counters = ACTIVE.get();
        ACTIVE.remove();
        return counters;
    }

    static void countSample() {
        Counters counters = ACTIVE.get();
        if (counters != null) {
            counters.sampleComputes++;
        }
    }

    static void countGrove() {
        Counters counters = ACTIVE.get();
        if (counters != null) {
            counters.groveComputes++;
        }
    }

    static void countScene() {
        Counters counters = ACTIVE.get();
        if (counters != null) {
            counters.sceneComputes++;
        }
    }

    static void recordBuilderNanos(int builderIndex, long nanos) {
        Counters counters = ACTIVE.get();
        if (counters != null && builderIndex >= 0 && builderIndex < counters.builderNanos.length) {
            counters.builderNanos[builderIndex] += nanos;
        }
    }

    static final class Counters {
        long sampleComputes;
        long groveComputes;
        long sceneComputes;
        final long[] builderNanos = new long[InnerVisualFeatureBuilders.builderCount()];
    }
}
