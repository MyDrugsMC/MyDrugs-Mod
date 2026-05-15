package org.mydrugs.mydrugs.menu.layout;

public final class KrisprKas9CombinatorLayout {
    public static final int GUI_WIDTH = 176;
    public static final int MACHINE_PANEL_X = 8;
    public static final int MACHINE_PANEL_Y = 16;
    public static final int MACHINE_PANEL_W = 160;
    public static final int MACHINE_PANEL_H = 82;

    public static final int INPUT_A_SLOT_X = 28;
    public static final int INPUT_A_SLOT_Y = 41;
    public static final int INPUT_B_SLOT_X = 28;
    public static final int INPUT_B_SLOT_Y = 63;
    public static final int OUTPUT_SLOT_X = 126;
    public static final int OUTPUT_SLOT_Y = 52;

    public static final int PROGRESS_X = 57;
    public static final int PROGRESS_Y = 57;
    public static final int PROGRESS_W = 48;
    public static final int PROGRESS_H = 8;

    public static final int STABILITY_TEXT_X = 52;
    public static final int STABILITY_TEXT_Y = 29;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);
    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private KrisprKas9CombinatorLayout() {
    }
}
