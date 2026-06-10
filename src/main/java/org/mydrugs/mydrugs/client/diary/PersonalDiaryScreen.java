package org.mydrugs.mydrugs.client.diary;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.effect.EffectPolarity;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.addiction.data.WithdrawalPhase;
import org.mydrugs.mydrugs.addiction.explain.AddictionDangerReason;
import org.mydrugs.mydrugs.addiction.explain.AddictionSuggestedAction;
import org.mydrugs.mydrugs.client.effects.AddictionClientState;
import org.mydrugs.mydrugs.client.effects.hud.HudSymptomIcons;
import org.mydrugs.mydrugs.client.guide.GuideBookScreen;
import org.mydrugs.mydrugs.client.recovery.music.tools.MyDrugsClientConfig;
import org.mydrugs.mydrugs.addiction.config.SymptomFlags;
import org.mydrugs.mydrugs.diary.DiaryDrugStatDto;
import org.mydrugs.mydrugs.diary.DiaryEntryDto;
import org.mydrugs.mydrugs.diary.DiaryIntegrationProgressDto;
import org.mydrugs.mydrugs.diary.DiaryMasteryStatDto;
import org.mydrugs.mydrugs.diary.DiaryBlocker;
import org.mydrugs.mydrugs.diary.DiaryBlockerRoutes;
import org.mydrugs.mydrugs.diary.DiaryBreadcrumb;
import org.mydrugs.mydrugs.diary.DiaryMemory;
import org.mydrugs.mydrugs.diary.DiaryPlayerStateDto;
import org.mydrugs.mydrugs.diary.DiaryWarning;
import org.mydrugs.mydrugs.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.addiction.network.PersonalDiarySnapshotPayload;
import org.mydrugs.mydrugs.addiction.network.PersonalDiarySubmitResultPayload;
import org.mydrugs.mydrugs.addiction.network.SubmitPersonalDiaryEntryPayload;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Book-style diary screen. Pages 0 and 1 are summary pages, followed by one
 * page per recorded day and a final editable page for today.
 */
public final class PersonalDiaryScreen extends Screen {
    // Layout
    private static final int PAGE_W = 320;
    private static final int PAGE_H = 230;
    private static final int PAGE_PADDING = 14;
    private static final int HEADER_H = 28;
    private static final int FOOTER_H = 42;
    private static final int LINE_H = 11;
    private static final int ICON_SIZE = 12;
    private static final int ICON_GAP = 4;

    // Colors
    private static final int PAPER = 0xFFEFD9B0;          // warm parchment
    private static final int PAPER_BORDER = 0xFF8C6738;   // darker rim
    private static final int PAPER_SHADOW = 0xFF52371C;   // deepest outline
    private static final int INK = 0xFF402815;            // primary text
    private static final int INK_SOFT = 0xFF7B5736;       // muted text
    private static final int INK_HIGHLIGHT = 0xFF7A4B1A;  // accent
    private static final int INK_WARNING = 0xFF8E2F25;    // warning text
    private static final int INK_HINT = 0xFF5F4C2A;       // mechanical hint text
    private static final int DIVIDER = 0x66402815;        // semi-transparent ink

    private PersonalDiarySnapshotPayload snapshot;
    private final List<DiaryPage> pages = new ArrayList<>();

    private int pageIndex;
    private int scrollY;

    // Layout-derived bounds (set in init)
    private int pageLeft;
    private int pageTop;
    private int contentLeft;
    private int contentRight;
    private int contentTop;
    private int contentBottom;

    // Widgets
    private Button prevButton;
    private Button nextButton;
    private MultiLineEditBox editor;
    private Button doneButton;
    private Button memoryCaptureButton;
    private Button memoryPreviewButton;
    private Button openMemoriesButton;

    private Component statusMessage;
    private long statusUntilTick;
    private boolean submitPending;
    private String pendingSubmittedText = "";
    private long snapshotReceivedClientTick;
    private int lastEffectiveCooldown;

    public PersonalDiaryScreen(PersonalDiarySnapshotPayload payload) {
        super(Component.translatable("screen.mydrugs.diary.title"));
        this.snapshot = payload;
        this.snapshotReceivedClientTick = clientTick();
        this.lastEffectiveCooldown = payload.cooldownTicksRemaining();
    }

    /**
     * Replace the snapshot in-place (e.g. after a custom-entry submit).
     * Keeps the same screen instance, preserves page index if still valid, scrolls to today.
     */
    public void applySnapshot(PersonalDiarySnapshotPayload newSnapshot) {
        this.snapshot = newSnapshot;
        this.snapshotReceivedClientTick = clientTick();
        this.lastEffectiveCooldown = newSnapshot.cooldownTicksRemaining();
        rebuildPages();
        // Jump to today's page after a successful submit so the user sees the new entry.
        this.pageIndex = pages.size() - 1;
        this.scrollY = 0;
        refreshWidgets();
    }

    public void handleSubmitResult(PersonalDiarySubmitResultPayload result) {
        submitPending = false;
        if (result.success()) {
            if (editor != null && editor.getValue().equals(pendingSubmittedText)) {
                editor.setValue("");
            }
            pendingSubmittedText = "";
            setStatus(Component.translatable(result.messageKey()).copy().withStyle(ChatFormatting.DARK_GREEN), 100);
        } else {
            pendingSubmittedText = "";
            setStatus(Component.translatable(result.messageKey(),
                    Math.max(1, (result.cooldownTicksRemaining() + 19) / 20))
                    .copy().withStyle(ChatFormatting.DARK_RED), 120);
        }
        refreshWidgets();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        pageLeft = (this.width - PAGE_W) / 2;
        pageTop = (this.height - PAGE_H) / 2;
        contentLeft = pageLeft + PAGE_PADDING;
        contentRight = pageLeft + PAGE_W - PAGE_PADDING;
        contentTop = pageTop + HEADER_H;
        contentBottom = pageTop + PAGE_H - FOOTER_H;

        rebuildPages();

        // Default to last page (today) when first opening; preserve otherwise.
        if (pageIndex < 0 || pageIndex >= pages.size()) {
            pageIndex = Math.max(0, pages.size() - 1);
        }

        // Footer nav buttons
        int navY = pageTop + PAGE_H - FOOTER_H + 4;
        prevButton = Button.builder(Component.translatable("screen.mydrugs.diary.previous"), b -> goToPage(pageIndex - 1))
                .bounds(contentLeft, navY, 22, 20).build();
        addRenderableWidget(prevButton);

        nextButton = Button.builder(Component.translatable("screen.mydrugs.diary.next"), b -> goToPage(pageIndex + 1))
                .bounds(contentRight - 22, navY, 22, 20).build();
        addRenderableWidget(nextButton);

        // Multi-line editor (only visible on today's page when cooldown == 0)
        int editorW = contentRight - contentLeft - 64;
        int editorX = contentLeft + 28;
        int editorY = navY;
        editor = MultiLineEditBox.builder()
                .setX(editorX)
                .setY(editorY)
                .setPlaceholder(Component.translatable("screen.mydrugs.diary.write_placeholder"))
                .setTextColor(INK)
                .setTextShadow(false)
                .setShowBackground(true)
                .build(this.font, editorW, 30, Component.translatable("screen.mydrugs.diary.title"));
        editor.setCharacterLimit(900);
        editor.visible = false;
        addRenderableWidget(editor);

        // Done button
        doneButton = Button.builder(Component.translatable("screen.mydrugs.diary.done"), b -> submitCustom())
                .bounds(contentRight - 28, navY, 28, 20).build();
        doneButton.visible = false;
        addRenderableWidget(doneButton);

        memoryCaptureButton = Button.builder(memoryCaptureLabel(), button -> {
            MyDrugsClientConfig config = MyDrugsClientConfig.get();
            config.setMemoryCaptureEnabled(!config.isMemoryCaptureEnabled());
            button.setMessage(memoryCaptureLabel());
            rebuildPages();
        }).bounds(editorX, navY, 72, 20).build();
        addRenderableWidget(memoryCaptureButton);
        memoryPreviewButton = Button.builder(memoryPreviewLabel(), button -> {
            MyDrugsClientConfig config = MyDrugsClientConfig.get();
            config.setMemoryPreviewEnabled(!config.isMemoryPreviewEnabled());
            button.setMessage(memoryPreviewLabel());
            rebuildPages();
        }).bounds(editorX + 76, navY, 72, 20).build();
        addRenderableWidget(memoryPreviewButton);
        openMemoriesButton = Button.builder(Component.translatable("screen.mydrugs.diary.privacy.open_folder"),
                button -> Util.getPlatform().openPath(MemoryStoragePaths.memoryRoot()))
                .bounds(editorX + 152, navY, 72, 20).build();
        addRenderableWidget(openMemoriesButton);

        refreshWidgets();
    }

    private void goToPage(int newIndex) {
        if (newIndex < 0 || newIndex >= pages.size()) return;
        pageIndex = newIndex;
        scrollY = 0;
        refreshWidgets();
    }

