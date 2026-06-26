package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.mydrugs.mydrugs.Config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Animates short particle trails from a Psychotrope Engine to each Psy Current target while the
 * engine is actively transferring. Pulses are queued by {@code DistillateEnginePulsePayload} and
 * tick out client-side, so the server only emits one packet per pulse — never one packet per
 * particle.
 *
 * <p>The trail's color and jitter shift with the engine's strain bucket: stable transfers read as
 * smooth purple arcs, critical strain reads as jagged red sparks. Disabled or muted entirely if
 * the player has reduced motion on or particle density at zero.
 */
public final class PsyCurrentPulseClientState {
    /** How many ticks each pulse lives before it finishes. */
    private static final int PULSE_LIFETIME_TICKS = 12;
    /** Bound on simultaneously-animating pulses; older pulses are dropped on overflow. */
    private static final int MAX_PULSES = 96;

    private static final List<Pulse> PULSES = new ArrayList<>();

    private PsyCurrentPulseClientState() {
    }

    public static void enqueue(BlockPos enginePos, List<BlockPos> targets, int strainBucket) {
        if (enginePos == null || targets == null || targets.isEmpty()) {
            return;
        }
        if (!shouldAnimate()) {
            return;
        }
        int bucket = Math.max(0, Math.min(4, strainBucket));
        for (BlockPos target : targets) {
            if (target == null) {
                continue;
            }
            PULSES.add(new Pulse(enginePos.immutable(), target.immutable(), bucket, PULSE_LIFETIME_TICKS));
        }
        while (PULSES.size() > MAX_PULSES) {
            PULSES.remove(0);
        }
    }

    public static void tick() {
        if (PULSES.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            PULSES.clear();
            return;
        }
        if (!shouldAnimate()) {
            PULSES.clear();
            return;
        }

        Iterator<Pulse> iterator = PULSES.iterator();
        while (iterator.hasNext()) {
            Pulse pulse = iterator.next();
            pulse.ticksRemaining--;
            if (pulse.ticksRemaining <= 0) {
                iterator.remove();
                continue;
            }
            renderPulse(level, pulse);
        }
    }

    public static void clear() {
        PULSES.clear();
    }

    private static boolean shouldAnimate() {
        Config.Client c = Config.CLIENT;
        if (c.reducedMotionMode.get()) {
            return false;
        }
        double density = c.particleDensityMultiplier.get();
        return density > 0.0D;
    }

    private static void renderPulse(ClientLevel level, Pulse pulse) {
        double progress = 1.0D - (pulse.ticksRemaining / (double) PULSE_LIFETIME_TICKS);
        double headProgress = Math.min(1.0D, progress + 0.10D);
        double tailProgress = Math.max(0.0D, progress - 0.15D);

        double ex = pulse.enginePos.getX() + 0.5D;
        double ey = pulse.enginePos.getY() + 0.75D;
        double ez = pulse.enginePos.getZ() + 0.5D;
        double tx = pulse.targetPos.getX() + 0.5D;
        double ty = pulse.targetPos.getY() + 0.75D;
        double tz = pulse.targetPos.getZ() + 0.5D;

        ParticleOptions options = particleFor(pulse.strainBucket);
        // Two samples along the trail keep particle count low while still reading as a moving arc.
        spawnAt(level, ex, ey, ez, tx, ty, tz, headProgress, pulse.strainBucket, options);
        spawnAt(level, ex, ey, ez, tx, ty, tz, tailProgress, pulse.strainBucket, options);
        if (pulse.strainBucket >= 3) {
            // Critical / overloaded strain throws sparks at the head.
            level.addParticle(ParticleTypes.CRIT,
                    lerp(ex, tx, headProgress),
                    lerp(ey, ty, headProgress) + 0.1D,
                    lerp(ez, tz, headProgress),
                    (level.random.nextDouble() - 0.5D) * 0.1D,
                    0.05D,
                    (level.random.nextDouble() - 0.5D) * 0.1D);
        }
    }

    private static void spawnAt(
            ClientLevel level,
            double ex, double ey, double ez,
            double tx, double ty, double tz,
            double progress,
            int strainBucket,
            ParticleOptions options
    ) {
        double x = lerp(ex, tx, progress);
        double y = lerp(ey, ty, progress);
        double z = lerp(ez, tz, progress);
        double jitter = jitterFor(strainBucket);
        x += (level.random.nextDouble() - 0.5D) * jitter;
        y += (level.random.nextDouble() - 0.5D) * jitter;
        z += (level.random.nextDouble() - 0.5D) * jitter;
        level.addParticle(options, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    private static double jitterFor(int strainBucket) {
        return switch (strainBucket) {
            case 0 -> 0.04D;
            case 1 -> 0.08D;
            case 2 -> 0.14D;
            case 3 -> 0.22D;
            default -> 0.32D;
        };
    }

    private static ParticleOptions particleFor(int strainBucket) {
        return switch (strainBucket) {
            case 0 -> new DustParticleOptions(0x8E7CFF, 0.9F);
            case 1 -> new DustParticleOptions(0xCFC2FF, 1.0F);
            case 2 -> new DustParticleOptions(0xFFB47A, 1.1F);
            case 3 -> new DustParticleOptions(0xFF6E55, 1.2F);
            default -> new DustParticleOptions(0xFF3322, 1.3F);
        };
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static final class Pulse {
        final BlockPos enginePos;
        final BlockPos targetPos;
        final int strainBucket;
        int ticksRemaining;

        Pulse(BlockPos enginePos, BlockPos targetPos, int strainBucket, int ticksRemaining) {
            this.enginePos = enginePos;
            this.targetPos = targetPos;
            this.strainBucket = strainBucket;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
