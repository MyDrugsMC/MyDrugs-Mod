package org.mydrugs.mydrugs.core.drug.strategy;

import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;

public record SmokingStrategy(boolean bang, boolean joint) implements ConsumptionStrategy {

    @Override
    public int getNewPotency(DrugEffect drugEffect) {
        return (int) Math.round(drugEffect.getBasePotency() * 0.9);
    }

    @Override
    public int getNewDuration(DrugEffect drugEffect) {
        return (int) Math.round(drugEffect.getBaseDuration() * 1.0);
    }

    /** Smoking is the baseline route — dose multiplier 1.0. */
    @Override
    public float getNewDose(float baseDose) {
        return baseDose * 1.0f;
    }
}