    private void refreshWidgets() {
        boolean onLastPage = pageIndex == pages.size() - 1;
        boolean onTodayPage = onLastPage
                && !pages.isEmpty()
                && pages.get(pageIndex) instanceof DayPage dp
                && dp.isToday;
        boolean cooldownOk = effectiveCooldownTicksRemaining() <= 0;
        boolean showEditor = onTodayPage && cooldownOk;
        boolean onPrivacyPage = !pages.isEmpty() && pages.get(pageIndex) instanceof PrivacyPage;

        if (editor != null) {
            editor.visible = showEditor;
        }
        if (doneButton != null) {
            doneButton.visible = showEditor;
            doneButton.active = !submitPending;
        }
        if (memoryCaptureButton != null) {
            memoryCaptureButton.visible = onPrivacyPage;
            memoryCaptureButton.setMessage(memoryCaptureLabel());
        }
        if (memoryPreviewButton != null) {
            memoryPreviewButton.visible = onPrivacyPage;
            memoryPreviewButton.setMessage(memoryPreviewLabel());
        }
        if (openMemoriesButton != null) openMemoriesButton.visible = onPrivacyPage;

        if (prevButton != null) prevButton.active = pageIndex > 0;
        if (nextButton != null) nextButton.active = pageIndex < pages.size() - 1;
    }

    private void submitCustom() {
        if (editor == null) return;
        String text = editor.getValue();
        if (text == null || text.trim().isEmpty()) {
            setStatus(Component.translatable("screen.mydrugs.diary.write_placeholder").copy().withStyle(ChatFormatting.GRAY), 50);
            return;
        }
        ClientPacketDistributor.sendToServer(new SubmitPersonalDiaryEntryPayload(text));
        submitPending = true;
        pendingSubmittedText = text;
        refreshWidgets();
        setStatus(Component.translatable("screen.mydrugs.diary.saving").withStyle(ChatFormatting.DARK_GRAY), 200);
    }

    @Override
    public void tick() {
        super.tick();
        int cooldown = effectiveCooldownTicksRemaining();
        if (lastEffectiveCooldown > 0 && cooldown == 0) {
            refreshWidgets();
        }
        lastEffectiveCooldown = cooldown;
    }

    private long clientTick() {
        return this.minecraft == null || this.minecraft.player == null ? 0L : this.minecraft.player.tickCount;
    }

    private int effectiveCooldownTicksRemaining() {
        long elapsed = Math.max(0L, clientTick() - snapshotReceivedClientTick);
        return Math.max(0, snapshot.cooldownTicksRemaining() - (int) Math.min(Integer.MAX_VALUE, elapsed));
    }

