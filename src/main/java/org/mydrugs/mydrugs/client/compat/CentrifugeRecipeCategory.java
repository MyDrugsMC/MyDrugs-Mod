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
import org.mydrugs.mydrugs.blocks.entity.CentrifugeBlockEntity;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.CentrifugeLayout;
import org.mydrugs.mydrugs.recipes.centrifuge.CentrifugeRecipe;

import java.util.List;

final class CentrifugeRecipeCategory extends AbstractNiceRecipeCategory<CentrifugeRecipe> {
    static final RecipeType<CentrifugeRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "centrifuge"), CentrifugeRecipe.class);

    CentrifugeRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.centrifuge"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "CENTRIFUGE"),
                CentrifugeLayout.GUI_WIDTH,
                MachineGuiRenderer.centrifugeHeight(false)
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CentrifugeRecipe recipe, IFocusGroup focuses) {
        addFluid(builder, RecipeIngredientRole.INPUT, CentrifugeLayout.INPUT_SLOT_X, CentrifugeLayout.INPUT_SLOT_Y, recipe.input().fluid(), recipe.input().amount());

        addFluid(builder, RecipeIngredientRole.OUTPUT, CentrifugeLayout.OUTPUT_A_SLOT_X, CentrifugeLayout.OUTPUT_A_SLOT_Y, recipe.output1().fluid(), recipe.output1().amount());
        recipe.output2().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, CentrifugeLayout.OUTPUT_B_SLOT_X, CentrifugeLayout.OUTPUT_B_SLOT_Y, output.fluid(), output.amount())
        );
    }

    @Override
    public void draw(CentrifugeRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        MachineGuiRenderer.drawCentrifuge(
                this,
                g,
                new MachineGuiRenderer.CentrifugeState(
                        MachineGuiRenderer.TankFill.preview(recipe.input().fluid(), recipe.input().amount(), CentrifugeBlockEntity.FLUID_CAPACITY),
                        MachineGuiRenderer.TankFill.preview(recipe.output1().fluid(), recipe.output1().amount(), CentrifugeBlockEntity.FLUID_CAPACITY),
                        recipe.output2()
                                .map(output -> MachineGuiRenderer.TankFill.preview(output.fluid(), output.amount(), CentrifugeBlockEntity.FLUID_CAPACITY))
                                .orElseGet(() -> MachineGuiRenderer.TankFill.liveColor(0, 0)),
                        CentrifugeLayout.PROGRESS_W,
                        CentrifugeLayout.FUEL_BAR_INNER_H,
                        0xFFE38D3F,
                        false,
                        false,
                        false,
                        true,
                        true,
                        recipe.output2().isPresent()
                ),
                false
        );
        MachineGuiRenderer.drawCentrifugeLabels(this, g, net.minecraft.client.Minecraft.getInstance().font, getTitle());
    }

    @Override
    public List<Component> getTooltipStrings(CentrifugeRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(CentrifugeLayout.DUMP_INPUT_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, CentrifugeLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump input tank");
        } else if (isHoveringBox(CentrifugeLayout.DUMP_OUTPUT_A_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, CentrifugeLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank A");
        } else if (isHoveringBox(CentrifugeLayout.DUMP_OUTPUT_B_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, CentrifugeLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank B");
        } else if (isHoveringBox(CentrifugeLayout.INPUT_TANK_X, CentrifugeLayout.INPUT_TANK_Y, CentrifugeLayout.TANK_W, CentrifugeLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Input tank", recipe.input().fluid(), recipe.input().amount(), CentrifugeBlockEntity.FLUID_CAPACITY);
        } else if (isHoveringBox(CentrifugeLayout.OUTPUT_A_TANK_X, CentrifugeLayout.OUTPUT_A_TANK_Y, CentrifugeLayout.TANK_W, CentrifugeLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Output tank A", recipe.output1().fluid(), recipe.output1().amount(), CentrifugeBlockEntity.FLUID_CAPACITY);
        } else if (isHoveringBox(CentrifugeLayout.OUTPUT_B_TANK_X, CentrifugeLayout.OUTPUT_B_TANK_Y, CentrifugeLayout.TANK_W, CentrifugeLayout.TANK_H, mouseX, mouseY)) {
            return recipe.output2()
                    .map(output -> fluidTankTooltip("Output tank B", output.fluid(), output.amount(), CentrifugeBlockEntity.FLUID_CAPACITY))
                    .orElseGet(() -> fluidTankTooltip("Output tank B", Fluids.EMPTY, 0, CentrifugeBlockEntity.FLUID_CAPACITY));
        } else if (isHoveringBox(CentrifugeLayout.PROGRESS_X, CentrifugeLayout.PROGRESS_Y, CentrifugeLayout.PROGRESS_W, CentrifugeLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Centrifuge progress", 0, recipe.baseTicks());
        } else if (isHoveringBox(CentrifugeLayout.FUEL_BAR_X, CentrifugeLayout.FUEL_BAR_Y, CentrifugeLayout.FUEL_BAR_W, CentrifugeLayout.FUEL_BAR_H, mouseX, mouseY)) {
            return amountTooltip("Fuel burn time", 0, recipe.baseTicks(), "ticks");
        }
        return List.of();
    }
}

