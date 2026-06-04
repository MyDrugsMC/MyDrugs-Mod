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
            "hero_features",
            "grove_trees",
            "path_scenes",
            "landmark_approaches",
            "transition_scenes",
            "flora",
            "glow_details"
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
        InnerHeroFeatureBuilder.placeInitialHeroFeatures(chunk, cache);
        InnerTreeBuilder.placeInitialTrees(chunk, cache);
        InnerPathSceneBuilder.placeInitialPathScenes(chunk, cache);
        InnerLandmarkApproachBuilder.placeInitialLandmarkApproaches(chunk, cache);
        InnerTransitionSceneBuilder.placeInitialTransitionScenes(chunk, cache);
        InnerPlantBuilder.placeInitialPlants(chunk, cache);
        InnerGlowBuilder.placeInitialGlow(chunk, cache);
    }

    static void placeOverlayFeatures(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        InnerLakeBuilder.placeOverlayLakeDetails(level, chunkPos, cache, count);
        InnerLakeSceneBuilder.placeOverlayLakeScenes(level, chunkPos, cache, count);
        InnerRiverBuilder.placeOverlayRivers(level, chunkPos, cache, count);
        InnerCoastDramaBuilder.placeOverlayCoastDrama(level, chunkPos, cache, count);
        InnerSpikeBuilder.placeOverlaySpikes(level, chunkPos, cache, count);
        InnerTalusBuilder.placeOverlayTalus(level, chunkPos, cache, count);
        InnerHeroFeatureBuilder.placeOverlayHeroFeatures(level, chunkPos, cache, count);
        InnerTreeBuilder.placeOverlayTrees(level, chunkPos, cache, count);
        InnerPathSceneBuilder.placeOverlayPathScenes(level, chunkPos, cache, count);
        InnerLandmarkApproachBuilder.placeOverlayLandmarkApproaches(level, chunkPos, cache, count);
        InnerTransitionSceneBuilder.placeOverlayTransitionScenes(level, chunkPos, cache, count);
        InnerPlantBuilder.placeOverlayPlants(level, chunkPos, cache, count);
        InnerGlowBuilder.placeOverlayGlow(level, chunkPos, cache, count);
    }

    static List<String> builderOrderForTest() {
        return BUILDER_ORDER;
    }
}