    private void setStatus(Component msg, int ticks) {
        this.statusMessage = msg;
        long now = this.minecraft == null || this.minecraft.player == null ? 0L : this.minecraft.player.tickCount;
        this.statusUntilTick = now + ticks;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // Editor takes priority for typing.
        if (editor != null && editor.visible && editor.isFocused()) {
            return super.keyPressed(event);
        }
        if (!pages.isEmpty() && pages.get(pageIndex) instanceof MindMapPageHolder holder
                && holder.page.keyPressed(event.key())) {
            return true;
        }
        int keyCode = event.key();
        if (keyCode == InputConstants.KEY_LEFT || keyCode == InputConstants.KEY_PAGEUP) {
            goToPage(pageIndex - 1);
            return true;
        }
        if (keyCode == InputConstants.KEY_RIGHT || keyCode == InputConstants.KEY_PAGEDOWN) {
            goToPage(pageIndex + 1);
            return true;
        }
        if (keyCode == InputConstants.KEY_UP) {
            scrollY = Math.max(0, scrollY - LINE_H);
            return true;
        }
        if (keyCode == InputConstants.KEY_DOWN) {
            scrollY += LINE_H;
            return true;
        }
        if (keyCode == InputConstants.KEY_HOME) {
            scrollY = 0;
            return true;
        }
        if (keyCode == InputConstants.KEY_END) {
            scrollY = Integer.MAX_VALUE / 2;
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!pages.isEmpty() && pages.get(pageIndex) instanceof MindMapPageHolder holder
                && holder.page.mouseClicked(event.x(), event.y(), event.button())) {
            return true;
        }
        if (event.button() == 0
                && !pages.isEmpty()
                && tryFollowLink(pages.get(pageIndex), event.x(), event.y())) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean tryFollowLink(DiaryPage page, double mouseX, double mouseY) {
        if (mouseX < contentLeft || mouseX > contentRight || mouseY < contentTop || mouseY > contentBottom) {
            return false;
        }
        boolean showDetails = isShiftDownForDiary();
        boolean showDebug = snapshot.clarity().diagnosticMode();
        int y = contentTop - scrollY;
        for (DiaryLine line : page.lines()) {
            if (!line.visible(showDetails, showDebug)) {
                continue;
            }
            if (line.kind == DiaryLine.Kind.GUIDE_LINK && mouseY >= y && mouseY < y + LINE_H) {
                minecraft.setScreen(new GuideBookScreen(this, line.linkTarget));
                return true;
            }
            if (line.kind == DiaryLine.Kind.LINK && mouseY >= y && mouseY < y + LINE_H) {
                int mindMapIndex = findMindMapPageIndex();
                if (mindMapIndex >= 0) {
                    MindMapPageHolder holder = (MindMapPageHolder) pages.get(mindMapIndex);
                    boolean focused = holder.page.focusNode(line.linkTarget);
                    pageIndex = mindMapIndex;
                    scrollY = 0;
                    refreshWidgets();
                    if (focused) {
                        setStatus(Component.translatable("screen.mydrugs.diary.memory_located"), 60);
                    }
                    return true;
                }
                return false;
            }
            y += LINE_H;
        }
        return false;
    }

    private int findMindMapPageIndex() {
        for (int i = 0; i < pages.size(); i++) {
            if (pages.get(i) instanceof MindMapPageHolder) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (!pages.isEmpty() && pages.get(pageIndex) instanceof MindMapPageHolder holder
                && holder.page.mouseReleased(event.button())) {
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (!pages.isEmpty() && pages.get(pageIndex) instanceof MindMapPageHolder holder
                && holder.page.mouseDragged(event.x(), event.y(), event.button(), dragX, dragY)) {
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDX, double scrollDY) {
        if (editor != null && editor.visible && editor.isFocused()) {
            return super.mouseScrolled(mouseX, mouseY, scrollDX, scrollDY);
        }
        if (!pages.isEmpty() && pages.get(pageIndex) instanceof MindMapPageHolder holder
                && holder.page.mouseScrolled(mouseX, mouseY, scrollDY)) {
            return true;
        }
        if (mouseX >= contentLeft && mouseX <= contentRight && mouseY >= contentTop && mouseY <= contentBottom) {
            scrollY = Math.max(0, scrollY - (int) (scrollDY * LINE_H * 2));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollDX, scrollDY);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(g);

        // Page paper with layered shadow border
        g.fill(pageLeft - 3, pageTop - 3, pageLeft + PAGE_W + 3, pageTop + PAGE_H + 3, PAPER_SHADOW);
        g.fill(pageLeft - 2, pageTop - 2, pageLeft + PAGE_W + 2, pageTop + PAGE_H + 2, PAPER_BORDER);
        g.fill(pageLeft, pageTop, pageLeft + PAGE_W, pageTop + PAGE_H, PAPER);

        DiaryPage page = pages.get(pageIndex);

        // Header: title + page indicator
        g.drawString(this.font, page.title(), contentLeft, pageTop + 10, INK, false);
        String indicator = (pageIndex + 1) + " / " + pages.size();
        int indW = this.font.width(indicator);
        g.drawString(this.font, indicator, contentRight - indW, pageTop + 10, INK_SOFT, false);
        g.fill(contentLeft, pageTop + 22, contentRight, pageTop + 23, DIVIDER);

        // Content area: clip and render scrollable lines
        g.enableScissor(contentLeft, contentTop, contentRight, contentBottom);
        boolean showDetails = isShiftDownForDiary();
        boolean showDebug = snapshot.clarity().diagnosticMode();
        int totalContentHeight = visibleLineCount(page, showDetails, showDebug) * LINE_H;
        int visibleHeight = contentBottom - contentTop;
        int maxScroll = Math.max(0, totalContentHeight - visibleHeight);
        if (scrollY > maxScroll) scrollY = maxScroll;

        int y = contentTop - scrollY;
        for (DiaryLine line : page.lines()) {
            if (!line.visible(showDetails, showDebug)) {
                continue;
            }
            if (y + LINE_H >= contentTop && y <= contentBottom) {
                line.render(g, this.font, contentLeft, contentRight - 4, y);
            }
            y += LINE_H;
        }
        g.disableScissor();
        if (page instanceof MindMapPageHolder holder) {
            holder.page.setBounds(contentLeft, contentTop, contentRight, contentBottom);
            holder.page.render(g, this.font, mouseX, mouseY);
        }

        // Scroll indicator (right edge) if content overflows
        if (maxScroll > 0) {
            int trackH = visibleHeight;
            int thumbH = Math.max(12, (int) ((float) visibleHeight / totalContentHeight * trackH));
            int thumbY = contentTop + (int) ((float) scrollY / maxScroll * (trackH - thumbH));
            g.fill(contentRight - 3, contentTop, contentRight - 1, contentTop + trackH, 0x33000000);
            g.fill(contentRight - 3, thumbY, contentRight - 1, thumbY + thumbH, 0x99402815);
        }

        // Footer divider
        g.fill(contentLeft, pageTop + PAGE_H - FOOTER_H - 4, contentRight, pageTop + PAGE_H - FOOTER_H - 3, DIVIDER);

        // Footer text: cooldown / status / today hint
        renderFooterText(g, page);

        super.render(g, mouseX, mouseY, partialTick);
    }

    private int visibleLineCount(DiaryPage page, boolean showDetails, boolean showDebug) {
        int count = 0;
        for (DiaryLine line : page.lines()) {
            if (line.visible(showDetails, showDebug)) {
                count++;
            }
        }
        return count;
    }

    private boolean isShiftDownForDiary() {
        if (this.minecraft == null) {
            return false;
        }
        var window = this.minecraft.getWindow();
        return InputConstants.isKeyDown(window, InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(window, InputConstants.KEY_RSHIFT);
    }

    private void renderFooterText(GuiGraphics g, DiaryPage page) {
        int textY = pageTop + PAGE_H - FOOTER_H + 8;
        boolean onTodayPage = page instanceof DayPage dp && dp.isToday;

        if (onTodayPage) {
            if (effectiveCooldownTicksRemaining() > 0) {
                int secs = (effectiveCooldownTicksRemaining() + 19) / 20;
                Component msg = Component.translatable("screen.mydrugs.diary.cooldown", secs);
                int w = this.font.width(msg);
                g.drawString(this.font, msg, (pageLeft + PAGE_W / 2) - w / 2, textY, INK_SOFT, false);
            }
        } else if (pageHasShiftLines(page) && !isShiftDownForDiary() && !snapshot.clarity().diagnosticMode()) {
            Component msg = Component.translatable("screen.mydrugs.diary.shift_for_hints");
            int w = this.font.width(msg);
            g.drawString(this.font, msg, (pageLeft + PAGE_W / 2) - w / 2, textY, INK_SOFT, false);
        }

        if (statusMessage != null) {
            long now = this.minecraft == null || this.minecraft.player == null ? 0L : this.minecraft.player.tickCount;
            if (now <= statusUntilTick) {
                int sw = this.font.width(statusMessage);
                g.drawString(this.font, statusMessage, (pageLeft + PAGE_W / 2) - sw / 2, textY + LINE_H, INK_SOFT, false);
            } else {
                statusMessage = null;
            }
        }
    }

    private boolean pageHasShiftLines(DiaryPage page) {
        for (DiaryLine line : page.lines()) {
            if (line.visibility == DiaryLine.Visibility.SHIFT) {
                return true;
            }
        }
        return false;
    }

    // ----------------------- Page construction -----------------------

    private void rebuildPages() {
        pages.clear();
        pages.add(buildPage1());
        pages.add(buildMindMapPage());
        pages.add(buildPrivacyPage());
        pages.add(buildPage2());
        if (!snapshot.integrationProgress().isEmpty()) {
            pages.add(buildIntegrationOverviewPage());
        }
        for (DiaryIntegrationProgressDto progress : snapshot.integrationProgress()) {
            pages.add(buildIntegrationPage(progress));
        }

        // Group entries by day
        Map<Long, List<DiaryEntryDto>> byDay = new LinkedHashMap<>();
        for (DiaryEntryDto e : snapshot.entries()) {
            byDay.computeIfAbsent(e.day(), k -> new ArrayList<>()).add(e);
        }
        long today = snapshot.currentDay();
        for (Map.Entry<Long, List<DiaryEntryDto>> entry : byDay.entrySet()) {
            long day = entry.getKey();
            if (day == today) continue;
            pages.add(buildDayPage(day, entry.getValue(), false));
        }
        // Today is always the last page
        pages.add(buildDayPage(today, byDay.getOrDefault(today, List.of()), true));
    }

    private DiaryPage buildPage1() {
        List<DiaryLine> lines = new ArrayList<>();
        DiaryPlayerStateDto s = snapshot.playerState();

        lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.current_thought")));
        lines.addAll(wrapToLines(tr(snapshot.clarity().thought().textKey()), DiaryLine.Kind.TEXT, DiaryLine.Visibility.ALWAYS));

        if (!snapshot.clarity().warnings().isEmpty()) {
            lines.add(DiaryLine.spacer());
            lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.warnings")));
            for (DiaryWarning warning : snapshot.clarity().warnings()) {
                addWarning(lines, warning);
            }
        }

        lines.add(DiaryLine.spacer());
        lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.next_thread")));
        if (snapshot.clarity().breadcrumbs().isEmpty()) {
            lines.addAll(wrapToLines(tr("screen.mydrugs.diary.no_breadcrumbs")));
        } else {
            for (DiaryBreadcrumb breadcrumb : snapshot.clarity().breadcrumbs()) {
                addBreadcrumb(lines, breadcrumb);
            }
        }

        if (!snapshot.clarity().blockers().isEmpty()) {
            lines.add(DiaryLine.spacer());
            lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.unresolved")));
            for (DiaryBlocker blocker : snapshot.clarity().blockers()) {
                addBlocker(lines, blocker);
            }
        }

        if (!snapshot.clarity().memories().isEmpty()) {
            lines.add(DiaryLine.spacer());
            lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.memories")));
            int shown = 0;
            for (DiaryMemory memory : snapshot.clarity().memories()) {
                addMemory(lines, memory);
                if (++shown >= 3) {
                    break;
                }
            }
        }

        lines.add(DiaryLine.spacer());
        lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.body_state")));
        lines.addAll(wrapToLines(bodyStateSummary(s)));
        lines.add(DiaryLine.text("Main risk: " + mainRiskLabel(s)));
        lines.add(DiaryLine.text("Next: " + suggestedActionLabel(s)));
        lines.add(DiaryLine.spacer());
        lines.add(DiaryLine.text(stressLine(s.stress())));
        lines.add(DiaryLine.text(withdrawalLine(s.globalSeverity())));
        lines.add(DiaryLine.spacer());

        // Section: dominant drug / dose / bad trip / overdose
        boolean hasFocus = !s.dominantDrugId().isEmpty();
        if (hasFocus) {
            DrugId id = DrugId.bySerializedNameOrNull(s.dominantDrugId());
            ItemStack icon = id == null ? ItemStack.EMPTY : DrugIconHelper.stackFor(id);
            lines.add(DiaryLine.withIcon(icon, "Dominant drug: " + prettyDrug(s.dominantDrugId())));
        }
        if (!s.dominantCategory().isEmpty() && !"OTHER".equals(s.dominantCategory())) {
            lines.add(DiaryLine.text("Dominant feeling: " + prettyCategory(s.dominantCategory())));
        }
        if (!s.doseState().isEmpty() && !"NORMAL".equals(s.doseState())) {
            lines.add(DiaryLine.text("Dose: " + prettyDose(s.doseState())));
        }
        if (s.badTripActive()) {
            lines.add(DiaryLine.text(String.format(Locale.ROOT, "Bad trip: active, %d%%", Math.round(s.badTripSeverity() * 100.0F))));
        }
        if (s.overdoseTimerTicks() > 0) {
            lines.add(DiaryLine.text("Overdose danger: " + ((s.overdoseTimerTicks() + 19) / 20) + "s"));
        }
        if (s.sleepBlocked()) {
            lines.add(DiaryLine.text("Sleep refuses me right now."));
        }

        appendSanctuaryModuleSection(lines);
        appendFeelingReasonSections(lines, s);

        return new BasicPage(Component.translatable("screen.mydrugs.diary.inner_compass"), lines);
    }

    private DiaryPage buildPage2() {
        List<DiaryLine> lines = new ArrayList<>();

        lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.memories")));
        if (snapshot.clarity().memories().isEmpty()) {
            lines.addAll(wrapToLines(tr("screen.mydrugs.diary.no_memories")));
        } else {
            for (DiaryMemory memory : snapshot.clarity().memories()) {
                addMemory(lines, memory);
            }
        }
        lines.add(DiaryLine.spacer());

        // Rituals
        lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.rituals")));
        int totalCompleted = 0;
        int totalFailed = 0;
        DiaryMasteryStatDto bestRitual = null;
        for (DiaryMasteryStatDto m : snapshot.masteryStats()) {
            totalCompleted += m.completed();
            totalFailed += m.failed();
            if (bestRitual == null || m.completed() > bestRitual.completed()) {
                bestRitual = m;
            }
        }
        if (totalCompleted == 0 && totalFailed == 0) {
            lines.addAll(wrapToLines(Component.translatable("screen.mydrugs.diary.no_rituals").getString()));
        } else {
            lines.addAll(wrapToLines(String.format(Locale.ROOT,
                    "Even if I failed %d rituals, I fulfilled %d ones with success.",
                    totalFailed, totalCompleted)));
        }
        if (bestRitual != null && bestRitual.completed() > 0) {
            String name = bestRitual.displayName().isBlank() ? prettyRecipeId(bestRitual.recipeId()) : bestRitual.displayName();
            lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.mastery.formula", name)));
            lines.addAll(wrapToLines(tr("screen.mydrugs.diary.mastery.rank",
                    tr(bestRitual.rankKey()))));
            lines.addAll(wrapToLines(tr("screen.mydrugs.diary.mastery.record",
                    bestRitual.completed(), bestRitual.failed())));
            lines.addAll(wrapToLines(tr("screen.mydrugs.diary.mastery.bonuses",
                    Math.round(bestRitual.timingWindowBonus() * 100.0F),
                    Math.round(bestRitual.actionTimeoutBonus() * 100.0F),
                    bestRitual.removedActions())));
            lines.addAll(wrapToLines(tr("screen.mydrugs.diary.mastery.next",
                    tr(bestRitual.nextBenefitKey()))));
        }

        // Lifetime drug use
        lines.add(DiaryLine.spacer());
        lines.add(DiaryLine.heading("What I have taken"));
        boolean any = false;
        for (DiaryDrugStatDto d : snapshot.drugStats()) {
            if (d.lifetimeDose() <= 0.0F) continue;
            any = true;
            DrugId id = DrugId.bySerializedNameOrNull(d.drugId());
            ItemStack icon = id == null ? ItemStack.EMPTY : DrugIconHelper.stackFor(id);
            String sentence = lifetimeSentence(d.drugId(), d.lifetimeDose());
            List<DiaryLine> wrapped = wrapToLines(sentence);
            // Attach the icon to the first wrapped line, indent subsequent ones.
            for (int i = 0; i < wrapped.size(); i++) {
                DiaryLine wl = wrapped.get(i);
                if (i == 0) {
                    lines.add(DiaryLine.withIcon(icon, wl.text));
                } else {
                    lines.add(DiaryLine.indented(wl.text));
                }
            }
            lines.add(DiaryLine.indented(toleranceLine(d.tolerance())));
        }
        if (!any) {
            lines.addAll(wrapToLines(Component.translatable("screen.mydrugs.diary.no_life_stats").getString()));
        }

        return new BasicPage(Component.translatable("screen.mydrugs.diary.what_i_did_in_my_life"), lines);
    }

    private MindMapPageHolder buildMindMapPage() {
        MindMapPage page = new MindMapPage(
                snapshot.psycheNodes(),
                contentLeft, contentTop, contentRight, contentBottom
        );
        return new MindMapPageHolder(Component.translatable("screen.mydrugs.diary.mind_map"), page);
    }

    private DiaryPage buildPrivacyPage() {
        MyDrugsClientConfig config = MyDrugsClientConfig.get();
        List<DiaryLine> lines = new ArrayList<>();
        lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.privacy.diary_storage")));
        lines.addAll(wrapToLines(tr("screen.mydrugs.diary.privacy.diary_storage_detail")));
        lines.add(DiaryLine.spacer());
        lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.privacy.memory_storage")));
        lines.addAll(wrapToLines(tr("screen.mydrugs.diary.privacy.memory_storage_detail")));
        lines.add(DiaryLine.text(tr(config.isMemoryCaptureEnabled()
                ? "screen.mydrugs.diary.privacy.capture_on"
                : "screen.mydrugs.diary.privacy.capture_off")));
        lines.add(DiaryLine.text(tr(config.isMemoryPreviewEnabled()
                ? "screen.mydrugs.diary.privacy.preview_on"
                : "screen.mydrugs.diary.privacy.preview_off")));
        lines.addAll(wrapToLines(tr("screen.mydrugs.diary.privacy.delete_local")));
        config.setMemoryPrivacyNoticeSeen(true);
        return new PrivacyPage(Component.translatable("screen.mydrugs.diary.privacy.title"), lines);
    }

    private Component memoryCaptureLabel() {
        return Component.translatable(MyDrugsClientConfig.get().isMemoryCaptureEnabled()
                ? "screen.mydrugs.diary.privacy.capture_on"
                : "screen.mydrugs.diary.privacy.capture_off");
    }

    private Component memoryPreviewLabel() {
        return Component.translatable(MyDrugsClientConfig.get().isMemoryPreviewEnabled()
                ? "screen.mydrugs.diary.privacy.preview_on"
                : "screen.mydrugs.diary.privacy.preview_off");
    }

    private DiaryPage buildIntegrationPage(DiaryIntegrationProgressDto p) {
        List<DiaryLine> lines = new ArrayList<>();
        DrugId id = DrugId.bySerializedNameOrNull(p.drugId());
        ItemStack icon = id == null ? ItemStack.EMPTY : DrugIconHelper.stackFor(id);
        String traitName = p.traitKey().isBlank() ? "-" : tr(p.traitKey());
        lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.integration.trait_header", traitName)));
        lines.add(DiaryLine.withIcon(icon, tr("screen.mydrugs.diary.integration.drug", prettyDrug(p.drugId()))));
        lines.add(DiaryLine.text(tr("screen.mydrugs.diary.integration.status", integrationStatus(p))));
        if (!p.roleplayKey().isBlank()) {
            addWrapped(lines, tr(p.roleplayKey()), DiaryLine.Kind.HINT, DiaryLine.Visibility.ALWAYS);
        }
        lines.add(DiaryLine.spacer());
        String reward = p.rewardKey().isBlank() ? "-" : tr(p.rewardKey());
        lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.integration.reward")));
        addWrapped(lines, traitName + ": " + reward, DiaryLine.Kind.TEXT, DiaryLine.Visibility.ALWAYS);
        lines.add(DiaryLine.spacer());
        lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.integration.next_step")));
        addWrapped(lines, integrationNextStep(p), DiaryLine.Kind.HINT, DiaryLine.Visibility.ALWAYS);
        lines.add(DiaryLine.spacer());
        lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.integration.checklist")));
        lines.add(DiaryLine.text(statusLine(p.knowledgeUnlocked(), tr("screen.mydrugs.diary.integration.knowledge"))));
        if ("CLEAN_PSYCHEDELIC_STREAK".equals(p.requirementType())) {
            lines.add(DiaryLine.text(statusLine(p.cleanDoseStreakMet(),
                    tr("screen.mydrugs.diary.integration.clean_doses",
                            p.cleanDoseStreak(), p.cleanDoseStreakRequired()))));
            lines.add(DiaryLine.text(statusLine(p.cleanSpacingReady(), spacingDiaryLine(p))));
            lines.add(DiaryLine.text(statusLine(p.psychedelicReflectionMet(),
                    tr("screen.mydrugs.diary.integration.reflection",
                            p.psychedelicReflections(), p.psychedelicReflectionsRequired()))));
            lines.add(DiaryLine.text(statusLine(p.safePsychedelicUseMet(),
                    tr("screen.mydrugs.diary.integration.safe_use",
                            p.safePsychedelicUses(), p.safePsychedelicUsesRequired()))));
            lines.add(DiaryLine.text(statusLine(p.noRecentBadTripMet(), badTripDiaryLine(p))));
        } else {
            lines.add(DiaryLine.text(statusLine(p.peakMet(),
                    tr("screen.mydrugs.diary.integration.peak",
                            formatOneDecimal(p.peakCurrent()), formatOneDecimal(p.peakRequired())))));
            lines.add(DiaryLine.text(statusLine(p.lowAddictionMet(),
                    tr("screen.mydrugs.diary.integration.current_addiction",
                            formatOneDecimal(p.addictionCurrent()), formatOneDecimal(p.addictionMax())))));
            lines.add(DiaryLine.text(statusLine(p.lifetimeDoseMet(),
                    tr("screen.mydrugs.diary.integration.lifetime",
                            formatOneDecimal(p.lifetimeDose()), formatOneDecimal(p.lifetimeDoseRequired())))));
            lines.add(DiaryLine.text(statusLine(p.recoveryMet(),
                    tr("screen.mydrugs.diary.integration.recovery",
                            formatPercent(progressRatio(p.recoveryProgress(), p.recoveryRequired()))))));
        }
        lines.add(DiaryLine.text(statusLine(p.diaryContext(), tr("screen.mydrugs.diary.integration.diary"))));
        lines.add(DiaryLine.text(statusLine(p.recoveryRoom(), tr("screen.mydrugs.diary.integration.room"))));
        lines.add(DiaryLine.withIcon(stackForItemId(p.materialItemId()),
                statusLine(p.materialInInventory(), tr("screen.mydrugs.diary.integration.material",
                        itemName(p.materialItemId())))));
        lines.add(DiaryLine.withIcon(stackForItemId("mydrugs:integration_core"),
                statusLine(p.coreSufficient(), tr("screen.mydrugs.diary.integration.core_tier",
                        coreTierName(p.requiredCoreTierId()), coreTierName(p.bestCoreTierId())))));
        lines.add(DiaryLine.text(statusLine(!p.alreadyIntegrated(), tr("screen.mydrugs.diary.integration.not_integrated"))));
        if (p.alreadyIntegrated()) {
            lines.add(DiaryLine.text(statusLine(true, tr("screen.mydrugs.diary.integration.integrated"))));
        }

        return new BasicPage(Component.translatable("screen.mydrugs.diary.integration.title", prettyDrug(p.drugId())), lines);
    }

    private DiaryPage buildIntegrationOverviewPage() {
        List<DiaryLine> lines = new ArrayList<>();
        for (DiaryIntegrationProgressDto p : snapshot.integrationProgress()) {
            String trait = p.traitKey().isBlank() ? "?" : tr(p.traitKey());
            lines.add(DiaryLine.text(tr("screen.mydrugs.diary.integration.overview_line",
                    trait, prettyDrug(p.drugId()), integrationStatus(p))));
            if (!p.alreadyIntegrated()) {
                lines.add(DiaryLine.indented(integrationNextStep(p)));
            }
        }
        return new BasicPage(Component.translatable("screen.mydrugs.diary.integration.overview"), lines);
    }

    private String integrationStatus(DiaryIntegrationProgressDto p) {
        if (p.alreadyIntegrated()) return tr("screen.mydrugs.diary.integration.status.integrated");
        if (integrationReady(p)) return tr("screen.mydrugs.diary.integration.status.ready");
        return tr("screen.mydrugs.diary.integration.status.blocked");
    }

    private boolean integrationReady(DiaryIntegrationProgressDto p) {
        boolean experience = "CLEAN_PSYCHEDELIC_STREAK".equals(p.requirementType())
                ? p.cleanDoseStreakMet() && p.cleanSpacingReady() && p.psychedelicReflectionMet()
                && p.safePsychedelicUseMet() && p.noRecentBadTripMet()
                : p.peakMet() && p.lowAddictionMet() && p.lifetimeDoseMet() && p.recoveryMet();
        return p.knowledgeUnlocked() && experience && p.diaryContext() && p.recoveryRoom()
                && p.materialInInventory() && p.coreSufficient() && !p.alreadyIntegrated();
    }

    private String integrationNextStep(DiaryIntegrationProgressDto p) {
        if (p.alreadyIntegrated()) return tr("screen.mydrugs.diary.integration.next.integrated");
        if (!p.knowledgeUnlocked()) return tr("screen.mydrugs.diary.integration.next.knowledge");
        if (!p.peakMet() || !p.lifetimeDoseMet() || !p.cleanDoseStreakMet()) return tr("screen.mydrugs.diary.integration.next.experience");
        if (!p.lowAddictionMet()) return tr("screen.mydrugs.diary.integration.next.addiction");
        if (!p.recoveryMet()) return tr("screen.mydrugs.diary.integration.next.recovery");
        if (!p.cleanSpacingReady()) return tr("screen.mydrugs.diary.integration.next.spacing");
        if (!p.psychedelicReflectionMet()) return tr("screen.mydrugs.diary.integration.next.reflection");
        if (!p.safePsychedelicUseMet()) return tr("screen.mydrugs.diary.integration.next.safe_setting");
        if (!p.noRecentBadTripMet()) return tr("screen.mydrugs.diary.integration.next.bad_trip");
        if (!p.materialInInventory()) return tr("screen.mydrugs.diary.integration.next.material");
        if (!p.coreSufficient()) return tr("screen.mydrugs.diary.integration.next.core");
        if (!p.diaryContext()) return tr("screen.mydrugs.diary.integration.next.diary");
        if (!p.recoveryRoom()) return tr("screen.mydrugs.diary.integration.next.room");
        return tr("screen.mydrugs.diary.integration.next.ready");
    }

    private void addWarning(List<DiaryLine> lines, DiaryWarning warning) {
        String severityKey = switch (warning.severity()) {
            case 3 -> "screen.mydrugs.diary.warning.critical";
            case 2 -> "screen.mydrugs.diary.warning.warning";
            default -> "screen.mydrugs.diary.warning.notice";
        };
        addWrappedWithIcon(lines, tr(severityKey) + ": " + tr(warning.textKey()), stackForItemId(warning.iconItemId()),
                DiaryLine.Kind.WARNING, DiaryLine.Visibility.ALWAYS);
        if (!warning.clearHintKey().isBlank()) {
            addWrapped(lines, tr("screen.mydrugs.diary.hint_line", tr(warning.clearHintKey())),
                    DiaryLine.Kind.HINT, DiaryLine.Visibility.SHIFT);
        }
    }

    private void addBreadcrumb(List<DiaryLine> lines, DiaryBreadcrumb breadcrumb) {
        addWrappedWithIcon(lines, tr(breadcrumb.textKey()), stackForItemId(breadcrumb.iconItemId()),
                DiaryLine.Kind.TEXT, DiaryLine.Visibility.ALWAYS);
        if (!breadcrumb.clearHintKey().isBlank()) {
            addWrapped(lines, tr("screen.mydrugs.diary.hint_line", tr(breadcrumb.clearHintKey())),
                    DiaryLine.Kind.HINT, DiaryLine.Visibility.SHIFT);
        }
        if (!breadcrumb.guideTarget().isBlank()) {
            addWrapped(lines, tr("screen.mydrugs.diary.guide_target", breadcrumb.guideTarget()),
                    DiaryLine.Kind.HINT, DiaryLine.Visibility.SHIFT);
        }
        if (!breadcrumb.explicitHintKey().isBlank()) {
            addWrapped(lines, tr("screen.mydrugs.diary.diagnostic_line", tr(breadcrumb.explicitHintKey())),
                    DiaryLine.Kind.HINT, DiaryLine.Visibility.DEBUG);
        }
    }

    private void addBlocker(List<DiaryLine> lines, DiaryBlocker blocker) {
        addWrapped(lines, tr(blocker.textKey()), DiaryLine.Kind.WARNING, DiaryLine.Visibility.ALWAYS);
        DiaryBlockerRoutes.Route route = DiaryBlockerRoutes.fromBlockerType(blocker.type());
        if (route != null) {
            addWrappedWithIcon(lines, tr(route.routeTextKey()), stackForItemId(route.iconItemId()),
                    DiaryLine.Kind.HINT, DiaryLine.Visibility.ALWAYS);
            lines.add(DiaryLine.guideLink(
                    tr("screen.mydrugs.diary.open_guide_page", route.guidePage()),
                    route.guidePage()
            ));
        }
        if (!blocker.clearHintKey().isBlank()) {
            addWrapped(lines, tr("screen.mydrugs.diary.hint_line", tr(blocker.clearHintKey())),
                    DiaryLine.Kind.HINT, DiaryLine.Visibility.SHIFT);
        }
        if (!blocker.explicitHintKey().isBlank()) {
            addWrapped(lines, tr("screen.mydrugs.diary.diagnostic_line", tr(blocker.explicitHintKey())),
                    DiaryLine.Kind.HINT, blocker.count() >= 3 ? DiaryLine.Visibility.SHIFT : DiaryLine.Visibility.DEBUG);
        }
    }

    private void addMemory(List<DiaryLine> lines, DiaryMemory memory) {
        String title = memory.titleKey().isBlank() ? "" : tr(memory.titleKey());
        String body = memory.textKey().isBlank() ? title : tr(memory.textKey());
        String text = tr("screen.mydrugs.diary.memory_line", memory.day(), body);
        addWrappedWithIcon(lines, text, stackForItemId(memory.iconItemId()), DiaryLine.Kind.TEXT, DiaryLine.Visibility.ALWAYS);
    }

    private DayPage buildDayPage(long day, List<DiaryEntryDto> entries, boolean isToday) {
        List<DiaryLine> lines = new ArrayList<>();
        if (entries.isEmpty() && isToday) {
            lines.add(DiaryLine.text(Component.translatable("screen.mydrugs.diary.empty_today").getString()));
        } else {
            int idx = 1;
            for (DiaryEntryDto e : entries) {
                lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.entry_heading", idx, entryLabel(e))));
                for (DiaryLine wl : wrapToLines(e.content())) {
                    lines.add(wl);
                }
                String sk = e.sourceKey();
                if (sk != null && sk.startsWith("memory:")) {
                    lines.add(DiaryLine.link(tr("screen.mydrugs.diary.see_in_mind_map"), sk.substring("memory:".length())));
                }
                lines.add(DiaryLine.spacer());
                idx++;
            }
        }
        return new DayPage(Component.translatable("screen.mydrugs.diary.day", day), lines, isToday);
    }

    private String entryLabel(DiaryEntryDto entry) {
        if ("CUSTOM".equals(entry.type())) {
            return tr("screen.mydrugs.diary.entry_label.custom");
        }
        String source = entry.sourceKey() == null ? "" : entry.sourceKey();
        if (source.startsWith("memory:")) return tr("screen.mydrugs.diary.entry_label.memory");
        return switch (source) {
            case "overdose" -> tr("screen.mydrugs.diary.entry_label.critical_body_warning");
            case "bad_trip" -> tr("screen.mydrugs.diary.entry_label.bad_trip");
            case "severe_withdrawal" -> tr("screen.mydrugs.diary.entry_label.severe_withdrawal");
            case "moderate_withdrawal" -> tr("screen.mydrugs.diary.entry_label.withdrawal");
            case "very_high_dose", "high_dose" -> tr("screen.mydrugs.diary.entry_label.dose");
            case "high_stress" -> tr("screen.mydrugs.diary.entry_label.stress");
            case "insomnia" -> tr("screen.mydrugs.diary.entry_label.sleep");
            case "recovery_support" -> tr("screen.mydrugs.diary.entry_label.recovery");
            case "calm_day" -> tr("screen.mydrugs.diary.entry_label.calm_day");
            default -> tr("screen.mydrugs.diary.entry_label.automatic");
        };
    }

    private List<DiaryLine> wrapToLines(String text) {
        return wrapToLines(text, DiaryLine.Kind.TEXT, DiaryLine.Visibility.ALWAYS);
    }

    private List<DiaryLine> wrapToLines(String text, DiaryLine.Kind kind, DiaryLine.Visibility visibility) {
        List<DiaryLine> out = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            out.add(DiaryLine.of("", ItemStack.EMPTY, null, kind, visibility));
            return out;
        }
        int maxWidth = contentRight - contentLeft - 6;
        Font f = this.font;
        for (String paragraph : text.split("\\R")) {
            StringBuilder current = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String trial = current.length() == 0 ? word : current + " " + word;
                if (f.width(trial) > maxWidth && current.length() > 0) {
                    out.add(DiaryLine.of(current.toString(), ItemStack.EMPTY, null, kind, visibility));
                    current.setLength(0);
                    current.append(word);
                } else {
                    if (current.length() > 0) current.append(' ');
                    current.append(word);
                }
            }
            if (current.length() > 0) {
                out.add(DiaryLine.of(current.toString(), ItemStack.EMPTY, null, kind, visibility));
            }
        }
        return out;
    }

    private void addWrapped(List<DiaryLine> target, String text, DiaryLine.Kind kind, DiaryLine.Visibility visibility) {
        target.addAll(wrapToLines(text, kind, visibility));
    }

    private void addWrappedWithIcon(List<DiaryLine> target, String text, ItemStack icon,
                                    DiaryLine.Kind kind, DiaryLine.Visibility visibility) {
        List<DiaryLine> wrapped = wrapToLines(text, kind, visibility);
        for (int i = 0; i < wrapped.size(); i++) {
            DiaryLine line = wrapped.get(i);
            target.add(i == 0
                    ? DiaryLine.of(line.text, icon, null, kind, visibility)
                    : DiaryLine.of(line.text, ItemStack.EMPTY, null, DiaryLine.Kind.INDENTED, visibility));
        }
    }

    private String tr(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }

    private ItemStack stackForItemId(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return ItemStack.EMPTY;
        }
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return ItemStack.EMPTY;
        }
        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item);
    }

