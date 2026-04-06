package org.mydrugs.mydrugs.menu.layout;

public final class RollerLayout {

    public static final int GUI_WIDTH = 176;

    public static final int MACHINE_PANEL_X = 14;
    public static final int MACHINE_PANEL_Y = 12;
    public static final int MACHINE_PANEL_W = 148;
    public static final int MACHINE_PANEL_H = 58;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN;

    public static final int PAPER_X = 26;
    public static final int PAPER_Y = 22;

    public static final int FILTER_X = 26;
    public static final int FILTER_Y = 44;

    public static final int INGREDIENT_1_X = 58;
    public static final int INGREDIENT_1_Y = 22;

    public static final int INGREDIENT_2_X = 58;
    public static final int INGREDIENT_2_Y = 44;

    public static final int INGREDIENT_3_X = 80;
    public static final int INGREDIENT_3_Y = 33;

    public static final int WIDGET_X = 107;
    public static final int WIDGET_Y = 20;
    public static final int WIDGET_W = 18;
    public static final int WIDGET_H = 42;

    public static final int OUTPUT_X = 136;
    public static final int OUTPUT_Y = 33;

    public static final int GUI_HEIGHT = MACHINE_PANEL_Y + MACHINE_PANEL_H + StandardInventoryLayout.INV_UPPER_MARGIN + StandardInventoryLayout.TOTAL_H + MACHINE_PANEL_Y;

    private RollerLayout() {
    }
}