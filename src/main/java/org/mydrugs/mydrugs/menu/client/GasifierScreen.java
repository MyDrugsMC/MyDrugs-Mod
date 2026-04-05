package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.GasifierMenu;
import org.mydrugs.mydrugs.menu.layout.GasifierLayout;

import java.util.ArrayList;
import java.util.List;

public class GasifierScreen extends AbstractContainerScreen<GasifierMenu> {
    private static final int GAS_COLOR = 0xFFB7D34B;

    public GasifierScreen(GasifierMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GasifierLayout.GUI_WIDTH;
        this.imageHeight = GasifierLayout.GUI_HEIGHT;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF171717);
        graphics.fill(left + 4, top + 4, left + this.imageWidth - 4, top + this.imageHeight - 4, 0xFF252525);

        fillPanel(
                graphics,
                left + GasifierLayout.MACHINE_PANEL_X,
                top + GasifierLayout.MACHINE_PANEL_Y,
                GasifierLayout.MACHINE_PANEL_W,
                GasifierLayout.MACHINE_PANEL_H,
                0xFF2B3038
        );

        fillPanel(
                graphics,
                left + GasifierLayout.INVENTORY_PANEL_X,
                top + GasifierLayout.INVENTORY_PANEL_Y,
                GasifierLayout.INVENTORY_PANEL_W,
                GasifierLayout.INVENTORY_PANEL_H,
                0xFF2A2D33
        );

        drawSlotFrame(graphics, GasifierLayout.INPUT_SLOT_X, GasifierLayout.INPUT_SLOT_Y);
        drawSlotFrame(graphics, GasifierLayout.EXPORT_SLOT_X, GasifierLayout.EXPORT_SLOT_Y);

        drawTankFrame(graphics, GasifierLayout.OUTPUT_TANK_X, GasifierLayout.OUTPUT_TANK_Y);
        drawTankFill(
                graphics,
                left + GasifierLayout.OUTPUT_TANK_X,
                top + GasifierLayout.OUTPUT_TANK_Y,
                this.menu.getScaledGasTank(GasifierLayout.TANK_INNER_H),
                GAS_COLOR
        );

        drawProgressBar(
                graphics,
                left + GasifierLayout.PROGRESS_X,
                top + GasifierLayout.PROGRESS_Y,
                this.menu.getScaledProgress(GasifierLayout.PROGRESS_W)
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // keep empty for clean industrial look
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHoveringBox(GasifierLayout.OUTPUT_TANK_X, GasifierLayout.OUTPUT_TANK_Y, GasifierLayout.TANK_W, GasifierLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output gas tank"),
                    this.menu.getGasName(),
                    Component.literal(this.menu.getGasAmount() + " / " + GasifierMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(GasifierLayout.PROGRESS_X, GasifierLayout.PROGRESS_Y, GasifierLayout.PROGRESS_W, GasifierLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Gasification progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        } else if (isHoveringBox(GasifierLayout.EXPORT_SLOT_X, GasifierLayout.EXPORT_SLOT_Y, 16, 16, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Gas Tank link slot"),
                    Component.literal("Insert your Gas Tank block item here"),
                    Component.literal("Machine exports to a placed Gas Tank in front")
            );
        }
    }

    private void fillPanel(GuiGraphics graphics, int x, int y, int width, int height, int fillColor) {
        graphics.fill(x, y, x + width, y + height, fillColor);
        drawBorder(graphics, x, y, width, height);
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + 1, 0xFF5C616B);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF0E1014);
        graphics.fill(x, y, x + 1, y + height, 0xFF5C616B);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF0E1014);
    }

    private void drawSlotFrame(GuiGraphics graphics, int slotX, int slotY) {
        int x = this.leftPos + slotX - 1;
        int y = this.topPos + slotY - 1;

        graphics.fill(x, y, x + 18, y + 18, 0xFF8A8F99);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF101216);
    }

    private void drawTankFrame(GuiGraphics graphics, int tankX, int tankY) {
        int x = this.leftPos + tankX;
        int y = this.topPos + tankY;

        graphics.fill(x - 1, y - 1, x + GasifierLayout.TANK_W + 1, y + GasifierLayout.TANK_H + 1, 0xFF7F8590);
        graphics.fill(x, y, x + GasifierLayout.TANK_W, y + GasifierLayout.TANK_H, 0xFF15171B);

        graphics.fill(
                x + GasifierLayout.TANK_INNER_X_OFFSET,
                y + GasifierLayout.TANK_INNER_Y_OFFSET,
                x + GasifierLayout.TANK_INNER_X_OFFSET + GasifierLayout.TANK_INNER_W,
                y + GasifierLayout.TANK_INNER_Y_OFFSET + GasifierLayout.TANK_INNER_H,
                0xFF090A0D
        );
    }

    private void drawTankFill(GuiGraphics graphics, int tankX, int tankY, int filledPixels, int color) {
        if (filledPixels <= 0) {
            return;
        }

        int x1 = tankX + GasifierLayout.TANK_INNER_X_OFFSET;
        int y1 = tankY + GasifierLayout.TANK_INNER_Y_OFFSET;
        int x2 = x1 + GasifierLayout.TANK_INNER_W;
        int y2 = y1 + GasifierLayout.TANK_INNER_H;
        int fillTop = y2 - filledPixels;

        graphics.fill(x1, fillTop, x2, y2, color);
        graphics.fill(x1 + 1, fillTop, x1 + 2, y2, 0xD0FFFFFF);
        graphics.fill(x1 + 3, fillTop, x1 + 5, y2, 0x55000000);
    }

    private void drawProgressBar(GuiGraphics graphics, int x, int y, int progress) {
        graphics.fill(x - 1, y - 1, x + GasifierLayout.PROGRESS_W + 1, y + GasifierLayout.PROGRESS_H + 1, 0xFF767C88);
        graphics.fill(x, y, x + GasifierLayout.PROGRESS_W, y + GasifierLayout.PROGRESS_H, 0xFF101216);

        if (progress > 0) {
            int clamped = Math.min(GasifierLayout.PROGRESS_W, progress);
            graphics.fill(x, y, x + clamped, y + GasifierLayout.PROGRESS_H, 0xFF8DBA63);
            graphics.fill(x, y, x + clamped, y + 2, 0xFFD5F0A4);
        }
    }

    private boolean isHoveringBox(int localX, int localY, int width, int height, int mouseX, int mouseY) {
        int x = this.leftPos + localX;
        int y = this.topPos + localY;
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void renderTooltipLines(GuiGraphics graphics, int mouseX, int mouseY, Component... lines) {
        List<ClientTooltipComponent> components = new ArrayList<>(lines.length);
        for (Component line : lines) {
            components.add(ClientTooltipComponent.create(line.getVisualOrderText()));
        }

        graphics.renderTooltip(
                this.font,
                components,
                mouseX,
                mouseY,
                DefaultTooltipPositioner.INSTANCE,
                null
        );
    }
}