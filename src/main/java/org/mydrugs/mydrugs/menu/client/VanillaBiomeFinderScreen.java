package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.items.VanillaBiomeFinderItem;
import org.mydrugs.mydrugs.menu.layout.LayoutMath;
import org.mydrugs.mydrugs.network.BiomeFinderSelectPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class VanillaBiomeFinderScreen extends Screen {
    private static final int SEARCH_W = 280;
    private static final int SEARCH_H = 20;
    private static final int LIST_W = 380;
    private static final int ROW_H = 28;
    private static final int PANEL_BG = 0xAA0B0D12;
    private static final int PANEL_EDGE = 0xAA4F5260;
    private static final int ROW_HOVER = 0x66374655;
    private static final int ROW_SELECTED = 0x885B4B86;
    private static final int TEXT = 0xFFE9EAF2;
    private static final int MUTED = 0xFF9CA3AF;
    private static final int ACCENT = 0xFFD8C46A;

    private final InteractionHand hand;
    private final Optional<ResourceLocation> selectedBiome;
    private final List<ResourceLocation> allBiomes;
    private final List<ResourceLocation> filteredBiomes = new ArrayList<>();
    private EditBox searchBox;
    private int scrollOffset = 0;
    private int listX;
    private int listY;
    private int listW;
    private int listH;

    public VanillaBiomeFinderScreen(
            InteractionHand hand,
            Optional<ResourceLocation> selectedBiome,
            List<ResourceLocation> availableBiomes
    ) {
        super(Component.translatable("screen.mydrugs.biome_finder.title"));
        this.hand = hand;
        this.selectedBiome = selectedBiome;
        this.allBiomes = List.copyOf(availableBiomes);
        this.filteredBiomes.addAll(availableBiomes);
    }

    @Override
    protected void init() {
        int searchW = Math.min(SEARCH_W, Math.max(160, this.width - 60));
        int searchX = LayoutMath.centered(this.width, searchW);
        int searchY = 34;
        this.searchBox = new EditBox(this.font, searchX, searchY, searchW, SEARCH_H, Component.translatable("screen.mydrugs.biome_finder.search"));
        this.searchBox.setHint(Component.translatable("screen.mydrugs.biome_finder.search"));
        this.searchBox.setMaxLength(64);
        this.searchBox.setResponder(value -> {
            updateFilter();
            scrollOffset = 0;
        });
        this.searchBox.setCanLoseFocus(true);
        this.searchBox.setFocused(true);
        this.addRenderableWidget(searchBox);
        this.setInitialFocus(searchBox);

        this.listW = Math.min(LIST_W, Math.max(180, this.width - 48));
        this.listX = LayoutMath.centered(this.width, listW);
        this.listY = LayoutMath.rowBelow(searchY, SEARCH_H, 14);
        this.listH = Math.max(ROW_H * 4, Math.min(ROW_H * 10, this.height - listY - 42));
    }

    private void updateFilter() {
        String query = normalize(searchBox == null ? "" : searchBox.getValue());
        filteredBiomes.clear();
        for (ResourceLocation biome : allBiomes) {
            String pretty = normalize(VanillaBiomeFinderItem.prettyName(biome));
            String id = normalize(biome.toString());
            if (query.isBlank() || pretty.contains(query) || id.contains(query)) {
                filteredBiomes.add(biome);
            }
        }
        clampScroll();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
        drawShell(graphics);
        drawBiomeRows(graphics, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawShell(GuiGraphics graphics) {
        int topPanelW = Math.min(420, this.width - 24);
        int topPanelX = LayoutMath.centered(this.width, topPanelW);
        graphics.fill(topPanelX, 20, topPanelX + topPanelW, 70, PANEL_BG);
        drawBorder(graphics, topPanelX, 20, topPanelW, 50);
        drawCentered(graphics, title, 0, 12, width, TEXT);

        graphics.fill(listX, listY, listX + listW, listY + listH, PANEL_BG);
        drawBorder(graphics, listX, listY, listW, listH);

        int footerY = listY + listH + 8;
        Component count = Component.translatable("screen.mydrugs.biome_finder.count", filteredBiomes.size(), allBiomes.size());
        drawCentered(graphics, count, listX, footerY, listW, MUTED);
    }

    private void drawBiomeRows(GuiGraphics graphics, int mouseX, int mouseY) {
        int visibleRows = visibleRows();
        clampScroll();

        graphics.enableScissor(listX + 2, listY + 2, listX + listW - 2, listY + listH - 2);
        for (int row = 0; row < visibleRows; row++) {
            int index = scrollOffset + row;
            if (index >= filteredBiomes.size()) {
                break;
            }

            ResourceLocation biome = filteredBiomes.get(index);
            int y = listY + 3 + row * ROW_H;
            boolean selected = selectedBiome.map(biome::equals).orElse(false);
            boolean hovered = mouseX >= listX + 4 && mouseX < listX + listW - 4 && mouseY >= y && mouseY < y + ROW_H;
            if (selected || hovered) {
                graphics.fill(listX + 4, y, listX + listW - 4, y + ROW_H - 2, selected ? ROW_SELECTED : ROW_HOVER);
            }

            String name = VanillaBiomeFinderItem.prettyName(biome);
            graphics.drawString(font, name, listX + 12, y + 4, selected ? ACCENT : TEXT, false);
            graphics.drawString(font, biome.toString(), listX + 12, y + 14, MUTED, false);
        }
        graphics.disableScissor();

        if (filteredBiomes.isEmpty()) {
            drawCentered(graphics, Component.translatable("screen.mydrugs.biome_finder.empty"), listX, listY + listH / 2 - 4, listW, MUTED);
        }

        drawScrollbar(graphics);
    }

    private void drawScrollbar(GuiGraphics graphics) {
        int visibleRows = visibleRows();
        if (filteredBiomes.size() <= visibleRows) {
            return;
        }

        int trackX = listX + listW - 8;
        int trackY = listY + 4;
        int trackH = listH - 8;
        graphics.fill(trackX, trackY, trackX + 3, trackY + trackH, 0x66242A35);
        int maxScroll = Math.max(1, filteredBiomes.size() - visibleRows);
        int thumbH = Math.max(14, trackH * visibleRows / filteredBiomes.size());
        int thumbY = trackY + (trackH - thumbH) * scrollOffset / maxScroll;
        graphics.fill(trackX - 1, thumbY, trackX + 4, thumbY + thumbH, 0xCC7A8090);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        if (event.button() != 0) {
            return false;
        }

        int row = rowAt(event.x(), event.y());
        if (row >= 0) {
            ResourceLocation selected = filteredBiomes.get(scrollOffset + row);
            ClientPacketDistributor.sendToServer(new BiomeFinderSelectPayload(hand, selected));
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX < listX || mouseX > listX + listW || mouseY < listY || mouseY > listY + listH) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        scrollOffset -= (int) Math.signum(scrollY);
        clampScroll();
        return true;
    }

    private int rowAt(double mouseX, double mouseY) {
        if (mouseX < listX + 4 || mouseX >= listX + listW - 4 || mouseY < listY + 3 || mouseY >= listY + listH - 3) {
            return -1;
        }
        int row = ((int) mouseY - listY - 3) / ROW_H;
        int index = scrollOffset + row;
        return row >= 0 && row < visibleRows() && index >= 0 && index < filteredBiomes.size() ? row : -1;
    }

    private int visibleRows() {
        return Math.max(1, (listH - 6) / ROW_H);
    }

    private void clampScroll() {
        int max = Math.max(0, filteredBiomes.size() - visibleRows());
        scrollOffset = Math.max(0, Math.min(scrollOffset, max));
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replace('_', ' ').trim();
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + 1, PANEL_EDGE);
        graphics.fill(x, y + h - 1, x + w, y + h, PANEL_EDGE);
        graphics.fill(x, y, x + 1, y + h, PANEL_EDGE);
        graphics.fill(x + w - 1, y, x + w, y + h, PANEL_EDGE);
    }

    private void drawCentered(GuiGraphics graphics, Component text, int x, int y, int width, int color) {
        graphics.drawString(font, text, x + Math.max(0, (width - font.width(text)) / 2), y, color, false);
    }
}
