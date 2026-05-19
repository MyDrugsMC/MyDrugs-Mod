package org.mydrugs.mydrugs.recovery;

import org.mydrugs.mydrugs.Config;

public enum RecoveryRoomTier {
    NONE(0, "recovery.mydrugs.room.none", 1.00F, 1.00F, 1.00F, 0.00F, 0.00F, 0.00F),
    FRAGILE_ROOM(25, "recovery.mydrugs.room.fragile", 1.10F, 1.05F, 1.04F, 0.04F, 0.05F, 0.06F),
    RESTING_ROOM(45, "recovery.mydrugs.room.resting", 1.25F, 1.12F, 1.08F, 0.08F, 0.10F, 0.12F),
    SAFE_ROOM(65, "recovery.mydrugs.room.safe", 1.40F, 1.22F, 1.13F, 0.12F, 0.18F, 0.22F),
    SANCTUARY(85, "recovery.mydrugs.room.sanctuary", 1.60F, 1.35F, 1.20F, 0.16F, 0.30F, 0.35F);

    private final int minScore;
    private final String translationKey;
    private final float withdrawalRecoveryMultiplier;
    private final float toleranceDecayMultiplier;
    private final float addictionRecoveryMultiplier;
    private final float stressTargetReduction;
    private final float badTripPressureReduction;
    private final float badTripIntensityReduction;

    RecoveryRoomTier(
            int minScore,
            String translationKey,
            float withdrawalRecoveryMultiplier,
            float toleranceDecayMultiplier,
            float addictionRecoveryMultiplier,
            float stressTargetReduction,
            float badTripPressureReduction,
            float badTripIntensityReduction
    ) {
        this.minScore = minScore;
        this.translationKey = translationKey;
        this.withdrawalRecoveryMultiplier = withdrawalRecoveryMultiplier;
        this.toleranceDecayMultiplier = toleranceDecayMultiplier;
        this.addictionRecoveryMultiplier = addictionRecoveryMultiplier;
        this.stressTargetReduction = stressTargetReduction;
        this.badTripPressureReduction = badTripPressureReduction;
        this.badTripIntensityReduction = badTripIntensityReduction;
    }

    public int minScore() {
        return minScore;
    }

    public String translationKey() {
        return translationKey;
    }

    public float withdrawalRecoveryMultiplier() {
        return withdrawalRecoveryMultiplier;
    }

    public float toleranceDecayMultiplier() {
        return toleranceDecayMultiplier;
    }

    public float addictionRecoveryMultiplier() {
        return addictionRecoveryMultiplier;
    }

    public float stressTargetReduction() {
        return stressTargetReduction;
    }

    public float badTripPressureReduction() {
        return badTripPressureReduction;
    }

    public float badTripIntensityReduction() {
        return badTripIntensityReduction;
    }

    public boolean isValidRoom() {
        return this != NONE;
    }

    public int networkId() {
        return ordinal();
    }

    public static RecoveryRoomTier byNetworkId(int id) {
        RecoveryRoomTier[] values = values();
        if (id < 0 || id >= values.length) {
            return NONE;
        }
        return values[id];
    }

    public static RecoveryRoomTier fromScore(int score) {
        if (score >= threshold(SANCTUARY)) {
            return SANCTUARY;
        }
        if (score >= threshold(SAFE_ROOM)) {
            return SAFE_ROOM;
        }
        if (score >= threshold(RESTING_ROOM)) {
            return RESTING_ROOM;
        }
        if (score >= threshold(FRAGILE_ROOM)) {
            return FRAGILE_ROOM;
        }
        return NONE;
    }

    private static int threshold(RecoveryRoomTier tier) {
        try {
            return switch (tier) {
                case FRAGILE_ROOM -> Config.SERVER.recoveryRoomFragileThreshold.get();
                case RESTING_ROOM -> Config.SERVER.recoveryRoomRestingThreshold.get();
                case SAFE_ROOM -> Config.SERVER.recoveryRoomSafeThreshold.get();
                case SANCTUARY -> Config.SERVER.recoveryRoomSanctuaryThreshold.get();
                case NONE -> 0;
            };
        } catch (Throwable ignored) {
            return tier.minScore;
        }
    }
}
