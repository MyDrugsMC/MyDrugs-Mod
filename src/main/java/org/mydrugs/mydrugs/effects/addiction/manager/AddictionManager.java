package org.mydrugs.mydrugs.effects.addiction.manager;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.AddictionCategoryConfig;
import org.mydrugs.mydrugs.core.drug.AddictionConfigs;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.effects.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.manager.dose.DoseManager;
import org.mydrugs.mydrugs.effects.addiction.manager.progression.RelapseManager;
import org.mydrugs.mydrugs.effects.addiction.manager.progression.WithdrawalManager;
import org.mydrugs.mydrugs.effects.addiction.manager.recovery.SafeZoneManager;
import org.mydrugs.mydrugs.effects.addiction.manager.recovery.SocialReliefManager;
import org.mydrugs.mydrugs.effects.addiction.manager.state.ResilienceManager;
import org.mydrugs.mydrugs.effects.addiction.manager.state.StressDamageManager;
import org.mydrugs.mydrugs.effects.addiction.manager.state.StressManager;
import org.mydrugs.mydrugs.effects.addiction.manager.state.SymptomManager;
import org.mydrugs.mydrugs.effects.addiction.manager.state.ToleranceManager;
import org.mydrugs.mydrugs.effects.addiction.network.DoseSyncPayload;
import org.mydrugs.mydrugs.effects.addiction.util.AddictionMath;

public final class AddictionManager {
    private AddictionManager() {
    }

    public static void consume(ServerPlayer player, DrugCategory category, float dose) {
        consume(player, category, dose, null);
    }

    public static void consume(ServerPlayer player,
                               DrugCategory category,
                               float dose,
                               @Nullable ConsumptionStrategy strategy) {
        DrugId fallbackId = DrugRegistry.getRepresentativeDrugId(category);
        if (fallbackId == null) {
            return;
        }

        DrugModel fallbackModel = DrugRegistry.getDrug(fallbackId);
        if (fallbackModel == null) {
            return;
        }

        consume(player, fallbackModel, dose, strategy);
    }

    public static void consume(ServerPlayer player, DrugModel model, float baseDose) {
        consume(player, model, baseDose, null);
    }

    public static void consume(ServerPlayer player,
                               DrugModel model,
                               float baseDose,
                               @Nullable ConsumptionStrategy strategy) {
        PlayerAddictionStats playerStats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugAddictionStats drugStats = playerStats.getOrCreateDrugStats(model.getId());
        AddictionCategoryConfig cfg = AddictionConfigs.get(model.getDrugCategory());

        float baseGain = AddictionMath.computeAddictionGain(
                baseDose,
                cfg,
                playerStats.geneticFactor,
                drugStats.tolerance,
                model.getAddictionRate()
        );
        float finalGain = RelapseManager.applyRelapseMultiplier(baseGain, drugStats);

        drugStats.addictionValue = AddictionMath.clamp(drugStats.addictionValue + finalGain, 0.0F, 1000.0F);
        drugStats.lastUseTime = player.level().getGameTime();

        ToleranceManager.onUse(playerStats, model, baseDose);
        RelapseManager.onUse(model, drugStats);

        float relief = AddictionMath.computeEffectiveRelief(cfg.reliefStrength(), drugStats.tolerance);
        drugStats.baseWithdrawalMeter = Math.max(0.0F, drugStats.baseWithdrawalMeter - relief);
        drugStats.peakHistoricalAddiction = Math.max(drugStats.peakHistoricalAddiction, drugStats.addictionValue);

        playerStats.stressLevel = Math.max(0.0F, playerStats.stressLevel - AddictionConstants.STRESS_RELIEF_ON_CONSUME);

        DoseManager.onConsume(drugStats, model, strategy);
    }

