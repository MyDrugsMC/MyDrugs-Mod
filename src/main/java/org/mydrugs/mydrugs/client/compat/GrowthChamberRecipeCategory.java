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
import org.mydrugs.mydrugs.menu.GrowthChamberMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.GrowthChamberLayout;
import org.mydrugs.mydrugs.recipes.growth_chamber.GrowthChamberRecipe;

import java.util.List;

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
                MachineGuiRenderer.growthChamberHeight(false)
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
        MachineGuiRenderer.drawGrowthChamber(
                this,
                g,
                new MachineGuiRenderer.GrowthChamberState(
                        tankPreviewFilledPixels(recipe.water(), GrowthChamberMenu.TANK_CAPACITY, GrowthChamberLayout.TANK_INNER_H),
                        GrowthChamberLayout.GROWTH_PROGRESS_W,
                        GrowthChamberLayout.MATURE_PROGRESS_W
                ),
                false
        );
        MachineGuiRenderer.drawGrowthChamberLabels(this, g, net.minecraft.client.Minecraft.getInstance().font, getTitle(), jeiString("screen.mydrugs.jei.growth_footer", recipe.water(), recipe.baseTicks()));
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