    private String statusLine(boolean ok, String text) {
        return (ok ? "[x] " : "[ ] ") + text;
    }

    private void appendSanctuaryModuleSection(List<DiaryLine> lines) {
        if (snapshot.sanctuaryModuleKeys().isEmpty() && snapshot.sanctuarySuggestionKeys().isEmpty()) {
            return;
        }

        lines.add(DiaryLine.spacer());
        lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.sanctuary_modules")));
        if (snapshot.sanctuaryModuleKeys().isEmpty()) {
            addWrapped(lines, tr("screen.mydrugs.diary.sanctuary_no_modules"),
                    DiaryLine.Kind.TEXT, DiaryLine.Visibility.ALWAYS);
        } else {
            for (String key : snapshot.sanctuaryModuleKeys()) {
                addWrappedReason(lines, tr(key));
            }
        }

        if (!snapshot.sanctuarySuggestionKeys().isEmpty()) {
            lines.add(DiaryLine.heading(tr("screen.mydrugs.diary.sanctuary_suggestions")));
            for (String key : snapshot.sanctuarySuggestionKeys()) {
                addWrapped(lines, "- " + tr(key), DiaryLine.Kind.HINT, DiaryLine.Visibility.ALWAYS);
            }
        }
    }

    private float progressRatio(float value, float required) {
        if (required <= 0.0F) {
            return 1.0F;
        }
        return Math.min(1.0F, Math.max(0.0F, value / required));
    }

