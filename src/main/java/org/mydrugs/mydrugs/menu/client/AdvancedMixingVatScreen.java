package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.AdvancedMixingVatBlockEntity;
import org.mydrugs.mydrugs.menu.AdvancedMixingVatMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.AdvancedMixingVatLayout;

public class AdvancedMixingVatScreen extends AbstractMachineScreen<AdvancedMixingVatMenu> {
    public AdvancedMixingVatScreen(AdvancedMixingVatMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, AdvancedMixingVatLayout.GUI_WIDTH, AdvancedMixingVatLayout.GUI_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineGuiRenderer.drawAdvancedMixingVat(
                this,
                graphics,
                MachineGuiRenderer.AdvancedMixingVatState.screen(
                        this.menu.getInputAFluid(),
                        this.menu.getScaledTank(this.menu.getInputATankAmount(), AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY, AdvancedMixingVatLayout.TANK_INNER_H),
                        this.menu.getInputBFluid(),
                        this.menu.getScaledTank(this.menu.getInputBTankAmount(), AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY, AdvancedMixingVatLayout.TANK_INNER_H),
                        this.menu.getInputCFluid(),
                        this.menu.getScaledTank(this.menu.getInputCTankAmount(), AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY, AdvancedMixingVatLayout.TANK_INNER_H),
                        this.menu.getScaledTank(this.menu.getGasAmount(), (int) AdvancedMixingVatBlockEntity.GAS_TANK_CAPACITY, AdvancedMixingVatLayout.TANK_INNER_H),
                        this.menu.getOutputFluid(),
                        this.menu.getScaledTank(this.menu.getOutputTankAmount(), AdvancedMixingVatBlockEntity.OUTPUT_TANK_CAPACITY, AdvancedMixingVatLayout.TANK_INNER_H),
                        this.menu.getScaledProgress(AdvancedMixingVatLayout.PROGRESS_W)
                ),
                true
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawAdvancedMixingVatLabels(this, graphics, this.font, this.title, null);
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(
                AdvancedMixingVatLayout.TANK_A_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Fluid Input A"),
                    Component.literal(getFluidName(this.menu.getInputAFluid())),
                    Component.literal(this.menu.getInputATankAmount() + " / " + AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(
                AdvancedMixingVatLayout.TANK_B_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Fluid Input B"),
                    Component.literal(getFluidName(this.menu.getInputBFluid())),
                    Component.literal(this.menu.getInputBTankAmount() + " / " + AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(
                AdvancedMixingVatLayout.TANK_C_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Fluid Input C"),
                    Component.literal(getFluidName(this.menu.getInputCFluid())),
                    Component.literal(this.menu.getInputCTankAmount() + " / " + AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(
                AdvancedMixingVatLayout.GAS_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Gas Input"),
                    Component.literal(this.menu.getGasAmount() + " / " + AdvancedMixingVatBlockEntity.GAS_TANK_CAPACITY + " units")
            );
        } else if (isHoveringBox(
                AdvancedMixingVatLayout.OUTPUT_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Fluid Output"),
                    Component.literal(getFluidName(this.menu.getOutputFluid())),
                    Component.literal(this.menu.getOutputTankAmount() + " / " + AdvancedMixingVatBlockEntity.OUTPUT_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(
                AdvancedMixingVatLayout.PROGRESS_X,
                AdvancedMixingVatLayout.PROGRESS_Y,
                AdvancedMixingVatLayout.PROGRESS_W,
                AdvancedMixingVatLayout.PROGRESS_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Mixing Progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        }
    }
}
