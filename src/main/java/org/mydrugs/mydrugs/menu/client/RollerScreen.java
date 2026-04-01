package org.mydrugs.mydrugs.menu.client;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.menu.layout.RollerLayout;
import org.mydrugs.mydrugs.menu.RollerMenu;
import org.mydrugs.mydrugs.network.RollerDragPayload;

public class RollerScreen extends AbstractContainerScreen<RollerMenu> {
    private static final int ROLL_RECT_W = 12;
    private static final int ROLL_RECT_H = 12;

    private boolean draggingRoll = false;

    private float rollVisualY;
    private float rollTargetY;
    private float lastDragY;

    private float pendingRoll = 0.0F;
    private long lastRollSendMs = 0L;

    public RollerScreen(RollerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = RollerLayout.GUI_WIDTH;
        this.imageHeight = RollerLayout.GUI_HEIGHT;
        this.inventoryLabelY = 74;
    }

    @Override
    protected void init() {
        super.init();
        float restY = this.getRollRestY();
        this.rollVisualY = restY;
        this.rollTargetY = restY;
        this.lastDragY = restY;
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        float minY = this.getRollMinY();
        float maxY = this.getRollRestY();

        if (!this.draggingRoll) {
            this.rollTargetY = Mth.lerp(0.22F, this.rollTargetY, maxY);
            this.rollTargetY = Mth.clamp(this.rollTargetY, minY, maxY);
            this.rollVisualY = Mth.lerp(0.42F, this.rollVisualY, this.rollTargetY);
        } else {
            this.rollTargetY = Mth.clamp(this.rollTargetY, minY, maxY);
            this.rollVisualY = this.rollTargetY;
        }

        this.flushPendingRoll(false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF171717);
        graphics.fill(left + 4, top + 4, left + this.imageWidth - 4, top + this.imageHeight - 4, 0xFF252525);

        fillPanel(
                graphics,
                left + RollerLayout.MACHINE_PANEL_X,
                top + RollerLayout.MACHINE_PANEL_Y,
                RollerLayout.MACHINE_PANEL_W,
                RollerLayout.MACHINE_PANEL_H,
                0xFF313131
        );

        fillPanel(
                graphics,
                left + RollerLayout.PLAYER_INV_X,
                top + RollerLayout.PLAYER_INV_Y,
                RollerLayout.PLAYER_INV_W,
                RollerLayout.PLAYER_INV_H,
                0xFF2C2C2C
        );

        fillPanel(
                graphics,
                left + RollerLayout.HOTBAR_X,
                top + RollerLayout.HOTBAR_Y,
                RollerLayout.HOTBAR_W,
                RollerLayout.HOTBAR_H,
                0xFF2C2C2C
        );

        drawSlotFrame(graphics, RollerLayout.PAPER_X, RollerLayout.PAPER_Y);
        drawSlotFrame(graphics, RollerLayout.FILTER_X, RollerLayout.FILTER_Y);
        drawSlotFrame(graphics, RollerLayout.INGREDIENT_1_X, RollerLayout.INGREDIENT_1_Y);
        drawSlotFrame(graphics, RollerLayout.INGREDIENT_2_X, RollerLayout.INGREDIENT_2_Y);
        drawSlotFrame(graphics, RollerLayout.INGREDIENT_3_X, RollerLayout.INGREDIENT_3_Y);
        drawSlotFrame(graphics, RollerLayout.OUTPUT_X, RollerLayout.OUTPUT_Y);

        drawRollWidget(graphics);
    }

    private void fillPanel(GuiGraphics graphics, int x, int y, int width, int height, int fillColor) {
        graphics.fill(x, y, x + width, y + height, fillColor);
        drawBorder(graphics, x, y, width, height);
    }

