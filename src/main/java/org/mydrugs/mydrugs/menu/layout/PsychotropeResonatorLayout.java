package org.mydrugs.mydrugs.menu.layout;

public final class PsychotropeResonatorLayout {
    public static final int GUI_WIDTH = 226;
    public static final int MACHINE_PANEL_X = 10;
    public static final int MACHINE_PANEL_Y = 15;
    public static final int MACHINE_PANEL_W = 206;
    public static final int MACHINE_PANEL_H = 106;

    public static final int DREAM_SLOT_X = 24;
    public static final int DREAM_SLOT_Y = 38;
    public static final int CORE_SLOT_X = 54;
    public static final int CORE_SLOT_Y = 38;
    public static final int DIARY_SLOT_X = 84;
    public static final int DIARY_SLOT_Y = 38;
    public static final int OUTPUT_SLOT_X = 186;
    public static final int OUTPUT_SLOT_Y = 38;

    public static final int PROGRESS_BAR_X = 24;
    public static final int PROGRESS_BAR_Y = 72;
    public static final int PROGRESS_BAR_W = 82;
    public static final int PROGRESS_BAR_H = 8;

    public static final int DREAM_BUTTON_X = 24;
    public static final int INTEGRATION_BUTTON_X = 75;
    public static final int RECOVERY_BUTTON_X = 126;
    public static final int DIMENSION_BUTTON_X = 177;
    public static final int BUTTON_Y = 88;
    public static final int BUTTON_W = 40;
    public static final int BUTTON_H = 16;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);
    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private PsychotropeResonatorLayout() {
    }
}
