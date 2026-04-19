package org.mydrugs.mydrugs.machine;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;

public interface MachineRecipe<I extends RecipeInput> extends Recipe<I> {

    default int workTime() {
        return 0;
    }

    @Override
    default boolean isSpecial() {
        return true;
    }

    @Override
    default RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.FURNACE_MISC;
    }
}