    private void drawSlotFrame(GuiGraphics graphics, int slotX, int slotY) {
        int x = this.leftPos + slotX - 1;
        int y = this.topPos + slotY - 1;

        graphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF111111);
    }

    private void drawRollWidget(GuiGraphics graphics) {
        int trackLeft = this.leftPos + RollerLayout.WIDGET_X;
        int trackTop = this.topPos + RollerLayout.WIDGET_Y;

        graphics.fill(trackLeft - 2, trackTop - 2, trackLeft + RollerLayout.WIDGET_W + 2, trackTop + RollerLayout.WIDGET_H + 2, 0xFF5A5A5A);
        graphics.fill(trackLeft - 1, trackTop - 1, trackLeft + RollerLayout.WIDGET_W + 1, trackTop + RollerLayout.WIDGET_H + 1, 0xFF161616);

        graphics.fill(trackLeft + 4, trackTop + 2, trackLeft + RollerLayout.WIDGET_W - 4, trackTop + RollerLayout.WIDGET_H - 2, 0xFF0D0D0D);

        int progressPixels = this.menu.getScaledProgress(RollerLayout.WIDGET_H - 4);
        if (progressPixels > 0) {
            graphics.fill(
                    trackLeft + 1,
                    trackTop + RollerLayout.WIDGET_H - 2 - progressPixels,
                    trackLeft + 3,
                    trackTop + RollerLayout.WIDGET_H - 2,
                    0xFF8FCB6A
            );
        }

        int rollX = trackLeft + (RollerLayout.WIDGET_W - ROLL_RECT_W) / 2;
        int rollY = Math.round(this.rollVisualY);

        graphics.fill(rollX, rollY, rollX + ROLL_RECT_W, rollY + ROLL_RECT_H, 0xFFD2C39C);
        graphics.fill(rollX + 1, rollY + 1, rollX + ROLL_RECT_W - 1, rollY + ROLL_RECT_H - 1, 0xFFB8A273);
        graphics.fill(rollX + 2, rollY + 2, rollX + ROLL_RECT_W - 2, rollY + 4, 0xFFE6D8B8);
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + 1, 0xFF595959);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF101010);
        graphics.fill(x, y, x + 1, y + height, 0xFF595959);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF101010);
    }

    private float getRollMinY() {
        return this.topPos + RollerLayout.WIDGET_Y + 2;
    }

    private float getRollRestY() {
        return this.topPos + RollerLayout.WIDGET_Y + RollerLayout.WIDGET_H - 2 - ROLL_RECT_H;
    }

    private int getRollX() {
        return this.leftPos + RollerLayout.WIDGET_X + (RollerLayout.WIDGET_W - ROLL_RECT_W) / 2;
    }

    private boolean isMouseOverRoll(double mouseX, double mouseY) {
        int x = this.getRollX();
        float y = this.rollVisualY;
        return mouseX >= x && mouseX < x + ROLL_RECT_W
                && mouseY >= y && mouseY < y + ROLL_RECT_H;
    }

    private void queueRoll(float amount) {
        if (amount <= 0.0F) {
            return;
        }
        this.pendingRoll = Math.min(this.pendingRoll + amount, 8.0F);
    }

    private void flushPendingRoll(boolean force) {
        long now = Util.getMillis();

        if (!force && now - this.lastRollSendMs < 25L) {
            return;
        }

        if (this.pendingRoll <= 0.02F) {
            return;
        }

        ClientPacketDistributor.sendToServer(new RollerDragPayload(this.menu.getMenuId(), this.pendingRoll));
        this.pendingRoll = 0.0F;
        this.lastRollSendMs = now;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == 0 && this.isMouseOverRoll(event.x(), event.y())) {
            this.draggingRoll = true;
            this.lastDragY = (float) event.y();
            this.rollTargetY = Mth.clamp((float) event.y(), this.getRollMinY(), this.getRollRestY());
            this.rollVisualY = this.rollTargetY;
            return true;
        }

        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.draggingRoll && event.button() == 0) {
            float clampedY = Mth.clamp((float) event.y(), this.getRollMinY(), this.getRollRestY());

            float upward = Math.max(0.0F, this.lastDragY - clampedY);

            this.lastDragY = clampedY;
            this.rollTargetY = clampedY;
            this.rollVisualY = clampedY;

            this.queueRoll(Math.min(upward * 0.75F, 4.0F));
            this.flushPendingRoll(false);
            return true;
        }

        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.draggingRoll && event.button() == 0) {
            this.draggingRoll = false;
            this.flushPendingRoll(true);
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = RollerLayout.MACHINE_PANEL_X + (RollerLayout.MACHINE_PANEL_W - this.font.width(this.title)) / 2;
        graphics.drawString(this.font, this.title, titleX, 4, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle, 8, this.inventoryLabelY, 0xD0D0D0, false);

        graphics.drawString(this.font, "Paper", RollerLayout.PAPER_X - 4, RollerLayout.PAPER_Y - 10, 0xCFCFCF, false);
        graphics.drawString(this.font, "Filter", RollerLayout.FILTER_X - 4, RollerLayout.FILTER_Y - 10, 0xCFCFCF, false);
        graphics.drawString(this.font, "Mix", RollerLayout.INGREDIENT_1_X + 8, RollerLayout.INGREDIENT_1_Y - 10, 0xCFCFCF, false);
        graphics.drawString(this.font, this.menu.getProgress() + "/" + this.menu.getMaxProgress(), 118, 18, 0xE6E6E6, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}