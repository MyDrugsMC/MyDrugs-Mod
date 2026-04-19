package org.mydrugs.mydrugs.effects.addiction.manager;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.config.SymptomThresholds;
import org.mydrugs.mydrugs.effects.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.util.AddictionMath;

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
            DrugAddictionStats drug = stats.get(category);
            drug.baseWithdrawalMeter = Math.max(0.0F, drug.baseWithdrawalMeter - 10.0F);
        }

        StressManager.reduce(stats, 0.12F);
        ResilienceManager.onSuccessfulSleep(stats);
    }
}