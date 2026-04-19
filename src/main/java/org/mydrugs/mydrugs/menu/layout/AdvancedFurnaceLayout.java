package org.mydrugs.mydrugs.menu.layout;

public final class AdvancedFurnaceLayout {
    public static final int GUI_WIDTH = 186;

    public static final int MACHINE_PANEL_W = 166;
    public static final int MACHINE_PANEL_H = 71;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 12;

    public static final int INPUT_A_X = 24;
    public static final int INPUT_A_Y = 23;

    public static final int INPUT_B_X = 42;
    public static final int INPUT_B_Y = 23;

    public static final int FUEL_X = 33;
    public static final int FUEL_Y = 52;

    public static final int PROGRESS_X = 70;
    public static final int PROGRESS_Y = 29;
    public static final int PROGRESS_W = 28;
    public static final int PROGRESS_H = 8;

    public static final int BURN_X = 72;
    public static final int BURN_Y = 54;
    public static final int BURN_W = 24;
    public static final int BURN_H = 8;

    public static final int OUTPUT_A_X = 108;
    public static final int OUTPUT_A_Y = 23;

    public static final int OUTPUT_B_X = 126;
    public static final int OUTPUT_B_Y = 23;

    public static final int OUTPUT_CONTAINER_X = 117;
    public static final int OUTPUT_CONTAINER_Y = 52;

    public static final int TANK_X = 148;
    public static final int TANK_Y = 20;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN;

    public static final int GUI_HEIGHT = PLAYER_INV_Y + StandardInventoryLayout.TOTAL_H + MACHINE_PANEL_Y;

    private AdvancedFurnaceLayout() {
    }
}