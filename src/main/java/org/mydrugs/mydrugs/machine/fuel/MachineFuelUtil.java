package org.mydrugs.mydrugs.machine.fuel;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class MachineFuelUtil {
    private MachineFuelUtil() {
    }

    public static final FuelResolver VANILLA = (stack, level) -> {
        if (stack.isEmpty() || level == null) {
            return 0;
        }
        return stack.getBurnTime(null, level.fuelValues());
    };

    public static boolean isFuel(ItemStack stack, @Nullable Level level, FuelResolver resolver) {
        return resolver.getBurnTime(stack, level) > 0;
    }

    public static int getBurnTime(ItemStack stack, @Nullable Level level, FuelResolver resolver) {
        return resolver.getBurnTime(stack, level);
    }

    public static FuelUse consumeOne(ItemStack original, @Nullable Level level, FuelResolver resolver) {
        if (original.isEmpty()) {
            return new FuelUse(0, ItemStack.EMPTY);
        }

        int burnTime = resolver.getBurnTime(original, level);
        if (burnTime <= 0) {
            return new FuelUse(0, original.copy());
        }

        ItemStack working = original.copy();
        ItemStack remainder = working.getCraftingRemainder().copy();

        working.shrink(1);

        ItemStack remaining = working.isEmpty()
                ? (remainder.isEmpty() ? ItemStack.EMPTY : remainder)
                : working;

        return new FuelUse(burnTime, remaining);
    }

    public record FuelUse(int burnTime, ItemStack remainingStack) {
        public boolean consumed() {
            return this.burnTime > 0;
        }
    }
}