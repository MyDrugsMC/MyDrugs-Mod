package org.mydrugs.mydrugs.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InnerClientLifecycleTest {

    @BeforeEach
    @AfterEach
    void resetTracker() {
        InnerClientLifecycle.reset();
    }

    @Test
    void firesOnlyOnTheInsideToOutsideEdge() {
        // Outside the whole time: never fires.
        assertFalse(InnerClientLifecycle.leftInnerThisTick(false));
        assertFalse(InnerClientLifecycle.leftInnerThisTick(false));

        // Entering does not count as leaving.
        assertFalse(InnerClientLifecycle.leftInnerThisTick(true));
        assertTrue(InnerClientLifecycle.wasInInner());

        // Staying inside does not fire.
        assertFalse(InnerClientLifecycle.leftInnerThisTick(true));

        // The leaving edge fires exactly once.
        assertTrue(InnerClientLifecycle.leftInnerThisTick(false));
        assertFalse(InnerClientLifecycle.wasInInner());

        // And does not re-fire while we stay outside.
        assertFalse(InnerClientLifecycle.leftInnerThisTick(false));
    }

    @Test
    void reEntryArmsTheEdgeAgain() {
        InnerClientLifecycle.leftInnerThisTick(true);   // enter
        assertTrue(InnerClientLifecycle.leftInnerThisTick(false)); // leave -> fire
        InnerClientLifecycle.leftInnerThisTick(true);   // re-enter
        assertTrue(InnerClientLifecycle.leftInnerThisTick(false)); // leave again -> fire
    }

    @Test
    void resetClearsMembershipSoNextLeaveDoesNotFire() {
        InnerClientLifecycle.leftInnerThisTick(true); // enter
        assertTrue(InnerClientLifecycle.wasInInner());

        // A logout-style reset forgets membership, so the following outside tick is not an edge.
        InnerClientLifecycle.reset();
        assertFalse(InnerClientLifecycle.wasInInner());
        assertFalse(InnerClientLifecycle.leftInnerThisTick(false));
    }
}
