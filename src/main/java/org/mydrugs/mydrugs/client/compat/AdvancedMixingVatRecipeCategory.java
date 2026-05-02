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
import org.mydrugs.mydrugs.blocks.entity.AdvancedMixingVatBlockEntity;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.AdvancedMixingVatLayout;
import org.mydrugs.mydrugs.recipes.advanced_mixing_vat.AdvancedMixingVatRecipe;

import java.util.List;

final class AdvancedMixingVatRecipeCategory extends AbstractNiceRecipeCategory<AdvancedMixingVatRecipe> {
    static final RecipeType<AdvancedMixingVatRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "advanced_mixing_vat"), AdvancedMixingVatRecipe.class);

    AdvancedMixingVatRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.advanced_mixing_vat"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "ADVANCED_MIXING_VAT"),
                AdvancedMixingVatLayout.GUI_WIDTH,
                MachineGuiRenderer.advancedMixingVatHeight(false)
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AdvancedMixingVatRecipe recipe, IFocusGroup focuses) {
        List<?> itemInputs = recipe.itemInputs();
        int[] itemX = {
                AdvancedMixingVatLayout.ITEM_0_X,
                AdvancedMixingVatLayout.ITEM_1_X,
                AdvancedMixingVatLayout.ITEM_2_X,
                AdvancedMixingVatLayout.ITEM_3_X
        };
        int[] itemY = {
                AdvancedMixingVatLayout.ITEM_0_Y,
                AdvancedMixingVatLayout.ITEM_1_Y,
                AdvancedMixingVatLayout.ITEM_2_Y,
                AdvancedMixingVatLayout.ITEM_3_Y
        };

        for (int i = 0; i < Math.min(itemInputs.size(), itemX.length); i++) {
            addItemIngredient(builder, RecipeIngredientRole.INPUT, itemX[i], itemY[i], JeiCompatUtil.ingredientOf(itemInputs.get(i)));
        }

        List<?> fluidInputs = recipe.fluidInputs();
        int[] tankSlotX = {
                AdvancedMixingVatLayout.TANK_A_SLOT_X,
                AdvancedMixingVatLayout.TANK_B_SLOT_X,
                AdvancedMixingVatLayout.TANK_C_SLOT_X
        };
        for (int i = 0; i < Math.min(fluidInputs.size(), tankSlotX.length); i++) {
            Object fluid = fluidInputs.get(i);
            addFluid(
                    builder,
                    RecipeIngredientRole.INPUT,
                    tankSlotX[i],
                    AdvancedMixingVatLayout.TANK_SLOT_Y,
                    JeiCompatUtil.idOf(fluid, "fluid", "fluidId"),
                    JeiCompatUtil.intOf(fluid, "amount")
            );
        }

        if (recipe.gasInput() != null) {
            addGas(
                    builder,
                    RecipeIngredientRole.INPUT,
                    AdvancedMixingVatLayout.GAS_SLOT_X,
                    AdvancedMixingVatLayout.TANK_SLOT_Y,
                    JeiCompatUtil.idOf(recipe.gasInput(), "gas", "gasId"),
                    recipe.gasInput().amount()
            );
        }

        addFluid(
                builder,
                RecipeIngredientRole.OUTPUT,
                AdvancedMixingVatLayout.OUTPUT_SLOT_X,
                AdvancedMixingVatLayout.TANK_SLOT_Y,
                JeiCompatUtil.idOf(recipe.output(), "fluid", "fluidId"),
                JeiCompatUtil.intOf(recipe.output(), "amount")
        );
    }

    @Override
    public void draw(AdvancedMixingVatRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        List<?> fluidInputs = recipe.fluidInputs();
        MachineGuiRenderer.TankFill tankA = advancedVatTank(fluidInputs, 0);
        MachineGuiRenderer.TankFill tankB = advancedVatTank(fluidInputs, 1);
        MachineGuiRenderer.TankFill tankC = advancedVatTank(fluidInputs, 2);
        MachineGuiRenderer.TankFill gas = recipe.gasInput() == null
                ? MachineGuiRenderer.TankFill.liveColor(0, 0)
                : MachineGuiRenderer.TankFill.previewGas(JeiCompatUtil.idOf(recipe.gasInput(), "gas", "gasId"), recipe.gasInput().amount(), AdvancedMixingVatBlockEntity.GAS_TANK_CAPACITY);

        List<?> itemInputs = recipe.itemInputs();
        int[] itemCounts = new int[Math.min(itemInputs.size(), 4)];
        for (int i = 0; i < itemCounts.length; i++) {
            itemCounts[i] = JeiCompatUtil.countOf(itemInputs.get(i));
        }

        MachineGuiRenderer.drawAdvancedMixingVat(
                this,
                g,
                new MachineGuiRenderer.AdvancedMixingVatState(
                        tankA,
                        tankB,
                        tankC,
                        gas,
                        MachineGuiRenderer.TankFill.preview(
                                JeiCompatUtil.idOf(recipe.output(), "fluid", "fluidId"),
                                JeiCompatUtil.intOf(recipe.output(), "amount"),
                                AdvancedMixingVatBlockEntity.OUTPUT_TANK_CAPACITY
                        ),
                        AdvancedMixingVatLayout.PROGRESS_W,
                        itemCounts
                ),
                false
        );
        MachineGuiRenderer.drawAdvancedMixingVatLabels(this, g, net.minecraft.client.Minecraft.getInstance().font, getTitle(), jeiString("screen.mydrugs.jei.no_heat_time", recipe.processingTime()));
    }

    private MachineGuiRenderer.TankFill advancedVatTank(List<?> fluidInputs, int index) {
        if (index >= fluidInputs.size()) {
            return MachineGuiRenderer.TankFill.liveColor(0, 0);
        }
        Object fluid = fluidInputs.get(index);
        return MachineGuiRenderer.TankFill.preview(
                JeiCompatUtil.idOf(fluid, "fluid", "fluidId"),
                JeiCompatUtil.intOf(fluid, "amount"),
                AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY
        );
    }

    @Override
    public List<Component> getTooltipStrings(AdvancedMixingVatRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        List<?> fluidInputs = recipe.fluidInputs();

        if (isHoveringBox(AdvancedMixingVatLayout.TANK_A_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H, mouseX, mouseY)) {
            return advancedVatFluidInputTooltip("Fluid Input A", fluidInputs, 0);
        } else if (isHoveringBox(AdvancedMixingVatLayout.TANK_B_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H, mouseX, mouseY)) {
            return advancedVatFluidInputTooltip("Fluid Input B", fluidInputs, 1);
        } else if (isHoveringBox(AdvancedMixingVatLayout.TANK_C_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H, mouseX, mouseY)) {
            return advancedVatFluidInputTooltip("Fluid Input C", fluidInputs, 2);
        } else if (isHoveringBox(AdvancedMixingVatLayout.GAS_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H, mouseX, mouseY)) {
            long amount = recipe.gasInput() == null ? 0L : recipe.gasInput().amount();
            return amountTooltip("Gas Input", amount, AdvancedMixingVatBlockEntity.GAS_TANK_CAPACITY, "units");
        } else if (isHoveringBox(AdvancedMixingVatLayout.OUTPUT_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H, mouseX, mouseY)) {
            return fluidTankTooltip(
                    "Fluid Output",
                    JeiCompatUtil.idOf(recipe.output(), "fluid", "fluidId"),
                    JeiCompatUtil.intOf(recipe.output(), "amount"),
                    AdvancedMixingVatBlockEntity.OUTPUT_TANK_CAPACITY
            );
        } else if (isHoveringBox(AdvancedMixingVatLayout.PROGRESS_X, AdvancedMixingVatLayout.PROGRESS_Y, AdvancedMixingVatLayout.PROGRESS_W, AdvancedMixingVatLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Mixing Progress", 0, recipe.processingTime());
        }

        return List.of();
    }

    private List<Component> advancedVatFluidInputTooltip(String title, List<?> fluidInputs, int index) {
        if (index >= fluidInputs.size()) {
            return fluidTankTooltip(title, Fluids.EMPTY, 0, AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY);
        }
        Object fluid = fluidInputs.get(index);
        return fluidTankTooltip(
                title,
                JeiCompatUtil.idOf(fluid, "fluid", "fluidId"),
                JeiCompatUtil.intOf(fluid, "amount"),
                AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY
        );
    }
}

