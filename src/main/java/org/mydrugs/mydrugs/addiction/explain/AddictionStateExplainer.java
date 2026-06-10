package org.mydrugs.mydrugs.addiction.explain;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.data.TemporaryRecoveryEffects;
import org.mydrugs.mydrugs.addiction.data.WithdrawalPhase;
import org.mydrugs.mydrugs.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.addiction.util.AddictionMath;
import org.mydrugs.mydrugs.core.drug.AddictionConfigs;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.dose.DoseManager;
import org.mydrugs.mydrugs.core.drug.dose.DosePath;
import org.mydrugs.mydrugs.core.drug.dose.DoseState;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.recovery.PlayerEnvironmentSnapshot;
import org.mydrugs.mydrugs.recovery.PlayerRecoveryEnvironmentCache;

public final class AddictionStateExplainer {
    private static final float WITHDRAWAL_ACTIVE_THRESHOLD = 0.10F;
    private static final float WITHDRAWAL_PEAK_THRESHOLD = 0.65F;
    private static final float STRESS_HIGH_THRESHOLD = 0.65F;
    private static final int HUNGER_THRESHOLD = 6;
    private static final float LOW_HEALTH_THRESHOLD = 0.50F;

    private AddictionStateExplainer() {
    }

    public static Explanation explain(ServerPlayer player,
                                      PlayerAddictionStats stats,
                                      float globalSeverity,
                                      boolean inSafeZone) {
        return explain(player, stats, globalSeverity, inSafeZone, null);
    }

    public static Explanation explain(ServerPlayer player,
                                      PlayerAddictionStats stats,
                                      float globalSeverity,
                                      boolean inSafeZone,
                                      @Nullable PlayerEnvironmentSnapshot environment) {
        PlayerEnvironmentSnapshot env = environment != null ? environment : PlayerRecoveryEnvironmentCache.snapshot(player);
        boolean safeZone = inSafeZone || (env != null && env.inSafeZone());
        long now = player.level().getGameTime();
        int recoveryFlags = recoveryFlags(stats, now, safeZone);
        DoseSnapshot dose = dominantDose(stats);
        WithdrawalPhase phase = withdrawalPhase(stats, now, globalSeverity);
        AddictionDangerReason danger = choosePrimaryDanger(player, stats, globalSeverity, safeZone, env, dose, phase, now);
        AddictionSuggestedAction action = chooseSuggestedAction(player, stats, danger, safeZone, env, now);

        return new Explanation(
                danger,
                action,
                phase,
                recoveryFlags,
                dose.category(),
                dose.state(),
                dose.dose(),
                dose.tolerance()
        );
    }

    public static int recoveryFlags(PlayerAddictionStats stats, long now, boolean inSafeZone) {
        TemporaryRecoveryEffects effects = stats.temporaryEffectsView();
        int flags = 0;
        if (inSafeZone) flags |= AddictionClientSnapshotPayload.RECOVERY_SAFE_ZONE;
        if (effects.hasDiaryCalm(now)) flags |= AddictionClientSnapshotPayload.RECOVERY_DIARY;
        if (effects.hasCalmingMixture(now)) flags |= AddictionClientSnapshotPayload.RECOVERY_CALMING_MIXTURE;
        if (effects.hasHeadphones(now)) flags |= AddictionClientSnapshotPayload.RECOVERY_HEADPHONES;
        if (effects.hasSleepBonus(now)) flags |= AddictionClientSnapshotPayload.RECOVERY_SLEEP_BONUS;
        if (effects.hasPreparedTea(now)) flags |= AddictionClientSnapshotPayload.RECOVERY_PREPARED_TEA;
        return flags;
    }

