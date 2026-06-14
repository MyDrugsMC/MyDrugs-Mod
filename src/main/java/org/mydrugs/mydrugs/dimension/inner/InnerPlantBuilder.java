package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.mydrugs.mydrugs.core.drug.DrugId;

public final class InnerPlantBuilder {
    private InnerPlantBuilder() {
    }

    public static void placeInitialPlants(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        placePlants(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), (pos, state) -> {
            if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
                return;
            }
            if (!chunkPos.equals(new ChunkPos(pos))) {
                return;
            }
            chunk.setBlockState(pos, state, 2);
        });
    }

    static void placeOverlayPlants(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placePlants(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
                (pos, state) -> InnerPlacement.safeSet(level, pos, state, false, count));
    }

    static boolean hasFloraIdentityFor(DrugId drugId) {
        InnerFloraPalette flora = InnerTerrainProfile.forDrug(drugId).flora();
        return flora.hasAny() && flora.categoryCount() >= 3;
    }

    private static void placePlants(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        if (!cache.anyLand()) {
            return;
        }
        int islandCenterX = InnerTerrain.slotCenter(minX + 8);
        int islandCenterZ = InnerTerrain.slotCenter(minZ + 8);
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                InnerGroveSample grove = cache.grove(localX, localZ);
                InnerSceneSample scene = cache.scene(localX, localZ);
                if (!canHostPlant(sample, scene)) {
                    continue;
                }
                long hash = InnerNoise.mix64(seed
                        ^ (long) worldX * 0x2C1B_3C6DL
                        ^ (long) worldZ * 0x4AA5_F731L);
                double density = sample.plantDensity()
                        + grove.groundDensity() * 0.75D
                        + sample.wetlandStrength() * 0.42D
                        + sample.shoreStrength() * 0.18D;
                density *= scene.plantMultiplier();
                density *= 1.0D - InnerNoise.clamp01(sample.pathStrength() / 0.90D) * 0.45D;
                if (scene.preserveOpenView()) {
                    density *= 0.62D;
                }
                double chance = InnerNoise.clamp01(0.08D + density * 0.34D);
                if ((hash & 1023L) >= chance * 1024.0D) {
                    continue;
                }
                BlockState plant = plantFor(sample, grove, scene, hash);
                int y = sample.topY() + 1;
                setter.set(new BlockPos(worldX, y, worldZ), plant);
            }
        }
    }

    private static boolean canHostPlant(InnerTerrain.Sample sample, InnerSceneSample scene) {
        return sample.land()
                && !sample.lake()
                && !sample.hole()
                && sample.pathStrength() < (scene.preserveOpenView() ? 0.58D : 0.82D)
                && sample.distanceFromCenter() > InnerDimensionConstants.CORE_RADIUS + 22.0D
                && (sample.plantPatch()
                || sample.shoreStrength() > 0.12D
                || sample.wetlandStrength() > 0.08D
                || scene.type() == InnerSceneType.TRANSITION_GARDEN
                || scene.type() == InnerSceneType.ROOTED_WETLAND);
    }

    private static BlockState plantFor(InnerTerrain.Sample sample, InnerGroveSample grove, InnerSceneSample scene, long hash) {
        DrugId featureDrug = sample.chooseFeatureDrug(hash);
        InnerFloraPalette flora = InnerTerrainProfile.forDrug(featureDrug).flora();
        int roll = (int) (hash & 255L);
        if (sample.transitionZone() && sample.transitionStrength() > 0.24D && roll < 28) {
            return InnerTransitionPalette.floraAccent(sample);
        }
        if (sample.holeStrength() > 0.14D || sample.scarStrength() > 0.55D) {
            return flora.danger(hash >>> 8);
        }
        if (sample.wetlandStrength() > 0.20D || sample.shoreStrength() > 0.24D) {
            return roll < 170 ? flora.reed(hash >>> 8) : flora.flower(hash >>> 8);
        }
        if (scene.preserveOpenView() && roll < 176) {
            return flora.groundCover(hash >>> 8);
        }
        if (grove.groveCore() && roll < 52) {
            return flora.shrub(hash >>> 8);
        }
        if ((scene.glowMultiplier() > 1.35D
                || sample.drugId() == DrugId.LSD
                || sample.drugId() == DrugId.MUSHROOMS
                || sample.drugId() == DrugId.HASH)
                && roll > 224) {
            return flora.glow(hash >>> 8);
        }
        if (roll < 92) {
            return flora.groundCover(hash >>> 8);
        }
        return flora.flower(hash >>> 8);
    }

    private interface BlockSetter {
        void set(BlockPos pos, BlockState state);
    }
}
