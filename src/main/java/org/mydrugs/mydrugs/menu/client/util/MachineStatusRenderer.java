package org.mydrugs.mydrugs.menu.client.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.client.ui.MyDrugsUiTheme;
import org.mydrugs.mydrugs.machine.MachineStatus;

import java.util.List;

public final class MachineStatusRenderer {
    private static final int BASE_HEIGHT = 14;
    private static final int DETAIL_LINE_HEIGHT = 10;
    private static final int MAX_DETAIL_LINES = 3;
    private static final int BADGE_WIDTH = 25;

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
        Component badge = Component.translatable("machine_status_badge.mydrugs." + badgeGroup(status));
        int badgeBorder = Config.CLIENT.colorblindSafeMode.get() ? MyDrugsUiTheme.STRONG_BORDER : color;

        graphics.fill(x, panelY, x + width, panelY + height, MyDrugsUiTheme.PANEL_BACKGROUND);
        graphics.fill(x, panelY, x + 3, panelY + height, color);
        MyDrugsUiTheme.drawBadge(
                graphics, font, badge, x + 6, panelY + 2, BADGE_WIDTH,
                MyDrugsUiTheme.NORMAL_TEXT, badgeBorder);
        graphics.drawString(
                font,
                Component.translatable(status.translationKey()),
                x + 35,
                panelY + 3,
                MyDrugsUiTheme.NORMAL_TEXT,
                false
        );
        int detailY = panelY + 14;
        for (Component line : details) {
            graphics.drawString(
                    font,
                    line,
                    x + 35,
                    detailY,
                    MyDrugsUiTheme.DIM_TEXT,
                    false
            );
            detailY += DETAIL_LINE_HEIGHT;
        }
    }

    private static int statusColor(MachineStatus status) {
        return switch (status) {
            case RUNNING -> MyDrugsUiTheme.SUCCESS;
            case UNKNOWN_ERROR, INVALID_RECIPE_OUTPUT, INVALID_MULTIBLOCK, DIMENSION_UNAVAILABLE ->
                    MyDrugsUiTheme.DANGER;
            case IDLE, PAUSED -> MyDrugsUiTheme.DIM_TEXT;
            default -> MyDrugsUiTheme.WARNING;
        };
    }

    private static String badgeGroup(MachineStatus status) {
        return switch (status) {
            case RUNNING -> "running";
            case IDLE, PAUSED -> "neutral";
            case MISSING_INPUT_ITEM, MISSING_INPUT_FLUID, MISSING_INPUT_GAS, MISSING_CATALYST,
                    MISSING_CONTAINER, INSUFFICIENT_INPUT_FLUID -> "input";
            case OUTPUT_SLOT_FULL, OUTPUT_TANK_FULL, OUTPUT_GAS_TANK_FULL, OUTPUT_TANK_A_FULL,
                    OUTPUT_TANK_B_FULL, INVALID_RECIPE_OUTPUT -> "output";
            case NOT_ENOUGH_ENERGY -> "power";
            case NOT_ENOUGH_HEAT -> "heat";
            case NO_MATCHING_RECIPE -> "recipe";
            case BLOCKED, BLOCKED_BY_TRANSFER -> "blocked";
            case INVALID_MULTIBLOCK, DIMENSION_UNAVAILABLE -> "structure";
            case UNKNOWN_ERROR -> "error";
            case MISSING_DIARY_CONTEXT, MISSING_RECOVERY_CONTEXT -> "context";
        };
    }
}
