package org.mydrugs.mydrugs.client.effects.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.mydrugs.mydrugs.client.effects.AddictionClientState;
import org.mydrugs.mydrugs.addiction.config.SymptomFlags;

public final class VisionEffectRenderer {
    private VisionEffectRenderer() {
    }

    public static void renderOverlay(GuiGraphics guiGraphics) {
        if (!AddictionClientState.has(SymptomFlags.VISION)) return;

        Minecraft mc = Minecraft.getInstance();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        int alpha = (int) (60 + AddictionClientState.globalSeverity * 100.0F);
        int color = (alpha << 24);

        guiGraphics.fill(0, 0, w, 10, color);
        guiGraphics.fill(0, h - 10, w, h, color);
        guiGraphics.fill(0, 0, 10, h, color);
        guiGraphics.fill(w - 10, 0, w, h, color);
    }

    public static float modifyFov(float originalFov) {
        if (!AddictionClientState.has(SymptomFlags.VISION)) return originalFov;
        return originalFov - (AddictionClientState.globalSeverity * 8.0F);
    }
}