package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

final class InnerDecorator {
    private InnerDecorator() {
    }

    static void decoratePathChunk(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        for (int localZ = 0; localZ < 16; localZ += 2) {
            for (int localX = 0; localX < 16; localX += 2) {
                int x = minX + localX;
                int z = minZ + localZ;
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                if (!sample.land() || sample.pathStrength() < 0.55D) {
                    continue;
                }
                long hash = InnerNoise.mix64(InnerTerrain.seedForSlot(InnerTerrain.slotCenter(x), InnerTerrain.slotCenter(z))
                        ^ (long) x * 31L ^ (long) z * 17L);
                if ((hash & 15L) == 0L) {
                    BlockPos top = InnerPlacement.surfaceTop(level, x, z);
                    InnerPlacement.safeSet(level, top, sample.profile().pathBlock(), true, count);
                    BlockState marker = sample.drugId() == DrugId.LSD
                            ? Blocks.SEA_LANTERN.defaultBlockState()
                            : Blocks.LANTERN.defaultBlockState();
                    InnerPlacement.safeSet(level, top.above(), marker, false, count);
                } else if ((hash & 31L) == 1L) {
                    BlockPos top = InnerPlacement.surfaceTop(level, x, z);
                    InnerPlacement.safeSet(level, top.above(), healingPlantFor(sample.drugId()), false, count);
                }
            }
        }
    }

    static void decorateAmbientChunk(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                if (!sample.land() || sample.lake() || sample.pathStrength() > 0.70D || !sample.plantPatch()) {
                    continue;
                }
                int x = minX + localX;
                int z = minZ + localZ;
                long hash = InnerNoise.mix64(InnerTerrain.seedForSlot(InnerTerrain.slotCenter(x), InnerTerrain.slotCenter(z))
                        ^ x * 0x2C1B3C6DL ^ z * 0x4AA5F731L);
                double chance = 0.10D + sample.plantDensity() * 0.26D;
                if ((hash & 1023L) >= chance * 1024.0D) {
                    continue;
                }
                BlockPos top = new BlockPos(x, sample.topY(), z);
                InnerPlacement.safeSet(level, top.above(), ambientPlantFor(sample, hash), false, count);
            }
        }
    }

    static void decorateRegionAwakening(
            ServerLevel level,
            ChunkPos chunkPos,
            DrugId drugId,
            boolean unlocked,
            int integratedCount,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        InnerTerrainProfile profile = InnerTerrainProfile.forDrug(drugId);
        // Healing brings order through overlay decoration without changing the pure terrain shape.
        double order = unlocked ? InnerNoise.clamp01(integratedCount / 9.0D) : 0.0D;
        for (int localZ = 1; localZ < 16; localZ += 3) {
            for (int localX = 1; localX < 16; localX += 3) {
                int x = minX + localX;
                int z = minZ + localZ;
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                if (!sample.land() || sample.drugId() != drugId) {
                    continue;
                }
                long hash = InnerNoise.mix64(InnerTerrain.seedForSlot(InnerTerrain.slotCenter(x), InnerTerrain.slotCenter(z))
                        + x * 73428767L + z * 912931L + drugId.networkId());
                int roll = (int) (hash & 1023L);
                BlockPos top = InnerPlacement.surfaceTop(level, x, z);
                if (sample.scar() && (drugId == DrugId.COCAINE || drugId == DrugId.METH)) {
                    // Thorns thin out as the region heals; some scar cells turn to calming growth.
                    double thornChance = 0.25D * (1.0D - 0.7D * order);
                    if (roll < thornChance * 1024.0D) {
                        InnerPlacement.safeSet(level, top.above(), ModInnerDimensionBlocks.REDLINE_THORN.get().defaultBlockState(), false, count);
                        continue;
                    }
                    if (unlocked && roll < (thornChance + 0.20D * order) * 1024.0D) {
                        InnerPlacement.safeSet(level, top.above(), healingPlantFor(drugId), false, count);
                        continue;
                    }
                }
                if (sample.scar() && unlocked && roll < (0.125D + 0.20D * order) * 1024.0D) {
                    InnerPlacement.safeSet(level, top.above(), healingPlantFor(drugId), false, count);
                    continue;
                }
                double plantChance = 0.0625D + 0.10D * order;
                double accentChance = unlocked ? 0.015D + 0.05D * order : 0.0D;
                if (roll < plantChance * 1024.0D) {
                    InnerPlacement.safeSet(level, top.above(), plantFromProfile(profile, hash), false, count);
                } else if (accentChance > 0.0D && roll >= 1024.0D - accentChance * 1024.0D) {
                    InnerPlacement.safeSet(level, top.above(), profile.accentBlock(), true, count);
                }
            }
        }
    }

    private static BlockState plantFromProfile(InnerTerrainProfile profile, long hash) {
        var plants = profile.plantStates();
        if (plants.isEmpty()) {
            return ModInnerDimensionBlocks.BREATH_GRASS.get().defaultBlockState();
        }
        int index = Math.floorMod((int) hash, plants.size());
        return plants.get(index);
    }

    private static BlockState ambientPlantFor(InnerTerrain.Sample sample, long hash) {
        if (sample.lakeStrength() > 0.08D) {
            return switch (sample.drugId()) {
                case ALCOHOL -> ModInnerDimensionBlocks.MEMORY_REEDS.get().defaultBlockState();
                case WEED, HASH -> ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState();
                case MUSHROOMS -> ModInnerDimensionBlocks.MYCELIAL_ROOT.get().defaultBlockState();
                default -> plantFromProfile(sample.profile(), hash);
            };
        }
        if (sample.holeStrength() > 0.14D) {
            return switch (sample.holeType()) {
                case ROOT_TUNNEL -> ModInnerDimensionBlocks.MYCELIAL_ROOT.get().defaultBlockState();
                case SCAR_SHAFT -> ModInnerDimensionBlocks.REDLINE_THORN.get().defaultBlockState();
                case MEMORY_SINK -> ModInnerDimensionBlocks.MEMORY_REEDS.get().defaultBlockState();
                default -> plantFromProfile(sample.profile(), hash);
            };
        }
        return plantFromProfile(sample.profile(), hash);
    }

    private static BlockState healingPlantFor(DrugId drugId) {
        return switch (drugId) {
            case WEED, HASH -> ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState();
            case ALCOHOL -> ModInnerDimensionBlocks.MEMORY_REEDS.get().defaultBlockState();
            case MUSHROOMS -> ModInnerDimensionBlocks.MYCELIAL_ROOT.get().defaultBlockState();
            case COCAINE, METH -> ModInnerDimensionBlocks.REDLINE_THORN.get().defaultBlockState();
            default -> ModInnerDimensionBlocks.BREATH_GRASS.get().defaultBlockState();
        };
    }
}
