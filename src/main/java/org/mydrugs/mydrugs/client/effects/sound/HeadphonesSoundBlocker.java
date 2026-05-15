package org.mydrugs.mydrugs.client.effects.sound;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.sound.PlaySoundEvent;
import org.mydrugs.mydrugs.MyDrugs;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class HeadphonesSoundBlocker {
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

        // Block vanilla music only.
        if (sound.getSource() == SoundSource.MUSIC) {
            event.setSound(null);
        }
    }
}