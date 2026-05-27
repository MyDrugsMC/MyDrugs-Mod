package org.mydrugs.mydrugs.menu.layout;

public final class PsychotropeResonatorLayout {
    public static final int GUI_WIDTH = 248;
    public static final int MACHINE_PANEL_X = 10;
    public static final int MACHINE_PANEL_Y = 15;
    public static final int MACHINE_PANEL_W = 228;
    public static final int MACHINE_PANEL_H = 76;

    public static final int MATERIAL_SLOT_X = 24;
    public static final int MATERIAL_SLOT_Y = 36;
    public static final int DREAM_SLOT_X = MATERIAL_SLOT_X;
    public static final int DREAM_SLOT_Y = MATERIAL_SLOT_Y;
    public static final int CORE_SLOT_X = 54;
    public static final int CORE_SLOT_Y = 36;
    public static final int DIARY_SLOT_X = 84;
    public static final int DIARY_SLOT_Y = 36;

    public static final int PROGRESS_BAR_X = 24;
    public static final int PROGRESS_BAR_Y = 58;
    public static final int PROGRESS_BAR_W = 82;
    public static final int PROGRESS_BAR_H = 8;

    public static final int DREAM_BUTTON_X = 24;
    public static final int INTEGRATION_BUTTON_X = 77;
    public static final int RECOVERY_BUTTON_X = 130;
    public static final int DIMENSION_BUTTON_X = 183;
    public static final int BUTTON_Y = 70;
    public static final int BUTTON_W = 46;
    public static final int BUTTON_H = 16;

    public static final int STATUS_X = 120;
    public static final int STATUS_Y = 30;
    /** Y offset (from STATUS_Y) of the drug-target line. */
    public static final int STATUS_DRUG_Y_OFFSET = 12;
    /** Y offset (from STATUS_Y) of the dimension-status line. */
    public static final int STATUS_DIMENSION_Y_OFFSET = 22;

    public static final int FAILURE_PANEL_X = 10;
    public static final int FAILURE_PANEL_Y = 98;
    public static final int FAILURE_PANEL_W = 228;
    public static final int FAILURE_PANEL_H = 50;
    /** Inner header / padding for the scrollable failure-checklist body. */
    public static final int FAILURE_PANEL_HEADER_H = 11;
    public static final int FAILURE_PANEL_BOTTOM_PAD = 4;
    public static final int FAILURE_PANEL_LINE_HEIGHT = 11;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(FAILURE_PANEL_Y, FAILURE_PANEL_H);
    // LayoutMath.guiHeight(panelY, panelH) uses panelY as a symmetric bottom margin. Passing
    // FAILURE_PANEL_Y (98) would dump that many px of blank space under the inventory; mirror
    // MACHINE_PANEL_Y (15) instead so the bottom margin matches the top.
    public static final int GUI_HEIGHT = PLAYER_INV_Y + StandardInventoryLayout.TOTAL_H + MACHINE_PANEL_Y;

    private PsychotropeResonatorLayout() {
    }
}
