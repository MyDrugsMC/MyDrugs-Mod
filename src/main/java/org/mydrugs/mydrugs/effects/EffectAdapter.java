package org.mydrugs.mydrugs.effects;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.client.shaders.*;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectPort;
import org.mydrugs.mydrugs.effects.payloads.IngameEffectPayload;

public class EffectAdapter implements EffectPort {

    @Override
    public void applyEffect(DrugEffect drugEffect) {
        ShaderManager shaderManager = ShaderManager.INSTANCE;
        switch (drugEffect.getType()) {
            case FOG, ACID_WARP, VOID_PULSE, CHROMATIC_DREAM
                    -> shaderManager.start(drugEffect.getBaseDuration(), drugEffect.getType());
            case NAUSEA, SLOWNESS -> ClientPacketDistributor.sendToServer(new IngameEffectPayload(drugEffect));
        }
    }
}
