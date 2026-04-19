package org.mydrugs.mydrugs.effects.addiction.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.effects.addiction.manager.AddictionManager;
import org.mydrugs.mydrugs.effects.addiction.manager.recovery.SleepRecoveryManager;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class SleepEvents {
    private SleepEvents() {}

    @SubscribeEvent
    public static void onCanSleep(CanPlayerSleepEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        float severity = AddictionManager.getGlobalSeverity(player);
        boolean canSleep = SleepRecoveryManager.canSleep(player, severity);

        if (!canSleep) {
            event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
    }

    @SubscribeEvent
    public static void onWake(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        SleepRecoveryManager.onWakeUp(player);
    }
}