package org.mydrugs.mydrugs.client.effects.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.MyDrugs;

public final class VomitOverlayClientState {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/gui/overlay/vomit_overlay.png");
    private static final int TEXTURE_SIZE = 64;
    private static int ticksRemaining;
    private static int totalTicks;
    private static float intensity;

    private VomitOverlayClientState() {
    }

    public static void trigger(float newIntensity) {
        intensity = Math.max(intensity, Mth.clamp(newIntensity, 0.15F, 1.0F));
        totalTicks = Config.CLIENT.reducedMotionMode.get() ? 36 : 56;
        ticksRemaining = Math.max(ticksRemaining, totalTicks);
    }

    public static void clear() {
        ticksRemaining = 0;
        totalTicks = 0;
        intensity = 0.0F;
    }

    public static void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
        }
    }

    public static void render(GuiGraphics graphics) {
        if (ticksRemaining <= 0) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        float progress = 1.0F - ticksRemaining / (float) Math.max(1, totalTicks);
        float alpha = Mth.clamp((ticksRemaining / (float) Math.max(1, totalTicks)) * intensity, 0.0F, 0.85F);
        int slide = Config.CLIENT.reducedMotionMode.get()
                ? Math.round(progress * 8.0F)
                : Math.round(progress * height * 0.20F);

        for (int y = -TEXTURE_SIZE + slide; y < height; y += TEXTURE_SIZE) {
            for (int x = 0; x < width; x += TEXTURE_SIZE) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        TEXTURE,
                        x,
                        y,
                        0,
                        0,
                        TEXTURE_SIZE,
                        TEXTURE_SIZE,
                        TEXTURE_SIZE,
                        TEXTURE_SIZE
                );
            }
        }

        int fillAlpha = Math.round(95.0F * alpha);
        graphics.fill(0, 0, width, height, (fillAlpha << 24) | 0x5D4A17);
    }
}
