package org.mydrugs.mydrugs.compat;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatRecipe;

import java.util.ArrayList;
import java.util.List;

final class MixingVatRecipeCategory extends AbstractNiceRecipeCategory<MixingVatRecipe> {
    static final RecipeType<MixingVatRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "mixing_vat"), MixingVatRecipe.class);

    MixingVatRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.mixing_vat"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "MIXING_VAT"),
                CategoryMode.LARGE
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MixingVatRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> items = new ArrayList<>();
        recipe.item1().ifPresent(items::add);
        recipe.item2().ifPresent(items::add);
        recipe.item3().ifPresent(items::add);
        recipe.item4().ifPresent(items::add);

        int itemAreaX = leftInnerX();
        int itemAreaY = leftInnerY();
        int itemAreaW = leftInnerW() - SLOT - s(8);
        int itemAreaH = leftInnerH();

        int fluidX = leftX + leftW - SLOT - s(8);
        int fluidY = leftInnerY();
        int fluidH = leftInnerH();

        int itemCount = items.size();
        if (itemCount > 0) {
            int cols = itemCount == 1 ? 1 : 2;
            int rows = (itemCount + cols - 1) / cols;

            for (int i = 0; i < itemCount; i++) {
                int row = i / cols;
                int col = i % cols;
                int rowCount = (row == rows - 1 && itemCount % cols != 0) ? itemCount % cols : cols;
                if (rowCount == 0) rowCount = cols;

                int x = spreadRegionX(itemAreaX, itemAreaW, rowCount, col);
                int y = spreadRegionY(itemAreaY, itemAreaH, rows, row);
                addItemIngredient(builder, RecipeIngredientRole.INPUT, x, y, items.get(i));
            }
        }

        List<Object> fluids = new ArrayList<>();
        recipe.fluidInput1().ifPresent(fluids::add);
        recipe.fluidInput2().ifPresent(fluids::add);

        for (int i = 0; i < fluids.size(); i++) {
            int y = spreadRegionY(fluidY, fluidH, fluids.size(), i);
            var fluid = fluids.get(i);
            addFluid(
                    builder,
                    RecipeIngredientRole.INPUT,
                    fluidX,
                    y,
                    JeiCompatUtil.idOf(fluid, "fluid", "fluidId"),
                    JeiCompatUtil.intOf(fluid, "amount")
            );
        }

        boolean hasItemOutput = !recipe.resultItem().isEmpty();
        boolean hasFluidOutput = recipe.resultFluid().isPresent();

        if (hasItemOutput && hasFluidOutput) {
            addItemStack(builder, RecipeIngredientRole.OUTPUT, spreadRightX(2, 0), centeredY(), recipe.resultItem());
            recipe.resultFluid().ifPresent(fluid ->
                    addFluid(builder, RecipeIngredientRole.OUTPUT, spreadRightX(2, 1), centeredY(), fluid.fluid(), fluid.amount())
            );
        } else if (hasItemOutput) {
            addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), recipe.resultItem());
        } else {
            recipe.resultFluid().ifPresent(fluid ->
                    addFluid(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), fluid.fluid(), fluid.amount())
            );
        }
    }

    @Override
    public void draw(MixingVatRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        super.draw(recipe, slots, g, mouseX, mouseY);
        drawPanelLabel(g, "ITEMS", leftInnerX(), panelY + s(2), leftInnerW() - SLOT - s(8));
        drawPanelLabel(g, "FLUIDS", leftX + leftW - SLOT - s(12), panelY + s(2), SLOT + s(10));
        drawBottomInfo(g, "Required stirs: " + recipe.requiredStirs());
        drawTitle(g);
    }
}

