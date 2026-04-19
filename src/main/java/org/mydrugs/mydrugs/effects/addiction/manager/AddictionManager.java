package org.mydrugs.mydrugs.effects.addiction.manager;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.core.drug.AddictionCategoryConfig;
import org.mydrugs.mydrugs.core.drug.AddictionConfigs;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.util.AddictionMath;

public final class AddictionManager {
    private AddictionManager() {
    }

    public static void consume(ServerPlayer player, DrugCategory category, float dose) {
        PlayerAddictionStats playerStats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugAddictionStats drugStats = playerStats.get(category);
        AddictionCategoryConfig cfg = AddictionConfigs.get(category);

        float baseGain = AddictionMath.computeAddictionGain(dose, cfg, playerStats.geneticFactor, drugStats.tolerance);
        float finalGain = RelapseManager.applyRelapseMultiplier(baseGain, drugStats);

        drugStats.addictionValue = AddictionMath.clamp(drugStats.addictionValue + finalGain, 0.0F, 1000.0F);
        drugStats.lastUseTime = player.level().getGameTime();

        ToleranceManager.onUse(playerStats, category, dose);
        RelapseManager.onUse(category, drugStats);

        float relief = AddictionMath.computeEffectiveRelief(cfg.reliefStrength(), drugStats.tolerance);
        drugStats.baseWithdrawalMeter = Math.max(0.0F, drugStats.baseWithdrawalMeter - relief);
        drugStats.peakHistoricalAddiction = Math.max(drugStats.peakHistoricalAddiction, drugStats.addictionValue);

        playerStats.stressLevel = Math.max(0.0F, playerStats.stressLevel - 0.03F);
    }

    public static void tickPlayer(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        ItemEffectHandler.tickHeadphones(player);

        boolean inCombat = player.tickCount - player.getLastHurtByMobTimestamp() < 200;
        int companions = SocialReliefManager.countCompanions(player, 12.0D);
        boolean inSafeZone = SafeZoneManager.isInSafeZone(player);
        long gameTime = player.level().getGameTime();

        float maxSeverity = 0.0F;
        float sumSeverity = 0.0F;
        int active = 0;

        for (DrugCategory category : DrugCategory.values()) {
            AddictionCategoryConfig cfg = AddictionConfigs.get(category);

            if (cfg == null) continue;

            DrugAddictionStats drugStats = stats.get(category);

            WithdrawalManager.tickCategory(player, stats, category, inCombat, companions, inSafeZone);

            float addictionRecovery = AddictionMath.computeAddictionRecoveryPerSecond(
                    AddictionConfigs.get(category),
                    stats.resilience,
                    false,
                    inSafeZone
            );

            drugStats.addictionValue = Math.max(0.0F, drugStats.addictionValue - addictionRecovery);
            RelapseManager.decay(drugStats);

            float sev = drugStats.withdrawalNorm();
            if (sev > 0.05F) {
                active++;
                sumSeverity += sev;
                maxSeverity = Math.max(maxSeverity, sev);
            }
        }

        float avg = active == 0 ? 0.0F : sumSeverity / active;
        float globalSeverity = AddictionMath.computeGlobalSeverity(maxSeverity, avg);

        StressManager.tick(player, stats, globalSeverity, inCombat, companions, inSafeZone);
        StressDamageManager.tick(player, stats);
        SymptomManager.applyServerSymptoms(player, globalSeverity);

        WithdrawalHintManager.tick(player, globalSeverity, inSafeZone, companions);

        if (inSafeZone && globalSeverity > 0.40F && gameTime % 200L == 0L) {
            ResilienceManager.onSafeZoneRecovery(stats);
        }

        if (player.tickCount % 20 == 0) {
            SymptomManager.sync(player, globalSeverity);
        }
    }

    public static float getGlobalSeverity(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        float max = 0.0F;
        float sum = 0.0F;
        int count = 0;

        for (DrugCategory category : DrugCategory.values()) {
            float sev = stats.get(category).withdrawalNorm();
            if (sev > 0.05F) {
                count++;
                sum += sev;
                max = Math.max(max, sev);
            }
        }

        return AddictionMath.computeGlobalSeverity(max, count == 0 ? 0.0F : sum / count);
    }

    public static DrugCategory getDominantCategory(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugCategory best = DrugCategory.CANNABINOID;
        float highest = -1.0F;

        for (DrugCategory category : DrugCategory.values()) {
            float sev = stats.get(category).withdrawalNorm();
            if (sev > highest) {
                highest = sev;
                best = category;
            }
        }

        return best;
    }
}