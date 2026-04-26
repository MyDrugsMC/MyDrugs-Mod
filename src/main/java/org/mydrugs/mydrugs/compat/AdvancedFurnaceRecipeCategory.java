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

final class AdvancedFurnaceRecipeCategory extends AbstractNiceRecipeCategory<AdvancedFurnaceRecipe> {
    static final RecipeType<AdvancedFurnaceRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "advanced_furnace"), AdvancedFurnaceRecipe.class);

    AdvancedFurnaceRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.advanced_furnace"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "ADVANCED_FURNACE_ITEM", "ADVANCED_FURNACE"),
                AdvancedFurnaceLayout.GUI_WIDTH,
                AdvancedFurnaceLayout.MACHINE_PANEL_Y + AdvancedFurnaceLayout.MACHINE_PANEL_H + 14
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AdvancedFurnaceRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, AdvancedFurnaceLayout.INPUT_A_X, AdvancedFurnaceLayout.INPUT_A_Y, recipe.inputA());
        recipe.inputB().ifPresent(input ->
                addItemIngredient(builder, RecipeIngredientRole.INPUT, AdvancedFurnaceLayout.INPUT_B_X, AdvancedFurnaceLayout.INPUT_B_Y, input)
        );

        addItemStack(builder, RecipeIngredientRole.OUTPUT, AdvancedFurnaceLayout.OUTPUT_A_X, AdvancedFurnaceLayout.OUTPUT_A_Y, recipe.resultA());
        addItemStack(builder, RecipeIngredientRole.OUTPUT, AdvancedFurnaceLayout.OUTPUT_B_X, AdvancedFurnaceLayout.OUTPUT_B_Y, recipe.resultB());

        recipe.fluidOutput().ifPresent(fluid ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, AdvancedFurnaceLayout.OUTPUT_CONTAINER_X, AdvancedFurnaceLayout.OUTPUT_CONTAINER_Y, fluid, recipe.fluidAmount())
        );
    }

    @Override
    public void draw(AdvancedFurnaceRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        drawWindow(g, width, height, 0xFF15171B, 0xFF23262B);

        drawPanel(
                g,
                AdvancedFurnaceLayout.MACHINE_PANEL_X,
                AdvancedFurnaceLayout.MACHINE_PANEL_Y,
                AdvancedFurnaceLayout.MACHINE_PANEL_W,
                AdvancedFurnaceLayout.MACHINE_PANEL_H,
                0xFF2F343C,
                0xFF646B77,
                0xFF0E1116
        );

        drawPanel(
                g,
                AdvancedFurnaceLayout.CENTER_PANEL_X,
                AdvancedFurnaceLayout.CENTER_PANEL_Y,
                AdvancedFurnaceLayout.CENTER_PANEL_W,
                AdvancedFurnaceLayout.CENTER_PANEL_H,
                0xFF1B1F25,
                0xFF505862,
                0xFF0A0C10
        );

        drawSlotFrame(g, AdvancedFurnaceLayout.INPUT_A_X, AdvancedFurnaceLayout.INPUT_A_Y);
        drawSlotFrame(g, AdvancedFurnaceLayout.INPUT_B_X, AdvancedFurnaceLayout.INPUT_B_Y);
        drawSlotFrame(g, AdvancedFurnaceLayout.FUEL_X, AdvancedFurnaceLayout.FUEL_Y);
        drawSlotFrame(g, AdvancedFurnaceLayout.OUTPUT_A_X, AdvancedFurnaceLayout.OUTPUT_A_Y);
        drawSlotFrame(g, AdvancedFurnaceLayout.OUTPUT_B_X, AdvancedFurnaceLayout.OUTPUT_B_Y);
        drawSlotFrame(g, AdvancedFurnaceLayout.OUTPUT_CONTAINER_X, AdvancedFurnaceLayout.OUTPUT_CONTAINER_Y);

        drawHorizontalBar(g, AdvancedFurnaceLayout.PROGRESS_X, AdvancedFurnaceLayout.PROGRESS_Y, AdvancedFurnaceLayout.PROGRESS_W, AdvancedFurnaceLayout.PROGRESS_H, AdvancedFurnaceLayout.PROGRESS_W, 0xFF62C8FF, 0xFFB9EEFF);
        drawHorizontalBar(g, AdvancedFurnaceLayout.BURN_X, AdvancedFurnaceLayout.BURN_Y, AdvancedFurnaceLayout.BURN_W, AdvancedFurnaceLayout.BURN_H, AdvancedFurnaceLayout.BURN_W, 0xFFFF9B47, 0xFFFFC87A);

        drawTankFrame(g, AdvancedFurnaceLayout.TANK_X, AdvancedFurnaceLayout.TANK_Y, StandardTankLayout.TANK_W, StandardTankLayout.TANK_H, StandardTankLayout.INNER_X, StandardTankLayout.INNER_Y, StandardTankLayout.INNER_W, StandardTankLayout.INNER_H);
        recipe.fluidOutput().ifPresent(fluid ->
                drawFluidTankPreview(g, fluid, recipe.fluidAmount(), AdvancedFurnaceLayout.TANK_X, AdvancedFurnaceLayout.TANK_Y, StandardTankLayout.INNER_X, StandardTankLayout.INNER_Y, StandardTankLayout.INNER_W, StandardTankLayout.INNER_H)
        );

        drawTitle(g);
        drawCentered(g, "Heat", AdvancedFurnaceLayout.HEAT_LABEL_X, AdvancedFurnaceLayout.HEAT_LABEL_Y, 40, 0xFFE0B58A);

        String footer = "Time: " + recipe.cookTime() + "t";
        if (recipe.fluidOutput().isPresent()) {
            footer += "  |  " + recipe.fluidAmount() + " mB " + JeiCompatUtil.shortId(recipe.fluidOutput().get());
        }
        drawBottomInfo(g, footer);
    }

    @Override
    public List<Component> getTooltipStrings(AdvancedFurnaceRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(AdvancedFurnaceLayout.TANK_X, AdvancedFurnaceLayout.TANK_Y, StandardTankLayout.TANK_W, StandardTankLayout.TANK_H, mouseX, mouseY)) {
            return recipe.fluidOutput()
                    .map(fluid -> fluidTankTooltip("Output Tank", fluid, recipe.fluidAmount(), AdvancedFurnaceBlockEntity.TANK_CAPACITY))
                    .orElseGet(() -> fluidTankTooltip("Output Tank", Fluids.EMPTY, 0, AdvancedFurnaceBlockEntity.TANK_CAPACITY));
        }
        if (isHoveringBox(AdvancedFurnaceLayout.PROGRESS_X, AdvancedFurnaceLayout.PROGRESS_Y, AdvancedFurnaceLayout.PROGRESS_W, AdvancedFurnaceLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Progress", 0, recipe.cookTime(), "ticks");
        }
        if (isHoveringBox(AdvancedFurnaceLayout.BURN_X, AdvancedFurnaceLayout.BURN_Y, AdvancedFurnaceLayout.BURN_W, AdvancedFurnaceLayout.BURN_H, mouseX, mouseY)) {
            return amountTooltip("Burn", 0, recipe.cookTime(), "ticks");
        }
        return List.of();
    }
}

