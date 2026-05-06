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
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.recipes.psy_anvil.PsyAnvilRecipe;

final class PsyAnvilRecipeCategory extends AbstractNiceRecipeCategory<PsyAnvilRecipe> {
    static final RecipeType<PsyAnvilRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "psy_anvil"), PsyAnvilRecipe.class);

    private static final int GRID_X = 12;
    private static final int GRID_Y = 24;
    private static final int OUTPUT_X = 132;
    private static final int OUTPUT_Y = 42;

    PsyAnvilRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.psy_anvil"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "PSY_ANVIL_ITEM", "PSY_ANVIL"),
                176,
                110
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PsyAnvilRecipe recipe, IFocusGroup focuses) {
        for (int i = 0; i < Math.min(9, recipe.ingredients().size()); i++) {
            PsyAnvilRecipe.PsyAnvilIngredient ingredient = recipe.ingredients().get(i);
            int x = GRID_X + (i % 3) * SLOT;
            int y = GRID_Y + (i / 3) * SLOT;
            addItemIngredient(builder, RecipeIngredientRole.INPUT, x, y, ingredient.ingredient());
        }

        addItemStack(builder, RecipeIngredientRole.OUTPUT, OUTPUT_X, OUTPUT_Y, recipe.result());
    }

    @Override
    public void draw(PsyAnvilRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        super.draw(recipe, slots, g, mouseX, mouseY);

        for (int i = 0; i < Math.min(9, recipe.ingredients().size()); i++) {
            int count = recipe.ingredients().get(i).count();
            if (count > 1) {
                drawSlotCount(g, GRID_X + (i % 3) * SLOT, GRID_Y + (i / 3) * SLOT, count);
            }
        }

        recipe.requiredKnowledge().ifPresent(id -> drawRightInfo(
                g,
                jeiString("screen.mydrugs.jei.required_knowledge", knowledgeName(id)),
                70
        ));

        if (recipe.experienceCost() > 0) {
            drawRightInfo(g, jeiString("screen.mydrugs.jei.experience_cost", recipe.experienceCost()), 82);
        }

        recipe.messageKey().ifPresent(key -> drawBottomInfo(g, Component.translatable(key).getString()));
    }

    private static String knowledgeName(ResourceLocation id) {
        return Component.translatable("knowledge." + id.getNamespace() + "." + id.getPath()).getString();
    }
}
