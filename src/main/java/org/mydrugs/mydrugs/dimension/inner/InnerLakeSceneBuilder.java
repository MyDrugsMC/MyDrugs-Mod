package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

final class InnerLakeSceneBuilder {
    private static final int MARGIN = 5;

    private InnerLakeSceneBuilder() {
    }

    static boolean rejectsSanctuaryForTest() {
        long seed = InnerTerrain.seedForSlot(0, 0);
        InnerTerrain.Sample terrain = InnerTerrain.sample(0, 0, 0, 0);
        InnerGroveSample grove = InnerGroveSampler.sample(seed, 0, 0, 0, 0, terrain);
        InnerSceneSample scene = InnerSceneSampler.sample(seed, 0, 0, 0, 0, terrain, grove);
        return !canHostLakeScene(terrain, scene);
    }

    static int lakeSceneTypeCountForTest() {
        return InnerLakeSceneType.values().length;
    }

    static InnerLakeSceneType typeForTest(DrugId drugId, InnerLakeType lakeType) {
        return typeFor(drugId, lakeType);
    }

    static void place(InnerBlockSink sink, InnerChunkSampleCache cache, int minX, int minZ) {
        int islandCenterX = cache.islandCenterX();
        int islandCenterZ = cache.islandCenterZ();
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        int scenesPlaced = 0;
        for (int worldZ = minZ - MARGIN; worldZ < minZ + 16 + MARGIN && scenesPlaced < 1; worldZ++) {
            if (!InnerChunkSampleCache.chunkLocalCandidate(worldZ, 2, 16, 3)) {
                continue;
            }
            for (int worldX = minX - MARGIN; worldX < minX + 16 + MARGIN && scenesPlaced < 1; worldX++) {
                if (!InnerChunkSampleCache.chunkLocalCandidate(worldX, 2, 16, 3)) {
                    continue;
                }
                InnerTerrain.Sample sample = cache.sampleAt(worldX, worldZ);
                InnerGroveSample grove = cache.groveAt(worldX, worldZ, sample);
                InnerSceneSample scene = cache.sceneAt(worldX, worldZ, sample, grove);
                if (!canHostLakeScene(sample, scene)) {
                    continue;
                }
                long hash = InnerNoise.mix64(seed
                        ^ (long) worldX * 0x52D2_17ABL
                        ^ (long) worldZ * 0x3C43_31D1L);
                double chance = 0.028D + sample.lakeCoreStrength() * 0.060D + scene.lakeDetailMultiplier() * 0.020D;
                if (!sample.lakeCenterpiece() && (hash & 1023L) >= chance * 1024.0D) {
                    continue;
                }
                buildLakeScene(worldX, sample.lakeSurfaceY(), worldZ, sample, scene, hash, sink);
                scenesPlaced++;
            }
        }
    }

    private static boolean canHostLakeScene(InnerTerrain.Sample sample, InnerSceneSample scene) {
        return sample.land()
                && sample.lake()
                && !sample.hole()
                && sample.pathStrength() < 0.52D
                && sample.distanceFromCenter() > InnerDimensionConstants.CORE_RADIUS + 72.0D
                && (scene.lakeDetailMultiplier() > 0.90D || sample.lakeCoreStrength() > 0.56D);
    }

    private static void buildLakeScene(
            int x,
            int surfaceY,
            int z,
            InnerTerrain.Sample sample,
            InnerSceneSample scene,
            long hash,
            InnerBlockSink sink
    ) {
        InnerLakeSceneType type = typeFor(sample.chooseFeatureDrug(hash), sample.lakeType());
        switch (type) {
            case MIRROR_LAKE -> mirrorLake(x, surfaceY, z, sample, hash, sink);
            case MEMORY_MARSH -> memoryMarsh(x, surfaceY, z, hash, sink);
            case PRISM_BASIN -> prismBasin(x, surfaceY, z, hash, sink);
            case ROOT_LAKE -> rootLake(x, surfaceY, z, hash, sink);
            case EMBER_PIT -> emberPit(x, surfaceY, z, hash, sink);
            case CRYSTAL_SINK -> crystalSink(x, surfaceY, z, hash, sink);
            case REDLINE_DRY_BASIN -> redlineDryBasin(x, surfaceY, z, hash, sink);
            case ASH_BASIN -> ashBasin(x, surfaceY, z, hash, sink);
        }
        if (scene.preserveOpenView()) {
            clearLowView(x, surfaceY, z, sink);
        }
    }

