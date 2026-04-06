package org.mydrugs.mydrugs.recipes.advanced_mixing_vat;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;
import org.mydrugs.mydrugs.gas.GasStack;

import java.util.List;

public record AdvancedMixingVatRecipeInput(
        List<ItemStack> items,
        FluidStack inputA,
        FluidStack inputB,
        FluidStack inputC,
        GasStack gas
) implements RecipeInput {
    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public int size() {
        return this.items.size();
    }
}