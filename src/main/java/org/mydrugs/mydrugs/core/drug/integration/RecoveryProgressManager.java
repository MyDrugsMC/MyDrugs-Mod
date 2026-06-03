package org.mydrugs.mydrugs.core.drug.integration;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.explain.AddictionRecoveryFeedback;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;
import org.mydrugs.mydrugs.progression.PsyKnowledgeManager;
import org.mydrugs.mydrugs.recovery.RecoveryRoomManager;
import org.mydrugs.mydrugs.recovery.RecoveryRoomReport;
import org.mydrugs.mydrugs.recovery.RecoverySessionManager;
import org.mydrugs.mydrugs.recovery.SanctuaryModule;

import java.util.Map;

/**
 * Active recovery (Phase B): productive labor — not idle time — burns addiction down and fills a
 * drug's {@code recoveryProgress} toward 1.0, the gate that makes integration eligible (Phase A).
 *
 * Idle time never advances {@code recoveryProgress}: it is only ever touched here, from a real
 * productive action. Recovery rooms multiply accrual but can never complete it on their own.
 */
public final class RecoveryProgressManager {
    private RecoveryProgressManager() {
    }

    /** A recovery action, each carrying base weight and whether passive support may finish recovery. */
    public enum ActionKind {
        ORE_MINED(1.0F, true, 1.0F, true),
        CROP_TENDED(0.7F, true, 1.0F, true),
        MACHINE_OUTPUT(1.4F, true, 1.0F, true),
        DISTILLERY_CYCLE(1.7F, true, 1.0F, true),
        PSY_MIXER_SUCCESS(2.0F, true, 1.0F, true),
        DIARY_WRITTEN(1.0F, true, 1.0F, false),
        THERAPY_SESSION(2.4F, true, 1.0F, false),
        SLEEP_REST(2.0F, true, 1.0F, false),
        FOOD(1.0F, true, 0.98F, false),
        HERBAL_TEA(1.35F, true, 1.0F, false),
        CALMING_MIXTURE(2.0F, true, 1.0F, false),
        SLEEPING_AID(1.7F, true, 1.0F, false),
        MUSIC_SUPPORT(1.0F, false, 0.95F, false),
        RECOVERY_ROOM_SUPPORT(1.0F, false, 0.95F, false),
        PASSIVE_COMPANION(1.0F, false, 0.90F, false),
        PET_CARE(2.0F, true, 1.0F, false),
        RECOVERY_RESONANCE(1.0F, false, IntegrationConstants.RECOVERY_RESONANCE_PROGRESS_CAP, false);

        private final float baseWeight;
        private final boolean canCompleteRecovery;
        private final float progressCap;
        private final boolean nextStageWork;

        ActionKind(float baseWeight, boolean canCompleteRecovery, float progressCap, boolean nextStageWork) {
            this.baseWeight = baseWeight;
            this.canCompleteRecovery = canCompleteRecovery;
            this.progressCap = progressCap;
            this.nextStageWork = nextStageWork;
        }

        public float baseWeight() {
            return baseWeight;
        }

        public boolean canCompleteRecovery() {
            return canCompleteRecovery;
        }

        public float progressCap() {
            return progressCap;
        }

        public boolean nextStageWork() {
            return nextStageWork;
        }
    }

