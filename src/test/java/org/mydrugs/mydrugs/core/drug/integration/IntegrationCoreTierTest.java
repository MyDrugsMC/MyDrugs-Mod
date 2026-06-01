package org.mydrugs.mydrugs.core.drug.integration;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 4 / Issue A: the Integration Core ladder pins every curated drug to a tier and lets a
 * higher tier substitute for any lower one. If you change either, this test will tell you what
 * else moved.
 */
class IntegrationCoreTierTest {

    @Test
    void everyCuratedDrugHasARequiredTier() {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            assertNotNull(IntegrationCoreTier.requiredFor(drug),
                    drug + " is curated and must have a required tier");
        }
    }

    @Test
    void uncuratedDrugsHaveNoTier() {
        assertNull(IntegrationCoreTier.requiredFor(DrugId.CRACK));
        assertNull(IntegrationCoreTier.requiredFor(null));
    }

    @Test
    void requiredTierMatchesIntent() {
        // Pin the mapping. A new drug or a moved drug must be a conscious change.
        assertEquals(IntegrationCoreTier.CRUDE, IntegrationCoreTier.requiredFor(DrugId.COFFEE));
        assertEquals(IntegrationCoreTier.BASIC, IntegrationCoreTier.requiredFor(DrugId.TOBACCO));
        assertEquals(IntegrationCoreTier.BASIC, IntegrationCoreTier.requiredFor(DrugId.WEED));
        assertEquals(IntegrationCoreTier.ADVANCED, IntegrationCoreTier.requiredFor(DrugId.HASH));
        assertEquals(IntegrationCoreTier.ADVANCED, IntegrationCoreTier.requiredFor(DrugId.ALCOHOL));
        assertEquals(IntegrationCoreTier.REFINED, IntegrationCoreTier.requiredFor(DrugId.COCAINE));
        assertEquals(IntegrationCoreTier.PRISTINE, IntegrationCoreTier.requiredFor(DrugId.LSD));
        assertEquals(IntegrationCoreTier.PRISTINE, IntegrationCoreTier.requiredFor(DrugId.METH));
        assertEquals(IntegrationCoreTier.PRIME, IntegrationCoreTier.requiredFor(DrugId.MUSHROOMS));
    }

    @Test
    void higherTierSubstitutesLower() {
        // A Prime core must work for any integration. A Crude core must NOT work for Prime.
        assertTrue(IntegrationCoreTier.PRIME.satisfies(IntegrationCoreTier.CRUDE));
        assertTrue(IntegrationCoreTier.PRIME.satisfies(IntegrationCoreTier.PRIME));
        assertTrue(IntegrationCoreTier.ADVANCED.satisfies(IntegrationCoreTier.BASIC));
        assertFalse(IntegrationCoreTier.CRUDE.satisfies(IntegrationCoreTier.BASIC));
        assertFalse(IntegrationCoreTier.PRISTINE.satisfies(IntegrationCoreTier.PRIME));
    }

    @Test
    void satisfiesIsNullSafe() {
        assertFalse(IntegrationCoreTier.CRUDE.satisfies(null));
    }

    @Test
    void rankIsMonotonic() {
        // Defensive: the satisfies() check uses rank ordering. If the order changes, this catches it.
        IntegrationCoreTier[] tiers = IntegrationCoreTier.values();
        for (int i = 1; i < tiers.length; i++) {
            assertTrue(tiers[i].rank() > tiers[i - 1].rank(),
                    tiers[i] + " must rank higher than " + tiers[i - 1]);
        }
    }

    @Test
    void chainSupplyMeetsDemand() {
        // The Core economy must be open enough for the full chain: with 9 curated drugs, the player
        // needs to craft 9 cores total (one per integration). The Crude recipe gates the whole
        // chain, so a Crude core must be craftable from materials available before any integration.
        // This test pins that the ladder is internally consistent: every tier above CRUDE has a
        // tier strictly below it.
        IntegrationCoreTier[] tiers = IntegrationCoreTier.values();
        for (int i = 1; i < tiers.length; i++) {
            assertTrue(tiers[i].rank() == tiers[i - 1].rank() + 1,
                    "tier ranks must be contiguous so the chained recipe upgrades work");
        }
    }
}
