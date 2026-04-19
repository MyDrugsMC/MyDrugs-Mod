package org.mydrugs.mydrugs.core.drug.effect;

import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;

public interface EffectPort {
    void applyEffect(DrugEffect effect, ConsumptionStrategy strategy);
}