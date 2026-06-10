package org.mydrugs.mydrugs.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.mydrugs.mydrugs.Config;

public final class MyDrugsUiTheme {
    public static final int PANEL_BACKGROUND = 0xE8151C19;
    public static final int HEADER = 0xFF1F2A26;
    public static final int ACCENT = 0xFF4E8C77;
    public static final int DIM_TEXT = 0xFF8B9B94;
    public static final int NORMAL_TEXT = 0xFFE7F1EC;
    public static final int WARNING = 0xFFF2C261;
    public static final int DANGER = 0xFFE77D7D;
    public static final int SUCCESS = 0xFF7FD6A6;
    public static final int DISABLED_OVERLAY = 0x990B0D0C;
    public static final int SUBTLE_BORDER = 0xFF2B3F37;
    public static final int STRONG_BORDER = 0xFFE7F1EC;

    private MyDrugsUiTheme() {
    }

    public static int contrastBorder() {
        return Config.CLIENT.colorblindSafeMode.get() ? STRONG_BORDER : SUBTLE_BORDER;
    }

    public static void drawBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y, x + 1, y + height, color);
        graphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    public static void drawBadge(
            GuiGraphics graphics,
            Font font,
            Component text,
            int x,
            int y,
            int width,
            int foreground,
            int border
    ) {
        graphics.fill(x, y, x + width, y + 11, 0xD0161A18);
        drawBorder(graphics, x, y, width, 11, border);
        graphics.drawCenteredString(font, text, x + width / 2, y + 2, foreground);
    }

    public static void drawDisabledOverlay(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, DISABLED_OVERLAY);
        drawBorder(graphics, x, y, width, height, contrastBorder());
    }
}
