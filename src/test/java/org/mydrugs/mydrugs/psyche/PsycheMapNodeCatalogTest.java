package org.mydrugs.mydrugs.psyche;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PsycheMapNodeCatalogTest {
    private static final List<String> ORIGINAL_NODE_IDS = List.of(
            "caffeine", "nicotinic", "cannabinoid", "fermented", "stimulant",
            "lysergic", "overclocked", "mycelial", "steel_plating",
            "first_diary_entry", "first_bad_trip", "first_recovery_anchor", "first_sanctuary",
            "first_therapist_visit", "first_psy_mixer_ritual", "first_ritual_success",
            "first_ritual_failure", "first_named_formula", "first_mutation", "first_infection",
            "first_inner_demon", "first_demon_defeated", "first_psychotrope_energy"
    );

    @Test
    void originalKnowledgeAndMilestoneNodesRemainInCatalog() {
        for (String path : ORIGINAL_NODE_IDS) {
            PsycheMapNodeCatalog.Entry entry = PsycheMapNodeCatalog.byId(MyDrugs.MODID + ":" + path);
            assertNotNull(entry, "Missing original Psyche Map node: " + path);
            assertFalse(entry.isIntegrationNode());
        }
        assertEquals(
                ORIGINAL_NODE_IDS.size() + CuratedDrugChain.ORDER.size(),
                PsycheMapNodeCatalog.totalNodes()
        );
    }

    @Test
    void everyIntegrationNodeIsLinkedToItsCuratedKnowledgeNode() {
        for (DrugId drugId : CuratedDrugChain.ORDER) {
            PsyKnowledgeKey knowledge = CuratedDrugChain.stageKnowledge(drugId);
            PsycheMapNodeCatalog.Entry entry = PsycheMapNodeCatalog.byDrug(drugId);
            assertNotNull(knowledge);
            assertNotNull(entry);
            assertTrue(entry.isIntegrationNode());
            assertEquals(MyDrugs.MODID + ":" + drugId.serializedName(), entry.nodeId);
            assertEquals(drugId, entry.integrationDrugId);
            assertEquals(knowledge.id().toString(), entry.knowledgeNodeId);
            assertEquals(List.of(knowledge.id().toString()), entry.parents);
            assertEquals(knowledge.translationKey(), entry.knowledgeTranslationKey());
        }
    }
}
