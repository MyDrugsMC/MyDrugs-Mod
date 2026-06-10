package org.mydrugs.mydrugs.client.recovery.disclaimer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.client.accessibility.AccessibilityPresets;
import org.mydrugs.mydrugs.client.accessibility.AccessibilityScreen;
import org.mydrugs.mydrugs.client.recovery.music.NativeFileDialog;
import org.mydrugs.mydrugs.client.recovery.music.tools.ExternalTool;
import org.mydrugs.mydrugs.client.recovery.music.tools.ExternalToolDownloader;
import org.mydrugs.mydrugs.client.recovery.music.tools.ExternalToolManager;
import org.mydrugs.mydrugs.client.recovery.music.tools.ExternalToolManifest;
import org.mydrugs.mydrugs.client.recovery.music.tools.ExternalToolPlatform;
import org.mydrugs.mydrugs.client.recovery.music.tools.MyDrugsClientConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * First-world content notice. The Accept button stays disabled until the player scrolls the
 * notice text to the bottom (mouse wheel or keyboard). The optional-tool rows let a player install
 * ffmpeg/ffprobe/yt-dlp explicitly; nothing is downloaded automatically. Client-only screen.
 */
public final class DisclaimerScreen extends Screen {
    private static final String[] SECTIONS = {
            "age", "safety", "fiction", "accessibility", "downloads", "platforms", "tools"
    };

    private static final int PANEL_WIDTH = 482;
    private static final int HEADER_H = 30;
    private static final int FOOTER_H = 36;
    private static final int TOOLS_HEADING_H = 14;
    private static final int TOOL_ROW_H = 24;
    private static final int LINE_H = 11;
    private static final int PAD = 10;

    // Palette, consistent with the mod's other recovery screens.
    private static final int COL_DIM = 0xE8070A09;
    private static final int COL_PANEL = 0xFF151C19;
    private static final int COL_HEADER = 0xFF1F2A26;
    private static final int COL_VIEWPORT = 0xFF0F1513;
    private static final int COL_BOX = 0xFF1C2723;
    private static final int COL_ACCENT = 0xFF4E8C77;
    private static final int COL_ACCENT_DIM = 0xFF2B3F37;
    private static final int COL_TEXT = 0xFFE7F1EC;
    private static final int COL_TEXT_MID = 0xFF9DB3A9;
    private static final int COL_TEXT_DIM = 0xFF6F837B;
    private static final int COL_HEADING = 0xFFAEDCC6;
    private static final int COL_OK = 0xFF7FD6A6;
    private static final int COL_WARN = 0xFFF2C261;
    private static final int COL_BAD = 0xFFE08C7A;

    private final boolean resetMode;

    private int panelLeft;
    private int panelRight;
    private int viewportTop;
    private int viewportBottom;
    private int toolsTop;

    private final List<FormattedCharSequence> lines = new ArrayList<>();
    private final List<Boolean> lineIsHeading = new ArrayList<>();
    private int contentHeight;
    private double scroll;
    private boolean reachedBottom;

    private Button acceptButton;
    private final List<ToolRow> toolRows = new ArrayList<>();

    public DisclaimerScreen(boolean resetMode) {
        super(Component.translatable("screen.mydrugs.disclaimer.title"));
        this.resetMode = resetMode;
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(PANEL_WIDTH, width - 24);
        panelLeft = (width - panelWidth) / 2;
        panelRight = panelLeft + panelWidth;

        int toolsBlockH = TOOLS_HEADING_H + TOOL_ROW_H * ExternalTool.values().length;
        viewportTop = HEADER_H + PAD;
        toolsTop = height - FOOTER_H - toolsBlockH;
        viewportBottom = toolsTop - PAD;

        rebuildText();

        toolRows.clear();
        int rowY = toolsTop + TOOLS_HEADING_H;
        for (ExternalTool tool : ExternalTool.values()) {
            toolRows.add(new ToolRow(tool, rowY));
            rowY += TOOL_ROW_H;
        }

        int footerX = panelLeft + PAD;
        addRenderableWidget(Button.builder(
                        Component.translatable("button.mydrugs.accessibility.open"),
                        b -> minecraft.setScreen(new AccessibilityScreen(this)))
                .bounds(footerX, height - FOOTER_H + 8, 100, 20)
                .build());
        footerX += 104;
        addRenderableWidget(Button.builder(
                        Component.translatable("button.mydrugs.accessibility.minimal_now"),
                        b -> AccessibilityPresets.apply(Config.Client.PRESET_MINIMAL_EFFECTS))
                .bounds(footerX, height - FOOTER_H + 8, 112, 20)
                .build());
        footerX += 116;

        acceptButton = Button.builder(
                        Component.translatable("button.mydrugs.disclaimer.accept"), b -> onAccept())
                .bounds(panelRight - PAD - 120, height - FOOTER_H + 8, 120, 20)
                .build();
        addRenderableWidget(acceptButton);

        if (resetMode) {
            addRenderableWidget(Button.builder(
                            Component.translatable("button.mydrugs.disclaimer.reset"),
                            b -> {
                                FirstWorldDisclaimerHandler.resetAcknowledgementForNextJoin();
                                b.active = false;
                            })
                    .bounds(footerX, height - FOOTER_H + 8, 108, 20)
                    .build());
        }

        updateScrollState();
    }

