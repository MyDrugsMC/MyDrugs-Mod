package org.mydrugs.mydrugs.effects.addiction.events;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.manager.ResilienceManager;
import org.mydrugs.mydrugs.effects.addiction.manager.StressManager;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class FoodRecoveryEvents {
    private FoodRecoveryEvents() {
    }

    @SubscribeEvent
    public static void onFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack used = event.getItem();

        // Simple filter: only reward actual edible items
        if (!used.has(DataComponents.FOOD)) {
            return;
        }

        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        // Small immediate relief
        StressManager.reduce(stats, 0.03F);

        // Tiny resilience gain
        ResilienceManager.add(stats, 0.001F);

        player.displayClientMessage(Component.literal(
                "A decent meal helps you steady yourself."
        ), true);
    }
}