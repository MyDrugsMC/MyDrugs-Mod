package org.mydrugs.mydrugs.menu.client.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.mydrugs.mydrugs.machine.MachineStatus;

public final class MachineStatusRenderer {
    private static final int HEIGHT = 14;

    private MachineStatusRenderer() {
    }

    public static void render(GuiGraphics graphics, Font font, int x, int y, int width, MachineStatus status) {
        if (status == MachineStatus.IDLE) {
            return;
        }

        int color = switch (status) {
            case RUNNING -> 0xFF7EDC8D;
            case UNKNOWN_ERROR -> 0xFFFF7878;
            default -> 0xFFFFD37A;
        };

        graphics.fill(x, y, x + width, y + HEIGHT, 0xB0181818);
        graphics.fill(x, y, x + 3, y + HEIGHT, color);
        graphics.drawString(font, Component.translatable(status.translationKey()), x + 7, y + 3, 0xFFECECEC, false);
    }
}
