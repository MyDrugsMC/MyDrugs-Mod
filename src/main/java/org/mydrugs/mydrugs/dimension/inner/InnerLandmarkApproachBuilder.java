package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

final class InnerLandmarkApproachBuilder {
    private static final int APPROACH_RADIUS = 132;
    private static final int CLEAR_RADIUS = 26;
    // Three staged rings (P9): outer = sparse waypoints, middle = formal markers + ground
    // banding, inner = dense composed base around the shrine clearing.
    private static final int INNER_RING_RADIUS = 40;
    private static final int MIDDLE_RING_RADIUS = 86;
    private static final long ECHO_SALT = 0x4543_484FL;
    private static final int MARGIN = 4;

    private InnerLandmarkApproachBuilder() {
    }

    static boolean hasApproachStyleFor(DrugId drugId) {
        return switch (drugId) {
            case COFFEE, TOBACCO, WEED, HASH, ALCOHOL, COCAINE, LSD, METH, MUSHROOMS -> true;
            default -> false;
        };
    }

    static void place(InnerBlockSink sink, InnerChunkSampleCache cache, int minX, int minZ) {
        int islandCenterX = cache.islandCenterX();
        int islandCenterZ = cache.islandCenterZ();
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        for (DrugId drugId : CuratedDrugChain.ORDER) {
            BlockPos landmark = InnerRegionMap.landmarkFor(islandCenterX, islandCenterZ, drugId);
            placeEchoLandmark(cache, minX, minZ, islandCenterX, islandCenterZ, drugId, seed, sink);
            if (!chunkTouchesApproach(minX, minZ, landmark)) {
                continue;
            }
            placeDrugApproach(cache, minX, minZ, islandCenterX, islandCenterZ, landmark, drugId, seed, sink);
        }
    }

