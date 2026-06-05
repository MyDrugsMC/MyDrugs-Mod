package org.mydrugs.mydrugs.dimension.inner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InnerChunkRebuilderTest {
    @Test
    void destructiveClearStartsAtWorldFloor() {
        assertEquals(-64, InnerChunkRebuilder.clearLowYForTest(-64));
    }

    @Test
    void destructiveClearKeepsExpectedGeneratedHeadroom() {
        assertEquals(118, InnerChunkRebuilder.clearHighYForTest(70, 48, 255));
    }

    @Test
    void destructiveClearIncludesExistingTallRemnants() {
        assertEquals(156, InnerChunkRebuilder.clearHighYForTest(70, 140, 255));
    }

    @Test
    void destructiveClearIsCappedAtWorldCeiling() {
        assertEquals(128, InnerChunkRebuilder.clearHighYForTest(70, 180, 128));
    }
}
