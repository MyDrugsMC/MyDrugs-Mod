package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.KrisprKas9CombinatorMenu;
import org.mydrugs.mydrugs.menu.layout.KrisprKas9CombinatorLayout;
import org.mydrugs.mydrugs.menu.layout.StandardInventoryLayout;

import java.util.List;

public final class KrisprKas9CombinatorScreen extends AbstractMachineScreen<KrisprKas9CombinatorMenu> {
    public KrisprKas9CombinatorScreen(KrisprKas9CombinatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, KrisprKas9CombinatorLayout.GUI_WIDTH, KrisprKas9CombinatorLayout.GUI_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);
        drawPanel(
                graphics,
                KrisprKas9CombinatorLayout.MACHINE_PANEL_X,
                KrisprKas9CombinatorLayout.MACHINE_PANEL_Y,
                KrisprKas9CombinatorLayout.MACHINE_PANEL_W,
                KrisprKas9CombinatorLayout.MACHINE_PANEL_H,
                0xFF20242A
        );
        drawSieveInventoryPanels(graphics, KrisprKas9CombinatorLayout.PLAYER_INV_X, KrisprKas9CombinatorLayout.PLAYER_INV_Y);
        drawSlotFrame(graphics, KrisprKas9CombinatorLayout.INPUT_A_SLOT_X, KrisprKas9CombinatorLayout.INPUT_A_SLOT_Y);
        drawSlotFrame(graphics, KrisprKas9CombinatorLayout.INPUT_B_SLOT_X, KrisprKas9CombinatorLayout.INPUT_B_SLOT_Y);
        drawSlotFrame(graphics, KrisprKas9CombinatorLayout.OUTPUT_SLOT_X, KrisprKas9CombinatorLayout.OUTPUT_SLOT_Y);
        drawHorizontalBar(
                graphics,
                KrisprKas9CombinatorLayout.PROGRESS_X,
                KrisprKas9CombinatorLayout.PROGRESS_Y,
                KrisprKas9CombinatorLayout.PROGRESS_W,
                KrisprKas9CombinatorLayout.PROGRESS_H,
                this.menu.getScaledProgress(KrisprKas9CombinatorLayout.PROGRESS_W),
                0xFF9A4DFF,
                0xFFC39AFF
        );
        drawArrow(graphics, KrisprKas9CombinatorLayout.PROGRESS_X + 11, KrisprKas9CombinatorLayout.PROGRESS_Y - 15, 28, 12, 0xFFB8C7D8);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xFFE8E8E8, false);
        Component stability = this.menu.isSameSourceBlocked()
                ? Component.translatable("screen.mydrugs.crispr_cas9_combinator.same_source_short")
                : Component.translatable("screen.mydrugs.crispr_cas9_combinator.stability", this.menu.getStabilityPercent());
        int color = this.menu.isSameSourceBlocked() ? 0xFFFF7777 : 0xFFE0E0E0;
        graphics.drawString(this.font, stability, KrisprKas9CombinatorLayout.STABILITY_TEXT_X, KrisprKas9CombinatorLayout.STABILITY_TEXT_Y, color, false);
        graphics.drawString(this.font, this.playerInventoryTitle, KrisprKas9CombinatorLayout.PLAYER_INV_X, standardInventoryLabelY(KrisprKas9CombinatorLayout.PLAYER_INV_Y), 0xFFE0E0E0, false);
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "gene_input_a", "gene_input_b" -> List.of(
                    slotHighlight(KrisprKas9CombinatorLayout.INPUT_A_SLOT_X, KrisprKas9CombinatorLayout.INPUT_A_SLOT_Y),
                    slotHighlight(KrisprKas9CombinatorLayout.INPUT_B_SLOT_X, KrisprKas9CombinatorLayout.INPUT_B_SLOT_Y)
            );
            case "gene_output" -> List.of(slotHighlight(KrisprKas9CombinatorLayout.OUTPUT_SLOT_X, KrisprKas9CombinatorLayout.OUTPUT_SLOT_Y));
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.menu.isSameSourceBlocked() && isHoveringBox(
                KrisprKas9CombinatorLayout.STABILITY_TEXT_X,
                KrisprKas9CombinatorLayout.STABILITY_TEXT_Y,
                78,
                10,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("message.mydrugs.crispr.same_source"));
            return;
        }

        if (isHoveringBox(
                KrisprKas9CombinatorLayout.PROGRESS_X,
                KrisprKas9CombinatorLayout.PROGRESS_Y,
                KrisprKas9CombinatorLayout.PROGRESS_W,
                KrisprKas9CombinatorLayout.PROGRESS_H,
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
