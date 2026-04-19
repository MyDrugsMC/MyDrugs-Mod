package org.mydrugs.mydrugs.recipes.stomp_crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public record StompCraftingInput(List<ItemStack> stacks) implements RecipeInput {
    @Override
    public ItemStack getItem(int slot) {
        return stacks.get(slot);
    }

    @Override
    public int size() {
        return stacks.size();
    }
}