package org.mydrugs.mydrugs.core.drug.integration;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.attachment.PlayerIntegrationAttachment;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.diary.IntegrationDiary;

/**
 * Decides whether a curated drug may be integrated and performs the integration (Phase A).
 *
 * The actual ritual home for {@link #integrate} is the Resonator (Phase E); until then it is
 * exercised through a debug command.
 */
public final class IntegrationService {
    private IntegrationService() {
    }

    public record EligibilityResult(
            boolean eligible,
            boolean peakMet,
            boolean lowAddictionMet,
            boolean recoveryMet,
            boolean lifetimeDoseMet,
            boolean alreadyIntegrated
    ) {
    }

    public static EligibilityResult evaluate(PlayerAddictionStats stats, DrugId drugId) {
        if (stats == null || drugId == null) {
            return new EligibilityResult(false, false, false, false, false, false);
        }
        DrugAddictionStats d = stats.getDrugStats(drugId);
        if (d == null) {
            return new EligibilityResult(false, false, false, false, false, false);
        }
        boolean peakMet = d.peakHistoricalAddiction >= IntegrationConstants.PEAK_THRESHOLD;
        boolean lowAddictionMet = d.addictionValue <= IntegrationConstants.LOW_THRESHOLD;
        boolean recoveryMet = d.recoveryProgress >= 1.0F;
        boolean lifetimeDoseMet = d.lifetimeDoseConsumed >= IntegrationConstants.MIN_LIFETIME_DOSE;
        boolean alreadyIntegrated = d.isIntegrated();
        return new EligibilityResult(
                peakMet && lowAddictionMet && recoveryMet && lifetimeDoseMet && !alreadyIntegrated,
                peakMet,
                lowAddictionMet,
                recoveryMet,
                lifetimeDoseMet,
                alreadyIntegrated
        );
    }

    private static boolean isEligible(PlayerAddictionStats stats, DrugId drugId) {
        return evaluate(stats, drugId).eligible();
    }

    /** True when the drug satisfies eligibility and has not already been integrated. */
    public static boolean canIntegrate(ServerPlayer player, DrugId drugId) {
        if (player == null || drugId == null) {
            return false;
        }
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugAddictionStats d = stats.getDrugStats(drugId);
        if (d != null && d.isIntegrated()) {
            return false;
        }
        return isEligible(stats, drugId);
    }

    /** Promotes an eligible drug into the reckoning window (stage 1). No-op once integrated. */
    public static void markEligible(ServerPlayer player, DrugId drugId) {
        if (player == null || drugId == null) {
            return;
        }
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugAddictionStats d = stats.getDrugStats(drugId);
        if (d == null || d.isIntegrated() || d.integrationStage >= 1) {
            return;
        }
        if (isEligible(stats, drugId)) {
            d.integrationStage = 1;
            IntegrationDiary.firstEligible(player, drugId);
        }
    }

    /**
     * Completes integration: unlocks the {@link IntegratedTrait}, stamps the drug stats, and syncs
     * the client. Returns false for drugs outside the curated set.
     */
    public static boolean integrate(ServerPlayer player, DrugId drugId) {
        if (player == null || drugId == null) {
            return false;
        }
        IntegratedTrait trait = IntegratedTrait.bySource(drugId);
        if (trait == null) {
            return false;
        }

        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugAddictionStats d = stats.getOrCreateDrugStats(drugId);
        d.integrationStage = 2;
        d.integratedAtGameTime = player.level().getGameTime();

        PlayerIntegrationAttachment integration = player.getData(ModAttachments.PLAYER_INTEGRATION.get());
        integration.unlock(trait);
        IntegrationDiary.integrated(player, drugId);

        IntegratedTraitManager.syncToClient(player);
        player.displayClientMessage(
                Component.translatable("message.mydrugs.integration.integrated",
                                Component.translatable(trait.translationKey()))
                        .withStyle(ChatFormatting.AQUA),
                false
        );
        return true;
    }
}
