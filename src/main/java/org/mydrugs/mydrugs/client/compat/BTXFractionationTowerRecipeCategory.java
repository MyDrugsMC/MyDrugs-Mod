package org.mydrugs.mydrugs.client.compat;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.menu.BTXFractionationTowerMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.BTXFractionationTowerLayout;
import org.mydrugs.mydrugs.recipes.btx_fractionation.BTXFractionationRecipe;
import org.mydrugs.mydrugs.recipes.chemical_reactor.FluidRequirement;

import java.util.List;

final class BTXFractionationTowerRecipeCategory extends AbstractNiceRecipeCategory<BTXFractionationRecipe> {
    static final RecipeType<BTXFractionationRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "btx_fractionation_tower"), BTXFractionationRecipe.class);

    private static final Ingredient FUEL_PREVIEW =
            Ingredient.of(Items.COAL, Items.CHARCOAL, Items.BLAZE_ROD, Items.LAVA_BUCKET);

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
    public void setRecipe(IRecipeLayoutBuilder builder, BTXFractionationRecipe recipe, IFocusGroup focuses) {
        addFluid(builder, RecipeIngredientRole.INPUT,
                BTXFractionationTowerLayout.INPUT_SLOT_X, BTXFractionationTowerLayout.INPUT_SLOT_Y,
                recipe.input().fluidId(), recipe.input().amount());
        addItemIngredient(builder, RecipeIngredientRole.INPUT,
                BTXFractionationTowerLayout.FUEL_SLOT_X, BTXFractionationTowerLayout.FUEL_SLOT_Y,
                FUEL_PREVIEW);

        FluidRequirement benzene = outputFor(recipe, ModFluids.rl(ModFluids.BENZENE.name()));
        FluidRequirement toluene = outputFor(recipe, ModFluids.rl(ModFluids.TOLUENE.name()));
        FluidRequirement xylene = outputFor(recipe, ModFluids.rl(ModFluids.XYLENE.name()));

        if (benzene != null) {
            addFluid(builder, RecipeIngredientRole.OUTPUT,
                    BTXFractionationTowerLayout.BENZENE_SLOT_X, BTXFractionationTowerLayout.BENZENE_SLOT_Y,
                    benzene.fluidId(), benzene.amount());
        }
        if (toluene != null) {
            addFluid(builder, RecipeIngredientRole.OUTPUT,
                    BTXFractionationTowerLayout.TOLUENE_SLOT_X, BTXFractionationTowerLayout.TOLUENE_SLOT_Y,
                    toluene.fluidId(), toluene.amount());
        }
        if (xylene != null) {
            addFluid(builder, RecipeIngredientRole.OUTPUT,
                    BTXFractionationTowerLayout.XYLENE_SLOT_X, BTXFractionationTowerLayout.XYLENE_SLOT_Y,
                    xylene.fluidId(), xylene.amount());
        }
    }

    @Override
    public void draw(BTXFractionationRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        FluidRequirement benzene = outputFor(recipe, ModFluids.rl(ModFluids.BENZENE.name()));
        FluidRequirement toluene = outputFor(recipe, ModFluids.rl(ModFluids.TOLUENE.name()));
        FluidRequirement xylene = outputFor(recipe, ModFluids.rl(ModFluids.XYLENE.name()));

        MachineGuiRenderer.drawBTXFractionationTower(
                this,
                g,
                new MachineGuiRenderer.BTXFractionationTowerState(
                        MachineGuiRenderer.TankFill.preview(recipe.input().fluidId(), recipe.input().amount(), BTXFractionationTowerMenu.TANK_CAPACITY),
                        MachineGuiRenderer.TankFill.preview(benzene == null ? null : benzene.fluidId(), benzene == null ? 0 : benzene.amount(), BTXFractionationTowerMenu.TANK_CAPACITY),
                        MachineGuiRenderer.TankFill.preview(toluene == null ? null : toluene.fluidId(), toluene == null ? 0 : toluene.amount(), BTXFractionationTowerMenu.TANK_CAPACITY),
                        MachineGuiRenderer.TankFill.preview(xylene == null ? null : xylene.fluidId(), xylene == null ? 0 : xylene.amount(), BTXFractionationTowerMenu.TANK_CAPACITY),
                        BTXFractionationTowerLayout.PROGRESS_W,
                        BTXFractionationTowerLayout.FUEL_BAR_INNER_H,
                        0xFFE38D3F,
                        false,
                        false,
                        false,
                        false,
                        recipe.input().amount() > 0,
                        benzene != null && benzene.amount() > 0,
                        toluene != null && toluene.amount() > 0,
                        xylene != null && xylene.amount() > 0
                ),
                false
        );
        MachineGuiRenderer.drawBTXFractionationTowerLabels(this, g, net.minecraft.client.Minecraft.getInstance().font, getTitle(), jeiString("screen.mydrugs.jei.fuel_time", recipe.processTime()));
    }

    @Override
    public List<Component> getTooltipStrings(BTXFractionationRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        FluidRequirement benzene = outputFor(recipe, ModFluids.rl(ModFluids.BENZENE.name()));
        FluidRequirement toluene = outputFor(recipe, ModFluids.rl(ModFluids.TOLUENE.name()));
        FluidRequirement xylene = outputFor(recipe, ModFluids.rl(ModFluids.XYLENE.name()));

        if (isHoveringBox(BTXFractionationTowerLayout.DUMP_INPUT_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump input tank");
        } else if (isHoveringBox(BTXFractionationTowerLayout.DUMP_BENZENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump benzene tank");
        } else if (isHoveringBox(BTXFractionationTowerLayout.DUMP_TOLUENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump toluene tank");
        } else if (isHoveringBox(BTXFractionationTowerLayout.DUMP_XYLENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump xylene tank");
        } else if (isHoveringBox(BTXFractionationTowerLayout.INPUT_TANK_X, BTXFractionationTowerLayout.INPUT_TANK_Y, BTXFractionationTowerLayout.TANK_W, BTXFractionationTowerLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("BTX Mix input tank", recipe.input().fluidId(), recipe.input().amount(), BTXFractionationTowerMenu.TANK_CAPACITY);
        } else if (benzene != null && isHoveringBox(BTXFractionationTowerLayout.BENZENE_TANK_X, BTXFractionationTowerLayout.BENZENE_TANK_Y, BTXFractionationTowerLayout.TANK_W, BTXFractionationTowerLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Benzene output tank", benzene.fluidId(), benzene.amount(), BTXFractionationTowerMenu.TANK_CAPACITY);
        } else if (toluene != null && isHoveringBox(BTXFractionationTowerLayout.TOLUENE_TANK_X, BTXFractionationTowerLayout.TOLUENE_TANK_Y, BTXFractionationTowerLayout.TANK_W, BTXFractionationTowerLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Toluene output tank", toluene.fluidId(), toluene.amount(), BTXFractionationTowerMenu.TANK_CAPACITY);
        } else if (xylene != null && isHoveringBox(BTXFractionationTowerLayout.XYLENE_TANK_X, BTXFractionationTowerLayout.XYLENE_TANK_Y, BTXFractionationTowerLayout.TANK_W, BTXFractionationTowerLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Xylene output tank", xylene.fluidId(), xylene.amount(), BTXFractionationTowerMenu.TANK_CAPACITY);
        } else if (isHoveringBox(BTXFractionationTowerLayout.PROGRESS_X, BTXFractionationTowerLayout.PROGRESS_Y, BTXFractionationTowerLayout.PROGRESS_W, BTXFractionationTowerLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Fractionation progress", 0, recipe.processTime());
        } else if (isHoveringBox(BTXFractionationTowerLayout.FUEL_BAR_X, BTXFractionationTowerLayout.FUEL_BAR_Y, BTXFractionationTowerLayout.FUEL_BAR_W, BTXFractionationTowerLayout.FUEL_BAR_H, mouseX, mouseY)) {
            return amountTooltip("Fuel burn time", 0, recipe.processTime(), "ticks");
        }
        return List.of();
    }

    private static FluidRequirement outputFor(BTXFractionationRecipe recipe, ResourceLocation fluidId) {
        for (FluidRequirement output : recipe.outputs()) {
            if (fluidId.equals(output.fluidId())) {
                return output;
            }
        }
        return null;
    }
}
