package org.mydrugs.mydrugs.menu.layout;

public final class ChemicalReactorLayout {
    public static final int GUI_WIDTH = 226;

    public static final int MACHINE_PANEL_W = 206;
    public static final int MACHINE_PANEL_H = 91;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 13;

    public static final int LABEL_Y = MACHINE_PANEL_Y + 1;

    public static final int TANK_W = 16;
    public static final int TANK_H = 50;
    public static final int TANK_INNER_X_OFFSET = 2;
    public static final int TANK_INNER_Y_OFFSET = 2;
    public static final int TANK_INNER_W = 12;
    public static final int TANK_INNER_H = 46;

    private static final int SIDE_GUTTER = 16;
    private static final int TANK_GAP = 8;

    public static final int PRIMARY_GAS_TANK_X = MACHINE_PANEL_X + SIDE_GUTTER;
    public static final int SECONDARY_TANK_X = PRIMARY_GAS_TANK_X + TANK_W + TANK_GAP;
    public static final int PRIMARY_GAS_TANK_Y = MACHINE_PANEL_Y + 10;
    public static final int SECONDARY_TANK_Y = PRIMARY_GAS_TANK_Y;

    public static final int OUTPUT_TANK_X = MACHINE_PANEL_X + MACHINE_PANEL_W - SIDE_GUTTER - TANK_W;
    public static final int OUTPUT_SECTION_DIVIDER_X = OUTPUT_TANK_X - 12;
    public static final int OUTPUT_TANK_Y = PRIMARY_GAS_TANK_Y;

    public static final int PROGRESS_W = 48;
    public static final int PROGRESS_H = 8;
    public static final int PROGRESS_X = LayoutMath.centeredBetween(SECONDARY_TANK_X + TANK_W, OUTPUT_TANK_X, PROGRESS_W);
    public static final int PROGRESS_Y = MACHINE_PANEL_Y + 23;

    public static final int HEAT_BAR_W = 8;
    public static final int HEAT_BAR_H = 50;
    public static final int HEAT_BAR_X = PROGRESS_X + PROGRESS_W + 10;
    public static final int HEAT_BAR_Y = PRIMARY_GAS_TANK_Y;
    public static final int HEAT_BAR_INNER_X_OFFSET = 1;
    public static final int HEAT_BAR_INNER_Y_OFFSET = 1;
    public static final int HEAT_BAR_INNER_W = 6;
    public static final int HEAT_BAR_INNER_H = 48;

    public static final int FUEL_BAR_W = 8;
    public static final int FUEL_BAR_H = 50;
    public static final int FUEL_BAR_X = PROGRESS_X - 18;
    public static final int FUEL_BAR_Y = PRIMARY_GAS_TANK_Y;
    public static final int FUEL_BAR_INNER_X_OFFSET = 1;
    public static final int FUEL_BAR_INNER_Y_OFFSET = 1;
    public static final int FUEL_BAR_INNER_W = 6;
    public static final int FUEL_BAR_INNER_H = 48;

    public static final int INPUT_SECTION_DIVIDER_X = PROGRESS_X - 12;

    public static final int MANUAL_BAR_W = 48;
    public static final int MANUAL_BAR_H = 6;
    public static final int MANUAL_BAR_X = PROGRESS_X;
    public static final int MANUAL_BAR_Y = PROGRESS_Y + 15;

    public static final int FUEL_SLOT_X = LayoutMath.centeredBetween(SECONDARY_TANK_X + TANK_W, OUTPUT_TANK_X, StandardInventoryLayout.SLOT_SIZE);
    public static final int FUEL_SLOT_Y = MANUAL_BAR_Y + 13;

    public static final int TRANSFER_SLOT_Y = PRIMARY_GAS_TANK_Y + TANK_H + 6;
    public static final int TRANSFER_SLOT_Y_2 = TRANSFER_SLOT_Y + 20;
    public static final int DIVIDER_Y = MACHINE_PANEL_Y + 10;
    public static final int DIVIDER_H = MACHINE_PANEL_H - 20;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private ChemicalReactorLayout() {
    }
}
