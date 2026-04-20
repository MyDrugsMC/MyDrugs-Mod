package org.mydrugs.mydrugs.effects.addiction.manager.dose;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.damage.ModDamageTypes;
import org.mydrugs.mydrugs.effects.addiction.config.DoseConstants;
import org.mydrugs.mydrugs.effects.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.dose.DosePath;
import org.mydrugs.mydrugs.effects.addiction.dose.DoseState;

/**
 * Runs the dose lifecycle for each drug category on every player tick:
 *   1. decay targetDose linearly over time,
 *   2. lerp currentDose toward targetDose at the absorption rate (set on consume),
 *   3. resolve the {@link DoseState} from currentDose + {@link DosePath},
 *   4. apply scaled mob effects for that state,
 *   5. manage the shared overdose death timer on {@link PlayerAddictionStats}.
 *
 * Effects scale linearly with dose between thresholds (user design: "effects
 * increment with the score, not by thresholds — thresholds add new symptoms").
 */
public final class DoseManager {
    private DoseManager() {}

    /** Called by AddictionManager.consume — bumps the target and sets the absorption rate. */
    public static void onConsume(DrugAddictionStats stats, float doseAmount, int absorptionTicks) {
        stats.targetDose = Math.max(0.0F, stats.targetDose + doseAmount);
        int ticks = Math.max(1, absorptionTicks);
        stats.absorptionRatePerTick = doseAmount / (float) ticks;
    }

    /** Runs every tick per-category from AddictionManager.tickPlayer. */
    public static void tickCategory(ServerPlayer player,
                                    PlayerAddictionStats playerStats,
                                    DrugCategory category) {
        DrugAddictionStats stats = playerStats.get(category);
        DosePath path = DosePath.of(category);

        // (1) Decay targetDose.
        if (stats.targetDose > 0.0F) {
            stats.targetDose = Math.max(0.0F, stats.targetDose - DoseConstants.DOSE_DECAY_PER_TICK);
        }

        // (2) Absorption / catch-up.
        if (stats.currentDose < stats.targetDose) {
            float rate = stats.absorptionRatePerTick > 0.0F
                    ? stats.absorptionRatePerTick
                    : 1.0F / DoseConstants.DEFAULT_ABSORPTION_TICKS;
            stats.currentDose = Math.min(stats.targetDose, stats.currentDose + rate);
        } else if (stats.currentDose > stats.targetDose) {
            // Target has decayed below current — bring current down with it.
            stats.currentDose = stats.targetDose;
        }

        if (stats.currentDose < 0.001F && stats.targetDose < 0.001F) {
            stats.currentDose = 0.0F;
            stats.targetDose = 0.0F;
        }

        // (3) & (4) resolve state and apply effects.
        if (path == DosePath.NONE) return;

        DoseState state = resolveState(path, stats.currentDose);
        applyStateEffects(player, playerStats, path, state, stats.currentDose);
    }

    /** Single source of truth: dose + path -> state. */
    public static DoseState resolveState(DosePath path, float dose) {
        if (path == DosePath.ALCOHOL) {
            if (dose >= DoseConstants.ETHYLIC_COMA_THRESHOLD) return DoseState.ETHYLIC_COMA;
            if (dose >= DoseConstants.VERY_DRUNK_THRESHOLD)   return DoseState.VERY_DRUNK;
            if (dose >= DoseConstants.DRUNK_THRESHOLD)        return DoseState.DRUNK;
            return DoseState.NORMAL;
        }
        if (path == DosePath.DRUG) {
            if (dose >= DoseConstants.OVERDOSE_THRESHOLD)  return DoseState.OVERDOSE;
            if (dose >= DoseConstants.VERY_HIGH_THRESHOLD) return DoseState.VERY_HIGH;
            if (dose >= DoseConstants.HIGH_THRESHOLD)      return DoseState.HIGH;
            return DoseState.NORMAL;
        }
        return DoseState.NORMAL;
    }

