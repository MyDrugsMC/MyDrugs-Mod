package org.mydrugs.mydrugs.recipes.psy_mixer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record PsyMixerRecipeInput(
        ItemStack base,
        ItemStack material,
        ItemStack catalyst,
        ItemStack stabilizer,
        ItemStack vessel
) implements RecipeInput {
    @Override
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case 0 -> base;
            case 1 -> material;
            case 2 -> catalyst;
            case 3 -> stabilizer;
            case 4 -> vessel;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 5;
    }
}
