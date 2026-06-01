package org.mydrugs.mydrugs.addiction.events;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.explain.AddictionRecoveryFeedback;
import org.mydrugs.mydrugs.addiction.manager.state.ResilienceManager;
import org.mydrugs.mydrugs.addiction.manager.state.StressManager;
import org.mydrugs.mydrugs.core.drug.integration.RecoveryProgressManager;
import org.mydrugs.mydrugs.core.drug.integration.RecoveryProgressManager.ActionKind;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class FoodRecoveryEvents {
    private FoodRecoveryEvents() {
    }

    @SubscribeEvent
    public static void onFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack used = event.getItem();

        // Simple filter: only reward actual edible items
        FoodProperties food = used.get(DataComponents.FOOD);
        if (food == null) {
            return;
        }

        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        // Small immediate relief
        StressManager.reduce(stats, 0.03F);

        // Tiny resilience gain
        ResilienceManager.add(stats, 0.001F);
        RecoveryProgressManager.onProductiveAction(player, ActionKind.FOOD, Math.max(1, food.nutrition()) * 0.10F);
        AddictionRecoveryFeedback.sendForAction(player, ActionKind.FOOD);
    }
}
