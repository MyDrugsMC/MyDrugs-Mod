package org.mydrugs.mydrugs.menu.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.menu.SieveLayout;
import org.mydrugs.mydrugs.menu.SieveMenu;
import org.mydrugs.mydrugs.network.SieveShakePayload;

public class SieveScreen extends AbstractContainerScreen<SieveMenu> {
    private static final int KNOB_RADIUS = 5;

    private boolean draggingKnob = false;

    private float knobVisualY;
    private float knobTargetY;
    private float lastDragY;

    private float pendingImpulse = 0.0F;
    private long lastImpulseSendMs = 0L;

    public SieveScreen(SieveMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = SieveLayout.GUI_WIDTH;
        this.imageHeight = SieveLayout.GUI_HEIGHT;
        this.inventoryLabelY = 54;
    }

    @Override
    protected void init() {
        super.init();
        float center = this.getTrackCenterY();
        this.knobVisualY = center;
        this.knobTargetY = center;
        this.lastDragY = center;
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        float minY = this.getTrackMinY();
        float maxY = this.getTrackMaxY();
        float center = this.getTrackCenterY();

        if (!this.draggingKnob) {
            this.knobTargetY = Mth.lerp(0.20F, this.knobTargetY, center);
            this.knobTargetY = Mth.clamp(this.knobTargetY, minY, maxY);
            this.knobVisualY = Mth.lerp(0.42F, this.knobVisualY, this.knobTargetY);
        } else {
            this.knobTargetY = Mth.clamp(this.knobTargetY, minY, maxY);
            this.knobVisualY = this.knobTargetY;
        }

        this.flushPendingImpulse(false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF181818);
        graphics.fill(left + 4, top + 4, left + this.imageWidth - 4, top + this.imageHeight - 4, 0xFF262626);

        // machine panel
        fillPanel(graphics,
                left + SieveLayout.MACHINE_PANEL_X,
                top + SieveLayout.MACHINE_PANEL_Y,
                SieveLayout.MACHINE_PANEL_W,
                SieveLayout.MACHINE_PANEL_H,
                0xFF323232);

        // player inventory
        fillPanel(graphics,
                left + SieveLayout.PLAYER_INV_X,
                top + SieveLayout.PLAYER_INV_Y,
                SieveLayout.PLAYER_INV_W,
                SieveLayout.PLAYER_INV_H,
                0xFF2C2C2C);

        // hotbar
        fillPanel(graphics,
                left + SieveLayout.HOTBAR_X,
                top + SieveLayout.HOTBAR_Y,
                SieveLayout.HOTBAR_W,
                SieveLayout.HOTBAR_H,
                0xFF2C2C2C);

        // subtle divider between machine and inventory
        // graphics.fill(left + 12, top + 53, left + this.imageWidth - 12, top + 54, 0xFF4A4A4A);

        drawSlotFrame(graphics, SieveLayout.INPUT_X, SieveLayout.INPUT_Y);
        drawSlotFrame(graphics, SieveLayout.RESULT_X, SieveLayout.RESULT_Y);
        drawSlotFrame(graphics, SieveLayout.BONUS_X, SieveLayout.BONUS_Y);

        drawShakeWidget(graphics);
    }

    private void fillPanel(GuiGraphics graphics, int x, int y, int width, int height, int fillColor) {
        graphics.fill(x, y, x + width, y + height, fillColor);
        drawBorder(graphics, x, y, width, height);
    }

