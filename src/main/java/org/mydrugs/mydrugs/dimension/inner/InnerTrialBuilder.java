package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

import java.util.List;

final class InnerTrialBuilder {
    private InnerTrialBuilder() {
    }

    static void placeFixtures(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            DrugId drug,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeFixtures(level, island, drug, count, InnerPlacement.PlacementMode.LIVE_OVERLAY);
    }

    static void placeFixtures(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            DrugId drug,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        boolean completed = island.hasCompletedInnerTrial(drug);
        String marker = InnerDimensionConstants.trialFixtureMarker(drug, completed);
        if (island.hasMarker(marker)) {
            return;
        }

        BlockPos landmark = InnerRegionMap.landmarkFor(island.centerX(), island.centerZ(), drug);
        BlockPos center = InnerPlacement.surfaceTop(level, landmark.getX(), landmark.getZ());
        switch (drug) {
            case COFFEE -> coffee(level, center, completed, count, mode);
            case TOBACCO -> tobacco(level, island, center, completed, count, mode);
            case WEED -> weed(level, center, completed, count, mode);
            case HASH -> hash(level, center, completed, count, mode);
            case ALCOHOL -> alcohol(level, center, completed, count, mode);
            case COCAINE -> cocaine(level, center, completed, count, mode);
            case LSD -> lsd(level, island, center, completed, count, mode);
            case METH -> meth(level, center, completed, count, mode);
            case MUSHROOMS -> mushrooms(level, center, completed, count, mode);
            default -> {
                return;
            }
        }
        InnerDimensionSavedData.get(level).markStructurePlaced(island.owner(), marker);
    }

    private static void coffee(
            ServerLevel level,
            BlockPos center,
            boolean completed,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        for (int i = 0; i < 24; i++) {
            double angle = i / 24.0D * Math.PI * 2.0D;
            BlockPos ring = surfaceOffset(level, center, (int) Math.round(Math.cos(angle) * 6.0D),
                    (int) Math.round(Math.sin(angle) * 6.0D));
            structural(level, ring, i % 3 == 0 ? Blocks.SEA_LANTERN.defaultBlockState()
                    : Blocks.SMOOTH_STONE.defaultBlockState(), true, count, mode);
        }
        structural(level, center.above(), completed
                ? ModInnerDimensionBlocks.LUCID_ECHO_NODE.get().defaultBlockState()
                : Blocks.SMOOTH_STONE.defaultBlockState(), true, count, mode);
    }

