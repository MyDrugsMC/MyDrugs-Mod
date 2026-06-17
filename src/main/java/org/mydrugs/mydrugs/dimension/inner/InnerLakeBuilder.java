package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.mydrugs.mydrugs.core.drug.DrugId;

public final class InnerLakeBuilder {
    private static final int MARGIN = 2;

    private InnerLakeBuilder() {
    }

    public static void fillLakeColumn(
            ChunkAccess chunk,
            BlockPos.MutableBlockPos pos,
            InnerTerrain.Sample sample,
            int worldX,
            int worldZ
    ) {
        if (!sample.land() || !sample.lake()) {
            return;
        }
        setChunkBlock(chunk, pos, worldX, sample.topY(), worldZ, bedState(sample, worldX, worldZ));
        for (int y = sample.topY() + 1; y <= sample.lakeSurfaceY(); y++) {
            BlockState fill = fillState(sample, worldX, y, worldZ);
            if (fill != null) {
                setChunkBlock(chunk, pos, worldX, y, worldZ, fill);
            }
        }
    }

    public static BlockState stateForLakeColumn(InnerTerrain.Sample sample, int worldX, int y, int worldZ) {
        if (!sample.land() || !sample.lake()) {
            return null;
        }
        if (y == sample.topY()) {
            return bedState(sample, worldX, worldZ);
        }
        if (y > sample.topY() && y <= sample.lakeSurfaceY()) {
            return fillState(sample, worldX, y, worldZ);
        }
        return null;
    }

    static void fillLakeColumn(
            ServerLevel level,
            BlockPos.MutableBlockPos pos,
            InnerTerrain.Sample sample,
            int worldX,
            int worldZ,
            InnerPlacement.MutablePlacementCount count
    ) {
        if (!sample.land() || !sample.lake()) {
            return;
        }
        setLevelBlock(level, pos, worldX, sample.topY(), worldZ, bedState(sample, worldX, worldZ), count);
        for (int y = sample.topY() + 1; y <= sample.lakeSurfaceY(); y++) {
            BlockState fill = fillState(sample, worldX, y, worldZ);
            if (fill != null) {
                setLevelBlock(level, pos, worldX, y, worldZ, fill, count);
            }
        }
    }

    private static BlockState bedState(InnerTerrain.Sample sample, int worldX, int worldZ) {
        if (sample.lakeCoreStrength() > 0.72D) {
            return switch (sample.lakeType()) {
                case WATER, MEMORY -> sample.drugId() == DrugId.MUSHROOMS
                        ? Blocks.MYCELIUM.defaultBlockState()
                        : Blocks.CLAY.defaultBlockState();
                case PRISM -> Blocks.SEA_LANTERN.defaultBlockState();
                case MAGMA -> Blocks.MAGMA_BLOCK.defaultBlockState();
                case SCULK_VOID -> Blocks.SCULK.defaultBlockState();
                case MUD, NONE -> Blocks.MUD.defaultBlockState();
            };
        }
        return switch (sample.lakeType()) {
            case WATER -> ((InnerNoise.mix64(worldX * 41L + worldZ * 89L) & 15L) == 0L)
                    // Occasional pale clay patches so plain water beds read as sediment, not paint.
                    ? Blocks.CLAY.defaultBlockState()
                    : switch (sample.drugId()) {
                        case WEED -> Blocks.MOSS_BLOCK.defaultBlockState();
                        case MUSHROOMS -> Blocks.MYCELIUM.defaultBlockState();
                        default -> Blocks.MUD.defaultBlockState();
                    };
            case MUD, MEMORY -> ((InnerNoise.mix64(worldX * 61L + worldZ * 113L) & 15L) == 0L)
                    ? Blocks.ROOTED_DIRT.defaultBlockState()
                    : Blocks.MUD.defaultBlockState();
            case PRISM -> ((InnerNoise.mix64(worldX * 37L + worldZ * 71L) & 7L) == 0L)
                    ? Blocks.SEA_LANTERN.defaultBlockState()
                    : Blocks.PRISMARINE.defaultBlockState();
            case MAGMA -> ((InnerNoise.mix64(worldX * 53L + worldZ * 97L) & 3L) == 0L)
                    ? Blocks.MAGMA_BLOCK.defaultBlockState()
                    : Blocks.BASALT.defaultBlockState();
            case SCULK_VOID -> Blocks.SCULK.defaultBlockState();
            case NONE -> sample.profile().surfaceBlock();
        };
    }

