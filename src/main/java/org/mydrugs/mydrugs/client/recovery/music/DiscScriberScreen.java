package org.mydrugs.mydrugs.client.recovery.music;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.mydrugs.mydrugs.network.ScribePersonalDiscPayload;
import org.mydrugs.mydrugs.network.ScribePersonalDiscResultPayload;
import org.mydrugs.mydrugs.recovery.item.ModRecoveryItems;

import java.util.List;
import java.util.Locale;

public final class DiscScriberScreen extends Screen {
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 226;
    private static final int ROW_H = 18;
    private static final int C_BG = 0xCC000000;
    private static final int C_PANEL = 0xFF1A2220;
    private static final int C_HEADER = 0xFF202F2A;
    private static final int C_ROW_HOVER = 0xFF2A3F37;
    private static final int C_ROW_SELECTED = 0xFF35524A;
    private static final int C_ROW_ALT = 0xFF1E2A26;
    private static final int C_TEXT = 0xFFE5F1EA;
    private static final int C_MUTED = 0xFF8FA89B;
    private static final int C_GOOD = 0xFFB7E4C7;
    private static final int C_WARN = 0xFFFFD166;
    private static final int C_BAD = 0xFFE56B6F;

    private final BlockPos scriberPos;
    private EditBox search;
    private Button scribeButton;
    private Button editButton;
    private int left;
    private int top;
    private int scroll;
    private int listX;
    private int listY;
    private int listW;
    private int listH;
    private MusicTrack hoveredTrack;
    private MusicTrack selectedTrack;
    private Component status = Component.empty();
    private boolean scribePending;
    private boolean uploadPending;
    private String readyServerTrackId = "";
    private String readyAudioHash = "";

    public DiscScriberScreen(BlockPos scriberPos) {
        super(Component.translatable("screen.mydrugs.disc_scriber.title"));
        this.scriberPos = scriberPos.immutable();
    }

    @Override
    protected void init() {
        left = (width - PANEL_W) / 2;
        top = (height - PANEL_H) / 2;
        clearWidgets();
        MusicLibrary.get().refreshBuiltins();

        search = new EditBox(font, left + 12, top + 40, PANEL_W - 24, 16, Component.translatable("screen.mydrugs.music.search"));
        search.setMaxLength(80);
        search.setHint(Component.translatable("screen.mydrugs.music.search"));
        addRenderableWidget(search);

        int bottomY = top + PANEL_H - 26;
        addRenderableWidget(Button.builder(Component.translatable("screen.mydrugs.music.add"),
                        button -> minecraft.setScreen(new MusicImportScreen(this)))
                .bounds(left + 12, bottomY, 44, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.mydrugs.music.open_folder"),
                        button -> Util.getPlatform().openPath(MusicLibraryStorage.root()))
                .bounds(left + 60, bottomY, 72, 20).build());
        editButton = Button.builder(Component.translatable("screen.mydrugs.music.edit"),
                        button -> {
                            if (selectedTrack != null) {
                                minecraft.setScreen(new TrackEditScreen(this, selectedTrack));
                            }
                        })
                .bounds(left + 136, bottomY, 38, 20).build();
        addRenderableWidget(editButton);

        scribeButton = Button.builder(Component.translatable("screen.mydrugs.disc_scriber.scribe"),
                        button -> scribeSelected())
                .bounds(left + PANEL_W - 104, bottomY, 92, 20).build();
        addRenderableWidget(scribeButton);
        updateButtons();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
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
            readyServerTrackId = "";
            readyAudioHash = "";
            status = Component.empty();
            updateButtons();
            return true;
        }
        return super.mouseClicked(event, doubleClicked);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
        graphics.fill(0, 0, width, height, C_BG);
        graphics.fill(left, top, left + PANEL_W, top + PANEL_H, C_PANEL);
        graphics.fill(left, top, left + PANEL_W, top + 30, C_HEADER);
        graphics.drawString(font, title, left + 12, top + 11, C_TEXT, false);

