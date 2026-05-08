package org.mydrugs.mydrugs.machine.manual;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.manager.effect.DrugEffectRuntimeManager;

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

    public static float getRitualTimingBonus(ServerPlayer player) {
        float focus = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.RITUAL_FOCUS);
        float stability = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.RITUAL_STABILITY);
        if (focus >= 1.5F) {
            return 0.88F;
        }
        return Mth.clamp(focus * 0.12F + stability * 0.06F, 0.0F, 0.42F);
    }

    public static float getRitualInstabilityReduction(ServerPlayer player) {
        float stability = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.RITUAL_STABILITY);
        float focus = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.RITUAL_FOCUS);
        if (focus >= 1.5F) {
            return 0.95F;
        }
        return Mth.clamp(stability * 0.18F + focus * 0.06F, 0.0F, 0.55F);
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
