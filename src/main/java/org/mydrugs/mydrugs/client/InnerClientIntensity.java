package org.mydrugs.mydrugs.client;

import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.Config;

/**
 * Single source of truth for how strongly the Inner Dimension's client visuals move, so the
 * accessibility policy is applied consistently instead of being re-derived in every controller.
 *
 * <p>Before this existed, half a dozen Inner client classes each carried a private copy of the same
 * {@code reducedMotionMode} / {@code particleDensityMultiplier} reads. They are centralised here.
 *
 * <p>The guiding rule (see {@code docs/GAMEPLAY_DESIGN.md} accessibility note): reduced motion
 * <em>damps motion</em> — drifting skies, churn, particle volume, screen stings — but never removes
 * the dimension's identity. Colour, fog and atmosphere are owned elsewhere and stay at full strength
 * regardless of these scalars.
 *
 * <p>The scalar maths is split into pure {@code (boolean reduced) -> double} cores so it can be unit
 * tested without a running client; the public no-arg accessors are thin wrappers that read the live
 * client config. No new config keys are introduced — only the existing {@code reducedMotionMode} and
 * {@code particleDensityMultiplier} are consulted.
 */
public final class InnerClientIntensity {
    /** Particle volume retained under reduced motion (matches the legacy footstep-trail factor). */
    static final double REDUCED_PARTICLE_SCALE = 0.35D;
    /** Sky drift/churn fully freezes under reduced motion (still rendered, just static). */
    static final double REDUCED_SKY_MOTION_SCALE = 0.0D;
    /** Full-screen stings (region-crossing flash, integration bloom) are halved under reduced motion. */
    static final double REDUCED_SCREEN_EFFECT_SCALE = 0.5D;

    private InnerClientIntensity() {
    }

    // -------------------------------------------------------------------------
    // Live config readers (safe to call before the config is loaded)
    // -------------------------------------------------------------------------

    /** Whether the player has enabled reduced motion. Defaults to {@code false} pre-config-load. */
    public static boolean reducedMotion() {
        try {
            return Config.CLIENT.reducedMotionMode.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    /** Player-chosen particle density multiplier in {@code [0, 1]}. Independent of reduced motion. */
    public static double particleDensity() {
        try {
            return Mth.clamp(Config.CLIENT.particleDensityMultiplier.get(), 0.0D, 1.0D);
        } catch (IllegalStateException ignored) {
            return 1.0D;
        }
    }

    // -------------------------------------------------------------------------
    // Documented intensity accessors (live)
    // -------------------------------------------------------------------------

    /**
     * Motion-driven particle volume factor in {@code [0, 1]}. This is the reduced-motion thinning
     * only; multiply by {@link #particleDensity()} for the full per-particle budget.
     */
    public static double particleScale() {
        return particleScale(reducedMotion());
    }

    /** Sky drift/churn speed factor: {@code 1} normally, {@code 0} (frozen) under reduced motion. */
    public static double skyMotionScale() {
        return skyMotionScale(reducedMotion());
    }

    /** Full-screen sting strength factor: {@code 1} normally, halved under reduced motion. */
    public static double screenEffectScale() {
        return screenEffectScale(reducedMotion());
    }

    // -------------------------------------------------------------------------
    // Pure scalar cores (unit tested)
    // -------------------------------------------------------------------------

    static double particleScale(boolean reduced) {
        return reduced ? REDUCED_PARTICLE_SCALE : 1.0D;
    }

    static double skyMotionScale(boolean reduced) {
        return reduced ? REDUCED_SKY_MOTION_SCALE : 1.0D;
    }

    static double screenEffectScale(boolean reduced) {
        return reduced ? REDUCED_SCREEN_EFFECT_SCALE : 1.0D;
    }
}
