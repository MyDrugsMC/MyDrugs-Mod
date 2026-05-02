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
import org.mydrugs.mydrugs.recipes.drying.DryingRecipe;

final class DryingRecipeCategory extends AbstractNiceRecipeCategory<DryingRecipe> {
    static final RecipeType<DryingRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "drying"), DryingRecipe.class);

    DryingRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.drying_rack"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "DRYING_RACK"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DryingRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, centeredInLeftX(), centeredY(), recipe.input());
        addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), recipe.result());
    }
}

