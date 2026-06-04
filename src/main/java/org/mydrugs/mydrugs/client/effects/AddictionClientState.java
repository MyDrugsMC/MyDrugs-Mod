package org.mydrugs.mydrugs.client.effects;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.addiction.config.SymptomFlags;
import org.mydrugs.mydrugs.addiction.data.WithdrawalPhase;
import org.mydrugs.mydrugs.addiction.explain.AddictionDangerReason;
import org.mydrugs.mydrugs.addiction.explain.AddictionSuggestedAction;
import org.mydrugs.mydrugs.core.drug.dose.DosePath;
import org.mydrugs.mydrugs.core.drug.dose.DoseState;
import org.mydrugs.mydrugs.core.drug.dose.DoseManager;
import org.mydrugs.mydrugs.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.addiction.network.BadTripPayload;
import org.mydrugs.mydrugs.addiction.network.DoseSyncPayload;
import org.mydrugs.mydrugs.addiction.network.DrugEffectSyncPayload;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

public final class AddictionClientState {
    public static float globalSeverity;
    public static float stressLevel;
    public static String dominantDrugId = "";
    public static String dominantCategory = "OTHER";
    public static int symptomFlags;
    public static int insomniaTicksRemaining;
    public static int recoveryFlags;
    public static int overdoseTicksRemaining;
    public static int primaryDangerReason;
    public static int suggestedAction;
    public static int withdrawalPhase;
    public static float dominantTolerance;
    public static float dominantDose;
    public static boolean badTripActive;
    public static float badTripThreshold;
    public static float badTripSeverity;
    public static int badTripTicksActive;
    public static String badTripSourceDrug = "";
    public static String badTripSourceCategory = "OTHER";
    public static float badTripSymptomIntensity;

    private static final float[] categoryDoses = new float[DrugCategory.values().length];
    private static final EnumMap<EffectType, ClientDrugEffect> activeEffects = new EnumMap<>(EffectType.class);
    private static final float TREND_DEADBAND = 0.02F;
    private static final float TREND_SMOOTHING = 0.12F;
    private static final String TREND_RISING = "↑";
    private static final String TREND_EASING = "↓";
    private static final String TREND_STABLE = "→";

    private static boolean withdrawalTrendInitialized;
    private static boolean stressTrendInitialized;
    private static boolean badTripTrendInitialized;
    private static boolean doseTrendInitialized;
    private static float withdrawalSmoothed;
    private static float stressSmoothed;
    private static float badTripSmoothed;
    private static float doseSmoothed;
    private static String withdrawalTrendArrow = TREND_STABLE;
    private static String stressTrendArrow = TREND_STABLE;
    private static String badTripTrendArrow = TREND_STABLE;
    private static String doseTrendArrow = TREND_STABLE;

    private AddictionClientState() {
    }

    public static void clear() {
        globalSeverity = 0.0F;
        stressLevel = 0.0F;
        dominantDrugId = "";
        dominantCategory = "OTHER";
        symptomFlags = 0;
        insomniaTicksRemaining = 0;
        recoveryFlags = 0;
        overdoseTicksRemaining = 0;
        primaryDangerReason = 0;
        suggestedAction = 0;
        withdrawalPhase = 0;
        dominantTolerance = 0.0F;
        dominantDose = 0.0F;
        badTripActive = false;
        badTripThreshold = 0.0F;
        badTripSeverity = 0.0F;
        badTripTicksActive = 0;
        badTripSourceDrug = "";
        badTripSourceCategory = "OTHER";
        badTripSymptomIntensity = 0.0F;
        Arrays.fill(categoryDoses, 0.0F);
        activeEffects.clear();
        resetTrends();
    }

