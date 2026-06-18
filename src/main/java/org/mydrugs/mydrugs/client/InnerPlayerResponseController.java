package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.mydrugs.mydrugs.dimension.inner.InnerAtmosphere;
import org.mydrugs.mydrugs.dimension.inner.InnerTerrain;
import org.mydrugs.mydrugs.sounds.ModSounds;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * Phase 7 (client half) — the dimension noticing the player.
 *
 * <ul>
 *   <li><b>Reactive flora:</b> plant/grove tiles near the player get a soft light halo that grows
 *       as the player approaches and dims as they leave — a cheap proximity-keyed particle glow, no
 *       block mutation. Occasionally rings the Phase 5 chime.</li>
 *   <li><b>Path memory:</b> where the player walks, faint light lingers in the ground and fades over
 *       ~30s. A pure client trail: recent footsteps are remembered and re-lit with a probability that
 *       decays with their age, so a walked path glows briefly and dissolves.</li>
 * </ul>
 *
 * <p>Honors the particle-density and reduced-motion budget. The optional persisted "worn path" via
 * {@code IslandState} is intentionally deferred (see the task summary) — this trail is client-only.
 */
public final class InnerPlayerResponseController {
    private static final int FOOTSTEP_LIFETIME = 600; // ~30s at 20 tps
    private static final int MAX_FOOTSTEPS = 96;

    private record Footstep(double x, double y, double z, int birthTick) {
    }

    private static final Deque<Footstep> FOOTSTEPS = new ArrayDeque<>();
    private static int age;
    private static double lastX = Double.NaN;
    private static double lastZ = Double.NaN;
    private static int chimeCooldown;

    private InnerPlayerResponseController() {
    }

    public static void tick(Minecraft mc) {
        if (!InnerAtmosphereClient.inInnerDimension(mc)) {
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
        RandomSource random = level.random;
        age++;
        if (chimeCooldown > 0) {
            chimeCooldown--;
        }

        recordFootstep(player);
        renderPathMemory(level, random, density);
        reactiveFlora(level, player, atmosphere, random, density);
    }

    // -------------------------------------------------------------------------
    // Path memory
    // -------------------------------------------------------------------------

    private static void recordFootstep(Player player) {
        double x = player.getX();
        double z = player.getZ();
        if (Double.isNaN(lastX) || (Math.abs(x - lastX) + Math.abs(z - lastZ)) > 1.0D) {
            FOOTSTEPS.addLast(new Footstep(x, player.getY() + 0.05D, z, age));
            lastX = x;
            lastZ = z;
            while (FOOTSTEPS.size() > MAX_FOOTSTEPS) {
                FOOTSTEPS.removeFirst();
            }
        }
    }

    private static void renderPathMemory(ClientLevel level, RandomSource random, double density) {
        if (FOOTSTEPS.isEmpty()) {
            return;
        }
        double motionScale = InnerClientIntensity.particleScale();
        Iterator<Footstep> it = FOOTSTEPS.iterator();
        while (it.hasNext()) {
            Footstep step = it.next();
            int elapsed = age - step.birthTick();
            if (elapsed >= FOOTSTEP_LIFETIME) {
                it.remove();
                continue;
            }
            float life = 1.0F - elapsed / (float) FOOTSTEP_LIFETIME;
            // Older footsteps re-light less often, so the path dissolves from the oldest end.
            double p = life * life * 0.18D * density * motionScale;
            if (random.nextDouble() < p) {
                float scale = 0.5F + life * 0.6F;
                level.addParticle(
                        new DustParticleOptions(0xCFE6FF, scale),
                        step.x() + (random.nextDouble() - 0.5D) * 0.4D,
                        step.y(),
                        step.z() + (random.nextDouble() - 0.5D) * 0.4D,
                        0.0D, 0.004D, 0.0D
                );
            }
        }
    }

    // -------------------------------------------------------------------------
    // Reactive flora
    // -------------------------------------------------------------------------

    private static void reactiveFlora(
            ClientLevel level,
            Player player,
            InnerAtmosphere.Sample atmosphere,
            RandomSource random,
            double density
    ) {
        if (reducedMotion() && random.nextInt(3) != 0) {
            return;
        }
        // A few proximity probes around the player each tick.
        int probes = reducedMotion() ? 1 : 3;
        for (int i = 0; i < probes; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double dist = 1.5D + random.nextDouble() * 4.0D;
            double x = player.getX() + Math.cos(angle) * dist;
            double z = player.getZ() + Math.sin(angle) * dist;
            int bx = (int) Math.floor(x);
            int bz = (int) Math.floor(z);
            int cx = InnerTerrain.slotCenter(bx);
            int cz = InnerTerrain.slotCenter(bz);
            InnerTerrain.Sample terrain = InnerTerrain.sample(cx, cz, bx, bz);
            boolean flora = terrain.plantPatch() || terrain.treeZone() || terrain.plantDensity() > 0.4D || terrain.treeDensity() > 0.4D;
            if (!flora) {
                continue;
            }
            // Halo brightens as the player nears: closer probes spawn more / brighter motes.
            float proximity = (float) (1.0D - dist / 5.5D);
            if (random.nextDouble() > proximity * density) {
                continue;
            }
            double y = terrain.topY() + 0.6D + random.nextDouble() * 1.2D;
            level.addParticle(
                    ParticleTypes.END_ROD,
                    x, y, z,
                    (random.nextDouble() - 0.5D) * 0.004D,
                    0.006D + proximity * 0.006D,
                    (random.nextDouble() - 0.5D) * 0.004D
            );
            // Occasional chime when a plant lights right next to the player.
            if (proximity > 0.6F && chimeCooldown <= 0 && random.nextInt(64) == 0) {
                InnerSoundscapeController.playOneShot(ModSounds.INNER_CHIME.get(), 0.4F + proximity * 0.2F, 1.0F + random.nextFloat() * 0.2F);
                chimeCooldown = 80;
            }
        }
    }

    public static void clear() {
        FOOTSTEPS.clear();
        age = 0;
        lastX = Double.NaN;
        lastZ = Double.NaN;
        chimeCooldown = 0;
    }

    private static double particleDensity() {
        return InnerClientIntensity.particleDensity();
    }

    private static boolean reducedMotion() {
        return InnerClientIntensity.reducedMotion();
    }
}
