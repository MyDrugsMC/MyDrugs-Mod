package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;

public final class InnerTreeBuilder {
    private static final int MARGIN = 2;

    private InnerTreeBuilder() {
    }

    static boolean hasArchetypeFor(DrugId drugId) {
        return InnerGroveSampler.hasTreeArchetype(drugId);
    }

    static void place(InnerBlockSink sink, InnerChunkSampleCache cache, int minX, int minZ) {
        int islandCenterX = cache.islandCenterX();
        int islandCenterZ = cache.islandCenterZ();
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        for (int worldZ = minZ - MARGIN; worldZ < minZ + 16 + MARGIN; worldZ++) {
            if (!InnerChunkSampleCache.chunkLocalCandidate(worldZ, 1, 16, 2)) {
                continue;
            }
            for (int worldX = minX - MARGIN; worldX < minX + 16 + MARGIN; worldX++) {
                if (!InnerChunkSampleCache.chunkLocalCandidate(worldX, 1, 16, 2)) {
                    continue;
                }
                InnerTerrain.Sample sample = cache.sampleAt(worldX, worldZ);
                InnerGroveSample grove = cache.groveAt(worldX, worldZ, sample);
                InnerSceneSample scene = cache.sceneAt(worldX, worldZ, sample, grove);
                if (!canHostTree(sample, grove, scene)
                        || InnerRegionMap.inCoreSightlineWedge(islandCenterX, islandCenterZ, worldX, worldZ)) {
                    continue;
                }
                long hash = InnerNoise.mix64(seed ^ worldX * 0x4F1BBCDCL ^ worldZ * 0x31DAA2A7L);
                double chance = 0.018D + sample.treeDensity() * 0.045D + grove.canopyDensity() * 0.105D;
                chance *= scene.canopyMultiplier();
                if (grove.groveCore()) {
                    chance += 0.045D;
                }
                if ((hash & 1023L) >= chance * 1024.0D) {
                    continue;
                }
                buildTree(worldX, sample.topY() + 1, worldZ, sample, grove, hash, sink);
            }
        }
    }

    static boolean canHostTreeForTest(InnerTerrain.Sample sample, InnerGroveSample grove, InnerSceneSample scene) {
        return canHostTree(sample, grove, scene);
    }

    static boolean scansTreeAnchorForTest(int minX, int minZ, int worldX, int worldZ) {
        return scansTreeAnchor(minX, minZ, worldX, worldZ);
    }

    static boolean selectedTreeAnchorForTest(InnerChunkSampleCache cache, int minX, int minZ, int worldX, int worldZ) {
        if (!scansTreeAnchor(minX, minZ, worldX, worldZ)) {
            return false;
        }
        int islandCenterX = cache.islandCenterX();
        int islandCenterZ = cache.islandCenterZ();
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        InnerTerrain.Sample sample = cache.sampleAt(worldX, worldZ);
        InnerGroveSample grove = cache.groveAt(worldX, worldZ, sample);
        InnerSceneSample scene = cache.sceneAt(worldX, worldZ, sample, grove);
        if (!canHostTree(sample, grove, scene)
                || InnerRegionMap.inCoreSightlineWedge(islandCenterX, islandCenterZ, worldX, worldZ)) {
            return false;
        }
        long hash = InnerNoise.mix64(seed ^ worldX * 0x4F1BBCDCL ^ worldZ * 0x31DAA2A7L);
        double chance = 0.018D + sample.treeDensity() * 0.045D + grove.canopyDensity() * 0.105D;
        chance *= scene.canopyMultiplier();
        if (grove.groveCore()) {
            chance += 0.045D;
        }
        return (hash & 1023L) < chance * 1024.0D;
    }

    private static boolean scansTreeAnchor(int minX, int minZ, int worldX, int worldZ) {
        return worldX >= minX - MARGIN
                && worldX < minX + 16 + MARGIN
                && worldZ >= minZ - MARGIN
                && worldZ < minZ + 16 + MARGIN
                && InnerChunkSampleCache.chunkLocalCandidate(worldX, 1, 16, 2)
                && InnerChunkSampleCache.chunkLocalCandidate(worldZ, 1, 16, 2);
    }

    private static boolean canHostTree(InnerTerrain.Sample sample, InnerGroveSample grove, InnerSceneSample scene) {
        return sample.land()
                && grove.inGrove()
                && (sample.treeZone() || grove.canopyDensity() > 0.08D)
                && !sample.lake()
                && !sample.hole()
                && sample.pathStrength() < 0.34D
                && sample.distanceFromCenter() > InnerDimensionConstants.CORE_RADIUS + 64.0D
                && (!scene.preserveOpenView()
                || scene.type() == InnerSceneType.DENSE_GROVE
                || scene.type() == InnerSceneType.HERO_TREE_GROVE);
    }

