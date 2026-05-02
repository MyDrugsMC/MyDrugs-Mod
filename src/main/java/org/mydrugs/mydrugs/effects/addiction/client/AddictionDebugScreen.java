package org.mydrugs.mydrugs.effects.addiction.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionDebugActionPayload;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionDebugOpenPayload;

import java.util.List;

public final class AddictionDebugScreen extends Screen {
    private static final int WIDTH = 360;
    private static final int HEIGHT = 220;
    private static final int ROW_H = 28;

    private AddictionDebugOpenPayload payload;
    private int left;
    private int top;
    private int scrollOffset;

    public AddictionDebugScreen(AddictionDebugOpenPayload payload) {
        super(Component.translatable("screen.mydrugs.addiction_debug.title"));
        this.payload = payload;
    }

    @Override
    protected void init() {
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - HEIGHT) / 2;
        this.scrollOffset = Math.clamp(this.scrollOffset, 0, maxScroll());
    }

    public void update(AddictionDebugOpenPayload payload) {
        this.payload = payload;
        this.scrollOffset = Math.clamp(this.scrollOffset, 0, maxScroll());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xB0000000);
        drawPanel(graphics, this.left, this.top, WIDTH, HEIGHT, 0xF018161D);
        graphics.drawCenteredString(this.font, this.title, this.left + WIDTH / 2, this.top + 8, 0xFFFFFFFF);

        int statsY = this.top + 26;
        graphics.drawString(this.font, "Genetic " + fmt(this.payload.geneticFactor()), this.left + 12, statsY, 0xFFE6DFF0, false);
        graphics.drawString(this.font, "Resilience " + fmt(this.payload.resilience()), this.left + 110, statsY, 0xFFE6DFF0, false);
        graphics.drawString(this.font, "Stress " + fmt(this.payload.stressLevel()), this.left + 220, statsY, 0xFFE6DFF0, false);

        drawButton(graphics, resetX(), resetY(), 72, 18, "Reset", 0xFF4B2730, 0xFFFFB8C3);
        drawCheckbox(graphics, immuneX(), immuneY(), this.payload.symptomsImmune());
        graphics.drawString(this.font, "Immune", immuneX() + 15, immuneY() + 2, 0xFFE6DFF0, false);

        int listX = this.left + 10;
        int listY = this.top + 54;
        int listW = WIDTH - 20;
        int listH = HEIGHT - 66;
        graphics.fill(listX, listY, listX + listW, listY + listH, 0xFF100E14);
        drawHeader(graphics, listX + 6, listY + 5);

        int visibleRows = visibleRows();
        for (int row = 0; row < visibleRows; row++) {
            int index = this.scrollOffset + row;
            if (index >= this.payload.rows().size()) {
                break;
            }
            drawRow(graphics, this.payload.rows().get(index), listX + 6, listY + 19 + row * ROW_H, row % 2 == 0);
        }
        drawScrollbar(graphics, listX + listW - 5, listY + 2, listH - 4);

        if (isOverImmune(mouseX, mouseY)) {
            renderTooltip(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.addiction_debug.immune.tooltip"));
        }
        if (isOverReset(mouseX, mouseY)) {
            renderTooltip(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.addiction_debug.reset.tooltip"));
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClicked) {
        if (event.button() == 0) {
            int mouseX = (int) event.x();
            int mouseY = (int) event.y();
            if (isOverReset(mouseX, mouseY)) {
                ClientPacketDistributor.sendToServer(new AddictionDebugActionPayload(AddictionDebugActionPayload.RESET_STATS, false));
                return true;
            }
            if (isOverImmune(mouseX, mouseY)) {
                boolean next = !this.payload.symptomsImmune();
                this.payload = new AddictionDebugOpenPayload(
                        this.payload.geneticFactor(),
                        this.payload.resilience(),
                        this.payload.stressLevel(),
                        next,
                        this.payload.rows()
                );
                ClientPacketDistributor.sendToServer(new AddictionDebugActionPayload(AddictionDebugActionPayload.SET_SYMPTOM_IMMUNITY, next));
                return true;
            }
        }
        return super.mouseClicked(event, doubleClicked);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= this.left + 10 && mouseX <= this.left + WIDTH - 10 && mouseY >= this.top + 54 && mouseY <= this.top + HEIGHT - 12) {
            this.scrollOffset = Math.clamp(this.scrollOffset - (int) Math.signum(scrollY), 0, maxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void drawHeader(GuiGraphics graphics, int x, int y) {
        graphics.drawString(this.font, "Drug", x, y, 0xFFB8B0C5, false);
        graphics.drawString(this.font, "Add", x + 72, y, 0xFFB8B0C5, false);
        graphics.drawString(this.font, "Wd", x + 110, y, 0xFFB8B0C5, false);
        graphics.drawString(this.font, "Tol", x + 145, y, 0xFFB8B0C5, false);
        graphics.drawString(this.font, "Dose", x + 180, y, 0xFFB8B0C5, false);
        graphics.drawString(this.font, "Peak", x + 225, y, 0xFFB8B0C5, false);
        graphics.drawString(this.font, "State / extra", x + 270, y, 0xFFB8B0C5, false);
    }

    private void drawRow(GuiGraphics graphics, AddictionDebugOpenPayload.DrugStatsRow row, int x, int y, boolean even) {
        graphics.fill(x - 3, y - 2, x + WIDTH - 31, y + ROW_H - 3, even ? 0xFF17131D : 0xFF120F17);
        graphics.drawString(this.font, trim(row.drugId(), 68), x, y, 0xFFE6DFF0, false);
        graphics.drawString(this.font, fmt(row.addiction()), x + 72, y, 0xFFFFFFFF, false);
        graphics.drawString(this.font, fmt(row.withdrawal()), x + 110, y, 0xFFFFFFFF, false);
        graphics.drawString(this.font, fmt(row.tolerance()), x + 145, y, 0xFFFFFFFF, false);
        graphics.drawString(this.font, fmt(row.currentDose()), x + 180, y, 0xFFFFFFFF, false);
        graphics.drawString(this.font, fmt(row.peakAddiction()), x + 225, y, 0xFFFFFFFF, false);
        graphics.drawString(this.font, trim(row.lastDoseState(), 54), x + 270, y, 0xFFFFFFFF, false);
        graphics.drawString(
                this.font,
                "Rel " + fmt(row.relapseMemory()) + "  parts " + row.activeDoseContributions() + "  last " + row.lastUseTime(),
                x + 72,
                y + 11,
                0xFFB8B0C5,
                false
        );
    }

    private void drawScrollbar(GuiGraphics graphics, int x, int y, int h) {
        graphics.fill(x, y, x + 3, y + h, 0xFF2A2532);
        if (maxScroll() <= 0) {
            return;
        }
        int thumbH = Math.max(16, h * visibleRows() / Math.max(1, this.payload.rows().size()));
        int thumbY = y + (h - thumbH) * this.scrollOffset / maxScroll();
        graphics.fill(x, thumbY, x + 3, thumbY + thumbH, 0xFF9A4DFF);
    }

    private void drawPanel(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + h, color);
        graphics.fill(x, y, x + w, y + 1, 0xFF72677E);
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFF09070C);
        graphics.fill(x, y, x + 1, y + h, 0xFF72677E);
        graphics.fill(x + w - 1, y, x + w, y + h, 0xFF09070C);
    }

    private void drawButton(GuiGraphics graphics, int x, int y, int w, int h, String label, int fill, int text) {
        graphics.fill(x, y, x + w, y + h, fill);
        graphics.fill(x, y, x + w, y + 1, 0xFF7C6D86);
        graphics.fill(x, y + h - 1, x + w, y + h, 0xFF171019);
        graphics.drawCenteredString(this.font, label, x + w / 2, y + 5, text);
    }

    private void drawCheckbox(GuiGraphics graphics, int x, int y, boolean checked) {
        graphics.fill(x, y, x + 11, y + 11, 0xFF08070A);
        graphics.fill(x + 1, y + 1, x + 10, y + 10, checked ? 0xFF7E49D8 : 0xFF241F2C);
        if (checked) {
            graphics.fill(x + 3, y + 6, x + 5, y + 8, 0xFFFFFFFF);
            graphics.fill(x + 5, y + 8, x + 7, y + 10, 0xFFFFFFFF);
            graphics.fill(x + 7, y + 3, x + 9, y + 8, 0xFFFFFFFF);
        }
    }

    private int visibleRows() {
        return 5;
    }

    private int maxScroll() {
        return Math.max(0, this.payload.rows().size() - visibleRows());
    }

    private int resetX() {
        return this.left + WIDTH - 90;
    }

    private int resetY() {
        return this.top + 24;
    }

    private int immuneX() {
        return this.left + 12;
    }

    private int immuneY() {
        return this.top + HEIGHT - 18;
    }

    private boolean isOverReset(int mouseX, int mouseY) {
        return mouseX >= resetX() && mouseX < resetX() + 72 && mouseY >= resetY() && mouseY < resetY() + 18;
    }

    private boolean isOverImmune(int mouseX, int mouseY) {
        return mouseX >= immuneX() && mouseX < immuneX() + 70 && mouseY >= immuneY() && mouseY < immuneY() + 12;
    }

    private static String fmt(float value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private String trim(String text, int width) {
        return this.font.width(text) <= width ? text : this.font.plainSubstrByWidth(text, Math.max(0, width - this.font.width("..."))) + "...";
    }

    private void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY, Component line) {
        graphics.renderTooltip(
                this.font,
                List.of(ClientTooltipComponent.create(line.getVisualOrderText())),
                mouseX,
                mouseY,
                DefaultTooltipPositioner.INSTANCE,
                null
        );
    }
}
