package org.mydrugs.mydrugs.menu.layout;

public final class ElectrolyzerLayout {
    public static final int GUI_WIDTH = 240;

    public static final int MACHINE_PANEL_W = 220;
    public static final int MACHINE_PANEL_H = 93;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 9;

    public static final int TANK_W = 16;
    public static final int TANK_H = 54;

    public static final int TANK_INNER_X_OFFSET = 2;
    public static final int TANK_INNER_Y_OFFSET = 2;
    public static final int TANK_INNER_W = 12;
    public static final int TANK_INNER_H = 50;

    public static final int TANK_ROW_Y = 25;
    public static final int SLOT_ROW_Y = TANK_ROW_Y + TANK_H + 3;

    public static final int LEFT_TANK_SECTION_X = MACHINE_PANEL_X + 12;
    public static final int LEFT_TANK_SECTION_W = 20;
    public static final int CENTER_SECTION_X = MACHINE_PANEL_X + 58;
    public static final int CENTER_SECTION_W = 74;
    public static final int RIGHT_TANK_SECTION_X = MACHINE_PANEL_X + 146;
    public static final int RIGHT_TANK_SECTION_W = 54;

    public static final int INPUT_TANK_X = LayoutMath.centeredAt(LEFT_TANK_SECTION_X, LEFT_TANK_SECTION_W, TANK_W);
    public static final int INPUT_TANK_Y = TANK_ROW_Y;

    public static final int OUTPUT_1_TANK_X = LayoutMath.horizontalSpreadWithOuterGaps(RIGHT_TANK_SECTION_X, RIGHT_TANK_SECTION_W, TANK_W, 3, 0);
    public static final int OUTPUT_1_TANK_Y = TANK_ROW_Y;

    public static final int OUTPUT_2_TANK_X = LayoutMath.horizontalSpreadWithOuterGaps(RIGHT_TANK_SECTION_X, RIGHT_TANK_SECTION_W, TANK_W, 3, 1);
    public static final int OUTPUT_2_TANK_Y = TANK_ROW_Y;

    public static final int OUTPUT_3_TANK_X = LayoutMath.horizontalSpreadWithOuterGaps(RIGHT_TANK_SECTION_X, RIGHT_TANK_SECTION_W, TANK_W, 3, 2);
    public static final int OUTPUT_3_TANK_Y = TANK_ROW_Y;

    public static final int DUMP_BUTTON_SIZE = 12;
    public static final int DUMP_BUTTON_Y = MACHINE_PANEL_Y + 2;

    public static final int DUMP_INPUT_X = INPUT_TANK_X + LayoutMath.centered(TANK_W, DUMP_BUTTON_SIZE);
    public static final int DUMP_OUTPUT_1_X = OUTPUT_1_TANK_X + LayoutMath.centered(TANK_W, DUMP_BUTTON_SIZE);
    public static final int DUMP_OUTPUT_2_X = OUTPUT_2_TANK_X + LayoutMath.centered(TANK_W, DUMP_BUTTON_SIZE);
    public static final int DUMP_OUTPUT_3_X = OUTPUT_3_TANK_X + LayoutMath.centered(TANK_W, DUMP_BUTTON_SIZE);

    public static final int PROGRESS_W = 64;
    public static final int PROGRESS_H = 6;
    public static final int PROGRESS_X = LayoutMath.centeredAt(CENTER_SECTION_X, CENTER_SECTION_W, PROGRESS_W);
    public static final int PROGRESS_Y = 28;

    public static final int CENTER_PANEL_W = 54;
    public static final int CENTER_PANEL_H = 56;
    public static final int CENTER_PANEL_X = LayoutMath.centeredAt(CENTER_SECTION_X, CENTER_SECTION_W, CENTER_PANEL_W);
    public static final int CENTER_PANEL_Y = PROGRESS_Y + PROGRESS_H + 2;

    public static final int FUEL_BAR_W = 12;
    public static final int FUEL_BAR_H = 24;
    public static final int FUEL_BAR_X = LayoutMath.centeredAt(CENTER_PANEL_X, CENTER_PANEL_W, FUEL_BAR_W);
    public static final int FUEL_BAR_Y = CENTER_PANEL_Y + 5;
    public static final int FUEL_BAR_INNER_X_OFFSET = 2;
    public static final int FUEL_BAR_INNER_Y_OFFSET = 2;
    public static final int FUEL_BAR_INNER_W = 8;
    public static final int FUEL_BAR_INNER_H = 20;

    public static final int FUEL_SLOT_X = LayoutMath.centeredAt(CENTER_PANEL_X, CENTER_PANEL_W, StandardInventoryLayout.SLOT_SIZE);
    public static final int FUEL_SLOT_Y = FUEL_BAR_Y + FUEL_BAR_H + 3;

    public static final int INPUT_SLOT_X = INPUT_TANK_X;
    public static final int INPUT_SLOT_Y = SLOT_ROW_Y;

    public static final int OUTPUT_1_SLOT_X = OUTPUT_1_TANK_X;
    public static final int OUTPUT_1_SLOT_Y = SLOT_ROW_Y;

    public static final int OUTPUT_2_SLOT_X = OUTPUT_2_TANK_X;
    public static final int OUTPUT_2_SLOT_Y = SLOT_ROW_Y;

    public static final int OUTPUT_3_SLOT_X = OUTPUT_3_TANK_X;
    public static final int OUTPUT_3_SLOT_Y = SLOT_ROW_Y;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN;

    public static final int GUI_HEIGHT = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN + StandardInventoryLayout.TOTAL_H + MACHINE_PANEL_Y;

    private ElectrolyzerLayout() {
    }
}
