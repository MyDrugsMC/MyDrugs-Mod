package org.mydrugs.mydrugs.menu.client.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.mydrugs.mydrugs.machine.MachineStatus;

import java.util.List;

public final class MachineStatusRenderer {
    private static final int BASE_HEIGHT = 14;
    private static final int DETAIL_LINE_HEIGHT = 10;
    private static final int MAX_DETAIL_LINES = 3;

    private MachineStatusRenderer() {
    }

    public static void render(GuiGraphics graphics, Font font, int x, int y, int width, MachineStatus status) {
        render(graphics, font, x, y, width, status, List.of());
    }

    public static void render(GuiGraphics graphics, Font font, int x, int y, int width, MachineStatus status, List<Component> detailLines) {
        List<Component> details = detailLines == null ? List.of() : detailLines.stream()
                .filter(line -> line != null && line != Component.empty())
                .limit(MAX_DETAIL_LINES)
                .toList();
        if (status == MachineStatus.IDLE && details.isEmpty()) {
            return;
        }

        int height = BASE_HEIGHT + details.size() * DETAIL_LINE_HEIGHT;
        int panelY = details.isEmpty() ? y : y - details.size() * DETAIL_LINE_HEIGHT;
        int color = statusColor(status);

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
        int detailY = panelY + 14;
        for (Component line : details) {
            graphics.drawString(
                    font,
                    line,
                    x + 7,
                    detailY,
                    0xFFBFC7BF,
                    false
            );
            detailY += DETAIL_LINE_HEIGHT;
        }
    }

    private static int statusColor(MachineStatus status) {
        return switch (status) {
            case RUNNING -> 0xFF7EDC8D;
            case UNKNOWN_ERROR, INVALID_RECIPE_OUTPUT, INVALID_MULTIBLOCK, DIMENSION_UNAVAILABLE -> 0xFFFF7878;
            case IDLE, PAUSED -> 0xFFBFC7BF;
            default -> 0xFFFFD37A;
        };
    }
}
