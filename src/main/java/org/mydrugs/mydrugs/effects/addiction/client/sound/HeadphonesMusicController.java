package org.mydrugs.mydrugs.effects.addiction.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.items.ModItems;

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
            stopCustom(mc);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void clear() {
        enabled = false;
        trackNonce = 0;
        appliedNonce = Integer.MIN_VALUE;
        stopCustom(Minecraft.getInstance());
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            stopCustom(mc);
            return;
        }

        boolean stillHasHeadphones = false;
        for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            if (mc.player.getInventory().getItem(i).is(ModItems.HEADPHONES.get())) {
                stillHasHeadphones = true;
                break;
            }
        }

        if (!enabled || !stillHasHeadphones) {
            stopCustom(mc);
            return;
        }

        // Kill any currently running vanilla music.
        mc.getMusicManager().stopPlaying();

        boolean needsRestart =
                current == null
                        || !mc.getSoundManager().isActive(current)
                        || appliedNonce != trackNonce;

        if (needsRestart) {
            stopCustom(mc);

            current = new SimpleSoundInstance(
                    ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "goodvibes_music"),
                    SoundSource.RECORDS,
                    1.0F,
                    1.0F,
                    RandomSource.create(),
                    false,
                    0,
                    SoundInstance.Attenuation.NONE,
                    0.0,
                    0.0,
                    0.0,
                    true
            );

            mc.getSoundManager().play(current);
            appliedNonce = trackNonce;
        }
    }

    private static void stopCustom(Minecraft mc) {
        if (current != null) {
            mc.getSoundManager().stop(current);
            current = null;
        }
    }
}
