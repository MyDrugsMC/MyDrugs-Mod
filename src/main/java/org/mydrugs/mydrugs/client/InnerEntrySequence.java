package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.dimension.InnerDimensions;
import org.mydrugs.mydrugs.sounds.ModSounds;

/**
 * Phase 6 — the entry sequence. Detects an actual dimension change into {@code INNER_LEVEL}
 * (client-side, by watching the local level's dimension key) and plays a short choreographed intro:
 * the screen resolves from a near-opaque veil through a held beat of near-silence, the soundscape
 * swells in (Phase 5 starts naturally), the core beacon ignites (Phase 1 brightness ramps as the
 * sync arrives), and a soft breath one-shot plays.
 *
 * <p>~3.6s long, skippable by any movement input after a brief minimum, and reduced-motion-safe:
 * the veil fade is always kept (no camera/shader drama is added here), so reduced motion simply
 * gets the fade and the breath. Fires only on real entry — the dimension key only changes on
 * teleport, never on region/chunk load.
 */
public final class InnerEntrySequence {
    private static final int DURATION = 72;   // ~3.6s
    private static final int HOLD = 16;       // held near-silent veil before it pulls back
    private static final int MIN_SKIP = 10;   // can't skip in the very first moment

    @Nullable
    private static ResourceKey<Level> lastDimension;
    private static boolean active;
    private static int ticks;

    private InnerEntrySequence() {
    }

    public static void tick(Minecraft mc) {
        ResourceKey<Level> current = (mc.level == null) ? null : mc.level.dimension();
        boolean entered = current != null
                && current.equals(InnerDimensions.INNER_LEVEL)
                && !current.equals(lastDimension);
        lastDimension = current;

        if (entered) {
            start();
        }

        if (!active) {
            return;
        }
        // No longer in the dimension (left/kicked) — abort cleanly.
        if (mc.player == null || current == null || !current.equals(InnerDimensions.INNER_LEVEL)) {
            active = false;
            return;
        }

        ticks++;
        if (ticks >= DURATION || (ticks >= MIN_SKIP && movementPressed(mc))) {
            active = false;
        }
    }

    private static void start() {
        active = true;
        ticks = 0;
        // Soft held breath as the world surfaces. Kept under reduced motion.
        InnerSoundscapeController.playOneShot(ModSounds.INNER_BREATH.get(), 0.7F, 1.0F);
    }

    public static void render(GuiGraphics graphics) {
        if (!active) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        float veil;
        if (ticks <= HOLD) {
            veil = 1.0F; // held beat — world not yet resolved
        } else {
            veil = 1.0F - (ticks - HOLD) / (float) (DURATION - HOLD);
        }
        veil = Mth.clamp(veil, 0.0F, 1.0F);
        if (veil <= 0.002F) {
            return;
        }
        int flashCap = Math.round(255.0F * Config.CLIENT.flashCap());
        int alpha = Math.min(flashCap, Math.round(255.0F * veil));
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        // A pale dream veil that pulls back to reveal the world.
        graphics.fill(0, 0, width, height, (alpha << 24) | 0xEDF0FB);
    }

    /** True while the intro veil is showing — lets other effects defer their drama if desired. */
    public static boolean isActive() {
        return active;
    }

    public static void clear() {
        active = false;
        ticks = 0;
        lastDimension = null;
    }

    private static boolean movementPressed(Minecraft mc) {
        return mc.options.keyUp.isDown()
                || mc.options.keyDown.isDown()
                || mc.options.keyLeft.isDown()
                || mc.options.keyRight.isDown()
                || mc.options.keyJump.isDown();
    }
}
