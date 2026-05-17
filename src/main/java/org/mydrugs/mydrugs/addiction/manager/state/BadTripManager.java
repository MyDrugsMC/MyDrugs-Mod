package org.mydrugs.mydrugs.addiction.manager.state;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.addiction.config.DoseConstants;
import org.mydrugs.mydrugs.addiction.config.SymptomFlags;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.core.drug.dose.DosePath;
import org.mydrugs.mydrugs.core.drug.dose.DoseState;
import org.mydrugs.mydrugs.core.drug.dose.DoseManager;
import org.mydrugs.mydrugs.core.drug.runtime.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.addiction.network.BadTripPayload;
import org.mydrugs.mydrugs.addiction.util.AddictionMath;
import org.mydrugs.mydrugs.entity.InnerDemonSpawnManager;
import org.mydrugs.mydrugs.psyche.PsycheMapMilestones;
import org.mydrugs.mydrugs.sounds.ModSounds;

public final class BadTripManager {
    private static final String[] START_MESSAGES = {
            "message.mydrugs.bad_trip.start.1",
            "message.mydrugs.bad_trip.start.2",
            "message.mydrugs.bad_trip.start.3",
            "message.mydrugs.bad_trip.start.4"
    };

    private BadTripManager() {
    }

    public static void tick(ServerPlayer player, PlayerAddictionStats stats) {
        BadTripState state = stats.badTrip;
        @Nullable Candidate candidate = findBestCandidate(stats, StressManager.getStress(stats));

        if (candidate == null) {
            if (state.active) {
                stop(player, state);
            } else {
                state.reset();
            }
            return;
        }

        if (!state.active) {
            if (candidate.stress() <= candidate.threshold()) {
                state.threshold = candidate.threshold();
                state.severity = candidate.severity();
                state.sourceDrug = candidate.drugId();
                state.sourceCategory = candidate.category();
                return;
            }

            start(player, state, candidate);
            applySymptoms(player, state);
            return;
        }

        state.threshold = candidate.threshold();
        state.severity = candidate.severity();
        state.sourceDrug = candidate.drugId();
        state.sourceCategory = candidate.category();
        state.violentDemonHook = severityBand(state.severity) >= 2;
        if (state.severity >= AddictionConstants.BAD_TRIP_VIOLENT_THRESHOLD) {
            state.reachedViolentSeverity = true;
        }
        state.ticksActive++;

        if (candidate.stress() < candidate.threshold() - AddictionConstants.BAD_TRIP_STOP_HYSTERESIS) {
            stop(player, state);
            return;
        }

        boolean rerolled = false;
        if (state.nextSymptomReroll <= 0) {
            state.symptomIntensity = rerollSymptomIntensity(player);
            state.nextSymptomReroll = AddictionConstants.BAD_TRIP_SYMPTOM_REROLL_TICKS;
            rerolled = true;
        } else {
            state.nextSymptomReroll--;
        }

        applySymptoms(player, state);
        InnerDemonSpawnManager.tickBadTrip(player, stats);
        syncIfNeeded(player, state, rerolled);
    }

    public static void stop(ServerPlayer player, PlayerAddictionStats stats) {
        if (stats.badTrip.active) {
            stop(player, stats.badTrip);
        } else {
            stats.badTrip.reset();
        }
    }

    public static boolean isActive(PlayerAddictionStats stats) {
        return stats.badTrip.active;
    }

    public static int symptomFlags(PlayerAddictionStats stats) {
        if (!stats.badTrip.active) {
            return 0;
        }

        return SymptomFlags.CONFUSION
                | SymptomFlags.VISION
                | SymptomFlags.HALLUCINATION
                | SymptomFlags.STRESS
                | SymptomFlags.DISSOCIATION
                | SymptomFlags.INTRUSIVE_THOUGHTS
                | SymptomFlags.INSOMNIA;
    }

    private static void start(ServerPlayer player, BadTripState state, Candidate candidate) {
        state.reset();
        state.active = true;
        state.threshold = candidate.threshold();
        state.severity = candidate.severity();
        state.ticksActive = 1;
        state.nextSymptomReroll = AddictionConstants.BAD_TRIP_SYMPTOM_REROLL_TICKS;
        state.nextDemonSpawnAttempt = 0;
        state.firstDemonSpawnDelay = 60 + player.getRandom().nextInt(81);
        state.symptomIntensity = rerollSymptomIntensity(player);
        state.sourceDrug = candidate.drugId();
        state.sourceCategory = candidate.category();
        state.violentDemonHook = severityBand(state.severity) >= 2;
        state.lastSyncedBand = severityBand(state.severity);

        String key = START_MESSAGES[player.getRandom().nextInt(START_MESSAGES.length)];
        player.displayClientMessage(Component.translatable(key), true);
        player.level().playSound(null, player.blockPosition(), ModSounds.HALLUCINATION_CUE.get(), SoundSource.PLAYERS, 0.75F, 0.75F);
        PsycheMapMilestones.badTrip(player);
        sync(player, state);
    }

    private static void stop(ServerPlayer player, BadTripState state) {
        InnerDemonSpawnManager.markOwnedDemonsForDespawn(player);
        if (state.reachedViolentSeverity) {
            net.minecraft.world.item.ItemStack reward = new net.minecraft.world.item.ItemStack(
                    org.mydrugs.mydrugs.items.ModItems.BROKEN_COURAGE.get(), 1);
            if (!player.getInventory().add(reward)) {
                player.drop(reward, false);
            }
        }
        state.reset();
        player.displayClientMessage(Component.translatable("message.mydrugs.bad_trip.end"), true);
        sync(player, state);
    }