    private static void buildTree(
            int x,
            int y,
            int z,
            InnerTerrain.Sample sample,
            InnerGroveSample grove,
            long hash,
            InnerBlockSink sink
    ) {
        DrugId drugId = sample.chooseFeatureDrug(hash);
        int height = treeHeight(drugId, grove, hash);
        switch (drugId) {
            case TOBACCO -> deadTree(x, y, z, height, hash, sink);
            case WEED -> leafyTree(x, y, z, height, Blocks.JUNGLE_LOG.defaultBlockState(),
                    Blocks.AZALEA_LEAVES.defaultBlockState(), true, hash, sink);
            case HASH -> crystalTree(x, y, z, height, hash, sink);
            case ALCOHOL -> drownedTree(x, y, z, height, hash, sink);
            case COCAINE -> needleTree(x, y, z, height, Blocks.SMOOTH_QUARTZ.defaultBlockState(),
                    Blocks.REDSTONE_BLOCK.defaultBlockState(), hash, sink);
            case LSD -> prismTree(x, y, z, height, hash, sink);
            case METH -> needleTree(x, y, z, height + 2, Blocks.BASALT.defaultBlockState(),
                    Blocks.MAGMA_BLOCK.defaultBlockState(), hash, sink);
            case MUSHROOMS -> mushroomTree(x, y, z, height, hash, sink);
            default -> leafyTree(x, y, z, height, Blocks.OAK_LOG.defaultBlockState(),
                    Blocks.OAK_LEAVES.defaultBlockState(), false, hash, sink);
        }
        if (grove.groveCore() || (hash & 7L) == 0L) {
            rootFan(x, y, z, drugId, hash, sink);
        }
    }

    private static int treeHeight(DrugId drugId, InnerGroveSample grove, long hash) {
        int jitter = (int) (Math.abs(hash) % 4L);
        int base = switch (drugId) {
            case METH, COCAINE, LSD -> 7 + jitter;
            case MUSHROOMS, HASH -> 6 + jitter;
            case TOBACCO, ALCOHOL -> 5 + jitter;
            default -> 4 + jitter;
        };
        int groveBoost = grove.groveCore() ? 4 : grove.groveEdge() ? 2 : 0;
        return base + groveBoost + (int) Math.round(grove.strength() * 3.0D);
    }

