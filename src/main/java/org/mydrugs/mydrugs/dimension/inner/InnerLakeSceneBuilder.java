package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

final class InnerLakeSceneBuilder {
    private InnerLakeSceneBuilder() {
    }

    static void placeInitialLakeScenes(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        placeLakeScenes(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), (pos, state) -> {
            if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
                return;
            }
            if (!chunkPos.equals(new ChunkPos(pos))) {
                return;
            }
            chunk.setBlockState(pos, state, 2);
        });
    }

    static void placeOverlayLakeScenes(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeLakeScenes(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
                (pos, state) -> InnerPlacement.safeSet(level, pos, state, true, count));
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

    private static void placeLakeScenes(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        if (!cache.anyLake()) {
            return;
        }
        int islandCenterX = InnerTerrain.slotCenter(minX + 8);
        int islandCenterZ = InnerTerrain.slotCenter(minZ + 8);
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        int scenesPlaced = 0;
        for (int localZ = 2; localZ < 16 && scenesPlaced < 1; localZ += 3) {
            for (int localX = 2; localX < 16 && scenesPlaced < 1; localX += 3) {
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                InnerSceneSample scene = cache.scene(localX, localZ);
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
                buildLakeScene(worldX, sample.lakeSurfaceY(), worldZ, sample, scene, hash, setter);
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
            BlockSetter setter
    ) {
        InnerLakeSceneType type = typeFor(sample.chooseFeatureDrug(hash), sample.lakeType());
        switch (type) {
            case MIRROR_LAKE -> mirrorLake(x, surfaceY, z, sample, hash, setter);
            case MEMORY_MARSH -> memoryMarsh(x, surfaceY, z, hash, setter);
            case PRISM_BASIN -> prismBasin(x, surfaceY, z, hash, setter);
            case ROOT_LAKE -> rootLake(x, surfaceY, z, hash, setter);
            case EMBER_PIT -> emberPit(x, surfaceY, z, hash, setter);
            case CRYSTAL_SINK -> crystalSink(x, surfaceY, z, hash, setter);
            case REDLINE_DRY_BASIN -> redlineDryBasin(x, surfaceY, z, hash, setter);
            case ASH_BASIN -> ashBasin(x, surfaceY, z, hash, setter);
        }
        if (scene.preserveOpenView()) {
            clearLowView(x, surfaceY, z, setter);
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

    private static void mirrorLake(int x, int y, int z, InnerTerrain.Sample sample, long hash, BlockSetter setter) {
        BlockState bottomGlow = sample.drugId() == DrugId.COFFEE
                ? Blocks.GLOWSTONE.defaultBlockState()
                : InnerGlowBuilder.glowStateFor(sample.drugId());
        setter.set(new BlockPos(x, y - 1, z), bottomGlow);
        setter.set(new BlockPos(x, y + 1, z), Blocks.CALCITE.defaultBlockState());
        rim(x, y, z, 4, sample.profile().surfaceBlock(), Blocks.MOSS_BLOCK.defaultBlockState(), hash, setter);
    }

    private static void memoryMarsh(int x, int y, int z, long hash, BlockSetter setter) {
        setter.set(new BlockPos(x, y, z), Blocks.MUD.defaultBlockState());
        setter.set(new BlockPos(x, y + 1, z), Blocks.SOUL_LANTERN.defaultBlockState());
        rootLine(x, y + 1, z, Blocks.DARK_OAK_LOG.defaultBlockState(), hash, setter);
        rim(x, y, z, 5, Blocks.MUD.defaultBlockState(), ModInnerDimensionBlocks.MEMORY_REEDS.get().defaultBlockState(), hash, setter);
    }

    private static void prismBasin(int x, int y, int z, long hash, BlockSetter setter) {
        setter.set(new BlockPos(x, y - 1, z), Blocks.SEA_LANTERN.defaultBlockState());
        setter.set(new BlockPos(x, y + 1, z), Blocks.TINTED_GLASS.defaultBlockState());
        rim(x, y, z, 5, Blocks.PRISMARINE.defaultBlockState(), Blocks.SEA_LANTERN.defaultBlockState(), hash, setter);
        shardCluster(x, y + 1, z, Blocks.CALCITE.defaultBlockState(), Blocks.TINTED_GLASS.defaultBlockState(), setter);
    }

    private static void rootLake(int x, int y, int z, long hash, BlockSetter setter) {
        setter.set(new BlockPos(x, y - 1, z), Blocks.SHROOMLIGHT.defaultBlockState());
        setter.set(new BlockPos(x, y + 1, z), ModInnerDimensionBlocks.SPORE_BLOOM.get().defaultBlockState());
        rootLine(x, y + 1, z, Blocks.MUSHROOM_STEM.defaultBlockState(), hash, setter);
        rim(x, y, z, 5, Blocks.MYCELIUM.defaultBlockState(), ModInnerDimensionBlocks.MYCELIAL_THREADS.get().defaultBlockState(), hash, setter);
    }

    private static void emberPit(int x, int y, int z, long hash, BlockSetter setter) {
        setter.set(new BlockPos(x, y, z), Blocks.MAGMA_BLOCK.defaultBlockState());
        setter.set(new BlockPos(x + 1, y, z), Blocks.POLISHED_BLACKSTONE.defaultBlockState());
        setter.set(new BlockPos(x - 1, y, z), Blocks.BASALT.defaultBlockState());
        rim(x, y, z, 4, Blocks.BLACKSTONE.defaultBlockState(), Blocks.MAGMA_BLOCK.defaultBlockState(), hash, setter);
    }

    private static void crystalSink(int x, int y, int z, long hash, BlockSetter setter) {
        setter.set(new BlockPos(x, y - 1, z), Blocks.SEA_LANTERN.defaultBlockState());
        setter.set(new BlockPos(x, y + 1, z), Blocks.AMETHYST_BLOCK.defaultBlockState());
        rim(x, y, z, 5, Blocks.CALCITE.defaultBlockState(), Blocks.AMETHYST_BLOCK.defaultBlockState(), hash, setter);
        shardCluster(x, y + 1, z, Blocks.CALCITE.defaultBlockState(), Blocks.AMETHYST_BLOCK.defaultBlockState(), setter);
    }

    private static void redlineDryBasin(int x, int y, int z, long hash, BlockSetter setter) {
        for (int i = -5; i <= 5; i++) {
            BlockState state = Math.abs(i) % 3 == 0
                    ? Blocks.REDSTONE_BLOCK.defaultBlockState()
                    : Blocks.SMOOTH_QUARTZ.defaultBlockState();
            setter.set(new BlockPos(x + i, y, z), state);
        }
        rim(x, y, z, 4, Blocks.WHITE_CONCRETE.defaultBlockState(), Blocks.REDSTONE_BLOCK.defaultBlockState(), hash, setter);
    }

    private static void ashBasin(int x, int y, int z, long hash, BlockSetter setter) {
        setter.set(new BlockPos(x, y, z), Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
        for (int i = 0; i < 4; i++) {
            setter.set(new BlockPos(x, y + i + 1, z), Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState());
        }
        rim(x, y, z, 4, Blocks.TUFF.defaultBlockState(), Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), hash, setter);
    }

    private static void rim(
            int x,
            int y,
            int z,
            int radius,
            BlockState rim,
            BlockState accent,
            long hash,
            BlockSetter setter
    ) {
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int distSq = dx * dx + dz * dz;
                if (distSq < radius * radius - radius || distSq > radius * radius + radius) {
                    continue;
                }
                long local = InnerNoise.mix64(hash + dx * 31L + dz * 17L);
                setter.set(new BlockPos(x + dx, y, z + dz), (local & 3L) == 0L ? accent : rim);
            }
        }
    }

    private static void rootLine(int x, int y, int z, BlockState root, long hash, BlockSetter setter) {
        boolean eastWest = (hash & 1L) == 0L;
        for (int i = -4; i <= 4; i++) {
            setter.set(new BlockPos(x + (eastWest ? i : 0), y, z + (eastWest ? 0 : i)), root);
        }
    }

    private static void shardCluster(int x, int y, int z, BlockState trunk, BlockState glow, BlockSetter setter) {
        int[][] offsets = {{0, 0}, {2, 1}, {-2, 0}, {1, -2}};
        for (int i = 0; i < offsets.length; i++) {
            int height = 2 + i;
            for (int dy = 0; dy < height; dy++) {
                setter.set(new BlockPos(x + offsets[i][0], y + dy, z + offsets[i][1]), dy == height - 1 ? glow : trunk);
            }
        }
    }

    private static void clearLowView(int x, int y, int z, BlockSetter setter) {
        for (int dz = -3; dz <= 3; dz++) {
            for (int dx = -3; dx <= 3; dx++) {
                if (dx * dx + dz * dz <= 9) {
                    setter.set(new BlockPos(x + dx, y + 2, z + dz), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private interface BlockSetter {
        void set(BlockPos pos, BlockState state);
    }
}
