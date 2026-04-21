package org.mydrugs.mydrugs.core.drug.strategy;

import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;

public class SniffingStrategy implements ConsumptionStrategy {
    @Override
    public int getNewPotency(DrugEffect drugEffect) {
        return (int) Math.round(drugEffect.getBasePotency() * 1.1);
    }

    @Override
    public int getNewDuration(DrugEffect drugEffect) {
        return (int) Math.round(drugEffect.getBaseDuration() * 0.5);
    }

    /** Sniffing delivers a stronger hit with shorter duration. */
    @Override
    public float getNewDose(float baseDose) {
        return baseDose * 1.3f;
    }
}
