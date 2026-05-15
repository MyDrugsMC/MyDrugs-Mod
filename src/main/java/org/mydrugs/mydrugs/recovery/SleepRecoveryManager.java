package org.mydrugs.mydrugs.recovery;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.addiction.config.SymptomThresholds;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.manager.state.ResilienceManager;
import org.mydrugs.mydrugs.addiction.manager.state.StressManager;
import org.mydrugs.mydrugs.addiction.util.AddictionMath;

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

        for (DrugCategory category : DrugCategory.values()) {
            stats.reduceWithdrawalInCategory(category, 10.0F);
        }

        StressManager.reduce(stats, AddictionConstants.RELIEF_ON_WAKE_UP);
        ResilienceManager.onSuccessfulSleep(stats);
    }
}