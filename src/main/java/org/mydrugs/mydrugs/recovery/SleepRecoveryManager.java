package org.mydrugs.mydrugs.recovery;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.addiction.config.SymptomThresholds;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.explain.AddictionRecoveryFeedback;
import org.mydrugs.mydrugs.addiction.manager.state.ResilienceManager;
import org.mydrugs.mydrugs.addiction.manager.state.StressManager;
import org.mydrugs.mydrugs.addiction.util.AddictionMath;
import org.mydrugs.mydrugs.core.drug.integration.RecoveryProgressManager;
import org.mydrugs.mydrugs.core.drug.integration.RecoveryProgressManager.ActionKind;

public final class SleepRecoveryManager {
    private SleepRecoveryManager() {
    }

    public static boolean canSleep(ServerPlayer player, float severity) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        if (player.level().getGameTime() < stats.sleepBlockedUntil) {
            return false;
        }

        if (severity < SymptomThresholds.INSOMNIA) {
            return true;
        }

        int delay = AddictionMath.computeInsomniaDelayTicks(severity);
        if (stats.temporaryEffects.hasSleepBonus(player.level().getGameTime())) {
            delay = 0;
        }

        stats.sleepBlockedUntil = player.level().getGameTime() + delay;
        return delay <= 0;
    }

    public static void onWakeUp(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        RecoveryRoomReport room = RecoveryRoomManager.getBestRoom(player).orElse(null);
        boolean restModule = RecoveryRoomManager.isValidRecoveryRoom(room)
                && room.hasModule(SanctuaryModule.REST_MODULE);
        float withdrawalRelief = restModule ? 14.0F : 10.0F;

        for (DrugCategory category : DrugCategory.values()) {
            stats.reduceWithdrawalInCategory(category, withdrawalRelief);
        }

        StressManager.reduce(stats, AddictionConstants.RELIEF_ON_WAKE_UP);
        ResilienceManager.onSuccessfulSleep(stats);
        if (restModule) {
            StressManager.reduce(stats, 0.015F);
            ResilienceManager.add(stats, 0.0015F);
        }
        RecoveryProgressManager.onProductiveAction(player, ActionKind.SLEEP_REST, restModule ? 1.15F : 1.0F);
        AddictionRecoveryFeedback.sendForAction(player, ActionKind.SLEEP_REST);
    }
}
