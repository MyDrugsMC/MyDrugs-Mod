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
        drawBottomInfo(g, "Time: " + recipe.baseTicks() + "t");
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

