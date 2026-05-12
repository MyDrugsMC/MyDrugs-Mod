package org.mydrugs.mydrugs.worldgen;

import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.core.Vec3i;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.crops.ModCrops;
import org.mydrugs.mydrugs.fluids.ModFluids;

import java.util.List;

public final class ModConfiguredFeatures {
    private ModConfiguredFeatures() {
    }

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        // Salt: clay-like seafloor patches in oceans.
        DiskConfiguration saltDiskConfig = new DiskConfiguration(
                RuleBasedBlockStateProvider.simple(ModBlocks.SALT_BLOCK.get()),
                BlockPredicate.allOf(
                        BlockPredicate.matchesBlocks(Blocks.SAND, Blocks.DIRT, Blocks.GRAVEL, Blocks.CLAY),
                        BlockPredicate.matchesFluids(new Vec3i(0, 1, 0), Fluids.WATER)
                ),
                UniformInt.of(2, 4),
                2
        );

        context.register(
                ModWorldGenKeys.SALT_DISK,
                new ConfiguredFeature<>(Feature.DISK, saltDiskConfig)
        );

        // Sulfur ore.
        List<OreConfiguration.TargetBlockState> sulfurTargets = List.of(
                OreConfiguration.target(
                        new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                        ModBlocks.SULFUR_ORE.get().defaultBlockState()
                ),
                OreConfiguration.target(
                        new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                        ModBlocks.DEEPSLATE_SULFUR_ORE.get().defaultBlockState()
                )
        );

        context.register(
                ModWorldGenKeys.SULFUR_ORE,
                new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(sulfurTargets, 7))
        );

        // Platinum ore: diamond-like, normal + deepslate variants.
        List<OreConfiguration.TargetBlockState> platinumTargets = List.of(
                OreConfiguration.target(
                        new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                        ModBlocks.PLATINUM_ORE.get().defaultBlockState()
                ),
                OreConfiguration.target(
                        new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                        ModBlocks.DEEPSLATE_PLATINUM_ORE.get().defaultBlockState()
                )
        );

        List<OreConfiguration.TargetBlockState> aluminiumTargets = List.of(
                OreConfiguration.target(
                        new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                        ModBlocks.ALUMINIUM_ORE.get().defaultBlockState()
                ),
                OreConfiguration.target(
                        new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                        ModBlocks.DEEPSLATE_ALUMINIUM_ORE.get().defaultBlockState()
                )
        );

        context.register(
                ModWorldGenKeys.PLATINUM_ORE,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(platinumTargets, 4, 0.5F)
                )
        );

        context.register(
                ModWorldGenKeys.ALUMINIUM_ORE,
                new ConfiguredFeature<>(
                        Feature.ORE,
                        new OreConfiguration(aluminiumTargets, 4, 0.5F)
                )
        );

        // Petroleum lake: real LakeFeature, using your already-registered petroleum fluid block.
        LakeFeature.Configuration petroleumLakeConfig = new LakeFeature.Configuration(
                SimpleStateProvider.simple(ModFluids.PETROLEUM.block().get().defaultBlockState()),
                SimpleStateProvider.simple(Blocks.STONE.defaultBlockState())
        );

        context.register(
                ModWorldGenKeys.PETROLEUM_LAKE,
                new ConfiguredFeature<>(Feature.LAKE, petroleumLakeConfig)
        );

        context.register(
                ModWorldGenKeys.ALOE_VERA_PATCH,
                new ConfiguredFeature<>(
                        Feature.RANDOM_PATCH,
                        new RandomPatchConfiguration(
                                10,
                                4,
                                2,
                                PlacementUtils.onlyWhenEmpty(
                                        Feature.SIMPLE_BLOCK,
                                        new SimpleBlockConfiguration(SimpleStateProvider.simple(ModCrops.ALOE_VERA_CROP.get().defaultBlockState()))
                                )
                        )
                )
        );

        // Bitter Nut Bush: jungle/forest small patches (a few bushes per patch).
        context.register(
                ModWorldGenKeys.BITTER_NUT_BUSH_PATCH,
                new ConfiguredFeature<>(
                        Feature.RANDOM_PATCH,
                        new RandomPatchConfiguration(
                                6,
                                5,
                                2,
                                PlacementUtils.onlyWhenEmpty(
                                        Feature.SIMPLE_BLOCK,
                                        new SimpleBlockConfiguration(SimpleStateProvider.simple(
                                                ModBlocks.BITTER_NUT_BUSH.get().defaultBlockState()
                                        ))
                                )
                        )
                )
        );

        // Third Eye Petal: rare flower patches.
        context.register(
                ModWorldGenKeys.THIRD_EYE_PETAL_PATCH,
                new ConfiguredFeature<>(
                        Feature.RANDOM_PATCH,
                        new RandomPatchConfiguration(
                                4,
                                3,
                                1,
                                PlacementUtils.onlyWhenEmpty(
                                        Feature.SIMPLE_BLOCK,
                                        new SimpleBlockConfiguration(SimpleStateProvider.simple(
                                                ModBlocks.THIRD_EYE_PETAL.get().defaultBlockState()
                                        ))
                                )
                        )
                )
        );
    }
}
