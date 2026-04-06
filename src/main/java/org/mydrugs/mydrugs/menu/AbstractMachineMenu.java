package org.mydrugs.mydrugs.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.mydrugs.mydrugs.menu.layout.StandardInventoryLayout;

public abstract class AbstractMachineMenu extends AbstractContainerMenu {
    protected AbstractMachineMenu(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    protected void addPlayerInventorySlots(Inventory playerInventory, int playerInvX, int playerInvY) {
        for (int row = 0; row < StandardInventoryLayout.PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < StandardInventoryLayout.PLAYER_INV_COLS; col++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        StandardInventoryLayout.playerSlotX(playerInvX, col),
                        StandardInventoryLayout.playerSlotY(playerInvY, row)
                ));
            }
        }

        for (int col = 0; col < StandardInventoryLayout.HOTBAR_COLS; col++) {
            this.addSlot(new Slot(
                    playerInventory,
                    col,
                    StandardInventoryLayout.hotbarSlotX(playerInvX, col),
                    StandardInventoryLayout.hotbarSlotY(playerInvY)
            ));
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
    }

    @Override
    public void setData(int id, int value) {
        super.setData(id, value);
    }
}