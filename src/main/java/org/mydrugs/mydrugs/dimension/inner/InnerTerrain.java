package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;

public final class InnerTerrain {
    // A1: low-frequency domain warp. Offsets the sampling position with fBm before any
    // polar math so the wedge/ring/spoke structure stops being a closed-form function of
    // (distance, angle) from one fixed center. Deterministic: derived purely from the slot seed.
    private static final double WARP_SCALE = 360.0D;
    private static final int WARP_OCTAVES = 4;
    private static final double WARP_AMPLITUDE = 135.0D;
    private static final long WARP_SALT_X = 0x5F1D_2A3BL;
    private static final long WARP_SALT_Z = 0x9E77_C4A1L;

    private InnerTerrain() {
    }

    public static Sample sample(int worldX, int worldZ) {
        return sample(slotCenter(worldX), slotCenter(worldZ), worldX, worldZ);
    }

    public static Sample sample(int centerX, int centerZ, int worldX, int worldZ) {
        long seed = seedForSlot(centerX, centerZ);

        // A1: warp the sampling position with low-frequency fBm before computing polar
        // coordinates so every distance/angle-derived feature inherits a wandering, organic
        // shape instead of perfect concentric/radial symmetry.
        double warpX = InnerNoise.fbm(seed + WARP_SALT_X, worldX, worldZ, WARP_SCALE, WARP_OCTAVES) * WARP_AMPLITUDE;
        double warpZ = InnerNoise.fbm(seed + WARP_SALT_Z, worldX, worldZ, WARP_SCALE, WARP_OCTAVES) * WARP_AMPLITUDE;
        double sampleX = worldX + warpX;
        double sampleZ = worldZ + warpZ;

        double dx = sampleX - centerX;
        double dz = sampleZ - centerZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        double angle = Math.atan2(dz, dx);

        DrugId drugId = InnerRegionMap.dominantDrugForAngle(angle);
        InnerTerrainProfile profile = InnerTerrainProfile.forDrug(drugId);

        double coastWarp = InnerNoise.fbm(seed, worldX, worldZ, 240.0D, 4)
                * (74.0D + profile.silhouetteScale() * 30.0D);
        double cliffBreak = InnerNoise.ridged(seed + 13L, worldX, worldZ, 84.0D, 3)
                * (24.0D + profile.ridgeScale() * 22.0D);
        double density = InnerDimensionConstants.ISLAND_RADIUS - distance + coastWarp - cliffBreak;

        double scarStrength = scarStrength(seed, dx, dz, angle, distance) * profile.scarCarve();
        density -= scarStrength * 78.0D;

        double pathStrength = pathStrength(distance, angle, drugId);
        if (pathStrength > 0.0D) {
            density += pathStrength * 84.0D;
        }

        Satellite satellite = satellite(seed, centerX, centerZ, sampleX, sampleZ);
        boolean satelliteLand = satellite.density() > 0.0D;
        boolean mainLand = density > 0.0D;
        boolean path = pathStrength > 0.42D && density > -72.0D;
        boolean land = mainLand || satelliteLand || path;

        double heightNoise = InnerNoise.fbm(seed + 29L, worldX, worldZ, 150.0D, 5);
        double ridge = InnerNoise.ridged(seed + 31L, worldX, worldZ, 72.0D, 3);
        double localSilhouette = silhouette(drugId, seed, worldX, worldZ, distance);
        int topY = InnerDimensionConstants.BASE_Y
                + (int) Math.round(heightNoise * (18.0D + profile.silhouetteScale() * 8.0D))
                + (int) Math.round(ridge * (8.0D + profile.ridgeScale() * 8.0D))
                + (int) Math.round(localSilhouette)
                + profile.heightBias();

        if (path) {
            topY = InnerDimensionConstants.BASE_Y + 4 + (int) Math.round(heightNoise * 3.0D);
        }
        if (satelliteLand && !mainLand) {
            topY = satellite.topY();
        }
        if (distance < InnerDimensionConstants.CORE_RADIUS) {
            topY = InnerDimensionConstants.BASE_Y + 5;
            path = true;
            pathStrength = Math.max(pathStrength, 1.0D);
            scarStrength = 0.0D;
        }

        int thickness = 18 + (int) Math.round(InnerNoise.ridged(seed + 41L, worldX, worldZ, 65.0D, 3) * 24.0D);
        if (path) {
            thickness = Math.max(9, thickness / 2);
        }
        int bottomY = Math.max(InnerDimensionConstants.MIN_Y + 4, topY - thickness);
        return new Sample(
                land,
                topY,
                bottomY,
                drugId,
                path,
                pathStrength,
                scarStrength > 0.42D,
                scarStrength,
                satelliteLand && !mainLand,
                density,
                distance,
                profile
        );
    }

