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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.menu.layout.LayoutMath;
import org.mydrugs.mydrugs.recipes.advanced_furnace.AdvancedFurnaceRecipe;
import org.mydrugs.mydrugs.recipes.advanced_mixing_vat.AdvancedMixingVatRecipe;
import org.mydrugs.mydrugs.recipes.biochemical_reactor.BiochemicalReactorRecipe;
import org.mydrugs.mydrugs.recipes.chemical_reactor.ChemicalReactorRecipe;
import org.mydrugs.mydrugs.recipes.centrifuge.CentrifugeRecipe;
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

final class AdvancedFurnaceRecipeCategory extends AbstractNiceRecipeCategory<AdvancedFurnaceRecipe> {
    static final RecipeType<AdvancedFurnaceRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "advanced_furnace"), AdvancedFurnaceRecipe.class);

    AdvancedFurnaceRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.advanced_furnace"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "ADVANCED_FURNACE_ITEM", "ADVANCED_FURNACE")
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AdvancedFurnaceRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> inputs = new ArrayList<>();
        inputs.add(recipe.inputA());
        recipe.inputB().ifPresent(inputs::add);

        for (int i = 0; i < inputs.size(); i++) {
            addItemIngredient(builder, RecipeIngredientRole.INPUT, spreadLeftX(inputs.size(), i), centeredY(), inputs.get(i));
        }

        List<ItemStack> itemOutputs = new ArrayList<>();
        itemOutputs.add(recipe.resultA());
        if (!recipe.resultB().isEmpty()) {
            itemOutputs.add(recipe.resultB());
        }

        if (recipe.fluidOutput().isPresent()) {
            int itemY = panelY + s(10);
            int fluidY = panelY + panelH - SLOT - s(8);

            for (int i = 0; i < itemOutputs.size(); i++) {
                addItemStack(builder, RecipeIngredientRole.OUTPUT, spreadRightX(itemOutputs.size(), i), itemY, itemOutputs.get(i));
            }

            addFluid(
                    builder,
                    RecipeIngredientRole.OUTPUT,
                    centeredInRightX(),
                    fluidY,
                    recipe.fluidOutput().get(),
                    recipe.fluidAmount()
            );
        } else {
            for (int i = 0; i < itemOutputs.size(); i++) {
                addItemStack(builder, RecipeIngredientRole.OUTPUT, spreadRightX(itemOutputs.size(), i), centeredY(), itemOutputs.get(i));
            }
        }
    }

    @Override
    public void draw(AdvancedFurnaceRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        super.draw(recipe, slots, g, mouseX, mouseY);
        String footer = "Time: " + recipe.cookTime() + "t";
        if (recipe.fluidOutput().isPresent()) {
            footer += "  |  " + recipe.fluidAmount() + " mB " + JeiCompatUtil.shortId(recipe.fluidOutput().get());
        }
        drawBottomInfo(g, footer);
    }
}

