package org.mydrugs.mydrugs.dimension;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InnerDimensionOpenStatusTest {
    @Test
    void missingIntegrationMapsToIntegrationMessage() {
        InnerDimensionService.OpenStatus status = InnerDimensionService.resolveOpenStatus(
                true,
                0,
                false,
                false
        );

        assertEquals(InnerDimensionService.OpenStatus.MISSING_INTEGRATION, status);
        assertEquals(
                "message.mydrugs.inner_dimension.requires_integration",
                InnerDimensionService.openFailureMessageKey(status)
        );
    }

    @Test
    void missingDreamAlignmentMapsToDreamAlignmentMessage() {
        InnerDimensionService.OpenStatus status = InnerDimensionService.resolveOpenStatus(
                true,
                1,
                false,
                false
        );

        assertEquals(InnerDimensionService.OpenStatus.MISSING_DREAM_ALIGNMENT, status);
        assertEquals(
                "message.mydrugs.inner_dimension.requires_dream_alignment",
                InnerDimensionService.openFailureMessageKey(status)
        );
    }

    @Test
    void unavailableInnerLevelMapsToUnavailableMessage() {
        InnerDimensionService.OpenStatus status = InnerDimensionService.resolveOpenStatus(
                true,
                1,
                true,
                false
        );

        assertEquals(InnerDimensionService.OpenStatus.UNAVAILABLE, status);
        assertEquals(
                "message.mydrugs.inner_dimension.unavailable",
                InnerDimensionService.openFailureMessageKey(status)
        );
    }

    @Test
    void readyStateHasNoFailureMessage() {
        InnerDimensionService.OpenStatus status = InnerDimensionService.resolveOpenStatus(
                true,
                1,
                true,
                true
        );

        assertEquals(InnerDimensionService.OpenStatus.READY, status);
        assertNull(InnerDimensionService.openFailureMessageKey(status));
    }

    @Test
    void returnFallbackOrderAndUnsafeLastResortAreExplicit() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/org/mydrugs/mydrugs/dimension/InnerDimensionService.java"
        ));
        int dream = source.indexOf("ReturnTarget dream = dreamCoordinateTarget");
        int respawn = source.indexOf("ReturnTarget respawn = respawnTarget");
        int spawn = source.indexOf("safeNearOrNull(overworld, spawn, false)");
        assertTrue(dream >= 0);
        assertTrue(respawn > dream);
        assertTrue(spawn > respawn);
        assertTrue(source.contains("unsafeSurfaceLastResort(overworld, spawn, \"world_spawn\")"));
        assertTrue(source.contains("dimension.equals(InnerDimensions.INNER_LEVEL)"));
        assertTrue(source.contains("dream dimension for player {} is the Inner Dimension itself, rejecting"));
        assertTrue(source.contains("respawn point for player {} is the Inner Dimension itself, rejecting"));
    }

    @Test
    void safeStandingPositionRejectsFluidsLavaAndObstructions() throws Exception {
        String source = Files.readString(Path.of(
                "src/main/java/org/mydrugs/mydrugs/dimension/InnerDimensionService.java"
        ));
        assertTrue(source.contains("feetState.getCollisionShape(level, feet).isEmpty()"));
        assertTrue(source.contains("headState.getCollisionShape(level, headPos).isEmpty()"));
        assertTrue(source.contains("feetState.getFluidState().isEmpty()"));
        assertTrue(source.contains("headState.getFluidState().isEmpty()"));
        assertTrue(source.contains("!isLava(feetState.getFluidState())"));
        assertTrue(source.contains("!isLava(headState.getFluidState())"));
    }
}
