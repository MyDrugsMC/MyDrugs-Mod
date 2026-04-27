package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.GrowthChamberMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.GrowthChamberLayout;

public class GrowthChamberScreen extends AbstractMachineScreen<GrowthChamberMenu> {
    public GrowthChamberScreen(GrowthChamberMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, GrowthChamberLayout.GUI_WIDTH, GrowthChamberLayout.GUI_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineGuiRenderer.drawGrowthChamber(
                this,
                graphics,
                new MachineGuiRenderer.GrowthChamberState(
                        this.menu.getScaledWaterTank(GrowthChamberLayout.TANK_INNER_H),
                        this.menu.getScaledGrowthProgress(GrowthChamberLayout.GROWTH_PROGRESS_W),
                        this.menu.getScaledMatureProgress(GrowthChamberLayout.MATURE_PROGRESS_W)
                ),
                true
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawGrowthChamberLabels(this, graphics, this.font, this.title, null);
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
