package org.mydrugs.mydrugs.effects.addiction.manager.dose;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
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

public final class DoseManager {
    private DoseManager() {
    }

    public static void onConsume(DrugAddictionStats stats,
                                 DrugModel model,
                                 @Nullable ConsumptionStrategy strategy) {
        if (DosePath.of(model.getDrugCategory()) == DosePath.NONE) {
            return;
        }

        float doseAmount = (strategy != null) ? strategy.getNewDose(1.0f) : 1.0f;

        int duration = 0;
        for (DrugEffect effect : model.getDrugEffects()) {
            int d = (strategy != null) ? strategy.getNewDuration(effect) : effect.getBaseDuration();
            if (d > duration) {
                duration = d;
            }
        }

        if (duration <= 0) {
            duration = DoseConstants.DEFAULT_ABSORPTION_TICKS;
        }

        stats.doseContributions.add(new DoseContribution(doseAmount, duration));
    }

    public static void tickDrug(ServerPlayer player,
                                PlayerAddictionStats playerStats,
                                DrugId drugId) {
        DrugAddictionStats stats = playerStats.getDrugStats(drugId);
        if (stats == null) {
            return;
        }

        DosePath path = DosePath.of(DrugRegistry.getCategory(drugId));
        if (path == DosePath.NONE) {
            return;
        }

        Iterator<DoseContribution> iter = stats.doseContributions.iterator();
        while (iter.hasNext()) {
            DoseContribution c = iter.next();
            c.ticksRemaining--;
            if (c.isExpired()) {
                iter.remove();
            }
        }

        float dose = stats.currentDose();
        DoseState state = resolveState(path, dose);

        if (state != stats.lastDoseState) {
            sendStateChangeMessage(player, path, stats.lastDoseState, state);
            stats.lastDoseState = state;
        }

        applyStateEffects(player, playerStats, path, state);
    }

    public static DoseState resolveState(DosePath path, float dose) {
        if (path == DosePath.ALCOHOL) {
            if (dose >= DoseConstants.ETHYLIC_COMA_THRESHOLD) return DoseState.ETHYLIC_COMA;
            if (dose >= DoseConstants.VERY_DRUNK_THRESHOLD) return DoseState.VERY_DRUNK;
            if (dose >= DoseConstants.DRUNK_THRESHOLD) return DoseState.DRUNK;
            return DoseState.NORMAL;
        }

        if (path == DosePath.DRUG) {
            if (dose >= DoseConstants.OVERDOSE_THRESHOLD) return DoseState.OVERDOSE;
            if (dose >= DoseConstants.VERY_HIGH_THRESHOLD) return DoseState.VERY_HIGH;
            if (dose >= DoseConstants.HIGH_THRESHOLD) return DoseState.HIGH;
            return DoseState.NORMAL;
        }

        return DoseState.NORMAL;
    }

    private static void applyStateEffects(ServerPlayer player,
                                          PlayerAddictionStats playerStats,
                                          DosePath path,
                                          DoseState state) {
        if (path == DosePath.ALCOHOL && state == DoseState.ETHYLIC_COMA) {
            player.setPose(Pose.SLEEPING);
        }

        if (path == DosePath.DRUG && state == DoseState.OVERDOSE) {
            if (playerStats.overdoseDeathTimer < 0) {
                playerStats.overdoseDeathTimer = DoseConstants.OVERDOSE_DEATH_TICKS;
            }
        }
    }

    public static void tickOverdoseTimer(ServerPlayer player, PlayerAddictionStats playerStats) {
        boolean anyOverdosing = false;

        for (var entry : playerStats.getAllDrugStats().entrySet()) {
            if (DosePath.of(DrugRegistry.getCategory(entry.getKey())) != DosePath.DRUG) {
                continue;
            }

            if (entry.getValue().currentDose() >= DoseConstants.OVERDOSE_THRESHOLD) {
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

    public static void applyAntidote(PlayerAddictionStats playerStats) {
        for (DrugId drugId : playerStats.getTrackedDrugIds()) {
            if (DosePath.of(DrugRegistry.getCategory(drugId)) != DosePath.DRUG) {
                continue;
            }

            DrugAddictionStats stats = playerStats.getDrugStats(drugId);
            if (stats == null) {
                continue;
            }

            float toRemove = DoseConstants.ANTIDOTE_DOSE_REDUCTION;
            Iterator<DoseContribution> iter = stats.doseContributions.iterator();
            while (iter.hasNext() && toRemove > 0f) {
                DoseContribution c = iter.next();
                float currentValue = c.currentValue();

                if (currentValue <= toRemove) {
                    toRemove -= currentValue;
                    iter.remove();
                } else {
                    float newValue = currentValue - toRemove;
                    c.ticksRemaining = (int) (newValue * c.totalDuration / c.amount);
                    toRemove = 0f;
                }
            }
        }

        playerStats.overdoseDeathTimer = -1;
    }

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
                case "NORMAL_DRUNK" -> "mydrugs.dose.alcohol.normal_to_drunk";
                case "DRUNK_VERY_DRUNK" -> "mydrugs.dose.alcohol.drunk_to_very_drunk";
                case "VERY_DRUNK_ETHYLIC_COMA" -> "mydrugs.dose.alcohol.very_drunk_to_ethylic_coma";
                case "ETHYLIC_COMA_VERY_DRUNK" -> "mydrugs.dose.alcohol.ethylic_coma_to_very_drunk";
                case "VERY_DRUNK_DRUNK" -> "mydrugs.dose.alcohol.very_drunk_to_drunk";
                case "DRUNK_NORMAL" -> "mydrugs.dose.alcohol.drunk_to_normal";
                default -> null;
            };
        }

        if (path == DosePath.DRUG) {
            return switch (transition) {
                case "NORMAL_HIGH" -> "mydrugs.dose.drug.normal_to_high";
                case "HIGH_VERY_HIGH" -> "mydrugs.dose.drug.high_to_very_high";
                case "VERY_HIGH_OVERDOSE" -> "mydrugs.dose.drug.very_high_to_overdose";
                case "OVERDOSE_VERY_HIGH" -> "mydrugs.dose.drug.overdose_to_very_high";
                case "VERY_HIGH_HIGH" -> "mydrugs.dose.drug.very_high_to_high";
                case "HIGH_NORMAL" -> "mydrugs.dose.drug.high_to_normal";
                default -> null;
            };
        }

        return null;
    }
}