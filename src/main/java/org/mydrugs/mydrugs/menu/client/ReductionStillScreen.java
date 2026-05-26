package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.ReductionStillBlockEntity;
import org.mydrugs.mydrugs.menu.ReductionStillMenu;
import org.mydrugs.mydrugs.menu.layout.ReductionStillLayout;

public class ReductionStillScreen extends AbstractMachineScreen<ReductionStillMenu> {

    public ReductionStillScreen(ReductionStillMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, ReductionStillLayout.IMAGE_WIDTH, ReductionStillLayout.IMAGE_HEIGHT);
        this.inventoryLabelY = standardInventoryLabelY(ReductionStillLayout.PLAYER_INV_Y);
    }

    @Override
    protected boolean shouldShowTransferConfigButton() {
        return false;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);
        drawPanel(graphics, 18, 16, 140, 62, 0xFF2A2A35, 0xFF55556B, 0xFF101018);
        drawSieveInventoryPanels(graphics, ReductionStillLayout.PLAYER_INV_X, ReductionStillLayout.PLAYER_INV_Y);
        drawSlotFrame(graphics, ReductionStillLayout.CUTTINGS_X, ReductionStillLayout.CUTTINGS_Y, 0xFF7A8AA0, 0xFF101018);
        drawSlotFrame(graphics, ReductionStillLayout.SOLVENT_X, ReductionStillLayout.SOLVENT_Y, 0xFF7A8AA0, 0xFF101018);
        drawSlotFrame(graphics, ReductionStillLayout.EXTRACT_X, ReductionStillLayout.EXTRACT_Y, 0xFF7A8AA0, 0xFF101018);
        drawSlotFrame(graphics, ReductionStillLayout.PULP_X, ReductionStillLayout.PULP_Y, 0xFF7A8AA0, 0xFF101018);
        drawProgressBar(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 5, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle, ReductionStillLayout.PLAYER_INV_X, this.inventoryLabelY, 0xD0D0D0, false);

        ReductionStillBlockEntity.Status status = this.menu.getStillStatus();
        Component statusText = switch (status) {
            case WORKING -> Component.translatable("screen.mydrugs.reduction_still.status.working",
                    this.menu.getProgress(), this.menu.getMaxProgress());
            case IDLE_NO_CUTTINGS -> Component.translatable("screen.mydrugs.reduction_still.status.missing_cuttings");
            case IDLE_NO_SOLVENT -> Component.translatable("screen.mydrugs.reduction_still.status.missing_solvent");
            case IDLE_NEED_MORE_CUTTINGS -> Component.translatable("screen.mydrugs.reduction_still.status.need_more_cuttings");
            case IDLE_OUTPUT_BLOCKED -> Component.translatable("screen.mydrugs.reduction_still.status.output_blocked");
        };
        int color = status == ReductionStillBlockEntity.Status.WORKING ? 0xC4E8C4 : 0xE8C4C4;
        graphics.drawString(this.font, statusText, 8, 70, color, false);
    }

    private void drawProgressBar(GuiGraphics graphics) {
        int left = guiX(ReductionStillLayout.PROGRESS_X);
        int top = guiY(ReductionStillLayout.PROGRESS_Y);
        int w = ReductionStillLayout.PROGRESS_W;
        int h = ReductionStillLayout.PROGRESS_H;
        graphics.fill(left - 1, top - 1, left + w + 1, top + h + 1, 0xFF101018);
        graphics.fill(left, top, left + w, top + h, 0xFF2A2A35);
        int filled = this.menu.getScaledProgress(w);
        if (filled > 0) {
            graphics.fill(left, top, left + filled, top + h, 0xFF6AA0C8);
        }
    }
}
