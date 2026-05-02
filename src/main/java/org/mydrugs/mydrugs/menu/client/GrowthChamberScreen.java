package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.GrowthChamberMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.GrowthChamberLayout;

import java.util.List;

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
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "item_input" -> List.of(
                    slotHighlight(GrowthChamberLayout.INPUT_SLOT_X, GrowthChamberLayout.INPUT_SLOT_Y),
                    slotHighlight(GrowthChamberLayout.BIOMASS_SLOT_X, GrowthChamberLayout.BIOMASS_SLOT_Y)
            );
            case "item_output" -> List.of(
                    slotHighlight(GrowthChamberLayout.MIDDLE_SLOT_X, GrowthChamberLayout.MIDDLE_SLOT_Y),
                    slotHighlight(GrowthChamberLayout.FINAL_SLOT_X, GrowthChamberLayout.FINAL_SLOT_Y)
            );
            case "fluid_input" -> List.of(
                    tankHighlight(GrowthChamberLayout.WATER_TANK_X, GrowthChamberLayout.WATER_TANK_Y, GrowthChamberLayout.TANK_W, GrowthChamberLayout.TANK_H),
                    slotHighlight(GrowthChamberLayout.WATER_INPUT_SLOT_X, GrowthChamberLayout.WATER_INPUT_SLOT_Y)
            );
            default -> super.transferPortHighlights(portIdPath);
        };
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
                    Component.translatable("screen.mydrugs.ui.water_tank"),
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
                    Component.translatable("screen.mydrugs.ui.growing_progress"),
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
                    Component.translatable("screen.mydrugs.ui.maturing_progress"),
                    Component.literal(this.menu.getMatureProgress() + " / " + this.menu.getMatureMaxProgress())
            );
        }
    }
}
