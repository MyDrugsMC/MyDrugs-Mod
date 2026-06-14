package org.mydrugs.mydrugs.dimension.inner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

/** Sanctuary sightline wedges and preserve-open-view guarantees (P10). */
class InnerSightlineAndOpenViewTest {

    @Test
    void sightlineWedgeCoversEverySpokeJustPastTheCoreRim() {
        for (DrugId drugId : CuratedDrugChain.ORDER) {
            double angle = InnerRegionMap.angleFor(drugId);
            int distance = InnerDimensionConstants.CORE_RADIUS + 30;
            int x = (int) Math.round(Math.cos(angle) * distance);
            int z = (int) Math.round(Math.sin(angle) * distance);
            assertTrue(InnerRegionMap.inCoreSightlineWedge(0, 0, x, z),
                    drugId + " spoke axis should be inside its sightline wedge");
        }
    }

    @Test
    void sightlineWedgeDoesNotCoverTheCoreOrFarTerrain() {
        assertFalse(InnerRegionMap.inCoreSightlineWedge(0, 0, 10, 10),
                "core interior is not a wedge (the core is already flat and clear)");
        double angle = InnerRegionMap.angleFor(CuratedDrugChain.ORDER.get(0));
        int far = InnerDimensionConstants.CORE_RADIUS + 400;
        assertFalse(InnerRegionMap.inCoreSightlineWedge(0, 0,
                        (int) Math.round(Math.cos(angle) * far), (int) Math.round(Math.sin(angle) * far)),
                "wedges end past the rim reach");
    }

    @Test
    void sightlineWedgeIsDeterministic() {
        for (int x = 100; x < 260; x += 7) {
            assertEquals(InnerRegionMap.inCoreSightlineWedge(0, 0, x, 31),
                    InnerRegionMap.inCoreSightlineWedge(0, 0, x, 31));
        }
    }

    @Test
    void treesNeverHostOnOpenViewColumnsOutsideGroveScenes() {
        long seed = InnerTerrain.seedForSlot(0, 0);
        int checked = 0;
        for (int x = -900; x <= 900; x += 23) {
            for (int z = -900; z <= 900; z += 29) {
                InnerTerrain.Sample sample = InnerTerrain.sample(0, 0, x, z);
                if (!sample.land()) {
                    continue;
                }
                InnerGroveSample grove = InnerGroveSampler.sample(seed, 0, 0, x, z, sample);
                InnerSceneSample scene = InnerSceneSampler.sample(seed, 0, 0, x, z, sample, grove);
                if (!scene.preserveOpenView()
                        || scene.type() == InnerSceneType.DENSE_GROVE
                        || scene.type() == InnerSceneType.HERO_TREE_GROVE) {
                    continue;
                }
                checked++;
                assertFalse(InnerTreeBuilder.canHostTreeForTest(sample, grove, scene),
                        "tree allowed on preserve-open-view column at " + x + "," + z + " scene=" + scene.type());
            }
        }
        assertTrue(checked > 0, "expected to check some open-view columns");
    }
}
