package org.mydrugs.mydrugs.effects.addiction.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.effects.addiction.manager.effect.DrugEffectRuntimeManager;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class CustomDrugEffectEvents {
    private CustomDrugEffectEvents() {
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        float multiplier = DrugEffectRuntimeManager.getMiningSpeedMultiplier(player);
        if (Math.abs(multiplier - 1.0F) > 0.001F) {
            event.setNewSpeed(event.getNewSpeed() * multiplier);
        }
    }

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        float resistance = DrugEffectRuntimeManager.getDamageResistance(player);
        if (resistance > 0.0F) {
            event.setNewDamage(event.getNewDamage() * (1.0F - resistance));
        }
    }
}
