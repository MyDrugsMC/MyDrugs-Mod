package org.mydrugs.mydrugs.menu.layout;

public final class SteamCrackerLayout {
    public static final int GUI_WIDTH = 256;
    public static final int MACHINE_PANEL_W = 236;
    public static final int MACHINE_PANEL_H = 96;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 13;

    public static final int TANK_W = StandardTankLayout.TANK_W;
    public static final int TANK_H = StandardTankLayout.TANK_H;
    public static final int TANK_INNER_H = StandardTankLayout.INNER_H;
    public static final int TANK_Y = MACHINE_PANEL_Y + 17;
    public static final int SLOT_Y = TANK_Y + TANK_H + 3;

    public static final int INPUT_TANK_X = MACHINE_PANEL_X + 16;
    public static final int INPUT_SLOT_X = INPUT_TANK_X;

    private static final int OUTPUT_GROUP_X = MACHINE_PANEL_X + MACHINE_PANEL_W - 16 - LayoutMath.groupWidth(TANK_W, 4, 8);
    public static final int OUTPUT_1_TANK_X = LayoutMath.groupItemX(OUTPUT_GROUP_X, TANK_W, 8, 0);
    public static final int OUTPUT_2_TANK_X = LayoutMath.groupItemX(OUTPUT_GROUP_X, TANK_W, 8, 1);
    public static final int OUTPUT_3_TANK_X = LayoutMath.groupItemX(OUTPUT_GROUP_X, TANK_W, 8, 2);
    public static final int OUTPUT_4_TANK_X = LayoutMath.groupItemX(OUTPUT_GROUP_X, TANK_W, 8, 3);
    public static final int OUTPUT_1_SLOT_X = OUTPUT_1_TANK_X;
    public static final int OUTPUT_2_SLOT_X = OUTPUT_2_TANK_X;
    public static final int OUTPUT_3_SLOT_X = OUTPUT_3_TANK_X;
    public static final int OUTPUT_4_SLOT_X = OUTPUT_4_TANK_X;

    public static final int DUMP_BUTTON_SIZE = 12;
    public static final int DUMP_BUTTON_Y = MACHINE_PANEL_Y + 3;
    public static final int DUMP_INPUT_X = LayoutMath.dumpButtonX(INPUT_TANK_X, TANK_W, DUMP_BUTTON_SIZE);
    public static final int DUMP_OUTPUT_1_X = LayoutMath.dumpButtonX(OUTPUT_1_TANK_X, TANK_W, DUMP_BUTTON_SIZE);
    public static final int DUMP_OUTPUT_2_X = LayoutMath.dumpButtonX(OUTPUT_2_TANK_X, TANK_W, DUMP_BUTTON_SIZE);
    public static final int DUMP_OUTPUT_3_X = LayoutMath.dumpButtonX(OUTPUT_3_TANK_X, TANK_W, DUMP_BUTTON_SIZE);
    public static final int DUMP_OUTPUT_4_X = LayoutMath.dumpButtonX(OUTPUT_4_TANK_X, TANK_W, DUMP_BUTTON_SIZE);

    public static final int CENTER_PANEL_W = 46;
    public static final int CENTER_PANEL_H = 66;
    public static final int CENTER_PANEL_X = LayoutMath.centeredBetween(INPUT_TANK_X + TANK_W, OUTPUT_1_TANK_X, CENTER_PANEL_W);
    public static final int CENTER_PANEL_Y = MACHINE_PANEL_Y + 23;
    public static final int FUEL_SLOT_X = LayoutMath.centeredAt(CENTER_PANEL_X, CENTER_PANEL_W, StandardInventoryLayout.SLOT_SIZE) + 1;
    public static final int FUEL_SLOT_Y = CENTER_PANEL_Y + 28;
    public static final int BURN_X = CENTER_PANEL_X + 8;
    public static final int BURN_Y = CENTER_PANEL_Y + 51;
    public static final int BURN_W = 30;
    public static final int BURN_H = 4;
    public static final int PROGRESS_W = 52;
    public static final int PROGRESS_X = LayoutMath.centeredAt(CENTER_PANEL_X, CENTER_PANEL_W, PROGRESS_W);
    public static final int PROGRESS_Y = CENTER_PANEL_Y - 12;
    public static final int PROGRESS_H = 5;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);
    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private SteamCrackerLayout() {
    }
}
