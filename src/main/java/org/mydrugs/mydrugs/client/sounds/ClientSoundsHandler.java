package org.mydrugs.mydrugs.client.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.addiction.network.DrugEffectSyncPayload;
import org.mydrugs.mydrugs.core.drug.effect.EffectCategory;
import org.mydrugs.mydrugs.sounds.ModSounds;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class ClientSoundsHandler {
    private static final Map<SoundEvent, SoundInstance> ACTIVE = new HashMap<>();
    private static final Map<SoundEvent, PendingSound> TO_START = new HashMap<>();
    private static final Set<SoundEvent> SYNC_OWNED = new HashSet<>();

    private ClientSoundsHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null || mc.isPaused()) {
            stopAll(mc);
            return;
        }

        startPending(mc);
        cleanupFinished(mc);
    }

    public static void setToStart(SoundEvent soundEvent, int durationTick) {
        setToStart(soundEvent, durationTick, 0, 0);
    }

    public static void setToStart(SoundEvent soundEvent, int durationTick, int fadeTicksRemaining, int fadeDurationTicks) {
        if (soundEvent == null || durationTick <= 0) {
            return;
        }
        float volume = volumeFor(soundEvent);
        if (volume <= 0.0F) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        SoundInstance existing = ACTIVE.get(soundEvent);
        if (existing != null && !existing.isStoppedFlag()) {
            existing.refreshDuration(durationTick, fadeTicksRemaining, fadeDurationTicks, volume);
            return;
        }

        TO_START.put(soundEvent, new PendingSound(durationTick, fadeTicksRemaining, fadeDurationTicks, volume));
    }

    public static void clear() {
        stopAll(Minecraft.getInstance());
    }

    public static void reconcileEffects(Collection<DrugEffectSyncPayload.Entry> entries) {
        Minecraft mc = Minecraft.getInstance();
        if (!Config.CLIENT.enableDrugSounds.get()) {
            stopAll(mc);
            return;
        }

        Set<SoundEvent> synced = new HashSet<>();
        for (DrugEffectSyncPayload.Entry entry : entries) {
            if (entry.type() == null) {
                continue;
            }
            EffectCategory category = entry.type().getCategory();
            if (category != EffectCategory.SOUND && category != EffectCategory.SOUND_EFFECT) {
                continue;
            }
            SoundEvent sound = ModSounds.fromEffectType(entry.type());
            if (sound == null || entry.remainingTicks() <= 0 || entry.effectiveIntensity() <= 0.0F) {
                continue;
            }
            if (isHeartbeatSound(sound) && !Config.CLIENT.enableHeartbeatSounds.get()) {
                stopSound(mc, sound);
                continue;
            }
            synced.add(sound);
            SYNC_OWNED.add(sound);
            setToStart(sound, entry.remainingTicks(), entry.fadeTicksRemaining(), entry.fadeDurationTicks());
        }

        for (SoundEvent sound : new HashSet<>(SYNC_OWNED)) {
            if (!synced.contains(sound)) {
                stopSound(mc, sound);
            }
        }
    }

    private static void startPending(Minecraft mc) {
        if (TO_START.isEmpty()) {
            return;
        }

        for (Map.Entry<SoundEvent, PendingSound> entry : TO_START.entrySet()) {
            SoundEvent soundEvent = entry.getKey();
            PendingSound pending = entry.getValue();

            SoundInstance existing = ACTIVE.get(soundEvent);
            if (existing != null && !existing.isStoppedFlag()) {
                existing.refreshDuration(pending.durationTicks(), pending.fadeTicksRemaining(), pending.fadeDurationTicks(), pending.volume());
                continue;
            }

            SoundInstance instance = new SoundInstance(
                    soundEvent,
                    mc.player,
                    pending.durationTicks(),
                    pending.fadeTicksRemaining(),
                    pending.fadeDurationTicks(),
                    pending.volume()
            );
            ACTIVE.put(soundEvent, instance);
            mc.getSoundManager().play(instance);
        }

        TO_START.clear();
    }

    private static void cleanupFinished(Minecraft mc) {
        Iterator<Map.Entry<SoundEvent, SoundInstance>> iterator = ACTIVE.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<SoundEvent, SoundInstance> entry = iterator.next();
            SoundInstance instance = entry.getValue();

            if (instance.isStoppedFlag()) {
                mc.getSoundManager().stop(instance);
                iterator.remove();
            }
        }
    }

    private static void stopAll(Minecraft mc) {
        for (SoundInstance instance : ACTIVE.values()) {
            mc.getSoundManager().stop(instance);
        }

        ACTIVE.clear();
        TO_START.clear();
        SYNC_OWNED.clear();
    }

    private static void stopSound(Minecraft mc, SoundEvent soundEvent) {
        SoundInstance instance = ACTIVE.remove(soundEvent);
        if (instance != null) {
            mc.getSoundManager().stop(instance);
        }
        TO_START.remove(soundEvent);
        SYNC_OWNED.remove(soundEvent);
    }

    private static float volumeFor(SoundEvent soundEvent) {
        float volume = 1.0F;
        if (isHeartbeatSound(soundEvent)) {
            if (!Config.CLIENT.enableHeartbeatSounds.get()) {
                return 0.0F;
            }
            volume *= Config.CLIENT.heartbeatVolume.get().floatValue();
        }
        return Config.CLIENT.suddenSoundCap(volume);
    }

    private static boolean isHeartbeatSound(SoundEvent soundEvent) {
        return soundEvent == ModSounds.HEARTBEAT.get() || soundEvent == ModSounds.SINGLE_HEARTBEAT.get();
    }

    private record PendingSound(int durationTicks, int fadeTicksRemaining, int fadeDurationTicks, float volume) {
    }
}