    public static void apply(AddictionClientSnapshotPayload payload) {
        updateWithdrawalTrend(payload.globalSeverity());
        updateStressTrend(payload.stressLevel());
        updateDoseTrend(payload.dominantDose());
        globalSeverity = payload.globalSeverity();
        stressLevel = payload.stressLevel();
        dominantDrugId = payload.dominantDrugId();
        dominantCategory = payload.dominantCategory();
        symptomFlags = payload.symptomFlags();
        insomniaTicksRemaining = payload.insomniaTicksRemaining();
        recoveryFlags = payload.recoveryFlags();
        overdoseTicksRemaining = payload.overdoseTicksRemaining();
        primaryDangerReason = payload.primaryDangerReason();
        suggestedAction = payload.suggestedAction();
        withdrawalPhase = payload.withdrawalPhase();
        dominantTolerance = payload.dominantTolerance();
        dominantDose = payload.dominantDose();
    }

    public static void applyDoseSync(DoseSyncPayload payload) {
        Arrays.fill(categoryDoses, 0.0F);
        float[] incoming = payload.doses();
        System.arraycopy(incoming, 0, categoryDoses, 0, Math.min(incoming.length, categoryDoses.length));
    }

    public static void applyBadTrip(BadTripPayload payload) {
        updateBadTripTrend(payload.severity());
        badTripActive = payload.active();
        badTripThreshold = payload.threshold();
        badTripSeverity = payload.severity();
        badTripTicksActive = payload.ticksActive();
        badTripSourceDrug = payload.sourceDrug();
        badTripSourceCategory = payload.sourceCategory();
        badTripSymptomIntensity = payload.symptomIntensity();
    }

    public static void applyDrugEffectSync(DrugEffectSyncPayload payload) {
        activeEffects.clear();
        for (DrugEffectSyncPayload.Entry entry : payload.effects()) {
            if (entry.type() != null && entry.intensity() > 0.0F
                    && (entry.activeTicks() > 0 || entry.fadeTicksRemaining() > 0 || entry.remainingTicks() > 0)) {
                activeEffects.put(entry.type(), new ClientDrugEffect(entry));
            }
        }
    }

    public static float getEffectIntensity(EffectType type) {
        ClientDrugEffect effect = activeEffects.get(type);
        return effect == null ? 0.0F : effect.intensity();
    }

    public static boolean hasEffect(EffectType type) {
        return getEffectIntensity(type) > 0.001F;
    }

    public static List<ClientEffectView> activeEffectViews() {
        List<ClientEffectView> views = new ArrayList<>(activeEffects.size());
        for (var entry : activeEffects.entrySet()) {
            ClientDrugEffect effect = entry.getValue();
            if (effect.remainingTicks() <= 0 || effect.intensity() <= 0.001F) {
                continue;
            }
            views.add(effect.toView(entry.getKey()));
        }
        views.sort(Comparator
                .comparingInt(AddictionClientState::effectPriority).reversed()
                .thenComparingInt(ClientEffectView::remainingTicks));
        return List.copyOf(views);
    }

    public static String formatDuration(int ticks) {
        int seconds = Math.max(0, Math.round(ticks / 20.0F));
        if (seconds <= 0) {
            return "<1s";
        }
        if (seconds < 60) {
            return seconds + "s";
        }
        int minutes = seconds / 60;
        int remainder = seconds % 60;
        return minutes + ":" + (remainder < 10 ? "0" : "") + remainder;
    }

    public static boolean has(int flag) {
        return SymptomFlags.has(symptomFlags, flag);
    }

    public static boolean isSleepBlocked() {
        return insomniaTicksRemaining > 0;
    }

    public static boolean hasInsomniaSymptom() {
        return has(SymptomFlags.INSOMNIA);
    }

    public static boolean hasInsomnia() {
        return isSleepBlocked() || hasInsomniaSymptom();
    }

    public static boolean hasSymptoms() {
        return symptomFlags != 0;
    }

    public static boolean isInSafeZone() {
        return hasRecoveryFlag(AddictionClientSnapshotPayload.RECOVERY_SAFE_ZONE);
    }

    public static boolean hasDiaryCalm() {
        return hasRecoveryFlag(AddictionClientSnapshotPayload.RECOVERY_DIARY);
    }

    public static boolean hasHeadphonesCalm() {
        return hasRecoveryFlag(AddictionClientSnapshotPayload.RECOVERY_HEADPHONES);
    }

    public static boolean hasCalmingMixture() {
        return hasRecoveryFlag(AddictionClientSnapshotPayload.RECOVERY_CALMING_MIXTURE);
    }

