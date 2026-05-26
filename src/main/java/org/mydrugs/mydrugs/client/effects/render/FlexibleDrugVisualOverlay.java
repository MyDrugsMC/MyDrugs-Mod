package org.mydrugs.mydrugs.client.effects.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.client.effects.AddictionClientState;
import org.mydrugs.mydrugs.mutation.MutationClientState;
import org.mydrugs.mydrugs.mutation.MutationStat;

public final class FlexibleDrugVisualOverlay {
    private FlexibleDrugVisualOverlay() {
    }

    public static void render(GuiGraphics graphics) {
        if (!Config.CLIENT.psychedelicShadersEnabled()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        float visualAccuracy = MutationClientState.get(MutationStat.VISUAL_ACCURACY);
        float negativeScale = Math.max(0.0F, 1.0F - visualAccuracy);
        float blur = AddictionClientState.getEffectIntensity(EffectType.BLUR)
                * negativeScale
                * Config.CLIENT.blurIntensity.get().floatValue();
        float confusion = AddictionClientState.getEffectIntensity(EffectType.CONFUSION) * negativeScale;
        float nausea = AddictionClientState.getEffectIntensity(EffectType.CUSTOM_NAUSEA) * negativeScale;
        float adrenaline = AddictionClientState.getEffectIntensity(EffectType.ADRENALINE_SURGE);
        float overlayScale = Config.CLIENT.visualScale();
        float strength = Mth.clamp((blur * 0.55F + confusion * 0.35F + nausea * 0.25F)
                * Config.CLIENT.shaderScale()
                * overlayScale, 0.0F, 0.75F);
        float adrenalineStrength = Mth.clamp(adrenaline
                * Config.CLIENT.shaderScale()
                * overlayScale
                * Config.CLIENT.colorShiftIntensity.get().floatValue(), 0.0F, 1.0F);
        if (strength <= 0.002F && adrenalineStrength <= 0.002F) {
            return;
        }

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int flashAlphaCap = Math.round(255.0F * Config.CLIENT.flashCap());
        if (strength > 0.002F) {
            int alpha = Math.min(flashAlphaCap, Math.round(95.0F * strength));
            int color = Config.CLIENT.colorblindSafeMode.get() ? 0x1C2430 : 0x241A2A;
            graphics.fill(0, 0, width, height, (alpha << 24) | color);
        }

        if (adrenalineStrength > 0.002F) {
            int edge = Math.round(9.0F + adrenalineStrength * 22.0F);
            int alpha = Math.min(flashAlphaCap, Math.round(42.0F + adrenalineStrength * 80.0F));
            int color = (alpha << 24) | 0x7A0613;
            graphics.fill(0, 0, width, edge, color);
            graphics.fill(0, height - edge, width, height, color);
            graphics.fill(0, 0, edge, height, color);
            graphics.fill(width - edge, 0, width, height, color);

            if (!Config.CLIENT.reducedMotionMode.get() && Config.CLIENT.staticNoiseIntensity.get() > 0.0D) {
                int veinAlpha = Math.round(35.0F * adrenalineStrength * Config.CLIENT.staticNoiseIntensity.get().floatValue());
                int veinColor = (veinAlpha << 24) | 0x2A0004;
                int tick = mc.player.tickCount;
                for (int i = 0; i < 5; i++) {
                    int y = Math.floorMod(tick * (i + 2) + i * 31, Math.max(1, height));
                    graphics.fill(0, y, edge + i * 3, y + 1, veinColor);
                    graphics.fill(width - edge - i * 3, height - y - 1, width, height - y, veinColor);
                }
            }
        }

        if (!Config.CLIENT.reducedMotionMode.get() && !Config.CLIENT.disableFullScreenFlashing.get()) {
            int pulseAlpha = Math.min(flashAlphaCap, Math.round(45.0F * strength * (0.5F + 0.5F * Mth.sin((mc.player.tickCount % 80) / 80.0F * ((float) Math.PI * 2.0F)))));
            graphics.fill(0, 0, width, height, (pulseAlpha << 24) | 0x5A355F);
        }
    }
}