final class AdvancedMixingVatRecipeCategory extends AbstractNiceRecipeCategory<AdvancedMixingVatRecipe> {
    static final RecipeType<AdvancedMixingVatRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "advanced_mixing_vat"), AdvancedMixingVatRecipe.class);

    AdvancedMixingVatRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.advanced_mixing_vat"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "ADVANCED_MIXING_VAT"),
                CategoryMode.LARGE
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AdvancedMixingVatRecipe recipe, IFocusGroup focuses) {
        List<?> itemInputs = recipe.itemInputs();
        List<?> fluidInputs = recipe.fluidInputs();

        int itemAreaX = leftInnerX();
        int itemAreaY = leftInnerY();
        int itemAreaW = leftInnerW() - SLOT - s(8);
        int itemAreaH = leftInnerH();

        int fluidX = leftX + leftW - SLOT - s(8);
        int fluidY = leftInnerY();
        int fluidH = leftInnerH();

        int gasX = centeredInGutterX();
        int gasY = LayoutMath.centeredAt(panelY, panelH, SLOT);

        int outputX = centeredInRightX();
        int outputY = centeredY();

        int itemCount = Math.min(itemInputs.size(), 4);
        if (itemCount > 0) {
            int cols = itemCount == 1 ? 1 : 2;
            int rows = (itemCount + cols - 1) / cols;

            for (int i = 0; i < itemCount; i++) {
                int row = i / cols;
                int col = i % cols;
                int rowCount = (row == rows - 1 && itemCount % cols != 0) ? itemCount % cols : cols;
                if (rowCount == 0) rowCount = cols;

                int x = spreadRegionX(itemAreaX, itemAreaW, rowCount, col);
                int y = spreadRegionY(itemAreaY, itemAreaH, rows, row);

                addItemIngredient(builder, RecipeIngredientRole.INPUT, x, y, JeiCompatUtil.ingredientOf(itemInputs.get(i)));
            }
        }

        int fluidCount = Math.min(fluidInputs.size(), 3);
        for (int i = 0; i < fluidCount; i++) {
            int y = spreadRegionY(fluidY, fluidH, fluidCount, i);
            addFluid(
                    builder,
                    RecipeIngredientRole.INPUT,
                    fluidX,
                    y,
                    JeiCompatUtil.idOf(fluidInputs.get(i), "fluid", "fluidId"),
                    JeiCompatUtil.intOf(fluidInputs.get(i), "amount")
            );
        }

        if (recipe.gasInput() != null) {
            addGas(
                    builder,
                    RecipeIngredientRole.INPUT,
                    gasX,
                    gasY,
                    JeiCompatUtil.idOf(recipe.gasInput(), "gas", "gasId"),
                    recipe.gasInput().amount()
            );
        }

        addFluid(
                builder,
                RecipeIngredientRole.OUTPUT,
                outputX,
                outputY,
                JeiCompatUtil.idOf(recipe.output(), "fluid", "fluidId"),
                JeiCompatUtil.intOf(recipe.output(), "amount")
        );
    }

    @Override
    public void draw(AdvancedMixingVatRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        super.draw(recipe, slots, g, mouseX, mouseY);

        List<?> itemInputs = recipe.itemInputs();

        int itemAreaX = leftInnerX();
        int itemAreaY = leftInnerY();
        int itemAreaW = leftInnerW() - SLOT - s(8);
        int itemAreaH = leftInnerH();

        int itemCount = Math.min(itemInputs.size(), 4);
        if (itemCount > 0) {
            int cols = itemCount == 1 ? 1 : 2;
            int rows = (itemCount + cols - 1) / cols;

            for (int i = 0; i < itemCount; i++) {
                int row = i / cols;
                int col = i % cols;
                int rowCount = (row == rows - 1 && itemCount % cols != 0) ? itemCount % cols : cols;
                if (rowCount == 0) rowCount = cols;

                int x = spreadRegionX(itemAreaX, itemAreaW, rowCount, col);
                int y = spreadRegionY(itemAreaY, itemAreaH, rows, row);

                drawSlotCount(g, x, y, JeiCompatUtil.countOf(itemInputs.get(i)));
            }
        }

        drawPanelLabel(g, "ITEMS", itemAreaX, panelY + s(2), itemAreaW);
        drawPanelLabel(g, "FLUIDS", leftX + leftW - SLOT - s(12), panelY + s(2), SLOT + s(8));

        if (recipe.gasInput() != null) {
            drawPanelLabel(g, "GAS", gutterX(), panelY + s(2), gutterW());
        }

        drawBottomInfo(g, "Time: " + recipe.processingTime() + "t");
    }
}

final class CentrifugeRecipeCategory extends AbstractNiceRecipeCategory<CentrifugeRecipe> {
    static final RecipeType<CentrifugeRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "centrifuge"), CentrifugeRecipe.class);

    CentrifugeRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.centrifuge"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "CENTRIFUGE"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CentrifugeRecipe recipe, IFocusGroup focuses) {
        addFluid(builder, RecipeIngredientRole.INPUT, centeredInLeftX(), centeredY(), recipe.input().fluid(), recipe.input().amount());

        addFluid(builder, RecipeIngredientRole.OUTPUT, spreadRightX(recipe.output2().isPresent() ? 2 : 1, 0), centeredY(), recipe.output1().fluid(), recipe.output1().amount());
        recipe.output2().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, spreadRightX(2, 1), centeredY(), output.fluid(), output.amount())
        );
    }
}

