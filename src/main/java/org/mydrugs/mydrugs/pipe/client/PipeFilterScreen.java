package org.mydrugs.mydrugs.pipe.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterConfig;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterMenu;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterMode;

import java.util.ArrayList;
import java.util.List;

public class PipeFilterScreen extends AbstractContainerScreen<PipeFilterMenu> {
    private static final int TAB_Y = 18;
    private static final int TAB_W = 38;
    private static final int TAB_H = 16;
    private static final int MODE_X = 8;
    private static final int MODE_Y = 42;
    private static final int MODE_W = 50;
    private static final int MODE_H = 18;
    private static final int CLEAR_X = 8;
    private static final int CLEAR_Y = 64;
    private static final int CLEAR_W = 46;
    private static final int CLEAR_H = 18;

    public PipeFilterScreen(PipeFilterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 184;
        this.inventoryLabelY = PipeFilterMenu.PLAYER_INV_Y - 10;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderFilterEntries(guiGraphics);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        renderHoveredHelp(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, 0xE0181818);
        guiGraphics.fill(this.leftPos + 4, this.topPos + 4, this.leftPos + this.imageWidth - 4, this.topPos + this.imageHeight - 4, 0xE0242930);
        guiGraphics.fill(this.leftPos + 4, this.topPos + 4, this.leftPos + this.imageWidth - 4, this.topPos + 16, 0xFF303942);
        guiGraphics.drawString(this.font, Component.translatable("screen.mydrugs.pipe_filter.title"), this.leftPos + 8, this.topPos + 7, 0xFFFFFFFF, false);

        renderKindTabs(guiGraphics);
        renderModeButton(guiGraphics);
        renderClearButton(guiGraphics);
        renderGhostSlotBackgrounds(guiGraphics);
        renderStatus(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFCAD2D8, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClicked) {
        if (event.button() == 0) {
            int mouseX = (int) event.x();
            int mouseY = (int) event.y();
            int button = buttonAt(mouseX, mouseY);
            if (button >= 0) {
                sendButton(button);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClicked);
    }

    private void renderKindTabs(GuiGraphics graphics) {
        drawTab(graphics, PipeResourceKind.ITEM, 8);
        drawTab(graphics, PipeResourceKind.FLUID, 50);
        drawTab(graphics, PipeResourceKind.GAS, 92);
    }

    private void drawTab(GuiGraphics graphics, PipeResourceKind kind, int localX) {
        PipeFilterConfig config = this.menu.config();
        boolean active = config.kind() == kind;
        int x = this.leftPos + localX;
        int y = this.topPos + TAB_Y;
        int fill = active ? 0xFF3D6D8F : 0xFF252A30;
        int border = active ? 0xFFD8EEF8 : 0xFF111317;
        drawRectButton(graphics, x, y, TAB_W, TAB_H, border, fill);
        graphics.drawString(this.font, kindLabel(kind), x + 5, y + 5, active ? 0xFFFFFFFF : 0xFFC2CBD4, false);
    }

    private void renderModeButton(GuiGraphics graphics) {
        PipeFilterMode mode = this.menu.config().mode();
        int fill = mode == PipeFilterMode.ALLOW_LIST ? 0xFF2D714C : 0xFF71452D;
        int x = this.leftPos + MODE_X;
        int y = this.topPos + MODE_Y;
        drawRectButton(graphics, x, y, MODE_W, MODE_H, 0xFF111317, fill);
        drawClipped(
                graphics,
                Component.translatable("screen.mydrugs.pipe_filter.mode." + mode.serializedName()).getString(),
                x + 4,
                y + 6,
                MODE_W - 8,
                0xFFFFFFFF
        );
    }

    private void renderClearButton(GuiGraphics graphics) {
        int x = this.leftPos + CLEAR_X;
        int y = this.topPos + CLEAR_Y;
        drawRectButton(graphics, x, y, CLEAR_W, CLEAR_H, 0xFF111317, 0xFF343A42);
        graphics.drawString(this.font, Component.translatable("screen.mydrugs.pipe_filter.clear"), x + 10, y + 6, 0xFFE6EEF5, false);
    }

    private void renderGhostSlotBackgrounds(GuiGraphics graphics) {
        for (int i = 0; i < PipeFilterMenu.GHOST_SLOT_COUNT; i++) {
            int x = this.leftPos + PipeFilterMenu.ghostSlotX(i) - 1;
            int y = this.topPos + PipeFilterMenu.ghostSlotY(i) - 1;
            boolean filled = this.menu.entry(i).isPresent();
            graphics.fill(x, y, x + 18, y + 18, filled ? 0xFFD8EEF8 : 0xFF111317);
            graphics.fill(x + 1, y + 1, x + 17, y + 17, filled ? 0xFF303942 : 0xFF1C2026);
        }
    }

    private void renderStatus(GuiGraphics graphics) {
        PipeFilterConfig config = this.menu.config();
        graphics.drawString(
                this.font,
                Component.translatable("screen.mydrugs.pipe_filter.entries", config.entries().size(), PipeFilterConfig.MAX_ENTRIES),
                this.leftPos + 8,
                this.topPos + 86,
                0xFFBFCAD4,
                false
        );
    }

    private void renderFilterEntries(GuiGraphics graphics) {
        for (int i = 0; i < PipeFilterMenu.GHOST_SLOT_COUNT; i++) {
            int x = this.leftPos + PipeFilterMenu.ghostSlotX(i);
            int y = this.topPos + PipeFilterMenu.ghostSlotY(i);
            ItemStack display = this.menu.displayStack(i);
            if (!display.isEmpty()) {
                graphics.renderItem(display, x, y);
                continue;
            }

            if (this.menu.entry(i).isPresent()) {
                graphics.drawString(this.font, "?", x + 6, y + 5, 0xFFFFD66D, false);
            }
        }
    }

    private void renderHoveredHelp(GuiGraphics graphics, int mouseX, int mouseY) {
        int button = buttonAt(mouseX, mouseY);
        if (button >= 0) {
            renderButtonTooltip(graphics, mouseX, mouseY, button);
            return;
        }

        int slot = ghostSlotAt(mouseX, mouseY);
        if (slot >= 0) {
            renderSlotTooltip(graphics, mouseX, mouseY, slot);
        }
    }

    private void renderButtonTooltip(GuiGraphics graphics, int mouseX, int mouseY, int button) {
        List<Component> lines = new ArrayList<>();
        switch (button) {
            case PipeFilterMenu.BUTTON_TOGGLE_MODE -> {
                lines.add(this.menu.config().mode() == PipeFilterMode.ALLOW_LIST
                        ? Component.translatable("tooltip.mydrugs.pipe_filter.allow_list_help")
                        : Component.translatable("tooltip.mydrugs.pipe_filter.deny_list_help"));
            }
            case PipeFilterMenu.BUTTON_KIND_ITEM -> lines.add(Component.translatable("tooltip.mydrugs.pipe_filter.item_help"));
            case PipeFilterMenu.BUTTON_KIND_FLUID -> lines.add(Component.translatable("tooltip.mydrugs.pipe_filter.fluid_help"));
            case PipeFilterMenu.BUTTON_KIND_GAS -> lines.add(Component.translatable("tooltip.mydrugs.pipe_filter.gas_help"));
            case PipeFilterMenu.BUTTON_CLEAR_ALL -> lines.add(Component.translatable("screen.mydrugs.pipe_filter.clear"));
            default -> {
            }
        }
        renderTooltipLines(graphics, lines, mouseX, mouseY);
    }

    private void renderSlotTooltip(GuiGraphics graphics, int mouseX, int mouseY, int slot) {
        List<Component> lines = new ArrayList<>();
        ResourceLocation entry = this.menu.entry(slot).orElse(null);
        if (entry == null) {
            lines.add(Component.translatable("screen.mydrugs.pipe_filter.empty"));
            lines.add(kindHelp(this.menu.config().kind()));
        } else {
            lines.add(Component.literal(entry.toString()));
        }
        lines.add(Component.translatable("tooltip.mydrugs.pipe_filter.clear_slot"));
        lines.add(Component.translatable("tooltip.mydrugs.pipe_filter.shift_click_help"));
        renderTooltipLines(graphics, lines, mouseX, mouseY);
    }

    private void renderTooltipLines(GuiGraphics graphics, List<Component> lines, int mouseX, int mouseY) {
        if (lines.isEmpty()) {
            return;
        }
        List<ClientTooltipComponent> components = lines.stream()
                .map(line -> ClientTooltipComponent.create(line.getVisualOrderText()))
                .toList();
        graphics.renderTooltip(this.font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
    }

    private int buttonAt(int mouseX, int mouseY) {
        if (inside(mouseX, mouseY, MODE_X, MODE_Y, MODE_W, MODE_H)) {
            return PipeFilterMenu.BUTTON_TOGGLE_MODE;
        }
        if (inside(mouseX, mouseY, CLEAR_X, CLEAR_Y, CLEAR_W, CLEAR_H)) {
            return PipeFilterMenu.BUTTON_CLEAR_ALL;
        }
        if (inside(mouseX, mouseY, 8, TAB_Y, TAB_W, TAB_H)) {
            return PipeFilterMenu.BUTTON_KIND_ITEM;
        }
        if (inside(mouseX, mouseY, 50, TAB_Y, TAB_W, TAB_H)) {
            return PipeFilterMenu.BUTTON_KIND_FLUID;
        }
        if (inside(mouseX, mouseY, 92, TAB_Y, TAB_W, TAB_H)) {
            return PipeFilterMenu.BUTTON_KIND_GAS;
        }
        return -1;
    }

    private int ghostSlotAt(int mouseX, int mouseY) {
        for (int i = 0; i < PipeFilterMenu.GHOST_SLOT_COUNT; i++) {
            if (inside(mouseX, mouseY, PipeFilterMenu.ghostSlotX(i), PipeFilterMenu.ghostSlotY(i), 16, 16)) {
                return i;
            }
        }
        return -1;
    }

    private boolean inside(int mouseX, int mouseY, int localX, int localY, int width, int height) {
        int x = this.leftPos + localX;
        int y = this.topPos + localY;
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void sendButton(int button) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, button);
        }
    }

    private void drawRectButton(GuiGraphics graphics, int x, int y, int w, int h, int border, int fill) {
        graphics.fill(x, y, x + w, y + h, border);
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
    }

    private void drawClipped(GuiGraphics graphics, String text, int x, int y, int width, int color) {
        graphics.drawString(this.font, trimToWidth(text, width), x, y, color, false);
    }

    private String trimToWidth(String text, int width) {
        if (this.font.width(text) <= width) {
            return text;
        }
        return this.font.plainSubstrByWidth(text, width);
    }

    private static Component kindLabel(PipeResourceKind kind) {
        return Component.translatable("screen.mydrugs.pipe_filter.kind." + kind.serializedName());
    }

    private static Component kindHelp(PipeResourceKind kind) {
        return switch (kind) {
            case ITEM -> Component.translatable("tooltip.mydrugs.pipe_filter.item_help");
            case FLUID -> Component.translatable("tooltip.mydrugs.pipe_filter.fluid_help");
            case GAS -> Component.translatable("tooltip.mydrugs.pipe_filter.gas_help");
        };
    }
}
