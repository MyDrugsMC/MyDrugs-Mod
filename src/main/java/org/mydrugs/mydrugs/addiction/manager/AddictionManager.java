package org.mydrugs.mydrugs.addiction.manager;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;
import org.mydrugs.mydrugs.core.drug.AddictionCategoryConfig;
import org.mydrugs.mydrugs.core.drug.AddictionConfigs;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.use.ResolvedDrugUse;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.core.drug.dose.DoseManager;
import org.mydrugs.mydrugs.addiction.progression.RelapseManager;
import org.mydrugs.mydrugs.addiction.withdrawal.WithdrawalManager;
import org.mydrugs.mydrugs.recovery.SafeZoneManager;
import org.mydrugs.mydrugs.recovery.SocialReliefManager;
import org.mydrugs.mydrugs.addiction.manager.state.ResilienceManager;
import org.mydrugs.mydrugs.addiction.manager.state.BadTripManager;
import org.mydrugs.mydrugs.addiction.manager.state.StressDamageManager;
import org.mydrugs.mydrugs.addiction.manager.state.StressManager;
import org.mydrugs.mydrugs.addiction.manager.state.SymptomManager;
import org.mydrugs.mydrugs.addiction.tolerance.ToleranceManager;
import org.mydrugs.mydrugs.addiction.network.DoseSyncPayload;
import org.mydrugs.mydrugs.addiction.util.AddictionMath;
import org.mydrugs.mydrugs.mutation.MutationManager;
import org.mydrugs.mydrugs.mutation.MutationStat;

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
        float effectiveDose = strategy != null ? strategy.getNewDose(baseDose) : baseDose;
        consumeEffective(player, model, effectiveDose, strategy);
    }

    public static void consume(ResolvedDrugUse use) {
        consumeEffective(use.player(), use.model(), use.effectiveDose(), use.strategy());
    }

    private static void consumeEffective(ServerPlayer player,
                                         DrugModel model,
                                         float effectiveDose,
                                         @Nullable ConsumptionStrategy strategy) {
        if (!Config.SERVER.addictionEnabled.get()) {
            return;
        }

        PlayerAddictionStats playerStats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugAddictionStats drugStats = playerStats.getOrCreateDrugStats(model.getId());
        AddictionCategoryConfig cfg = AddictionConfigs.get(model.getDrugCategory());

        float baseGain = AddictionMath.computeAddictionGain(
                effectiveDose,
                cfg,
                playerStats.geneticFactor,
                drugStats.tolerance,
                model.getAddictionRate()
        ) * Config.SERVER.addictionGainMultiplier.get().floatValue();
        float finalGain = RelapseManager.applyRelapseMultiplier(baseGain, drugStats);

        float pleasureSensitivity = MutationManager.getValue(player, MutationStat.PLEASURE_SENSITIVITY);
        float addictionResistance = MutationManager.getValue(player, MutationStat.ADDICTION_RESISTANCE);
        finalGain *= (1.0F + pleasureSensitivity * 0.20F);
        finalGain *= Math.max(0.0F, 1.0F - addictionResistance);

        drugStats.addictionValue = AddictionMath.clamp(drugStats.addictionValue + finalGain, 0.0F, 1000.0F);
        drugStats.lastUseTime = player.level().getGameTime();
        drugStats.lifetimeDoseConsumed = Math.min(1_000_000F, drugStats.lifetimeDoseConsumed + Math.max(0.0F, effectiveDose));

        ToleranceManager.onUse(playerStats, model, effectiveDose);
        RelapseManager.onUse(model, drugStats);

        float relief = AddictionMath.computeEffectiveRelief(cfg.reliefStrength(), drugStats.tolerance);
        drugStats.baseWithdrawalMeter = Math.max(0.0F, drugStats.baseWithdrawalMeter - relief);
        drugStats.peakHistoricalAddiction = Math.max(drugStats.peakHistoricalAddiction, drugStats.addictionValue);

        StressManager.reduceStress(playerStats, AddictionConstants.STRESS_RELIEF_ON_CONSUME);

        DoseManager.onConsume(drugStats, model, effectiveDose, strategy);
    }

    public static void tickPlayer(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        ItemEffectHandler.tickHeadphones(player);

        boolean inCombat = player.tickCount - player.getLastHurtByMobTimestamp() < AddictionConstants.COMBAT_DETECTION_TICKS;
        int companions = SocialReliefManager.countCompanions(player, AddictionConstants.COMPANION_DETECTION_RADIUS);
        boolean inSafeZone = SafeZoneManager.isInSafeZone(player);
        if (inSafeZone && !stats.wasInSafeZoneLastTick) {
            AdvancementEventHooks.recoveryAction(player, "safe_zone");
        }
        stats.wasInSafeZoneLastTick = inSafeZone;
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

            // This loop runs every server tick; category recovery values are configured per second.
            float addictionRecovery = AddictionMath.computeAddictionRecoveryPerSecond(
                    AddictionConfigs.get(category),
                    stats.resilience,
                    false,
                    inSafeZone
            ) / 20.0F;

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

        if (stats.addictionSymptomsImmune) {
            BadTripManager.stop(player, stats);
            SymptomManager.applyServerSymptoms(player, 0.0F);
            if (player.tickCount % 20 == 0) {
                SymptomManager.sync(player, 0.0F, inSafeZone);
                sendDoseSync(player, stats);
            }
            return;
        }

        StressManager.tick(player, stats, globalSeverity, inCombat, companions, inSafeZone);
        BadTripManager.tick(player, stats);
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
        for (DrugCategory category : categories) {
            doses[category.networkId()] = stats.getCategoryCurrentDose(category);
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
