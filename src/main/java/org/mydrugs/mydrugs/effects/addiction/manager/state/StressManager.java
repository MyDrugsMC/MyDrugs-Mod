package org.mydrugs.mydrugs.effects.addiction.manager.state;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.manager.effect.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.effects.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.effects.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.dose.DosePath;
import org.mydrugs.mydrugs.effects.addiction.dose.DoseState;
import org.mydrugs.mydrugs.effects.addiction.manager.dose.DoseManager;
import org.mydrugs.mydrugs.effects.addiction.util.AddictionMath;

public final class StressManager {
    private StressManager() {}

    public static float getStress(ServerPlayer player) {
        return getStress(player.getData(ModAttachments.PLAYER_ADDICTION.get()));
    }

    public static float getStress(PlayerAddictionStats stats) {
        return AddictionMath.clamp(stats.stressLevel, 0.0F, 1.0F);
    }

    public static void setStress(ServerPlayer player, float value) {
        setStress(player.getData(ModAttachments.PLAYER_ADDICTION.get()), value);
    }

    public static void setStress(PlayerAddictionStats stats, float value) {
        stats.stressLevel = AddictionMath.clamp(value, 0.0F, 1.0F);
    }

    public static void addStress(ServerPlayer player, float amount) {
        float resistance = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.STRESS_RESISTANCE);
        float scaled = amount * Math.max(0.0F, 1.0F - resistance);
        addStress(player.getData(ModAttachments.PLAYER_ADDICTION.get()), scaled);
    }

    public static void addStress(PlayerAddictionStats stats, float amount) {
        setStress(stats, stats.stressLevel + Math.max(0.0F, amount));
    }

    public static void reduceStress(ServerPlayer player, float amount) {
        reduceStress(player.getData(ModAttachments.PLAYER_ADDICTION.get()), amount);
    }

    public static void reduceStress(PlayerAddictionStats stats, float amount) {
        setStress(stats, stats.stressLevel - Math.max(0.0F, amount));
    }

    public static void reduce(PlayerAddictionStats stats, float amount) {
        reduceStress(stats, amount);
    }

    public static void tick(ServerPlayer player, PlayerAddictionStats stats, float globalSeverity, boolean inCombat, int companions, boolean inSafeZone) {
        tickStress(player, stats, globalSeverity, inCombat, companions, inSafeZone);
    }

    public static void tickStress(ServerPlayer player, PlayerAddictionStats stats, float globalSeverity, boolean inCombat, int companions, boolean inSafeZone) {
        float target = getStressTarget(player, stats, globalSeverity, inCombat, companions, inSafeZone);
        float current = getStress(stats);
        float delta = target - current;
        if (Math.abs(delta) <= 0.0001F) {
            setStress(stats, target);
            return;
        }

        float rate = delta > 0.0F ? AddictionConstants.STRESS_RISE_RATE : getStressFallRate(player, stats);
        setStress(stats, current + Math.copySign(Math.min(Math.abs(delta), rate), delta));
    }

    public static float getStressTarget(ServerPlayer player, PlayerAddictionStats stats, float globalSeverity, boolean inCombat, int companions, boolean inSafeZone) {
        float target = AddictionConstants.STRESS_BASELINE + globalSeverity * AddictionConstants.STRESS_SEVERITY_SCALE;
        long gameTime = player.level().getGameTime();

        long time = player.level().getDayTime() % 24000L;
        if (time >= 13000L && time < 23000L) target += AddictionConstants.STRESS_NIGHT_BONUS;
        if (inCombat) target += AddictionConstants.STRESS_COMBAT_BONUS;
        if (companions == 0) target += AddictionConstants.STRESS_ALONE_BONUS;
        if (inSafeZone) target -= AddictionConstants.STRESS_SAFE_ZONE_REDUCTION;
        if (stats.temporaryEffects.hasCalmRelief(gameTime)) target -= AddictionConstants.STRESS_DIARY_REDUCTION;
        if (stats.temporaryEffects.hasHeadphones(gameTime)) target -= AddictionConstants.STRESS_HEADPHONES_REDUCTION;
        if (hasActiveCannabis(stats)) target -= AddictionConstants.STRESS_CANNABIS_REDUCTION;

        int food = player.getFoodData().getFoodLevel();

        if (food <= 6) {
            target += 0.10F;
        } else if (food <= 12) {
            target += 0.04F;
        }

        if (food >= 20) {
            target *= 0.90F;
        }

        float healthRatio = player.getHealth() / player.getMaxHealth();
        if (healthRatio <= 0.25F) {
            target += 0.15F;
        } else if (healthRatio <= 0.50F) {
            target += 0.08F;
        }

        target += getDoseStressBonus(stats);

        return AddictionMath.clamp(target, 0.0F, 1.0F);
    }

    public static void onDamage(ServerPlayer player, float finalDamage) {
        addStress(player, AddictionConstants.STRESS_DAMAGE_FLAT_BONUS + finalDamage * AddictionConstants.STRESS_DAMAGE_BONUS_PER_HP);
    }

    public static boolean hasActiveCannabis(PlayerAddictionStats stats) {
        for (DrugId drugId : stats.getTrackedDrugIds()) {
            if (DrugRegistry.getCategory(drugId) != DrugCategory.CANNABINOID) {
                continue;
            }

            DrugAddictionStats drugStats = stats.getDrugStats(drugId);
            if (drugStats != null && drugStats.currentDose() > 0.01F) {
                return true;
            }
        }

        return false;
    }

    private static float getStressFallRate(ServerPlayer player, PlayerAddictionStats stats) {
        float rate = AddictionConstants.STRESS_FALL_RATE;
        long gameTime = player.level().getGameTime();
        if (stats.temporaryEffects.hasHeadphones(gameTime)) {
            rate += AddictionConstants.STRESS_HEADPHONES_EXTRA_FALL_RATE;
        }
        if (hasActiveCannabis(stats)) {
            rate += AddictionConstants.STRESS_CANNABIS_EXTRA_FALL_RATE;
        }
        return rate;
    }

    private static float getDoseStressBonus(PlayerAddictionStats stats) {
        float bonus = 0.0F;
        for (DrugId drugId : stats.getTrackedDrugIds()) {
            DrugAddictionStats drugStats = stats.getDrugStats(drugId);
            if (drugStats == null) {
                continue;
            }

            DrugCategory category = DrugRegistry.getCategory(drugId);
            DosePath path = DosePath.of(category);
            if (path == DosePath.NONE) {
                continue;
            }

            DoseState state = DoseManager.resolveState(path, drugStats.currentDose());
            bonus = Math.max(bonus, switch (state) {
                case HIGH, DRUNK -> AddictionConstants.STRESS_HIGH_DOSE_BONUS;
                case VERY_HIGH, VERY_DRUNK -> AddictionConstants.STRESS_BAD_TRIP_DOSE_BONUS;
                case OVERDOSE, ETHYLIC_COMA -> AddictionConstants.STRESS_OVERDOSE_DOSE_BONUS;
                case NORMAL -> 0.0F;
            });
        }

        return bonus;
    }
}
