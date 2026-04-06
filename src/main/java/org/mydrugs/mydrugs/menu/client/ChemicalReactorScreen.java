package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.ChemicalReactorBlockEntity;
import org.mydrugs.mydrugs.menu.ChemicalReactorMenu;
import org.mydrugs.mydrugs.menu.layout.ChemicalReactorLayout;

public class ChemicalReactorScreen extends AbstractMachineScreen<ChemicalReactorMenu> {
    public ChemicalReactorScreen(ChemicalReactorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, ChemicalReactorLayout.GUI_WIDTH, ChemicalReactorLayout.GUI_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);

        drawPanel(
                graphics,
                ChemicalReactorLayout.MACHINE_PANEL_X,
                ChemicalReactorLayout.MACHINE_PANEL_Y,
                ChemicalReactorLayout.MACHINE_PANEL_W,
                ChemicalReactorLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        drawSieveInventoryPanels(
                graphics,
                ChemicalReactorLayout.PLAYER_INV_X,
                ChemicalReactorLayout.PLAYER_INV_Y
        );

        drawTankFrame(
                graphics,
                ChemicalReactorLayout.PRIMARY_GAS_TANK_X,
                ChemicalReactorLayout.PRIMARY_GAS_TANK_Y,
                ChemicalReactorLayout.TANK_W,
                ChemicalReactorLayout.TANK_H,
                ChemicalReactorLayout.TANK_INNER_X_OFFSET,
                ChemicalReactorLayout.TANK_INNER_Y_OFFSET,
                ChemicalReactorLayout.TANK_INNER_W,
                ChemicalReactorLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                ChemicalReactorLayout.SECONDARY_TANK_X,
                ChemicalReactorLayout.SECONDARY_TANK_Y,
                ChemicalReactorLayout.TANK_W,
                ChemicalReactorLayout.TANK_H,
                ChemicalReactorLayout.TANK_INNER_X_OFFSET,
                ChemicalReactorLayout.TANK_INNER_Y_OFFSET,
                ChemicalReactorLayout.TANK_INNER_W,
                ChemicalReactorLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                ChemicalReactorLayout.OUTPUT_TANK_X,
                ChemicalReactorLayout.OUTPUT_TANK_Y,
                ChemicalReactorLayout.TANK_W,
                ChemicalReactorLayout.TANK_H,
                ChemicalReactorLayout.TANK_INNER_X_OFFSET,
                ChemicalReactorLayout.TANK_INNER_Y_OFFSET,
                ChemicalReactorLayout.TANK_INNER_W,
                ChemicalReactorLayout.TANK_INNER_H
        );

        ChemicalReactorBlockEntity blockEntity = getBlockEntity();
        if (blockEntity != null) {
            drawTankFillShaded(
                    graphics,
                    ChemicalReactorLayout.PRIMARY_GAS_TANK_X,
                    ChemicalReactorLayout.PRIMARY_GAS_TANK_Y,
                    ChemicalReactorLayout.TANK_INNER_X_OFFSET,
                    ChemicalReactorLayout.TANK_INNER_Y_OFFSET,
                    ChemicalReactorLayout.TANK_INNER_W,
                    ChemicalReactorLayout.TANK_INNER_H,
                    blockEntity.getScaledPrimaryGas(ChemicalReactorLayout.TANK_INNER_H),
                    blockEntity.getPrimaryGasColor()
            );

            int secondaryPixels = blockEntity.isSecondaryFluidMode()
                    ? blockEntity.getScaledSecondaryFluid(ChemicalReactorLayout.TANK_INNER_H)
                    : blockEntity.getScaledSecondaryGas(ChemicalReactorLayout.TANK_INNER_H);

            int secondaryColor = blockEntity.isSecondaryFluidMode()
                    ? getFluidColor(blockEntity.getSecondaryFluid())
                    : blockEntity.getSecondaryGasColor();

            drawTankFillShaded(
                    graphics,
                    ChemicalReactorLayout.SECONDARY_TANK_X,
                    ChemicalReactorLayout.SECONDARY_TANK_Y,
                    ChemicalReactorLayout.TANK_INNER_X_OFFSET,
                    ChemicalReactorLayout.TANK_INNER_Y_OFFSET,
                    ChemicalReactorLayout.TANK_INNER_W,
                    ChemicalReactorLayout.TANK_INNER_H,
                    secondaryPixels,
                    secondaryColor
            );

            int outputPixels = blockEntity.isOutputFluidMode()
                    ? blockEntity.getScaledOutputFluid(ChemicalReactorLayout.TANK_INNER_H)
                    : blockEntity.getScaledOutputGas(ChemicalReactorLayout.TANK_INNER_H);

            int outputColor = blockEntity.isOutputFluidMode()
                    ? getFluidColor(blockEntity.getOutputFluid())
                    : blockEntity.getOutputGasColor();

            drawTankFillShaded(
                    graphics,
                    ChemicalReactorLayout.OUTPUT_TANK_X,
                    ChemicalReactorLayout.OUTPUT_TANK_Y,
                    ChemicalReactorLayout.TANK_INNER_X_OFFSET,
                    ChemicalReactorLayout.TANK_INNER_Y_OFFSET,
                    ChemicalReactorLayout.TANK_INNER_W,
                    ChemicalReactorLayout.TANK_INNER_H,
                    outputPixels,
                    outputColor
            );
        }

        drawSlotFrame(graphics, ChemicalReactorLayout.FUEL_SLOT_X, ChemicalReactorLayout.FUEL_SLOT_Y);

        drawHorizontalBar(
                graphics,
                ChemicalReactorLayout.PROGRESS_X,
                ChemicalReactorLayout.PROGRESS_Y,
                ChemicalReactorLayout.PROGRESS_W,
                ChemicalReactorLayout.PROGRESS_H,
                this.menu.getScaledProgress(ChemicalReactorLayout.PROGRESS_W),
                0xFF85A6C9,
                0xFFC6DCF2
        );

        drawVerticalBar(
                graphics,
                ChemicalReactorLayout.HEAT_BAR_X,
                ChemicalReactorLayout.HEAT_BAR_Y,
                ChemicalReactorLayout.HEAT_BAR_W,
                ChemicalReactorLayout.HEAT_BAR_H,
                ChemicalReactorLayout.HEAT_BAR_INNER_X_OFFSET,
                ChemicalReactorLayout.HEAT_BAR_INNER_Y_OFFSET,
                ChemicalReactorLayout.HEAT_BAR_INNER_W,
                ChemicalReactorLayout.HEAT_BAR_INNER_H,
                this.menu.getScaledHeat(ChemicalReactorLayout.HEAT_BAR_INNER_H),
                0xFFE35C3F,
                0xFFFFB870
        );

        drawVerticalBar(
                graphics,
                ChemicalReactorLayout.FUEL_BAR_X,
                ChemicalReactorLayout.FUEL_BAR_Y,
                ChemicalReactorLayout.FUEL_BAR_W,
                ChemicalReactorLayout.FUEL_BAR_H,
                ChemicalReactorLayout.FUEL_BAR_INNER_X_OFFSET,
                ChemicalReactorLayout.FUEL_BAR_INNER_Y_OFFSET,
                ChemicalReactorLayout.FUEL_BAR_INNER_W,
                ChemicalReactorLayout.FUEL_BAR_INNER_H,
                this.menu.getScaledBurnTime(ChemicalReactorLayout.FUEL_BAR_INNER_H),
                this.menu.isLit() ? 0xFFE38D3F : 0xFF8E6A4A,
                0xFFFFC270
        );

        drawHorizontalBar(
                graphics,
                ChemicalReactorLayout.MANUAL_BAR_X,
                ChemicalReactorLayout.MANUAL_BAR_Y,
                ChemicalReactorLayout.MANUAL_BAR_W,
                ChemicalReactorLayout.MANUAL_BAR_H,
                this.menu.getScaledManualEnergy(ChemicalReactorLayout.MANUAL_BAR_W),
                0xFF63B36D,
                0xFFA8E4AF
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xE0E0E0, false);
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ChemicalReactorBlockEntity blockEntity = getBlockEntity();
        if (blockEntity == null) {
            return;
        }

        if (isHoveringBox(ChemicalReactorLayout.PRIMARY_GAS_TANK_X, ChemicalReactorLayout.PRIMARY_GAS_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Primary gas"),
                    Component.literal(blockEntity.getPrimaryGasName()),
                    Component.literal(blockEntity.getPrimaryGasAmount() + " / " + ChemicalReactorBlockEntity.GAS_TANK_CAPACITY)
            );
        } else if (isHoveringBox(ChemicalReactorLayout.SECONDARY_TANK_X, ChemicalReactorLayout.SECONDARY_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            if (blockEntity.isSecondaryFluidMode()) {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Secondary fluid"),
                        Component.literal(getFluidName(blockEntity.getSecondaryFluid().getFluid())),
                        Component.literal(blockEntity.getSecondaryFluidAmount() + " / " + ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY + " mB")
                );
            } else {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Secondary gas"),
                        Component.literal(blockEntity.getSecondaryGasName()),
                        Component.literal(blockEntity.getSecondaryGasAmount() + " / " + ChemicalReactorBlockEntity.GAS_TANK_CAPACITY)
                );
            }
        } else if (isHoveringBox(ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.OUTPUT_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            if (blockEntity.isOutputFluidMode()) {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output fluid"),
                        Component.literal(getFluidName(blockEntity.getOutputFluid().getFluid())),
                        Component.literal(blockEntity.getOutputFluidAmount() + " / " + ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY + " mB")
                );
            } else {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output gas"),
                        Component.literal(blockEntity.getOutputGasName()),
                        Component.literal(blockEntity.getOutputGasAmount() + " / " + ChemicalReactorBlockEntity.GAS_TANK_CAPACITY)
                );
            }
        } else if (isHoveringBox(ChemicalReactorLayout.PROGRESS_X, ChemicalReactorLayout.PROGRESS_Y, ChemicalReactorLayout.PROGRESS_W, ChemicalReactorLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        } else if (isHoveringBox(ChemicalReactorLayout.HEAT_BAR_X, ChemicalReactorLayout.HEAT_BAR_Y, ChemicalReactorLayout.HEAT_BAR_W, ChemicalReactorLayout.HEAT_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Heat"),
                    Component.literal(this.menu.getHeat() + " / " + this.menu.getMaxHeat())
            );
        } else if (isHoveringBox(ChemicalReactorLayout.FUEL_BAR_X, ChemicalReactorLayout.FUEL_BAR_Y, ChemicalReactorLayout.FUEL_BAR_W, ChemicalReactorLayout.FUEL_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Fuel burn time"),
                    Component.literal(this.menu.getBurnTimeRemaining() + " / " + this.menu.getBurnTimeTotal() + " ticks")
            );
        } else if (isHoveringBox(ChemicalReactorLayout.MANUAL_BAR_X, ChemicalReactorLayout.MANUAL_BAR_Y, ChemicalReactorLayout.MANUAL_BAR_W, ChemicalReactorLayout.MANUAL_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Manual boost"),
                    Component.literal(this.menu.getManualEnergy() + " / " + this.menu.getMaxManualEnergy())
            );
        }
    }

    private ChemicalReactorBlockEntity getBlockEntity() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return null;
        }
        return this.menu.getBlockEntity(this.minecraft.player);
    }
}