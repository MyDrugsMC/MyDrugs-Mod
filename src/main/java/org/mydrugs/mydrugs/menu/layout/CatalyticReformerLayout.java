package org.mydrugs.mydrugs.menu.layout;

public final class CatalyticReformerLayout {
    public static final int GUI_WIDTH = 244;

    public static final int MACHINE_PANEL_W = 224;
    public static final int MACHINE_PANEL_H = 96;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 13;

    public static final int TANK_W = StandardTankLayout.TANK_W;
    public static final int TANK_H = StandardTankLayout.TANK_H;
    public static final int TANK_INNER_X_OFFSET = StandardTankLayout.INNER_X;
    public static final int TANK_INNER_Y_OFFSET = StandardTankLayout.INNER_Y;
    public static final int TANK_INNER_W = StandardTankLayout.INNER_W;
    public static final int TANK_INNER_H = StandardTankLayout.INNER_H;

    public static final int TANK_Y = MACHINE_PANEL_Y + 17;
    public static final int SLOT_Y = TANK_Y + TANK_H + 3;

    private static final int EDGE_MARGIN = 16;
    private static final int TANK_GAP = 8;
    private static final int INPUT_GROUP_W = LayoutMath.groupWidth(TANK_W, 2, TANK_GAP);
    private static final int OUTPUT_GROUP_W = LayoutMath.groupWidth(TANK_W, 3, TANK_GAP);

    private static final int INPUT_GROUP_X = MACHINE_PANEL_X + EDGE_MARGIN;
    private static final int OUTPUT_GROUP_X = MACHINE_PANEL_X + MACHINE_PANEL_W - EDGE_MARGIN - OUTPUT_GROUP_W;

    public static final int INPUT_1_TANK_X = LayoutMath.groupItemX(INPUT_GROUP_X, TANK_W, TANK_GAP, 0);
    public static final int INPUT_2_TANK_X = LayoutMath.groupItemX(INPUT_GROUP_X, TANK_W, TANK_GAP, 1);

    public static final int OUTPUT_1_TANK_X = LayoutMath.groupItemX(OUTPUT_GROUP_X, TANK_W, TANK_GAP, 0);
    public static final int OUTPUT_2_TANK_X = LayoutMath.groupItemX(OUTPUT_GROUP_X, TANK_W, TANK_GAP, 1);
    public static final int OUTPUT_3_TANK_X = LayoutMath.groupItemX(OUTPUT_GROUP_X, TANK_W, TANK_GAP, 2);

    public static final int INPUT_1_SLOT_X = INPUT_1_TANK_X;
    public static final int INPUT_2_SLOT_X = INPUT_2_TANK_X;

    public static final int OUTPUT_1_SLOT_X = OUTPUT_1_TANK_X;
    public static final int OUTPUT_2_SLOT_X = OUTPUT_2_TANK_X;
    public static final int OUTPUT_3_SLOT_X = OUTPUT_3_TANK_X;

    public static final int DUMP_BUTTON_SIZE = 12;
    public static final int DUMP_BUTTON_Y = MACHINE_PANEL_Y + 3;

    public static final int DUMP_INPUT_1_X = LayoutMath.dumpButtonX(INPUT_1_TANK_X, TANK_W, DUMP_BUTTON_SIZE);
    public static final int DUMP_INPUT_2_X = LayoutMath.dumpButtonX(INPUT_2_TANK_X, TANK_W, DUMP_BUTTON_SIZE);

    public static final int DUMP_OUTPUT_1_X = LayoutMath.dumpButtonX(OUTPUT_1_TANK_X, TANK_W, DUMP_BUTTON_SIZE);
    public static final int DUMP_OUTPUT_2_X = LayoutMath.dumpButtonX(OUTPUT_2_TANK_X, TANK_W, DUMP_BUTTON_SIZE);
    public static final int DUMP_OUTPUT_3_X = LayoutMath.dumpButtonX(OUTPUT_3_TANK_X, TANK_W, DUMP_BUTTON_SIZE);

    public static final int CENTER_PANEL_W = 44;
    public static final int CENTER_PANEL_H = 52;
    public static final int CENTER_PANEL_X = LayoutMath.centeredBetween(INPUT_2_TANK_X + TANK_W, OUTPUT_1_TANK_X, CENTER_PANEL_W);
    public static final int CENTER_PANEL_Y = MACHINE_PANEL_Y + 31;

    public static final int CATALYST_SLOT_X = LayoutMath.centeredAt(CENTER_PANEL_X, CENTER_PANEL_W, StandardInventoryLayout.SLOT_SIZE);
    public static final int CATALYST_SLOT_Y = CENTER_PANEL_Y + 8;

    public static final int PROGRESS_W = 36;
    public static final int PROGRESS_H = 6;
    public static final int PROGRESS_X = LayoutMath.centeredAt(CENTER_PANEL_X, CENTER_PANEL_W, PROGRESS_W);
    public static final int PROGRESS_Y = CENTER_PANEL_Y + 34;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private CatalyticReformerLayout() {
    }
}
