package org.mydrugs.mydrugs.menu.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.blocks.entity.AdvancedFurnaceBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.AdvancedMixingVatBlockEntity;
import org.mydrugs.mydrugs.menu.layout.*;

import java.util.Optional;

/**
 * Shared machine GUI compositions for real screens and JEI recipe categories.
 *
 * Keep machine-specific visual layout here; screens and recipe categories should
 * only adapt live/menu or recipe data into the small render state records.
 */
public final class MachineGuiRenderer {
    private MachineGuiRenderer() {
    }

    public record AdvancedFurnaceState(
            int progressPixels,
            int burnPixels,
            TankFill outputTank
    ) {
        public static AdvancedFurnaceState screen(int progressPixels, int burnPixels, Fluid tankFluid, int tankPixels) {
            return new AdvancedFurnaceState(progressPixels, burnPixels, TankFill.live(tankFluid, tankPixels));
        }

        public static AdvancedFurnaceState recipe(Optional<ResourceLocation> fluid, int amount) {
            return new AdvancedFurnaceState(
                    AdvancedFurnaceLayout.PROGRESS_W,
                    AdvancedFurnaceLayout.BURN_W,
                    TankFill.preview(fluid, amount, AdvancedFurnaceBlockEntity.TANK_CAPACITY)
            );
        }
    }

    public record TankFill(
            Fluid fluid,
            ResourceLocation gasId,
            long amount,
            long capacity,
            int filledPixels,
            int color,
            boolean topLit
    ) {
        public static TankFill live(Fluid fluid, int filledPixels) {
            return new TankFill(fluid == null ? Fluids.EMPTY : fluid, null, 0, 0, filledPixels, 0, false);
        }

        public static TankFill liveColor(int filledPixels, int color) {
            return new TankFill(Fluids.EMPTY, null, 0, 0, filledPixels, color, false);
        }

        public static TankFill liveTopLit(int filledPixels, int color) {
            return new TankFill(Fluids.EMPTY, null, 0, 0, filledPixels, color, true);
        }

        public static TankFill preview(Optional<ResourceLocation> fluidId, int amount, int capacity) {
            Fluid fluid = fluidId
                    .map(BuiltInRegistries.FLUID::getValue)
                    .orElse(Fluids.EMPTY);
            return new TankFill(fluid, null, amount, capacity, -1, 0, false);
        }

        public static TankFill preview(ResourceLocation fluidId, int amount, int capacity) {
            return preview(Optional.ofNullable(fluidId), amount, capacity);
        }

        public static TankFill previewGas(ResourceLocation gasId, long amount, long capacity) {
            return new TankFill(Fluids.EMPTY, gasId, amount, capacity, -1, 0, true);
        }

        private boolean isEmpty() {
            return (this.fluid == null || this.fluid == Fluids.EMPTY) && this.gasId == null && this.color == 0;
        }
    }

    public record AromaticExtractorState(
            TankFill inputTank,
            TankFill catalystTank,
            TankFill output1Tank,
            TankFill output2Tank,
            int scaledProgress,
            int scaledBurnTime,
            boolean inputDumpHovered,
            boolean catalystDumpHovered,
            boolean outputADumpHovered,
            boolean outputBDumpHovered,
            boolean inputDumpEnabled,
            boolean catalystDumpEnabled,
            boolean outputADumpEnabled,
            boolean outputBDumpEnabled
    ) {
    }

    public record BiochemicalReactorState(
            int progressPixels,
            int heatPixels,
            int manualEnergyPixels,
            TankFill outputTank,
            boolean manualButtonHovered,
            String status,
            int ergotCount,
            int tryptophanCount
    ) {
        public static BiochemicalReactorState screen(
                int progressPixels,
                int heatPixels,
                int manualEnergyPixels,
                Fluid outputFluid,
                int outputTankPixels,
                boolean manualButtonHovered,
                String status
        ) {
            return new BiochemicalReactorState(
                    progressPixels,
                    heatPixels,
                    manualEnergyPixels,
                    TankFill.live(outputFluid, outputTankPixels),
                    manualButtonHovered,
                    status,
                    0,
                    0
            );
        }

        public static BiochemicalReactorState recipe(
                ResourceLocation outputFluid,
                int outputAmount,
                int outputCapacity,
                int ergotCount,
                int tryptophanCount
        ) {
            return new BiochemicalReactorState(
                    BiochemicalReactorLayout.PROGRESS_W,
                    BiochemicalReactorLayout.HEAT_BAR_INNER_H,
                    BiochemicalReactorLayout.MANUAL_BAR_INNER_H,
                    TankFill.preview(Optional.ofNullable(outputFluid), outputAmount, outputCapacity),
                    false,
                    "Processing",
                    ergotCount,
                    tryptophanCount
            );
        }
    }

    public record AdvancedMixingVatState(
            TankFill tankA,
            TankFill tankB,
            TankFill tankC,
            TankFill gasTank,
            TankFill outputTank,
            int progressPixels,
            int[] itemCounts
    ) {
        public static AdvancedMixingVatState screen(
                Fluid inputAFluid,
                int inputAPixels,
                Fluid inputBFluid,
                int inputBPixels,
                Fluid inputCFluid,
                int inputCPixels,
                int gasPixels,
                Fluid outputFluid,
                int outputPixels,
                int progressPixels
        ) {
            return new AdvancedMixingVatState(
                    TankFill.live(inputAFluid, inputAPixels),
                    TankFill.live(inputBFluid, inputBPixels),
                    TankFill.live(inputCFluid, inputCPixels),
                    TankFill.liveTopLit(gasPixels, 0xFF9BC4D8),
                    TankFill.live(outputFluid, outputPixels),
                    progressPixels,
                    new int[0]
            );
        }
    }

    public record CentrifugeState(
            TankFill inputTank,
            TankFill outputATank,
            TankFill outputBTank,
            int progressPixels,
            int fuelPixels,
            int fuelColor,
            boolean inputDumpHovered,
            boolean outputADumpHovered,
            boolean outputBDumpHovered,
            boolean inputDumpEnabled,
            boolean outputADumpEnabled,
            boolean outputBDumpEnabled
    ) {
    }

    public record ChemicalReactorState(
            TankFill primaryTank,
            TankFill secondaryTank,
            TankFill outputTank,
            int progressPixels,
            int heatPixels,
            int fuelPixels,
            int fuelColor,
            int manualPixels
    ) {
    }

    public record DistillerState(
            TankFill inputTank,
            TankFill outputATank,
            TankFill outputBTank,
            int progressPixels,
            boolean inputDumpHovered,
            boolean outputADumpHovered,
            boolean outputBDumpHovered,
            boolean inputDumpEnabled,
            boolean outputADumpEnabled,
            boolean outputBDumpEnabled,
            boolean runHovered,
            boolean working,
            boolean boosted,
            String cpsText,
            String speedText
    ) {
    }

    public record ElectrolyzerState(
            TankFill inputTank,
            TankFill output1Tank,
            TankFill output2Tank,
            TankFill output3Tank,
            int progressPixels,
            int fuelPixels,
            int fuelColor,
            boolean inputDumpHovered,
            boolean output1DumpHovered,
            boolean output2DumpHovered,
            boolean output3DumpHovered,
            boolean inputDumpEnabled,
            boolean output1DumpEnabled,
            boolean output2DumpEnabled,
            boolean output3DumpEnabled
    ) {
    }

    public record FluidFiltererState(
            TankFill inputTank,
            TankFill outputTank,
            int progressPixels,
            boolean inputDumpHovered,
            boolean outputDumpHovered,
            boolean inputDumpEnabled,
            boolean outputDumpEnabled,
            boolean runHovered,
            boolean runActive,
            String progressText
    ) {
    }

    public record GasifierState(
            int fuelPixels,
            int progressPixels,
            TankFill outputTank
    ) {
    }

    public record SieveState(
            int knobCenterY
    ) {
    }

    public record CatalyticReformerState(
            TankFill input1Tank,
            TankFill input2Tank,
            TankFill output1Tank,
            TankFill output2Tank,
            TankFill output3Tank,
            int progressPixels,
            boolean input1DumpHovered,
            boolean input2DumpHovered,
            boolean output1DumpHovered,
            boolean output2DumpHovered,
            boolean output3DumpHovered,
            boolean input1DumpEnabled,
            boolean input2DumpEnabled,
            boolean output1DumpEnabled,
            boolean output2DumpEnabled,
            boolean output3DumpEnabled
    ) {
    }

    public record SteamCrackerState(
            TankFill inputTank,
            TankFill output1Tank,
            TankFill output2Tank,
            TankFill output3Tank,
            TankFill output4Tank,
            int progressPixels,
            int fuelPixels,
            boolean inputDumpHovered,
            boolean output1DumpHovered,
            boolean output2DumpHovered,
            boolean output3DumpHovered,
            boolean output4DumpHovered,
            boolean inputDumpEnabled,
            boolean output1DumpEnabled,
            boolean output2DumpEnabled,
            boolean output3DumpEnabled,
            boolean output4DumpEnabled
    ) {
    }

    public record BTXFractionationTowerState(
            TankFill inputTank,
            TankFill benzeneTank,
            TankFill tolueneTank,
            TankFill xyleneTank,
            int progressPixels,
            int fuelPixels,
            int fuelColor,
            boolean inputDumpHovered,
            boolean benzeneDumpHovered,
            boolean tolueneDumpHovered,
            boolean xyleneDumpHovered,
            boolean inputDumpEnabled,
            boolean benzeneDumpEnabled,
            boolean tolueneDumpEnabled,
            boolean xyleneDumpEnabled
    ) {
    }

    public record GrowthChamberState(
            int waterPixels,
            int growthPixels,
            int maturePixels
    ) {
    }

    public static void drawAdvancedFurnace(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            AdvancedFurnaceState state,
            boolean drawPlayerInventory
    ) {
        draw.drawWindow(graphics, AdvancedFurnaceLayout.GUI_WIDTH, advancedFurnaceHeight(drawPlayerInventory), 0xFF15171B, 0xFF23262B);

        draw.drawPanel(
                graphics,
                AdvancedFurnaceLayout.MACHINE_PANEL_X,
                AdvancedFurnaceLayout.MACHINE_PANEL_Y,
                AdvancedFurnaceLayout.MACHINE_PANEL_W,
                AdvancedFurnaceLayout.MACHINE_PANEL_H,
                0xFF2F343C,
                0xFF646B77,
                0xFF0E1116
        );

        draw.drawPanel(
                graphics,
                AdvancedFurnaceLayout.CENTER_PANEL_X,
                AdvancedFurnaceLayout.CENTER_PANEL_Y,
                AdvancedFurnaceLayout.CENTER_PANEL_W,
                AdvancedFurnaceLayout.CENTER_PANEL_H,
                0xFF1B1F25,
                0xFF505862,
                0xFF0A0C10
        );

        if (drawPlayerInventory) {
            drawAdvancedFurnaceInventoryPanels(draw, graphics);
        }

        draw.drawSlotFrame(graphics, AdvancedFurnaceLayout.INPUT_A_X, AdvancedFurnaceLayout.INPUT_A_Y);
        draw.drawSlotFrame(graphics, AdvancedFurnaceLayout.INPUT_B_X, AdvancedFurnaceLayout.INPUT_B_Y);
        draw.drawSlotFrame(graphics, AdvancedFurnaceLayout.FUEL_X, AdvancedFurnaceLayout.FUEL_Y);
        draw.drawSlotFrame(graphics, AdvancedFurnaceLayout.OUTPUT_A_X, AdvancedFurnaceLayout.OUTPUT_A_Y);
        draw.drawSlotFrame(graphics, AdvancedFurnaceLayout.OUTPUT_B_X, AdvancedFurnaceLayout.OUTPUT_B_Y);
        draw.drawSlotFrame(graphics, AdvancedFurnaceLayout.OUTPUT_CONTAINER_X, AdvancedFurnaceLayout.OUTPUT_CONTAINER_Y);

        draw.drawHorizontalBar(
                graphics,
                AdvancedFurnaceLayout.PROGRESS_X,
                AdvancedFurnaceLayout.PROGRESS_Y,
                AdvancedFurnaceLayout.PROGRESS_W,
                AdvancedFurnaceLayout.PROGRESS_H,
                state.progressPixels(),
                0xFF62C8FF,
                0xFFB9EEFF
        );

        draw.drawHorizontalBar(
                graphics,
                AdvancedFurnaceLayout.BURN_X,
                AdvancedFurnaceLayout.BURN_Y,
                AdvancedFurnaceLayout.BURN_W,
                AdvancedFurnaceLayout.BURN_H,
                state.burnPixels(),
                0xFFFF9B47,
                0xFFFFC87A
        );

        draw.drawTankFrame(
                graphics,
                AdvancedFurnaceLayout.TANK_X,
                AdvancedFurnaceLayout.TANK_Y,
                StandardTankLayout.TANK_W,
                StandardTankLayout.TANK_H,
                StandardTankLayout.INNER_X,
                StandardTankLayout.INNER_Y,
                StandardTankLayout.INNER_W,
                StandardTankLayout.INNER_H
        );

        drawAdvancedFurnaceTank(draw, graphics, state.outputTank());
    }

