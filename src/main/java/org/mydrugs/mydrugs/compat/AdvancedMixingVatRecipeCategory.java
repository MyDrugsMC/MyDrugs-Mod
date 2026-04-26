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

final class AdvancedMixingVatRecipeCategory extends AbstractNiceRecipeCategory<AdvancedMixingVatRecipe> {
    static final RecipeType<AdvancedMixingVatRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "advanced_mixing_vat"), AdvancedMixingVatRecipe.class);

    AdvancedMixingVatRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.advanced_mixing_vat"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "ADVANCED_MIXING_VAT"),
                AdvancedMixingVatLayout.GUI_WIDTH,
                AdvancedMixingVatLayout.MACHINE_PANEL_Y + AdvancedMixingVatLayout.MACHINE_PANEL_H + 14
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AdvancedMixingVatRecipe recipe, IFocusGroup focuses) {
        List<?> itemInputs = recipe.itemInputs();
        int[] itemX = {
                AdvancedMixingVatLayout.ITEM_0_X,
                AdvancedMixingVatLayout.ITEM_1_X,
                AdvancedMixingVatLayout.ITEM_2_X,
                AdvancedMixingVatLayout.ITEM_3_X
        };
        int[] itemY = {
                AdvancedMixingVatLayout.ITEM_0_Y,
                AdvancedMixingVatLayout.ITEM_1_Y,
                AdvancedMixingVatLayout.ITEM_2_Y,
                AdvancedMixingVatLayout.ITEM_3_Y
        };

        for (int i = 0; i < Math.min(itemInputs.size(), itemX.length); i++) {
            addItemIngredient(builder, RecipeIngredientRole.INPUT, itemX[i], itemY[i], JeiCompatUtil.ingredientOf(itemInputs.get(i)));
        }

        List<?> fluidInputs = recipe.fluidInputs();
        int[] tankSlotX = {
                AdvancedMixingVatLayout.TANK_A_SLOT_X,
                AdvancedMixingVatLayout.TANK_B_SLOT_X,
                AdvancedMixingVatLayout.TANK_C_SLOT_X
        };
        for (int i = 0; i < Math.min(fluidInputs.size(), tankSlotX.length); i++) {
            Object fluid = fluidInputs.get(i);
            addFluid(
                    builder,
                    RecipeIngredientRole.INPUT,
                    tankSlotX[i],
                    AdvancedMixingVatLayout.TANK_SLOT_Y,
                    JeiCompatUtil.idOf(fluid, "fluid", "fluidId"),
                    JeiCompatUtil.intOf(fluid, "amount")
            );
        }

        if (recipe.gasInput() != null) {
            addGas(
                    builder,
                    RecipeIngredientRole.INPUT,
                    AdvancedMixingVatLayout.GAS_SLOT_X,
                    AdvancedMixingVatLayout.TANK_SLOT_Y,
                    JeiCompatUtil.idOf(recipe.gasInput(), "gas", "gasId"),
                    recipe.gasInput().amount()
            );
        }

        addFluid(
                builder,
                RecipeIngredientRole.OUTPUT,
                AdvancedMixingVatLayout.OUTPUT_SLOT_X,
                AdvancedMixingVatLayout.TANK_SLOT_Y,
                JeiCompatUtil.idOf(recipe.output(), "fluid", "fluidId"),
                JeiCompatUtil.intOf(recipe.output(), "amount")
        );
    }

    @Override
    public void draw(AdvancedMixingVatRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        drawWindow(g, width, height);
        drawPanel(
                g,
                AdvancedMixingVatLayout.MACHINE_PANEL_X,
                AdvancedMixingVatLayout.MACHINE_PANEL_Y,
                AdvancedMixingVatLayout.MACHINE_PANEL_W,
                AdvancedMixingVatLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        int[] itemX = {
                AdvancedMixingVatLayout.ITEM_0_X,
                AdvancedMixingVatLayout.ITEM_1_X,
                AdvancedMixingVatLayout.ITEM_2_X,
                AdvancedMixingVatLayout.ITEM_3_X
        };
        int[] itemY = {
                AdvancedMixingVatLayout.ITEM_0_Y,
                AdvancedMixingVatLayout.ITEM_1_Y,
                AdvancedMixingVatLayout.ITEM_2_Y,
                AdvancedMixingVatLayout.ITEM_3_Y
        };
        for (int i = 0; i < itemX.length; i++) {
            drawSlotFrame(g, itemX[i], itemY[i]);
        }

        int[] tankX = {
                AdvancedMixingVatLayout.TANK_A_X,
                AdvancedMixingVatLayout.TANK_B_X,
                AdvancedMixingVatLayout.TANK_C_X,
                AdvancedMixingVatLayout.GAS_X,
                AdvancedMixingVatLayout.OUTPUT_X
        };
        int[] tankSlotX = {
                AdvancedMixingVatLayout.TANK_A_SLOT_X,
                AdvancedMixingVatLayout.TANK_B_SLOT_X,
                AdvancedMixingVatLayout.TANK_C_SLOT_X,
                AdvancedMixingVatLayout.GAS_SLOT_X,
                AdvancedMixingVatLayout.OUTPUT_SLOT_X
        };
        for (int i = 0; i < tankX.length; i++) {
            drawSlotFrame(g, tankSlotX[i], AdvancedMixingVatLayout.TANK_SLOT_Y);
            drawTankFrame(
                    g,
                    tankX[i],
                    AdvancedMixingVatLayout.TANK_Y,
                    AdvancedMixingVatLayout.TANK_W,
                    AdvancedMixingVatLayout.TANK_H,
                    AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                    AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                    AdvancedMixingVatLayout.TANK_INNER_W,
                    AdvancedMixingVatLayout.TANK_INNER_H
            );
        }

        List<?> fluidInputs = recipe.fluidInputs();
        for (int i = 0; i < Math.min(fluidInputs.size(), 3); i++) {
            Object fluid = fluidInputs.get(i);
            drawFluidTankPreview(
                    g,
                    JeiCompatUtil.fluid(JeiCompatUtil.idOf(fluid, "fluid", "fluidId")),
                    JeiCompatUtil.intOf(fluid, "amount"),
                    tankX[i],
                    AdvancedMixingVatLayout.TANK_Y,
                    AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                    AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                    AdvancedMixingVatLayout.TANK_INNER_W,
                    AdvancedMixingVatLayout.TANK_INNER_H
            );
        }

        if (recipe.gasInput() != null) {
            drawGasTankPreview(
                    g,
                    JeiCompatUtil.idOf(recipe.gasInput(), "gas", "gasId"),
                    recipe.gasInput().amount(),
                    AdvancedMixingVatLayout.GAS_X,
                    AdvancedMixingVatLayout.TANK_Y,
                    AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                    AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                    AdvancedMixingVatLayout.TANK_INNER_W,
                    AdvancedMixingVatLayout.TANK_INNER_H
            );
        }

        drawFluidTankPreview(
                g,
                JeiCompatUtil.fluid(JeiCompatUtil.idOf(recipe.output(), "fluid", "fluidId")),
                JeiCompatUtil.intOf(recipe.output(), "amount"),
                AdvancedMixingVatLayout.OUTPUT_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_W,
                AdvancedMixingVatLayout.TANK_INNER_H
        );

        drawHorizontalBar(g, AdvancedMixingVatLayout.PROGRESS_X, AdvancedMixingVatLayout.PROGRESS_Y, AdvancedMixingVatLayout.PROGRESS_W, AdvancedMixingVatLayout.PROGRESS_H, AdvancedMixingVatLayout.PROGRESS_W, 0xFF768AB8, 0xFFAAB9DB);

        List<?> itemInputs = recipe.itemInputs();
        for (int i = 0; i < Math.min(itemInputs.size(), itemX.length); i++) {
            drawSlotCount(g, itemX[i], itemY[i], JeiCompatUtil.countOf(itemInputs.get(i)));
        }

        drawTitle(g);
        drawBottomInfo(g, "No heat required  |  Time: " + recipe.processingTime() + "t");
    }

    @Override
    public List<Component> getTooltipStrings(AdvancedMixingVatRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        List<?> fluidInputs = recipe.fluidInputs();

        if (isHoveringBox(AdvancedMixingVatLayout.TANK_A_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H, mouseX, mouseY)) {
            return advancedVatFluidInputTooltip("Fluid Input A", fluidInputs, 0);
        } else if (isHoveringBox(AdvancedMixingVatLayout.TANK_B_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H, mouseX, mouseY)) {
            return advancedVatFluidInputTooltip("Fluid Input B", fluidInputs, 1);
        } else if (isHoveringBox(AdvancedMixingVatLayout.TANK_C_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H, mouseX, mouseY)) {
            return advancedVatFluidInputTooltip("Fluid Input C", fluidInputs, 2);
        } else if (isHoveringBox(AdvancedMixingVatLayout.GAS_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H, mouseX, mouseY)) {
            long amount = recipe.gasInput() == null ? 0L : recipe.gasInput().amount();
            return amountTooltip("Gas Input", amount, AdvancedMixingVatBlockEntity.GAS_TANK_CAPACITY, "units");
        } else if (isHoveringBox(AdvancedMixingVatLayout.OUTPUT_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip(
                    "Fluid Output",
                    JeiCompatUtil.idOf(recipe.output(), "fluid", "fluidId"),
                    JeiCompatUtil.intOf(recipe.output(), "amount"),
                    AdvancedMixingVatBlockEntity.OUTPUT_TANK_CAPACITY
            );
        } else if (isHoveringBox(AdvancedMixingVatLayout.PROGRESS_X, AdvancedMixingVatLayout.PROGRESS_Y, AdvancedMixingVatLayout.PROGRESS_W, AdvancedMixingVatLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Mixing Progress", 0, recipe.processingTime());
        }

        return List.of();
    }

    private List<Component> advancedVatFluidInputTooltip(String title, List<?> fluidInputs, int index) {
        if (index >= fluidInputs.size()) {
            return fluidTankTooltip(title, Fluids.EMPTY, 0, AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY);
        }
        Object fluid = fluidInputs.get(index);
        return fluidTankTooltip(
                title,
                JeiCompatUtil.idOf(fluid, "fluid", "fluidId"),
                JeiCompatUtil.intOf(fluid, "amount"),
                AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY
        );
    }
}

