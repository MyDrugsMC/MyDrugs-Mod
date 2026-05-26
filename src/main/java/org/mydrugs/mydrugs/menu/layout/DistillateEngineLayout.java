package org.mydrugs.mydrugs.menu.layout;

public final class DistillateEngineLayout {
    public static final int GUI_WIDTH = 212;
    public static final int MACHINE_PANEL_X = 10;
    public static final int MACHINE_PANEL_Y = 15;
    public static final int MACHINE_PANEL_W = 192;
    public static final int MACHINE_PANEL_H = 86;

    public static final int FUEL_SLOT_X = 22;
    public static final int FUEL_SLOT_Y = 35;
    public static final int REGULATOR_SLOT_X = 48;
    public static final int REGULATOR_SLOT_Y = 35;
    public static final int WASTE_SLOT_X = 172;
    public static final int WASTE_SLOT_Y = 49;

    public static final int CURRENT_BAR_X = 82;
    public static final int CURRENT_BAR_Y = 25;
    public static final int CURRENT_BAR_W = 74;
    public static final int CURRENT_BAR_H = 10;

    public static final int FUEL_BAR_X = 82;
    public static final int FUEL_BAR_Y = 45;
    public static final int FUEL_BAR_W = 74;
    public static final int FUEL_BAR_H = 8;

    public static final int STRAIN_BAR_X = 82;
    public static final int STRAIN_BAR_Y = 64;
    public static final int STRAIN_BAR_W = 74;
    public static final int STRAIN_BAR_H = 8;

    public static final int RADIUS_DOWN_X = 20;
    public static final int RADIUS_UP_X = 44;
    public static final int RADIUS_BUTTON_Y = 62;
    public static final int RADIUS_BUTTON_W = 18;
    public static final int RADIUS_BUTTON_H = 14;

    public static final int SHOW_AREA_X = 64;
    public static final int SHOW_AREA_Y = 62;
    public static final int SHOW_AREA_W = 16;
    public static final int SHOW_AREA_H = 14;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);
    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private DistillateEngineLayout() {
    }
}
