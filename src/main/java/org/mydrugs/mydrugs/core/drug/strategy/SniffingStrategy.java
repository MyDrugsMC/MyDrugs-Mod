package org.mydrugs.mydrugs.core.drug.strategy;

import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;

public class SniffingStrategy implements ConsumptionStrategy {
    private static final RouteEffectProfile PROFILE = new RouteEffectProfile(
            8,
            20 * 24,
            20 * 25,
            1.15F,
            0.65F,
            1.25F,
            1.25F
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
