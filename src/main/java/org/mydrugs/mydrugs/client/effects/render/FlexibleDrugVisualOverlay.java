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
        if (!Config.CLIENT.enableDrugShaders.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        float visualAccuracy = MutationClientState.get(MutationStat.VISUAL_ACCURACY);
        float negativeScale = Math.max(0.0F, 1.0F - visualAccuracy);
        float blur = AddictionClientState.getEffectIntensity(EffectType.BLUR) * negativeScale;
        float confusion = AddictionClientState.getEffectIntensity(EffectType.CONFUSION) * negativeScale;
        float nausea = AddictionClientState.getEffectIntensity(EffectType.CUSTOM_NAUSEA) * negativeScale;
        float adrenaline = AddictionClientState.getEffectIntensity(EffectType.ADRENALINE_SURGE);
        float strength = Mth.clamp((blur * 0.55F + confusion * 0.35F + nausea * 0.25F) * Config.CLIENT.shaderIntensity.get().floatValue(), 0.0F, 0.75F);
        float adrenalineStrength = Mth.clamp(adrenaline * Config.CLIENT.shaderIntensity.get().floatValue(), 0.0F, 1.0F);
        if (strength <= 0.002F && adrenalineStrength <= 0.002F) {
            return;
        }

        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        if (strength > 0.002F) {
            int alpha = Math.round(95.0F * strength);
            graphics.fill(0, 0, width, height, (alpha << 24) | 0x241A2A);
        }

        if (adrenalineStrength > 0.002F) {
            int edge = Math.round(9.0F + adrenalineStrength * 22.0F);
            int alpha = Math.round(42.0F + adrenalineStrength * 80.0F);
            int color = (alpha << 24) | 0x7A0613;
            graphics.fill(0, 0, width, edge, color);
            graphics.fill(0, height - edge, width, height, color);
            graphics.fill(0, 0, edge, height, color);
            graphics.fill(width - edge, 0, width, height, color);

            if (!Config.CLIENT.reducedMotionMode.get()) {
                int veinAlpha = Math.round(35.0F * adrenalineStrength);
                int veinColor = (veinAlpha << 24) | 0x2A0004;
                int tick = mc.player.tickCount;
                for (int i = 0; i < 5; i++) {
                    int y = Math.floorMod(tick * (i + 2) + i * 31, Math.max(1, height));
                    graphics.fill(0, y, edge + i * 3, y + 1, veinColor);
                    graphics.fill(width - edge - i * 3, height - y - 1, width, height - y, veinColor);
                }
            }
        }

        if (!Config.CLIENT.reducedMotionMode.get()) {
            int pulseAlpha = Math.round(45.0F * strength * (0.5F + 0.5F * Mth.sin((mc.player.tickCount % 80) / 80.0F * ((float) Math.PI * 2.0F))));
            graphics.fill(0, 0, width, height, (pulseAlpha << 24) | 0x5A355F);
        }
    }
}
