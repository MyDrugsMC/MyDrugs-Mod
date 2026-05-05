package org.mydrugs.mydrugs.client.compat;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.recipes.coffee_pulping.CoffeePulpingRecipe;

final class CoffeePulpingRecipeCategory extends AbstractNiceRecipeCategory<CoffeePulpingRecipe> {
    static final RecipeType<CoffeePulpingRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "coffee_pulping"), CoffeePulpingRecipe.class);

    CoffeePulpingRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.manual_coffee_pulper"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "MANUAL_COFFEE_PULPER_ITEM", "MANUAL_COFFEE_PULPER"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CoffeePulpingRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, centeredInLeftX(), centeredY(), recipe.ingredient());
        addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX() - 11, centeredY(), recipe.beanResult());
        if (!recipe.biomassResult().isEmpty()) {
            addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX() + 11, centeredY(), recipe.biomassResult());
        }
    }
}
