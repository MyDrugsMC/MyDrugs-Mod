package org.mydrugs.mydrugs.menu.slot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.items.rolling.RollerLogic;
import org.mydrugs.mydrugs.menu.RollerMenu;

public class RollerMachineSlot extends Slot {
    protected final RollerMenu menu;

    public RollerMachineSlot(RollerMenu menu, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.menu = menu;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return switch (this.getSlotIndex()) {
            case RollerMenu.PAPER_SLOT -> RollerLogic.isPaper(stack);
            case RollerMenu.FILTER_SLOT -> RollerLogic.isFilter(stack);
            case RollerMenu.INGREDIENT_1_SLOT,
                 RollerMenu.INGREDIENT_2_SLOT,
                 RollerMenu.INGREDIENT_3_SLOT -> RollerLogic.isRollingIngredient(stack);
            case RollerMenu.OUTPUT_SLOT -> false;
            default -> false;
        };
    }

    @Override
    public int getMaxStackSize() {
        return this.getSlotIndex() == RollerMenu.OUTPUT_SLOT ? 64 : 1;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.menu.onMachineContentsChanged();
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
        this.menu.onMachineContentsChanged();
    }
}