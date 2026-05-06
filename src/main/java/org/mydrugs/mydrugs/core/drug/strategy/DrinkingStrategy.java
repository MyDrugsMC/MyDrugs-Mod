package org.mydrugs.mydrugs.core.drug.strategy;

import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;

public class DrinkingStrategy implements ConsumptionStrategy {
    @Override
    public float getNewIntensity(DrugEffect drugEffect) {
        return drugEffect.getBaseIntensity() * 0.9F;
    }

    @Override
    public int getNewDuration(DrugEffect drugEffect) {
        return Math.round(drugEffect.getBaseDuration() * 1.15F);
    }

    @Override
    public float getNewDose(float baseDose) {
        return baseDose * 0.7F;
    }
}
