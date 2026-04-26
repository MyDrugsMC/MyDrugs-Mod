package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.AdvancedFurnaceBlockEntity;
import org.mydrugs.mydrugs.menu.AdvancedFurnaceMenu;
import org.mydrugs.mydrugs.menu.layout.AdvancedFurnaceLayout;
import org.mydrugs.mydrugs.menu.layout.StandardInventoryLayout;
import org.mydrugs.mydrugs.menu.layout.StandardTankLayout;

public final class AdvancedFurnaceScreen extends AbstractMachineScreen<AdvancedFurnaceMenu> {
    public AdvancedFurnaceScreen(AdvancedFurnaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, AdvancedFurnaceLayout.GUI_WIDTH, AdvancedFurnaceLayout.GUI_HEIGHT);
        this.inventoryLabelY = standardInventoryLabelY(AdvancedFurnaceLayout.PLAYER_INV_Y);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindowColored(graphics, 0xFF15171B, 0xFF23262B);

        drawPanel(
                graphics,
                AdvancedFurnaceLayout.MACHINE_PANEL_X,
                AdvancedFurnaceLayout.MACHINE_PANEL_Y,
                AdvancedFurnaceLayout.MACHINE_PANEL_W,
                AdvancedFurnaceLayout.MACHINE_PANEL_H,
                0xFF2F343C,
                0xFF646B77,
                0xFF0E1116
        );

        drawPanel(
                graphics,
                AdvancedFurnaceLayout.CENTER_PANEL_X,
                AdvancedFurnaceLayout.CENTER_PANEL_Y,
                AdvancedFurnaceLayout.CENTER_PANEL_W,
                AdvancedFurnaceLayout.CENTER_PANEL_H,
                0xFF1B1F25,
                0xFF505862,
                0xFF0A0C10
        );

        drawPanel(
                graphics,
                AdvancedFurnaceLayout.PLAYER_INV_X,
                AdvancedFurnaceLayout.PLAYER_INV_Y,
                StandardInventoryLayout.PLAYER_INV_PANEL_W,
                StandardInventoryLayout.PLAYER_INV_PANEL_H,
                0xFF2A2D33,
                0xFF5C616B,
                0xFF0E1014
        );

        drawPanel(
                graphics,
                AdvancedFurnaceLayout.PLAYER_INV_X,
                StandardInventoryLayout.hotbarPanelY(AdvancedFurnaceLayout.PLAYER_INV_Y),
                StandardInventoryLayout.HOTBAR_PANEL_W,
                StandardInventoryLayout.HOTBAR_PANEL_H,
                0xFF2A2D33,
                0xFF5C616B,
                0xFF0E1014
        );

        drawSlotFrame(graphics, AdvancedFurnaceLayout.INPUT_A_X, AdvancedFurnaceLayout.INPUT_A_Y);
        drawSlotFrame(graphics, AdvancedFurnaceLayout.INPUT_B_X, AdvancedFurnaceLayout.INPUT_B_Y);
        drawSlotFrame(graphics, AdvancedFurnaceLayout.FUEL_X, AdvancedFurnaceLayout.FUEL_Y);

        drawSlotFrame(graphics, AdvancedFurnaceLayout.OUTPUT_A_X, AdvancedFurnaceLayout.OUTPUT_A_Y);
        drawSlotFrame(graphics, AdvancedFurnaceLayout.OUTPUT_B_X, AdvancedFurnaceLayout.OUTPUT_B_Y);
        drawSlotFrame(graphics, AdvancedFurnaceLayout.OUTPUT_CONTAINER_X, AdvancedFurnaceLayout.OUTPUT_CONTAINER_Y);

        drawHorizontalBar(
                graphics,
                AdvancedFurnaceLayout.PROGRESS_X,
                AdvancedFurnaceLayout.PROGRESS_Y,
                AdvancedFurnaceLayout.PROGRESS_W,
                AdvancedFurnaceLayout.PROGRESS_H,
                this.menu.getScaledProgress(AdvancedFurnaceLayout.PROGRESS_W),
                0xFF62C8FF,
                0xFFB9EEFF
        );

        drawHorizontalBar(
                graphics,
                AdvancedFurnaceLayout.BURN_X,
                AdvancedFurnaceLayout.BURN_Y,
                AdvancedFurnaceLayout.BURN_W,
                AdvancedFurnaceLayout.BURN_H,
                this.menu.getScaledBurn(AdvancedFurnaceLayout.BURN_W),
                0xFFFF9B47,
                0xFFFFC87A
        );

        drawTankFrame(
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

        drawTankFillShaded(
                graphics,
                AdvancedFurnaceLayout.TANK_X,
                AdvancedFurnaceLayout.TANK_Y,
                StandardTankLayout.INNER_X,
                StandardTankLayout.INNER_Y,
                StandardTankLayout.INNER_W,
                StandardTankLayout.INNER_H,
                this.menu.getScaledTank(StandardTankLayout.INNER_H),
                0xFF57B7D8
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int machineTitleX = AdvancedFurnaceLayout.MACHINE_PANEL_X
                + (AdvancedFurnaceLayout.MACHINE_PANEL_W - this.font.width(this.title)) / 2;

        graphics.drawString(this.font, this.title, machineTitleX, 5, 0xFFF0F3F8, false);
        graphics.drawString(this.font, this.playerInventoryTitle, AdvancedFurnaceLayout.PLAYER_INV_X, this.inventoryLabelY, 0xFFD0D4DC, false);

        graphics.drawCenteredString(this.font, "Heat", AdvancedFurnaceLayout.HEAT_LABEL_X, AdvancedFurnaceLayout.HEAT_LABEL_Y, 0xFFE0B58A);
        //graphics.drawString(this.font, "Tank", 145, 9, 0xFFBFE4F0, false);
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(
                AdvancedFurnaceLayout.TANK_X,
                AdvancedFurnaceLayout.TANK_Y,
                StandardTankLayout.TANK_W,
                StandardTankLayout.TANK_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output Tank"),
                    Component.literal(getFluidName(this.menu.getTankFluid())),
                    Component.literal(this.menu.getTankAmount() + " / " + AdvancedFurnaceBlockEntity.TANK_CAPACITY + " mB")
            );
        }

        if (isHoveringBox(
                AdvancedFurnaceLayout.PROGRESS_X,
                AdvancedFurnaceLayout.PROGRESS_Y,
                AdvancedFurnaceLayout.PROGRESS_W,
                AdvancedFurnaceLayout.PROGRESS_H,
                mouseX,
                mouseY
        )) {
            renderSimpleAmountTooltip(
                    graphics,
                    mouseX,
                    mouseY,
                    "Progress",
                    this.menu.getProgress(),
                    this.menu.getMaxProgress(),
                    "ticks"
            );
        }

        if (isHoveringBox(
                AdvancedFurnaceLayout.BURN_X,
                AdvancedFurnaceLayout.BURN_Y,
                AdvancedFurnaceLayout.BURN_W,
                AdvancedFurnaceLayout.BURN_H,
                mouseX,
                mouseY
        )) {
            renderSimpleAmountTooltip(
                    graphics,
                    mouseX,
                    mouseY,
                    "Burn",
                    this.menu.getBurnTime(),
                    this.menu.getBurnDuration(),
                    "ticks"
            );
        }
    }
}