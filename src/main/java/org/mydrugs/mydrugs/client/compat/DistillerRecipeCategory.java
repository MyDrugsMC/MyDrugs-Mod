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
import org.mydrugs.mydrugs.menu.DistillerMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.DistillerLayout;
import org.mydrugs.mydrugs.recipes.distiller.DistillerRecipe;

import java.util.List;

final class DistillerRecipeCategory extends AbstractNiceRecipeCategory<DistillerRecipe> {
    static final RecipeType<DistillerRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "distiller"), DistillerRecipe.class);

    DistillerRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.distiller"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "DISTILLER"),
                DistillerLayout.GUI_WIDTH,
                MachineGuiRenderer.distillerHeight(false)
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DistillerRecipe recipe, IFocusGroup focuses) {
        addFluid(builder, RecipeIngredientRole.INPUT, DistillerLayout.INPUT_SLOT_X, DistillerLayout.INPUT_SLOT_Y, recipe.input().fluid(), recipe.input().amount());

        addFluid(builder, RecipeIngredientRole.OUTPUT, DistillerLayout.OUTPUT_A_SLOT_X, DistillerLayout.OUTPUT_A_SLOT_Y, recipe.output1().fluid(), recipe.output1().amount());
        recipe.output2().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, DistillerLayout.OUTPUT_B_SLOT_X, DistillerLayout.OUTPUT_B_SLOT_Y, output.fluid(), output.amount())
        );
    }

    @Override
    public void draw(DistillerRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        MachineGuiRenderer.drawDistiller(
                this,
                g,
                new MachineGuiRenderer.DistillerState(
                        MachineGuiRenderer.TankFill.preview(recipe.input().fluid(), recipe.input().amount(), DistillerMenu.TANK_CAPACITY),
                        MachineGuiRenderer.TankFill.preview(recipe.output1().fluid(), recipe.output1().amount(), DistillerMenu.TANK_CAPACITY),
                        recipe.output2()
                                .map(output -> MachineGuiRenderer.TankFill.preview(output.fluid(), output.amount(), DistillerMenu.TANK_CAPACITY))
                                .orElseGet(() -> MachineGuiRenderer.TankFill.liveColor(0, 0)),
                        DistillerLayout.PROGRESS_W,
                        false,
                        false,
                        false,
                        true,
                        true,
                        recipe.output2().isPresent(),
                        false,
                        true,
                        false,
                        null,
                        null
                ),
                false
        );
        MachineGuiRenderer.drawDistillerLabels(this, g, net.minecraft.client.Minecraft.getInstance().font, getTitle());
    }

    @Override
    public List<Component> getTooltipStrings(DistillerRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(DistillerLayout.DUMP_INPUT_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, DistillerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump input tank");
        } else if (isHoveringBox(DistillerLayout.DUMP_OUTPUT_A_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, DistillerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank A");
        } else if (isHoveringBox(DistillerLayout.DUMP_OUTPUT_B_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, DistillerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Dump output tank B");
        } else if (isHoveringBox(DistillerLayout.INPUT_TANK_X, DistillerLayout.INPUT_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Input tank", recipe.input().fluid(), recipe.input().amount(), DistillerMenu.TANK_CAPACITY);
        } else if (isHoveringBox(DistillerLayout.OUTPUT_A_TANK_X, DistillerLayout.OUTPUT_A_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip("Output tank A", recipe.output1().fluid(), recipe.output1().amount(), DistillerMenu.TANK_CAPACITY);
        } else if (isHoveringBox(DistillerLayout.OUTPUT_B_TANK_X, DistillerLayout.OUTPUT_B_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, mouseX, mouseY)) {
            return recipe.output2()
                    .map(output -> fluidTankTooltip("Output tank B", output.fluid(), output.amount(), DistillerMenu.TANK_CAPACITY))
                    .orElseGet(() -> fluidTankTooltip("Output tank B", Fluids.EMPTY, 0, DistillerMenu.TANK_CAPACITY));
        } else if (isHoveringBox(DistillerLayout.PROGRESS_X, DistillerLayout.PROGRESS_Y, DistillerLayout.PROGRESS_W, DistillerLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Distillation progress", 0, recipe.baseTicks());
        } else if (isHoveringBox(DistillerLayout.RUN_BUTTON_X, DistillerLayout.RUN_BUTTON_Y, DistillerLayout.RUN_BUTTON_SIZE, DistillerLayout.RUN_BUTTON_SIZE, mouseX, mouseY)) {
            return tooltip("Run distiller", "More than 5 CPS increases speed");
        }
        return List.of();
    }

    private void drawReactor(GuiGraphics graphics, int localX, int localY, boolean hovered, boolean working, boolean boosted) {
        int cx = localX + DistillerLayout.RUN_BUTTON_SIZE / 2;
        int cy = localY + DistillerLayout.RUN_BUTTON_SIZE / 2;

        if (hovered) {
            graphics.fill(localX + 2, localY + 2, localX + DistillerLayout.RUN_BUTTON_SIZE - 2, localY + DistillerLayout.RUN_BUTTON_SIZE - 2, 0x16FFFFFF);
        }

        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS + 2, 0xFF818793);
        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS, 0xFF20242B);
        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS - 3, 0xFF9FA7B4);
        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS - 5, 0xFF3E4652);

        int coreColor = boosted ? 0xFF6FD6FF : working ? 0xFFE8E8E8 : 0xFF90959E;

        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_INNER_RADIUS, 0xFF2D333B);
        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_CORE_RADIUS, coreColor);

        if (boosted) {
            drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_CORE_RADIUS + 3, 0x336FD6FF);
        }
    }
}

