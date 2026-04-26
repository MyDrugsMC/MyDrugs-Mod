package org.mydrugs.mydrugs.menu.layout;

public final class AdvancedFurnaceLayout {
    public static final int GUI_WIDTH = 206;

    public static final int MACHINE_PANEL_W = 186;
    public static final int MACHINE_PANEL_H = 76;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 13;

    private static final int SIDE_GUTTER = 12;
    private static final int SLOT_GAP = 4;
    private static final int TOP_ROW_Y = MACHINE_PANEL_Y + 14;
    private static final int BOTTOM_ROW_Y = MACHINE_PANEL_Y + 44;

    public static final int INPUT_A_X = MACHINE_PANEL_X + SIDE_GUTTER;
    public static final int INPUT_A_Y = TOP_ROW_Y;

    public static final int INPUT_B_X = INPUT_A_X + StandardInventoryLayout.SLOT_SIZE + SLOT_GAP;
    public static final int INPUT_B_Y = TOP_ROW_Y;

    public static final int FUEL_X = LayoutMath.centeredBetween(INPUT_A_X, INPUT_B_X + StandardInventoryLayout.SLOT_SIZE, StandardInventoryLayout.SLOT_SIZE);
    public static final int FUEL_Y = BOTTOM_ROW_Y;

    public static final int TANK_X = MACHINE_PANEL_X + MACHINE_PANEL_W - SIDE_GUTTER - StandardTankLayout.TANK_W;
    public static final int TANK_Y = LayoutMath.centeredAt(MACHINE_PANEL_Y, MACHINE_PANEL_H, StandardTankLayout.TANK_H);

    public static final int OUTPUT_B_X = TANK_X - 6 - StandardInventoryLayout.SLOT_SIZE;
    public static final int OUTPUT_B_Y = TOP_ROW_Y;

    public static final int OUTPUT_A_X = OUTPUT_B_X - StandardInventoryLayout.SLOT_SIZE - SLOT_GAP;
    public static final int OUTPUT_A_Y = TOP_ROW_Y;

    public static final int OUTPUT_CONTAINER_X = LayoutMath.centeredBetween(OUTPUT_A_X, OUTPUT_B_X + StandardInventoryLayout.SLOT_SIZE, StandardInventoryLayout.SLOT_SIZE);
    public static final int OUTPUT_CONTAINER_Y = BOTTOM_ROW_Y;

    public static final int PROGRESS_W = 38;
    public static final int PROGRESS_H = 8;
    public static final int PROGRESS_X = LayoutMath.centeredBetween(INPUT_B_X + StandardInventoryLayout.SLOT_SIZE, OUTPUT_A_X, PROGRESS_W);
    public static final int PROGRESS_Y = TOP_ROW_Y + 5;

    public static final int BURN_W = 30;
    public static final int BURN_H = 8;
    public static final int BURN_X = LayoutMath.centeredBetween(INPUT_B_X + StandardInventoryLayout.SLOT_SIZE, OUTPUT_A_X, BURN_W);
    public static final int BURN_Y = BOTTOM_ROW_Y + 5;

    public static final int CENTER_PANEL_W = PROGRESS_W + 14;
    public static final int CENTER_PANEL_H = MACHINE_PANEL_H - 16;
    public static final int CENTER_PANEL_X = LayoutMath.centeredBetween(INPUT_B_X + StandardInventoryLayout.SLOT_SIZE, OUTPUT_A_X, CENTER_PANEL_W);
    public static final int CENTER_PANEL_Y = MACHINE_PANEL_Y + 8;
    public static final int HEAT_LABEL_X = CENTER_PANEL_X + CENTER_PANEL_W / 2;
    public static final int HEAT_LABEL_Y = BURN_Y - 10;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private AdvancedFurnaceLayout() {
    }
}
