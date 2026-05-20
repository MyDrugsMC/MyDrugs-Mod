package org.mydrugs.mydrugs.client.recovery.music;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.mydrugs.mydrugs.network.HeadphonesControlPayload;

import java.util.List;
import java.util.Locale;

public final class HeadphonesMusicScreen extends Screen {
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 246;
    private static final int ROW_H = 18;

    // Palette
    private static final int C_BG = 0xCC000000;
    private static final int C_PANEL = 0xFF1A2220;
    private static final int C_PANEL_2 = 0xFF243430;
    private static final int C_HEADER = 0xFF202F2A;
    private static final int C_LINE = 0xFF3A5A4F;
    private static final int C_LINE_SOFT = 0xFF2B403A;
    private static final int C_TEXT = 0xFFE5F1EA;
    private static final int C_MUTED = 0xFF8FA89B;
    private static final int C_GOOD = 0xFFB7E4C7;
    private static final int C_WARN = 0xFFFFD166;
    private static final int C_BAD = 0xFFE56B6F;
    private static final int C_ROW_HOVER = 0xFF2A3F37;
    private static final int C_ROW_ALT = 0xFF1E2A26;
    private static final int C_PROGRESS_BG = 0xFF0D1412;

    private EditBox search;
    private Button playPauseButton;
    private Button repeatButton;
    private Button shuffleButton;
    private Button likeButton;
    private Button editButton;
    private Button removeButton;
    private Button openUrlButton;
    private int left;
    private int top;
    private int scroll;
    private MusicTrack hoveredTrack;
    private MusicTrack selectedTrack;
    private String selectedTrackId = "";
    private int listX;
    private int listY;
    private int listW;
    private int listH;

