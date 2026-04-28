package org.mydrugs.mydrugs.pipe.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterMenu;

public class PipeFilterScreen extends AbstractContainerScreen<PipeFilterMenu> {
    public PipeFilterScreen(PipeFilterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 96;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xE01A1A1A);
        guiGraphics.fill(this.leftPos + 6, this.topPos + 6, this.leftPos + this.imageWidth - 6, this.topPos + this.imageHeight - 6, 0xE0262630);
        guiGraphics.drawString(this.font, this.title, this.leftPos + 10, this.topPos + 10, 0xFFFFFF, false);
        guiGraphics.drawString(
                this.font,
                Component.translatable("screen.mydrugs.pipe_filter.placeholder"),
                this.leftPos + 10,
                this.topPos + 34,
                0xC8C8D8,
                false
        );
        guiGraphics.drawString(
                this.font,
                Component.translatable("screen.mydrugs.pipe_filter.instructions"),
                this.leftPos + 10,
                this.topPos + 52,
                0x8F8FA3,
                false
        );
    }
}
