package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.mydrugs.mydrugs.core.drug.DrugId;

final class InnerFeatureSampler {
    private InnerFeatureSampler() {
    }

    static InnerFeatureSample sample(
            long seed,
            int centerX,
            int centerZ,
            int worldX,
            int worldZ,
            DrugId drugId,
            double distanceFromCenter,
            double pathStrength,
            double density,
            int terrainTopY,
            double slopeHint,
            double cliffStrength,
            boolean satellite
    ) {
        double coreAccess = distanceFromCenter <= InnerDimensionConstants.CORE_RADIUS + 52.0D ? 0.0D : 1.0D;
        double pathAccess = 1.0D - InnerNoise.clamp01((pathStrength - 0.18D) / 0.42D);
        double landmarkAccess = landmarkAccess(centerX, centerZ, worldX, worldZ, drugId);
        double featureAccess = coreAccess * pathAccess * landmarkAccess;
        double landAccess = density > -80.0D ? 1.0D : 0.0D;

        double lakeNoise = ((InnerNoise.smoothValue(seed + 0x1A4EL + drugId.networkId(), worldX, worldZ, 260.0D) + 1.0D) * 0.5D)
                * 0.68D
                + InnerNoise.ridged(seed + 0x51A7L + drugId.networkId(), worldX, worldZ, 88.0D, 3) * 0.32D;
        double lakeThreshold = lakeThreshold(drugId);
        double lakePresence = InnerNoise.clamp01((lakeNoise - (lakeThreshold - 0.20D)) / (1.20D - lakeThreshold))
                * featureAccess
                * landAccess
                * (satellite ? 0.35D : 1.0D);
        boolean lake = lakePresence > 0.38D;
        double lakeCoreStrength = lake ? InnerNoise.clamp01((lakePresence - 0.55D) / 0.45D) : 0.0D;
        double shoreStrength = lake
                ? InnerNoise.clamp01(1.0D - lakeCoreStrength) * 0.72D
                : InnerNoise.clamp01((lakePresence - 0.13D) / 0.30D);
        double wetlandStrength = wetlandScale(drugId)
                * InnerNoise.clamp01(shoreStrength + lakePresence * 0.35D)
                * featureAccess
                * (lakeCoreStrength > 0.72D ? 0.55D : 1.0D);
        InnerLakeType lakeType = lake ? lakeType(drugId) : InnerLakeType.NONE;
        int lakeDepth = 2 + (int) Math.round(lakePresence * lakeDepthScale(drugId) + lakeCoreStrength * 3.0D);
        int lakeSurfaceY = terrainTopY - 1;
        int lakeFloorY = lakeSurfaceY - lakeDepth;
        long lakeHash = InnerNoise.mix64(seed ^ (long) worldX * 0x63D1_F45BL ^ (long) worldZ * 0x18A7_23CDL);
        boolean lakeIsland = lake && lakeCoreStrength > 0.62D && (lakeHash & 2047L) < 34L;
        boolean lakeCenterpiece = lake && lakeCoreStrength > 0.72D && ((lakeHash >>> 12) & 4095L) < 23L;

        double holeNoise = InnerNoise.ridged(seed + 0x0F01DL + drugId.networkId(), worldX, worldZ, 50.0D, 2) * 0.78D
                + ((InnerNoise.smoothValue(seed + 0x6A11L + drugId.networkId(), worldX, worldZ, 88.0D) + 1.0D) * 0.5D) * 0.22D;
        double holeStrength = InnerNoise.clamp01((holeNoise - holeThreshold(drugId)) / (1.0D - holeThreshold(drugId)))
                * featureAccess
                * landAccess
                * (lake ? 0.15D : 1.0D);
        boolean hole = holeStrength > 0.18D;
        InnerHoleType holeType = hole ? holeType(drugId) : InnerHoleType.NONE;

        double treeCluster = (InnerNoise.smoothValue(seed + 0x7E33L + drugId.networkId(), worldX, worldZ, 92.0D) + 1.0D) * 0.5D;
        double treeDensity = InnerNoise.clamp01((treeCluster - 0.43D) / 0.57D)
                * treeDensityScale(drugId)
                * coreAccess
                * (1.0D - InnerNoise.clamp01(pathStrength / 0.70D) * 0.82D)
                * landmarkAccess
                * (lake || hole ? 0.2D : 1.0D)
                * (shoreStrength > 0.2D ? 1.18D : 1.0D);
        boolean treeZone = treeDensity > 0.08D;

        double spikeNoise = InnerNoise.ridged(seed + 0x5F1AEL + drugId.networkId(), worldX, worldZ, 54.0D, 2);
        double spikeStrength = InnerNoise.clamp01((spikeNoise - spikeThreshold(drugId)) / (1.0D - spikeThreshold(drugId)))
                * spikeDensityScale(drugId)
                * coreAccess
                * pathAccess
                * landmarkAccess
                * (0.45D + cliffStrength * 0.75D + (hole ? holeStrength * 0.65D : 0.0D))
                * (lake ? 0.4D : 1.0D);
        boolean spikeField = spikeStrength > 0.12D;

        double plantNoise = (InnerNoise.smoothValue(seed + 0x91A7L + drugId.networkId(), worldX, worldZ, 34.0D) + 1.0D) * 0.5D;
        double rimBoost = shoreStrength * 0.30D + wetlandStrength * 0.36D;
        double scarBoost = holeStrength > 0.12D ? 0.18D : 0.0D;
        double plantDensity = InnerNoise.clamp01((plantNoise - 0.36D) / 0.64D)
                * plantDensityScale(drugId)
                * coreAccess
                * landmarkAccess
                * (1.0D - InnerNoise.clamp01(pathStrength / 0.85D) * 0.48D);
        plantDensity = InnerNoise.clamp01(plantDensity + rimBoost + scarBoost);
        boolean plantPatch = plantDensity > 0.08D;

        return new InnerFeatureSample(
                lake,
                lakePresence,
                lakeCoreStrength,
                shoreStrength,
                wetlandStrength,
                lakeType,
                lakeSurfaceY,
                lakeFloorY,
                lakeIsland,
                lakeCenterpiece,
                hole,
                holeStrength,
                holeType,
                spikeField,
                spikeStrength,
                treeZone,
                treeDensity,
                plantPatch,
                plantDensity,
                cliffStrength,
                slopeHint
        );
    }

