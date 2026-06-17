package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.InnerDimensions;
import org.mydrugs.mydrugs.dimension.inner.InnerAtmosphere;
import org.mydrugs.mydrugs.dimension.inner.InnerSceneType;
import org.mydrugs.mydrugs.dimension.inner.InnerTerrain;

/**
 * Phase 4 — feature-anchored & hero particles. Upgraded from camera-attached motes to place-aware
 * atmosphere:
 *
 * <ul>
 *   <li><b>Anchored ambient spawns</b> sample the terrain at the candidate spawn XZ (not just the
 *       player) and bias particles to the feature there: mist/light rising off lake surfaces, embers
 *       off {@code EMBER_PIT}/scar floors, spores clustered in {@code DENSE_GROVE}, drifting light in
 *       {@code CRYSTAL_CLEARING} / {@code PRISM_BASIN}. The existing density / reduced-motion budget
 *       is preserved.</li>
 *   <li><b>Hero events</b> are rare, high-drama, per-scene signature moments — a slow rising light
 *       column in {@code CRYSTAL_CLEARING}, a ripple sweeping across a {@code MIRROR_LAKE}, an ember
 *       burst in an {@code EMBER_PIT}. One at a time, on a long cooldown, suppressed under reduced
 *       motion.</li>
 * </ul>
 */
public final class InnerAmbientParticleController {
    private static int tickCounter;
    private static int heroCooldown = 200;

    private InnerAmbientParticleController() {
    }

    public static void tick(Minecraft mc) {
        if (mc == null || mc.level == null || mc.player == null
                || !mc.level.dimension().equals(InnerDimensions.INNER_LEVEL)) {
            return;
        }
        double density = particleDensity();
        if (density <= 0.0D) {
            return;
        }
        InnerAtmosphere.Sample atmosphere = InnerAtmosphereClient.current(mc);
        if (atmosphere == null) {
            return;
        }
        ClientLevel level = mc.level;
        Player player = mc.player;

        // Hero events run on their own slow cadence, independent of the ambient throttle.
        if (heroCooldown > 0) {
            heroCooldown--;
        } else if (!reducedMotion()) {
            tryHeroEvent(level, player, atmosphere, level.random, density);
        }

        int interval = reducedMotion() ? 14 : 8;
        if (++tickCounter % interval != 0) {
            return;
        }
        double chance = Math.min(0.72D, atmosphere.particleIntensity() * density);
        if (level.random.nextDouble() > chance) {
            return;
        }
        int count = Math.max(1, Math.min(3, (int) Math.round(1.0D + atmosphere.particleIntensity() * 2.0D * density)));
        if (reducedMotion()) {
            count = 1;
        }
        for (int i = 0; i < count; i++) {
            spawnAnchoredParticle(level, player, atmosphere, level.random);
        }
    }

    // -------------------------------------------------------------------------
    // Feature-anchored ambient particles
    // -------------------------------------------------------------------------

    private static void spawnAnchoredParticle(
            ClientLevel level,
            Player player,
            InnerAtmosphere.Sample atmosphere,
            RandomSource random
    ) {
        double radius = atmosphere.sceneType() == InnerSceneType.PATH_VISTA ? 5.0D : 7.0D;
        double x = player.getX() + (random.nextDouble() - 0.5D) * 2.0D * radius;
        double z = player.getZ() + (random.nextDouble() - 0.5D) * 2.0D * radius;

        int candidateX = (int) Math.floor(x);
        int candidateZ = (int) Math.floor(z);
        int centerX = InnerTerrain.slotCenter(candidateX);
        int centerZ = InnerTerrain.slotCenter(candidateZ);
        InnerTerrain.Sample terrain = InnerTerrain.sample(centerX, centerZ, candidateX, candidateZ);

        InnerSceneType scene = atmosphere.sceneType();
        double drift = 0.004D + atmosphere.glowBias() * 0.006D;

        if (terrain.skyLand() && random.nextInt(3) == 0) {
            // C3: faint glints falling from the floating sky-shards overhead.
            double y = terrain.skyBottomY() - 0.5D - random.nextDouble() * 2.0D;
            level.addParticle(ParticleTypes.END_ROD, x, y, z,
                    (random.nextDouble() - 0.5D) * 0.004D, -0.015D - random.nextDouble() * 0.01D,
                    (random.nextDouble() - 0.5D) * 0.004D);
            return;
        }
        if (terrain.lake() || terrain.lakeStrength() > 0.45D) {
            // Mist / drifting light rising off the lake surface.
            double y = terrain.lakeSurfaceY() + 0.1D + random.nextDouble() * 0.6D;
            ParticleOptions p = random.nextInt(3) == 0 ? ParticleTypes.END_ROD : new DustParticleOptions(0x7FB6FF, 0.8F);
            level.addParticle(p, x, y, z, (random.nextDouble() - 0.5D) * drift, 0.012D + random.nextDouble() * 0.01D, (random.nextDouble() - 0.5D) * drift);
            return;
        }
        if (scene == InnerSceneType.EMBER_PIT || terrain.scar() || terrain.scarStrength() > 0.5D) {
            // Embers lifting off the scorched floor.
            double y = terrain.topY() + 0.2D + random.nextDouble() * 0.8D;
            ParticleOptions p = random.nextBoolean() ? ParticleTypes.FLAME : ParticleTypes.LAVA;
            level.addParticle(p, x, y, z, (random.nextDouble() - 0.5D) * 0.02D, 0.02D + random.nextDouble() * 0.02D, (random.nextDouble() - 0.5D) * 0.02D);
            return;
        }
        if (scene == InnerSceneType.DENSE_GROVE || scene == InnerSceneType.HERO_TREE_GROVE) {
            // Spores clustered low to the ground.
            double y = terrain.topY() + 0.4D + random.nextDouble() * 1.4D;
            level.addParticle(ParticleTypes.COMPOSTER, x, y, z, (random.nextDouble() - 0.5D) * 0.01D, 0.002D, (random.nextDouble() - 0.5D) * 0.01D);
            return;
        }
        if (scene == InnerSceneType.CRYSTAL_CLEARING || scene == InnerSceneType.PRISM_BASIN) {
            // Slow drifting light, higher in the air.
            double y = terrain.topY() + 1.0D + random.nextDouble() * 3.0D;
            level.addParticle(ParticleTypes.END_ROD, x, y, z, (random.nextDouble() - 0.5D) * 0.006D, 0.004D + random.nextDouble() * 0.006D, (random.nextDouble() - 0.5D) * 0.006D);
            return;
        }

        // Default: drug-flavoured motes near the surface.
        double y = terrain.visualTopY() + 1.0D + random.nextDouble() * 2.2D;
        ParticleOptions particle = defaultParticleFor(atmosphere);
        double dy = 0.004D + atmosphere.fogDensity() * 0.006D;
        level.addParticle(particle, x, y, z, (random.nextDouble() - 0.5D) * drift, dy, (random.nextDouble() - 0.5D) * drift);
    }

