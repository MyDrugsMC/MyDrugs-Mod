package org.mydrugs.mydrugs.core.drug.strategy;

import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;

public class InjectingStrategy implements ConsumptionStrategy {
    private static final RouteEffectProfile PROFILE = new RouteEffectProfile(
            0,
            20 * 35,
            20 * 90,
            1.80F,
            1.15F,
            1.80F,
            1.70F
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
