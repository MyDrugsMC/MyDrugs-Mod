package org.mydrugs.mydrugs.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.menu.RollerMenu;

public class RollerOutputSlot extends RollerMachineSlot {
    public RollerOutputSlot(RollerMenu menu, Container container, int slot, int x, int y) {
        super(menu, container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}