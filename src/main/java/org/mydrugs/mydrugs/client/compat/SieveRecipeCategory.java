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
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.SieveLayout;
import org.mydrugs.mydrugs.recipes.sieving.SieveRecipe;

import java.util.List;

final class SieveRecipeCategory extends AbstractNiceRecipeCategory<SieveRecipe> {
    static final RecipeType<SieveRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "sieving"), SieveRecipe.class);

    SieveRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.sieve"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "SIEVE", "SIEVING_TABLE"),
                SieveLayout.GUI_WIDTH,
                MachineGuiRenderer.sieveHeight(false)
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SieveRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, SieveLayout.INPUT_X, SieveLayout.INPUT_Y, recipe.input());
        addItemStack(builder, RecipeIngredientRole.OUTPUT, SieveLayout.RESULT_X, SieveLayout.RESULT_Y, recipe.result());
        recipe.bonusResult().ifPresent(stack ->
                addItemStack(builder, RecipeIngredientRole.OUTPUT, SieveLayout.BONUS_X, SieveLayout.BONUS_Y, stack)
        );
    }

    @Override
    public void draw(SieveRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        String footer = jeiString("screen.mydrugs.jei.time_ticks", recipe.sieveTime());
        if (recipe.hasBonus()) {
            footer += jeiString("screen.mydrugs.jei.bonus_suffix", (int) (recipe.bonusChance() * 100.0F));
        }
        MachineGuiRenderer.drawSieve(this, g, new MachineGuiRenderer.SieveState(SieveLayout.WIDGET_Y + SieveLayout.WIDGET_H / 2), false);
        MachineGuiRenderer.drawSieveLabels(this, g, net.minecraft.client.Minecraft.getInstance().font, getTitle(), null, 0, footer);
    }

    @Override
    public List<Component> getTooltipStrings(SieveRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(SieveLayout.WIDGET_X, SieveLayout.WIDGET_Y, SieveLayout.WIDGET_W, SieveLayout.WIDGET_H, mouseX, mouseY)) {
            return tooltip("Shake handle", "Drag to sieve");
        }
        return List.of();
    }

}

