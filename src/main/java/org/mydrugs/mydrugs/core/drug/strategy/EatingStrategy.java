package org.mydrugs.mydrugs.core.drug.strategy;

import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;

public class EatingStrategy implements ConsumptionStrategy {
    private static final RouteEffectProfile PROFILE = new RouteEffectProfile(
            20 * 35,
            20 * 90,
            20 * 75,
            0.85F,
            1.60F,
            0.80F,
            0.85F
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
