package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.AdvancedMixingVatBlockEntity;
import org.mydrugs.mydrugs.menu.AdvancedMixingVatMenu;
import org.mydrugs.mydrugs.menu.layout.AdvancedMixingVatLayout;

public class AdvancedMixingVatScreen extends AbstractMachineScreen<AdvancedMixingVatMenu> {
    public AdvancedMixingVatScreen(AdvancedMixingVatMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, AdvancedMixingVatLayout.GUI_WIDTH, AdvancedMixingVatLayout.GUI_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);

        drawPanel(
                graphics,
                AdvancedMixingVatLayout.MACHINE_PANEL_X,
                AdvancedMixingVatLayout.MACHINE_PANEL_Y,
                AdvancedMixingVatLayout.MACHINE_PANEL_W,
                AdvancedMixingVatLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        drawSieveInventoryPanels(
                graphics,
                AdvancedMixingVatLayout.PLAYER_INV_X,
                AdvancedMixingVatLayout.PLAYER_INV_Y
        );

        drawSlotFrame(graphics, AdvancedMixingVatLayout.ITEM_0_X, AdvancedMixingVatLayout.ITEM_0_Y);
        drawSlotFrame(graphics, AdvancedMixingVatLayout.ITEM_1_X, AdvancedMixingVatLayout.ITEM_1_Y);
        drawSlotFrame(graphics, AdvancedMixingVatLayout.ITEM_2_X, AdvancedMixingVatLayout.ITEM_2_Y);
        drawSlotFrame(graphics, AdvancedMixingVatLayout.ITEM_3_X, AdvancedMixingVatLayout.ITEM_3_Y);

        drawSlotFrame(graphics, AdvancedMixingVatLayout.TANK_A_SLOT_X, AdvancedMixingVatLayout.TANK_SLOT_Y);
        drawSlotFrame(graphics, AdvancedMixingVatLayout.TANK_B_SLOT_X, AdvancedMixingVatLayout.TANK_SLOT_Y);
        drawSlotFrame(graphics, AdvancedMixingVatLayout.TANK_C_SLOT_X, AdvancedMixingVatLayout.TANK_SLOT_Y);
        drawSlotFrame(graphics, AdvancedMixingVatLayout.GAS_SLOT_X, AdvancedMixingVatLayout.TANK_SLOT_Y);
        drawSlotFrame(graphics, AdvancedMixingVatLayout.OUTPUT_SLOT_X, AdvancedMixingVatLayout.TANK_SLOT_Y);

        drawTankFrame(
                graphics,
                AdvancedMixingVatLayout.TANK_A_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_W,
                AdvancedMixingVatLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                AdvancedMixingVatLayout.TANK_B_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_W,
                AdvancedMixingVatLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                AdvancedMixingVatLayout.TANK_C_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_W,
                AdvancedMixingVatLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                AdvancedMixingVatLayout.GAS_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_W,
                AdvancedMixingVatLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                AdvancedMixingVatLayout.OUTPUT_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_W,
                AdvancedMixingVatLayout.TANK_INNER_H
        );

        drawTankFillShaded(
                graphics,
                AdvancedMixingVatLayout.TANK_A_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_W,
                AdvancedMixingVatLayout.TANK_INNER_H,
                this.menu.getScaledTank(
                        this.menu.getInputATankAmount(),
                        AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY,
                        AdvancedMixingVatLayout.TANK_INNER_H
                ),
                getFluidColor(this.menu.getInputAFluid())
        );

        drawTankFillShaded(
                graphics,
                AdvancedMixingVatLayout.TANK_B_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_W,
                AdvancedMixingVatLayout.TANK_INNER_H,
                this.menu.getScaledTank(
                        this.menu.getInputBTankAmount(),
                        AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY,
                        AdvancedMixingVatLayout.TANK_INNER_H
                ),
                getFluidColor(this.menu.getInputBFluid())
        );

        drawTankFillShaded(
                graphics,
                AdvancedMixingVatLayout.TANK_C_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_W,
                AdvancedMixingVatLayout.TANK_INNER_H,
                this.menu.getScaledTank(
                        this.menu.getInputCTankAmount(),
                        AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY,
                        AdvancedMixingVatLayout.TANK_INNER_H
                ),
                getFluidColor(this.menu.getInputCFluid())
        );

        drawTankFillTopLit(
                graphics,
                AdvancedMixingVatLayout.GAS_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_W,
                AdvancedMixingVatLayout.TANK_INNER_H,
                this.menu.getScaledTank(
                        this.menu.getGasAmount(),
                        (int) AdvancedMixingVatBlockEntity.GAS_TANK_CAPACITY,
                        AdvancedMixingVatLayout.TANK_INNER_H
                ),
                0xFF9BC4D8,
                0xFFD4EEF7
        );

        drawTankFillShaded(
                graphics,
                AdvancedMixingVatLayout.OUTPUT_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                AdvancedMixingVatLayout.TANK_INNER_W,
                AdvancedMixingVatLayout.TANK_INNER_H,
                this.menu.getScaledTank(
                        this.menu.getOutputTankAmount(),
                        AdvancedMixingVatBlockEntity.OUTPUT_TANK_CAPACITY,
                        AdvancedMixingVatLayout.TANK_INNER_H
                ),
                getFluidColor(this.menu.getOutputFluid())
        );

        drawHorizontalBar(
                graphics,
                AdvancedMixingVatLayout.PROGRESS_X,
                AdvancedMixingVatLayout.PROGRESS_Y,
                AdvancedMixingVatLayout.PROGRESS_W,
                AdvancedMixingVatLayout.PROGRESS_H,
                this.menu.getScaledProgress(AdvancedMixingVatLayout.PROGRESS_W),
                0xFF768AB8,
                0xFFAAB9DB
        );


    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xCFCFCF, false);
        graphics.drawString(this.font, Component.literal("No heat required"), 52, 90, 0x8AA0B5, false);
        graphics.drawString(this.font, "A=" + this.menu.getInputATankAmount(), 8, 106, 0xFFFFFF, false);
        graphics.drawString(this.font, "B=" + this.menu.getInputBTankAmount(), 8, 116, 0xFFFFFF, false);
        graphics.drawString(this.font, "C=" + this.menu.getInputCTankAmount(), 8, 126, 0xFFFFFF, false);
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