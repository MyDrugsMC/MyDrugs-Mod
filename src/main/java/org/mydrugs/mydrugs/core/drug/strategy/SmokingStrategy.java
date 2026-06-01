package org.mydrugs.mydrugs.core.drug.strategy;

import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;

public record SmokingStrategy(boolean bang, boolean joint) implements ConsumptionStrategy {
    private static final RouteEffectProfile PROFILE = new RouteEffectProfile(
            20,
            20 * 35,
            20 * 35,
            0.95F,
            0.95F,
            1.0F,
            1.0F
    );

    @Override
    public float getNewIntensity(DrugEffect drugEffect) {
        return drugEffect.getBaseIntensity() * PROFILE.intensityMultiplier();
    }

    @Override
    public int getNewDuration(DrugEffect drugEffect) {
        return Math.max(1, Math.round(drugEffect.getBaseDuration() * PROFILE.durationMultiplier()));
    }

    @Override
    public RouteEffectProfile routeEffectProfile() {
        return PROFILE;
    }
}
