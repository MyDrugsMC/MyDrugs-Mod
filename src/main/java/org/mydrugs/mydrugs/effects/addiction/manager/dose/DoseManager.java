package org.mydrugs.mydrugs.effects.addiction.manager.dose;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.damage.ModDamageTypes;
import org.mydrugs.mydrugs.effects.addiction.config.DoseConstants;
import org.mydrugs.mydrugs.effects.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.dose.DoseContribution;
import org.mydrugs.mydrugs.effects.addiction.dose.DosePath;
import org.mydrugs.mydrugs.effects.addiction.dose.DoseState;

import java.util.Iterator;

/**
 * Manages the dose lifecycle:
 *   1. On consume: add a {@link DoseContribution} that linearly decays to 0 over the
 *      drug's longest effect duration — multiple consumes simply stack contributions.
 *   2. Per tick: advance all contributions, remove expired ones, resolve the
 *      {@link DoseState}, detect threshold crossings and send action-bar messages.
 *   3. Apply state-specific server effects (ETHYLIC_COMA pose, OVERDOSE death timer).
 *      All visual effects are removed — the client shader system driven by
 *      {@link org.mydrugs.mydrugs.effects.addiction.network.DoseSyncPayload} handles visuals.
 */
public final class DoseManager {
    private DoseManager() {}

    // -------------------------------------------------------------------------
    // Consume
    // -------------------------------------------------------------------------

    /**
     * Called from {@code AddictionManager.consume} when a drug is used.
     * Adds a contribution sized by {@code strategy.getNewDose(1.0f)} that decays
     * over the drug's longest effect duration.
     */
    public static void onConsume(DrugAddictionStats stats,
                                 DrugModel model,
                                 @Nullable ConsumptionStrategy strategy) {
        if (DosePath.of(model.getDrugCategory()) == DosePath.NONE) return;

        float doseAmount = (strategy != null) ? strategy.getNewDose(1.0f) : 1.0f;

        int duration = 0;
        for (DrugEffect effect : model.getDrugEffects()) {
            int d = (strategy != null) ? strategy.getNewDuration(effect) : effect.getBaseDuration();
            if (d > duration) duration = d;
        }
        if (duration <= 0) duration = DoseConstants.DEFAULT_ABSORPTION_TICKS;

        stats.doseContributions.add(new DoseContribution(doseAmount, duration));
    }

    // -------------------------------------------------------------------------
    // Per-tick
    // -------------------------------------------------------------------------

    /** Runs every tick per-category from {@code AddictionManager.tickPlayer}. */
    public static void tickCategory(ServerPlayer player,
                                    PlayerAddictionStats playerStats,
                                    DrugCategory category) {
        DosePath path = DosePath.of(category);
        if (path == DosePath.NONE) return;

        DrugAddictionStats stats = playerStats.get(category);

        // Advance every contribution and remove expired ones.
        Iterator<DoseContribution> iter = stats.doseContributions.iterator();
        while (iter.hasNext()) {
            DoseContribution c = iter.next();
            c.ticksRemaining--;
            if (c.isExpired()) iter.remove();
        }

        float dose = stats.currentDose();
        DoseState state = resolveState(path, dose);

        // Detect threshold crossing → send action-bar message once per transition.
        if (state != stats.lastDoseState) {
            sendStateChangeMessage(player, path, stats.lastDoseState, state);
            stats.lastDoseState = state;
        }

        applyStateEffects(player, playerStats, path, state);
    }

