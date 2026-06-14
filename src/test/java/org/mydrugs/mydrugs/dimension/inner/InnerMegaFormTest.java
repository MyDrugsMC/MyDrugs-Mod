package org.mydrugs.mydrugs.dimension.inner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

/** Geometry guarantees for the nine region mega-forms (A2). */
class InnerMegaFormTest {

    @Test
    void everyRegionHasAFormInsideItsOwnSectorAndOffThePath() {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            InnerMegaForms.Form form = InnerMegaForms.formFor(0, 0, drug);
            double distance = Math.hypot(form.x(), form.z());
            assertTrue(distance > InnerDimensionConstants.CORE_RADIUS + 60,
                    drug + " form too close to the sanctuary");
            assertTrue(distance + form.radius() < InnerDimensionConstants.ISLAND_RADIUS,
                    drug + " form must sit inside the island");
            double angle = Math.atan2(form.z(), form.x());
            double offSpoke = InnerRegionMap.angularDistance(angle, InnerRegionMap.angleFor(drug));
            assertTrue(offSpoke > 0.10D, drug + " form sits on its spoke (paths must pass beside it)");
            assertTrue(offSpoke < Math.PI / CuratedDrugChain.ORDER.size(),
                    drug + " form left its own sector");
            assertEquals(drug, InnerRegionMap.dominantDrug(0, 0, form.x(), form.z()),
                    drug + " form centre must lie in its own region");
        }
    }

    @Test
    void formPositionsAreDeterministicAndPerSlot() {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            assertEquals(InnerMegaForms.formFor(0, 0, drug), InnerMegaForms.formFor(0, 0, drug));
        }
        int otherCenter = InnerTerrain.slotCenter(123_456);
        boolean anyDifferent = false;
        for (DrugId drug : CuratedDrugChain.ORDER) {
            InnerMegaForms.Form a = InnerMegaForms.formFor(0, 0, drug);
            InnerMegaForms.Form b = InnerMegaForms.formFor(otherCenter, otherCenter, drug);
            if ((a.x() - 0) != (b.x() - otherCenter) || (a.z() - 0) != (b.z() - otherCenter)) {
                anyDifferent = true;
            }
        }
        assertTrue(anyDifferent, "different islands should place forms differently");
    }

    @Test
    void plinthShapesTheGroundUnderEveryForm() {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            InnerMegaForms.Form form = InnerMegaForms.formFor(0, 0, drug);
            InnerTerrain.Sample centre = InnerTerrain.sample(0, 0, form.x(), form.z());
            assertTrue(centre.land(), drug + " form centre must be solid ground");
            assertEquals(InnerMegaForms.plinthY(form, form.x(), form.z()), centre.topY(),
                    drug + " ground must match the plinth at the form centre");
            assertTrue(!centre.lake() && !centre.hole() && !centre.spikeField(),
                    drug + " form interior must be calmed (no lakes/holes/spikes)");
        }
    }

    @Test
    void faultSpiresSitOnTheRiftEdges() {
        InnerMegaForms.Form fault = InnerMegaForms.formFor(0, 0, DrugId.METH);
        int[][] spires = InnerMegaForms.faultSpireOffsets(fault);
        assertEquals(6, spires.length);
        for (int[] spire : spires) {
            assertTrue(fault.distance(spire[0], spire[1]) < fault.radius(),
                    "spire outside the fault footprint");
            assertTrue(fault.perp(spire[0], spire[1]) > 5.5D,
                    "spire must stand on the rift edge, not in the channel");
        }
    }
}
