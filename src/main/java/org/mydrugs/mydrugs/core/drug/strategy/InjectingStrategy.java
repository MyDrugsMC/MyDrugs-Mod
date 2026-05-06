package org.mydrugs.mydrugs.core.drug.strategy;

import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;

public class InjectingStrategy implements ConsumptionStrategy {
    @Override
    public float getNewIntensity(DrugEffect drugEffect) {
        return drugEffect.getBaseIntensity() * 2.0F;
    }

    @Override
    public int getNewDuration(DrugEffect drugEffect) {
        return (int) Math.round(drugEffect.getBaseDuration() * 2.5);
    }

    /** Injecting bypasses first-pass metabolism — highest dose multiplier. */
    @Override
    public float getNewDose(float baseDose) {
        return baseDose * 2.0f;
    }
}