    static void place(InnerBlockSink sink, InnerChunkSampleCache cache, int minX, int minZ) {
        placeShoreBands(cache, minX, minZ, sink);
        int islandCenterX = cache.islandCenterX();
        int islandCenterZ = cache.islandCenterZ();
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        for (int worldZ = minZ - MARGIN; worldZ < minZ + 16 + MARGIN; worldZ++) {
            if (!InnerChunkSampleCache.chunkLocalCandidate(worldZ, 2, 16, 3)) {
                continue;
            }
            for (int worldX = minX - MARGIN; worldX < minX + 16 + MARGIN; worldX++) {
                if (!InnerChunkSampleCache.chunkLocalCandidate(worldX, 2, 16, 3)) {
                    continue;
                }
                InnerTerrain.Sample sample = cache.sampleAt(worldX, worldZ);
                if (!sample.land() || !sample.lake()) {
                    continue;
                }
                long hash = InnerNoise.mix64(seed
                        ^ (long) worldX * 0x10E1_5A7EL
                        ^ (long) worldZ * 0x71AC_3B11L);
                if (sample.lakeIsland() && (hash & 255L) < 38L) {
                    buildLakeIsland(worldX, sample.lakeSurfaceY(), worldZ, sample, hash, sink);
                }
                if (sample.lakeCenterpiece() && ((hash >>> 8) & 511L) < 46L) {
                    buildLakeCenterpiece(worldX, sample.lakeSurfaceY() + 1, worldZ, sample.chooseFeatureDrug(hash), sink);
                }
            }
        }
    }

