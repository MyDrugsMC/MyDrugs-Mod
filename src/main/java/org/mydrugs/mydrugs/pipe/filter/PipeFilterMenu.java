package org.mydrugs.mydrugs.pipe.filter;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.menu.ModMenus;

public class PipeFilterMenu extends AbstractContainerMenu {
    public PipeFilterMenu(int containerId, Inventory playerInventory) {
        super(ModMenus.PIPE_FILTER.get(), containerId);
        // TODO Phase 6: add ghost/filter slots and server payload handling.
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
