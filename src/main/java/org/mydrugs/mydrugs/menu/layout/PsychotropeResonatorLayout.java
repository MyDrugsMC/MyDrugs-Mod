package org.mydrugs.mydrugs.menu.layout;

public final class PsychotropeResonatorLayout {
    public static final int GUI_WIDTH = 248;
    public static final int MACHINE_PANEL_X = 10;
    public static final int MACHINE_PANEL_Y = 15;
    public static final int MACHINE_PANEL_W = 228;
    public static final int MACHINE_PANEL_H = 112;

    public static final int MATERIAL_SLOT_X = 24;
    public static final int MATERIAL_SLOT_Y = 36;
    public static final int DREAM_SLOT_X = MATERIAL_SLOT_X;
    public static final int DREAM_SLOT_Y = MATERIAL_SLOT_Y;
    public static final int CORE_SLOT_X = 54;
    public static final int CORE_SLOT_Y = 36;
    public static final int DIARY_SLOT_X = 84;
    public static final int DIARY_SLOT_Y = 36;

    public static final int PROGRESS_BAR_X = 24;
    public static final int PROGRESS_BAR_Y = 69;
    public static final int PROGRESS_BAR_W = 82;
    public static final int PROGRESS_BAR_H = 8;

    public static final int DREAM_BUTTON_X = 24;
    public static final int INTEGRATION_BUTTON_X = 77;
    public static final int RECOVERY_BUTTON_X = 130;
    public static final int DIMENSION_BUTTON_X = 183;
    public static final int BUTTON_Y = 91;
    public static final int BUTTON_W = 46;
    public static final int BUTTON_H = 16;

    public static final int STATUS_X = 120;
    public static final int STATUS_Y = 30;
    public static final int FAILURE_PANEL_X = 10;
    public static final int FAILURE_PANEL_Y = 134;
    public static final int FAILURE_PANEL_W = 228;
    public static final int FAILURE_PANEL_H = 58;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(FAILURE_PANEL_Y, FAILURE_PANEL_H);
    public static final int GUI_HEIGHT = LayoutMath.guiHeight(FAILURE_PANEL_Y, FAILURE_PANEL_H);

    private PsychotropeResonatorLayout() {
    }
}
