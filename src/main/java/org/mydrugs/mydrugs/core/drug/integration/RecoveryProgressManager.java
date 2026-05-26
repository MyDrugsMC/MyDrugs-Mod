package org.mydrugs.mydrugs.core.drug.integration;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;
import org.mydrugs.mydrugs.progression.PsyKnowledgeManager;
import org.mydrugs.mydrugs.recovery.RecoveryRoomManager;
import org.mydrugs.mydrugs.recovery.RecoveryRoomReport;

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

    /** A kind of productive labor, each carrying a base accrual weight. */
    public enum ActionKind {
        ORE_MINED(1.0F),
        CROP_TENDED(0.7F);

        private final float baseWeight;

        ActionKind(float baseWeight) {
            this.baseWeight = baseWeight;
        }

        public float baseWeight() {
            return baseWeight;
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
        if (stats.perDrug.isEmpty()) {
            return;
        }

        RecoveryRoomReport room = RecoveryRoomManager.getBestRoom(player).orElse(null);
        float roomMultiplier = RecoveryRoomManager.addictionRecoveryMultiplier(room);

        for (Map.Entry<DrugId, DrugAddictionStats> entry : stats.perDrug.entrySet()) {
            DrugId drug = entry.getKey();
            DrugAddictionStats d = entry.getValue();
            if (!isInReckoning(d)) {
                continue;
            }

            float effectiveWeight = effectiveWeight(kind, weight, roomMultiplier, worksTowardNextDrug(player, drug));
            applyRecoveryAction(d, effectiveWeight);

            int stageBefore = d.integrationStage;
            if (d.integrationStage < 1) {
                IntegrationService.markEligible(player, drug);
            }
            if (stageBefore == 0 && d.integrationStage == 1) {
                notifyEligible(player, drug);
            }
        }
    }

    /**
     * A drug is "in reckoning" once it has truly been an addiction, has come down from its peak,
     * and has not yet completed recovery or been integrated.
     */
    public static boolean isInReckoning(DrugAddictionStats d) {
        return d != null
                && d.peakHistoricalAddiction >= IntegrationConstants.PEAK_THRESHOLD
                && d.integrationStage < 2
                && d.recoveryProgress < 1.0F
                && d.addictionValue < d.peakHistoricalAddiction;
    }

    public static float effectiveWeight(ActionKind kind, float weight, float roomMultiplier, boolean nextDrugWork) {
        if (kind == null || weight <= 0.0F || roomMultiplier <= 0.0F) {
            return 0.0F;
        }
        float effectiveWeight = weight * kind.baseWeight() * roomMultiplier;
        return nextDrugWork ? effectiveWeight * IntegrationConstants.NEXT_DRUG_WORK_BONUS : effectiveWeight;
    }

    public static RecoveryDelta applyRecoveryAction(DrugAddictionStats d, float effectiveWeight) {
        if (!isInReckoning(d) || effectiveWeight <= 0.0F) {
            return new RecoveryDelta(0.0F, 0.0F);
        }

        float oldAddiction = d.addictionValue;
        float oldRecovery = d.recoveryProgress;
        d.addictionValue = Math.max(0.0F,
                d.addictionValue - effectiveWeight * IntegrationConstants.DETOX_PER_ACTION);
        d.recoveryProgress = Math.min(1.0F,
                d.recoveryProgress + effectiveWeight * IntegrationConstants.RECOVERY_PROGRESS_PER_ACTION);
        return new RecoveryDelta(oldAddiction - d.addictionValue, d.recoveryProgress - oldRecovery);
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

    private static void notifyEligible(ServerPlayer player, DrugId drug) {
        player.displayClientMessage(
                Component.translatable("message.mydrugs.integration.eligible",
                                Component.translatable("drug.mydrugs." + drug.serializedName()))
                        .withStyle(ChatFormatting.AQUA),
                true
        );
    }
}
