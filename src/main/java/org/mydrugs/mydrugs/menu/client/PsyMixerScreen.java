package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.menu.PsyMixerMenu;
import org.mydrugs.mydrugs.network.PsyMixerRitualInputPayload;
import org.mydrugs.mydrugs.network.PsyMixerStartRitualPayload;

public final class PsyMixerScreen extends AbstractContainerScreen<PsyMixerMenu> {
    private static final ResourceLocation BG = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    private Button beginButton;
    private Button tapButton;

    public PsyMixerScreen(PsyMixerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        beginButton = Button.builder(
                Component.translatable("screen.mydrugs.psy_mixer.begin"),
                btn -> {
                    if (!menu.isRunning()) {
                        ClientPacketDistributor.sendToServer(new PsyMixerStartRitualPayload(menu.getMenuId(), menu.getCorePos()));
                    }
                }
        ).bounds(this.leftPos + 8, this.topPos + 60, 60, 16).build();
        this.addRenderableWidget(beginButton);

        tapButton = Button.builder(
                Component.translatable("screen.mydrugs.psy_mixer.tap"),
                btn -> {
                    if (menu.isRunning()) {
                        ClientPacketDistributor.sendToServer(new PsyMixerRitualInputPayload(menu.getMenuId(), menu.getServerPhase()));
                    }
                }
        ).bounds(this.leftPos + 130, this.topPos + 60, 40, 16).build();
        this.addRenderableWidget(tapButton);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Solid dark ritual background
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xFF1A0E12);
        graphics.fill(this.leftPos + 4, this.topPos + 4, this.leftPos + this.imageWidth - 4, this.topPos + this.imageHeight - 4, 0xFF2A1A22);

        int cx = this.leftPos + 80;
        int cy = this.topPos + 30;
        // Ritual circle decoration
        for (int i = 0; i < 32; i++) {
            double angle = i * Math.PI * 2.0 / 32.0;
            int dx = (int) Math.round(Math.cos(angle) * 38.0);
            int dy = (int) Math.round(Math.sin(angle) * 38.0);
            graphics.fill(cx + dx - 1, cy + dy - 1, cx + dx + 1, cy + dy + 1, 0xFF6A3A4A);
        }

        // PASS 2: rhythm timing ring
        if (menu.isRunning()) {
            float window = menu.getTimingWindow();
            float phase = menu.getServerPhase();
            // Sacred zone arc at phase 0 +/- window/2 (drawn as bright dots)
            for (int i = 0; i < 64; i++) {
                float p = i / 64.0F;
                float dist = Math.min(p, 1.0F - p);
                if (dist <= window / 2.0F) {
                    double angle = -Math.PI / 2.0 + p * Math.PI * 2.0;
                    int dx = (int) Math.round(Math.cos(angle) * 44.0);
                    int dy = (int) Math.round(Math.sin(angle) * 44.0);
                    graphics.fill(cx + dx - 2, cy + dy - 2, cx + dx + 2, cy + dy + 2, 0xFFCCAA44);
                }
            }
            // Marker at current phase
            double markerAngle = -Math.PI / 2.0 + phase * Math.PI * 2.0;
            int mx = (int) Math.round(Math.cos(markerAngle) * 44.0);
            int my = (int) Math.round(Math.sin(markerAngle) * 44.0);
            graphics.fill(cx + mx - 3, cy + my - 3, cx + mx + 3, cy + my + 3, 0xFFFFEEAA);
        }

        // Slot wells (small darker squares behind each slot)
        drawSlotWell(graphics, cx, cy);
        drawSlotWell(graphics, cx - 28, cy);
        drawSlotWell(graphics, cx, cy - 28);
        drawSlotWell(graphics, cx + 28, cy);
        drawSlotWell(graphics, cx, cy + 28);
        drawSlotWell(graphics, cx + 58, cy);

        // Player inv backdrop
        graphics.fill(this.leftPos + 6, this.topPos + 82, this.leftPos + this.imageWidth - 6, this.topPos + 162, 0xFF1A0E12);
    }

    private void drawSlotWell(GuiGraphics graphics, int cx, int cy) {
        graphics.fill(cx - 1, cy - 1, cx + 17, cy + 17, 0xFF120608);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        int instabBar = (int) (Math.min(1.0F, menu.getInstability()) * 80.0F);
        graphics.fill(8, 76, 88, 78, 0xFF222222);
        graphics.fill(8, 76, 8 + instabBar, 78, 0xFFCC2244);

        if (menu.isRunning()) {
            int progBar = menu.getMaxProgress() > 0
                    ? (int) (((float) menu.getProgress() / menu.getMaxProgress()) * 80.0F)
                    : 0;
            graphics.fill(8, 70, 88, 73, 0xFF221122);
            graphics.fill(8, 70, 8 + progBar, 73, 0xFFAA66CC);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (beginButton != null) {
            beginButton.active = !menu.isRunning();
        }
        if (tapButton != null) {
            tapButton.active = menu.isRunning();
        }
    }

}
