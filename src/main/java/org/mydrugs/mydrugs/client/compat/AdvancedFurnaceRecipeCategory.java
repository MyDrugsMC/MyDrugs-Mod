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
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.AdvancedFurnaceBlockEntity;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.AdvancedFurnaceLayout;
import org.mydrugs.mydrugs.menu.layout.StandardTankLayout;
import org.mydrugs.mydrugs.recipes.advanced_furnace.AdvancedFurnaceRecipe;

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
                MachineGuiRenderer.advancedFurnaceHeight(false)
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
        String footer = jeiString("screen.mydrugs.jei.time_ticks", recipe.cookTime());
        if (recipe.fluidOutput().isPresent()) {
            footer += jeiString("screen.mydrugs.jei.fluid_suffix", recipe.fluidAmount(), JeiCompatUtil.shortId(recipe.fluidOutput().get()));
        }

        MachineGuiRenderer.drawAdvancedFurnace(
                this,
                g,
                MachineGuiRenderer.AdvancedFurnaceState.recipe(recipe.fluidOutput(), recipe.fluidAmount()),
                false
        );
        MachineGuiRenderer.drawAdvancedFurnaceRecipeLabels(this, g, getTitle(), footer);
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

