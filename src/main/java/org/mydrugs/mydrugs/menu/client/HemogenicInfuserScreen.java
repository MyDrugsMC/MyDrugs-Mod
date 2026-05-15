package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.HemogenicInfuserMenu;
import org.mydrugs.mydrugs.menu.layout.HemogenicInfuserLayout;

import java.util.List;

public final class HemogenicInfuserScreen extends AbstractMachineScreen<HemogenicInfuserMenu> {
    public HemogenicInfuserScreen(HemogenicInfuserMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, HemogenicInfuserLayout.GUI_WIDTH, HemogenicInfuserLayout.GUI_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);
        drawPanel(
                graphics,
                HemogenicInfuserLayout.MACHINE_PANEL_X,
                HemogenicInfuserLayout.MACHINE_PANEL_Y,
                HemogenicInfuserLayout.MACHINE_PANEL_W,
                HemogenicInfuserLayout.MACHINE_PANEL_H,
                0xFF20242A
        );
        drawSieveInventoryPanels(graphics, HemogenicInfuserLayout.PLAYER_INV_X, HemogenicInfuserLayout.PLAYER_INV_Y);
        drawSlotFrame(graphics, HemogenicInfuserLayout.VECTOR_SLOT_X, HemogenicInfuserLayout.VECTOR_SLOT_Y);
        drawSlotFrame(graphics, HemogenicInfuserLayout.BLOOD_SLOT_X, HemogenicInfuserLayout.BLOOD_SLOT_Y);
        drawSlotFrame(graphics, HemogenicInfuserLayout.OUTPUT_SLOT_X, HemogenicInfuserLayout.OUTPUT_SLOT_Y);
        drawHorizontalBar(
                graphics,
                HemogenicInfuserLayout.PROGRESS_X,
                HemogenicInfuserLayout.PROGRESS_Y,
                HemogenicInfuserLayout.PROGRESS_W,
                HemogenicInfuserLayout.PROGRESS_H,
                this.menu.getScaledProgress(HemogenicInfuserLayout.PROGRESS_W),
                0xFFB83A55,
                0xFFE08095
        );
        drawArrow(graphics, HemogenicInfuserLayout.PROGRESS_X + 8, HemogenicInfuserLayout.PROGRESS_Y - 14, 28, 12, 0xFFB8C7D8);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xFFE8E8E8, false);
        graphics.drawString(this.font, this.playerInventoryTitle, HemogenicInfuserLayout.PLAYER_INV_X, standardInventoryLabelY(HemogenicInfuserLayout.PLAYER_INV_Y), 0xFFE0E0E0, false);
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "vector_input" -> List.of(slotHighlight(HemogenicInfuserLayout.VECTOR_SLOT_X, HemogenicInfuserLayout.VECTOR_SLOT_Y));
            case "blood_input" -> List.of(slotHighlight(HemogenicInfuserLayout.BLOOD_SLOT_X, HemogenicInfuserLayout.BLOOD_SLOT_Y));
            case "mutagenic_blood_output" -> List.of(slotHighlight(HemogenicInfuserLayout.OUTPUT_SLOT_X, HemogenicInfuserLayout.OUTPUT_SLOT_Y));
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(
                HemogenicInfuserLayout.PROGRESS_X,
                HemogenicInfuserLayout.PROGRESS_Y,
                HemogenicInfuserLayout.PROGRESS_W,
                HemogenicInfuserLayout.PROGRESS_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.hemogenic_infuser.progress"),
                    Component.translatable(
                            "screen.mydrugs.hemogenic_infuser.progress_amount",
                            this.menu.getProgress(),
                            this.menu.getMaxProgress(),
                            this.menu.getEnergyPerTick()
                    )
            );
        }
    }
}
