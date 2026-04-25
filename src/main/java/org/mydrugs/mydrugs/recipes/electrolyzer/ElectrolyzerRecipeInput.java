package org.mydrugs.mydrugs.recipes.electrolyzer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.Nullable;

public record ElectrolyzerRecipeInput(@Nullable ResourceLocation inputFluid, int inputAmount) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return inputFluid == null || inputAmount <= 0;
    }
}
