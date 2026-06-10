package org.mydrugs.mydrugs.core.drug.integration;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.attachment.PlayerIntegrationAttachment;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.diary.IntegrationDiary;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.psyche.PsycheMapManager;

/**
 * Decides whether a curated drug may be integrated and performs trait unlocks.
 *
 * <p>Normal gameplay must use {@link #tryIntegrate(ServerPlayer, DrugId)} so eligibility is
 * checked at the integration boundary. {@link #forceIntegrateUnsafe(ServerPlayer, DrugId)}
 * exists only for admin/debug recovery.
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
            boolean cleanDoseStreakMet,
            boolean psychedelicReflectionMet,
            boolean safePsychedelicUseMet,
            boolean noRecentBadTripMet,
            boolean alreadyIntegrated
    ) {
        public EligibilityResult(
                boolean eligible,
                boolean peakMet,
                boolean lowAddictionMet,
                boolean recoveryMet,
                boolean lifetimeDoseMet,
                boolean cleanDoseStreakMet,
                boolean alreadyIntegrated
        ) {
            this(eligible, peakMet, lowAddictionMet, recoveryMet, lifetimeDoseMet, cleanDoseStreakMet,
                    true, true, true, alreadyIntegrated);
        }
    }

    public record IntegrationAttemptResult(
            boolean success,
            String failureKey,
            EligibilityResult eligibility
    ) {
        public static IntegrationAttemptResult passed() {
            return new IntegrationAttemptResult(true, "", null);
        }

        public static IntegrationAttemptResult fail(String failureKey, EligibilityResult eligibility) {
            return new IntegrationAttemptResult(false, failureKey == null ? "" : failureKey, eligibility);
        }
    }

    public static EligibilityResult evaluate(PlayerAddictionStats stats, DrugId drugId) {
        return evaluate(stats, drugId, Long.MAX_VALUE);
    }

    public static EligibilityResult evaluate(PlayerAddictionStats stats, DrugId drugId, long currentGameTime) {
        if (stats == null || drugId == null) {
            return new EligibilityResult(false, false, false, false, false, false, false, false, false, false);
        }
        IntegrationRequirementProfile profile = IntegrationRequirements.profile(drugId);
        if (profile == null) {
            return new EligibilityResult(false, false, false, false, false, false, false, false, false, false);
        }
        DrugAddictionStats d = stats.getDrugStats(drugId);
        if (d == null) {
            return new EligibilityResult(false, false, false, false, false, false, false, false, false, false);
        }
        boolean peakMet = profile.usesCleanDoseStreak()
                || d.peakHistoricalAddiction >= profile.requiredPeakExposure();
        boolean lowAddictionMet = !profile.currentAddictionRelevant()
                || d.addictionValue <= profile.maxCurrentAddiction();
        boolean recoveryMet = !profile.requiresRecoveryProgress()
                || d.recoveryProgress >= profile.requiredRecoveryProgress();
        boolean lifetimeDoseMet = d.lifetimeDoseConsumed >= profile.requiredLifetimeDose();
        boolean cleanDoseStreakMet = !profile.usesCleanDoseStreak()
                || d.cleanIntegrationDoseStreak >= profile.requiredCleanDoseStreak();
        boolean psychedelicReflectionMet = !profile.requiresPsychedelicReflection()
                || d.cleanPsychedelicReflectionCount >= profile.requiredPsychedelicReflections();
        boolean safePsychedelicUseMet = !profile.requiresSafePsychedelicUse()
                || d.cleanPsychedelicSafeUseCount >= profile.requiredSafePsychedelicUses();
        boolean noRecentBadTripMet = recentBadTripRemainingTicks(d, profile, currentGameTime) <= 0L;
        boolean alreadyIntegrated = d.isIntegrated();
        return new EligibilityResult(
                peakMet
                        && lowAddictionMet
                        && recoveryMet
                        && lifetimeDoseMet
                        && cleanDoseStreakMet
                        && psychedelicReflectionMet
                        && safePsychedelicUseMet
                        && noRecentBadTripMet
                        && !alreadyIntegrated,
                peakMet,
                lowAddictionMet,
                recoveryMet,
                lifetimeDoseMet,
                cleanDoseStreakMet,
                psychedelicReflectionMet,
                safePsychedelicUseMet,
                noRecentBadTripMet,
                alreadyIntegrated
        );
    }

    public static IntegrationRequirementProfile profile(DrugId drugId) {
        return IntegrationRequirements.profile(drugId);
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
        return evaluate(stats, drugId, player.level().getGameTime()).eligible();
    }

    public static void afterDrugStatsUpdated(ServerPlayer player, DrugId drugId) {
        markEligible(player, drugId);
    }

    /**
     * Updates the clean-streak counter for a psychedelic dose, applying the spacing rule:
     * if the previous use happened less than {@code minSpacingTicks} ago, the streak resets to 0;
     * otherwise it increments (capped at 999). Pure function on the stats record so unit-testable.
     *
     * <p>Bad-trip resets are handled separately in {@code BadTripManager}.
     */
    public static void onCleanStreakUse(DrugAddictionStats stats,
                                        long currentGameTime,
                                        long prevLastUseTime,
                                        long minSpacingTicks) {
        if (stats == null) {
            return;
        }
        boolean rushed = prevLastUseTime > 0L
                && currentGameTime - prevLastUseTime < minSpacingTicks;
        if (rushed) {
            stats.cleanIntegrationDoseStreak = 0;
            stats.cleanPsychedelicReflectionCount = 0;
            stats.cleanPsychedelicSafeUseCount = 0;
            stats.lastPsychedelicReflectionUseTime = 0L;
        } else {
            stats.cleanIntegrationDoseStreak = Math.min(999, stats.cleanIntegrationDoseStreak + 1);
        }
    }

    public static void onCleanPsychedelicUseContext(DrugAddictionStats stats, boolean safeSetting) {
        if (stats == null || stats.cleanIntegrationDoseStreak <= 0 || !safeSetting) {
            return;
        }
        stats.cleanPsychedelicSafeUseCount = Math.min(999, stats.cleanPsychedelicSafeUseCount + 1);
    }

    public static void onReflectionAction(ServerPlayer player) {
        if (player == null) {
            return;
        }
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        long now = player.level().getGameTime();
        for (DrugId drugId : stats.getTrackedDrugIds()) {
            IntegrationRequirementProfile profile = IntegrationRequirements.profile(drugId);
            if (profile == null || !profile.usesCleanDoseStreak()) {
                continue;
            }
            DrugAddictionStats drugStats = stats.getDrugStats(drugId);
            if (!canRecordPsychedelicReflection(drugStats, now)) {
                continue;
            }
            drugStats.cleanPsychedelicReflectionCount = Math.min(999, drugStats.cleanPsychedelicReflectionCount + 1);
            drugStats.lastPsychedelicReflectionUseTime = drugStats.lastUseTime;
            markEligible(player, drugId);
        }
    }

    public static void recordPsychedelicBadTrip(ServerPlayer player, DrugId drugId) {
        if (player == null || !IntegrationRequirements.usesCleanDoseStreak(drugId)) {
            return;
        }
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugAddictionStats drugStats = stats.getDrugStats(drugId);
        if (drugStats == null) {
            return;
        }
        drugStats.cleanIntegrationDoseStreak = 0;
        drugStats.cleanPsychedelicReflectionCount = 0;
        drugStats.cleanPsychedelicSafeUseCount = 0;
        drugStats.lastPsychedelicReflectionUseTime = 0L;
        drugStats.lastBadTripGameTime = player.level().getGameTime();
    }

    public static long cleanDoseSpacingRemainingTicks(DrugAddictionStats stats, long currentGameTime) {
        if (stats == null || stats.lastUseTime <= 0L) {
            return 0L;
        }
        long readyAt = stats.lastUseTime + IntegrationConstants.MIN_CLEAN_STREAK_SPACING_TICKS;
        return Math.max(0L, readyAt - currentGameTime);
    }

    public static long recentBadTripRemainingTicks(DrugAddictionStats stats,
                                                   IntegrationRequirementProfile profile,
                                                   long currentGameTime) {
        if (stats == null || profile == null || !profile.blocksRecentBadTrips() || stats.lastBadTripGameTime <= 0L) {
            return 0L;
        }
        long clearAt = stats.lastBadTripGameTime + profile.recentBadTripBlockTicks();
        return Math.max(0L, clearAt - currentGameTime);
    }

    /** Promotes a fully eligible drug to the named ELIGIBLE stage. No-op once integrated. */
    public static boolean markEligible(ServerPlayer player, DrugId drugId) {
        if (player == null || drugId == null) {
            return false;
        }
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugAddictionStats d = stats.getDrugStats(drugId);
        if (d == null || d.isIntegrated()) {
            return false;
        }
        EligibilityResult eligibility = evaluate(stats, drugId, player.level().getGameTime());
        if (!eligibility.eligible()) {
            return false;
        }

        boolean transitioned = false;
        if (d.integrationStage < DrugAddictionStats.INTEGRATION_STAGE_ELIGIBLE) {
            d.integrationStage = DrugAddictionStats.INTEGRATION_STAGE_ELIGIBLE;
            IntegrationDiary.firstEligible(player, drugId);
            notifyEligible(player, drugId);
            transitioned = true;
        }
        if (drugId == DrugId.COFFEE) {
            awardFirstIntegrationCore(player);
        }
        return transitioned;
    }

    /**
     * Safe gameplay path. Validates the player's stored eligibility before unlocking a trait.
     */
    public static IntegrationAttemptResult tryIntegrate(ServerPlayer player, DrugId drugId) {
        if (player == null || drugId == null) {
            return IntegrationAttemptResult.fail("message.mydrugs.resonator.no_eligible_drug", null);
        }
        IntegratedTrait trait = IntegratedTrait.bySource(drugId);
        if (trait == null) {
            return IntegrationAttemptResult.fail("message.mydrugs.resonator.no_eligible_drug", null);
        }

        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        EligibilityResult eligibility = evaluate(stats, drugId, player.level().getGameTime());
        if (!eligibility.eligible()) {
            return IntegrationAttemptResult.fail("message.mydrugs.integration.requirements_unmet", eligibility);
        }
        if (!forceIntegrateUnsafe(player, drugId)) {
            return IntegrationAttemptResult.fail("message.mydrugs.resonator.integration_lost", eligibility);
        }
        PsycheMapManager.unlockIntegration(player, drugId);
        return IntegrationAttemptResult.passed();
    }

    /**
     * Backward-compatible safe boolean wrapper. New gameplay code should prefer
     * {@link #tryIntegrate(ServerPlayer, DrugId)} so it can surface the failure reason.
     */
    public static boolean integrate(ServerPlayer player, DrugId drugId) {
        return tryIntegrate(player, drugId).success();
    }

    /**
     * Explicit bypass for admin/debug recovery only. Normal gameplay must use {@link #tryIntegrate}.
     */
    public static boolean forceIntegrateUnsafe(ServerPlayer player, DrugId drugId) {
        if (player == null || drugId == null) {
            return false;
        }
        IntegratedTrait trait = IntegratedTrait.bySource(drugId);
        if (trait == null) {
            return false;
        }

        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugAddictionStats d = stats.getOrCreateDrugStats(drugId);
        d.integrationStage = DrugAddictionStats.INTEGRATION_STAGE_INTEGRATED;
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

    private static boolean canRecordPsychedelicReflection(DrugAddictionStats stats, long currentGameTime) {
        if (stats == null || stats.isIntegrated() || stats.cleanIntegrationDoseStreak <= 0 || stats.lastUseTime <= 0L) {
            return false;
        }
        if (currentGameTime < stats.lastUseTime
                || currentGameTime - stats.lastUseTime > IntegrationConstants.PSYCHEDELIC_REFLECTION_WINDOW_TICKS) {
            return false;
        }
        if (stats.lastBadTripGameTime >= stats.lastUseTime) {
            return false;
        }
        return stats.lastPsychedelicReflectionUseTime != stats.lastUseTime;
    }

    private static void awardFirstIntegrationCore(ServerPlayer player) {
        PlayerIntegrationAttachment integration = player.getData(ModAttachments.PLAYER_INTEGRATION.get());
        if (integration.hasReceivedFirstIntegrationCore()) {
            return;
        }

        /*
         * The shaped recipe is vanilla JSON and cannot enforce this per-player attachment without
         * replacing it with a custom recipe type. The Psy Mixer recovery route is gated, and this
         * reward remains the first guided core moment for normal progression.
         */
        integration.markFirstIntegrationCoreAwarded();
        ItemStack core = new ItemStack(ModItems.INTEGRATION_CORE.get());
        if (!player.getInventory().add(core)) {
            player.drop(core, false);
        }
        player.displayClientMessage(
                Component.translatable("message.mydrugs.integration_core.awarded").withStyle(ChatFormatting.AQUA),
                false
        );
        IntegrationDiary.firstIntegrationCore(player);
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
