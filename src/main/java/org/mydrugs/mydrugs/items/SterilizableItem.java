package org.mydrugs.mydrugs.items;

import net.minecraft.world.item.ItemStack;

public interface SterilizableItem {
    boolean canBeSterilized(ItemStack stack);

    ItemStack createSterilizedStack(ItemStack stack);
}
