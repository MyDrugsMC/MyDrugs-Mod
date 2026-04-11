package org.mydrugs.mydrugs.effects.addiction.events;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionPayloads;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class RegisterPayloadEvents {
    private RegisterPayloadEvents() {}

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        AddictionPayloads.registerCommon(event);
    }
}