    private void rebuildText() {
        lines.clear();
        lineIsHeading.clear();
        int wrapWidth = panelRight - panelLeft - PAD * 2 - 12;

        for (int s = 0; s < SECTIONS.length; s++) {
            if (s > 0) {
                lines.add(FormattedCharSequence.EMPTY);
                lineIsHeading.add(false);
            }
            Component sectionTitle = Component.translatable(
                    "screen.mydrugs.disclaimer.section." + SECTIONS[s] + ".title");
            for (FormattedCharSequence seq : font.split(sectionTitle, wrapWidth)) {
                lines.add(seq);
                lineIsHeading.add(true);
            }
            Component body = Component.translatable(
                    "screen.mydrugs.disclaimer.section." + SECTIONS[s] + ".body");
            for (FormattedCharSequence seq : font.split(body, wrapWidth)) {
                lines.add(seq);
                lineIsHeading.add(false);
            }
        }
        contentHeight = lines.size() * LINE_H;
    }

    private int maxScroll() {
        return Math.max(0, contentHeight - (viewportBottom - viewportTop) + 4);
    }

    private void updateScrollState() {
        scroll = Math.max(0, Math.min(scroll, maxScroll()));
        if (scroll >= maxScroll() - 0.5) {
            reachedBottom = true;
        }
        if (acceptButton != null) {
            acceptButton.active = reachedBottom;
        }
    }

    private void onAccept() {
        if (!reachedBottom) {
            return;
        }
        FirstWorldDisclaimerHandler.acknowledgeDisclaimer();
        onClose();
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return resetMode;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseY >= viewportTop && mouseY <= viewportBottom) {
            scroll -= scrollY * LINE_H * 3;
            updateScrollState();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int page = viewportBottom - viewportTop;
        switch (event.key()) {
            case GLFW.GLFW_KEY_DOWN -> scroll += LINE_H * 2;
            case GLFW.GLFW_KEY_UP -> scroll -= LINE_H * 2;
            case GLFW.GLFW_KEY_PAGE_DOWN, GLFW.GLFW_KEY_SPACE -> scroll += page;
            case GLFW.GLFW_KEY_PAGE_UP -> scroll -= page;
            case GLFW.GLFW_KEY_HOME -> scroll = 0;
            case GLFW.GLFW_KEY_END -> scroll = maxScroll();
            default -> {
                return super.keyPressed(event);
            }
        }
        updateScrollState();
        return true;
    }

