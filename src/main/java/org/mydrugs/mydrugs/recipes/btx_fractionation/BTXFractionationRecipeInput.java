package org.mydrugs.mydrugs.recipes.btx_fractionation;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

/** Input to a {@link BTXFractionationRecipe}: the current contents of the tower's input tank. */
public record BTXFractionationRecipeInput(FluidStack inputFluid) implements RecipeInput {
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
        return inputFluid == null || inputFluid.isEmpty();
    }
}
