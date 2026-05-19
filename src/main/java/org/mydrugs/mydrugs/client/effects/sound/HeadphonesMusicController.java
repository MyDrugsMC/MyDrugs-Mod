package org.mydrugs.mydrugs.client.effects.sound;

import org.mydrugs.mydrugs.addiction.network.HeadphonesStatePayload;
import org.mydrugs.mydrugs.client.recovery.music.CustomMusicPlayer;

public final class HeadphonesMusicController {
    private HeadphonesMusicController() {
    }

    public static void apply(HeadphonesStatePayload payload) {
        CustomMusicPlayer.get().applyHeadphonesState(payload);
    }

    public static boolean isEnabled() {
        return CustomMusicPlayer.get().isPlaying();
    }

    public static void clear() {
        CustomMusicPlayer.get().clear();
    }

    public static void tick() {
        CustomMusicPlayer.get().tick();
    }
}
