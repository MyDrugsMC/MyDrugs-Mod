package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnerTerrainTest {

    private static final int CENTER_X = 0;
    private static final int CENTER_Z = 0;

    @Test
    void sampleIsDeterministic() {
        for (int i = 0; i < 400; i++) {
            int x = i * 13 - 1500;
            int z = i * -29 + 900;
            InnerTerrain.Sample a = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
            InnerTerrain.Sample b = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
            assertEquals(a, b, "same inputs must yield an identical sample at " + x + "," + z);
        }
    }

    @Test
    void passCacheDoesNotChangeResults() {
        int x = 321;
        int z = -654;
        InnerTerrain.Sample uncached = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
        InnerTerrain.beginCachePass();
        try {
            InnerTerrain.Sample first = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
            InnerTerrain.Sample second = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
            assertEquals(uncached, first, "cached sample must equal the uncached one");
            assertEquals(first, second, "repeated cached lookups must be identical");
        } finally {
            InnerTerrain.endCachePass();
        }
    }

    @Test
    void farOutsideIslandIsNotLand() {
        InnerTerrain.Sample sample = InnerTerrain.sample(CENTER_X, CENTER_Z, 100_000, 100_000);
        assertFalse(sample.land(), "a column far beyond the island footprint must be void");
    }

    @Test
    void islandBodyIsLand() {
        // Sweep a mid-radius ring well inside the island; the main land body should dominate.
        int land = 0;
        int total = 0;
        for (int deg = 0; deg < 360; deg += 5) {
            double rad = Math.toRadians(deg);
            int x = (int) Math.round(Math.cos(rad) * 360.0D);
            int z = (int) Math.round(Math.sin(rad) * 360.0D);
            total++;
            if (InnerTerrain.sample(CENTER_X, CENTER_Z, x, z).land()) {
                land++;
            }
        }
        assertTrue(land >= total * 0.9, "expected the mid-radius ring to be almost entirely land, was " + land + "/" + total);
    }

    @Test
    void scarFlagMatchesStrengthThreshold() {
        for (int i = 0; i < 1500; i++) {
            int x = i * 7 - 1200;
            int z = i * 17 - 1200;
            InnerTerrain.Sample sample = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
            assertEquals(sample.scarStrength() > 0.42D, sample.scar(),
                    "scar flag must follow the strength threshold at " + x + "," + z);
        }
    }

    @Test
    void surfaceDepthStaysInExpectedBand() {
        for (int i = 0; i < 1000; i++) {
            int x = i * 19 - 800;
            int z = i * -23 + 600;
            InnerTerrain.Sample sample = InnerTerrain.sample(CENTER_X, CENTER_Z, x, z);
            assertTrue(sample.surfaceDepth() >= 2 && sample.surfaceDepth() <= 6,
                    "surface depth out of band: " + sample.surfaceDepth());
        }
    }

    @Test
    void everyLandmarkSitsInsideTheIslandRadius() {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            BlockPos landmark = InnerRegionMap.landmarkFor(CENTER_X, CENTER_Z, drug);
            double distance = Math.hypot(landmark.getX() - CENTER_X, landmark.getZ() - CENTER_Z);
            assertTrue(distance <= InnerDimensionConstants.ISLAND_RADIUS,
                    drug + " landmark sits outside the island radius (" + distance + ")");
        }
    }

    @Test
    void everyRegionLandmarkIsReachableFromTheSanctuary() {
        // Walk the straight line from the centre to each landmark; the island body should keep it
        // almost entirely on land, so the shrine is reachable from the central sanctuary.
        for (DrugId drug : CuratedDrugChain.ORDER) {
            BlockPos landmark = InnerRegionMap.landmarkFor(CENTER_X, CENTER_Z, drug);
            int steps = 120;
            int land = 0;
            for (int i = 0; i <= steps; i++) {
                double t = i / (double) steps;
                int x = (int) Math.round(CENTER_X + (landmark.getX() - CENTER_X) * t);
                int z = (int) Math.round(CENTER_Z + (landmark.getZ() - CENTER_Z) * t);
                if (InnerTerrain.sample(CENTER_X, CENTER_Z, x, z).land()) {
                    land++;
                }
            }
            assertTrue(land >= (steps + 1) * 0.9,
                    drug + " landmark is not reachable: only " + land + "/" + (steps + 1) + " steps are land");
        }
    }

    @Test
    void slotCenterSnapsToSpacingGrid() {
        int spacing = InnerDimensionConstants.SLOT_SPACING;
        assertEquals(0, InnerTerrain.slotCenter(10));
        assertEquals(spacing, InnerTerrain.slotCenter(spacing - 10));
        assertEquals(spacing, InnerTerrain.slotCenter(spacing + 10));
        for (int i = -5; i <= 5; i++) {
            int snapped = InnerTerrain.slotCenter(i * spacing + 3);
            assertEquals(0, snapped % spacing, "slot centre must land on the spacing grid");
        }
    }

    @Test
    void seedForSlotIsDeterministicAndDistinct() {
        long a = InnerTerrain.seedForSlot(0, 0);
        long b = InnerTerrain.seedForSlot(0, 0);
        long c = InnerTerrain.seedForSlot(InnerDimensionConstants.SLOT_SPACING, 0);
        assertEquals(a, b, "seed must be deterministic for a slot");
        assertTrue(a != c, "distinct slots should generally produce distinct seeds");
    }
}
