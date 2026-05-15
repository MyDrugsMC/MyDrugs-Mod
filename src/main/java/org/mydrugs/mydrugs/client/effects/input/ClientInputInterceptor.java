package org.mydrugs.mydrugs.client.effects.input;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.client.effects.AddictionClientState;
import org.mydrugs.mydrugs.addiction.config.SymptomFlags;

public final class ClientInputInterceptor {
    private static final Random RANDOM = new Random();
    private static final VarHandle MOVE_VECTOR_HANDLE = resolveMoveVectorHandle();

    private static int failCooldownTicks;
    private static int failTicks;

    private ClientInputInterceptor() {
    }

    public static void tick(Minecraft mc) {
        if (failCooldownTicks > 0) {
            failCooldownTicks--;
        }
        if (failTicks > 0) {
            failTicks--;
        }

        float inputFail = AddictionClientState.getEffectIntensity(EffectType.INPUT_FAIL);
        if (inputFail > 0.0F && failCooldownTicks <= 0) {
            float chance = Math.min(0.25F, 0.015F + inputFail * 0.035F);
            if (RANDOM.nextFloat() < chance) {
                failTicks = 4 + Math.round(inputFail * 6.0F);
                failCooldownTicks = 25 + RANDOM.nextInt(30);
            }
        }
    }

    public static void applyToInput(ClientInput input, int tickCount) {
        if (input == null) {
            return;
        }

        Vec2 moveVector = input.getMoveVector();
        if (moveVector == null) {
            moveVector = Vec2.ZERO;
        }

        if (failTicks > 0) {
            moveVector = new Vec2(moveVector.x * 0.20F, moveVector.y * 0.20F);
            suppressJumpAndSprint(input);
        }

        float multiplier = getMovementMultiplier();
        if (Math.abs(multiplier - 1.0F) > 0.001F) {
            moveVector = new Vec2(moveVector.x * multiplier, moveVector.y * multiplier);
        }

        float stumble = AddictionClientState.getEffectIntensity(EffectType.STUMBLE);
        if (stumble > 0.0F && !Config.CLIENT.reducedMotionMode.get()) {
            double t = tickCount * 0.17D;
            float sideways = (float) Math.sin(t) * Math.min(0.55F, stumble * 0.35F);
            moveVector = new Vec2(
                    clamp(moveVector.x + sideways * Math.max(0.25F, Math.abs(moveVector.y)), -1.0F, 1.0F),
                    clamp(moveVector.y, -1.0F, 1.0F)
            );
        }

        setMoveVector(input, moveVector);
    }

    public static boolean shouldFailInput() {
        float custom = AddictionClientState.getEffectIntensity(EffectType.INPUT_FAIL);
        if (custom > 0.0F) {
            float chance = Math.min(0.20F, 0.02F + custom * 0.08F);
            return RANDOM.nextFloat() < chance;
        }

        if (!AddictionClientState.has(SymptomFlags.CONFUSION)) return false;

        float severity = AddictionClientState.globalSeverity;
        float chance = 0.08F + severity * 0.12F;
        return RANDOM.nextFloat() < chance;
    }

    public static float getMovementMultiplier() {
        float multiplier = 1.0F;
        multiplier += AddictionClientState.getEffectIntensity(EffectType.MOVEMENT_SPEED);
        multiplier -= AddictionClientState.getEffectIntensity(EffectType.MOVEMENT_SLOWDOWN);

        if (AddictionClientState.has(SymptomFlags.FATIGUE)) {
            multiplier -= Math.min(0.20F, AddictionClientState.globalSeverity * 0.20F);
        }

        return Math.max(0.05F, multiplier);
    }

    private static void suppressJumpAndSprint(ClientInput input) {
        Input keyPresses = input.keyPresses == null ? Input.EMPTY : input.keyPresses;
        input.keyPresses = new Input(
                keyPresses.forward(),
                keyPresses.backward(),
                keyPresses.left(),
                keyPresses.right(),
                false,
                keyPresses.shift(),
                false
        );
    }

    private static void setMoveVector(ClientInput input, Vec2 moveVector) {
        if (MOVE_VECTOR_HANDLE != null) {
            MOVE_VECTOR_HANDLE.set(input, moveVector);
        }
    }

    private static VarHandle resolveMoveVectorHandle() {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(ClientInput.class, MethodHandles.lookup());
            return lookup.findVarHandle(ClientInput.class, "moveVector", Vec2.class);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
