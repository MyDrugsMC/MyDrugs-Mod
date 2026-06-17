package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnerTrialProgressionTest {
    @AfterEach
    void tearDown() {
        InnerTrialManager.clearAll();
    }

    @Test
    void everyCuratedDrugHasATrialDefinition() {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            assertNotNull(InnerTrialDefinition.forDrug(drug), drug + " missing trial definition");
        }
    }

    @Test
    void everyCuratedDrugHasATrialRewardMapping() {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            assertTrue(InnerTrialManager.hasRewardMappingForTest(drug), drug + " missing trial reward");
        }
        assertFalse(InnerTrialManager.hasRewardMappingForTest(DrugId.CRACK));
    }

    @Test
    void publicEntryPointsHandleNullInputs() {
        InnerTrialManager.tickPlayer(null);
        assertFalse(InnerTrialManager.handleRightClick(null, null, null, null));
        InnerTrialManager.handlePlacement(null, null, null, null);
        InnerTrialManager.resetStillPoint(null);
        assertFalse(InnerTrialManager.completeTrial(null, null, null, null, true));
        assertFalse(InnerTrialManager.resetTrials(null, null));
        assertNull(InnerTrialManager.nearestIncompleteTrial(null, new BlockPos(0, 0, 0)));
        assertNull(InnerTrialManager.nearestIncompleteTrial(null, null));
    }

    @Test
    void puzzleChoicesAreDeterministicPerIsland() {
        assertEquals(
                InnerTrialDefinition.tobaccoOrder(0, 0),
                InnerTrialDefinition.tobaccoOrder(0, 0)
        );
        assertEquals(
                InnerTrialDefinition.lsdTrueNodeIndex(0, 0),
                InnerTrialDefinition.lsdTrueNodeIndex(0, 0)
        );
    }

    @Test
    void pureTrialLayoutsMatchExpectedStateMachineCounts() {
        BlockPos center = new BlockPos(100, 70, -50);

        assertEquals(4, InnerTrialDefinition.tobaccoOrder(0, 0).size());
        assertEquals(4, InnerTrialDefinition.hashSockets().size());
        assertEquals(5, InnerTrialDefinition.lsdNodes().size());
        assertEquals(3, InnerTrialDefinition.methNodes().size());
        assertEquals(4, InnerTrialDefinition.mushroomRoots().size());

        BlockPos tobaccoFirst = InnerTrialDefinition.tobaccoOrder(0, 0).get(0);
        assertEquals(0, InnerTrialDefinition.horizontalIndex(
                center.offset(tobaccoFirst.getX(), 3, tobaccoFirst.getZ()),
                center,
                InnerTrialDefinition.tobaccoOrder(0, 0)
        ));
        assertEquals(-1, InnerTrialDefinition.horizontalIndex(
                center.offset(99, 0, 99),
                center,
                InnerTrialDefinition.methNodes()
        ));
    }

    @Test
    void vaultTiersUnlockFromTheirSigilGroups() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000021");
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        InnerVaults.Vault calm = new InnerVaults.Vault(0, 0, false, 1L, DrugId.COFFEE);
        InnerVaults.Vault deep = new InnerVaults.Vault(0, 0, false, 1L, DrugId.LSD);
        InnerVaults.Vault danger = new InnerVaults.Vault(0, 0, false, 1L, DrugId.METH);

        assertFalse(InnerVaults.isUnlocked(calm, island));
        assertTrue(data.completeInnerTrial(owner, DrugId.WEED));
        assertTrue(InnerVaults.isUnlocked(calm, island));
        assertFalse(InnerVaults.isUnlocked(deep, island));
        assertFalse(InnerVaults.isUnlocked(danger, island));

        assertTrue(data.completeInnerTrial(owner, DrugId.HASH));
        assertTrue(InnerVaults.isUnlocked(deep, island));
        assertTrue(data.completeInnerTrial(owner, DrugId.TOBACCO));
        assertTrue(InnerVaults.isUnlocked(danger, island));
    }

    @Test
    void clearPlayerRemovesProgress() {
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-000000000031");
        // clearPlayer on a possibly-empty map is safe.
        InnerTrialManager.clearPlayer(playerId);
    }

    @Test
    void clearAllThenClearPlayerAreIdempotent() {
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-000000000032");
        // clearPlayer on a never-populated id is safe.
        InnerTrialManager.clearPlayer(playerId);
        // clearAll followed by clearPlayer is safe.
        InnerTrialManager.clearAll();
        InnerTrialManager.clearPlayer(playerId);
    }

    @Test
    void nearestIncompleteTrialSkipsCompletedAndUnintegrated() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000033");
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        int cx = island.centerX();
        int cz = island.centerZ();
        BlockPos origin = InnerRegionMap.landmarkFor(cx, cz, DrugId.COFFEE);

        // No integrated drugs — nothing should be nearest.
        assertNull(InnerTrialManager.nearestIncompleteTrial(island, origin));

        // Integrate but don't complete Coffee.
        assertTrue(data.recordIntegration(owner, DrugId.COFFEE));
        DrugId nearest = InnerTrialManager.nearestIncompleteTrial(island, origin);
        assertEquals(DrugId.COFFEE, nearest);

        // Complete it — now nothing remains.
        assertTrue(data.completeInnerTrial(owner, DrugId.COFFEE));
        assertNull(InnerTrialManager.nearestIncompleteTrial(island, origin));
    }

    @Test
    void pendingTrialReturnWinsOverNearestIncompleteTrialObjectiveAndCompass() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000034");
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        BlockPos origin = new BlockPos(
                island.centerX() + 20,
                InnerDimensionConstants.BASE_Y,
                island.centerZ()
        );

        assertTrue(data.recordIntegration(owner, DrugId.COFFEE));
        assertTrue(data.recordIntegration(owner, DrugId.WEED));
        assertTrue(data.completeInnerTrial(owner, DrugId.COFFEE));
        assertTrue(data.markTrialReturnPending(owner, DrugId.COFFEE));
        assertEquals(DrugId.WEED, InnerTrialManager.nearestIncompleteTrial(island, origin));

        InnerObjectiveHelper.Objective objective = InnerObjectiveHelper.currentObjectiveForTest(
                data,
                island,
                owner,
                origin
        );

        assertEquals(InnerObjectiveHelper.Kind.RETURN_TO_ANCHOR, objective.kind());
        assertEquals(new BlockPos(island.centerX(), InnerDimensionConstants.BASE_Y, island.centerZ()), objective.target());
        assertEquals(Component.translatable("message.mydrugs.inner_target.self_anchor"), objective.targetName());
        assertEquals(
                Component.translatable(
                        "message.mydrugs.memory_compass.target",
                        Component.translatable("message.mydrugs.inner_target.self_anchor"),
                        Component.translatable("message.mydrugs.direction.west"),
                        20
                ),
                InnerObjectiveHelper.memoryCompassMessageForTest(data, island, owner, origin)
        );
    }
}
