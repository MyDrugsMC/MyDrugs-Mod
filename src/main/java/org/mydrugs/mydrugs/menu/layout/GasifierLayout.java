package org.mydrugs.mydrugs.menu.layout;

public final class GasifierLayout {
    private GasifierLayout() {
    }

    public static final int GUI_WIDTH = 176;

    public static final int MACHINE_PANEL_W = 136;
    public static final int MACHINE_PANEL_H = 68;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 12;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN;

    public static final int INPUT_SLOT_X = MACHINE_PANEL_X + 12;
    public static final int INPUT_SLOT_Y = MACHINE_PANEL_Y + LayoutMath.verticalSpreadWithOuterGaps(
            0,
            MACHINE_PANEL_H,
            StandardInventoryLayout.SLOT_SIZE,
            2,
            0
    );

    public static final int FUEL_SLOT_X = MACHINE_PANEL_X + 12;
    public static final int FUEL_SLOT_Y = MACHINE_PANEL_Y + LayoutMath.verticalSpreadWithOuterGaps(
            0,
            MACHINE_PANEL_H,
            StandardInventoryLayout.SLOT_SIZE,
            2,
            1
    );

    public static final int FUEL_BAR_W = 8;
    public static final int FUEL_BAR_H = 16;
    public static final int FUEL_BAR_INNER_X_OFFSET = 1;
    public static final int FUEL_BAR_INNER_Y_OFFSET = 1;
    public static final int FUEL_BAR_INNER_W = 6;
    public static final int FUEL_BAR_INNER_H = FUEL_BAR_H - 2;
    public static final int FUEL_BAR_X = FUEL_SLOT_X + 22;
    public static final int FUEL_BAR_Y = FUEL_SLOT_Y;

    public static final int PROGRESS_W = 28;
    public static final int PROGRESS_H = 8;
    public static final int PROGRESS_X = MACHINE_PANEL_X + LayoutMath.centered(MACHINE_PANEL_W, PROGRESS_W);
    public static final int PROGRESS_Y = MACHINE_PANEL_Y + LayoutMath.centered(MACHINE_PANEL_H, PROGRESS_H);

    public static final int OUTPUT_TANK_X = MACHINE_PANEL_X + 88;
    public static final int OUTPUT_TANK_Y = MACHINE_PANEL_Y + LayoutMath.centered(MACHINE_PANEL_H, StandardTankLayout.TANK_H);
    public static final int TANK_W = StandardTankLayout.TANK_W;
    public static final int TANK_H = StandardTankLayout.TANK_H;
    public static final int TANK_INNER_X_OFFSET = StandardTankLayout.INNER_X;
    public static final int TANK_INNER_Y_OFFSET = StandardTankLayout.INNER_Y;
    public static final int TANK_INNER_W = StandardTankLayout.INNER_W;
    public static final int TANK_INNER_H = StandardTankLayout.INNER_H;

    public static final int EXPORT_SLOT_X = MACHINE_PANEL_X + 110;
    public static final int EXPORT_SLOT_Y = MACHINE_PANEL_Y + LayoutMath.centered(MACHINE_PANEL_H, StandardInventoryLayout.SLOT_SIZE);

    public static final int GUI_HEIGHT = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN + StandardInventoryLayout.TOTAL_H + MACHINE_PANEL_Y;
}