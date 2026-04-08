package org.mydrugs.mydrugs.recipes.chemical_reactor;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;
import org.mydrugs.mydrugs.gas.GasStack;

public record ChemicalReactorRecipeInput(
        GasStack primaryGas,
        GasStack secondaryGas,
        FluidStack secondaryFluid
) implements RecipeInput {
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
        return primaryGas.isEmpty() && secondaryGas.isEmpty() && secondaryFluid.isEmpty();
    }
}