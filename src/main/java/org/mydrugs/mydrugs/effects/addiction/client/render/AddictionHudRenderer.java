package org.mydrugs.mydrugs.effects.addiction.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.mydrugs.mydrugs.effects.addiction.client.AddictionClientState;

public final class AddictionHudRenderer {
    private AddictionHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        int x = 10;
        int y = 10;
        int width = 100;
        int height = 8;

        int severityFill = (int) (width * AddictionClientState.globalSeverity);
        int stressFill = (int) (width * AddictionClientState.stressLevel);

        guiGraphics.fill(x, y, x + width, y + height, 0x66000000);
        guiGraphics.fill(x, y, x + severityFill, y + height, 0x88CC3333);

        guiGraphics.drawString(mc.font, "Withdrawal", x, y - 10, 0xFFFFFF, false);

        y += 16;
        guiGraphics.fill(x, y, x + width, y + height, 0x66000000);
        guiGraphics.fill(x, y, x + stressFill, y + height, 0x889955FF);
        guiGraphics.drawString(mc.font, "Stress", x, y - 10, 0xFFFFFF, false);
    }
}