package org.mydrugs.mydrugs.core.drug.runtime;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.addiction.network.DrugEffectCueKind;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ServerInputDisruptionManager {
    private static final int MIN_COOLDOWN_TICKS = 35;
    private static final int COOLDOWN_VARIANCE_TICKS = 45;
    private static final int MIN_WINDOW_TICKS = 5;
    private static final float MAX_ACTION_CANCEL_CHANCE = 0.48F;
    private static final ResourceLocation MOVEMENT_ID =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "input_fail_movement_disruption");

    private static final Map<UUID, State> STATES = new HashMap<>();

    private ServerInputDisruptionManager() {
    }

    public static void tick(ServerPlayer player, float intensity) {
        if (player == null) {
            return;
        }
        UUID id = player.getUUID();
        State state = STATES.get(id);
        if (intensity <= 0.001F) {
            if (state != null) {
                cleanup(player);
            }
            return;
        }

        if (state == null) {
            state = new State();
            STATES.put(id, state);
        }
        state.intensity = Math.clamp(intensity, 0.0F, 1.5F);

        if (state.cooldownTicks > 0) {
            state.cooldownTicks--;
        }
        if (state.disruptionTicks > 0) {
            state.disruptionTicks--;
            applyPenalty(player, state.intensity);
            player.setSprinting(false);
            player.getFoodData().addExhaustion(0.015F + state.intensity * 0.015F);
            if (state.disruptionTicks <= 0) {
                removePenalty(player);
            }
            return;
        }

        removePenalty(player);
        if (state.cooldownTicks <= 0 && player.getRandom().nextFloat() < startChance(state.intensity)) {
            state.disruptionTicks = MIN_WINDOW_TICKS + Math.round(state.intensity * 8.0F);
            state.cooldownTicks = MIN_COOLDOWN_TICKS + player.getRandom().nextInt(COOLDOWN_VARIANCE_TICKS);
            DrugEffectRuntimeManager.sendCue(player, EffectType.INPUT_FAIL, DrugEffectCueKind.INPUT_FAIL, state.intensity);
        }
    }

    public static boolean maybeCancelAction(ServerPlayer player) {
        State state = player == null ? null : STATES.get(player.getUUID());
        if (state == null || state.disruptionTicks <= 0) {
            return false;
        }
        float chance = Math.min(MAX_ACTION_CANCEL_CHANCE, 0.18F + state.intensity * 0.22F);
        return player.getRandom().nextFloat() < chance;
    }

    public static InteractionResult disruptedResult() {
        return InteractionResult.FAIL;
    }

    public static void cleanup(ServerPlayer player) {
        if (player == null) {
            return;
        }
        STATES.remove(player.getUUID());
        removePenalty(player);
    }

    public static void clearAll() {
        STATES.clear();
    }

    private static float startChance(float intensity) {
        return Math.min(0.08F, 0.012F + intensity * 0.032F);
    }

    private static void applyPenalty(ServerPlayer player, float intensity) {
        var instance = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance == null) {
            return;
        }
        double penalty = -Math.min(0.55D, 0.18D + intensity * 0.22D);
        instance.addOrUpdateTransientModifier(new AttributeModifier(
                MOVEMENT_ID,
                penalty,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        ));
    }

    private static void removePenalty(ServerPlayer player) {
        var instance = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance != null) {
            instance.removeModifier(MOVEMENT_ID);
        }
    }

    private static final class State {
        int disruptionTicks;
        int cooldownTicks;
        float intensity;
    }
}
