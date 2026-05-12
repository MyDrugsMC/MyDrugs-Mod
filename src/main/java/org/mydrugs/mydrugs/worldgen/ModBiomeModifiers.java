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
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier;

public final class ModBiomeModifiers {
    private ModBiomeModifiers() {
    }

    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);

        context.register(
                ModWorldGenKeys.ADD_SALT_TO_OCEANS,
                new AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OCEAN),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.SALT_DISK_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );

        context.register(
                ModWorldGenKeys.ADD_SULFUR_ORE,
                new AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.SULFUR_ORE_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );

        context.register(
                ModWorldGenKeys.ADD_PLATINUM_ORE,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.PLATINUM_ORE_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );

        context.register(
                ModWorldGenKeys.ADD_ALUMINIUM_ORE,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.ALUMINIUM_ORE_PLACED)),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );

        // Vanilla desert only.
        context.register(
                ModWorldGenKeys.ADD_PETROLEUM_LAKES_TO_DESERTS,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        HolderSet.direct(biomes.getOrThrow(Biomes.DESERT)),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.PETROLEUM_LAKE_SURFACE_PLACED)),
                        GenerationStep.Decoration.LAKES
                )
        );

        context.register(
                ModWorldGenKeys.ADD_ALOE_VERA_TO_OVERWORLD,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.ALOE_VERA_PATCH_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION
                )
        );

        // Bitter Nut Bush in jungle + forest biomes
        context.register(
                ModWorldGenKeys.ADD_BITTER_NUT_BUSH,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        HolderSet.direct(
                                biomes.getOrThrow(Biomes.JUNGLE),
                                biomes.getOrThrow(Biomes.SPARSE_JUNGLE),
                                biomes.getOrThrow(Biomes.BAMBOO_JUNGLE),
                                biomes.getOrThrow(Biomes.FOREST),
                                biomes.getOrThrow(Biomes.DARK_FOREST),
                                biomes.getOrThrow(Biomes.BIRCH_FOREST)
                        ),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.BITTER_NUT_BUSH_PATCH_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION
                )
        );

        // Third Eye Petal: mountain biomes only (HeightRangePlacement gates Y >= 130 inside the placed feature)
        context.register(
                ModWorldGenKeys.ADD_THIRD_EYE_PETAL,
                new BiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_MOUNTAIN),
                        HolderSet.direct(placedFeatures.getOrThrow(ModWorldGenKeys.THIRD_EYE_PETAL_PATCH_PLACED)),
                        GenerationStep.Decoration.VEGETAL_DECORATION
                )
        );
    }
}
