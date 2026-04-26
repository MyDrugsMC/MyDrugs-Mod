package org.mydrugs.mydrugs.menu.layout;

public final class GasifierLayout {
    public static final int GUI_WIDTH = 196;
    public static final int MACHINE_PANEL_W = 176;
    public static final int MACHINE_PANEL_H = 72;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 13;

    private static final int SIDE_GUTTER = 16;

    public static final int INPUT_SLOT_X = MACHINE_PANEL_X + SIDE_GUTTER;
    public static final int INPUT_SLOT_Y = MACHINE_PANEL_Y + 14;

    public static final int FUEL_SLOT_X = INPUT_SLOT_X;
    public static final int FUEL_SLOT_Y = MACHINE_PANEL_Y + 42;

    public static final int OUTPUT_TANK_X = MACHINE_PANEL_X + MACHINE_PANEL_W - SIDE_GUTTER - StandardTankLayout.TANK_W;
    public static final int OUTPUT_TANK_Y = LayoutMath.centeredAt(MACHINE_PANEL_Y, MACHINE_PANEL_H, StandardTankLayout.TANK_H);

    public static final int EXPORT_SLOT_X = OUTPUT_TANK_X - StandardInventoryLayout.SLOT_SIZE - 8;
    public static final int EXPORT_SLOT_Y = LayoutMath.centeredAt(MACHINE_PANEL_Y, MACHINE_PANEL_H, StandardInventoryLayout.SLOT_SIZE);

    public static final int PROGRESS_W = 36;
    public static final int PROGRESS_H = 8;
    public static final int PROGRESS_X = LayoutMath.centeredBetween(INPUT_SLOT_X + StandardInventoryLayout.SLOT_SIZE, EXPORT_SLOT_X, PROGRESS_W);
    public static final int PROGRESS_Y = MACHINE_PANEL_Y + 19;

    public static final int FUEL_BAR_W = 8;
    public static final int FUEL_BAR_H = 16;
    public static final int FUEL_BAR_X = LayoutMath.centeredBetween(INPUT_SLOT_X + StandardInventoryLayout.SLOT_SIZE, EXPORT_SLOT_X, FUEL_BAR_W);
    public static final int FUEL_BAR_Y = FUEL_SLOT_Y + 1;
    public static final int FUEL_BAR_INNER_X_OFFSET = 1;
    public static final int FUEL_BAR_INNER_Y_OFFSET = 1;
    public static final int FUEL_BAR_INNER_W = 6;
    public static final int FUEL_BAR_INNER_H = FUEL_BAR_H - 2;

    public static final int TANK_W = StandardTankLayout.TANK_W;
    public static final int TANK_H = StandardTankLayout.TANK_H;
    public static final int TANK_INNER_X_OFFSET = StandardTankLayout.INNER_X;
    public static final int TANK_INNER_Y_OFFSET = StandardTankLayout.INNER_Y;
    public static final int TANK_INNER_W = StandardTankLayout.INNER_W;
    public static final int TANK_INNER_H = StandardTankLayout.INNER_H;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);
    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private GasifierLayout() {
    }
}
