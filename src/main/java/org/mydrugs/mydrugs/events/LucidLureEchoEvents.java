package org.mydrugs.mydrugs.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.items.ModItems;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class LucidLureEchoEvents {
    public static final String ECHO_TAG = MyDrugs.MODID + "_lucid_lure_echo";

    private LucidLureEchoEvents() {
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!entity.getTags().contains(ECHO_TAG)) {
            return;
        }
        entity.spawnAtLocation(serverLevel, new ItemStack(ModItems.UNSTABLE_PEARL.get()));
    }
}
