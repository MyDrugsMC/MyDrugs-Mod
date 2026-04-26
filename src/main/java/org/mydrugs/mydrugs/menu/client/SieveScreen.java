package org.mydrugs.mydrugs.menu.client;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.menu.SieveMenu;
import org.mydrugs.mydrugs.menu.layout.SieveLayout;
import org.mydrugs.mydrugs.network.SieveShakePayload;

public class SieveScreen extends AbstractMachineScreen<SieveMenu> {
    private static final int KNOB_RADIUS = 5;

    private boolean draggingKnob = false;

    private float knobVisualY;
    private float knobTargetY;
    private float lastDragY;

    private float pendingImpulse = 0.0F;
    private long lastImpulseSendMs = 0L;

    public SieveScreen(SieveMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, SieveLayout.GUI_WIDTH, SieveLayout.GUI_HEIGHT);
        this.inventoryLabelY = standardInventoryLabelY(SieveLayout.PLAYER_INV_Y);
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
        drawWindowColored(graphics, 0xFF181818, 0xFF262626);

        drawPanel(
                graphics,
                SieveLayout.MACHINE_PANEL_X,
                SieveLayout.MACHINE_PANEL_Y,
                SieveLayout.MACHINE_PANEL_W,
                SieveLayout.MACHINE_PANEL_H,
                0xFF323232,
                0xFF595959,
                0xFF101010
        );

        drawSieveInventoryPanels(
                graphics,
                SieveLayout.PLAYER_INV_X,
                SieveLayout.PLAYER_INV_Y
        );

        drawSlotFrame(graphics, SieveLayout.INPUT_X, SieveLayout.INPUT_Y, 0xFF8A8A8A, 0xFF111111);
        drawSlotFrame(graphics, SieveLayout.RESULT_X, SieveLayout.RESULT_Y, 0xFF8A8A8A, 0xFF111111);
        drawSlotFrame(graphics, SieveLayout.BONUS_X, SieveLayout.BONUS_Y, 0xFF8A8A8A, 0xFF111111);

        drawShakeWidget(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        int machineTitleX = SieveLayout.MACHINE_PANEL_X + (SieveLayout.MACHINE_PANEL_W - this.font.width(this.title)) / 2;
        graphics.drawString(this.font, this.title, machineTitleX, 5, 0xFFFFFF, false);
        graphics.drawString(this.font, this.playerInventoryTitle, SieveLayout.PLAYER_INV_X, this.inventoryLabelY, 0xD0D0D0, false);
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

            ClientLevel level = Minecraft.getInstance().level;
            Player player = Minecraft.getInstance().player;

            if (level != null && player != null && (level.getDayTime() % 10 == 0)) {
                player.playSound(SoundEvents.SAND_HIT, 0.7F, 0.8F);
            }
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

    private void drawShakeWidget(GuiGraphics graphics) {
        int trackLeft = guiX(SieveLayout.WIDGET_X);
        int trackTop = guiY(SieveLayout.WIDGET_Y);

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

    private float getTrackMinY() {
        return guiY(SieveLayout.WIDGET_Y + 2 + KNOB_RADIUS);
    }

    private float getTrackMaxY() {
        return guiY(SieveLayout.WIDGET_Y + SieveLayout.WIDGET_H - 2 - KNOB_RADIUS);
    }

    private float getTrackCenterY() {
        return (this.getTrackMinY() + this.getTrackMaxY()) * 0.5F;
    }

    private int getKnobCenterX() {
        return guiX(SieveLayout.WIDGET_X + SieveLayout.WIDGET_W / 2);
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
}