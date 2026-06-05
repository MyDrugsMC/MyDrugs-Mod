package org.mydrugs.mydrugs.dimension.inner;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnerScarHealerTest {

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
