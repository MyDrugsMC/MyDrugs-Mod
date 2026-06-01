package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.client.sounds.InnerLoopSoundInstance;
import org.mydrugs.mydrugs.dimension.inner.InnerAtmosphere;
import org.mydrugs.mydrugs.dimension.inner.InnerSceneType;
import org.mydrugs.mydrugs.sounds.ModSounds;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Phase 5 ambient soundscape for the Inner Dimension. Ticked from
 * {@link org.mydrugs.mydrugs.client.effects.ClientEventHandler#onClientTick}. Builds three layers,
 * all gated on {@code INNER_LEVEL} and the master {@link Config.Client#innerAmbientVolume}:
 *
 * <ol>
 *   <li><b>Per-region drones</b> — one looping bed per region group (warm hum / tense tone /
 *       water reverb / crystalline shimmer / ash wind). The active group's bed eases up while the
 *       previous group's eases out, crossfading on the same boundary the visuals blend on.</li>
 *   <li><b>Heartbeat bed</b> — a one-shot pulse paced from {@code danger} (fast) to {@code calm}
 *       (slow, steady), playing under everything.</li>
 *   <li><b>Reactive one-shots</b> — {@link #playOneShot} is called by other phases for the crossing
 *       sting (Phase 3), entry breath (Phase 6), reactive-plant chime and landmark tone (Phase 7).</li>
 * </ol>
 *
 * <p>Everything plays through {@link Minecraft#getSoundManager()}, so the existing
 * {@code AccessibilitySoundLimiter} / {@code HeadphonesSoundBlocker} {@code PlaySoundEvent}
 * subscribers see and can gate each sound. No-ops cleanly outside the dimension; cleared on logout.
 */
public final class InnerSoundscapeController {
    private enum RegionGroup {
        WARM(ModSounds.INNER_DRONE_WARM),
        TENSE(ModSounds.INNER_DRONE_TENSE),
        WATER(ModSounds.INNER_DRONE_WATER),
        CRYSTAL(ModSounds.INNER_DRONE_CRYSTAL),
        ASH(ModSounds.INNER_DRONE_ASH);

        private final Supplier<SoundEvent> sound;

        RegionGroup(Supplier<SoundEvent> sound) {
            this.sound = sound;
        }
    }

    private static final float DRONE_BASE = 0.5F;
    private static final Map<RegionGroup, InnerLoopSoundInstance> ACTIVE = new EnumMap<>(RegionGroup.class);
    private static int heartbeatTimer;

    private InnerSoundscapeController() {
    }

    public static void tick(Minecraft mc) {
        float master = masterVolume();
        if (master <= 0.0F || !InnerAtmosphereClient.inInnerDimension(mc)) {
            clear();
            return;
        }
        InnerAtmosphere.Sample sample = InnerAtmosphereClient.current(mc);
        if (sample == null) {
            clear();
            return;
        }

        updateDrones(mc, sample, master);
        updateHeartbeat(mc, sample, master);
    }

    private static void updateDrones(Minecraft mc, InnerAtmosphere.Sample sample, float master) {
        RegionGroup current = groupFor(sample.sceneType());
        float activeTarget = master * DRONE_BASE;

        // Ensure the current group's bed exists and is ramping up.
        InnerLoopSoundInstance active = ACTIVE.get(current);
        if (active == null || active.isStopped()) {
            InnerLoopSoundInstance instance = new InnerLoopSoundInstance(current.sound.get(), activeTarget);
            instance.setTargetVolume(activeTarget);
            ACTIVE.put(current, instance);
            mc.getSoundManager().play(instance);
        } else {
            active.setTargetVolume(activeTarget);
        }

        // Fade out / reap every other group.
        Iterator<Map.Entry<RegionGroup, InnerLoopSoundInstance>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<RegionGroup, InnerLoopSoundInstance> entry = it.next();
            InnerLoopSoundInstance instance = entry.getValue();
            if (instance.isStopped()) {
                it.remove();
                continue;
            }
            if (entry.getKey() != current) {
                instance.fadeOut();
            }
        }
    }

    private static void updateHeartbeat(Minecraft mc, InnerAtmosphere.Sample sample, float master) {
        // Faster under danger, slow and steady under calm.
        float danger = (float) Mth.clamp(sample.danger(), 0.0D, 1.0D);
        float calm = (float) Mth.clamp(sample.calm(), 0.0D, 1.0D);
        float pace = Mth.clamp(danger - calm * 0.4F, 0.0F, 1.0F);
        int interval = Math.round(Mth.lerp(pace, 32.0F, 12.0F));
        if (++heartbeatTimer >= interval) {
            heartbeatTimer = 0;
            playOneShot(ModSounds.INNER_HEARTBEAT.get(), 0.35F + danger * 0.2F, 0.9F + danger * 0.15F);
        }
    }

    /**
     * Plays a player-local one-shot through the sound manager, scaled by the master ambient volume.
     * Used by the crossing sting, entry breath, reactive-plant chime, and landmark tone. No-ops when
     * the ambient layer is disabled or the player is outside the dimension.
     */
    public static void playOneShot(SoundEvent event, float volume, float pitch) {
        if (event == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        float master = masterVolume();
        if (master <= 0.0F || !InnerAtmosphereClient.inInnerDimension(mc)) {
            return;
        }
        float vol = Mth.clamp(volume * master, 0.0F, 1.0F);
        if (vol <= 0.0F) {
            return;
        }
        mc.getSoundManager().play(SimpleSoundInstance.forUI(event, pitch, vol));
    }

    private static RegionGroup groupFor(InnerSceneType scene) {
        return switch (scene) {
            case MIRROR_LAKE, MEMORY_MARSH, ROOTED_WETLAND -> RegionGroup.WATER;
            case PRISM_BASIN, CRYSTAL_CLEARING -> RegionGroup.CRYSTAL;
            case EMBER_PIT, SCAR_FIELD -> RegionGroup.TENSE;
            case ASH_FLAT -> RegionGroup.ASH;
            default -> RegionGroup.WARM;
        };
    }

    private static float masterVolume() {
        try {
            return Config.CLIENT.innerAmbientVolume.get().floatValue();
        } catch (IllegalStateException ignored) {
            return 1.0F;
        }
    }

    public static void clear() {
        Minecraft mc = Minecraft.getInstance();
        for (InnerLoopSoundInstance instance : ACTIVE.values()) {
            mc.getSoundManager().stop(instance);
        }
        ACTIVE.clear();
        heartbeatTimer = 0;
    }
}