    private static ParticleOptions defaultParticleFor(InnerAtmosphere.Sample atmosphere) {
        DrugId drugId = atmosphere.primaryDrug();
        if (atmosphere.sceneType() == InnerSceneType.MEMORY_MARSH || drugId == DrugId.ALCOHOL) {
            return new DustParticleOptions(0x668CFF, 0.85F);
        }
        if (drugId == DrugId.LSD) {
            return ParticleTypes.END_ROD;
        }
        return switch (drugId) {
            case COFFEE -> new DustParticleOptions(0xE7B16E, 0.85F);
            case TOBACCO -> ParticleTypes.ASH;
            case WEED -> new DustParticleOptions(0x7EC36C, 0.75F);
            case HASH -> new DustParticleOptions(0xB9A4FF, 0.85F);
            case COCAINE -> new DustParticleOptions(0xFF3344, 0.80F);
            case MUSHROOMS -> ParticleTypes.COMPOSTER;
            default -> ParticleTypes.WAX_ON;
        };
    }

    // -------------------------------------------------------------------------
    // Hero events — rare, high-drama, one at a time
    // -------------------------------------------------------------------------

    private static void tryHeroEvent(
            ClientLevel level,
            Player player,
            InnerAtmosphere.Sample atmosphere,
            RandomSource random,
            double density
    ) {
        // Low base probability, only some scenes have a signature moment.
        if (random.nextDouble() > 0.012D * density) {
            return;
        }
        InnerSceneType scene = atmosphere.sceneType();

        double angle = random.nextDouble() * Math.PI * 2.0D;
        double dist = 4.0D + random.nextDouble() * 6.0D;
        double x = player.getX() + Math.cos(angle) * dist;
        double z = player.getZ() + Math.sin(angle) * dist;
        int cx = InnerTerrain.slotCenter((int) Math.floor(x));
        int cz = InnerTerrain.slotCenter((int) Math.floor(z));
        InnerTerrain.Sample terrain = InnerTerrain.sample(cx, cz, (int) Math.floor(x), (int) Math.floor(z));

        switch (scene) {
            case CRYSTAL_CLEARING, PRISM_BASIN -> {
                // Slow rising light column.
                double baseY = terrain.topY() + 0.5D;
                for (int i = 0; i < 14; i++) {
                    double y = baseY + i * 0.45D;
                    level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.03D + i * 0.002D, 0.0D);
                }
                heroCooldown = 360;
            }
            case MIRROR_LAKE -> {
                if (!terrain.lake() && terrain.lakeStrength() <= 0.3D) {
                    return;
                }
                // Ripple sweeping outward across the surface.
                double surfaceY = (terrain.lake() ? terrain.lakeSurfaceY() : terrain.topY()) + 0.05D;
                int points = 24;
                double ringR = 3.0D;
                for (int i = 0; i < points; i++) {
                    double a = (i / (double) points) * Math.PI * 2.0D;
                    double rx = x + Math.cos(a) * ringR;
                    double rz = z + Math.sin(a) * ringR;
                    level.addParticle(ParticleTypes.SPLASH, rx, surfaceY, rz, Math.cos(a) * 0.04D, 0.01D, Math.sin(a) * 0.04D);
                }
                heroCooldown = 300;
            }
            case EMBER_PIT -> {
                // Upward ember burst.
                double baseY = terrain.topY() + 0.3D;
                for (int i = 0; i < 30; i++) {
                    double vx = (random.nextDouble() - 0.5D) * 0.18D;
                    double vz = (random.nextDouble() - 0.5D) * 0.18D;
                    double vy = 0.18D + random.nextDouble() * 0.22D;
                    ParticleOptions p = random.nextBoolean() ? ParticleTypes.FLAME : ParticleTypes.LAVA;
                    level.addParticle(p, x, baseY, z, vx, vy, vz);
                }
                heroCooldown = 280;
            }
            default -> {
                // No signature moment in this scene; short retry delay so we don't spin.
                heroCooldown = 80;
            }
        }
    }

    private static double particleDensity() {
        try {
            return Mth.clamp(Config.CLIENT.particleDensityMultiplier.get(), 0.0D, 1.0D);
        } catch (IllegalStateException ignored) {
            return 1.0D;
        }
    }

    private static boolean reducedMotion() {
        try {
            return Config.CLIENT.reducedMotionMode.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    public static void clear() {
        tickCounter = 0;
        heroCooldown = 200;
    }
}
