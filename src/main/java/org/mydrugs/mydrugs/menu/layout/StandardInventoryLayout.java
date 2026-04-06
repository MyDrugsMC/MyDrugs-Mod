package org.mydrugs.mydrugs.menu.layout;

public final class StandardInventoryLayout {
    public static final int SLOT_SIZE = 18;
    public static final int PANEL_INNER_OFFSET = 2;

    public static final int PLAYER_INV_PANEL_W = 164;
    public static final int PLAYER_INV_PANEL_H = 56;

    public static final int HOTBAR_PANEL_W = 164;
    public static final int HOTBAR_PANEL_H = 20;

    public static final int HOTBAR_GAP = 6;

    public static final int PLAYER_INV_ROWS = 3;
    public static final int PLAYER_INV_COLS = 9;
    public static final int HOTBAR_COLS = 9;
    public static final int INV_UPPER_MARGIN = 12;

    public static final int TOTAL_H = PLAYER_INV_PANEL_H + HOTBAR_GAP + HOTBAR_PANEL_H;

    private StandardInventoryLayout() {
    }

    public static int hotbarPanelY(int playerInvPanelY) {
        return playerInvPanelY + PLAYER_INV_PANEL_H + HOTBAR_GAP;
    }

    public static int playerSlotX(int playerInvPanelX, int col) {
        return playerInvPanelX + PANEL_INNER_OFFSET + col * SLOT_SIZE;
    }

    public static int playerSlotY(int playerInvPanelY, int row) {
        return playerInvPanelY + PANEL_INNER_OFFSET + row * SLOT_SIZE;
    }

    public static int hotbarSlotX(int playerInvPanelX, int col) {
        return playerInvPanelX + PANEL_INNER_OFFSET + col * SLOT_SIZE;
    }

    public static int hotbarSlotY(int playerInvPanelY) {
        return hotbarPanelY(playerInvPanelY) + PANEL_INNER_OFFSET;
    }

    public static int inventoryLabelY(int playerInvPanelY) {
        return playerInvPanelY - 10;
    }
}