    private static InnerLakeSceneType typeFor(DrugId drugId, InnerLakeType lakeType) {
        if (lakeType == InnerLakeType.PRISM) {
            return drugId == DrugId.HASH ? InnerLakeSceneType.CRYSTAL_SINK : InnerLakeSceneType.PRISM_BASIN;
        }
        if (lakeType == InnerLakeType.MAGMA || drugId == DrugId.METH) {
            return InnerLakeSceneType.EMBER_PIT;
        }
        if (lakeType == InnerLakeType.MEMORY || drugId == DrugId.ALCOHOL) {
            return InnerLakeSceneType.MEMORY_MARSH;
        }
        return switch (drugId) {
            case TOBACCO -> InnerLakeSceneType.ASH_BASIN;
            case HASH -> InnerLakeSceneType.CRYSTAL_SINK;
            case COCAINE -> InnerLakeSceneType.REDLINE_DRY_BASIN;
            case LSD -> InnerLakeSceneType.PRISM_BASIN;
            case MUSHROOMS -> InnerLakeSceneType.ROOT_LAKE;
            default -> InnerLakeSceneType.MIRROR_LAKE;
        };
    }

    private static void mirrorLake(int x, int y, int z, InnerTerrain.Sample sample, long hash, InnerBlockSink sink) {
        BlockState bottomGlow = sample.drugId() == DrugId.COFFEE
                ? Blocks.GLOWSTONE.defaultBlockState()
                : InnerGlowBuilder.glowStateFor(sample.drugId());
        sink.setBlock(new BlockPos(x, y - 1, z), bottomGlow, true);
        sink.setBlock(new BlockPos(x, y + 1, z), Blocks.CALCITE.defaultBlockState(), true);
        rim(x, y, z, 4, sample.profile().surfaceBlock(), Blocks.MOSS_BLOCK.defaultBlockState(), hash, sink);
    }

