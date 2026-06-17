package org.mydrugs.mydrugs.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.network.InnerSkyStatePayload;

import java.util.List;
import java.util.Set;

class InnerSkyClientStateTest {

    @AfterEach
    void tearDown() {
        InnerSkyClientState.clear();
    }

    @Test
    void integratedDrugsReturnsUnmodifiableSet() {
        Set<DrugId> view = InnerSkyClientState.integratedDrugs();
        assertTrue(view.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> view.add(DrugId.WEED));
        assertThrows(UnsupportedOperationException.class, view::clear);
    }

    @Test
    void integratedDrugsReturnsDetachedSnapshotAndPreventsExternalMutation() {
        // Initially empty.
        assertTrue(InnerSkyClientState.integratedDrugs().isEmpty());

        // Simulate payload acceptance (internal mutation only).
        InnerSkyClientState.accept(new InnerSkyStatePayload(List.of(
                DrugId.WEED.networkId(),
                DrugId.LSD.networkId()
        )));

        // The view reflects the internal state.
        Set<DrugId> view = InnerSkyClientState.integratedDrugs();
        assertEquals(2, view.size());
        assertTrue(view.contains(DrugId.WEED));
        assertTrue(view.contains(DrugId.LSD));

        // But cannot mutate via the view.
        assertThrows(UnsupportedOperationException.class, () -> view.remove(DrugId.WEED));
        assertEquals(2, InnerSkyClientState.integratedCount());

        InnerSkyClientState.accept(new InnerSkyStatePayload(List.of(DrugId.COFFEE.networkId())));
        assertEquals(Set.of(DrugId.WEED, DrugId.LSD), view);
        assertEquals(1, InnerSkyClientState.integratedCount());
        assertTrue(InnerSkyClientState.isIntegrated(DrugId.COFFEE));
        assertFalse(InnerSkyClientState.isIntegrated(DrugId.WEED));
    }

    @Test
    void isIntegratedAndCountAreConsistentWithView() {
        InnerSkyClientState.accept(new InnerSkyStatePayload(List.of(
                DrugId.WEED.networkId(),
                DrugId.COFFEE.networkId(),
                DrugId.TOBACCO.networkId()
        )));

        Set<DrugId> view = InnerSkyClientState.integratedDrugs();
        assertEquals(3, InnerSkyClientState.integratedCount());
        assertEquals(3, view.size());

        for (DrugId drug : view) {
            assertTrue(InnerSkyClientState.isIntegrated(drug));
        }
        assertFalse(InnerSkyClientState.isIntegrated(DrugId.LSD));
    }
}