final class DistillerRecipeCategory extends AbstractNiceRecipeCategory<DistillerRecipe> {
    static final RecipeType<DistillerRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "distiller"), DistillerRecipe.class);

    DistillerRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.distiller"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "DISTILLER"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DistillerRecipe recipe, IFocusGroup focuses) {
        addFluid(builder, RecipeIngredientRole.INPUT, centeredInLeftX(), centeredY(), recipe.input().fluid(), recipe.input().amount());

        addFluid(builder, RecipeIngredientRole.OUTPUT, spreadRightX(recipe.output2().isPresent() ? 2 : 1, 0), centeredY(), recipe.output1().fluid(), recipe.output1().amount());
        recipe.output2().ifPresent(output ->
                addFluid(builder, RecipeIngredientRole.OUTPUT, spreadRightX(2, 1), centeredY(), output.fluid(), output.amount())
        );
    }
}

final class DryingRecipeCategory extends AbstractNiceRecipeCategory<DryingRecipe> {
    static final RecipeType<DryingRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "drying"), DryingRecipe.class);

    DryingRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.drying_rack"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "DRYING_RACK"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DryingRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, centeredInLeftX(), centeredY(), recipe.input());
        addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), recipe.result());
    }
}

final class EvaporationTrayRecipeCategory extends AbstractNiceRecipeCategory<EvaporationTrayRecipe> {
    static final RecipeType<EvaporationTrayRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "evaporation_tray"), EvaporationTrayRecipe.class);

    EvaporationTrayRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.evaporation_tray"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "EVAPORATION_TRAY"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EvaporationTrayRecipe recipe, IFocusGroup focuses) {
        addFluid(builder, RecipeIngredientRole.INPUT, centeredInLeftX(), centeredY(), recipe.inputFluid(), recipe.inputAmount());
        addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), recipe.result());
    }
}

final class GrindingRecipeCategory extends AbstractNiceRecipeCategory<GrindingRecipe> {
    static final RecipeType<GrindingRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "grinding"), GrindingRecipe.class);

    GrindingRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.grinding_bowl"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "GRINDING_BOWL"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GrindingRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, centeredInLeftX(), centeredY(), recipe.ingredient());
        addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), recipe.result());
    }
}

final class BiochemicalReactorRecipeCategory extends AbstractNiceRecipeCategory<BiochemicalReactorRecipe> {
    static final RecipeType<BiochemicalReactorRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "biochemical_reactor"), BiochemicalReactorRecipe.class);

    BiochemicalReactorRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.biochemical_reactor"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "BIOCHEMICAL_REACTOR"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BiochemicalReactorRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, spreadLeftX(2, 0), centeredY(), JeiCompatUtil.ingredientOf(recipe.ergot()));
        addItemIngredient(builder, RecipeIngredientRole.INPUT, spreadLeftX(2, 1), centeredY(), JeiCompatUtil.ingredientOf(recipe.tryptophan()));

        addFluid(
                builder,
                RecipeIngredientRole.OUTPUT,
                centeredInRightX(),
                centeredY(),
                JeiCompatUtil.idOf(recipe.fluidOutput(), "fluid", "fluidId"),
                JeiCompatUtil.intOf(recipe.fluidOutput(), "amount")
        );
    }

    @Override
    public void draw(BiochemicalReactorRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        super.draw(recipe, slots, g, mouseX, mouseY);
        drawSlotCount(g, spreadLeftX(2, 0), centeredY(), JeiCompatUtil.countOf(recipe.ergot()));
        drawSlotCount(g, spreadLeftX(2, 1), centeredY(), JeiCompatUtil.countOf(recipe.tryptophan()));
        drawBottomInfo(g, "Heat ≥ " + recipe.minimumHeat() + "  |  Time: " + recipe.processingTime() + "t");
    }
}

