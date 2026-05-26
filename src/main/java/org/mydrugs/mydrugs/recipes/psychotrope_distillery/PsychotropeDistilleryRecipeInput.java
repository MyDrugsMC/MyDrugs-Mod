package org.mydrugs.mydrugs.recipes.psychotrope_distillery;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record PsychotropeDistilleryRecipeInput(ItemStack drugInput, ItemStack reagentInput) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> this.drugInput;
            case 1 -> this.reagentInput;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return this.drugInput.isEmpty() && this.reagentInput.isEmpty();
    }
}
