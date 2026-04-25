package org.mydrugs.mydrugs.menu.layout;

public final class CatalyticReformerLayout {
    public static final int GUI_WIDTH = 244;

    public static final int MACHINE_PANEL_X = 10;
    public static final int MACHINE_PANEL_Y = 9;
    public static final int MACHINE_PANEL_W = 224;
    public static final int MACHINE_PANEL_H = 104;

    public static final int TANK_W = 16;
    public static final int TANK_H = 54;
    public static final int TANK_INNER_X_OFFSET = 2;
    public static final int TANK_INNER_Y_OFFSET = 2;
    public static final int TANK_INNER_W = 12;
    public static final int TANK_INNER_H = 50;

    public static final int TANK_Y = 26;
    public static final int SLOT_Y = TANK_Y + TANK_H + 3;

    private static final int EDGE_MARGIN = 12;
    private static final int INPUT_GROUP_W = TANK_W * 2 + 8;
    private static final int OUTPUT_GROUP_W = TANK_W * 3 + 16;

    private static final int INPUT_GROUP_X = MACHINE_PANEL_X + EDGE_MARGIN;
    private static final int OUTPUT_GROUP_X = MACHINE_PANEL_X + MACHINE_PANEL_W - EDGE_MARGIN - OUTPUT_GROUP_W;

    public static final int INPUT_1_TANK_X = LayoutMath.horizontalSpread(INPUT_GROUP_X, INPUT_GROUP_W, TANK_W, 2, 0);
    public static final int INPUT_2_TANK_X = LayoutMath.horizontalSpread(INPUT_GROUP_X, INPUT_GROUP_W, TANK_W, 2, 1);

    public static final int OUTPUT_1_TANK_X = LayoutMath.horizontalSpread(OUTPUT_GROUP_X, OUTPUT_GROUP_W, TANK_W, 3, 0);
    public static final int OUTPUT_2_TANK_X = LayoutMath.horizontalSpread(OUTPUT_GROUP_X, OUTPUT_GROUP_W, TANK_W, 3, 1);
    public static final int OUTPUT_3_TANK_X = LayoutMath.horizontalSpread(OUTPUT_GROUP_X, OUTPUT_GROUP_W, TANK_W, 3, 2);

    public static final int INPUT_1_SLOT_X = INPUT_1_TANK_X;
    public static final int INPUT_2_SLOT_X = INPUT_2_TANK_X;

    public static final int OUTPUT_1_SLOT_X = OUTPUT_1_TANK_X;
    public static final int OUTPUT_2_SLOT_X = OUTPUT_2_TANK_X;
    public static final int OUTPUT_3_SLOT_X = OUTPUT_3_TANK_X;

    public static final int DUMP_BUTTON_SIZE = 12;
    public static final int DUMP_BUTTON_Y = 12;

    public static final int DUMP_INPUT_1_X = INPUT_1_TANK_X + 2;
    public static final int DUMP_INPUT_2_X = INPUT_2_TANK_X + 2;

    public static final int DUMP_OUTPUT_1_X = OUTPUT_1_TANK_X + 2;
    public static final int DUMP_OUTPUT_2_X = OUTPUT_2_TANK_X + 2;
    public static final int DUMP_OUTPUT_3_X = OUTPUT_3_TANK_X + 2;

    public static final int CENTER_PANEL_W = 44;
    public static final int CENTER_PANEL_H = 52;
    public static final int CENTER_PANEL_X = LayoutMath.centeredAt(MACHINE_PANEL_X, MACHINE_PANEL_W, CENTER_PANEL_W);
    public static final int CENTER_PANEL_Y = 30;

    public static final int CATALYST_SLOT_X = LayoutMath.centeredAt(MACHINE_PANEL_X, MACHINE_PANEL_W, StandardInventoryLayout.SLOT_SIZE);
    public static final int CATALYST_SLOT_Y = 38;

    public static final int PROGRESS_W = 36;
    public static final int PROGRESS_H = 6;
    public static final int PROGRESS_X = LayoutMath.centeredAt(MACHINE_PANEL_X, MACHINE_PANEL_W, PROGRESS_W);
    public static final int PROGRESS_Y = 64;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN;

    public static final int GUI_HEIGHT =
            MACHINE_PANEL_Y
                    + MACHINE_PANEL_H
                    + StandardInventoryLayout.INV_UPPER_MARGIN
                    + StandardInventoryLayout.TOTAL_H
                    + MACHINE_PANEL_Y;

    private CatalyticReformerLayout() {
    }
}