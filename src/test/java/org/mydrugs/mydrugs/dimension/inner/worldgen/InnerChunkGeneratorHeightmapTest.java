package org.mydrugs.mydrugs.dimension.inner.worldgen;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.dimension.inner.InnerDimensionConstants;
import org.mydrugs.mydrugs.dimension.inner.InnerTerrain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnerChunkGeneratorHeightmapTest {
    private static final int MIN_Y = InnerDimensionConstants.MIN_Y;
    private static final int HEIGHT = InnerDimensionConstants.GEN_DEPTH;

    @Test
    void baseHeightMatchesNormalLandSurface() {
        int x = 0;
        int z = 0;
        InnerTerrain.Sample sample = InnerTerrain.sampleVanillaColumn(x, z);

        assertTrue(sample.land());
        assertTrue(!sample.lake());
        assertTrue(!sample.skyLand());
        assertEquals(sample.topY() + 1, height(sample, x, z));
    }

    @Test
    void baseHeightIsMinYForEmptyVoidColumn() {
        Column column = findColumn(sample -> !sample.land() && !sample.skyLand());

        assertEquals(MIN_Y, height(column.sample(), column.x(), column.z()));
    }

    @Test
    void baseHeightUsesLakeSurfaceWhenLakeIsGenerated() {
        Column column = findColumn(sample -> sample.land() && sample.lake() && !sample.skyLand());

        assertEquals(
                column.sample().lakeSurfaceY() + 1,
                height(column.sample(), column.x(), column.z())
        );
    }

    @Test
    void baseHeightIncludesSkyShardSurfaceWhenGenerated() {
        Column column = findColumn(InnerTerrain.Sample::skyLand);

        assertEquals(
                column.sample().skyTopY() + 1,
                height(column.sample(), column.x(), column.z())
        );
    }

    @Test
    void baseHeightMatchesPathSurface() {
        InnerTerrain.Sample sample = InnerTerrain.sampleVanillaColumn(0, 0);

        assertTrue(sample.path());
        assertEquals(sample.topY() + 1, height(sample, 0, 0));
    }

    private static int height(InnerTerrain.Sample sample, int x, int z) {
        return InnerTerrain.generatedBaseHeight(sample, x, z, MIN_Y, HEIGHT);
    }

    private static Column findColumn(java.util.function.Predicate<InnerTerrain.Sample> predicate) {
        for (int x = -1600; x <= 1600; x += 13) {
            for (int z = -1600; z <= 1600; z += 17) {
                InnerTerrain.Sample sample = InnerTerrain.sampleVanillaColumn(x, z);
                if (predicate.test(sample)) {
                    return new Column(x, z, sample);
                }
            }
        }
        throw new AssertionError("No matching Inner Dimension terrain column found");
    }

    private record Column(int x, int z, InnerTerrain.Sample sample) {
    }
}
