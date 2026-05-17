package org.mydrugs.mydrugs.diary;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.data.TemporaryRecoveryEffects;
import org.mydrugs.mydrugs.core.drug.dose.DoseState;
import org.mydrugs.mydrugs.addiction.manager.AddictionManager;
import org.mydrugs.mydrugs.recovery.SafeZoneManager;
import org.mydrugs.mydrugs.addiction.manager.state.BadTripManager;
import org.mydrugs.mydrugs.addiction.manager.state.SymptomManager;
import org.mydrugs.mydrugs.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.addiction.network.PersonalDiarySnapshotPayload;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.progression.PsyMixerMasteryAttachment;
import org.mydrugs.mydrugs.psyche.PlayerPsycheMapAttachment;
import org.mydrugs.mydrugs.psyche.PsycheMapNodeDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DiarySnapshotBuilder {
    private DiarySnapshotBuilder() {
    }

    public static PersonalDiarySnapshotPayload build(ServerPlayer player) {
        PlayerDiaryAttachment diary = player.getData(ModAttachments.PLAYER_DIARY.get());
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        PsyMixerMasteryAttachment mastery = player.getData(ModAttachments.PSY_MIXER_MASTERY.get());

        long gameTime = player.level().getGameTime();
        long currentDay = PlayerDiaryAttachment.currentDay(gameTime);
        int cooldown = diary.remainingCooldownTicks(gameTime);

        // Entries DTOs
        List<DiaryEntryDto> entries = new ArrayList<>(diary.getEntries().size());
        for (DiaryEntry e : diary.getEntries()) {
            entries.add(new DiaryEntryDto(
                    e.day(), e.createdGameTime(), e.type().name(),
                    e.content(), e.sourceKey(), e.dominantDrugId()
            ));
        }

        // Drug stats DTOs (only ones with any lifetime dose > 0 or non-empty addiction)
        List<DiaryDrugStatDto> drugStats = new ArrayList<>();
        for (DrugId id : stats.getTrackedDrugIds()) {
            DrugAddictionStats ds = stats.getDrugStats(id);
            if (ds == null) continue;
            if (ds.lifetimeDoseConsumed <= 0.0F
                    && ds.addictionValue <= 0.0F
                    && ds.peakHistoricalAddiction <= 0.0F) {
                continue;
            }
            drugStats.add(new DiaryDrugStatDto(
                    id.serializedName(),
                    ds.lifetimeDoseConsumed,
                    ds.addictionValue,
                    ds.baseWithdrawalMeter,
                    ds.tolerance,
                    ds.peakHistoricalAddiction,
                    ds.currentDose()
            ));
        }

        // Mastery DTOs
        List<DiaryMasteryStatDto> masteryStats = new ArrayList<>();
        Map<ResourceLocation, Integer> completedMap = mastery.getCompletedEntriesView();
        Map<ResourceLocation, Integer> failedMap = mastery.getFailedEntriesView();
        // union of keys
        java.util.Set<ResourceLocation> all = new java.util.LinkedHashSet<>(completedMap.keySet());
        all.addAll(failedMap.keySet());
        for (ResourceLocation rl : all) {
            int comp = completedMap.getOrDefault(rl, 0);
            int fail = failedMap.getOrDefault(rl, 0);
            masteryStats.add(new DiaryMasteryStatDto(
                    rl.toString(),
                    comp, fail,
                    mastery.getSpeedMultiplier(rl),
                    mastery.getInstabilityReduction(rl)
            ));
        }

        // Player state DTO
        DrugId dominantDrug = findDominantDrug(stats);
        DrugCategory dominantCategory = dominantDrug == null ? null : DrugRegistry.getCategory(dominantDrug);
        DrugAddictionStats dominantStats = dominantDrug == null ? null : stats.getDrugStats(dominantDrug);
        DoseState dose = dominantStats == null ? DoseState.NORMAL : dominantStats.lastDoseState;

        float globalSeverity = AddictionManager.getGlobalSeverity(player);
        int symptomFlags = SymptomManager.buildFlags(globalSeverity) | BadTripManager.symptomFlags(stats);
        int recoveryFlags = buildRecoveryFlags(player, stats);

        DiaryPlayerStateDto state = new DiaryPlayerStateDto(
                stats.stressLevel,
                globalSeverity,
                dominantDrug == null ? "" : dominantDrug.serializedName(),
                dominantCategory == null ? "" : dominantCategory.name(),
                dose == null ? "NORMAL" : dose.name(),
                stats.badTrip.active,
                stats.badTrip.severity,
                Math.max(0, stats.overdoseDeathTimer),
                symptomFlags,
                recoveryFlags,
                stats.sleepBlockedUntil > gameTime
        );

        PlayerPsycheMapAttachment psycheMap = player.getData(ModAttachments.PLAYER_PSYCHE_MAP.get());
        List<PsycheMapNodeDto> psycheNodes = new ArrayList<>();
        for (PlayerPsycheMapAttachment.Node n : psycheMap.getNodes()) {
            psycheNodes.add(new PsycheMapNodeDto(
                    n.nodeId, n.unlockedAtGameTime, n.unlockedDay, n.trigger, n.dominantDrugId
            ));
        }

        return new PersonalDiarySnapshotPayload(
                entries, drugStats, masteryStats, state, currentDay, cooldown, psycheNodes
        );
    }

    @Nullable
    public static ItemStack findDiaryStack(ServerPlayer player) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.getItem() == ModItems.PERSONAL_DIARY.get()) {
                return s;
            }
        }
        return null;
    }

    @Nullable
    private static DrugId findDominantDrug(PlayerAddictionStats stats) {
        DrugId best = null;
        float bestScore = -1.0F;
        for (DrugId id : stats.getTrackedDrugIds()) {
            DrugAddictionStats ds = stats.getDrugStats(id);
            if (ds == null) continue;
            float score = ds.currentDose() * 2.0F + ds.baseWithdrawalMeter * 1.2F + ds.addictionValue * 0.6F;
            if (score > bestScore) {
                bestScore = score;
                best = id;
            }
        }
        return best;
    }

    private static int buildRecoveryFlags(ServerPlayer player, PlayerAddictionStats stats) {
        TemporaryRecoveryEffects te = stats.temporaryEffects;
        long now = player.level().getGameTime();
        int flags = 0;
        if (te.hasDiaryCalm(now)) flags |= AddictionClientSnapshotPayload.RECOVERY_DIARY;
        if (te.hasHeadphones(now)) flags |= AddictionClientSnapshotPayload.RECOVERY_HEADPHONES;
        if (te.hasCalmingMixture(now)) flags |= AddictionClientSnapshotPayload.RECOVERY_CALMING_MIXTURE;
        if (te.hasSleepBonus(now)) flags |= AddictionClientSnapshotPayload.RECOVERY_SLEEP_BONUS;
        if (SafeZoneManager.isInSafeZone(player)) {
            flags |= AddictionClientSnapshotPayload.RECOVERY_SAFE_ZONE;
        }
        return flags;
    }
}
