package org.mydrugs.mydrugs.client.recovery.music;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.mydrugs.mydrugs.network.HeadphonesControlPayload;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

public final class HeadphonesMusicScreen extends Screen {
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 272;
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
    private MusicLibrary.SortMode sortMode = MusicLibrary.SortMode.ALL_ALPHA;
    private final List<Button> filterButtons = new ArrayList<>();
    private Component transientStatus = Component.empty();
    private long transientStatusUntilTick;
    private String pendingRemovalId = "";
    private long pendingRemovalUntilTick;

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
        }).bounds(btnX, controlsY, 24, 20)
                .tooltip(Tooltip.create(Component.translatable("screen.mydrugs.music.previous"))).build());
        btnX += 26;

        playPauseButton = Button.builder(playPauseLabel(), button -> {
            CustomMusicPlayer.get().toggle();
            sendControl(HeadphonesControlPayload.Action.TOGGLE_PLAY, "");
            button.setMessage(playPauseLabel());
        }).bounds(btnX, controlsY, 28, 20)
                .tooltip(Tooltip.create(Component.translatable("screen.mydrugs.music.play_pause"))).build();
        addRenderableWidget(playPauseButton);
        btnX += 30;

        addRenderableWidget(Button.builder(Component.literal("⏭"), button -> {
            CustomMusicPlayer.get().next();
            sendControl(HeadphonesControlPayload.Action.NEXT, "");
        }).bounds(btnX, controlsY, 24, 20)
                .tooltip(Tooltip.create(Component.translatable("screen.mydrugs.music.next"))).build());
        btnX += 28;

        likeButton = Button.builder(Component.literal("♡"), button -> {
            MusicTrack track = CustomMusicPlayer.get().currentTrack();
            if (track != null) {
                MusicLibrary.get().toggleLike(track.id);
                sendControl(HeadphonesControlPayload.Action.LIKE_TRACK, track.id);
                button.setMessage(likeLabel(track));
            }
        }).bounds(btnX, controlsY, 22, 20)
                .tooltip(Tooltip.create(Component.translatable("screen.mydrugs.music.like"))).build();
        addRenderableWidget(likeButton);
        btnX += 26;

        repeatButton = Button.builder(repeatLabel(), button -> {
            CustomMusicPlayer.get().setRepeat(!CustomMusicPlayer.get().repeat());
            button.setMessage(repeatLabel());
        }).bounds(btnX, controlsY, 22, 20)
                .tooltip(Tooltip.create(Component.translatable("screen.mydrugs.music.repeat"))).build();
        addRenderableWidget(repeatButton);
        btnX += 26;

        shuffleButton = Button.builder(shuffleLabel(), button -> {
            CustomMusicPlayer.get().setShuffle(!CustomMusicPlayer.get().shuffle());
            button.setMessage(shuffleLabel());
        }).bounds(btnX, controlsY, 22, 20)
                .tooltip(Tooltip.create(Component.translatable("screen.mydrugs.music.shuffle"))).build();
        addRenderableWidget(shuffleButton);

        // Volume buttons (right side of controls row)
        addRenderableWidget(Button.builder(Component.literal("−"), button -> {
            changeVolume(-0.05F);
        }).bounds(left + PANEL_W - 60, controlsY, 18, 20)
                .tooltip(Tooltip.create(Component.translatable("screen.mydrugs.music.volume_down"))).build());
        addRenderableWidget(Button.builder(Component.literal("+"), button -> {
            changeVolume(0.05F);
        }).bounds(left + PANEL_W - 40, controlsY, 18, 20)
                .tooltip(Tooltip.create(Component.translatable("screen.mydrugs.music.volume_up"))).build());

        // ---- Search ----
        search = new EditBox(font, left + 12, top + 104, PANEL_W - 24, 16, Component.translatable("screen.mydrugs.music.search"));
        search.setMaxLength(80);
        search.setHint(Component.translatable("screen.mydrugs.music.search"));
        addRenderableWidget(search);

        filterButtons.clear();
        int filterX = left + 12;
        for (MusicLibrary.SortMode mode : MusicLibrary.SortMode.values()) {
            int filterWidth = mode == MusicLibrary.SortMode.MOST_PLAYED ? 62 : 52;
            Button filter = Button.builder(filterLabel(mode), ignored -> {
                sortMode = mode;
                scroll = 0;
                updateFilterButtons();
            }).bounds(filterX, top + 123, filterWidth, 18).build();
            filterButtons.add(filter);
            addRenderableWidget(filter);
            filterX += filterWidth + 2;
        }
        updateFilterButtons();

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
                            long now = clientTick();
                            if (!target.id.equals(pendingRemovalId) || now > pendingRemovalUntilTick) {
                                pendingRemovalId = target.id;
                                pendingRemovalUntilTick = now + 100;
                                button.setMessage(Component.translatable("screen.mydrugs.music.confirm_remove"));
                                setTransient(Component.translatable("screen.mydrugs.music.confirm_remove"), 100);
                                return;
                            }
                            boolean wasPlaying = isCurrentlyPlaying(target);
                            MusicLibrary.ImportResult result = MusicLibrary.get().remove(target.id);
                            if (result.success() && wasPlaying) {
                                CustomMusicPlayer.get().stop();
                            }
                            if (result.success()) {
                                selectedTrack = null;
                                selectedTrackId = "";
                            }
                            pendingRemovalId = "";
                            button.setMessage(Component.translatable("screen.mydrugs.music.remove"));
                            setTransient(result.message(), 80);
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
    public void tick() {
        super.tick();
        if (!pendingRemovalId.isBlank() && clientTick() > pendingRemovalUntilTick) {
            pendingRemovalId = "";
            if (removeButton != null) {
                removeButton.setMessage(Component.translatable("screen.mydrugs.music.remove"));
            }
            setTransient(Component.translatable("screen.mydrugs.music.remove_cancelled"), 40);
        }
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
                changeVolume(0.05F);
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                changeVolume(-0.05F);
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
        graphics.drawCenteredString(font, sourceLabel(current), artX + artSize / 2, artY + (artSize - 8) / 2,
                CustomMusicPlayer.get().isPlaying() ? C_GOOD : C_MUTED);

        int textX = artX + artSize + 8;
        graphics.drawString(font, trim(titleText, 40), textX, cardY + 6, C_TEXT, false);
        String detail = (artist.isBlank() ? "" : artist + " | ") + playbackStateText().getString();
        Component playerStatus = CustomMusicPlayer.get().status();
        if (playerStatus != null && !playerStatus.getString().isBlank()) {
            detail += ": " + playerStatus.getString();
        }
        graphics.drawString(font, trim(detail, 48), textX, cardY + 18, playbackStateColor(), false);
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
        String query = search == null ? "" : search.getValue();
        List<MusicTrack> visible = MusicLibrary.get().sortedTracks(query, sortMode);
        listX = left + 12;
        listY = top + 145;
        listW = PANEL_W - 24;
        listH = PANEL_H - 181;
        graphics.fill(listX, listY, listX + listW, listY + listH, 0xFF111A17);

        if (visible.isEmpty()) {
            Component empty = MusicLibrary.get().tracks().isEmpty()
                    ? Component.translatable("screen.mydrugs.music.empty_library")
                    : Component.translatable("screen.mydrugs.music.no_search_results", query);
            graphics.drawWordWrap(font, empty, listX + 8, listY + 10, listW - 16, C_MUTED);
            if (MusicLibrary.get().tracks().isEmpty()) {
                graphics.drawWordWrap(font, Component.translatable("screen.mydrugs.music.empty_help"),
                        listX + 8, listY + 30, listW - 16, C_MUTED);
            }
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
        Component modes = Component.translatable("screen.mydrugs.music.modes",
                CustomMusicPlayer.get().repeat()
                        ? Component.translatable("screen.mydrugs.music.on").getString()
                        : Component.translatable("screen.mydrugs.music.off").getString(),
                CustomMusicPlayer.get().shuffle()
                        ? Component.translatable("screen.mydrugs.music.on").getString()
                        : Component.translatable("screen.mydrugs.music.off").getString());
        graphics.drawString(font, modes, left + 12, volY - 14, C_MUTED, false);
        if (transientStatus != null && !transientStatus.getString().isBlank()
                && clientTick() <= transientStatusUntilTick) {
            graphics.drawCenteredString(font, transientStatus, left + PANEL_W / 2, top + PANEL_H - 39, C_WARN);
        }
    }

    private void changeVolume(float delta) {
        CustomMusicPlayer.get().setVolume(CustomMusicPlayer.get().volume() + delta);
        sendControl(HeadphonesControlPayload.Action.SET_VOLUME, "");
        int percent = Math.round(CustomMusicPlayer.get().volume() * 100.0F);
        Component message = percent == 0
                ? Component.translatable("screen.mydrugs.music.volume_muted")
                : percent == 100
                ? Component.translatable("screen.mydrugs.music.volume_max")
                : Component.translatable("screen.mydrugs.music.volume_feedback", percent);
        setTransient(message, 40);
    }

    private void setTransient(Component message, int ticks) {
        transientStatus = message;
        transientStatusUntilTick = clientTick() + ticks;
    }

    private long clientTick() {
        return minecraft == null || minecraft.player == null ? 0L : minecraft.player.tickCount;
    }

    private Component playbackStateText() {
        return Component.translatable(switch (CustomMusicPlayer.get().state()) {
            case PLAYING -> "screen.mydrugs.music.state.playing";
            case PAUSED -> "screen.mydrugs.music.state.paused";
            case STOPPED -> "screen.mydrugs.music.state.stopped";
            case ERROR -> "screen.mydrugs.music.state.error";
        });
    }

    private int playbackStateColor() {
        return switch (CustomMusicPlayer.get().state()) {
            case PLAYING -> C_GOOD;
            case ERROR -> C_BAD;
            case PAUSED, STOPPED -> C_MUTED;
        };
    }

    private static String sourceLabel(MusicTrack track) {
        if (track == null) return "-";
        return switch (track.sourceType) {
            case LOCAL_FILE -> "L";
            case DIRECT_URL -> "URL";
            case BUILT_IN -> "MC";
            case BOOKMARK -> "BM";
        };
    }

    private Component filterLabel(MusicLibrary.SortMode mode) {
        return Component.translatable(switch (mode) {
            case ALL_ALPHA -> "screen.mydrugs.music.filter.all";
            case LIKED -> "screen.mydrugs.music.filter.liked";
            case RECENT -> "screen.mydrugs.music.filter.recent";
            case MOST_PLAYED -> "screen.mydrugs.music.filter.most_played";
            case IMPORTED -> "screen.mydrugs.music.filter.imported";
            case BUILT_IN -> "screen.mydrugs.music.filter.built_in";
        });
    }

    private void updateFilterButtons() {
        MusicLibrary.SortMode[] modes = MusicLibrary.SortMode.values();
        for (int i = 0; i < filterButtons.size() && i < modes.length; i++) {
            filterButtons.get(i).active = modes[i] != sortMode;
        }
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
