package org.mydrugs.mydrugs.dimension.inner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

class InnerTerrainTest {

    @Test
    void centerIsLandAndSpawnIsAboveSurface() {
        InnerTerrain.Sample center = InnerTerrain.sample(0, 0, 0, 0);

        assertTrue(center.land());
        assertEquals(InnerDimensionConstants.BASE_Y + 5, center.topY());
        assertEquals(center.topY() + 1, InnerTerrain.safeSpawnY(0, 0));
    }

    @Test
    void chunkLandHintAcceptsCenterAndRejectsFarVoidBand() {
        assertTrue(InnerTerrain.chunkMayHaveLand(0, 0));
        assertFalse(InnerTerrain.chunkMayHaveLand(InnerDimensionConstants.SLOT_SPACING / 2 - 32, 0));
    }

    @Test
    void everyCuratedDrugHasRegionProfileAndLandmark() {
        for (DrugId drugId : CuratedDrugChain.ORDER) {
            assertTrue(InnerRegionMap.hasAngle(drugId), drugId.serializedName());
            assertTrue(InnerTerrainProfile.hasProfile(drugId), drugId.serializedName());
            assertNotNull(InnerTerrainProfile.forDrug(drugId).nodeBlock(), drugId.serializedName());
            assertNotNull(InnerTerrainProfile.forDrug(drugId).accent(), drugId.serializedName());

            BlockPos landmark = InnerRegionMap.landmarkFor(0, 0, drugId);
            InnerTerrain.Sample sample = InnerTerrain.sample(0, 0, landmark.getX(), landmark.getZ());
            assertTrue(sample.land(), drugId.serializedName());
            assertEquals(drugId, sample.drugId(), drugId.serializedName());
        }
    }
}
