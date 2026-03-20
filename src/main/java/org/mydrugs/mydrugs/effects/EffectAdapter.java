package org.mydrugs.mydrugs.effects;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.client.shaders.*;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectPort;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.effects.payloads.IngameEffectPayload;

public class EffectAdapter implements EffectPort {

    @Override
    public void applyEffect(DrugEffect drugEffect, ConsumptionStrategy strategy) {
        ShaderManager shaderManager = ShaderManager.INSTANCE;
        switch (drugEffect.getEffectType()) {
            case FOG, ACID_WARP, VOID_PULSE, CHROMATIC_DREAM
                    -> shaderManager.start(strategy.getNewDuration(drugEffect), drugEffect.getEffectType());
            case NAUSEA, SLOWNESS -> ClientPacketDistributor.sendToServer(new IngameEffectPayload(drugEffect));
        }
    }
}
