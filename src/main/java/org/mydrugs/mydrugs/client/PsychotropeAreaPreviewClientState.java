package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.PsychotropeMultiblock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class PsychotropeAreaPreviewClientState {
    private static final int TICK_INTERVAL = 8;
    private static final int MAX_INVALID_TICKS = 80;
    private static final Map<Key, Preview> PREVIEWS = new HashMap<>();
    private static int tickCounter;

    private PsychotropeAreaPreviewClientState() {
    }

    public static boolean isPersistentEnabled(@Nullable ResourceLocation dimension, BlockPos corePos) {
        if (dimension == null) {
            return false;
        }
        Preview preview = PREVIEWS.get(new Key(dimension, corePos.immutable()));
        return preview != null && preview.persistent;
    }

    public static void setPersistent(@Nullable ResourceLocation dimension, BlockPos corePos, int radius, boolean enabled) {
        if (dimension == null) {
            return;
        }
        Key key = new Key(dimension, corePos.immutable());
        if (!enabled) {
            PREVIEWS.remove(key);
            return;
        }

        Preview preview = PREVIEWS.computeIfAbsent(key, ignored -> new Preview());
        preview.radius = clampRadius(radius);
        preview.persistent = true;
        preview.ticksRemaining = 0;
        preview.invalidTicks = 0;
    }

    public static void refreshPersistent(@Nullable ResourceLocation dimension, BlockPos corePos, int radius) {
        if (dimension == null) {
            return;
        }
        Preview preview = PREVIEWS.get(new Key(dimension, corePos.immutable()));
        if (preview != null && preview.persistent) {
            preview.radius = clampRadius(radius);
            preview.invalidTicks = 0;
        }
    }

    public static void flash(@Nullable ResourceLocation dimension, BlockPos corePos, int radius, int durationTicks) {
        if (dimension == null || durationTicks <= 0) {
            return;
        }
        Preview preview = PREVIEWS.computeIfAbsent(new Key(dimension, corePos.immutable()), ignored -> new Preview());
        preview.radius = clampRadius(radius);
        preview.ticksRemaining = Math.max(preview.ticksRemaining, durationTicks);
        preview.invalidTicks = 0;
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

        ResourceLocation currentDimension = level.dimension().location();
        Iterator<Map.Entry<Key, Preview>> iterator = PREVIEWS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Key, Preview> entry = iterator.next();
            Key key = entry.getKey();
            Preview preview = entry.getValue();

            if (!key.dimension.equals(currentDimension)) {
                continue;
            }

            if (!preview.persistent) {
                preview.ticksRemaining -= TICK_INTERVAL;
                if (preview.ticksRemaining <= 0) {
                    iterator.remove();
                    continue;
                }
            }

            if (!level.hasChunkAt(key.corePos) || !PsychotropeMultiblock.validate(level, key.corePos)) {
                preview.invalidTicks += TICK_INTERVAL;
                if (preview.invalidTicks >= MAX_INVALID_TICKS) {
                    iterator.remove();
                }
                continue;
            }

            preview.invalidTicks = 0;
            spawnRadiusPreviewParticles(level, key.corePos, preview.radius);
        }
    }

    private static void spawnRadiusPreviewParticles(ClientLevel level, BlockPos center, int radius) {
        double minX = center.getX() + 0.5D - radius;
        double maxX = center.getX() + 0.5D + radius;
        double minY = center.getY() + 0.5D - radius;
        double maxY = center.getY() + 0.5D + radius;
        double minZ = center.getZ() + 0.5D - radius;
        double maxZ = center.getZ() + 0.5D + radius;

        int points = Math.max(2, radius * 2);
        for (int i = 0; i <= points; i++) {
            double t = i / (double) points;
            double x = lerp(minX, maxX, t);
            double y = lerp(minY, maxY, t);
            double z = lerp(minZ, maxZ, t);
            spawnPreviewParticle(level, x, minY, minZ);
            spawnPreviewParticle(level, x, minY, maxZ);
            spawnPreviewParticle(level, x, maxY, minZ);
            spawnPreviewParticle(level, x, maxY, maxZ);
            spawnPreviewParticle(level, minX, y, minZ);
            spawnPreviewParticle(level, minX, y, maxZ);
            spawnPreviewParticle(level, maxX, y, minZ);
            spawnPreviewParticle(level, maxX, y, maxZ);
            spawnPreviewParticle(level, minX, minY, z);
            spawnPreviewParticle(level, minX, maxY, z);
            spawnPreviewParticle(level, maxX, minY, z);
            spawnPreviewParticle(level, maxX, maxY, z);
        }
    }

    private static int clampRadius(int radius) {
        return Math.max(1, Math.min(8, radius));
    }

    private static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    private static void spawnPreviewParticle(ClientLevel level, double x, double y, double z) {
        level.addParticle(ParticleTypes.WITCH, x, y, z, 0.0D, 0.01D, 0.0D);
    }

    private record Key(ResourceLocation dimension, BlockPos corePos) {
    }

    private static final class Preview {
        private int radius = 1;
        private boolean persistent;
        private int ticksRemaining;
        private int invalidTicks;
    }
}
