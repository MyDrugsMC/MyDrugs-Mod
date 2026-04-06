package org.mydrugs.mydrugs.menu.layout;

public final class DistillerLayout {
    public static final int GUI_WIDTH = 196;

    public static final int MACHINE_PANEL_X = 10;
    public static final int MACHINE_PANEL_Y = 9;
    public static final int MACHINE_PANEL_W = 176;
    public static final int MACHINE_PANEL_H = 93;

    public static final int INPUT_TANK_X = 22;
    public static final int INPUT_TANK_Y = 25;

    public static final int OUTPUT_A_TANK_X = 142;
    public static final int OUTPUT_A_TANK_Y = INPUT_TANK_Y;

    public static final int OUTPUT_B_TANK_X = 166;
    public static final int OUTPUT_B_TANK_Y = INPUT_TANK_Y;

    public static final int TANK_W = 16;
    public static final int TANK_H = 54;

    public static final int TANK_INNER_X_OFFSET = 2;
    public static final int TANK_INNER_Y_OFFSET = 2;
    public static final int TANK_INNER_W = 12;
    public static final int TANK_INNER_H = 50;

    public static final int DUMP_BUTTON_SIZE = 12;
    public static final int DUMP_BUTTON_Y = 11;
    public static final int DUMP_INPUT_X = 24;
    public static final int DUMP_OUTPUT_A_X = 144;
    public static final int DUMP_OUTPUT_B_X = 168;

    public static final int RUN_BUTTON_X = 72;
    public static final int RUN_BUTTON_Y = 28;
    public static final int RUN_BUTTON_SIZE = 44;

    public static final int REACTOR_CENTER_X = RUN_BUTTON_X + RUN_BUTTON_SIZE / 2;
    public static final int REACTOR_CENTER_Y = RUN_BUTTON_Y + RUN_BUTTON_SIZE / 2;
    public static final int REACTOR_OUTER_RADIUS = 18;
    public static final int REACTOR_INNER_RADIUS = 12;
    public static final int REACTOR_CORE_RADIUS = 8;

    public static final int PROGRESS_X = 60;
    public static final int PROGRESS_Y = 18;
    public static final int PROGRESS_W = 76;
    public static final int PROGRESS_H = 6;

    public static final int INPUT_SLOT_X = 22;
    public static final int INPUT_SLOT_Y = 82;

    public static final int OUTPUT_A_SLOT_X = 142;
    public static final int OUTPUT_A_SLOT_Y = INPUT_SLOT_Y;

    public static final int OUTPUT_B_SLOT_X = 166;
    public static final int OUTPUT_B_SLOT_Y = INPUT_SLOT_Y;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN;

    public static final int TITLE_Y = 6;
    public static final int CPS_TEXT_X = 90;
    public static final int CPS_TEXT_Y = 80;
    public static final int SPEED_TEXT_X = 90;
    public static final int SPEED_TEXT_Y = 90;

    public static final int GUI_HEIGHT = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN + StandardInventoryLayout.TOTAL_H + MACHINE_PANEL_Y;

    private DistillerLayout() {
    }
}