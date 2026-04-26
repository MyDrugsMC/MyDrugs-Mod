package org.mydrugs.mydrugs.menu.layout;

public final class GrowthChamberLayout {
    public static final int GUI_WIDTH = 216;

    public static final int MACHINE_PANEL_W = 196;
    public static final int MACHINE_PANEL_H = 110;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 13;

    private static final int SIDE_GUTTER = 16;
    private static final int SLOT_GAP = 8;

    public static final int TANK_W = StandardTankLayout.TANK_W;
    public static final int TANK_H = StandardTankLayout.TANK_H;

    public static final int TANK_INNER_X_OFFSET = StandardTankLayout.INNER_X;
    public static final int TANK_INNER_Y_OFFSET = StandardTankLayout.INNER_Y;
    public static final int TANK_INNER_W = StandardTankLayout.INNER_W;
    public static final int TANK_INNER_H = StandardTankLayout.INNER_H;

    public static final int WATER_TANK_X = MACHINE_PANEL_X + SIDE_GUTTER;
    public static final int WATER_TANK_Y = MACHINE_PANEL_Y + 28;

    public static final int SLOT_Y = WATER_TANK_Y + TANK_H + 3;

    public static final int WATER_INPUT_SLOT_X = WATER_TANK_X;
    public static final int WATER_INPUT_SLOT_Y = SLOT_Y;

    public static final int INPUT_SLOT_X = WATER_INPUT_SLOT_X + StandardInventoryLayout.SLOT_SIZE + SLOT_GAP;
    public static final int INPUT_SLOT_Y = SLOT_Y;

    public static final int BIOMASS_SLOT_X = INPUT_SLOT_X + StandardInventoryLayout.SLOT_SIZE + SLOT_GAP;
    public static final int BIOMASS_SLOT_Y = SLOT_Y;

    public static final int FINAL_SLOT_X = MACHINE_PANEL_X + MACHINE_PANEL_W - SIDE_GUTTER - StandardInventoryLayout.SLOT_SIZE;
    public static final int FINAL_SLOT_Y = SLOT_Y;

    public static final int MIDDLE_SLOT_X = FINAL_SLOT_X - StandardInventoryLayout.SLOT_SIZE - SLOT_GAP;
    public static final int MIDDLE_SLOT_Y = SLOT_Y;

    public static final int GROWTH_PROGRESS_X = INPUT_SLOT_X;
    public static final int GROWTH_PROGRESS_Y = WATER_TANK_Y;
    public static final int GROWTH_PROGRESS_W = MIDDLE_SLOT_X + StandardInventoryLayout.SLOT_SIZE - INPUT_SLOT_X;
    public static final int GROWTH_PROGRESS_H = 6;

    public static final int MATURE_PROGRESS_X = GROWTH_PROGRESS_X;
    public static final int MATURE_PROGRESS_Y = WATER_TANK_Y + 20;
    public static final int MATURE_PROGRESS_W = GROWTH_PROGRESS_W;
    public static final int MATURE_PROGRESS_H = GROWTH_PROGRESS_H;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private GrowthChamberLayout() {
    }
}
