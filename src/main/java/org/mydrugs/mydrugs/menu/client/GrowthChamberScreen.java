package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.GrowthChamberMenu;
import org.mydrugs.mydrugs.menu.layout.GrowthChamberLayout;

import java.util.ArrayList;
import java.util.List;

public class GrowthChamberScreen extends AbstractContainerScreen<GrowthChamberMenu> {
    public GrowthChamberScreen(GrowthChamberMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GrowthChamberLayout.GUI_WIDTH;
        this.imageHeight = GrowthChamberLayout.GUI_HEIGHT;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF171717);
        graphics.fill(left + 4, top + 4, left + this.imageWidth - 4, top + this.imageHeight - 4, 0xFF252525);

        fillPanel(
                graphics,
                left + GrowthChamberLayout.MACHINE_PANEL_X,
                top + GrowthChamberLayout.MACHINE_PANEL_Y,
                GrowthChamberLayout.MACHINE_PANEL_W,
                GrowthChamberLayout.MACHINE_PANEL_H,
                0xFF2E3138
        );

        fillPanel(
                graphics,
                left + GrowthChamberLayout.INVENTORY_PANEL_X,
                top + GrowthChamberLayout.INVENTORY_PANEL_Y,
                GrowthChamberLayout.INVENTORY_PANEL_W,
                GrowthChamberLayout.INVENTORY_PANEL_H,
                0xFF2A2D33
        );

        drawTankFrame(graphics, GrowthChamberLayout.WATER_TANK_X, GrowthChamberLayout.WATER_TANK_Y);
        drawTankFill(
                graphics,
                left + GrowthChamberLayout.WATER_TANK_X,
                top + GrowthChamberLayout.WATER_TANK_Y,
                this.menu.getScaledWaterTank(GrowthChamberLayout.TANK_INNER_H),
                0xFF4F88D6
        );

        drawSlotFrame(graphics, GrowthChamberLayout.INPUT_SLOT_X, GrowthChamberLayout.INPUT_SLOT_Y);
        drawSlotFrame(graphics, GrowthChamberLayout.BIOMASS_SLOT_X, GrowthChamberLayout.BIOMASS_SLOT_Y);
        drawSlotFrame(graphics, GrowthChamberLayout.MIDDLE_SLOT_X, GrowthChamberLayout.MIDDLE_SLOT_Y);
        drawSlotFrame(graphics, GrowthChamberLayout.FINAL_SLOT_X, GrowthChamberLayout.FINAL_SLOT_Y);

        drawProgressBar(
                graphics,
                left + GrowthChamberLayout.GROWTH_PROGRESS_X,
                top + GrowthChamberLayout.GROWTH_PROGRESS_Y,
                GrowthChamberLayout.GROWTH_PROGRESS_W,
                GrowthChamberLayout.GROWTH_PROGRESS_H,
                this.menu.getScaledGrowthProgress(GrowthChamberLayout.GROWTH_PROGRESS_W),
                0xFF6FBF73,
                0xFFB7E0B9
        );

