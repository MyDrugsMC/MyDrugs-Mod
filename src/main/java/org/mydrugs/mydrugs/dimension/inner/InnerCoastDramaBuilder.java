package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

final class InnerCoastDramaBuilder {
    // Demoted coast-drama density (was 0.11 + intensity*0.10 / 0.24 satellite).
    private static final double COAST_BASE_CHANCE = 0.05D;
    private static final double COAST_INTENSITY_CHANCE = 0.05D;
    private static final double SATELLITE_CROWN_CHANCE = 0.12D;
    private static final int MARGIN = 2;

    private InnerCoastDramaBuilder() {
    }

    static boolean rejectsSanctuaryForTest() {
        return !isCoastCandidate(InnerTerrain.sample(0, 0, 0, 0));
    }

    static void place(InnerBlockSink sink, InnerChunkSampleCache cache, int minX, int minZ) {
        int islandCenterX = cache.islandCenterX();
        int islandCenterZ = cache.islandCenterZ();
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        int placed = 0;
        for (int worldZ = minZ - MARGIN; worldZ < minZ + 16 + MARGIN && placed < 6; worldZ++) {
            if (!InnerChunkSampleCache.chunkLocalCandidate(worldZ, 2, 16, 4)) {
                continue;
            }
            for (int worldX = minX - MARGIN; worldX < minX + 16 + MARGIN && placed < 6; worldX++) {
                if (!InnerChunkSampleCache.chunkLocalCandidate(worldX, 2, 16, 4)) {
                    continue;
                }
                InnerTerrain.Sample sample = cache.sampleAt(worldX, worldZ);
                if (!isCoastCandidate(sample)) {
                    continue;
                }
                InnerGroveSample grove = cache.groveAt(worldX, worldZ, sample);
                InnerSceneSample scene = cache.sceneAt(worldX, worldZ, sample, grove);
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
                    buildSatelliteCrown(worldX, worldZ, sample, hash, sink);
                } else {
                    buildCoastFragment(islandCenterX, islandCenterZ, worldX, worldZ, sample, hash, sink);
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
            InnerBlockSink sink
    ) {
        DrugId drugId = sample.chooseFeatureDrug(hash);
        BlockState debris = debrisState(drugId);
        BlockState hanging = hangingState(drugId);
        double angle = Math.atan2(worldZ - centerZ, worldX - centerX);
        int outX = (int) Math.round(Math.cos(angle) * 2.0D);
        int outZ = (int) Math.round(Math.sin(angle) * 2.0D);
        BlockPos top = new BlockPos(worldX, sample.topY(), worldZ);
        sink.setBlock(top.above(), debris, true);
        if ((hash & 3L) == 0L) {
            sink.setBlock(top.offset(outX, 2 + (int) (hash & 1L), outZ), debris, true);
        }
        for (int drop = 1; drop <= 3 + (int) ((hash >>> 4) & 1L); drop++) {
            sink.setBlock(top.below(drop), hanging, true);
        }
        if (((hash >>> 8) & 7L) == 0L) {
            sink.setBlock(top.offset(outX * 2, 1, outZ * 2), InnerGlowBuilder.glowStateFor(drugId), true);
        }
    }

    private static void buildSatelliteCrown(int worldX, int worldZ, InnerTerrain.Sample sample, long hash, InnerBlockSink sink) {
        DrugId drugId = sample.chooseFeatureDrug(hash);
        BlockState base = debrisState(drugId);
        BlockState glow = drugId == DrugId.COFFEE
                ? Blocks.GLOWSTONE.defaultBlockState()
                : InnerGlowBuilder.glowStateFor(drugId);
        int height = 4 + (int) (hash & 3L);
        int y = sample.topY() + 1;
        for (int dy = 0; dy < height; dy++) {
            sink.setBlock(new BlockPos(worldX, y + dy, worldZ), dy == height - 1 ? glow : base, true);
        }
        for (int i = 0; i < 4; i++) {
            int dx = i == 0 ? 2 : i == 1 ? -2 : 0;
            int dz = i == 2 ? 2 : i == 3 ? -2 : 0;
            sink.setBlock(new BlockPos(worldX + dx, y, worldZ + dz), base, true);
            if (((hash >>> (i + 6)) & 1L) == 0L) {
                sink.setBlock(new BlockPos(worldX + dx, y + 1, worldZ + dz), coastPlant(drugId), true);
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
}
