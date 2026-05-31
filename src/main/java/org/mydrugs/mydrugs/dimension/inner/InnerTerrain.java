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

        // A2: blend the two regions nearest this angle so the profile's scalar fields cross
        // the sector boundary continuously (no dead-straight cliffs/seams). drugId stays the
        // dominant region for downstream keying; only the visual fields are interpolated.
        InnerRegionMap.RegionBlend blend = InnerRegionMap.regionBlend(angle);
        DrugId drugId = blend.primary();
        InnerTerrainProfile profile = InnerTerrainProfile.forDrug(drugId);
        InnerTerrainProfile secondaryProfile = InnerTerrainProfile.forDrug(blend.secondary());
        double blendW = blend.secondaryWeight();
        double silhouetteScale = InnerNoise.lerp(profile.silhouetteScale(), secondaryProfile.silhouetteScale(), blendW);
        double ridgeScale = InnerNoise.lerp(profile.ridgeScale(), secondaryProfile.ridgeScale(), blendW);
        double scarCarve = InnerNoise.lerp(profile.scarCarve(), secondaryProfile.scarCarve(), blendW);
        double heightBias = InnerNoise.lerp(profile.heightBias(), secondaryProfile.heightBias(), blendW);

        // Within the transition band scatter the two palettes by a per-column noise threshold
        // against the blend weight, so the boundary reads as a dithered mingling, not a hard line.
        InnerTerrainProfile surfaceProfile = profile;
        if (blendW > 0.0D) {
            double dither = (InnerNoise.smoothValue(seed + 0x1357L, worldX, worldZ, 14.0D) + 1.0D) * 0.5D;
            if (dither < blendW) {
                surfaceProfile = secondaryProfile;
            }
        }

        double coastWarp = InnerNoise.fbm(seed, worldX, worldZ, 240.0D, 4)
                * (74.0D + silhouetteScale * 30.0D);
        double cliffBreak = InnerNoise.ridged(seed + 13L, worldX, worldZ, 84.0D, 3)
                * (24.0D + ridgeScale * 22.0D);
        double density = InnerDimensionConstants.ISLAND_RADIUS - distance + coastWarp - cliffBreak;

        double scarStrength = scarStrength(seed, dx, dz, angle, distance) * scarCarve;
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
        double localSilhouette = silhouette(drugId, seed, worldX, worldZ);
        int topY = InnerDimensionConstants.BASE_Y
                + (int) Math.round(heightNoise * (18.0D + silhouetteScale * 8.0D))
                + (int) Math.round(ridge * (8.0D + ridgeScale * 8.0D))
                + (int) Math.round(localSilhouette)
                + (int) Math.round(heightBias);

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
                surfaceProfile
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

    /**
     * A3: per-drug surface character expressed purely as noise sampled at world XZ — no term is
     * a function of {@code distance} alone, so silhouettes no longer ripple in concentric rings.
     * Each region mixes ridged (jagged), billow (bulbous) and fbm (rolling) noise at its own
     * amplitude/frequency: shattered glass for METH/COCAINE, soft hills for WEED, pressed
     * terraces for HASH, sunken marsh basins for ALCOHOL, layered shelves for LSD.
     */
    private static double silhouette(DrugId drugId, long seed, int worldX, int worldZ) {
        long s = seed + 501L + drugId.networkId();
        return switch (drugId) {
            case TOBACCO -> InnerNoise.ridged(s, worldX, worldZ, 54.0D, 3) * 11.0D;
            case WEED -> InnerNoise.fbm(s, worldX, worldZ, 96.0D, 4) * 6.0D - 1.5D;
            case HASH -> terraced(InnerNoise.ridged(s, worldX, worldZ, 60.0D, 3), 3) * 12.0D;
            case ALCOHOL -> (InnerNoise.fbm(s, worldX, worldZ, 70.0D, 4) - 0.55D) * 14.0D;
            case COCAINE -> sharpen(InnerNoise.ridged(s, worldX, worldZ, 38.0D, 4)) * 16.0D;
            case LSD -> InnerNoise.ridged(s, worldX, worldZ, 44.0D, 3) * 12.0D
                    + InnerNoise.fbm(s + 7L, worldX, worldZ, 120.0D, 4) * 9.0D;
            case METH -> sharpen(InnerNoise.ridged(s, worldX, worldZ, 33.0D, 4)) * 18.0D;
            case MUSHROOMS -> InnerNoise.fbm(s, worldX, worldZ, 80.0D, 4) * 5.0D
                    + Math.abs(InnerNoise.fbm(s + 3L, worldX, worldZ, 30.0D, 3)) * 3.0D;
            default -> InnerNoise.fbm(s, worldX, worldZ, 110.0D, 4) * 4.0D;
        };
    }

    /** Quantise a 0..1 noise value into {@code levels} flat steps for a terraced look. */
    private static double terraced(double value, int levels) {
        return Math.floor(InnerNoise.clamp01(value) * levels) / levels;
    }

    /** Sharpen a 0..1 ridged value so peaks become brittle spikes and flanks fall away faster. */
    private static double sharpen(double value) {
        double v = InnerNoise.clamp01(value);
        return v * v;
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
