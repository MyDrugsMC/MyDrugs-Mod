package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.InnerDimensionService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnerGameplayLoopTest {
    @Test
    void outsideStatusDerivesEntryPhase() {
        assertEquals(
                InnerGameplayLoop.Phase.ENTER_READY,
                InnerGameplayLoop.outside(InnerDimensionService.OpenStatus.READY)
        );
        assertEquals(
                InnerGameplayLoop.Phase.UNAVAILABLE,
                InnerGameplayLoop.outside(InnerDimensionService.OpenStatus.MISSING_INTEGRATION)
        );
        assertEquals(
                InnerGameplayLoop.Phase.UNAVAILABLE,
                InnerGameplayLoop.outside(InnerDimensionService.OpenStatus.MISSING_DREAM_ALIGNMENT)
        );
    }

    @Test
    void phaseFollowsMajorInnerLoopTransitions() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000047");
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        BlockPos anchor = new BlockPos(island.centerX(), InnerDimensionConstants.BASE_Y, island.centerZ());
        BlockPos coffeeLandmark = InnerRegionMap.landmarkFor(island.centerX(), island.centerZ(), DrugId.COFFEE);

        assertPhase(InnerGameplayLoop.Phase.WAIT_FOR_INTEGRATION, data, island, owner, anchor);

        assertTrue(data.recordIntegration(owner, DrugId.COFFEE));
        assertPhase(InnerGameplayLoop.Phase.SEEK_TRIAL, data, island, owner, coffeeLandmark.offset(100, 0, 0));
        assertPhase(InnerGameplayLoop.Phase.ATTEMPT_TRIAL, data, island, owner, coffeeLandmark);

        assertTrue(data.completeInnerTrial(owner, DrugId.COFFEE));
        assertTrue(data.markTrialReturnPending(owner, DrugId.COFFEE));
        assertPhase(InnerGameplayLoop.Phase.RETURN_TO_ANCHOR, data, island, owner, anchor);

        assertTrue(data.clearPendingTrialReturn(owner));
        assertPhase(InnerGameplayLoop.Phase.REFLECT_UNLOCK, data, island, owner, anchor);

        assertTrue(data.markProgressMarker(owner, InnerDimensionConstants.MARKER_FIRST_VAULT_OPENED));
        assertPhase(InnerGameplayLoop.Phase.REFLECT_UNLOCK, data, island, owner, anchor);

        assertTrue(data.markScarRestored(owner, "scar_cell:0:0"));
        assertPhase(InnerGameplayLoop.Phase.WAIT_FOR_INTEGRATION, data, island, owner, anchor);

        assertTrue(data.recordIntegration(owner, DrugId.WEED));
        BlockPos weedSearch = InnerRegionMap.landmarkFor(island.centerX(), island.centerZ(), DrugId.WEED)
                .offset(100, 0, 0);
        assertPhase(InnerGameplayLoop.Phase.SEEK_NEXT, data, island, owner, weedSearch);
    }

    @Test
    void spiralAndCompletionPhasesComeAfterAllTrials() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000048");
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        BlockPos anchor = new BlockPos(island.centerX(), InnerDimensionConstants.BASE_Y, island.centerZ());

        for (DrugId drug : CuratedDrugChain.ORDER) {
            assertTrue(data.completeInnerTrial(owner, drug));
        }

        assertPhase(InnerGameplayLoop.Phase.OPEN_SPIRAL_COURT, data, island, owner, anchor);

        assertTrue(data.markSpiralCourtPlaced(owner));
        assertPhase(InnerGameplayLoop.Phase.COMPLETE_SPIRAL_COURT, data, island, owner, anchor);

        assertTrue(data.markProgressMarker(owner, InnerDimensionConstants.MARKER_SPIRAL_COMPLETED));
        assertPhase(InnerGameplayLoop.Phase.COMPLETE, data, island, owner, anchor);
    }

    @Test
    void reflectionObjectiveWinsBeforeSeekingNextTrial() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000049");
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        BlockPos origin = new BlockPos(island.centerX(), InnerDimensionConstants.BASE_Y, island.centerZ());

        assertTrue(data.recordIntegration(owner, DrugId.COFFEE));
        assertTrue(data.recordIntegration(owner, DrugId.WEED));
        assertTrue(data.completeInnerTrial(owner, DrugId.COFFEE));

        assertEquals(
                InnerObjectiveHelper.Kind.VAULT,
                InnerObjectiveHelper.currentObjectiveForTest(data, island, owner, origin).kind()
        );

        assertTrue(data.markProgressMarker(owner, InnerDimensionConstants.MARKER_FIRST_VAULT_OPENED));
        assertEquals(
                InnerObjectiveHelper.Kind.SCAR,
                InnerObjectiveHelper.currentObjectiveForTest(data, island, owner, origin).kind()
        );

        assertTrue(data.markScarRestored(owner, "scar_cell:0:0"));
        assertEquals(
                InnerObjectiveHelper.Kind.TRIAL,
                InnerObjectiveHelper.currentObjectiveForTest(data, island, owner, origin).kind()
        );
    }

    @Test
    void vaultObjectiveTargetsNearestUnlockedVault() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000050");
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        BlockPos origin = new BlockPos(island.centerX(), InnerDimensionConstants.BASE_Y, island.centerZ());

        assertTrue(data.completeInnerTrial(owner, DrugId.COFFEE));
        InnerObjectiveHelper.Objective objective =
                InnerObjectiveHelper.currentObjectiveForTest(data, island, owner, origin);
        InnerVaults.Vault vault = InnerVaults.nearestUnlockedVault(island, origin);

        assertEquals(InnerObjectiveHelper.Kind.VAULT, objective.kind());
        assertNotNull(vault);
        assertNotNull(objective.target());
        assertEquals(Component.translatable("message.mydrugs.inner_target.memory_vault"), objective.targetName());
        assertEquals(vault.x(), objective.target().getX());
        assertEquals(vault.z(), objective.target().getZ());
        assertEquals(
                Component.translatable(
                        "message.mydrugs.memory_compass.target",
                        objective.targetName(),
                        Component.translatable("message.mydrugs.direction." + direction(origin, objective.target())),
                        horizontalDistance(origin, objective.target())
                ),
                InnerObjectiveHelper.memoryCompassMessageForTest(data, island, owner, origin)
        );
    }

    @Test
    void scarObjectiveTargetsExistingScarTerrain() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000051");
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        BlockPos scar = requireScarPosition(island);

        assertTrue(data.completeInnerTrial(owner, DrugId.COFFEE));
        assertTrue(data.markProgressMarker(owner, InnerDimensionConstants.MARKER_FIRST_VAULT_OPENED));
        InnerObjectiveHelper.Objective objective =
                InnerObjectiveHelper.currentObjectiveForTest(data, island, owner, scar);

        assertEquals(InnerObjectiveHelper.Kind.SCAR, objective.kind());
        assertNotNull(objective.target());
        assertEquals(Component.translatable("message.mydrugs.inner_target.scar"), objective.targetName());
        assertTrue(InnerTerrain.sample(
                island.centerX(),
                island.centerZ(),
                objective.target().getX(),
                objective.target().getZ()
        ).scar());
    }

    @Test
    void compassFallsBackToObjectiveMessageWhenTargetIsUnavailable() {
        InnerObjectiveHelper.Objective objective = new InnerObjectiveHelper.Objective(
                InnerObjectiveHelper.Kind.SCAR,
                Component.translatable("message.mydrugs.inner_objective.scar"),
                null,
                null
        );

        assertEquals(
                objective.message(),
                InnerObjectiveHelper.compassTargetMessageForTest(objective, new BlockPos(0, 64, 0))
        );
    }

    private static void assertPhase(
            InnerGameplayLoop.Phase expected,
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            UUID owner,
            BlockPos origin
    ) {
        assertEquals(expected, InnerGameplayLoop.inside(data, island, owner, origin).phase());
    }

    private static BlockPos requireScarPosition(InnerDimensionSavedData.IslandState island) {
        for (int radius = InnerDimensionConstants.CORE_RADIUS + 80;
             radius < InnerDimensionConstants.ISLAND_RADIUS - 80;
             radius += 16) {
            for (int i = 0; i < 96; i++) {
                double angle = Math.PI * 2.0D * i / 96.0D;
                int x = island.centerX() + (int) Math.round(Math.cos(angle) * radius);
                int z = island.centerZ() + (int) Math.round(Math.sin(angle) * radius);
                InnerTerrain.Sample sample = InnerTerrain.sample(island.centerX(), island.centerZ(), x, z);
                if (sample.land() && sample.scar()) {
                    return new BlockPos(x, sample.topY() + 1, z);
                }
            }
        }
        throw new AssertionError("Expected deterministic island terrain to contain at least one scar sample");
    }

    private static int horizontalDistance(BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (int) Math.round(Math.sqrt(dx * dx + dz * dz));
    }

    private static String direction(BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double degrees = Math.toDegrees(Math.atan2(dz, dx));
        int sector = Math.floorMod((int) Math.round(degrees / 45.0D), 8);
        return switch (sector) {
            case 0 -> "east";
            case 1 -> "southeast";
            case 2 -> "south";
            case 3 -> "southwest";
            case 4 -> "west";
            case 5 -> "northwest";
            case 6 -> "north";
            default -> "northeast";
        };
    }
}
