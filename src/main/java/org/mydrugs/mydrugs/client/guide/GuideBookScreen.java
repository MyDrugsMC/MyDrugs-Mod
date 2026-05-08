package org.mydrugs.mydrugs.client.guide;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GuideBookScreen extends Screen {
    private static final int BOOK_W = 204;
    private static final int BOOK_H = 240;
    private static final int SPINE_W = 8;
    private static final int BORDER_R = 3;
    private static final int TITLE_SEP_Y = 26;
    private static final int FOOTER_SEP_Y = BOOK_H - 30;
    private static final int CONTENT_PAD = 10;
    private static final int CONTENT_INSET = SPINE_W + 2 + CONTENT_PAD;
    private static final int SCROLLBAR_W = 3;
    private static final int SCROLLBAR_GAP = 3;
    private static final int CONTENT_W = BOOK_W - CONTENT_INSET - BORDER_R - CONTENT_PAD - SCROLLBAR_W - SCROLLBAR_GAP;
    private static final int LINE_H = 10;
    private static final int SCROLL_STEP = 18;

    private static final int C_SHADOW = 0x55000000;
    private static final int C_SPINE = 0xFF3A1E0A;
    private static final int C_BORDER = 0xFF2C1408;
    private static final int C_PAGE = 0xFFF8F0DC;
    private static final int C_PAGE_DARK = 0xFFEEE2C2;
    private static final int C_RULE = 0xFFC4A256;
    private static final int C_CORNER = 0xFF7A5520;
    private static final int C_TITLE = 0xFF3A1E0A;
    private static final int C_HEADING = 0xFF6B3300;
    private static final int C_BODY = 0xFF2A1808;
    private static final int C_PAGE_NUM = 0xFF9B7020;
    private static final int C_SCROLL_TRACK = 0x33000000;
    private static final int C_SCROLL_THUMB = 0xAA7A5520;
    private static final int C_TIP_BG = 0x3019701A;
    private static final int C_TIP_EDGE = 0xFF1A7020;
    private static final int C_TIP_TEXT = 0xFF155015;
    private static final int C_WARN_BG = 0x30AA1010;
    private static final int C_WARN_EDGE = 0xFFAA1010;
    private static final int C_WARN_TEXT = 0xFF8B0000;
    private static final int C_GOAL_BG = 0x301050AA;
    private static final int C_GOAL_EDGE = 0xFF1050AA;
    private static final int C_GOAL_TEXT = 0xFF0A3F8B;
    private static final int C_LINK = 0xFF1F5FA8;
    private static final int C_LINK_HOVER = 0xFF7A3CD4;
    private static final int C_LINK_BG = 0x1A7A3CD4;

    private static final Pattern INLINE_LINK = Pattern.compile("\\[\\[([^|\\]]+)(?:\\|([^\\]]+))?\\]\\]");
    private static final Pattern TOKEN = Pattern.compile("\\S+\\s*|\\s+");

    private List<GuidePage> pages;
    private Map<String, Integer> pageLookup = Map.of();
    private final List<LinkHitbox> linkHitboxes = new ArrayList<>();
    private int currentPage = 0;
    private int scrollOffset = 0;
    private int bookX;
    private int bookY;
    private int contentX;
    private int contentTop;
    private int contentBottom;
    private int lastMouseX;
    private int lastMouseY;

    public GuideBookScreen() {
        super(Component.literal("MyDrugs Field Guide"));
    }

    @Override
    protected void init() {
        super.init();

        this.bookX = (this.width - BOOK_W) / 2;
        this.bookY = (this.height - BOOK_H) / 2;
        this.contentX = this.bookX + CONTENT_INSET;
        this.contentTop = this.bookY + TITLE_SEP_Y + 5;
        this.contentBottom = this.bookY + FOOTER_SEP_Y - 4;

        if (this.pages == null) {
            this.pages = GuideLoader.load(Minecraft.getInstance().getResourceManager());
            rebuildPageLookup();
        }

        clampPage();
        clampScroll();

        int btnY = this.bookY + FOOTER_SEP_Y + 7;
        addRenderableWidget(Button.builder(Component.literal("<"), button -> navigate(-1))
                .bounds(this.bookX + SPINE_W + 4, btnY, 22, 15)
                .build());
        addRenderableWidget(Button.builder(Component.literal(">"), button -> navigate(1))
                .bounds(this.bookX + BOOK_W - BORDER_R - 26, btnY, 22, 15)
                .build());

        addRenderableWidget(Button.builder(Component.literal("menu"), button -> navigateTo(1))
                .bounds(this.bookX + BOOK_W - BORDER_R + 16, btnY, 52, 15)
                .build());
    }

    private void rebuildPageLookup() {
        Map<String, Integer> lookup = new HashMap<>();
        for (int i = 0; i < this.pages.size(); i++) {
            GuidePage page = this.pages.get(i);
            lookup.put(page.title(), i);
            lookup.put(normalizeTarget(page.title()), i);
        }
        this.pageLookup = Map.copyOf(lookup);
    }

    private void navigate(int delta) {
        navigateTo(this.currentPage + delta);
    }

    private void navigateTo(int page) {
        this.currentPage = page;
        this.scrollOffset = 0;
        clampPage();
    }

    private void navigateTo(String target) {
        Integer page = this.pageLookup.get(target);
        if (page == null) {
            page = this.pageLookup.get(normalizeTarget(target));
        }
        if (page == null) {
            return;
        }
        navigateTo(page);
    }

    private void clampPage() {
        int max = this.pages == null ? 0 : Math.max(0, this.pages.size() - 1);
        this.currentPage = Math.max(0, Math.min(this.currentPage, max));
    }

    private void clampScroll() {
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, computeMaxScroll()));
    }

    private int computeMaxScroll() {
        if (this.pages == null || this.pages.isEmpty()) {
            return 0;
        }
        int totalH = measurePageHeight(this.pages.get(this.currentPage));
        int visible = this.contentBottom - this.contentTop;
        return Math.max(0, totalH - visible);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_PAGE_UP) {
            navigate(-1);
            return true;
        }
        if (key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_PAGE_DOWN) {
            navigate(1);
            return true;
        }
        if (key == GLFW.GLFW_KEY_UP) {
            this.scrollOffset -= SCROLL_STEP;
            clampScroll();
            return true;
        }
        if (key == GLFW.GLFW_KEY_DOWN) {
            this.scrollOffset += SCROLL_STEP;
            clampScroll();
            return true;
        }
        if (key == GLFW.GLFW_KEY_HOME) {
            this.scrollOffset = 0;
            return true;
        }
        if (key == GLFW.GLFW_KEY_END) {
            this.scrollOffset = computeMaxScroll();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= this.contentX && mouseX <= this.contentX + CONTENT_W
                && mouseY >= this.contentTop && mouseY <= this.contentBottom) {
            this.scrollOffset -= (int) Math.round(scrollY * SCROLL_STEP);
            clampScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClicked) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            LinkHitbox link = hoveredLink((int) event.x(), (int) event.y());
            if (link != null) {
                navigateTo(link.target());
                return true;
            }
        }
        return super.mouseClicked(event, doubleClicked);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        this.linkHitboxes.clear();

        renderTransparentBackground(graphics);
        drawBookFrame(graphics);
        if (this.pages != null && !this.pages.isEmpty()) {
            drawPageContent(graphics, this.pages.get(this.currentPage));
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawBookFrame(GuiGraphics graphics) {
        graphics.fill(this.bookX + 4, this.bookY + 4, this.bookX + BOOK_W + 4, this.bookY + BOOK_H + 4, C_SHADOW);
        graphics.fill(this.bookX, this.bookY, this.bookX + SPINE_W, this.bookY + BOOK_H, C_SPINE);
        graphics.fill(this.bookX + SPINE_W, this.bookY, this.bookX + BOOK_W, this.bookY + BORDER_R, C_BORDER);
        graphics.fill(this.bookX + SPINE_W, this.bookY + BOOK_H - BORDER_R, this.bookX + BOOK_W, this.bookY + BOOK_H, C_BORDER);
        graphics.fill(this.bookX + BOOK_W - BORDER_R, this.bookY, this.bookX + BOOK_W, this.bookY + BOOK_H, C_BORDER);
        graphics.fill(this.bookX + SPINE_W, this.bookY + BORDER_R, this.bookX + BOOK_W - BORDER_R, this.bookY + BOOK_H - BORDER_R, C_PAGE);
        graphics.fill(this.bookX + SPINE_W, this.bookY + BORDER_R, this.bookX + SPINE_W + 3, this.bookY + BOOK_H - BORDER_R, C_PAGE_DARK);

        int cr = this.bookX + BOOK_W - BORDER_R - 6;
        int cb = this.bookY + BOOK_H - BORDER_R - 6;
        graphics.fill(this.bookX + SPINE_W + 3, this.bookY + BORDER_R + 3, this.bookX + SPINE_W + 5, this.bookY + BORDER_R + 5, C_CORNER);
        graphics.fill(cr, this.bookY + BORDER_R + 3, cr + 2, this.bookY + BORDER_R + 5, C_CORNER);
        graphics.fill(this.bookX + SPINE_W + 3, cb, this.bookX + SPINE_W + 5, cb + 2, C_CORNER);
        graphics.fill(cr, cb, cr + 2, cb + 2, C_CORNER);

        graphics.fill(this.bookX + SPINE_W + 4, this.bookY + TITLE_SEP_Y, this.bookX + BOOK_W - BORDER_R - 4, this.bookY + TITLE_SEP_Y + 1, C_RULE);
        graphics.fill(this.bookX + SPINE_W + 4, this.bookY + FOOTER_SEP_Y, this.bookX + BOOK_W - BORDER_R - 4, this.bookY + FOOTER_SEP_Y + 1, C_RULE);
    }

    private void drawPageContent(GuiGraphics graphics, GuidePage page) {
        int surfaceX = this.bookX + SPINE_W + 2;
        int surfaceW = BOOK_W - SPINE_W - 2 - BORDER_R;
        int titleX = surfaceX + Math.max(0, (surfaceW - this.font.width(page.title())) / 2);
        graphics.drawString(this.font, page.title(), titleX, this.bookY + 9, C_TITLE, false);

        clampScroll();
        int totalH = measurePageHeight(page);
        int visible = this.contentBottom - this.contentTop;

        graphics.enableScissor(this.contentX, this.contentTop, this.contentX + CONTENT_W + SCROLLBAR_GAP + SCROLLBAR_W, this.contentBottom);
        int y = this.contentTop - this.scrollOffset;
        for (GuideElement element : page.elements()) {
            int elementHeight = measureElement(element);
            if (y + elementHeight <= this.contentTop) {
                y += elementHeight;
                continue;
            }
            y = renderElement(graphics, element, y);
        }
        graphics.disableScissor();

        if (totalH > visible) {
            drawScrollbar(graphics, totalH, visible);
        }

        String indicator = (this.currentPage + 1) + " / " + this.pages.size();
        int indicatorX = surfaceX + (surfaceW - this.font.width(indicator)) / 2;
        graphics.drawString(this.font, indicator, indicatorX, this.bookY + FOOTER_SEP_Y + 9, C_PAGE_NUM, false);
    }

    private void drawScrollbar(GuiGraphics graphics, int totalH, int visibleH) {
        int trackX = this.contentX + CONTENT_W + SCROLLBAR_GAP;
        int trackY = this.contentTop;
        int trackH = this.contentBottom - this.contentTop;
        graphics.fill(trackX, trackY, trackX + SCROLLBAR_W, trackY + trackH, C_SCROLL_TRACK);

        int thumbH = Math.max(12, trackH * visibleH / totalH);
        int maxScroll = Math.max(1, totalH - visibleH);
        int thumbY = trackY + (int) ((long) (trackH - thumbH) * this.scrollOffset / maxScroll);
        graphics.fill(trackX, thumbY, trackX + SCROLLBAR_W, thumbY + thumbH, C_SCROLL_THUMB);
    }

    private int renderElement(GuiGraphics graphics, GuideElement element, int y) {
        return switch (element.type()) {
            case TEXT -> renderRichText(graphics, element.text(), "", y, C_BODY, this.contentX, CONTENT_W) + 3;
            case HEADING -> renderHeading(graphics, element.text(), y);
            case TIP -> renderCallout(graphics, element.text(), y, "TIP", C_TIP_BG, C_TIP_EDGE, C_TIP_TEXT);
            case WARNING -> renderCallout(graphics, element.text(), y, "!", C_WARN_BG, C_WARN_EDGE, C_WARN_TEXT);
            case GOAL -> renderCallout(graphics, element.text(), y, "GOAL", C_GOAL_BG, C_GOAL_EDGE, C_GOAL_TEXT);
            case LINK -> renderLink(graphics, element.text(), element.target(), y);
            case TITLE -> renderTitleElement(graphics, element.text(), y);
            case SEPARATOR -> renderSeparator(graphics, y);
            case ITEM -> renderItemIcon(graphics, element.text(), y);
        };
    }

    private int renderHeading(GuiGraphics graphics, String text, int y) {
        y += 4;
        String upper = text.toUpperCase(Locale.ROOT);
        graphics.drawString(this.font, upper, this.contentX, y, C_HEADING, false);
        y += LINE_H;
        graphics.fill(this.contentX, y, this.contentX + Math.min(this.font.width(upper), CONTENT_W), y + 1, C_RULE);
        return y + 4;
    }

    private int renderCallout(GuiGraphics graphics, String text, int y, String label, int bgColor, int edgeColor, int textColor) {
        int innerX = this.contentX + 5;
        int innerW = CONTENT_W - 10;
        int textH = measureRichText(text, "", innerW);
        int boxH = LINE_H + textH + 8;

        graphics.fill(this.contentX, y, this.contentX + CONTENT_W, y + boxH, bgColor);
        graphics.fill(this.contentX, y, this.contentX + 2, y + boxH, edgeColor);
        graphics.drawString(this.font, "[" + label + "]", innerX, y + 3, edgeColor, false);
        renderRichText(graphics, text, "", y + 3 + LINE_H, textColor, innerX, innerW);
        return y + boxH + 4;
    }

    private int renderLink(GuiGraphics graphics, String text, String target, int y) {
        int nextY = renderRichText(graphics, "> " + text, target, y, C_LINK, this.contentX, CONTENT_W);
        return nextY + 3;
    }

    private int renderTitleElement(GuiGraphics graphics, String text, int y) {
        y += 18;
        int width = this.font.width(text);
        int x = this.contentX + Math.max(0, (CONTENT_W - width) / 2);
        graphics.drawString(this.font, text, x, y, C_TITLE, false);
        y += LINE_H + 8;
        graphics.fill(this.contentX + 24, y, this.contentX + CONTENT_W - 24, y + 1, C_RULE);
        return y + 16;
    }

    private int renderSeparator(GuiGraphics graphics, int y) {
        graphics.fill(this.contentX + 10, y + 3, this.contentX + CONTENT_W - 10, y + 4, C_RULE);
        return y + 10;
    }

    private int renderItemIcon(GuiGraphics graphics, String itemId, int y) {
        try {
            ResourceLocation location = ResourceLocation.parse(itemId);
            var item = BuiltInRegistries.ITEM.getOptional(location).orElse(null);
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                graphics.renderItem(stack, this.contentX, y);
                graphics.drawString(this.font, stack.getHoverName().getString(), this.contentX + 20, y + 4, C_BODY, false);
                return y + 20;
            }
        } catch (Exception ignored) {
        }
        return y + LINE_H;
    }

    private int renderRichText(GuiGraphics graphics, String text, String forcedTarget, int y, int color, int x, int width) {
        List<RichLine> lines = wrapRichText(text, forcedTarget, width);
        for (RichLine line : lines) {
            int cursorX = x;
            for (RichSegment segment : line.segments()) {
                boolean link = !segment.target().isEmpty();
                int segmentWidth = this.font.width(segment.text());
                boolean hovered = link && contains(this.lastMouseX, this.lastMouseY, cursorX, y, segmentWidth, LINE_H);
                int drawColor = link ? (hovered ? C_LINK_HOVER : C_LINK) : color;
                if (link) {
                    if (hovered) {
                        graphics.fill(cursorX - 1, y - 1, cursorX + segmentWidth + 1, y + LINE_H, C_LINK_BG);
                    }
                    graphics.fill(cursorX, y + LINE_H - 1, cursorX + segmentWidth, y + LINE_H, drawColor);
                    addLinkHitbox(cursorX, y, segmentWidth, LINE_H, segment.target());
                }
                graphics.drawString(this.font, segment.text(), cursorX, y, drawColor, false);
                cursorX += segmentWidth;
            }
            y += LINE_H;
        }
        return y;
    }

    private void addLinkHitbox(int x, int y, int width, int height, String target) {
        if (y + height < this.contentTop || y > this.contentBottom) {
            return;
        }
        this.linkHitboxes.add(new LinkHitbox(x, y, width, height, target));
    }

    private int measurePageHeight(GuidePage page) {
        int height = 0;
        for (GuideElement element : page.elements()) {
            height += measureElement(element);
        }
        return height;
    }

    private int measureElement(GuideElement element) {
        return switch (element.type()) {
            case TEXT -> measureRichText(element.text(), "", CONTENT_W) + 3;
            case HEADING -> LINE_H + 8;
            case TIP, WARNING, GOAL -> LINE_H + measureRichText(element.text(), "", CONTENT_W - 10) + 8 + 4;
            case LINK -> measureRichText("> " + element.text(), element.target(), CONTENT_W) + 3;
            case TITLE -> LINE_H + 43;
            case SEPARATOR -> 10;
            case ITEM -> 20;
        };
    }

    private int measureRichText(String text, String forcedTarget, int width) {
        return wrapRichText(text, forcedTarget, width).size() * LINE_H;
    }

    private List<RichLine> wrapRichText(String text, String forcedTarget, int width) {
        List<RichRun> runs = parseRuns(text, forcedTarget);
        List<RichLine> lines = new ArrayList<>();
        List<RichSegment> segments = new ArrayList<>();
        int lineWidth = 0;

        for (RichRun run : runs) {
            Matcher tokenMatcher = TOKEN.matcher(run.text());
            while (tokenMatcher.find()) {
                String token = tokenMatcher.group();
                if (lineWidth == 0) {
                    token = token.stripLeading();
                }
                if (token.isEmpty()) {
                    continue;
                }

                int tokenWidth = this.font.width(token);
                if (lineWidth > 0 && lineWidth + tokenWidth > width) {
                    lines.add(new RichLine(List.copyOf(segments), lineWidth));
                    segments.clear();
                    lineWidth = 0;
                    token = token.stripLeading();
                    if (token.isEmpty()) {
                        continue;
                    }
                    tokenWidth = this.font.width(token);
                }

                segments.add(new RichSegment(token, run.target()));
                lineWidth += tokenWidth;
            }
        }

        if (!segments.isEmpty() || lines.isEmpty()) {
            lines.add(new RichLine(List.copyOf(segments), lineWidth));
        }
        return lines;
    }

    private static List<RichRun> parseRuns(String text, String forcedTarget) {
        if (forcedTarget != null && !forcedTarget.isBlank()) {
            return List.of(new RichRun(text, forcedTarget));
        }

        List<RichRun> runs = new ArrayList<>();
        Matcher matcher = INLINE_LINK.matcher(text);
        int index = 0;
        while (matcher.find()) {
            if (matcher.start() > index) {
                runs.add(new RichRun(text.substring(index, matcher.start()), ""));
            }
            String target = matcher.group(1).trim();
            String label = matcher.group(2) == null ? target : matcher.group(2).trim();
            runs.add(new RichRun("[" + label + "]", target));
            index = matcher.end();
        }
        if (index < text.length()) {
            runs.add(new RichRun(text.substring(index), ""));
        }
        return runs;
    }

    private LinkHitbox hoveredLink(int mouseX, int mouseY) {
        for (LinkHitbox hitbox : this.linkHitboxes) {
            if (hitbox.contains(mouseX, mouseY)) {
                return hitbox;
            }
        }
        return null;
    }

    private static String normalizeTarget(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-");
        normalized = normalized.replaceAll("^-+", "").replaceAll("-+$", "");
        return normalized;
    }

    private static boolean contains(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private record RichRun(String text, String target) {
    }

    private record RichSegment(String text, String target) {
    }

    private record RichLine(List<RichSegment> segments, int width) {
    }

    private record LinkHitbox(int x, int y, int width, int height, String target) {
        boolean contains(int mouseX, int mouseY) {
            return GuideBookScreen.contains(mouseX, mouseY, this.x, this.y, this.width, this.height);
        }
    }
}
