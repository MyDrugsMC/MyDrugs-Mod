package org.mydrugs.mydrugs.forge.effects;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.core.DrugEffect;
import org.mydrugs.mydrugs.core.EffectPort;
import org.mydrugs.mydrugs.forge.client.shaders.*;
import org.mydrugs.mydrugs.forge.effects.payloads.IngameEffectPayload;

public class EffectAdapter extends EffectPort {


    @Override
    protected void applyIngameEffect(DrugEffect effect) {
        ClientPacketDistributor.sendToServer(new IngameEffectPayload(effect));
    }

    @Override
    protected void applyShader(DrugEffect effect) {
        int duration = 10*20;
        ShaderManager shaderManager = ShaderManager.INSTANCE;
        switch (effect.getEffect()) {
            case FOG -> shaderManager.start(duration, FogShader.class);
            case ACID_WARP -> shaderManager.start(duration, AcidWarpShader.class);
            case VOID_PULSE -> shaderManager.start(duration, VoidPulseShader.class);
            case CHROMATIC_DREAM -> shaderManager.start(duration, ChromaticDreamShader.class);
        }
    }

    @Override
    protected void applyIngamePermanentBuff(DrugEffect effect) {
        //TODO switch between every ingame buffs implemented
    }

    @Override
    protected void applyMisc(DrugEffect effect) {
        //TODO switch between every misc buffs implemented
    }
}