final class ChemicalReactorRecipeCategory extends AbstractNiceRecipeCategory<ChemicalReactorRecipe> {
    static final RecipeType<ChemicalReactorRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "chemical_reactor"), ChemicalReactorRecipe.class);

    ChemicalReactorRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.chemical_reactor"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "CHEMICAL_REACTOR"),
                CategoryMode.LARGE
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ChemicalReactorRecipe recipe, IFocusGroup focuses) {
        int gasColumnX = leftInnerX();
        int secondaryColumnX = leftInnerX() + leftInnerW() - SLOT;
        int topGasY = panelY + s(12);
        int bottomGasY = panelY + panelH - SLOT - s(12);

        addGas(
                builder,
                RecipeIngredientRole.INPUT,
                gasColumnX,
                topGasY,
                JeiCompatUtil.idOf(recipe.primaryGas(), "gas", "gasId"),
                recipe.primaryGas().amount()
        );

        recipe.secondaryGas().ifPresent(gas ->
                addGas(
                        builder,
                        RecipeIngredientRole.INPUT,
                        gasColumnX,
                        bottomGasY,
                        JeiCompatUtil.idOf(gas, "gas", "gasId"),
                        gas.amount()
                )
        );

        recipe.secondaryFluid().ifPresent(fluidReq ->
                addFluid(
                        builder,
                        RecipeIngredientRole.INPUT,
                        secondaryColumnX,
                        centeredY(),
                        JeiCompatUtil.idOf(fluidReq, "fluid", "fluidId"),
                        JeiCompatUtil.intOf(fluidReq, "amount")
                )
        );

        ResourceLocation outputId = recipe.outputId();
        ItemStack outputStack = JeiCompatUtil.stack(outputId, recipe.outputAmount());
        var outputFluid = JeiCompatUtil.fluid(outputId);

        if (!outputStack.isEmpty()) {
            addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), outputStack);
        } else if (outputFluid != Fluids.EMPTY) {
            addFluid(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), outputFluid, recipe.outputAmount());
        } else {
            addGas(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), outputId, recipe.outputAmount());
        }
    }

    @Override
    public void draw(ChemicalReactorRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        super.draw(recipe, slots, g, mouseX, mouseY);
        if (recipe.secondaryGas().isPresent()) {
            drawPanelLabel(g, "GASES", leftInnerX(), panelY + s(2), SLOT);
        } else {
            drawPanelLabel(g, "GAS", leftInnerX(), panelY + s(2), SLOT);
        }
        if (recipe.secondaryFluid().isPresent()) {
            drawPanelLabel(g, "FLUID", leftInnerX() + leftInnerW() - SLOT - s(6), panelY + s(2), SLOT + s(12));
        }
        drawBottomInfo(g, "Heat ≥ " + recipe.minHeat() + "  |  Time: " + recipe.processTime() + "t");
    }
}

final class FluidFiltererRecipeCategory extends AbstractNiceRecipeCategory<FluidFiltererRecipe> {
    static final RecipeType<FluidFiltererRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "fluid_filterer"), FluidFiltererRecipe.class);

    FluidFiltererRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.fluid_filterer"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "FLUID_FILTERER"),
                CategoryMode.LARGE
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, FluidFiltererRecipe recipe, IFocusGroup focuses) {
        addFluid(
                builder,
                RecipeIngredientRole.INPUT,
                centeredInLeftX(),
                centeredY(),
                JeiCompatUtil.idOf(recipe.input(), "fluid", "fluidId"),
                JeiCompatUtil.intOf(recipe.input(), "amount")
        );

        int topY = panelY + s(12);
        int bottomY = panelY + panelH - SLOT - s(12);

        addFluid(
                builder,
                RecipeIngredientRole.OUTPUT,
                spreadRightX(recipe.output2().isPresent() ? 2 : 1, 0),
                topY,
                JeiCompatUtil.idOf(recipe.output1(), "fluid", "fluidId"),
                JeiCompatUtil.intOf(recipe.output1(), "amount")
        );

        recipe.output2().ifPresent(output ->
                addFluid(
                        builder,
                        RecipeIngredientRole.OUTPUT,
                        spreadRightX(2, 1),
                        topY,
                        JeiCompatUtil.idOf(output, "fluid", "fluidId"),
                        JeiCompatUtil.intOf(output, "amount")
                )
        );

        recipe.outputItem().ifPresent(outputItem ->
                addItemStack(
                        builder,
                        RecipeIngredientRole.OUTPUT,
                        centeredInRightX(),
                        bottomY,
                        JeiCompatUtil.stackOf(outputItem)
                )
        );
    }

    @Override
    public void draw(FluidFiltererRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        super.draw(recipe, slots, g, mouseX, mouseY);
        drawBottomInfo(g, "Clicks: " + recipe.clicksRequired() + "  |  Hunger/tick: " + recipe.hungerPerTick());
    }
}

final class GasifierRecipeCategory extends AbstractNiceRecipeCategory<GasifierRecipe> {
    static final RecipeType<GasifierRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "gasifier"), GasifierRecipe.class);

    GasifierRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.gasifier"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "GASIFIER"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GasifierRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, centeredInLeftX(), centeredY(), recipe.input());
        addGas(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), recipe.gasOutput(), recipe.gasAmount());
    }

    @Override
    public void draw(GasifierRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        super.draw(recipe, slots, g, mouseX, mouseY);
        drawBottomInfo(g, "Time: " + recipe.processTime() + "t");
    }
}

