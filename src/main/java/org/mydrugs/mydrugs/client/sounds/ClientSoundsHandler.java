package org.mydrugs.mydrugs.client.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class ClientSoundsHandler {
    private static final Map<SoundEvent, SoundInstance> ACTIVE = new HashMap<>();
    private static final Map<SoundEvent, PendingSound> TO_START = new HashMap<>();

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

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        SoundInstance existing = ACTIVE.get(soundEvent);
        if (existing != null && !existing.isStoppedFlag()) {
            existing.refreshDuration(durationTick, fadeTicksRemaining, fadeDurationTicks);
            return;
        }

        TO_START.put(soundEvent, new PendingSound(durationTick, fadeTicksRemaining, fadeDurationTicks));
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
                existing.refreshDuration(pending.durationTicks(), pending.fadeTicksRemaining(), pending.fadeDurationTicks());
                continue;
            }

            SoundInstance instance = new SoundInstance(
                    soundEvent,
                    mc.player,
                    pending.durationTicks(),
                    pending.fadeTicksRemaining(),
                    pending.fadeDurationTicks()
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
    }

    private record PendingSound(int durationTicks, int fadeTicksRemaining, int fadeDurationTicks) {
    }
}
