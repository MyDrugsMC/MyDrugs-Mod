package org.mydrugs.mydrugs.menu;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.items.SingleSlotContainerItem;
import org.mydrugs.mydrugs.menu.layout.SingleSlotMenuLayout;

public class SingleSlotMenu extends AbstractMachineMenu {
    private static final int BANG_SLOT = 0;
    private static final int PLAYER_INV_START = 1;
    private static final int PLAYER_INV_END = 28;
    private static final int HOTBAR_START = 28;
    private static final int HOTBAR_END = 37;

    private final Container container;
    private final ItemStack carrier;
    private final InteractionHand hand;

    public SingleSlotMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, ItemStack.EMPTY, InteractionHand.MAIN_HAND, null);
    }

    public SingleSlotMenu(int containerId, Inventory playerInv, ItemStack carrier, InteractionHand hand, ServerLevel level) {
        super(ModMenus.BANG_CONTAINER.get(), containerId);

        this.carrier = carrier;
        this.hand = hand;
        this.container = new SingleSlotItemContainer(carrier);

        this.addSlot(new Slot(this.container, 0, SingleSlotMenuLayout.STORAGE_SLOT_X, SingleSlotMenuLayout.STORAGE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                if (carrier.getItem() instanceof SingleSlotContainerItem item) {
                    return item.mayPlace(stack, level);
                }
                return false;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                return 1;
            }
        });

        this.addPlayerInventorySlots(playerInv, SingleSlotMenuLayout.PLAYER_INV_X, SingleSlotMenuLayout.PLAYER_INV_Y);
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.carrier.isEmpty()) {
            return true;
        }

        return player.getItemInHand(this.hand) == this.carrier;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack quickMoved = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack raw = slot.getItem();
            quickMoved = raw.copy();

            if (index == BANG_SLOT) {
                if (!this.moveToPlayerInventory(raw, PLAYER_INV_START, HOTBAR_END)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(raw, BANG_SLOT, BANG_SLOT + 1, false)) {
                if (!this.moveBetweenPlayerInventoryAndHotbar(
                        raw,
                        index,
                        PLAYER_INV_START,
                        PLAYER_INV_END,
                        HOTBAR_START,
                        HOTBAR_END
                )) {
                    return ItemStack.EMPTY;
                }
            }

            return this.finishQuickMove(player, slot, raw, quickMoved);
        }

        return quickMoved;
    }
}