final class GrowthChamberRecipeCategory extends AbstractNiceRecipeCategory<GrowthChamberRecipe> {
    static final RecipeType<GrowthChamberRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "growth_chamber"), GrowthChamberRecipe.class);

    GrowthChamberRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.growth_chamber"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "GROWTH_CHAMBER"),
                CategoryMode.LARGE
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GrowthChamberRecipe recipe, IFocusGroup focuses) {
        int inX = centeredInLeftX();
        int outX = centeredInRightX();
        int topY = panelY + s(12);
        int bottomY = panelY + panelH - SLOT - s(12);

        addItemStack(builder, RecipeIngredientRole.INPUT, inX, topY, JeiCompatUtil.stackOf(recipe.input()));
        addItemStack(builder, RecipeIngredientRole.INPUT, inX, bottomY, JeiCompatUtil.stackOf(recipe.biomassInput()));

        addItemStack(builder, RecipeIngredientRole.OUTPUT, outX, topY, JeiCompatUtil.stackOf(recipe.middleResult()));
        addItemStack(builder, RecipeIngredientRole.OUTPUT, outX, bottomY, JeiCompatUtil.stackOf(recipe.finalResult()));
    }

    @Override
    public void draw(GrowthChamberRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        super.draw(recipe, slots, g, mouseX, mouseY);
        drawPanelLabel(g, "SEED", leftInnerX(), panelY + s(2), leftInnerW());
        drawPanelLabel(g, "GROWTH", rightInnerX(), panelY + s(2), rightInnerW());
        drawBottomInfo(g, "Water: " + recipe.water() + " mB  |  Base: " + recipe.baseTicks() + "t");
    }
}

final class MixingVatRecipeCategory extends AbstractNiceRecipeCategory<MixingVatRecipe> {
    static final RecipeType<MixingVatRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "mixing_vat"), MixingVatRecipe.class);

    MixingVatRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("block.mydrugs.mixing_vat"),
                JeiCompatUtil.iconFromField(helper, ModBlocks.class, "MIXING_VAT"),
                CategoryMode.LARGE
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MixingVatRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> items = new ArrayList<>();
        recipe.item1().ifPresent(items::add);
        recipe.item2().ifPresent(items::add);
        recipe.item3().ifPresent(items::add);
        recipe.item4().ifPresent(items::add);

        int itemAreaX = leftInnerX();
        int itemAreaY = leftInnerY();
        int itemAreaW = leftInnerW() - SLOT - s(8);
        int itemAreaH = leftInnerH();

        int fluidX = leftX + leftW - SLOT - s(8);
        int fluidY = leftInnerY();
        int fluidH = leftInnerH();

        int itemCount = items.size();
        if (itemCount > 0) {
            int cols = itemCount == 1 ? 1 : 2;
            int rows = (itemCount + cols - 1) / cols;

            for (int i = 0; i < itemCount; i++) {
                int row = i / cols;
                int col = i % cols;
                int rowCount = (row == rows - 1 && itemCount % cols != 0) ? itemCount % cols : cols;
                if (rowCount == 0) rowCount = cols;

                int x = spreadRegionX(itemAreaX, itemAreaW, rowCount, col);
                int y = spreadRegionY(itemAreaY, itemAreaH, rows, row);
                addItemIngredient(builder, RecipeIngredientRole.INPUT, x, y, items.get(i));
            }
        }

        List<Object> fluids = new ArrayList<>();
        recipe.fluidInput1().ifPresent(fluids::add);
        recipe.fluidInput2().ifPresent(fluids::add);

        for (int i = 0; i < fluids.size(); i++) {
            int y = spreadRegionY(fluidY, fluidH, fluids.size(), i);
            var fluid = fluids.get(i);
            addFluid(
                    builder,
                    RecipeIngredientRole.INPUT,
                    fluidX,
                    y,
                    JeiCompatUtil.idOf(fluid, "fluid", "fluidId"),
                    JeiCompatUtil.intOf(fluid, "amount")
            );
        }

        boolean hasItemOutput = !recipe.resultItem().isEmpty();
        boolean hasFluidOutput = recipe.resultFluid().isPresent();

        if (hasItemOutput && hasFluidOutput) {
            addItemStack(builder, RecipeIngredientRole.OUTPUT, spreadRightX(2, 0), centeredY(), recipe.resultItem());
            recipe.resultFluid().ifPresent(fluid ->
                    addFluid(builder, RecipeIngredientRole.OUTPUT, spreadRightX(2, 1), centeredY(), fluid.fluid(), fluid.amount())
            );
        } else if (hasItemOutput) {
            addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), recipe.resultItem());
        } else {
            recipe.resultFluid().ifPresent(fluid ->
                    addFluid(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), fluid.fluid(), fluid.amount())
            );
        }
    }

    @Override
    public void draw(MixingVatRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        super.draw(recipe, slots, g, mouseX, mouseY);
        drawPanelLabel(g, "ITEMS", leftInnerX(), panelY + s(2), leftInnerW() - SLOT - s(8));
        drawPanelLabel(g, "FLUIDS", leftX + leftW - SLOT - s(12), panelY + s(2), SLOT + s(10));
        drawBottomInfo(g, "Required stirs: " + recipe.requiredStirs());
    }
}