    /**
     * The single funnel for productive actions. Accrues recovery and active detox for every drug
     * currently in its reckoning window.
     */
    public static void onProductiveAction(ServerPlayer player, ActionKind kind, float weight) {
        if (player == null || kind == null || weight <= 0.0F) {
            return;
        }
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        RecoveryRoomReport room = RecoveryRoomManager.getBestRoom(player).orElse(null);
        float roomMultiplier = RecoveryRoomManager.addictionRecoveryMultiplier(room);
        boolean recoveryMomentumApplied = consumeRecoveryMomentum(player, stats, kind);
        float momentumMultiplier = recoveryMomentumApplied ? 1.12F : 1.0F;

        if (stats.perDrug.isEmpty()) {
            if (shouldSendReliefFallback(kind)) {
                AddictionRecoveryFeedback.sendForAction(player, kind);
            }
            RecoverySessionManager.onProductiveAction(player, kind);
            return;
        }

        boolean applied = false;
        float totalAddictionReduced = 0.0F;
        float maxRecoveryProgressAdded = 0.0F;
        float bestRecoveryProgressPercent = 0.0F;

        for (Map.Entry<DrugId, DrugAddictionStats> entry : stats.perDrug.entrySet()) {
            DrugId drug = entry.getKey();
            DrugAddictionStats d = entry.getValue();
            if (!isInReckoning(drug, d)) {
                continue;
            }

            float effectiveWeight = effectiveWeight(kind, weight, roomMultiplier, worksTowardNextDrug(player, drug))
                    * momentumMultiplier;
            RecoveryDelta delta = applyRecoveryAction(drug, d, kind, effectiveWeight);
            if (delta.addictionReduced() > 0.0F || delta.recoveryProgressAdded() > 0.0F) {
                applied = true;
                totalAddictionReduced += delta.addictionReduced();
                maxRecoveryProgressAdded = Math.max(maxRecoveryProgressAdded, delta.recoveryProgressAdded());
                bestRecoveryProgressPercent = Math.max(bestRecoveryProgressPercent, recoveryProgressPercent(drug, d));
            }

            IntegrationService.markEligible(player, drug);
        }

        if (maxRecoveryProgressAdded > 0.0F && showsActionFeedback(kind)) {
            AddictionRecoveryFeedback.sendProgressDetail(player, kind, Math.round(bestRecoveryProgressPercent));
        } else if ((applied && totalAddictionReduced > 0.0F && showsActionFeedback(kind))
                || shouldSendReliefFallback(kind)) {
            AddictionRecoveryFeedback.sendForAction(player, kind);
        }

        if (isNearPassiveCap(player, stats, kind)) {
            AddictionRecoveryFeedback.sendPassiveCap(player);
        }

        RecoverySessionManager.onProductiveAction(player, kind);
    }

    /**
     * A drug can receive active recovery once it has truly been an addiction, has come down from
     * its peak, and still needs either recovery progress or addiction reduction for eligibility.
     */
    public static boolean isInReckoning(DrugAddictionStats d) {
        return isInReckoning(null, d);
    }

    public static boolean isInReckoning(@Nullable DrugId drugId, DrugAddictionStats d) {
        IntegrationRequirementProfile profile = drugId == null ? null : IntegrationRequirements.profile(drugId);
        float requiredPeak = profile == null ? IntegrationConstants.PEAK_THRESHOLD_FALLBACK : profile.requiredPeakExposure();
        float requiredRecovery = profile == null ? 1.0F : profile.requiredRecoveryProgress();
        float maxCurrentAddiction = profile == null ? 0.0F : profile.maxCurrentAddiction();
        if (profile != null && !profile.requiresRecoveryProgress()) {
            return false;
        }
        return d != null
                && d.peakHistoricalAddiction >= requiredPeak
                && d.integrationStage < DrugAddictionStats.INTEGRATION_STAGE_INTEGRATED
                && (d.recoveryProgress < requiredRecovery || d.addictionValue > maxCurrentAddiction)
                && d.addictionValue < d.peakHistoricalAddiction;
    }

    public static float effectiveWeight(ActionKind kind, float weight, float roomMultiplier, boolean nextDrugWork) {
        if (kind == null || weight <= 0.0F || roomMultiplier <= 0.0F) {
            return 0.0F;
        }
        float effectiveWeight = weight * kind.baseWeight() * roomMultiplier;
        return nextDrugWork && kind.nextStageWork()
                ? effectiveWeight * IntegrationConstants.NEXT_DRUG_WORK_BONUS
                : effectiveWeight;
    }

    public static RecoveryDelta applyRecoveryAction(DrugAddictionStats d, float effectiveWeight) {
        return applyRecoveryAction(null, d, ActionKind.ORE_MINED, effectiveWeight);
    }

    public static RecoveryDelta applyRecoveryAction(@Nullable DrugId drugId, DrugAddictionStats d, ActionKind kind, float effectiveWeight) {
        if (!isInReckoning(drugId, d) || kind == null || effectiveWeight <= 0.0F) {
            return new RecoveryDelta(0.0F, 0.0F);
        }
        IntegrationRequirementProfile profile = drugId == null ? null : IntegrationRequirements.profile(drugId);

        float oldAddiction = d.addictionValue;
        float oldRecovery = d.recoveryProgress;
        applyActiveDetox(d, effectiveWeight * IntegrationConstants.DETOX_PER_ACTION,
                detoxFloor(profile));
        float cap = kind.canCompleteRecovery() ? 1.0F : kind.progressCap();
        if (d.recoveryProgress < cap) {
            d.recoveryProgress = Math.min(cap,
                    d.recoveryProgress + effectiveWeight * IntegrationConstants.RECOVERY_PROGRESS_PER_ACTION);
        }
        return new RecoveryDelta(oldAddiction - d.addictionValue, d.recoveryProgress - oldRecovery);
    }

