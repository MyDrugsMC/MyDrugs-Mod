package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;

public final class InnerHeroFeatureBuilder {
    // Demoted from 420: hero set-pieces are rarer now so the world reads as natural, not staged.
    private static final long HERO_BASE_WEIGHT = 170L;
    private static final int MARGIN = 6;

    private InnerHeroFeatureBuilder() {
    }

    static boolean hasHeroForDrug(DrugId drugId) {
        return InnerTreeBuilder.hasArchetypeFor(drugId);
    }

    static boolean rejectsSanctuaryForTest() {
        return InnerGroveSampler.rejectsSanctuaryForTest();
    }

    static void place(InnerBlockSink sink, InnerChunkSampleCache cache, int minX, int minZ) {
        int islandCenterX = cache.islandCenterX();
        int islandCenterZ = cache.islandCenterZ();
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        for (int worldZ = minZ - MARGIN; worldZ < minZ + 16 + MARGIN; worldZ++) {
            if (!InnerChunkSampleCache.chunkLocalCandidate(worldZ, 4, 12, 1)) {
                continue;
            }
            for (int worldX = minX - MARGIN; worldX < minX + 16 + MARGIN; worldX++) {
                if (!InnerChunkSampleCache.chunkLocalCandidate(worldX, 4, 12, 1)) {
                    continue;
                }
                InnerTerrain.Sample sample = cache.sampleAt(worldX, worldZ);
                InnerGroveSample grove = cache.groveAt(worldX, worldZ, sample);
                InnerSceneSample scene = cache.sceneAt(worldX, worldZ, sample, grove);
                if (!grove.heroCandidate()
                        || !InnerGroveSampler.canHostMajorFeature(sample)
                        || (scene.preserveOpenView() && scene.type() != InnerSceneType.HERO_TREE_GROVE)
                        || InnerRegionMap.inCoreSightlineWedge(islandCenterX, islandCenterZ, worldX, worldZ)) {
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
                buildHero(worldX, sample.topY() + 1, worldZ, sample.chooseFeatureDrug(hash), hash, sink);
            }
        }
    }

    private static void buildHero(int x, int y, int z, DrugId drugId, long hash, InnerBlockSink sink) {
        int height = switch (drugId) {
            case LSD, METH -> 24;
            case HASH, MUSHROOMS -> 21;
            case WEED -> 22;
            case COCAINE -> 20;
            default -> 18;
        } + (int) (hash & 3L);
        switch (drugId) {
            case TOBACCO -> skeletalHero(x, y, z, height, sink);
            case WEED -> canopyHero(x, y, z, height, Blocks.JUNGLE_LOG.defaultBlockState(), Blocks.AZALEA_LEAVES.defaultBlockState(), sink);
            case HASH -> crystalHero(x, y, z, height, sink);
            case ALCOHOL -> drownedHero(x, y, z, height, sink);
            case COCAINE -> redlineHero(x, y, z, height, sink);
            case LSD -> prismHero(x, y, z, height, sink);
            case METH -> lightningHero(x, y, z, height, sink);
            case MUSHROOMS -> motherMushroom(x, y, z, height, hash, sink);
            default -> canopyHero(x, y, z, height, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LEAVES.defaultBlockState(), sink);
        }
    }

    private static void canopyHero(int x, int y, int z, int height, BlockState log, BlockState leaves, InnerBlockSink sink) {
        thickColumn(x, y, z, height, log, sink);
        int top = y + height;
        canopy(x, top, z, 5, leaves, sink);
        canopy(x, top + 2, z, 3, leaves, sink);
        ringRoots(x, y, z, Blocks.ROOTED_DIRT.defaultBlockState(), sink);
    }

    private static void skeletalHero(int x, int y, int z, int height, InnerBlockSink sink) {
        thickColumn(x, y, z, height, Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState(), sink);
        for (int i = 3; i < height; i += 4) {
            sink.setBlock(new BlockPos(x + 2, y + i, z), Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), true);
            sink.setBlock(new BlockPos(x - 2, y + i + 1, z), Blocks.TUFF.defaultBlockState(), true);
            sink.setBlock(new BlockPos(x, y + i, z + 2), Blocks.IRON_BARS.defaultBlockState(), true);
        }
        ringRoots(x, y, z, Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), sink);
    }

    private static void crystalHero(int x, int y, int z, int height, InnerBlockSink sink) {
        column(x, y, z, height, Blocks.CALCITE.defaultBlockState(), sink);
        for (int i = 2; i < height; i += 3) {
            sink.setBlock(new BlockPos(x + 1, y + i, z), Blocks.AMETHYST_BLOCK.defaultBlockState(), true);
            sink.setBlock(new BlockPos(x - 1, y + i + 1, z), Blocks.TINTED_GLASS.defaultBlockState(), true);
            sink.setBlock(new BlockPos(x, y + i, z + 1), Blocks.SMOOTH_BASALT.defaultBlockState(), true);
        }
        canopy(x, y + height, z, 3, Blocks.AMETHYST_BLOCK.defaultBlockState(), sink);
    }

    private static void drownedHero(int x, int y, int z, int height, InnerBlockSink sink) {
        thickColumn(x, y, z, height, Blocks.DARK_OAK_LOG.defaultBlockState(), sink);
        ringRoots(x, y, z, Blocks.MUD.defaultBlockState(), sink);
        canopy(x, y + height - 2, z, 4, Blocks.ROOTED_DIRT.defaultBlockState(), sink);
    }

    private static void redlineHero(int x, int y, int z, int height, InnerBlockSink sink) {
        column(x, y, z, height, Blocks.SMOOTH_QUARTZ.defaultBlockState(), sink);
        for (int i = 0; i <= height; i += 4) {
            sink.setBlock(new BlockPos(x, y + i, z), Blocks.REDSTONE_BLOCK.defaultBlockState(), true);
            sink.setBlock(new BlockPos(x + 1, y + i, z), Blocks.SMOOTH_QUARTZ.defaultBlockState(), true);
        }
        ringRoots(x, y, z, Blocks.REDSTONE_BLOCK.defaultBlockState(), sink);
    }

    private static void prismHero(int x, int y, int z, int height, InnerBlockSink sink) {
        column(x, y, z, height, Blocks.PRISMARINE.defaultBlockState(), sink);
        for (int i = 2; i <= height; i += 5) {
            sink.setBlock(new BlockPos(x + 2, y + i, z), Blocks.SEA_LANTERN.defaultBlockState(), true);
            sink.setBlock(new BlockPos(x - 2, y + i + 1, z), Blocks.TINTED_GLASS.defaultBlockState(), true);
            sink.setBlock(new BlockPos(x, y + i, z + 2), Blocks.PRISMARINE.defaultBlockState(), true);
            sink.setBlock(new BlockPos(x, y + i + 1, z - 2), Blocks.SEA_LANTERN.defaultBlockState(), true);
        }
        canopy(x, y + height, z, 4, Blocks.TINTED_GLASS.defaultBlockState(), sink);
    }

    private static void lightningHero(int x, int y, int z, int height, InnerBlockSink sink) {
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
            sink.setBlock(new BlockPos(x + ox, y + i, z + oz), state, true);
        }
        ringRoots(x, y, z, Blocks.POLISHED_BLACKSTONE.defaultBlockState(), sink);
    }

    private static void motherMushroom(int x, int y, int z, int height, long hash, InnerBlockSink sink) {
        column(x, y, z, height, Blocks.MUSHROOM_STEM.defaultBlockState(), sink);
        BlockState cap = (hash & 1L) == 0L
                ? Blocks.RED_MUSHROOM_BLOCK.defaultBlockState()
                : Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState();
        canopy(x, y + height, z, 6, cap, sink);
        canopy(x, y + height + 2, z, 4, cap, sink);
        ringRoots(x, y, z, Blocks.MYCELIUM.defaultBlockState(), sink);
    }

    private static void column(int x, int y, int z, int height, BlockState state, InnerBlockSink sink) {
        for (int i = 0; i < height; i++) {
            sink.setBlock(new BlockPos(x, y + i, z), state, true);
        }
    }

    private static void thickColumn(int x, int y, int z, int height, BlockState state, InnerBlockSink sink) {
        for (int i = 0; i < height; i++) {
            sink.setBlock(new BlockPos(x, y + i, z), state, true);
            sink.setBlock(new BlockPos(x + 1, y + i, z), state, true);
            sink.setBlock(new BlockPos(x, y + i, z + 1), state, true);
            sink.setBlock(new BlockPos(x + 1, y + i, z + 1), state, true);
        }
    }

    private static void canopy(int x, int y, int z, int radius, BlockState state, InnerBlockSink sink) {
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dz * dz <= radius * radius + 1) {
                    sink.setBlock(new BlockPos(x + dx, y, z + dz), state, true);
                }
            }
        }
    }

    private static void ringRoots(int x, int y, int z, BlockState state, InnerBlockSink sink) {
        for (int dz = -3; dz <= 3; dz++) {
            for (int dx = -3; dx <= 3; dx++) {
                int dist = Math.abs(dx) + Math.abs(dz);
                if (dist >= 2 && dist <= 4) {
                    sink.setBlock(new BlockPos(x + dx, y - 1, z + dz), state, true);
                }
            }
        }
    }
}
