package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;
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
        int integratedCount = island.integratedCount();
        // Skip the full sanctuary rebuild if it has already been placed at this integration count.
        String marker = InnerDimensionConstants.sanctuaryMarker(integratedCount);
        if (island.hasMarker(marker)) {
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
                InnerPlacement.safeSet(level, top, floor, true, count);
                InnerPlacement.safeSet(level, top.above(), Blocks.AIR.defaultBlockState(), true, count);
                InnerPlacement.safeSet(level, top.above(2), Blocks.AIR.defaultBlockState(), true, count);
                if (dist > radius - 4 && ((dx * 31 + dz * 17) & 7) == 0) {
                    BlockState plant = integratedCount >= 3
                            ? ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState()
                            : ModInnerDimensionBlocks.BREATH_GRASS.get().defaultBlockState();
                    InnerPlacement.safeSet(level, top.above(), plant, false, count);
                }
            }
        }

        BlockPos centerTop = InnerPlacement.surfaceTop(level, centerX, centerZ);
        InnerPlacement.safeSet(level, centerTop.above(2), Blocks.SEA_LANTERN.defaultBlockState(), true, count);
        InnerPlacement.safeSet(level, centerTop.above(3), ModInnerDimensionBlocks.LUCID_ECHO_NODE.get().defaultBlockState(), true, count);
        InnerPlacement.clearSpawnColumn(level, centerTop.above(), count);
        placeCompassMarkers(level, island, centerTop, count);
        placePathExits(level, island, count);
        placeGentleLightRings(level, island, count);
        placeSubtlePlantRings(level, island, count);
        placeVistaOpenings(level, island, count);

        InnerDimensionSavedData.get(level).markStructurePlaced(island.owner(), marker);
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
            InnerPlacement.MutablePlacementCount count
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
                InnerPlacement.safeSet(level, top.above(y), pillarState, true, count);
            }
            InnerPlacement.safeSet(level, top.above(3), Blocks.SEA_LANTERN.defaultBlockState(), true, count);
        }

        if (island.integratedCount() > 0) {
            int ring = Math.min(8, island.integratedCount());
            for (int i = 0; i < ring; i++) {
                double angle = i / (double) ring * Math.PI * 2.0D;
                BlockPos marker = centerTop.offset(
                        (int) Math.round(Math.cos(angle) * 7.0D),
                        1,
                        (int) Math.round(Math.sin(angle) * 7.0D)
                );
                InnerPlacement.safeSet(level, marker, Blocks.LANTERN.defaultBlockState(), false, count);
            }
        }
    }

    private static void placePathExits(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerPlacement.MutablePlacementCount count
    ) {
        for (DrugId drugId : InnerRegionMap.regionOrder()) {
            double angle = InnerRegionMap.angleFor(drugId);
            for (int radius = 18; radius <= 56; radius++) {
                int x = island.centerX() + (int) Math.round(Math.cos(angle) * radius);
                int z = island.centerZ() + (int) Math.round(Math.sin(angle) * radius);
                BlockPos top = InnerPlacement.surfaceTop(level, x, z);
                InnerPlacement.safeSet(level, top, Blocks.DIRT_PATH.defaultBlockState(), true, count);
                if (radius % 17 == 0) {
                    InnerPlacement.safeSet(level, top.above(), Blocks.LANTERN.defaultBlockState(), false, count);
                }
            }
        }
    }

    private static void placeGentleLightRings(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerPlacement.MutablePlacementCount count
    ) {
        int rings = island.integratedCount() >= 4 ? 16 : 8;
        for (int i = 0; i < rings; i++) {
            double angle = i / (double) rings * Math.PI * 2.0D;
            double radius = i % 2 == 0 ? 9.0D : 13.0D;
            BlockPos top = InnerPlacement.surfaceTop(
                    level,
                    island.centerX() + (int) Math.round(Math.cos(angle) * radius),
                    island.centerZ() + (int) Math.round(Math.sin(angle) * radius)
            );
            InnerPlacement.safeSet(level, top, Blocks.SEA_LANTERN.defaultBlockState(), true, count);
        }
    }

    private static void placeSubtlePlantRings(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerPlacement.MutablePlacementCount count
    ) {
        int radius = 18 + Math.min(5, island.integratedCount());
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
            BlockState plant = island.integratedCount() >= 3
                    ? ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState()
                    : ModInnerDimensionBlocks.BREATH_GRASS.get().defaultBlockState();
            InnerPlacement.safeSet(level, top.above(), plant, false, count);
        }
    }

    private static void placeVistaOpenings(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            InnerPlacement.MutablePlacementCount count
    ) {
        for (DrugId drugId : InnerRegionMap.regionOrder()) {
            double angle = InnerRegionMap.angleFor(drugId);
            for (int radius = 24; radius <= 42; radius += 3) {
                int x = island.centerX() + (int) Math.round(Math.cos(angle) * radius);
                int z = island.centerZ() + (int) Math.round(Math.sin(angle) * radius);
                BlockPos top = InnerPlacement.surfaceTop(level, x, z);
                InnerPlacement.safeSet(level, top, Blocks.DIRT_PATH.defaultBlockState(), true, count);
                InnerPlacement.safeSet(level, top.above(), Blocks.AIR.defaultBlockState(), true, count);
                InnerPlacement.safeSet(level, top.above(2), Blocks.AIR.defaultBlockState(), true, count);
            }
        }
    }
}