    private static double landmarkAccess(int centerX, int centerZ, int worldX, int worldZ, DrugId drugId) {
        if (!InnerRegionMap.hasAngle(drugId)) {
            return 1.0D;
        }
        BlockPos landmark = InnerRegionMap.landmarkFor(centerX, centerZ, drugId);
        double distance = Math.hypot(worldX - landmark.getX(), worldZ - landmark.getZ());
        return InnerNoise.clamp01((distance - 72.0D) / 48.0D);
    }

    private static double lakeThreshold(DrugId drugId) {
        return switch (drugId) {
            case WEED, MUSHROOMS, ALCOHOL -> 0.76D;
            case LSD, HASH -> 0.81D;
            case COFFEE -> 0.86D;
            case METH, COCAINE, TOBACCO -> 0.91D;
            default -> 0.88D;
        };
    }

    private static int lakeDepthScale(DrugId drugId) {
        return switch (drugId) {
            case ALCOHOL, MUSHROOMS -> 6;
            case LSD, METH -> 5;
            default -> 4;
        };
    }

    private static double wetlandScale(DrugId drugId) {
        return switch (drugId) {
            case ALCOHOL -> 1.0D;
            case WEED, MUSHROOMS -> 0.86D;
            case HASH, LSD -> 0.42D;
            case COFFEE -> 0.34D;
            default -> 0.18D;
        };
    }

    private static InnerLakeType lakeType(DrugId drugId) {
        return switch (drugId) {
            case ALCOHOL -> InnerLakeType.MEMORY;
            case WEED, MUSHROOMS, COFFEE -> InnerLakeType.WATER;
            case LSD, HASH -> InnerLakeType.PRISM;
            case METH -> InnerLakeType.MAGMA;
            case TOBACCO, COCAINE -> InnerLakeType.MUD;
            default -> InnerLakeType.SCULK_VOID;
        };
    }

    private static double holeThreshold(DrugId drugId) {
        return switch (drugId) {
            case METH, COCAINE, LSD -> 0.87D;
            case TOBACCO, ALCOHOL, MUSHROOMS -> 0.90D;
            case HASH -> 0.92D;
            default -> 0.94D;
        };
    }

    private static InnerHoleType holeType(DrugId drugId) {
        return switch (drugId) {
            case COCAINE, METH, TOBACCO -> InnerHoleType.SCAR_SHAFT;
            case MUSHROOMS -> InnerHoleType.ROOT_TUNNEL;
            case ALCOHOL -> InnerHoleType.MEMORY_SINK;
            case LSD, HASH -> InnerHoleType.PRISM_WELL;
            default -> InnerHoleType.COLLAPSE;
        };
    }

    private static double treeDensityScale(DrugId drugId) {
        return switch (drugId) {
            case WEED -> 0.92D;
            case MUSHROOMS -> 0.88D;
            case COFFEE -> 0.76D;
            case ALCOHOL -> 0.54D;
            case HASH -> 0.42D;
            case LSD -> 0.34D;
            case TOBACCO -> 0.30D;
            case COCAINE, METH -> 0.20D;
            default -> 0.26D;
        };
    }

    private static double spikeThreshold(DrugId drugId) {
        return switch (drugId) {
            case METH, COCAINE, LSD, HASH -> 0.78D;
            case TOBACCO, ALCOHOL, MUSHROOMS -> 0.83D;
            default -> 0.89D;
        };
    }

    private static double spikeDensityScale(DrugId drugId) {
        return switch (drugId) {
            case METH -> 1.18D;
            case COCAINE -> 1.06D;
            case LSD, HASH -> 0.92D;
            case TOBACCO -> 0.74D;
            case ALCOHOL, MUSHROOMS -> 0.58D;
            default -> 0.34D;
        };
    }

    private static double plantDensityScale(DrugId drugId) {
        return switch (drugId) {
            case WEED, MUSHROOMS -> 0.96D;
            case COFFEE, ALCOHOL -> 0.72D;
            case HASH -> 0.62D;
            case COCAINE, METH -> 0.48D;
            default -> 0.56D;
        };
    }
}