    private static void applySymptoms(ServerPlayer player, BadTripState state) {
        if (!state.active || player.tickCount % AddictionConstants.BAD_TRIP_EFFECT_REFRESH_TICKS != 0) {
            return;
        }

        float rawIntensity = AddictionMath.clamp(state.symptomIntensity * (0.70F + state.severity * 0.30F), 0.0F, 1.0F);
        float resistance = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.BAD_TRIP_RESISTANCE);
        float intensity = rawIntensity * Math.max(0.0F, 1.0F - resistance);
        int duration = AddictionConstants.BAD_TRIP_EFFECT_DURATION_TICKS;

        DrugEffectRuntimeManager.addEffect(player, EffectType.CONFUSION, 0.45F + intensity * 0.35F, duration);
        DrugEffectRuntimeManager.addEffect(player, EffectType.CAMERA_SWAY, 0.10F + intensity * 0.22F, duration);
        DrugEffectRuntimeManager.addEffect(player, EffectType.INPUT_FAIL, 0.03F + intensity * 0.08F, duration);
        DrugEffectRuntimeManager.addEffect(player, EffectType.STUMBLE, 0.04F + intensity * 0.10F, duration);
        DrugEffectRuntimeManager.addEffect(player, EffectType.BLUR, 0.18F + intensity * 0.20F, duration);
        DrugEffectRuntimeManager.addEffect(player, EffectType.HEARTBEAT, 0.35F + intensity * 0.35F, duration);
    }

    private static void syncIfNeeded(ServerPlayer player, BadTripState state, boolean rerolled) {
        int band = severityBand(state.severity);
        if (rerolled || band != state.lastSyncedBand) {
            state.lastSyncedBand = band;
            sync(player, state);
        }
    }

    private static void sync(ServerPlayer player, BadTripState state) {
        PacketDistributor.sendToPlayer(player, BadTripPayload.from(state));
    }

    private static @Nullable Candidate findBestCandidate(PlayerAddictionStats stats, float stress) {
        Candidate best = null;
        for (DrugId drugId : stats.getTrackedDrugIds()) {
            DrugCategory category = DrugRegistry.getCategory(drugId);
            if (!allowsBadTrip(category) || DosePath.of(category) != DosePath.DRUG) {
                continue;
            }

            DrugAddictionStats drugStats = stats.getDrugStats(drugId);
            if (drugStats == null) {
                continue;
            }

            float dose = drugStats.currentDose();
            DoseState doseState = DoseManager.resolveState(DosePath.DRUG, dose);
            if (doseState != DoseState.VERY_HIGH && doseState != DoseState.OVERDOSE) {
                continue;
            }

            float dosePressure = dosePressure(dose);
            float threshold = threshold(dosePressure);
            float stressPressure = AddictionMath.clamp(
                    (stress - threshold) / AddictionConstants.BAD_TRIP_STRESS_PRESSURE_RANGE,
                    0.0F,
                    1.0F
            );
            float severity = AddictionMath.clamp(
                    stressPressure * AddictionConstants.BAD_TRIP_STRESS_PRESSURE_WEIGHT
                            + dosePressure * AddictionConstants.BAD_TRIP_DOSE_PRESSURE_WEIGHT,
                    0.0F,
                    1.0F
            );

            Candidate candidate = new Candidate(drugId, category, stress, threshold, severity, dosePressure, dose);
            if (best == null
                    || candidate.severity() > best.severity()
                    || candidate.severity() == best.severity() && candidate.dose() > best.dose()) {
                best = candidate;
            }
        }
        return best;
    }

    private static boolean allowsBadTrip(DrugCategory category) {
        return switch (category) {
            case PSYCHEDELIC, CANNABINOID, DISSOCIATIVE, STIMULANT -> true;
            default -> false;
        };
    }

    private static float dosePressure(float dose) {
        float multiplier = Config.SERVER.overdoseThresholdMultiplier.get().floatValue();
        float veryHigh = DoseConstants.VERY_HIGH_THRESHOLD * multiplier;
        float overdose = DoseConstants.OVERDOSE_THRESHOLD * multiplier;
        if (overdose <= veryHigh) {
            return dose >= overdose ? 1.0F : 0.0F;
        }
        return AddictionMath.clamp((dose - veryHigh) / (overdose - veryHigh), 0.0F, 1.0F);
    }

    private static float threshold(float dosePressure) {
        return AddictionConstants.BAD_TRIP_THRESHOLD_AT_VERY_HIGH
                + (AddictionConstants.BAD_TRIP_THRESHOLD_AT_OVERDOSE - AddictionConstants.BAD_TRIP_THRESHOLD_AT_VERY_HIGH)
                * AddictionMath.clamp(dosePressure, 0.0F, 1.0F);
    }

    private static int severityBand(float severity) {
        if (severity >= AddictionConstants.BAD_TRIP_VIOLENT_THRESHOLD) {
            return 2;
        }
        if (severity >= AddictionConstants.BAD_TRIP_STRONG_THRESHOLD) {
            return 1;
        }
        return 0;
    }

    private static float rerollSymptomIntensity(ServerPlayer player) {
        return AddictionConstants.BAD_TRIP_SYMPTOM_INTENSITY_MIN
                + player.getRandom().nextFloat()
                * (AddictionConstants.BAD_TRIP_SYMPTOM_INTENSITY_MAX - AddictionConstants.BAD_TRIP_SYMPTOM_INTENSITY_MIN);
    }

    private record Candidate(
            DrugId drugId,
            DrugCategory category,
            float stress,
            float threshold,
            float severity,
            float dosePressure,
            float dose
    ) {
    }
}
