package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

final class InnerPathSceneBuilder {
    // Demoted (was 0.10 / 0.08): keep the route legible but less manicured.
    private static final double PATH_DETAIL_CHANCE = 0.07D;
    private static final double PATH_CORE_BONUS_CHANCE = 0.05D;
    private static final int MARGIN = 5;

    private InnerPathSceneBuilder() {
    }

    static boolean hasDecorationFor(DrugId drugId) {
        return switch (drugId) {
            case COFFEE, TOBACCO, WEED, HASH, ALCOHOL, COCAINE, LSD, METH, MUSHROOMS -> true;
            default -> false;
        };
    }

    static boolean hasArchFor(DrugId drugId) {
        return switch (drugId) {
            case COFFEE, TOBACCO, WEED, HASH, ALCOHOL, COCAINE, LSD, METH, MUSHROOMS -> true;
            default -> false;
        };
    }

    static boolean vistaDeterministicForTest() {
        long seed = InnerTerrain.seedForSlot(0, 0);
        for (int radius = 160; radius < 1100; radius += 8) {
            int x = radius;
            int z = 0;
            InnerTerrain.Sample sample = InnerTerrain.sample(0, 0, x, z);
            boolean first = InnerSceneSampler.isVistaPoint(seed, 0, 0, x, z, sample);
            boolean second = InnerSceneSampler.isVistaPoint(seed, 0, 0, x, z, sample);
            if (first != second) {
                return false;
            }
        }
        return true;
    }

    static boolean rejectsSanctuaryForTest() {
        long seed = InnerTerrain.seedForSlot(0, 0);
        InnerTerrain.Sample center = InnerTerrain.sample(0, 0, 0, 0);
        return !InnerSceneSampler.isVistaPoint(seed, 0, 0, 0, 0, center);
    }

    static void place(InnerBlockSink sink, InnerChunkSampleCache cache, int minX, int minZ) {
        int islandCenterX = cache.islandCenterX();
        int islandCenterZ = cache.islandCenterZ();
        long seed = InnerTerrain.seedForSlot(islandCenterX, islandCenterZ);
        int decorations = 0;
        int arches = 0;
        for (int worldZ = minZ - MARGIN; worldZ < minZ + 16 + MARGIN; worldZ++) {
            if (!InnerChunkSampleCache.chunkLocalCandidate(worldZ, 0, 16, 2)) {
                continue;
            }
            for (int worldX = minX - MARGIN; worldX < minX + 16 + MARGIN; worldX++) {
                if (!InnerChunkSampleCache.chunkLocalCandidate(worldX, 0, 16, 2)) {
                    continue;
                }
                if (decorations >= 10 && arches >= 1) {
                    return;
                }
                InnerTerrain.Sample sample = cache.sampleAt(worldX, worldZ);
                if (!canDecoratePath(sample)) {
                    continue;
                }
                InnerGroveSample grove = cache.groveAt(worldX, worldZ, sample);
                InnerSceneSample scene = cache.sceneAt(worldX, worldZ, sample, grove);
                long hash = InnerNoise.mix64(seed
                        ^ (long) worldX * 0x4B1D_2271L
                        ^ (long) worldZ * 0x2925_39EFL);
                double chance = PATH_DETAIL_CHANCE * scene.pathDetailMultiplier();
                if (sample.pathStrength() > 0.78D) {
                    chance += PATH_CORE_BONUS_CHANCE;
                }
                if ((hash & 1023L) < chance * 1024.0D && decorations < 10) {
                    decorateEdge(seed, islandCenterX, islandCenterZ, worldX, worldZ, sample, scene, hash, sink);
                    decorations++;
                }
                if (scene.type() == InnerSceneType.PATH_VISTA && arches < 1 && shouldBuildArch(hash, sample)) {
                    buildArch(seed, islandCenterX, islandCenterZ, worldX, worldZ, sample, hash, sink);
                    arches++;
                }
                if (scene.type() == InnerSceneType.PATH_VISTA) {
                    clearVista(worldX, sample.topY(), worldZ, sink);
                }
            }
        }
    }

