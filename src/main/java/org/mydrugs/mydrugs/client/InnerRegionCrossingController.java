package org.mydrugs.mydrugs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.client.shaders.InnerAtmosphereShader;
import org.mydrugs.mydrugs.dimension.inner.InnerAtmosphere;
import org.mydrugs.mydrugs.dimension.inner.InnerSceneType;
import org.mydrugs.mydrugs.sounds.ModSounds;

/**
 * Phase 3 — crossing a region boundary as a threshold moment. Tracks the player's current
 * {@link InnerSceneType}; on a change it fires a brief, debounced choreographed sting:
 *
 * <ul>
 *   <li>a short post-pulse spike on {@link InnerAtmosphereShader} that decays over ~0.8s;</li>
 *   <li>a screen-edge flash in the new region's hue (the cached fog/mood colour);</li>
 *   <li>a one-shot crossing sound through the Phase 5 soundscape.</li>
 * </ul>
 *
 * <p>Everything scales with {@code transitionStrength}. Under reduced motion the edge flash is
 * replaced by a gentle, slowly decaying full-screen tint (the "1 fps colour shift") and only the
 * sound and a soft tint remain — never a strobe. A debounce window prevents rapid back-and-forth at
 * a seam from machine-gunning the effect.
 */
public final class InnerRegionCrossingController {
    private static final int DEBOUNCE_TICKS = 30;

    private static InnerSceneType lastScene;
    private static int cooldown;

    private static float flashIntensity;
    private static int flashColor = 0xFFFFFF;
    private static boolean flashGentle;

    private InnerRegionCrossingController() {
    }

    public static void tick(Minecraft mc) {
        if (cooldown > 0) {
            cooldown--;
        }
        if (!InnerAtmosphereClient.inInnerDimension(mc)) {
            lastScene = null;
            return;
        }
        InnerAtmosphere.Sample sample = InnerAtmosphereClient.current(mc);
        if (sample == null) {
            return;
        }
        InnerSceneType scene = sample.sceneType();
        if (lastScene == null) {
            // First sample after entry: establish baseline without a sting.
            lastScene = scene;
            return;
        }
        if (scene != lastScene) {
            lastScene = scene;
            if (cooldown <= 0) {
                trigger(sample);
                cooldown = DEBOUNCE_TICKS;
            }
        }
    }

    private static void trigger(InnerAtmosphere.Sample sample) {
        float transition = (float) Mth.clamp(sample.transitionStrength(), 0.0D, 1.0D);
        float scale = 0.3F + transition * 0.7F;
        boolean reduced = reducedMotion();

        // Post-pulse (no-op visually if the post pass is disabled / reduced motion).
        InnerAtmosphereShader.INSTANCE.addCrossingPulse(0.5F * scale);

        // One-shot sting — always kept, even under reduced motion.
        InnerSoundscapeController.playOneShot(ModSounds.INNER_CROSSING.get(), 0.55F * scale + 0.15F, 1.0F);

        // Edge flash in the new region's hue.
        flashColor = (sample.fogRed() << 16) | (sample.fogGreen() << 8) | sample.fogBlue();
        flashGentle = reduced;
        flashIntensity = reduced ? Math.min(0.5F, scale * 0.5F) : scale;
    }

    public static void render(GuiGraphics graphics) {
        if (flashIntensity <= 0.002F) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !InnerAtmosphereClient.inInnerDimension(mc)) {
            flashIntensity = 0.0F;
            return;
        }
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int flashAlphaCap = Math.round(255.0F * Config.CLIENT.flashCap());

        if (flashGentle || Config.CLIENT.disableFullScreenFlashing.get()) {
            // Gentle slowly-decaying full-screen tint (reduced-motion / no-flash path).
            int alpha = Math.min(flashAlphaCap, Math.round(50.0F * flashIntensity));
            graphics.fill(0, 0, width, height, (alpha << 24) | flashColor);
        } else {
            // Screen-edge flash: thicker, brighter at the rim, fading inward.
            int edge = Math.round(10.0F + flashIntensity * 26.0F);
            int alpha = Math.min(flashAlphaCap, Math.round(60.0F + flashIntensity * 120.0F));
            int color = (alpha << 24) | flashColor;
            graphics.fill(0, 0, width, edge, color);
            graphics.fill(0, height - edge, width, height, color);
            graphics.fill(0, 0, edge, height, color);
            graphics.fill(width - edge, 0, width, height, color);
        }

        // Decay: ~0.8s normal, much slower (gentle shift) under reduced motion.
        flashIntensity *= flashGentle ? 0.97F : 0.85F;
        if (flashIntensity < 0.002F) {
            flashIntensity = 0.0F;
        }
    }

    public static void clear() {
        lastScene = null;
        cooldown = 0;
        flashIntensity = 0.0F;
    }

    private static boolean reducedMotion() {
        try {
            return Config.CLIENT.reducedMotionMode.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }
}