        super.render(graphics, mouseX, mouseY, partialTick);
        drawTrackRows(graphics, mouseX, mouseY);
        drawSelectedTrack(graphics);
        drawBlankDiscState(graphics);
        if (!status.getString().isBlank()) {
            graphics.drawString(font, status, left + 12, top + PANEL_H - 40, C_WARN, false);
        }
    }

    private void drawTrackRows(GuiGraphics graphics, int mouseX, int mouseY) {
        hoveredTrack = null;
        List<MusicTrack> visible = MusicLibrary.get().sortedTracks(search == null ? "" : search.getValue()).stream()
                .filter(track -> track.sourceType != MusicTrack.SourceType.BOOKMARK)
                .toList();
        listX = left + 12;
        listY = top + 62;
        listW = PANEL_W - 24;
        listH = 92;
        graphics.fill(listX, listY, listX + listW, listY + listH, 0xFF111A17);

        if (visible.isEmpty()) {
            String query = search == null ? "" : search.getValue();
            Component empty = MusicLibrary.get().tracks().isEmpty()
                    ? Component.translatable("screen.mydrugs.music.empty_library")
                    : Component.translatable("screen.mydrugs.music.no_search_results", query);
            graphics.drawWordWrap(font, empty, listX + 8, listY + 10, listW - 16, C_MUTED);
            return;
        }

        int maxRows = listH / ROW_H;
        int maxScroll = Math.max(0, visible.size() - maxRows);
        scroll = Math.min(scroll, maxScroll);
        for (int row = 0; row < maxRows && row + scroll < visible.size(); row++) {
            MusicTrack track = visible.get(row + scroll);
            int y = listY + row * ROW_H;
            boolean hover = mouseX >= listX && mouseX <= listX + listW && mouseY >= y && mouseY < y + ROW_H;
            boolean selected = selectedTrack != null && selectedTrack.id.equals(track.id);
            int rowBg = selected ? C_ROW_SELECTED : hover ? C_ROW_HOVER : row % 2 == 0 ? C_ROW_ALT : 0;
            if (rowBg != 0) {
                graphics.fill(listX, y, listX + listW, y + ROW_H, rowBg);
            }
            if (hover) {
                hoveredTrack = track;
            }
            int textColor = playableForDisc(track) ? C_TEXT : C_BAD;
            String titleText = (track.liked ? "* " : "") + trim(track.title, 32);
            graphics.drawString(font, titleText, listX + 6, y + 5, textColor, false);
            graphics.drawString(font, trim(track.displayArtist(), 18), listX + 190, y + 5, C_MUTED, false);
            String duration = track.durationMs > 0 ? formatTime(track.durationMs) : "--:--";
            graphics.drawString(font, duration, listX + listW - font.width(duration) - 6, y + 5, C_MUTED, false);
        }
    }

    private void drawSelectedTrack(GuiGraphics graphics) {
        int y = top + 160;
        graphics.drawString(font, Component.translatable("screen.mydrugs.disc_scriber.selected"), left + 12, y, C_MUTED, false);
        if (selectedTrack == null) {
            graphics.drawString(font, Component.translatable("screen.mydrugs.music.no_track_selected"), left + 92, y, C_WARN, false);
            return;
        }
        graphics.drawString(font, trim(selectedTrack.title, 32), left + 92, y, C_TEXT, false);
        graphics.drawString(font, trim(selectedTrack.displayArtist(), 24), left + 92, y + 11, C_MUTED, false);
        String duration = selectedTrack.durationMs > 0 ? formatTime(selectedTrack.durationMs) : "--:--";
        graphics.drawString(font, duration, left + PANEL_W - font.width(duration) - 12, y, C_MUTED, false);
        Component blocker = discScribeBlocker(selectedTrack);
        if (blocker != null) {
            graphics.drawString(font, blocker, left + 92, y + 22, C_BAD, false);
        }
    }

    private void drawBlankDiscState(GuiGraphics graphics) {
        Component label = Component.translatable(hasBlankDisc() ? "screen.mydrugs.disc_scriber.has_blank_disc" : "screen.mydrugs.disc_scriber.no_blank_disc");
        graphics.drawString(font, label, left + 12, top + PANEL_H - 52, hasBlankDisc() ? C_GOOD : C_BAD, false);
    }

    private void scribeSelected() {
        if (selectedTrack == null) {
            status = Component.translatable("screen.mydrugs.music.no_track_selected");
            return;
        }
        if (!playableForDisc(selectedTrack)) {
            status = Component.translatable("screen.mydrugs.music.unsupported");
            return;
        }
        if (!hasBlankDisc()) {
            status = Component.translatable("screen.mydrugs.disc_scriber.no_blank_disc");
            return;
        }
        if (selectedTrack.sourceType == MusicTrack.SourceType.BUILT_IN) {
            sendScribeRequest();
            return;
        }
        if (readyServerTrackId.isBlank() || readyAudioHash.isBlank()) {
            uploadPending = true;
            status = Component.translatable("screen.mydrugs.disc_scriber.uploading");
            SharedMusicTransferClient.upload(selectedTrack);
            updateButtons();
            return;
        }
        sendScribeRequest();
    }

    private void sendScribeRequest() {
        if (selectedTrack == null) return;
        boolean serverHosted = !readyServerTrackId.isBlank() && !readyAudioHash.isBlank();
        ClientPacketDistributor.sendToServer(new ScribePersonalDiscPayload(
                scriberPos,
                selectedTrack.id,
                selectedTrack.title,
                selectedTrack.artist,
                (int) Math.min(Integer.MAX_VALUE, selectedTrack.durationMs),
                selectedTrack.liked,
                readyServerTrackId,
                readyAudioHash,
                serverHosted
        ));
        scribePending = true;
        status = Component.translatable("screen.mydrugs.disc_scriber.scribing");
        updateButtons();
    }

    public void handleUploadResult(org.mydrugs.mydrugs.network.ServerMusicUploadResultPayload result) {
        uploadPending = false;
        status = Component.translatable(result.messageKey());
        if (result.success()) {
            readyServerTrackId = result.serverTrackId();
            readyAudioHash = result.audioHash();
            sendScribeRequest();
        } else {
            updateButtons();
        }
    }

    public void handleUploadProgress(int current, int total) {
        status = Component.translatable("screen.mydrugs.disc_scriber.upload_progress", current, total);
    }

    public void handleScribeResult(ScribePersonalDiscResultPayload result) {
        scribePending = false;
        status = Component.translatable(result.messageKey());
        updateButtons();
    }

    private void updateButtons() {
        boolean canScribe = !scribePending && !uploadPending
                && selectedTrack != null && playableForDisc(selectedTrack) && hasBlankDisc();
        if (scribeButton != null) {
            scribeButton.active = canScribe;
            scribeButton.setMessage(Component.translatable(
                    readyServerTrackId.isBlank() && selectedTrack != null
                            && selectedTrack.sourceType != MusicTrack.SourceType.BUILT_IN
                            ? "screen.mydrugs.disc_scriber.upload_and_scribe"
                            : "screen.mydrugs.disc_scriber.scribe"));
        }
        if (editButton != null) {
            editButton.active = selectedTrack != null;
        }
    }

    private boolean hasBlankDisc() {
        if (minecraft == null || minecraft.player == null) {
            return false;
        }
        Inventory inventory = minecraft.player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(ModRecoveryItems.BLANK_MUSIC_DISC.get())) {
                return true;
            }
        }
        return false;
    }

    private static boolean playableForDisc(MusicTrack track) {
        return track != null && track.sourceType != MusicTrack.SourceType.BOOKMARK && track.isPlayable() && track.isOgg();
    }

    private Component discScribeBlocker(MusicTrack track) {
        if (track == null) return Component.translatable("screen.mydrugs.music.no_track_selected");
        if (track.sourceType == MusicTrack.SourceType.BOOKMARK) {
            return Component.translatable("screen.mydrugs.disc_scriber.bookmark_blocked");
        }
        if (!track.isPlayable()) return Component.translatable("screen.mydrugs.disc_scriber.missing_local_file");
        if (!track.isOgg()) return Component.translatable("screen.mydrugs.disc_scriber.needs_ogg");
        if (uploadPending) return Component.translatable("screen.mydrugs.disc_scriber.uploading");
        if (!hasBlankDisc()) return Component.translatable("screen.mydrugs.disc_scriber.no_blank_disc");
        return null;
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
