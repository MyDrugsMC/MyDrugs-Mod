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


final class ElectrolyzerRecipeCategory extends AbstractNiceRecipeCategory<ElectrolyzerRecipe> {
    static final RecipeType<ElectrolyzerRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "electrolyzer"), ElectrolyzerRecipe.class);

    ElectrolyzerRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.electrolyzer"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "ELECTROLYZER"),
                ElectrolyzerLayout.GUI_WIDTH,
                ElectrolyzerLayout.MACHINE_PANEL_Y + ElectrolyzerLayout.MACHINE_PANEL_H + 14
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ElectrolyzerRecipe recipe, IFocusGroup focuses) {
        addFluid(builder, RecipeIngredientRole.INPUT, ElectrolyzerLayout.INPUT_SLOT_X, ElectrolyzerLayout.INPUT_SLOT_Y, recipe.inputFluid().fluid(), recipe.inputFluid().amount());

        recipe.outputFluid1().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, ElectrolyzerLayout.OUTPUT_1_SLOT_X, ElectrolyzerLayout.OUTPUT_1_SLOT_Y, output.fluid(), output.amount())
        );
        recipe.outputGas1().ifPresent(output ->
                addGas(builder, RecipeIngredientRole.OUTPUT, ElectrolyzerLayout.OUTPUT_1_SLOT_X, ElectrolyzerLayout.OUTPUT_1_SLOT_Y, output.gas(), output.amount())
        );

        recipe.outputFluid2().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, ElectrolyzerLayout.OUTPUT_2_SLOT_X, ElectrolyzerLayout.OUTPUT_2_SLOT_Y, output.fluid(), output.amount())
        );
        recipe.outputGas2().ifPresent(output ->
                addGas(builder, RecipeIngredientRole.OUTPUT, ElectrolyzerLayout.OUTPUT_2_SLOT_X, ElectrolyzerLayout.OUTPUT_2_SLOT_Y, output.gas(), output.amount())
        );

        recipe.outputFluid3().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, ElectrolyzerLayout.OUTPUT_3_SLOT_X, ElectrolyzerLayout.OUTPUT_3_SLOT_Y, output.fluid(), output.amount())
        );
        recipe.outputGas3().ifPresent(output ->
                addGas(builder, RecipeIngredientRole.OUTPUT, ElectrolyzerLayout.OUTPUT_3_SLOT_X, ElectrolyzerLayout.OUTPUT_3_SLOT_Y, output.gas(), output.amount())
        );
    }

    @Override
    public void draw(ElectrolyzerRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        drawWindow(g, width, height);
        drawPanel(g, ElectrolyzerLayout.MACHINE_PANEL_X, ElectrolyzerLayout.MACHINE_PANEL_Y, ElectrolyzerLayout.MACHINE_PANEL_W, ElectrolyzerLayout.MACHINE_PANEL_H, 0xFF323232);
        drawPanel(g, ElectrolyzerLayout.CENTER_PANEL_X, ElectrolyzerLayout.CENTER_PANEL_Y, ElectrolyzerLayout.CENTER_PANEL_W, ElectrolyzerLayout.CENTER_PANEL_H, 0xFF262B32);

        drawTankFrame(g, ElectrolyzerLayout.INPUT_TANK_X, ElectrolyzerLayout.INPUT_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, ElectrolyzerLayout.TANK_INNER_X_OFFSET, ElectrolyzerLayout.TANK_INNER_Y_OFFSET, ElectrolyzerLayout.TANK_INNER_W, ElectrolyzerLayout.TANK_INNER_H);
        drawTankFrame(g, ElectrolyzerLayout.OUTPUT_1_TANK_X, ElectrolyzerLayout.OUTPUT_1_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, ElectrolyzerLayout.TANK_INNER_X_OFFSET, ElectrolyzerLayout.TANK_INNER_Y_OFFSET, ElectrolyzerLayout.TANK_INNER_W, ElectrolyzerLayout.TANK_INNER_H);
        drawTankFrame(g, ElectrolyzerLayout.OUTPUT_2_TANK_X, ElectrolyzerLayout.OUTPUT_2_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, ElectrolyzerLayout.TANK_INNER_X_OFFSET, ElectrolyzerLayout.TANK_INNER_Y_OFFSET, ElectrolyzerLayout.TANK_INNER_W, ElectrolyzerLayout.TANK_INNER_H);
        drawTankFrame(g, ElectrolyzerLayout.OUTPUT_3_TANK_X, ElectrolyzerLayout.OUTPUT_3_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, ElectrolyzerLayout.TANK_INNER_X_OFFSET, ElectrolyzerLayout.TANK_INNER_Y_OFFSET, ElectrolyzerLayout.TANK_INNER_W, ElectrolyzerLayout.TANK_INNER_H);

        drawFluidTankPreview(g, recipe.inputFluid().fluid(), recipe.inputFluid().amount(), ElectrolyzerLayout.INPUT_TANK_X, ElectrolyzerLayout.INPUT_TANK_Y, ElectrolyzerLayout.TANK_INNER_X_OFFSET, ElectrolyzerLayout.TANK_INNER_Y_OFFSET, ElectrolyzerLayout.TANK_INNER_W, ElectrolyzerLayout.TANK_INNER_H);

        recipe.outputFluid1().ifPresent(output -> drawFluidTankPreview(g, output.fluid(), output.amount(), ElectrolyzerLayout.OUTPUT_1_TANK_X, ElectrolyzerLayout.OUTPUT_1_TANK_Y, ElectrolyzerLayout.TANK_INNER_X_OFFSET, ElectrolyzerLayout.TANK_INNER_Y_OFFSET, ElectrolyzerLayout.TANK_INNER_W, ElectrolyzerLayout.TANK_INNER_H));
        recipe.outputGas1().ifPresent(output -> drawGasTankPreview(g, output.gas(), output.amount(), ElectrolyzerLayout.OUTPUT_1_TANK_X, ElectrolyzerLayout.OUTPUT_1_TANK_Y, ElectrolyzerLayout.TANK_INNER_X_OFFSET, ElectrolyzerLayout.TANK_INNER_Y_OFFSET, ElectrolyzerLayout.TANK_INNER_W, ElectrolyzerLayout.TANK_INNER_H));
        recipe.outputFluid2().ifPresent(output -> drawFluidTankPreview(g, output.fluid(), output.amount(), ElectrolyzerLayout.OUTPUT_2_TANK_X, ElectrolyzerLayout.OUTPUT_2_TANK_Y, ElectrolyzerLayout.TANK_INNER_X_OFFSET, ElectrolyzerLayout.TANK_INNER_Y_OFFSET, ElectrolyzerLayout.TANK_INNER_W, ElectrolyzerLayout.TANK_INNER_H));
        recipe.outputGas2().ifPresent(output -> drawGasTankPreview(g, output.gas(), output.amount(), ElectrolyzerLayout.OUTPUT_2_TANK_X, ElectrolyzerLayout.OUTPUT_2_TANK_Y, ElectrolyzerLayout.TANK_INNER_X_OFFSET, ElectrolyzerLayout.TANK_INNER_Y_OFFSET, ElectrolyzerLayout.TANK_INNER_W, ElectrolyzerLayout.TANK_INNER_H));
        recipe.outputFluid3().ifPresent(output -> drawFluidTankPreview(g, output.fluid(), output.amount(), ElectrolyzerLayout.OUTPUT_3_TANK_X, ElectrolyzerLayout.OUTPUT_3_TANK_Y, ElectrolyzerLayout.TANK_INNER_X_OFFSET, ElectrolyzerLayout.TANK_INNER_Y_OFFSET, ElectrolyzerLayout.TANK_INNER_W, ElectrolyzerLayout.TANK_INNER_H));
        recipe.outputGas3().ifPresent(output -> drawGasTankPreview(g, output.gas(), output.amount(), ElectrolyzerLayout.OUTPUT_3_TANK_X, ElectrolyzerLayout.OUTPUT_3_TANK_Y, ElectrolyzerLayout.TANK_INNER_X_OFFSET, ElectrolyzerLayout.TANK_INNER_Y_OFFSET, ElectrolyzerLayout.TANK_INNER_W, ElectrolyzerLayout.TANK_INNER_H));

        drawSlotFrame(g, ElectrolyzerLayout.INPUT_SLOT_X, ElectrolyzerLayout.INPUT_SLOT_Y);
        drawSlotFrame(g, ElectrolyzerLayout.OUTPUT_1_SLOT_X, ElectrolyzerLayout.OUTPUT_1_SLOT_Y);
        drawSlotFrame(g, ElectrolyzerLayout.OUTPUT_2_SLOT_X, ElectrolyzerLayout.OUTPUT_2_SLOT_Y);
        drawSlotFrame(g, ElectrolyzerLayout.OUTPUT_3_SLOT_X, ElectrolyzerLayout.OUTPUT_3_SLOT_Y);
        drawSlotFrame(g, ElectrolyzerLayout.FUEL_SLOT_X, ElectrolyzerLayout.FUEL_SLOT_Y);

        drawHorizontalBar(g, ElectrolyzerLayout.PROGRESS_X, ElectrolyzerLayout.PROGRESS_Y, ElectrolyzerLayout.PROGRESS_W, ElectrolyzerLayout.PROGRESS_H, ElectrolyzerLayout.PROGRESS_W, 0xFF768AB8, 0xFFAAB9DB);
        drawVerticalBar(g, ElectrolyzerLayout.FUEL_BAR_X, ElectrolyzerLayout.FUEL_BAR_Y, ElectrolyzerLayout.FUEL_BAR_W, ElectrolyzerLayout.FUEL_BAR_H, ElectrolyzerLayout.FUEL_BAR_INNER_X_OFFSET, ElectrolyzerLayout.FUEL_BAR_INNER_Y_OFFSET, ElectrolyzerLayout.FUEL_BAR_INNER_W, ElectrolyzerLayout.FUEL_BAR_INNER_H, ElectrolyzerLayout.FUEL_BAR_INNER_H, 0xFFE38D3F, 0xFFFFC270);

        drawDumpButton(g, ElectrolyzerLayout.DUMP_INPUT_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, false, true);
        drawDumpButton(g, ElectrolyzerLayout.DUMP_OUTPUT_1_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, false, recipe.outputFluid1().isPresent() || recipe.outputGas1().isPresent());
        drawDumpButton(g, ElectrolyzerLayout.DUMP_OUTPUT_2_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, false, recipe.outputFluid2().isPresent() || recipe.outputGas2().isPresent());
        drawDumpButton(g, ElectrolyzerLayout.DUMP_OUTPUT_3_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, false, recipe.outputFluid3().isPresent() || recipe.outputGas3().isPresent());

        drawTitle(g);
    }

    @Override
    public List<Component> getTooltipStrings(ElectrolyzerRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(ElectrolyzerLayout.DUMP_INPUT_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, ElectrolyzerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump input tank");
        } else if (isHoveringBox(ElectrolyzerLayout.DUMP_OUTPUT_1_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, ElectrolyzerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank 1");
        } else if (isHoveringBox(ElectrolyzerLayout.DUMP_OUTPUT_2_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, ElectrolyzerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank 2");
        } else if (isHoveringBox(ElectrolyzerLayout.DUMP_OUTPUT_3_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, ElectrolyzerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank 3");
        } else if (isHoveringBox(ElectrolyzerLayout.INPUT_TANK_X, ElectrolyzerLayout.INPUT_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Input tank", recipe.inputFluid().fluid(), recipe.inputFluid().amount(), ElectrolyzerBlockEntity.FLUID_CAPACITY);
        } else if (isHoveringBox(ElectrolyzerLayout.OUTPUT_1_TANK_X, ElectrolyzerLayout.OUTPUT_1_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, mouseX, mouseY)) {
            return electrolyzerOutputTooltip(1, recipe.outputFluid1().orElse(null), recipe.outputGas1().orElse(null));
        } else if (isHoveringBox(ElectrolyzerLayout.OUTPUT_2_TANK_X, ElectrolyzerLayout.OUTPUT_2_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, mouseX, mouseY)) {
            return electrolyzerOutputTooltip(2, recipe.outputFluid2().orElse(null), recipe.outputGas2().orElse(null));
        } else if (isHoveringBox(ElectrolyzerLayout.OUTPUT_3_TANK_X, ElectrolyzerLayout.OUTPUT_3_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, mouseX, mouseY)) {
            return electrolyzerOutputTooltip(3, recipe.outputFluid3().orElse(null), recipe.outputGas3().orElse(null));
        } else if (isHoveringBox(ElectrolyzerLayout.PROGRESS_X, ElectrolyzerLayout.PROGRESS_Y, ElectrolyzerLayout.PROGRESS_W, ElectrolyzerLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Electrolyzer progress", 0, recipe.baseTicks());
        } else if (isHoveringBox(ElectrolyzerLayout.FUEL_BAR_X, ElectrolyzerLayout.FUEL_BAR_Y, ElectrolyzerLayout.FUEL_BAR_W, ElectrolyzerLayout.FUEL_BAR_H, mouseX, mouseY)) {
            return amountTooltip("Fuel burn time", 0, recipe.baseTicks(), "ticks");
        }
        return List.of();
    }

    private List<Component> electrolyzerOutputTooltip(int index, Object fluidOutput, Object gasOutput) {
        if (gasOutput != null) {
            return gasTankTooltip(
                    "Output gas tank " + index,
                    JeiCompatUtil.idOf(gasOutput, "gas", "gasId"),
                    JeiCompatUtil.longOf(gasOutput, "amount"),
                    ElectrolyzerBlockEntity.GAS_CAPACITY
            );
        }
        if (fluidOutput != null) {
            return fluidTankTooltip(
                    "Output fluid tank " + index,
                    JeiCompatUtil.idOf(fluidOutput, "fluid", "fluidId"),
                    JeiCompatUtil.intOf(fluidOutput, "amount"),
                    ElectrolyzerBlockEntity.FLUID_CAPACITY
            );
        }
        return fluidTankTooltip("Output fluid tank " + index, Fluids.EMPTY, 0, ElectrolyzerBlockEntity.FLUID_CAPACITY);
    }
}


final class DistillerRecipeCategory extends AbstractNiceRecipeCategory<DistillerRecipe> {
    static final RecipeType<DistillerRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "distiller"), DistillerRecipe.class);

    DistillerRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.distiller"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "DISTILLER"),
                DistillerLayout.GUI_WIDTH,
                DistillerLayout.MACHINE_PANEL_Y + DistillerLayout.MACHINE_PANEL_H + 14
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DistillerRecipe recipe, IFocusGroup focuses) {
        addFluid(builder, RecipeIngredientRole.INPUT, DistillerLayout.INPUT_SLOT_X, DistillerLayout.INPUT_SLOT_Y, recipe.input().fluid(), recipe.input().amount());

        addFluid(builder, RecipeIngredientRole.OUTPUT, DistillerLayout.OUTPUT_A_SLOT_X, DistillerLayout.OUTPUT_A_SLOT_Y, recipe.output1().fluid(), recipe.output1().amount());
        recipe.output2().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, DistillerLayout.OUTPUT_B_SLOT_X, DistillerLayout.OUTPUT_B_SLOT_Y, output.fluid(), output.amount())
        );
    }

    @Override
    public void draw(DistillerRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        drawWindow(g, width, height);
        drawPanel(g, DistillerLayout.MACHINE_PANEL_X, DistillerLayout.MACHINE_PANEL_Y, DistillerLayout.MACHINE_PANEL_W, DistillerLayout.MACHINE_PANEL_H, 0xFF323232);

        drawTankFrame(g, DistillerLayout.INPUT_TANK_X, DistillerLayout.INPUT_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, DistillerLayout.TANK_INNER_X_OFFSET, DistillerLayout.TANK_INNER_Y_OFFSET, DistillerLayout.TANK_INNER_W, DistillerLayout.TANK_INNER_H);
        drawTankFrame(g, DistillerLayout.OUTPUT_A_TANK_X, DistillerLayout.OUTPUT_A_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, DistillerLayout.TANK_INNER_X_OFFSET, DistillerLayout.TANK_INNER_Y_OFFSET, DistillerLayout.TANK_INNER_W, DistillerLayout.TANK_INNER_H);
        drawTankFrame(g, DistillerLayout.OUTPUT_B_TANK_X, DistillerLayout.OUTPUT_B_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, DistillerLayout.TANK_INNER_X_OFFSET, DistillerLayout.TANK_INNER_Y_OFFSET, DistillerLayout.TANK_INNER_W, DistillerLayout.TANK_INNER_H);

        drawFluidTankPreview(g, recipe.input().fluid(), recipe.input().amount(), DistillerLayout.INPUT_TANK_X, DistillerLayout.INPUT_TANK_Y, DistillerLayout.TANK_INNER_X_OFFSET, DistillerLayout.TANK_INNER_Y_OFFSET, DistillerLayout.TANK_INNER_W, DistillerLayout.TANK_INNER_H);
        drawFluidTankPreview(g, recipe.output1().fluid(), recipe.output1().amount(), DistillerLayout.OUTPUT_A_TANK_X, DistillerLayout.OUTPUT_A_TANK_Y, DistillerLayout.TANK_INNER_X_OFFSET, DistillerLayout.TANK_INNER_Y_OFFSET, DistillerLayout.TANK_INNER_W, DistillerLayout.TANK_INNER_H);
        recipe.output2().ifPresent(output ->
                drawFluidTankPreview(g, output.fluid(), output.amount(), DistillerLayout.OUTPUT_B_TANK_X, DistillerLayout.OUTPUT_B_TANK_Y, DistillerLayout.TANK_INNER_X_OFFSET, DistillerLayout.TANK_INNER_Y_OFFSET, DistillerLayout.TANK_INNER_W, DistillerLayout.TANK_INNER_H)
        );

        drawSlotFrame(g, DistillerLayout.INPUT_SLOT_X, DistillerLayout.INPUT_SLOT_Y);
        drawSlotFrame(g, DistillerLayout.OUTPUT_A_SLOT_X, DistillerLayout.OUTPUT_A_SLOT_Y);
        drawSlotFrame(g, DistillerLayout.OUTPUT_B_SLOT_X, DistillerLayout.OUTPUT_B_SLOT_Y);

        drawHorizontalBar(g, DistillerLayout.PROGRESS_X, DistillerLayout.PROGRESS_Y, DistillerLayout.PROGRESS_W, DistillerLayout.PROGRESS_H, DistillerLayout.PROGRESS_W, 0xFF768AB8, 0xFFAAB9DB);

        drawDumpButton(g, DistillerLayout.DUMP_INPUT_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, false, true);
        drawDumpButton(g, DistillerLayout.DUMP_OUTPUT_A_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, false, true);
        drawDumpButton(g, DistillerLayout.DUMP_OUTPUT_B_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, false, recipe.output2().isPresent());

        drawReactor(g, DistillerLayout.RUN_BUTTON_X, DistillerLayout.RUN_BUTTON_Y, false, true, false);

        drawTitle(g);
    }

    @Override
    public List<Component> getTooltipStrings(DistillerRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(DistillerLayout.DUMP_INPUT_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, DistillerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump input tank");
        } else if (isHoveringBox(DistillerLayout.DUMP_OUTPUT_A_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, DistillerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank A");
        } else if (isHoveringBox(DistillerLayout.DUMP_OUTPUT_B_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, DistillerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank B");
        } else if (isHoveringBox(DistillerLayout.INPUT_TANK_X, DistillerLayout.INPUT_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Input tank", recipe.input().fluid(), recipe.input().amount(), DistillerMenu.TANK_CAPACITY);
        } else if (isHoveringBox(DistillerLayout.OUTPUT_A_TANK_X, DistillerLayout.OUTPUT_A_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Output tank A", recipe.output1().fluid(), recipe.output1().amount(), DistillerMenu.TANK_CAPACITY);
        } else if (isHoveringBox(DistillerLayout.OUTPUT_B_TANK_X, DistillerLayout.OUTPUT_B_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, mouseX, mouseY)) {
            return recipe.output2()
                    .map(output -> fluidTankTooltip("Output tank B", output.fluid(), output.amount(), DistillerMenu.TANK_CAPACITY))
                    .orElseGet(() -> fluidTankTooltip("Output tank B", Fluids.EMPTY, 0, DistillerMenu.TANK_CAPACITY));
        } else if (isHoveringBox(DistillerLayout.PROGRESS_X, DistillerLayout.PROGRESS_Y, DistillerLayout.PROGRESS_W, DistillerLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Distillation progress", 0, recipe.baseTicks());
        } else if (isHoveringBox(DistillerLayout.RUN_BUTTON_X, DistillerLayout.RUN_BUTTON_Y, DistillerLayout.RUN_BUTTON_SIZE, DistillerLayout.RUN_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Run distiller", "More than 5 CPS increases speed");
        }
        return List.of();
    }

    private void drawReactor(GuiGraphics graphics, int localX, int localY, boolean hovered, boolean working, boolean boosted) {
        int cx = localX + DistillerLayout.RUN_BUTTON_SIZE / 2;
        int cy = localY + DistillerLayout.RUN_BUTTON_SIZE / 2;

        if (hovered) {
            graphics.fill(localX + 2, localY + 2, localX + DistillerLayout.RUN_BUTTON_SIZE - 2, localY + DistillerLayout.RUN_BUTTON_SIZE - 2, 0x16FFFFFF);
        }

        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS + 2, 0xFF818793);
        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS, 0xFF20242B);
        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS - 3, 0xFF9FA7B4);
        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS - 5, 0xFF3E4652);

        int coreColor = boosted ? 0xFF6FD6FF : working ? 0xFFE8E8E8 : 0xFF90959E;

        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_INNER_RADIUS, 0xFF2D333B);
        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_CORE_RADIUS, coreColor);

        if (boosted) {
            drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_CORE_RADIUS + 3, 0x336FD6FF);
        }
    }
}


