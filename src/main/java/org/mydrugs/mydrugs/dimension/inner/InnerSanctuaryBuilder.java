package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

final class InnerSanctuaryBuilder {
    private InnerSanctuaryBuilder() {
    }

    static void placeCenterSanctuary(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeCenterSanctuary(level, island, count, InnerPlacement.PlacementMode.LIVE_OVERLAY);
    }

    static void placeCenterSanctuary(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        int integratedCount = island.integratedDrugCount();
        int completedCount = island.completedInnerTrialCount();
        // Skip the full sanctuary rebuild if it has already been placed at this integration count.
        String marker = InnerDimensionConstants.sanctuaryMarker(integratedCount, completedCount);
        if (island.hasMarker(marker)) {
            InnerSpiralCourtBuilder.place(level, InnerDimensionSavedData.get(level), island, count, mode);
            return;
        }
        int centerX = island.centerX();
        int centerZ = island.centerZ();
        int radius = 21 + Math.min(8, integratedCount);

        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > radius) {
                    continue;
                }
                BlockPos top = InnerPlacement.surfaceTop(level, centerX + dx, centerZ + dz);
                BlockState floor = sanctuaryFloor(dist, integratedCount);
                InnerPlacement.safeSetStructural(level, top, floor, true, count, mode);
                InnerPlacement.safeSetStructural(level, top.above(), Blocks.AIR.defaultBlockState(), true, count, mode);
                InnerPlacement.safeSetStructural(level, top.above(2), Blocks.AIR.defaultBlockState(), true, count, mode);
                if (dist > radius - 4 && ((dx * 31 + dz * 17) & 7) == 0) {
                    BlockState plant = integratedCount >= 3
                            ? ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState()
                            : ModInnerDimensionBlocks.BREATH_GRASS.get().defaultBlockState();
                    InnerPlacement.safeSetStructural(level, top.above(), plant, false, count, mode);
                }
            }
        }

        BlockPos centerTop = InnerPlacement.surfaceTop(level, centerX, centerZ);
        InnerPlacement.safeSetStructural(level, centerTop.above(2), Blocks.SEA_LANTERN.defaultBlockState(), true, count, mode);
        InnerPlacement.safeSetStructural(level, centerTop.above(3), ModInnerDimensionBlocks.LUCID_ECHO_NODE.get().defaultBlockState(), true, count, mode);
        InnerPlacement.clearSpawnColumn(level, centerTop.above(), count, mode);
        placeCompassMarkers(level, island, centerTop, count, mode);
        placeTrialSigils(level, island, centerTop, count, mode);
        placePathExits(level, island, count, mode);
        placeGentleLightRings(level, island, count, mode);
        placeSubtlePlantRings(level, island, count, mode);
        placeVistaOpenings(level, island, count, mode);
        updateCompletionCrown(level, centerTop, island.allInnerTrialsCompleted(), count, mode);

        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        data.markStructurePlaced(island.owner(), marker);
        InnerSpiralCourtBuilder.place(level, data, island, count, mode);
    }

    private static BlockState sanctuaryFloor(double distance, int integratedCount) {
        if (distance <= 5.0D) {
            return Blocks.SMOOTH_STONE.defaultBlockState();
        }
        if (distance <= 11.0D) {
            return integratedCount >= 4
                    ? Blocks.CALCITE.defaultBlockState()
                    : Blocks.STONE_BRICKS.defaultBlockState();
        }
        if (distance <= 17.0D) {
            return integratedCount >= 2
                    ? Blocks.MOSS_BLOCK.defaultBlockState()
                    : Blocks.DIRT_PATH.defaultBlockState();
        }
        return integratedCount >= 6
                ? Blocks.ROOTED_DIRT.defaultBlockState()
                : Blocks.GRASS_BLOCK.defaultBlockState();
    }

    private static void placeCompassMarkers(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            BlockPos centerTop,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        for (int i = 0; i < 8; i++) {
            double angle = i / 8.0D * Math.PI * 2.0D;
            int x = island.centerX() + (int) Math.round(Math.cos(angle) * 15.0D);
            int z = island.centerZ() + (int) Math.round(Math.sin(angle) * 15.0D);
            BlockPos top = InnerPlacement.surfaceTop(level, x, z);
            BlockState pillarState = i % 2 == 0
                    ? Blocks.SMOOTH_STONE.defaultBlockState()
                    : Blocks.CALCITE.defaultBlockState();
            for (int y = 0; y < 3; y++) {
                InnerPlacement.safeSetStructural(level, top.above(y), pillarState, true, count, mode);
            }
            InnerPlacement.safeSetStructural(level, top.above(3), Blocks.SEA_LANTERN.defaultBlockState(), true, count, mode);
        }

        int integratedCount = island.integratedDrugCount();
        if (integratedCount > 0) {
            int ring = Math.min(8, integratedCount);
            for (int i = 0; i < ring; i++) {
                double angle = i / (double) ring * Math.PI * 2.0D;
                BlockPos marker = centerTop.offset(
                        (int) Math.round(Math.cos(angle) * 7.0D),
                        1,
                        (int) Math.round(Math.sin(angle) * 7.0D)
                );
                InnerPlacement.safeSetStructural(level, marker, Blocks.LANTERN.defaultBlockState(), false, count, mode);
            }
        }
    }

    private static void placePathExits(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        for (DrugId drugId : InnerRegionMap.regionOrder()) {
            double angle = InnerRegionMap.angleFor(drugId);
            for (int radius = 18; radius <= 56; radius++) {
                int x = island.centerX() + (int) Math.round(Math.cos(angle) * radius);
                int z = island.centerZ() + (int) Math.round(Math.sin(angle) * radius);
                BlockPos top = InnerPlacement.surfaceTop(level, x, z);
                InnerPlacement.safeSetStructural(level, top, Blocks.DIRT_PATH.defaultBlockState(), true, count, mode);
                if (radius % 17 == 0) {
                    InnerPlacement.safeSetStructural(level, top.above(), Blocks.LANTERN.defaultBlockState(), false, count, mode);
                }
            }
        }
    }

    private static void placeTrialSigils(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            BlockPos centerTop,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        for (int i = 0; i < CuratedDrugChain.ORDER.size(); i++) {
            DrugId drug = CuratedDrugChain.ORDER.get(i);
            double angle = InnerRegionMap.angleFor(drug);
            int x = centerTop.getX() + (int) Math.round(Math.cos(angle) * 10.0D);
            int z = centerTop.getZ() + (int) Math.round(Math.sin(angle) * 10.0D);
            BlockPos marker = InnerPlacement.surfaceTop(level, x, z);
            BlockState state;
            if (island.hasCompletedInnerTrial(drug)) {
                state = InnerTerrainProfile.forDrug(drug).nodeState();
            } else if (island.hasIntegrated(drug)) {
                state = InnerTerrainProfile.forDrug(drug).accentBlock();
            } else {
                state = Blocks.DEEPSLATE_TILES.defaultBlockState();
            }
            InnerPlacement.safeSetStructural(level, marker, Blocks.SMOOTH_STONE.defaultBlockState(), true, count, mode);
            InnerPlacement.safeSetStructural(level, marker.above(), state, true, count, mode);
        }
    }

    private static void updateCompletionCrown(
            ServerLevel level,
            BlockPos centerTop,
            boolean completed,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        for (int y = 4; y <= 9; y++) {
            InnerPlacement.safeSetStructural(level, centerTop.above(y),
                    completed
                            ? (y == 9 ? Blocks.SEA_LANTERN.defaultBlockState()
                            : Blocks.TINTED_GLASS.defaultBlockState())
                            : Blocks.AIR.defaultBlockState(), true, count, mode);
        }
        for (int i = 0; i < 18; i++) {
            double angle = i / 18.0D * Math.PI * 2.0D;
            BlockPos ring = centerTop.offset(
                    (int) Math.round(Math.cos(angle) * 13.0D),
                    2,
                    (int) Math.round(Math.sin(angle) * 13.0D)
            );
            InnerPlacement.safeSetStructural(level, ring,
                    completed ? Blocks.SEA_LANTERN.defaultBlockState() : Blocks.AIR.defaultBlockState(),
                    true, count, mode);
        }
    }

    private static void placeGentleLightRings(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        int rings = island.integratedDrugCount() >= 4 ? 16 : 8;
        for (int i = 0; i < rings; i++) {
            double angle = i / (double) rings * Math.PI * 2.0D;
            double radius = i % 2 == 0 ? 9.0D : 13.0D;
            BlockPos top = InnerPlacement.surfaceTop(
                    level,
                    island.centerX() + (int) Math.round(Math.cos(angle) * radius),
                    island.centerZ() + (int) Math.round(Math.sin(angle) * radius)
            );
            InnerPlacement.safeSetStructural(level, top, Blocks.SEA_LANTERN.defaultBlockState(), true, count, mode);
        }
    }

    private static void placeSubtlePlantRings(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        int integratedCount = island.integratedDrugCount();
        int radius = 18 + Math.min(5, integratedCount);
        for (int i = 0; i < 32; i++) {
            if (i % 3 == 0) {
                continue;
            }
            double angle = i / 32.0D * Math.PI * 2.0D;
            BlockPos top = InnerPlacement.surfaceTop(
                    level,
                    island.centerX() + (int) Math.round(Math.cos(angle) * radius),
                    island.centerZ() + (int) Math.round(Math.sin(angle) * radius)
            );
            BlockState plant = integratedCount >= 3
                    ? ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState()
                    : ModInnerDimensionBlocks.BREATH_GRASS.get().defaultBlockState();
            InnerPlacement.safeSetStructural(level, top.above(), plant, false, count, mode);
        }
    }

    private static void placeVistaOpenings(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        for (DrugId drugId : InnerRegionMap.regionOrder()) {
            double angle = InnerRegionMap.angleFor(drugId);
            for (int radius = 24; radius <= 42; radius += 3) {
                int x = island.centerX() + (int) Math.round(Math.cos(angle) * radius);
                int z = island.centerZ() + (int) Math.round(Math.sin(angle) * radius);
                BlockPos top = InnerPlacement.surfaceTop(level, x, z);
                InnerPlacement.safeSetStructural(level, top, Blocks.DIRT_PATH.defaultBlockState(), true, count, mode);
                InnerPlacement.safeSetStructural(level, top.above(), Blocks.AIR.defaultBlockState(), true, count, mode);
                InnerPlacement.safeSetStructural(level, top.above(2), Blocks.AIR.defaultBlockState(), true, count, mode);
            }
        }
    }
}
