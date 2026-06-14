package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.mydrugs.mydrugs.core.drug.DrugId;

final class InnerGroveSampler {
    private static final int CELL_SIZE = 56;
    private static final long CENTER_SALT = 0x5197_0F0DL;
    private static final long HERO_SALT = 0x4845_524FL;

    private InnerGroveSampler() {
    }

    static InnerGroveSample sample(
            long seed,
            int islandCenterX,
            int islandCenterZ,
            int worldX,
            int worldZ,
            InnerTerrain.Sample terrain
    ) {
        InnerGenerationProfiler.countGrove();
        if (!canHostGrove(terrain)) {
            return empty(terrain);
        }

        int cellX = Math.floorDiv(worldX, CELL_SIZE);
        int cellZ = Math.floorDiv(worldZ, CELL_SIZE);
        double best = 0.0D;
        double bestRadius = 1.0D;
        long bestHash = 0L;
        DrugId bestDrug = terrain.primaryDrug();

        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                int cx = cellX + dx;
                int cz = cellZ + dz;
                long cellHash = InnerNoise.mix64(seed + CENTER_SALT + (long) cx * 341873128712L + (long) cz * 132897987541L);
                double jitterX = InnerNoise.value(seed + CENTER_SALT, cx, cz) * 18.0D;
                double jitterZ = InnerNoise.value(seed + CENTER_SALT + 19L, cx, cz) * 18.0D;
                double centerX = cx * CELL_SIZE + CELL_SIZE * 0.5D + jitterX;
                double centerZ = cz * CELL_SIZE + CELL_SIZE * 0.5D + jitterZ;
                DrugId featureDrug = terrain.chooseFeatureDrug(cellHash);
                double radius = groveRadius(featureDrug, cellHash);
                double distance = Math.hypot(worldX - centerX, worldZ - centerZ);
                double strength = InnerNoise.clamp01(1.0D - distance / radius);
                strength *= 0.72D + 0.28D * ((cellHash >>> 19) & 255L) / 255.0D;
                if (strength > best) {
                    best = strength;
                    bestRadius = radius;
                    bestHash = cellHash;
                    bestDrug = featureDrug;
                }
            }
        }

        double landmarkAccess = landmarkAccess(islandCenterX, islandCenterZ, worldX, worldZ, terrain.primaryDrug());
        best *= landmarkAccess;
        boolean inGrove = best > 0.12D;
        boolean core = best > 0.58D;
        boolean edge = inGrove && !core;
        double scale = groveDensityScale(bestDrug);
        boolean heroCandidate = core
                && canHostMajorFeature(terrain)
                && ((InnerNoise.mix64(bestHash + HERO_SALT) & 4095L) < 220L)
                && bestRadius >= 36.0D;

        return new InnerGroveSample(
                inGrove,
                core,
                edge,
                best,
                InnerNoise.clamp01(best * scale),
                InnerNoise.clamp01((best * 0.76D + terrain.plantDensity() * 0.36D) * (0.85D + scale * 0.35D)),
                InnerNoise.clamp01((best * 0.84D + terrain.wetlandStrength() * 0.38D) * plantGroundScale(bestDrug)),
                heroCandidate,
                terrain.primaryDrug(),
                terrain.secondaryDrug(),
                terrain.secondaryWeight(),
                InnerTreeArchetype.forDrug(bestDrug)
        );
    }

    static boolean hasTreeArchetype(DrugId drugId) {
        return InnerTreeArchetype.forDrug(drugId) != null;
    }

    static boolean canHostMajorFeature(InnerTerrain.Sample sample) {
        return sample.land()
                && !sample.lake()
                && !sample.hole()
                && sample.pathStrength() < 0.34D
                && sample.distanceFromCenter() > InnerDimensionConstants.CORE_RADIUS + 84.0D;
    }

    static boolean rejectsSanctuaryForTest() {
        return !canHostMajorFeature(InnerTerrain.sample(0, 0, 0, 0));
    }

    private static boolean canHostGrove(InnerTerrain.Sample sample) {
        return sample.land()
                && !sample.hole()
                && !sample.lake()
                && sample.pathStrength() < 0.48D
                && sample.distanceFromCenter() > InnerDimensionConstants.CORE_RADIUS + 56.0D
                && (sample.treeZone() || sample.plantPatch() || sample.shoreStrength() > 0.18D);
    }

    private static InnerGroveSample empty(InnerTerrain.Sample terrain) {
        return new InnerGroveSample(
                false,
                false,
                false,
                0.0D,
                0.0D,
                0.0D,
                0.0D,
                false,
                terrain.primaryDrug(),
                terrain.secondaryDrug(),
                terrain.secondaryWeight(),
                InnerTreeArchetype.forDrug(terrain.primaryDrug())
        );
    }

    private static double groveRadius(DrugId drugId, long hash) {
        double jitter = ((hash >>> 24) & 255L) / 255.0D;
        return switch (drugId) {
            case WEED, MUSHROOMS -> 44.0D + jitter * 30.0D;
            case COFFEE, ALCOHOL -> 36.0D + jitter * 24.0D;
            case HASH, LSD -> 32.0D + jitter * 20.0D;
            case TOBACCO -> 28.0D + jitter * 18.0D;
            case COCAINE, METH -> 24.0D + jitter * 18.0D;
            default -> 30.0D + jitter * 18.0D;
        };
    }

    private static double groveDensityScale(DrugId drugId) {
        return switch (drugId) {
            case WEED -> 1.22D;
            case MUSHROOMS -> 1.16D;
            case COFFEE -> 0.92D;
            case ALCOHOL -> 0.76D;
            case HASH, LSD -> 0.62D;
            case TOBACCO -> 0.52D;
            case COCAINE, METH -> 0.38D;
            default -> 0.58D;
        };
    }

    private static double plantGroundScale(DrugId drugId) {
        return switch (drugId) {
            case WEED, MUSHROOMS -> 1.32D;
            case ALCOHOL -> 1.12D;
            case COFFEE -> 0.98D;
            default -> 0.82D;
        };
    }

    private static double landmarkAccess(int centerX, int centerZ, int worldX, int worldZ, DrugId drugId) {
        if (!InnerRegionMap.hasAngle(drugId)) {
            return 1.0D;
        }
        BlockPos landmark = InnerRegionMap.landmarkFor(centerX, centerZ, drugId);
        double distance = Math.hypot(worldX - landmark.getX(), worldZ - landmark.getZ());
        return InnerNoise.clamp01((distance - 54.0D) / 54.0D);
    }
}
