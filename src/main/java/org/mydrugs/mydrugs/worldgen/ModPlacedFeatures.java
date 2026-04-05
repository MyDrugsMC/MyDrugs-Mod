package org.mydrugs.mydrugs.worldgen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public final class ModPlacedFeatures {
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
                                CountPlacement.of(10),
                                InSquarePlacement.spread(),
                                HeightRangePlacement.uniform(
                                        VerticalAnchor.absolute(-64),
                                        VerticalAnchor.absolute(64)
                                ),
                                BiomeFilter.biome()
                        )
                )
        );
    }

    private ModPlacedFeatures() {}
}