    /**
     * One small deterministic "echo" cairn per sector at mid-radius — a minor skyline beat that
     * rhymes with the real landmark without competing with it. The position is a pure function of
     * the slot seed, so initial generation and overlay refresh place it identically, and the
     * landmark itself never moves.
     */
    private static void placeEchoLandmark(
            InnerChunkSampleCache cache,
            int minX,
            int minZ,
            int centerX,
            int centerZ,
            DrugId drugId,
            long seed,
            InnerBlockSink sink
    ) {
        long echoHash = InnerNoise.mix64(seed + ECHO_SALT + (long) drugId.networkId() * 0x9E37_79B9L);
        double angle = InnerRegionMap.angleFor(drugId)
                + (((echoHash & 1L) == 0L ? 1.0D : -1.0D) * (0.14D + ((echoHash >>> 8) & 63L) / 63.0D * 0.10D));
        double radius = InnerRegionMap.landmarkRadiusFor(drugId) * (0.52D + ((echoHash >>> 16) & 63L) / 63.0D * 0.12D);
        int echoX = centerX + (int) Math.round(Math.cos(angle) * radius);
        int echoZ = centerZ + (int) Math.round(Math.sin(angle) * radius);
        if (echoX < minX - MARGIN || echoX >= minX + 16 + MARGIN
                || echoZ < minZ - MARGIN || echoZ >= minZ + 16 + MARGIN) {
            return;
        }
        InnerTerrain.Sample sample = cache.sampleAt(echoX, echoZ);
        if (!sample.land() || sample.lake() || sample.hole() || sample.pathStrength() > 0.30D) {
            return;
        }
        ApproachStyle style = style(drugId);
        int baseY = sample.topY() + 1;
        int height = 3 + (int) ((echoHash >>> 24) & 3L);
        for (int dy = 0; dy < height; dy++) {
            sink.setBlock(new BlockPos(echoX, baseY + dy, echoZ), style.threshold(), true);
        }
        sink.setBlock(new BlockPos(echoX, baseY + height, echoZ), style.glow(), true);
        sink.setBlock(new BlockPos(echoX + 1, baseY, echoZ), style.pathEdge(), true);
        sink.setBlock(new BlockPos(echoX - 1, baseY, echoZ), style.pathEdge(), true);
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
            InnerBlockSink sink
    ) {
        ApproachStyle style = style(drugId);
        int placed = 0;
        boolean thresholdPlaced = false;
        for (int worldZ = minZ - MARGIN; worldZ < minZ + 16 + MARGIN && placed < 8; worldZ++) {
            if (!InnerChunkSampleCache.chunkLocalCandidate(worldZ, 0, 16, 3)) {
                continue;
            }
            for (int worldX = minX - MARGIN; worldX < minX + 16 + MARGIN && placed < 8; worldX++) {
                if (!InnerChunkSampleCache.chunkLocalCandidate(worldX, 0, 16, 3)) {
                    continue;
                }
                InnerTerrain.Sample sample = cache.sampleAt(worldX, worldZ);
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
                    clearEntry(worldX, sample.topY(), worldZ, sink);
                    continue;
                }
                // Staged rings: density and formality rise as the shrine nears.
                if (distance < INNER_RING_RADIUS) {
                    // Inner ring: dense composed base — formal ground checker plus rich accents.
                    if (corridor > 0.30D && ((worldX + worldZ) & 1) == 0) {
                        sink.setBlock(new BlockPos(worldX, sample.topY(), worldZ), style.pathEdge(), true);
                    }
                    if ((hash & 1023L) < 0.45D * 1024.0D) {
                        decorateApproachPoint(worldX, worldZ, sample, style, hash, sink);
                        placed++;
                    }
                } else if (distance < MIDDLE_RING_RADIUS) {
                    // Middle ring: formal paired markers and concentric ground banding.
                    if ((int) Math.round(distance) % 14 < 2 && corridor > 0.22D) {
                        sink.setBlock(new BlockPos(worldX, sample.topY(), worldZ), style.pathEdge(), true);
                    }
                    if ((hash & 1023L) < (0.22D + corridor * 0.30D) * 1024.0D) {
                        decorateApproachPoint(worldX, worldZ, sample, style, hash, sink);
                        placed++;
                    }
                    if (!thresholdPlaced && distance > 68.0D && (hash & 15L) == 0L) {
                        buildThreshold(worldX, worldZ, sample, style, centerX, centerZ, sink);
                        thresholdPlaced = true;
                    }
                } else if ((hash & 1023L) < (0.12D + corridor * 0.26D) * 1024.0D) {
                    // Outer ring: sparse waypoints that keep the sightline open.
                    decorateApproachPoint(worldX, worldZ, sample, style, hash, sink);
                    placed++;
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
            InnerBlockSink sink
    ) {
        BlockPos top = new BlockPos(worldX, sample.topY(), worldZ);
        sink.setBlock(top, ((hash >>> 4) & 1L) == 0L ? style.pathEdge() : sample.profile().pathBlock(), true);
        if ((hash & 3L) != 0L) {
            sink.setBlock(top.above(), style.plant(), true);
        }
        if (((hash >>> 8) & 3L) == 0L) {
            if (style.glow().getBlock() == Blocks.LANTERN || style.glow().getBlock() == Blocks.SOUL_LANTERN) {
                sink.setBlock(top, style.pathEdge(), true);
                sink.setBlock(top.above(), style.glow(), true);
            } else {
                sink.setBlock(top.east(), style.glow(), true);
            }
        }
        if (((hash >>> 12) & 7L) == 0L) {
            sink.setBlock(top.west().above(), style.threshold(), true);
        }
    }

    private static void buildThreshold(
            int worldX,
            int worldZ,
            InnerTerrain.Sample sample,
            ApproachStyle style,
            int centerX,
            int centerZ,
            InnerBlockSink sink
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
                sink.setBlock(new BlockPos(x, baseY + dy, z), style.threshold(), true);
            }
        }
        for (int i = -2; i <= 2; i++) {
            sink.setBlock(new BlockPos(worldX + px * i / 2, sample.topY() + 5, worldZ + pz * i / 2), style.threshold(), true);
        }
    }

    private static void clearEntry(int worldX, int topY, int worldZ, InnerBlockSink sink) {
        sink.setBlock(new BlockPos(worldX, topY + 1, worldZ), Blocks.AIR.defaultBlockState(), true);
        sink.setBlock(new BlockPos(worldX, topY + 2, worldZ), Blocks.AIR.defaultBlockState(), true);
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
}
