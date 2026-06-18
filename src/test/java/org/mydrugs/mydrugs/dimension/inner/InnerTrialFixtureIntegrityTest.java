package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression protection for trial-fixture placement geometry and gameplay reachability across the
 * actual per-player island slots (not just slot 0). Existing coverage proves a landmark sits inside
 * the island and that a walkable corridor reaches it ({@link InnerTerrainTest},
 * {@link InnerSpokeReachabilityTest}, {@link InnerSlotGeometryTest}); this guards the next step —
 * that the puzzle nodes {@link InnerTrialBuilder} stamps around each landmark land on solid, sane
 * surface columns rather than in the void, buried, or floating far off the fixture's own grade.
 *
 * <p>All checks reproduce the exact surface query the builder uses
 * ({@link InnerPlacement#surfaceTop}: a column's {@code topY} when it is land, otherwise the
 * {@code BASE_Y} fallback) so a failure means the live placement would also be wrong.
 */
class InnerTrialFixtureIntegrityTest {
    /** Realistic island anchors: slot 0 plus several neighbouring per-player slots on the grid. */
    private static final int[][] SLOT_CENTERS = {
            {0, 0},
            {InnerDimensionConstants.SLOT_SPACING, 0},
            {0, -InnerDimensionConstants.SLOT_SPACING},
            {InnerDimensionConstants.SLOT_SPACING, InnerDimensionConstants.SLOT_SPACING},
            {-2 * InnerDimensionConstants.SLOT_SPACING, InnerDimensionConstants.SLOT_SPACING},
    };

    /** Inner radius of the decorative satellite ring (see {@link InnerTerrain} satelliteCenters). */
    private static final int SATELLITE_RING_INNER_RADIUS = 1180;

    /** Drugs whose trial fixtures expose explicit interaction-node offsets via InnerTrialDefinition. */
    private interface NodeOffsets {
        List<BlockPos> offsets(int centerX, int centerZ);
    }

    private static final List<Map.Entry<DrugId, NodeOffsets>> DEFINITION_FIXTURES = List.of(
            entry(DrugId.TOBACCO, InnerTrialDefinition::tobaccoOrder),
            entry(DrugId.HASH, (cx, cz) -> InnerTrialDefinition.hashSockets()),
            entry(DrugId.LSD, (cx, cz) -> InnerTrialDefinition.lsdNodes()),
            entry(DrugId.METH, (cx, cz) -> InnerTrialDefinition.methNodes()),
            entry(DrugId.MUSHROOMS, (cx, cz) -> InnerTrialDefinition.mushroomRoots())
    );

    @Test
    void definitionFixtureNodesLandOnSolidGroundAcrossEverySlot() {
        for (int[] slot : SLOT_CENTERS) {
            int cx = slot[0];
            int cz = slot[1];
            for (Map.Entry<DrugId, NodeOffsets> fixture : DEFINITION_FIXTURES) {
                DrugId drug = fixture.getKey();
                BlockPos landmark = InnerRegionMap.landmarkFor(cx, cz, drug);
                // The builder anchors the fixture on the landmark column's surface, so that column
                // itself must be solid land — otherwise every node hangs off the BASE_Y fallback.
                assertTrue(isLand(cx, cz, landmark.getX(), landmark.getZ()),
                        () -> failure(drug, slot, "landmark anchor column is void", landmark));

                int anchorY = surfaceTopY(cx, cz, landmark.getX(), landmark.getZ());
                for (BlockPos offset : fixture.getValue().offsets(cx, cz)) {
                    int nx = landmark.getX() + offset.getX();
                    int nz = landmark.getZ() + offset.getZ();
                    BlockPos node = new BlockPos(nx, 0, nz);

                    // 1. Not in void: the node sits on real terrain, never the BASE_Y fallback slab.
                    assertTrue(isLand(cx, cz, nx, nz),
                            () -> failure(drug, slot, "node column is void", node));

                    int nodeY = surfaceTopY(cx, cz, nx, nz);
                    // 2. Not buried / not in the void floor: clearly above the world bottom.
                    assertTrue(nodeY >= InnerDimensionConstants.MIN_Y + 8,
                            () -> failure(drug, slot, "node surface " + nodeY + " is at the void floor", node));
                    // 3. Not absurdly far above/below the fixture's own grade. A trial fixture spans a
                    //    few blocks; nodes that differ from the anchor by more than a tall cliff would
                    //    read as floating or buried relative to the rest of the fixture.
                    int drop = Math.abs(nodeY - anchorY);
                    assertTrue(drop <= 32,
                            () -> failure(drug, slot,
                                    "node surface " + nodeY + " differs from anchor " + anchorY
                                            + " by " + drop + " (>32)", node));
                    // 4. Within the intended island slot, never out on the void rim.
                    double dist = Math.hypot(nx - cx, nz - cz);
                    assertTrue(dist <= InnerDimensionConstants.ISLAND_RADIUS,
                            () -> failure(drug, slot,
                                    "node distance " + (int) dist + " exceeds ISLAND_RADIUS", node));
                }
            }
        }
    }

    @Test
    void selfAnchorCenterIsStableAndSafeAcrossEverySlot() {
        for (int[] slot : SLOT_CENTERS) {
            int cx = slot[0];
            int cz = slot[1];
            InnerTerrain.Sample center = InnerTerrain.sample(cx, cz, cx, cz);
            // The Self Anchor is the flat sanctuary clearing at the slot centre. It must always be a
            // safe, walkable, hazard-free platform so the player can never spawn into void/lava/hole.
            assertTrue(center.land(),
                    () -> "Self Anchor at slot " + slot[0] + "," + slot[1] + " is not land");
            assertTrue(center.path(),
                    () -> "Self Anchor at slot " + slot[0] + "," + slot[1] + " is not safe path ground");
            assertTrue(!center.hole() && !center.lake(),
                    () -> "Self Anchor at slot " + slot[0] + "," + slot[1] + " contains a hazard");
            // The clearing height is deterministic geometry (BASE_Y + 5), independent of the slot.
            assertTrue(center.topY() == InnerDimensionConstants.BASE_Y + 5,
                    () -> "Self Anchor top " + center.topY() + " at slot " + slot[0] + "," + slot[1]
                            + " must equal the flat-clearing height " + (InnerDimensionConstants.BASE_Y + 5));
            // Spawn lands exactly one block above that surface.
            assertTrue(InnerTerrain.safeSpawnY(cx, cz) == center.topY() + 1,
                    () -> "Self Anchor spawn Y at slot " + slot[0] + "," + slot[1]
                            + " must sit one block above the clearing");
        }
    }

    @Test
    void everyLandmarkStaysOnTheMainContinentNotTheSatelliteRing() {
        // Satellites and floating sky shards are decorative outer scenery; existing gameplay never
        // requires them to be traversable, so reachability is intentionally NOT asserted for them.
        // What IS a contract: every progression target (landmark) sits on the main island body, well
        // inside the satellite ring, so trials are always reachable over connected land.
        for (int[] slot : SLOT_CENTERS) {
            for (DrugId drug : CuratedDrugChain.ORDER) {
                BlockPos landmark = InnerRegionMap.landmarkFor(slot[0], slot[1], drug);
                double dist = Math.hypot(landmark.getX() - slot[0], landmark.getZ() - slot[1]);
                assertTrue(dist < SATELLITE_RING_INNER_RADIUS,
                        () -> failure(drug, slot,
                                "landmark distance " + (int) dist
                                        + " reaches the decorative satellite ring (>= "
                                        + SATELLITE_RING_INNER_RADIUS + ")", landmark));
            }
        }
    }

    private static boolean isLand(int centerX, int centerZ, int x, int z) {
        return InnerTerrain.sample(centerX, centerZ, x, z).land();
    }

    /** Mirrors {@link InnerPlacement#surfaceTop}: a land column's top, else the BASE_Y fallback. */
    private static int surfaceTopY(int centerX, int centerZ, int x, int z) {
        InnerTerrain.Sample sample = InnerTerrain.sample(centerX, centerZ, x, z);
        return sample.land() ? sample.topY() : InnerDimensionConstants.BASE_Y;
    }

    private static String failure(DrugId drug, int[] slot, String why, BlockPos pos) {
        return drug + " trial fixture (island slot " + slot[0] + "," + slot[1] + "): " + why
                + " at (" + pos.getX() + "," + pos.getZ() + ")";
    }

    private static Map.Entry<DrugId, NodeOffsets> entry(DrugId drug, NodeOffsets offsets) {
        return java.util.Map.entry(drug, offsets);
    }
}
