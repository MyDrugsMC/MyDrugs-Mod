package org.mydrugs.mydrugs.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedBlockStateProvider;
import org.mydrugs.mydrugs.blocks.ModBlocks;

import java.util.List;

public final class ModConfiguredFeatures {
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<Block> blockLookup = context.lookup(Registries.BLOCK);

        // Salt: clay-like seafloor patches in oceans.
        DiskConfiguration saltDiskConfig = new DiskConfiguration(
                RuleBasedBlockStateProvider.simple(ModBlocks.SALT_BLOCK.get()),
                BlockPredicate.allOf(
                        BlockPredicate.matchesBlocks(Blocks.SAND, Blocks.DIRT, Blocks.GRAVEL, Blocks.CLAY),
                        BlockPredicate.matchesFluids(new Vec3i(0, 1, 0), Fluids.WATER)
                ),
                UniformInt.of(2, 4), // radius
                2                    // half-height
        );

        context.register(
                ModWorldGenKeys.SALT_DISK,
                new ConfiguredFeature<>(Feature.DISK, saltDiskConfig)
        );

        // Sulfur ore: one configured feature that can place stone and deepslate variants.
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
    }

    private ModConfiguredFeatures() {}
}