    public static void drawAdvancedFurnaceLabels(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            Font font,
            Component title,
            Component playerInventoryTitle,
            int inventoryLabelY
    ) {
        int machineTitleX = AdvancedFurnaceLayout.MACHINE_PANEL_X
                + (AdvancedFurnaceLayout.MACHINE_PANEL_W - font.width(title)) / 2;

        graphics.drawString(font, title, draw.labelX(machineTitleX), draw.labelY(5), 0xFFF0F3F8, false);

        if (playerInventoryTitle != null) {
            graphics.drawString(
                    font,
                    playerInventoryTitle,
                    draw.labelX(AdvancedFurnaceLayout.PLAYER_INV_X),
                    draw.labelY(inventoryLabelY),
                    0xFFD0D4DC,
                    false
            );
        }

        graphics.drawCenteredString(
                font,
                "Heat",
                draw.labelX(AdvancedFurnaceLayout.HEAT_LABEL_X),
                draw.labelY(AdvancedFurnaceLayout.HEAT_LABEL_Y),
                0xFFE0B58A
        );
    }

    public static void drawAdvancedFurnaceRecipeLabels(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            Component title,
            String footer
    ) {
        Font font = Minecraft.getInstance().font;
        drawAdvancedFurnaceLabels(draw, graphics, font, title, null, 0);
        drawCentered(graphics, font, footer, draw.labelX(5), draw.labelY(advancedFurnaceHeight(false) - 12), AdvancedFurnaceLayout.GUI_WIDTH - 10, 0xFFE5E7EB);
    }


