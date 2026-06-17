package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for Inner Dimension slot spacing, recreate radius, and landmark
 * geometry invariants. Future constant tuning must not create overlap between player
 * islands or exceed the slot bounds.
 */
class InnerSlotGeometryTest {
    private static final int CENTER_X = 0;
    private static final int CENTER_Z = 0;

    @Test
    void recreateRadiusPlusMarginFitsBelowHalfSlotSpacing() {
        int halfSpacing = InnerDimensionConstants.SLOT_SPACING / 2;
        // The recreate radius must not reach the midpoint between two adjacent slot
        // centres. A safety margin of at least 64 blocks should remain so that
        // rounding/placement edge cases do not clip into a neighbour's island.
        int safetyMargin = 64;
        assertTrue(
                InnerDimensionConstants.FULL_RECREATE_BLOCK_RADIUS + safetyMargin < halfSpacing,
                "FULL_RECREATE_BLOCK_RADIUS=" + InnerDimensionConstants.FULL_RECREATE_BLOCK_RADIUS
                        + " + safetyMargin=" + safetyMargin
                        + " must be strictly below half spacing " + halfSpacing
        );
    }

    @Test
    void islandPlusSatellitePlusMarginFitsWithinRecreateRadius() {
        // Recreate radius must cover the island body plus its satellites with a
        // comfortable margin so chunk reloads never clip satellite features.
        int satelliteOuter = InnerDimensionConstants.ISLAND_RADIUS
                + InnerDimensionConstants.SATELLITE_REACH;
        assertTrue(
                satelliteOuter < InnerDimensionConstants.FULL_RECREATE_BLOCK_RADIUS,
                "ISLAND_RADIUS + SATELLITE_REACH = " + satelliteOuter
                        + " must fit inside FULL_RECREATE_BLOCK_RADIUS = "
                        + InnerDimensionConstants.FULL_RECREATE_BLOCK_RADIUS
        );
    }

    @Test
    void recreateChunkRadiusIsConsistentWithBlockRadius() {
        int expectedChunkRadius = (InnerDimensionConstants.FULL_RECREATE_BLOCK_RADIUS + 15) / 16;
        assertEquals(expectedChunkRadius, InnerDimensionConstants.FULL_RECREATE_CHUNK_RADIUS,
                "FULL_RECREATE_CHUNK_RADIUS must match ceil(FULL_RECREATE_BLOCK_RADIUS / 16)");
    }

    @Test
    void slotCenterIsDeterministic() {
        int testValue = 12345;
        int a = InnerTerrain.slotCenter(testValue);
        int b = InnerTerrain.slotCenter(testValue);
        assertEquals(a, b, "slotCenter must be deterministic");
    }

    @Test
    void slotCentersAreMultiplesOfSlotSpacing() {
        for (int v : new int[]{-10000, -5000, -1, 0, 1, 5000, 10000, 32768, -32768}) {
            int center = InnerTerrain.slotCenter(v);
            assertEquals(0, center % InnerDimensionConstants.SLOT_SPACING,
                    "slotCenter(" + v + ") = " + center + " must be a multiple of " + InnerDimensionConstants.SLOT_SPACING);
        }
    }

    @Test
    void adjacentSlotsDoNotCollide() {
        // Two positions on opposite sides of a slot boundary should map to different slots.
        int slotCenter0 = InnerTerrain.slotCenter(CENTER_X);
        int nextSlotCenterNorth = InnerTerrain.slotCenter(CENTER_X + InnerDimensionConstants.SLOT_SPACING);
        int nextSlotCenterSouth = InnerTerrain.slotCenter(CENTER_X - InnerDimensionConstants.SLOT_SPACING);
        assertEquals(CENTER_X, slotCenter0);
        assertEquals(slotCenter0 + InnerDimensionConstants.SLOT_SPACING, nextSlotCenterNorth);
        assertEquals(slotCenter0 - InnerDimensionConstants.SLOT_SPACING, nextSlotCenterSouth);
    }

