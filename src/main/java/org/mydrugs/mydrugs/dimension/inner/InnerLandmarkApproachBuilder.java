package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

final class InnerLandmarkApproachBuilder {
    private static final int APPROACH_RADIUS = 132;
    private static final int CLEAR_RADIUS = 26;

    private InnerLandmarkApproachBuilder() {
    }

    static void placeInitialLandmarkApproaches(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        placeLandmarkApproaches(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), (pos, state) -> {
            if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
                return;
            }
            if (!chunkPos.equals(new ChunkPos(pos))) {
                return;
            }
            chunk.setBlockState(pos, state, 2);
        });
    }

    static void placeOverlayLandmarkApproaches(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeLandmarkApproaches(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
                (pos, state) -> InnerPlacement.safeSet(level, pos, state, true, count));
    }

    static boolean hasApproachStyleFor(DrugId drugId) {
        return switch (drugId) {
            case COFFEE, TOBACCO, WEED, HASH, ALCOHOL, COCAINE, LSD, METH, MUSHROOMS -> true;
            default -> false;
        };
    }

    private static void placeLandmarkApproaches(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        int islandCenterX = InnerTerrain.slotCenter(minX + 8);
        int islandCenterZ = InnerTerrain.slotCenter(minZ + 8);
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        for (DrugId drugId : CuratedDrugChain.ORDER) {
            BlockPos landmark = InnerRegionMap.landmarkFor(islandCenterX, islandCenterZ, drugId);
            if (!chunkTouchesApproach(minX, minZ, landmark)) {
                continue;
            }
            placeDrugApproach(cache, minX, minZ, islandCenterX, islandCenterZ, landmark, drugId, seed, setter);
        }
    }

    private static boolean chunkTouchesApproach(int minX, int minZ, BlockPos landmark) {
        int chunkMidX = minX + 8;
        int chunkMidZ = minZ + 8;
        return Math.abs(chunkMidX - landmark.getX()) <= APPROACH_RADIUS + 16
                && Math.abs(chunkMidZ - landmark.getZ()) <= APPROACH_RADIUS + 16;
    }

    private static void placeDrugApproach(
            InnerChunkSampleCache cache,
            int minX,
            int minZ,
            int centerX,
            int centerZ,
            BlockPos landmark,
            DrugId drugId,
            long seed,
            BlockSetter setter
    ) {
        ApproachStyle style = style(drugId);
        int placed = 0;
        boolean thresholdPlaced = false;
        for (int localZ = 0; localZ < 16 && placed < 8; localZ += 3) {
            for (int localX = 0; localX < 16 && placed < 8; localX += 3) {
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                double distance = Math.hypot(worldX - landmark.getX(), worldZ - landmark.getZ());
                if (!canPlaceApproach(sample, distance, drugId)) {
                    continue;
                }
                double corridor = approachCorridor(centerX, centerZ, landmark, worldX, worldZ);
                if (corridor < 0.22D && distance > 58.0D) {
                    continue;
                }
                long hash = InnerNoise.mix64(seed
                        ^ (long) drugId.networkId() * 0x355B_7A5DL
                        ^ (long) worldX * 0x2A0B_37EFL
                        ^ (long) worldZ * 0x6A09_E667L);
                if (distance < CLEAR_RADIUS) {
                    clearEntry(worldX, sample.topY(), worldZ, setter);
                    continue;
                }
                if ((hash & 1023L) < (0.18D + corridor * 0.32D) * 1024.0D) {
                    decorateApproachPoint(worldX, worldZ, sample, style, hash, setter);
                    placed++;
                }
                if (!thresholdPlaced && distance > 68.0D && distance < 86.0D && (hash & 15L) == 0L) {
                    buildThreshold(worldX, worldZ, sample, style, centerX, centerZ, setter);
                    thresholdPlaced = true;
                }
            }
        }
    }

    private static boolean canPlaceApproach(InnerTerrain.Sample sample, double landmarkDistance, DrugId drugId) {
        return sample.land()
                && !sample.hole()
                && !sample.lake()
                && landmarkDistance <= APPROACH_RADIUS
                && sample.distanceFromCenter() > InnerDimensionConstants.CORE_RADIUS + 44.0D
                && (sample.drugId() == drugId || sample.primaryDrug() == drugId || sample.secondaryDrug() == drugId);
    }

    private static double approachCorridor(int centerX, int centerZ, BlockPos landmark, int worldX, int worldZ) {
        double ax = landmark.getX() - centerX;
        double az = landmark.getZ() - centerZ;
        double bx = worldX - centerX;
        double bz = worldZ - centerZ;
        double lengthSq = ax * ax + az * az;
        if (lengthSq <= 1.0D) {
            return 1.0D;
        }
        double projection = InnerNoise.clamp01((bx * ax + bz * az) / lengthSq);
        double cx = centerX + ax * projection;
        double cz = centerZ + az * projection;
        double distance = Math.hypot(worldX - cx, worldZ - cz);
        return InnerNoise.clamp01(1.0D - distance / 30.0D);
    }

    private static void decorateApproachPoint(
            int worldX,
            int worldZ,
            InnerTerrain.Sample sample,
            ApproachStyle style,
            long hash,
            BlockSetter setter
    ) {
        BlockPos top = new BlockPos(worldX, sample.topY(), worldZ);
        setter.set(top, ((hash >>> 4) & 1L) == 0L ? style.pathEdge() : sample.profile().pathBlock());
        if ((hash & 3L) != 0L) {
            setter.set(top.above(), style.plant());
        }
        if (((hash >>> 8) & 3L) == 0L) {
            if (style.glow().getBlock() == Blocks.LANTERN || style.glow().getBlock() == Blocks.SOUL_LANTERN) {
                setter.set(top, style.pathEdge());
                setter.set(top.above(), style.glow());
            } else {
                setter.set(top.east(), style.glow());
            }
        }
        if (((hash >>> 12) & 7L) == 0L) {
            setter.set(top.west().above(), style.threshold());
        }
    }

    private static void buildThreshold(
            int worldX,
            int worldZ,
            InnerTerrain.Sample sample,
            ApproachStyle style,
            int centerX,
            int centerZ,
            BlockSetter setter
    ) {
        double angle = Math.atan2(worldZ - centerZ, worldX - centerX);
        int px = (int) Math.round(-Math.sin(angle) * 4.0D);
        int pz = (int) Math.round(Math.cos(angle) * 4.0D);
        for (int side = -1; side <= 1; side += 2) {
            int x = worldX + px * side;
            int z = worldZ + pz * side;
            InnerTerrain.Sample edge = InnerTerrain.sample(centerX, centerZ, x, z);
            if (!edge.land() || edge.lake() || edge.hole()) {
                continue;
            }
            int baseY = edge.topY() + 1;
            for (int dy = 0; dy < 4; dy++) {
                setter.set(new BlockPos(x, baseY + dy, z), style.threshold());
            }
        }
        for (int i = -2; i <= 2; i++) {
            setter.set(new BlockPos(worldX + px * i / 2, sample.topY() + 5, worldZ + pz * i / 2), style.threshold());
        }
    }

    private static void clearEntry(int worldX, int topY, int worldZ, BlockSetter setter) {
        setter.set(new BlockPos(worldX, topY + 1, worldZ), Blocks.AIR.defaultBlockState());
        setter.set(new BlockPos(worldX, topY + 2, worldZ), Blocks.AIR.defaultBlockState());
    }

    private static ApproachStyle style(DrugId drugId) {
        return switch (drugId) {
            case COFFEE -> new ApproachStyle(
                    Blocks.SMOOTH_STONE.defaultBlockState(),
                    Blocks.LANTERN.defaultBlockState(),
                    ModInnerDimensionBlocks.BREATH_GRASS.get().defaultBlockState(),
                    Blocks.OAK_LOG.defaultBlockState()
            );
            case TOBACCO -> new ApproachStyle(
                    Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
                    Blocks.MAGMA_BLOCK.defaultBlockState(),
                    ModInnerDimensionBlocks.BITTER_SPROUT.get().defaultBlockState(),
                    Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState()
            );
            case WEED -> new ApproachStyle(
                    Blocks.MOSS_BLOCK.defaultBlockState(),
                    Blocks.SHROOMLIGHT.defaultBlockState(),
                    ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState(),
                    Blocks.ROOTED_DIRT.defaultBlockState()
            );
            case HASH -> new ApproachStyle(
                    Blocks.CALCITE.defaultBlockState(),
                    Blocks.AMETHYST_BLOCK.defaultBlockState(),
                    ModInnerDimensionBlocks.CRYSTAL_SHRUB.get().defaultBlockState(),
                    Blocks.CALCITE.defaultBlockState()
            );
            case ALCOHOL -> new ApproachStyle(
                    Blocks.MUD.defaultBlockState(),
                    Blocks.SEA_LANTERN.defaultBlockState(),
                    ModInnerDimensionBlocks.MEMORY_REEDS.get().defaultBlockState(),
                    Blocks.DARK_OAK_LOG.defaultBlockState()
            );
            case COCAINE -> new ApproachStyle(
                    Blocks.SMOOTH_QUARTZ.defaultBlockState(),
                    Blocks.REDSTONE_BLOCK.defaultBlockState(),
                    ModInnerDimensionBlocks.REDLINE_SPARK_BLOOM.get().defaultBlockState(),
                    Blocks.WHITE_CONCRETE.defaultBlockState()
            );
            case LSD -> new ApproachStyle(
                    Blocks.PRISMARINE.defaultBlockState(),
                    Blocks.SEA_LANTERN.defaultBlockState(),
                    ModInnerDimensionBlocks.PRISM_LOTUS.get().defaultBlockState(),
                    Blocks.TINTED_GLASS.defaultBlockState()
            );
            case METH -> new ApproachStyle(
                    Blocks.POLISHED_BLACKSTONE.defaultBlockState(),
                    Blocks.MAGMA_BLOCK.defaultBlockState(),
                    ModInnerDimensionBlocks.REDLINE_THORN.get().defaultBlockState(),
                    Blocks.BLACKSTONE.defaultBlockState()
            );
            case MUSHROOMS -> new ApproachStyle(
                    Blocks.MYCELIUM.defaultBlockState(),
                    Blocks.SHROOMLIGHT.defaultBlockState(),
                    ModInnerDimensionBlocks.MYCELIAL_THREADS.get().defaultBlockState(),
                    Blocks.MUSHROOM_STEM.defaultBlockState()
            );
            default -> new ApproachStyle(
                    Blocks.STONE_BRICKS.defaultBlockState(),
                    Blocks.GLOWSTONE.defaultBlockState(),
                    ModInnerDimensionBlocks.BREATH_GRASS.get().defaultBlockState(),
                    Blocks.OAK_LOG.defaultBlockState()
            );
        };
    }

    private record ApproachStyle(BlockState pathEdge, BlockState glow, BlockState plant, BlockState threshold) {
    }

    private interface BlockSetter {
        void set(BlockPos pos, BlockState state);
    }
}
