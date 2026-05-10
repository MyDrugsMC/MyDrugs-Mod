package org.mydrugs.mydrugs.effects.addiction.client;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.effects.addiction.config.SymptomFlags;
import org.mydrugs.mydrugs.effects.addiction.dose.DosePath;
import org.mydrugs.mydrugs.effects.addiction.dose.DoseState;
import org.mydrugs.mydrugs.effects.addiction.manager.dose.DoseManager;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.effects.addiction.network.BadTripPayload;
import org.mydrugs.mydrugs.effects.addiction.network.DoseSyncPayload;
import org.mydrugs.mydrugs.effects.addiction.network.DrugEffectSyncPayload;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.Arrays;
import java.util.EnumMap;

public final class AddictionClientState {
    public static float globalSeverity;
    public static float stressLevel;
    public static String dominantDrugId = "";
    public static String dominantCategory = "OTHER";
    public static int symptomFlags;
    public static int insomniaTicksRemaining;
    public static int recoveryFlags;
    public static int overdoseTicksRemaining;
    public static boolean badTripActive;
    public static float badTripThreshold;
    public static float badTripSeverity;
    public static int badTripTicksActive;
    public static String badTripSourceDrug = "";
    public static String badTripSourceCategory = "OTHER";
    public static float badTripSymptomIntensity;

    private static final float[] categoryDoses = new float[DrugCategory.values().length];
    private static final EnumMap<EffectType, ClientDrugEffect> activeEffects = new EnumMap<>(EffectType.class);

    private AddictionClientState() {
    }

    public static void apply(AddictionClientSnapshotPayload payload) {
        globalSeverity = payload.globalSeverity();
        stressLevel = payload.stressLevel();
        dominantDrugId = payload.dominantDrugId();
        dominantCategory = payload.dominantCategory();
        symptomFlags = payload.symptomFlags();
        insomniaTicksRemaining = payload.insomniaTicksRemaining();
        recoveryFlags = payload.recoveryFlags();
        overdoseTicksRemaining = payload.overdoseTicksRemaining();
    }

    public static void applyDoseSync(DoseSyncPayload payload) {
        Arrays.fill(categoryDoses, 0.0F);
        float[] incoming = payload.doses();
        System.arraycopy(incoming, 0, categoryDoses, 0, Math.min(incoming.length, categoryDoses.length));
    }

    public static void applyBadTrip(BadTripPayload payload) {
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
            if (entry.type() != null && entry.intensity() > 0.0F && entry.remainingTicks() > 0) {
                activeEffects.put(entry.type(), new ClientDrugEffect(
                        entry.intensity(),
                        entry.remainingTicks(),
                        entry.fadeTicksRemaining(),
                        entry.fadeDurationTicks()
                ));
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
        return badTripActive || unstable || doseDanger || temporarySupport || safeZoneContext;
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

    private static final class ClientDrugEffect {
        private final float intensity;
        private int remainingTicks;
        private int fadeTicksRemaining;
        private final int fadeDurationTicks;

        private ClientDrugEffect(float intensity, int remainingTicks, int fadeTicksRemaining, int fadeDurationTicks) {
            this.intensity = intensity;
            this.remainingTicks = remainingTicks;
            this.fadeTicksRemaining = fadeTicksRemaining;
            this.fadeDurationTicks = fadeDurationTicks;
        }

        private float intensity() {
            if (this.fadeTicksRemaining <= 0 || this.fadeDurationTicks <= 0) {
                return this.intensity;
            }
            return this.intensity * Math.clamp(this.fadeTicksRemaining / (float) this.fadeDurationTicks, 0.0F, 1.0F);
        }

        private boolean tick() {
            if (this.remainingTicks > 0) {
                this.remainingTicks--;
            }
            if (this.remainingTicks <= 0 && this.fadeTicksRemaining <= 0 && this.fadeDurationTicks > 0) {
                this.fadeTicksRemaining = this.fadeDurationTicks;
            }
            if (this.fadeTicksRemaining > 0) {
                this.fadeTicksRemaining--;
            }
            return this.remainingTicks <= 0 && this.fadeTicksRemaining <= 0;
        }
    }

    private static boolean hasRecoveryFlag(int flag) {
        return (recoveryFlags & flag) != 0;
    }

    private static int doseSeverity(DoseState state) {
        return switch (state) {
            case NORMAL -> 0;
            case DRUNK, HIGH -> 1;
            case VERY_DRUNK, VERY_HIGH -> 2;
            case ETHYLIC_COMA, OVERDOSE -> 3;
        };
    }
}
