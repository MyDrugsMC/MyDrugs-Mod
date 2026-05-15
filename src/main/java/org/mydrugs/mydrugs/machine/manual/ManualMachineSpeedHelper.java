package org.mydrugs.mydrugs.machine.manual;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.manager.effect.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.mutation.MutationManager;
import org.mydrugs.mydrugs.mutation.MutationStat;

public final class ManualMachineSpeedHelper {
    /** Lower bound on the computed multiplier — a player should never grind to a halt from negative state. */
    public static final float MIN_MULTIPLIER = 0.25F;
    /** Upper bound — caps stacking buffs so a fully buffed player cannot one-shot every machine. */
    public static final float MAX_MULTIPLIER = 4.0F;

    private ManualMachineSpeedHelper() {
    }

    public static float getSpeedMultiplier(ServerPlayer player, ManualMachineType type) {
        return computeMultiplier(
                type,
                serverIntensity(player, EffectType.MANUAL_WORK_SPEED),
                serverIntensity(player, EffectType.FOCUS),
                serverIntensity(player, EffectType.PRECISION),
                serverIntensity(player, EffectType.ADRENALINE_SURGE)
        );
    }

    /**
     * Shared formula used by both server (gameplay) and client (GUI hint) sides.
     * Keeping a single source of truth prevents the display from drifting away from
     * the actual speed bonus when the constants are tuned.
     *
     * @return the speed multiplier (1.0 = no bonus), clamped to [{@link #MIN_MULTIPLIER}, {@link #MAX_MULTIPLIER}].
     */
    public static float computeMultiplier(
            ManualMachineType type,
            float manualWorkSpeed,
            float focus,
            float precision,
            float adrenalineSurge
    ) {
        float bonus = computeBonus(type, manualWorkSpeed, focus, precision, adrenalineSurge);
        return Mth.clamp(1.0F + bonus, MIN_MULTIPLIER, MAX_MULTIPLIER);
    }

    /**
     * Raw additive bonus (0.0 = no bonus). Use this when displaying the "+X%" hint
     * separately from the baseline 1.0 multiplier.
     */
    public static float computeBonus(
            ManualMachineType type,
            float manualWorkSpeed,
            float focus,
            float precision,
            float adrenalineSurge
    ) {
        float clampedManual = Math.max(0.0F, manualWorkSpeed);
        float clampedFocus = Math.max(0.0F, focus);
        float clampedPrecision = Math.max(0.0F, precision);
        float clampedAdrenaline = Math.max(0.0F, adrenalineSurge);
        return clampedManual
                + clampedFocus * 0.35F
                + clampedPrecision * precisionScale(type)
                + clampedAdrenaline * 0.65F;
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

    private static float serverIntensity(ServerPlayer player, EffectType type) {
        return DrugEffectRuntimeManager.getServerIntensity(player, type);
    }

    static float precisionScale(ManualMachineType type) {
        return switch (type) {
            case PSY_MIXER -> 0.35F;
            case GRINDING_BOWL, SIEVE, STOMP_CRAFTER -> 0.20F;
            default -> 0.12F;
        };
    }
}
