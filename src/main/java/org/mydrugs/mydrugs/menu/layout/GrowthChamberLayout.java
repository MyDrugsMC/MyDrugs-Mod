package org.mydrugs.mydrugs.menu.layout;

public final class GrowthChamberLayout {
    public static final int GUI_WIDTH = 196;

    public static final int MACHINE_PANEL_X = 10;
    public static final int MACHINE_PANEL_Y = 9;
    public static final int MACHINE_PANEL_W = 176;
    public static final int MACHINE_PANEL_H = 110;

    public static final int WATER_TANK_X = 20;
    public static final int WATER_TANK_Y = 37;
    public static final int TANK_W = 16;
    public static final int TANK_H = 54;

    public static final int TANK_INNER_X_OFFSET = 2;
    public static final int TANK_INNER_Y_OFFSET = 2;
    public static final int TANK_INNER_W = 12;
    public static final int TANK_INNER_H = 50;

    public static final int SLOT_Y = 94;

    public static final int WATER_INPUT_SLOT_X = WATER_TANK_X;
    public static final int WATER_INPUT_SLOT_Y = SLOT_Y;

    public static final int INPUT_SLOT_X = 46;
    public static final int INPUT_SLOT_Y = SLOT_Y;

    public static final int BIOMASS_SLOT_X = 70;
    public static final int BIOMASS_SLOT_Y = SLOT_Y;

    public static final int MIDDLE_SLOT_X = 124;
    public static final int MIDDLE_SLOT_Y = SLOT_Y;

    public static final int FINAL_SLOT_X = 148;
    public static final int FINAL_SLOT_Y = SLOT_Y;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN;

    public static final int GROWTH_PROGRESS_X = 46;
    public static final int GROWTH_PROGRESS_Y = WATER_TANK_Y;
    public static final int GROWTH_PROGRESS_W = MACHINE_PANEL_W - GROWTH_PROGRESS_X - WATER_TANK_X + MACHINE_PANEL_X;
    public static final int GROWTH_PROGRESS_H = 6;

    public static final int MATURE_PROGRESS_X = GROWTH_PROGRESS_X;
    public static final int MATURE_PROGRESS_Y = WATER_TANK_Y + 20;
    public static final int MATURE_PROGRESS_W = GROWTH_PROGRESS_W;
    public static final int MATURE_PROGRESS_H = GROWTH_PROGRESS_H;

    public static final int GUI_HEIGHT = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN + StandardInventoryLayout.TOTAL_H + MACHINE_PANEL_Y;

    private GrowthChamberLayout() {
    }
}