    public HeadphonesMusicScreen() {
        super(Component.translatable("screen.mydrugs.music.title"));
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        clearWidgets();
        MusicLibrary.get().refreshBuiltins();

        // ---- Now playing controls (top-right area) ----
        int controlsY = top + 76;
        int btnX = left + 12;
        addRenderableWidget(Button.builder(Component.literal("⏮"), button -> {
            CustomMusicPlayer.get().previous();
            sendControl(HeadphonesControlPayload.Action.PREVIOUS, "");
        }).bounds(btnX, controlsY, 24, 20).build());
        btnX += 26;

        playPauseButton = Button.builder(playPauseLabel(), button -> {
            CustomMusicPlayer.get().toggle();
            sendControl(HeadphonesControlPayload.Action.TOGGLE_PLAY, "");
            button.setMessage(playPauseLabel());
        }).bounds(btnX, controlsY, 28, 20).build();
        addRenderableWidget(playPauseButton);
        btnX += 30;

        addRenderableWidget(Button.builder(Component.literal("⏭"), button -> {
            CustomMusicPlayer.get().next();
            sendControl(HeadphonesControlPayload.Action.NEXT, "");
        }).bounds(btnX, controlsY, 24, 20).build());
        btnX += 28;

        likeButton = Button.builder(Component.literal("♡"), button -> {
            MusicTrack track = CustomMusicPlayer.get().currentTrack();
            if (track != null) {
                MusicLibrary.get().toggleLike(track.id);
                sendControl(HeadphonesControlPayload.Action.LIKE_TRACK, track.id);
                button.setMessage(likeLabel(track));
            }
        }).bounds(btnX, controlsY, 22, 20).build();
        addRenderableWidget(likeButton);
        btnX += 26;

        repeatButton = Button.builder(repeatLabel(), button -> {
            CustomMusicPlayer.get().setRepeat(!CustomMusicPlayer.get().repeat());
            button.setMessage(repeatLabel());
        }).bounds(btnX, controlsY, 22, 20).build();
        addRenderableWidget(repeatButton);
        btnX += 26;

        shuffleButton = Button.builder(shuffleLabel(), button -> {
            CustomMusicPlayer.get().setShuffle(!CustomMusicPlayer.get().shuffle());
            button.setMessage(shuffleLabel());
        }).bounds(btnX, controlsY, 22, 20).build();
        addRenderableWidget(shuffleButton);

        // Volume buttons (right side of controls row)
        addRenderableWidget(Button.builder(Component.literal("−"), button -> {
            CustomMusicPlayer.get().setVolume(CustomMusicPlayer.get().volume() - 0.05F);
            sendControl(HeadphonesControlPayload.Action.SET_VOLUME, "");
        }).bounds(left + PANEL_W - 60, controlsY, 18, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> {
            CustomMusicPlayer.get().setVolume(CustomMusicPlayer.get().volume() + 0.05F);
            sendControl(HeadphonesControlPayload.Action.SET_VOLUME, "");
        }).bounds(left + PANEL_W - 40, controlsY, 18, 20).build());

        // ---- Search ----
        search = new EditBox(font, left + 12, top + 104, PANEL_W - 24, 16, Component.translatable("screen.mydrugs.music.search"));
        search.setMaxLength(80);
        search.setHint(Component.translatable("screen.mydrugs.music.search"));
        addRenderableWidget(search);

        // ---- Footer ----
        int bottomY = top + PANEL_H - 26;
        addRenderableWidget(Button.builder(Component.translatable("screen.mydrugs.music.add"),
                        button -> minecraft.setScreen(new MusicImportScreen(this)))
                .bounds(left + 12, bottomY, 44, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.mydrugs.music.open_folder"),
                        button -> Util.getPlatform().openPath(MusicLibraryStorage.root()))
                .bounds(left + 60, bottomY, 70, 20).build());
        editButton = Button.builder(Component.translatable("screen.mydrugs.music.edit"),
                        button -> {
                            MusicTrack target = effectiveActionTarget();
                            if (target != null) {
                                minecraft.setScreen(new TrackEditScreen(this, target));
                            }
                        })
                .bounds(left + 134, bottomY, 36, 20).build();
        addRenderableWidget(editButton);

        removeButton = Button.builder(Component.translatable("screen.mydrugs.music.remove"),
                        button -> {
                            MusicTrack target = effectiveActionTarget();
                            if (target == null) return;
                            boolean wasPlaying = isCurrentlyPlaying(target);
                            MusicLibrary.get().remove(target.id);
                            if (wasPlaying) {
                                CustomMusicPlayer.get().stop();
                            }
                            selectedTrack = null;
                            selectedTrackId = "";
                            updateActionButtons();
                        })
                .bounds(left + 174, bottomY, 48, 20).build();
        addRenderableWidget(removeButton);

        openUrlButton = Button.builder(Component.translatable("screen.mydrugs.music.open_url"),
                        button -> {
                            MusicTrack target = effectiveActionTarget();
                            if (target == null || target.originalSource == null || target.originalSource.isBlank()) {
                                return;
                            }
                            try {
                                Util.getPlatform().openUri(new java.net.URI(target.originalSource));
                            } catch (Exception ignored) {
                            }
                        })
                .bounds(left + 226, bottomY, 60, 20).build();
        addRenderableWidget(openUrlButton);

        updateActionButtons();
    }

