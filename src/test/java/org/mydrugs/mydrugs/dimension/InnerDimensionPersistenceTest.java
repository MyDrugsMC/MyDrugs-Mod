package org.mydrugs.mydrugs.dimension;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.inner.InnerDimensionConstants;

import java.util.Set;
import java.util.UUID;

class InnerDimensionPersistenceTest {
    private static final String OVERWORLD = "minecraft:overworld";
    private static final String NETHER = "minecraft:the_nether";

    @Test
    void integrationRecordsEachDrugOnlyOncePerOwner() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000001");

        assertTrue(data.recordIntegration(owner, DrugId.COFFEE));
        assertFalse(data.recordIntegration(owner, DrugId.COFFEE));

        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        assertEquals(1, island.integratedDrugCount());
        assertTrue(island.hasIntegrated(DrugId.COFFEE));
    }

    @Test
    void integrationRejectsNonCuratedDrugWithoutCreatingIsland() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000035");

        assertFalse(data.recordIntegration(owner, DrugId.CRACK));
        assertNull(data.island(owner));

        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        assertFalse(island.hasIntegrated(DrugId.CRACK));
        assertTrue(data.recordIntegration(owner, DrugId.COFFEE));
        assertFalse(data.recordIntegration(owner, DrugId.COFFEE));
        assertTrue(island.hasIntegrated(DrugId.COFFEE));
        assertEquals(1, island.integratedDrugCount());
    }

    @Test
    void semanticOverlayMarkersAreRecordedOnce() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String marker = InnerDimensionConstants.landmarkMarker(DrugId.COFFEE, false);

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
        assertTrue(firstIsland.hasIntegrated(DrugId.COFFEE));
        assertFalse(secondIsland.hasIntegrated(DrugId.COFFEE));
        assertEquals(InnerDimensionConstants.SLOT_SPACING, secondIsland.centerX() - firstIsland.centerX());
        assertEquals(0, secondIsland.centerZ() - firstIsland.centerZ());
    }

    @Test
    void findIslandBySlotUsesCreatedIslandIndex() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID first = UUID.fromString("00000000-0000-0000-0000-000000000019");
        UUID second = UUID.fromString("00000000-0000-0000-0000-000000000020");

        InnerDimensionSavedData.IslandState firstIsland = data.getOrCreateIsland(first);
        InnerDimensionSavedData.IslandState secondIsland = data.getOrCreateIsland(second);

        assertSame(firstIsland, data.findIslandBySlot(firstIsland.centerX(), firstIsland.centerZ()));
        assertSame(secondIsland, data.findIslandBySlot(secondIsland.centerX(), secondIsland.centerZ()));
    }

    @Test
    void loadedDataRebuildsSlotIndexAndKeepsFirstDuplicateCenter() {
        String first = "00000000-0000-0000-0000-000000000021";
        String second = "00000000-0000-0000-0000-000000000022";
        var encoded = JsonParser.parseString("""
                {
                  "player_islands": {
                    "%s": {
                      "owner": "%s",
                      "center_x": 0,
                      "center_z": 0,
                      "slot_index": 0
                    },
                    "%s": {
                      "owner": "%s",
                      "center_x": 0,
                      "center_z": 0,
                      "slot_index": 0
                    }
                  }
                }
                """.formatted(first, first, second, second));
        InnerDimensionSavedData data = InnerDimensionSavedData.CODEC
                .parse(JsonOps.INSTANCE, encoded)
                .result()
                .orElseThrow();

        InnerDimensionSavedData.IslandState firstIsland = data.island(UUID.fromString(first));
        InnerDimensionSavedData.IslandState secondIsland = data.island(UUID.fromString(second));

        assertSame(firstIsland, data.findIslandBySlot(0, 0));
        assertSame(secondIsland, data.island(UUID.fromString(second)));
    }

    @Test
    void islandWithUnparseableOwnerIsDiscardedOnLoad() {
        var encoded = JsonParser.parseString("""
                {
                  "player_islands": {
                    "not-a-uuid": {
                      "owner": "also-not-a-uuid",
                      "center_x": 0,
                      "center_z": 0,
                      "slot_index": 0
                    }
                  }
                }
                """);
        InnerDimensionSavedData data = InnerDimensionSavedData.CODEC
                .parse(JsonOps.INSTANCE, encoded)
                .result()
                .orElseThrow();

        assertNull(data.findIslandBySlot(0, 0));
    }

    @Test
    void islandWithInvalidKeyRecoversOwnerFromBody() {
        String owner = "00000000-0000-0000-0000-000000000031";
        var encoded = JsonParser.parseString("""
                {
                  "player_islands": {
                    "legacy-non-uuid-key": {
                      "owner": "%s",
                      "center_x": 0,
                      "center_z": 0,
                      "slot_index": 0
                    }
                  }
                }
                """.formatted(owner));
        InnerDimensionSavedData data = InnerDimensionSavedData.CODEC
                .parse(JsonOps.INSTANCE, encoded)
                .result()
                .orElseThrow();

        InnerDimensionSavedData.IslandState island = data.island(UUID.fromString(owner));
        assertNotNull(island);
        assertSame(island, data.findIslandBySlot(0, 0));
    }

    @Test
    void invalidIntegratedDrugIdsAreDroppedButValidOnesSurvive() {
        String owner = "00000000-0000-0000-0000-000000000032";
        var encoded = JsonParser.parseString("""
                {
                  "owner": "%s",
                  "center_x": 0,
                  "center_z": 0,
                  "integrated_drugs": ["coffee", "totally_not_a_drug", "crack", "weed"]
                }
                """.formatted(owner));
        InnerDimensionSavedData.IslandState state = InnerDimensionSavedData.ISLAND_CODEC
                .parse(JsonOps.INSTANCE, encoded)
                .result()
                .orElseThrow();

        assertEquals(Set.of(DrugId.COFFEE, DrugId.WEED), state.integratedDrugs());
        assertFalse(state.hasIntegrated(DrugId.CRACK));
    }

    @Test
    void malformedDreamDimensionIsPreservedAndLoadsSafely() {
        String owner = "00000000-0000-0000-0000-000000000033";
        String malformed = "Not A Valid Id!";
        var encoded = JsonParser.parseString("""
                {
                  "owner": "%s",
                  "center_x": 0,
                  "center_z": 0,
                  "dream_dimension": "%s"
                }
                """.formatted(owner, malformed));
        InnerDimensionSavedData.IslandState state = InnerDimensionSavedData.ISLAND_CODEC
                .parse(JsonOps.INSTANCE, encoded)
                .result()
                .orElseThrow();

        // Behavior is preserved: the raw id is stored unchanged; validation stays in the service layer.
        assertEquals(malformed, state.dreamDimension());
    }

    @Test
    void blankAndStaleMarkersAreDiscardedKeepingValidProgress() {
        int current = InnerDimensionConstants.OVERLAY_SCHEMA_VERSION;
        String owner = "00000000-0000-0000-0000-000000000034";
        var encoded = JsonParser.parseString("""
                {
                  "owner": "%s",
                  "center_x": 0,
                  "center_z": 0,
                  "placed_markers": ["", "path:v%d:coffee", "path:v%d:coffee", "progress:first_vault_opened"]
                }
                """.formatted(owner, current - 1, current));
        InnerDimensionSavedData.IslandState state = InnerDimensionSavedData.ISLAND_CODEC
                .parse(JsonOps.INSTANCE, encoded)
                .result()
                .orElseThrow();

        assertFalse(state.hasMarker(""));
        assertFalse(state.hasMarker("path:v" + (current - 1) + ":coffee"));
        assertTrue(state.hasMarker("path:v" + current + ":coffee"));
        assertTrue(state.hasMarker(InnerDimensionConstants.MARKER_FIRST_VAULT_OPENED));
    }

    @Test
    void integratedDrugSnapshotIsImmutableAndDetached() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000018");

        assertTrue(data.recordIntegration(owner, DrugId.COFFEE));
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        Set<DrugId> snapshot = island.integratedDrugs();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(DrugId.WEED));
        assertTrue(island.hasIntegrated(DrugId.COFFEE));
        assertFalse(island.hasIntegrated(DrugId.WEED));
        assertEquals(1, island.integratedDrugCount());
    }

    @Test
    void innerTrialsCompleteOnceAndResetWithoutDuplicating() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000011");

        for (DrugId drug : CuratedDrugChain.ORDER) {
            assertTrue(data.completeInnerTrial(owner, drug));
            assertFalse(data.completeInnerTrial(owner, drug));
        }

        assertEquals(9, data.completedInnerTrialCount(owner));
        assertTrue(data.allInnerTrialsCompleted(owner));
        assertTrue(data.resetInnerTrials(owner));
        assertEquals(0, data.completedInnerTrialCount(owner));
        assertFalse(data.allInnerTrialsCompleted(owner));
    }

    @Test
    void legacyCodecDefaultsNewFieldsAndIgnoresUnknownTrialNames() {
        String owner = "00000000-0000-0000-0000-000000000012";
        var legacy = JsonParser.parseString("""
                {
                  "owner": "%s",
                  "center_x": 0,
                  "center_z": 0,
                  "completed_inner_trials": ["coffee", "future_unknown_drug"],
                  "restored_scar_markers": ["scar_cell:1:2"]
                }
                """.formatted(owner));
        InnerDimensionSavedData.IslandState state = InnerDimensionSavedData.ISLAND_CODEC
                .parse(JsonOps.INSTANCE, legacy)
                .result()
                .orElseThrow();

        assertEquals(Set.of(DrugId.COFFEE), state.completedInnerTrials());
        assertEquals(Set.of("scar_cell:1:2"), state.restoredScarMarkers());
        assertEquals(OVERWORLD, state.dreamDimension());
    }

    @Test
    void dreamCoordinateStoresDimensionWithOverworldDefault() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000016");

        assertTrue(data.markDreamAligned(owner, new BlockPos(10, 64, -20)));
        assertEquals(OVERWORLD, data.getOrCreateIsland(owner).dreamDimension());

        assertTrue(data.markDreamAligned(
                owner,
                new BlockPos(30, 70, -40),
                NETHER
        ));
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        assertEquals(30, island.dreamX());
        assertEquals(70, island.dreamY());
        assertEquals(-40, island.dreamZ());
        assertEquals(NETHER, island.dreamDimension());
    }

    @Test
    void firstDreamAlignmentReportsChanged() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000023");

        assertTrue(data.markDreamAligned(owner, new BlockPos(10, 64, -20), OVERWORLD));

        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        assertTrue(island.dreamAligned());
        assertTrue(island.hasDreamCoordinate());
        assertEquals(10, island.dreamX());
        assertEquals(64, island.dreamY());
        assertEquals(-20, island.dreamZ());
        assertEquals(OVERWORLD, island.dreamDimension());
    }

    @Test
    void repeatedIdenticalDreamAlignmentReportsUnchanged() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000024");
        BlockPos pos = new BlockPos(10, 64, -20);

        assertTrue(data.markDreamAligned(owner, pos, OVERWORLD));
        assertFalse(data.markDreamAligned(owner, pos, OVERWORLD));
    }

    @Test
    void changedDreamAlignmentReportsChanged() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000025");

        assertTrue(data.markDreamAligned(owner, new BlockPos(10, 64, -20), OVERWORLD));
        assertTrue(data.markDreamAligned(owner, new BlockPos(30, 70, -40), NETHER));

        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        assertEquals(30, island.dreamX());
        assertEquals(70, island.dreamY());
        assertEquals(-40, island.dreamZ());
        assertEquals(NETHER, island.dreamDimension());
    }

    @Test
    void burstMarkerClearPreservesGameplayProgressMarkers() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000013");
        String overlayMarker = InnerDimensionConstants.landmarkMarker(DrugId.COFFEE, true);
        String pathMarker = InnerDimensionConstants.pathMarker(DrugId.COFFEE);
        String trialMarker = InnerDimensionConstants.trialFixtureMarker(DrugId.COFFEE, false);

        assertTrue(data.markStructurePlaced(owner, overlayMarker));
        assertTrue(data.markStructurePlaced(owner, pathMarker));
        assertTrue(data.markStructurePlaced(owner, trialMarker));
        assertTrue(data.markProgressMarker(owner, InnerDimensionConstants.MARKER_FIRST_VAULT_OPENED));
        assertTrue(data.markTrialReturnPending(owner, DrugId.COFFEE));
        assertTrue(data.clearOverlayMarkers(owner));

        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        assertFalse(island.hasMarker(overlayMarker));
        assertFalse(island.hasMarker(pathMarker));
        assertFalse(island.hasMarker(trialMarker));
        assertTrue(data.hasProgressMarker(owner, InnerDimensionConstants.MARKER_FIRST_VAULT_OPENED));
        assertTrue(data.hasPendingTrialReturn(owner));
    }

    @Test
    void visualMarkersCarryOverlaySchemaAndAreInvalidatedByRefresh() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000015");
        String suffix = ":v" + InnerDimensionConstants.OVERLAY_SCHEMA_VERSION;
        String pathMarker = InnerDimensionConstants.pathMarker(DrugId.COFFEE);
        String trialMarker = InnerDimensionConstants.trialFixtureMarker(DrugId.COFFEE, false);
        String landmarkMarker = InnerDimensionConstants.landmarkMarker(DrugId.COFFEE, true);
        String oldPathMarker = "path:" + DrugId.COFFEE.serializedName();
        String oldTrialMarker = InnerDimensionConstants.MARKER_TRIAL_FIXTURE
                + DrugId.COFFEE.serializedName()
                + ":active";

        assertTrue(pathMarker.contains(suffix));
        assertTrue(trialMarker.contains(suffix));
        assertTrue(landmarkMarker.contains(suffix));
        assertTrue(data.markStructurePlaced(owner, pathMarker));
        assertTrue(data.markStructurePlaced(owner, trialMarker));
        assertTrue(data.markStructurePlaced(owner, landmarkMarker));
        assertTrue(data.markStructurePlaced(owner, oldPathMarker));
        assertTrue(data.markStructurePlaced(owner, oldTrialMarker));
        assertTrue(data.markProgressMarker(owner, InnerDimensionConstants.MARKER_SPIRAL_COMPLETED));

        assertTrue(data.clearOverlayMarkers(owner));
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);
        assertFalse(island.hasMarker(pathMarker));
        assertFalse(island.hasMarker(trialMarker));
        assertFalse(island.hasMarker(landmarkMarker));
        assertFalse(island.hasMarker(oldPathMarker));
        assertFalse(island.hasMarker(oldTrialMarker));
        assertTrue(data.hasProgressMarker(owner, InnerDimensionConstants.MARKER_SPIRAL_COMPLETED));
    }

    @Test
    void staleVisualMarkerVersionsArePrunedOnLoad() {
        int current = InnerDimensionConstants.OVERLAY_SCHEMA_VERSION;
        String owner = "00000000-0000-0000-0000-000000000017";
        var encoded = JsonParser.parseString("""
                {
                  "owner": "%s",
                  "center_x": 0,
                  "center_z": 0,
                  "placed_markers": [
                    "path:v%d:coffee",
                    "path:v%d:coffee",
                    "progress:first_vault_opened"
                  ]
                }
                """.formatted(owner, current - 1, current));
        InnerDimensionSavedData.IslandState state = InnerDimensionSavedData.ISLAND_CODEC
                .parse(JsonOps.INSTANCE, encoded)
                .result()
                .orElseThrow();

        assertFalse(state.hasMarker("path:v" + (current - 1) + ":coffee"));
        assertTrue(state.hasMarker("path:v" + current + ":coffee"));
        assertTrue(state.hasMarker(InnerDimensionConstants.MARKER_FIRST_VAULT_OPENED));
        assertTrue(InnerDimensionSavedData.keepLoadedMarkerForTest("trial_fixture:v" + current + ":coffee:active"));
        assertFalse(InnerDimensionSavedData.keepLoadedMarkerForTest("trial_fixture:v" + (current - 1) + ":coffee:active"));
    }

    @Test
    void trialResetClearsReturnObjectiveWithoutRearmingCourtCompletion() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000014");

        assertTrue(data.completeInnerTrial(owner, DrugId.COFFEE));
        assertTrue(data.markTrialReturnPending(owner, DrugId.COFFEE));
        assertTrue(data.markProgressMarker(owner, InnerDimensionConstants.MARKER_SPIRAL_COMPLETED));
        assertTrue(data.resetInnerTrials(owner));

        assertFalse(data.hasPendingTrialReturn(owner));
        assertTrue(data.hasProgressMarker(owner, InnerDimensionConstants.MARKER_SPIRAL_COMPLETED));
    }

    @Test
    void pendingRecreateJobSurvivesCodecRoundTrip() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000036");
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        data.getOrCreateIsland(owner);
        assertTrue(data.markRecreateJobPending(owner));
        assertTrue(data.updateRecreateJobPhase(owner, "decorate"));

        var encoded = InnerDimensionSavedData.CODEC.encodeStart(JsonOps.INSTANCE, data)
                .result()
                .orElseThrow();
        InnerDimensionSavedData decoded = InnerDimensionSavedData.CODEC
                .parse(JsonOps.INSTANCE, encoded)
                .result()
                .orElseThrow();

        InnerDimensionSavedData.RecreateJobState job = decoded.pendingRecreateJob(owner);
        assertNotNull(job);
        assertEquals(owner, job.owner());
        assertTrue(job.fullRecreate());
        assertEquals("decorate", job.phase());
        assertTrue(job.schemaVersion() > 0);
    }

    @Test
    void lastSafeReturnSurvivesCodecRoundTrip() {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000037");
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        assertTrue(data.recordLastSafeReturn(owner, new BlockPos(12, 70, -8), NETHER));

        var encoded = InnerDimensionSavedData.CODEC.encodeStart(JsonOps.INSTANCE, data)
                .result()
                .orElseThrow();
        InnerDimensionSavedData decoded = InnerDimensionSavedData.CODEC
                .parse(JsonOps.INSTANCE, encoded)
                .result()
                .orElseThrow();

        InnerDimensionSavedData.ReturnPositionState lastSafe = decoded.lastSafeReturn(owner);
        assertNotNull(lastSafe);
        assertEquals(12, lastSafe.x());
        assertEquals(70, lastSafe.y());
        assertEquals(-8, lastSafe.z());
        assertEquals(NETHER, lastSafe.dimension());
    }
}
