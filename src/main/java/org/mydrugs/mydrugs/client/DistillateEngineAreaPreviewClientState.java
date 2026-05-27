package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.mydrugs.mydrugs.Config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Client state for the Distillate Engine "show area" preview.
 *
 * <p>Each entry is a TTL'd particle outline at a known engine position, plus the per-category
 * target lists the server captured when the preview started. The cube outline tells the player
 * which blocks are in range; the colored target highlights tell them <em>what</em> the engine
 * found there (valid, full, missing upgrade, the engine itself).
 *
 * <p>Particles are spawned on a slow throttle and respect the player's particle density /
 * reduced-motion preferences so a busy base does not become a particle storm.
 */
public final class DistillateEngineAreaPreviewClientState {
    private static final int TICK_INTERVAL = 8;
    private static final int TARGET_PULSE_INTERVAL = 16;
    private static final int MAX_TARGETS_PER_CATEGORY = 64;
    private static final Map<BlockPos, Preview> PREVIEWS = new HashMap<>();
    private static int tickCounter;

    private static final int COLOR_VALID = 0x8E7CFF;
    private static final int COLOR_FULL = 0xFFC74D;
    private static final int COLOR_INCOMPATIBLE = 0x8C8C8C;
    private static final int COLOR_ENGINE = 0xC79DFF;

    private DistillateEngineAreaPreviewClientState() {
    }

    public static void start(
            BlockPos enginePos,
            int radius,
            int durationTicks,
            List<BlockPos> validTargets,
            List<BlockPos> fullTargets,
            List<BlockPos> incompatibleTargets
    ) {
        if (enginePos == null || radius <= 0 || durationTicks <= 0) {
            return;
        }
        Preview preview = PREVIEWS.computeIfAbsent(enginePos.immutable(), ignored -> new Preview());
        preview.radius = Math.max(1, Math.min(16, radius));
        preview.ticksRemaining = Math.max(preview.ticksRemaining, durationTicks);
        preview.validTargets = limit(validTargets);
        preview.fullTargets = limit(fullTargets);
        preview.incompatibleTargets = limit(incompatibleTargets);
    }

    private static List<BlockPos> limit(List<BlockPos> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        if (source.size() <= MAX_TARGETS_PER_CATEGORY) {
            return List.copyOf(source);
        }
        return List.copyOf(source.subList(0, MAX_TARGETS_PER_CATEGORY));
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

        boolean targetPulseTick = tickCounter % TARGET_PULSE_INTERVAL == 0;
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
            if (targetPulseTick) {
                spawnTargetHighlights(level, entry.getKey(), preview);
            }
        }
    }

    public static void clear() {
        PREVIEWS.clear();
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
            spawnEdge(level, x, minY, minZ);
            spawnEdge(level, x, minY, maxZ);
            spawnEdge(level, x, maxY, minZ);
            spawnEdge(level, x, maxY, maxZ);
            spawnEdge(level, minX, y, minZ);
            spawnEdge(level, minX, y, maxZ);
            spawnEdge(level, maxX, y, minZ);
            spawnEdge(level, maxX, y, maxZ);
            spawnEdge(level, minX, minY, z);
            spawnEdge(level, minX, maxY, z);
            spawnEdge(level, maxX, minY, z);
            spawnEdge(level, maxX, maxY, z);
        }
    }

    private static void spawnTargetHighlights(ClientLevel level, BlockPos enginePos, Preview preview) {
        // Reduced motion players keep the cube outline (it's a passive marker) but skip the
        // pulsing per-target highlights so there's no rhythmic flicker on the side of the screen.
        if (Config.CLIENT.reducedMotionMode.get()) {
            return;
        }
        float density = (float) Math.max(0.0D, Config.CLIENT.particleDensityMultiplier.get());
        if (density <= 0.0F) {
            return;
        }

        DustParticleOptions valid = new DustParticleOptions(COLOR_VALID, 1.0F);
        DustParticleOptions full = new DustParticleOptions(COLOR_FULL, 1.0F);
        DustParticleOptions incompatible = new DustParticleOptions(COLOR_INCOMPATIBLE, 1.0F);
        DustParticleOptions engine = new DustParticleOptions(COLOR_ENGINE, 1.4F);

        int validPulses = Math.max(1, Math.round(3 * density));
        int otherPulses = Math.max(1, Math.round(2 * density));

        for (BlockPos pos : preview.validTargets) {
            spawnHighlight(level, pos, valid, validPulses);
        }
        for (BlockPos pos : preview.fullTargets) {
            spawnHighlight(level, pos, full, otherPulses);
        }
        for (BlockPos pos : preview.incompatibleTargets) {
            // Smoke + a desaturated dust hint so the "missing upgrade" reading is unambiguous.
            spawnHighlight(level, pos, incompatible, 1);
            level.addParticle(ParticleTypes.SMOKE,
                    pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D,
                    0.0D, 0.01D, 0.0D);
        }
        // Engine itself gets a brighter central pulse so the source is unmistakable.
        spawnHighlight(level, enginePos, engine, Math.max(2, Math.round(4 * density)));
    }

    private static void spawnHighlight(ClientLevel level, BlockPos pos, ParticleOptions options, int count) {
        double cx = pos.getX() + 0.5D;
        double cy = pos.getY() + 1.0D;
        double cz = pos.getZ() + 0.5D;
        for (int i = 0; i < count; i++) {
            double offsetX = (level.random.nextDouble() - 0.5D) * 0.5D;
            double offsetY = level.random.nextDouble() * 0.4D;
            double offsetZ = (level.random.nextDouble() - 0.5D) * 0.5D;
            level.addParticle(options, cx + offsetX, cy + offsetY, cz + offsetZ, 0.0D, 0.01D, 0.0D);
        }
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static void spawnEdge(ClientLevel level, double x, double y, double z) {
        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    private static final class Preview {
        int radius = 1;
        int ticksRemaining;
        List<BlockPos> validTargets = List.of();
        List<BlockPos> fullTargets = List.of();
        List<BlockPos> incompatibleTargets = List.of();
    }
}
