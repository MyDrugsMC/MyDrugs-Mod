package org.mydrugs.mydrugs.addiction.withdrawal;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.core.drug.AddictionCategoryConfig;
import org.mydrugs.mydrugs.core.drug.AddictionConfigs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.tolerance.ToleranceManager;
import org.mydrugs.mydrugs.addiction.util.AddictionMath;

public final class WithdrawalManager {
    private WithdrawalManager() {
    }

    public static void tickDrug(ServerPlayer player,
                                PlayerAddictionStats playerStats,
                                DrugId drugId,
                                boolean inCombat,
                                int companions,
                                boolean inSafeZone) {
        DrugAddictionStats stats = playerStats.getDrugStats(drugId);
        if (stats == null) {
            return;
        }

        AddictionCategoryConfig cfg = AddictionConfigs.get(DrugRegistry.getCategory(drugId));

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

        float target = Config.SERVER.withdrawalEnabled.get()
                ? AddictionMath.computeWithdrawalTarget(
                stats.addictionNorm(),
                phaseFactor,
                context,
                playerStats.resilience,
                cfg
        ) * Config.SERVER.withdrawalSeverityMultiplier.get().floatValue()
                : 0.0F;

        float response = AddictionMath.computeWithdrawalResponseRate(stats.addictionNorm());
        float recovery = AddictionMath.computeWithdrawalRecoveryRate(
                playerStats.resilience,
                player.isSleeping(),
                inSafeZone,
                companions > 0,
                playerStats.temporaryEffects.hasCalmRelief(now)
        );

        stats.baseWithdrawalMeter += (target - stats.baseWithdrawalMeter) * response;
        stats.baseWithdrawalMeter -= recovery;
        stats.baseWithdrawalMeter = AddictionMath.clamp(stats.baseWithdrawalMeter, 0.0F, 100.0F);

        ToleranceManager.decay(player, playerStats, drugId, abstinence);
    }
}
