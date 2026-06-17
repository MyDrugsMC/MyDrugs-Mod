package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;

public final class InnerSpiralCourtBuilder {
    public static final ResourceKey<LootTable> SPIRAL_COURT_REWARD = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "chests/inner_spiral_court")
    );

    private static final int COURT_RADIUS = 14;
    private static final int COURT_DEPTH = 10;

    private InnerSpiralCourtBuilder() {
    }

    static void place(
            ServerLevel level,
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            InnerPlacement.MutablePlacementCount count
    ) {
        place(level, data, island, count, InnerPlacement.PlacementMode.LIVE_OVERLAY);
    }

    static void place(
            ServerLevel level,
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        if (!island.allInnerTrialsCompleted()) {
            return;
        }

        BlockPos surface = InnerPlacement.surfaceTop(level, island.centerX(), island.centerZ());
        int floorY = surface.getY() - COURT_DEPTH;
        if (!data.isSpiralCourtPlaced(island.owner())) {
            buildCourt(level, island, surface, floorY, count, mode);
            if (data.markSpiralCourtPlaced(island.owner())) {
                InnerProgressionMilestones.spiralCourtOpened(level, island);
            }
        }
        if (!data.hasProgressMarker(island.owner(), InnerDimensionConstants.MARKER_SPIRAL_COMPLETED)
                && !data.isSpiralCourtRewardPlaced(island.owner())) {
            placeReward(level, data, island, floorY, count, mode);
        }
    }

    static boolean isRewardChest(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            BlockPos pos
    ) {
        BlockPos surface = InnerPlacement.surfaceTop(level, island.centerX(), island.centerZ());
        return pos.getX() == island.centerX()
                && pos.getY() == surface.getY() - COURT_DEPTH + 1
                && pos.getZ() == island.centerZ();
    }

    private static void buildCourt(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            BlockPos surface,
            int floorY,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        for (int dz = -COURT_RADIUS; dz <= COURT_RADIUS; dz++) {
            for (int dx = -COURT_RADIUS; dx <= COURT_RADIUS; dx++) {
                double distance = Math.hypot(dx, dz);
                if (distance > COURT_RADIUS + 0.4D) {
                    continue;
                }
                BlockPos floor = new BlockPos(island.centerX() + dx, floorY, island.centerZ() + dz);
                if (distance <= COURT_RADIUS - 1.0D) {
                    InnerPlacement.safeSetStructural(level, floor, courtFloor(dx, dz), true, count, mode);
                    for (int y = 1; y <= 6; y++) {
                        InnerPlacement.safeSetStructural(level, floor.above(y), Blocks.AIR.defaultBlockState(), true, count, mode);
                    }
                }
                if (distance >= COURT_RADIUS - 1.2D) {
                    for (int y = 1; y <= 4; y++) {
                        InnerPlacement.safeSetStructural(level, floor.above(y),
                                y == 4 ? Blocks.CALCITE.defaultBlockState()
                                        : Blocks.DEEPSLATE_TILES.defaultBlockState(), true, count, mode);
                    }
                }
            }
        }

        for (int i = 0; i < CuratedDrugChain.ORDER.size(); i++) {
            DrugId drug = CuratedDrugChain.ORDER.get(i);
            double angle = InnerRegionMap.angleFor(drug);
            int x = island.centerX() + (int) Math.round(Math.cos(angle) * 10.0D);
            int z = island.centerZ() + (int) Math.round(Math.sin(angle) * 10.0D);
            BlockPos alcove = new BlockPos(x, floorY + 1, z);
            for (int y = 0; y < 3; y++) {
                InnerPlacement.safeSetStructural(level, alcove.above(y),
                        y == 2 ? InnerTerrainProfile.forDrug(drug).nodeState()
                                : Blocks.SMOOTH_STONE.defaultBlockState(), true, count, mode);
            }
        }

        for (int step = 0; step <= 56; step++) {
            double progress = step / 56.0D;
            double angle = progress * Math.PI * 4.0D;
            double radius = 3.0D + progress * 6.0D;
            int x = island.centerX() + (int) Math.round(Math.cos(angle) * radius);
            int z = island.centerZ() + (int) Math.round(Math.sin(angle) * radius);
            int y = surface.getY() - (int) Math.round(progress * (COURT_DEPTH - 1));
            BlockPos stair = new BlockPos(x, y, z);
            InnerPlacement.safeSetStructural(level, stair, Blocks.SMOOTH_STONE.defaultBlockState(), true, count, mode);
            InnerPlacement.safeSetStructural(level, stair.above(), Blocks.AIR.defaultBlockState(), true, count, mode);
            InnerPlacement.safeSetStructural(level, stair.above(2), Blocks.AIR.defaultBlockState(), true, count, mode);
        }
    }

    private static void placeReward(
            ServerLevel level,
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            int floorY,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        BlockPos chestPos = new BlockPos(island.centerX(), floorY + 1, island.centerZ());
        InnerPlacement.safeSetStructural(level, chestPos.below(), Blocks.SEA_LANTERN.defaultBlockState(), true, count, mode);
        if (!InnerPlacement.safeSetStructural(level, chestPos, Blocks.CHEST.defaultBlockState(), true, count, mode)) {
            return;
        }
        RandomizableContainer.setBlockEntityLootTable(level, level.getRandom(), chestPos, SPIRAL_COURT_REWARD);
        data.markSpiralCourtRewardPlaced(island.owner());
    }

    private static net.minecraft.world.level.block.state.BlockState courtFloor(int dx, int dz) {
        int band = Math.floorMod((int) Math.round(Math.hypot(dx, dz)), 3);
        return switch (band) {
            case 0 -> Blocks.SMOOTH_STONE.defaultBlockState();
            case 1 -> Blocks.CALCITE.defaultBlockState();
            default -> Blocks.DEEPSLATE_TILES.defaultBlockState();
        };
    }
}
