package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.ChemicalReactorBlockEntity;
import org.mydrugs.mydrugs.menu.ChemicalReactorMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.ChemicalReactorLayout;

public class ChemicalReactorScreen extends AbstractMachineScreen<ChemicalReactorMenu> {
    private static final int PANEL_COLOR = 0xFF323232;
    private static final int DIVIDER_COLOR = 0xFF4A4A4A;
    private static final int TITLE_COLOR = 0xFFE0E0E0;
    private static final int LABEL_COLOR = 0xFFB8B8B8;

    public ChemicalReactorScreen(ChemicalReactorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, ChemicalReactorLayout.GUI_WIDTH, ChemicalReactorLayout.GUI_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int secondaryPixels = this.menu.isSecondaryFluidMode()
                ? this.menu.getScaledSecondaryFluid(ChemicalReactorLayout.TANK_INNER_H)
                : this.menu.getScaledSecondaryGas(ChemicalReactorLayout.TANK_INNER_H);
        int secondaryColor = this.menu.isSecondaryFluidMode()
                ? getFluidColor(this.menu.getSecondaryFluid())
                : this.menu.getSecondaryGasColor();
        int outputPixels = this.menu.isOutputFluidMode()
                ? this.menu.getScaledOutputFluid(ChemicalReactorLayout.TANK_INNER_H)
                : this.menu.getScaledOutputGas(ChemicalReactorLayout.TANK_INNER_H);
        int outputColor = this.menu.isOutputFluidMode()
                ? getFluidColor(this.menu.getOutputFluid())
                : this.menu.getOutputGasColor();

        MachineGuiRenderer.drawChemicalReactor(
                this,
                graphics,
                new MachineGuiRenderer.ChemicalReactorState(
                        MachineGuiRenderer.TankFill.liveColor(this.menu.getScaledPrimaryGas(ChemicalReactorLayout.TANK_INNER_H), this.menu.getPrimaryGasColor()),
                        MachineGuiRenderer.TankFill.liveColor(secondaryPixels, secondaryColor),
                        MachineGuiRenderer.TankFill.liveColor(outputPixels, outputColor),
                        this.menu.getScaledProgress(ChemicalReactorLayout.PROGRESS_W),
                        this.menu.getScaledHeat(ChemicalReactorLayout.HEAT_BAR_INNER_H),
                        this.menu.getScaledBurnTime(ChemicalReactorLayout.FUEL_BAR_INNER_H),
                        this.menu.isLit() ? 0xFFE38D3F : 0xFF8E6A4A,
                        this.menu.getScaledManualEnergy(ChemicalReactorLayout.MANUAL_BAR_W)
                ),
                true
        );
    }

    private void drawSectionDivider(GuiGraphics graphics, int x) {
        graphics.fill(
                x,
                ChemicalReactorLayout.DIVIDER_Y,
                x + 1,
                ChemicalReactorLayout.DIVIDER_Y + ChemicalReactorLayout.DIVIDER_H,
                DIVIDER_COLOR
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawChemicalReactorLabels(this, graphics, this.font, this.title, null);
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(ChemicalReactorLayout.PRIMARY_GAS_TANK_X, ChemicalReactorLayout.PRIMARY_GAS_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Primary input gas"),
                    Component.literal(this.menu.getPrimaryGasName()),
                    Component.literal(this.menu.getPrimaryGasAmount() + " / " + ChemicalReactorBlockEntity.GAS_TANK_CAPACITY)
            );
        } else if (isHoveringBox(ChemicalReactorLayout.SECONDARY_TANK_X, ChemicalReactorLayout.SECONDARY_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            if (this.menu.isSecondaryFluidMode()) {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Secondary input fluid"),
                        Component.literal(getFluidName(this.menu.getSecondaryFluid())),
                        Component.literal(this.menu.getSecondaryFluidAmount() + " / " + ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY + " mB")
                );
            } else {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Secondary input gas"),
                        Component.literal(this.menu.getSecondaryGasName()),
                        Component.literal(this.menu.getSecondaryGasAmount() + " / " + ChemicalReactorBlockEntity.GAS_TANK_CAPACITY)
                );
            }
        } else if (isHoveringBox(ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.OUTPUT_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            if (this.menu.isOutputFluidMode()) {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output fluid"),
                        Component.literal(getFluidName(this.menu.getOutputFluid())),
                        Component.literal(this.menu.getOutputFluidAmount() + " / " + ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY + " mB")
                );
            } else {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output gas"),
                        Component.literal(this.menu.getOutputGasName()),
                        Component.literal(this.menu.getOutputGasAmount() + " / " + ChemicalReactorBlockEntity.GAS_TANK_CAPACITY)
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
}
