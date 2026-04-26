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
        drawBottomInfo(g, "Heat â‰¥ " + recipe.minHeat() + "  |  Time: " + recipe.processTime() + "t");
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

