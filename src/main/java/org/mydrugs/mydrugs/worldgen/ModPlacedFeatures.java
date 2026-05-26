package org.mydrugs.mydrugs.worldgen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import org.mydrugs.mydrugs.Config;

import java.util.List;

public final class ModPlacedFeatures {
    private ModPlacedFeatures() {
    }

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        context.register(
                ModWorldGenKeys.SALT_DISK_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(ModWorldGenKeys.SALT_DISK),
                        List.of(
                                RarityFilter.onAverageOnceEvery(4),
                                InSquarePlacement.spread(),
                                HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR_WG),
                                BiomeFilter.biome()
                        )
                )
        );

        context.register(
                ModWorldGenKeys.SULFUR_ORE_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(ModWorldGenKeys.SULFUR_ORE),
                        List.of(
                                CountPlacement.of(Config.WORLDGEN.sulfurVeinsPerChunk.get()),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(WorldgenConfig.orderedMinHeight(Config.WORLDGEN.sulfurMinHeight.get(), Config.WORLDGEN.sulfurMaxHeight.get())),
                                        VerticalAnchor.absolute(WorldgenConfig.orderedMaxHeight(Config.WORLDGEN.sulfurMinHeight.get(), Config.WORLDGEN.sulfurMaxHeight.get()))
                                ),
                                BiomeFilter.biome()
                        )
                )
        );

        context.register(
                ModWorldGenKeys.PLATINUM_ORE_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(ModWorldGenKeys.PLATINUM_ORE),
                        List.of(
                                CountPlacement.of(Config.WORLDGEN.platinumVeinsPerChunk.get()),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(WorldgenConfig.orderedMinHeight(Config.WORLDGEN.platinumMinHeight.get(), Config.WORLDGEN.platinumMaxHeight.get())),
                                        VerticalAnchor.absolute(WorldgenConfig.orderedMaxHeight(Config.WORLDGEN.platinumMinHeight.get(), Config.WORLDGEN.platinumMaxHeight.get()))
                                ),
                                BiomeFilter.biome()
                        )
                )
        );

        context.register(
                ModWorldGenKeys.ALUMINIUM_ORE_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(ModWorldGenKeys.ALUMINIUM_ORE),
                        List.of(
                                CountPlacement.of(Config.WORLDGEN.aluminiumVeinsPerChunk.get()),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(WorldgenConfig.orderedMinHeight(Config.WORLDGEN.aluminiumMinHeight.get(), Config.WORLDGEN.aluminiumMaxHeight.get())),
                                        VerticalAnchor.absolute(WorldgenConfig.orderedMaxHeight(Config.WORLDGEN.aluminiumMinHeight.get(), Config.WORLDGEN.aluminiumMaxHeight.get()))
                                ),
                                BiomeFilter.biome()
                        )
                )
        );

        context.register(
                ModWorldGenKeys.PETROLEUM_LAKE_SURFACE_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(ModWorldGenKeys.PETROLEUM_LAKE),
                        List.of(
                                RarityFilter.onAverageOnceEvery(200),
                                InSquarePlacement.spread(),
                                HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                BiomeFilter.biome()
                        )
                )
        );

        context.register(
                ModWorldGenKeys.ALOE_VERA_PATCH_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(ModWorldGenKeys.ALOE_VERA_PATCH),
                        List.of(
                                RarityFilter.onAverageOnceEvery(Config.WORLDGEN.aloeVeraSpawnRate.get()),
                                InSquarePlacement.spread(),
                                HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                BiomeFilter.biome()
                        )
                )
        );

        context.register(
                ModWorldGenKeys.LAVENDER_PATCH_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(ModWorldGenKeys.LAVENDER_PATCH),
                        List.of(
                                RarityFilter.onAverageOnceEvery(Config.WORLDGEN.lavenderSpawnRate.get()),
                                InSquarePlacement.spread(),
                                HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                BiomeFilter.biome()
                        )
                )
        );

        context.register(
                ModWorldGenKeys.VALERIAN_PATCH_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(ModWorldGenKeys.VALERIAN_PATCH),
                        List.of(
                                RarityFilter.onAverageOnceEvery(Config.WORLDGEN.valerianSpawnRate.get()),
                                InSquarePlacement.spread(),
                                HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                BiomeFilter.biome()
                        )
                )
        );

        // Bitter Nut Bush patches in jungle/forest (uncommon)
        context.register(
                ModWorldGenKeys.BITTER_NUT_BUSH_PATCH_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(ModWorldGenKeys.BITTER_NUT_BUSH_PATCH),
                        List.of(
                                RarityFilter.onAverageOnceEvery(Config.WORLDGEN.bitterNutBushSpawnRate.get()),
                                InSquarePlacement.spread(),
                                HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                BiomeFilter.biome()
                        )
                )
        );

        // Third Eye Petal: very rare, restricted to Y >= 130
        context.register(
                ModWorldGenKeys.THIRD_EYE_PETAL_PATCH_PLACED,
                new PlacedFeature(
                        configuredFeatures.getOrThrow(ModWorldGenKeys.THIRD_EYE_PETAL_PATCH),
                        List.of(
                                RarityFilter.onAverageOnceEvery(Config.WORLDGEN.thirdEyePetalSpawnRate.get()),
                                InSquarePlacement.spread(),
                                HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(130),
                                        VerticalAnchor.absolute(256)
                                ),
                                BiomeFilter.biome()
                        )
                )
        );
    }
}
