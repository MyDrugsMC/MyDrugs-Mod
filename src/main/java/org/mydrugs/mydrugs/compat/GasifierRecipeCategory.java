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

