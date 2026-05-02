package org.mydrugs.mydrugs.effects;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.effects.payloads.DrugVisualPayload;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class EffectsNetworkHandler {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MyDrugs.NETWORK_VERSION);
        registrar.playToClient(DrugVisualPayload.TYPE, DrugVisualPayload.STREAM_CODEC);
    }
}