    private static boolean isCurrentlyPlaying(MusicTrack track) {
        MusicTrack current = CustomMusicPlayer.get().currentTrack();
        return current != null && track != null && current.id.equals(track.id);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (search != null && search.isFocused()) {
            return super.keyPressed(event);
        }
        switch (event.key()) {
            case GLFW.GLFW_KEY_SPACE -> {
                CustomMusicPlayer.get().toggle();
                sendControl(HeadphonesControlPayload.Action.TOGGLE_PLAY, "");
                if (playPauseButton != null) {
                    playPauseButton.setMessage(playPauseLabel());
                }
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                CustomMusicPlayer.get().next();
                sendControl(HeadphonesControlPayload.Action.NEXT, "");
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                CustomMusicPlayer.get().previous();
                sendControl(HeadphonesControlPayload.Action.PREVIOUS, "");
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                CustomMusicPlayer.get().setVolume(CustomMusicPlayer.get().volume() + 0.05F);
                sendControl(HeadphonesControlPayload.Action.SET_VOLUME, "");
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                CustomMusicPlayer.get().setVolume(CustomMusicPlayer.get().volume() - 0.05F);
                sendControl(HeadphonesControlPayload.Action.SET_VOLUME, "");
                return true;
            }
            default -> {
                return super.keyPressed(event);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            scroll = Math.max(0, scroll - (int) Math.round(scrollY));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClicked) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && hoveredTrack != null) {
            selectedTrack = hoveredTrack;
            selectedTrackId = hoveredTrack.id;
            updateActionButtons();
            if (doubleClicked && hoveredTrack.isPlayable() && hoveredTrack.isOgg()) {
                playTrack(hoveredTrack);
            } else if (hoveredTrack.sourceType == MusicTrack.SourceType.BOOKMARK) {
                // bookmarks aren't playable - selection is enough; don't try to play
            } else if (hoveredTrack.isPlayable() && hoveredTrack.isOgg()) {
                // single click on a regular track also plays for convenience
                playTrack(hoveredTrack);
            }
            return true;
        }
        return super.mouseClicked(event, doubleClicked);
    }

    private void playTrack(MusicTrack track) {
        CustomMusicPlayer.get().play(track);
        sendControl(HeadphonesControlPayload.Action.SELECT_TRACK, track.id);
        if (playPauseButton != null) {
            playPauseButton.setMessage(playPauseLabel());
        }
        if (likeButton != null) {
            likeButton.setMessage(likeLabel(track));
        }
    }

    private void updateActionButtons() {
        MusicTrack target = effectiveActionTarget();
        boolean has = target != null;
        boolean isBookmark = target != null && target.sourceType == MusicTrack.SourceType.BOOKMARK;
        boolean hasUrl = target != null
                && (isBookmark || target.sourceType == MusicTrack.SourceType.DIRECT_URL)
                && target.originalSource != null
                && !target.originalSource.isBlank();
        if (editButton != null) editButton.active = has;
        if (removeButton != null) removeButton.active = has;
        if (openUrlButton != null) openUrlButton.active = hasUrl;
    }

