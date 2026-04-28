package org.mydrugs.mydrugs.recipes.steam_cracker;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;
import org.mydrugs.mydrugs.gas.GasStack;

public record SteamCrackerRecipeInput(FluidStack inputFluid, GasStack inputGas) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return this.inputFluid.isEmpty() && this.inputGas.isEmpty();
    }
}
