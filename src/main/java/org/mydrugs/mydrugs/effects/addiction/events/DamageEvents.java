package org.mydrugs.mydrugs.effects.addiction.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.manager.state.StressManager;
import org.mydrugs.mydrugs.effects.addiction.util.AddictionMath;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class DamageEvents {
    private DamageEvents() {}

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        float finalDamage = event.getNewDamage();
        if (finalDamage <= 0.0F) return;

        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        for (DrugCategory category : DrugCategory.values()) {
            float addictionNorm = stats.get(category).addictionNorm();
            if (addictionNorm <= 0.05F) continue;

            float spike = finalDamage * (0.60F + addictionNorm * 0.60F);
            spike *= (1.0F - stats.resilience * 0.40F);

            stats.get(category).baseWithdrawalMeter =
                    AddictionMath.clamp(stats.get(category).baseWithdrawalMeter + spike, 0.0F, 100.0F);
        }

        StressManager.onDamage(player, finalDamage);
    }
}