    /** Single source of truth: dose + path → state. */
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
                                          DoseState state) {
        // Visual effects (nausea, slowness, blindness, speed) removed — handled by client shaders.

        // Ethylic coma: force sleeping pose so the player lies on the floor.
        if (path == DosePath.ALCOHOL && state == DoseState.ETHYLIC_COMA) {
            player.setPose(Pose.SLEEPING);
        }

        // Overdose: start (or keep) the shared death timer.
        if (path == DosePath.DRUG && state == DoseState.OVERDOSE) {
            if (playerStats.overdoseDeathTimer < 0) {
                playerStats.overdoseDeathTimer = DoseConstants.OVERDOSE_DEATH_TICKS;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Overdose timer (called once per player tick after the per-category loop)
    // -------------------------------------------------------------------------

    public static void tickOverdoseTimer(ServerPlayer player, PlayerAddictionStats playerStats) {
        boolean anyOverdosing = false;
        for (DrugCategory category : DrugCategory.values()) {
            if (DosePath.of(category) != DosePath.DRUG) continue;
            if (playerStats.get(category).currentDose() >= DoseConstants.OVERDOSE_THRESHOLD) {
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

    // -------------------------------------------------------------------------
    // Antidote
    // -------------------------------------------------------------------------

    /**
     * Reduces total dose for all DRUG-path categories by {@code ANTIDOTE_DOSE_REDUCTION}
     * and cancels the overdose death timer.
     */
    public static void applyAntidote(PlayerAddictionStats playerStats) {
        for (DrugCategory category : DrugCategory.values()) {
            if (DosePath.of(category) != DosePath.DRUG) continue;
            DrugAddictionStats stats = playerStats.get(category);

            float toRemove = DoseConstants.ANTIDOTE_DOSE_REDUCTION;
            Iterator<DoseContribution> iter = stats.doseContributions.iterator();
            while (iter.hasNext() && toRemove > 0f) {
                DoseContribution c = iter.next();
                float cv = c.currentValue();
                if (cv <= toRemove) {
                    toRemove -= cv;
                    iter.remove();
                } else {
                    // Partially drain: back-calculate new ticksRemaining.
                    float newValue = cv - toRemove;
                    c.ticksRemaining = (int) (newValue * c.totalDuration / c.amount);
                    toRemove = 0f;
                }
            }
        }
        playerStats.overdoseDeathTimer = -1;
    }

    // -------------------------------------------------------------------------
    // State-change messages
    // -------------------------------------------------------------------------

    private static void sendStateChangeMessage(ServerPlayer player,
                                               DosePath path,
                                               DoseState from,
                                               DoseState to) {
        String key = messageKey(path, from, to);
        if (key != null) {
            player.displayClientMessage(Component.translatable(key), true);
        }
    }

    private static @Nullable String messageKey(DosePath path, DoseState from, DoseState to) {
        String transition = from.name() + "_" + to.name();
        if (path == DosePath.ALCOHOL) {
            return switch (transition) {
                case "NORMAL_DRUNK"            -> "mydrugs.dose.alcohol.normal_to_drunk";
                case "DRUNK_VERY_DRUNK"        -> "mydrugs.dose.alcohol.drunk_to_very_drunk";
                case "VERY_DRUNK_ETHYLIC_COMA" -> "mydrugs.dose.alcohol.very_drunk_to_ethylic_coma";
                case "ETHYLIC_COMA_VERY_DRUNK" -> "mydrugs.dose.alcohol.ethylic_coma_to_very_drunk";
                case "VERY_DRUNK_DRUNK"        -> "mydrugs.dose.alcohol.very_drunk_to_drunk";
                case "DRUNK_NORMAL"            -> "mydrugs.dose.alcohol.drunk_to_normal";
                default -> null;
            };
        }
        if (path == DosePath.DRUG) {
            return switch (transition) {
                case "NORMAL_HIGH"        -> "mydrugs.dose.drug.normal_to_high";
                case "HIGH_VERY_HIGH"     -> "mydrugs.dose.drug.high_to_very_high";
                case "VERY_HIGH_OVERDOSE" -> "mydrugs.dose.drug.very_high_to_overdose";
                case "OVERDOSE_VERY_HIGH" -> "mydrugs.dose.drug.overdose_to_very_high";
                case "VERY_HIGH_HIGH"     -> "mydrugs.dose.drug.very_high_to_high";
                case "HIGH_NORMAL"        -> "mydrugs.dose.drug.high_to_normal";
                default -> null;
            };
        }
        return null;
    }
}
