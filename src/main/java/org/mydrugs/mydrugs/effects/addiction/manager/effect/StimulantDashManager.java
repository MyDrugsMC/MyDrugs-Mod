package org.mydrugs.mydrugs.effects.addiction.manager.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class StimulantDashManager {
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();

    private StimulantDashManager() {
    }

    public static boolean tryDash(ServerPlayer player, float requestedForward, float requestedStrafe) {
        float stimulant = stimulantIntensity(player);
        float dashPower = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.DASH_POWER);
        if (stimulant + dashPower <= 0.08F) {
            return false;
        }

        long gameTime = player.level().getGameTime();
        int cooldownTicks = cooldownTicks(stimulant + dashPower * 0.6F);
        long lastDash = COOLDOWNS.getOrDefault(player.getUUID(), (long) -cooldownTicks);
        if (gameTime - lastDash < cooldownTicks) {
            return false;
        }

        Vec3 direction = movementDirection(player, requestedForward, requestedStrafe);
        if (direction.lengthSqr() < 0.001D) {
            direction = player.getLookAngle();
            direction = new Vec3(direction.x, 0.0D, direction.z);
            if (direction.lengthSqr() < 0.001D) {
                return false;
            }
            direction = direction.normalize();
        }

        float strength = Mth.clamp(0.42F + stimulant * 0.28F + dashPower * 0.40F, 0.42F, 1.30F);
        Vec3 current = player.getDeltaMovement();
        Vec3 impulse = direction.scale(strength);
        player.setDeltaMovement(
                current.x * 0.35D + impulse.x,
                Math.max(current.y, player.onGround() ? 0.10D : -0.02D),
                current.z * 0.35D + impulse.z
        );
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        COOLDOWNS.put(player.getUUID(), gameTime);

        BurstWindowManager.trigger(player);
        DrugEffectRuntimeManager.addEffect(player, EffectType.ADRENALINE_SURGE, Math.min(1.2F, 0.18F + stimulant * 0.22F), 20 * 4);
        DrugEffectRuntimeManager.addEffect(player, EffectType.HEARTBEAT, Math.min(1.3F, 0.25F + stimulant * 0.18F), 20 * 5);
        DrugEffectRuntimeManager.addEffect(player, EffectType.TREMOR, Math.min(0.35F, stimulant * 0.045F), 20 * 4);

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    player.getX(),
                    player.getY() + 0.15D,
                    player.getZ(),
                    8,
                    0.20D,
                    0.04D,
                    0.20D,
                    0.04D
            );
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.55F, 1.35F);
        return true;
    }

    private static float stimulantIntensity(ServerPlayer player) {
        float rush = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.MANUAL_WORK_SPEED);
        float movement = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.MOVEMENT_SPEED);
        float heartbeat = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.HEARTBEAT);
        float adrenaline = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.ADRENALINE_SURGE);
        return Math.max(adrenaline, rush * 0.85F + movement * 1.4F + heartbeat * 0.12F);
    }

    private static int cooldownTicks(float stimulant) {
        return Mth.clamp(Math.round(34.0F - stimulant * 8.0F), 14, 34);
    }

    private static Vec3 movementDirection(ServerPlayer player, float requestedForward, float requestedStrafe) {
        float forward = Mth.clamp(requestedForward, -1.0F, 1.0F);
        float strafe = Mth.clamp(requestedStrafe, -1.0F, 1.0F);
        Vec3 input = new Vec3(strafe, 0.0D, forward);
        if (input.lengthSqr() < 0.001D) {
            return Vec3.ZERO;
        }

        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        double sin = Math.sin(yaw);
        double cos = Math.cos(yaw);
        double x = input.x * cos - input.z * sin;
        double z = input.z * cos + input.x * sin;
        return new Vec3(x, 0.0D, z).normalize();
    }
}
