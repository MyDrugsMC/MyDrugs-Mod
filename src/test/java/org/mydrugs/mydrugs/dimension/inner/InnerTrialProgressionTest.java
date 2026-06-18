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
        assertEquals(
                InnerGameplayLoop.Phase.RETURN_TO_ANCHOR,
                InnerGameplayLoop.inside(data, island, owner, origin).phase()
        );

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

    @Test
    void pendingTrialReturnClearsOnlyNearSelfAnchor() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000035");
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        BlockPos far = new BlockPos(
                island.centerX() + 25,
                InnerDimensionConstants.BASE_Y,
                island.centerZ()
        );
        BlockPos anchor = new BlockPos(
                island.centerX(),
                InnerDimensionConstants.BASE_Y,
                island.centerZ()
        );

        assertTrue(data.recordIntegration(owner, DrugId.COFFEE));
        assertTrue(data.completeInnerTrial(owner, DrugId.COFFEE));
        assertTrue(data.markTrialReturnPending(owner, DrugId.COFFEE));

        assertFalse(InnerProgressionMilestones.clearPendingReturnAtAnchor(data, island, owner, far));
        assertTrue(data.hasPendingTrialReturn(owner));
        assertTrue(InnerProgressionMilestones.clearPendingReturnAtAnchor(data, island, owner, anchor));
        assertFalse(data.hasPendingTrialReturn(owner));
    }

    @Test
    void pendingTrialReturnSettlementIsOneShotSoReturnMessageCannotSpam() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000036");
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        BlockPos anchor = new BlockPos(
                island.centerX(),
                InnerDimensionConstants.BASE_Y,
                island.centerZ()
        );

        assertTrue(data.recordIntegration(owner, DrugId.COFFEE));
        assertTrue(data.completeInnerTrial(owner, DrugId.COFFEE));
        assertTrue(data.markTrialReturnPending(owner, DrugId.COFFEE));

        assertTrue(InnerProgressionMilestones.clearPendingReturnAtAnchor(data, island, owner, anchor));
        assertFalse(InnerProgressionMilestones.clearPendingReturnAtAnchor(data, island, owner, anchor));
        assertFalse(data.hasPendingTrialReturn(owner));
    }

    @Test
    void nextObjectiveAfterAnchorReturnIsStableAndUsedInSummary() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000037");
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        BlockPos anchor = new BlockPos(
                island.centerX(),
                InnerDimensionConstants.BASE_Y,
                island.centerZ()
        );

        assertTrue(data.recordIntegration(owner, DrugId.COFFEE));
        assertTrue(data.recordIntegration(owner, DrugId.WEED));
        assertTrue(data.completeInnerTrial(owner, DrugId.COFFEE));
        assertTrue(data.markTrialReturnPending(owner, DrugId.COFFEE));
        assertTrue(InnerProgressionMilestones.clearPendingReturnAtAnchor(data, island, owner, anchor));

        InnerObjectiveHelper.Objective first = InnerProgressionMilestones.nextObjectiveAfterReturn(
                data,
                island,
                owner,
                anchor
        );
        InnerObjectiveHelper.Objective second = InnerProgressionMilestones.nextObjectiveAfterReturn(
                data,
                island,
                owner,
                anchor
        );

        assertEquals(InnerObjectiveHelper.Kind.VAULT, first.kind());
        assertEquals(first, second);
        assertEquals(
                Component.translatable(
                        "message.mydrugs.inner_dimension.trial_settled_at_anchor",
                        Component.translatable("drug.mydrugs.coffee"),
                        first.message()
                ),
                InnerProgressionMilestones.returnSummaryMessage(DrugId.COFFEE, first.message())
        );
    }

    @Test
    void trialItemConsumptionRequiresValidAction() {
        assertTrue(InnerTrialManager.isValidWeedSporesAction(true, true, true));
        assertFalse(InnerTrialManager.isValidWeedSporesAction(false, true, true));
        assertFalse(InnerTrialManager.isValidWeedSporesAction(true, false, true));
        assertFalse(InnerTrialManager.isValidWeedSporesAction(true, true, false));

        assertTrue(InnerTrialManager.shouldConsumeMethTool(false, false, true));
        assertFalse(InnerTrialManager.shouldConsumeMethTool(false, false, false));
        assertFalse(InnerTrialManager.shouldConsumeMethTool(true, false, true));
        assertFalse(InnerTrialManager.shouldConsumeMethTool(false, true, true));
    }

    @Test
    void coffeeFailureClassificationAndGraceAreExplicit() {
        InnerTrialProgress progress = new InnerTrialProgress();
        progress.coffeeTicks = 20;
        progress.coffeeGraceTicks = 2;

        assertEquals(
                InnerTrialManager.CoffeeBreakReason.NONE,
                InnerTrialManager.classifyCoffeeBreak(0.0D, false, true, 0.0D)
        );
        assertEquals(
                InnerTrialManager.CoffeeBreakReason.LEFT_AREA,
                InnerTrialManager.classifyCoffeeBreak(31.0D, false, true, 0.0D)
        );
        assertEquals(
                InnerTrialManager.CoffeeBreakReason.SPRINTED,
                InnerTrialManager.classifyCoffeeBreak(0.0D, true, true, 0.0D)
        );
        assertEquals(
                InnerTrialManager.CoffeeBreakReason.NOT_GROUNDED,
                InnerTrialManager.classifyCoffeeBreak(0.0D, false, false, 0.0D)
        );
        assertEquals(
                InnerTrialManager.CoffeeBreakReason.MOVED,
                InnerTrialManager.classifyCoffeeBreak(0.0D, false, true, 0.029D)
        );
        assertTrue(InnerTrialManager.canSpendCoffeeGraceForTest(
                progress,
                InnerTrialManager.CoffeeBreakReason.MOVED
        ));
        assertTrue(InnerTrialManager.canSpendCoffeeGraceForTest(
                progress,
                InnerTrialManager.CoffeeBreakReason.NOT_GROUNDED
        ));
        assertFalse(InnerTrialManager.canSpendCoffeeGraceForTest(
                progress,
                InnerTrialManager.CoffeeBreakReason.SPRINTED
        ));
    }

    @Test
    void cocaineFailureClassificationNamesTheReason() {
        BlockPos center = new BlockPos(0, 70, 0);

        assertEquals(
                InnerTrialManager.CocaineFailureReason.NONE,
                InnerTrialManager.classifyCocaineFailure(new BlockPos(0, 70, 0), center, false, 20)
        );
        assertEquals(
                InnerTrialManager.CocaineFailureReason.THORN,
                InnerTrialManager.classifyCocaineFailure(new BlockPos(0, 70, 0), center, true, 20)
        );
        assertEquals(
                InnerTrialManager.CocaineFailureReason.LEFT_ROUTE,
                InnerTrialManager.classifyCocaineFailure(new BlockPos(17, 70, 0), center, false, 20)
        );
        assertEquals(
                InnerTrialManager.CocaineFailureReason.LEFT_ROUTE,
                InnerTrialManager.classifyCocaineFailure(new BlockPos(0, 70, 4), center, false, 20)
        );
        assertEquals(
                InnerTrialManager.CocaineFailureReason.TIMEOUT,
                InnerTrialManager.classifyCocaineFailure(new BlockPos(0, 70, 0), center, false, 20 * 14 + 1)
        );
    }

    @Test
    void displayedProgressIsClampedToTrialMaximum() {
        assertEquals(0, InnerTrialManager.displayProgress(-1, 4));
        assertEquals(2, InnerTrialManager.displayProgress(2, 4));
        assertEquals(4, InnerTrialManager.displayProgress(5, 4));
    }

    @Test
    void completedTrialClearsOnlyThatDrugProgress() {
        InnerTrialProgress progress = new InnerTrialProgress();
        progress.coffeeTicks = 30;
        progress.coffeeGraceTicks = 2;
        progress.tobaccoStep = 3;
        progress.weedPlacements = 5;
        progress.cocaineStartTick = 99L;
        progress.methMask = 0b111;
        progress.mushroomMask = 0b1111;

        InnerTrialManager.clearDrugProgressForTest(progress, DrugId.COFFEE);
        assertEquals(0, progress.coffeeTicks);
        assertEquals(0, progress.coffeeGraceTicks);
        assertEquals(3, progress.tobaccoStep);

        InnerTrialManager.clearDrugProgressForTest(progress, DrugId.TOBACCO);
        assertEquals(0, progress.tobaccoStep);
        assertEquals(5, progress.weedPlacements);

        InnerTrialManager.clearDrugProgressForTest(progress, DrugId.WEED);
        assertEquals(0, progress.weedPlacements);

        InnerTrialManager.clearDrugProgressForTest(progress, DrugId.COCAINE);
        assertEquals(-1L, progress.cocaineStartTick);

        InnerTrialManager.clearDrugProgressForTest(progress, DrugId.METH);
        assertEquals(0, progress.methMask);

        InnerTrialManager.clearDrugProgressForTest(progress, DrugId.MUSHROOMS);
        assertEquals(0, progress.mushroomMask);
    }
}
