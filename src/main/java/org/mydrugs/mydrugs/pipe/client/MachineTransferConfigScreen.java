package org.mydrugs.mydrugs.pipe.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.pipe.machine.MachineLocalSide;
import org.mydrugs.mydrugs.pipe.machine.MachineOrientation;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferAccess;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferConfigMenu;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferPortSpec;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferResourceKind;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferSideRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MachineTransferConfigScreen extends AbstractContainerScreen<MachineTransferConfigMenu> {
    private static final int PORT_LIST_X = 8;
    private static final int PORT_LIST_Y = 30;
    private static final int PORT_LIST_W = 114;
    private static final int PORT_ROW_H = 24;
    private static final int PORT_ROW_GAP = 3;
    private static final int VISIBLE_PORT_ROWS = 5;
    private static final int SIDE_BUTTON_W = 30;
    private static final int SIDE_BUTTON_H = 22;
    private static final int PANEL_X = 130;
    private static final int PANEL_Y = 30;
    private static final int PANEL_W = 178;
    private static final int PANEL_H = 142;
    private final List<RuleButton> ruleButtons = new ArrayList<>();
    private int selectedPortIndex;
    private int scrollOffset;

    public MachineTransferConfigScreen(MachineTransferConfigMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 318;
        this.imageHeight = 194;
    }

    @Override
    protected void init() {
        super.init();
        this.ruleButtons.clear();
        clampSelectionAndScroll();
        addSideButton(MachineLocalSide.TOP, PANEL_X + 74, PANEL_Y + 28);
        addSideButton(MachineLocalSide.LEFT, PANEL_X + 38, PANEL_Y + 58);
        addSideButton(MachineLocalSide.FRONT, PANEL_X + 74, PANEL_Y + 58);
        addSideButton(MachineLocalSide.RIGHT, PANEL_X + 110, PANEL_Y + 58);
        addSideButton(MachineLocalSide.BOTTOM, PANEL_X + 74, PANEL_Y + 88);
        addSideButton(MachineLocalSide.BACK, PANEL_X + 110, PANEL_Y + 88);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        clampSelectionAndScroll();
        updateRuleButtons();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        renderHoveredTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xDC181818);
        guiGraphics.fill(this.leftPos + 4, this.topPos + 4, this.leftPos + this.imageWidth - 4, this.topPos + this.imageHeight - 4, 0xE022262A);
        guiGraphics.fill(this.leftPos + 4, this.topPos + 4, this.leftPos + this.imageWidth - 4, this.topPos + 22, 0xFF303942);
        guiGraphics.drawString(this.font, Component.translatable("screen.mydrugs.machine_transfer.title"), this.leftPos + 9, this.topPos + 10, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.mydrugs.machine_transfer.front", directionName(this.menu.frontDirection())), this.leftPos + 188, this.topPos + 10, 0xC8D2DD, false);
        renderPortList(guiGraphics, mouseX, mouseY);
        renderSidePanel(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // The compact transfer window draws all labels in renderBg to avoid the default inventory labels.
    }

    private void renderPortList(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + PORT_LIST_X;
        int y = this.topPos + PORT_LIST_Y;
        int h = VISIBLE_PORT_ROWS * (PORT_ROW_H + PORT_ROW_GAP) - PORT_ROW_GAP;
        graphics.fill(x, y, x + PORT_LIST_W, y + h, 0xFF101214);
        graphics.fill(x + 1, y + 1, x + PORT_LIST_W - 1, y + h - 1, 0xFF242A30);

        if (this.menu.ports().isEmpty()) {
            drawClipped(graphics, Component.translatable("screen.mydrugs.machine_transfer.no_ports").getString(), x + 6, y + 8, PORT_LIST_W - 12, 0xFFD6DEE8);
            return;
        }

        int end = Math.min(this.menu.ports().size(), this.scrollOffset + VISIBLE_PORT_ROWS);
        for (int portIndex = this.scrollOffset; portIndex < end; portIndex++) {
            renderPortRow(graphics, portIndex, x + 4, y + 4 + (portIndex - this.scrollOffset) * (PORT_ROW_H + PORT_ROW_GAP));
        }
        renderScrollbar(graphics, x + PORT_LIST_W - 5, y + 3, h - 6);
    }

    private void renderPortRow(GuiGraphics graphics, int portIndex, int x, int y) {
        MachineTransferPortSpec port = this.menu.ports().get(portIndex);
        boolean selected = portIndex == this.selectedPortIndex;
        int rowW = PORT_LIST_W - 11;
        int border = selected ? 0xFFE8F1E8 : 0xFF111317;
        int fill = selected ? 0xFF3B4954 : 0xFF252A30;
        graphics.fill(x, y, x + rowW, y + PORT_ROW_H, border);
        graphics.fill(x + 1, y + 1, x + rowW - 1, y + PORT_ROW_H - 1, fill);

        String marker = kindMarker(port.kind()) + " " + accessMarker(port.access());
        graphics.drawString(this.font, marker, x + 4, y + 3, 0xFFAFC0CC, false);
        drawClipped(graphics, Component.translatable(port.translationKey()).getString(), x + 4, y + 13, rowW - 8, 0xFFFFFFFF);
    }

    private void renderScrollbar(GuiGraphics graphics, int x, int y, int h) {
        int maxScroll = maxScroll();
        if (maxScroll <= 0) {
            return;
        }
        graphics.fill(x, y, x + 3, y + h, 0xFF101214);
        int thumbH = Math.max(16, h * VISIBLE_PORT_ROWS / this.menu.ports().size());
        int thumbY = y + (h - thumbH) * this.scrollOffset / maxScroll;
        graphics.fill(x, thumbY, x + 3, thumbY + thumbH, 0xFFB8C7D8);
    }

    private void renderSidePanel(GuiGraphics graphics) {
        int x = this.leftPos + PANEL_X;
        int y = this.topPos + PANEL_Y;
        graphics.fill(x, y, x + PANEL_W, y + PANEL_H, 0xFF101214);
        graphics.fill(x + 1, y + 1, x + PANEL_W - 1, y + PANEL_H - 1, 0xFF242A30);
        if (this.menu.ports().isEmpty()) {
            drawClipped(graphics, Component.translatable("screen.mydrugs.machine_transfer.no_ports").getString(), x + 8, y + 10, PANEL_W - 16, 0xFFE8F1E8);
            return;
        }

        MachineTransferPortSpec port = this.menu.ports().get(this.selectedPortIndex);
        drawClipped(graphics, Component.translatable(port.translationKey()).getString(), x + 8, y + 8, PANEL_W - 16, 0xFFE8F1E8);
        graphics.drawString(this.font, port.id().id().getPath(), x + 8, y + 20, 0xFF8FA2B5, false);
        drawSideLabels(graphics, x, y);
        graphics.drawString(this.font, kindMarker(port.kind()) + " " + accessMarker(port.access()), x + 8, y + PANEL_H - 15, 0xFFAFC0CC, false);
    }

    private void drawSideLabels(GuiGraphics graphics, int panelX, int panelY) {
        drawTinyLabel(graphics, "Top", panelX + 77, panelY + 18);
        drawTinyLabel(graphics, "Left", panelX + 34, panelY + 50);
        drawTinyLabel(graphics, "Front", panelX + 72, panelY + 50);
        drawTinyLabel(graphics, "Right", panelX + 109, panelY + 50);
        drawTinyLabel(graphics, "Bottom", panelX + 68, panelY + 80);
        drawTinyLabel(graphics, "Back", panelX + 110, panelY + 80);
    }

    private void drawTinyLabel(GuiGraphics graphics, String text, int x, int y) {
        graphics.drawString(this.font, text, x, y, 0xFFB8C7D8, false);
    }

    private void addSideButton(MachineLocalSide side, int localX, int localY) {
        RuleButton button = new RuleButton(
                side,
                this.leftPos + localX,
                this.topPos + localY,
                SIDE_BUTTON_W,
                SIDE_BUTTON_H,
                Component.empty(),
                ignored -> pressRuleButton(this.selectedPortIndex, side)
        );
        this.ruleButtons.add(button);
        this.addRenderableWidget(button);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClicked) {
        if (event.button() == 0 && handlePortClick((int) event.x(), (int) event.y())) {
            return true;
        }
        return super.mouseClicked(event, doubleClicked);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isOverPortList((int) mouseX, (int) mouseY) && maxScroll() > 0) {
            this.scrollOffset = clamp(this.scrollOffset - (int) Math.signum(scrollY), 0, maxScroll());
            clampSelectionAndScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean handlePortClick(int mouseX, int mouseY) {
        if (!isOverPortList(mouseX, mouseY) || this.menu.ports().isEmpty()) {
            return false;
        }

        int localY = mouseY - (this.topPos + PORT_LIST_Y + 4);
        if (localY < 0) {
            return true;
        }
        int row = localY / (PORT_ROW_H + PORT_ROW_GAP);
        if (row < 0 || row >= VISIBLE_PORT_ROWS) {
            return true;
        }
        int withinRow = localY % (PORT_ROW_H + PORT_ROW_GAP);
        if (withinRow >= PORT_ROW_H) {
            return true;
        }

        int portIndex = this.scrollOffset + row;
        if (portIndex >= 0 && portIndex < this.menu.ports().size()) {
            this.selectedPortIndex = portIndex;
            clampSelectionAndScroll();
        }
        return true;
    }

    private boolean isOverPortList(int mouseX, int mouseY) {
        int x = this.leftPos + PORT_LIST_X;
        int y = this.topPos + PORT_LIST_Y;
        int h = VISIBLE_PORT_ROWS * (PORT_ROW_H + PORT_ROW_GAP) - PORT_ROW_GAP;
        return mouseX >= x && mouseX < x + PORT_LIST_W && mouseY >= y && mouseY < y + h;
    }

    private void pressRuleButton(int portIndex, MachineLocalSide side) {
        if (this.menu.ports().isEmpty()) {
            return;
        }
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, MachineTransferConfigMenu.buttonId(portIndex, side));
        }
    }

    private void updateRuleButtons() {
        for (RuleButton button : this.ruleButtons) {
            MachineTransferSideRule rule = this.menu.ports().isEmpty()
                    ? MachineTransferSideRule.DISABLED
                    : this.menu.rule(this.selectedPortIndex, button.side);
            button.setRule(rule);
            button.setOccupiedByOther(occupiedPorts(button.side));
        }
    }

    private List<MachineTransferPortSpec> occupiedPorts(MachineLocalSide side) {
        List<MachineTransferPortSpec> occupied = new ArrayList<>();
        for (int i = 0; i < this.menu.ports().size(); i++) {
            if (i != this.selectedPortIndex && this.menu.rule(i, side) != MachineTransferSideRule.DISABLED) {
                occupied.add(this.menu.ports().get(i));
            }
        }
        return occupied;
    }

    private void renderHoveredTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        RuleButton sideButton = hoveredRuleButton(mouseX, mouseY);
        if (sideButton != null && !this.menu.ports().isEmpty()) {
            renderRuleTooltip(graphics, mouseX, mouseY, sideButton);
            return;
        }

        int hoveredPort = hoveredPort(mouseX, mouseY);
        if (hoveredPort >= 0) {
            renderPortTooltip(graphics, mouseX, mouseY, this.menu.ports().get(hoveredPort));
        }
    }

    private int hoveredPort(int mouseX, int mouseY) {
        if (!isOverPortList(mouseX, mouseY) || this.menu.ports().isEmpty()) {
            return -1;
        }
        int localY = mouseY - (this.topPos + PORT_LIST_Y + 4);
        int row = localY / (PORT_ROW_H + PORT_ROW_GAP);
        int withinRow = localY % (PORT_ROW_H + PORT_ROW_GAP);
        int portIndex = this.scrollOffset + row;
        return row >= 0 && row < VISIBLE_PORT_ROWS && withinRow < PORT_ROW_H && portIndex < this.menu.ports().size() ? portIndex : -1;
    }

    private @Nullable RuleButton hoveredRuleButton(int mouseX, int mouseY) {
        for (RuleButton button : this.ruleButtons) {
            if (button.isHoveredOrFocused() && button.isMouseOver(mouseX, mouseY)) {
                return button;
            }
        }
        return null;
    }

    private void renderPortTooltip(GuiGraphics graphics, int mouseX, int mouseY, MachineTransferPortSpec port) {
        List<ClientTooltipComponent> components = List.of(
                ClientTooltipComponent.create(Component.translatable(port.translationKey()).getVisualOrderText()),
                ClientTooltipComponent.create(Component.literal(port.id().id().toString()).getVisualOrderText()),
                ClientTooltipComponent.create(Component.literal(port.kind().name() + " / " + port.access().name()).getVisualOrderText())
        );
        graphics.renderTooltip(this.font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
    }

    private void renderRuleTooltip(GuiGraphics graphics, int mouseX, int mouseY, RuleButton button) {
        MachineTransferPortSpec port = this.menu.ports().get(this.selectedPortIndex);
        List<ClientTooltipComponent> components = new ArrayList<>();
        components.add(ClientTooltipComponent.create(Component.translatable(port.translationKey()).getVisualOrderText()));
        components.add(ClientTooltipComponent.create(Component.translatable("screen.mydrugs.machine_transfer.local_and_world",
                MachineOrientation.displayName(button.side),
                directionName(this.menu.worldDirection(button.side))).getVisualOrderText()));
        components.add(ClientTooltipComponent.create(ruleName(button.rule).getVisualOrderText()));
        components.add(ClientTooltipComponent.create(Component.translatable("screen.mydrugs.machine_transfer.click_to_cycle").getVisualOrderText()));
        for (MachineTransferPortSpec occupied : button.occupiedByOther) {
            components.add(ClientTooltipComponent.create(Component.translatable(
                    "screen.mydrugs.machine_transfer.occupied_by",
                    Component.translatable(occupied.translationKey())
            ).getVisualOrderText()));
        }
        graphics.renderTooltip(this.font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
    }

    private void drawClipped(GuiGraphics graphics, String text, int x, int y, int width, int color) {
        graphics.drawString(this.font, trimToWidthWithEllipsis(text, width), x, y, color, false);
    }

    private String trimToWidthWithEllipsis(String text, int width) {
        if (this.font.width(text) <= width) {
            return text;
        }
        String ellipsis = "...";
        return this.font.plainSubstrByWidth(text, Math.max(0, width - this.font.width(ellipsis))) + ellipsis;
    }

    private void clampSelectionAndScroll() {
        if (this.menu.ports().isEmpty()) {
            this.selectedPortIndex = 0;
            this.scrollOffset = 0;
            return;
        }
        this.selectedPortIndex = clamp(this.selectedPortIndex, 0, this.menu.ports().size() - 1);
        this.scrollOffset = clamp(this.scrollOffset, 0, maxScroll());
        if (this.selectedPortIndex < this.scrollOffset) {
            this.scrollOffset = this.selectedPortIndex;
        } else if (this.selectedPortIndex >= this.scrollOffset + VISIBLE_PORT_ROWS) {
            this.scrollOffset = clamp(this.selectedPortIndex - VISIBLE_PORT_ROWS + 1, 0, maxScroll());
        }
    }

    private int maxScroll() {
        return Math.max(0, this.menu.ports().size() - VISIBLE_PORT_ROWS);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Component directionName(Direction direction) {
        return Component.translatable("screen.mydrugs.machine_transfer.direction." + direction.getSerializedName());
    }

    private static Component ruleName(MachineTransferSideRule rule) {
        return Component.translatable("screen.mydrugs.machine_transfer.rule." + rule.name().toLowerCase(Locale.ROOT));
    }

    private static int colorFor(MachineTransferSideRule rule) {
        return switch (rule) {
            case DISABLED -> 0xFF3A4047;
            case INPUT -> 0xFF1E7EEA;
            case OUTPUT -> 0xFFE07A1F;
        };
    }

    private static String kindMarker(MachineTransferResourceKind kind) {
        return switch (kind) {
            case ITEM -> "[I]";
            case FLUID -> "[F]";
            case GAS -> "[G]";
        };
    }

    private static String accessMarker(MachineTransferAccess access) {
        return switch (access) {
            case INPUT_ONLY -> "In";
            case OUTPUT_ONLY -> "Out";
            case BIDIRECTIONAL -> "Both";
        };
    }

    private class RuleButton extends Button {
        private final MachineLocalSide side;
        private MachineTransferSideRule rule = MachineTransferSideRule.DISABLED;
        private List<MachineTransferPortSpec> occupiedByOther = List.of();

        RuleButton(MachineLocalSide side, int x, int y, int width, int height, Component label, OnPress onPress) {
            super(x, y, width, height, label, onPress, DEFAULT_NARRATION);
            this.side = side;
        }

        void setRule(MachineTransferSideRule rule) {
            this.rule = rule;
        }

        void setOccupiedByOther(List<MachineTransferPortSpec> occupiedByOther) {
            this.occupiedByOther = List.copyOf(occupiedByOther);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int border = this.occupiedByOther.isEmpty() ? 0xFF111317 : 0xFFFFE35A;
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, border);
            graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.width - 1, this.getY() + this.height - 1, colorFor(this.rule));
            if (!this.occupiedByOther.isEmpty()) {
                graphics.fill(this.getX() + this.width - 6, this.getY() + 3, this.getX() + this.width - 3, this.getY() + 6, 0xFFFFFF7A);
            }
        }
    }
}
