package org.mydrugs.mydrugs.client.compat;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.AromaticExtractorBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.BiochemicalReactorBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.CentrifugeBlockEntity;
import org.mydrugs.mydrugs.menu.AromaticExtractorMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.AromaticExtractorLayout;
import org.mydrugs.mydrugs.menu.layout.CentrifugeLayout;
import org.mydrugs.mydrugs.recipes.aromatic_extractor.AromaticExtractorRecipe;
import org.mydrugs.mydrugs.recipes.centrifuge.CentrifugeRecipe;

import java.util.List;

public class AromaticExtractorRecipeCategory extends AbstractNiceRecipeCategory<AromaticExtractorRecipe>{
    static final RecipeType<AromaticExtractorRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "aromatic_extractor"), AromaticExtractorRecipe.class);

    AromaticExtractorRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.aromatic_extractor"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "AROMATIC_EXTRACTOR"),
                AromaticExtractorLayout.GUI_WIDTH,
                MachineGuiRenderer.aromaticExtractorHeight(false)
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AromaticExtractorRecipe recipe, IFocusGroup focuses) {
        addFluid(builder, RecipeIngredientRole.INPUT, AromaticExtractorLayout.INPUT_SLOT_X, AromaticExtractorLayout.INPUT_SLOT_Y, recipe.input().fluid(), recipe.input().amount());
        addFluid(builder, RecipeIngredientRole.INPUT, AromaticExtractorLayout.CATALYST_SLOT_X, AromaticExtractorLayout.CATALYST_SLOT_Y, recipe.catalyst().fluid(), recipe.catalyst().amount());

        addFluid(builder, RecipeIngredientRole.OUTPUT, AromaticExtractorLayout.OUTPUT_A_SLOT_X, AromaticExtractorLayout.OUTPUT_A_SLOT_Y, recipe.output1().fluid(), recipe.output1().amount());
        addFluid(builder, RecipeIngredientRole.OUTPUT, AromaticExtractorLayout.OUTPUT_B_SLOT_X, AromaticExtractorLayout.OUTPUT_B_SLOT_Y, recipe.output2().fluid(), recipe.output2().amount());
    }

    @Override
    public void draw(AromaticExtractorRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        MachineGuiRenderer.drawAromaticExtractor(
                this,
                g,
                new MachineGuiRenderer.AromaticExtractorState(
                        MachineGuiRenderer.TankFill.preview(recipe.input().fluid(), recipe.input().amount(), CentrifugeBlockEntity.FLUID_CAPACITY),
                        MachineGuiRenderer.TankFill.preview(recipe.output1().fluid(), recipe.output1().amount(), CentrifugeBlockEntity.FLUID_CAPACITY),
                        MachineGuiRenderer.TankFill.preview(recipe.output1().fluid(), recipe.output1().amount(), CentrifugeBlockEntity.FLUID_CAPACITY),
                        MachineGuiRenderer.TankFill.preview(recipe.output1().fluid(), recipe.output1().amount(), CentrifugeBlockEntity.FLUID_CAPACITY),
                        CentrifugeLayout.PROGRESS_W,
                        CentrifugeLayout.FUEL_BAR_INNER_H,
                        false,
                        false,
                        false,
                        false,
                        true,
                        true,
                        true,
                        true
                ),
                false
        );
    }

    @Override
    public List<Component> getTooltipStrings(AromaticExtractorRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(AromaticExtractorLayout.DUMP_INPUT_X, AromaticExtractorLayout.DUMP_BUTTON_Y, AromaticExtractorLayout.DUMP_BUTTON_SIZE, AromaticExtractorLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump input tank");
        } else if (isHoveringBox(AromaticExtractorLayout.DUMP_CATALYST_X, AromaticExtractorLayout.DUMP_BUTTON_Y, AromaticExtractorLayout.DUMP_BUTTON_SIZE, AromaticExtractorLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump catalyst tank");
        } else if (isHoveringBox(AromaticExtractorLayout.DUMP_OUTPUT_A_X, AromaticExtractorLayout.DUMP_BUTTON_Y, AromaticExtractorLayout.DUMP_BUTTON_SIZE, AromaticExtractorLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output A tank");
        } else if (isHoveringBox(AromaticExtractorLayout.DUMP_OUTPUT_B_X, AromaticExtractorLayout.DUMP_BUTTON_Y, AromaticExtractorLayout.DUMP_BUTTON_SIZE, AromaticExtractorLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output B tank");
        } else if (isHoveringBox(AromaticExtractorLayout.INPUT_TANK_X, AromaticExtractorLayout.INPUT_TANK_Y, AromaticExtractorLayout.TANK_W, AromaticExtractorLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip(
                    "Input Tank",
                    recipe.input().fluid(),
                    recipe.input().amount(),
                    AromaticExtractorBlockEntity.INPUT_CAPACITY
            );
        } else if (isHoveringBox(AromaticExtractorLayout.CATALYST_TANK_X, AromaticExtractorLayout.CATALYST_TANK_Y, AromaticExtractorLayout.TANK_W, AromaticExtractorLayout.TANK_H, mouseX, mouseY)) {
            return tooltip(
                    ui("Catalyst tank"),
                    ui("Catalyst fluid is not consumed"),
                    Component.translatable("screen.mydrugs.ui.minimum_mb", AromaticExtractorBlockEntity.MIN_CATALYST_AMOUNT),
                    Component.translatable("screen.mydrugs.ui.speed_percent", 100),
                    Component.literal(getFluidName(BuiltInRegistries.FLUID.getValue(recipe.catalyst().fluid()))),
                    Component.translatable("screen.mydrugs.ui.amount_unit", recipe.catalyst().amount(), AromaticExtractorMenu.CATALYST_TANK_CAPACITY, "mB")
            );
        } else if (isHoveringBox(AromaticExtractorLayout.OUTPUT_A_TANK_X, AromaticExtractorLayout.OUTPUT_A_TANK_Y, AromaticExtractorLayout.TANK_W, AromaticExtractorLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip(
                    "Output A Tank",
                    recipe.output1().fluid(),
                    recipe.output1().amount(),
                    AromaticExtractorBlockEntity.OUTPUT_CAPACITY
            );
        } else if (isHoveringBox(AromaticExtractorLayout.OUTPUT_B_TANK_X, AromaticExtractorLayout.OUTPUT_B_TANK_Y, AromaticExtractorLayout.TANK_W, AromaticExtractorLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip(
                    "Output B Tank",
                    recipe.output2().fluid(),
                    recipe.output2().amount(),
                    AromaticExtractorBlockEntity.OUTPUT_CAPACITY
            );
        } else if (isHoveringBox(AromaticExtractorLayout.PROGRESS_X, AromaticExtractorLayout.PROGRESS_Y, AromaticExtractorLayout.PROGRESS_W, AromaticExtractorLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Extraction progress", 0, recipe.baseTicks());
        } else if (isHoveringBox(AromaticExtractorLayout.FUEL_BAR_X, AromaticExtractorLayout.FUEL_BAR_Y, AromaticExtractorLayout.FUEL_BAR_W, AromaticExtractorLayout.FUEL_BAR_H, mouseX, mouseY)) {
            return amountTooltip("Fuel burn time", 0, recipe.baseTicks());
        }
        return List.of();
    }
}
