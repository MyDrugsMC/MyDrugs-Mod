package org.mydrugs.mydrugs.client.sounds;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.*;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class ClientSoundsHandler {
    private static final List<SoundInstance> PLAYING = new ArrayList<>();
    private static final List<SoundInstance> TO_START = new ArrayList<>();


    private ClientSoundsHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) {
            stopAll(mc);
            return;
        }

        for (SoundInstance instance : TO_START) {
            if (PLAYING.contains(instance)) continue;
            PLAYING.add(instance);
            mc.getSoundManager().play(instance);
        }
    }

    public static void setToStart(SoundEvent soundEvent, int durationTick) {
        if (soundEvent == null || durationTick <= 0) return;
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) return;
        SoundInstance soundInstance = new SoundInstance(soundEvent, player, durationTick);
        TO_START.add(soundInstance);
    }

    private static void stopAll(Minecraft mc) {
        for (SoundInstance instance : PLAYING) {
            mc.getSoundManager().stop(instance);
        }
        PLAYING.clear();
    }
}