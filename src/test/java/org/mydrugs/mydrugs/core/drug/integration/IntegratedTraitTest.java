package org.mydrugs.mydrugs.core.drug.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.mutation.MutationStat;

class IntegratedTraitTest {

    @Test
    void curatedDrugsHaveExactlyOneIntegratedTraitEach() {
        assertEquals(CuratedDrugChain.ORDER.size(), IntegratedTrait.values().length);

        Set<DrugId> seen = EnumSet.noneOf(DrugId.class);
        for (IntegratedTrait trait : IntegratedTrait.values()) {
            assertTrue(CuratedDrugChain.ORDER.contains(trait.source()), trait.source() + " must be curated");
            assertTrue(seen.add(trait.source()), trait.source() + " has more than one trait");
            assertSame(trait, IntegratedTrait.bySource(trait.source()));
            assertSame(trait, IntegratedTrait.bySerializedName(trait.serializedName()).orElseThrow());
        }
    }

    @Test
    void traitMappingMatchesTheMasterPlan() {
        Map<DrugId, ExpectedTrait> expected = new EnumMap<>(DrugId.class);
        expected.put(DrugId.COFFEE, new ExpectedTrait("clear_focus", EffectType.MANUAL_WORK_SPEED));
        expected.put(DrugId.TOBACCO, new ExpectedTrait("steady_hands", EffectType.PRECISION));
        expected.put(DrugId.WEED, new ExpectedTrait("even_keel", EffectType.STRESS_RESISTANCE));
        expected.put(DrugId.HASH, new ExpectedTrait("fine_motor", EffectType.PRECISION));
        expected.put(DrugId.ALCOHOL, new ExpectedTrait("hardened", EffectType.DAMAGE_RESISTANCE));
        expected.put(DrugId.COCAINE, new ExpectedTrait("quickstep", EffectType.MOVEMENT_SPEED));
        expected.put(DrugId.LSD, new ExpectedTrait("richer_seams", EffectType.ORE_FORTUNE));
        expected.put(DrugId.METH, new ExpectedTrait("overdrive_memory", EffectType.MINING_SPEED));
        expected.put(DrugId.MUSHROOMS, new ExpectedTrait("structural_sense", EffectType.MULTIBLOCK_VISION));

        for (Map.Entry<DrugId, ExpectedTrait> entry : expected.entrySet()) {
            IntegratedTrait trait = IntegratedTrait.bySource(entry.getKey());
            assertEquals(entry.getValue().id(), trait.serializedName());
            assertEquals(entry.getValue().effect(), trait.effect());
            assertTrue(trait.magnitude() > 0.0F);
        }
    }

    @Test
    void nonCuratedDrugsDoNotGainIntegratedTraits() {
        assertNull(IntegratedTrait.bySource(DrugId.CRACK));
        assertNull(IntegratedTrait.bySource(null));
    }

    @Test
    void integratedTraitsAreSeparateFromMutationStats() {
        for (IntegratedTrait trait : IntegratedTrait.values()) {
            assertTrue(MutationStat.bySerializedName(trait.serializedName()).isEmpty());
        }
    }

    private record ExpectedTrait(String id, EffectType effect) {
    }
}
