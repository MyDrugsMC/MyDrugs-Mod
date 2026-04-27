package org.mydrugs.mydrugs.compat;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.recipes.evaporation_tray.EvaporationTrayRecipe;

final class EvaporationTrayRecipeCategory extends AbstractNiceRecipeCategory<EvaporationTrayRecipe> {
    static final RecipeType<EvaporationTrayRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "evaporation_tray"), EvaporationTrayRecipe.class);

    EvaporationTrayRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.evaporation_tray"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "EVAPORATION_TRAY"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EvaporationTrayRecipe recipe, IFocusGroup focuses) {
        addFluid(builder, RecipeIngredientRole.INPUT, centeredInLeftX(), centeredY(), recipe.inputFluid(), recipe.inputAmount());
        addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), recipe.result());
    }
}