    private static void leafyTree(
            int x,
            int y,
            int z,
            int height,
            BlockState log,
            BlockState leaves,
            boolean mossy,
            long hash,
            InnerBlockSink sink
    ) {
        for (int dy = 0; dy < height; dy++) {
            sink.setBlock(new BlockPos(x, y + dy, z), log, true);
        }
        int top = y + height - 1;
        for (int dz = -2; dz <= 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                int manhattan = Math.abs(dx) + Math.abs(dz);
                if (manhattan > 3) {
                    continue;
                }
                sink.setBlock(new BlockPos(x + dx, top, z + dz), leaves, true);
                if (manhattan <= 2) {
                    sink.setBlock(new BlockPos(x + dx, top + 1, z + dz), leaves, true);
                }
            }
        }
        if (mossy) {
            sink.setBlock(new BlockPos(x + 1, y, z), Blocks.MOSS_BLOCK.defaultBlockState(), true);
            sink.setBlock(new BlockPos(x - 1, y, z), Blocks.MOSS_BLOCK.defaultBlockState(), true);
        } else if ((hash & 31L) == 0L) {
            sink.setBlock(new BlockPos(x + 1, y, z), Blocks.BOOKSHELF.defaultBlockState(), true);
        }
    }

    private static void deadTree(int x, int y, int z, int height, long hash, InnerBlockSink sink) {
        BlockState trunk = ((hash & 1L) == 0L)
                ? Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState()
                : Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
        for (int dy = 0; dy < height; dy++) {
            sink.setBlock(new BlockPos(x, y + dy, z), trunk, true);
        }
        int top = y + height - 2;
        sink.setBlock(new BlockPos(x + 1, top, z), Blocks.TUFF.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x - 1, top + 1, z), Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x, y, z + 1), Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), true);
    }

    private static void crystalTree(int x, int y, int z, int height, long hash, InnerBlockSink sink) {
        for (int dy = 0; dy < height; dy++) {
            sink.setBlock(new BlockPos(x, y + dy, z), Blocks.CALCITE.defaultBlockState(), true);
        }
        int top = y + height - 1;
        sink.setBlock(new BlockPos(x, top + 1, z), Blocks.AMETHYST_BLOCK.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x + 1, top, z), Blocks.AMETHYST_BLOCK.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x - 1, top, z), Blocks.TINTED_GLASS.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x, top, z + 1), Blocks.SMOOTH_BASALT.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x, top, z - 1), Blocks.AMETHYST_BLOCK.defaultBlockState(), true);
    }

    private static void drownedTree(int x, int y, int z, int height, long hash, InnerBlockSink sink) {
        BlockState trunk = ((hash & 2L) == 0L)
                ? Blocks.DARK_OAK_LOG.defaultBlockState()
                : Blocks.SPRUCE_LOG.defaultBlockState();
        for (int dy = 0; dy < height; dy++) {
            sink.setBlock(new BlockPos(x, y + dy, z), trunk, true);
        }
        sink.setBlock(new BlockPos(x + 1, y, z), Blocks.MUD.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x, y, z + 1), Blocks.ROOTED_DIRT.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x, y + height, z), Blocks.ROOTED_DIRT.defaultBlockState(), true);
    }

    private static void needleTree(
            int x,
            int y,
            int z,
            int height,
            BlockState trunk,
            BlockState accent,
            long hash,
            InnerBlockSink sink
    ) {
        for (int dy = 0; dy < height; dy++) {
            sink.setBlock(new BlockPos(x, y + dy, z), dy % 5 == 0 ? accent : trunk, true);
            if (dy < 2) {
                sink.setBlock(new BlockPos(x + 1, y + dy, z), trunk, true);
            }
        }
        sink.setBlock(new BlockPos(x, y + height, z), accent, true);
    }

    private static void prismTree(int x, int y, int z, int height, long hash, InnerBlockSink sink) {
        for (int dy = 0; dy < height; dy++) {
            sink.setBlock(new BlockPos(x, y + dy, z), Blocks.PRISMARINE.defaultBlockState(), true);
        }
        int top = y + height;
        sink.setBlock(new BlockPos(x, top, z), Blocks.SEA_LANTERN.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x + 1, top - 1, z), Blocks.TINTED_GLASS.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x - 1, top, z), Blocks.TINTED_GLASS.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x, top - 1, z + 1), Blocks.PRISMARINE.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x, top, z - 1), Blocks.SEA_LANTERN.defaultBlockState(), true);
    }

    private static void mushroomTree(int x, int y, int z, int height, long hash, InnerBlockSink sink) {
        for (int dy = 0; dy < height; dy++) {
            sink.setBlock(new BlockPos(x, y + dy, z), Blocks.MUSHROOM_STEM.defaultBlockState(), true);
        }
        BlockState cap = (hash & 1L) == 0L
                ? Blocks.RED_MUSHROOM_BLOCK.defaultBlockState()
                : Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState();
        int top = y + height;
        for (int dz = -2; dz <= 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                if (Math.abs(dx) + Math.abs(dz) <= 3) {
                    sink.setBlock(new BlockPos(x + dx, top, z + dz), cap, true);
                }
            }
        }
        sink.setBlock(new BlockPos(x + 1, y, z), Blocks.ROOTED_DIRT.defaultBlockState(), true);
        sink.setBlock(new BlockPos(x, y, z + 1), Blocks.MYCELIUM.defaultBlockState(), true);
    }

    private static void rootFan(int x, int y, int z, DrugId drugId, long hash, InnerBlockSink sink) {
        BlockState root = switch (drugId) {
            case HASH, LSD -> Blocks.CALCITE.defaultBlockState();
            case COCAINE -> Blocks.SMOOTH_QUARTZ.defaultBlockState();
            case METH -> Blocks.BASALT.defaultBlockState();
            case ALCOHOL -> Blocks.MUD.defaultBlockState();
            case MUSHROOMS -> Blocks.MYCELIUM.defaultBlockState();
            case TOBACCO -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            default -> Blocks.ROOTED_DIRT.defaultBlockState();
        };
        for (int i = 0; i < 4; i++) {
            int dx = switch (i) {
                case 0 -> 1;
                case 1 -> -1;
                default -> 0;
            };
            int dz = switch (i) {
                case 2 -> 1;
                case 3 -> -1;
                default -> 0;
            };
            sink.setBlock(new BlockPos(x + dx, y - 1, z + dz), root, true);
            if (((hash >>> (i * 3)) & 1L) == 0L) {
                sink.setBlock(new BlockPos(x + dx * 2, y - 1, z + dz * 2), root, true);
            }
        }
    }
}
