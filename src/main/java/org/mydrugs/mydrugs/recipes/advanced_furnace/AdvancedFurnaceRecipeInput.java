package org.mydrugs.mydrugs.recipes.advanced_furnace;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record AdvancedFurnaceRecipeInput(ItemStack inputA, ItemStack inputB) implements RecipeInput {

    @Override
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case 0 -> inputA;
            case 1 -> inputB;
            default -> throw new IllegalArgumentException("No item for slot " + slot);
        };
    }

    @Override
    public int size() {
        return 2;
    }
}