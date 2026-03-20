package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.SingleSlotMenu;

public class SingleSlotMenuScreen extends AbstractContainerScreen<SingleSlotMenu> {
    public SingleSlotMenuScreen(SingleSlotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 148;
        this.inventoryLabelY = 54;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        // outer background
        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF1E1E1E);

        // main inner panel
        graphics.fill(left + 4, top + 4, left + this.imageWidth - 4, top + this.imageHeight - 4, 0xFF2B2B2B);

        // top section for pouch slot
        graphics.fill(left + 28, top + 12, left + 148, top + 50, 0xFF353535);

        // player inventory section
        graphics.fill(left + 6, top + 58, left + 170, top + 116, 0xFF303030);

        // hotbar section
        graphics.fill(left + 6, top + 120, left + 170, top + 140, 0xFF303030);

        // slot frame
        graphics.fill(left + 78, top + 22, left + 98, top + 42, 0xFF8A8A8A);
        graphics.fill(left + 79, top + 23, left + 97, top + 41, 0xFF111111);

        // subtle borders
        drawBorder(graphics, left + 28, top + 12, 120, 38);
        drawBorder(graphics, left + 6, top + 58, 164, 58);
        drawBorder(graphics, left + 6, top + 120, 164, 20);
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + 1, 0xFF5A5A5A);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF111111);
        graphics.fill(x, y, x + 1, y + height, 0xFF5A5A5A);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF111111);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 8, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0xD0D0D0, false);
        graphics.drawString(this.font, "Storage", 62, 14, 0xD0D0D0, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}