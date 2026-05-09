package org.mydrugs.mydrugs.effects.addiction.events;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.manager.effect.DrugEffectRuntimeManager;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class CustomDrugEffectEvents {
    private CustomDrugEffectEvents() {
    }

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            float resistance = DrugEffectRuntimeManager.getDamageResistance(player);
            if (resistance > 0.0F) {
                event.setNewDamage(event.getNewDamage() * (1.0F - resistance));
            }
            DrugEffectRuntimeManager.triggerStimulantAdrenaline(player, event.getNewDamage());
        }

        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            float multiplier = DrugEffectRuntimeManager.getAttackDamageMultiplier(attacker);
            if (multiplier > 1.001F) {
                event.setNewDamage(event.getNewDamage() * multiplier);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(event.getNewAboutToBeSetTarget() instanceof ServerPlayer player)) {
            return;
        }

        float reduction = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.MOB_DETECTION_REDUCTION);
        if (reduction <= 0.01F) {
            return;
        }

        var followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
        double vanillaRange = followRange == null ? 16.0D : followRange.getValue();
        double reducedRange = vanillaRange * Math.max(0.45D, 1.0D - Math.min(0.45F, reduction));
        if (mob.distanceToSqr(player) > reducedRange * reducedRange) {
            event.setCanceled(true);
        }
    }
}