final class DryingRecipeCategory extends AbstractNiceRecipeCategory<DryingRecipe> {
    static final RecipeType<DryingRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "drying"), DryingRecipe.class);

    DryingRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.drying_rack"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "DRYING_RACK"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DryingRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, centeredInLeftX(), centeredY(), recipe.input());
        addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), recipe.result());
    }
}

final class EvaporationTrayRecipeCategory extends AbstractNiceRecipeCategory<EvaporationTrayRecipe> {
    static final RecipeType<EvaporationTrayRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "evaporation_tray"), EvaporationTrayRecipe.class);

    EvaporationTrayRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.evaporation_tray"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "EVAPORATION_TRAY"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EvaporationTrayRecipe recipe, IFocusGroup focuses) {
        addFluid(builder, RecipeIngredientRole.INPUT, centeredInLeftX(), centeredY(), recipe.inputFluid(), recipe.inputAmount());
        addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), recipe.result());
    }
}

final class GrindingRecipeCategory extends AbstractNiceRecipeCategory<GrindingRecipe> {
    static final RecipeType<GrindingRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "grinding"), GrindingRecipe.class);

    GrindingRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.grinding_bowl"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "GRINDING_BOWL"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GrindingRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, centeredInLeftX(), centeredY(), recipe.ingredient());
        addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), recipe.result());
    }
}

