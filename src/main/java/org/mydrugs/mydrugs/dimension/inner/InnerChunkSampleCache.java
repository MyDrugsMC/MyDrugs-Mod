package org.mydrugs.mydrugs.dimension.inner;

/**
 * Per-chunk column context shared by the terrain fill and every feature builder pass.
 *
 * <p>Terrain samples are computed eagerly (the fill loop touches all 256 columns anyway);
 * grove and scene samples are memoized lazily on first access so the many builders that need
 * them stop recomputing identical values per column. All accessors return exactly what the
 * underlying samplers would return for the same inputs — this class is pure memoization and
 * must never change generation output.
 */
public final class InnerChunkSampleCache {
    private final InnerTerrain.Sample[][] samples = new InnerTerrain.Sample[16][16];
    private final InnerGroveSample[][] groves = new InnerGroveSample[16][16];
    private final InnerSceneSample[][] scenes = new InnerSceneSample[16][16];
    private final long seed;
    private final int centerX;
    private final int centerZ;
    private final int chunkMinX;
    private final int chunkMinZ;

    // Chunk-level builder early-out flags, accumulated during the eager sample pass. Each is a
    // necessary condition of the corresponding builder's per-column predicate (a conservative
    // superset): a false flag proves no column in this chunk can host that feature, so the
    // builder may skip its scan entirely. Flags must never be false when a column qualifies.
    private boolean anyLand;
    private boolean anyLake;
    private boolean anyScarOrHole;
    private boolean anyTransition;
    private boolean anyTreeZone;
    private boolean anyPlantPatch;
    private boolean anyShoreOrWetland;
    private boolean anySatellite;
    private boolean anySpikeCandidate;
    private boolean anySkyLand;
    private double maxPathStrength;
    private double maxCliffStrength;
    private int minLandTopY = Integer.MAX_VALUE;

    private InnerChunkSampleCache(int centerX, int centerZ, int chunkMinX, int chunkMinZ) {
        this.seed = InnerTerrain.seedForSlot(centerX, centerZ);
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.chunkMinX = chunkMinX;
        this.chunkMinZ = chunkMinZ;
    }

    public static InnerChunkSampleCache build(int centerX, int centerZ, int chunkMinX, int chunkMinZ) {
        InnerChunkSampleCache cache = new InnerChunkSampleCache(centerX, centerZ, chunkMinX, chunkMinZ);
        for (int localX = 0; localX < 16; localX++) {
            int worldX = chunkMinX + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                InnerTerrain.Sample sample = InnerTerrain.sample(centerX, centerZ, worldX, chunkMinZ + localZ);
                cache.samples[localX][localZ] = sample;
                cache.accumulateFlags(sample);
            }
        }
        return cache;
    }

    private void accumulateFlags(InnerTerrain.Sample sample) {
        if (sample.land()) {
            anyLand = true;
            minLandTopY = Math.min(minLandTopY, sample.topY());
        }
        anyLake |= sample.land() && sample.lake();
        anyScarOrHole |= sample.scar() || sample.hole();
        anyTransition |= sample.transitionZone();
        anyTreeZone |= sample.treeZone();
        anyPlantPatch |= sample.plantPatch();
        anyShoreOrWetland |= sample.shoreStrength() > 0.0D || sample.wetlandStrength() > 0.0D;
        anySatellite |= sample.satellite();
        anySpikeCandidate |= sample.spikeField() || sample.holeStrength() > 0.10D || sample.satellite();
        anySkyLand |= sample.skyLand();
        maxPathStrength = Math.max(maxPathStrength, sample.pathStrength());
        maxCliffStrength = Math.max(maxCliffStrength, sample.cliffStrength());
    }

    boolean anyLand() {
        return anyLand;
    }

    boolean anyLake() {
        return anyLake;
    }

    boolean anyScarOrHole() {
        return anyScarOrHole;
    }

    boolean anyTransition() {
        return anyTransition;
    }

    /** Whether any column could host a grove (necessary condition of canHostGrove). */
    boolean anyGroveGround() {
        return anyLand && (anyTreeZone || anyPlantPatch || anyShoreOrWetland);
    }

    boolean anyShoreOrWetland() {
        return anyShoreOrWetland;
    }

    boolean anySatellite() {
        return anySatellite;
    }

    boolean anySpikeCandidate() {
        return anySpikeCandidate;
    }

    boolean anySkyLand() {
        return anySkyLand;
    }

    double maxPathStrength() {
        return maxPathStrength;
    }

    double maxCliffStrength() {
        return maxCliffStrength;
    }

    /** Lowest land surface in the chunk, or {@link Integer#MAX_VALUE} when there is no land. */
    int minLandTopY() {
        return minLandTopY;
    }

    public InnerTerrain.Sample sample(int localX, int localZ) {
        return samples[localX][localZ];
    }

    long seed() {
        return seed;
    }

    int islandCenterX() {
        return centerX;
    }

    int islandCenterZ() {
        return centerZ;
    }

    /** Memoized {@link InnerGroveSampler#sample} for the column at chunk-local coordinates. */
    InnerGroveSample grove(int localX, int localZ) {
        InnerGroveSample grove = groves[localX][localZ];
        if (grove == null) {
            grove = InnerGroveSampler.sample(
                    seed, centerX, centerZ, chunkMinX + localX, chunkMinZ + localZ, samples[localX][localZ]);
            groves[localX][localZ] = grove;
        }
        return grove;
    }

    /** Memoized {@link InnerSceneSampler#sample} for the column at chunk-local coordinates. */
    InnerSceneSample scene(int localX, int localZ) {
        InnerSceneSample scene = scenes[localX][localZ];
        if (scene == null) {
            scene = InnerSceneSampler.sample(
                    seed, centerX, centerZ, chunkMinX + localX, chunkMinZ + localZ,
                    samples[localX][localZ], grove(localX, localZ));
            scenes[localX][localZ] = scene;
        }
        return scene;
    }
}
