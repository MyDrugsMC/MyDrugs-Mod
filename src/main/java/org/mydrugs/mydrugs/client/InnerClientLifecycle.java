package org.mydrugs.mydrugs.client;

/**
 * Tracks whether the local client was inside {@link org.mydrugs.mydrugs.dimension.InnerDimensions#INNER_LEVEL}
 * on the previous client tick so the Inner Dimension's client-side effect state can be reset exactly
 * once on the leaving edge — not only on a full logout.
 *
 * <p>The leaving edge covers every way out that does not raise
 * {@code ClientPlayerNetworkEvent.LoggingOut}: stepping back through the portal / Self Anchor return,
 * a level change, or a respawn that lands outside the dimension. Logout/login are still handled by
 * {@link org.mydrugs.mydrugs.client.effects.ClientEventHandler}; this only adds the in-session
 * transition.
 *
 * <p>Pure state with no client imports so it can be unit-tested. The actual reset wiring (which
 * touches {@code Minecraft}) lives in the event handler.
 */
public final class InnerClientLifecycle {
    private static boolean wasInInner;

    private InnerClientLifecycle() {
    }

    /**
     * Records the current Inner Dimension membership and returns {@code true} exactly on the tick
     * the player transitions from inside the dimension to outside it.
     */
    public static boolean leftInnerThisTick(boolean nowInInner) {
        boolean left = wasInInner && !nowInInner;
        wasInInner = nowInInner;
        return left;
    }

    /** True if the last observed tick had the player inside the Inner Dimension. */
    public static boolean wasInInner() {
        return wasInInner;
    }

    /** Forgets the tracked membership. Called on logout/login so a fresh world starts clean. */
    public static void reset() {
        wasInInner = false;
    }
}
