package org.mydrugs.mydrugs.effects.addiction.manager;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.core.drug.AddictionCategoryConfig;
import org.mydrugs.mydrugs.core.drug.AddictionConfigs;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.effects.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.util.AddictionMath;

public final class WithdrawalManager {
    private WithdrawalManager() {
    }

    public static void tickCategory(ServerPlayer player, PlayerAddictionStats playerStats, DrugCategory category, boolean inCombat, int companions, boolean inSafeZone) {
        DrugAddictionStats stats = playerStats.get(category);
        AddictionCategoryConfig cfg = AddictionConfigs.get(category);

        long now = player.level().getGameTime();
        long abstinence = Math.max(0L, now - stats.lastUseTime);

        float phaseFactor = AddictionMath.computePhaseFactor(abstinence, cfg);
        long time = player.level().getDayTime() % 24000L;
        boolean night = (time >= 13000L && time < 23000L);
        float context = AddictionMath.computeContextMultiplier(
                night,
                playerStats.stressLevel,
                companions,
                inSafeZone,
                inCombat
        );

        float target = AddictionMath.computeWithdrawalTarget(
                stats.addictionNorm(),
                phaseFactor,
                context,
                playerStats.resilience,
                cfg
        );

        float response = AddictionMath.computeWithdrawalResponseRate(stats.addictionNorm());
        float recovery = AddictionMath.computeWithdrawalRecoveryRate(
                playerStats.resilience,
                player.isSleeping(),
                inSafeZone,
                companions > 0,
                playerStats.temporaryEffects.hasDiaryCalm(now)
        );

        stats.baseWithdrawalMeter += (target - stats.baseWithdrawalMeter) * response;
        stats.baseWithdrawalMeter -= recovery;
        stats.baseWithdrawalMeter = AddictionMath.clamp(stats.baseWithdrawalMeter, 0.0F, 100.0F);

        ToleranceManager.decay(player, playerStats, category, abstinence);
    }
}