    public static boolean chunkMayHaveLand(int chunkMinX, int chunkMinZ) {
        int centerX = slotCenter(chunkMinX + 8);
        int centerZ = slotCenter(chunkMinZ + 8);
        int farthestX = Math.max(Math.abs(chunkMinX - centerX), Math.abs(chunkMinX + 15 - centerX));
        int farthestZ = Math.max(Math.abs(chunkMinZ - centerZ), Math.abs(chunkMinZ + 15 - centerZ));
        double farthest = Math.sqrt((double) farthestX * farthestX + (double) farthestZ * farthestZ);
        return farthest <= InnerDimensionConstants.ISLAND_RADIUS
                + InnerDimensionConstants.SATELLITE_REACH
                + 160;
    }

    public static BlockState stateFor(Sample sample, int y) {
        if (sample.path() && y == sample.topY()) {
            return sample.profile().pathBlock();
        }
        if (sample.scar() && y >= sample.topY() - 2) {
            return sample.profile().scarBlock();
        }
        if (y == sample.topY()) {
            return sample.profile().surfaceBlock();
        }
        if (y >= sample.topY() - 3) {
            return sample.profile().subsurfaceBlock();
        }
        return sample.profile().deepBlock();
    }

    public static boolean caveAir(Sample sample, int worldX, int y, int worldZ) {
        if (sample.pathStrength() > 0.2D
                || sample.distanceFromCenter() < InnerDimensionConstants.CORE_RADIUS + 40.0D
                || y > sample.topY() - 5
                || y < sample.bottomY() + 4) {
            return false;
        }
        long seed = InnerDimensionConstants.BASE_SEED + sample.drugId().networkId() * 101L;
        double cave = InnerNoise.ridged(seed, worldX, worldZ + y * 5.0D, 46.0D, 3);
        return cave > 0.84D;
    }

    public static int slotCenter(int coordinate) {
        return (int) Math.round((double) coordinate / InnerDimensionConstants.SLOT_SPACING)
                * InnerDimensionConstants.SLOT_SPACING;
    }

    public static long seedForSlot(int centerX, int centerZ) {
        return InnerDimensionConstants.BASE_SEED
                ^ (long) centerX * 341873128712L
                ^ (long) centerZ * 132897987541L;
    }

    public static int safeSpawnY(int centerX, int centerZ) {
        return sample(centerX, centerZ, centerX, centerZ).topY() + 1;
    }

    private static double silhouette(DrugId drugId, long seed, int worldX, int worldZ, double distance) {
        double ridge = InnerNoise.ridged(seed + 501L + drugId.networkId(), worldX, worldZ, 48.0D, 3);
        return switch (drugId) {
            case TOBACCO -> ridge * 11.0D;
            case WEED -> -Math.max(0.0D, ridge - 0.54D) * 6.0D;
            case HASH -> ridge * 8.0D;
            case ALCOHOL -> -Math.max(0.0D, Math.sin(distance / 58.0D)) * 9.0D;
            case COCAINE -> ridge * 14.0D;
            case LSD -> Math.sin(distance / 42.0D) * 8.0D + ridge * 14.0D;
            case METH -> ridge * 16.0D;
            case MUSHROOMS -> Math.sin(distance / 85.0D) * 5.0D;
            default -> ridge * 4.0D;
        };
    }

