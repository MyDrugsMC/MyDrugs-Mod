package org.mydrugs.mydrugs.effects.addiction.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.effects.addiction.manager.AddictionManager;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class PlayerTickEvents {
    private PlayerTickEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        AddictionManager.tickPlayer(player);
    }
}