    private static void tobacco(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            BlockPos center,
            boolean completed,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        List<BlockPos> order = InnerTrialDefinition.tobaccoOrder(island.centerX(), island.centerZ());
        for (int i = 0; i < order.size(); i++) {
            BlockPos offset = order.get(i);
            BlockPos top = surfaceOffset(level, center, offset.getX(), offset.getZ());
            for (int y = 1; y <= i + 1; y++) {
                structural(level, top.above(y), Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), true, count, mode);
            }
            structural(level, top.above(i + 2), completed
                    ? ModInnerDimensionBlocks.LUCID_ECHO_NODE.get().defaultBlockState()
                    : (i % 2 == 0
                    ? ModInnerDimensionBlocks.BITTER_ECHO_NODE.get().defaultBlockState()
                    : ModInnerDimensionBlocks.LUCID_ECHO_NODE.get().defaultBlockState()), true, count, mode);
        }
    }

    private static void weed(
            ServerLevel level,
            BlockPos center,
            boolean completed,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        for (int i = 0; i < 8; i++) {
            double angle = i / 8.0D * Math.PI * 2.0D;
            BlockPos top = surfaceOffset(level, center, (int) Math.round(Math.cos(angle) * 7.0D),
                    (int) Math.round(Math.sin(angle) * 7.0D));
            structural(level, top, Blocks.MOSS_BLOCK.defaultBlockState(), true, count, mode);
            if (completed && (i & 1) == 0) {
                structural(level, top.above(), ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState(), false, count, mode);
            }
        }
        structural(level, center.above(), completed
                ? ModInnerDimensionBlocks.CALMING_ECHO_NODE.get().defaultBlockState()
                : Blocks.MOSS_BLOCK.defaultBlockState(), true, count, mode);
    }

    private static void hash(
            ServerLevel level,
            BlockPos center,
            boolean completed,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        for (BlockPos offset : InnerTrialDefinition.hashSockets()) {
            BlockPos top = surfaceOffset(level, center, offset.getX(), offset.getZ());
            structural(level, top, Blocks.CALCITE.defaultBlockState(), true, count, mode);
            structural(level, top.above(), completed
                    ? ModInnerDimensionBlocks.PRESSED_CALM_NODE.get().defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), true, count, mode);
        }
    }

    private static void alcohol(
            ServerLevel level,
            BlockPos center,
            boolean completed,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        int poolY = center.getY() - 2;
        for (int dz = -3; dz <= 3; dz++) {
            for (int dx = -3; dx <= 3; dx++) {
                double distance = Math.hypot(dx, dz);
                if (distance > 3.5D) {
                    continue;
                }
                BlockPos floor = new BlockPos(center.getX() + dx, poolY, center.getZ() + dz);
                structural(level, floor, distance > 2.7D
                        ? Blocks.DEEPSLATE_TILES.defaultBlockState()
                        : Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState(), true, count, mode);
                structural(level, floor.above(), Blocks.WATER.defaultBlockState(), true, count, mode);
                structural(level, floor.above(2), Blocks.WATER.defaultBlockState(), true, count, mode);
            }
        }
        structural(level, new BlockPos(center.getX(), poolY + 1, center.getZ()),
                ModInnerDimensionBlocks.FERMENTED_MEMORY_NODE.get().defaultBlockState(), true, count, mode);
        if (completed) {
            structural(level, center.above(2), Blocks.SEA_LANTERN.defaultBlockState(), true, count, mode);
        }
    }

    private static void cocaine(
            ServerLevel level,
            BlockPos center,
            boolean completed,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        for (int x = -14; x <= 14; x++) {
            BlockPos top = surfaceOffset(level, center, x, 0);
            structural(level, top, x == -14 || x == 14
                    ? Blocks.REDSTONE_BLOCK.defaultBlockState()
                    : Blocks.SMOOTH_QUARTZ.defaultBlockState(), true, count, mode);
            if (!completed && (x == -5 || x == 4)) {
                structural(level, top.above(), ModInnerDimensionBlocks.REDLINE_THORN.get().defaultBlockState(), false, count, mode);
            }
            BlockPos north = surfaceOffset(level, center, x, -3);
            BlockPos south = surfaceOffset(level, center, x, 3);
            if (x % 3 == 0) {
                structural(level, north.above(), ModInnerDimensionBlocks.REDLINE_THORN.get().defaultBlockState(), false, count, mode);
                structural(level, south.above(), ModInnerDimensionBlocks.REDLINE_THORN.get().defaultBlockState(), false, count, mode);
            }
        }
    }

    private static void lsd(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            BlockPos center,
            boolean completed,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        int trueNode = InnerTrialDefinition.lsdTrueNodeIndex(island.centerX(), island.centerZ());
        List<BlockPos> nodes = InnerTrialDefinition.lsdNodes();
        for (int i = 0; i < nodes.size(); i++) {
            BlockPos offset = nodes.get(i);
            BlockPos top = surfaceOffset(level, center, offset.getX(), offset.getZ());
            structural(level, top, i == trueNode && completed
                    ? Blocks.SEA_LANTERN.defaultBlockState()
                    : Blocks.PRISMARINE.defaultBlockState(), true, count, mode);
            structural(level, top.above(), ModInnerDimensionBlocks.DREAM_RESIDUE_GEODE.get().defaultBlockState(), true, count, mode);
        }
    }

    private static void meth(
            ServerLevel level,
            BlockPos center,
            boolean completed,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        for (BlockPos offset : InnerTrialDefinition.methNodes()) {
            BlockPos top = surfaceOffset(level, center, offset.getX(), offset.getZ());
            structural(level, top, completed
                    ? Blocks.CALCITE.defaultBlockState()
                    : Blocks.MAGMA_BLOCK.defaultBlockState(), true, count, mode);
            structural(level, top.above(), ModInnerDimensionBlocks.OVERDRIVE_SLAG.get().defaultBlockState(), true, count, mode);
            structural(level, top.above(2), Blocks.LIGHTNING_ROD.defaultBlockState(), true, count, mode);
        }
    }

    private static void mushrooms(
            ServerLevel level,
            BlockPos center,
            boolean completed,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        for (BlockPos offset : InnerTrialDefinition.mushroomRoots()) {
            BlockPos top = surfaceOffset(level, center, offset.getX(), offset.getZ());
            structural(level, top, Blocks.MYCELIUM.defaultBlockState(), true, count, mode);
            structural(level, top.above(), completed
                    ? ModInnerDimensionBlocks.MYCELIAL_INSIGHT_NODE.get().defaultBlockState()
                    : ModInnerDimensionBlocks.MYCELIAL_ROOT.get().defaultBlockState(), true, count, mode);
        }
        structural(level, center.above(), completed
                ? Blocks.SHROOMLIGHT.defaultBlockState()
                : Blocks.MUSHROOM_STEM.defaultBlockState(), true, count, mode);
    }

    private static BlockPos surfaceOffset(ServerLevel level, BlockPos center, int dx, int dz) {
        return InnerPlacement.surfaceTop(level, center.getX() + dx, center.getZ() + dz);
    }

    private static void structural(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            boolean allowTerrainReplace,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        InnerPlacement.safeSetStructural(level, pos, state, allowTerrainReplace, count, mode);
    }
}
