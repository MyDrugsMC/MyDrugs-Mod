package org.mydrugs.mydrugs.menu.layout;

public final class AromaticExtractorLayout {
    public static final int GUI_WIDTH = 220;

    public static final int MACHINE_PANEL_X = 10;
    public static final int MACHINE_PANEL_Y = 9;
    public static final int MACHINE_PANEL_W = 200;
    public static final int MACHINE_PANEL_H = 93;

    public static final int INPUT_TANK_X = 22;
    public static final int INPUT_TANK_Y = 25;

    public static final int CATALYST_TANK_X = 54;
    public static final int CATALYST_TANK_Y = INPUT_TANK_Y;

    public static final int OUTPUT_A_TANK_X = 160;
    public static final int OUTPUT_A_TANK_Y = INPUT_TANK_Y;

    public static final int OUTPUT_B_TANK_X = 188;
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
    public static final int DUMP_CATALYST_X = 56;
    public static final int DUMP_OUTPUT_A_X = 162;
    public static final int DUMP_OUTPUT_B_X = 190;

    public static final int PROGRESS_X = 84;
    public static final int PROGRESS_Y = 28;
    public static final int PROGRESS_W = 62;
    public static final int PROGRESS_H = 6;

    public static final int FUEL_BAR_W = 12;
    public static final int FUEL_BAR_H = 24;

    public static final int CENTER_PANEL_X = 90;
    public static final int CENTER_PANEL_Y = PROGRESS_Y + PROGRESS_H + 2;
    public static final int FUEL_BAR_Y = CENTER_PANEL_Y + 5;
    public static final int FUEL_SLOT_Y = FUEL_BAR_Y + FUEL_BAR_H + 3;
    public static final int CENTER_PANEL_W = 50;
    public static final int CENTER_PANEL_H = 56;
    public static final int FUEL_BAR_X = 109;
    public static final int FUEL_BAR_INNER_X_OFFSET = 2;
    public static final int FUEL_BAR_INNER_Y_OFFSET = 2;
    public static final int FUEL_BAR_INNER_W = 8;
    public static final int FUEL_BAR_INNER_H = 20;
    public static final int FUEL_SLOT_X = 107;

    public static final int INPUT_SLOT_X = 22;
    public static final int INPUT_SLOT_Y = 82;

    public static final int CATALYST_SLOT_X = 54;
    public static final int CATALYST_SLOT_Y = INPUT_SLOT_Y;

    public static final int OUTPUT_A_SLOT_X = 160;
    public static final int OUTPUT_A_SLOT_Y = INPUT_SLOT_Y;

    public static final int OUTPUT_B_SLOT_X = 188;
    public static final int OUTPUT_B_SLOT_Y = INPUT_SLOT_Y;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN;

    public static final int GUI_HEIGHT = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN + StandardInventoryLayout.TOTAL_H + MACHINE_PANEL_Y;

    private AromaticExtractorLayout() {
    }
}
