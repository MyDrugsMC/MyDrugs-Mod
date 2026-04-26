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

final class CentrifugeRecipeCategory extends AbstractNiceRecipeCategory<CentrifugeRecipe> {
    static final RecipeType<CentrifugeRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "centrifuge"), CentrifugeRecipe.class);

    CentrifugeRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.centrifuge"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "CENTRIFUGE"),
                CentrifugeLayout.GUI_WIDTH,
                CentrifugeLayout.MACHINE_PANEL_Y + CentrifugeLayout.MACHINE_PANEL_H + 14
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CentrifugeRecipe recipe, IFocusGroup focuses) {
        addFluid(builder, RecipeIngredientRole.INPUT, CentrifugeLayout.INPUT_SLOT_X, CentrifugeLayout.INPUT_SLOT_Y, recipe.input().fluid(), recipe.input().amount());

        addFluid(builder, RecipeIngredientRole.OUTPUT, CentrifugeLayout.OUTPUT_A_SLOT_X, CentrifugeLayout.OUTPUT_A_SLOT_Y, recipe.output1().fluid(), recipe.output1().amount());
        recipe.output2().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, CentrifugeLayout.OUTPUT_B_SLOT_X, CentrifugeLayout.OUTPUT_B_SLOT_Y, output.fluid(), output.amount())
        );
    }

    @Override
    public void draw(CentrifugeRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        drawWindow(g, width, height);
        drawPanel(g, CentrifugeLayout.MACHINE_PANEL_X, CentrifugeLayout.MACHINE_PANEL_Y, CentrifugeLayout.MACHINE_PANEL_W, CentrifugeLayout.MACHINE_PANEL_H, 0xFF323232);
        drawPanel(g, CentrifugeLayout.CENTER_PANEL_X, CentrifugeLayout.CENTER_PANEL_Y, CentrifugeLayout.CENTER_PANEL_W, CentrifugeLayout.CENTER_PANEL_H, 0xFF262B32);

        drawTankFrame(g, CentrifugeLayout.INPUT_TANK_X, CentrifugeLayout.INPUT_TANK_Y, CentrifugeLayout.TANK_W, CentrifugeLayout.TANK_H, CentrifugeLayout.TANK_INNER_X_OFFSET, CentrifugeLayout.TANK_INNER_Y_OFFSET, CentrifugeLayout.TANK_INNER_W, CentrifugeLayout.TANK_INNER_H);
        drawTankFrame(g, CentrifugeLayout.OUTPUT_A_TANK_X, CentrifugeLayout.OUTPUT_A_TANK_Y, CentrifugeLayout.TANK_W, CentrifugeLayout.TANK_H, CentrifugeLayout.TANK_INNER_X_OFFSET, CentrifugeLayout.TANK_INNER_Y_OFFSET, CentrifugeLayout.TANK_INNER_W, CentrifugeLayout.TANK_INNER_H);
        drawTankFrame(g, CentrifugeLayout.OUTPUT_B_TANK_X, CentrifugeLayout.OUTPUT_B_TANK_Y, CentrifugeLayout.TANK_W, CentrifugeLayout.TANK_H, CentrifugeLayout.TANK_INNER_X_OFFSET, CentrifugeLayout.TANK_INNER_Y_OFFSET, CentrifugeLayout.TANK_INNER_W, CentrifugeLayout.TANK_INNER_H);

        drawFluidTankPreview(g, recipe.input().fluid(), recipe.input().amount(), CentrifugeLayout.INPUT_TANK_X, CentrifugeLayout.INPUT_TANK_Y, CentrifugeLayout.TANK_INNER_X_OFFSET, CentrifugeLayout.TANK_INNER_Y_OFFSET, CentrifugeLayout.TANK_INNER_W, CentrifugeLayout.TANK_INNER_H);
        drawFluidTankPreview(g, recipe.output1().fluid(), recipe.output1().amount(), CentrifugeLayout.OUTPUT_A_TANK_X, CentrifugeLayout.OUTPUT_A_TANK_Y, CentrifugeLayout.TANK_INNER_X_OFFSET, CentrifugeLayout.TANK_INNER_Y_OFFSET, CentrifugeLayout.TANK_INNER_W, CentrifugeLayout.TANK_INNER_H);
        recipe.output2().ifPresent(output ->
                drawFluidTankPreview(g, output.fluid(), output.amount(), CentrifugeLayout.OUTPUT_B_TANK_X, CentrifugeLayout.OUTPUT_B_TANK_Y, CentrifugeLayout.TANK_INNER_X_OFFSET, CentrifugeLayout.TANK_INNER_Y_OFFSET, CentrifugeLayout.TANK_INNER_W, CentrifugeLayout.TANK_INNER_H)
        );

        drawSlotFrame(g, CentrifugeLayout.INPUT_SLOT_X, CentrifugeLayout.INPUT_SLOT_Y);
        drawSlotFrame(g, CentrifugeLayout.OUTPUT_A_SLOT_X, CentrifugeLayout.OUTPUT_A_SLOT_Y);
        drawSlotFrame(g, CentrifugeLayout.OUTPUT_B_SLOT_X, CentrifugeLayout.OUTPUT_B_SLOT_Y);
        drawSlotFrame(g, CentrifugeLayout.FUEL_SLOT_X, CentrifugeLayout.FUEL_SLOT_Y);

        drawHorizontalBar(g, CentrifugeLayout.PROGRESS_X, CentrifugeLayout.PROGRESS_Y, CentrifugeLayout.PROGRESS_W, CentrifugeLayout.PROGRESS_H, CentrifugeLayout.PROGRESS_W, 0xFF768AB8, 0xFFAAB9DB);
        drawVerticalBar(g, CentrifugeLayout.FUEL_BAR_X, CentrifugeLayout.FUEL_BAR_Y, CentrifugeLayout.FUEL_BAR_W, CentrifugeLayout.FUEL_BAR_H, CentrifugeLayout.FUEL_BAR_INNER_X_OFFSET, CentrifugeLayout.FUEL_BAR_INNER_Y_OFFSET, CentrifugeLayout.FUEL_BAR_INNER_W, CentrifugeLayout.FUEL_BAR_INNER_H, CentrifugeLayout.FUEL_BAR_INNER_H, 0xFFE38D3F, 0xFFFFC270);

        drawDumpButton(g, CentrifugeLayout.DUMP_INPUT_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, false, true);
        drawDumpButton(g, CentrifugeLayout.DUMP_OUTPUT_A_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, false, true);
        drawDumpButton(g, CentrifugeLayout.DUMP_OUTPUT_B_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, false, recipe.output2().isPresent());

        drawTitle(g);
    }

    @Override
    public List<Component> getTooltipStrings(CentrifugeRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(CentrifugeLayout.DUMP_INPUT_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, CentrifugeLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump input tank");
        } else if (isHoveringBox(CentrifugeLayout.DUMP_OUTPUT_A_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, CentrifugeLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank A");
        } else if (isHoveringBox(CentrifugeLayout.DUMP_OUTPUT_B_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, CentrifugeLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank B");
        } else if (isHoveringBox(CentrifugeLayout.INPUT_TANK_X, CentrifugeLayout.INPUT_TANK_Y, CentrifugeLayout.TANK_W, CentrifugeLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Input tank", recipe.input().fluid(), recipe.input().amount(), CentrifugeBlockEntity.FLUID_CAPACITY);
        } else if (isHoveringBox(CentrifugeLayout.OUTPUT_A_TANK_X, CentrifugeLayout.OUTPUT_A_TANK_Y, CentrifugeLayout.TANK_W, CentrifugeLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Output tank A", recipe.output1().fluid(), recipe.output1().amount(), CentrifugeBlockEntity.FLUID_CAPACITY);
        } else if (isHoveringBox(CentrifugeLayout.OUTPUT_B_TANK_X, CentrifugeLayout.OUTPUT_B_TANK_Y, CentrifugeLayout.TANK_W, CentrifugeLayout.TANK_H, mouseX, mouseY)) {
            return recipe.output2()
                    .map(output -> fluidTankTooltip("Output tank B", output.fluid(), output.amount(), CentrifugeBlockEntity.FLUID_CAPACITY))
                    .orElseGet(() -> fluidTankTooltip("Output tank B", Fluids.EMPTY, 0, CentrifugeBlockEntity.FLUID_CAPACITY));
        } else if (isHoveringBox(CentrifugeLayout.PROGRESS_X, CentrifugeLayout.PROGRESS_Y, CentrifugeLayout.PROGRESS_W, CentrifugeLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Centrifuge progress", 0, recipe.baseTicks());
        } else if (isHoveringBox(CentrifugeLayout.FUEL_BAR_X, CentrifugeLayout.FUEL_BAR_Y, CentrifugeLayout.FUEL_BAR_W, CentrifugeLayout.FUEL_BAR_H, mouseX, mouseY)) {
            return amountTooltip("Fuel burn time", 0, recipe.baseTicks(), "ticks");
        }
        return List.of();
    }
}

