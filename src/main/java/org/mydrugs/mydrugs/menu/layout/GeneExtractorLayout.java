package org.mydrugs.mydrugs.menu.layout;

public final class GeneExtractorLayout {
    public static final int GUI_WIDTH = 176;
    public static final int MACHINE_PANEL_X = 8;
    public static final int MACHINE_PANEL_Y = 16;
    public static final int MACHINE_PANEL_W = 160;
    public static final int MACHINE_PANEL_H = 78;

    public static final int INPUT_SLOT_X = 29;
    public static final int INPUT_SLOT_Y = 49;

    public static final int OUTPUT_A_SLOT_X = 112;
    public static final int OUTPUT_A_SLOT_Y = 31;
    public static final int OUTPUT_B_SLOT_X = 112;
    public static final int OUTPUT_B_SLOT_Y = 49;
    public static final int OUTPUT_C_SLOT_X = 112;
    public static final int OUTPUT_C_SLOT_Y = 67;

    public static final int PROGRESS_X = 55;
    public static final int PROGRESS_Y = 54;
    public static final int PROGRESS_W = 42;
    public static final int PROGRESS_H = 8;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);
    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private GeneExtractorLayout() {
    }
}
