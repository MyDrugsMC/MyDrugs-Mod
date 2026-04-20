package org.mydrugs.mydrugs.core.drug;

import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectPort;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;

public class DrugService {
    private final EffectPort effectPort;

    public DrugService(EffectPort effectPort) {
        this.effectPort = effectPort;
    }

    public void consume(DrugModel drugModel, ConsumptionStrategy strategy) {
        for (DrugEffect effect : drugModel.getDrugEffects()) {
            effectPort.applyEffect(effect, strategy);
        }
    }
}
