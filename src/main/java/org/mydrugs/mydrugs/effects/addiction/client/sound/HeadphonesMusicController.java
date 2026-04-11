package org.mydrugs.mydrugs.effects.addiction.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.ModSounds;

public final class HeadphonesMusicController {
    private static boolean enabled;
    private static int trackNonce;
    private static int appliedNonce = Integer.MIN_VALUE;
    private static SoundInstance current;

    private HeadphonesMusicController() {
    }

    public static void apply(boolean enabled, int trackNonce) {
        HeadphonesMusicController.enabled = enabled;
        HeadphonesMusicController.trackNonce = trackNonce;

        Minecraft mc = Minecraft.getInstance();
        if (!enabled) {
            stopCustom(mc.getSoundManager());

            // Optional: resume vanilla music immediately when headphones turn off.
            mc.getMusicManager().startPlaying(mc.getSituationalMusic());
        }
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            stopCustom(mc.getSoundManager());
            return;
        }

        SoundManager soundManager = mc.getSoundManager();

        boolean stillHasHeadphones = false;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            if (mc.player.getInventory().getItem(i).is(ModItems.HEADPHONES.get())) {
                stillHasHeadphones = true;
                break;
            }
        }

        if (!enabled || !stillHasHeadphones) {
            stopCustom(soundManager);
            return;
        }

        // Suppress vanilla ambient/background music while headphones are active.
        mc.getMusicManager().stopPlaying();

        boolean needsRestart = current == null
                || !soundManager.isActive(current)
                || appliedNonce != trackNonce;

        if (needsRestart) {
            stopCustom(soundManager);
            current = SimpleSoundInstance.forMusic(ModSounds.GOODVIBES_MUSIC.get(), 1.0f);
            soundManager.play(current);
            appliedNonce = trackNonce;
        }
    }

    private static void stopCustom(SoundManager soundManager) {
        if (current != null) {
            soundManager.stop(current);
            current = null;
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }
}