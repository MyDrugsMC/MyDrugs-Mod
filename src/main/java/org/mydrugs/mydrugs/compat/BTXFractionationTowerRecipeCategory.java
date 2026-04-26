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

final class BTXFractionationTowerRecipeCategory extends AbstractNiceRecipeCategory<BTXFractionationTowerJeiRecipe> {
    static final RecipeType<BTXFractionationTowerJeiRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "btx_fractionation_tower"), BTXFractionationTowerJeiRecipe.class);

    static final List<BTXFractionationTowerJeiRecipe> RECIPES = List.of(BTXFractionationTowerJeiRecipe.DEFAULT);

    BTXFractionationTowerRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.btx_fractionation_tower"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "BTX_FRACTIONATION_TOWER_ITEM", "BTX_FRACTIONATION_TOWER"),
                BTXFractionationTowerLayout.GUI_WIDTH,
                BTXFractionationTowerLayout.MACHINE_PANEL_Y + BTXFractionationTowerLayout.MACHINE_PANEL_H + 14
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BTXFractionationTowerJeiRecipe recipe, IFocusGroup focuses) {
        addFluid(builder, RecipeIngredientRole.INPUT, BTXFractionationTowerLayout.INPUT_SLOT_X, BTXFractionationTowerLayout.INPUT_SLOT_Y, recipe.inputFluid(), recipe.inputAmount());
        addItemIngredient(builder, RecipeIngredientRole.INPUT, BTXFractionationTowerLayout.FUEL_SLOT_X, BTXFractionationTowerLayout.FUEL_SLOT_Y, recipe.fuelPreview());

        addFluid(builder, RecipeIngredientRole.OUTPUT, BTXFractionationTowerLayout.BENZENE_SLOT_X, BTXFractionationTowerLayout.BENZENE_SLOT_Y, recipe.benzeneFluid(), recipe.benzeneAmount());
        addFluid(builder, RecipeIngredientRole.OUTPUT, BTXFractionationTowerLayout.TOLUENE_SLOT_X, BTXFractionationTowerLayout.TOLUENE_SLOT_Y, recipe.tolueneFluid(), recipe.tolueneAmount());
        addFluid(builder, RecipeIngredientRole.OUTPUT, BTXFractionationTowerLayout.XYLENE_SLOT_X, BTXFractionationTowerLayout.XYLENE_SLOT_Y, recipe.xyleneFluid(), recipe.xyleneAmount());
    }

    @Override
    public void draw(BTXFractionationTowerJeiRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        drawWindow(g, width, height);

        drawPanel(
                g,
                BTXFractionationTowerLayout.MACHINE_PANEL_X,
                BTXFractionationTowerLayout.MACHINE_PANEL_Y,
                BTXFractionationTowerLayout.MACHINE_PANEL_W,
                BTXFractionationTowerLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        drawPanel(
                g,
                BTXFractionationTowerLayout.CENTER_PANEL_X,
                BTXFractionationTowerLayout.CENTER_PANEL_Y,
                BTXFractionationTowerLayout.CENTER_PANEL_W,
                BTXFractionationTowerLayout.CENTER_PANEL_H,
                0xFF262B32
        );

        drawBtxTank(g, BTXFractionationTowerLayout.INPUT_TANK_X, BTXFractionationTowerLayout.INPUT_TANK_Y);
        drawBtxTank(g, BTXFractionationTowerLayout.BENZENE_TANK_X, BTXFractionationTowerLayout.BENZENE_TANK_Y);
        drawBtxTank(g, BTXFractionationTowerLayout.TOLUENE_TANK_X, BTXFractionationTowerLayout.TOLUENE_TANK_Y);
        drawBtxTank(g, BTXFractionationTowerLayout.XYLENE_TANK_X, BTXFractionationTowerLayout.XYLENE_TANK_Y);

        drawFluidTankPreview(g, JeiCompatUtil.fluid(recipe.inputFluid()), recipe.inputAmount(), BTXFractionationTowerLayout.INPUT_TANK_X, BTXFractionationTowerLayout.INPUT_TANK_Y, BTXFractionationTowerLayout.TANK_INNER_X_OFFSET, BTXFractionationTowerLayout.TANK_INNER_Y_OFFSET, BTXFractionationTowerLayout.TANK_INNER_W, BTXFractionationTowerLayout.TANK_INNER_H);
        drawFluidTankPreview(g, JeiCompatUtil.fluid(recipe.benzeneFluid()), recipe.benzeneAmount(), BTXFractionationTowerLayout.BENZENE_TANK_X, BTXFractionationTowerLayout.BENZENE_TANK_Y, BTXFractionationTowerLayout.TANK_INNER_X_OFFSET, BTXFractionationTowerLayout.TANK_INNER_Y_OFFSET, BTXFractionationTowerLayout.TANK_INNER_W, BTXFractionationTowerLayout.TANK_INNER_H);
        drawFluidTankPreview(g, JeiCompatUtil.fluid(recipe.tolueneFluid()), recipe.tolueneAmount(), BTXFractionationTowerLayout.TOLUENE_TANK_X, BTXFractionationTowerLayout.TOLUENE_TANK_Y, BTXFractionationTowerLayout.TANK_INNER_X_OFFSET, BTXFractionationTowerLayout.TANK_INNER_Y_OFFSET, BTXFractionationTowerLayout.TANK_INNER_W, BTXFractionationTowerLayout.TANK_INNER_H);
        drawFluidTankPreview(g, JeiCompatUtil.fluid(recipe.xyleneFluid()), recipe.xyleneAmount(), BTXFractionationTowerLayout.XYLENE_TANK_X, BTXFractionationTowerLayout.XYLENE_TANK_Y, BTXFractionationTowerLayout.TANK_INNER_X_OFFSET, BTXFractionationTowerLayout.TANK_INNER_Y_OFFSET, BTXFractionationTowerLayout.TANK_INNER_W, BTXFractionationTowerLayout.TANK_INNER_H);

        drawSlotFrame(g, BTXFractionationTowerLayout.INPUT_SLOT_X, BTXFractionationTowerLayout.INPUT_SLOT_Y);
        drawSlotFrame(g, BTXFractionationTowerLayout.BENZENE_SLOT_X, BTXFractionationTowerLayout.BENZENE_SLOT_Y);
        drawSlotFrame(g, BTXFractionationTowerLayout.TOLUENE_SLOT_X, BTXFractionationTowerLayout.TOLUENE_SLOT_Y);
        drawSlotFrame(g, BTXFractionationTowerLayout.XYLENE_SLOT_X, BTXFractionationTowerLayout.XYLENE_SLOT_Y);
        drawSlotFrame(g, BTXFractionationTowerLayout.FUEL_SLOT_X, BTXFractionationTowerLayout.FUEL_SLOT_Y);

        drawHorizontalBar(
                g,
                BTXFractionationTowerLayout.PROGRESS_X,
                BTXFractionationTowerLayout.PROGRESS_Y,
                BTXFractionationTowerLayout.PROGRESS_W,
                BTXFractionationTowerLayout.PROGRESS_H,
                BTXFractionationTowerLayout.PROGRESS_W,
                0xFFB8865F,
                0xFFFFD0A6
        );

        drawVerticalBar(
                g,
                BTXFractionationTowerLayout.FUEL_BAR_X,
                BTXFractionationTowerLayout.FUEL_BAR_Y,
                BTXFractionationTowerLayout.FUEL_BAR_W,
                BTXFractionationTowerLayout.FUEL_BAR_H,
                BTXFractionationTowerLayout.FUEL_BAR_INNER_X_OFFSET,
                BTXFractionationTowerLayout.FUEL_BAR_INNER_Y_OFFSET,
                BTXFractionationTowerLayout.FUEL_BAR_INNER_W,
                BTXFractionationTowerLayout.FUEL_BAR_INNER_H,
                BTXFractionationTowerLayout.FUEL_BAR_INNER_H,
                0xFFE38D3F,
                0xFFFFC270
        );

        drawDumpButton(g, BTXFractionationTowerLayout.DUMP_INPUT_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, false, recipe.inputAmount() > 0);
        drawDumpButton(g, BTXFractionationTowerLayout.DUMP_BENZENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, false, recipe.benzeneAmount() > 0);
        drawDumpButton(g, BTXFractionationTowerLayout.DUMP_TOLUENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, false, recipe.tolueneAmount() > 0);
        drawDumpButton(g, BTXFractionationTowerLayout.DUMP_XYLENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, false, recipe.xyleneAmount() > 0);

        drawTitle(g);
        drawBottomInfo(g, "Uses any Minecraft fuel  |  Time: " + recipe.processingTime() + "t");
    }

    @Override
    public List<Component> getTooltipStrings(BTXFractionationTowerJeiRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(BTXFractionationTowerLayout.DUMP_INPUT_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump input tank");
        } else if (isHoveringBox(BTXFractionationTowerLayout.DUMP_BENZENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump benzene tank");
        } else if (isHoveringBox(BTXFractionationTowerLayout.DUMP_TOLUENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump toluene tank");
        } else if (isHoveringBox(BTXFractionationTowerLayout.DUMP_XYLENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump xylene tank");
        } else if (isHoveringBox(BTXFractionationTowerLayout.INPUT_TANK_X, BTXFractionationTowerLayout.INPUT_TANK_Y, BTXFractionationTowerLayout.TANK_W, BTXFractionationTowerLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("BTX Mix input tank", recipe.inputFluid(), recipe.inputAmount(), BTXFractionationTowerMenu.TANK_CAPACITY);
        } else if (isHoveringBox(BTXFractionationTowerLayout.BENZENE_TANK_X, BTXFractionationTowerLayout.BENZENE_TANK_Y, BTXFractionationTowerLayout.TANK_W, BTXFractionationTowerLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Benzene output tank", recipe.benzeneFluid(), recipe.benzeneAmount(), BTXFractionationTowerMenu.TANK_CAPACITY);
        } else if (isHoveringBox(BTXFractionationTowerLayout.TOLUENE_TANK_X, BTXFractionationTowerLayout.TOLUENE_TANK_Y, BTXFractionationTowerLayout.TANK_W, BTXFractionationTowerLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Toluene output tank", recipe.tolueneFluid(), recipe.tolueneAmount(), BTXFractionationTowerMenu.TANK_CAPACITY);
        } else if (isHoveringBox(BTXFractionationTowerLayout.XYLENE_TANK_X, BTXFractionationTowerLayout.XYLENE_TANK_Y, BTXFractionationTowerLayout.TANK_W, BTXFractionationTowerLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Xylene output tank", recipe.xyleneFluid(), recipe.xyleneAmount(), BTXFractionationTowerMenu.TANK_CAPACITY);
        } else if (isHoveringBox(BTXFractionationTowerLayout.PROGRESS_X, BTXFractionationTowerLayout.PROGRESS_Y, BTXFractionationTowerLayout.PROGRESS_W, BTXFractionationTowerLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Fractionation progress", 0, recipe.processingTime());
        } else if (isHoveringBox(BTXFractionationTowerLayout.FUEL_BAR_X, BTXFractionationTowerLayout.FUEL_BAR_Y, BTXFractionationTowerLayout.FUEL_BAR_W, BTXFractionationTowerLayout.FUEL_BAR_H, mouseX, mouseY)) {
            return amountTooltip("Fuel burn time", 0, recipe.processingTime(), "ticks");
        }
        return List.of();
    }

    private void drawBtxTank(GuiGraphics g, int x, int y) {
        drawTankFrame(
                g,
                x,
                y,
                BTXFractionationTowerLayout.TANK_W,
                BTXFractionationTowerLayout.TANK_H,
                BTXFractionationTowerLayout.TANK_INNER_X_OFFSET,
                BTXFractionationTowerLayout.TANK_INNER_Y_OFFSET,
                BTXFractionationTowerLayout.TANK_INNER_W,
                BTXFractionationTowerLayout.TANK_INNER_H
        );
    }
}

