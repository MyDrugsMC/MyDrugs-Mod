package org.mydrugs.mydrugs.menu.layout;

public final class AdvancedMixingVatLayout {
    public static final int GUI_WIDTH = 246;

    public static final int MACHINE_PANEL_W = 226;
    public static final int MACHINE_PANEL_H = 123;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 13;

    private static final int SIDE_GUTTER = 14;
    private static final int TANK_GAP = 8;
    private static final int INPUT_TANK_COUNT = 4;
    private static final int INPUT_TANK_GROUP_W = LayoutMath.groupWidth(StandardTankLayout.TANK_W, INPUT_TANK_COUNT, TANK_GAP);

    public static final int ITEM_0_X = MACHINE_PANEL_X + SIDE_GUTTER;
    public static final int ITEM_1_X = ITEM_0_X;
    public static final int ITEM_2_X = ITEM_0_X;
    public static final int ITEM_3_X = ITEM_0_X;

    public static final int ITEM_0_Y = MACHINE_PANEL_Y + 13;
    public static final int ITEM_1_Y = ITEM_0_Y + 19;
    public static final int ITEM_2_Y = ITEM_1_Y + 19;
    public static final int ITEM_3_Y = ITEM_2_Y + 19;

    private static final int INPUT_TANK_GROUP_X = ITEM_0_X + StandardInventoryLayout.SLOT_SIZE + 18;

    public static final int TANK_A_X = LayoutMath.groupItemX(INPUT_TANK_GROUP_X, StandardTankLayout.TANK_W, TANK_GAP, 0);
    public static final int TANK_A_SLOT_X = TANK_A_X;
    public static final int TANK_B_X = LayoutMath.groupItemX(INPUT_TANK_GROUP_X, StandardTankLayout.TANK_W, TANK_GAP, 1);
    public static final int TANK_B_SLOT_X = TANK_B_X;
    public static final int TANK_C_X = LayoutMath.groupItemX(INPUT_TANK_GROUP_X, StandardTankLayout.TANK_W, TANK_GAP, 2);
    public static final int TANK_C_SLOT_X = TANK_C_X;
    public static final int GAS_X = LayoutMath.groupItemX(INPUT_TANK_GROUP_X, StandardTankLayout.TANK_W, TANK_GAP, 3);
    public static final int GAS_SLOT_X = GAS_X;

    public static final int OUTPUT_X = MACHINE_PANEL_X + MACHINE_PANEL_W - SIDE_GUTTER - StandardTankLayout.TANK_W;
    public static final int OUTPUT_SLOT_X = OUTPUT_X;

    public static final int TANK_Y = ITEM_0_Y;
    public static final int TANK_W = StandardTankLayout.TANK_W;
    public static final int TANK_H = StandardTankLayout.TANK_H;
    public static final int TANK_INNER_X_OFFSET = StandardTankLayout.INNER_X;
    public static final int TANK_INNER_Y_OFFSET = StandardTankLayout.INNER_Y;
    public static final int TANK_INNER_W = StandardTankLayout.INNER_W;
    public static final int TANK_INNER_H = StandardTankLayout.INNER_H;

    public static final int TANK_SLOT_Y = TANK_Y + TANK_H + 3;

    public static final int PROGRESS_W = 44;
    public static final int PROGRESS_H = 8;
    public static final int PROGRESS_X = LayoutMath.centeredBetween(GAS_X + TANK_W, OUTPUT_X, PROGRESS_W);
    public static final int PROGRESS_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H - 21;

    public static final int STATUS_TEXT_X = PROGRESS_X + PROGRESS_W / 2;
    public static final int STATUS_TEXT_Y = PROGRESS_Y - 12;
    public static final int TANK_AMOUNT_LABEL_X = MACHINE_PANEL_X + 6;
    public static final int TANK_AMOUNT_LABEL_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H - 28;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);
    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private AdvancedMixingVatLayout() {
    }
}
