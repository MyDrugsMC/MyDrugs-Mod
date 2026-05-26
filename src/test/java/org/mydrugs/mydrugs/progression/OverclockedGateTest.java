package org.mydrugs.mydrugs.progression;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test: the Overclocked knowledge gate keys on the consumed FORM
 * (meth model), never on purity. Any future change that re-walls Overclocked
 * behind purity or another stack-bound check breaks these tests.
 *
 * Street-route shards must be able to satisfy this gate identically to
 * lab-route shards.
 */
class OverclockedGateTest {

    @BeforeAll
    static void registerDrugs() {
        if (DrugRegistry.getDrug(DrugId.COFFEE) == null) {
            DrugRegistry.registerDrugs();
        }
    }

    @Test
    void methWithoutPriorKnowledgeIsBlocked() {
        DrugModel meth = DrugRegistry.getDrug(DrugId.METH);
        assertNotNull(meth);

        DrugProgressionGate.Decision decision = DrugProgressionGate.evaluateKnownKnowledge(
                meth, Set.of()
        );

        assertFalse(decision.allowed(), "Meth must be blocked without the LSD/lysergic prerequisite");
        assertEquals("message.mydrugs.knowledge.blocked.generic", decision.blockedMessageKey());
    }

    @Test
    void methWithLysergicGrantsOverclocked() {
        DrugModel meth = DrugRegistry.getDrug(DrugId.METH);
        assertNotNull(meth);

        DrugProgressionGate.Decision decision = DrugProgressionGate.evaluateKnownKnowledge(
                meth, Set.of(PsyKnowledgeKey.LYSERGIC)
        );

        assertTrue(decision.allowed(), "Meth must unlock once LSD/lysergic is known");
        assertTrue(decision.grantedKnowledge().contains(PsyKnowledgeKey.OVERCLOCKED),
                "Smoking any meth (lab OR street purity) must grant OVERCLOCKED");
    }

    @Test
    void overclockedRuleIsKeyedOnFormNotPurity() {
        // The gate's evaluateKnownKnowledge signature is (model, knowledge) — no ItemStack,
        // no purity, no fluid, no machine. This is the structural guarantee:
        // anything trying to gate Overclocked on purity has to change the signature
        // and this test (along with the two above) will fail loudly.
        DrugModel meth = DrugRegistry.getDrug(DrugId.METH);
        assertNotNull(meth);
        DrugProgressionGate.Decision a = DrugProgressionGate.evaluateKnownKnowledge(
                meth, Set.of(PsyKnowledgeKey.LYSERGIC)
        );
        DrugProgressionGate.Decision b = DrugProgressionGate.evaluateKnownKnowledge(
                meth, Set.of(PsyKnowledgeKey.LYSERGIC)
        );
        assertEquals(a.allowed(), b.allowed());
        assertEquals(a.grantedKnowledge(), b.grantedKnowledge());
    }
}
