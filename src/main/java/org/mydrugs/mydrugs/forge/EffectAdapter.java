package org.mydrugs.mydrugs.forge;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.core.DrugEffect;
import org.mydrugs.mydrugs.core.EffectPort;

public class EffectAdapter implements EffectPort {
    @Override
    public void applyEffect(DrugEffect effect) {
        ClientPacketDistributor.sendToServer(new EffectPayload(effect));
    }
}