final class BiochemicalReactorRecipeCategory extends AbstractNiceRecipeCategory<BiochemicalReactorRecipe> {
    static final RecipeType<BiochemicalReactorRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "biochemical_reactor"), BiochemicalReactorRecipe.class);

    BiochemicalReactorRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.biochemical_reactor"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "BIOCHEMICAL_REACTOR"),
                BiochemicalReactorLayout.GUI_WIDTH,
                BiochemicalReactorLayout.MACHINE_PANEL_Y + BiochemicalReactorLayout.MACHINE_PANEL_H + 14
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BiochemicalReactorRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, BiochemicalReactorLayout.ERGOT_SLOT_X, BiochemicalReactorLayout.ERGOT_SLOT_Y, JeiCompatUtil.ingredientOf(recipe.ergot()));
        addItemIngredient(builder, RecipeIngredientRole.INPUT, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_X, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_Y, JeiCompatUtil.ingredientOf(recipe.tryptophan()));

        addFluid(
                builder,
                RecipeIngredientRole.OUTPUT,
                BiochemicalReactorLayout.OUTPUT_SLOT_X,
                BiochemicalReactorLayout.OUTPUT_SLOT_Y,
                JeiCompatUtil.idOf(recipe.fluidOutput(), "fluid", "fluidId"),
                JeiCompatUtil.intOf(recipe.fluidOutput(), "amount")
        );
    }

    @Override
    public void draw(BiochemicalReactorRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        drawWindow(g, width, height);
        drawPanel(g, BiochemicalReactorLayout.MACHINE_PANEL_X, BiochemicalReactorLayout.MACHINE_PANEL_Y, BiochemicalReactorLayout.MACHINE_PANEL_W, BiochemicalReactorLayout.MACHINE_PANEL_H, 0xFF323232);

        drawSlotFrame(g, BiochemicalReactorLayout.ERGOT_SLOT_X, BiochemicalReactorLayout.ERGOT_SLOT_Y);
        drawSlotFrame(g, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_X, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_Y);
        drawSlotFrame(g, BiochemicalReactorLayout.CHARCOAL_SLOT_X, BiochemicalReactorLayout.CHARCOAL_SLOT_Y);
        drawSlotFrame(g, BiochemicalReactorLayout.OUTPUT_SLOT_X, BiochemicalReactorLayout.OUTPUT_SLOT_Y);

        drawHorizontalBar(g, BiochemicalReactorLayout.PROGRESS_X, BiochemicalReactorLayout.PROGRESS_Y, BiochemicalReactorLayout.PROGRESS_W, BiochemicalReactorLayout.PROGRESS_H, BiochemicalReactorLayout.PROGRESS_W, 0xFF768AB8, 0xFFAAB9DB);
        drawVerticalBar(g, BiochemicalReactorLayout.HEAT_BAR_X, BiochemicalReactorLayout.HEAT_BAR_Y, BiochemicalReactorLayout.HEAT_BAR_W, BiochemicalReactorLayout.HEAT_BAR_H, BiochemicalReactorLayout.HEAT_BAR_INNER_X_OFFSET, BiochemicalReactorLayout.HEAT_BAR_INNER_Y_OFFSET, BiochemicalReactorLayout.HEAT_BAR_INNER_W, BiochemicalReactorLayout.HEAT_BAR_INNER_H, BiochemicalReactorLayout.HEAT_BAR_INNER_H, 0xFFE38D3F, 0x22FFFFFF);
        drawVerticalBar(g, BiochemicalReactorLayout.MANUAL_BAR_X, BiochemicalReactorLayout.MANUAL_BAR_Y, BiochemicalReactorLayout.MANUAL_BAR_W, BiochemicalReactorLayout.MANUAL_BAR_H, BiochemicalReactorLayout.MANUAL_BAR_INNER_X_OFFSET, BiochemicalReactorLayout.MANUAL_BAR_INNER_Y_OFFSET, BiochemicalReactorLayout.MANUAL_BAR_INNER_W, BiochemicalReactorLayout.MANUAL_BAR_INNER_H, BiochemicalReactorLayout.MANUAL_BAR_INNER_H, 0xFF77A8E8, 0x22FFFFFF);

        drawTankFrame(g, BiochemicalReactorLayout.OUTPUT_TANK_X, BiochemicalReactorLayout.OUTPUT_TANK_Y, BiochemicalReactorLayout.TANK_W, BiochemicalReactorLayout.TANK_H, BiochemicalReactorLayout.TANK_INNER_X_OFFSET, BiochemicalReactorLayout.TANK_INNER_Y_OFFSET, BiochemicalReactorLayout.TANK_INNER_W, BiochemicalReactorLayout.TANK_INNER_H);
        drawFluidTankPreview(
                g,
                JeiCompatUtil.fluid(JeiCompatUtil.idOf(recipe.fluidOutput(), "fluid", "fluidId")),
                JeiCompatUtil.intOf(recipe.fluidOutput(), "amount"),
                BiochemicalReactorLayout.OUTPUT_TANK_X,
                BiochemicalReactorLayout.OUTPUT_TANK_Y,
                BiochemicalReactorLayout.TANK_INNER_X_OFFSET,
                BiochemicalReactorLayout.TANK_INNER_Y_OFFSET,
                BiochemicalReactorLayout.TANK_INNER_W,
                BiochemicalReactorLayout.TANK_INNER_H
        );

        drawPlusButton(g, BiochemicalReactorLayout.MANUAL_BUTTON_X, BiochemicalReactorLayout.MANUAL_BUTTON_Y, BiochemicalReactorLayout.MANUAL_BUTTON_W, BiochemicalReactorLayout.MANUAL_BUTTON_H, false);

        drawSlotCount(g, BiochemicalReactorLayout.ERGOT_SLOT_X, BiochemicalReactorLayout.ERGOT_SLOT_Y, JeiCompatUtil.countOf(recipe.ergot()));
        drawSlotCount(g, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_X, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_Y, JeiCompatUtil.countOf(recipe.tryptophan()));

        drawTitle(g);
        drawPanelLabel(g, "Processing", BiochemicalReactorLayout.PROGRESS_X, BiochemicalReactorLayout.PROGRESS_Y - 10, BiochemicalReactorLayout.PROGRESS_W);
        drawBottomInfo(g, "Heat ≥ " + recipe.minimumHeat() + "  |  Time: " + recipe.processingTime() + "t");
    }
    @Override
    public List<Component> getTooltipStrings(BiochemicalReactorRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(BiochemicalReactorLayout.PROGRESS_X, BiochemicalReactorLayout.PROGRESS_Y, BiochemicalReactorLayout.PROGRESS_W, BiochemicalReactorLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Progress", 0, recipe.processingTime(), "units");
        } else if (isHoveringBox(BiochemicalReactorLayout.HEAT_BAR_X, BiochemicalReactorLayout.HEAT_BAR_Y, BiochemicalReactorLayout.HEAT_BAR_W, BiochemicalReactorLayout.HEAT_BAR_H, mouseX, mouseY)) {
            return tooltip(
                    Component.literal("Heat"),
                    Component.literal(recipe.minimumHeat() + " minimum"),
                    Component.literal("Raises processing speed")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.MANUAL_BAR_X, BiochemicalReactorLayout.MANUAL_BAR_Y, BiochemicalReactorLayout.MANUAL_BAR_W, BiochemicalReactorLayout.MANUAL_BAR_H, mouseX, mouseY)) {
            return tooltip("Manual Energy", "0 / 100", "Generated by player interaction");
        } else if (isHoveringBox(BiochemicalReactorLayout.OUTPUT_TANK_X, BiochemicalReactorLayout.OUTPUT_TANK_Y, BiochemicalReactorLayout.TANK_W, BiochemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip(
                    "Output Tank",
                    JeiCompatUtil.idOf(recipe.fluidOutput(), "fluid", "fluidId"),
                    JeiCompatUtil.intOf(recipe.fluidOutput(), "amount"),
                    BiochemicalReactorBlockEntity.OUTPUT_TANK_CAPACITY
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.MANUAL_BUTTON_X, BiochemicalReactorLayout.MANUAL_BUTTON_Y, BiochemicalReactorLayout.MANUAL_BUTTON_W, BiochemicalReactorLayout.MANUAL_BUTTON_H, mouseX, mouseY)) {
            return tooltip("Manual Boost", "Click to add manual energy");
        } else if (isHoveringBox(BiochemicalReactorLayout.ERGOT_SLOT_X, BiochemicalReactorLayout.ERGOT_SLOT_Y, 18, 18, mouseX, mouseY)) {
            return tooltip("Ergot", "More Ergot in this slot increases speed");
        } else if (isHoveringBox(BiochemicalReactorLayout.TRYPTOPHAN_SLOT_X, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_Y, 18, 18, mouseX, mouseY)) {
            return tooltip("Tryptophan");
        } else if (isHoveringBox(BiochemicalReactorLayout.CHARCOAL_SLOT_X, BiochemicalReactorLayout.CHARCOAL_SLOT_Y, 18, 18, mouseX, mouseY)) {
            return tooltip("Charcoal", "Adds heat to the reactor");
        } else if (isHoveringBox(BiochemicalReactorLayout.OUTPUT_SLOT_X, BiochemicalReactorLayout.OUTPUT_SLOT_Y, 18, 18, mouseX, mouseY)) {
            return tooltip("Output Container");
        }
        return List.of();
    }

}


