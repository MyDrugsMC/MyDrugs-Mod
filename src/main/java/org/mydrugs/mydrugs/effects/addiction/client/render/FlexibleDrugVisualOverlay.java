package org.mydrugs.mydrugs.effects.addiction.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.client.AddictionClientState;

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

        float blur = AddictionClientState.getEffectIntensity(EffectType.BLUR);
        float confusion = AddictionClientState.getEffectIntensity(EffectType.CONFUSION);
        float nausea = AddictionClientState.getEffectIntensity(EffectType.CUSTOM_NAUSEA);
        float strength = Mth.clamp((blur * 0.55F + confusion * 0.35F + nausea * 0.25F) * Config.CLIENT.shaderIntensity.get().floatValue(), 0.0F, 0.75F);
        if (strength <= 0.002F) {
            return;
        }

        int alpha = Math.round(95.0F * strength);
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        graphics.fill(0, 0, width, height, (alpha << 24) | 0x241A2A);

        if (!Config.CLIENT.reducedMotionMode.get()) {
            int pulseAlpha = Math.round(45.0F * strength * (0.5F + 0.5F * Mth.sin((mc.player.tickCount % 80) / 80.0F * ((float) Math.PI * 2.0F))));
            graphics.fill(0, 0, width, height, (pulseAlpha << 24) | 0x5A355F);
        }
    }
}
