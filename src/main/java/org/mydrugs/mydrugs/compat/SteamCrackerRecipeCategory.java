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
import org.mydrugs.mydrugs.blocks.entity.SteamCrackerBlockEntity;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.SteamCrackerLayout;
import org.mydrugs.mydrugs.recipes.steam_cracker.SteamCrackerFluidStack;
import org.mydrugs.mydrugs.recipes.steam_cracker.SteamCrackerGasStack;
import org.mydrugs.mydrugs.recipes.steam_cracker.SteamCrackerRecipe;

import java.util.List;
import java.util.Optional;

final class SteamCrackerRecipeCategory extends AbstractNiceRecipeCategory<SteamCrackerRecipe> {
    static final RecipeType<SteamCrackerRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "steam_cracker"), SteamCrackerRecipe.class);

    SteamCrackerRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.steam_cracker"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "STEAM_CRACKER"),
                SteamCrackerLayout.GUI_WIDTH,
                MachineGuiRenderer.steamCrackerHeight(false)
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SteamCrackerRecipe recipe, IFocusGroup focuses) {
        addMixed(builder, RecipeIngredientRole.INPUT, SteamCrackerLayout.INPUT_SLOT_X, SteamCrackerLayout.SLOT_Y, recipe.inputFluid(), recipe.inputGas());
        addMixed(builder, RecipeIngredientRole.OUTPUT, SteamCrackerLayout.OUTPUT_1_SLOT_X, SteamCrackerLayout.SLOT_Y, recipe.outputFluid1(), recipe.outputGas1());
        addMixed(builder, RecipeIngredientRole.OUTPUT, SteamCrackerLayout.OUTPUT_2_SLOT_X, SteamCrackerLayout.SLOT_Y, recipe.outputFluid2(), recipe.outputGas2());
        addMixed(builder, RecipeIngredientRole.OUTPUT, SteamCrackerLayout.OUTPUT_3_SLOT_X, SteamCrackerLayout.SLOT_Y, recipe.outputFluid3(), recipe.outputGas3());
        addMixed(builder, RecipeIngredientRole.OUTPUT, SteamCrackerLayout.OUTPUT_4_SLOT_X, SteamCrackerLayout.SLOT_Y, recipe.outputFluid4(), recipe.outputGas4());
    }

    private void addMixed(IRecipeLayoutBuilder builder, RecipeIngredientRole role, int x, int y, Optional<SteamCrackerFluidStack> fluid, Optional<SteamCrackerGasStack> gas) {
        fluid.ifPresent(stack -> addFluid(builder, role, x, y, stack.fluid(), stack.amount()));
        gas.ifPresent(stack -> addGas(builder, role, x, y, stack.gas(), stack.amount()));
    }

    @Override
    public void draw(SteamCrackerRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        MachineGuiRenderer.drawSteamCracker(
                this,
                graphics,
                new MachineGuiRenderer.SteamCrackerState(
                        mixedTank(recipe.inputFluid(), recipe.inputGas()),
                        mixedTank(recipe.outputFluid1(), recipe.outputGas1()),
                        mixedTank(recipe.outputFluid2(), recipe.outputGas2()),
                        mixedTank(recipe.outputFluid3(), recipe.outputGas3()),
                        mixedTank(recipe.outputFluid4(), recipe.outputGas4()),
                        SteamCrackerLayout.PROGRESS_W,
                        SteamCrackerLayout.BURN_W,
                        false,
                        false,
                        false,
                        false,
                        false,
                        true,
                        true,
                        true,
                        true,
                        true
                ),
                false
        );
        MachineGuiRenderer.drawSteamCrackerLabels(
                this,
                graphics,
                net.minecraft.client.Minecraft.getInstance().font,
                getTitle(),
                null,
                0,
                "Time: " + recipe.baseTicks() + "t"
        );
    }

    private static MachineGuiRenderer.TankFill mixedTank(Optional<SteamCrackerFluidStack> fluid, Optional<SteamCrackerGasStack> gas) {
        if (gas.isPresent()) {
            SteamCrackerGasStack stack = gas.get();
            return MachineGuiRenderer.TankFill.previewGas(stack.gas(), stack.amount(), SteamCrackerBlockEntity.GAS_CAPACITY);
        }
        if (fluid.isPresent()) {
            SteamCrackerFluidStack stack = fluid.get();
            return MachineGuiRenderer.TankFill.preview(stack.fluid(), stack.amount(), SteamCrackerBlockEntity.FLUID_CAPACITY);
        }
        return MachineGuiRenderer.TankFill.liveColor(0, 0);
    }

    @Override
    public List<Component> getTooltipStrings(SteamCrackerRecipe recipe, IRecipeSlotsView slots, double mouseX, double mouseY) {
        if (isHoveringBox(SteamCrackerLayout.PROGRESS_X, SteamCrackerLayout.PROGRESS_Y, SteamCrackerLayout.PROGRESS_W, SteamCrackerLayout.PROGRESS_H, mouseX, mouseY)) {
            return amountTooltip("Cracking time", recipe.baseTicks(), recipe.baseTicks(), "ticks");
        }
        return List.of();
    }
}