    private static boolean canDecoratePath(InnerTerrain.Sample sample) {
        return sample.land()
                && sample.pathStrength() > 0.52D
                && !sample.hole()
                && sample.distanceFromCenter() > InnerDimensionConstants.CORE_RADIUS + 28.0D;
    }

    private static boolean shouldBuildArch(long hash, InnerTerrain.Sample sample) {
        return sample.pathStrength() > 0.62D
                && sample.distanceFromCenter() > InnerDimensionConstants.CORE_RADIUS + 90.0D
                && (hash & 31L) == 0L;
    }

    private static void decorateEdge(
            long seed,
            int centerX,
            int centerZ,
            int worldX,
            int worldZ,
            InnerTerrain.Sample pathSample,
            InnerSceneSample scene,
            long hash,
            InnerBlockSink sink
    ) {
        DrugId drugId = pathSample.chooseFeatureDrug(hash);
        int offset = scene.type() == InnerSceneType.PATH_VISTA ? 4 : 3;
        for (int side = -1; side <= 1; side += 2) {
            if (((hash >>> (side < 0 ? 3 : 5)) & 1L) == 0L && scene.type() != InnerSceneType.PATH_VISTA) {
                continue;
            }
            BlockPos edge = edgePosition(seed, centerX, centerZ, worldX, worldZ, pathSample, side, offset);
            InnerTerrain.Sample edgeSample = InnerTerrain.sample(centerX, centerZ, edge.getX(), edge.getZ());
            if (!edgeSample.land() || edgeSample.lake() || edgeSample.hole()) {
                continue;
            }
            BlockPos top = new BlockPos(edge.getX(), edgeSample.topY(), edge.getZ());
            BlockState marker = pathSample.transitionZone() && pathSample.transitionStrength() > 0.32D
                    ? InnerTransitionPalette.pathAccent(pathSample)
                    : pathMarker(drugId);
            sink.setBlock(top, marker, true);
            if (((hash >>> 9) & 3L) == 0L || scene.type() == InnerSceneType.PATH_VISTA) {
                sink.setBlock(top.above(), edgePlant(drugId), true);
            }
            if (((hash >>> 13) & 7L) == 0L || scene.type() == InnerSceneType.PATH_VISTA) {
                sink.setBlock(top.above(1), pathGlow(drugId), true);
            }
        }
    }

    private static BlockPos edgePosition(
            long seed,
            int centerX,
            int centerZ,
            int worldX,
            int worldZ,
            InnerTerrain.Sample sample,
            int side,
            int offset
    ) {
        double angle = sample.distanceFromCenter() < 1.0D
                ? InnerRegionMap.angleFor(sample.drugId())
                : Math.atan2(worldZ - centerZ, worldX - centerX);
        double perpX = -Math.sin(angle);
        double perpZ = Math.cos(angle);
        if (sample.pathStrength() > 0.90D && Math.abs(sample.distanceFromCenter() - InnerDimensionConstants.MID_RING_RADIUS) < 16.0D) {
            perpX = Math.cos(angle);
            perpZ = Math.sin(angle);
        }
        int jitter = (int) (Math.abs(InnerNoise.mix64(seed + worldX * 13L + worldZ * 7L)) & 1L);
        return new BlockPos(
                worldX + (int) Math.round(perpX * (offset + jitter) * side),
                sample.topY(),
                worldZ + (int) Math.round(perpZ * (offset + jitter) * side)
        );
    }

    private static void buildArch(
            long seed,
            int centerX,
            int centerZ,
            int worldX,
            int worldZ,
            InnerTerrain.Sample sample,
            long hash,
            InnerBlockSink sink
    ) {
        DrugId drugId = sample.chooseFeatureDrug(hash);
        BlockState arch = archState(drugId);
        for (int side = -1; side <= 1; side += 2) {
            BlockPos edge = edgePosition(seed, centerX, centerZ, worldX, worldZ, sample, side, 3);
            InnerTerrain.Sample edgeSample = InnerTerrain.sample(centerX, centerZ, edge.getX(), edge.getZ());
            if (!edgeSample.land() || edgeSample.lake() || edgeSample.hole()) {
                continue;
            }
            int baseY = edgeSample.topY() + 1;
            for (int dy = 0; dy < 4; dy++) {
                sink.setBlock(new BlockPos(edge.getX(), baseY + dy, edge.getZ()), arch, true);
            }
        }
        int topY = sample.topY() + 5;
        boolean eastWest = Math.abs(worldX - centerX) > Math.abs(worldZ - centerZ);
        for (int i = -3; i <= 3; i++) {
            sink.setBlock(new BlockPos(worldX + (eastWest ? 0 : i), topY, worldZ + (eastWest ? i : 0)), arch, true);
        }
    }

