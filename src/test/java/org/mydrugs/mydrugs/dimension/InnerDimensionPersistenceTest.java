package org.mydrugs.mydrugs.dimension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.inner.InnerDimensionConstants;

import java.util.UUID;

class InnerDimensionPersistenceTest {

    @Test
    void integrationRecordsEachDrugOnlyOncePerOwner() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000001");

        assertTrue(data.recordIntegration(owner, DrugId.COFFEE));
        assertFalse(data.recordIntegration(owner, DrugId.COFFEE));

        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        assertEquals(1, island.integratedCount());
        assertTrue(island.integratedDrugs().contains(DrugId.COFFEE));
    }

    @Test
    void semanticOverlayMarkersAreRecordedOnce() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String marker = InnerDimensionConstants.landmarkMarker(DrugId.COFFEE);

        assertTrue(data.markStructurePlaced(owner, marker));
        assertFalse(data.markStructurePlaced(owner, marker));

        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        assertTrue(island.placedMarkers().contains(marker));
        assertEquals(1, island.placedMarkers().size());
    }

    @Test
    void playerIslandStateDoesNotCrossContaminate() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID first = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID second = UUID.fromString("00000000-0000-0000-0000-000000000002");

        InnerDimensionSavedData.IslandState firstIsland = data.getOrCreateIsland(first);
        InnerDimensionSavedData.IslandState secondIsland = data.getOrCreateIsland(second);

        assertTrue(data.recordIntegration(first, DrugId.COFFEE));
        assertTrue(firstIsland.integratedDrugs().contains(DrugId.COFFEE));
        assertFalse(secondIsland.integratedDrugs().contains(DrugId.COFFEE));
        assertEquals(InnerDimensionConstants.SLOT_SPACING, secondIsland.centerX() - firstIsland.centerX());
        assertEquals(0, secondIsland.centerZ() - firstIsland.centerZ());
    }
}
