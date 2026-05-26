package org.mydrugs.mydrugs.worldgen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;

public final class ModBiomeModifiers {
    private ModBiomeModifiers() {
    }

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);

        context.register(
                ModWorldGenKeys.ADD_SALT_TO_OCEANS,
                new ConfigurableAddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OCEAN),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.SALT_DISK_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES,
                        WorldgenConfig.SALT
                )
        );

        context.register(
                ModWorldGenKeys.ADD_SULFUR_ORE,
                new ConfigurableAddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.SULFUR_ORE_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES,
                        WorldgenConfig.SULFUR_ORE
                )
        );

        context.register(
                ModWorldGenKeys.ADD_PLATINUM_ORE,
                new ConfigurableAddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.PLATINUM_ORE_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES,
                        WorldgenConfig.PLATINUM_ORE
                )
        );

        context.register(
                ModWorldGenKeys.ADD_PHOSPHATE_ORE,
                new ConfigurableAddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.PHOSPHATE_ORE_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES,
                        WorldgenConfig.PHOSPHATE_ORE
                )
        );

        context.register(
                ModWorldGenKeys.ADD_ALUMINIUM_ORE,
                new ConfigurableAddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.ALUMINIUM_ORE_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES,
                        WorldgenConfig.ALUMINIUM_ORE
                )
        );

        // Vanilla desert only.
        context.register(
                ModWorldGenKeys.ADD_PETROLEUM_LAKES_TO_DESERTS,
                new ConfigurableAddFeaturesBiomeModifier(
                        HolderSet.direct(biomes.getOrThrow(Biomes.DESERT)),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.PETROLEUM_LAKE_SURFACE_PLACED)),
                        GenerationStep.Decoration.LAKES,
                        WorldgenConfig.PETROLEUM_LAKE
                )
        );

        context.register(
                ModWorldGenKeys.ADD_ALOE_VERA_TO_OVERWORLD,
                new ConfigurableAddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.ALOE_VERA_PATCH_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        WorldgenConfig.ALOE_VERA
                )
        );

        context.register(
                ModWorldGenKeys.ADD_LAVENDER_TO_MEADOWS,
                new ConfigurableAddFeaturesBiomeModifier(
                        HolderSet.direct(
                                biomes.getOrThrow(Biomes.MEADOW),
                                biomes.getOrThrow(Biomes.PLAINS),
                                biomes.getOrThrow(Biomes.SUNFLOWER_PLAINS),
                                biomes.getOrThrow(Biomes.FLOWER_FOREST)
                        ),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.LAVENDER_PATCH_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        WorldgenConfig.LAVENDER
                )
        );

        context.register(
                ModWorldGenKeys.ADD_VALERIAN_TO_FORESTS,
                new ConfigurableAddFeaturesBiomeModifier(
                        HolderSet.direct(
                                biomes.getOrThrow(Biomes.FOREST),
                                biomes.getOrThrow(Biomes.BIRCH_FOREST),
                                biomes.getOrThrow(Biomes.OLD_GROWTH_BIRCH_FOREST),
                                biomes.getOrThrow(Biomes.RIVER)
                        ),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.VALERIAN_PATCH_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        WorldgenConfig.VALERIAN
                )
        );

        // Bitter Nut Bush in jungle + forest biomes
        context.register(
                ModWorldGenKeys.ADD_BITTER_NUT_BUSH,
                new ConfigurableAddFeaturesBiomeModifier(
                        HolderSet.direct(
                                biomes.getOrThrow(Biomes.JUNGLE),
                                biomes.getOrThrow(Biomes.SPARSE_JUNGLE),
                                biomes.getOrThrow(Biomes.BAMBOO_JUNGLE),
                                biomes.getOrThrow(Biomes.FOREST),
                                biomes.getOrThrow(Biomes.DARK_FOREST),
                                biomes.getOrThrow(Biomes.BIRCH_FOREST)
                        ),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.BITTER_NUT_BUSH_PATCH_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        WorldgenConfig.BITTER_NUT_BUSH
                )
        );

        // Ephedra: arid biomes (desert / savanna / badlands)
        context.register(
                ModWorldGenKeys.ADD_EPHEDRA,
                new ConfigurableAddFeaturesBiomeModifier(
                        HolderSet.direct(
                                biomes.getOrThrow(Biomes.DESERT),
                                biomes.getOrThrow(Biomes.SAVANNA),
                                biomes.getOrThrow(Biomes.SAVANNA_PLATEAU),
                                biomes.getOrThrow(Biomes.WINDSWEPT_SAVANNA),
                                biomes.getOrThrow(Biomes.BADLANDS),
                                biomes.getOrThrow(Biomes.ERODED_BADLANDS),
                                biomes.getOrThrow(Biomes.WOODED_BADLANDS)
                        ),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.EPHEDRA_PATCH_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        WorldgenConfig.EPHEDRA
                )
        );

        // Third Eye Petal: mountain biomes only (HeightRangePlacement gates Y >= 130 inside the placed feature)
        context.register(
                ModWorldGenKeys.ADD_THIRD_EYE_PETAL,
                new ConfigurableAddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_MOUNTAIN),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.THIRD_EYE_PETAL_PATCH_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        WorldgenConfig.THIRD_EYE_PETAL
                )
        );
    }
}
