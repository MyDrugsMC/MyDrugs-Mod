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
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.menu.BTXFractionationTowerMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.BTXFractionationTowerLayout;

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
                MachineGuiRenderer.btxFractionationTowerHeight(false)
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
        MachineGuiRenderer.drawBTXFractionationTower(
                this,
                g,
                new MachineGuiRenderer.BTXFractionationTowerState(
                        MachineGuiRenderer.TankFill.preview(recipe.inputFluid(), recipe.inputAmount(), BTXFractionationTowerMenu.TANK_CAPACITY),
                        MachineGuiRenderer.TankFill.preview(recipe.benzeneFluid(), recipe.benzeneAmount(), BTXFractionationTowerMenu.TANK_CAPACITY),
                        MachineGuiRenderer.TankFill.preview(recipe.tolueneFluid(), recipe.tolueneAmount(), BTXFractionationTowerMenu.TANK_CAPACITY),
                        MachineGuiRenderer.TankFill.preview(recipe.xyleneFluid(), recipe.xyleneAmount(), BTXFractionationTowerMenu.TANK_CAPACITY),
                        BTXFractionationTowerLayout.PROGRESS_W,
                        BTXFractionationTowerLayout.FUEL_BAR_INNER_H,
                        0xFFE38D3F,
                        false,
                        false,
                        false,
                        false,
                        recipe.inputAmount() > 0,
                        recipe.benzeneAmount() > 0,
                        recipe.tolueneAmount() > 0,
                        recipe.xyleneAmount() > 0
                ),
                false
        );
        MachineGuiRenderer.drawBTXFractionationTowerLabels(this, g, net.minecraft.client.Minecraft.getInstance().font, getTitle(), "Uses any Minecraft fuel  |  Time: " + recipe.processingTime() + "t");
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

}

