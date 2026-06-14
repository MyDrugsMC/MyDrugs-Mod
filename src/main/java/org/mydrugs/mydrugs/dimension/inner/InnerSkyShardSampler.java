package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;

/**
 * Floating sky-shard field: deterministic islets in a band high above the island (A1).
 * Same cell-hash technique as {@link InnerGroveSampler}: each {@value #CELL_SIZE}-block cell may
 * host one islet whose centre, radius, altitude and identity derive purely from the slot seed and
 * cell coordinates, so every column (in any chunk, on any pass) reconstructs the same islet.
 *
 * <p>Islets hang deeper than they rise — the classic inverted-teardrop floating island — and take
 * the region identity of their cell centre so a single islet never mixes palettes.
 */
final class InnerSkyShardSampler {
    private static final long SHARD_SALT = 0x534B_5953L;
    private static final int CELL_SIZE = 80;
    /** No shards over the sanctuary or beyond the satellite reach. */
    private static final double INNER_EXCLUSION = InnerDimensionConstants.CORE_RADIUS + 72.0D;
    private static final double OUTER_LIMIT =
            InnerDimensionConstants.ISLAND_RADIUS + InnerDimensionConstants.SATELLITE_REACH;
    static final int MIN_ALTITUDE = 150;
    static final int MAX_ALTITUDE = 206;
    static final int MAX_TOP = 222;

    private InnerSkyShardSampler() {
    }

    /** Minimum air gap between a mountain top and the shard hanging above it. */
    static final int GROUND_CLEARANCE = 24;

