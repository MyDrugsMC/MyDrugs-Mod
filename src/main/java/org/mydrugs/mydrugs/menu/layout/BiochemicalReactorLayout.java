package org.mydrugs.mydrugs.menu.layout;

public final class BiochemicalReactorLayout {

    public static final int GUI_WIDTH = 196;

    public static final int MACHINE_PANEL_X = 10;
    public static final int MACHINE_PANEL_Y = 9;
    public static final int MACHINE_PANEL_W = 176;
    public static final int MACHINE_PANEL_H = 93;

    public static final int ERGOT_SLOT_X = 22;
    public static final int ERGOT_SLOT_Y = 32;

    public static final int TRYPTOPHAN_SLOT_X = 22;
    public static final int TRYPTOPHAN_SLOT_Y = 56;

    public static final int CHARCOAL_SLOT_X = 22;
    public static final int CHARCOAL_SLOT_Y = 80;

    public static final int PROGRESS_X = 58;
    public static final int PROGRESS_Y = 40;
    public static final int PROGRESS_W = 58;
    public static final int PROGRESS_H = 8;

    public static final int MANUAL_BUTTON_X = 78;
    public static final int MANUAL_BUTTON_Y = 62;
    public static final int MANUAL_BUTTON_W = 18;
    public static final int MANUAL_BUTTON_H = 18;

    public static final int HEAT_BAR_X = 126;
    public static final int HEAT_BAR_Y = 24;
    public static final int HEAT_BAR_W = 12;
    public static final int HEAT_BAR_H = 54;

    public static final int HEAT_BAR_INNER_X_OFFSET = 2;
    public static final int HEAT_BAR_INNER_Y_OFFSET = 2;
    public static final int HEAT_BAR_INNER_W = 8;
    public static final int HEAT_BAR_INNER_H = 50;

    public static final int MANUAL_BAR_X = 142;
    public static final int MANUAL_BAR_Y = 24;
    public static final int MANUAL_BAR_W = 12;
    public static final int MANUAL_BAR_H = 54;

    public static final int MANUAL_BAR_INNER_X_OFFSET = 2;
    public static final int MANUAL_BAR_INNER_Y_OFFSET = 2;
    public static final int MANUAL_BAR_INNER_W = 8;
    public static final int MANUAL_BAR_INNER_H = 50;

    public static final int OUTPUT_TANK_X = 160;
    public static final int OUTPUT_TANK_Y = 24;
    public static final int TANK_W = 16;
    public static final int TANK_H = 54;

    public static final int TANK_INNER_X_OFFSET = 2;
    public static final int TANK_INNER_Y_OFFSET = 2;
    public static final int TANK_INNER_W = 12;
    public static final int TANK_INNER_H = 50;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN;

    public static final int OUTPUT_SLOT_X = 160;
    public static final int OUTPUT_SLOT_Y = 82;

    public static final int GUI_HEIGHT = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN + StandardInventoryLayout.TOTAL_H + MACHINE_PANEL_Y;

    private BiochemicalReactorLayout() {
    }
}