    private String itemName(String itemId) {
        ItemStack stack = stackForItemId(itemId);
        return stack.isEmpty() ? "-" : stack.getHoverName().getString();
    }

    private String coreTierName(String tierId) {
        if (tierId == null || tierId.isBlank()) {
            return tr("screen.mydrugs.diary.integration.core_none");
        }
        return tr("integration.mydrugs.core_tier." + tierId);
    }

    private String spacingDiaryLine(DiaryIntegrationProgressDto p) {
        if (p.cleanSpacingRemainingTicks() <= 0) {
            return tr("screen.mydrugs.diary.integration.spacing_ready");
        }
        return tr("screen.mydrugs.diary.integration.spacing_wait", formatDuration(p.cleanSpacingRemainingTicks()));
    }

    private String badTripDiaryLine(DiaryIntegrationProgressDto p) {
        if (p.recentBadTripRemainingTicks() <= 0) {
            return tr("screen.mydrugs.diary.integration.bad_trip_clear");
        }
        return tr("screen.mydrugs.diary.integration.bad_trip_wait", formatDuration(p.recentBadTripRemainingTicks()));
    }

    // ----------------------- Helpers (formatting) -----------------------
    private String bodyStateSummary(DiaryPlayerStateDto s) {
        StringBuilder builder = new StringBuilder();
        builder.append("Now: WD ")
                .append(compactBand(s.globalSeverity()))
                .append(' ')
                .append(withdrawalArrow(s))
                .append(" | ST ")
                .append(compactBand(s.stress()))
                .append(' ')
                .append(AddictionClientState.trendArrowForStress());

        String dose = compactDose(s.doseState());
        if (!dose.isBlank()) {
            builder.append(" | ").append(dose);
        }

        String chips = compactRecoveryChips(s.recoveryFlags());
        if (!chips.isBlank()) {
            builder.append(" | ").append(chips);
        }
        return builder.toString();
    }

