package org.mydrugs.mydrugs.items;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public interface SingleSlotContainerItem {
    boolean mayPlace(ItemStack itemStack, ServerLevel level);
}
