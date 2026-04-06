package org.mydrugs.mydrugs.menu.layout;

public final class SieveLayout {
    public static final int GUI_WIDTH = 176;
    public static final int MACHINE_PANEL_X = 19;
    public static final int MACHINE_PANEL_Y = 12;
    public static final int MACHINE_PANEL_W = 136;
    public static final int MACHINE_PANEL_H = 41;
    public static final int INPUT_X = 41;
    public static final int INPUT_Y = (MACHINE_PANEL_H / 2) + 4;
    public static final int WIDGET_X = 79;
    public static final int WIDGET_Y = 19;
    public static final int WIDGET_W = 17;
    public static final int WIDGET_H = 28;
    public static final int RESULT_X = 122;
    public static final int RESULT_Y = 16;
    public static final int BONUS_X = 122;
    public static final int BONUS_Y = 33;
    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN;

    public static final int GUI_HEIGHT = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN + StandardInventoryLayout.TOTAL_H + MACHINE_PANEL_Y;

    private SieveLayout() {
    }
}