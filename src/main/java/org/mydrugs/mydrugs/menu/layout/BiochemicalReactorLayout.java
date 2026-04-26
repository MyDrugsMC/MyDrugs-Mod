package org.mydrugs.mydrugs.menu.layout;

public final class BiochemicalReactorLayout {

    public static final int GUI_WIDTH = 206;

    public static final int MACHINE_PANEL_W = 186;
    public static final int MACHINE_PANEL_H = 94;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 13;

    private static final int SIDE_GUTTER = 16;
    private static final int SLOT_STEP_Y = 24;

    public static final int ERGOT_SLOT_X = MACHINE_PANEL_X + SIDE_GUTTER;
    public static final int ERGOT_SLOT_Y = MACHINE_PANEL_Y + 23;

    public static final int TRYPTOPHAN_SLOT_X = ERGOT_SLOT_X;
    public static final int TRYPTOPHAN_SLOT_Y = ERGOT_SLOT_Y + SLOT_STEP_Y;

    public static final int CHARCOAL_SLOT_X = ERGOT_SLOT_X;
    public static final int CHARCOAL_SLOT_Y = TRYPTOPHAN_SLOT_Y + SLOT_STEP_Y;

    public static final int TANK_W = StandardTankLayout.TANK_W;
    public static final int TANK_H = StandardTankLayout.TANK_H;

    public static final int TANK_INNER_X_OFFSET = StandardTankLayout.INNER_X;
    public static final int TANK_INNER_Y_OFFSET = StandardTankLayout.INNER_Y;
    public static final int TANK_INNER_W = StandardTankLayout.INNER_W;
    public static final int TANK_INNER_H = StandardTankLayout.INNER_H;

    public static final int OUTPUT_TANK_X = MACHINE_PANEL_X + MACHINE_PANEL_W - SIDE_GUTTER - TANK_W;
    public static final int OUTPUT_TANK_Y = MACHINE_PANEL_Y + 15;

    public static final int OUTPUT_SLOT_X = OUTPUT_TANK_X;
    public static final int OUTPUT_SLOT_Y = OUTPUT_TANK_Y + TANK_H + 3;

    public static final int PROGRESS_W = 56;
    public static final int PROGRESS_H = 8;
    public static final int PROGRESS_X = LayoutMath.centeredBetween(ERGOT_SLOT_X + StandardInventoryLayout.SLOT_SIZE, OUTPUT_TANK_X, PROGRESS_W);
    public static final int PROGRESS_Y = MACHINE_PANEL_Y + 28;

    public static final int MANUAL_BUTTON_W = 18;
    public static final int MANUAL_BUTTON_H = 18;
    public static final int MANUAL_BUTTON_X = LayoutMath.centeredBetween(ERGOT_SLOT_X + StandardInventoryLayout.SLOT_SIZE, OUTPUT_TANK_X, MANUAL_BUTTON_W);
    public static final int MANUAL_BUTTON_Y = MACHINE_PANEL_Y + 52;

    public static final int HEAT_BAR_W = 12;
    public static final int HEAT_BAR_H = 54;
    public static final int HEAT_BAR_X = OUTPUT_TANK_X - 36;
    public static final int HEAT_BAR_Y = OUTPUT_TANK_Y;

    public static final int HEAT_BAR_INNER_X_OFFSET = 2;
    public static final int HEAT_BAR_INNER_Y_OFFSET = 2;
    public static final int HEAT_BAR_INNER_W = 8;
    public static final int HEAT_BAR_INNER_H = 50;

    public static final int MANUAL_BAR_W = 12;
    public static final int MANUAL_BAR_H = 54;
    public static final int MANUAL_BAR_X = OUTPUT_TANK_X - 20;
    public static final int MANUAL_BAR_Y = OUTPUT_TANK_Y;

    public static final int MANUAL_BAR_INNER_X_OFFSET = 2;
    public static final int MANUAL_BAR_INNER_Y_OFFSET = 2;
    public static final int MANUAL_BAR_INNER_W = 8;
    public static final int MANUAL_BAR_INNER_H = 50;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private BiochemicalReactorLayout() {
    }
}
