package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.mydrugs.mydrugs.core.drug.DrugId;

final class InnerGlowBuilder {
    private InnerGlowBuilder() {
    }

    static void placeInitialGlow(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        placeGlow(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), (pos, state) -> {
            if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
                return;
            }
            if (!chunkPos.equals(new ChunkPos(pos))) {
                return;
            }
            chunk.setBlockState(pos, state, 2);
        });
    }

    static void placeOverlayGlow(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeGlow(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
                (pos, state) -> InnerPlacement.safeSet(level, pos, state, true, count));
    }

    static boolean hasGlowLanguageFor(DrugId drugId) {
        return switch (drugId) {
            case COFFEE, TOBACCO, WEED, HASH, ALCOHOL, COCAINE, LSD, METH, MUSHROOMS -> true;
            default -> false;
        };
    }

    static BlockState glowStateFor(DrugId drugId) {
        return switch (drugId) {
            case COFFEE -> Blocks.LANTERN.defaultBlockState();
            case TOBACCO -> Blocks.MAGMA_BLOCK.defaultBlockState();
            case WEED -> Blocks.SHROOMLIGHT.defaultBlockState();
            case HASH -> Blocks.AMETHYST_BLOCK.defaultBlockState();
            case ALCOHOL -> Blocks.SEA_LANTERN.defaultBlockState();
            case COCAINE -> Blocks.REDSTONE_BLOCK.defaultBlockState();
            case LSD -> Blocks.SEA_LANTERN.defaultBlockState();
            case METH -> Blocks.MAGMA_BLOCK.defaultBlockState();
            case MUSHROOMS -> Blocks.SHROOMLIGHT.defaultBlockState();
            default -> Blocks.GLOWSTONE.defaultBlockState();
        };
    }

    private static void placeGlow(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        int islandCenterX = InnerTerrain.slotCenter(minX + 8);
        int islandCenterZ = InnerTerrain.slotCenter(minZ + 8);
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        int placed = 0;
        for (int localZ = 1; localZ < 16 && placed < 7; localZ += 3) {
            for (int localX = 1; localX < 16 && placed < 7; localX += 3) {
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                if (!canHostGlow(sample)) {
                    continue;
                }
                InnerGroveSample grove = InnerGroveSampler.sample(seed, islandCenterX, islandCenterZ, worldX, worldZ, sample);
                InnerSceneSample scene = InnerSceneSampler.sample(seed, islandCenterX, islandCenterZ, worldX, worldZ, sample, grove);
                long hash = InnerNoise.mix64(seed
                        ^ (long) worldX * 0x1A36_5C89L
                        ^ (long) worldZ * 0x0F5E_2D49L);
                if (!shouldPlaceGlow(sample, scene, hash)) {
                    continue;
                }
                placeGlowAt(worldX, worldZ, sample, scene, hash, setter);
                placed++;
            }
        }
    }

    private static boolean canHostGlow(InnerTerrain.Sample sample) {
        return sample.land()
                && !sample.hole()
                && sample.distanceFromCenter() > InnerDimensionConstants.CORE_RADIUS + 18.0D;
    }

    private static boolean shouldPlaceGlow(InnerTerrain.Sample sample, InnerSceneSample scene, long hash) {
        double chance = 0.010D
                + scene.glowMultiplier() * 0.026D
                + sample.scarStrength() * 0.030D
                + sample.lakeCoreStrength() * 0.026D
                + (sample.transitionZone() ? sample.transitionStrength() * 0.018D : 0.0D);
        if (scene.type() == InnerSceneType.PATH_VISTA || scene.type() == InnerSceneType.LANDMARK_APPROACH) {
            chance += 0.050D;
        }
        if (scene.type() == InnerSceneType.QUIET_FIELD || scene.type() == InnerSceneType.ASH_FLAT) {
            chance *= 0.54D;
        }
        return (hash & 1023L) < chance * 1024.0D;
    }

    private static void placeGlowAt(
            int worldX,
            int worldZ,
            InnerTerrain.Sample sample,
            InnerSceneSample scene,
            long hash,
            BlockSetter setter
    ) {
        DrugId drugId = sample.chooseFeatureDrug(hash);
        BlockState glow = sample.transitionZone() && sample.transitionStrength() > 0.35D
                ? InnerTransitionPalette.glowAccent(sample)
                : glowStateFor(drugId);
        int y = sample.lake() ? sample.topY() : sample.topY() + 1;
        BlockPos pos = new BlockPos(worldX, y, worldZ);
        Block block = glow.getBlock();
        if (block == Blocks.LANTERN || block == Blocks.SOUL_LANTERN || block == Blocks.REDSTONE_TORCH) {
            setter.set(pos.below(), scene.type() == InnerSceneType.PATH_VISTA
                    ? sample.profile().pathBlock()
                    : sample.profile().accentBlock());
            setter.set(pos, glow);
            return;
        }
        if (sample.pathStrength() > 0.48D && scene.type() != InnerSceneType.PATH_VISTA) {
            setter.set(pos, sample.profile().pathBlock());
            return;
        }
        setter.set(pos, glow);
    }

    private interface BlockSetter {
        void set(BlockPos pos, BlockState state);
    }
}
