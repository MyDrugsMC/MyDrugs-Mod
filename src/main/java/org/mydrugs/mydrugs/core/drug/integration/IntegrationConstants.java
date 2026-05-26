package org.mydrugs.mydrugs.core.drug.integration;

/**
 * Tunable constants for the integration arc (Phase A).
 */
public final class IntegrationConstants {
    /** A drug must have driven addiction at least this high to ever become integratable. */
    public static final float PEAK_THRESHOLD = 60.0F;
    /** Current addiction must have fallen at or below this for a drug to be eligible. */
    public static final float LOW_THRESHOLD = 8.0F;
    /** Lifetime dose required: the drug must have been used massively, not dabbled with. */
    public static final float MIN_LIFETIME_DOSE = 50.0F;

    /** How often (ticks) integrated traits are reapplied through the effect runtime. */
    public static final int TRAIT_REFRESH_INTERVAL_TICKS = 20;
    /** Duration each refresh grants, kept longer than the refresh interval so traits never lapse. */
    public static final int TRAIT_EFFECT_DURATION_TICKS = 40;
    /** How often (ticks) the integrated-trait set is resynced to the client. */
    public static final int CLIENT_SYNC_INTERVAL_TICKS = 100;

    // --- Active recovery (Phase B) ---
    /** Recovery progress (0..1) gained per unit-weight productive action. */
    public static final float RECOVERY_PROGRESS_PER_ACTION = 0.006F;
    /** Raw addiction burned down per unit-weight productive action (active detox). */
    public static final float DETOX_PER_ACTION = 0.05F;
    /** Multiplier when a productive action is part of the next drug's production stage. */
    public static final float NEXT_DRUG_WORK_BONUS = 1.75F;
    /** Recovery Resonance is support only; it may boost recovery but must not complete integration. */
    public static final float RECOVERY_RESONANCE_ACTION_WEIGHT = 1.75F;
    /** Hard cap for Resonator recovery aid so active production still has to finish recovery. */
    public static final float RECOVERY_RESONANCE_PROGRESS_CAP = 0.95F;

    private IntegrationConstants() {
    }
}
