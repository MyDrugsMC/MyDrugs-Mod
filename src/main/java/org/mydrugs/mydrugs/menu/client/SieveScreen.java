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
import org.mydrugs.mydrugs.menu.client.util.DrugBonusClientText;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.SieveLayout;
import org.mydrugs.mydrugs.network.SieveShakePayload;

import java.util.List;

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
    protected boolean shouldRenderSharedEnergyBar() {
        return false;
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
        MachineGuiRenderer.drawSieve(this, graphics, new MachineGuiRenderer.SieveState(Math.round(this.knobVisualY) - this.topPos), true);
        if (this.menu.hasEnergyStorage()) {
            drawExternalEnergyBar(graphics, this.menu.getEnergyStored(), this.menu.getEnergyCapacity());
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawSieveLabels(this, graphics, this.font, this.title, this.playerInventoryTitle, this.inventoryLabelY, null);
        DrugBonusClientText.drawManualWorkBonus(graphics, this.font, -leftPos + 5, 12);
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "item_input" -> List.of(slotHighlight(SieveLayout.INPUT_X, SieveLayout.INPUT_Y));
            case "item_output" -> List.of(
                    slotHighlight(SieveLayout.RESULT_X, SieveLayout.RESULT_Y),
                    slotHighlight(SieveLayout.BONUS_X, SieveLayout.BONUS_Y)
            );
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.menu.hasEnergyStorage()) {
            renderExternalEnergyTooltip(graphics, mouseX, mouseY, this.menu.getEnergyStored(), this.menu.getEnergyCapacity());
        }
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
