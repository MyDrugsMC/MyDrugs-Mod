package org.mydrugs.mydrugs.events;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.items.AdnScraperItem;
import org.mydrugs.mydrugs.items.ModItems;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class AdnScraperEvents {
    private AdnScraperEvents() {
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        if (!(event.getTarget() instanceof LivingEntity target)) {
            return;
        }

        InteractionHand hand = player.getMainHandItem().is(ModItems.ADN_SCRAPER.get())
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(ModItems.ADN_SCRAPER.get())) {
            return;
        }

        event.setCanceled(true);
        AdnScraperItem.scrapeTarget(stack, player, target, hand);
    }
}
