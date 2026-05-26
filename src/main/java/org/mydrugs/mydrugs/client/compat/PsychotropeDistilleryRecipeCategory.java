package org.mydrugs.mydrugs.client.compat;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.PsychotropeDistilleryLayout;
import org.mydrugs.mydrugs.recipes.psychotrope_distillery.PsychotropeDistilleryRecipe;

import java.util.List;

/**
 * JEI category for the Psychotrope Distillery. Shares its GUI geometry with the in-world screen
 * via {@link MachineGuiRenderer#drawPsychotropeDistillery} so any layout change updates both.
 */
final class PsychotropeDistilleryRecipeCategory extends AbstractNiceRecipeCategory<PsychotropeDistilleryRecipe> {
    static final RecipeType<PsychotropeDistilleryRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "psychotrope_distillery"), PsychotropeDistilleryRecipe.class);

    PsychotropeDistilleryRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.psychotrope_distillery"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "PSYCHOTROPE_DISTILLERY"),
                PsychotropeDistilleryLayout.GUI_WIDTH,
                MachineGuiRenderer.psychotropeDistilleryHeight(false)
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PsychotropeDistilleryRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT,
                PsychotropeDistilleryLayout.DRUG_SLOT_X, PsychotropeDistilleryLayout.DRUG_SLOT_Y,
                recipe.drugInput());
        addItemIngredient(builder, RecipeIngredientRole.INPUT,
                PsychotropeDistilleryLayout.REAGENT_SLOT_X, PsychotropeDistilleryLayout.REAGENT_SLOT_Y,
                recipe.reagentInput());

        addItemStack(builder, RecipeIngredientRole.OUTPUT,
                PsychotropeDistilleryLayout.EXTRACT_SLOT_X, PsychotropeDistilleryLayout.EXTRACT_SLOT_Y,
                recipe.result());
        recipe.residueResult().ifPresent(residue ->
                addItemStack(builder, RecipeIngredientRole.OUTPUT,
                        PsychotropeDistilleryLayout.RESIDUE_SLOT_X, PsychotropeDistilleryLayout.RESIDUE_SLOT_Y,
                        residue));
    }

    @Override
    public void draw(PsychotropeDistilleryRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        MachineGuiRenderer.drawPsychotropeDistillery(
                this,
                g,
                MachineGuiRenderer.PsychotropeDistilleryState.recipe(),
                false
        );
        String footer = jeiString("screen.mydrugs.jei.time_ticks", recipe.baseTicks());
        if (recipe.hasResidue()) {
            footer += jeiString("screen.mydrugs.jei.residue_every", recipe.residueEvery());
        }
        MachineGuiRenderer.drawPsychotropeDistilleryLabels(this, g, Minecraft.getInstance().font, getTitle(), footer);
    }

    @Override
    public List<Component> getTooltipStrings(PsychotropeDistilleryRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(PsychotropeDistilleryLayout.PROGRESS_X, PsychotropeDistilleryLayout.PROGRESS_Y,
                PsychotropeDistilleryLayout.PROGRESS_W, PsychotropeDistilleryLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Distillation progress", 0, recipe.baseTicks(), "ticks");
        }
        if (isHoveringBox(PsychotropeDistilleryLayout.BURN_X, PsychotropeDistilleryLayout.BURN_Y,
                PsychotropeDistilleryLayout.BURN_W, PsychotropeDistilleryLayout.BURN_H, mouseX, mouseY)) {
            return tooltip("Fuel burn", "Accepts furnace fuels");
        }
        if (isHoveringBox(PsychotropeDistilleryLayout.FUEL_SLOT_X, PsychotropeDistilleryLayout.FUEL_SLOT_Y,
                18, 18, mouseX, mouseY)) {
            return tooltip("Fuel slot", "Accepts furnace fuels");
        }
        if (recipe.hasResidue()
                && isHoveringBox(PsychotropeDistilleryLayout.RESIDUE_SLOT_X, PsychotropeDistilleryLayout.RESIDUE_SLOT_Y,
                        18, 18, mouseX, mouseY)) {
            return tooltip(
                    Component.translatable("screen.mydrugs.jei.residue"),
                    Component.translatable("screen.mydrugs.jei.residue_every_tooltip", recipe.residueEvery())
            );
        }
        return List.of();
    }
}