    private MusicTrack effectiveActionTarget() {
        if (selectedTrack != null) {
            // Refresh from library in case it was renamed or removed elsewhere.
            return MusicLibrary.get().find(selectedTrack.id).orElse(null);
        }
        if (!selectedTrackId.isBlank()) {
            return MusicLibrary.get().find(selectedTrackId).orElse(null);
        }
        return CustomMusicPlayer.get().currentTrack();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
        graphics.fill(0, 0, width, height, C_BG);

        // Panel + header
        graphics.fill(left, top, left + PANEL_W, top + PANEL_H, C_PANEL);
        graphics.fill(left, top, left + PANEL_W, top + 30, C_HEADER);
        graphics.fill(left, top + 30, left + PANEL_W, top + 31, C_LINE);
        graphics.drawString(font, title, left + 12, top + 11, C_TEXT, false);
        int trackCount = MusicLibrary.get().tracks().size();
        Component countText = Component.translatable("screen.mydrugs.music.tracks_count", trackCount);
        graphics.drawString(font, countText, left + PANEL_W - font.width(countText) - 12, top + 11, C_MUTED, false);

        drawNowPlayingCard(graphics);
        drawProgress(graphics);

        // Refresh play/pause label live
        if (playPauseButton != null) {
            playPauseButton.setMessage(playPauseLabel());
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        drawTrackRows(graphics, mouseX, mouseY);
        drawFooter(graphics);
    }

    private void drawNowPlayingCard(GuiGraphics graphics) {
        int cardY = top + 36;
        int cardH = 36;
        graphics.fill(left + 8, cardY, left + PANEL_W - 8, cardY + cardH, C_PANEL_2);
        graphics.fill(left + 8, cardY + cardH, left + PANEL_W - 8, cardY + cardH + 1, C_LINE_SOFT);

        MusicTrack current = CustomMusicPlayer.get().currentTrack();
        String titleText = current == null
                ? Component.translatable("screen.mydrugs.music.empty").getString()
                : current.title;
        String artist = current == null ? "" : current.displayArtist();

        // Album-art placeholder square
        int artSize = 28;
        int artX = left + 14;
        int artY = cardY + 4;
        graphics.fill(artX, artY, artX + artSize, artY + artSize, 0xFF101816);
        graphics.fill(artX, artY, artX + artSize, artY + 1, C_LINE);
        graphics.fill(artX, artY + artSize - 1, artX + artSize, artY + artSize, C_LINE);
        graphics.drawCenteredString(font, "♪", artX + artSize / 2, artY + (artSize - 8) / 2,
                CustomMusicPlayer.get().isPlaying() ? C_GOOD : C_MUTED);

        int textX = artX + artSize + 8;
        graphics.drawString(font, trim(titleText, 40), textX, cardY + 6, C_TEXT, false);
        if (!artist.isBlank()) {
            graphics.drawString(font, trim(artist, 40), textX, cardY + 18, C_MUTED, false);
        }
    }

    private void drawProgress(GuiGraphics graphics) {
        int progressY = top + 100;
        int barX = left + 12;
        int barW = PANEL_W - 24;
        int barH = 2;
        // Background
        graphics.fill(barX, progressY, barX + barW, progressY + barH, C_PROGRESS_BG);

        MusicTrack current = CustomMusicPlayer.get().currentTrack();
        long progress = CustomMusicPlayer.get().progressMs();
        long duration = current == null ? 0L : current.durationMs;
        int fillW;
        if (duration > 0L) {
            fillW = (int) Math.min(barW, (long) (progress / (float) duration * barW));
        } else {
            // Indeterminate scroller while playing without known duration
            fillW = CustomMusicPlayer.get().isPlaying()
                    ? (int) ((System.currentTimeMillis() / 50L) % barW)
                    : 0;
        }
        graphics.fill(barX, progressY, barX + fillW, progressY + barH, C_GOOD);

        // Time labels under the bar (only if we have a duration)
        if (duration > 0L) {
            String left = formatTime(progress);
            String right = formatTime(duration);
            graphics.drawString(font, left, barX, progressY + 4, C_MUTED, false);
            graphics.drawString(font, right, barX + barW - font.width(right), progressY + 4, C_MUTED, false);
        }
    }

    private void drawTrackRows(GuiGraphics graphics, int mouseX, int mouseY) {
        hoveredTrack = null;
        List<MusicTrack> visible = MusicLibrary.get().sortedTracks(search == null ? "" : search.getValue());
        listX = left + 12;
        listY = top + 124;
        listW = PANEL_W - 24;
        listH = PANEL_H - 158;
        graphics.fill(listX, listY, listX + listW, listY + listH, 0xFF111A17);

        if (visible.isEmpty()) {
            graphics.drawWordWrap(font,
                    Component.translatable("screen.mydrugs.music.empty_help"),
                    listX + 8, listY + 10, listW - 16, C_MUTED);
            graphics.drawWordWrap(font,
                    Component.translatable("screen.mydrugs.music.supported_formats"),
                    listX + 8, listY + 30, listW - 16, C_MUTED);
            return;
        }

        int maxRows = listH / ROW_H;
        int maxScroll = Math.max(0, visible.size() - maxRows);
        scroll = Math.min(scroll, maxScroll);

        MusicTrack playing = CustomMusicPlayer.get().currentTrack();
        for (int row = 0; row < maxRows && row + scroll < visible.size(); row++) {
            MusicTrack track = visible.get(row + scroll);
            int y = listY + row * ROW_H;
            boolean hover = mouseX >= listX && mouseX <= listX + listW && mouseY >= y && mouseY < y + ROW_H;
            boolean isPlaying = playing != null && playing.id.equals(track.id);
            boolean isSelected = !selectedTrackId.isBlank() && selectedTrackId.equals(track.id);
            int rowBg;
            if (isSelected) {
                rowBg = 0xFF35524A;
            } else if (hover) {
                rowBg = C_ROW_HOVER;
            } else if (row % 2 == 0) {
                rowBg = C_ROW_ALT;
            } else {
                rowBg = 0;
            }
            if (rowBg != 0) {
                graphics.fill(listX, y, listX + listW, y + ROW_H, rowBg);
            }
            if (isPlaying) {
                graphics.fill(listX, y, listX + 2, y + ROW_H, C_GOOD);
            }
            if (hover) {
                hoveredTrack = track;
            }

            int textColor;
            if (track.sourceType == MusicTrack.SourceType.BOOKMARK) {
                textColor = 0xFF7FB6FF;
            } else if (!track.isPlayable()) {
                textColor = C_BAD;
            } else if (!track.isOgg()) {
                textColor = C_WARN;
            } else if (isPlaying) {
                textColor = C_GOOD;
            } else {
                textColor = C_TEXT;
            }
            String heart = track.liked ? "♥" : "";
            graphics.drawString(font, heart, listX + 6, y + 5, 0xFFE56B6F, false);

            String prefix = switch (track.sourceType) {
                case BOOKMARK -> "🔗 ";
                case BUILT_IN -> "[MC] ";
                case DIRECT_URL -> "↓ ";
                case LOCAL_FILE -> "";
            };
            graphics.drawString(font, prefix + trim(track.title, prefix.isEmpty() ? 30 : 26),
                    listX + 22, y + 5, textColor, false);
            graphics.drawString(font, trim(track.displayArtist(), 18), listX + 196, y + 5, C_MUTED, false);
            String durText = track.durationMs > 0 ? formatTime(track.durationMs) : "--:--";
            graphics.drawString(font, durText, listX + listW - font.width(durText) - 6, y + 5, C_MUTED, false);
        }

        // Scrollbar
        if (visible.size() > maxRows) {
            int barX = listX + listW - 3;
            int barH = listH;
            graphics.fill(barX, listY, barX + 2, listY + barH, 0xFF1B2624);
            int knobH = Math.max(8, (int) ((float) maxRows / visible.size() * barH));
            int knobY = listY + (int) ((float) scroll / Math.max(1, maxScroll) * (barH - knobH));
            graphics.fill(barX, knobY, barX + 2, knobY + knobH, C_LINE);
        }
    }

    private void drawFooter(GuiGraphics graphics) {
        int volY = top + PANEL_H - 6;
        int volPct = Math.round(CustomMusicPlayer.get().volume() * 100.0F);
        graphics.drawString(font, Component.translatable("screen.mydrugs.music.volume", volPct),
                left + PANEL_W - 69, volY - 14, C_MUTED, false);

        // ffmpeg status indicator
        boolean ff = AudioConverter.isFfmpegAvailable();
        graphics.drawString(font, "●", left + PANEL_W - 5, top + PANEL_H - 7, ff ? C_GOOD : C_WARN, false);
    }

    private Component playPauseLabel() {
        return Component.literal(CustomMusicPlayer.get().isPlaying() ? "⏸" : "▶");
    }

    private Component repeatLabel() {
        return Component.literal(CustomMusicPlayer.get().repeat() ? "↻!" : "↻");
    }

    private Component shuffleLabel() {
        return Component.literal(CustomMusicPlayer.get().shuffle() ? "⇅!" : "⇅");
    }

    private static Component likeLabel(MusicTrack track) {
        return Component.literal(track != null && track.liked ? "♥" : "♡");
    }

    private void sendControl(HeadphonesControlPayload.Action action, String trackId) {
        ClientPacketDistributor.sendToServer(
                new HeadphonesControlPayload(action, trackId == null ? "" : trackId, CustomMusicPlayer.get().volume())
        );
    }

    private static String trim(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, Math.max(0, max - 3)) + "...";
    }

    private static String formatTime(long ms) {
        long total = Math.max(0L, ms / 1000L);
        long mins = total / 60L;
        long secs = total % 60L;
        return String.format(Locale.ROOT, "%d:%02d", mins, secs);
    }
}
