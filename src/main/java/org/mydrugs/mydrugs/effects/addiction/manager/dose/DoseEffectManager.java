package org.mydrugs.mydrugs.effects.addiction.manager.dose;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.dose.DoseState;
import org.mydrugs.mydrugs.effects.addiction.manager.effect.DrugEffectRuntimeManager;

public final class DoseEffectManager {
    private static final int REFRESH_TICKS = 45;

    private DoseEffectManager() {
    }

    public static void tickDrug(ServerPlayer player, DrugId drugId, float dose, DoseState state) {
        if (dose <= 0.01F || state == DoseState.NORMAL) {
            return;
        }
        if (player.level().getGameTime() % 10L != 0L) {
            return;
        }

        DrugCategory category = DrugRegistry.getCategory(drugId);
        float stateScale = stateScale(state);
        float doseScale = Math.min(1.0F, Math.max(0.0F, dose / 6.0F));
        float intensity = Math.max(stateScale, doseScale);

        switch (category) {
            case CAFFEINE -> caffeine(player, state, intensity);
            case STIMULANT, EMPATHOGEN -> stimulant(player, state, intensity);
            case CANNABINOID -> cannabinoid(player, state, intensity);
            case DEPRESSANT -> depressant(player, state, intensity);
            case OPIOID -> opioid(player, state, intensity);
            case PSYCHEDELIC -> psychedelic(player, state, intensity);
            case DISSOCIATIVE -> dissociative(player, state, intensity);
            case NICOTINIC -> nicotine(player, state, intensity);
            default -> generic(player, state, intensity);
        }
    }

    private static float stateScale(DoseState state) {
        return switch (state) {
            case HIGH, DRUNK -> 0.35F;
            case VERY_HIGH, VERY_DRUNK -> 0.65F;
            case OVERDOSE, ETHYLIC_COMA -> 1.0F;
            case NORMAL -> 0.0F;
        };
    }

    private static void add(ServerPlayer player, EffectType type, float intensity) {
        if (intensity <= 0.0F) return;
        DrugEffectRuntimeManager.addEffect(player, type, intensity, REFRESH_TICKS);
    }

    private static void caffeine(ServerPlayer player, DoseState state, float i) {
        add(player, EffectType.MINING_SPEED, 0.02F + i * 0.04F);
        add(player, EffectType.MOVEMENT_SPEED, 0.02F + i * 0.05F);
        if (atLeast(state, DoseState.VERY_HIGH)) {
            add(player, EffectType.HEARTBEAT, 0.25F + i * 0.55F);
            add(player, EffectType.TREMOR, 0.10F + i * 0.35F);
            add(player, EffectType.INPUT_FAIL, i * 0.12F);
            add(player, EffectType.BLUR, i * 0.10F);
            add(player, EffectType.VOMIT, i * 0.10F);
        }
        if (state == DoseState.OVERDOSE) {
            add(player, EffectType.CONFUSION, 0.35F + i * 0.45F);
            add(player, EffectType.CAMERA_SWAY, i * 0.25F);
            add(player, EffectType.VOMIT, i * 0.35F);
        }
    }

    private static void stimulant(ServerPlayer player, DoseState state, float i) {
        add(player, EffectType.MOVEMENT_SPEED, 0.05F + i * 0.18F);
        add(player, EffectType.MINING_SPEED, 0.04F + i * 0.10F);
        add(player, EffectType.HEARTBEAT, i * 0.55F);
        if (atLeast(state, DoseState.VERY_HIGH)) {
            add(player, EffectType.INPUT_FAIL, i * 0.22F);
            add(player, EffectType.TREMOR, i * 0.55F);
            add(player, EffectType.BLUR, i * 0.22F);
            add(player, EffectType.CAMERA_SWAY, i * 0.28F);
            add(player, EffectType.VOMIT, i * 0.20F);
        }
        if (state == DoseState.OVERDOSE) {
            add(player, EffectType.CONFUSION, i * 0.70F);
            add(player, EffectType.INPUT_FAIL, i * 0.45F);
        }
    }

    private static void cannabinoid(ServerPlayer player, DoseState state, float i) {
        add(player, EffectType.GAMMA_BOOST, i * 0.20F);
        add(player, EffectType.CAMERA_SWAY, i * 0.08F);
        if (atLeast(state, DoseState.VERY_HIGH)) {
            add(player, EffectType.STUMBLE, i * 0.25F);
            add(player, EffectType.INPUT_FAIL, i * 0.16F);
            add(player, EffectType.BLUR, i * 0.24F);
            add(player, EffectType.CUSTOM_NAUSEA, i * 0.12F);
        }
    }

