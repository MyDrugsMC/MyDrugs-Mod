package org.mydrugs.mydrugs.recipes.grinder;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

public class GrindingRecipes {
    public static GrindingRecipe get(ItemStack itemStack, ServerLevel level) {
        SingleRecipeInput input = new SingleRecipeInput(itemStack);

        if (level == null) {
            return null;
        }

        RecipeHolder<GrindingRecipe> recipeHolder = level.recipeAccess()
                .getRecipeFor(ModRecipeTypes.GRINDING.get(), input, level).orElse(null);

        if (recipeHolder == null) {
            return null;
        }
        return recipeHolder.value();
    }
}
