package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.BacterialIncubatorMenu;
import org.mydrugs.mydrugs.menu.layout.BacterialIncubatorLayout;

import java.util.List;

public final class BacterialIncubatorScreen extends AbstractMachineScreen<BacterialIncubatorMenu> {
    public BacterialIncubatorScreen(BacterialIncubatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, BacterialIncubatorLayout.GUI_WIDTH, BacterialIncubatorLayout.GUI_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);
        drawPanel(
                graphics,
                BacterialIncubatorLayout.MACHINE_PANEL_X,
                BacterialIncubatorLayout.MACHINE_PANEL_Y,
                BacterialIncubatorLayout.MACHINE_PANEL_W,
                BacterialIncubatorLayout.MACHINE_PANEL_H,
                0xFF20242A
        );
        drawSieveInventoryPanels(graphics, BacterialIncubatorLayout.PLAYER_INV_X, BacterialIncubatorLayout.PLAYER_INV_Y);
        drawSlotFrame(graphics, BacterialIncubatorLayout.GENE_SLOT_X, BacterialIncubatorLayout.GENE_SLOT_Y);
        drawSlotFrame(graphics, BacterialIncubatorLayout.NUTRIENT_SLOT_X, BacterialIncubatorLayout.NUTRIENT_SLOT_Y);
        drawSlotFrame(graphics, BacterialIncubatorLayout.OUTPUT_SLOT_X, BacterialIncubatorLayout.OUTPUT_SLOT_Y);
        drawHorizontalBar(
                graphics,
                BacterialIncubatorLayout.PROGRESS_X,
                BacterialIncubatorLayout.PROGRESS_Y,
                BacterialIncubatorLayout.PROGRESS_W,
                BacterialIncubatorLayout.PROGRESS_H,
                this.menu.getScaledProgress(BacterialIncubatorLayout.PROGRESS_W),
                0xFF79B85A,
                0xFFB4E08A
        );
        drawArrow(graphics, BacterialIncubatorLayout.PROGRESS_X + 8, BacterialIncubatorLayout.PROGRESS_Y - 14, 28, 12, 0xFFB8C7D8);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xFFE8E8E8, false);
        graphics.drawString(this.font, this.playerInventoryTitle, BacterialIncubatorLayout.PLAYER_INV_X, standardInventoryLabelY(BacterialIncubatorLayout.PLAYER_INV_Y), 0xFFE0E0E0, false);
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "gene_input" -> List.of(slotHighlight(BacterialIncubatorLayout.GENE_SLOT_X, BacterialIncubatorLayout.GENE_SLOT_Y));
            case "nutrient_input" -> List.of(slotHighlight(BacterialIncubatorLayout.NUTRIENT_SLOT_X, BacterialIncubatorLayout.NUTRIENT_SLOT_Y));
            case "vector_output" -> List.of(slotHighlight(BacterialIncubatorLayout.OUTPUT_SLOT_X, BacterialIncubatorLayout.OUTPUT_SLOT_Y));
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(
                BacterialIncubatorLayout.PROGRESS_X,
                BacterialIncubatorLayout.PROGRESS_Y,
                BacterialIncubatorLayout.PROGRESS_W,
                BacterialIncubatorLayout.PROGRESS_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.bacterial_incubator.progress"),
                    Component.translatable(
                            "screen.mydrugs.bacterial_incubator.progress_amount",
                            this.menu.getProgress(),
                            this.menu.getMaxProgress(),
                            this.menu.getEnergyPerTick()
                    )
            );
        }
    }
}
