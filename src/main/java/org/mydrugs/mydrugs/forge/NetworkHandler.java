package org.mydrugs.mydrugs.forge;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.mydrugs.mydrugs.MyDrugs;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class NetworkHandler {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MyDrugs.MODID);

        registrar.playToServer(EffectPayload.TYPE, EffectPayload.STREAM_CODEC,
                ((payload, ctx) -> {
                    Player player = ctx.player();
                    switch (payload.effect().getEffectType()) {
                        case NAUSEA -> player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 10*3));
                        case SLOWNESS -> player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 10*3));
                    }
                }));
    }
}