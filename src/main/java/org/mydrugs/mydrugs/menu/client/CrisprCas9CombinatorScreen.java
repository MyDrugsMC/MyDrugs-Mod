package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.CrisprCas9CombinatorMenu;
import org.mydrugs.mydrugs.menu.layout.CrisprCas9CombinatorLayout;
import org.mydrugs.mydrugs.menu.layout.StandardInventoryLayout;

import java.util.List;

public final class CrisprCas9CombinatorScreen extends AbstractMachineScreen<CrisprCas9CombinatorMenu> {
    public CrisprCas9CombinatorScreen(CrisprCas9CombinatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, CrisprCas9CombinatorLayout.GUI_WIDTH, CrisprCas9CombinatorLayout.GUI_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);
        drawPanel(
                graphics,
                CrisprCas9CombinatorLayout.MACHINE_PANEL_X,
                CrisprCas9CombinatorLayout.MACHINE_PANEL_Y,
                CrisprCas9CombinatorLayout.MACHINE_PANEL_W,
                CrisprCas9CombinatorLayout.MACHINE_PANEL_H,
                0xFF20242A
        );
        drawSieveInventoryPanels(graphics, CrisprCas9CombinatorLayout.PLAYER_INV_X, CrisprCas9CombinatorLayout.PLAYER_INV_Y);
        drawSlotFrame(graphics, CrisprCas9CombinatorLayout.INPUT_A_SLOT_X, CrisprCas9CombinatorLayout.INPUT_A_SLOT_Y);
        drawSlotFrame(graphics, CrisprCas9CombinatorLayout.INPUT_B_SLOT_X, CrisprCas9CombinatorLayout.INPUT_B_SLOT_Y);
        drawSlotFrame(graphics, CrisprCas9CombinatorLayout.OUTPUT_SLOT_X, CrisprCas9CombinatorLayout.OUTPUT_SLOT_Y);
        drawHorizontalBar(
                graphics,
                CrisprCas9CombinatorLayout.PROGRESS_X,
                CrisprCas9CombinatorLayout.PROGRESS_Y,
                CrisprCas9CombinatorLayout.PROGRESS_W,
                CrisprCas9CombinatorLayout.PROGRESS_H,
                this.menu.getScaledProgress(CrisprCas9CombinatorLayout.PROGRESS_W),
                0xFF9A4DFF,
                0xFFC39AFF
        );
        drawArrow(graphics, CrisprCas9CombinatorLayout.PROGRESS_X + 11, CrisprCas9CombinatorLayout.PROGRESS_Y - 15, 28, 12, 0xFFB8C7D8);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xFFE8E8E8, false);
        Component stability = this.menu.isSameSourceBlocked()
                ? Component.translatable("screen.mydrugs.crispr_cas9_combinator.same_source_short")
                : Component.translatable("screen.mydrugs.crispr_cas9_combinator.stability", this.menu.getStabilityPercent());
        int color = this.menu.isSameSourceBlocked() ? 0xFFFF7777 : 0xFFE0E0E0;
        graphics.drawString(this.font, stability, CrisprCas9CombinatorLayout.STABILITY_TEXT_X, CrisprCas9CombinatorLayout.STABILITY_TEXT_Y, color, false);
        graphics.drawString(this.font, this.playerInventoryTitle, CrisprCas9CombinatorLayout.PLAYER_INV_X, standardInventoryLabelY(CrisprCas9CombinatorLayout.PLAYER_INV_Y), 0xFFE0E0E0, false);
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "gene_input_a", "gene_input_b" -> List.of(
                    slotHighlight(CrisprCas9CombinatorLayout.INPUT_A_SLOT_X, CrisprCas9CombinatorLayout.INPUT_A_SLOT_Y),
                    slotHighlight(CrisprCas9CombinatorLayout.INPUT_B_SLOT_X, CrisprCas9CombinatorLayout.INPUT_B_SLOT_Y)
            );
            case "gene_output" -> List.of(slotHighlight(CrisprCas9CombinatorLayout.OUTPUT_SLOT_X, CrisprCas9CombinatorLayout.OUTPUT_SLOT_Y));
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.menu.isSameSourceBlocked() && isHoveringBox(
                CrisprCas9CombinatorLayout.STABILITY_TEXT_X,
                CrisprCas9CombinatorLayout.STABILITY_TEXT_Y,
                78,
                10,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("message.mydrugs.crispr.same_source"));
            return;
        }

        if (isHoveringBox(
                CrisprCas9CombinatorLayout.PROGRESS_X,
                CrisprCas9CombinatorLayout.PROGRESS_Y,
                CrisprCas9CombinatorLayout.PROGRESS_W,
                CrisprCas9CombinatorLayout.PROGRESS_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.crispr_cas9_combinator.progress"),
                    Component.translatable(
                            "screen.mydrugs.crispr_cas9_combinator.progress_amount",
                            this.menu.getProgress(),
                            this.menu.getMaxProgress(),
                            this.menu.getEnergyPerTick()
                    )
            );
        }
    }
}
