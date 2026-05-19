package org.mydrugs.mydrugs.client.recovery.music;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.nio.file.Path;

public final class MusicImportScreen extends Screen {
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 220;

    private final Screen parent;
    private EditBox input;
    private TrackImportJob activeJob;

    public MusicImportScreen(Screen parent) {
        super(Component.translatable("screen.mydrugs.music.add"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = (width - PANEL_W) / 2;
        int top = (height - PANEL_H) / 2;

        input = new EditBox(font, left + 12, top + 56, PANEL_W - 124, 18,
                Component.translatable("screen.mydrugs.music.path_or_url"));
        input.setMaxLength(400);
        input.setHint(Component.translatable("screen.mydrugs.music.path_or_url"));
        addRenderableWidget(input);

        // Native picker buttons next to the input
        Button browseFile = Button.builder(Component.translatable("screen.mydrugs.music.browse_file"), button -> {
            if (NativeFileDialog.isBusy()) return;
            button.active = false;
            NativeFileDialog.openFile(
                    Component.translatable("screen.mydrugs.music.browse_file").getString(),
                    MusicLibraryStorage.root(),
                    false
            ).whenComplete((path, error) -> minecraft.execute(() -> {
                button.active = true;
                if (path != null && input != null) {
                    input.setValue(path.toString());
                }
            }));
        }).bounds(left + PANEL_W - 108, top + 56, 44, 18).build();
        addRenderableWidget(browseFile);

        Button browseFolder = Button.builder(Component.translatable("screen.mydrugs.music.browse_folder"), button -> {
            if (NativeFileDialog.isBusy()) return;
            button.active = false;
            NativeFileDialog.selectFolder(
                    Component.translatable("screen.mydrugs.music.browse_folder").getString(),
                    MusicLibraryStorage.root()
            ).whenComplete((path, error) -> minecraft.execute(() -> {
                button.active = true;
                if (path != null && input != null) {
                    input.setValue(path.toString());
                }
            }));
        }).bounds(left + PANEL_W - 60, top + 56, 48, 18).build();
        addRenderableWidget(browseFolder);

        int y = top + 84;
        addRenderableWidget(Button.builder(Component.translatable("screen.mydrugs.music.import_file"),
                        button -> activeJob = TrackImportManager.importFile(Path.of(input.getValue().trim())))
                .bounds(left + 12, y, 88, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.mydrugs.music.import_folder"),
                        button -> activeJob = TrackImportManager.importFolder(Path.of(input.getValue().trim())))
                .bounds(left + 104, y, 92, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.mydrugs.music.add_direct_url"),
                        button -> activeJob = TrackImportManager.importYoutubeAudio(input.getValue().trim()))
                .bounds(left + 200, y, 96, 20).build());

        int y2 = y + 26;
        addRenderableWidget(Button.builder(Component.translatable("screen.mydrugs.music.add_bookmark"),
                        button -> activeJob = TrackImportManager.addBookmark(input.getValue().trim()))
                .bounds(left + 12, y2, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.mydrugs.music.open_folder"),
                        button -> net.minecraft.Util.getPlatform().openPath(MusicLibraryStorage.root()))
                .bounds(left + 116, y2, 80, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("screen.mydrugs.music.done"),
                        button -> minecraft.setScreen(parent))
                .bounds(left + PANEL_W - 64, top + PANEL_H - 28, 52, 20).build());
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

        graphics.drawString(font, Component.translatable("screen.mydrugs.music.supported_formats"),
                left + 12, top + 34, 0xFF8FA89B, false);
        graphics.drawString(font, Component.translatable("screen.mydrugs.music.path_or_url"),
                left + 12, top + 46, 0xFF8FA89B, false);

        super.render(graphics, mouseX, mouseY, partialTick);

        int statusY = top + PANEL_H - 50;
        boolean ff = AudioConverter.isFfmpegAvailable();
        Component ffStatus = ff
                ? Component.translatable("screen.mydrugs.music.ffmpeg_ok").copy().withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))
                : Component.translatable("screen.mydrugs.music.ffmpeg_missing").copy().withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW));
        graphics.drawWordWrap(font, ffStatus, left + 12, statusY, PANEL_W - 24, 0xFFFFFFFF);

        Component status = activeJob == null ? MusicLibrary.get().lastStatus() : activeJob.message();
        if (status != null && !status.getString().isBlank()) {
            graphics.drawWordWrap(font, status, left + 12, top + PANEL_H - 78, PANEL_W - 24, 0xFFB7E4C7);
        }

        if (NativeFileDialog.isBusy()) {
            graphics.drawString(font, Component.translatable("screen.mydrugs.music.picker_open"),
                    left + 12, top + PANEL_H - 60, 0xFFFFD166, false);
        }
    }
}