final class ChemicalReactorRecipeCategory extends AbstractNiceRecipeCategory<ChemicalReactorRecipe> {
    static final RecipeType<ChemicalReactorRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "chemical_reactor"), ChemicalReactorRecipe.class);

    ChemicalReactorRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.chemical_reactor"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "CHEMICAL_REACTOR"),
                ChemicalReactorLayout.GUI_WIDTH,
                ChemicalReactorLayout.MACHINE_PANEL_Y + ChemicalReactorLayout.MACHINE_PANEL_H + 14
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ChemicalReactorRecipe recipe, IFocusGroup focuses) {
        addGas(
                builder,
                RecipeIngredientRole.INPUT,
                ChemicalReactorLayout.PRIMARY_GAS_TANK_X,
                ChemicalReactorLayout.TRANSFER_SLOT_Y,
                JeiCompatUtil.idOf(recipe.primaryGas(), "gas", "gasId"),
                recipe.primaryGas().amount()
        );

        recipe.secondaryGas().ifPresent(gas ->
                addGas(
                        builder,
                        RecipeIngredientRole.INPUT,
                        ChemicalReactorLayout.SECONDARY_TANK_X,
                        ChemicalReactorLayout.TRANSFER_SLOT_Y,
                        JeiCompatUtil.idOf(gas, "gas", "gasId"),
                        gas.amount()
                )
        );

        recipe.secondaryFluid().ifPresent(fluidReq ->
                addFluid(
                        builder,
                        RecipeIngredientRole.INPUT,
                        ChemicalReactorLayout.SECONDARY_TANK_X,
                        ChemicalReactorLayout.TRANSFER_SLOT_Y_2,
                        JeiCompatUtil.idOf(fluidReq, "fluid", "fluidId"),
                        JeiCompatUtil.intOf(fluidReq, "amount")
                )
        );

        ResourceLocation outputId = recipe.outputId();
        ItemStack outputStack = JeiCompatUtil.stack(outputId, recipe.outputAmount());
        var outputFluid = JeiCompatUtil.fluid(outputId);

        if (!outputStack.isEmpty()) {
            addItemStack(builder, RecipeIngredientRole.OUTPUT, ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y_2, outputStack);
        } else if (outputFluid != Fluids.EMPTY) {
            addFluid(builder, RecipeIngredientRole.OUTPUT, ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y_2, outputFluid, recipe.outputAmount());
        } else {
            addGas(builder, RecipeIngredientRole.OUTPUT, ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y, outputId, recipe.outputAmount());
        }
    }

    @Override
    public void draw(ChemicalReactorRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        drawWindow(g, width, height);
        drawPanel(g, ChemicalReactorLayout.MACHINE_PANEL_X, ChemicalReactorLayout.MACHINE_PANEL_Y, ChemicalReactorLayout.MACHINE_PANEL_W, ChemicalReactorLayout.MACHINE_PANEL_H, 0xFF323232);

        drawChemicalTank(g, ChemicalReactorLayout.PRIMARY_GAS_TANK_X, ChemicalReactorLayout.PRIMARY_GAS_TANK_Y);
        drawChemicalTank(g, ChemicalReactorLayout.SECONDARY_TANK_X, ChemicalReactorLayout.SECONDARY_TANK_Y);
        drawChemicalTank(g, ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.OUTPUT_TANK_Y);

        drawGasTankPreview(
                g,
                JeiCompatUtil.idOf(recipe.primaryGas(), "gas", "gasId"),
                recipe.primaryGas().amount(),
                ChemicalReactorLayout.PRIMARY_GAS_TANK_X,
                ChemicalReactorLayout.PRIMARY_GAS_TANK_Y,
                ChemicalReactorLayout.TANK_INNER_X_OFFSET,
                ChemicalReactorLayout.TANK_INNER_Y_OFFSET,
                ChemicalReactorLayout.TANK_INNER_W,
                ChemicalReactorLayout.TANK_INNER_H
        );

        recipe.secondaryGas().ifPresent(gas ->
                drawGasTankPreview(
                        g,
                        JeiCompatUtil.idOf(gas, "gas", "gasId"),
                        gas.amount(),
                        ChemicalReactorLayout.SECONDARY_TANK_X,
                        ChemicalReactorLayout.SECONDARY_TANK_Y,
                        ChemicalReactorLayout.TANK_INNER_X_OFFSET,
                        ChemicalReactorLayout.TANK_INNER_Y_OFFSET,
                        ChemicalReactorLayout.TANK_INNER_W,
                        ChemicalReactorLayout.TANK_INNER_H
                )
        );

        recipe.secondaryFluid().ifPresent(fluidReq ->
                drawFluidTankPreview(
                        g,
                        JeiCompatUtil.fluid(JeiCompatUtil.idOf(fluidReq, "fluid", "fluidId")),
                        JeiCompatUtil.intOf(fluidReq, "amount"),
                        ChemicalReactorLayout.SECONDARY_TANK_X,
                        ChemicalReactorLayout.SECONDARY_TANK_Y,
                        ChemicalReactorLayout.TANK_INNER_X_OFFSET,
                        ChemicalReactorLayout.TANK_INNER_Y_OFFSET,
                        ChemicalReactorLayout.TANK_INNER_W,
                        ChemicalReactorLayout.TANK_INNER_H
                )
        );

        ResourceLocation outputId = recipe.outputId();
        var outputFluid = JeiCompatUtil.fluid(outputId);
        if (outputFluid != Fluids.EMPTY) {
            drawFluidTankPreview(g, outputFluid, recipe.outputAmount(), ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.OUTPUT_TANK_Y, ChemicalReactorLayout.TANK_INNER_X_OFFSET, ChemicalReactorLayout.TANK_INNER_Y_OFFSET, ChemicalReactorLayout.TANK_INNER_W, ChemicalReactorLayout.TANK_INNER_H);
        } else if (JeiCompatUtil.stack(outputId, recipe.outputAmount()).isEmpty()) {
            drawGasTankPreview(g, outputId, recipe.outputAmount(), ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.OUTPUT_TANK_Y, ChemicalReactorLayout.TANK_INNER_X_OFFSET, ChemicalReactorLayout.TANK_INNER_Y_OFFSET, ChemicalReactorLayout.TANK_INNER_W, ChemicalReactorLayout.TANK_INNER_H);
        }

        drawSlotFrame(g, ChemicalReactorLayout.FUEL_SLOT_X, ChemicalReactorLayout.FUEL_SLOT_Y);
        drawSlotFrame(g, ChemicalReactorLayout.PRIMARY_GAS_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y);
        drawSlotFrame(g, ChemicalReactorLayout.SECONDARY_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y);
//        drawSlotFrame(g, ChemicalReactorLayout.SECONDARY_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y_2);
        drawSlotFrame(g, ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y);
//        drawSlotFrame(g, ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y_2);

        drawHorizontalBar(g, ChemicalReactorLayout.PROGRESS_X, ChemicalReactorLayout.PROGRESS_Y, ChemicalReactorLayout.PROGRESS_W, ChemicalReactorLayout.PROGRESS_H, ChemicalReactorLayout.PROGRESS_W, 0xFF85A6C9, 0xFFC6DCF2);
        drawVerticalBar(g, ChemicalReactorLayout.HEAT_BAR_X, ChemicalReactorLayout.HEAT_BAR_Y, ChemicalReactorLayout.HEAT_BAR_W, ChemicalReactorLayout.HEAT_BAR_H, ChemicalReactorLayout.HEAT_BAR_INNER_X_OFFSET, ChemicalReactorLayout.HEAT_BAR_INNER_Y_OFFSET, ChemicalReactorLayout.HEAT_BAR_INNER_W, ChemicalReactorLayout.HEAT_BAR_INNER_H, ChemicalReactorLayout.HEAT_BAR_INNER_H, 0xFFE35C3F, 0xFFFFB870);
        drawVerticalBar(g, ChemicalReactorLayout.FUEL_BAR_X, ChemicalReactorLayout.FUEL_BAR_Y, ChemicalReactorLayout.FUEL_BAR_W, ChemicalReactorLayout.FUEL_BAR_H, ChemicalReactorLayout.FUEL_BAR_INNER_X_OFFSET, ChemicalReactorLayout.FUEL_BAR_INNER_Y_OFFSET, ChemicalReactorLayout.FUEL_BAR_INNER_W, ChemicalReactorLayout.FUEL_BAR_INNER_H, ChemicalReactorLayout.FUEL_BAR_INNER_H, 0xFFE38D3F, 0xFFFFC270);
        drawHorizontalBar(g, ChemicalReactorLayout.MANUAL_BAR_X, ChemicalReactorLayout.MANUAL_BAR_Y, ChemicalReactorLayout.MANUAL_BAR_W, ChemicalReactorLayout.MANUAL_BAR_H, ChemicalReactorLayout.MANUAL_BAR_W, 0xFF63B36D, 0xFFA8E4AF);

        drawTitle(g);
//        drawCentered(g, "Input A", ChemicalReactorLayout.PRIMARY_GAS_TANK_X - 16, ChemicalReactorLayout.LABEL_Y, 48, 0xFFB8B8B8);
//        drawCentered(g, "Input B", ChemicalReactorLayout.SECONDARY_TANK_X - 16, ChemicalReactorLayout.LABEL_Y, 48, 0xFFB8B8B8);
        drawCentered(g, "Process", ChemicalReactorLayout.PROGRESS_X, ChemicalReactorLayout.LABEL_Y + 6, ChemicalReactorLayout.PROGRESS_W, 0xFFB8B8B8);
//        drawCentered(g, "Output", ChemicalReactorLayout.OUTPUT_TANK_X - 16, ChemicalReactorLayout.LABEL_Y, 48, 0xFFB8B8B8);
        drawBottomInfo(g, "Heat ≥ " + recipe.minHeat() + "  |  Time: " + recipe.processTime() + "t");
    }

    @Override
    public List<Component> getTooltipStrings(ChemicalReactorRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(ChemicalReactorLayout.PRIMARY_GAS_TANK_X, ChemicalReactorLayout.PRIMARY_GAS_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            return gasTankTooltip(
                    "Primary input gas",
                    JeiCompatUtil.idOf(recipe.primaryGas(), "gas", "gasId"),
                    recipe.primaryGas().amount(),
                    ChemicalReactorBlockEntity.GAS_TANK_CAPACITY
            );
        } else if (isHoveringBox(ChemicalReactorLayout.SECONDARY_TANK_X, ChemicalReactorLayout.SECONDARY_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            if (recipe.secondaryFluid().isPresent()) {
                Object fluid = recipe.secondaryFluid().get();
                return fluidTankTooltip(
                        "Secondary input fluid",
                        JeiCompatUtil.idOf(fluid, "fluid", "fluidId"),
                        JeiCompatUtil.intOf(fluid, "amount"),
                        ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY
                );
            }
            if (recipe.secondaryGas().isPresent()) {
                Object gas = recipe.secondaryGas().get();
                return gasTankTooltip(
                        "Secondary input gas",
                        JeiCompatUtil.idOf(gas, "gas", "gasId"),
                        JeiCompatUtil.longOf(gas, "amount"),
                        ChemicalReactorBlockEntity.GAS_TANK_CAPACITY
                );
            }
            return fluidTankTooltip("Secondary input fluid", Fluids.EMPTY, 0, ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY);
        } else if (isHoveringBox(ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.OUTPUT_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            if ("fluid".equals(JeiCompatUtil.serializedName(recipe.outputKind()))) {
                return fluidTankTooltip("Output fluid", recipe.outputId(), recipe.outputAmount(), ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY);
            }
            return gasTankTooltip("Output gas", recipe.outputId(), recipe.outputAmount(), ChemicalReactorBlockEntity.GAS_TANK_CAPACITY);
        } else if (isHoveringBox(ChemicalReactorLayout.PROGRESS_X, ChemicalReactorLayout.PROGRESS_Y, ChemicalReactorLayout.PROGRESS_W, ChemicalReactorLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Progress", 0, recipe.processTime());
        } else if (isHoveringBox(ChemicalReactorLayout.HEAT_BAR_X, ChemicalReactorLayout.HEAT_BAR_Y, ChemicalReactorLayout.HEAT_BAR_W, ChemicalReactorLayout.HEAT_BAR_H, mouseX, mouseY)) {
            return amountTooltip("Heat", recipe.minHeat(), ChemicalReactorBlockEntity.MAX_HEAT);
        } else if (isHoveringBox(ChemicalReactorLayout.FUEL_BAR_X, ChemicalReactorLayout.FUEL_BAR_Y, ChemicalReactorLayout.FUEL_BAR_W, ChemicalReactorLayout.FUEL_BAR_H, mouseX, mouseY)) {
            return amountTooltip("Fuel burn time", 0, recipe.processTime(), "ticks");
        } else if (isHoveringBox(ChemicalReactorLayout.MANUAL_BAR_X, ChemicalReactorLayout.MANUAL_BAR_Y, ChemicalReactorLayout.MANUAL_BAR_W, ChemicalReactorLayout.MANUAL_BAR_H, mouseX, mouseY)) {
            return amountTooltip("Manual boost", 0, ChemicalReactorBlockEntity.MAX_MANUAL_ENERGY);
        }
        return List.of();
    }

    private void drawChemicalTank(GuiGraphics g, int x, int y) {
        drawTankFrame(g, x, y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, ChemicalReactorLayout.TANK_INNER_X_OFFSET, ChemicalReactorLayout.TANK_INNER_Y_OFFSET, ChemicalReactorLayout.TANK_INNER_W, ChemicalReactorLayout.TANK_INNER_H);
    }
}


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


final class GasifierRecipeCategory extends AbstractNiceRecipeCategory<GasifierRecipe> {
    static final RecipeType<GasifierRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "gasifier"), GasifierRecipe.class);

    GasifierRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.gasifier"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "GASIFIER"),
                GasifierLayout.GUI_WIDTH,
                GasifierLayout.MACHINE_PANEL_Y + GasifierLayout.MACHINE_PANEL_H + 14
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GasifierRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, GasifierLayout.INPUT_SLOT_X, GasifierLayout.INPUT_SLOT_Y, recipe.input());
        addGas(builder, RecipeIngredientRole.OUTPUT, GasifierLayout.EXPORT_SLOT_X, GasifierLayout.EXPORT_SLOT_Y, recipe.gasOutput(), recipe.gasAmount());
    }

    @Override
    public void draw(GasifierRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        drawWindow(g, width, height, 0xFF181818, 0xFF262626);
        drawPanel(g, GasifierLayout.MACHINE_PANEL_X, GasifierLayout.MACHINE_PANEL_Y, GasifierLayout.MACHINE_PANEL_W, GasifierLayout.MACHINE_PANEL_H, 0xFF323232, 0xFF595959, 0xFF101010);

        drawSlotFrame(g, GasifierLayout.INPUT_SLOT_X, GasifierLayout.INPUT_SLOT_Y, 0xFF8A8A8A, 0xFF111111);
        drawSlotFrame(g, GasifierLayout.FUEL_SLOT_X, GasifierLayout.FUEL_SLOT_Y, 0xFF8A8A8A, 0xFF111111);
        drawSlotFrame(g, GasifierLayout.EXPORT_SLOT_X, GasifierLayout.EXPORT_SLOT_Y, 0xFF8A8A8A, 0xFF111111);

        drawVerticalBar(g, GasifierLayout.FUEL_BAR_X, GasifierLayout.FUEL_BAR_Y, GasifierLayout.FUEL_BAR_W, GasifierLayout.FUEL_BAR_H, GasifierLayout.FUEL_BAR_INNER_X_OFFSET, GasifierLayout.FUEL_BAR_INNER_Y_OFFSET, GasifierLayout.FUEL_BAR_INNER_W, GasifierLayout.FUEL_BAR_INNER_H, GasifierLayout.FUEL_BAR_INNER_H, 0xFFE38D3F, 0xFFFFC270);
        drawHorizontalBar(g, GasifierLayout.PROGRESS_X, GasifierLayout.PROGRESS_Y, GasifierLayout.PROGRESS_W, GasifierLayout.PROGRESS_H, GasifierLayout.PROGRESS_W, 0xFF768AB8, 0xFFAAB9DB);

        drawTankFrame(g, GasifierLayout.OUTPUT_TANK_X, GasifierLayout.OUTPUT_TANK_Y, GasifierLayout.TANK_W, GasifierLayout.TANK_H, GasifierLayout.TANK_INNER_X_OFFSET, GasifierLayout.TANK_INNER_Y_OFFSET, GasifierLayout.TANK_INNER_W, GasifierLayout.TANK_INNER_H);
        drawGasTankPreview(g, recipe.gasOutput(), recipe.gasAmount(), GasifierLayout.OUTPUT_TANK_X, GasifierLayout.OUTPUT_TANK_Y, GasifierLayout.TANK_INNER_X_OFFSET, GasifierLayout.TANK_INNER_Y_OFFSET, GasifierLayout.TANK_INNER_W, GasifierLayout.TANK_INNER_H);

        drawTitle(g);
        drawBottomInfo(g, "Time: " + recipe.processTime() + "t");
    }
    @Override
    public List<Component> getTooltipStrings(GasifierRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(GasifierLayout.OUTPUT_TANK_X, GasifierLayout.OUTPUT_TANK_Y, GasifierLayout.TANK_W, GasifierLayout.TANK_H, mouseX, mouseY)) {
            return gasTankTooltip("Output gas tank", recipe.gasOutput(), recipe.gasAmount(), GasifierMenu.TANK_CAPACITY, "mB");
        } else if (isHoveringBox(GasifierLayout.PROGRESS_X, GasifierLayout.PROGRESS_Y, GasifierLayout.PROGRESS_W, GasifierLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Gasification progress", 0, recipe.processTime());
        } else if (isHoveringBox(GasifierLayout.FUEL_BAR_X, GasifierLayout.FUEL_BAR_Y, GasifierLayout.FUEL_BAR_W, GasifierLayout.FUEL_BAR_H, mouseX, mouseY)) {
            return tooltip("Fuel burn", "Idle", "0 / " + recipe.processTime() + " ticks");
        } else if (isHoveringBox(GasifierLayout.FUEL_SLOT_X, GasifierLayout.FUEL_SLOT_Y, 18, 18, mouseX, mouseY)) {
            return tooltip("Fuel slot", "Accepts furnace fuels");
        } else if (isHoveringBox(GasifierLayout.EXPORT_SLOT_X, GasifierLayout.EXPORT_SLOT_Y, 18, 18, mouseX, mouseY)) {
            return tooltip("Gas Tank link slot", "Insert a Gas Tank block item", "Exports to the placed tank in front");
        }
        return List.of();
    }

}


