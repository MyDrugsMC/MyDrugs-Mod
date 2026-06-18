package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnerScarHealerTest {

    @Test
    void restoredMarkerPersistsAndCoversItsWholeCellButNotNeighbours() {
        InnerDimensionSavedData data = new InnerDimensionSavedData();
        UUID owner = UUID.fromString("00000000-0000-0000-0000-0000000005ca");
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(owner);

        BlockPos inCell = new BlockPos(island.centerX() + 3, 64, island.centerZ() + 5);
        assertFalse(InnerScarHealer.isRestoredAt(island, inCell), "cell starts unrestored");

        String marker = InnerScarHealer.scarMarkerFor(island, inCell);
        assertTrue(data.markScarRestored(owner, marker), "first restore should persist a new marker");
        assertFalse(data.markScarRestored(owner, marker), "re-restoring the same cell is idempotent");

        assertTrue(InnerScarHealer.isRestoredAt(island, inCell), "the restored cell is detected");
        // Another block within the same 16-wide scar cell shares the marker and reads as restored.
        BlockPos sameCell = new BlockPos(island.centerX() + 12, 70, island.centerZ() + 11);
        assertTrue(InnerScarHealer.isRestoredAt(island, sameCell), "whole cell is restored");
        // A block a full cell away is a different marker and stays unrestored.
        BlockPos neighbourCell = new BlockPos(island.centerX() + 20, 64, island.centerZ() + 5);
        assertFalse(InnerScarHealer.isRestoredAt(island, neighbourCell), "neighbouring cell is untouched");
    }

    @Test
    void rollingSweepCoversTheFiveByFiveChunkArea() {
        Set<String> offsets = new HashSet<>();
        for (int i = 0; i < InnerScarHealer.chunksPerHealSweepForTest(); i++) {
            int dx = InnerScarHealer.chunkDxForIndexForTest(i);
            int dz = InnerScarHealer.chunkDzForIndexForTest(i);
            assertTrue(dx >= -2 && dx <= 2, "dx out of sweep bounds: " + dx);
            assertTrue(dz >= -2 && dz <= 2, "dz out of sweep bounds: " + dz);
            offsets.add(dx + "," + dz);
        }
        assertEquals(25, offsets.size(), "the rolling sweep must still visit every chunk in the 5x5 area");
        assertEquals(8, InnerScarHealer.healStepIntervalForTest(),
                "25 chunk slices should spread the former 200-tick pass over the same window");
        assertEquals(24, InnerScarHealer.maxBlocksPerSweepForTest(),
                "rolling healing should preserve the previous per-pass block update budget");
    }

    @Test
    void thornRetractionRollIsDeterministicAndClamped() {
        int first = InnerScarHealer.thornRetractionRollForTest(0, 0, 128, -96, DrugId.COCAINE);
        int second = InnerScarHealer.thornRetractionRollForTest(0, 0, 128, -96, DrugId.COCAINE);
        assertEquals(first, second, "thorn retraction rolls must be deterministic");
        assertFalse(InnerScarHealer.shouldRetractThornForTest(first, 0.0D));
        assertTrue(InnerScarHealer.shouldRetractThornForTest(first, 1.0D));
        assertFalse(InnerScarHealer.shouldRetractThornForTest(first, -1.0D));
        assertTrue(InnerScarHealer.shouldRetractThornForTest(first, 2.0D));
    }
}
