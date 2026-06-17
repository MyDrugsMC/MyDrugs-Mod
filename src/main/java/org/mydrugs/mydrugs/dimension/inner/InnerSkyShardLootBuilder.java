package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;

public final class InnerSkyShardLootBuilder {
    public static final ResourceKey<LootTable> SKY_SHRINE = ResourceKey.create(
            Registries.LOOT_TABLE,
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "chests/inner_sky_shrine")
    );
    private static final long SHRINE_SALT = 0x5348_5249_4E45L;

    private InnerSkyShardLootBuilder() {
    }

    static void placeRewards(
            ServerLevel level,
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeRewards(level, data, island, chunkPos, cache, count, InnerPlacement.PlacementMode.LIVE_OVERLAY);
    }

    static void placeRewards(
            ServerLevel level,
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count,
            InnerPlacement.PlacementMode mode
    ) {
        if (!cache.anySkyLand()) {
            return;
        }
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                InnerSkyShardSample sky = cache.sample(localX, localZ).sky();
                if (!sky.land() || sky.strength() < 0.985D) {
                    continue;
                }
                int x = minX + localX;
                int z = minZ + localZ;
                if (!isLocalMaximum(island, x, z, sky.strength())) {
                    continue;
                }
                long hash = InnerNoise.mix64(cache.seed() + SHRINE_SALT
                        ^ (long) x * 0x9E37_79B9L
                        ^ (long) z * 0x85EB_CA6BL);
                if (Math.floorMod(hash, 8L) != 0L) {
                    continue;
                }
                String marker = "sky_shrine:" + x + ":" + z;
                if (island.hasMarker(marker)) {
                    continue;
                }
                BlockPos base = new BlockPos(x, sky.topY() + 1, z);
                InnerPlacement.safeSetStructural(level, base,
                        InnerTerrainProfile.forDrug(sky.drug()).nodeState(), true, count, mode);
                BlockPos chest = base.above();
                if (!InnerPlacement.safeSetStructural(level, chest, Blocks.CHEST.defaultBlockState(), true, count, mode)) {
                    continue;
                }
                RandomizableContainer.setBlockEntityLootTable(level, level.getRandom(), chest, SKY_SHRINE);
                data.markStructurePlaced(island.owner(), marker);
            }
        }
    }

    static boolean isShrineChest(
            InnerDimensionSavedData.IslandState island,
            BlockPos chestPos
    ) {
        InnerSkyShardSample sky = InnerTerrain.sample(
                island.centerX(),
                island.centerZ(),
                chestPos.getX(),
                chestPos.getZ()
        ).sky();
        return sky.land()
                && chestPos.getY() == sky.topY() + 2
                && island.hasMarker("sky_shrine:" + chestPos.getX() + ":" + chestPos.getZ());
    }

    private static boolean isLocalMaximum(
            InnerDimensionSavedData.IslandState island,
            int x,
            int z,
            double strength
    ) {
        int[][] neighbors = {{-1, 0}, {0, -1}, {1, 0}, {0, 1}};
        for (int[] neighbor : neighbors) {
            int nx = x + neighbor[0];
            int nz = z + neighbor[1];
            InnerSkyShardSample other = InnerTerrain.sample(
                    island.centerX(),
                    island.centerZ(),
                    nx,
                    nz
            ).sky();
            if (other.land() && (other.strength() > strength
                    || (other.strength() == strength && (nx < x || nx == x && nz < z)))) {
                return false;
            }
        }
        return true;
    }
}
