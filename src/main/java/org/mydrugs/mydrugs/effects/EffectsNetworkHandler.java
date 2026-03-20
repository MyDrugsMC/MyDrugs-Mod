package org.mydrugs.mydrugs.effects;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.effects.payloads.IngameEffectPayload;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class EffectsNetworkHandler {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MyDrugs.MODID);

        registrar.playToServer(IngameEffectPayload.TYPE, IngameEffectPayload.STREAM_CODEC,
                ((payload, ctx) -> {
                    Player player = ctx.player();
                    DrugEffect effect = payload.effect();
                    int baseDuration = effect.getBaseDuration();
                    int basePotency = effect.getBasePotency();
                    switch (effect.getEffectType()) {
                        case NAUSEA -> player.addEffect(
                                new MobEffectInstance(
                                        MobEffects.NAUSEA,
                                        baseDuration,
                                        basePotency)
                        );
                        case SLOWNESS -> player.addEffect(
                                new MobEffectInstance(
                                        MobEffects.SLOWNESS,
                                        baseDuration,
                                        basePotency)
                        );
                    }
                }));
    }
}