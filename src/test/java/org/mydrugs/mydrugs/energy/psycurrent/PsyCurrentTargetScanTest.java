package org.mydrugs.mydrugs.energy.psycurrent;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PsyCurrentTargetScanTest {

    @Test
    void emptyScanReportsZeroCounts() {
        PsyCurrentTargetScan scan = PsyCurrentTargetScan.EMPTY;
        assertEquals(0, scan.validCount());
        assertEquals(0, scan.fullCount());
        assertEquals(0, scan.incompatibleCount());
        assertEquals(0, scan.totalReceivable());
    }

    @Test
    void countsReflectBucketSizes() {
        PsyCurrentTargetScan scan = new PsyCurrentTargetScan(
                List.of(new BlockPos(0, 0, 0), new BlockPos(1, 0, 0), new BlockPos(2, 0, 0)),
                List.of(new BlockPos(3, 0, 0)),
                List.of(new BlockPos(4, 0, 0), new BlockPos(5, 0, 0)),
                12_500
        );
        assertEquals(3, scan.validCount());
        assertEquals(1, scan.fullCount());
        assertEquals(2, scan.incompatibleCount());
        assertEquals(12_500, scan.totalReceivable());
    }

    @Test
    void recordDefensivelyCopiesProvidedLists() {
        List<BlockPos> mutableValid = new ArrayList<>();
        mutableValid.add(new BlockPos(0, 0, 0));
        PsyCurrentTargetScan scan = new PsyCurrentTargetScan(mutableValid, List.of(), List.of(), 0);
        mutableValid.add(new BlockPos(99, 99, 99));
        assertEquals(1, scan.validCount(),
                "The scan must not mutate when the caller's source list changes.");
        assertNotSame(mutableValid, scan.valid());
        // The defensive copy is immutable: tampering should throw, not silently succeed.
        assertThrows(UnsupportedOperationException.class,
                () -> scan.valid().add(new BlockPos(0, 0, 0)));
    }

    @Test
    void negativeReceivableIsClampedToZero() {
        PsyCurrentTargetScan scan = new PsyCurrentTargetScan(List.of(), List.of(), List.of(), -42);
        assertEquals(0, scan.totalReceivable());
    }
}
