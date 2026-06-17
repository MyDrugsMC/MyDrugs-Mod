package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.List;

public final class InnerVisualFeatureBuilders {
    private static final List<String> BUILDER_ORDER = List.of(
            "lake_details",
            "lake_scenes",
            "rivers",
            "coast_drama",
            "spikes",
            "talus",
            "mega_forms",
            "hero_features",
            "grove_trees",
            "path_scenes",
            "landmark_approaches",
            "transition_scenes",
            "vaults",
            "flora",
            "glow_details",
            "sky_shards"
    );

    /**
     * The visual builders in canonical order, index-aligned with {@link #BUILDER_ORDER} (and so
     * with {@code InnerGenerationProfiler}'s per-builder nanos / {@code QueueState.perBuilderNanos}).
     * Each builder is written once against {@link InnerBlockSink}; the worldgen-thread initial pass
     * and the server-thread overlay pass run the same {@code place} method through the appropriate
     * sink, so the two passes cannot diverge.
     */
    private static final VisualBuilder[] BUILDERS = {
            InnerLakeBuilder::place,
            InnerLakeSceneBuilder::place,
            InnerRiverBuilder::place,
            InnerCoastDramaBuilder::place,
            InnerSpikeBuilder::place,
            InnerTalusBuilder::place,
            InnerMegaFormBuilder::place,
            InnerHeroFeatureBuilder::place,
            InnerTreeBuilder::place,
            InnerPathSceneBuilder::place,
            InnerLandmarkApproachBuilder::place,
            InnerTransitionSceneBuilder::place,
            InnerVaultBuilder::place,
            InnerPlantBuilder::place,
            InnerGlowBuilder::place,
            InnerSkyShardBuilder::place
    };

    private InnerVisualFeatureBuilders() {
    }

    /** Worldgen-thread pass: dress the chunk being generated via a chunk-backed sink. */
    public static void placeInitialFeatures(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        InnerBlockSink sink = InnerBlockSink.forChunk(chunk);
        for (VisualBuilder builder : BUILDERS) {
            builder.place(sink, cache, minX, minZ);
        }
    }

    /** Server-thread overlay pass: idempotent re-dress via a {@link InnerPlacement#safeSet} sink. */
    static void placeOverlayFeatures(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeOverlayFeatures(level, chunkPos, cache, count, InnerPlacement.PlacementMode.LIVE_OVERLAY);
    }

    /** Server-thread overlay/recreate pass with explicit placement mode. */
    static void placeOverlayFeatures(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        InnerBlockSink sink = InnerBlockSink.forLevel(level, count, mode);
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        for (int i = 0; i < BUILDERS.length; i++) {
            long start = System.nanoTime();
            BUILDERS[i].place(sink, cache, minX, minZ);
            InnerGenerationProfiler.recordBuilderNanos(i, System.nanoTime() - start);
        }
    }

    static int builderCount() {
        return BUILDER_ORDER.size();
    }

    static List<String> builderOrderForTest() {
        return BUILDER_ORDER;
    }

    static int overlayStepCountForTest() {
        return BUILDERS.length;
    }

    /** A visual builder collapsed onto the single sink-based placement API. */
    @FunctionalInterface
    interface VisualBuilder {
        void place(InnerBlockSink sink, InnerChunkSampleCache cache, int minX, int minZ);
    }
}
