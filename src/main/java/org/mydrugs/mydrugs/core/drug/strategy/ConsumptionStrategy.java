package org.mydrugs.mydrugs.core.drug.strategy;

import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;

public interface ConsumptionStrategy {
    int getNewPotency(DrugEffect drugEffect);

    int getNewDuration(DrugEffect drugEffect);

    /**
     * Returns the dose amount added when consuming one drug via this route.
     * Base dose of 1.0 = one standard consume. Strategies can multiply it
     * (e.g. injecting doubles the dose, eating reduces it slightly).
     */
    default float getNewDose(float baseDose) {
        return baseDose;
    }
}
