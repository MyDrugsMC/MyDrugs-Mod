package org.mydrugs.mydrugs.mutation.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.items.data.MutationStatValue;
import org.mydrugs.mydrugs.mutation.GeneticRarityTier;
import org.mydrugs.mydrugs.mutation.MutationStat;
import org.mydrugs.mydrugs.mutation.PlayerMutationsAttachment;

class MutationFragilityTest {

    @Test
    void mutationValueMapsToTierEquivalent() {
        assertEquals(GeneticRarityTier.COMMON, tierFor(0.01F));
        assertEquals(GeneticRarityTier.UNCOMMON, tierFor(0.20F));
        assertEquals(GeneticRarityTier.RARE, tierFor(0.40F));
        assertEquals(GeneticRarityTier.DANGEROUS, tierFor(0.60F));
        assertEquals(GeneticRarityTier.MYTHIC, tierFor(0.80F));
        assertNull(MutationFragilityEvents.strongestActiveStatTier(new PlayerMutationsAttachment()));
    }

    @Test
    void equalOrHigherTierHitsDecayBorrowedStats() {
        assertTrue(MutationFragilityEvents.shouldDecayForHit(GeneticRarityTier.COMMON, GeneticRarityTier.COMMON));
        assertFalse(MutationFragilityEvents.shouldDecayForHit(GeneticRarityTier.RARE, GeneticRarityTier.MYTHIC));
        assertTrue(MutationFragilityEvents.shouldDecayForHit(GeneticRarityTier.MYTHIC, GeneticRarityTier.RARE));
    }

    @Test
    void decayOnlyTouchesMutationAttachment() {
        PlayerMutationsAttachment attachment = new PlayerMutationsAttachment();
        attachment.replaceStats(List.of(new MutationStatValue(MutationStat.HEALTH_STABILITY.serializedName(), 0.60F, 0.0F)));

        assertTrue(attachment.decayMutations(MutationFragilityEvents.MUTATION_HIT_DECAY));

        float valueAfterDecay = attachment.snapshotCurrent().get(MutationStat.HEALTH_STABILITY.serializedName());
        assertEquals(0.45F, valueAfterDecay, 1e-6F);
    }

    private static GeneticRarityTier tierFor(float value) {
        PlayerMutationsAttachment attachment = new PlayerMutationsAttachment();
        attachment.replaceStats(List.of(new MutationStatValue(MutationStat.HEALTH_STABILITY.serializedName(), value, 0.0F)));
        return MutationFragilityEvents.strongestActiveStatTier(attachment);
    }
}
