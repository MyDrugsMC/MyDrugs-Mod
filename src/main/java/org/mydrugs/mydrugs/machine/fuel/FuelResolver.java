package org.mydrugs.mydrugs.machine.fuel;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface FuelResolver {
    int getBurnTime(ItemStack stack, @Nullable Level level);
}