    private String compactBand(float value) {
        if (value >= 0.70F) return "High";
        if (value >= 0.35F) return "Med";
        if (value >= 0.10F) return "Low";
        return "None";
    }

    private String withdrawalArrow(DiaryPlayerStateDto s) {
        WithdrawalPhase phase = WithdrawalPhase.byNetworkId(s.withdrawalPhase());
        return switch (phase) {
            case RISING -> "↑";
            case EASING, SETTLING -> "↓";
            case PEAK -> "→";
            case NONE -> AddictionClientState.trendArrowForWithdrawal();
        };
    }

    private String compactDose(String state) {
        return switch (state) {
            case "HIGH", "DRUNK" -> "Dose H";
            case "VERY_HIGH", "VERY_DRUNK" -> "Dose VH";
            case "OVERDOSE", "ETHYLIC_COMA" -> "Dose OD";
            default -> "";
        };
    }

    private String compactRecoveryChips(int flags) {
        List<String> chips = new ArrayList<>();
        if ((flags & AddictionClientSnapshotPayload.RECOVERY_SAFE_ZONE) != 0) chips.add("+Room");
        if ((flags & AddictionClientSnapshotPayload.RECOVERY_DIARY) != 0) chips.add("+Diary");
        if ((flags & AddictionClientSnapshotPayload.RECOVERY_HEADPHONES) != 0) chips.add("+Music");
        if ((flags & AddictionClientSnapshotPayload.RECOVERY_PREPARED_TEA) != 0) chips.add("+Tea");
        if ((flags & AddictionClientSnapshotPayload.RECOVERY_SLEEP_BONUS) != 0) chips.add("+Sleep");
        if ((flags & AddictionClientSnapshotPayload.RECOVERY_CALMING_MIXTURE) != 0) chips.add("+Calm");
        return String.join(" ", chips);
    }

    private String mainRiskLabel(DiaryPlayerStateDto s) {
        return switch (AddictionDangerReason.byNetworkId(s.primaryDangerReason())) {
            case OVERDOSE -> "overdose";
            case BAD_TRIP -> "bad trip";
            case VERY_HIGH_DOSE, HIGH_DOSE -> "dose too high";
            case HUNGER -> "hunger";
            case LOW_HEALTH -> "low health";
            case WITHDRAWAL_PEAK -> "withdrawal peak";
            case HIGH_STRESS -> "high stress";
            case SLEEP_BLOCKED -> "sleep blocked";
            case ISOLATION -> "isolation";
            case UNSAFE_PLACE -> "unsafe place";
            case NONE -> "none";
        };
    }

    private String suggestedActionLabel(DiaryPlayerStateDto s) {
        return switch (AddictionSuggestedAction.byNetworkId(s.suggestedAction())) {
            case USE_ANTIDOTE -> "use antidote";
            case STOP_DOSING -> "stop dosing";
            case EAT -> "eat";
            case HEAL -> "heal";
            case GET_SAFE -> "get safe";
            case STAY_SAFE -> "stay safe";
            case WRITE_DIARY -> "write";
            case USE_HEADPHONES -> "music";
            case DRINK_TEA -> "drink tea";
            case SLEEP_LATER -> "sleep later";
            case WAIT -> "wait";
            case GROUND -> "ground";
            case NONE -> "keep steady";
        };
    }

    private void appendFeelingReasonSections(List<DiaryLine> lines, DiaryPlayerStateDto s) {
        List<DiaryLine> good = new ArrayList<>();
        List<DiaryLine> bad = new ArrayList<>();

        appendRecoveryGoodReasons(good, s);
        appendGoodDrugEffectReasons(good);
        appendBadStateReasons(bad, s);
        appendBadDrugEffectReasons(bad);
        appendHudSymptomReasons(bad);

        lines.add(DiaryLine.spacer());
        lines.add(DiaryLine.heading("Why I feel good"));
        if (good.isEmpty()) {
            lines.add(DiaryLine.text("- No clear positive effect is active."));
        } else {
            lines.addAll(good);
        }

        lines.add(DiaryLine.spacer());
        lines.add(DiaryLine.heading("Why I feel bad"));
        if (bad.isEmpty()) {
            lines.add(DiaryLine.text("- No clear negative effect is active."));
        } else {
            lines.addAll(bad);
        }
    }

