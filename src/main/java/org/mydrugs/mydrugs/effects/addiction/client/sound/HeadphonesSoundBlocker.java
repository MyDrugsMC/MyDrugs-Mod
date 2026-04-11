package org.mydrugs.mydrugs.effects.addiction.client.sound;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import org.mydrugs.mydrugs.MyDrugs;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class HeadphonesSoundBlocker {
    private static final ResourceLocation GOODVIBES_EVENT =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "goodvibes_music");

    private HeadphonesSoundBlocker() {
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (!HeadphonesMusicController.isEnabled()) {
            return;
        }

        SoundInstance sound = event.getSound();
        if (sound == null) {
            return;
        }

        if (sound.getSource() != SoundSource.MUSIC) {
            return;
        }

        ResourceLocation location = sound.getLocation();

        // Allow our own headphones music through.
        if (GOODVIBES_EVENT.equals(location)) {
            return;
        }

        // Block vanilla / other music while headphones are on.
        event.setSound(null);
    }
}