    private static void memoryMarsh(int x, int y, int z, long hash, InnerBlockSink sink) {
        sink.setBlock(new BlockPos(x, y, z), Blocks.MUD.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x, y + 1, z), Blocks.SOUL_LANTERN.defaultBlockState(), true);
        rootLine(x, y + 1, z, Blocks.DARK_OAK_LOG.defaultBlockState(), hash, sink);
        rim(x, y, z, 5, Blocks.MUD.defaultBlockState(), ModInnerDimensionBlocks.MEMORY_REEDS.get().defaultBlockState(), hash, sink);
    }

    private static void prismBasin(int x, int y, int z, long hash, InnerBlockSink sink) {
        sink.setBlock(new BlockPos(x, y - 1, z), Blocks.SEA_LANTERN.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x, y + 1, z), Blocks.TINTED_GLASS.defaultBlockState(), true);
        rim(x, y, z, 5, Blocks.PRISMARINE.defaultBlockState(), Blocks.SEA_LANTERN.defaultBlockState(), hash, sink);
        shardCluster(x, y + 1, z, Blocks.CALCITE.defaultBlockState(), Blocks.TINTED_GLASS.defaultBlockState(), sink);
    }

    private static void rootLake(int x, int y, int z, long hash, InnerBlockSink sink) {
        sink.setBlock(new BlockPos(x, y - 1, z), Blocks.SHROOMLIGHT.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x, y + 1, z), ModInnerDimensionBlocks.SPORE_BLOOM.get().defaultBlockState(), true);
        rootLine(x, y + 1, z, Blocks.MUSHROOM_STEM.defaultBlockState(), hash, sink);
        rim(x, y, z, 5, Blocks.MYCELIUM.defaultBlockState(), ModInnerDimensionBlocks.MYCELIAL_THREADS.get().defaultBlockState(), hash, sink);
    }

    private static void emberPit(int x, int y, int z, long hash, InnerBlockSink sink) {
        sink.setBlock(new BlockPos(x, y, z), Blocks.MAGMA_BLOCK.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x + 1, y, z), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x - 1, y, z), Blocks.BASALT.defaultBlockState(), true);
        rim(x, y, z, 4, Blocks.BLACKSTONE.defaultBlockState(), Blocks.MAGMA_BLOCK.defaultBlockState(), hash, sink);
    }

    private static void crystalSink(int x, int y, int z, long hash, InnerBlockSink sink) {
        sink.setBlock(new BlockPos(x, y - 1, z), Blocks.SEA_LANTERN.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x, y + 1, z), Blocks.AMETHYST_BLOCK.defaultBlockState(), true);
        rim(x, y, z, 5, Blocks.CALCITE.defaultBlockState(), Blocks.AMETHYST_BLOCK.defaultBlockState(), hash, sink);
        shardCluster(x, y + 1, z, Blocks.CALCITE.defaultBlockState(), Blocks.AMETHYST_BLOCK.defaultBlockState(), sink);
    }

    private static void redlineDryBasin(int x, int y, int z, long hash, InnerBlockSink sink) {
        for (int i = -5; i <= 5; i++) {
            BlockState state = Math.abs(i) % 3 == 0
                    ? Blocks.REDSTONE_BLOCK.defaultBlockState()
                    : Blocks.SMOOTH_QUARTZ.defaultBlockState();
            sink.setBlock(new BlockPos(x + i, y, z), state, true);
        }
        rim(x, y, z, 4, Blocks.WHITE_CONCRETE.defaultBlockState(), Blocks.REDSTONE_BLOCK.defaultBlockState(), hash, sink);
    }

    private static void ashBasin(int x, int y, int z, long hash, InnerBlockSink sink) {
        sink.setBlock(new BlockPos(x, y, z), Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), true);
        for (int i = 0; i < 4; i++) {
            sink.setBlock(new BlockPos(x, y + i + 1, z), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), true);
        }
        rim(x, y, z, 4, Blocks.TUFF.defaultBlockState(), Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), hash, sink);
    }

    private static void rim(
            int x,
            int y,
            int z,
            int radius,
            BlockState rim,
            BlockState accent,
            long hash,
            InnerBlockSink sink
    ) {
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int distSq = dx * dx + dz * dz;
                if (distSq < radius * radius - radius || distSq > radius * radius + radius) {
                    continue;
                }
                long local = InnerNoise.mix64(hash + dx * 31L + dz * 17L);
                sink.setBlock(new BlockPos(x + dx, y, z + dz), (local & 3L) == 0L ? accent : rim, true);
            }
        }
    }

    private static void rootLine(int x, int y, int z, BlockState root, long hash, InnerBlockSink sink) {
        boolean eastWest = (hash & 1L) == 0L;
        for (int i = -4; i <= 4; i++) {
            sink.setBlock(new BlockPos(x + (eastWest ? i : 0), y, z + (eastWest ? 0 : i)), root, true);
        }
    }

    private static void shardCluster(int x, int y, int z, BlockState trunk, BlockState glow, InnerBlockSink sink) {
        int[][] offsets = {{0, 0}, {2, 1}, {-2, 0}, {1, -2}};
        for (int i = 0; i < offsets.length; i++) {
            int height = 2 + i;
            for (int dy = 0; dy < height; dy++) {
                sink.setBlock(new BlockPos(x + offsets[i][0], y + dy, z + offsets[i][1]), dy == height - 1 ? glow : trunk, true);
            }
        }
    }

    private static void clearLowView(int x, int y, int z, InnerBlockSink sink) {
        for (int dz = -3; dz <= 3; dz++) {
            for (int dx = -3; dx <= 3; dx++) {
                if (dx * dx + dz * dz <= 9) {
                    sink.setBlock(new BlockPos(x + dx, y + 2, z + dz), Blocks.AIR.defaultBlockState(), true);
                }
            }
        }
    }
}