final class GrowthChamberRecipeCategory extends AbstractNiceRecipeCategory<GrowthChamberRecipe> {
    static final RecipeType<GrowthChamberRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "growth_chamber"), GrowthChamberRecipe.class);

    GrowthChamberRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.growth_chamber"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "GROWTH_CHAMBER"),
                GrowthChamberLayout.GUI_WIDTH,
                GrowthChamberLayout.MACHINE_PANEL_Y + GrowthChamberLayout.MACHINE_PANEL_H + 14
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GrowthChamberRecipe recipe, IFocusGroup focuses) {
        if (recipe.water() > 0) {
            addFluid(builder, RecipeIngredientRole.INPUT, GrowthChamberLayout.WATER_INPUT_SLOT_X, GrowthChamberLayout.WATER_INPUT_SLOT_Y, Fluids.WATER, recipe.water());
        }

        addItemStack(builder, RecipeIngredientRole.INPUT, GrowthChamberLayout.INPUT_SLOT_X, GrowthChamberLayout.INPUT_SLOT_Y, JeiCompatUtil.stackOf(recipe.input()));
        addItemStack(builder, RecipeIngredientRole.INPUT, GrowthChamberLayout.BIOMASS_SLOT_X, GrowthChamberLayout.BIOMASS_SLOT_Y, JeiCompatUtil.stackOf(recipe.biomassInput()));

        addItemStack(builder, RecipeIngredientRole.OUTPUT, GrowthChamberLayout.MIDDLE_SLOT_X, GrowthChamberLayout.MIDDLE_SLOT_Y, JeiCompatUtil.stackOf(recipe.middleResult()));
        addItemStack(builder, RecipeIngredientRole.OUTPUT, GrowthChamberLayout.FINAL_SLOT_X, GrowthChamberLayout.FINAL_SLOT_Y, JeiCompatUtil.stackOf(recipe.finalResult()));
    }

    @Override
    public void draw(GrowthChamberRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        drawWindow(g, width, height);

        drawPanel(
                g,
                GrowthChamberLayout.MACHINE_PANEL_X,
                GrowthChamberLayout.MACHINE_PANEL_Y,
                GrowthChamberLayout.MACHINE_PANEL_W,
                GrowthChamberLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        drawTankFrame(
                g,
                GrowthChamberLayout.WATER_TANK_X,
                GrowthChamberLayout.WATER_TANK_Y,
                GrowthChamberLayout.TANK_W,
                GrowthChamberLayout.TANK_H,
                GrowthChamberLayout.TANK_INNER_X_OFFSET,
                GrowthChamberLayout.TANK_INNER_Y_OFFSET,
                GrowthChamberLayout.TANK_INNER_W,
                GrowthChamberLayout.TANK_INNER_H
        );

        drawTankFillTopLit(
                g,
                GrowthChamberLayout.WATER_TANK_X,
                GrowthChamberLayout.WATER_TANK_Y,
                GrowthChamberLayout.TANK_INNER_X_OFFSET,
                GrowthChamberLayout.TANK_INNER_Y_OFFSET,
                GrowthChamberLayout.TANK_INNER_W,
                GrowthChamberLayout.TANK_INNER_H,
                tankPreviewFilledPixels(recipe.water(), GrowthChamberMenu.TANK_CAPACITY, GrowthChamberLayout.TANK_INNER_H),
                0xFF4F88D6,
                0xFFA6C8FF
        );

        drawSlotFrame(g, GrowthChamberLayout.INPUT_SLOT_X, GrowthChamberLayout.INPUT_SLOT_Y);
        drawSlotFrame(g, GrowthChamberLayout.BIOMASS_SLOT_X, GrowthChamberLayout.BIOMASS_SLOT_Y);
        drawSlotFrame(g, GrowthChamberLayout.MIDDLE_SLOT_X, GrowthChamberLayout.MIDDLE_SLOT_Y);
        drawSlotFrame(g, GrowthChamberLayout.FINAL_SLOT_X, GrowthChamberLayout.FINAL_SLOT_Y);
        drawSlotFrame(g, GrowthChamberLayout.WATER_INPUT_SLOT_X, GrowthChamberLayout.WATER_INPUT_SLOT_Y);

        drawHorizontalBar(
                g,
                GrowthChamberLayout.GROWTH_PROGRESS_X,
                GrowthChamberLayout.GROWTH_PROGRESS_Y,
                GrowthChamberLayout.GROWTH_PROGRESS_W,
                GrowthChamberLayout.GROWTH_PROGRESS_H,
                GrowthChamberLayout.GROWTH_PROGRESS_W,
                0xFF6FBF73,
                0xFFB7E0B9
        );

        drawHorizontalBar(
                g,
                GrowthChamberLayout.MATURE_PROGRESS_X,
                GrowthChamberLayout.MATURE_PROGRESS_Y,
                GrowthChamberLayout.MATURE_PROGRESS_W,
                GrowthChamberLayout.MATURE_PROGRESS_H,
                GrowthChamberLayout.MATURE_PROGRESS_W,
                0xFFB58C5A,
                0xFFE4C18F
        );

        drawTitle(g);
        g.drawString(net.minecraft.client.Minecraft.getInstance().font, "Water", 16, GrowthChamberLayout.WATER_TANK_Y - 10, 0xFF9BB2D1, false);
        g.drawString(net.minecraft.client.Minecraft.getInstance().font, "Growing", GrowthChamberLayout.GROWTH_PROGRESS_X, GrowthChamberLayout.GROWTH_PROGRESS_Y - 10, 0xFFA9D8AC, false);
        g.drawString(net.minecraft.client.Minecraft.getInstance().font, "Maturing", GrowthChamberLayout.MATURE_PROGRESS_X, GrowthChamberLayout.MATURE_PROGRESS_Y - 10, 0xFFD7B78E, false);

        drawBottomInfo(g, "Water: " + recipe.water() + " mB  |  Base: " + recipe.baseTicks() + "t");
    }
    @Override
    public List<Component> getTooltipStrings(GrowthChamberRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(GrowthChamberLayout.WATER_TANK_X, GrowthChamberLayout.WATER_TANK_Y, GrowthChamberLayout.TANK_W, GrowthChamberLayout.TANK_H, mouseX, mouseY)) {
            return amountTooltip("Water tank", recipe.water(), GrowthChamberMenu.TANK_CAPACITY, "mB");
        } else if (isHoveringBox(GrowthChamberLayout.GROWTH_PROGRESS_X, GrowthChamberLayout.GROWTH_PROGRESS_Y, GrowthChamberLayout.GROWTH_PROGRESS_W, GrowthChamberLayout.GROWTH_PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Growing progress", 0, recipe.baseTicks());
        } else if (isHoveringBox(GrowthChamberLayout.MATURE_PROGRESS_X, GrowthChamberLayout.MATURE_PROGRESS_Y, GrowthChamberLayout.MATURE_PROGRESS_W, GrowthChamberLayout.MATURE_PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Maturing progress", 0, recipe.baseTicks());
        }
        return List.of();
    }

}

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
        drawBottomInfo(g, "Work: " + recipe.work() + "  |  Clamped: " + recipe.clampedWork());
    }
}

