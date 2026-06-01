package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.mydrugs.mydrugs.core.drug.DrugId;

public final class InnerTreeBuilder {
    private InnerTreeBuilder() {
    }

    public static void placeInitialTrees(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        placeTrees(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), (pos, state) -> {
            if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
                return;
            }
            if (!chunkPos.equals(new ChunkPos(pos))) {
                return;
            }
            chunk.setBlockState(pos, state, 2);
        });
    }

    static void placeOverlayTrees(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeTrees(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
                (pos, state) -> InnerPlacement.safeSet(level, pos, state, true, count));
    }

    static boolean hasArchetypeFor(DrugId drugId) {
        return InnerGroveSampler.hasTreeArchetype(drugId);
    }

    private static void placeTrees(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        int islandCenterX = InnerTerrain.slotCenter(minX + 8);
        int islandCenterZ = InnerTerrain.slotCenter(minZ + 8);
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        for (int localZ = 1; localZ < 16; localZ += 2) {
            for (int localX = 1; localX < 16; localX += 2) {
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                InnerGroveSample grove = InnerGroveSampler.sample(seed, islandCenterX, islandCenterZ, worldX, worldZ, sample);
                InnerSceneSample scene = InnerSceneSampler.sample(seed, islandCenterX, islandCenterZ, worldX, worldZ, sample, grove);
                if (!canHostTree(sample, grove, scene)) {
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
                buildTree(worldX, sample.topY() + 1, worldZ, sample, grove, hash, setter);
            }
        }
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
            BlockSetter setter
    ) {
        DrugId drugId = sample.chooseFeatureDrug(hash);
        int height = treeHeight(drugId, grove, hash);
        switch (drugId) {
            case TOBACCO -> deadTree(x, y, z, height, hash, setter);
            case WEED -> leafyTree(x, y, z, height, Blocks.JUNGLE_LOG.defaultBlockState(),
                    Blocks.AZALEA_LEAVES.defaultBlockState(), true, hash, setter);
            case HASH -> crystalTree(x, y, z, height, hash, setter);
            case ALCOHOL -> drownedTree(x, y, z, height, hash, setter);
            case COCAINE -> needleTree(x, y, z, height, Blocks.SMOOTH_QUARTZ.defaultBlockState(),
                    Blocks.REDSTONE_BLOCK.defaultBlockState(), hash, setter);
            case LSD -> prismTree(x, y, z, height, hash, setter);
            case METH -> needleTree(x, y, z, height + 2, Blocks.BASALT.defaultBlockState(),
                    Blocks.MAGMA_BLOCK.defaultBlockState(), hash, setter);
            case MUSHROOMS -> mushroomTree(x, y, z, height, hash, setter);
            default -> leafyTree(x, y, z, height, Blocks.OAK_LOG.defaultBlockState(),
                    Blocks.OAK_LEAVES.defaultBlockState(), false, hash, setter);
        }
        if (grove.groveCore() || (hash & 7L) == 0L) {
            rootFan(x, y, z, drugId, hash, setter);
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
            BlockSetter setter
    ) {
        for (int dy = 0; dy < height; dy++) {
            setter.set(new BlockPos(x, y + dy, z), log);
        }
        int top = y + height - 1;
        for (int dz = -2; dz <= 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                int manhattan = Math.abs(dx) + Math.abs(dz);
                if (manhattan > 3) {
                    continue;
                }
                setter.set(new BlockPos(x + dx, top, z + dz), leaves);
                if (manhattan <= 2) {
                    setter.set(new BlockPos(x + dx, top + 1, z + dz), leaves);
                }
            }
        }
        if (mossy) {
            setter.set(new BlockPos(x + 1, y, z), Blocks.MOSS_BLOCK.defaultBlockState());
            setter.set(new BlockPos(x - 1, y, z), Blocks.MOSS_BLOCK.defaultBlockState());
        } else if ((hash & 31L) == 0L) {
            setter.set(new BlockPos(x + 1, y, z), Blocks.BOOKSHELF.defaultBlockState());
        }
    }

    private static void deadTree(int x, int y, int z, int height, long hash, BlockSetter setter) {
        BlockState trunk = ((hash & 1L) == 0L)
                ? Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState()
                : Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
        for (int dy = 0; dy < height; dy++) {
            setter.set(new BlockPos(x, y + dy, z), trunk);
        }
        int top = y + height - 2;
        setter.set(new BlockPos(x + 1, top, z), Blocks.TUFF.defaultBlockState());
        setter.set(new BlockPos(x - 1, top + 1, z), Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
        setter.set(new BlockPos(x, y, z + 1), Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
    }

    private static void crystalTree(int x, int y, int z, int height, long hash, BlockSetter setter) {
        for (int dy = 0; dy < height; dy++) {
            setter.set(new BlockPos(x, y + dy, z), Blocks.CALCITE.defaultBlockState());
        }
        int top = y + height - 1;
        setter.set(new BlockPos(x, top + 1, z), Blocks.AMETHYST_BLOCK.defaultBlockState());
        setter.set(new BlockPos(x + 1, top, z), Blocks.AMETHYST_BLOCK.defaultBlockState());
        setter.set(new BlockPos(x - 1, top, z), Blocks.TINTED_GLASS.defaultBlockState());
        setter.set(new BlockPos(x, top, z + 1), Blocks.SMOOTH_BASALT.defaultBlockState());
        setter.set(new BlockPos(x, top, z - 1), Blocks.AMETHYST_BLOCK.defaultBlockState());
    }

    private static void drownedTree(int x, int y, int z, int height, long hash, BlockSetter setter) {
        BlockState trunk = ((hash & 2L) == 0L)
                ? Blocks.DARK_OAK_LOG.defaultBlockState()
                : Blocks.SPRUCE_LOG.defaultBlockState();
        for (int dy = 0; dy < height; dy++) {
            setter.set(new BlockPos(x, y + dy, z), trunk);
        }
        setter.set(new BlockPos(x + 1, y, z), Blocks.MUD.defaultBlockState());
        setter.set(new BlockPos(x, y, z + 1), Blocks.ROOTED_DIRT.defaultBlockState());
        setter.set(new BlockPos(x, y + height, z), Blocks.ROOTED_DIRT.defaultBlockState());
    }

    private static void needleTree(
            int x,
            int y,
            int z,
            int height,
            BlockState trunk,
            BlockState accent,
            long hash,
            BlockSetter setter
    ) {
        for (int dy = 0; dy < height; dy++) {
            setter.set(new BlockPos(x, y + dy, z), dy % 5 == 0 ? accent : trunk);
            if (dy < 2) {
                setter.set(new BlockPos(x + 1, y + dy, z), trunk);
            }
        }
        setter.set(new BlockPos(x, y + height, z), accent);
    }

    private static void prismTree(int x, int y, int z, int height, long hash, BlockSetter setter) {
        for (int dy = 0; dy < height; dy++) {
            setter.set(new BlockPos(x, y + dy, z), Blocks.PRISMARINE.defaultBlockState());
        }
        int top = y + height;
        setter.set(new BlockPos(x, top, z), Blocks.SEA_LANTERN.defaultBlockState());
        setter.set(new BlockPos(x + 1, top - 1, z), Blocks.TINTED_GLASS.defaultBlockState());
        setter.set(new BlockPos(x - 1, top, z), Blocks.TINTED_GLASS.defaultBlockState());
        setter.set(new BlockPos(x, top - 1, z + 1), Blocks.PRISMARINE.defaultBlockState());
        setter.set(new BlockPos(x, top, z - 1), Blocks.SEA_LANTERN.defaultBlockState());
    }

    private static void mushroomTree(int x, int y, int z, int height, long hash, BlockSetter setter) {
        for (int dy = 0; dy < height; dy++) {
            setter.set(new BlockPos(x, y + dy, z), Blocks.MUSHROOM_STEM.defaultBlockState());
        }
        BlockState cap = (hash & 1L) == 0L
                ? Blocks.RED_MUSHROOM_BLOCK.defaultBlockState()
                : Blocks.BROWN_MUSHROOM_BLOCK.defaultBlockState();
        int top = y + height;
        for (int dz = -2; dz <= 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                if (Math.abs(dx) + Math.abs(dz) <= 3) {
                    setter.set(new BlockPos(x + dx, top, z + dz), cap);
                }
            }
        }
        setter.set(new BlockPos(x + 1, y, z), Blocks.ROOTED_DIRT.defaultBlockState());
        setter.set(new BlockPos(x, y, z + 1), Blocks.MYCELIUM.defaultBlockState());
    }

    private static void rootFan(int x, int y, int z, DrugId drugId, long hash, BlockSetter setter) {
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
            setter.set(new BlockPos(x + dx, y - 1, z + dz), root);
            if (((hash >>> (i * 3)) & 1L) == 0L) {
                setter.set(new BlockPos(x + dx * 2, y - 1, z + dz * 2), root);
            }
        }
    }

    private interface BlockSetter {
        void set(BlockPos pos, BlockState state);
    }
}
