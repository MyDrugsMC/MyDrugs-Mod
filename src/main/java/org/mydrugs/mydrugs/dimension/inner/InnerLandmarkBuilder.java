package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

public final class InnerLandmarkBuilder {
    private InnerLandmarkBuilder() {
    }

    public static void placeLandmark(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            DrugId drugId,
            boolean unlocked,
            InnerPlacement.MutablePlacementCount count
    ) {
        BlockPos anchor = InnerRegionMap.landmarkFor(island.centerX(), island.centerZ(), drugId);
        BlockPos surface = InnerPlacement.surfaceTop(level, anchor.getX(), anchor.getZ());
        if (unlocked) {
            placeUnlockedLandmark(level, island, drugId, surface, count);
            InnerDimensionSavedData.get(level).markStructurePlaced(
                    island.owner(),
                    InnerDimensionConstants.landmarkMarker(drugId)
            );
        } else {
            placeLockedLandmark(level, drugId, surface, count);
        }
    }

    static void placeLockedLandmark(
            ServerLevel level,
            DrugId drugId,
            BlockPos surface,
            InnerPlacement.MutablePlacementCount count
    ) {
        InnerTerrainProfile profile = InnerTerrainProfile.forDrug(drugId);
        for (int dz = -8; dz <= 8; dz++) {
            for (int dx = -8; dx <= 8; dx++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 8.5D) {
                    continue;
                }
                BlockPos top = InnerPlacement.surfaceTop(level, surface.getX() + dx, surface.getZ() + dz);
                BlockState floor = dist < 5.0D
                        ? Blocks.DEEPSLATE_TILES.defaultBlockState()
                        : Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState();
                InnerPlacement.safeSet(level, top, floor, true, count);
                if (dist > 6.8D && ((dx + dz) & 1) == 0) {
                    InnerPlacement.safeSet(level, top.above(), Blocks.IRON_BARS.defaultBlockState(), true, count);
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            double angle = i / 6.0D * Math.PI * 2.0D;
            BlockPos top = InnerPlacement.surfaceTop(
                    level,
                    surface.getX() + (int) Math.round(Math.cos(angle) * 6.0D),
                    surface.getZ() + (int) Math.round(Math.sin(angle) * 6.0D)
            );
            for (int y = 0; y < 4; y++) {
                InnerPlacement.safeSet(level, top.above(y), Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState(), true, count);
            }
        }
        InnerPlacement.safeSet(level, surface.above(2), profile.accentBlock(), true, count);
    }

    static void placeUnlockedLandmark(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            DrugId drugId,
            BlockPos surface,
            InnerPlacement.MutablePlacementCount count
    ) {
        InnerTerrainProfile profile = InnerTerrainProfile.forDrug(drugId);
        int radius = switch (drugId) {
            case LSD, MUSHROOMS -> 20;
            case METH, COCAINE -> 18;
            default -> 16;
        };
        placeStandingPlace(level, surface, profile, radius, count);
        switch (drugId) {
            case COFFEE -> coffeeCircle(level, surface, count);
            case TOBACCO -> tobaccoChapel(level, surface, count);
            case WEED -> weedGarden(level, surface, count);
            case HASH -> hashShrine(level, surface, count);
            case ALCOHOL -> alcoholBasin(level, surface, count);
            case COCAINE -> cocaineBridge(level, surface, count);
            case LSD -> lsdSpiral(level, surface, count);
            case METH -> methPylon(level, surface, count);
            case MUSHROOMS -> mushroomCathedral(level, surface, count);
            default -> {
            }
        }
        InnerPlacement.safeSet(level, surface.above(2), profile.nodeState(), true, count);
    }

    private static void placeStandingPlace(
            ServerLevel level,
            BlockPos center,
            InnerTerrainProfile profile,
            int radius,
            InnerPlacement.MutablePlacementCount count
    ) {
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > radius) {
                    continue;
                }
                BlockPos top = InnerPlacement.surfaceTop(level, center.getX() + dx, center.getZ() + dz);
                BlockState floor = dist < 7.0D ? profile.surfaceBlock() : profile.pathBlock();
                InnerPlacement.safeSet(level, top, floor, true, count);
                if (dist < 10.0D) {
                    InnerPlacement.safeSet(level, top.above(), Blocks.AIR.defaultBlockState(), true, count);
                    InnerPlacement.safeSet(level, top.above(2), Blocks.AIR.defaultBlockState(), true, count);
                }
            }
        }
    }

    private static void coffeeCircle(ServerLevel level, BlockPos center, InnerPlacement.MutablePlacementCount count) {
        for (int i = 0; i < 12; i++) {
            double angle = i / 12.0D * Math.PI * 2.0D;
            BlockPos top = InnerPlacement.surfaceTop(
                    level,
                    center.getX() + (int) Math.round(Math.cos(angle) * 10.0D),
                    center.getZ() + (int) Math.round(Math.sin(angle) * 10.0D)
            );
            InnerPlacement.safeSet(level, top.above(), i % 3 == 0
                    ? Blocks.LANTERN.defaultBlockState()
                    : Blocks.BOOKSHELF.defaultBlockState(), true, count);
        }
    }

    private static void tobaccoChapel(ServerLevel level, BlockPos center, InnerPlacement.MutablePlacementCount count) {
        for (int i = 0; i < 8; i++) {
            double angle = i / 8.0D * Math.PI * 2.0D;
            BlockPos top = InnerPlacement.surfaceTop(
                    level,
                    center.getX() + (int) Math.round(Math.cos(angle) * 11.0D),
                    center.getZ() + (int) Math.round(Math.sin(angle) * 11.0D)
            );
            for (int y = 0; y < 6; y++) {
                InnerPlacement.safeSet(level, top.above(y), y == 5
                        ? Blocks.TUFF.defaultBlockState()
                        : Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), true, count);
            }
        }
    }

    private static void weedGarden(ServerLevel level, BlockPos center, InnerPlacement.MutablePlacementCount count) {
        for (int dz = -12; dz <= 12; dz += 2) {
            for (int dx = -12; dx <= 12; dx += 2) {
                if (dx * dx + dz * dz > 145 || ((dx * 19 + dz * 31) & 3) != 0) {
                    continue;
                }
                BlockPos top = InnerPlacement.surfaceTop(level, center.getX() + dx, center.getZ() + dz);
                InnerPlacement.safeSet(level, top.above(), ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState(), false, count);
            }
        }
    }

    private static void hashShrine(ServerLevel level, BlockPos center, InnerPlacement.MutablePlacementCount count) {
        for (int i = 0; i < 10; i++) {
            double angle = i / 10.0D * Math.PI * 2.0D;
            for (int r = 8; r <= 14; r += 3) {
                BlockPos top = InnerPlacement.surfaceTop(
                        level,
                        center.getX() + (int) Math.round(Math.cos(angle) * r),
                        center.getZ() + (int) Math.round(Math.sin(angle) * r)
                );
                for (int y = 0; y < 3 + (i % 3); y++) {
                    InnerPlacement.safeSet(level, top.above(y), i % 2 == 0
                            ? Blocks.CALCITE.defaultBlockState()
                            : Blocks.AMETHYST_BLOCK.defaultBlockState(), true, count);
                }
            }
        }
    }

    private static void alcoholBasin(ServerLevel level, BlockPos center, InnerPlacement.MutablePlacementCount count) {
        for (int dz = -11; dz <= 11; dz++) {
            for (int dx = -11; dx <= 11; dx++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 11.0D || dist < 5.0D) {
                    continue;
                }
                BlockPos top = InnerPlacement.surfaceTop(level, center.getX() + dx, center.getZ() + dz);
                InnerPlacement.safeSet(level, top, dist < 8.0D
                        ? Blocks.MUD.defaultBlockState()
                        : Blocks.DEEPSLATE_TILES.defaultBlockState(), true, count);
                if (((dx * 13 + dz * 7) & 5) == 0) {
                    InnerPlacement.safeSet(level, top.above(), ModInnerDimensionBlocks.MEMORY_REEDS.get().defaultBlockState(), false, count);
                }
            }
        }
    }

    private static void cocaineBridge(ServerLevel level, BlockPos center, InnerPlacement.MutablePlacementCount count) {
        for (int i = -17; i <= 17; i++) {
            BlockPos top = InnerPlacement.surfaceTop(level, center.getX() + i, center.getZ());
            InnerPlacement.safeSet(level, top, Blocks.SMOOTH_QUARTZ.defaultBlockState(), true, count);
            if (Math.abs(i) % 5 == 0) {
                InnerPlacement.safeSet(level, top.above(), Blocks.REDSTONE_BLOCK.defaultBlockState(), true, count);
            }
            if (Math.abs(i) % 7 == 0) {
                InnerPlacement.safeSet(level, top.north().above(), ModInnerDimensionBlocks.REDLINE_THORN.get().defaultBlockState(), false, count);
                InnerPlacement.safeSet(level, top.south().above(), ModInnerDimensionBlocks.REDLINE_THORN.get().defaultBlockState(), false, count);
            }
        }
    }

    private static void lsdSpiral(ServerLevel level, BlockPos center, InnerPlacement.MutablePlacementCount count) {
        for (int i = 0; i < 52; i++) {
            double t = i / 8.0D;
            double radius = 3.0D + i * 0.28D;
            BlockPos top = InnerPlacement.surfaceTop(
                    level,
                    center.getX() + (int) Math.round(Math.cos(t) * radius),
                    center.getZ() + (int) Math.round(Math.sin(t) * radius)
            ).above(i % 5);
            InnerPlacement.safeSet(level, top, i % 4 == 0
                    ? Blocks.SEA_LANTERN.defaultBlockState()
                    : Blocks.PRISMARINE.defaultBlockState(), true, count);
            if (i % 6 == 0) {
                InnerPlacement.safeSet(level, top.above(), Blocks.TINTED_GLASS.defaultBlockState(), true, count);
            }
        }
    }

    private static void methPylon(ServerLevel level, BlockPos center, InnerPlacement.MutablePlacementCount count) {
        for (int y = 0; y < 13; y++) {
            InnerPlacement.safeSet(level, center.above(y), y % 4 == 0
                    ? Blocks.MAGMA_BLOCK.defaultBlockState()
                    : Blocks.POLISHED_BLACKSTONE.defaultBlockState(), true, count);
        }
        for (int i = 0; i < 6; i++) {
            double angle = i / 6.0D * Math.PI * 2.0D;
            BlockPos top = InnerPlacement.surfaceTop(
                    level,
                    center.getX() + (int) Math.round(Math.cos(angle) * 9.0D),
                    center.getZ() + (int) Math.round(Math.sin(angle) * 9.0D)
            );
            InnerPlacement.safeSet(level, top.above(), Blocks.MAGMA_BLOCK.defaultBlockState(), true, count);
        }
    }

    private static void mushroomCathedral(ServerLevel level, BlockPos center, InnerPlacement.MutablePlacementCount count) {
        for (int i = 0; i < 9; i++) {
            double angle = i / 9.0D * Math.PI * 2.0D;
            BlockPos top = InnerPlacement.surfaceTop(
                    level,
                    center.getX() + (int) Math.round(Math.cos(angle) * 12.0D),
                    center.getZ() + (int) Math.round(Math.sin(angle) * 12.0D)
            );
            for (int y = 0; y < 7; y++) {
                InnerPlacement.safeSet(level, top.above(y), Blocks.MUSHROOM_STEM.defaultBlockState(), true, count);
            }
            InnerPlacement.safeSet(level, top.above(7), i % 2 == 0
                    ? Blocks.RED_MUSHROOM_BLOCK.defaultBlockState()
                    : Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState(), true, count);
        }
        for (int dz = -14; dz <= 14; dz += 3) {
            for (int dx = -14; dx <= 14; dx += 3) {
                if (dx * dx + dz * dz > 210 || ((dx * 7 + dz * 11) & 3) != 0) {
                    continue;
                }
                BlockPos top = InnerPlacement.surfaceTop(level, center.getX() + dx, center.getZ() + dz);
                InnerPlacement.safeSet(level, top.above(), ModInnerDimensionBlocks.MYCELIAL_ROOT.get().defaultBlockState(), false, count);
            }
        }
    }
}
