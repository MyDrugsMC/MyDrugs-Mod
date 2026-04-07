package org.mydrugs.mydrugs.menu.layout;

public final class AdvancedMixingVatLayout {
    public static final int GUI_WIDTH = 226;

    public static final int MACHINE_PANEL_W = 186;
    public static final int MACHINE_PANEL_H = 123;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 9;

    public static final int ITEM_0_X = MACHINE_PANEL_X + LayoutMath.horizontalSpreadWithOuterGaps(0, MACHINE_PANEL_W, StandardInventoryLayout.SLOT_SIZE, 7, 0);
    public static final int ITEM_0_Y = 20;
    public static final int ITEM_1_X = ITEM_0_X;
    public static final int ITEM_1_Y = 39;
    public static final int ITEM_2_X = ITEM_0_X;
    public static final int ITEM_2_Y = 58;
    public static final int ITEM_3_X = ITEM_0_X;
    public static final int ITEM_3_Y = 77;

    public static final int TANK_A_X = MACHINE_PANEL_X + LayoutMath.horizontalSpreadWithOuterGaps(0, MACHINE_PANEL_W, StandardInventoryLayout.SLOT_SIZE, 7, 1);
    public static final int TANK_B_X = MACHINE_PANEL_X + LayoutMath.horizontalSpreadWithOuterGaps(0, MACHINE_PANEL_W, StandardInventoryLayout.SLOT_SIZE, 7, 2);
    public static final int TANK_C_X = MACHINE_PANEL_X + LayoutMath.horizontalSpreadWithOuterGaps(0, MACHINE_PANEL_W, StandardInventoryLayout.SLOT_SIZE, 7, 3);
    public static final int GAS_X = MACHINE_PANEL_X + LayoutMath.horizontalSpreadWithOuterGaps(0, MACHINE_PANEL_W, StandardInventoryLayout.SLOT_SIZE, 7, 4);
    public static final int OUTPUT_X = MACHINE_PANEL_X + LayoutMath.horizontalSpreadWithOuterGaps(0, MACHINE_PANEL_W, StandardInventoryLayout.SLOT_SIZE, 7, 6);
    public static final int TANK_Y = ITEM_0_Y;
    public static final int TANK_W = StandardTankLayout.TANK_W;
    public static final int TANK_H = StandardTankLayout.TANK_H;
    public static final int TANK_INNER_X_OFFSET = 2;
    public static final int TANK_INNER_Y_OFFSET = 2;
    public static final int TANK_INNER_W = StandardTankLayout.INNER_W;
    public static final int TANK_INNER_H = StandardTankLayout.INNER_H;

    public static final int PROGRESS_W = 68;
    public static final int PROGRESS_H = 8;
    public static final int PROGRESS_X = LayoutMath.centered(GUI_WIDTH, PROGRESS_W);
    public static final int PROGRESS_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H - 20;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN;

    public static final int GUI_HEIGHT = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN + StandardInventoryLayout.TOTAL_H + MACHINE_PANEL_Y;

    public static final int TANK_SLOT_Y = 77;

    public static final int TANK_A_SLOT_X = TANK_A_X;
    public static final int TANK_B_SLOT_X = TANK_B_X;
    public static final int TANK_C_SLOT_X = TANK_C_X;
    public static final int GAS_SLOT_X = GAS_X;
    public static final int OUTPUT_SLOT_X = OUTPUT_X;

    private AdvancedMixingVatLayout() {
    }
}