    private void appendRecoveryGoodReasons(List<DiaryLine> good, DiaryPlayerStateDto s) {
        if ((s.recoveryFlags() & AddictionClientSnapshotPayload.RECOVERY_DIARY) != 0) {
            addWrappedReason(good, "+Diary: writing anchored me.");
        }
        if ((s.recoveryFlags() & AddictionClientSnapshotPayload.RECOVERY_HEADPHONES) != 0) {
            addWrappedReason(good, "+Music: rhythm is lowering pressure.");
        }
        if ((s.recoveryFlags() & AddictionClientSnapshotPayload.RECOVERY_CALMING_MIXTURE) != 0) {
            addWrappedReason(good, "+Calm: my body is settling.");
        }
        if ((s.recoveryFlags() & AddictionClientSnapshotPayload.RECOVERY_SLEEP_BONUS) != 0) {
            addWrappedReason(good, "+Sleep: rest is still helping.");
        }
        if ((s.recoveryFlags() & AddictionClientSnapshotPayload.RECOVERY_PREPARED_TEA) != 0) {
            addWrappedReason(good, "+Tea: stress is rising more slowly.");
        }
        if ((s.recoveryFlags() & AddictionClientSnapshotPayload.RECOVERY_SAFE_ZONE) != 0) {
            addWrappedReason(good, "+Room: this place makes recovery easier.");
        }
    }

    private void appendGoodDrugEffectReasons(List<DiaryLine> good) {
        appendDrugEffectReasons(good, EffectPolarity.BENEFICIAL);
    }

    private void appendBadStateReasons(List<DiaryLine> bad, DiaryPlayerStateDto s) {
        AddictionDangerReason reason = AddictionDangerReason.byNetworkId(s.primaryDangerReason());
        if (reason != AddictionDangerReason.NONE) {
            addWrappedReason(bad, "Main risk: " + mainRiskLabel(s) + ". Next: " + suggestedActionLabel(s) + ".");
        }
        if (s.globalSeverity() > 0.15F) {
            addWrappedReason(bad, "WD: " + formatPercent(s.globalSeverity()) + AddictionClientState.trendArrowForWithdrawal() + " - recovery pressure is active.");
        }
        if (s.badTripActive()) {
            addWrappedReason(bad, "BT: " + formatPercent(s.badTripSeverity()) + AddictionClientState.trendArrowForBadTrip() + " - ground and get safe.");
        }
        if (s.overdoseTimerTicks() > 0) {
            addWrappedReason(bad, "OD: " + ((s.overdoseTimerTicks() + 19) / 20) + "s - " + suggestedActionLabel(s) + ".");
        }
        if (s.sleepBlocked()) {
            addWrappedReason(bad, "Sleep blocked: calm first.");
        }
        if (!s.doseState().isEmpty() && !"NORMAL".equals(s.doseState())) {
            addWrappedReason(bad, compactDose(s.doseState()) + ": wait before pushing further.");
        }
    }

    private void appendBadDrugEffectReasons(List<DiaryLine> bad) {
        appendDrugEffectReasons(bad, EffectPolarity.HARMFUL);
    }

    private void appendHudSymptomReasons(List<DiaryLine> bad) {
        for (HudSymptomIcons.HudSymptomIcon icon : HudSymptomIcons.LIST) {
            float intensity = icon.intensity();
            if (intensity <= HudSymptomIcons.MIN_VISIBLE) continue;
            addWrappedReason(bad, icon.label() + ": " + formatPercent(Math.min(1.0F, intensity)) + ".");
        }
    }

    private void appendDrugEffectReasons(List<DiaryLine> target, EffectPolarity polarity) {
        for (EffectType type : EffectType.values()) {
            if (type.polarity() != polarity || !type.hasDiaryReason()) {
                continue;
            }
            float intensity = effectIntensity(type);
            if (!isVisibleEffect(intensity)) {
                continue;
            }
            if (type == EffectType.HP_DECREASE) {
                addWrappedReason(target, type.diaryLabel() + ": max health is lower by " + formatOneDecimal(intensity) + " hearts.");
                continue;
            }
            String formatted = polarity == EffectPolarity.BENEFICIAL
                    ? formatSignedPercent(intensity)
                    : formatPercent(Math.min(1.0F, intensity));
            addWrappedReason(target, type.diaryLabel() + ": " + formatted + " - " + type.diaryExplanation() + ".");
        }
    }

    private float effectIntensity(EffectType type) {
        return AddictionClientState.getEffectIntensity(type);
    }

    private boolean isVisibleEffect(float intensity) {
        return intensity > 0.015F;
    }

    private void addWrappedReason(List<DiaryLine> target, String text) {
        target.addAll(wrapToLines("- " + text));
    }

    private String formatSignedPercent(float value) {
        return "+" + Math.round(value * 100.0F) + "%";
    }

    private String formatPercent(float value) {
        return Math.round(value * 100.0F) + "%";
    }