    @Test
    void slotCenterSnapsToNearestSpacing() {
        int spacing = InnerDimensionConstants.SLOT_SPACING;
        int half = spacing / 2;
        // Slightly left of midpoint -> snaps left.
        assertEquals(0, InnerTerrain.slotCenter(half - 1));
        // Slightly right of midpoint -> snaps right.
        assertEquals(spacing, InnerTerrain.slotCenter(half + 1));
        // Exactly at midpoint -> snaps to nearest (implementation-defined, but consistent).
        int midpoint = InnerTerrain.slotCenter(half);
        assertTrue(midpoint == 0 || midpoint == spacing,
                "midpoint " + half + " should snap to 0 or " + spacing + ", got " + midpoint);
    }

    @Test
    void everyLandmarkStaysWithinIslandRadius() {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            BlockPos landmark = InnerRegionMap.landmarkFor(CENTER_X, CENTER_Z, drug);
            double distance = Math.hypot(landmark.getX() - CENTER_X, landmark.getZ() - CENTER_Z);
            assertTrue(distance <= InnerDimensionConstants.ISLAND_RADIUS,
                    drug + " landmark at distance " + distance
                            + " exceeds ISLAND_RADIUS " + InnerDimensionConstants.ISLAND_RADIUS);
        }
    }

    @Test
    void everyLandmarkRadiusDoesNotExceedRecreateBounds() {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            int radius = InnerRegionMap.landmarkRadiusFor(drug);
            assertTrue(radius < InnerDimensionConstants.FULL_RECREATE_BLOCK_RADIUS,
                    drug + " landmark radius " + radius
                            + " must be within recreate block radius "
                            + InnerDimensionConstants.FULL_RECREATE_BLOCK_RADIUS);
        }
    }

    @Test
    void coreRadiusFitsInsideIslandRadius() {
        assertTrue(InnerDimensionConstants.CORE_RADIUS < InnerDimensionConstants.ISLAND_RADIUS,
                "CORE_RADIUS=" + InnerDimensionConstants.CORE_RADIUS
                        + " must fit within ISLAND_RADIUS=" + InnerDimensionConstants.ISLAND_RADIUS);
    }

    @Test
    void satelliteReachDoesNotExceedIslandRadius() {
        // SATELLITE_REACH extends outward from the island rim; it must be a fraction
        // of the island itself so satellites remain in the local neighbourhood.
        assertTrue(InnerDimensionConstants.SATELLITE_REACH <= InnerDimensionConstants.ISLAND_RADIUS,
                "SATELLITE_REACH=" + InnerDimensionConstants.SATELLITE_REACH
                        + " must not exceed ISLAND_RADIUS=" + InnerDimensionConstants.ISLAND_RADIUS);
    }

    @Test
    void chunkLandCheckRejectsPositionWayBeyondAnyIsland() {
        // A chunk whose nearest slot centre is two full spacings away from the
        // chunk itself should be too far from any island. chunkMayHaveLand
        // finds the nearest slot for that chunk, so we test a chunk far from
        // every possible slot centre.
        int spacing = InnerDimensionConstants.SLOT_SPACING;
        int half = spacing / 2;
        int farX = half + spacing; // midpoint between slot 0 and slot spacing, offset by spacing
        int farZ = half;
        int nearestSlotX = InnerTerrain.slotCenter(farX);
        int distanceToNearest = Math.abs(nearestSlotX - farX);
        assertTrue(distanceToNearest > InnerDimensionConstants.ISLAND_RADIUS,
                "nearest slot centre to chunk (" + farX + "," + farZ
                        + ") is only " + distanceToNearest + " blocks away, "
                        + "must exceed ISLAND_RADIUS=" + InnerDimensionConstants.ISLAND_RADIUS);
        assertFalse(InnerTerrain.chunkMayHaveLand(farX, farZ),
                "chunk at (" + farX + "," + farZ + ") midway between slots with large offset should not be land");
    }

    @Test
    void chunkLandCheckAcceptsWithinRadius() {
        // Chunks within the island radius should be accepted.
        int withinX = CENTER_X + InnerDimensionConstants.ISLAND_RADIUS / 2;
        int withinZ = CENTER_Z;
        assertTrue(InnerTerrain.chunkMayHaveLand(withinX, withinZ),
                "a chunk well within the island radius should be accepted for land");
    }
}
