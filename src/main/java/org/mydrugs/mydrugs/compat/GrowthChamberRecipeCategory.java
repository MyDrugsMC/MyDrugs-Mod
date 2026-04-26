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

