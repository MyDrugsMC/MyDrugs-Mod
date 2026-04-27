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
import org.mydrugs.mydrugs.menu.GasifierMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.GasifierLayout;
import org.mydrugs.mydrugs.recipes.gasifier.GasifierRecipe;

import java.util.List;

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
                MachineGuiRenderer.gasifierHeight(false)
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GasifierRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, GasifierLayout.INPUT_SLOT_X, GasifierLayout.INPUT_SLOT_Y, recipe.input());
        addGas(builder, RecipeIngredientRole.OUTPUT, GasifierLayout.EXPORT_SLOT_X, GasifierLayout.EXPORT_SLOT_Y, recipe.gasOutput(), recipe.gasAmount());
    }

    @Override
    public void draw(GasifierRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        MachineGuiRenderer.drawGasifier(
                this,
                g,
                new MachineGuiRenderer.GasifierState(
                        GasifierLayout.FUEL_BAR_INNER_H,
                        GasifierLayout.PROGRESS_W,
                        MachineGuiRenderer.TankFill.previewGas(recipe.gasOutput(), recipe.gasAmount(), GasifierMenu.TANK_CAPACITY)
                ),
                false
        );
        MachineGuiRenderer.drawGasifierLabels(this, g, net.minecraft.client.Minecraft.getInstance().font, getTitle(), null, 0, "Time: " + recipe.processTime() + "t");
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

