package org.mydrugs.mydrugs.menu.layout;

public final class RollerLayout {

    public static final int GUI_WIDTH = 196;

    public static final int MACHINE_PANEL_W = 176;
    public static final int MACHINE_PANEL_H = 64;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 13;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private static final int SIDE_GUTTER = 16;
    private static final int TOP_SLOT_Y = MACHINE_PANEL_Y + 14;
    private static final int BOTTOM_SLOT_Y = MACHINE_PANEL_Y + 38;

    public static final int PAPER_X = MACHINE_PANEL_X + SIDE_GUTTER;
    public static final int PAPER_Y = TOP_SLOT_Y;

    public static final int FILTER_X = PAPER_X;
    public static final int FILTER_Y = BOTTOM_SLOT_Y;

    public static final int INGREDIENT_1_X = PAPER_X + 32;
    public static final int INGREDIENT_1_Y = TOP_SLOT_Y;

    public static final int INGREDIENT_2_X = INGREDIENT_1_X;
    public static final int INGREDIENT_2_Y = BOTTOM_SLOT_Y;

    public static final int INGREDIENT_3_X = INGREDIENT_1_X + 24;
    public static final int INGREDIENT_3_Y = LayoutMath.centeredBetween(TOP_SLOT_Y, BOTTOM_SLOT_Y + StandardInventoryLayout.SLOT_SIZE, StandardInventoryLayout.SLOT_SIZE);

    public static final int OUTPUT_X = MACHINE_PANEL_X + MACHINE_PANEL_W - SIDE_GUTTER - StandardInventoryLayout.SLOT_SIZE;
    public static final int OUTPUT_Y = INGREDIENT_3_Y;

    public static final int WIDGET_W = 18;
    public static final int WIDGET_H = 42;
    public static final int WIDGET_X = LayoutMath.centeredBetween(INGREDIENT_3_X + StandardInventoryLayout.SLOT_SIZE, OUTPUT_X, WIDGET_W);
    public static final int WIDGET_Y = MACHINE_PANEL_Y + 11;

    public static final int PROGRESS_LABEL_X = WIDGET_X + WIDGET_W / 2;
    public static final int PROGRESS_LABEL_Y = MACHINE_PANEL_Y + 6;

    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private RollerLayout() {
    }
}