    /** Panel chrome. Drawn before the widgets so buttons render cleanly on top. */
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, COL_DIM);
        graphics.fill(panelLeft, 0, panelRight, height, COL_PANEL);
        graphics.fill(panelLeft, 0, panelLeft + 1, height, COL_ACCENT_DIM);
        graphics.fill(panelRight - 1, 0, panelRight, height, COL_ACCENT_DIM);

        // header
        graphics.fill(panelLeft, 0, panelRight, HEADER_H, COL_HEADER);
        graphics.fill(panelLeft, HEADER_H - 1, panelRight, HEADER_H, COL_ACCENT);
        graphics.drawCenteredString(font, title, (panelLeft + panelRight) / 2, 5, COL_TEXT);
        graphics.drawCenteredString(font,
                Component.translatable("screen.mydrugs.disclaimer.subtitle"),
                (panelLeft + panelRight) / 2, 17, COL_TEXT_DIM);

        // footer divider
        graphics.fill(panelLeft, height - FOOTER_H, panelRight, height - FOOTER_H + 1, COL_ACCENT_DIM);

        // tool-row boxes are drawn here, before the widgets, so buttons render on top of them
        for (ToolRow row : toolRows) {
            row.renderBox(graphics);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        // --- notice text viewport ---
        int vpLeft = panelLeft + PAD;
        int vpRight = panelRight - PAD;
        graphics.fill(vpLeft, viewportTop, vpRight, viewportBottom, COL_VIEWPORT);
        drawBorder(graphics, vpLeft, viewportTop, vpRight, viewportBottom, COL_ACCENT_DIM);

        graphics.enableScissor(vpLeft + 1, viewportTop + 1, vpRight - 1, viewportBottom - 1);
        int y = viewportTop + 6 - (int) scroll;
        for (int i = 0; i < lines.size(); i++) {
            if (y + LINE_H >= viewportTop && y <= viewportBottom) {
                graphics.drawString(font, lines.get(i), vpLeft + 8, y,
                        lineIsHeading.get(i) ? COL_HEADING : COL_TEXT_MID, false);
            }
            y += LINE_H;
        }
        graphics.disableScissor();

        // scrollbar
        int max = maxScroll();
        if (max > 0) {
            int trackTop = viewportTop + 2;
            int trackH = viewportBottom - viewportTop - 4;
            int barH = Math.max(20, (int) ((long) trackH * (viewportBottom - viewportTop) / contentHeight));
            barH = Math.min(barH, trackH);
            int barY = trackTop + (int) ((trackH - barH) * (scroll / max));
            graphics.fill(vpRight - 5, trackTop, vpRight - 2, trackTop + trackH, COL_ACCENT_DIM);
            graphics.fill(vpRight - 5, barY, vpRight - 2, barY + barH, COL_ACCENT);
        }
        if (!reachedBottom) {
            graphics.drawCenteredString(font,
                    Component.translatable("screen.mydrugs.disclaimer.scroll_hint"),
                    (panelLeft + panelRight) / 2, viewportBottom + 4, COL_WARN);
        }

        // --- optional tools ---
        graphics.drawString(font, Component.translatable("screen.mydrugs.disclaimer.tools.header"),
                panelLeft + PAD, toolsTop + 5, COL_TEXT_DIM, false);
        for (ToolRow row : toolRows) {
            row.render(graphics);
        }
    }

    private static void drawBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    /** One ffmpeg / ffprobe / yt-dlp control row. */
    private final class ToolRow {
        private final ExternalTool tool;
        private final int rowTop;
        private final int boxLeft;
        private final int boxRight;
        private final Button downloadButton;

        private volatile boolean downloading;
        private volatile double progress;
        private volatile String resultKey;

        ToolRow(ExternalTool tool, int rowTop) {
            this.tool = tool;
            this.rowTop = rowTop;
            this.boxLeft = panelLeft + PAD;
            this.boxRight = panelRight - PAD;

            ExternalToolManifest manifest = ExternalToolManifest.get();
            ExternalToolPlatform platform = ExternalToolPlatform.current();

            int by = rowTop + 4;
            int h = 14;
            int gap = 3;
            int wPath = 46, wCopy = 50, wWeb = 44, wDown = 54;
            int xPath = boxRight - 6 - wPath;
            int xCopy = xPath - gap - wCopy;
            int xWeb = xCopy - gap - wWeb;
            int xDown = xWeb - gap - wDown;

            downloadButton = Button.builder(
                            Component.translatable("button.mydrugs.disclaimer.download"),
                            b -> startDownload())
                    .bounds(xDown, by, wDown, h).build();
            downloadButton.active = ExternalToolManager.get().canDownload(tool);

            Button web = Button.builder(
                            Component.translatable("button.mydrugs.disclaimer.open_website"),
                            b -> net.minecraft.Util.getPlatform().openUri(manifest.officialWebsiteUrl(tool)))
                    .bounds(xWeb, by, wWeb, h).build();
            Button copy = Button.builder(
                            Component.translatable("button.mydrugs.disclaimer.copy_command"),
                            b -> minecraft.keyboardHandler.setClipboard(
                                    manifest.manualInstallCommand(tool, platform)))
                    .bounds(xCopy, by, wCopy, h).build();
            Button local = Button.builder(
                            Component.translatable("button.mydrugs.disclaimer.select_local"),
                            b -> selectLocal())
                    .bounds(xPath, by, wPath, h).build();

            addRenderableWidget(downloadButton);
            addRenderableWidget(web);
            addRenderableWidget(copy);
            addRenderableWidget(local);
        }

        private void selectLocal() {
            if (NativeFileDialog.isBusy()) {
                return;
            }
            NativeFileDialog.openFile(
                    Component.translatable("button.mydrugs.disclaimer.select_local").getString(),
                    minecraft.gameDirectory.toPath(),
                    false
            ).whenComplete((path, error) -> minecraft.execute(() -> {
                if (path != null) {
                    MyDrugsClientConfig.get().setToolPath(tool, path.toString());
                    ExternalToolManager.get().invalidate();
                    resultKey = ExternalToolManager.get().resolve(tool).status().messageKey();
                }
            }));
        }

        private void startDownload() {
            if (downloading || !ExternalToolManager.get().canDownload(tool)) {
                return;
            }
            downloading = true;
            progress = 0;
            resultKey = null;
            downloadButton.active = false;
            Thread thread = new Thread(() -> {
                ExternalToolDownloader.Outcome outcome = ExternalToolManager.get().download(tool,
                        (read, total) -> progress = total > 0 ? (double) read / total : progress);
                minecraft.execute(() -> {
                    downloading = false;
                    resultKey = outcome.messageKey();
                    downloadButton.active = ExternalToolManager.get().canDownload(tool);
                });
            }, "MyDrugs tool download " + tool.id());
            thread.setDaemon(true);
            thread.start();
        }

        /** Static box chrome, drawn before the widgets. */
        void renderBox(GuiGraphics graphics) {
            int top = rowTop + 1;
            int bottom = rowTop + TOOL_ROW_H - 2;
            graphics.fill(boxLeft, top, boxRight, bottom, COL_BOX);
            drawBorder(graphics, boxLeft, top, boxRight, bottom, COL_ACCENT_DIM);
        }

        /** Dynamic text + progress, drawn after the widgets in non-button regions. */
        void render(GuiGraphics graphics) {
            ExternalToolManager.Resolution resolution = ExternalToolManager.get().resolve(tool);

            // name + status share one line on the left, clear of the button cluster
            String name = ExternalToolManifest.get().displayName(tool);
            graphics.drawString(font, name, boxLeft + 8, rowTop + 8, COL_TEXT, false);

            String statusKey = resultKey != null ? resultKey : resolution.status().messageKey();
            int statusColor = downloading ? COL_WARN : statusColor(resolution.status());
            graphics.drawString(font, Component.translatable(statusKey),
                    boxLeft + 8 + font.width(name) + 8, rowTop + 8, statusColor, false);

            // thin progress bar along the bottom edge of the row box
            int barLeft = boxLeft + 8;
            int barRight = boxRight - 8;
            int barY = rowTop + 19;
            graphics.fill(barLeft, barY, barRight, barY + 2, COL_ACCENT_DIM);
            double shown = downloading ? progress : (resolution.usable() ? 1.0 : 0.0);
            int fillRight = barLeft + (int) ((barRight - barLeft) * Math.max(0, Math.min(1, shown)));
            graphics.fill(barLeft, barY, fillRight, barY + 2,
                    downloading ? COL_WARN : (resolution.usable() ? COL_OK : COL_ACCENT_DIM));
        }

        private int statusColor(org.mydrugs.mydrugs.client.recovery.music.tools.ExternalToolStatus status) {
            return switch (status) {
                case FOUND_ON_SYSTEM, USER_CONFIGURED, DOWNLOADED_VERIFIED -> COL_OK;
                case MISSING_CODEC, VERIFICATION_FAILED -> COL_BAD;
                default -> COL_TEXT_DIM;
            };
        }
    }
}
