package org.mydrugs.mydrugs.menu.layout;

public final class DistillerLayout {
    public static final int GUI_WIDTH = 216;

    public static final int MACHINE_PANEL_W = 196;
    public static final int MACHINE_PANEL_H = 94;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 13;

    private static final int SIDE_GUTTER = 16;
    private static final int TANK_GAP = 8;
    private static final int OUTPUT_GROUP_W = LayoutMath.groupWidth(StandardTankLayout.TANK_W, 2, TANK_GAP);
    private static final int OUTPUT_GROUP_X = MACHINE_PANEL_X + MACHINE_PANEL_W - SIDE_GUTTER - OUTPUT_GROUP_W;

    public static final int INPUT_TANK_X = MACHINE_PANEL_X + SIDE_GUTTER;
    public static final int INPUT_TANK_Y = MACHINE_PANEL_Y + 16;

    public static final int OUTPUT_A_TANK_X = LayoutMath.groupItemX(OUTPUT_GROUP_X, StandardTankLayout.TANK_W, TANK_GAP, 0);
    public static final int OUTPUT_A_TANK_Y = INPUT_TANK_Y;

    public static final int OUTPUT_B_TANK_X = LayoutMath.groupItemX(OUTPUT_GROUP_X, StandardTankLayout.TANK_W, TANK_GAP, 1);
    public static final int OUTPUT_B_TANK_Y = INPUT_TANK_Y;

    public static final int TANK_W = StandardTankLayout.TANK_W;
    public static final int TANK_H = StandardTankLayout.TANK_H;

    public static final int TANK_INNER_X_OFFSET = StandardTankLayout.INNER_X;
    public static final int TANK_INNER_Y_OFFSET = StandardTankLayout.INNER_Y;
    public static final int TANK_INNER_W = StandardTankLayout.INNER_W;
    public static final int TANK_INNER_H = StandardTankLayout.INNER_H;

    public static final int DUMP_BUTTON_SIZE = 12;
    public static final int DUMP_BUTTON_Y = MACHINE_PANEL_Y + 2;
    public static final int DUMP_INPUT_X = LayoutMath.dumpButtonX(INPUT_TANK_X, TANK_W, DUMP_BUTTON_SIZE);
    public static final int DUMP_OUTPUT_A_X = LayoutMath.dumpButtonX(OUTPUT_A_TANK_X, TANK_W, DUMP_BUTTON_SIZE);
    public static final int DUMP_OUTPUT_B_X = LayoutMath.dumpButtonX(OUTPUT_B_TANK_X, TANK_W, DUMP_BUTTON_SIZE);

    public static final int RUN_BUTTON_SIZE = 44;
    public static final int RUN_BUTTON_X = LayoutMath.centeredBetween(INPUT_TANK_X + TANK_W, OUTPUT_A_TANK_X, RUN_BUTTON_SIZE);
    public static final int RUN_BUTTON_Y = MACHINE_PANEL_Y + 25;

    public static final int REACTOR_CENTER_X = RUN_BUTTON_X + RUN_BUTTON_SIZE / 2;
    public static final int REACTOR_CENTER_Y = RUN_BUTTON_Y + RUN_BUTTON_SIZE / 2;
    public static final int REACTOR_OUTER_RADIUS = 18;
    public static final int REACTOR_INNER_RADIUS = 12;
    public static final int REACTOR_CORE_RADIUS = 8;

    public static final int PROGRESS_W = 64;
    public static final int PROGRESS_H = 6;
    public static final int PROGRESS_X = LayoutMath.centeredBetween(INPUT_TANK_X + TANK_W, OUTPUT_A_TANK_X, PROGRESS_W);
    public static final int PROGRESS_Y = MACHINE_PANEL_Y + 9;

    public static final int INPUT_SLOT_X = INPUT_TANK_X;
    public static final int INPUT_SLOT_Y = INPUT_TANK_Y + TANK_H + 3;

    public static final int OUTPUT_A_SLOT_X = OUTPUT_A_TANK_X;
    public static final int OUTPUT_A_SLOT_Y = INPUT_SLOT_Y;

    public static final int OUTPUT_B_SLOT_X = OUTPUT_B_TANK_X;
    public static final int OUTPUT_B_SLOT_Y = INPUT_SLOT_Y;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    public static final int TITLE_Y = 6;
    public static final int CPS_TEXT_X = REACTOR_CENTER_X;
    public static final int CPS_TEXT_Y = RUN_BUTTON_Y + RUN_BUTTON_SIZE + 4;
    public static final int SPEED_TEXT_X = REACTOR_CENTER_X;
    public static final int SPEED_TEXT_Y = CPS_TEXT_Y + 10;

    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private DistillerLayout() {
    }
}
