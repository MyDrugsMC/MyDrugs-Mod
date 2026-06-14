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

    private InnerVisualFeatureBuilders() {
    }

    public static void placeInitialFeatures(ChunkAccess chunk, InnerChunkSampleCache cache) {
        InnerLakeBuilder.placeInitialLakeDetails(chunk, cache);
        InnerLakeSceneBuilder.placeInitialLakeScenes(chunk, cache);
        InnerRiverBuilder.placeInitialRivers(chunk, cache);
        InnerCoastDramaBuilder.placeInitialCoastDrama(chunk, cache);
        InnerSpikeBuilder.placeInitialSpikes(chunk, cache);
        InnerTalusBuilder.placeInitialTalus(chunk, cache);
        InnerMegaFormBuilder.placeInitialMegaForms(chunk, cache);
        InnerHeroFeatureBuilder.placeInitialHeroFeatures(chunk, cache);
        InnerTreeBuilder.placeInitialTrees(chunk, cache);
        InnerPathSceneBuilder.placeInitialPathScenes(chunk, cache);
        InnerLandmarkApproachBuilder.placeInitialLandmarkApproaches(chunk, cache);
        InnerTransitionSceneBuilder.placeInitialTransitionScenes(chunk, cache);
        InnerVaultBuilder.placeInitialVaults(chunk, cache);
        InnerPlantBuilder.placeInitialPlants(chunk, cache);
        InnerGlowBuilder.placeInitialGlow(chunk, cache);
        InnerSkyShardBuilder.placeInitialSkyShardDetails(chunk, cache);
    }

    /** Overlay steps, index-aligned with {@link #BUILDER_ORDER} for per-builder metrics. */
    private static final OverlayStep[] OVERLAY_STEPS = {
            InnerLakeBuilder::placeOverlayLakeDetails,
            InnerLakeSceneBuilder::placeOverlayLakeScenes,
            InnerRiverBuilder::placeOverlayRivers,
            InnerCoastDramaBuilder::placeOverlayCoastDrama,
            InnerSpikeBuilder::placeOverlaySpikes,
            InnerTalusBuilder::placeOverlayTalus,
            InnerMegaFormBuilder::placeOverlayMegaForms,
            InnerHeroFeatureBuilder::placeOverlayHeroFeatures,
            InnerTreeBuilder::placeOverlayTrees,
            InnerPathSceneBuilder::placeOverlayPathScenes,
            InnerLandmarkApproachBuilder::placeOverlayLandmarkApproaches,
            InnerTransitionSceneBuilder::placeOverlayTransitionScenes,
            InnerVaultBuilder::placeOverlayVaults,
            InnerPlantBuilder::placeOverlayPlants,
            InnerGlowBuilder::placeOverlayGlow,
            InnerSkyShardBuilder::placeOverlaySkyShardDetails
    };

    static void placeOverlayFeatures(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        for (int i = 0; i < OVERLAY_STEPS.length; i++) {
            long start = System.nanoTime();
            OVERLAY_STEPS[i].place(level, chunkPos, cache, count);
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
        return OVERLAY_STEPS.length;
    }

    @FunctionalInterface
    private interface OverlayStep {
        void place(ServerLevel level, ChunkPos chunkPos, InnerChunkSampleCache cache, InnerPlacement.MutablePlacementCount count);
    }
}