    public static void tickPlayer(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        ItemEffectHandler.tickHeadphones(player);

        boolean inCombat = player.tickCount - player.getLastHurtByMobTimestamp() < AddictionConstants.COMBAT_DETECTION_TICKS;
        int companions = SocialReliefManager.countCompanions(player, AddictionConstants.COMPANION_DETECTION_RADIUS);
        boolean inSafeZone = SafeZoneManager.isInSafeZone(player);
        long gameTime = player.level().getGameTime();

        float maxSeverity = 0.0F;
        float sumSeverity = 0.0F;
        int active = 0;

        for (DrugId drugId : stats.getTrackedDrugIds()) {
            DrugAddictionStats drugStats = stats.getDrugStats(drugId);
            if (drugStats == null) {
                continue;
            }

            DrugCategory category = DrugRegistry.getCategory(drugId);

            WithdrawalManager.tickDrug(player, stats, drugId, inCombat, companions, inSafeZone);

            float addictionRecovery = AddictionMath.computeAddictionRecoveryPerSecond(
                    AddictionConfigs.get(category),
                    stats.resilience,
                    false,
                    inSafeZone
            );

            drugStats.addictionValue = Math.max(0.0F, drugStats.addictionValue - addictionRecovery);
            RelapseManager.decay(drugStats);

            DoseManager.tickDrug(player, stats, drugId);

            float sev = drugStats.withdrawalNorm();
            if (sev > 0.05F) {
                active++;
                sumSeverity += sev;
                maxSeverity = Math.max(maxSeverity, sev);
            }

            stats.removeDrugStatsIfEmpty(drugId);
        }

        float avg = active == 0 ? 0.0F : sumSeverity / active;
        float globalSeverity = AddictionMath.computeGlobalSeverity(maxSeverity, avg);

        StressManager.tick(player, stats, globalSeverity, inCombat, companions, inSafeZone);
        StressDamageManager.tick(player, stats);
        SymptomManager.applyServerSymptoms(player, globalSeverity);
        DoseManager.tickOverdoseTimer(player, stats);

        WithdrawalHintManager.tick(player, globalSeverity, inSafeZone, companions);

        if (inSafeZone
                && globalSeverity > AddictionConstants.SAFE_ZONE_RECOVERY_THRESHOLD
                && gameTime % AddictionConstants.SAFE_ZONE_RECOVERY_INTERVAL_TICKS == 0L) {
            ResilienceManager.onSafeZoneRecovery(stats);
        }

        if (player.tickCount % 20 == 0) {
            SymptomManager.sync(player, globalSeverity, inSafeZone);
            sendDoseSync(player, stats);
        }
    }

    private static void sendDoseSync(ServerPlayer player, PlayerAddictionStats stats) {
        DrugCategory[] categories = DrugCategory.values();
        float[] doses = new float[categories.length];
        for (int i = 0; i < categories.length; i++) {
            doses[i] = stats.getCategoryCurrentDose(categories[i]);
        }
        PacketDistributor.sendToPlayer(player, new DoseSyncPayload(doses));
    }

    public static float getGlobalSeverity(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        float max = 0.0F;
        float sum = 0.0F;
        int count = 0;

        for (DrugAddictionStats drugStats : stats.getAllDrugStats().values()) {
            float sev = drugStats.withdrawalNorm();
            if (sev > 0.05F) {
                count++;
                sum += sev;
                max = Math.max(max, sev);
            }
        }

        return AddictionMath.computeGlobalSeverity(max, count == 0 ? 0.0F : sum / count);
    }

    public static @Nullable DrugId getDominantDrugId(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        DrugId bestId = null;
        float bestSeverity = -1.0F;
        float bestAddiction = -1.0F;

        for (var entry : stats.getAllDrugStats().entrySet()) {
            DrugAddictionStats drugStats = entry.getValue();
            float severity = drugStats.withdrawalNorm();

            if (severity > bestSeverity || (severity == bestSeverity && drugStats.addictionValue > bestAddiction)) {
                bestSeverity = severity;
                bestAddiction = drugStats.addictionValue;
                bestId = entry.getKey();
            }
        }

        return bestId;
    }

    public static DrugCategory getDominantCategory(ServerPlayer player) {
        DrugId dominantDrugId = getDominantDrugId(player);
        return dominantDrugId != null ? DrugRegistry.getCategory(dominantDrugId) : DrugCategory.OTHER;
    }
}