    public static void drawAromaticExtractor(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            AromaticExtractorState state,
            boolean drawPlayerInventory
    ) {
        draw.drawWindow(graphics, AromaticExtractorLayout.GUI_WIDTH, aromaticExtractorHeight(drawPlayerInventory));
        draw.drawPanel(
                graphics,
                AromaticExtractorLayout.MACHINE_PANEL_X,
                AromaticExtractorLayout.MACHINE_PANEL_Y,
                AromaticExtractorLayout.MACHINE_PANEL_W,
                AromaticExtractorLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        if (drawPlayerInventory) {
            drawStandardInventoryPanels(draw, graphics, AromaticExtractorLayout.PLAYER_INV_X, AromaticExtractorLayout.PLAYER_INV_Y, 0xFF2C2C2C, 0xFF595959, 0xFF101010);
        }

        draw.drawPanel(
                graphics,
                AromaticExtractorLayout.CENTER_PANEL_X,
                AromaticExtractorLayout.CENTER_PANEL_Y,
                AromaticExtractorLayout.CENTER_PANEL_W,
                AromaticExtractorLayout.CENTER_PANEL_H,
                0xFF262B32
        );

        draw.drawTankFrame(
                graphics,
                AromaticExtractorLayout.INPUT_TANK_X,
                AromaticExtractorLayout.INPUT_TANK_Y,
                AromaticExtractorLayout.TANK_W,
                AromaticExtractorLayout.TANK_H,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H
        );
        draw.drawTankFrame(
                graphics,
                AromaticExtractorLayout.CATALYST_TANK_X,
                AromaticExtractorLayout.CATALYST_TANK_Y,
                AromaticExtractorLayout.TANK_W,
                AromaticExtractorLayout.TANK_H,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H
        );
        draw.drawTankFrame(
                graphics,
                AromaticExtractorLayout.OUTPUT_A_TANK_X,
                AromaticExtractorLayout.OUTPUT_A_TANK_Y,
                AromaticExtractorLayout.TANK_W,
                AromaticExtractorLayout.TANK_H,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H
        );
        draw.drawTankFrame(
                graphics,
                AromaticExtractorLayout.OUTPUT_B_TANK_X,
                AromaticExtractorLayout.OUTPUT_B_TANK_Y,
                AromaticExtractorLayout.TANK_W,
                AromaticExtractorLayout.TANK_H,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H
        );

        drawTankFill(
                draw,
                graphics,
                state.inputTank,
                AromaticExtractorLayout.INPUT_TANK_X,
                AromaticExtractorLayout.INPUT_TANK_Y,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H
        );

        drawTankFill(
                draw,
                graphics,
                state.catalystTank,
                AromaticExtractorLayout.CATALYST_TANK_X,
                AromaticExtractorLayout.CATALYST_TANK_Y,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H
        );

        drawTankFill(
                draw,
                graphics,
                state.output1Tank,
                AromaticExtractorLayout.OUTPUT_A_TANK_X,
                AromaticExtractorLayout.OUTPUT_A_TANK_Y,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H
        );

        drawTankFill(
                draw,
                graphics,
                state.output2Tank,
                AromaticExtractorLayout.OUTPUT_B_TANK_X,
                AromaticExtractorLayout.OUTPUT_B_TANK_Y,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H
        );

        draw.drawSlotFrame(graphics, AromaticExtractorLayout.INPUT_SLOT_X, AromaticExtractorLayout.INPUT_SLOT_Y);
        draw.drawSlotFrame(graphics, AromaticExtractorLayout.CATALYST_SLOT_X, AromaticExtractorLayout.CATALYST_SLOT_Y);
        draw.drawSlotFrame(graphics, AromaticExtractorLayout.OUTPUT_A_SLOT_X, AromaticExtractorLayout.OUTPUT_A_SLOT_Y);
        draw.drawSlotFrame(graphics, AromaticExtractorLayout.OUTPUT_B_SLOT_X, AromaticExtractorLayout.OUTPUT_B_SLOT_Y);
        draw.drawSlotFrame(graphics, AromaticExtractorLayout.FUEL_SLOT_X, AromaticExtractorLayout.FUEL_SLOT_Y);

        draw.drawHorizontalBar(
                graphics,
                AromaticExtractorLayout.PROGRESS_X,
                AromaticExtractorLayout.PROGRESS_Y,
                AromaticExtractorLayout.PROGRESS_W,
                AromaticExtractorLayout.PROGRESS_H,
                state.scaledProgress,
                0xFFB8865F,
                0xFFFFD0A6
        );

        draw.drawVerticalBar(
                graphics,
                AromaticExtractorLayout.FUEL_BAR_X,
                AromaticExtractorLayout.FUEL_BAR_Y,
                AromaticExtractorLayout.FUEL_BAR_W,
                AromaticExtractorLayout.FUEL_BAR_H,
                AromaticExtractorLayout.FUEL_BAR_INNER_X_OFFSET,
                AromaticExtractorLayout.FUEL_BAR_INNER_Y_OFFSET,
                AromaticExtractorLayout.FUEL_BAR_INNER_W,
                AromaticExtractorLayout.FUEL_BAR_INNER_H,
                state.scaledBurnTime,
                0xFFE38D3F,
                0xFFFFC270
        );

        draw.drawDumpButton(
                graphics,
                AromaticExtractorLayout.DUMP_INPUT_X,
                AromaticExtractorLayout.DUMP_BUTTON_Y,
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                state.inputDumpHovered,
                state.inputDumpEnabled
        );

        draw.drawDumpButton(
                graphics,
                AromaticExtractorLayout.DUMP_CATALYST_X,
                AromaticExtractorLayout.DUMP_BUTTON_Y,
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                state.catalystDumpHovered,
                state.catalystDumpEnabled
        );

        draw.drawDumpButton(
                graphics,
                AromaticExtractorLayout.DUMP_OUTPUT_A_X,
                AromaticExtractorLayout.DUMP_BUTTON_Y,
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                state.outputADumpHovered,
                state.outputADumpEnabled
        );

        draw.drawDumpButton(
                graphics,
                AromaticExtractorLayout.DUMP_OUTPUT_B_X,
                AromaticExtractorLayout.DUMP_BUTTON_Y,
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                state.outputBDumpHovered,
                state.outputBDumpEnabled
        );
    }

    public static void drawBiochemicalReactor(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            BiochemicalReactorState state,
            boolean drawPlayerInventory
    ) {
        draw.drawWindow(graphics, BiochemicalReactorLayout.GUI_WIDTH, biochemicalReactorHeight(drawPlayerInventory));

        draw.drawPanel(
                graphics,
                BiochemicalReactorLayout.MACHINE_PANEL_X,
                BiochemicalReactorLayout.MACHINE_PANEL_Y,
                BiochemicalReactorLayout.MACHINE_PANEL_W,
                BiochemicalReactorLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        if (drawPlayerInventory) {
            drawStandardInventoryPanels(draw, graphics, BiochemicalReactorLayout.PLAYER_INV_X, BiochemicalReactorLayout.PLAYER_INV_Y, 0xFF2C2C2C, 0xFF595959, 0xFF101010);
        }

        draw.drawSlotFrame(graphics, BiochemicalReactorLayout.ERGOT_SLOT_X, BiochemicalReactorLayout.ERGOT_SLOT_Y);
        draw.drawSlotFrame(graphics, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_X, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_Y);
        draw.drawSlotFrame(graphics, BiochemicalReactorLayout.CHARCOAL_SLOT_X, BiochemicalReactorLayout.CHARCOAL_SLOT_Y);
        draw.drawSlotFrame(graphics, BiochemicalReactorLayout.OUTPUT_SLOT_X, BiochemicalReactorLayout.OUTPUT_SLOT_Y);

        draw.drawHorizontalBar(
                graphics,
                BiochemicalReactorLayout.PROGRESS_X,
                BiochemicalReactorLayout.PROGRESS_Y,
                BiochemicalReactorLayout.PROGRESS_W,
                BiochemicalReactorLayout.PROGRESS_H,
                state.progressPixels(),
                0xFF768AB8,
                0xFFAAB9DB
        );

        draw.drawVerticalBar(
                graphics,
                BiochemicalReactorLayout.HEAT_BAR_X,
                BiochemicalReactorLayout.HEAT_BAR_Y,
                BiochemicalReactorLayout.HEAT_BAR_W,
                BiochemicalReactorLayout.HEAT_BAR_H,
                BiochemicalReactorLayout.HEAT_BAR_INNER_X_OFFSET,
                BiochemicalReactorLayout.HEAT_BAR_INNER_Y_OFFSET,
                BiochemicalReactorLayout.HEAT_BAR_INNER_W,
                BiochemicalReactorLayout.HEAT_BAR_INNER_H,
                state.heatPixels(),
                0xFFE38D3F,
                0x22FFFFFF
        );

        draw.drawVerticalBar(
                graphics,
                BiochemicalReactorLayout.MANUAL_BAR_X,
                BiochemicalReactorLayout.MANUAL_BAR_Y,
                BiochemicalReactorLayout.MANUAL_BAR_W,
                BiochemicalReactorLayout.MANUAL_BAR_H,
                BiochemicalReactorLayout.MANUAL_BAR_INNER_X_OFFSET,
                BiochemicalReactorLayout.MANUAL_BAR_INNER_Y_OFFSET,
                BiochemicalReactorLayout.MANUAL_BAR_INNER_W,
                BiochemicalReactorLayout.MANUAL_BAR_INNER_H,
                state.manualEnergyPixels(),
                0xFF77A8E8,
                0x22FFFFFF
        );

        draw.drawTankFrame(
                graphics,
                BiochemicalReactorLayout.OUTPUT_TANK_X,
                BiochemicalReactorLayout.OUTPUT_TANK_Y,
                BiochemicalReactorLayout.TANK_W,
                BiochemicalReactorLayout.TANK_H,
                BiochemicalReactorLayout.TANK_INNER_X_OFFSET,
                BiochemicalReactorLayout.TANK_INNER_Y_OFFSET,
                BiochemicalReactorLayout.TANK_INNER_W,
                BiochemicalReactorLayout.TANK_INNER_H
        );

        drawTankFill(
                draw,
                graphics,
                state.outputTank(),
                BiochemicalReactorLayout.OUTPUT_TANK_X,
                BiochemicalReactorLayout.OUTPUT_TANK_Y,
                BiochemicalReactorLayout.TANK_INNER_X_OFFSET,
                BiochemicalReactorLayout.TANK_INNER_Y_OFFSET,
                BiochemicalReactorLayout.TANK_INNER_W,
                BiochemicalReactorLayout.TANK_INNER_H
        );

        draw.drawPlusButton(
                graphics,
                BiochemicalReactorLayout.MANUAL_BUTTON_X,
                BiochemicalReactorLayout.MANUAL_BUTTON_Y,
                BiochemicalReactorLayout.MANUAL_BUTTON_W,
                BiochemicalReactorLayout.MANUAL_BUTTON_H,
                state.manualButtonHovered()
        );

        drawSlotCount(draw, graphics, BiochemicalReactorLayout.ERGOT_SLOT_X, BiochemicalReactorLayout.ERGOT_SLOT_Y, state.ergotCount());
        drawSlotCount(draw, graphics, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_X, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_Y, state.tryptophanCount());
    }

    public static void drawBiochemicalReactorLabels(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            Font font,
            Component title,
            String status
    ) {
        graphics.drawCenteredString(
                font,
                title,
                draw.labelX(BiochemicalReactorLayout.MACHINE_PANEL_X + BiochemicalReactorLayout.MACHINE_PANEL_W / 2),
                draw.labelY(5),
                0xFFFFFFFF
        );
        graphics.drawCenteredString(
                font,
                Component.literal(status),
                draw.labelX(BiochemicalReactorLayout.PROGRESS_X + BiochemicalReactorLayout.PROGRESS_W / 2),
                draw.labelY(BiochemicalReactorLayout.PROGRESS_Y - 10),
                0xFFB5BAC5
        );
    }

    public static void drawBiochemicalReactorRecipeLabels(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            Component title,
            String status,
            String footer
    ) {
        Font font = Minecraft.getInstance().font;
        drawBiochemicalReactorLabels(draw, graphics, font, title, status);
        drawCentered(graphics, font, footer, draw.labelX(5), draw.labelY(biochemicalReactorHeight(false) - 12), BiochemicalReactorLayout.GUI_WIDTH - 10, 0xFFE5E7EB);
    }

    public static void drawAdvancedMixingVat(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            AdvancedMixingVatState state,
            boolean drawPlayerInventory
    ) {
        draw.drawWindow(graphics, AdvancedMixingVatLayout.GUI_WIDTH, advancedMixingVatHeight(drawPlayerInventory));
        draw.drawPanel(
                graphics,
                AdvancedMixingVatLayout.MACHINE_PANEL_X,
                AdvancedMixingVatLayout.MACHINE_PANEL_Y,
                AdvancedMixingVatLayout.MACHINE_PANEL_W,
                AdvancedMixingVatLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        if (drawPlayerInventory) {
            drawStandardInventoryPanels(draw, graphics, AdvancedMixingVatLayout.PLAYER_INV_X, AdvancedMixingVatLayout.PLAYER_INV_Y, 0xFF2C2C2C, 0xFF595959, 0xFF101010);
        }

        int[] itemX = advancedMixingVatItemX();
        int[] itemY = advancedMixingVatItemY();
        for (int i = 0; i < itemX.length; i++) {
            draw.drawSlotFrame(graphics, itemX[i], itemY[i]);
        }

        TankFill[] tanks = {state.tankA(), state.tankB(), state.tankC(), state.gasTank(), state.outputTank()};
        int[] tankX = advancedMixingVatTankX();
        int[] tankSlotX = advancedMixingVatTankSlotX();
        for (int i = 0; i < tankX.length; i++) {
            draw.drawSlotFrame(graphics, tankSlotX[i], AdvancedMixingVatLayout.TANK_SLOT_Y);
            draw.drawTankFrame(
                    graphics,
                    tankX[i],
                    AdvancedMixingVatLayout.TANK_Y,
                    AdvancedMixingVatLayout.TANK_W,
                    AdvancedMixingVatLayout.TANK_H,
                    AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                    AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                    AdvancedMixingVatLayout.TANK_INNER_W,
                    AdvancedMixingVatLayout.TANK_INNER_H
            );
            drawTankFill(
                    draw,
                    graphics,
                    tanks[i],
                    tankX[i],
                    AdvancedMixingVatLayout.TANK_Y,
                    AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                    AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                    AdvancedMixingVatLayout.TANK_INNER_W,
                    AdvancedMixingVatLayout.TANK_INNER_H
            );
        }

        draw.drawHorizontalBar(
                graphics,
                AdvancedMixingVatLayout.PROGRESS_X,
                AdvancedMixingVatLayout.PROGRESS_Y,
                AdvancedMixingVatLayout.PROGRESS_W,
                AdvancedMixingVatLayout.PROGRESS_H,
                state.progressPixels(),
                0xFF768AB8,
                0xFFAAB9DB
        );

        int[] itemCounts = state.itemCounts();
        for (int i = 0; i < Math.min(itemCounts.length, itemX.length); i++) {
            drawSlotCount(draw, graphics, itemX[i], itemY[i], itemCounts[i]);
        }
    }

    public static void drawAdvancedMixingVatLabels(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            Font font,
            Component title,
            String footer
    ) {
        graphics.drawCenteredString(
                font,
                title,
                draw.labelX(AdvancedMixingVatLayout.MACHINE_PANEL_X + AdvancedMixingVatLayout.MACHINE_PANEL_W / 2),
                draw.labelY(5),
                0xFFCFCFCF
        );

        graphics.drawCenteredString(
                font,
                Component.literal("No heat required"),
                draw.labelX(AdvancedMixingVatLayout.STATUS_TEXT_X),
                draw.labelY(AdvancedMixingVatLayout.STATUS_TEXT_Y),
                0xFF8AA0B5
        );

        if (footer != null) {
            drawCentered(graphics, font, footer, draw.labelX(5), draw.labelY(advancedMixingVatHeight(false) - 12), AdvancedMixingVatLayout.GUI_WIDTH - 10, 0xFFE5E7EB);
        }
    }

    public static void drawCentrifuge(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            CentrifugeState state,
            boolean drawPlayerInventory
    ) {
        draw.drawWindow(graphics, CentrifugeLayout.GUI_WIDTH, centrifugeHeight(drawPlayerInventory));
        draw.drawPanel(graphics, CentrifugeLayout.MACHINE_PANEL_X, CentrifugeLayout.MACHINE_PANEL_Y, CentrifugeLayout.MACHINE_PANEL_W, CentrifugeLayout.MACHINE_PANEL_H, 0xFF323232);

        if (drawPlayerInventory) {
            drawStandardInventoryPanels(draw, graphics, CentrifugeLayout.PLAYER_INV_X, CentrifugeLayout.PLAYER_INV_Y, 0xFF2C2C2C, 0xFF595959, 0xFF101010);
        }

        draw.drawPanel(graphics, CentrifugeLayout.CENTER_PANEL_X, CentrifugeLayout.CENTER_PANEL_Y, CentrifugeLayout.CENTER_PANEL_W, CentrifugeLayout.CENTER_PANEL_H, 0xFF262B32);

        int[] tankX = {CentrifugeLayout.INPUT_TANK_X, CentrifugeLayout.OUTPUT_A_TANK_X, CentrifugeLayout.OUTPUT_B_TANK_X};
        int[] tankY = {CentrifugeLayout.INPUT_TANK_Y, CentrifugeLayout.OUTPUT_A_TANK_Y, CentrifugeLayout.OUTPUT_B_TANK_Y};
        TankFill[] tanks = {state.inputTank(), state.outputATank(), state.outputBTank()};
        for (int i = 0; i < tankX.length; i++) {
            draw.drawTankFrame(graphics, tankX[i], tankY[i], CentrifugeLayout.TANK_W, CentrifugeLayout.TANK_H, CentrifugeLayout.TANK_INNER_X_OFFSET, CentrifugeLayout.TANK_INNER_Y_OFFSET, CentrifugeLayout.TANK_INNER_W, CentrifugeLayout.TANK_INNER_H);
            drawTankFill(draw, graphics, tanks[i], tankX[i], tankY[i], CentrifugeLayout.TANK_INNER_X_OFFSET, CentrifugeLayout.TANK_INNER_Y_OFFSET, CentrifugeLayout.TANK_INNER_W, CentrifugeLayout.TANK_INNER_H);
        }

        draw.drawSlotFrame(graphics, CentrifugeLayout.INPUT_SLOT_X, CentrifugeLayout.INPUT_SLOT_Y);
        draw.drawSlotFrame(graphics, CentrifugeLayout.OUTPUT_A_SLOT_X, CentrifugeLayout.OUTPUT_A_SLOT_Y);
        draw.drawSlotFrame(graphics, CentrifugeLayout.OUTPUT_B_SLOT_X, CentrifugeLayout.OUTPUT_B_SLOT_Y);
        draw.drawSlotFrame(graphics, CentrifugeLayout.FUEL_SLOT_X, CentrifugeLayout.FUEL_SLOT_Y);

        draw.drawHorizontalBar(graphics, CentrifugeLayout.PROGRESS_X, CentrifugeLayout.PROGRESS_Y, CentrifugeLayout.PROGRESS_W, CentrifugeLayout.PROGRESS_H, state.progressPixels(), 0xFF768AB8, 0xFFAAB9DB);
        draw.drawVerticalBar(graphics, CentrifugeLayout.FUEL_BAR_X, CentrifugeLayout.FUEL_BAR_Y, CentrifugeLayout.FUEL_BAR_W, CentrifugeLayout.FUEL_BAR_H, CentrifugeLayout.FUEL_BAR_INNER_X_OFFSET, CentrifugeLayout.FUEL_BAR_INNER_Y_OFFSET, CentrifugeLayout.FUEL_BAR_INNER_W, CentrifugeLayout.FUEL_BAR_INNER_H, state.fuelPixels(), state.fuelColor(), 0xFFFFC270);

        draw.drawDumpButton(graphics, CentrifugeLayout.DUMP_INPUT_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, state.inputDumpHovered(), state.inputDumpEnabled());
        draw.drawDumpButton(graphics, CentrifugeLayout.DUMP_OUTPUT_A_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, state.outputADumpHovered(), state.outputADumpEnabled());
        draw.drawDumpButton(graphics, CentrifugeLayout.DUMP_OUTPUT_B_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, state.outputBDumpHovered(), state.outputBDumpEnabled());
    }

    public static void drawCentrifugeLabels(AbstractMachineDrawMethods draw, GuiGraphics graphics, Font font, Component title) {
        graphics.drawCenteredString(font, title, draw.labelX(CentrifugeLayout.GUI_WIDTH / 2), draw.labelY(5), 0xFFFFFFFF);
    }

    public static void drawChemicalReactor(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            ChemicalReactorState state,
            boolean drawPlayerInventory
    ) {
        draw.drawWindow(graphics, ChemicalReactorLayout.GUI_WIDTH, chemicalReactorHeight(drawPlayerInventory));
        draw.drawPanel(graphics, ChemicalReactorLayout.MACHINE_PANEL_X, ChemicalReactorLayout.MACHINE_PANEL_Y, ChemicalReactorLayout.MACHINE_PANEL_W, ChemicalReactorLayout.MACHINE_PANEL_H, 0xFF323232);

        if (drawPlayerInventory) {
            drawStandardInventoryPanels(draw, graphics, ChemicalReactorLayout.PLAYER_INV_X, ChemicalReactorLayout.PLAYER_INV_Y, 0xFF2C2C2C, 0xFF595959, 0xFF101010);
        }

        int[] tankX = {ChemicalReactorLayout.PRIMARY_GAS_TANK_X, ChemicalReactorLayout.SECONDARY_TANK_X, ChemicalReactorLayout.OUTPUT_TANK_X};
        int[] tankY = {ChemicalReactorLayout.PRIMARY_GAS_TANK_Y, ChemicalReactorLayout.SECONDARY_TANK_Y, ChemicalReactorLayout.OUTPUT_TANK_Y};
        TankFill[] tanks = {state.primaryTank(), state.secondaryTank(), state.outputTank()};
        for (int i = 0; i < tankX.length; i++) {
            draw.drawTankFrame(graphics, tankX[i], tankY[i], ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, ChemicalReactorLayout.TANK_INNER_X_OFFSET, ChemicalReactorLayout.TANK_INNER_Y_OFFSET, ChemicalReactorLayout.TANK_INNER_W, ChemicalReactorLayout.TANK_INNER_H);
            drawTankFill(draw, graphics, tanks[i], tankX[i], tankY[i], ChemicalReactorLayout.TANK_INNER_X_OFFSET, ChemicalReactorLayout.TANK_INNER_Y_OFFSET, ChemicalReactorLayout.TANK_INNER_W, ChemicalReactorLayout.TANK_INNER_H);
        }

        draw.drawSlotFrame(graphics, ChemicalReactorLayout.FUEL_SLOT_X, ChemicalReactorLayout.FUEL_SLOT_Y);
        draw.drawSlotFrame(graphics, ChemicalReactorLayout.PRIMARY_GAS_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y);
        draw.drawSlotFrame(graphics, ChemicalReactorLayout.SECONDARY_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y);
        draw.drawSlotFrame(graphics, ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.TRANSFER_SLOT_Y);

        draw.drawHorizontalBar(graphics, ChemicalReactorLayout.PROGRESS_X, ChemicalReactorLayout.PROGRESS_Y, ChemicalReactorLayout.PROGRESS_W, ChemicalReactorLayout.PROGRESS_H, state.progressPixels(), 0xFF85A6C9, 0xFFC6DCF2);
        draw.drawVerticalBar(graphics, ChemicalReactorLayout.HEAT_BAR_X, ChemicalReactorLayout.HEAT_BAR_Y, ChemicalReactorLayout.HEAT_BAR_W, ChemicalReactorLayout.HEAT_BAR_H, ChemicalReactorLayout.HEAT_BAR_INNER_X_OFFSET, ChemicalReactorLayout.HEAT_BAR_INNER_Y_OFFSET, ChemicalReactorLayout.HEAT_BAR_INNER_W, ChemicalReactorLayout.HEAT_BAR_INNER_H, state.heatPixels(), 0xFFE35C3F, 0xFFFFB870);
        draw.drawVerticalBar(graphics, ChemicalReactorLayout.FUEL_BAR_X, ChemicalReactorLayout.FUEL_BAR_Y, ChemicalReactorLayout.FUEL_BAR_W, ChemicalReactorLayout.FUEL_BAR_H, ChemicalReactorLayout.FUEL_BAR_INNER_X_OFFSET, ChemicalReactorLayout.FUEL_BAR_INNER_Y_OFFSET, ChemicalReactorLayout.FUEL_BAR_INNER_W, ChemicalReactorLayout.FUEL_BAR_INNER_H, state.fuelPixels(), state.fuelColor(), 0xFFFFC270);
        draw.drawHorizontalBar(graphics, ChemicalReactorLayout.MANUAL_BAR_X, ChemicalReactorLayout.MANUAL_BAR_Y, ChemicalReactorLayout.MANUAL_BAR_W, ChemicalReactorLayout.MANUAL_BAR_H, state.manualPixels(), 0xFF63B36D, 0xFFA8E4AF);
    }

    public static void drawChemicalReactorLabels(AbstractMachineDrawMethods draw, GuiGraphics graphics, Font font, Component title, String footer) {
        graphics.drawString(font, title, draw.labelX(ChemicalReactorLayout.GUI_WIDTH / 2 - font.width(title) / 2), draw.labelY(5), 0xFFE0E0E0, false);
        drawCentered(graphics, font, "Process", draw.labelX(ChemicalReactorLayout.PROGRESS_X), draw.labelY(ChemicalReactorLayout.LABEL_Y + 6), ChemicalReactorLayout.PROGRESS_W, 0xFFB8B8B8);
        if (footer != null) {
            drawCentered(graphics, font, footer, draw.labelX(5), draw.labelY(chemicalReactorHeight(false) - 12), ChemicalReactorLayout.GUI_WIDTH - 10, 0xFFE5E7EB);
        }
    }

    public static void drawDistiller(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            DistillerState state,
            boolean drawPlayerInventory
    ) {
        draw.drawWindow(graphics, DistillerLayout.GUI_WIDTH, distillerHeight(drawPlayerInventory));
        draw.drawPanel(graphics, DistillerLayout.MACHINE_PANEL_X, DistillerLayout.MACHINE_PANEL_Y, DistillerLayout.MACHINE_PANEL_W, DistillerLayout.MACHINE_PANEL_H, 0xFF323232);

        if (drawPlayerInventory) {
            drawStandardInventoryPanels(draw, graphics, DistillerLayout.PLAYER_INV_X, DistillerLayout.PLAYER_INV_Y, 0xFF2C2C2C, 0xFF595959, 0xFF101010);
        }

        int[] tankX = {DistillerLayout.INPUT_TANK_X, DistillerLayout.OUTPUT_A_TANK_X, DistillerLayout.OUTPUT_B_TANK_X};
        int[] tankY = {DistillerLayout.INPUT_TANK_Y, DistillerLayout.OUTPUT_A_TANK_Y, DistillerLayout.OUTPUT_B_TANK_Y};
        TankFill[] tanks = {state.inputTank(), state.outputATank(), state.outputBTank()};
        for (int i = 0; i < tankX.length; i++) {
            draw.drawTankFrame(graphics, tankX[i], tankY[i], DistillerLayout.TANK_W, DistillerLayout.TANK_H, DistillerLayout.TANK_INNER_X_OFFSET, DistillerLayout.TANK_INNER_Y_OFFSET, DistillerLayout.TANK_INNER_W, DistillerLayout.TANK_INNER_H);
            drawTankFill(draw, graphics, tanks[i], tankX[i], tankY[i], DistillerLayout.TANK_INNER_X_OFFSET, DistillerLayout.TANK_INNER_Y_OFFSET, DistillerLayout.TANK_INNER_W, DistillerLayout.TANK_INNER_H);
        }

        draw.drawSlotFrame(graphics, DistillerLayout.INPUT_SLOT_X, DistillerLayout.INPUT_SLOT_Y);
        draw.drawSlotFrame(graphics, DistillerLayout.OUTPUT_A_SLOT_X, DistillerLayout.OUTPUT_A_SLOT_Y);
        draw.drawSlotFrame(graphics, DistillerLayout.OUTPUT_B_SLOT_X, DistillerLayout.OUTPUT_B_SLOT_Y);
        draw.drawHorizontalBar(graphics, DistillerLayout.PROGRESS_X, DistillerLayout.PROGRESS_Y, DistillerLayout.PROGRESS_W, DistillerLayout.PROGRESS_H, state.progressPixels(), 0xFF768AB8, 0xFFAAB9DB);

        draw.drawDumpButton(graphics, DistillerLayout.DUMP_INPUT_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, state.inputDumpHovered(), state.inputDumpEnabled());
        draw.drawDumpButton(graphics, DistillerLayout.DUMP_OUTPUT_A_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, state.outputADumpHovered(), state.outputADumpEnabled());
        draw.drawDumpButton(graphics, DistillerLayout.DUMP_OUTPUT_B_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, state.outputBDumpHovered(), state.outputBDumpEnabled());
        drawDistillerReactor(draw, graphics, state.runHovered(), state.working(), state.boosted());

        Font font = Minecraft.getInstance().font;
        if (state.cpsText() != null) {
            graphics.drawCenteredString(font, Component.literal(state.cpsText()), draw.guiX(DistillerLayout.RUN_BUTTON_X + DistillerLayout.RUN_BUTTON_SIZE / 2), draw.guiY(DistillerLayout.CPS_TEXT_Y), 0xFFD8D8D8);
        }
        if (state.speedText() != null) {
            graphics.drawCenteredString(font, Component.literal(state.speedText()), draw.guiX(DistillerLayout.RUN_BUTTON_X + DistillerLayout.RUN_BUTTON_SIZE / 2), draw.guiY(DistillerLayout.SPEED_TEXT_Y), 0xFFBEBEBE);
        }
    }

    public static void drawDistillerLabels(AbstractMachineDrawMethods draw, GuiGraphics graphics, Font font, Component title) {
        graphics.drawCenteredString(font, title, draw.labelX(DistillerLayout.GUI_WIDTH / 2), draw.labelY(5), 0xFFFFFFFF);
    }

    public static void drawElectrolyzer(AbstractMachineDrawMethods draw, GuiGraphics graphics, ElectrolyzerState state, boolean drawPlayerInventory) {
        draw.drawWindow(graphics, ElectrolyzerLayout.GUI_WIDTH, electrolyzerHeight(drawPlayerInventory));
        draw.drawPanel(graphics, ElectrolyzerLayout.MACHINE_PANEL_X, ElectrolyzerLayout.MACHINE_PANEL_Y, ElectrolyzerLayout.MACHINE_PANEL_W, ElectrolyzerLayout.MACHINE_PANEL_H, 0xFF323232);
        if (drawPlayerInventory) {
            drawStandardInventoryPanels(draw, graphics, ElectrolyzerLayout.PLAYER_INV_X, ElectrolyzerLayout.PLAYER_INV_Y, 0xFF2C2C2C, 0xFF595959, 0xFF101010);
        }
        draw.drawPanel(graphics, ElectrolyzerLayout.CENTER_PANEL_X, ElectrolyzerLayout.CENTER_PANEL_Y, ElectrolyzerLayout.CENTER_PANEL_W, ElectrolyzerLayout.CENTER_PANEL_H, 0xFF262B32);

        int[] tankX = {ElectrolyzerLayout.INPUT_TANK_X, ElectrolyzerLayout.OUTPUT_1_TANK_X, ElectrolyzerLayout.OUTPUT_2_TANK_X, ElectrolyzerLayout.OUTPUT_3_TANK_X};
        int[] tankY = {ElectrolyzerLayout.INPUT_TANK_Y, ElectrolyzerLayout.OUTPUT_1_TANK_Y, ElectrolyzerLayout.OUTPUT_2_TANK_Y, ElectrolyzerLayout.OUTPUT_3_TANK_Y};
        TankFill[] tanks = {state.inputTank(), state.output1Tank(), state.output2Tank(), state.output3Tank()};
        for (int i = 0; i < tankX.length; i++) {
            draw.drawTankFrame(graphics, tankX[i], tankY[i], ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, ElectrolyzerLayout.TANK_INNER_X_OFFSET, ElectrolyzerLayout.TANK_INNER_Y_OFFSET, ElectrolyzerLayout.TANK_INNER_W, ElectrolyzerLayout.TANK_INNER_H);
            drawTankFill(draw, graphics, tanks[i], tankX[i], tankY[i], ElectrolyzerLayout.TANK_INNER_X_OFFSET, ElectrolyzerLayout.TANK_INNER_Y_OFFSET, ElectrolyzerLayout.TANK_INNER_W, ElectrolyzerLayout.TANK_INNER_H);
        }

        draw.drawSlotFrame(graphics, ElectrolyzerLayout.INPUT_SLOT_X, ElectrolyzerLayout.INPUT_SLOT_Y);
        draw.drawSlotFrame(graphics, ElectrolyzerLayout.OUTPUT_1_SLOT_X, ElectrolyzerLayout.OUTPUT_1_SLOT_Y);
        draw.drawSlotFrame(graphics, ElectrolyzerLayout.OUTPUT_2_SLOT_X, ElectrolyzerLayout.OUTPUT_2_SLOT_Y);
        draw.drawSlotFrame(graphics, ElectrolyzerLayout.OUTPUT_3_SLOT_X, ElectrolyzerLayout.OUTPUT_3_SLOT_Y);
        draw.drawSlotFrame(graphics, ElectrolyzerLayout.FUEL_SLOT_X, ElectrolyzerLayout.FUEL_SLOT_Y);

        draw.drawHorizontalBar(graphics, ElectrolyzerLayout.PROGRESS_X, ElectrolyzerLayout.PROGRESS_Y, ElectrolyzerLayout.PROGRESS_W, ElectrolyzerLayout.PROGRESS_H, state.progressPixels(), 0xFF768AB8, 0xFFAAB9DB);
        draw.drawVerticalBar(graphics, ElectrolyzerLayout.FUEL_BAR_X, ElectrolyzerLayout.FUEL_BAR_Y, ElectrolyzerLayout.FUEL_BAR_W, ElectrolyzerLayout.FUEL_BAR_H, ElectrolyzerLayout.FUEL_BAR_INNER_X_OFFSET, ElectrolyzerLayout.FUEL_BAR_INNER_Y_OFFSET, ElectrolyzerLayout.FUEL_BAR_INNER_W, ElectrolyzerLayout.FUEL_BAR_INNER_H, state.fuelPixels(), state.fuelColor(), 0xFFFFC270);

        draw.drawDumpButton(graphics, ElectrolyzerLayout.DUMP_INPUT_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, state.inputDumpHovered(), state.inputDumpEnabled());
        draw.drawDumpButton(graphics, ElectrolyzerLayout.DUMP_OUTPUT_1_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, state.output1DumpHovered(), state.output1DumpEnabled());
        draw.drawDumpButton(graphics, ElectrolyzerLayout.DUMP_OUTPUT_2_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, state.output2DumpHovered(), state.output2DumpEnabled());
        draw.drawDumpButton(graphics, ElectrolyzerLayout.DUMP_OUTPUT_3_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, state.output3DumpHovered(), state.output3DumpEnabled());
    }

    public static void drawElectrolyzerLabels(AbstractMachineDrawMethods draw, GuiGraphics graphics, Font font, Component title) {
        graphics.drawCenteredString(font, title, draw.labelX(ElectrolyzerLayout.GUI_WIDTH / 2), draw.labelY(5), 0xFFFFFFFF);
    }

    public static void drawFluidFilterer(AbstractMachineDrawMethods draw, GuiGraphics graphics, FluidFiltererState state, boolean drawPlayerInventory) {
        draw.drawWindow(graphics, FluidFiltererLayout.GUI_WIDTH, fluidFiltererHeight(drawPlayerInventory));
        draw.drawPanel(graphics, FluidFiltererLayout.MACHINE_PANEL_X, FluidFiltererLayout.MACHINE_PANEL_Y, FluidFiltererLayout.MACHINE_PANEL_W, FluidFiltererLayout.MACHINE_PANEL_H, 0xFF323232);
        if (drawPlayerInventory) {
            drawStandardInventoryPanels(draw, graphics, FluidFiltererLayout.PLAYER_INV_X, FluidFiltererLayout.PLAYER_INV_Y, 0xFF2C2C2C, 0xFF595959, 0xFF101010);
        }

        draw.drawTankFrame(graphics, FluidFiltererLayout.INPUT_TANK_X, FluidFiltererLayout.INPUT_TANK_Y, FluidFiltererLayout.TANK_W, FluidFiltererLayout.TANK_H, FluidFiltererLayout.TANK_INNER_X_OFFSET, FluidFiltererLayout.TANK_INNER_Y_OFFSET, FluidFiltererLayout.TANK_INNER_W, FluidFiltererLayout.TANK_INNER_H);
        draw.drawTankFrame(graphics, FluidFiltererLayout.OUTPUT_A_TANK_X, FluidFiltererLayout.OUTPUT_A_TANK_Y, FluidFiltererLayout.TANK_W, FluidFiltererLayout.TANK_H, FluidFiltererLayout.TANK_INNER_X_OFFSET, FluidFiltererLayout.TANK_INNER_Y_OFFSET, FluidFiltererLayout.TANK_INNER_W, FluidFiltererLayout.TANK_INNER_H);
        drawTankFill(draw, graphics, state.inputTank(), FluidFiltererLayout.INPUT_TANK_X, FluidFiltererLayout.INPUT_TANK_Y, FluidFiltererLayout.TANK_INNER_X_OFFSET, FluidFiltererLayout.TANK_INNER_Y_OFFSET, FluidFiltererLayout.TANK_INNER_W, FluidFiltererLayout.TANK_INNER_H);
        drawTankFill(draw, graphics, state.outputTank(), FluidFiltererLayout.OUTPUT_A_TANK_X, FluidFiltererLayout.OUTPUT_A_TANK_Y, FluidFiltererLayout.TANK_INNER_X_OFFSET, FluidFiltererLayout.TANK_INNER_Y_OFFSET, FluidFiltererLayout.TANK_INNER_W, FluidFiltererLayout.TANK_INNER_H);

        draw.drawSlotFrame(graphics, FluidFiltererLayout.INPUT_SLOT_X, FluidFiltererLayout.INPUT_SLOT_Y);
        draw.drawSlotFrame(graphics, FluidFiltererLayout.OUTPUT_A_SLOT_X, FluidFiltererLayout.OUTPUT_A_SLOT_Y);
        draw.drawSlotFrame(graphics, FluidFiltererLayout.FILTER_SLOT_X, FluidFiltererLayout.FILTER_SLOT_Y);
        draw.drawSlotFrame(graphics, FluidFiltererLayout.RESIDUE_SLOT_X, FluidFiltererLayout.RESIDUE_SLOT_Y);
        draw.drawHorizontalBar(graphics, FluidFiltererLayout.PROGRESS_X, FluidFiltererLayout.PROGRESS_Y, FluidFiltererLayout.PROGRESS_W, FluidFiltererLayout.PROGRESS_H, state.progressPixels(), 0xFF768AB8, 0xFFAAB9DB);

        draw.drawDumpButton(graphics, FluidFiltererLayout.DUMP_INPUT_X, FluidFiltererLayout.DUMP_BUTTON_Y, FluidFiltererLayout.DUMP_BUTTON_SIZE, state.inputDumpHovered(), state.inputDumpEnabled());
        draw.drawDumpButton(graphics, FluidFiltererLayout.DUMP_OUTPUT_A_X, FluidFiltererLayout.DUMP_BUTTON_Y, FluidFiltererLayout.DUMP_BUTTON_SIZE, state.outputDumpHovered(), state.outputDumpEnabled());
        draw.drawHoldButton(graphics, FluidFiltererLayout.RUN_BUTTON_X, FluidFiltererLayout.RUN_BUTTON_Y, FluidFiltererLayout.RUN_BUTTON_W, FluidFiltererLayout.RUN_BUTTON_H, state.runHovered(), state.runActive(), "HOLD", "FILTERING...");

        if (state.progressText() != null) {
            graphics.drawCenteredString(Minecraft.getInstance().font, Component.literal(state.progressText()), draw.guiX(FluidFiltererLayout.GUI_WIDTH / 2), draw.guiY(FluidFiltererLayout.PROGRESS_TEXT_Y), 0xFFE6E6E6);
        }
    }

    public static void drawFluidFiltererLabels(AbstractMachineDrawMethods draw, GuiGraphics graphics, Font font, Component title, String footer) {
        graphics.drawCenteredString(font, title, draw.labelX(FluidFiltererLayout.GUI_WIDTH / 2), draw.labelY(5), 0xFFFFFFFF);
        if (footer != null) {
            drawCentered(graphics, font, footer, draw.labelX(5), draw.labelY(fluidFiltererHeight(false) - 12), FluidFiltererLayout.GUI_WIDTH - 10, 0xFFE5E7EB);
        }
    }

    public static void drawCatalyticReformer(AbstractMachineDrawMethods draw, GuiGraphics graphics, CatalyticReformerState state, boolean drawPlayerInventory) {
        draw.drawWindow(graphics, CatalyticReformerLayout.GUI_WIDTH, catalyticReformerHeight(drawPlayerInventory));
        draw.drawPanel(graphics, CatalyticReformerLayout.MACHINE_PANEL_X, CatalyticReformerLayout.MACHINE_PANEL_Y, CatalyticReformerLayout.MACHINE_PANEL_W, CatalyticReformerLayout.MACHINE_PANEL_H, 0xFF323232);
        if (drawPlayerInventory) {
            drawStandardInventoryPanels(draw, graphics, CatalyticReformerLayout.PLAYER_INV_X, CatalyticReformerLayout.PLAYER_INV_Y, 0xFF2C2C2C, 0xFF595959, 0xFF101010);
        }
        draw.drawPanel(graphics, CatalyticReformerLayout.CENTER_PANEL_X, CatalyticReformerLayout.CENTER_PANEL_Y, CatalyticReformerLayout.CENTER_PANEL_W, CatalyticReformerLayout.CENTER_PANEL_H, 0xFF262B32);

        drawCatalyticTank(draw, graphics, CatalyticReformerLayout.INPUT_1_TANK_X, state.input1Tank());
        drawCatalyticTank(draw, graphics, CatalyticReformerLayout.INPUT_2_TANK_X, state.input2Tank());
        drawCatalyticTank(draw, graphics, CatalyticReformerLayout.OUTPUT_1_TANK_X, state.output1Tank());
        drawCatalyticTank(draw, graphics, CatalyticReformerLayout.OUTPUT_2_TANK_X, state.output2Tank());
        drawCatalyticTank(draw, graphics, CatalyticReformerLayout.OUTPUT_3_TANK_X, state.output3Tank());

        draw.drawSlotFrame(graphics, CatalyticReformerLayout.INPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        draw.drawSlotFrame(graphics, CatalyticReformerLayout.INPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        draw.drawSlotFrame(graphics, CatalyticReformerLayout.OUTPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        draw.drawSlotFrame(graphics, CatalyticReformerLayout.OUTPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        draw.drawSlotFrame(graphics, CatalyticReformerLayout.OUTPUT_3_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        draw.drawSlotFrame(graphics, CatalyticReformerLayout.CATALYST_SLOT_X, CatalyticReformerLayout.CATALYST_SLOT_Y);

        draw.drawHorizontalBar(graphics, CatalyticReformerLayout.PROGRESS_X, CatalyticReformerLayout.PROGRESS_Y, CatalyticReformerLayout.PROGRESS_W, CatalyticReformerLayout.PROGRESS_H, state.progressPixels(), 0xFF768AB8, 0xFFAAB9DB);

        draw.drawDumpButton(graphics, CatalyticReformerLayout.DUMP_INPUT_1_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, state.input1DumpHovered(), state.input1DumpEnabled());
        draw.drawDumpButton(graphics, CatalyticReformerLayout.DUMP_INPUT_2_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, state.input2DumpHovered(), state.input2DumpEnabled());
        draw.drawDumpButton(graphics, CatalyticReformerLayout.DUMP_OUTPUT_1_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, state.output1DumpHovered(), state.output1DumpEnabled());
        draw.drawDumpButton(graphics, CatalyticReformerLayout.DUMP_OUTPUT_2_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, state.output2DumpHovered(), state.output2DumpEnabled());
        draw.drawDumpButton(graphics, CatalyticReformerLayout.DUMP_OUTPUT_3_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, state.output3DumpHovered(), state.output3DumpEnabled());
    }

    public static void drawCatalyticReformerLabels(AbstractMachineDrawMethods draw, GuiGraphics graphics, Font font, Component title, String footer) {
        graphics.drawCenteredString(font, title, draw.labelX(CatalyticReformerLayout.GUI_WIDTH / 2), draw.labelY(5), 0xFFFFFFFF);
        if (footer != null) {
            drawCentered(graphics, font, footer, draw.labelX(5), draw.labelY(catalyticReformerHeight(false) - 12), CatalyticReformerLayout.GUI_WIDTH - 10, 0xFFE5E7EB);
        }
    }

    public static void drawSteamCracker(AbstractMachineDrawMethods draw, GuiGraphics graphics, SteamCrackerState state, boolean drawPlayerInventory) {
        draw.drawWindow(graphics, SteamCrackerLayout.GUI_WIDTH, steamCrackerHeight(drawPlayerInventory));
        draw.drawPanel(graphics, SteamCrackerLayout.MACHINE_PANEL_X, SteamCrackerLayout.MACHINE_PANEL_Y, SteamCrackerLayout.MACHINE_PANEL_W, SteamCrackerLayout.MACHINE_PANEL_H, 0xFF20242C);
        if (drawPlayerInventory) {
            drawStandardInventoryPanels(draw, graphics, SteamCrackerLayout.PLAYER_INV_X, SteamCrackerLayout.PLAYER_INV_Y, 0xFF2C2C2C, 0xFF595959, 0xFF101010);
        }
        draw.drawPanel(graphics, SteamCrackerLayout.CENTER_PANEL_X, SteamCrackerLayout.CENTER_PANEL_Y, SteamCrackerLayout.CENTER_PANEL_W, SteamCrackerLayout.CENTER_PANEL_H, 0xFF15181E);

        drawSteamCrackerTank(draw, graphics, SteamCrackerLayout.INPUT_TANK_X, state.inputTank());
        drawSteamCrackerTank(draw, graphics, SteamCrackerLayout.OUTPUT_1_TANK_X, state.output1Tank());
        drawSteamCrackerTank(draw, graphics, SteamCrackerLayout.OUTPUT_2_TANK_X, state.output2Tank());
        drawSteamCrackerTank(draw, graphics, SteamCrackerLayout.OUTPUT_3_TANK_X, state.output3Tank());
        drawSteamCrackerTank(draw, graphics, SteamCrackerLayout.OUTPUT_4_TANK_X, state.output4Tank());

        draw.drawSlotFrame(graphics, SteamCrackerLayout.INPUT_SLOT_X, SteamCrackerLayout.SLOT_Y);
        draw.drawSlotFrame(graphics, SteamCrackerLayout.OUTPUT_1_SLOT_X, SteamCrackerLayout.SLOT_Y);
        draw.drawSlotFrame(graphics, SteamCrackerLayout.OUTPUT_2_SLOT_X, SteamCrackerLayout.SLOT_Y);
        draw.drawSlotFrame(graphics, SteamCrackerLayout.OUTPUT_3_SLOT_X, SteamCrackerLayout.SLOT_Y);
        draw.drawSlotFrame(graphics, SteamCrackerLayout.OUTPUT_4_SLOT_X, SteamCrackerLayout.SLOT_Y);
        draw.drawSlotFrame(graphics, SteamCrackerLayout.FUEL_SLOT_X, SteamCrackerLayout.FUEL_SLOT_Y);

        draw.drawHorizontalBar(graphics, SteamCrackerLayout.BURN_X, SteamCrackerLayout.BURN_Y, SteamCrackerLayout.BURN_W, SteamCrackerLayout.BURN_H, state.fuelPixels(), 0xFFFFA53D, 0xFFFFD189);
        draw.drawHorizontalBar(graphics, SteamCrackerLayout.PROGRESS_X, SteamCrackerLayout.PROGRESS_Y, SteamCrackerLayout.PROGRESS_W, SteamCrackerLayout.PROGRESS_H, state.progressPixels(), 0xFF66E0FF, 0xFFA8F2FF);

        draw.drawDumpButton(graphics, SteamCrackerLayout.DUMP_INPUT_X, SteamCrackerLayout.DUMP_BUTTON_Y, SteamCrackerLayout.DUMP_BUTTON_SIZE, state.inputDumpHovered(), state.inputDumpEnabled());
        draw.drawDumpButton(graphics, SteamCrackerLayout.DUMP_OUTPUT_1_X, SteamCrackerLayout.DUMP_BUTTON_Y, SteamCrackerLayout.DUMP_BUTTON_SIZE, state.output1DumpHovered(), state.output1DumpEnabled());
        draw.drawDumpButton(graphics, SteamCrackerLayout.DUMP_OUTPUT_2_X, SteamCrackerLayout.DUMP_BUTTON_Y, SteamCrackerLayout.DUMP_BUTTON_SIZE, state.output2DumpHovered(), state.output2DumpEnabled());
        draw.drawDumpButton(graphics, SteamCrackerLayout.DUMP_OUTPUT_3_X, SteamCrackerLayout.DUMP_BUTTON_Y, SteamCrackerLayout.DUMP_BUTTON_SIZE, state.output3DumpHovered(), state.output3DumpEnabled());
        draw.drawDumpButton(graphics, SteamCrackerLayout.DUMP_OUTPUT_4_X, SteamCrackerLayout.DUMP_BUTTON_Y, SteamCrackerLayout.DUMP_BUTTON_SIZE, state.output4DumpHovered(), state.output4DumpEnabled());
    }

    public static void drawSteamCrackerLabels(AbstractMachineDrawMethods draw, GuiGraphics graphics, Font font, Component title, Component playerInventoryTitle, int inventoryLabelY, String footer) {
        graphics.drawCenteredString(font, title, draw.labelX(SteamCrackerLayout.GUI_WIDTH / 2), draw.labelY(5), 0xFFE5E7EB);
        graphics.drawString(font, "Fuel", draw.labelX(SteamCrackerLayout.CENTER_PANEL_X + 13), draw.labelY(SteamCrackerLayout.FUEL_SLOT_Y - 10), 0xFFB8C0CC, false);
        if (playerInventoryTitle != null) {
            graphics.drawString(font, playerInventoryTitle, draw.labelX(SteamCrackerLayout.PLAYER_INV_X), draw.labelY(inventoryLabelY), 0xFFD0D0D0, false);
        }
        if (footer != null) {
            drawCentered(graphics, font, footer, draw.labelX(5), draw.labelY(steamCrackerHeight(false) - 12), SteamCrackerLayout.GUI_WIDTH - 10, 0xFFE5E7EB);
        }
    }

    public static void drawBTXFractionationTower(AbstractMachineDrawMethods draw, GuiGraphics graphics, BTXFractionationTowerState state, boolean drawPlayerInventory) {
        draw.drawWindow(graphics, BTXFractionationTowerLayout.GUI_WIDTH, btxFractionationTowerHeight(drawPlayerInventory));
        draw.drawPanel(graphics, BTXFractionationTowerLayout.MACHINE_PANEL_X, BTXFractionationTowerLayout.MACHINE_PANEL_Y, BTXFractionationTowerLayout.MACHINE_PANEL_W, BTXFractionationTowerLayout.MACHINE_PANEL_H, 0xFF323232);
        if (drawPlayerInventory) {
            drawStandardInventoryPanels(draw, graphics, BTXFractionationTowerLayout.PLAYER_INV_X, BTXFractionationTowerLayout.PLAYER_INV_Y, 0xFF2C2C2C, 0xFF595959, 0xFF101010);
        }
        draw.drawPanel(graphics, BTXFractionationTowerLayout.CENTER_PANEL_X, BTXFractionationTowerLayout.CENTER_PANEL_Y, BTXFractionationTowerLayout.CENTER_PANEL_W, BTXFractionationTowerLayout.CENTER_PANEL_H, 0xFF262B32);

        drawBtxTank(draw, graphics, BTXFractionationTowerLayout.INPUT_TANK_X, BTXFractionationTowerLayout.INPUT_TANK_Y, state.inputTank());
        drawBtxTank(draw, graphics, BTXFractionationTowerLayout.BENZENE_TANK_X, BTXFractionationTowerLayout.BENZENE_TANK_Y, state.benzeneTank());
        drawBtxTank(draw, graphics, BTXFractionationTowerLayout.TOLUENE_TANK_X, BTXFractionationTowerLayout.TOLUENE_TANK_Y, state.tolueneTank());
        drawBtxTank(draw, graphics, BTXFractionationTowerLayout.XYLENE_TANK_X, BTXFractionationTowerLayout.XYLENE_TANK_Y, state.xyleneTank());

        draw.drawSlotFrame(graphics, BTXFractionationTowerLayout.INPUT_SLOT_X, BTXFractionationTowerLayout.INPUT_SLOT_Y);
        draw.drawSlotFrame(graphics, BTXFractionationTowerLayout.BENZENE_SLOT_X, BTXFractionationTowerLayout.BENZENE_SLOT_Y);
        draw.drawSlotFrame(graphics, BTXFractionationTowerLayout.TOLUENE_SLOT_X, BTXFractionationTowerLayout.TOLUENE_SLOT_Y);
        draw.drawSlotFrame(graphics, BTXFractionationTowerLayout.XYLENE_SLOT_X, BTXFractionationTowerLayout.XYLENE_SLOT_Y);
        draw.drawSlotFrame(graphics, BTXFractionationTowerLayout.FUEL_SLOT_X, BTXFractionationTowerLayout.FUEL_SLOT_Y);

        draw.drawHorizontalBar(graphics, BTXFractionationTowerLayout.PROGRESS_X, BTXFractionationTowerLayout.PROGRESS_Y, BTXFractionationTowerLayout.PROGRESS_W, BTXFractionationTowerLayout.PROGRESS_H, state.progressPixels(), 0xFFB8865F, 0xFFFFD0A6);
        draw.drawVerticalBar(graphics, BTXFractionationTowerLayout.FUEL_BAR_X, BTXFractionationTowerLayout.FUEL_BAR_Y, BTXFractionationTowerLayout.FUEL_BAR_W, BTXFractionationTowerLayout.FUEL_BAR_H, BTXFractionationTowerLayout.FUEL_BAR_INNER_X_OFFSET, BTXFractionationTowerLayout.FUEL_BAR_INNER_Y_OFFSET, BTXFractionationTowerLayout.FUEL_BAR_INNER_W, BTXFractionationTowerLayout.FUEL_BAR_INNER_H, state.fuelPixels(), state.fuelColor(), 0xFFFFC270);

        draw.drawDumpButton(graphics, BTXFractionationTowerLayout.DUMP_INPUT_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, state.inputDumpHovered(), state.inputDumpEnabled());
        draw.drawDumpButton(graphics, BTXFractionationTowerLayout.DUMP_BENZENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, state.benzeneDumpHovered(), state.benzeneDumpEnabled());
        draw.drawDumpButton(graphics, BTXFractionationTowerLayout.DUMP_TOLUENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, state.tolueneDumpHovered(), state.tolueneDumpEnabled());
        draw.drawDumpButton(graphics, BTXFractionationTowerLayout.DUMP_XYLENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, state.xyleneDumpHovered(), state.xyleneDumpEnabled());
    }

    public static void drawBTXFractionationTowerLabels(AbstractMachineDrawMethods draw, GuiGraphics graphics, Font font, Component title, String footer) {
        graphics.drawCenteredString(font, title, draw.labelX(BTXFractionationTowerLayout.GUI_WIDTH / 2), draw.labelY(5), 0xFFFFFFFF);
        if (footer != null) {
            drawCentered(graphics, font, footer, draw.labelX(5), draw.labelY(btxFractionationTowerHeight(false) - 12), BTXFractionationTowerLayout.GUI_WIDTH - 10, 0xFFE5E7EB);
        }
    }

    public static void drawGrowthChamber(AbstractMachineDrawMethods draw, GuiGraphics graphics, GrowthChamberState state, boolean drawPlayerInventory) {
        draw.drawWindow(graphics, GrowthChamberLayout.GUI_WIDTH, growthChamberHeight(drawPlayerInventory));
        draw.drawPanel(graphics, GrowthChamberLayout.MACHINE_PANEL_X, GrowthChamberLayout.MACHINE_PANEL_Y, GrowthChamberLayout.MACHINE_PANEL_W, GrowthChamberLayout.MACHINE_PANEL_H, 0xFF323232);
        if (drawPlayerInventory) {
            drawStandardInventoryPanels(draw, graphics, GrowthChamberLayout.PLAYER_INV_X, GrowthChamberLayout.PLAYER_INV_Y, 0xFF2C2C2C, 0xFF595959, 0xFF101010);
        }

        draw.drawTankFrame(graphics, GrowthChamberLayout.WATER_TANK_X, GrowthChamberLayout.WATER_TANK_Y, GrowthChamberLayout.TANK_W, GrowthChamberLayout.TANK_H, GrowthChamberLayout.TANK_INNER_X_OFFSET, GrowthChamberLayout.TANK_INNER_Y_OFFSET, GrowthChamberLayout.TANK_INNER_W, GrowthChamberLayout.TANK_INNER_H);
        draw.drawTankFillTopLit(graphics, GrowthChamberLayout.WATER_TANK_X, GrowthChamberLayout.WATER_TANK_Y, GrowthChamberLayout.TANK_INNER_X_OFFSET, GrowthChamberLayout.TANK_INNER_Y_OFFSET, GrowthChamberLayout.TANK_INNER_W, GrowthChamberLayout.TANK_INNER_H, state.waterPixels(), 0xFF4F88D6, 0xFFA6C8FF);

        draw.drawSlotFrame(graphics, GrowthChamberLayout.INPUT_SLOT_X, GrowthChamberLayout.INPUT_SLOT_Y);
        draw.drawSlotFrame(graphics, GrowthChamberLayout.BIOMASS_SLOT_X, GrowthChamberLayout.BIOMASS_SLOT_Y);
        draw.drawSlotFrame(graphics, GrowthChamberLayout.MIDDLE_SLOT_X, GrowthChamberLayout.MIDDLE_SLOT_Y);
        draw.drawSlotFrame(graphics, GrowthChamberLayout.FINAL_SLOT_X, GrowthChamberLayout.FINAL_SLOT_Y);
        draw.drawSlotFrame(graphics, GrowthChamberLayout.WATER_INPUT_SLOT_X, GrowthChamberLayout.WATER_INPUT_SLOT_Y);

        draw.drawHorizontalBar(graphics, GrowthChamberLayout.GROWTH_PROGRESS_X, GrowthChamberLayout.GROWTH_PROGRESS_Y, GrowthChamberLayout.GROWTH_PROGRESS_W, GrowthChamberLayout.GROWTH_PROGRESS_H, state.growthPixels(), 0xFF6FBF73, 0xFFB7E0B9);
        draw.drawHorizontalBar(graphics, GrowthChamberLayout.MATURE_PROGRESS_X, GrowthChamberLayout.MATURE_PROGRESS_Y, GrowthChamberLayout.MATURE_PROGRESS_W, GrowthChamberLayout.MATURE_PROGRESS_H, state.maturePixels(), 0xFFB58C5A, 0xFFE4C18F);
    }

    public static void drawGrowthChamberLabels(AbstractMachineDrawMethods draw, GuiGraphics graphics, Font font, Component title, String footer) {
        graphics.drawCenteredString(font, title, draw.labelX(GrowthChamberLayout.GUI_WIDTH / 2), draw.labelY(5), 0xFFFFFFFF);
        graphics.drawCenteredString(font, Component.literal("Water"), draw.labelX(GrowthChamberLayout.WATER_TANK_X + GrowthChamberLayout.TANK_W / 2), draw.labelY(GrowthChamberLayout.WATER_TANK_Y - 10), 0xFF9BB2D1);
        graphics.drawString(font, Component.literal("Growing"), draw.labelX(GrowthChamberLayout.GROWTH_PROGRESS_X), draw.labelY(GrowthChamberLayout.GROWTH_PROGRESS_Y - 10), 0xFFA9D8AC, false);
        graphics.drawString(font, Component.literal("Maturing"), draw.labelX(GrowthChamberLayout.MATURE_PROGRESS_X), draw.labelY(GrowthChamberLayout.MATURE_PROGRESS_Y - 10), 0xFFD7B78E, false);
        if (footer != null) {
            drawCentered(graphics, font, footer, draw.labelX(5), draw.labelY(growthChamberHeight(false) - 12), GrowthChamberLayout.GUI_WIDTH - 10, 0xFFE5E7EB);
        }
    }

    public static void drawGasifier(AbstractMachineDrawMethods draw, GuiGraphics graphics, GasifierState state, boolean drawPlayerInventory) {
        draw.drawWindow(graphics, GasifierLayout.GUI_WIDTH, gasifierHeight(drawPlayerInventory), 0xFF181818, 0xFF262626);
        draw.drawPanel(graphics, GasifierLayout.MACHINE_PANEL_X, GasifierLayout.MACHINE_PANEL_Y, GasifierLayout.MACHINE_PANEL_W, GasifierLayout.MACHINE_PANEL_H, 0xFF323232, 0xFF595959, 0xFF101010);
        if (drawPlayerInventory) {
            drawStandardInventoryPanels(draw, graphics, GasifierLayout.PLAYER_INV_X, GasifierLayout.PLAYER_INV_Y, 0xFF2C2C2C, 0xFF595959, 0xFF101010);
        }
        draw.drawSlotFrame(graphics, GasifierLayout.INPUT_SLOT_X, GasifierLayout.INPUT_SLOT_Y, 0xFF8A8A8A, 0xFF111111);
        draw.drawSlotFrame(graphics, GasifierLayout.FUEL_SLOT_X, GasifierLayout.FUEL_SLOT_Y, 0xFF8A8A8A, 0xFF111111);
        draw.drawSlotFrame(graphics, GasifierLayout.EXPORT_SLOT_X, GasifierLayout.EXPORT_SLOT_Y, 0xFF8A8A8A, 0xFF111111);
        draw.drawVerticalBar(graphics, GasifierLayout.FUEL_BAR_X, GasifierLayout.FUEL_BAR_Y, GasifierLayout.FUEL_BAR_W, GasifierLayout.FUEL_BAR_H, GasifierLayout.FUEL_BAR_INNER_X_OFFSET, GasifierLayout.FUEL_BAR_INNER_Y_OFFSET, GasifierLayout.FUEL_BAR_INNER_W, GasifierLayout.FUEL_BAR_INNER_H, state.fuelPixels(), 0xFFE3A44B, 0xFFFFD28E);
        draw.drawHorizontalBar(graphics, GasifierLayout.PROGRESS_X, GasifierLayout.PROGRESS_Y, GasifierLayout.PROGRESS_W, GasifierLayout.PROGRESS_H, state.progressPixels(), 0xFF88B85D, 0xFFD8EEA9);
        draw.drawTankFrame(graphics, GasifierLayout.OUTPUT_TANK_X, GasifierLayout.OUTPUT_TANK_Y, GasifierLayout.TANK_W, GasifierLayout.TANK_H, GasifierLayout.TANK_INNER_X_OFFSET, GasifierLayout.TANK_INNER_Y_OFFSET, GasifierLayout.TANK_INNER_W, GasifierLayout.TANK_INNER_H);
        drawTankFill(draw, graphics, state.outputTank(), GasifierLayout.OUTPUT_TANK_X, GasifierLayout.OUTPUT_TANK_Y, GasifierLayout.TANK_INNER_X_OFFSET, GasifierLayout.TANK_INNER_Y_OFFSET, GasifierLayout.TANK_INNER_W, GasifierLayout.TANK_INNER_H);
    }

    public static void drawGasifierLabels(AbstractMachineDrawMethods draw, GuiGraphics graphics, Font font, Component title, Component playerInventoryTitle, int inventoryLabelY, String footer) {
        int machineTitleX = GasifierLayout.MACHINE_PANEL_X + (GasifierLayout.MACHINE_PANEL_W - font.width(title)) / 2;
        graphics.drawString(font, title, draw.labelX(machineTitleX), draw.labelY(5), 0xFFFFFFFF, false);
        if (playerInventoryTitle != null) {
            graphics.drawString(font, playerInventoryTitle, draw.labelX(GasifierLayout.PLAYER_INV_X), draw.labelY(inventoryLabelY), 0xFFD0D0D0, false);
        }
        if (footer != null) {
            drawCentered(graphics, font, footer, draw.labelX(5), draw.labelY(gasifierHeight(false) - 12), GasifierLayout.GUI_WIDTH - 10, 0xFFE5E7EB);
        }
    }

    public static void drawSieve(AbstractMachineDrawMethods draw, GuiGraphics graphics, SieveState state, boolean drawPlayerInventory) {
        draw.drawWindow(graphics, SieveLayout.GUI_WIDTH, sieveHeight(drawPlayerInventory), 0xFF181818, 0xFF262626);
        draw.drawPanel(graphics, SieveLayout.MACHINE_PANEL_X, SieveLayout.MACHINE_PANEL_Y, SieveLayout.MACHINE_PANEL_W, SieveLayout.MACHINE_PANEL_H, 0xFF323232, 0xFF595959, 0xFF101010);
        if (drawPlayerInventory) {
            drawStandardInventoryPanels(draw, graphics, SieveLayout.PLAYER_INV_X, SieveLayout.PLAYER_INV_Y, 0xFF2C2C2C, 0xFF595959, 0xFF101010);
        }
        draw.drawSlotFrame(graphics, SieveLayout.INPUT_X, SieveLayout.INPUT_Y, 0xFF8A8A8A, 0xFF111111);
        draw.drawSlotFrame(graphics, SieveLayout.RESULT_X, SieveLayout.RESULT_Y, 0xFF8A8A8A, 0xFF111111);
        draw.drawSlotFrame(graphics, SieveLayout.BONUS_X, SieveLayout.BONUS_Y, 0xFF8A8A8A, 0xFF111111);
        drawSieveWidget(draw, graphics, state.knobCenterY());
    }

    public static void drawSieve(AbstractMachineDrawMethods draw, GuiGraphics graphics, int knobCenterY, boolean drawPlayerInventory) {
        drawSieve(draw, graphics, new SieveState(knobCenterY), drawPlayerInventory);
    }

    public static void drawSieveLabels(AbstractMachineDrawMethods draw, GuiGraphics graphics, Font font, Component title, Component playerInventoryTitle, int inventoryLabelY, String footer) {
        int machineTitleX = SieveLayout.MACHINE_PANEL_X + (SieveLayout.MACHINE_PANEL_W - font.width(title)) / 2;
        graphics.drawString(font, title, draw.labelX(machineTitleX), draw.labelY(5), 0xFFFFFFFF, false);
        if (playerInventoryTitle != null) {
            graphics.drawString(font, playerInventoryTitle, draw.labelX(SieveLayout.PLAYER_INV_X), draw.labelY(inventoryLabelY), 0xFFD0D0D0, false);
        }
        if (footer != null) {
            drawCentered(graphics, font, footer, draw.labelX(5), draw.labelY(sieveHeight(false) - 12), SieveLayout.GUI_WIDTH - 10, 0xFFE5E7EB);
        }
    }

    public static int advancedFurnaceHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? AdvancedFurnaceLayout.GUI_HEIGHT
                : AdvancedFurnaceLayout.MACHINE_PANEL_Y + AdvancedFurnaceLayout.MACHINE_PANEL_H + 14;
    }

    public static int aromaticExtractorHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? AromaticExtractorLayout.GUI_HEIGHT
                : AromaticExtractorLayout.MACHINE_PANEL_Y + AromaticExtractorLayout.MACHINE_PANEL_H + 14;
    }

    public static int biochemicalReactorHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? BiochemicalReactorLayout.GUI_HEIGHT
                : BiochemicalReactorLayout.MACHINE_PANEL_Y + BiochemicalReactorLayout.MACHINE_PANEL_H + 14;
    }

    public static int advancedMixingVatHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? AdvancedMixingVatLayout.GUI_HEIGHT
                : AdvancedMixingVatLayout.MACHINE_PANEL_Y + AdvancedMixingVatLayout.MACHINE_PANEL_H + 14;
    }

    public static int centrifugeHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? CentrifugeLayout.GUI_HEIGHT
                : CentrifugeLayout.MACHINE_PANEL_Y + CentrifugeLayout.MACHINE_PANEL_H + 14;
    }

    public static int chemicalReactorHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? ChemicalReactorLayout.GUI_HEIGHT
                : ChemicalReactorLayout.MACHINE_PANEL_Y + ChemicalReactorLayout.MACHINE_PANEL_H + 14;
    }

    public static int distillerHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? DistillerLayout.GUI_HEIGHT
                : DistillerLayout.MACHINE_PANEL_Y + DistillerLayout.MACHINE_PANEL_H + 14;
    }

    public static int electrolyzerHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? ElectrolyzerLayout.GUI_HEIGHT
                : ElectrolyzerLayout.MACHINE_PANEL_Y + ElectrolyzerLayout.MACHINE_PANEL_H + 14;
    }

