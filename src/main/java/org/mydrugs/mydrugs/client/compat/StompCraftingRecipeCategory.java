package org.mydrugs.mydrugs.client.compat;

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
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.recipes.stomp_crafting.StompCraftingRecipe;

import java.util.List;

final class StompCraftingRecipeCategory extends AbstractNiceRecipeCategory<StompCraftingRecipe> {
    static final RecipeType<StompCraftingRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "stomp_crafting"), StompCraftingRecipe.class);

    StompCraftingRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("item.mydrugs.stomp_plate"),
                JeiCompatUtil.iconFromField(helper, ModItems.class, "STOMP_PLATE"),
                CategoryMode.LARGE
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, StompCraftingRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.expandedIngredients();

        int gridX = leftInnerX();
        int gridY = leftInnerY();
        int gridW = leftInnerW();
        int gridH = leftInnerH();

        int columns = ingredients.size() <= 2 ? 1 : (ingredients.size() <= 4 ? 2 : 3);
        int rows = (ingredients.size() + columns - 1) / columns;

        for (int i = 0; i < ingredients.size(); i++) {
            int row = i / columns;
            int col = i % columns;

            int x = spreadRegionX(gridX, gridW, columns, col);
            int y = spreadRegionY(gridY, gridH, rows, row);

            addItemIngredient(builder, RecipeIngredientRole.INPUT, x, y, ingredients.get(i));
        }

        addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), recipe.result());
    }

    @Override
    public void draw(StompCraftingRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        super.draw(recipe, slots, g, mouseX, mouseY);
        drawBottomInfo(g, jeiString("screen.mydrugs.jei.work_clamped", recipe.work(), recipe.clampedWork()));
    }
}

