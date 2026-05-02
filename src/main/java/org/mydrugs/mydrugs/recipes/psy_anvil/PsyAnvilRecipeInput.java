package org.mydrugs.mydrugs.recipes.psy_anvil;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public record PsyAnvilRecipeInput(int width, int height, NonNullList<ItemStack> stacks) implements RecipeInput {
    public static PsyAnvilRecipeInput of(List<ItemStack> stacks) {
        NonNullList<ItemStack> copy = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < Math.min(stacks.size(), 9); i++) {
            copy.set(i, stacks.get(i));
        }
        return new PsyAnvilRecipeInput(3, 3, copy);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.stacks.get(slot);
    }

    public ItemStack getItem(int x, int y) {
        return this.stacks.get(x + y * this.width);
    }

    @Override
    public int size() {
        return this.stacks.size();
    }
}
