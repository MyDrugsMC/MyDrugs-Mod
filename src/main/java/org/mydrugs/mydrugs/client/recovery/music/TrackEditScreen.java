package org.mydrugs.mydrugs.client.recovery.music;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class TrackEditScreen extends Screen {
    private static final int PANEL_W = 280;
    private static final int PANEL_H = 130;

    private final Screen parent;
    private final MusicTrack track;
    private EditBox titleBox;
    private EditBox artistBox;

    public TrackEditScreen(Screen parent, MusicTrack track) {
        super(Component.translatable("screen.mydrugs.music.edit"));
        this.parent = parent;
        this.track = track;
    }

    @Override
    protected void init() {
        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;

        titleBox = new EditBox(font, left + 12, top + 36, PANEL_W - 24, 18,
                Component.translatable("screen.mydrugs.music.rename_title"));
        titleBox.setMaxLength(120);
        titleBox.setValue(track.title == null ? "" : track.title);
        addRenderableWidget(titleBox);

        artistBox = new EditBox(font, left + 12, top + 66, PANEL_W - 24, 18,
                Component.translatable("screen.mydrugs.music.rename_artist"));
        artistBox.setMaxLength(120);
        artistBox.setValue(track.artist == null ? "" : track.artist);
        addRenderableWidget(artistBox);

        addRenderableWidget(Button.builder(Component.translatable("screen.mydrugs.music.save"), button -> {
            MusicLibrary.get().renameTrack(track.id, titleBox.getValue(), artistBox.getValue());
            minecraft.setScreen(parent);
        }).bounds(left + 12, top + PANEL_H - 28, 60, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("screen.mydrugs.music.cancel"),
                        button -> minecraft.setScreen(parent))
                .bounds(left + 76, top + PANEL_H - 28, 60, 20).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;
        graphics.fill(0, 0, width, height, 0xCC000000);
        graphics.fill(left, top, left + PANEL_W, top + PANEL_H, 0xFF1A2220);
        graphics.fill(left, top, left + PANEL_W, top + 26, 0xFF202F2A);
        graphics.fill(left, top + 26, left + PANEL_W, top + 27, 0xFF3A5A4F);
        graphics.drawString(font, title, left + 12, top + 9, 0xFFE5F1EA, false);

        graphics.drawString(font, Component.translatable("screen.mydrugs.music.rename_title"),
                left + 12, top + 30, 0xFF8FA89B, false);
        graphics.drawString(font, Component.translatable("screen.mydrugs.music.rename_artist"),
                left + 12, top + 60, 0xFF8FA89B, false);

        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
