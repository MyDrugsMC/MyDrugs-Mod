package org.mydrugs.mydrugs.menu.layout;

public final class ChemicalReactorLayout {
    public static final int GUI_WIDTH = 176;

    public static final int MACHINE_PANEL_X = 8;
    public static final int MACHINE_PANEL_Y = 8;
    public static final int MACHINE_PANEL_W = 160;
    public static final int MACHINE_PANEL_H = 78;

    public static final int PRIMARY_GAS_TANK_X = 16;
    public static final int PRIMARY_GAS_TANK_Y = 18;

    public static final int SECONDARY_TANK_X = 46;
    public static final int SECONDARY_TANK_Y = 18;

    public static final int OUTPUT_TANK_X = 132;
    public static final int OUTPUT_TANK_Y = 18;

    public static final int TANK_W = 16;
    public static final int TANK_H = 50;
    public static final int TANK_INNER_X_OFFSET = 2;
    public static final int TANK_INNER_Y_OFFSET = 2;
    public static final int TANK_INNER_W = 12;
    public static final int TANK_INNER_H = 46;

    public static final int PROGRESS_X = 70;
    public static final int PROGRESS_Y = 31;
    public static final int PROGRESS_W = 48;
    public static final int PROGRESS_H = 8;

    public static final int HEAT_BAR_X = 156;
    public static final int HEAT_BAR_Y = 18;
    public static final int HEAT_BAR_W = 8;
    public static final int HEAT_BAR_H = 50;
    public static final int HEAT_BAR_INNER_X_OFFSET = 1;
    public static final int HEAT_BAR_INNER_Y_OFFSET = 1;
    public static final int HEAT_BAR_INNER_W = 6;
    public static final int HEAT_BAR_INNER_H = 48;

    public static final int FUEL_BAR_X = 118;
    public static final int FUEL_BAR_Y = 18;
    public static final int FUEL_BAR_W = 8;
    public static final int FUEL_BAR_H = 50;
    public static final int FUEL_BAR_INNER_X_OFFSET = 1;
    public static final int FUEL_BAR_INNER_Y_OFFSET = 1;
    public static final int FUEL_BAR_INNER_W = 6;
    public static final int FUEL_BAR_INNER_H = 48;

    public static final int MANUAL_BAR_X = 70;
    public static final int MANUAL_BAR_Y = 46;
    public static final int MANUAL_BAR_W = 48;
    public static final int MANUAL_BAR_H = 6;

    public static final int FUEL_SLOT_X = 79;
    public static final int FUEL_SLOT_Y = 61;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN;

    public static final int GUI_HEIGHT = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN + StandardInventoryLayout.TOTAL_H + MACHINE_PANEL_Y;

    private ChemicalReactorLayout() {
    }
}