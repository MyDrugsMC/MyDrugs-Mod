package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.GrowthChamberMenu;
import org.mydrugs.mydrugs.menu.layout.GasifierLayout;
import org.mydrugs.mydrugs.menu.layout.GrowthChamberLayout;

public class GrowthChamberScreen extends AbstractMachineScreen<GrowthChamberMenu> {
    public GrowthChamberScreen(GrowthChamberMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, GrowthChamberLayout.GUI_WIDTH, GrowthChamberLayout.GUI_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);

        drawPanel(
                graphics,
                GrowthChamberLayout.MACHINE_PANEL_X,
                GrowthChamberLayout.MACHINE_PANEL_Y,
                GrowthChamberLayout.MACHINE_PANEL_W,
                GrowthChamberLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        drawSieveInventoryPanels(
                graphics,
                GrowthChamberLayout.PLAYER_INV_X,
                GrowthChamberLayout.PLAYER_INV_Y
        );

        drawTankFrame(
                graphics,
                GrowthChamberLayout.WATER_TANK_X,
                GrowthChamberLayout.WATER_TANK_Y,
                GrowthChamberLayout.TANK_W,
                GrowthChamberLayout.TANK_H,
                GrowthChamberLayout.TANK_INNER_X_OFFSET,
                GrowthChamberLayout.TANK_INNER_Y_OFFSET,
                GrowthChamberLayout.TANK_INNER_W,
                GrowthChamberLayout.TANK_INNER_H
        );

        drawTankFillTopLit(
                graphics,
                GrowthChamberLayout.WATER_TANK_X,
                GrowthChamberLayout.WATER_TANK_Y,
                GrowthChamberLayout.TANK_INNER_X_OFFSET,
                GrowthChamberLayout.TANK_INNER_Y_OFFSET,
                GrowthChamberLayout.TANK_INNER_W,
                GrowthChamberLayout.TANK_INNER_H,
                this.menu.getScaledWaterTank(GrowthChamberLayout.TANK_INNER_H),
                0xFF4F88D6,
                0xFFA6C8FF
        );

        drawSlotFrame(graphics, GrowthChamberLayout.INPUT_SLOT_X, GrowthChamberLayout.INPUT_SLOT_Y);
        drawSlotFrame(graphics, GrowthChamberLayout.BIOMASS_SLOT_X, GrowthChamberLayout.BIOMASS_SLOT_Y);
        drawSlotFrame(graphics, GrowthChamberLayout.MIDDLE_SLOT_X, GrowthChamberLayout.MIDDLE_SLOT_Y);
        drawSlotFrame(graphics, GrowthChamberLayout.FINAL_SLOT_X, GrowthChamberLayout.FINAL_SLOT_Y);
        drawSlotFrame(graphics, GrowthChamberLayout.WATER_INPUT_SLOT_X, GrowthChamberLayout.WATER_INPUT_SLOT_Y);

        drawHorizontalBar(
                graphics,
                GrowthChamberLayout.GROWTH_PROGRESS_X,
                GrowthChamberLayout.GROWTH_PROGRESS_Y,
                GrowthChamberLayout.GROWTH_PROGRESS_W,
                GrowthChamberLayout.GROWTH_PROGRESS_H,
                this.menu.getScaledGrowthProgress(GrowthChamberLayout.GROWTH_PROGRESS_W),
                0xFF6FBF73,
                0xFFB7E0B9
        );

        drawHorizontalBar(
                graphics,
                GrowthChamberLayout.MATURE_PROGRESS_X,
                GrowthChamberLayout.MATURE_PROGRESS_Y,
                GrowthChamberLayout.MATURE_PROGRESS_W,
                GrowthChamberLayout.MATURE_PROGRESS_H,
                this.menu.getScaledMatureProgress(GrowthChamberLayout.MATURE_PROGRESS_W),
                0xFFB58C5A,
                0xFFE4C18F
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, this.title, GrowthChamberLayout.GUI_WIDTH / 2, GrowthChamberLayout.MACHINE_PANEL_Y + 4, 0xFFFFFFFF);
        graphics.drawString(this.font, Component.literal("Water"), 16, GrowthChamberLayout.WATER_TANK_Y - 10, 0xFF9BB2D1, false);
        graphics.drawString(this.font, Component.literal("Growing"), GrowthChamberLayout.GROWTH_PROGRESS_X, GrowthChamberLayout.GROWTH_PROGRESS_Y - 10, 0xFFA9D8AC, false);
        graphics.drawString(this.font, Component.literal("Maturing"), GrowthChamberLayout.MATURE_PROGRESS_X, GrowthChamberLayout.MATURE_PROGRESS_Y - 10, 0xFFD7B78E, false);
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(
                GrowthChamberLayout.WATER_TANK_X,
                GrowthChamberLayout.WATER_TANK_Y,
                GrowthChamberLayout.TANK_W,
                GrowthChamberLayout.TANK_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Water tank"),
                    Component.literal(this.menu.getWaterAmount() + " / " + GrowthChamberMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(
                GrowthChamberLayout.GROWTH_PROGRESS_X,
                GrowthChamberLayout.GROWTH_PROGRESS_Y,
                GrowthChamberLayout.GROWTH_PROGRESS_W,
                GrowthChamberLayout.GROWTH_PROGRESS_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Growing progress"),
                    Component.literal(this.menu.getGrowthProgress() + " / " + this.menu.getGrowthMaxProgress())
            );
        } else if (isHoveringBox(
                GrowthChamberLayout.MATURE_PROGRESS_X,
                GrowthChamberLayout.MATURE_PROGRESS_Y,
                GrowthChamberLayout.MATURE_PROGRESS_W,
                GrowthChamberLayout.MATURE_PROGRESS_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Maturing progress"),
                    Component.literal(this.menu.getMatureProgress() + " / " + this.menu.getMatureMaxProgress())
            );
        }
    }
}