    private static void depressant(ServerPlayer player, DoseState state, float i) {
        add(player, EffectType.STUMBLE, i * 0.35F);
        add(player, EffectType.CAMERA_SWAY, i * 0.45F);
        add(player, EffectType.INPUT_FAIL, i * 0.25F);
        add(player, EffectType.BLUR, i * 0.30F);
        if (state == DoseState.VERY_DRUNK || state == DoseState.ETHYLIC_COMA) {
            add(player, EffectType.MOVEMENT_SLOWDOWN, i * 0.35F);
            add(player, EffectType.VOMIT, i * 0.35F);
        }
        if (state == DoseState.ETHYLIC_COMA) {
            add(player, EffectType.MOVEMENT_SLOWDOWN, 0.90F);
            add(player, EffectType.BLUR, 0.95F);
            add(player, EffectType.INPUT_FAIL, 0.75F);
        }
    }

    private static void opioid(ServerPlayer player, DoseState state, float i) {
        add(player, EffectType.DAMAGE_RESISTANCE, Math.min(0.50F, 0.10F + i * 0.25F));
        add(player, EffectType.MOVEMENT_SLOWDOWN, i * 0.20F);
        add(player, EffectType.BLUR, i * 0.22F);
        if (atLeast(state, DoseState.VERY_HIGH)) {
            add(player, EffectType.INPUT_FAIL, i * 0.25F);
            add(player, EffectType.CAMERA_SWAY, i * 0.15F);
        }
        if (state == DoseState.OVERDOSE) {
            add(player, EffectType.MOVEMENT_SLOWDOWN, 0.75F);
            add(player, EffectType.BLUR, 0.85F);
            add(player, EffectType.CONFUSION, i * 0.55F);
        }
    }

    private static void psychedelic(ServerPlayer player, DoseState state, float i) {
        add(player, EffectType.GAMMA_BOOST, i * 0.35F);
        add(player, EffectType.CAMERA_SWAY, i * 0.20F);
        if (atLeast(state, DoseState.VERY_HIGH)) {
            add(player, EffectType.CONFUSION, i * 0.55F);
            add(player, EffectType.INPUT_FAIL, i * 0.25F);
            add(player, EffectType.BLUR, i * 0.30F);
            add(player, EffectType.CUSTOM_NAUSEA, i * 0.18F);
        }
    }

    private static void dissociative(ServerPlayer player, DoseState state, float i) {
        add(player, EffectType.DAMAGE_RESISTANCE, Math.min(0.45F, i * 0.28F));
        add(player, EffectType.CAMERA_SWAY, i * 0.30F);
        add(player, EffectType.MOVEMENT_SLOWDOWN, i * 0.18F);
        if (atLeast(state, DoseState.VERY_HIGH)) {
            add(player, EffectType.STUMBLE, i * 0.45F);
            add(player, EffectType.INPUT_FAIL, i * 0.30F);
            add(player, EffectType.BLUR, i * 0.35F);
            add(player, EffectType.CONFUSION, i * 0.45F);
        }
    }

    private static void nicotine(ServerPlayer player, DoseState state, float i) {
        add(player, EffectType.PRECISION, 0.08F + i * 0.18F);
        if (atLeast(state, DoseState.HIGH)) {
            add(player, EffectType.HEARTBEAT, i * 0.20F);
            add(player, EffectType.CUSTOM_NAUSEA, i * 0.12F);
            add(player, EffectType.BLUR, i * 0.08F);
        }
    }

    private static void generic(ServerPlayer player, DoseState state, float i) {
        if (atLeast(state, DoseState.VERY_HIGH)) {
            add(player, EffectType.BLUR, i * 0.20F);
            add(player, EffectType.INPUT_FAIL, i * 0.15F);
        }
    }

    private static boolean atLeast(DoseState actual, DoseState threshold) {
        return severity(actual) >= severity(threshold);
    }

    private static int severity(DoseState state) {
        return switch (state) {
            case NORMAL -> 0;
            case HIGH, DRUNK -> 1;
            case VERY_HIGH, VERY_DRUNK -> 2;
            case OVERDOSE, ETHYLIC_COMA -> 3;
        };
    }
}
