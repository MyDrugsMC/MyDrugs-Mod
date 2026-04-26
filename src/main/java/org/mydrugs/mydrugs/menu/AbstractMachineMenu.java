package org.mydrugs.mydrugs.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
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

    protected boolean moveToPlayerInventory(ItemStack stack, int playerInventoryStart, int hotbarEnd) {
        return this.moveItemStackTo(stack, playerInventoryStart, hotbarEnd, true);
    }

    protected boolean moveToPlayerInventory(ItemStack stack, int playerInventoryStart, int hotbarEnd, boolean reverse) {
        return this.moveItemStackTo(stack, playerInventoryStart, hotbarEnd, reverse);
    }

    protected boolean moveBetweenPlayerInventoryAndHotbar(
            ItemStack stack,
            int sourceIndex,
            int playerInventoryStart,
            int playerInventoryEnd,
            int hotbarStart,
            int hotbarEnd
    ) {
        if (sourceIndex < playerInventoryEnd) {
            return this.moveItemStackTo(stack, hotbarStart, hotbarEnd, false);
        }

        return sourceIndex < hotbarEnd
                && this.moveItemStackTo(stack, playerInventoryStart, playerInventoryEnd, false);
    }

    protected ItemStack finishQuickMove(Player player, Slot sourceSlot, ItemStack sourceStack, ItemStack originalStack) {
        if (sourceStack.isEmpty()) {
            sourceSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(player, sourceStack);
        return originalStack;
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
