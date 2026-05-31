package org.mydrugs.mydrugs.dimension.inner;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnerRegionMapTest {

    private static final double TWO_PI = Math.PI * 2.0D;

    @Test
    void blendWeightsAlwaysSumToOne() {
        for (int i = 0; i < 2000; i++) {
            double angle = (i / 2000.0D) * TWO_PI;
            InnerRegionMap.RegionBlend blend = InnerRegionMap.regionBlend(angle);
            assertEquals(1.0D, blend.primaryWeight() + blend.secondaryWeight(), 1.0e-9D,
                    "weights must sum to 1 at angle " + angle);
            assertTrue(blend.secondaryWeight() >= 0.0D && blend.secondaryWeight() <= 0.5D,
                    "secondary weight out of [0,0.5]: " + blend.secondaryWeight());
        }
    }

    @Test
    void blendPrimaryMatchesDominantRegion() {
        for (int i = 0; i < 2000; i++) {
            double angle = (i / 2000.0D) * TWO_PI - Math.PI;
            assertEquals(InnerRegionMap.dominantDrugForAngle(angle), InnerRegionMap.regionBlend(angle).primary(),
                    "blend primary must match dominant region at angle " + angle);
        }
    }

    @Test
    void sectorCentersArePurePrimary() {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            InnerRegionMap.RegionBlend blend = InnerRegionMap.regionBlend(InnerRegionMap.angleFor(drug));
            assertEquals(drug, blend.primary());
            assertEquals(0.0D, blend.secondaryWeight(), 1.0e-9D,
                    "a sector centre must be pure primary for " + drug);
        }
    }

    @Test
    void sectorBoundaryBlendsTwoDistinctRegions() {
        int n = CuratedDrugChain.ORDER.size();
        double sectorSize = TWO_PI / n;
        for (int i = 0; i < n; i++) {
            double boundary = i * sectorSize; // exact seam between sectors i-1 and i
            InnerRegionMap.RegionBlend blend = InnerRegionMap.regionBlend(boundary + 1.0e-6D);
            assertNotEquals(blend.primary(), blend.secondary(), "a boundary must mingle two regions");
            assertTrue(blend.secondaryWeight() > 0.45D,
                    "secondary weight should approach 0.5 at the seam, was " + blend.secondaryWeight());
        }
    }

    @Test
    void dominantRegionCoversEveryDrugOnce() {
        boolean[] seen = new boolean[CuratedDrugChain.ORDER.size()];
        for (int i = 0; i < 4000; i++) {
            double angle = (i / 4000.0D) * TWO_PI;
            DrugId drug = InnerRegionMap.dominantDrugForAngle(angle);
            seen[CuratedDrugChain.ORDER.indexOf(drug)] = true;
        }
        for (int i = 0; i < seen.length; i++) {
            assertTrue(seen[i], "region " + CuratedDrugChain.ORDER.get(i) + " never appears around the circle");
        }
    }

    @Test
    void angularDistanceIsSymmetricAndWrapped() {
        assertEquals(0.0D, InnerRegionMap.angularDistance(0.1D, 0.1D), 1.0e-9D);
        assertEquals(0.2D, InnerRegionMap.angularDistance(0.0D, TWO_PI - 0.2D), 1.0e-9D);
        assertTrue(InnerRegionMap.angularDistance(0.0D, Math.PI) <= Math.PI + 1.0e-9D);
    }
}
