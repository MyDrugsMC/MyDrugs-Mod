package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.SingleSlotMenu;
import org.mydrugs.mydrugs.menu.layout.SingleSlotMenuLayout;

public class SingleSlotMenuScreen extends AbstractMachineScreen<SingleSlotMenu> {
    public SingleSlotMenuScreen(SingleSlotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, SingleSlotMenuLayout.GUI_WIDTH, SingleSlotMenuLayout.GUI_HEIGHT);
        this.inventoryLabelX = SingleSlotMenuLayout.PLAYER_INV_X;
        this.inventoryLabelY = standardInventoryLabelY(SingleSlotMenuLayout.PLAYER_INV_Y);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindowColored(graphics, 0xFF181818, 0xFF262626);

        drawPanel(
                graphics,
                SingleSlotMenuLayout.MACHINE_PANEL_X,
                SingleSlotMenuLayout.MACHINE_PANEL_Y,
                SingleSlotMenuLayout.MACHINE_PANEL_W,
                SingleSlotMenuLayout.MACHINE_PANEL_H,
                0xFF323232,
                0xFF595959,
                0xFF101010
        );

        drawSieveInventoryPanels(
                graphics,
                SingleSlotMenuLayout.PLAYER_INV_X,
                SingleSlotMenuLayout.PLAYER_INV_Y
        );

        drawSlotFrame(graphics, SingleSlotMenuLayout.STORAGE_SLOT_X, SingleSlotMenuLayout.STORAGE_SLOT_Y, 0xFF8A8A8A, 0xFF111111);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = SingleSlotMenuLayout.MACHINE_PANEL_X
                + (SingleSlotMenuLayout.MACHINE_PANEL_W - this.font.width(this.title)) / 2;

        graphics.drawString(this.font, this.title, titleX, 4, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xD0D0D0, false);
        graphics.drawCenteredString(
                this.font,
                Component.literal("Storage"),
                SingleSlotMenuLayout.MACHINE_PANEL_X + SingleSlotMenuLayout.MACHINE_PANEL_W / 2,
                SingleSlotMenuLayout.MACHINE_PANEL_Y + 6,
                0xFFD0D0D0
        );
    }
}
