package org.mydrugs.mydrugs.worldgen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
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
    }
}