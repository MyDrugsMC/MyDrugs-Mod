package org.mydrugs.mydrugs.machine.manual;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.manager.effect.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.mutation.MutationManager;
import org.mydrugs.mydrugs.mutation.MutationStat;

public final class ManualMachineSpeedHelper {
    private ManualMachineSpeedHelper() {
    }

    public static float getSpeedMultiplier(ServerPlayer player, ManualMachineType type) {
        float multiplier = 1.0F;
        multiplier += bonus(player, EffectType.MANUAL_WORK_SPEED, 1.0F);
        multiplier += bonus(player, EffectType.FOCUS, 0.35F);
        multiplier += bonus(player, EffectType.PRECISION, precisionScale(type));
        multiplier += bonus(player, EffectType.ADRENALINE_SURGE, 0.65F);
        return Mth.clamp(multiplier, 0.25F, 4.0F);
    }

    public static float getRitualInstabilityReduction(ServerPlayer player) {
        float stability = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.RITUAL_STABILITY);
        float focus = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.RITUAL_FOCUS);
        if (hasPsychedelicRitualInsight(player)) {
            return 0.95F;
        }
        float ritualSync = MutationManager.getValue(player, MutationStat.RITUAL_NEURAL_SYNC);
        float mutationBonus = ritualSync * 0.20F;
        return Mth.clamp(stability * 0.18F + focus * 0.06F + mutationBonus, 0.0F, 0.75F);
    }

    public static boolean hasPsychedelicRitualInsight(ServerPlayer player) {
        return DrugEffectRuntimeManager.getServerIntensity(player, EffectType.RITUAL_FOCUS) >= 1.5F;
    }

    public static float getRitualZoneWidthBonus(ServerPlayer player) {
        if (hasPsychedelicRitualInsight(player)) {
            return 1.0F;
        }
        float focus = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.RITUAL_FOCUS);
        float precision = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.PRECISION);
        float ritualSync = MutationManager.getValue(player, MutationStat.RITUAL_NEURAL_SYNC);
        return Mth.clamp(focus * 0.10F + precision * 0.18F + ritualSync * 0.10F, 0.0F, 0.32F);
    }

    public static float getRitualZoneMotionScale(ServerPlayer player) {
        float stability = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.RITUAL_STABILITY);
        return Mth.clamp(1.0F / (1.0F + stability * 1.35F), 0.25F, 1.0F);
    }

    private static float bonus(ServerPlayer player, EffectType type, float scale) {
        return Math.max(0.0F, DrugEffectRuntimeManager.getServerIntensity(player, type)) * scale;
    }

    private static float precisionScale(ManualMachineType type) {
        return switch (type) {
            case PSY_MIXER -> 0.35F;
            case GRINDING_BOWL, SIEVE, STOMP_CRAFTER -> 0.20F;
            default -> 0.12F;
        };
    }
}
