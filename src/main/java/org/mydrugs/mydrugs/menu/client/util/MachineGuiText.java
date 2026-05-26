package org.mydrugs.mydrugs.menu.client.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

final class MachineGuiText {
    private MachineGuiText() {
    }

    static void drawCentered(GuiGraphics graphics, Font font, String text, int x, int y, int width, int color) {
        String clipped = font.plainSubstrByWidth(text, width);
        graphics.drawString(font, clipped, x + Math.max(0, (width - font.width(clipped)) / 2), y, color, false);
    }
}
