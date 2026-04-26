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
        drawBottomInfo(g, "Heat â‰¥ " + recipe.minimumHeat() + "  |  Time: " + recipe.processingTime() + "t");
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

