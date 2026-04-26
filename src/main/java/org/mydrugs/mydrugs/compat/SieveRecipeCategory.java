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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.*;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.menu.*;
import org.mydrugs.mydrugs.menu.layout.*;
import org.mydrugs.mydrugs.recipes.advanced_furnace.AdvancedFurnaceRecipe;
import org.mydrugs.mydrugs.recipes.advanced_mixing_vat.AdvancedMixingVatRecipe;
import org.mydrugs.mydrugs.recipes.biochemical_reactor.BiochemicalReactorRecipe;
import org.mydrugs.mydrugs.recipes.catalytic_reformer.CatalyticReformerRecipe;
import org.mydrugs.mydrugs.recipes.chemical_reactor.ChemicalReactorRecipe;
import org.mydrugs.mydrugs.recipes.centrifuge.CentrifugeRecipe;
import org.mydrugs.mydrugs.recipes.electrolyzer.ElectrolyzerRecipe;
import org.mydrugs.mydrugs.recipes.distiller.DistillerRecipe;
import org.mydrugs.mydrugs.recipes.drying.DryingRecipe;
import org.mydrugs.mydrugs.recipes.evaporation_tray.EvaporationTrayRecipe;
import org.mydrugs.mydrugs.recipes.filterer.FluidFiltererRecipe;
import org.mydrugs.mydrugs.recipes.gasifier.GasifierRecipe;
import org.mydrugs.mydrugs.recipes.grinder.GrindingRecipe;
import org.mydrugs.mydrugs.recipes.growth_chamber.GrowthChamberRecipe;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatRecipe;
import org.mydrugs.mydrugs.recipes.sieving.SieveRecipe;
import org.mydrugs.mydrugs.recipes.stomp_crafting.StompCraftingRecipe;

import java.util.ArrayList;
import java.util.List;

final class SieveRecipeCategory extends AbstractNiceRecipeCategory<SieveRecipe> {
    private static final int KNOB_RADIUS = 5;

    static final RecipeType<SieveRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "sieving"), SieveRecipe.class);

    SieveRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.sieve"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "SIEVE", "SIEVING_TABLE"),
                SieveLayout.GUI_WIDTH,
                SieveLayout.MACHINE_PANEL_Y + SieveLayout.MACHINE_PANEL_H + 14
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
        drawWindow(g, width, height, 0xFF181818, 0xFF262626);

        drawPanel(
                g,
                SieveLayout.MACHINE_PANEL_X,
                SieveLayout.MACHINE_PANEL_Y,
                SieveLayout.MACHINE_PANEL_W,
                SieveLayout.MACHINE_PANEL_H,
                0xFF323232,
                0xFF595959,
                0xFF101010
        );

        drawSlotFrame(g, SieveLayout.INPUT_X, SieveLayout.INPUT_Y, 0xFF8A8A8A, 0xFF111111);
        drawSlotFrame(g, SieveLayout.RESULT_X, SieveLayout.RESULT_Y, 0xFF8A8A8A, 0xFF111111);
        drawSlotFrame(g, SieveLayout.BONUS_X, SieveLayout.BONUS_Y, 0xFF8A8A8A, 0xFF111111);

        drawShakeWidget(g);

        drawTitle(g);

        String footer = "Time: " + recipe.sieveTime() + "t";
        if (recipe.hasBonus()) {
            footer += "  |  Bonus: " + (int) (recipe.bonusChance() * 100.0F) + "%";
        }
        drawBottomInfo(g, footer);
    }

    @Override
    public List<Component> getTooltipStrings(SieveRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(SieveLayout.WIDGET_X, SieveLayout.WIDGET_Y, SieveLayout.WIDGET_W, SieveLayout.WIDGET_H, mouseX, mouseY)) {
            return tooltip("Shake handle", "Drag to sieve");
        }
        return List.of();
    }

    private void drawShakeWidget(GuiGraphics g) {
        int trackLeft = SieveLayout.WIDGET_X;
        int trackTop = SieveLayout.WIDGET_Y;

        g.fill(trackLeft - 2, trackTop - 2, trackLeft + SieveLayout.WIDGET_W + 2, trackTop + SieveLayout.WIDGET_H + 2, 0xFF5A5A5A);
        g.fill(trackLeft - 1, trackTop - 1, trackLeft + SieveLayout.WIDGET_W + 1, trackTop + SieveLayout.WIDGET_H + 1, 0xFF161616);

        int shaftX1 = trackLeft + 7;
        int shaftX2 = trackLeft + 11;
        g.fill(shaftX1, trackTop + 2, shaftX2, trackTop + SieveLayout.WIDGET_H - 2, 0xFF090909);

        g.fill(trackLeft + 5, trackTop + 1, trackLeft + 13, trackTop + 3, 0xFF727272);
        g.fill(trackLeft + 5, trackTop + SieveLayout.WIDGET_H - 3, trackLeft + 13, trackTop + SieveLayout.WIDGET_H - 1, 0xFF0E0E0E);

        int centerX = trackLeft + SieveLayout.WIDGET_W / 2;
        int centerY = trackTop + SieveLayout.WIDGET_H / 2;

        drawCircle(g, centerX, centerY, KNOB_RADIUS + 1, 0xFFBABABA);
        drawCircle(g, centerX, centerY, KNOB_RADIUS, 0xFF3B3B3B);
        drawCircle(g, centerX - 1, centerY - 1, 1, 0xFFE8E8E8);
    }
}