    /**
     * Three readable shore bands around water (Section 4.4): a continuous wet ground ring right
     * at the waterline, a reed ring behind it, and a sparse dry accent ring beyond — all pure
     * functions of the cached samples so the initial and overlay passes agree.
     */
    private static void placeShoreBands(InnerChunkSampleCache cache, int minX, int minZ, InnerBlockSink sink) {
        if (!cache.anyShoreOrWetland()) {
            return;
        }
        long seed = cache.seed();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                if (!sample.land() || sample.lake() || sample.hole() || sample.pathStrength() > 0.50D) {
                    continue;
                }
                double shore = sample.shoreStrength();
                if (shore <= 0.16D) {
                    continue;
                }
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                long hash = InnerNoise.mix64(seed
                        ^ (long) worldX * 0x53C5_BF2DL
                        ^ (long) worldZ * 0x2BD6_AA63L);
                BlockPos top = new BlockPos(worldX, sample.topY(), worldZ);
                if (shore > 0.55D) {
                    // Wet ring: continuous damp ground at the waterline.
                    sink.setBlock(top, wetGroundFor(sample), true);
                } else if (shore > 0.34D) {
                    // Reed ring: dense but not solid, and kept out of protected sightlines.
                    if ((hash & 3L) != 0L && !cache.scene(localX, localZ).preserveOpenView()) {
                        sink.setBlock(top.above(), sample.profile().flora().reed(hash >>> 8), true);
                    }
                } else if ((hash & 15L) == 0L) {
                    // Dry accent ring: rare flowers marking where the moisture gives out.
                    sink.setBlock(top.above(), sample.profile().flora().flower(hash >>> 8), true);
                }
            }
        }
    }

    private static BlockState wetGroundFor(InnerTerrain.Sample sample) {
        return switch (sample.drugId()) {
            case WEED -> Blocks.MOSS_BLOCK.defaultBlockState();
            case MUSHROOMS -> Blocks.MYCELIUM.defaultBlockState();
            case HASH, LSD -> Blocks.CLAY.defaultBlockState();
            default -> Blocks.MUD.defaultBlockState();
        };
    }

    private static void buildLakeIsland(
            int x,
            int y,
            int z,
            InnerTerrain.Sample sample,
            long hash,
            InnerBlockSink sink
    ) {
        BlockState shore = sample.transitionZone()
                ? InnerTransitionPalette.shoreAccent(sample)
                : sample.profile().surfaceBlock();
        for (int dz = -2; dz <= 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                if (dx * dx + dz * dz > 5) {
                    continue;
                }
                sink.setBlock(new BlockPos(x + dx, y, z + dz), shore, true);
                if (Math.abs(dx) + Math.abs(dz) <= 2) {
                    sink.setBlock(new BlockPos(x + dx, y + 1, z + dz), islandPlant(sample, hash + dx * 31L + dz * 17L), true);
                }
            }
        }
    }

    private static BlockState islandPlant(InnerTerrain.Sample sample, long hash) {
        if (sample.wetlandStrength() > 0.3D) {
            return sample.profile().flora().reed(hash);
        }
        return sample.profile().flora().flower(hash);
    }

    private static void buildLakeCenterpiece(int x, int y, int z, DrugId drugId, InnerBlockSink sink) {
        switch (drugId) {
            case ALCOHOL -> {
                column(x, y, z, 5, Blocks.DARK_OAK_LOG.defaultBlockState(), sink);
                sink.setBlock(new BlockPos(x + 1, y, z), Blocks.MUD.defaultBlockState(), true);
                sink.setBlock(new BlockPos(x, y + 5, z), Blocks.ROOTED_DIRT.defaultBlockState(), true);
            }
            case WEED -> {
                column(x, y, z, 4, Blocks.MOSS_BLOCK.defaultBlockState(), sink);
                sink.setBlock(new BlockPos(x + 1, y + 1, z), Blocks.AZALEA_LEAVES.defaultBlockState(), true);
                sink.setBlock(new BlockPos(x - 1, y + 1, z), Blocks.AZALEA_LEAVES.defaultBlockState(), true);
            }
            case MUSHROOMS -> {
                column(x, y, z, 5, Blocks.MUSHROOM_STEM.defaultBlockState(), sink);
                sink.setBlock(new BlockPos(x, y + 5, z), Blocks.RED_MUSHROOM_BLOCK.defaultBlockState(), true);
                sink.setBlock(new BlockPos(x + 1, y + 5, z), Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState(), true);
            }
            case HASH, LSD -> {
                column(x, y, z, 6, Blocks.PRISMARINE.defaultBlockState(), sink);
                sink.setBlock(new BlockPos(x, y + 6, z), Blocks.SEA_LANTERN.defaultBlockState(), true);
                sink.setBlock(new BlockPos(x + 1, y + 4, z), Blocks.TINTED_GLASS.defaultBlockState(), true);
            }
            case COCAINE -> {
                column(x, y, z, 5, Blocks.SMOOTH_QUARTZ.defaultBlockState(), sink);
                sink.setBlock(new BlockPos(x, y + 5, z), Blocks.REDSTONE_BLOCK.defaultBlockState(), true);
            }
            case METH -> {
                column(x, y, z, 5, Blocks.BASALT.defaultBlockState(), sink);
                sink.setBlock(new BlockPos(x, y + 5, z), Blocks.MAGMA_BLOCK.defaultBlockState(), true);
            }
            case TOBACCO -> {
                column(x, y, z, 4, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), sink);
                sink.setBlock(new BlockPos(x + 1, y + 3, z), Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), true);
            }
            default -> {
                column(x, y, z, 4, Blocks.OAK_LOG.defaultBlockState(), sink);
                sink.setBlock(new BlockPos(x, y + 4, z), Blocks.LANTERN.defaultBlockState(), true);
            }
        }
    }

    private static void column(int x, int y, int z, int height, BlockState state, InnerBlockSink sink) {
        for (int i = 0; i < height; i++) {
            sink.setBlock(new BlockPos(x, y + i, z), state, true);
        }
    }

    private static BlockState fillState(InnerTerrain.Sample sample, int worldX, int y, int worldZ) {
        return switch (sample.lakeType()) {
            case WATER, MEMORY -> Blocks.WATER.defaultBlockState();
            case PRISM -> y == sample.lakeSurfaceY()
                    ? Blocks.TINTED_GLASS.defaultBlockState()
                    : Blocks.PRISMARINE.defaultBlockState();
            case MAGMA -> y == sample.lakeSurfaceY()
                    ? Blocks.MAGMA_BLOCK.defaultBlockState()
                    : null;
            case SCULK_VOID -> y == sample.lakeSurfaceY()
                    ? Blocks.SCULK.defaultBlockState()
                    : null;
            case MUD, NONE -> null;
        };
    }

    private static void setChunkBlock(
            ChunkAccess chunk,
            BlockPos.MutableBlockPos pos,
            int x,
            int y,
            int z,
            BlockState state
    ) {
        if (y < chunk.getMinY() || y >= chunk.getMinY() + chunk.getHeight()) {
            return;
        }
        chunk.setBlockState(pos.set(x, y, z), state, 2);
    }

    private static void setLevelBlock(
            ServerLevel level,
            BlockPos.MutableBlockPos pos,
            int x,
            int y,
            int z,
            BlockState state,
            InnerPlacement.MutablePlacementCount count
    ) {
        if (y < level.getMinY() || y >= level.getMaxY()) {
            count.recordSkipped();
            return;
        }
        pos.set(x, y, z);
        if (level.getBlockState(pos).equals(state)) {
            return;
        }
        level.setBlock(pos, state, InnerDimensionConstants.RECREATE_UPDATE_FLAGS);
        count.recordPlaced();
    }
}