    private static double scarStrength(long seed, double dx, double dz, double angle, double distance) {
        if (distance < InnerDimensionConstants.CORE_RADIUS + 32.0D) {
            return 0.0D;
        }
        double strength = 0.0D;
        for (int i = 0; i < 5; i++) {
            double scarAngle = i * Math.PI * 0.4D + InnerNoise.value(seed + 71L, i, 0) * 0.25D;
            double angular = InnerRegionMap.angularDistance(angle, scarAngle);
            double radialBand = Math.sin(distance / (150.0D + i * 17.0D));
            double line = Math.max(0.0D, 1.0D - angular / 0.035D) * Math.max(0.0D, 0.65D + radialBand);
            strength = Math.max(strength, line);
        }
        double cross = Math.abs(Math.sin((dx * 0.006D) + InnerNoise.smoothValue(seed + 73L, dx, dz, 240.0D)));
        return Math.max(strength, cross > 0.985D && distance > 360.0D ? 0.7D : 0.0D);
    }

    private static double pathStrength(double distance, double angle, DrugId drugId) {
        if (distance < InnerDimensionConstants.CORE_RADIUS) {
            return 1.0D;
        }
        double centerRing = ringStrength(distance, InnerDimensionConstants.CENTER_RING_RADIUS, 9.0D);
        double midRing = ringStrength(distance, InnerDimensionConstants.MID_RING_RADIUS, 8.0D);
        double outerRing = ringStrength(distance, InnerDimensionConstants.OUTER_RING_RADIUS, 7.0D);
        double radial = 0.0D;
        double regionAngle = InnerRegionMap.angleFor(drugId);
        double angular = InnerRegionMap.angularDistance(angle, regionAngle);
        if (angular < 0.024D && distance > 78.0D && distance < 1170.0D) {
            radial = 1.0D - angular / 0.024D;
        }
        return Math.max(Math.max(centerRing, midRing), Math.max(outerRing, radial));
    }

    private static double ringStrength(double distance, double ringRadius, double halfWidth) {
        return InnerNoise.clamp01(1.0D - Math.abs(distance - ringRadius) / halfWidth);
    }

    private static Satellite satellite(long seed, int centerX, int centerZ, double worldX, double worldZ) {
        double bestDensity = -1000.0D;
        int bestTop = InnerDimensionConstants.BASE_Y + 22;
        for (int i = 0; i < 9; i++) {
            double angle = (i + 0.35D) / 9.0D * Math.PI * 2.0D;
            double radius = 1320.0D + InnerNoise.value(seed + 91L, i, 0) * 150.0D;
            int sx = centerX + (int) Math.round(Math.cos(angle) * radius);
            int sz = centerZ + (int) Math.round(Math.sin(angle) * radius);
            double dx = worldX - sx;
            double dz = worldZ - sz;
            double distance = Math.sqrt(dx * dx + dz * dz);
            double size = 82.0D + InnerNoise.value(seed + 93L, i, 0) * 36.0D;
            double density = size - distance + InnerNoise.fbm(seed + i * 19L, worldX, worldZ, 55.0D, 3) * 28.0D;
            if (density > bestDensity) {
                bestDensity = density;
                bestTop = InnerDimensionConstants.BASE_Y + 22 + (int) Math.round(InnerNoise.value(seed + 97L, i, 0) * 16.0D);
            }
        }
        return new Satellite(bestDensity, bestTop);
    }

    private record Satellite(double density, int topY) {
    }

    public record Sample(
            boolean land,
            int topY,
            int bottomY,
            DrugId drugId,
            boolean path,
            double pathStrength,
            boolean scar,
            double scarStrength,
            boolean satellite,
            double density,
            double distanceFromCenter,
            InnerTerrainProfile profile
    ) {
    }
}