final class CatalyticReformerRecipeCategory extends AbstractNiceRecipeCategory<CatalyticReformerRecipe> {
    static final RecipeType<CatalyticReformerRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "catalytic_reformer"), CatalyticReformerRecipe.class);

    CatalyticReformerRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.catalytic_reformer"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "CATALYTIC_REFORMER"),
                CatalyticReformerLayout.GUI_WIDTH,
                CatalyticReformerLayout.MACHINE_PANEL_Y + CatalyticReformerLayout.MACHINE_PANEL_H + 14
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CatalyticReformerRecipe recipe, IFocusGroup focuses) {
        recipe.inputFluid1().ifPresent(input ->
                addFluid(builder, RecipeIngredientRole.INPUT, CatalyticReformerLayout.INPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y, input.fluid(), input.amount())
        );
        recipe.inputGas1().ifPresent(input ->
                addGas(builder, RecipeIngredientRole.INPUT, CatalyticReformerLayout.INPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y, input.gas(), input.amount())
        );

        recipe.inputFluid2().ifPresent(input ->
                addFluid(builder, RecipeIngredientRole.INPUT, CatalyticReformerLayout.INPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y, input.fluid(), input.amount())
        );
        recipe.inputGas2().ifPresent(input ->
                addGas(builder, RecipeIngredientRole.INPUT, CatalyticReformerLayout.INPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y, input.gas(), input.amount())
        );

        addItemIngredient(builder, RecipeIngredientRole.INPUT, CatalyticReformerLayout.CATALYST_SLOT_X, CatalyticReformerLayout.CATALYST_SLOT_Y, recipe.catalyst());

        recipe.outputFluid1().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, CatalyticReformerLayout.OUTPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y, output.fluid(), output.amount())
        );
        recipe.outputGas1().ifPresent(output ->
                addGas(builder, RecipeIngredientRole.OUTPUT, CatalyticReformerLayout.OUTPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y, output.gas(), output.amount())
        );

        recipe.outputFluid2().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, CatalyticReformerLayout.OUTPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y, output.fluid(), output.amount())
        );
        recipe.outputGas2().ifPresent(output ->
                addGas(builder, RecipeIngredientRole.OUTPUT, CatalyticReformerLayout.OUTPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y, output.gas(), output.amount())
        );

        recipe.outputFluid3().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, CatalyticReformerLayout.OUTPUT_3_SLOT_X, CatalyticReformerLayout.SLOT_Y, output.fluid(), output.amount())
        );
        recipe.outputGas3().ifPresent(output ->
                addGas(builder, RecipeIngredientRole.OUTPUT, CatalyticReformerLayout.OUTPUT_3_SLOT_X, CatalyticReformerLayout.SLOT_Y, output.gas(), output.amount())
        );
    }

    @Override
    public void draw(CatalyticReformerRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        drawWindow(g, width, height);

        drawPanel(
                g,
                CatalyticReformerLayout.MACHINE_PANEL_X,
                CatalyticReformerLayout.MACHINE_PANEL_Y,
                CatalyticReformerLayout.MACHINE_PANEL_W,
                CatalyticReformerLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        drawPanel(
                g,
                CatalyticReformerLayout.CENTER_PANEL_X,
                CatalyticReformerLayout.CENTER_PANEL_Y,
                CatalyticReformerLayout.CENTER_PANEL_W,
                CatalyticReformerLayout.CENTER_PANEL_H,
                0xFF262B32
        );

        drawCatalyticTank(g, CatalyticReformerLayout.INPUT_1_TANK_X);
        drawCatalyticTank(g, CatalyticReformerLayout.INPUT_2_TANK_X);
        drawCatalyticTank(g, CatalyticReformerLayout.OUTPUT_1_TANK_X);
        drawCatalyticTank(g, CatalyticReformerLayout.OUTPUT_2_TANK_X);
        drawCatalyticTank(g, CatalyticReformerLayout.OUTPUT_3_TANK_X);

        recipe.inputFluid1().ifPresent(input ->
                drawFluidTankPreview(g, JeiCompatUtil.fluid(input.fluid()), input.amount(), CatalyticReformerLayout.INPUT_1_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_INNER_X_OFFSET, CatalyticReformerLayout.TANK_INNER_Y_OFFSET, CatalyticReformerLayout.TANK_INNER_W, CatalyticReformerLayout.TANK_INNER_H)
        );
        recipe.inputGas1().ifPresent(input ->
                drawGasTankPreview(g, input.gas(), input.amount(), CatalyticReformerLayout.INPUT_1_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_INNER_X_OFFSET, CatalyticReformerLayout.TANK_INNER_Y_OFFSET, CatalyticReformerLayout.TANK_INNER_W, CatalyticReformerLayout.TANK_INNER_H)
        );

        recipe.inputFluid2().ifPresent(input ->
                drawFluidTankPreview(g, JeiCompatUtil.fluid(input.fluid()), input.amount(), CatalyticReformerLayout.INPUT_2_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_INNER_X_OFFSET, CatalyticReformerLayout.TANK_INNER_Y_OFFSET, CatalyticReformerLayout.TANK_INNER_W, CatalyticReformerLayout.TANK_INNER_H)
        );
        recipe.inputGas2().ifPresent(input ->
                drawGasTankPreview(g, input.gas(), input.amount(), CatalyticReformerLayout.INPUT_2_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_INNER_X_OFFSET, CatalyticReformerLayout.TANK_INNER_Y_OFFSET, CatalyticReformerLayout.TANK_INNER_W, CatalyticReformerLayout.TANK_INNER_H)
        );

        recipe.outputFluid1().ifPresent(output ->
                drawFluidTankPreview(g, JeiCompatUtil.fluid(output.fluid()), output.amount(), CatalyticReformerLayout.OUTPUT_1_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_INNER_X_OFFSET, CatalyticReformerLayout.TANK_INNER_Y_OFFSET, CatalyticReformerLayout.TANK_INNER_W, CatalyticReformerLayout.TANK_INNER_H)
        );
        recipe.outputGas1().ifPresent(output ->
                drawGasTankPreview(g, output.gas(), output.amount(), CatalyticReformerLayout.OUTPUT_1_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_INNER_X_OFFSET, CatalyticReformerLayout.TANK_INNER_Y_OFFSET, CatalyticReformerLayout.TANK_INNER_W, CatalyticReformerLayout.TANK_INNER_H)
        );

        recipe.outputFluid2().ifPresent(output ->
                drawFluidTankPreview(g, JeiCompatUtil.fluid(output.fluid()), output.amount(), CatalyticReformerLayout.OUTPUT_2_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_INNER_X_OFFSET, CatalyticReformerLayout.TANK_INNER_Y_OFFSET, CatalyticReformerLayout.TANK_INNER_W, CatalyticReformerLayout.TANK_INNER_H)
        );
        recipe.outputGas2().ifPresent(output ->
                drawGasTankPreview(g, output.gas(), output.amount(), CatalyticReformerLayout.OUTPUT_2_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_INNER_X_OFFSET, CatalyticReformerLayout.TANK_INNER_Y_OFFSET, CatalyticReformerLayout.TANK_INNER_W, CatalyticReformerLayout.TANK_INNER_H)
        );

        recipe.outputFluid3().ifPresent(output ->
                drawFluidTankPreview(g, JeiCompatUtil.fluid(output.fluid()), output.amount(), CatalyticReformerLayout.OUTPUT_3_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_INNER_X_OFFSET, CatalyticReformerLayout.TANK_INNER_Y_OFFSET, CatalyticReformerLayout.TANK_INNER_W, CatalyticReformerLayout.TANK_INNER_H)
        );
        recipe.outputGas3().ifPresent(output ->
                drawGasTankPreview(g, output.gas(), output.amount(), CatalyticReformerLayout.OUTPUT_3_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_INNER_X_OFFSET, CatalyticReformerLayout.TANK_INNER_Y_OFFSET, CatalyticReformerLayout.TANK_INNER_W, CatalyticReformerLayout.TANK_INNER_H)
        );

        drawSlotFrame(g, CatalyticReformerLayout.INPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        drawSlotFrame(g, CatalyticReformerLayout.INPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        drawSlotFrame(g, CatalyticReformerLayout.OUTPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        drawSlotFrame(g, CatalyticReformerLayout.OUTPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        drawSlotFrame(g, CatalyticReformerLayout.OUTPUT_3_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        drawSlotFrame(g, CatalyticReformerLayout.CATALYST_SLOT_X, CatalyticReformerLayout.CATALYST_SLOT_Y);

        drawHorizontalBar(
                g,
                CatalyticReformerLayout.PROGRESS_X,
                CatalyticReformerLayout.PROGRESS_Y,
                CatalyticReformerLayout.PROGRESS_W,
                CatalyticReformerLayout.PROGRESS_H,
                CatalyticReformerLayout.PROGRESS_W,
                0xFF768AB8,
                0xFFAAB9DB
        );

        drawDumpButton(g, CatalyticReformerLayout.DUMP_INPUT_1_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, false, true);
        drawDumpButton(g, CatalyticReformerLayout.DUMP_INPUT_2_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, false, true);
        drawDumpButton(g, CatalyticReformerLayout.DUMP_OUTPUT_1_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, false, true);
        drawDumpButton(g, CatalyticReformerLayout.DUMP_OUTPUT_2_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, false, true);
        drawDumpButton(g, CatalyticReformerLayout.DUMP_OUTPUT_3_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, false, true);

        drawTitle(g);
        drawBottomInfo(g, "Time: " + recipe.baseTicks() + "t" + (recipe.consumeCatalyst() ? "  |  Consumes catalyst" : ""));
    }

    @Override
    public List<Component> getTooltipStrings(CatalyticReformerRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(CatalyticReformerLayout.DUMP_INPUT_1_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, CatalyticReformerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump input tank 1");
        } else if (isHoveringBox(CatalyticReformerLayout.DUMP_INPUT_2_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, CatalyticReformerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump input tank 2");
        } else if (isHoveringBox(CatalyticReformerLayout.DUMP_OUTPUT_1_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, CatalyticReformerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank 1");
        } else if (isHoveringBox(CatalyticReformerLayout.DUMP_OUTPUT_2_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, CatalyticReformerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank 2");
        } else if (isHoveringBox(CatalyticReformerLayout.DUMP_OUTPUT_3_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, CatalyticReformerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank 3");
        } else if (isHoveringBox(CatalyticReformerLayout.INPUT_1_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H, mouseX, mouseY)) {
            return catalyticTankTooltip("Input", 1, recipe.inputFluid1().orElse(null), recipe.inputGas1().orElse(null));
        } else if (isHoveringBox(CatalyticReformerLayout.INPUT_2_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H, mouseX, mouseY)) {
            return catalyticTankTooltip("Input", 2, recipe.inputFluid2().orElse(null), recipe.inputGas2().orElse(null));
        } else if (isHoveringBox(CatalyticReformerLayout.OUTPUT_1_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H, mouseX, mouseY)) {
            return catalyticTankTooltip("Output", 1, recipe.outputFluid1().orElse(null), recipe.outputGas1().orElse(null));
        } else if (isHoveringBox(CatalyticReformerLayout.OUTPUT_2_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H, mouseX, mouseY)) {
            return catalyticTankTooltip("Output", 2, recipe.outputFluid2().orElse(null), recipe.outputGas2().orElse(null));
        } else if (isHoveringBox(CatalyticReformerLayout.OUTPUT_3_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H, mouseX, mouseY)) {
            return catalyticTankTooltip("Output", 3, recipe.outputFluid3().orElse(null), recipe.outputGas3().orElse(null));
        } else if (isHoveringBox(CatalyticReformerLayout.CATALYST_SLOT_X, CatalyticReformerLayout.CATALYST_SLOT_Y, 18, 18, mouseX, mouseY)) {
            return tooltip("Catalyst slot");
        } else if (isHoveringBox(CatalyticReformerLayout.PROGRESS_X, CatalyticReformerLayout.PROGRESS_Y, CatalyticReformerLayout.PROGRESS_W, CatalyticReformerLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Catalytic reforming progress", 0, recipe.baseTicks());
        }
        return List.of();
    }

    private List<Component> catalyticTankTooltip(String prefix, int index, Object fluidStack, Object gasStack) {
        if (gasStack != null) {
            return gasTankTooltip(
                    prefix + " gas tank " + index,
                    JeiCompatUtil.idOf(gasStack, "gas", "gasId"),
                    JeiCompatUtil.longOf(gasStack, "amount"),
                    CatalyticReformerBlockEntity.GAS_CAPACITY
            );
        }
        if (fluidStack != null) {
            return fluidTankTooltip(
                    prefix + " fluid tank " + index,
                    JeiCompatUtil.idOf(fluidStack, "fluid", "fluidId"),
                    JeiCompatUtil.intOf(fluidStack, "amount"),
                    CatalyticReformerBlockEntity.FLUID_CAPACITY
            );
        }
        return fluidTankTooltip(prefix + " fluid tank " + index, Fluids.EMPTY, 0, CatalyticReformerBlockEntity.FLUID_CAPACITY);
    }

    private void drawCatalyticTank(GuiGraphics g, int x) {
        drawTankFrame(
                g,
                x,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_W,
                CatalyticReformerLayout.TANK_H,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H
        );
    }
}

record BTXFractionationTowerJeiRecipe(
        ResourceLocation inputFluid,
        int inputAmount,
        ResourceLocation benzeneFluid,
        int benzeneAmount,
        ResourceLocation tolueneFluid,
        int tolueneAmount,
        ResourceLocation xyleneFluid,
        int xyleneAmount,
        int processingTime
) {
    static final BTXFractionationTowerJeiRecipe DEFAULT = new BTXFractionationTowerJeiRecipe(
            ModFluids.rl("btx_mix"),
            BTXFractionationTowerBlockEntity.INPUT_PER_BATCH,
            ModFluids.rl(ModFluids.BENZENE.name()),
            BTXFractionationTowerBlockEntity.BENZENE_PER_BATCH,
            ModFluids.rl(ModFluids.TOLUENE.name()),
            BTXFractionationTowerBlockEntity.TOLUENE_PER_BATCH,
            ModFluids.rl(ModFluids.XYLENE.name()),
            BTXFractionationTowerBlockEntity.XYLENE_PER_BATCH,
            BTXFractionationTowerBlockEntity.BASE_TICKS
    );

    Ingredient fuelPreview() {
        return Ingredient.of(Items.COAL, Items.CHARCOAL, Items.BLAZE_ROD, Items.LAVA_BUCKET);
    }
}

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