    private static void clearVista(int x, int y, int z, InnerBlockSink sink) {
        for (int dz = -2; dz <= 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                if (Math.abs(dx) + Math.abs(dz) <= 3) {
                    sink.setBlock(new BlockPos(x + dx, y + 1, z + dz), Blocks.AIR.defaultBlockState(), true);
                    sink.setBlock(new BlockPos(x + dx, y + 2, z + dz), Blocks.AIR.defaultBlockState(), true);
                }
            }
        }
    }

    private static BlockState pathMarker(DrugId drugId) {
        return switch (drugId) {
            case COFFEE -> Blocks.SMOOTH_STONE.defaultBlockState();
            case TOBACCO -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            case WEED -> Blocks.MOSS_BLOCK.defaultBlockState();
            case HASH -> Blocks.CALCITE.defaultBlockState();
            case ALCOHOL -> Blocks.MUD.defaultBlockState();
            case COCAINE -> Blocks.SMOOTH_QUARTZ.defaultBlockState();
            case LSD -> Blocks.PRISMARINE.defaultBlockState();
            case METH -> Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            case MUSHROOMS -> Blocks.MYCELIUM.defaultBlockState();
            default -> Blocks.STONE_BRICKS.defaultBlockState();
        };
    }

    private static BlockState edgePlant(DrugId drugId) {
        return switch (drugId) {
            case WEED -> ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState();
            case HASH -> ModInnerDimensionBlocks.CRYSTAL_SHRUB.get().defaultBlockState();
            case ALCOHOL -> ModInnerDimensionBlocks.MEMORY_REEDS.get().defaultBlockState();
            case COCAINE -> ModInnerDimensionBlocks.REDLINE_SPARK_BLOOM.get().defaultBlockState();
            case LSD -> ModInnerDimensionBlocks.PRISM_LOTUS.get().defaultBlockState();
            case METH -> ModInnerDimensionBlocks.REDLINE_THORN.get().defaultBlockState();
            case MUSHROOMS -> ModInnerDimensionBlocks.MYCELIAL_THREADS.get().defaultBlockState();
            default -> ModInnerDimensionBlocks.BREATH_GRASS.get().defaultBlockState();
        };
    }

    private static BlockState pathGlow(DrugId drugId) {
        return switch (drugId) {
            case COFFEE -> Blocks.LANTERN.defaultBlockState();
            case TOBACCO -> Blocks.MAGMA_BLOCK.defaultBlockState();
            default -> InnerGlowBuilder.glowStateFor(drugId);
        };
    }

    private static BlockState archState(DrugId drugId) {
        return switch (drugId) {
            case COFFEE -> Blocks.OAK_LOG.defaultBlockState();
            case TOBACCO -> Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState();
            case WEED -> Blocks.ROOTED_DIRT.defaultBlockState();
            case HASH -> Blocks.CALCITE.defaultBlockState();
            case ALCOHOL -> Blocks.DARK_OAK_LOG.defaultBlockState();
            case COCAINE -> Blocks.SMOOTH_QUARTZ.defaultBlockState();
            case LSD -> Blocks.TINTED_GLASS.defaultBlockState();
            case METH -> Blocks.BLACKSTONE.defaultBlockState();
            case MUSHROOMS -> Blocks.MUSHROOM_STEM.defaultBlockState();
            default -> Blocks.OAK_LOG.defaultBlockState();
        };
    }

    static boolean allRegionDecorationsDefinedForTest() {
        for (DrugId drugId : CuratedDrugChain.ORDER) {
            if (!hasDecorationFor(drugId) || !hasArchFor(drugId)) {
                return false;
            }
        }
        return true;
    }
}
