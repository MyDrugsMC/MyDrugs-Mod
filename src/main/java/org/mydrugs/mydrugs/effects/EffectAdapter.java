package org.mydrugs.mydrugs.effects;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.ModSounds;
import org.mydrugs.mydrugs.client.shaders.ShaderManager;
import org.mydrugs.mydrugs.client.sounds.ClientSoundsHandler;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectPort;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.effects.payloads.IngameEffectPayload;

public class EffectAdapter implements EffectPort {

    @Override
    public void applyEffect(DrugEffect drugEffect, ConsumptionStrategy strategy) {
        int duration = strategy != null
                ? strategy.getNewDuration(drugEffect)
                : drugEffect.getBaseDuration();

        int potency = strategy != null
                ? strategy.getNewPotency(drugEffect)
                : drugEffect.getBasePotency();

        ShaderManager shaderManager = ShaderManager.INSTANCE;

        switch (drugEffect.getEffectType().getCategory()) {
            case SHADER -> shaderManager.add(duration, drugEffect.getEffectType());

            case MINECRAFT_EFFECT ->
                    ClientPacketDistributor.sendToServer(
                            new IngameEffectPayload(drugEffect.getEffectType(), duration, potency)
                    );

            case SOUND_EFFECT ->
                    ClientSoundsHandler.setToStart(
                            ModSounds.fromEffectType(drugEffect.getEffectType()),
                            duration
                    );
        }
    }
}
