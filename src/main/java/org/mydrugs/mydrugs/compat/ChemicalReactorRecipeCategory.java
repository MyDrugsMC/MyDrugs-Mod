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
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.ChemicalReactorBlockEntity;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.ChemicalReactorLayout;
import org.mydrugs.mydrugs.recipes.chemical_reactor.ChemicalReactorRecipe;

import java.util.List;

final class ChemicalReactorRecipeCategory extends AbstractNiceRecipeCategory<ChemicalReactorRecipe> {
    static final RecipeType<ChemicalReactorRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "chemical_reactor"), ChemicalReactorRecipe.class);

    ChemicalReactorRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.chemical_reactor"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "CHEMICAL_REACTOR"),
                ChemicalReactorLayout.GUI_WIDTH,
                MachineGuiRenderer.chemicalReactorHeight(false)
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ChemicalReactorRecipe recipe, IFocusGroup focuses) {
        addGas(builder, RecipeIngredientRole.INPUT, ChemicalReactorLayout.PRIMARY_GAS_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y, JeiCompatUtil.idOf(recipe.primaryGas(), "gas", "gasId"), recipe.primaryGas().amount());

        recipe.secondaryGas().ifPresent(gas ->
                addGas(builder, RecipeIngredientRole.INPUT, ChemicalReactorLayout.SECONDARY_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y, JeiCompatUtil.idOf(gas, "gas", "gasId"), gas.amount())
        );

        recipe.secondaryFluid().ifPresent(fluidReq ->
                addFluid(builder, RecipeIngredientRole.INPUT, ChemicalReactorLayout.SECONDARY_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y_2, JeiCompatUtil.idOf(fluidReq, "fluid", "fluidId"), JeiCompatUtil.intOf(fluidReq, "amount"))
        );

        ResourceLocation outputId = recipe.outputId();
        ItemStack outputStack = JeiCompatUtil.stack(outputId, recipe.outputAmount());
        var outputFluid = JeiCompatUtil.fluid(outputId);

        if (!outputStack.isEmpty()) {
            addItemStack(builder, RecipeIngredientRole.OUTPUT, ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y_2, outputStack);
        } else if (outputFluid != Fluids.EMPTY) {
            addFluid(builder, RecipeIngredientRole.OUTPUT, ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y_2, outputFluid, recipe.outputAmount());
        } else {
            addGas(builder, RecipeIngredientRole.OUTPUT, ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y, outputId, recipe.outputAmount());
        }
    }

    @Override
    public void draw(ChemicalReactorRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        ResourceLocation outputId = recipe.outputId();
        var outputFluid = JeiCompatUtil.fluid(outputId);
        MachineGuiRenderer.TankFill outputTank = outputFluid != Fluids.EMPTY
                ? MachineGuiRenderer.TankFill.preview(outputId, recipe.outputAmount(), ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY)
                : JeiCompatUtil.stack(outputId, recipe.outputAmount()).isEmpty()
                ? MachineGuiRenderer.TankFill.previewGas(outputId, recipe.outputAmount(), ChemicalReactorBlockEntity.GAS_TANK_CAPACITY)
                : MachineGuiRenderer.TankFill.liveColor(0, 0);

        MachineGuiRenderer.drawChemicalReactor(
                this,
                g,
                new MachineGuiRenderer.ChemicalReactorState(
                        MachineGuiRenderer.TankFill.previewGas(JeiCompatUtil.idOf(recipe.primaryGas(), "gas", "gasId"), recipe.primaryGas().amount(), ChemicalReactorBlockEntity.GAS_TANK_CAPACITY),
                        chemicalSecondaryTank(recipe),
                        outputTank,
                        ChemicalReactorLayout.PROGRESS_W,
                        ChemicalReactorLayout.HEAT_BAR_INNER_H,
                        ChemicalReactorLayout.FUEL_BAR_INNER_H,
                        0xFFE38D3F,
                        ChemicalReactorLayout.MANUAL_BAR_W
                ),
                false
        );
        MachineGuiRenderer.drawChemicalReactorLabels(
                this,
                g,
                net.minecraft.client.Minecraft.getInstance().font,
                getTitle(),
                "Heat >= " + recipe.minHeat() + "  |  Time: " + recipe.processTime() + "t"
        );
    }

    @Override
    public List<Component> getTooltipStrings(ChemicalReactorRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(ChemicalReactorLayout.PRIMARY_GAS_TANK_X, ChemicalReactorLayout.PRIMARY_GAS_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            return gasTankTooltip("Primary input gas", JeiCompatUtil.idOf(recipe.primaryGas(), "gas", "gasId"), recipe.primaryGas().amount(), ChemicalReactorBlockEntity.GAS_TANK_CAPACITY);
        } else if (isHoveringBox(ChemicalReactorLayout.SECONDARY_TANK_X, ChemicalReactorLayout.SECONDARY_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            if (recipe.secondaryFluid().isPresent()) {
                Object fluid = recipe.secondaryFluid().get();
                return fluidTankTooltip("Secondary input fluid", JeiCompatUtil.idOf(fluid, "fluid", "fluidId"), JeiCompatUtil.intOf(fluid, "amount"), ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY);
            }
            if (recipe.secondaryGas().isPresent()) {
                Object gas = recipe.secondaryGas().get();
                return gasTankTooltip("Secondary input gas", JeiCompatUtil.idOf(gas, "gas", "gasId"), JeiCompatUtil.longOf(gas, "amount"), ChemicalReactorBlockEntity.GAS_TANK_CAPACITY);
            }
            return fluidTankTooltip("Secondary input fluid", Fluids.EMPTY, 0, ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY);
        } else if (isHoveringBox(ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.OUTPUT_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            if ("fluid".equals(JeiCompatUtil.serializedName(recipe.outputKind()))) {
                return fluidTankTooltip("Output fluid", recipe.outputId(), recipe.outputAmount(), ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY);
            }
            return gasTankTooltip("Output gas", recipe.outputId(), recipe.outputAmount(), ChemicalReactorBlockEntity.GAS_TANK_CAPACITY);
        } else if (isHoveringBox(ChemicalReactorLayout.PROGRESS_X, ChemicalReactorLayout.PROGRESS_Y, ChemicalReactorLayout.PROGRESS_W, ChemicalReactorLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Progress", 0, recipe.processTime());
        } else if (isHoveringBox(ChemicalReactorLayout.HEAT_BAR_X, ChemicalReactorLayout.HEAT_BAR_Y, ChemicalReactorLayout.HEAT_BAR_W, ChemicalReactorLayout.HEAT_BAR_H, mouseX, mouseY)) {
            return amountTooltip("Heat", recipe.minHeat(), ChemicalReactorBlockEntity.MAX_HEAT);
        } else if (isHoveringBox(ChemicalReactorLayout.FUEL_BAR_X, ChemicalReactorLayout.FUEL_BAR_Y, ChemicalReactorLayout.FUEL_BAR_W, ChemicalReactorLayout.FUEL_BAR_H, mouseX, mouseY)) {
            return amountTooltip("Fuel burn time", 0, recipe.processTime(), "ticks");
        } else if (isHoveringBox(ChemicalReactorLayout.MANUAL_BAR_X, ChemicalReactorLayout.MANUAL_BAR_Y, ChemicalReactorLayout.MANUAL_BAR_W, ChemicalReactorLayout.MANUAL_BAR_H, mouseX, mouseY)) {
            return amountTooltip("Manual boost", 0, ChemicalReactorBlockEntity.MAX_MANUAL_ENERGY);
        }
        return List.of();
    }

    private MachineGuiRenderer.TankFill chemicalSecondaryTank(ChemicalReactorRecipe recipe) {
        if (recipe.secondaryFluid().isPresent()) {
            Object fluidReq = recipe.secondaryFluid().get();
            return MachineGuiRenderer.TankFill.preview(JeiCompatUtil.idOf(fluidReq, "fluid", "fluidId"), JeiCompatUtil.intOf(fluidReq, "amount"), ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY);
        }
        if (recipe.secondaryGas().isPresent()) {
            Object gas = recipe.secondaryGas().get();
            return MachineGuiRenderer.TankFill.previewGas(JeiCompatUtil.idOf(gas, "gas", "gasId"), JeiCompatUtil.longOf(gas, "amount"), ChemicalReactorBlockEntity.GAS_TANK_CAPACITY);
        }
        return MachineGuiRenderer.TankFill.liveColor(0, 0);
    }
}
