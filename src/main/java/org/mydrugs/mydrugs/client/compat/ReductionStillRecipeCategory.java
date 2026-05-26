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
import org.mydrugs.mydrugs.recipes.reduction_still.ReductionStillRecipe;

final class ReductionStillRecipeCategory extends AbstractNiceRecipeCategory<ReductionStillRecipe> {
    static final RecipeType<ReductionStillRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "reduction_still"), ReductionStillRecipe.class);

    ReductionStillRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.reduction_still"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "REDUCTION_STILL_ITEM", "REDUCTION_STILL"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ReductionStillRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, centeredInLeftX() - 11, centeredY(), recipe.cuttings());
        addItemIngredient(builder, RecipeIngredientRole.INPUT, centeredInLeftX() + 11, centeredY(), recipe.solvent());
        addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX() - 11, centeredY(), recipe.extractResult());
        if (!recipe.pulpResult().isEmpty()) {
            addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX() + 11, centeredY(), recipe.pulpResult());
        }
    }
}