    public static void tickPassiveSupport(ServerPlayer player, PlayerAddictionStats stats,
                                          @Nullable RecoveryRoomReport room, int companions) {
        if (player == null || stats == null || stats.perDrug.isEmpty()) {
            return;
        }
        long now = player.level().getGameTime();
        if (stats.temporaryEffects.hasHeadphones(now)) {
            onProductiveAction(player, ActionKind.MUSIC_SUPPORT, 0.35F);
        }
        if (RecoveryRoomManager.isValidRecoveryRoom(room)) {
            onProductiveAction(player, ActionKind.RECOVERY_ROOM_SUPPORT, roomSupportWeight(player, room));
            if (room.hasModule(SanctuaryModule.MUSIC_CORNER) && room.hasActiveMusic()) {
                onProductiveAction(player, ActionKind.MUSIC_SUPPORT, 0.20F);
            }
        }
        int cappedCompanions = Math.min(3, Math.max(0, companions));
        if (cappedCompanions > 0) {
            onProductiveAction(player, ActionKind.PASSIVE_COMPANION, cappedCompanions * 0.12F);
        }
    }

    public static void applyRecoveryResonance(ServerPlayer player, RecoveryRoomReport room) {
        if (player == null || !RecoveryRoomManager.isValidRecoveryRoom(room)) {
            return;
        }
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        float tierMultiplier = recoveryResonanceMultiplier(room);
        for (Map.Entry<DrugId, DrugAddictionStats> entry : stats.perDrug.entrySet()) {
            DrugId drug = entry.getKey();
            DrugAddictionStats d = entry.getValue();
            if (!isInReckoning(drug, d)) {
                continue;
            }
            IntegrationRequirementProfile profile = IntegrationRequirements.profile(drug);
            applyActiveDetox(d, IntegrationConstants.RECOVERY_RESONANCE_DETOX_BASE * tierMultiplier,
                    detoxFloor(profile));
            if (d.recoveryProgress < IntegrationConstants.RECOVERY_RESONANCE_PROGRESS_CAP) {
                d.recoveryProgress = Math.min(
                        IntegrationConstants.RECOVERY_RESONANCE_PROGRESS_CAP,
                        d.recoveryProgress + IntegrationConstants.RECOVERY_RESONANCE_PROGRESS_BASE * tierMultiplier
                );
            }
            IntegrationService.markEligible(player, drug);
        }
    }

    private static float detoxFloor(@Nullable IntegrationRequirementProfile profile) {
        return profile == null || !profile.currentAddictionRelevant()
                ? 0.0F
                : Math.max(0.0F, profile.maxCurrentAddiction());
    }

    private static void applyActiveDetox(DrugAddictionStats stats, float amount, float floor) {
        if (stats == null || amount <= 0.0F || stats.addictionValue <= floor) {
            return;
        }
        stats.addictionValue = Math.max(floor, stats.addictionValue - amount);
    }

    private static float roomSupportWeight(ServerPlayer player, RecoveryRoomReport room) {
        if (room == null) {
            return 0.0F;
        }
        float weight = switch (room.tier()) {
            case NONE -> 0.0F;
            case FRAGILE_ROOM -> 0.12F;
            case RESTING_ROOM -> 0.22F;
            case SAFE_ROOM -> 0.38F;
            case SANCTUARY -> 0.60F;
        };
        if (room.hasModule(SanctuaryModule.PLANT_BREATHING_CORNER)) {
            weight += 0.04F;
        }
        if (room.hasModule(SanctuaryModule.TEA_KITCHEN)) {
            weight += 0.03F;
        }
        if (room.hasModule(SanctuaryModule.MEMORY_WALL) && hasEarnedMemory(player)) {
            weight += 0.05F;
        }
        if (room.hasModule(SanctuaryModule.INTEGRATION_ALCOVE)) {
            weight += 0.05F;
        }
        return Math.min(0.75F, weight);
    }

    private static boolean consumeRecoveryMomentum(ServerPlayer player, PlayerAddictionStats stats, ActionKind kind) {
        if (!qualifiesForRecoveryMomentum(kind)) {
            return false;
        }
        long now = player.level().getGameTime();
        if (!stats.temporaryEffects.hasRecoveryMomentum(now)) {
            return false;
        }
        stats.temporaryEffects.recoveryMomentumCharges = Math.max(0, stats.temporaryEffects.recoveryMomentumCharges - 1);
        if (stats.temporaryEffects.recoveryMomentumCharges <= 0) {
            stats.temporaryEffects.recoveryMomentumUntil = 0L;
        }
        AddictionRecoveryFeedback.sendRecoveryMomentumUsed(player);
        return true;
    }