final class SieveRecipeCategory extends AbstractNiceRecipeCategory<SieveRecipe> {
    static final RecipeType<SieveRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "sieving"), SieveRecipe.class);

    SieveRecipeCategory(IGuiHelper helper) {
        super(helper, TYPE, Component.translatable("block.mydrugs.sieve"), JeiCompatUtil.iconFromField(helper, ModBlocks.class, "SIEVE", "SIEVING_TABLE"));
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SieveRecipe recipe, IFocusGroup focuses) {
        addItemIngredient(builder, RecipeIngredientRole.INPUT, centeredInLeftX(), centeredY(), recipe.input());

        int outputCount = recipe.hasBonus() ? 2 : 1;
        addItemStack(builder, RecipeIngredientRole.OUTPUT, spreadRightX(outputCount, 0), centeredY(), recipe.result());
        recipe.bonusResult().ifPresent(stack ->
                addItemStack(builder, RecipeIngredientRole.OUTPUT, spreadRightX(2, 1), centeredY(), stack)
        );
    }

    @Override
    public void draw(SieveRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        super.draw(recipe, slots, g, mouseX, mouseY);
        String footer = "Time: " + recipe.sieveTime() + "t";
        if (recipe.hasBonus()) {
            footer += "  |  Bonus: " + (int) (recipe.bonusChance() * 100.0F) + "%";
        }
        drawBottomInfo(g, footer);
    }
}

final class StompCraftingRecipeCategory extends AbstractNiceRecipeCategory<StompCraftingRecipe> {
    static final RecipeType<StompCraftingRecipe> TYPE =
            new RecipeType<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "stomp_crafting"), StompCraftingRecipe.class);

    StompCraftingRecipeCategory(IGuiHelper helper) {
        super(
                helper,
                TYPE,
                Component.translatable("item.mydrugs.stomp_plate"),
                JeiCompatUtil.iconFromField(helper, ModItems.class, "STOMP_PLATE"),
                CategoryMode.LARGE
        );
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, StompCraftingRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.expandedIngredients();

        int gridX = leftInnerX();
        int gridY = leftInnerY();
        int gridW = leftInnerW();
        int gridH = leftInnerH();

        int columns = ingredients.size() <= 2 ? 1 : (ingredients.size() <= 4 ? 2 : 3);
        int rows = (ingredients.size() + columns - 1) / columns;

        for (int i = 0; i < ingredients.size(); i++) {
            int row = i / columns;
            int col = i % columns;

            int x = spreadRegionX(gridX, gridW, columns, col);
            int y = spreadRegionY(gridY, gridH, rows, row);

            addItemIngredient(builder, RecipeIngredientRole.INPUT, x, y, ingredients.get(i));
        }

        addItemStack(builder, RecipeIngredientRole.OUTPUT, centeredInRightX(), centeredY(), recipe.result());
    }

    @Override
    public void draw(StompCraftingRecipe recipe, IRecipeSlotsView slots, GuiGraphics g, double mouseX, double mouseY) {
        super.draw(recipe, slots, g, mouseX, mouseY);
        drawBottomInfo(g, "Work: " + recipe.work() + "  |  Clamped: " + recipe.clampedWork());
    }
}