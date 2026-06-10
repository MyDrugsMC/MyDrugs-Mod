package org.mydrugs.mydrugs.client.compat;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

import java.util.List;
import java.util.function.Function;

record JeiCategoryDescriptor<T>(
        String recipeTypeId,
        String titleKey,
        Function<IGuiHelper, IRecipeCategory<T>> categoryFactory,
        RecipeType<T> jeiType,
        String cacheMethod,
        Class<?> catalystOwner,
        String[] catalystFields
) {
    void registerCategory(IRecipeCategoryRegistration registration, IGuiHelper guiHelper) {
        registration.addRecipeCategories(categoryFactory.apply(guiHelper));
    }

    void registerRecipes(IRecipeRegistration registration) {
        List<T> recipes = JeiCompatUtil.cachedRecipes(cacheMethod);
        registration.addRecipes(jeiType, recipes);
    }

    void registerCatalyst(IRecipeCatalystRegistration registration) {
        JeiCompatUtil.registerFieldCatalyst(registration, jeiType, catalystOwner, catalystFields);
    }
}
