package org.mydrugs.mydrugs.menu.layout;

public final class SieveLayout {
    public static final int GUI_WIDTH = 196;
    public static final int MACHINE_PANEL_W = 176;
    public static final int MACHINE_PANEL_H = 50;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 13;

    private static final int SIDE_GUTTER = 22;

    public static final int INPUT_X = MACHINE_PANEL_X + SIDE_GUTTER;
    public static final int INPUT_Y = LayoutMath.centeredAt(MACHINE_PANEL_Y, MACHINE_PANEL_H, StandardInventoryLayout.SLOT_SIZE);

    public static final int RESULT_X = MACHINE_PANEL_X + MACHINE_PANEL_W - SIDE_GUTTER - StandardInventoryLayout.SLOT_SIZE;
    public static final int RESULT_Y = MACHINE_PANEL_Y + 7;
    public static final int BONUS_X = RESULT_X;
    public static final int BONUS_Y = MACHINE_PANEL_Y + 25;

    public static final int WIDGET_W = 17;
    public static final int WIDGET_H = 28;
    public static final int WIDGET_X = LayoutMath.centeredBetween(INPUT_X + StandardInventoryLayout.SLOT_SIZE, RESULT_X, WIDGET_W);
    public static final int WIDGET_Y = LayoutMath.centeredAt(MACHINE_PANEL_Y, MACHINE_PANEL_H, WIDGET_H);

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private SieveLayout() {
    }
}
