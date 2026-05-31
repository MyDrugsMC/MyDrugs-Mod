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
    // A5: salt for the surface accent-scatter noise.
    private static final long ACCENT_SALT = 0x00AC_CE57L;

    // B4: optional per-pass Sample memoization. Enabled only around the (single-threaded,
    // server-side) overlay/placement pass via begin/endCachePass, where the same column is
    // sampled repeatedly (decorator + builders + surfaceTop + rebuilder). Worldgen worker threads
    // never enable it, so there is no cross-thread sharing of mutable state.
    private static final ThreadLocal<java.util.Map<Long, Sample>> PASS_CACHE = new ThreadLocal<>();

    // B4: satellite ring layout is a function of the slot seed only; precompute it once per slot
    // instead of re-running the 9-iteration trig loop on every sample() call. Immutable once built.
    private static final java.util.Map<Long, SatelliteCenter[]> SATELLITE_CENTERS =
            new java.util.concurrent.ConcurrentHashMap<>();

    private InnerTerrain() {
    }

    /** Begin a per-pass Sample cache on the current thread. Must be paired with {@link #endCachePass()}. */
    public static void beginCachePass() {
        PASS_CACHE.set(new java.util.HashMap<>());
    }

    /** End the current thread's per-pass Sample cache. */
    public static void endCachePass() {
        PASS_CACHE.remove();
    }

    public static Sample sample(int worldX, int worldZ) {
        return sample(slotCenter(worldX), slotCenter(worldZ), worldX, worldZ);
    }

    public static Sample sample(int centerX, int centerZ, int worldX, int worldZ) {
        java.util.Map<Long, Sample> cache = PASS_CACHE.get();
        if (cache == null) {
            return computeSample(centerX, centerZ, worldX, worldZ);
        }
        // A pass always runs against a single island, so (worldX, worldZ) keys the column uniquely.
        long key = ((long) worldX & 0xffffffffL) | (((long) worldZ & 0xffffffffL) << 32);
        Sample cached = cache.get(key);
        if (cached != null) {
            return cached;
        }
        Sample computed = computeSample(centerX, centerZ, worldX, worldZ);
        cache.put(key, computed);
        return computed;
    }

    private static Sample computeSample(int centerX, int centerZ, int worldX, int worldZ) {
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

        double scarStrength = scarStrength(seed, sampleX, sampleZ, distance) * scarCarve;
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

        // A4: a low-frequency base elevation pushed through a plateau/cliff spline (mid values
        // flatten into plateaus, extremes drop to valleys or rear into cliffs), plus ridged
        // detail scaled by local slope so flats stay readable and slopes gain texture. Everything
        // is summed in double and rounded exactly once at the end (no per-term stair-stepping).
        double baseElev = InnerNoise.fbm(seed + 29L, worldX, worldZ, 150.0D, 5);
        double probe = 6.0D;
        double shaped = elevationSpline(baseElev);
        double gradX = elevationSpline(InnerNoise.fbm(seed + 29L, worldX + probe, worldZ, 150.0D, 5)) - shaped;
        double gradZ = elevationSpline(InnerNoise.fbm(seed + 29L, worldX, worldZ + probe, 150.0D, 5)) - shaped;
        double slope = InnerNoise.clamp01(Math.hypot(gradX, gradZ) * 3.0D);
        double localSilhouette = silhouette(drugId, seed, worldX, worldZ);
        double baseHeight = shaped * (20.0D + silhouetteScale * 10.0D);
        double detail = InnerNoise.ridged(seed + 31L, worldX, worldZ, 72.0D, 3)
                * (3.0D + ridgeScale * 7.0D) * (0.25D + 0.75D * slope);
        double topYd = InnerDimensionConstants.BASE_Y + baseHeight + detail + localSilhouette + heightBias;
        int topY = (int) Math.round(topYd);

        if (path) {
            topY = InnerDimensionConstants.BASE_Y + 4 + (int) Math.round(baseElev * 3.0D);
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

        // A5: vary the surface/subsurface band depth (2..6) so flat palettes do not read as a
        // uniformly thin painted slab.
        int surfaceDepth = 2 + (int) Math.round(InnerNoise.ridged(seed + 53L, worldX, worldZ, 40.0D, 2) * 4.0D);
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
                surfaceProfile,
                surfaceDepth
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

    public static BlockState stateFor(Sample sample, int worldX, int y, int worldZ) {
        InnerTerrainProfile profile = sample.profile();
        if (sample.path() && y == sample.topY()) {
            return profile.pathBlock();
        }
        if (sample.scar() && y >= sample.topY() - 2) {
            return profile.scarBlock();
        }
        // A5: scatter the profile's accent block through the surface band by a clumpy noise
        // threshold so flat single-colour palettes (quartz, calcite, prismarine, magma...) gain
        // texture instead of reading as painted slabs.
        if (y == sample.topY() || y >= sample.topY() - sample.surfaceDepth()) {
            boolean nearTop = y >= sample.topY() - 1;
            if (accentScatter(profile, worldX, y, worldZ, nearTop)) {
                return profile.accentBlock();
            }
            return y == sample.topY() ? profile.surfaceBlock() : profile.subsurfaceBlock();
        }
        return profile.deepBlock();
    }

    private static boolean accentScatter(InnerTerrainProfile profile, int worldX, int y, int worldZ, boolean nearTop) {
        long salt = ACCENT_SALT + profile.drugId().networkId() * 131L;
        double noise = InnerNoise.smoothValue(salt, worldX, worldZ, 6.5D);
        // Looser threshold right at the surface, tighter just below, for a weathered crust.
        double threshold = nearTop ? 0.58D : 0.74D;
        return noise > threshold;
    }

    public static boolean caveAir(Sample sample, int worldX, int y, int worldZ) {
        if (sample.pathStrength() > 0.2D
                || sample.distanceFromCenter() < InnerDimensionConstants.CORE_RADIUS + 40.0D
                || y > sample.topY() - 5
                || y < sample.bottomY() + 4) {
            return false;
        }
        long seed = InnerDimensionConstants.BASE_SEED + sample.drugId().networkId() * 101L;
        // B4: 2 octaves instead of 3 — this runs per-block inside the column, so the cheaper
        // ridged lookup matters; the cave silhouette is unchanged at the visible scale.
        double cave = InnerNoise.ridged(seed, worldX, worldZ + y * 5.0D, 46.0D, 2);
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

    /**
     * A4 elevation spline. Blends a linear and a cubic response so mid-range base-elevation values
     * compress toward a common height (broad plateaus) while the tails are preserved or pushed
     * further (deeper valleys, sharper cliffs). Maps [-1,1] -> [-1,1].
     */
    private static double elevationSpline(double n) {
        double clamped = Math.max(-1.0D, Math.min(1.0D, n));
        double cubic = clamped * clamped * clamped;
        return 0.35D * clamped + 0.65D * cubic;
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

    /**
     * A6: scars as a branching fault network rather than drawn angular spokes. Two ridged-noise
     * octaves on the (already domain-warped) sample coords are combined and thresholded high; the
     * ridge crests form thin, forking cracks that feather out from a strong core. Region-level
     * intensity (scars radiate from hard drugs) comes from the per-region {@code scarCarve} the
     * caller multiplies in. No term depends on angle or on distance alone.
     */
    private static double scarStrength(long seed, double worldX, double worldZ, double distance) {
        if (distance < InnerDimensionConstants.CORE_RADIUS + 32.0D) {
            return 0.0D;
        }
        double fault = InnerNoise.ridged(seed + 71L, worldX, worldZ, 120.0D, 4);
        double detail = InnerNoise.ridged(seed + 211L, worldX, worldZ, 38.0D, 3);
        double combined = fault * 0.7D + detail * 0.3D;
        double threshold = 0.85D;
        if (combined <= threshold) {
            return 0.0D;
        }
        return InnerNoise.clamp01((combined - threshold) / (1.0D - threshold));
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
        SatelliteCenter[] centers = satelliteCenters(seed, centerX, centerZ);
        double bestDensity = -1000.0D;
        int bestTop = InnerDimensionConstants.BASE_Y + 22;
        for (int i = 0; i < centers.length; i++) {
            SatelliteCenter c = centers[i];
            double dx = worldX - c.x();
            double dz = worldZ - c.z();
            double distance = Math.sqrt(dx * dx + dz * dz);
            double density = c.size() - distance + InnerNoise.fbm(seed + i * 19L, worldX, worldZ, 55.0D, 3) * 28.0D;
            if (density > bestDensity) {
                bestDensity = density;
                bestTop = c.topY();
            }
        }
        return new Satellite(bestDensity, bestTop);
    }

    private static SatelliteCenter[] satelliteCenters(long seed, int centerX, int centerZ) {
        return SATELLITE_CENTERS.computeIfAbsent(seed, s -> {
            SatelliteCenter[] arr = new SatelliteCenter[9];
            for (int i = 0; i < arr.length; i++) {
                double angle = (i + 0.35D) / 9.0D * Math.PI * 2.0D;
                double radius = 1320.0D + InnerNoise.value(s + 91L, i, 0) * 150.0D;
                int sx = centerX + (int) Math.round(Math.cos(angle) * radius);
                int sz = centerZ + (int) Math.round(Math.sin(angle) * radius);
                double size = 82.0D + InnerNoise.value(s + 93L, i, 0) * 36.0D;
                int topY = InnerDimensionConstants.BASE_Y + 22
                        + (int) Math.round(InnerNoise.value(s + 97L, i, 0) * 16.0D);
                arr[i] = new SatelliteCenter(sx, sz, size, topY);
            }
            return arr;
        });
    }

    private record SatelliteCenter(int x, int z, double size, int topY) {
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
            InnerTerrainProfile profile,
            int surfaceDepth
    ) {
    }
}
