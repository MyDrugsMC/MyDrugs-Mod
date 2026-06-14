package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.mydrugs.mydrugs.core.drug.DrugId;

public final class InnerLakeBuilder {
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

    public static void placeInitialLakeDetails(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        placeLakeDetails(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), (pos, state) -> {
            if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
                return;
            }
            if (!chunkPos.equals(new ChunkPos(pos))) {
                return;
            }
            chunk.setBlockState(pos, state, 2);
        });
    }

    static void placeOverlayLakeDetails(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeLakeDetails(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
                (pos, state) -> InnerPlacement.safeSet(level, pos, state, true, count));
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

    private static void placeLakeDetails(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        placeShoreBands(cache, minX, minZ, setter);
        if (!cache.anyLake()) {
            return;
        }
        int islandCenterX = InnerTerrain.slotCenter(minX + 8);
        int islandCenterZ = InnerTerrain.slotCenter(minZ + 8);
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        for (int localZ = 2; localZ < 16; localZ += 3) {
            for (int localX = 2; localX < 16; localX += 3) {
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                if (!sample.land() || !sample.lake()) {
                    continue;
                }
                long hash = InnerNoise.mix64(seed
                        ^ (long) worldX * 0x10E1_5A7EL
                        ^ (long) worldZ * 0x71AC_3B11L);
                if (sample.lakeIsland() && (hash & 255L) < 38L) {
                    buildLakeIsland(worldX, sample.lakeSurfaceY(), worldZ, sample, hash, setter);
                }
                if (sample.lakeCenterpiece() && ((hash >>> 8) & 511L) < 46L) {
                    buildLakeCenterpiece(worldX, sample.lakeSurfaceY() + 1, worldZ, sample.chooseFeatureDrug(hash), setter);
                }
            }
        }
    }

    /**
     * Three readable shore bands around water (Section 4.4): a continuous wet ground ring right
     * at the waterline, a reed ring behind it, and a sparse dry accent ring beyond — all pure
     * functions of the cached samples so the initial and overlay passes agree.
     */
    private static void placeShoreBands(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
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
                    setter.set(top, wetGroundFor(sample));
                } else if (shore > 0.34D) {
                    // Reed ring: dense but not solid, and kept out of protected sightlines.
                    if ((hash & 3L) != 0L && !cache.scene(localX, localZ).preserveOpenView()) {
                        setter.set(top.above(), sample.profile().flora().reed(hash >>> 8));
                    }
                } else if ((hash & 15L) == 0L) {
                    // Dry accent ring: rare flowers marking where the moisture gives out.
                    setter.set(top.above(), sample.profile().flora().flower(hash >>> 8));
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
            BlockSetter setter
    ) {
        BlockState shore = sample.transitionZone()
                ? InnerTransitionPalette.shoreAccent(sample)
                : sample.profile().surfaceBlock();
        for (int dz = -2; dz <= 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                if (dx * dx + dz * dz > 5) {
                    continue;
                }
                setter.set(new BlockPos(x + dx, y, z + dz), shore);
                if (Math.abs(dx) + Math.abs(dz) <= 2) {
                    setter.set(new BlockPos(x + dx, y + 1, z + dz), islandPlant(sample, hash + dx * 31L + dz * 17L));
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

    private static void buildLakeCenterpiece(int x, int y, int z, DrugId drugId, BlockSetter setter) {
        switch (drugId) {
            case ALCOHOL -> {
                column(x, y, z, 5, Blocks.DARK_OAK_LOG.defaultBlockState(), setter);
                setter.set(new BlockPos(x + 1, y, z), Blocks.MUD.defaultBlockState());
                setter.set(new BlockPos(x, y + 5, z), Blocks.ROOTED_DIRT.defaultBlockState());
            }
            case WEED -> {
                column(x, y, z, 4, Blocks.MOSS_BLOCK.defaultBlockState(), setter);
                setter.set(new BlockPos(x + 1, y + 1, z), Blocks.AZALEA_LEAVES.defaultBlockState());
                setter.set(new BlockPos(x - 1, y + 1, z), Blocks.AZALEA_LEAVES.defaultBlockState());
            }
            case MUSHROOMS -> {
                column(x, y, z, 5, Blocks.MUSHROOM_STEM.defaultBlockState(), setter);
                setter.set(new BlockPos(x, y + 5, z), Blocks.RED_MUSHROOM_BLOCK.defaultBlockState());
                setter.set(new BlockPos(x + 1, y + 5, z), Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState());
            }
            case HASH, LSD -> {
                column(x, y, z, 6, Blocks.PRISMARINE.defaultBlockState(), setter);
                setter.set(new BlockPos(x, y + 6, z), Blocks.SEA_LANTERN.defaultBlockState());
                setter.set(new BlockPos(x + 1, y + 4, z), Blocks.TINTED_GLASS.defaultBlockState());
            }
            case COCAINE -> {
                column(x, y, z, 5, Blocks.SMOOTH_QUARTZ.defaultBlockState(), setter);
                setter.set(new BlockPos(x, y + 5, z), Blocks.REDSTONE_BLOCK.defaultBlockState());
            }
            case METH -> {
                column(x, y, z, 5, Blocks.BASALT.defaultBlockState(), setter);
                setter.set(new BlockPos(x, y + 5, z), Blocks.MAGMA_BLOCK.defaultBlockState());
            }
            case TOBACCO -> {
                column(x, y, z, 4, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState(), setter);
                setter.set(new BlockPos(x + 1, y + 3, z), Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
            }
            default -> {
                column(x, y, z, 4, Blocks.OAK_LOG.defaultBlockState(), setter);
                setter.set(new BlockPos(x, y + 4, z), Blocks.LANTERN.defaultBlockState());
            }
        }
    }

    private static void column(int x, int y, int z, int height, BlockState state, BlockSetter setter) {
        for (int i = 0; i < height; i++) {
            setter.set(new BlockPos(x, y + i, z), state);
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

    private interface BlockSetter {
        void set(BlockPos pos, BlockState state);
    }
}