    private String formatOneDecimal(float value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private String formatDuration(int ticks) {
        int seconds = Math.max(1, (ticks + 19) / 20);
        if (seconds < 90) {
            return seconds + "s";
        }
        int minutes = Math.max(1, (seconds + 59) / 60);
        return minutes + "m";
    }

    private String stressLine(float stress) {
        int pct = Math.round(stress * 100.0F);
        String tail;
        if (pct >= 80) tail = " - I feel close to the edge.";
        else if (pct >= 60) tail = " - The pressure shows.";
        else if (pct >= 35) tail = " - There is a steady weight.";
        else tail = " - I can breathe.";
        return "Stress: " + pct + "%" + tail;
    }

    private String withdrawalLine(float severity) {
        int pct = Math.round(severity * 100.0F);
        String tail;
        if (pct >= 70) tail = " - my body is asking for something.";
        else if (pct >= 35) tail = " - the lack hums under everything.";
        else tail = " - I am steady enough.";
        return "Withdrawal: " + pct + "%" + tail;
    }

    private String prettyDrug(String id) {
        if (id == null || id.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        boolean upper = true;
        for (char c : id.toCharArray()) {
            if (c == '_') { sb.append(' '); upper = true; continue; }
            sb.append(upper ? Character.toUpperCase(c) : c);
            upper = false;
        }
        return sb.toString();
    }

    private String prettyCategory(String name) {
        try {
            DrugCategory cat = DrugCategory.valueOf(name);
            return prettyDrug(cat.name().toLowerCase(Locale.ROOT));
        } catch (Exception e) {
            return prettyDrug(name.toLowerCase(Locale.ROOT));
        }
    }

    private String prettyDose(String state) {
        return switch (state) {
            case "HIGH" -> "High";
            case "VERY_HIGH" -> "Very high";
            case "OVERDOSE" -> "Overdose";
            case "DRUNK" -> "Drunk";
            case "VERY_DRUNK" -> "Very drunk";
            case "ETHYLIC_COMA" -> "Ethylic coma";
            default -> "Normal";
        };
    }

    private String toleranceLine(float tolerance) {
        if (tolerance >= 0.60F) {
            return "Tolerance: high - weaker effect, risk remains";
        }
        if (tolerance >= 0.25F) {
            return "Tolerance: medium";
        }
        return "Tolerance: low";
    }

    private String prettyRecipeId(String full) {
        if (full == null || full.isEmpty()) return "ritual";
        int colon = full.indexOf(':');
        String path = colon >= 0 ? full.substring(colon + 1) : full;
        int slash = path.lastIndexOf('/');
        if (slash >= 0) path = path.substring(slash + 1);
        return prettyDrug(path);
    }

    private String lifetimeSentence(String drugIdSerialized, float lifetimeDose) {
        int rounded = Math.max(1, Math.round(lifetimeDose));
        String name = prettyDrug(drugIdSerialized).toLowerCase(Locale.ROOT);
        return switch (drugIdSerialized.toLowerCase(Locale.ROOT)) {
            case "weed", "hash" -> "I took " + rounded + " doses of " + name + ". Not something to be proud of, but I survived.";
            case "tobacco" -> "I smoked " + rounded + " doses of tobacco. The habit left marks even when I ignored them.";
            case "coffee" -> "I drank " + rounded + " doses of coffee. My hands remember every late night.";
            case "alcohol" -> "I drank " + rounded + " doses of alcohol. Some courage was borrowed, and some of it cost me.";
            case "cocaine" -> "I pushed through " + rounded + " doses of cocaine. Speed helped for a moment, then asked for payment.";
            case "crack" -> "I survived " + rounded + " doses of crack. That sentence feels heavier than the number.";
            case "meth" -> "I forced myself through " + rounded + " doses of meth. My body kept the score.";
            case "lsd" -> "I crossed " + rounded + " doses of LSD. I saw things, but not all of them helped me understand.";
            case "mushrooms" -> "I took " + rounded + " doses of mushrooms. Some visions were lessons. Some were only storms.";
            case "mdma" -> "I leaned on " + rounded + " doses of MDMA. The closeness was real, the morning after also.";
            case "dmt" -> "I crossed " + rounded + " doses of DMT. I came back with more questions than answers.";
            case "salvia" -> "I tried " + rounded + " doses of salvia. The room never quite stayed a room.";
            case "heroin", "morphine", "fentanyl", "opium" -> "I took " + rounded + " doses of " + name + ". The quiet it promised was never free.";
            case "ketamine" -> "I floated through " + rounded + " doses of ketamine. Distance can feel safe; it is rarely free.";
            case "pcp" -> "I went through " + rounded + " doses of PCP. The edges of fear got blurry, and so did everything else.";
            case "dxm" -> "I rode " + rounded + " doses of DXM. The world became a long hallway for a while.";
            case "benzodiazepine" -> "I used " + rounded + " doses of benzodiazepine. Numbness can look like peace from far away.";
            case "barbiturate" -> "I used " + rounded + " doses of barbiturate. Some quiets are too final to court.";
            case "nitrous_oxide" -> "I inhaled " + rounded + " doses of nitrous oxide. A short escape still leaves a trace.";
            default -> "I took " + rounded + " doses of " + name + ".";
        };
    }

    // ----------------------- Page records -----------------------

    private interface DiaryPage {
        Component title();
        List<DiaryLine> lines();
    }

    private record BasicPage(Component title, List<DiaryLine> lines) implements DiaryPage {
    }

    private record PrivacyPage(Component title, List<DiaryLine> lines) implements DiaryPage {
    }

    private static final class MindMapPageHolder implements DiaryPage {
        private final Component title;
        final MindMapPage page;
        private final List<DiaryLine> lines = List.of();

        MindMapPageHolder(Component title, MindMapPage page) {
            this.title = title;
            this.page = page;
        }

        @Override public Component title() { return title; }
        @Override public List<DiaryLine> lines() { return lines; }
    }

    private static final class DayPage implements DiaryPage {
        private final Component title;
        private final List<DiaryLine> lines;
        final boolean isToday;

        DayPage(Component title, List<DiaryLine> lines, boolean isToday) {
            this.title = title;
            this.lines = lines;
            this.isToday = isToday;
        }

        @Override public Component title() { return title; }
        @Override public List<DiaryLine> lines() { return lines; }
    }

    /** A single rendered line; optional icon (item-stack), optional texture icon, optional heading style. */
    private static final class DiaryLine {
        enum Kind { TEXT, HEADING, SPACER, INDENTED, WARNING, HINT, SYMPTOM_METER, LINK, GUIDE_LINK }
        enum Visibility { ALWAYS, SHIFT, DEBUG }
        final String text;
        final ItemStack icon;
        final ResourceLocation iconTexture;
        final Kind kind;
        final Visibility visibility;
        final float intensity;     // 0..1 for SYMPTOM_METER
        final String tail;         // right-aligned suffix text (e.g. "73%")
        /** Memory-map node id for LINK lines, empty otherwise. */
        final String linkTarget;

        private DiaryLine(String text, ItemStack icon, ResourceLocation tex, Kind kind, Visibility visibility,
                          float intensity, String tail, String linkTarget) {
            this.text = text == null ? "" : text;
            this.icon = icon == null ? ItemStack.EMPTY : icon;
            this.iconTexture = tex;
            this.kind = kind == null ? Kind.TEXT : kind;
            this.visibility = visibility == null ? Visibility.ALWAYS : visibility;
            this.intensity = intensity;
            this.tail = tail == null ? "" : tail;
            this.linkTarget = linkTarget == null ? "" : linkTarget;
        }

        static DiaryLine of(String s, ItemStack stack, ResourceLocation tex, Kind kind, Visibility visibility) {
            return new DiaryLine(s, stack, tex, kind, visibility, 0F, "", "");
        }
        static DiaryLine text(String s)        { return new DiaryLine(s, ItemStack.EMPTY, null, Kind.TEXT, Visibility.ALWAYS, 0F, "", ""); }
        static DiaryLine indented(String s)    { return new DiaryLine(s, ItemStack.EMPTY, null, Kind.INDENTED, Visibility.ALWAYS, 0F, "", ""); }
        static DiaryLine heading(String s)     { return new DiaryLine(s, ItemStack.EMPTY, null, Kind.HEADING, Visibility.ALWAYS, 0F, "", ""); }
        static DiaryLine spacer()              { return new DiaryLine("", ItemStack.EMPTY, null, Kind.SPACER, Visibility.ALWAYS, 0F, "", ""); }
        static DiaryLine withIcon(ItemStack stack, String s) {
            return new DiaryLine(s, stack, null, Kind.TEXT, Visibility.ALWAYS, 0F, "", "");
        }
        static DiaryLine withTexture(ResourceLocation tex, String s) {
            return new DiaryLine(s, ItemStack.EMPTY, tex, Kind.TEXT, Visibility.ALWAYS, 0F, "", "");
        }
        static DiaryLine withSymptomMeter(ResourceLocation tex, String label, float intensity, String tail) {
            return new DiaryLine(label, ItemStack.EMPTY, tex, Kind.SYMPTOM_METER, Visibility.ALWAYS, intensity, tail, "");
        }
        /** A clickable link line that, when clicked, jumps the diary to the mind-map page. */
        static DiaryLine link(String label, String targetNodeId) {
            return new DiaryLine(label, ItemStack.EMPTY, null, Kind.LINK, Visibility.ALWAYS, 0F, "", targetNodeId);
        }
        static DiaryLine guideLink(String label, String targetPage) {
            return new DiaryLine(label, ItemStack.EMPTY, null, Kind.GUIDE_LINK, Visibility.ALWAYS, 0F, "", targetPage);
        }

        boolean visible(boolean showDetails, boolean showDebug) {
            return switch (visibility) {
                case ALWAYS -> true;
                case SHIFT -> showDetails || showDebug;
                case DEBUG -> showDebug;
            };
        }

        void render(GuiGraphics g, Font font, int x, int xRight, int y) {
            switch (kind) {
                case SPACER -> { /* nothing */ }
                case HEADING -> {
                    g.drawString(font, text, x, y, INK_HIGHLIGHT, false);
                    g.fill(x, y + 9, x + Math.min(60, font.width(text)), y + 10, DIVIDER);
                }
                case INDENTED -> g.drawString(font, text, x + ICON_SIZE + ICON_GAP, y, INK, false);
                case WARNING, HINT, TEXT -> {
                    int textX = x;
                    if (!icon.isEmpty()) {
                        g.renderFakeItem(icon, x, y - 3);
                        textX += ICON_SIZE + ICON_GAP;
                    } else if (iconTexture != null) {
                        int alpha = 0xCC;
                        g.fill(x - 1, y - 1, x + ICON_SIZE + 1, y + ICON_SIZE + 1, (alpha << 24) | 0x08080B);
                        g.blit(RenderPipelines.GUI_TEXTURED, iconTexture,
                                x, y, 0, 0, ICON_SIZE, ICON_SIZE, 16, 16, 16, 16);
                        textX += ICON_SIZE + ICON_GAP;
                    }
                    int color = kind == Kind.WARNING ? INK_WARNING : kind == Kind.HINT ? INK_HINT : INK;
                    g.drawString(font, text, textX, y, color, false);
                }
                case LINK, GUIDE_LINK -> {
                    int linkColor = 0xFF5BB8FF; // soft sky-blue, vanilla-link feel
                    int textX = x + 4;
                    g.drawString(font, "↗ " + text, textX, y, linkColor, false);
                    int w = font.width("↗ " + text);
                    g.fill(textX, y + 9, textX + w, y + 10, linkColor);
                }
                case SYMPTOM_METER -> {
                    // 1) HUD-style icon tile with dark backing
                    int alpha = 0xCC;
                    g.fill(x - 1, y - 1, x + ICON_SIZE + 1, y + ICON_SIZE + 1, (alpha << 24) | 0x08080B);
                    if (iconTexture != null) {
                        g.blit(RenderPipelines.GUI_TEXTURED, iconTexture,
                                x, y, 0, 0, ICON_SIZE, ICON_SIZE, 16, 16, 16, 16);
                    }
                    // 2) Yellow intensity meter strip under the icon (mirrors HUD)
                    int meter = Math.max(1, Math.round(ICON_SIZE * intensity));
                    g.fill(x, y + ICON_SIZE - 1, x + meter, y + ICON_SIZE, 0xFFE6D06C);

                    // 3) Label
                    int textX = x + ICON_SIZE + ICON_GAP;
                    g.drawString(font, text, textX, y + 2, INK, false);

                    // 4) Right-aligned percentage tail
                    if (!tail.isEmpty()) {
                        int tw = font.width(tail);
                        // Choose tail color by intensity bracket
                        int tailColor =
                                intensity >= 0.66F ? 0xFFB23A3A :
                                intensity >= 0.33F ? 0xFF9C6B2B :
                                INK_SOFT;
                        g.drawString(font, tail, xRight - tw, y + 2, tailColor, false);
                    }
                }
            }
        }
    }
}
