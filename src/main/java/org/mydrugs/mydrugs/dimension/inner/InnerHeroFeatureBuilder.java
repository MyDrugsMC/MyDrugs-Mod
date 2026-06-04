package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.mydrugs.mydrugs.core.drug.DrugId;

public final class InnerHeroFeatureBuilder {
    // Demoted from 420: hero set-pieces are rarer now so the world reads as natural, not staged.
    private static final long HERO_BASE_WEIGHT = 170L;

    private InnerHeroFeatureBuilder() {
    }

    public static void placeInitialHeroFeatures(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        placeHeroFeatures(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), (pos, state) -> {
            if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
                return;
            }
            if (!chunkPos.equals(new ChunkPos(pos))) {
                return;
            }
            chunk.setBlockState(pos, state, 2);
        });
    }

    static void placeOverlayHeroFeatures(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeHeroFeatures(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
                (pos, state) -> InnerPlacement.safeSet(level, pos, state, true, count));
    }

    static boolean hasHeroForDrug(DrugId drugId) {
        return InnerTreeBuilder.hasArchetypeFor(drugId);
    }

    static boolean rejectsSanctuaryForTest() {
        return InnerGroveSampler.rejectsSanctuaryForTest();
    }

    private static void placeHeroFeatures(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        int islandCenterX = InnerTerrain.slotCenter(minX + 8);
        int islandCenterZ = InnerTerrain.slotCenter(minZ + 8);
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        for (int localZ = 4; localZ <= 11; localZ++) {
            for (int localX = 4; localX <= 11; localX++) {
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                InnerGroveSample grove = InnerGroveSampler.sample(seed, islandCenterX, islandCenterZ, worldX, worldZ, sample);
                InnerSceneSample scene = InnerSceneSampler.sample(seed, islandCenterX, islandCenterZ, worldX, worldZ, sample, grove);
                if (!grove.heroCandidate()
                        || !InnerGroveSampler.canHostMajorFeature(sample)
                        || (scene.preserveOpenView() && scene.type() != InnerSceneType.HERO_TREE_GROVE)) {
                    continue;
                }
                // Bias hero set-pieces off the routes so they feel stumbled-upon, not staged along
                // the way to a shrine.
                if (sample.path()
                        || sample.pathStrength() > 0.25D
                        || scene.type() == InnerSceneType.LANDMARK_APPROACH) {
                    continue;
                }
                long hash = InnerNoise.mix64(seed
                        ^ (long) worldX * 0x7EC1_11ADL
                        ^ (long) worldZ * 0x45D9_F3B1L);
                if ((hash & 4095L) >= HERO_BASE_WEIGHT * scene.heroFeatureMultiplier()) {
                    continue;
                }
                buildHero(worldX, sample.topY() + 1, worldZ, sample.chooseFeatureDrug(hash), hash, setter);
            }
        }
    }

    private static void buildHero(int x, int y, int z, DrugId drugId, long hash, BlockSetter setter) {
        int height = switch (drugId) {
            case LSD, METH -> 24;
            case HASH, MUSHROOMS -> 21;
            case WEED -> 22;
            case COCAINE -> 20;
            default -> 18;
        } + (int) (hash & 3L);
        switch (drugId) {
            case TOBACCO -> skeletalHero(x, y, z, height, setter);
            case WEED -> canopyHero(x, y, z, height, Blocks.JUNGLE_LOG.defaultBlockState(), Blocks.AZALEA_LEAVES.defaultBlockState(), setter);
            case HASH -> crystalHero(x, y, z, height, setter);
            case ALCOHOL -> drownedHero(x, y, z, height, setter);
            case COCAINE -> redlineHero(x, y, z, height, setter);
            case LSD -> prismHero(x, y, z, height, setter);
            case METH -> lightningHero(x, y, z, height, setter);
            case MUSHROOMS -> motherMushroom(x, y, z, height, hash, setter);
            default -> canopyHero(x, y, z, height, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LEAVES.defaultBlockState(), setter);
        }
    }

    private static void canopyHero(int x, int y, int z, int height, BlockState log, BlockState leaves, BlockSetter setter) {
        thickColumn(x, y, z, height, log, setter);
        int top = y + height;
        canopy(x, top, z, 5, leaves, setter);
        canopy(x, top + 2, z, 3, leaves, setter);
        ringRoots(x, y, z, Blocks.ROOTED_DIRT.defaultBlockState(), setter);
    }

    private static void skeletalHero(int x, int y, int z, int height, BlockSetter setter) {
        thickColumn(x, y, z, height, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), setter);
        for (int i = 3; i < height; i += 4) {
            setter.set(new BlockPos(x + 2, y + i, z), Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
            setter.set(new BlockPos(x - 2, y + i + 1, z), Blocks.TUFF.defaultBlockState());
            setter.set(new BlockPos(x, y + i, z + 2), Blocks.IRON_BARS.defaultBlockState());
        }
        ringRoots(x, y, z, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), setter);
    }

    private static void crystalHero(int x, int y, int z, int height, BlockSetter setter) {
        column(x, y, z, height, Blocks.CALCITE.defaultBlockState(), setter);
        for (int i = 2; i < height; i += 3) {
            setter.set(new BlockPos(x + 1, y + i, z), Blocks.AMETHYST_BLOCK.defaultBlockState());
            setter.set(new BlockPos(x - 1, y + i + 1, z), Blocks.TINTED_GLASS.defaultBlockState());
            setter.set(new BlockPos(x, y + i, z + 1), Blocks.SMOOTH_BASALT.defaultBlockState());
        }
        canopy(x, y + height, z, 3, Blocks.AMETHYST_BLOCK.defaultBlockState(), setter);
    }

    private static void drownedHero(int x, int y, int z, int height, BlockSetter setter) {
        thickColumn(x, y, z, height, Blocks.DARK_OAK_LOG.defaultBlockState(), setter);
        ringRoots(x, y, z, Blocks.MUD.defaultBlockState(), setter);
        canopy(x, y + height - 2, z, 4, Blocks.ROOTED_DIRT.defaultBlockState(), setter);
    }

    private static void redlineHero(int x, int y, int z, int height, BlockSetter setter) {
        column(x, y, z, height, Blocks.SMOOTH_QUARTZ.defaultBlockState(), setter);
        for (int i = 0; i <= height; i += 4) {
            setter.set(new BlockPos(x, y + i, z), Blocks.REDSTONE_BLOCK.defaultBlockState());
            setter.set(new BlockPos(x + 1, y + i, z), Blocks.SMOOTH_QUARTZ.defaultBlockState());
        }
        ringRoots(x, y, z, Blocks.REDSTONE_BLOCK.defaultBlockState(), setter);
    }

    private static void prismHero(int x, int y, int z, int height, BlockSetter setter) {
        column(x, y, z, height, Blocks.PRISMARINE.defaultBlockState(), setter);
        for (int i = 2; i <= height; i += 5) {
            setter.set(new BlockPos(x + 2, y + i, z), Blocks.SEA_LANTERN.defaultBlockState());
            setter.set(new BlockPos(x - 2, y + i + 1, z), Blocks.TINTED_GLASS.defaultBlockState());
            setter.set(new BlockPos(x, y + i, z + 2), Blocks.PRISMARINE.defaultBlockState());
            setter.set(new BlockPos(x, y + i + 1, z - 2), Blocks.SEA_LANTERN.defaultBlockState());
        }
        canopy(x, y + height, z, 4, Blocks.TINTED_GLASS.defaultBlockState(), setter);
    }

    private static void lightningHero(int x, int y, int z, int height, BlockSetter setter) {
        int ox = 0;
        int oz = 0;
        for (int i = 0; i < height; i++) {
            if (i % 5 == 0) {
                ox += i % 10 == 0 ? 1 : -1;
            }
            if (i % 7 == 0) {
                oz += i % 14 == 0 ? 1 : -1;
            }
            BlockState state = i % 4 == 0 ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.BASALT.defaultBlockState();
            setter.set(new BlockPos(x + ox, y + i, z + oz), state);
        }
        ringRoots(x, y, z, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), setter);
    }

    private static void motherMushroom(int x, int y, int z, int height, long hash, BlockSetter setter) {
        column(x, y, z, height, Blocks.MUSHROOM_STEM.defaultBlockState(), setter);
        BlockState cap = (hash & 1L) == 0L
                ? Blocks.RED_MUSHROOM_BLOCK.defaultBlockState()
                : Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState();
        canopy(x, y + height, z, 6, cap, setter);
        canopy(x, y + height + 2, z, 4, cap, setter);
        ringRoots(x, y, z, Blocks.MYCELIUM.defaultBlockState(), setter);
    }

    private static void column(int x, int y, int z, int height, BlockState state, BlockSetter setter) {
        for (int i = 0; i < height; i++) {
            setter.set(new BlockPos(x, y + i, z), state);
        }
    }

    private static void thickColumn(int x, int y, int z, int height, BlockState state, BlockSetter setter) {
        for (int i = 0; i < height; i++) {
            setter.set(new BlockPos(x, y + i, z), state);
            setter.set(new BlockPos(x + 1, y + i, z), state);
            setter.set(new BlockPos(x, y + i, z + 1), state);
            setter.set(new BlockPos(x + 1, y + i, z + 1), state);
        }
    }

    private static void canopy(int x, int y, int z, int radius, BlockState state, BlockSetter setter) {
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dz * dz <= radius * radius + 1) {
                    setter.set(new BlockPos(x + dx, y, z + dz), state);
                }
            }
        }
    }

    private static void ringRoots(int x, int y, int z, BlockState state, BlockSetter setter) {
        for (int dz = -3; dz <= 3; dz++) {
            for (int dx = -3; dx <= 3; dx++) {
                int dist = Math.abs(dx) + Math.abs(dz);
                if (dist >= 2 && dist <= 4) {
                    setter.set(new BlockPos(x + dx, y - 1, z + dz), state);
                }
            }
        }
    }

    private interface BlockSetter {
        void set(BlockPos pos, BlockState state);
    }
}
