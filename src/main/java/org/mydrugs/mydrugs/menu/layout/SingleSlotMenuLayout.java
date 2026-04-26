package org.mydrugs.mydrugs.menu.layout;

public final class SingleSlotMenuLayout {
    public static final int GUI_WIDTH = 196;

    public static final int MACHINE_PANEL_W = 176;
    public static final int MACHINE_PANEL_H = 50;
    public static final int MACHINE_PANEL_X = LayoutMath.centered(GUI_WIDTH, MACHINE_PANEL_W);
    public static final int MACHINE_PANEL_Y = 13;

    public static final int STORAGE_SLOT_X = LayoutMath.centeredAt(MACHINE_PANEL_X, MACHINE_PANEL_W, StandardInventoryLayout.SLOT_SIZE);
    public static final int STORAGE_SLOT_Y = LayoutMath.centeredAt(MACHINE_PANEL_Y, MACHINE_PANEL_H, StandardInventoryLayout.SLOT_SIZE);

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private SingleSlotMenuLayout() {
    }
}