    static InnerSkyShardSample sample(
            long seed,
            int centerX,
            int centerZ,
            int worldX,
            int worldZ,
            double distanceFromCenter,
            boolean groundLand,
            int groundTopY
    ) {
        if (distanceFromCenter < INNER_EXCLUSION || distanceFromCenter > OUTER_LIMIT) {
            return InnerSkyShardSample.NONE;
        }
        int cellX = Math.floorDiv(worldX, CELL_SIZE);
        int cellZ = Math.floorDiv(worldZ, CELL_SIZE);
        InnerSkyShardSample best = InnerSkyShardSample.NONE;
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                InnerSkyShardSample slice = cellSlice(seed, centerX, centerZ, cellX + dx, cellZ + dz, worldX, worldZ);
                if (slice.land() && slice.strength() > best.strength()) {
                    best = slice;
                }
            }
        }
        if (!best.land() || !groundLand) {
            return best;
        }
        // Amplified peaks can climb toward the shard band: the underside parts around them like
        // a cloud base, never welding the islet to the ground.
        int minBottom = groundTopY + GROUND_CLEARANCE;
        if (best.bottomY() >= minBottom) {
            return best;
        }
        if (minBottom >= best.topY()) {
            return InnerSkyShardSample.NONE;
        }
        return new InnerSkyShardSample(true, best.topY(), minBottom, best.strength(), best.drug(), best.crystalline());
    }

    private static InnerSkyShardSample cellSlice(
            long seed,
            int centerX,
            int centerZ,
            int cellX,
            int cellZ,
            int worldX,
            int worldZ
    ) {
        long hash = InnerNoise.mix64(seed + SHARD_SALT
                + (long) cellX * 341873128712L
                + (long) cellZ * 132897987541L);
        double jitterX = InnerNoise.value(seed + SHARD_SALT + 3L, cellX, cellZ) * (CELL_SIZE * 0.28D);
        double jitterZ = InnerNoise.value(seed + SHARD_SALT + 7L, cellX, cellZ) * (CELL_SIZE * 0.28D);
        double isletX = cellX * CELL_SIZE + CELL_SIZE * 0.5D + jitterX;
        double isletZ = cellZ * CELL_SIZE + CELL_SIZE * 0.5D + jitterZ;

        // The whole islet takes the identity of its centre, and density is per-region.
        double isletDistance = Math.hypot(isletX - centerX, isletZ - centerZ);
        if (isletDistance < INNER_EXCLUSION || isletDistance > OUTER_LIMIT) {
            return InnerSkyShardSample.NONE;
        }
        DrugId drug = InnerRegionMap.dominantDrug(centerX, centerZ, (int) Math.round(isletX), (int) Math.round(isletZ));
        if ((hash & 1023L) >= densityFor(drug) * 1024.0D) {
            return InnerSkyShardSample.NONE;
        }

        double radius = 4.0D + ((hash >>> 10) & 255L) / 255.0D * 10.0D; // 4..14
        double dx = worldX - isletX;
        double dz = worldZ - isletZ;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal >= radius) {
            return InnerSkyShardSample.NONE;
        }
        double t = 1.0D - (horizontal * horizontal) / (radius * radius); // 1 at centre, 0 at rim
        int altitude = MIN_ALTITUDE + (int) ((hash >>> 18) % (MAX_ALTITUDE - MIN_ALTITUDE));
        // Organic rim: a touch of column noise so islets are not perfect lenses.
        double rough = InnerNoise.smoothValue(seed + SHARD_SALT + 11L, worldX, worldZ, 9.0D) * 1.4D;
        int top = altitude + (int) Math.round(t * (2.0D + radius * 0.30D) + rough * t);
        int bottom = altitude - (int) Math.round(t * (4.0D + radius * 0.85D) + rough);
        if (bottom >= top) {
            bottom = top - 1;
        }
        return new InnerSkyShardSample(true, Math.min(MAX_TOP, top), bottom, t, drug, crystallineFor(drug));
    }

    /** Per-region islet density: psychedelic skies are busy, ashen skies nearly empty. */
    private static double densityFor(DrugId drug) {
        return switch (drug) {
            case LSD -> 0.62D;
            case HASH -> 0.55D;
            case MUSHROOMS -> 0.42D;
            case METH -> 0.40D;
            case WEED -> 0.36D;
            case COCAINE -> 0.30D;
            case ALCOHOL -> 0.26D;
            case COFFEE -> 0.24D;
            case TOBACCO -> 0.16D;
            default -> 0.25D;
        };
    }

    private static boolean crystallineFor(DrugId drug) {
        return switch (drug) {
            case HASH, LSD, COCAINE, METH -> true;
            default -> false;
        };
    }

    /** Block for one sky-shard cell of a column. Crown on top, hanging strata toward the tip. */
    static BlockState stateFor(InnerSkyShardSample sky, InnerTerrainProfile profile, int worldX, int y, int worldZ) {
        int top = sky.topY();
        int bottom = sky.bottomY();
        if (sky.crystalline()) {
            if (y == top) {
                return profile.accentBlock();
            }
            // Crystals: translucent-ish strata with an emissive heart near the hanging tip.
            if (y <= bottom + 1 && sky.strength() > 0.55D) {
                return profile.nodeState();
            }
            long strata = InnerNoise.mix64((long) worldX * 31L + (long) y * 17L + (long) worldZ * 13L);
            return (strata & 3L) == 0L ? profile.accentBlock() : profile.subsurfaceBlock();
        }
        if (y == top) {
            return profile.surfaceBlock();
        }
        if (y >= top - 1) {
            return profile.subsurfaceBlock();
        }
        // The hanging tip of a soft islet ends in a faint glow — a lantern seed in the dark.
        if (y <= bottom + 1 && sky.strength() > 0.62D) {
            return profile.nodeState();
        }
        return profile.deepBlock();
    }

    /** Crown flora for soft islets / crystal teeth for crystalline ones. Null = nothing. */
    static BlockState crownState(InnerSkyShardSample sky, InnerTerrainProfile profile, long hash) {
        if (sky.crystalline()) {
            return (hash & 7L) == 0L ? Blocks.AMETHYST_CLUSTER.defaultBlockState() : null;
        }
        if ((hash & 3L) == 0L) {
            return profile.flora().flower(hash >>> 8);
        }
        if ((hash & 15L) == 1L) {
            return profile.flora().groundCover(hash >>> 8);
        }
        return null;
    }
}
