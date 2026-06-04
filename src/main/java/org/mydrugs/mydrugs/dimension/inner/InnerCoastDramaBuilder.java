package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

final class InnerCoastDramaBuilder {
    // Demoted coast-drama density (was 0.11 + intensity*0.10 / 0.24 satellite).
    private static final double COAST_BASE_CHANCE = 0.05D;
    private static final double COAST_INTENSITY_CHANCE = 0.05D;
    private static final double SATELLITE_CROWN_CHANCE = 0.12D;

    private InnerCoastDramaBuilder() {
    }

    static void placeInitialCoastDrama(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        placeCoastDrama(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), (pos, state) -> {
            if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
                return;
            }
            if (!chunkPos.equals(new ChunkPos(pos))) {
                return;
            }
            chunk.setBlockState(pos, state, 2);
        });
    }

    static void placeOverlayCoastDrama(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeCoastDrama(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
                (pos, state) -> InnerPlacement.safeSet(level, pos, state, true, count));
    }

    static boolean rejectsSanctuaryForTest() {
        return !isCoastCandidate(InnerTerrain.sample(0, 0, 0, 0));
    }

    private static void placeCoastDrama(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        int islandCenterX = InnerTerrain.slotCenter(minX + 8);
        int islandCenterZ = InnerTerrain.slotCenter(minZ + 8);
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        int placed = 0;
        for (int localZ = 2; localZ < 16 && placed < 6; localZ += 4) {
            for (int localX = 2; localX < 16 && placed < 6; localX += 4) {
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                InnerTerrain.Sample sample = cache.sample(localX, localZ);
                if (!isCoastCandidate(sample)) {
                    continue;
                }
                InnerGroveSample grove = InnerGroveSampler.sample(seed, islandCenterX, islandCenterZ, worldX, worldZ, sample);
                InnerSceneSample scene = InnerSceneSampler.sample(seed, islandCenterX, islandCenterZ, worldX, worldZ, sample, grove);
                long hash = InnerNoise.mix64(seed
                        ^ (long) worldX * 0x59BD_31C3L
                        ^ (long) worldZ * 0x33C7_513FL);
                // Demoted: manufactured coastlines are the least natural feature, so lean on the
                // domain warp + coastWarp/cliffBreak in computeSample for coastline interest instead.
                double chance = sample.satellite()
                        ? SATELLITE_CROWN_CHANCE
                        : COAST_BASE_CHANCE + scene.intensity() * COAST_INTENSITY_CHANCE;
                if ((hash & 1023L) >= chance * 1024.0D) {
                    continue;
                }
                if (sample.satellite() || scene.type() == InnerSceneType.SATELLITE_CROWN) {
                    buildSatelliteCrown(worldX, worldZ, sample, hash, setter);
                } else {
                    buildCoastFragment(islandCenterX, islandCenterZ, worldX, worldZ, sample, hash, setter);
                }
                placed++;
            }
        }
    }

    private static boolean isCoastCandidate(InnerTerrain.Sample sample) {
        return sample.land()
                && !sample.hole()
                && sample.pathStrength() < 0.50D
                && sample.distanceFromCenter() > InnerDimensionConstants.CORE_RADIUS + 120.0D
                && (sample.satellite()
                || sample.distanceFromCenter() > InnerDimensionConstants.ISLAND_RADIUS - 210.0D
                || (sample.density() > 0.0D && sample.density() < 82.0D));
    }

    private static void buildCoastFragment(
            int centerX,
            int centerZ,
            int worldX,
            int worldZ,
            InnerTerrain.Sample sample,
            long hash,
            BlockSetter setter
    ) {
        DrugId drugId = sample.chooseFeatureDrug(hash);
        BlockState debris = debrisState(drugId);
        BlockState hanging = hangingState(drugId);
        double angle = Math.atan2(worldZ - centerZ, worldX - centerX);
        int outX = (int) Math.round(Math.cos(angle) * 2.0D);
        int outZ = (int) Math.round(Math.sin(angle) * 2.0D);
        BlockPos top = new BlockPos(worldX, sample.topY(), worldZ);
        setter.set(top.above(), debris);
        if ((hash & 3L) == 0L) {
            setter.set(top.offset(outX, 2 + (int) (hash & 1L), outZ), debris);
        }
        for (int drop = 1; drop <= 3 + (int) ((hash >>> 4) & 1L); drop++) {
            setter.set(top.below(drop), hanging);
        }
        if (((hash >>> 8) & 7L) == 0L) {
            setter.set(top.offset(outX * 2, 1, outZ * 2), InnerGlowBuilder.glowStateFor(drugId));
        }
    }

    private static void buildSatelliteCrown(int worldX, int worldZ, InnerTerrain.Sample sample, long hash, BlockSetter setter) {
        DrugId drugId = sample.chooseFeatureDrug(hash);
        BlockState base = debrisState(drugId);
        BlockState glow = drugId == DrugId.COFFEE
                ? Blocks.GLOWSTONE.defaultBlockState()
                : InnerGlowBuilder.glowStateFor(drugId);
        int height = 4 + (int) (hash & 3L);
        int y = sample.topY() + 1;
        for (int dy = 0; dy < height; dy++) {
            setter.set(new BlockPos(worldX, y + dy, worldZ), dy == height - 1 ? glow : base);
        }
        for (int i = 0; i < 4; i++) {
            int dx = i == 0 ? 2 : i == 1 ? -2 : 0;
            int dz = i == 2 ? 2 : i == 3 ? -2 : 0;
            setter.set(new BlockPos(worldX + dx, y, worldZ + dz), base);
            if (((hash >>> (i + 6)) & 1L) == 0L) {
                setter.set(new BlockPos(worldX + dx, y + 1, worldZ + dz), coastPlant(drugId));
            }
        }
    }

    private static BlockState debrisState(DrugId drugId) {
        return switch (drugId) {
            case TOBACCO -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            case WEED -> Blocks.MOSS_BLOCK.defaultBlockState();
            case HASH -> Blocks.CALCITE.defaultBlockState();
            case ALCOHOL -> Blocks.SMOOTH_BASALT.defaultBlockState();
            case COCAINE -> Blocks.SMOOTH_QUARTZ.defaultBlockState();
            case LSD -> Blocks.TINTED_GLASS.defaultBlockState();
            case METH -> Blocks.BLACKSTONE.defaultBlockState();
            case MUSHROOMS -> Blocks.MUSHROOM_STEM.defaultBlockState();
            default -> Blocks.SMOOTH_STONE.defaultBlockState();
        };
    }

    private static BlockState hangingState(DrugId drugId) {
        return switch (drugId) {
            case WEED -> Blocks.ROOTED_DIRT.defaultBlockState();
            case HASH, LSD -> Blocks.CALCITE.defaultBlockState();
            case COCAINE -> Blocks.WHITE_CONCRETE.defaultBlockState();
            case METH -> Blocks.BASALT.defaultBlockState();
            case MUSHROOMS -> Blocks.MYCELIUM.defaultBlockState();
            default -> debrisState(drugId);
        };
    }

    private static BlockState coastPlant(DrugId drugId) {
        return switch (drugId) {
            case WEED -> ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState();
            case HASH -> ModInnerDimensionBlocks.CRYSTAL_SHRUB.get().defaultBlockState();
            case COCAINE, METH -> ModInnerDimensionBlocks.REDLINE_THORN.get().defaultBlockState();
            case LSD -> ModInnerDimensionBlocks.PRISM_LOTUS.get().defaultBlockState();
            case MUSHROOMS -> ModInnerDimensionBlocks.MYCELIAL_ROOT.get().defaultBlockState();
            default -> ModInnerDimensionBlocks.BREATH_GRASS.get().defaultBlockState();
        };
    }

    private interface BlockSetter {
        void set(BlockPos pos, BlockState state);
    }
}
