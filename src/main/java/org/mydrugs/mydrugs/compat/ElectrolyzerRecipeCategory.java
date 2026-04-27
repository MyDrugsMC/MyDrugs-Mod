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
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.ElectrolyzerBlockEntity;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.ElectrolyzerLayout;
import org.mydrugs.mydrugs.recipes.electrolyzer.ElectrolyzerRecipe;

import java.util.List;

final class ElectrolyzerRecipeCategory extends AbstractNiceRecipeCategory<ElectrolyzerRecipe> {
    static final RecipeType<ElectrolyzerRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "electrolyzer"), ElectrolyzerRecipe.class);

    ElectrolyzerRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.electrolyzer"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "ELECTROLYZER"),
                ElectrolyzerLayout.GUI_WIDTH,
                MachineGuiRenderer.electrolyzerHeight(false)
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ElectrolyzerRecipe recipe, IFocusGroup focuses) {
        addFluid(builder, RecipeIngredientRole.INPUT, ElectrolyzerLayout.INPUT_SLOT_X, ElectrolyzerLayout.INPUT_SLOT_Y, recipe.inputFluid().fluid(), recipe.inputFluid().amount());

        recipe.outputFluid1().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, ElectrolyzerLayout.OUTPUT_1_SLOT_X, ElectrolyzerLayout.OUTPUT_1_SLOT_Y, output.fluid(), output.amount())
        );
        recipe.outputGas1().ifPresent(output ->
                addGas(builder, RecipeIngredientRole.OUTPUT, ElectrolyzerLayout.OUTPUT_1_SLOT_X, ElectrolyzerLayout.OUTPUT_1_SLOT_Y, output.gas(), output.amount())
        );

        recipe.outputFluid2().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, ElectrolyzerLayout.OUTPUT_2_SLOT_X, ElectrolyzerLayout.OUTPUT_2_SLOT_Y, output.fluid(), output.amount())
        );
        recipe.outputGas2().ifPresent(output ->
                addGas(builder, RecipeIngredientRole.OUTPUT, ElectrolyzerLayout.OUTPUT_2_SLOT_X, ElectrolyzerLayout.OUTPUT_2_SLOT_Y, output.gas(), output.amount())
        );

        recipe.outputFluid3().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, ElectrolyzerLayout.OUTPUT_3_SLOT_X, ElectrolyzerLayout.OUTPUT_3_SLOT_Y, output.fluid(), output.amount())
        );
        recipe.outputGas3().ifPresent(output ->
                addGas(builder, RecipeIngredientRole.OUTPUT, ElectrolyzerLayout.OUTPUT_3_SLOT_X, ElectrolyzerLayout.OUTPUT_3_SLOT_Y, output.gas(), output.amount())
        );
    }

    @Override
    public void draw(ElectrolyzerRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        MachineGuiRenderer.drawElectrolyzer(
                this,
                g,
                new MachineGuiRenderer.ElectrolyzerState(
                        MachineGuiRenderer.TankFill.preview(recipe.inputFluid().fluid(), recipe.inputFluid().amount(), ElectrolyzerBlockEntity.FLUID_CAPACITY),
                        electrolyzerOutput(recipe.outputFluid1().orElse(null), recipe.outputGas1().orElse(null)),
                        electrolyzerOutput(recipe.outputFluid2().orElse(null), recipe.outputGas2().orElse(null)),
                        electrolyzerOutput(recipe.outputFluid3().orElse(null), recipe.outputGas3().orElse(null)),
                        ElectrolyzerLayout.PROGRESS_W,
                        ElectrolyzerLayout.FUEL_BAR_INNER_H,
                        0xFFE38D3F,
                        false,
                        false,
                        false,
                        false,
                        true,
                        recipe.outputFluid1().isPresent() || recipe.outputGas1().isPresent(),
                        recipe.outputFluid2().isPresent() || recipe.outputGas2().isPresent(),
                        recipe.outputFluid3().isPresent() || recipe.outputGas3().isPresent()
                ),
                false
        );
        MachineGuiRenderer.drawElectrolyzerLabels(this, g, net.minecraft.client.Minecraft.getInstance().font, getTitle());
    }

    private MachineGuiRenderer.TankFill electrolyzerOutput(Object fluidOutput, Object gasOutput) {
        if (fluidOutput != null) {
            return MachineGuiRenderer.TankFill.preview(JeiCompatUtil.idOf(fluidOutput, "fluid", "fluidId"), JeiCompatUtil.intOf(fluidOutput, "amount"), ElectrolyzerBlockEntity.FLUID_CAPACITY);
        }
        if (gasOutput != null) {
            return MachineGuiRenderer.TankFill.previewGas(JeiCompatUtil.idOf(gasOutput, "gas", "gasId"), JeiCompatUtil.longOf(gasOutput, "amount"), ElectrolyzerBlockEntity.GAS_CAPACITY);
        }
        return MachineGuiRenderer.TankFill.liveColor(0, 0);
    }

    @Override
    public List<Component> getTooltipStrings(ElectrolyzerRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(ElectrolyzerLayout.DUMP_INPUT_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, ElectrolyzerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump input tank");
        } else if (isHoveringBox(ElectrolyzerLayout.DUMP_OUTPUT_1_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, ElectrolyzerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank 1");
        } else if (isHoveringBox(ElectrolyzerLayout.DUMP_OUTPUT_2_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, ElectrolyzerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank 2");
        } else if (isHoveringBox(ElectrolyzerLayout.DUMP_OUTPUT_3_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, ElectrolyzerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank 3");
        } else if (isHoveringBox(ElectrolyzerLayout.INPUT_TANK_X, ElectrolyzerLayout.INPUT_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Input tank", recipe.inputFluid().fluid(), recipe.inputFluid().amount(), ElectrolyzerBlockEntity.FLUID_CAPACITY);
        } else if (isHoveringBox(ElectrolyzerLayout.OUTPUT_1_TANK_X, ElectrolyzerLayout.OUTPUT_1_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, mouseX, mouseY)) {
            return electrolyzerOutputTooltip(1, recipe.outputFluid1().orElse(null), recipe.outputGas1().orElse(null));
        } else if (isHoveringBox(ElectrolyzerLayout.OUTPUT_2_TANK_X, ElectrolyzerLayout.OUTPUT_2_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, mouseX, mouseY)) {
            return electrolyzerOutputTooltip(2, recipe.outputFluid2().orElse(null), recipe.outputGas2().orElse(null));
        } else if (isHoveringBox(ElectrolyzerLayout.OUTPUT_3_TANK_X, ElectrolyzerLayout.OUTPUT_3_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, mouseX, mouseY)) {
            return electrolyzerOutputTooltip(3, recipe.outputFluid3().orElse(null), recipe.outputGas3().orElse(null));
        } else if (isHoveringBox(ElectrolyzerLayout.PROGRESS_X, ElectrolyzerLayout.PROGRESS_Y, ElectrolyzerLayout.PROGRESS_W, ElectrolyzerLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Electrolyzer progress", 0, recipe.baseTicks());
        } else if (isHoveringBox(ElectrolyzerLayout.FUEL_BAR_X, ElectrolyzerLayout.FUEL_BAR_Y, ElectrolyzerLayout.FUEL_BAR_W, ElectrolyzerLayout.FUEL_BAR_H, mouseX, mouseY)) {
            return amountTooltip("Fuel burn time", 0, recipe.baseTicks(), "ticks");
        }
        return List.of();
    }

    private List<Component> electrolyzerOutputTooltip(int index, Object fluidOutput, Object gasOutput) {
        if (gasOutput != null) {
            return gasTankTooltip(
                    "Output gas tank " + index,
                    JeiCompatUtil.idOf(gasOutput, "gas", "gasId"),
                    JeiCompatUtil.longOf(gasOutput, "amount"),
                    ElectrolyzerBlockEntity.GAS_CAPACITY
            );
        }
        if (fluidOutput != null) {
            return fluidTankTooltip(
                    "Output fluid tank " + index,
                    JeiCompatUtil.idOf(fluidOutput, "fluid", "fluidId"),
                    JeiCompatUtil.intOf(fluidOutput, "amount"),
                    ElectrolyzerBlockEntity.FLUID_CAPACITY
            );
        }
        return fluidTankTooltip("Output fluid tank " + index, Fluids.EMPTY, 0, ElectrolyzerBlockEntity.FLUID_CAPACITY);
    }
}

