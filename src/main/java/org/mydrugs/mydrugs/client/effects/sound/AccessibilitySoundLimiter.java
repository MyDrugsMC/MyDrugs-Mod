package org.mydrugs.mydrugs.client.effects.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.MyDrugs;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class AccessibilitySoundLimiter {
    private AccessibilitySoundLimiter() {
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        String name = event.getName();
        if (name == null || !name.startsWith(MyDrugs.MODID + ":")) {
            return;
        }

        if (shouldMute(name)) {
            event.setSound(null);
            showTextAlternative(name);
        }
    }

    private static boolean shouldMute(String name) {
        if (name.contains("hallucination")) {
            return Config.CLIENT.hallucinationSoundVolume.get() <= 0.0D
                    || Config.CLIENT.disableSuddenLoudSounds.get();
        }
        if (name.contains("whisper")) {
            return Config.CLIENT.badTripVoiceVolume.get() <= 0.0D
                    || Config.CLIENT.disableSuddenLoudSounds.get();
        }
        if (name.contains("scream")) {
            return Config.CLIENT.muteScreamers.get()
                    || Config.CLIENT.screamerVolumeCap.get() <= 0.0D
                    || Config.CLIENT.disableSuddenLoudSounds.get();
        }
        return false;
    }

    private static void showTextAlternative(String name) {
        if (!Config.CLIENT.showActiveEffectExplanations.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (name.contains("hallucination") || name.contains("whisper") || name.contains("scream")) {
            mc.player.displayClientMessage(Component.translatable("message.mydrugs.audio_suppressed"), true);
        }
    }
}
