package org.mydrugs.mydrugs.core.drug;

public record AddictionCategoryConfig(
        float addictionRate,
        float withdrawalIntensity,
        float toleranceGainRate,
        float toleranceDecayPerSecond,
        float addictionRecoveryPerSecond,
        int withdrawalOnsetTicks,
        int risingTicks,
        int peakTicks,
        int recoveryTicks,
        float reliefStrength,
        float relapseWeight
) {
}