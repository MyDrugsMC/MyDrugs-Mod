package org.mydrugs.mydrugs.recipes.mixing_vat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record MixingVatRecipeInput(
        List<ItemStack> items,
        @Nullable ResourceLocation fluidId,
        int fluidAmount
) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }
}