    public static int fluidFiltererHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? FluidFiltererLayout.GUI_HEIGHT
                : FluidFiltererLayout.MACHINE_PANEL_Y + FluidFiltererLayout.MACHINE_PANEL_H + 14;
    }

    public static int catalyticReformerHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? CatalyticReformerLayout.GUI_HEIGHT
                : CatalyticReformerLayout.MACHINE_PANEL_Y + CatalyticReformerLayout.MACHINE_PANEL_H + 14;
    }

    public static int steamCrackerHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? SteamCrackerLayout.GUI_HEIGHT
                : SteamCrackerLayout.MACHINE_PANEL_Y + SteamCrackerLayout.MACHINE_PANEL_H + 14;
    }

    public static int btxFractionationTowerHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? BTXFractionationTowerLayout.GUI_HEIGHT
                : BTXFractionationTowerLayout.MACHINE_PANEL_Y + BTXFractionationTowerLayout.MACHINE_PANEL_H + 14;
    }

    public static int growthChamberHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? GrowthChamberLayout.GUI_HEIGHT
                : GrowthChamberLayout.MACHINE_PANEL_Y + GrowthChamberLayout.MACHINE_PANEL_H + 14;
    }

    public static int gasifierHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? GasifierLayout.GUI_HEIGHT
                : GasifierLayout.MACHINE_PANEL_Y + GasifierLayout.MACHINE_PANEL_H + 14;
    }

    public static int sieveHeight(boolean includePlayerInventory) {
        return includePlayerInventory
                ? SieveLayout.GUI_HEIGHT
                : SieveLayout.MACHINE_PANEL_Y + SieveLayout.MACHINE_PANEL_H + 14;
    }

    private static void drawAdvancedFurnaceInventoryPanels(AbstractMachineDrawMethods draw, GuiGraphics graphics) {
        draw.drawPanel(
                graphics,
                AdvancedFurnaceLayout.PLAYER_INV_X,
                AdvancedFurnaceLayout.PLAYER_INV_Y,
                StandardInventoryLayout.PLAYER_INV_PANEL_W,
                StandardInventoryLayout.PLAYER_INV_PANEL_H,
                0xFF2A2D33,
                0xFF5C616B,
                0xFF0E1014
        );

        draw.drawPanel(
                graphics,
                AdvancedFurnaceLayout.PLAYER_INV_X,
                StandardInventoryLayout.hotbarPanelY(AdvancedFurnaceLayout.PLAYER_INV_Y),
                StandardInventoryLayout.HOTBAR_PANEL_W,
                StandardInventoryLayout.HOTBAR_PANEL_H,
                0xFF2A2D33,
                0xFF5C616B,
                0xFF0E1014
        );
    }

    private static void drawAdvancedFurnaceTank(AbstractMachineDrawMethods draw, GuiGraphics graphics, TankFill fill) {
        drawTankFill(
                draw,
                graphics,
                fill,
                AdvancedFurnaceLayout.TANK_X,
                AdvancedFurnaceLayout.TANK_Y,
                StandardTankLayout.INNER_X,
                StandardTankLayout.INNER_Y,
                StandardTankLayout.INNER_W,
                StandardTankLayout.INNER_H
        );
    }

    private static void drawStandardInventoryPanels(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            int x,
            int y,
            int fillColor,
            int lightBorderColor,
            int darkBorderColor
    ) {
        draw.drawPanel(
                graphics,
                x,
                y,
                StandardInventoryLayout.PLAYER_INV_PANEL_W,
                StandardInventoryLayout.PLAYER_INV_PANEL_H,
                fillColor,
                lightBorderColor,
                darkBorderColor
        );

        draw.drawPanel(
                graphics,
                x,
                StandardInventoryLayout.hotbarPanelY(y),
                StandardInventoryLayout.HOTBAR_PANEL_W,
                StandardInventoryLayout.HOTBAR_PANEL_H,
                fillColor,
                lightBorderColor,
                darkBorderColor
        );
    }

    private static void drawTankFill(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            TankFill fill,
            int tankX,
            int tankY,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH
    ) {
        if (fill == null || fill.isEmpty()) {
            return;
        }

        if (fill.filledPixels() >= 0) {
            int color = fill.color() != 0 ? fill.color() : draw.getFluidColor(fill.fluid());
            if (fill.topLit()) {
                draw.drawTankFillTopLit(
                        graphics,
                        tankX,
                        tankY,
                        innerOffsetX,
                        innerOffsetY,
                        innerW,
                        innerH,
                        fill.filledPixels(),
                        color,
                        draw.lighten(color, 1.20f)
                );
            } else {
                draw.drawTankFillShaded(
                        graphics,
                        tankX,
                        tankY,
                        innerOffsetX,
                        innerOffsetY,
                        innerW,
                        innerH,
                        fill.filledPixels(),
                        color
                );
            }
            return;
        }

        if (fill.gasId() != null) {
            draw.drawGasTankPreview(
                    graphics,
                    fill.gasId(),
                    fill.amount(),
                    fill.capacity(),
                    tankX,
                    tankY,
                    innerOffsetX,
                    innerOffsetY,
                    innerW,
                    innerH
            );
            return;
        }

        draw.drawFluidTankPreview(
                graphics,
                fill.fluid(),
                (int) fill.amount(),
                (int) fill.capacity(),
                tankX,
                tankY,
                innerOffsetX,
                innerOffsetY,
                innerW,
                innerH
        );
    }

    private static void drawSlotCount(AbstractMachineDrawMethods draw, GuiGraphics graphics, int slotX, int slotY, int count) {
        if (count <= 1) {
            return;
        }
        graphics.drawString(Minecraft.getInstance().font, "x" + count, draw.guiX(slotX + 9), draw.guiY(slotY + 10), 0xFFF0D57A, false);
    }

    private static int[] advancedMixingVatItemX() {
        return new int[]{
                AdvancedMixingVatLayout.ITEM_0_X,
                AdvancedMixingVatLayout.ITEM_1_X,
                AdvancedMixingVatLayout.ITEM_2_X,
                AdvancedMixingVatLayout.ITEM_3_X
        };
    }

    private static int[] advancedMixingVatItemY() {
        return new int[]{
                AdvancedMixingVatLayout.ITEM_0_Y,
                AdvancedMixingVatLayout.ITEM_1_Y,
                AdvancedMixingVatLayout.ITEM_2_Y,
                AdvancedMixingVatLayout.ITEM_3_Y
        };
    }

    private static int[] advancedMixingVatTankX() {
        return new int[]{
                AdvancedMixingVatLayout.TANK_A_X,
                AdvancedMixingVatLayout.TANK_B_X,
                AdvancedMixingVatLayout.TANK_C_X,
                AdvancedMixingVatLayout.GAS_X,
                AdvancedMixingVatLayout.OUTPUT_X
        };
    }

    private static int[] advancedMixingVatTankSlotX() {
        return new int[]{
                AdvancedMixingVatLayout.TANK_A_SLOT_X,
                AdvancedMixingVatLayout.TANK_B_SLOT_X,
                AdvancedMixingVatLayout.TANK_C_SLOT_X,
                AdvancedMixingVatLayout.GAS_SLOT_X,
                AdvancedMixingVatLayout.OUTPUT_SLOT_X
        };
    }

    private static void drawDistillerReactor(AbstractMachineDrawMethods draw, GuiGraphics graphics, boolean hovered, boolean working, boolean boosted) {
        int x = DistillerLayout.RUN_BUTTON_X;
        int y = DistillerLayout.RUN_BUTTON_Y;
        int cx = x + DistillerLayout.RUN_BUTTON_SIZE / 2;
        int cy = y + DistillerLayout.RUN_BUTTON_SIZE / 2;

        if (hovered) {
            graphics.fill(draw.guiX(x + 2), draw.guiY(y + 2), draw.guiX(x + DistillerLayout.RUN_BUTTON_SIZE - 2), draw.guiY(y + DistillerLayout.RUN_BUTTON_SIZE - 2), 0x16FFFFFF);
        }

        draw.drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS + 2, 0xFF818793);
        draw.drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS, 0xFF20242B);
        draw.drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS - 3, 0xFF9FA7B4);
        draw.drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS - 5, 0xFF3E4652);

        int coreColor = boosted ? 0xFF6FD6FF : working ? 0xFFE8E8E8 : 0xFF90959E;
        draw.drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_INNER_RADIUS, 0xFF2D333B);
        draw.drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_CORE_RADIUS, coreColor);

        if (boosted) {
            draw.drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_CORE_RADIUS + 3, 0x336FD6FF);
        }
    }

    private static void drawCatalyticTank(AbstractMachineDrawMethods draw, GuiGraphics graphics, int tankX, TankFill fill) {
        draw.drawTankFrame(
                graphics,
                tankX,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_W,
                CatalyticReformerLayout.TANK_H,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H
        );
        drawTankFill(
                draw,
                graphics,
                fill,
                tankX,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H
        );
    }

    private static void drawSteamCrackerTank(AbstractMachineDrawMethods draw, GuiGraphics graphics, int tankX, TankFill fill) {
        draw.drawTankFrame(
                graphics,
                tankX,
                SteamCrackerLayout.TANK_Y,
                SteamCrackerLayout.TANK_W,
                SteamCrackerLayout.TANK_H,
                StandardTankLayout.INNER_X,
                StandardTankLayout.INNER_Y,
                StandardTankLayout.INNER_W,
                SteamCrackerLayout.TANK_INNER_H
        );
        drawTankFill(
                draw,
                graphics,
                fill,
                tankX,
                SteamCrackerLayout.TANK_Y,
                StandardTankLayout.INNER_X,
                StandardTankLayout.INNER_Y,
                StandardTankLayout.INNER_W,
                SteamCrackerLayout.TANK_INNER_H
        );
    }

    private static void drawBtxTank(AbstractMachineDrawMethods draw, GuiGraphics graphics, int tankX, int tankY, TankFill fill) {
        draw.drawTankFrame(
                graphics,
                tankX,
                tankY,
                BTXFractionationTowerLayout.TANK_W,
                BTXFractionationTowerLayout.TANK_H,
                BTXFractionationTowerLayout.TANK_INNER_X_OFFSET,
                BTXFractionationTowerLayout.TANK_INNER_Y_OFFSET,
                BTXFractionationTowerLayout.TANK_INNER_W,
                BTXFractionationTowerLayout.TANK_INNER_H
        );
        drawTankFill(
                draw,
                graphics,
                fill,
                tankX,
                tankY,
                BTXFractionationTowerLayout.TANK_INNER_X_OFFSET,
                BTXFractionationTowerLayout.TANK_INNER_Y_OFFSET,
                BTXFractionationTowerLayout.TANK_INNER_W,
                BTXFractionationTowerLayout.TANK_INNER_H
        );
    }

    private static void drawSieveWidget(AbstractMachineDrawMethods draw, GuiGraphics graphics, int knobCenterY) {
        int trackLeft = draw.guiX(SieveLayout.WIDGET_X);
        int trackTop = draw.guiY(SieveLayout.WIDGET_Y);

        graphics.fill(trackLeft - 2, trackTop - 2, trackLeft + SieveLayout.WIDGET_W + 2, trackTop + SieveLayout.WIDGET_H + 2, 0xFF5A5A5A);
        graphics.fill(trackLeft - 1, trackTop - 1, trackLeft + SieveLayout.WIDGET_W + 1, trackTop + SieveLayout.WIDGET_H + 1, 0xFF161616);
        graphics.fill(trackLeft + 7, trackTop + 2, trackLeft + 11, trackTop + SieveLayout.WIDGET_H - 2, 0xFF090909);
        graphics.fill(trackLeft + 5, trackTop + 1, trackLeft + 13, trackTop + 3, 0xFF727272);
        graphics.fill(trackLeft + 5, trackTop + SieveLayout.WIDGET_H - 3, trackLeft + 13, trackTop + SieveLayout.WIDGET_H - 1, 0xFF0E0E0E);

        int centerX = SieveLayout.WIDGET_X + SieveLayout.WIDGET_W / 2;
        draw.drawCircle(graphics, centerX, knobCenterY, 6, 0xFFBABABA);
        draw.drawCircle(graphics, centerX, knobCenterY, 5, 0xFF3B3B3B);
        draw.drawCircle(graphics, centerX - 1, knobCenterY - 1, 1, 0xFFE8E8E8);
    }

    private static void drawCentered(GuiGraphics graphics, Font font, String text, int x, int y, int width, int color) {
        String clipped = font.plainSubstrByWidth(text, width);
        graphics.drawString(font, clipped, x + Math.max(0, (width - font.width(clipped)) / 2), y, color, false);
    }

}
