package org.mydrugs.mydrugs.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.HashMap;
import java.util.Map;

public final class MachineFeedback {
    private static final Map<Long, Long> LAST_OPTIONAL_FEEDBACK = new HashMap<>();
    private static final int OPTIONAL_COOLDOWN_TICKS = 40;

    private MachineFeedback() {
    }

    public static void recipeStarted(ServerLevel level, BlockPos pos) {
        if (shouldPlayOptional(level, pos, 1)) {
            level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.25F, 1.15F);
        }
    }

    public static void recipeCompleted(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.35F, 1.35F);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, 4, 0.18D, 0.18D, 0.18D, 0.01D);
    }

    public static void outputBlocked(ServerLevel level, BlockPos pos) {
        if (shouldPlayOptional(level, pos, 2)) {
            level.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.BLOCKS, 0.25F, 0.75F);
            level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D, 2, 0.12D, 0.12D, 0.12D, 0.0D);
        }
    }

    public static void modeChanged(ServerLevel level, BlockPos pos) {
        if (shouldPlayOptional(level, pos, 3)) {
            level.playSound(null, pos, SoundEvents.COPPER_HIT, SoundSource.BLOCKS, 0.2F, 1.4F);
        }
    }

    private static boolean shouldPlayOptional(ServerLevel level, BlockPos pos, int kind) {
        long key = pos.asLong() ^ ((long) kind << 56);
        long now = level.getGameTime();
        long last = LAST_OPTIONAL_FEEDBACK.getOrDefault(key, Long.MIN_VALUE);
        if (now - last < OPTIONAL_COOLDOWN_TICKS) {
            return false;
        }
        LAST_OPTIONAL_FEEDBACK.put(key, now);
        return true;
    }
}
