package org.mydrugs.mydrugs.effects.addiction.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.MyDrugs;

public final class BadTripScreamerOverlay {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/gui/overlay/bad_trip_screamer.png");
    private static int ticksRemaining;
    private static int totalTicks;
    private static float intensity;

    private BadTripScreamerOverlay() {
    }

    public static void trigger(int durationTicks, float rawIntensity) {
        if (!Config.CLIENT.enableBadTripScreamers.get()) {
            return;
        }
        float configScale = Config.CLIENT.screamerIntensity.get().floatValue();
        float motionScale = Config.CLIENT.reducedMotionMode.get() ? 0.55F : 1.0F;
        int duration = Config.CLIENT.reducedMotionMode.get()
                ? Math.min(12, Math.max(6, durationTicks / 2))
                : Mth.clamp(durationTicks, 10, 20);

        intensity = Math.max(intensity, Mth.clamp(rawIntensity * configScale * motionScale, 0.15F, 1.0F));
        totalTicks = duration;
        ticksRemaining = Math.max(ticksRemaining, duration);
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
        float age = 1.0F - ticksRemaining / (float) Math.max(1, totalTicks);
        float fade = ticksRemaining / (float) Math.max(1, totalTicks);
        float flash = age < 0.18F ? 1.0F : fade;
        float alpha = Mth.clamp(flash * intensity, 0.0F, Config.CLIENT.reducedMotionMode.get() ? 0.55F : 0.85F);

        int shake = 0;
        if (!Config.CLIENT.reducedMotionMode.get()) {
            shake = Math.round((Mth.sin((Minecraft.getInstance().player == null ? 0 : Minecraft.getInstance().player.tickCount) * 2.4F) * 4.0F) * alpha);
        }

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                -shake,
                shake,
                0.0F,
                0.0F,
                width + Math.abs(shake) * 2,
                height + Math.abs(shake) * 2,
                width + Math.abs(shake) * 2,
                height + Math.abs(shake) * 2
        );

        int fillAlpha = Math.round((Config.CLIENT.reducedMotionMode.get() ? 60.0F : 95.0F) * alpha);
        graphics.fill(0, 0, width, height, (fillAlpha << 24) | 0x220006);
    }
}
