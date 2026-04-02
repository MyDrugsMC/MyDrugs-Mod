package org.mydrugs.mydrugs.recipes.evaporation_tray;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.Nullable;

public record EvaporationTrayRecipeInput(@Nullable ResourceLocation fluidId, int fluidAmount) implements RecipeInput {
    @Override
    public ItemStack getItem(int slot) {
        throw new IllegalArgumentException("No item for index " + slot);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return fluidId == null || fluidAmount <= 0;
    }
}