    private static void applyStateEffects(ServerPlayer player,
                                          PlayerAddictionStats playerStats,
                                          DosePath path,
                                          DoseState state,
                                          float dose) {
        // Effect instances are refreshed every 40 ticks so they're effectively continuous.
        final int refresh = 40;

        if (path == DosePath.ALCOHOL) {
            // Slowness starts at DRUNK and scales across VERY_DRUNK and beyond.
            if (dose >= DoseConstants.DRUNK_THRESHOLD) {
                int amp = (int) Math.floor((dose - DoseConstants.DRUNK_THRESHOLD) / 2.0F);
                amp = clamp(amp, 0, 3);
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, refresh, amp, true, false));
            }

            // Nausea kicks in at VERY_DRUNK.
            if (dose >= DoseConstants.VERY_DRUNK_THRESHOLD) {
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, refresh, 0, true, false));
            }

            // Ethylic coma: immobile, near-blind, flat on the floor.
            if (state == DoseState.ETHYLIC_COMA) {
                player.setPose(Pose.SLEEPING);
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, refresh, 0, true, false));
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, refresh, 10, true, false));
            }
        }

        if (path == DosePath.DRUG) {
            // Mild high: speed scales gently from HIGH up.
            if (dose >= DoseConstants.HIGH_THRESHOLD) {
                int amp = (int) Math.floor((dose - DoseConstants.HIGH_THRESHOLD) / 4.0F);
                amp = clamp(amp, 0, 1);
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, refresh, amp, true, false));
            }

            // Bad trip: nausea scales across VERY_HIGH.
            if (dose >= DoseConstants.VERY_HIGH_THRESHOLD) {
                int amp = (int) Math.floor((dose - DoseConstants.VERY_HIGH_THRESHOLD) / 2.0F);
                amp = clamp(amp, 0, 2);
                player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, refresh, amp, true, false));
            }

            // Overdose: start (or keep) the shared death timer.
            if (state == DoseState.OVERDOSE) {
                if (playerStats.overdoseDeathTimer < 0) {
                    playerStats.overdoseDeathTimer = DoseConstants.OVERDOSE_DEATH_TICKS;
                }
            }
        }
    }

    /**
     * Global per-tick pass: if any drug-path category is still in OVERDOSE, count the
     * shared death timer down; otherwise cancel it. Called once per player tick from
     * AddictionManager after the per-category loop.
     */
    public static void tickOverdoseTimer(ServerPlayer player, PlayerAddictionStats playerStats) {
        boolean anyOverdosing = false;
        for (DrugCategory category : DrugCategory.values()) {
            if (DosePath.of(category) != DosePath.DRUG) continue;
            if (playerStats.get(category).currentDose >= DoseConstants.OVERDOSE_THRESHOLD) {
                anyOverdosing = true;
                break;
            }
        }

        if (!anyOverdosing) {
            playerStats.overdoseDeathTimer = -1;
            return;
        }

        if (playerStats.overdoseDeathTimer < 0) {
            playerStats.overdoseDeathTimer = DoseConstants.OVERDOSE_DEATH_TICKS;
            return;
        }

        playerStats.overdoseDeathTimer--;
        if (playerStats.overdoseDeathTimer <= 0) {
            player.hurt(ModDamageTypes.overdose(player.level()), Float.MAX_VALUE);
            playerStats.overdoseDeathTimer = -1;
        }
    }

    /**
     * Reduces every drug-path category's dose (current + target) by the antidote amount
     * and cancels the overdose death timer.
     */
    public static void applyAntidote(PlayerAddictionStats playerStats) {
        for (DrugCategory category : DrugCategory.values()) {
            if (DosePath.of(category) != DosePath.DRUG) continue;
            DrugAddictionStats stats = playerStats.get(category);
            stats.targetDose = Math.max(0.0F, stats.targetDose - DoseConstants.ANTIDOTE_DOSE_REDUCTION);
            stats.currentDose = Math.min(stats.currentDose, stats.targetDose);
        }
        playerStats.overdoseDeathTimer = -1;
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
