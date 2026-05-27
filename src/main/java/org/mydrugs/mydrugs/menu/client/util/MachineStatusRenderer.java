package org.mydrugs.mydrugs.menu.client.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.mydrugs.mydrugs.machine.MachineStatus;

public final class MachineStatusRenderer {
    private static final int BASE_HEIGHT = 14;
    private static final int EXTENDED_HEIGHT = 24;
    private static final int HINT_OFFSET_Y = 10;

    private MachineStatusRenderer() {
    }

    public static void render(GuiGraphics graphics, Font font, int x, int y, int width, MachineStatus status) {
        if (status == MachineStatus.IDLE) {
            return;
        }

        boolean showHandbookHint = shouldShowHandbookHint(status);
        int panelY = showHandbookHint ? y - HINT_OFFSET_Y : y;
        int height = showHandbookHint ? EXTENDED_HEIGHT : BASE_HEIGHT;
        int color = switch (status) {
            case RUNNING -> 0xFF7EDC8D;
            case UNKNOWN_ERROR -> 0xFFFF7878;
            default -> 0xFFFFD37A;
        };

        graphics.fill(x, panelY, x + width, panelY + height, 0xB0181818);
        graphics.fill(x, panelY, x + 3, panelY + height, color);
        graphics.drawString(
                font,
                Component.translatable(status.translationKey()),
                x + 7,
                panelY + 3,
                0xFFECECEC,
                false
        );
        if (showHandbookHint) {
            graphics.drawString(
                    font,
                    Component.translatable("screen.mydrugs.machine_handbook.status_hint"),
                    x + 7,
                    panelY + 14,
                    0xFFBFC7BF,
                    false
            );
        }
    }

    private static boolean shouldShowHandbookHint(MachineStatus status) {
        return status != MachineStatus.RUNNING && status != MachineStatus.PAUSED;
    }
}
