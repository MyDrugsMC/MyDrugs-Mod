package org.mydrugs.mydrugs.recipes.growthchamber;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record GrowthChamberRecipeInput(ItemStack inputStack, ItemStack middleStack) implements RecipeInput {
    @Override
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case 0 -> this.inputStack;
            case 1 -> this.middleStack;
            default -> throw new IllegalArgumentException("No item for index " + slot);
        };
    }

    @Override
    public int size() {
        return 2;
    }
}