package org.mydrugs.mydrugs.dimension.inner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Golden-hash regression lock for Inner terrain generation.
 *
 * <p>Every numeric/boolean field of {@link InnerTerrain.Sample} (plus cave carving) is folded
 * into one hash per island slot over a fixed coordinate grid. Performance refactors (caching,
 * memoization, loop restructuring) must keep these hashes bit-identical.
 *
 * <p><b>Regenerating goldens:</b> when generation is changed <i>intentionally</i> (new shaping
 * math, new feature fields), run this test once; the failure message prints the new actual
 * hashes. Replace the constants below with those values in the same commit as the generation
 * change, and call out the regeneration explicitly in the commit message.
 */
class InnerTerrainDeterminismTest {

    /** Prime stride so the grid does not align with any noise cell size used by the generator. */
    private static final int GRID_STEP = 53;
    private static final int GRID_REACH = InnerDimensionConstants.ISLAND_RADIUS
            + InnerDimensionConstants.SATELLITE_REACH;

    // Regenerated intentionally for the grand remake (A1-A4) plus the smaller-CORE_RADIUS fix:
    // core/rim measured on unwarped distance, warp ramp decoupled from CORE_RADIUS, and the
    // path corridor's inner end scaled to the platform.
    private static final long GOLDEN_ORIGIN_SLOT = 4074051957395300248L;
    private static final long GOLDEN_FAR_SLOT = 3948977173032735938L;

    @Test
    void originSlotGenerationIsBitStable() {
        long actual = goldenHash(0, 0);
        assertEquals(GOLDEN_ORIGIN_SLOT, actual,
                "origin slot golden hash changed; if intentional, set GOLDEN_ORIGIN_SLOT to " + actual + "L");
    }

    @Test
    void farSlotGenerationIsBitStable() {
        int centerX = InnerTerrain.slotCenter(123_456);
        int centerZ = InnerTerrain.slotCenter(-987_654);
        long actual = goldenHash(centerX, centerZ);
        assertEquals(GOLDEN_FAR_SLOT, actual,
                "far slot golden hash changed; if intentional, set GOLDEN_FAR_SLOT to " + actual + "L");
    }

    @Test
    void goldenHashIsStableAcrossRepeatedRuns() {
        assertEquals(goldenHash(0, 0), goldenHash(0, 0));
    }

    private static long goldenHash(int centerX, int centerZ) {
        long h = InnerNoise.mix64(InnerTerrain.seedForSlot(centerX, centerZ));
        int column = 0;
        for (int dz = -GRID_REACH; dz <= GRID_REACH; dz += GRID_STEP) {
            for (int dx = -GRID_REACH; dx <= GRID_REACH; dx += GRID_STEP) {
                int worldX = centerX + dx;
                int worldZ = centerZ + dz;
                InnerTerrain.Sample sample = InnerTerrain.sample(centerX, centerZ, worldX, worldZ);
                h = foldSample(h, sample);
                // Cave carving for a sparse subset of land columns (full y range per column).
                if (sample.land() && (column & 15) == 0) {
                    for (int y = sample.bottomY(); y <= sample.topY(); y++) {
                        h = fold(h, InnerTerrain.caveAir(sample, worldX, y, worldZ) ? 1L : 0L);
                    }
                }
                column++;
            }
        }
        return h;
    }

    private static long foldSample(long h, InnerTerrain.Sample s) {
        h = fold(h, s.land() ? 1L : 0L);
        h = fold(h, s.topY());
        h = fold(h, s.bottomY());
        h = fold(h, s.drugId().networkId());
        h = fold(h, s.primaryDrug().networkId());
        h = fold(h, s.secondaryDrug().networkId());
        h = fold(h, Double.doubleToLongBits(s.secondaryWeight()));
        h = fold(h, s.transitionZone() ? 1L : 0L);
        h = fold(h, Double.doubleToLongBits(s.transitionStrength()));
        h = fold(h, s.path() ? 1L : 0L);
        h = fold(h, Double.doubleToLongBits(s.pathStrength()));
        h = fold(h, s.scar() ? 1L : 0L);
        h = fold(h, Double.doubleToLongBits(s.scarStrength()));
        h = fold(h, s.satellite() ? 1L : 0L);
        h = fold(h, Double.doubleToLongBits(s.density()));
        h = fold(h, Double.doubleToLongBits(s.distanceFromCenter()));
        h = fold(h, s.surfaceDepth());
        h = fold(h, s.lake() ? 1L : 0L);
        h = fold(h, Double.doubleToLongBits(s.lakeStrength()));
        h = fold(h, Double.doubleToLongBits(s.lakeCoreStrength()));
        h = fold(h, Double.doubleToLongBits(s.shoreStrength()));
        h = fold(h, Double.doubleToLongBits(s.wetlandStrength()));
        h = fold(h, s.lakeType().name().hashCode());
        h = fold(h, s.lakeSurfaceY());
        h = fold(h, s.lakeFloorY());
        h = fold(h, s.lakeIsland() ? 1L : 0L);
        h = fold(h, s.lakeCenterpiece() ? 1L : 0L);
        h = fold(h, s.hole() ? 1L : 0L);
        h = fold(h, Double.doubleToLongBits(s.holeStrength()));
        h = fold(h, s.holeType().name().hashCode());
        h = fold(h, s.spikeField() ? 1L : 0L);
        h = fold(h, Double.doubleToLongBits(s.spikeStrength()));
        h = fold(h, s.treeZone() ? 1L : 0L);
        h = fold(h, Double.doubleToLongBits(s.treeDensity()));
        h = fold(h, s.plantPatch() ? 1L : 0L);
        h = fold(h, Double.doubleToLongBits(s.plantDensity()));
        h = fold(h, Double.doubleToLongBits(s.cliffStrength()));
        h = fold(h, Double.doubleToLongBits(s.slopeHint()));
        h = fold(h, s.skyLand() ? 1L : 0L);
        h = fold(h, s.skyTopY());
        h = fold(h, s.skyBottomY());
        h = fold(h, Double.doubleToLongBits(s.sky().strength()));
        h = fold(h, s.sky().drug().networkId());
        return h;
    }

    private static long fold(long h, long value) {
        return InnerNoise.mix64(h ^ value);
    }
}
