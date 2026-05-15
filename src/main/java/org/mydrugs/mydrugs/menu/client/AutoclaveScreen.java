package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.AutoclaveBlockEntity;
import org.mydrugs.mydrugs.menu.AutoclaveMenu;
import org.mydrugs.mydrugs.menu.layout.AutoclaveLayout;

import java.util.List;

public final class AutoclaveScreen extends AbstractMachineScreen<AutoclaveMenu> {
    public AutoclaveScreen(AutoclaveMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, AutoclaveLayout.GUI_WIDTH, AutoclaveLayout.GUI_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);
        drawPanel(
                graphics,
                AutoclaveLayout.MACHINE_PANEL_X,
                AutoclaveLayout.MACHINE_PANEL_Y,
                AutoclaveLayout.MACHINE_PANEL_W,
                AutoclaveLayout.MACHINE_PANEL_H,
                0xFF20242A
        );
        drawSieveInventoryPanels(graphics, AutoclaveLayout.PLAYER_INV_X, AutoclaveLayout.PLAYER_INV_Y);
        drawSlotFrame(graphics, AutoclaveLayout.INPUT_SLOT_X, AutoclaveLayout.INPUT_SLOT_Y);
        drawSlotFrame(graphics, AutoclaveLayout.OUTPUT_SLOT_X, AutoclaveLayout.OUTPUT_SLOT_Y);
        drawHorizontalBar(
                graphics,
                AutoclaveLayout.PROGRESS_X,
                AutoclaveLayout.PROGRESS_Y,
                AutoclaveLayout.PROGRESS_W,
                AutoclaveLayout.PROGRESS_H,
                this.menu.getScaledProgress(AutoclaveLayout.PROGRESS_W),
                0xFF4DC3FF,
                0xFF9ADFFF
        );
        drawArrow(graphics, AutoclaveLayout.PROGRESS_X + 6, AutoclaveLayout.PROGRESS_Y - 14, 26, 12, 0xFFB8C7D8);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xFFE8E8E8, false);
        graphics.drawString(this.font, this.playerInventoryTitle, AutoclaveLayout.PLAYER_INV_X, standardInventoryLabelY(AutoclaveLayout.PLAYER_INV_Y), 0xFFE0E0E0, false);
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "syringe_input" -> List.of(slotHighlight(AutoclaveLayout.INPUT_SLOT_X, AutoclaveLayout.INPUT_SLOT_Y));
            case "syringe_output" -> List.of(slotHighlight(AutoclaveLayout.OUTPUT_SLOT_X, AutoclaveLayout.OUTPUT_SLOT_Y));
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(
                AutoclaveLayout.PROGRESS_X,
                AutoclaveLayout.PROGRESS_Y,
                AutoclaveLayout.PROGRESS_W,
                AutoclaveLayout.PROGRESS_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.autoclave.progress"),
                    Component.translatable(
                            "screen.mydrugs.autoclave.progress_amount",
                            this.menu.getProgress(),
                            this.menu.getMaxProgress(),
                            AutoclaveBlockEntity.ENERGY_PER_TICK
                    )
            );
        }
    }
}
