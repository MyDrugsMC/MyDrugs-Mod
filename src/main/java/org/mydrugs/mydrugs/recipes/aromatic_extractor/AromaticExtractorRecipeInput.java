package org.mydrugs.mydrugs.recipes.aromatic_extractor;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.Nullable;

public record AromaticExtractorRecipeInput(
        @Nullable ResourceLocation inputFluid,
        int inputAmount,
        @Nullable ResourceLocation catalystFluid,
        int catalystAmount
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
        return (inputFluid == null || inputAmount <= 0) && (catalystFluid == null || catalystAmount <= 0);
    }
}
