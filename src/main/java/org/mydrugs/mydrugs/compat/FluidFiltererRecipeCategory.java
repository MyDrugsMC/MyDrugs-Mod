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

final class FluidFiltererRecipeCategory extends AbstractNiceRecipeCategory<FluidFiltererRecipe> {
    static final RecipeType<FluidFiltererRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "fluid_filterer"), FluidFiltererRecipe.class);

    FluidFiltererRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.fluid_filterer"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "FLUID_FILTERER"),
                FluidFiltererLayout.GUI_WIDTH,
                FluidFiltererLayout.MACHINE_PANEL_Y + FluidFiltererLayout.MACHINE_PANEL_H + 14
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FluidFiltererRecipe recipe, IFocusGroup focuses) {
        addFluid(
                builder,
                RecipeIngredientRole.INPUT,
                FluidFiltererLayout.INPUT_SLOT_X,
                FluidFiltererLayout.INPUT_SLOT_Y,
                JeiCompatUtil.idOf(recipe.input(), "fluid", "fluidId"),
                JeiCompatUtil.intOf(recipe.input(), "amount")
        );

        addFluid(
                builder,
                RecipeIngredientRole.OUTPUT,
                FluidFiltererLayout.OUTPUT_A_SLOT_X,
                FluidFiltererLayout.OUTPUT_A_SLOT_Y,
                JeiCompatUtil.idOf(recipe.output1(), "fluid", "fluidId"),
                JeiCompatUtil.intOf(recipe.output1(), "amount")
        );

        recipe.output2().ifPresent(output ->
                addFluid(
                        builder,
                        RecipeIngredientRole.OUTPUT,
                        FluidFiltererLayout.OUTPUT_B_SLOT_X,
                        FluidFiltererLayout.OUTPUT_B_SLOT_Y,
                        JeiCompatUtil.idOf(output, "fluid", "fluidId"),
                        JeiCompatUtil.intOf(output, "amount")
                )
        );

        recipe.outputItem().ifPresent(outputItem ->
                addItemStack(builder, RecipeIngredientRole.OUTPUT, FluidFiltererLayout.RESIDUE_SLOT_X, FluidFiltererLayout.RESIDUE_SLOT_Y, JeiCompatUtil.stackOf(outputItem))
        );
    }

    @Override
    public void draw(FluidFiltererRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        drawWindow(g, width, height);
        drawPanel(g, FluidFiltererLayout.MACHINE_PANEL_X, FluidFiltererLayout.MACHINE_PANEL_Y, FluidFiltererLayout.MACHINE_PANEL_W, FluidFiltererLayout.MACHINE_PANEL_H, 0xFF323232);

        drawTankFrame(g, FluidFiltererLayout.INPUT_TANK_X, FluidFiltererLayout.INPUT_TANK_Y, FluidFiltererLayout.TANK_W, FluidFiltererLayout.TANK_H, FluidFiltererLayout.TANK_INNER_X_OFFSET, FluidFiltererLayout.TANK_INNER_Y_OFFSET, FluidFiltererLayout.TANK_INNER_W, FluidFiltererLayout.TANK_INNER_H);
        drawTankFrame(g, FluidFiltererLayout.OUTPUT_A_TANK_X, FluidFiltererLayout.OUTPUT_A_TANK_Y, FluidFiltererLayout.TANK_W, FluidFiltererLayout.TANK_H, FluidFiltererLayout.TANK_INNER_X_OFFSET, FluidFiltererLayout.TANK_INNER_Y_OFFSET, FluidFiltererLayout.TANK_INNER_W, FluidFiltererLayout.TANK_INNER_H);

        drawFluidTankPreview(g, JeiCompatUtil.fluid(JeiCompatUtil.idOf(recipe.input(), "fluid", "fluidId")), JeiCompatUtil.intOf(recipe.input(), "amount"), FluidFiltererLayout.INPUT_TANK_X, FluidFiltererLayout.INPUT_TANK_Y, FluidFiltererLayout.TANK_INNER_X_OFFSET, FluidFiltererLayout.TANK_INNER_Y_OFFSET, FluidFiltererLayout.TANK_INNER_W, FluidFiltererLayout.TANK_INNER_H);
        drawFluidTankPreview(g, JeiCompatUtil.fluid(JeiCompatUtil.idOf(recipe.output1(), "fluid", "fluidId")), JeiCompatUtil.intOf(recipe.output1(), "amount"), FluidFiltererLayout.OUTPUT_A_TANK_X, FluidFiltererLayout.OUTPUT_A_TANK_Y, FluidFiltererLayout.TANK_INNER_X_OFFSET, FluidFiltererLayout.TANK_INNER_Y_OFFSET, FluidFiltererLayout.TANK_INNER_W, FluidFiltererLayout.TANK_INNER_H);

        drawSlotFrame(g, FluidFiltererLayout.INPUT_SLOT_X, FluidFiltererLayout.INPUT_SLOT_Y);
        drawSlotFrame(g, FluidFiltererLayout.OUTPUT_A_SLOT_X, FluidFiltererLayout.OUTPUT_A_SLOT_Y);
        drawSlotFrame(g, FluidFiltererLayout.FILTER_SLOT_X, FluidFiltererLayout.FILTER_SLOT_Y);
        drawSlotFrame(g, FluidFiltererLayout.RESIDUE_SLOT_X, FluidFiltererLayout.RESIDUE_SLOT_Y);

        drawHorizontalBar(g, FluidFiltererLayout.PROGRESS_X, FluidFiltererLayout.PROGRESS_Y, FluidFiltererLayout.PROGRESS_W, FluidFiltererLayout.PROGRESS_H, FluidFiltererLayout.PROGRESS_W, 0xFF768AB8, 0xFFAAB9DB);
        drawDumpButton(g, FluidFiltererLayout.DUMP_INPUT_X, FluidFiltererLayout.DUMP_BUTTON_Y, FluidFiltererLayout.DUMP_BUTTON_SIZE, false, true);
        drawDumpButton(g, FluidFiltererLayout.DUMP_OUTPUT_A_X, FluidFiltererLayout.DUMP_BUTTON_Y, FluidFiltererLayout.DUMP_BUTTON_SIZE, false, true);
        drawHoldButton(g, FluidFiltererLayout.RUN_BUTTON_X, FluidFiltererLayout.RUN_BUTTON_Y, FluidFiltererLayout.RUN_BUTTON_W, FluidFiltererLayout.RUN_BUTTON_H, false, false, "HOLD", "FILTERING...");

        drawTitle(g);
        drawBottomInfo(g, "Clicks: " + recipe.clicksRequired() + "  |  Hunger/tick: " + recipe.hungerPerTick());
    }
    @Override
    public List<Component> getTooltipStrings(FluidFiltererRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(FluidFiltererLayout.DUMP_INPUT_X, FluidFiltererLayout.DUMP_BUTTON_Y, FluidFiltererLayout.DUMP_BUTTON_SIZE, FluidFiltererLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump input tank");
        } else if (isHoveringBox(FluidFiltererLayout.DUMP_OUTPUT_A_X, FluidFiltererLayout.DUMP_BUTTON_Y, FluidFiltererLayout.DUMP_BUTTON_SIZE, FluidFiltererLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank");
        } else if (isHoveringBox(FluidFiltererLayout.INPUT_TANK_X, FluidFiltererLayout.INPUT_TANK_Y, FluidFiltererLayout.TANK_W, FluidFiltererLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip(
                    "Input tank",
                    JeiCompatUtil.idOf(recipe.input(), "fluid", "fluidId"),
                    JeiCompatUtil.intOf(recipe.input(), "amount"),
                    FluidFiltererMenu.TANK_CAPACITY
            );
        } else if (isHoveringBox(FluidFiltererLayout.OUTPUT_A_TANK_X, FluidFiltererLayout.OUTPUT_A_TANK_Y, FluidFiltererLayout.TANK_W, FluidFiltererLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip(
                    "Output tank",
                    JeiCompatUtil.idOf(recipe.output1(), "fluid", "fluidId"),
                    JeiCompatUtil.intOf(recipe.output1(), "amount"),
                    FluidFiltererMenu.TANK_CAPACITY
            );
        } else if (isHoveringBox(FluidFiltererLayout.INPUT_SLOT_X, FluidFiltererLayout.INPUT_SLOT_Y, 18, 18, mouseX, mouseY)) {
            return tooltip("Input fluid container");
        } else if (isHoveringBox(FluidFiltererLayout.OUTPUT_A_SLOT_X, FluidFiltererLayout.OUTPUT_A_SLOT_Y, 18, 18, mouseX, mouseY)) {
            return tooltip("Output container");
        } else if (isHoveringBox(FluidFiltererLayout.PROGRESS_X, FluidFiltererLayout.PROGRESS_Y, FluidFiltererLayout.PROGRESS_W, FluidFiltererLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Filtering progress", 0, recipe.clicksRequired());
        } else if (isHoveringBox(FluidFiltererLayout.RUN_BUTTON_X, FluidFiltererLayout.RUN_BUTTON_Y, FluidFiltererLayout.RUN_BUTTON_W, FluidFiltererLayout.RUN_BUTTON_H, mouseX, mouseY)) {
            return tooltip("Hold to filter");
        } else if (isHoveringBox(FluidFiltererLayout.FILTER_SLOT_X, FluidFiltererLayout.FILTER_SLOT_Y, 18, 18, mouseX, mouseY)) {
            return tooltip("Filter slot");
        } else if (isHoveringBox(FluidFiltererLayout.RESIDUE_SLOT_X, FluidFiltererLayout.RESIDUE_SLOT_Y, 18, 18, mouseX, mouseY)) {
            return tooltip("Waste output");
        }
        return List.of();
    }

}