    public static boolean hasSleepBonus() {
        return hasRecoveryFlag(AddictionClientSnapshotPayload.RECOVERY_SLEEP_BONUS);
    }

    public static boolean hasPreparedTea() {
        return hasRecoveryFlag(AddictionClientSnapshotPayload.RECOVERY_PREPARED_TEA);
    }

    public static boolean hasActiveRecoverySupport() {
        return recoveryFlags != 0;
    }

    public static boolean hasOverdoseTimer() {
        return overdoseTicksRemaining > 0;
    }

    public static boolean hasAnyDose() {
        for (float dose : categoryDoses) {
            if (dose > 0.001F) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasDangerousDoseState() {
        return doseSeverity(getDominantDoseState()) > 0 || hasOverdoseTimer();
    }

    public static float getDose(DrugCategory category) {
        int id = category.networkId();
        return id < categoryDoses.length ? categoryDoses[id] : 0.0F;
    }

    public static DoseState getDoseState(DrugCategory category) {
        return DoseManager.resolveState(DosePath.of(category), getDose(category));
    }

    public static DrugCategory getDisplayedDoseCategory() {
        DrugCategory dominant = getDominantCategoryEnum();
        if (DosePath.of(dominant) != DosePath.NONE) {
            DoseState dominantState = getDoseState(dominant);
            if (doseSeverity(dominantState) > 0 || getDose(dominant) > 0.001F) {
                return dominant;
            }
        }

        DrugCategory bestCategory = dominant;
        int bestSeverity = -1;
        float bestDose = -1.0F;
        for (DrugCategory category : DrugCategory.values()) {
            if (DosePath.of(category) == DosePath.NONE) {
                continue;
            }

            DoseState state = getDoseState(category);
            int severity = doseSeverity(state);
            float dose = getDose(category);
            if (severity > bestSeverity || (severity == bestSeverity && dose > bestDose)) {
                bestSeverity = severity;
                bestDose = dose;
                bestCategory = category;
            }
        }
        return bestCategory;
    }

    public static DoseState getDominantDoseState() {
        DrugCategory category = getDisplayedDoseCategory();
        return DosePath.of(category) == DosePath.NONE ? DoseState.NORMAL : getDoseState(category);
    }

    public static DrugCategory getDominantCategoryEnum() {
        try {
            return DrugCategory.valueOf(dominantCategory);
        } catch (IllegalArgumentException ignored) {
            return DrugCategory.OTHER;
        }
    }

    public static @Nullable DrugId getDominantDrugIdEnum() {
        if (dominantDrugId == null || dominantDrugId.isBlank()) {
            return null;
        }

        return DrugId.bySerializedName(dominantDrugId).orElse(null);
    }

    public static boolean shouldRenderHud() {
        boolean unstable = globalSeverity > 0.01F || stressLevel > 0.05F || hasSymptoms() || isSleepBlocked();
        boolean doseDanger = hasDangerousDoseState();
        boolean temporarySupport = hasDiaryCalm() || hasCalmingMixture() || hasHeadphonesCalm() || hasSleepBonus();
        boolean safeZoneContext = isInSafeZone() && unstable;
        return badTripActive || unstable || doseDanger || temporarySupport || safeZoneContext
                || getPrimaryDangerReason() != AddictionDangerReason.NONE
                || !activeEffects.isEmpty();
    }

    public static void tick() {
        if (insomniaTicksRemaining > 0) {
            insomniaTicksRemaining--;
        }
        if (overdoseTicksRemaining > 0) {
            overdoseTicksRemaining--;
        }
        if (badTripActive) {
            badTripTicksActive++;
        }
        activeEffects.entrySet().removeIf(entry -> entry.getValue().tick());
    }

    public static AddictionDangerReason getPrimaryDangerReason() {
        return AddictionDangerReason.byNetworkId(primaryDangerReason);
    }

    public static AddictionSuggestedAction getSuggestedAction() {
        return AddictionSuggestedAction.byNetworkId(suggestedAction);
    }

    public static WithdrawalPhase getWithdrawalPhase() {
        return WithdrawalPhase.byNetworkId(withdrawalPhase);
    }

    public static String trendArrowForWithdrawal() {
        return withdrawalTrendArrow;
    }

    public static String trendArrowForStress() {
        return stressTrendArrow;
    }

    public static String trendArrowForBadTrip() {
        return badTripTrendArrow;
    }

    public static String trendArrowForDose() {
        return doseTrendArrow;
    }

    public static String trendArrow(float current, float previousSmoothed) {
        float diff = current - previousSmoothed;
        if (diff >= TREND_DEADBAND) {
            return TREND_RISING;
        }
        if (diff <= -TREND_DEADBAND) {
            return TREND_EASING;
        }
        return TREND_STABLE;
    }

    public record ClientEffectView(EffectType type, float effectiveIntensity, float riskPressure, int remainingTicks,
                                   boolean fading, boolean comedown, String displayLabel) {
        public String durationText() {
            return formatDuration(remainingTicks);
        }
    }

    private static final class ClientDrugEffect {
        private final float intensity;
        private final float riskPressure;
        private int activeTicks;
        private int ageTicks;
        private final int baselineDurationTicks;
        private int fadeTicksRemaining;
        private final int fadeDurationTicks;
        private final int onsetTicks;
        private final int peakTicks;
        private final int comedownTicks;

        private ClientDrugEffect(DrugEffectSyncPayload.Entry entry) {
            this.intensity = entry.intensity();
            this.riskPressure = entry.riskPressure();
            this.activeTicks = Math.max(0, entry.activeTicks());
            this.ageTicks = Math.max(0, entry.ageTicks());
            this.baselineDurationTicks = Math.max(this.activeTicks, entry.baselineDurationTicks());
            this.fadeTicksRemaining = Math.max(0, entry.fadeTicksRemaining());
            this.fadeDurationTicks = Math.max(0, entry.fadeDurationTicks());
            this.onsetTicks = Math.max(0, entry.onsetTicks());
            this.peakTicks = Math.max(0, entry.peakTicks());
            this.comedownTicks = Math.max(0, entry.comedownTicks());
        }

        private float intensity() {
            return this.intensity * activePhaseScale() * fadeScale();
        }

        private int remainingTicks() {
            return activeTicks + fadeTicksRemaining;
        }

        private boolean fading() {
            return activeTicks <= 0 && fadeTicksRemaining > 0;
        }

        private boolean comedown() {
            if (fading() || !hasPhaseCurve() || activeTicks <= 0) {
                return false;
            }
            int total = activeDurationTotal();
            int onset = Math.min(onsetTicks, Math.max(0, total - 1));
            return ageTicks >= comedownStart(total, onset);
        }

        private ClientEffectView toView(EffectType type) {
            return new ClientEffectView(
                    type,
                    intensity(),
                    riskPressure,
                    remainingTicks(),
                    fading(),
                    comedown(),
                    type.diaryLabel().isBlank() ? type.serializedName() : type.diaryLabel()
            );
        }

        private boolean tick() {
            if (this.activeTicks > 0) {
                this.ageTicks++;
                this.activeTicks--;
            }
            if (this.activeTicks <= 0 && this.fadeTicksRemaining <= 0 && this.fadeDurationTicks > 0) {
                this.fadeTicksRemaining = this.fadeDurationTicks;
            }
            if (this.fadeTicksRemaining > 0) {
                this.fadeTicksRemaining--;
            }
            return this.activeTicks <= 0 && this.fadeTicksRemaining <= 0;
        }

        private boolean hasPhaseCurve() {
            return onsetTicks > 0 || peakTicks > 0 || comedownTicks > 0;
        }

        private float activePhaseScale() {
            if (!hasPhaseCurve() || activeTicks <= 0) {
                return 1.0F;
            }

            int total = activeDurationTotal();
            int onset = Math.min(onsetTicks, Math.max(0, total - 1));
            if (onset > 0 && ageTicks < onset) {
                return Math.clamp(ageTicks / (float) onset, 0.0F, 1.0F);
            }

            int comedownStart = comedownStart(total, onset);
            if (ageTicks >= comedownStart && total > comedownStart) {
                return Math.clamp((total - ageTicks) / (float) (total - comedownStart), 0.0F, 1.0F);
            }

            return 1.0F;
        }

        private float fadeScale() {
            if (activeTicks > 0 || fadeDurationTicks <= 0) {
                return 1.0F;
            }
            return Math.clamp(fadeTicksRemaining / (float) fadeDurationTicks, 0.0F, 1.0F);
        }

        private int activeDurationTotal() {
            return Math.max(1, Math.max(baselineDurationTicks, ageTicks + activeTicks));
        }

        private int comedownStart(int total, int onset) {
            int remainingAfterOnset = Math.max(0, total - onset);
            int comedown = Math.min(comeDownTicks(), remainingAfterOnset);
            int peak = Math.min(peakTicks, Math.max(0, remainingAfterOnset - comedown));
            return Math.clamp(Math.max(onset + peak, total - comedown), onset, total);
        }

        private int comeDownTicks() {
            return comedownTicks;
        }
    }

    private static boolean hasRecoveryFlag(int flag) {
        return (recoveryFlags & flag) != 0;
    }

    private static void resetTrends() {
        withdrawalTrendInitialized = false;
        stressTrendInitialized = false;
        badTripTrendInitialized = false;
        doseTrendInitialized = false;
        withdrawalSmoothed = 0.0F;
        stressSmoothed = 0.0F;
        badTripSmoothed = 0.0F;
        doseSmoothed = 0.0F;
        withdrawalTrendArrow = TREND_STABLE;
        stressTrendArrow = TREND_STABLE;
        badTripTrendArrow = TREND_STABLE;
        doseTrendArrow = TREND_STABLE;
    }

    private static void updateWithdrawalTrend(float current) {
        if (!withdrawalTrendInitialized) {
            withdrawalSmoothed = current;
            withdrawalTrendInitialized = true;
            withdrawalTrendArrow = TREND_STABLE;
            return;
        }
        withdrawalTrendArrow = trendArrow(current, withdrawalSmoothed);
        withdrawalSmoothed += (current - withdrawalSmoothed) * TREND_SMOOTHING;
    }

    private static void updateStressTrend(float current) {
        if (!stressTrendInitialized) {
            stressSmoothed = current;
            stressTrendInitialized = true;
            stressTrendArrow = TREND_STABLE;
            return;
        }
        stressTrendArrow = trendArrow(current, stressSmoothed);
        stressSmoothed += (current - stressSmoothed) * TREND_SMOOTHING;
    }

    private static void updateBadTripTrend(float current) {
        if (!badTripTrendInitialized) {
            badTripSmoothed = current;
            badTripTrendInitialized = true;
            badTripTrendArrow = TREND_STABLE;
            return;
        }
        badTripTrendArrow = trendArrow(current, badTripSmoothed);
        badTripSmoothed += (current - badTripSmoothed) * TREND_SMOOTHING;
    }

    private static void updateDoseTrend(float current) {
        if (!doseTrendInitialized) {
            doseSmoothed = current;
            doseTrendInitialized = true;
            doseTrendArrow = TREND_STABLE;
            return;
        }
        doseTrendArrow = trendArrow(current, doseSmoothed);
        doseSmoothed += (current - doseSmoothed) * TREND_SMOOTHING;
    }

    private static int doseSeverity(DoseState state) {
        return switch (state) {
            case NORMAL -> 0;
            case DRUNK, HIGH -> 1;
            case VERY_DRUNK, VERY_HIGH -> 2;
            case ETHYLIC_COMA, OVERDOSE -> 3;
        };
    }

    private static int effectPriority(ClientEffectView view) {
        int priority = 0;
        if (view.type().isHarmful()) {
            priority += 100;
        }
        if (view.riskPressure() > view.effectiveIntensity() + 0.25F) {
            priority += 40;
        }
        priority += Math.round(Math.min(1.5F, view.effectiveIntensity()) * 20.0F);
        if (view.remainingTicks() < 20 * 12) {
            priority += 15;
        }
        if (view.fading() || view.comedown()) {
            priority += 8;
        }
        return priority;
    }
}