    public static boolean hasInventoryItem(ServerPlayer player, Item item) {
        if (player == null || item == null) {
            return false;
        }
        var inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                return true;
            }
        }
        return false;
    }

    public static int doseSeverity(DoseState state) {
        return switch (state) {
            case NORMAL -> 0;
            case DRUNK, HIGH -> 1;
            case VERY_DRUNK, VERY_HIGH -> 2;
            case ETHYLIC_COMA, OVERDOSE -> 3;
        };
    }

    private static AddictionDangerReason choosePrimaryDanger(ServerPlayer player,
                                                             PlayerAddictionStats stats,
                                                             float globalSeverity,
                                                             boolean inSafeZone,
                                                             @Nullable PlayerEnvironmentSnapshot environment,
                                                             DoseSnapshot dose,
                                                             WithdrawalPhase phase,
                                                             long now) {
        int doseSeverity = doseSeverity(dose.state());
        if (stats.overdoseDeathTimer > 0) {
            return AddictionDangerReason.OVERDOSE;
        }
        if (stats.badTrip.active) {
            return AddictionDangerReason.BAD_TRIP;
        }
        if (doseSeverity >= 3) {
            return AddictionDangerReason.OVERDOSE;
        }
        if (doseSeverity >= 2) {
            return AddictionDangerReason.VERY_HIGH_DOSE;
        }
        if (stats.sleepBlockedUntil > now) {
            return AddictionDangerReason.SLEEP_BLOCKED;
        }
        if (player.getFoodData().getFoodLevel() <= HUNGER_THRESHOLD) {
            return AddictionDangerReason.HUNGER;
        }
        if (healthRatio(player) <= LOW_HEALTH_THRESHOLD) {
            return AddictionDangerReason.LOW_HEALTH;
        }
        if (phase == WithdrawalPhase.PEAK || globalSeverity >= WITHDRAWAL_PEAK_THRESHOLD) {
            return AddictionDangerReason.WITHDRAWAL_PEAK;
        }
        if (stats.stressLevel >= STRESS_HIGH_THRESHOLD) {
            return AddictionDangerReason.HIGH_STRESS;
        }
        int companions = environment == null ? 0 : environment.companionCount();
        float pressure = Math.max(globalSeverity, stats.stressLevel);
        if (companions <= 0 && pressure >= 0.60F) {
            return AddictionDangerReason.ISOLATION;
        }
        if (!inSafeZone && pressure >= 0.45F) {
            return AddictionDangerReason.UNSAFE_PLACE;
        }
        if (doseSeverity >= 1) {
            return AddictionDangerReason.HIGH_DOSE;
        }
        return AddictionDangerReason.NONE;
    }

    private static AddictionSuggestedAction chooseSuggestedAction(ServerPlayer player,
                                                                  PlayerAddictionStats stats,
                                                                  AddictionDangerReason danger,
                                                                  boolean inSafeZone,
                                                                  @Nullable PlayerEnvironmentSnapshot environment,
                                                                  long now) {
        return switch (danger) {
            case OVERDOSE -> hasInventoryItem(player, ModItems.OVERDOSE_ANTIDOTE.get())
                    ? AddictionSuggestedAction.USE_ANTIDOTE
                    : AddictionSuggestedAction.GET_SAFE;
            case BAD_TRIP -> bestGroundingAction(player, stats, inSafeZone, environment, now);
            case VERY_HIGH_DOSE -> AddictionSuggestedAction.STOP_DOSING;
            case HIGH_DOSE -> AddictionSuggestedAction.WAIT;
            case SLEEP_BLOCKED -> sleepAction(player);
            case HUNGER -> AddictionSuggestedAction.EAT;
            case LOW_HEALTH -> AddictionSuggestedAction.HEAL;
            case WITHDRAWAL_PEAK, HIGH_STRESS -> bestStabilizingAction(player, stats, inSafeZone, environment, now);
            case ISOLATION -> inSafeZone ? AddictionSuggestedAction.STAY_SAFE : AddictionSuggestedAction.GET_SAFE;
            case UNSAFE_PLACE -> AddictionSuggestedAction.GET_SAFE;
            case NONE -> AddictionSuggestedAction.NONE;
        };
    }

    private static AddictionSuggestedAction bestGroundingAction(ServerPlayer player,
                                                                PlayerAddictionStats stats,
                                                                boolean inSafeZone,
                                                                @Nullable PlayerEnvironmentSnapshot environment,
                                                                long now) {
        if (!inSafeZone) {
            return AddictionSuggestedAction.GET_SAFE;
        }
        AddictionSuggestedAction support = supportAction(player, stats, environment, now);
        return support == AddictionSuggestedAction.NONE ? AddictionSuggestedAction.GROUND : support;
    }

    private static AddictionSuggestedAction bestStabilizingAction(ServerPlayer player,
                                                                  PlayerAddictionStats stats,
                                                                  boolean inSafeZone,
                                                                  @Nullable PlayerEnvironmentSnapshot environment,
                                                                  long now) {
        AddictionSuggestedAction support = supportAction(player, stats, environment, now);
        if (support != AddictionSuggestedAction.NONE) {
            return support;
        }
        return inSafeZone ? AddictionSuggestedAction.STAY_SAFE : AddictionSuggestedAction.GET_SAFE;
    }

    private static AddictionSuggestedAction supportAction(ServerPlayer player,
                                                          PlayerAddictionStats stats,
                                                          @Nullable PlayerEnvironmentSnapshot environment,
                                                          long now) {
        boolean hasDiary = environment != null && environment.hasDiary();
        boolean hasHeadphones = environment != null && environment.hasHeadphones();
        if (hasDiary && !stats.temporaryEffectsView().hasDiaryCalm(now)) {
            return AddictionSuggestedAction.WRITE_DIARY;
        }
        if (hasHeadphones && !stats.temporaryEffectsView().hasHeadphones(now)) {
            return AddictionSuggestedAction.USE_HEADPHONES;
        }
        if (hasInventoryItem(player, ModItems.HERBAL_TEA.get()) && !stats.temporaryEffectsView().hasPreparedTea(now)) {
            return AddictionSuggestedAction.DRINK_TEA;
        }
        return AddictionSuggestedAction.NONE;
    }

    private static AddictionSuggestedAction sleepAction(ServerPlayer player) {
        if (player.getFoodData().getFoodLevel() <= HUNGER_THRESHOLD) {
            return AddictionSuggestedAction.EAT;
        }
        if (hasInventoryItem(player, ModItems.HERBAL_TEA.get())) {
            return AddictionSuggestedAction.DRINK_TEA;
        }
        if (hasInventoryItem(player, ModItems.SLEEPING_AID.get())) {
            return AddictionSuggestedAction.SLEEP_LATER;
        }
        return AddictionSuggestedAction.SLEEP_LATER;
    }

    private static WithdrawalPhase withdrawalPhase(PlayerAddictionStats stats, long now, float globalSeverity) {
        if (globalSeverity < WITHDRAWAL_ACTIVE_THRESHOLD) {
            return WithdrawalPhase.NONE;
        }

        DrugId dominant = stats.getMostWithdrawingDrugId();
        if (dominant == null) {
            return globalSeverity >= WITHDRAWAL_PEAK_THRESHOLD ? WithdrawalPhase.PEAK : WithdrawalPhase.SETTLING;
        }

        DrugAddictionStats drugStats = stats.getDrugStats(dominant);
        if (drugStats == null) {
            return WithdrawalPhase.NONE;
        }

        WithdrawalPhase phase = AddictionMath.getPhase(
                Math.max(0L, now - drugStats.lastUseTime),
                AddictionConfigs.get(DrugRegistry.getCategory(dominant))
        );
        if (phase == WithdrawalPhase.NONE && globalSeverity >= WITHDRAWAL_PEAK_THRESHOLD) {
            return WithdrawalPhase.PEAK;
        }
        if (phase == WithdrawalPhase.NONE && globalSeverity >= WITHDRAWAL_ACTIVE_THRESHOLD) {
            return WithdrawalPhase.SETTLING;
        }
        return phase;
    }

    private static DoseSnapshot dominantDose(PlayerAddictionStats stats) {
        DrugCategory bestCategory = DrugCategory.OTHER;
        DoseState bestState = DoseState.NORMAL;
        float bestDose = 0.0F;
        float bestTolerance = 0.0F;
        int bestSeverity = -1;

        for (DrugCategory category : DrugCategory.values()) {
            DosePath path = DosePath.of(category);
            if (path == DosePath.NONE) {
                continue;
            }

            float dose = stats.getCategoryCurrentDose(category);
            DoseState state = DoseManager.resolveState(path, dose);
            int severity = doseSeverity(state);
            if (dose <= 0.001F && severity <= 0) {
                continue;
            }
            if (severity > bestSeverity || (severity == bestSeverity && dose > bestDose)) {
                bestSeverity = severity;
                bestCategory = category;
                bestState = state;
                bestDose = dose;
                bestTolerance = stats.getCategoryTolerance(category);
            }
        }

        return new DoseSnapshot(bestCategory, bestState, bestDose, AddictionMath.clamp(bestTolerance, 0.0F, 1.0F));
    }

    private static float healthRatio(ServerPlayer player) {
        float maxHealth = player.getMaxHealth();
        if (maxHealth <= 0.0F) {
            return 1.0F;
        }
        return AddictionMath.clamp(player.getHealth() / maxHealth, 0.0F, 1.0F);
    }

    public record Explanation(
            AddictionDangerReason primaryDangerReason,
            AddictionSuggestedAction suggestedAction,
            WithdrawalPhase withdrawalPhase,
            int recoveryFlags,
            DrugCategory dominantDoseCategory,
            DoseState dominantDoseState,
            float dominantDose,
            float dominantTolerance
    ) {
        public Explanation {
            primaryDangerReason = primaryDangerReason == null ? AddictionDangerReason.NONE : primaryDangerReason;
            suggestedAction = suggestedAction == null ? AddictionSuggestedAction.NONE : suggestedAction;
            withdrawalPhase = withdrawalPhase == null ? WithdrawalPhase.NONE : withdrawalPhase;
            dominantDoseCategory = dominantDoseCategory == null ? DrugCategory.OTHER : dominantDoseCategory;
            dominantDoseState = dominantDoseState == null ? DoseState.NORMAL : dominantDoseState;
            dominantDose = Float.isFinite(dominantDose) ? AddictionMath.clamp(dominantDose, 0.0F, Float.MAX_VALUE) : 0.0F;
            dominantTolerance = Float.isFinite(dominantTolerance) ? AddictionMath.clamp(dominantTolerance, 0.0F, 1.0F) : 0.0F;
        }

        public int primaryDangerReasonId() {
            return primaryDangerReason.networkId();
        }

        public int suggestedActionId() {
            return suggestedAction.networkId();
        }

        public int withdrawalPhaseId() {
            return withdrawalPhase.networkId();
        }
    }

    private record DoseSnapshot(DrugCategory category, DoseState state, float dose, float tolerance) {
    }
}
