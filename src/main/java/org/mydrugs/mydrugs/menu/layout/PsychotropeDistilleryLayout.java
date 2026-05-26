package org.mydrugs.mydrugs.menu.layout;

public final class PsychotropeDistilleryLayout {
    public static final int GUI_WIDTH = 196;
    public static final int MACHINE_PANEL_X = 10;
    public static final int MACHINE_PANEL_Y = 15;
    public static final int MACHINE_PANEL_W = 176;
    public static final int MACHINE_PANEL_H = 78;

    // Inputs: drug + reagent in the top row, fuel below the drug — three slots grouped on the
    // left as a coherent "input column" instead of being scattered between input and progress.
    public static final int DRUG_SLOT_X = 22;
    public static final int DRUG_SLOT_Y = 28;
    public static final int REAGENT_SLOT_X = 48;
    public static final int REAGENT_SLOT_Y = 28;
    public static final int FUEL_SLOT_X = 22;
    public static final int FUEL_SLOT_Y = 54;

    // Outputs stacked on the right, vertically aligned with the input column.
    public static final int EXTRACT_SLOT_X = 138;
    public static final int EXTRACT_SLOT_Y = 28;
    public static final int RESIDUE_SLOT_X = 138;
    public static final int RESIDUE_SLOT_Y = 54;

    // Progress bar centered between input and output columns; burn meter sits under it.
    public static final int PROGRESS_X = 76;
    public static final int PROGRESS_Y = 33;
    public static final int PROGRESS_W = 56;
    public static final int PROGRESS_H = 8;

    public static final int BURN_X = 76;
    public static final int BURN_Y = 58;
    public static final int BURN_W = 56;
    public static final int BURN_H = 6;

    public static final int PLAYER_INV_X = LayoutMath.centered(GUI_WIDTH, StandardInventoryLayout.PLAYER_INV_PANEL_W);
    public static final int PLAYER_INV_Y = LayoutMath.inventoryY(MACHINE_PANEL_Y, MACHINE_PANEL_H);
    public static final int GUI_HEIGHT = LayoutMath.guiHeight(MACHINE_PANEL_Y, MACHINE_PANEL_H);

    private PsychotropeDistilleryLayout() {
    }
}
