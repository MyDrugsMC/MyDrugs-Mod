package org.mydrugs.mydrugs.recipes.catalytic_reformer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;
import org.mydrugs.mydrugs.gas.GasStack;

public record CatalyticReformerRecipeInput(
        FluidStack inputFluid1,
        GasStack inputGas1,
        FluidStack inputFluid2,
        GasStack inputGas2,
        ItemStack catalyst
) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? this.catalyst : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.inputFluid1.isEmpty()
                && this.inputGas1.isEmpty()
                && this.inputFluid2.isEmpty()
                && this.inputGas2.isEmpty()
                && this.catalyst.isEmpty();
    }
}