package org.mydrugs.mydrugs.core.drug.strategy;

import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;

public class EatingStrategy implements ConsumptionStrategy {
    @Override
    public float getNewIntensity(DrugEffect drugEffect) {
        return drugEffect.getBaseIntensity() * 1.2F;
    }

    @Override
    public int getNewDuration(DrugEffect drugEffect) {
        return (int) Math.round(drugEffect.getBaseDuration() * 1.7);
    }

    /** Eating has lower bioavailability — slightly reduced dose. */
    @Override
    public float getNewDose(float baseDose) {
        return baseDose * 0.8f;
    }
}