    private void drawSlotFrame(GuiGraphics graphics, int slotX, int slotY) {
        int x = this.leftPos + slotX - 1;
        int y = this.topPos + slotY - 1;

        graphics.fill(x, y, x + 18, y + 18, 0xFF8A8A8A);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF111111);
    }

    private void drawShakeWidget(GuiGraphics graphics) {
        int trackLeft = this.leftPos + SieveLayout.WIDGET_X;
        int trackTop = this.topPos + SieveLayout.WIDGET_Y;

        graphics.fill(trackLeft - 2, trackTop - 2, trackLeft + SieveLayout.WIDGET_W + 2, trackTop + SieveLayout.WIDGET_H + 2, 0xFF5A5A5A);
        graphics.fill(trackLeft - 1, trackTop - 1, trackLeft + SieveLayout.WIDGET_W + 1, trackTop + SieveLayout.WIDGET_H + 1, 0xFF161616);

        int shaftX1 = trackLeft + 7;
        int shaftX2 = trackLeft + 11;
        graphics.fill(shaftX1, trackTop + 2, shaftX2, trackTop + SieveLayout.WIDGET_H - 2, 0xFF090909);

        graphics.fill(trackLeft + 5, trackTop + 1, trackLeft + 13, trackTop + 3, 0xFF727272);
        graphics.fill(trackLeft + 5, trackTop + SieveLayout.WIDGET_H - 3, trackLeft + 13, trackTop + SieveLayout.WIDGET_H - 1, 0xFF0E0E0E);

        int centerX = trackLeft + SieveLayout.WIDGET_W / 2;
        int centerY = Math.round(this.knobVisualY);

        drawCircle(graphics, centerX, centerY, KNOB_RADIUS + 1, 0xFFBABABA);
        drawCircle(graphics, centerX, centerY, KNOB_RADIUS, 0xFF3B3B3B);
        drawCircle(graphics, centerX - 1, centerY - 1, 1, 0xFFE8E8E8);
    }

    private void drawCircle(GuiGraphics graphics, int cx, int cy, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    graphics.fill(cx + x, cy + y, cx + x + 1, cy + y + 1, color);
                }
            }
        }
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + 1, 0xFF595959);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF101010);
        graphics.fill(x, y, x + 1, y + height, 0xFF595959);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF101010);
    }

    private float getTrackMinY() {
        return this.topPos + SieveLayout.WIDGET_Y + 2 + KNOB_RADIUS;
    }

    private float getTrackMaxY() {
        return this.topPos + SieveLayout.WIDGET_Y + SieveLayout.WIDGET_H - 2 - KNOB_RADIUS;
    }

    private float getTrackCenterY() {
        return (this.getTrackMinY() + this.getTrackMaxY()) * 0.5F;
    }

    private int getKnobCenterX() {
        return this.leftPos + SieveLayout.WIDGET_X + SieveLayout.WIDGET_W / 2;
    }

    private boolean isMouseOverKnob(double mouseX, double mouseY) {
        double dx = mouseX - this.getKnobCenterX();
        double dy = mouseY - this.knobVisualY;
        int hitRadius = KNOB_RADIUS + 2;
        return dx * dx + dy * dy <= hitRadius * hitRadius;
    }

    private void queueImpulse(float amount) {
        if (amount <= 0.0F) {
            return;
        }
        this.pendingImpulse = Math.min(this.pendingImpulse + amount, 6.0F);
    }

    private void flushPendingImpulse(boolean force) {
        long now = Util.getMillis();
        if (!force && now - this.lastImpulseSendMs < 25L) {
            return;
        }

        if (this.pendingImpulse <= 0.02F) {
            return;
        }

        ClientPacketDistributor.sendToServer(new SieveShakePayload(this.menu.getMenuId(), this.pendingImpulse));
        this.pendingImpulse = 0.0F;
        this.lastImpulseSendMs = now;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == 0 && this.isMouseOverKnob(event.x(), event.y())) {
            this.draggingKnob = true;
            this.lastDragY = (float) event.y();
            this.knobTargetY = Mth.clamp((float) event.y(), this.getTrackMinY(), this.getTrackMaxY());
            this.knobVisualY = this.knobTargetY;
            return true;
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.draggingKnob && event.button() == 0) {
            float clampedY = Mth.clamp((float) event.y(), this.getTrackMinY(), this.getTrackMaxY());
            float dy = clampedY - this.lastDragY;

            this.lastDragY = clampedY;
            this.knobTargetY = clampedY;
            this.knobVisualY = clampedY;

            float impulse = Math.min(Math.abs(dy) * 0.30F, 4.0F);
            this.queueImpulse(impulse);
            this.flushPendingImpulse(false);
            if (Minecraft.getInstance())
                return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.draggingKnob && event.button() == 0) {
            this.draggingKnob = false;
            this.flushPendingImpulse(true);
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int machineTitleX = SieveLayout.MACHINE_PANEL_X + (SieveLayout.MACHINE_PANEL_W - this.font.width(this.title)) / 2;
        graphics.drawString(this.font, this.title, machineTitleX, 4, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0xD0D0D0, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}