    private static boolean qualifiesForRecoveryMomentum(ActionKind kind) {
        return kind != null && (kind.nextStageWork() || kind == ActionKind.PET_CARE);
    }

    private static boolean showsActionFeedback(ActionKind kind) {
        return kind != ActionKind.MUSIC_SUPPORT
                && kind != ActionKind.RECOVERY_ROOM_SUPPORT
                && kind != ActionKind.PASSIVE_COMPANION
                && kind != ActionKind.RECOVERY_RESONANCE;
    }

    private static boolean shouldSendReliefFallback(ActionKind kind) {
        return switch (kind) {
            case FOOD, SLEEP_REST, DIARY_WRITTEN, THERAPY_SESSION, HERBAL_TEA,
                    CALMING_MIXTURE, SLEEPING_AID, PET_CARE -> true;
            case ORE_MINED, CROP_TENDED, MACHINE_OUTPUT, DISTILLERY_CYCLE, PSY_MIXER_SUCCESS,
                    MUSIC_SUPPORT, RECOVERY_ROOM_SUPPORT, PASSIVE_COMPANION, RECOVERY_RESONANCE -> false;
        };
    }

    private static boolean isNearPassiveCap(ServerPlayer player, PlayerAddictionStats stats, ActionKind kind) {
        if (kind != ActionKind.MUSIC_SUPPORT
                && kind != ActionKind.RECOVERY_ROOM_SUPPORT
                && kind != ActionKind.PASSIVE_COMPANION) {
            return false;
        }

        long now = player.level().getGameTime();
        float cap = kind.progressCap();
        for (Map.Entry<DrugId, DrugAddictionStats> entry : stats.perDrug.entrySet()) {
            DrugId drug = entry.getKey();
            DrugAddictionStats drugStats = entry.getValue();
            IntegrationRequirementProfile profile = IntegrationRequirements.profile(drug);
            if (profile == null || !profile.requiresRecoveryProgress() || drugStats.isIntegrated()) {
                continue;
            }
            float required = Math.max(0.01F, profile.requiredRecoveryProgress());
            float nearCap = Math.min(required, cap) - 0.02F;
            if (drugStats.recoveryProgress >= nearCap
                    && drugStats.recoveryProgress < required
                    && !IntegrationService.evaluate(stats, drug, now).eligible()) {
                return true;
            }
        }
        return false;
    }

    private static float recoveryProgressPercent(DrugId drug, DrugAddictionStats stats) {
        IntegrationRequirementProfile profile = IntegrationRequirements.profile(drug);
        float required = profile == null ? 1.0F : profile.requiredRecoveryProgress();
        if (required <= 0.0F || profile != null && !profile.requiresRecoveryProgress()) {
            return 100.0F;
        }
        return Math.max(0.0F, Math.min(100.0F, stats.recoveryProgress / required * 100.0F));
    }

    private static boolean hasEarnedMemory(ServerPlayer player) {
        return !player.getData(ModAttachments.PLAYER_PSYCHE_MAP.get()).getNodes().isEmpty()
                || !player.getData(ModAttachments.PLAYER_INTEGRATION.get()).isEmpty();
    }

    public static float recoveryResonanceMultiplier(@Nullable RecoveryRoomReport room) {
        if (!RecoveryRoomManager.isValidRecoveryRoom(room)) {
            return 0.0F;
        }
        float multiplier = switch (room.tier()) {
            case NONE -> 0.0F;
            case FRAGILE_ROOM -> 1.0F;
            case RESTING_ROOM -> 1.2F;
            case SAFE_ROOM -> 1.5F;
            case SANCTUARY -> 2.0F;
        };
        if (room.hasModule(SanctuaryModule.INTEGRATION_ALCOVE)) {
            multiplier += 0.20F;
        }
        return Math.min(2.25F, multiplier);
    }

    public record RecoveryDelta(float addictionReduced, float recoveryProgressAdded) {
    }

    /** True when the player has reached the production stage of the drug after {@code recovering}. */
    private static boolean worksTowardNextDrug(ServerPlayer player, DrugId recovering) {
        DrugId next = CuratedDrugChain.next(recovering);
        if (next == null) {
            return false;
        }
        PsyKnowledgeKey key = CuratedDrugChain.stageKnowledge(next);
        return key != null && PsyKnowledgeManager.has(player, key);
    }

}
