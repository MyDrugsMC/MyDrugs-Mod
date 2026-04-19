package org.mydrugs.mydrugs.recipes.biochemical_reactor;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record BiochemicalReactorRecipeInput(ItemStack ergot, ItemStack tryptophan) implements RecipeInput {
    @Override
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case 0 -> this.ergot;
            case 1 -> this.tryptophan;
            default -> throw new IllegalArgumentException("No item for slot " + slot);
        };
    }

    @Override
    public int size() {
        return 2;
    }
}