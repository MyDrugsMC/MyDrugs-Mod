package org.mydrugs.mydrugs.menu.client;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.menu.ManualCoffeePulperMenu;
import org.mydrugs.mydrugs.menu.client.util.DrugBonusClientText;
import org.mydrugs.mydrugs.menu.layout.ManualCoffeePulperLayout;
import org.mydrugs.mydrugs.network.CoffeePulperDragPayload;

public class ManualCoffeePulperScreen extends AbstractMachineScreen<ManualCoffeePulperMenu> {
    private static final int ROLLER_W = 30;
    private static final int ROLLER_H = 10;
    private boolean dragging;
    private float lastDragY;
    private float rollerOffset;
    private float pendingWork;
    private long lastSendMs;

    public ManualCoffeePulperScreen(ManualCoffeePulperMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, ManualCoffeePulperLayout.IMAGE_WIDTH, ManualCoffeePulperLayout.IMAGE_HEIGHT);
        this.inventoryLabelY = standardInventoryLabelY(ManualCoffeePulperLayout.PLAYER_INV_Y);
    }

    @Override
    protected boolean shouldShowTransferConfigButton() {
        return false;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!dragging) {
            rollerOffset = Mth.lerp(0.25F, rollerOffset, 0.0F);
        }
        flushPending(false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);
        drawPanel(graphics, 18, 16, 140, 62, 0xFF33271E, 0xFF6B5138, 0xFF160F0A);
        drawSieveInventoryPanels(graphics, ManualCoffeePulperLayout.PLAYER_INV_X, ManualCoffeePulperLayout.PLAYER_INV_Y);
        drawSlotFrame(graphics, ManualCoffeePulperLayout.INPUT_X, ManualCoffeePulperLayout.INPUT_Y, 0xFF9A7A55, 0xFF130D08);
        drawSlotFrame(graphics, ManualCoffeePulperLayout.BEAN_OUTPUT_X, ManualCoffeePulperLayout.BEAN_OUTPUT_Y, 0xFF9A7A55, 0xFF130D08);
        drawSlotFrame(graphics, ManualCoffeePulperLayout.BIOMASS_OUTPUT_X, ManualCoffeePulperLayout.BIOMASS_OUTPUT_Y, 0xFF9A7A55, 0xFF130D08);
        drawRoller(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 5, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle, ManualCoffeePulperLayout.PLAYER_INV_X, this.inventoryLabelY, 0xD0D0D0, false);
        graphics.drawCenteredString(this.font, Component.literal(this.menu.getProgress() + "/" + this.menu.getMaxProgress()), 88, 68, 0xE6D7BA);
        DrugBonusClientText.drawManualWorkBonus(graphics, this.font, -leftPos + 5, 12);
    }

    private void drawRoller(GuiGraphics graphics) {
        int left = guiX(ManualCoffeePulperLayout.WIDGET_X);
        int top = guiY(ManualCoffeePulperLayout.WIDGET_Y);
        graphics.fill(left - 3, top - 3, left + ManualCoffeePulperLayout.WIDGET_W + 3, top + ManualCoffeePulperLayout.WIDGET_H + 3, 0xFF1C1410);
        graphics.fill(left, top, left + ManualCoffeePulperLayout.WIDGET_W, top + ManualCoffeePulperLayout.WIDGET_H, 0xFF3A2C20);

        int progress = this.menu.getScaledProgress(ManualCoffeePulperLayout.WIDGET_H - 6);
        if (progress > 0) {
            graphics.fill(left + ManualCoffeePulperLayout.WIDGET_W - 4, top + ManualCoffeePulperLayout.WIDGET_H - 3 - progress, left + ManualCoffeePulperLayout.WIDGET_W - 2, top + ManualCoffeePulperLayout.WIDGET_H - 3, 0xFFA8753B);
        }

        int rollerX = left + 3;
        int rollerY = Math.round(top + 6 + rollerOffset);
        graphics.fill(rollerX, rollerY, rollerX + ROLLER_W, rollerY + ROLLER_H, 0xFFD8C08D);
        graphics.fill(rollerX + 1, rollerY + 2, rollerX + ROLLER_W - 1, rollerY + 4, 0xFF8F6B3C);
        int diagonal = Math.floorMod(Math.round(this.menu.getRollerAngle() / 18.0F), 6);
        for (int i = -6; i < ROLLER_W; i += 6) {
            graphics.fill(rollerX + i + diagonal, rollerY, rollerX + i + diagonal + 2, rollerY + ROLLER_H, 0x66432B17);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == 0 && isOverWidget(event.x(), event.y())) {
            dragging = true;
            lastDragY = (float) event.y();
            return true;
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging && event.button() == 0) {
            float y = (float) event.y();
            float delta = y - lastDragY;
            lastDragY = y;
            if (delta > 0.0F) {
                rollerOffset = Mth.clamp(rollerOffset + delta, 0.0F, ManualCoffeePulperLayout.WIDGET_H - 16);
                queueWork(delta * 0.55F);
            } else if (delta < 0.0F) {
                rollerOffset = Math.max(0.0F, rollerOffset + delta * 0.5F);
            }
            flushPending(false);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging && event.button() == 0) {
            dragging = false;
            flushPending(true);
            return true;
        }
        return super.mouseReleased(event);
    }

    private boolean isOverWidget(double mouseX, double mouseY) {
        int left = guiX(ManualCoffeePulperLayout.WIDGET_X);
        int top = guiY(ManualCoffeePulperLayout.WIDGET_Y);
        return mouseX >= left && mouseX < left + ManualCoffeePulperLayout.WIDGET_W && mouseY >= top && mouseY < top + ManualCoffeePulperLayout.WIDGET_H;
    }

    private void queueWork(float amount) {
        if (amount > 0.0F) pendingWork = Math.min(10.0F, pendingWork + amount);
    }

    private void flushPending(boolean force) {
        long now = Util.getMillis();
        if (!force && now - lastSendMs < 25L) return;
        if (pendingWork <= 0.02F) return;
        ClientPacketDistributor.sendToServer(new CoffeePulperDragPayload(this.menu.getMenuId(), pendingWork));
        pendingWork = 0.0F;
        lastSendMs = now;
    }
}
