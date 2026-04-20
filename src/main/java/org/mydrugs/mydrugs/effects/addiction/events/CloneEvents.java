package org.mydrugs.mydrugs.effects.addiction.events;

import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class CloneEvents {
    private CloneEvents() {
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        PlayerAddictionStats oldStats = event.getOriginal().getData(ModAttachments.PLAYER_ADDICTION.get());
        PlayerAddictionStats newStats = event.getEntity().getData(ModAttachments.PLAYER_ADDICTION.get());

        boolean wasDeath = event.isWasDeath();
        RandomSource random = event.getEntity().level().getRandom();

        newStats.copyFrom(oldStats, wasDeath, random);
    }
}