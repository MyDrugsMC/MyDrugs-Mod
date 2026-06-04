package org.mydrugs.mydrugs.core.drug.strategy;

import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;

public class DrinkingStrategy implements ConsumptionStrategy {
    private static final RouteEffectProfile PROFILE = new RouteEffectProfile(
            20 * 20,
            20 * 75,
            20 * 70,
            0.90F,
            1.20F,
            0.70F,
            0.95F
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
