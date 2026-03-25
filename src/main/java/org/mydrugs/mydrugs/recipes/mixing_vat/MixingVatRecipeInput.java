package org.mydrugs.mydrugs.recipes.mixing_vat;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public record MixingVatRecipeInput(
        List<ItemStack> items,
        List<MixingVatFluidStack> fluids
) implements RecipeInput {

    public MixingVatRecipeInput {
        items = List.copyOf(items);
        fluids = List.copyOf(fluids);
    }

    @Override
    public ItemStack getItem(int index) {
        if (index < 0 || index >= items.size()) {
            return ItemStack.EMPTY;
        }
        return items.get(index);
    }

    @Override
    public int size() {
        return Math.max(1, items.size());
    }

    @Override
    public boolean isEmpty() {
        boolean noItems = items.stream().allMatch(ItemStack::isEmpty);
        boolean noFluids = fluids.isEmpty() || fluids.stream().allMatch(f -> f.amount() <= 0);
        return noItems && noFluids;
    }
}