package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.GeneExtractorBlockEntity;
import org.mydrugs.mydrugs.menu.GeneExtractorMenu;
import org.mydrugs.mydrugs.menu.layout.GeneExtractorLayout;
import org.mydrugs.mydrugs.menu.layout.StandardInventoryLayout;

import java.util.List;

public final class GeneExtractorScreen extends AbstractMachineScreen<GeneExtractorMenu> {
    public GeneExtractorScreen(GeneExtractorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, GeneExtractorLayout.GUI_WIDTH, GeneExtractorLayout.GUI_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);
        drawPanel(
                graphics,
                GeneExtractorLayout.MACHINE_PANEL_X,
                GeneExtractorLayout.MACHINE_PANEL_Y,
                GeneExtractorLayout.MACHINE_PANEL_W,
                GeneExtractorLayout.MACHINE_PANEL_H,
                0xFF20242A
        );
        drawSieveInventoryPanels(graphics, GeneExtractorLayout.PLAYER_INV_X, GeneExtractorLayout.PLAYER_INV_Y);
        drawSlotFrame(graphics, GeneExtractorLayout.INPUT_SLOT_X, GeneExtractorLayout.INPUT_SLOT_Y);
        drawSlotFrame(graphics, GeneExtractorLayout.OUTPUT_A_SLOT_X, GeneExtractorLayout.OUTPUT_A_SLOT_Y);
        drawSlotFrame(graphics, GeneExtractorLayout.OUTPUT_B_SLOT_X, GeneExtractorLayout.OUTPUT_B_SLOT_Y);
        drawSlotFrame(graphics, GeneExtractorLayout.OUTPUT_C_SLOT_X, GeneExtractorLayout.OUTPUT_C_SLOT_Y);
        drawHorizontalBar(
                graphics,
                GeneExtractorLayout.PROGRESS_X,
                GeneExtractorLayout.PROGRESS_Y,
                GeneExtractorLayout.PROGRESS_W,
                GeneExtractorLayout.PROGRESS_H,
                this.menu.getScaledProgress(GeneExtractorLayout.PROGRESS_W),
                0xFF9A4DFF,
                0xFFC39AFF
        );
        drawArrow(graphics, GeneExtractorLayout.PROGRESS_X + 8, GeneExtractorLayout.PROGRESS_Y - 14, 28, 12, 0xFFB8C7D8);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xFFE8E8E8, false);
        graphics.drawString(this.font, this.playerInventoryTitle, GeneExtractorLayout.PLAYER_INV_X, standardInventoryLabelY(GeneExtractorLayout.PLAYER_INV_Y), 0xFFE0E0E0, false);
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "adn_input" -> List.of(slotHighlight(GeneExtractorLayout.INPUT_SLOT_X, GeneExtractorLayout.INPUT_SLOT_Y));
            case "gene_output_a", "gene_output_b", "gene_output_c" -> List.of(
                    slotHighlight(GeneExtractorLayout.OUTPUT_A_SLOT_X, GeneExtractorLayout.OUTPUT_A_SLOT_Y),
                    slotHighlight(GeneExtractorLayout.OUTPUT_B_SLOT_X, GeneExtractorLayout.OUTPUT_B_SLOT_Y),
                    slotHighlight(GeneExtractorLayout.OUTPUT_C_SLOT_X, GeneExtractorLayout.OUTPUT_C_SLOT_Y)
            );
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(
                GeneExtractorLayout.PROGRESS_X,
                GeneExtractorLayout.PROGRESS_Y,
                GeneExtractorLayout.PROGRESS_W,
                GeneExtractorLayout.PROGRESS_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.gene_extractor.progress"),
                    Component.translatable(
                            "screen.mydrugs.gene_extractor.progress_amount",
                            this.menu.getProgress(),
                            this.menu.getMaxProgress(),
                            GeneExtractorBlockEntity.ENERGY_PER_TICK
                    )
            );
        }
    }
}