        drawProgressBar(
                graphics,
                left + GrowthChamberLayout.MATURE_PROGRESS_X,
                top + GrowthChamberLayout.MATURE_PROGRESS_Y,
                GrowthChamberLayout.MATURE_PROGRESS_W,
                GrowthChamberLayout.MATURE_PROGRESS_H,
                this.menu.getScaledMatureProgress(GrowthChamberLayout.MATURE_PROGRESS_W),
                0xFFB58C5A,
                0xFFE4C18F
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        //graphics.drawString(this.font, this.title, 14, 6, 0xFFD6DCE6, false);

        graphics.drawString(this.font, Component.literal("Water"), 16, 15, 0xFF9BB2D1, false);

        graphics.drawString(this.font, Component.literal("Growing"), GrowthChamberLayout.GROWTH_PROGRESS_X, GrowthChamberLayout.GROWTH_PROGRESS_Y - 10, 0xFFA9D8AC, false);
        graphics.drawString(this.font, Component.literal("Maturing"), GrowthChamberLayout.MATURE_PROGRESS_X, GrowthChamberLayout.MATURE_PROGRESS_Y - 10, 0xFFD7B78E, false);

        //graphics.drawString(this.font, Component.literal("Input"), GrowthChamberLayout.INPUT_SLOT_X - 2, GrowthChamberLayout.INPUT_SLOT_Y - 12, 0xFF9DA6B3, false);
        //graphics.drawString(this.font, Component.literal("Biomass"), GrowthChamberLayout.BIOMASS_SLOT_X - 6, GrowthChamberLayout.BIOMASS_SLOT_Y - 12, 0xFF9DA6B3, false);
        //graphics.drawString(this.font, Component.literal("Middle"), GrowthChamberLayout.MIDDLE_SLOT_X - 4, GrowthChamberLayout.MIDDLE_SLOT_Y - 12, 0xFF9DA6B3, false);
        //graphics.drawString(this.font, Component.literal("Final"), GrowthChamberLayout.FINAL_SLOT_X + 1, GrowthChamberLayout.FINAL_SLOT_Y - 12, 0xFF9DA6B3, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHoveringBox(
                GrowthChamberLayout.WATER_TANK_X,
                GrowthChamberLayout.WATER_TANK_Y,
                GrowthChamberLayout.TANK_W,
                GrowthChamberLayout.TANK_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Water tank"),
                    Component.literal(this.menu.getWaterAmount() + " / " + GrowthChamberMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(
                GrowthChamberLayout.GROWTH_PROGRESS_X,
                GrowthChamberLayout.GROWTH_PROGRESS_Y,
                GrowthChamberLayout.GROWTH_PROGRESS_W,
                GrowthChamberLayout.GROWTH_PROGRESS_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Growing progress"),
                    Component.literal(this.menu.getGrowthProgress() + " / " + this.menu.getGrowthMaxProgress())
            );
        } else if (isHoveringBox(
                GrowthChamberLayout.MATURE_PROGRESS_X,
                GrowthChamberLayout.MATURE_PROGRESS_Y,
                GrowthChamberLayout.MATURE_PROGRESS_W,
                GrowthChamberLayout.MATURE_PROGRESS_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Maturing progress"),
                    Component.literal(this.menu.getMatureProgress() + " / " + this.menu.getMatureMaxProgress())
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

        graphics.fill(x - 1, y - 1, x + GrowthChamberLayout.TANK_W + 1, y + GrowthChamberLayout.TANK_H + 1, 0xFF7F8590);
        graphics.fill(x, y, x + GrowthChamberLayout.TANK_W, y + GrowthChamberLayout.TANK_H, 0xFF15171B);

        graphics.fill(
                x + GrowthChamberLayout.TANK_INNER_X_OFFSET,
                y + GrowthChamberLayout.TANK_INNER_Y_OFFSET,
                x + GrowthChamberLayout.TANK_INNER_X_OFFSET + GrowthChamberLayout.TANK_INNER_W,
                y + GrowthChamberLayout.TANK_INNER_Y_OFFSET + GrowthChamberLayout.TANK_INNER_H,
                0xFF090A0D
        );
    }

    private void drawTankFill(GuiGraphics graphics, int tankX, int tankY, int filledPixels, int color) {
        if (filledPixels <= 0) {
            return;
        }

        int x1 = tankX + GrowthChamberLayout.TANK_INNER_X_OFFSET;
        int y1 = tankY + GrowthChamberLayout.TANK_INNER_Y_OFFSET;
        int x2 = x1 + GrowthChamberLayout.TANK_INNER_W;
        int y2 = y1 + GrowthChamberLayout.TANK_INNER_H;
        int fillTop = y2 - filledPixels;

        graphics.fill(x1, fillTop, x2, y2, color);
        graphics.fill(x1, fillTop, x2, fillTop + 2, 0xFFA6C8FF);
    }

    private void drawProgressBar(GuiGraphics graphics, int x, int y, int width, int height, int progress, int fillColor, int highlightColor) {
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF767C88);
        graphics.fill(x, y, x + width, y + height, 0xFF101216);

        if (progress > 0) {
            int clamped = Math.min(width, progress);
            graphics.fill(x, y, x + clamped, y + height, fillColor);
            graphics.fill(x, y, x + clamped, y + 2, highlightColor);
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