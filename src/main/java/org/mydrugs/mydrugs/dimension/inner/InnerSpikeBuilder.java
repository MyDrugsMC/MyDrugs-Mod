package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;

public final class InnerSpikeBuilder {
    private static final int MARGIN = 3;

    private InnerSpikeBuilder() {
    }

    static void place(InnerBlockSink sink, InnerChunkSampleCache cache, int minX, int minZ) {
        int islandCenterX = cache.islandCenterX();
        int islandCenterZ = cache.islandCenterZ();
        for (int worldZ = minZ - MARGIN; worldZ < minZ + 16 + MARGIN; worldZ++) {
            if (!InnerChunkSampleCache.chunkLocalCandidate(worldZ, 3, 13, 3)) {
                continue;
            }
            for (int worldX = minX - MARGIN; worldX < minX + 16 + MARGIN; worldX++) {
                if (!InnerChunkSampleCache.chunkLocalCandidate(worldX, 3, 13, 3)) {
                    continue;
                }
                InnerTerrain.Sample sample = cache.sampleAt(worldX, worldZ);
                if (!canHostSpike(sample)) {
                    continue;
                }
                InnerGroveSample grove = cache.groveAt(worldX, worldZ, sample);
                InnerSceneSample scene = cache.sceneAt(worldX, worldZ, sample, grove);
                // Spikes are the tallest scatter feature: keep them out of vista/approach framing
                // and out of the core sightline wedges.
                if (scene.vista()
                        || scene.landmarkApproach()
                        || InnerRegionMap.inCoreSightlineWedge(islandCenterX, islandCenterZ, worldX, worldZ)) {
                    continue;
                }
                long hash = InnerNoise.mix64(InnerTerrain.seedForSlot(InnerTerrain.slotCenter(worldX), InnerTerrain.slotCenter(worldZ))
                        ^ worldX * 0x6C8E9CF5L ^ worldZ * 0x2A88D351L);
                double chance = sample.spikeStrength() * 118.0D + (sample.holeStrength() > 0.12D ? 18.0D : 0.0D);
                if ((hash & 1023L) >= chance) {
                    continue;
                }
                buildSpike(worldX, sample.topY() + 1, worldZ, sample, hash, sink);
            }
        }
    }

    private static boolean canHostSpike(InnerTerrain.Sample sample) {
        return sample.land()
                && !sample.lake()
                && sample.pathStrength() < 0.30D
                && sample.distanceFromCenter() > InnerDimensionConstants.CORE_RADIUS + 54.0D
                && (sample.spikeField() || sample.holeStrength() > 0.10D || sample.satellite());
    }

    private static void buildSpike(
            int x,
            int y,
            int z,
            InnerTerrain.Sample sample,
            long hash,
            InnerBlockSink sink
    ) {
        int height = spikeHeight(sample.drugId(), sample.spikeStrength(), hash);
        int leanX = ((hash >>> 8) & 1L) == 0L ? 0 : (((hash >>> 9) & 1L) == 0L ? -1 : 1);
        int leanZ = ((hash >>> 10) & 1L) == 0L ? 0 : (((hash >>> 11) & 1L) == 0L ? -1 : 1);
        if ((hash & 63L) != 0L) {
            leanX = 0;
            leanZ = 0;
        }
        for (int dy = 0; dy < height; dy++) {
            int radius = radiusAt(height, dy);
            int offsetX = leanX == 0 ? 0 : dy / 6 * leanX;
            int offsetZ = leanZ == 0 ? 0 : dy / 6 * leanZ;
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    if (dx * dx + dz * dz > radius * radius) {
                        continue;
                    }
                    sink.setBlock(new BlockPos(x + offsetX + dx, y + dy, z + offsetZ + dz),
                            material(sample.drugId(), dy, height, hash), true);
                }
            }
        }
    }

    private static int spikeHeight(DrugId drugId, double spikeStrength, long hash) {
        int jitter = (int) ((hash >>> 16) & 7L);
        int base = switch (drugId) {
            case METH -> 10;
            case COCAINE, LSD, HASH -> 8;
            case TOBACCO, ALCOHOL, MUSHROOMS -> 6;
            default -> 4;
        };
        int scaled = (int) Math.round(spikeStrength * switch (drugId) {
            case METH, COCAINE -> 15.0D;
            case LSD, HASH -> 13.0D;
            default -> 9.0D;
        });
        return Math.min(24, Math.max(3, base + scaled + jitter / 2));
    }

    private static int radiusAt(int height, int dy) {
        if (height < 9) {
            return dy < 2 ? 1 : 0;
        }
        double t = dy / (double) Math.max(1, height - 1);
        if (t < 0.22D) {
            return 2;
        }
        if (t < 0.62D) {
            return 1;
        }
        return 0;
    }

    private static BlockState material(DrugId drugId, int dy, int height, long hash) {
        boolean accent = dy == height - 1 || dy % 7 == 0;
        return switch (drugId) {
            case TOBACCO -> accent
                    ? Blocks.CRACKED_STONE_BRICKS.defaultBlockState()
                    : Blocks.TUFF.defaultBlockState();
            case HASH -> accent
                    ? Blocks.AMETHYST_BLOCK.defaultBlockState()
                    : Blocks.CALCITE.defaultBlockState();
            case COCAINE -> accent
                    ? Blocks.REDSTONE_BLOCK.defaultBlockState()
                    : Blocks.SMOOTH_QUARTZ.defaultBlockState();
            case LSD -> accent
                    ? Blocks.SEA_LANTERN.defaultBlockState()
                    : (((hash + dy) & 1L) == 0L ? Blocks.PRISMARINE.defaultBlockState() : Blocks.TINTED_GLASS.defaultBlockState());
            case METH -> accent
                    ? Blocks.MAGMA_BLOCK.defaultBlockState()
                    : Blocks.BLACKSTONE.defaultBlockState();
            case MUSHROOMS -> accent
                    ? Blocks.RED_MUSHROOM_BLOCK.defaultBlockState()
                    : Blocks.MUSHROOM_STEM.defaultBlockState();
            case ALCOHOL -> accent
                    ? Blocks.MUD.defaultBlockState()
                    : Blocks.DEEPSLATE.defaultBlockState();
            case WEED -> accent
                    ? Blocks.MOSSY_COBBLESTONE.defaultBlockState()
                    : Blocks.MOSS_BLOCK.defaultBlockState();
            default -> accent
                    ? Blocks.BOOKSHELF.defaultBlockState()
                    : Blocks.SMOOTH_STONE.defaultBlockState();
        };
    }
}
