package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Client state for the Distillate Engine "show area" preview.
 *
 * Each entry is a TTL'd particle-outline at a known engine position. The list is small (one entry
 * per engine the player has previewed) and is purged automatically when its TTL ticks down to 0.
 */
public final class DistillateEngineAreaPreviewClientState {
    private static final int TICK_INTERVAL = 8;
    private static final Map<BlockPos, Preview> PREVIEWS = new HashMap<>();
    private static int tickCounter;

    private DistillateEngineAreaPreviewClientState() {
    }

    public static void start(BlockPos enginePos, int radius, int durationTicks) {
        if (enginePos == null || radius <= 0 || durationTicks <= 0) {
            return;
        }
        Preview preview = PREVIEWS.computeIfAbsent(enginePos.immutable(), ignored -> new Preview());
        preview.radius = Math.max(1, Math.min(16, radius));
        preview.ticksRemaining = Math.max(preview.ticksRemaining, durationTicks);
    }

    public static void tick() {
        if (++tickCounter % TICK_INTERVAL != 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || PREVIEWS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<BlockPos, Preview>> iterator = PREVIEWS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Preview> entry = iterator.next();
            Preview preview = entry.getValue();
            preview.ticksRemaining -= TICK_INTERVAL;
            if (preview.ticksRemaining <= 0) {
                iterator.remove();
                continue;
            }
            spawnAreaParticles(level, entry.getKey(), preview.radius);
        }
    }

    private static void spawnAreaParticles(ClientLevel level, BlockPos center, int radius) {
        double cx = center.getX() + 0.5D;
        double cy = center.getY() + 0.5D;
        double cz = center.getZ() + 0.5D;
        double minX = cx - radius;
        double maxX = cx + radius;
        double minY = cy - radius;
        double maxY = cy + radius;
        double minZ = cz - radius;
        double maxZ = cz + radius;

        int points = Math.max(2, radius * 2);
        for (int i = 0; i <= points; i++) {
            double t = i / (double) points;
            double x = lerp(minX, maxX, t);
            double y = lerp(minY, maxY, t);
            double z = lerp(minZ, maxZ, t);
            // 12 edges of the cube
            spawn(level, x, minY, minZ);
            spawn(level, x, minY, maxZ);
            spawn(level, x, maxY, minZ);
            spawn(level, x, maxY, maxZ);
            spawn(level, minX, y, minZ);
            spawn(level, minX, y, maxZ);
            spawn(level, maxX, y, minZ);
            spawn(level, maxX, y, maxZ);
            spawn(level, minX, minY, z);
            spawn(level, minX, maxY, z);
            spawn(level, maxX, minY, z);
            spawn(level, maxX, maxY, z);
        }
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static void spawn(ClientLevel level, double x, double y, double z) {
        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    private static final class Preview {
        int radius = 1;
        int ticksRemaining;
    }
}
