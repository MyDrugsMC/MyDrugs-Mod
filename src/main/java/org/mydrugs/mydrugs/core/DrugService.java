package org.mydrugs.mydrugs.core;

public class DrugService {
    EffectPort effectPort;

    public DrugService(EffectPort effectPort) {
        this.effectPort = effectPort;
    }

    public void consume(DrugModel drugModel) {
        for (DrugEffect effect : drugModel.getDrugEffects()) {
            effectPort.applyEffect(effect);
        }
    }
}
