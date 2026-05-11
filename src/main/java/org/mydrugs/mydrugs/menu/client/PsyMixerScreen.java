package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;
import org.mydrugs.mydrugs.blocks.PsyMixerMultiblock;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualEngine;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualJudgement;
import org.mydrugs.mydrugs.client.compat.ClientRecipesCache;
import org.mydrugs.mydrugs.menu.PsyMixerMenu;
import org.mydrugs.mydrugs.menu.client.util.DrugBonusClientText;
import org.mydrugs.mydrugs.network.PsyMixerRitualInputPayload;
import org.mydrugs.mydrugs.network.PsyMixerStartRitualPayload;

public final class PsyMixerScreen extends AbstractContainerScreen<PsyMixerMenu> {
    private static final int OUTER = 0xFF12070A;
    private static final int PANEL = 0xFF2A1A22;
    private static final int PANEL_DARK = 0xFF1A0E12;
    private static final int PANEL_LINE = 0xFF5C3344;
    private static final int TEXT = 0xFFE8D6C2;
    private static final int MUTED = 0xFFB58A96;
    private static final int GOOD = 0xFF76E08A;
    private static final int WARN = 0xFFFFD060;
    private static final int BAD = 0xFFFF5A77;

    private final RitualLayout layout = RitualLayout.create();
    private Button beginButton;
    private Button tapButton;

    public PsyMixerScreen(PsyMixerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = layout.width;
        this.imageHeight = layout.height;
        this.titleLabelX = layout.titleX;
        this.titleLabelY = layout.titleY;
        this.inventoryLabelX = layout.inventoryX;
        this.inventoryLabelY = layout.inventoryLabelY;
    }

    @Override
    protected void init() {
        super.init();
        beginButton = Button.builder(
                Component.translatable("screen.mydrugs.psy_mixer.begin"),
                btn -> {
                    if (!menu.isRunning()) {
                        ClientPacketDistributor.sendToServer(new PsyMixerStartRitualPayload(menu.getMenuId(), menu.getCorePos()));
                    }
                }
        ).bounds(this.leftPos + layout.actionX, this.topPos + layout.actionY, layout.actionWidth, 20).build();
        this.addRenderableWidget(beginButton);

        tapButton = Button.builder(
                Component.translatable("screen.mydrugs.psy_mixer.tap"),
                btn -> {
                    if (menu.isRunning()) {
                        ClientPacketDistributor.sendToServer(new PsyMixerRitualInputPayload(menu.getMenuId(), menu.getServerPhase()));
                    }
                }
        ).bounds(this.leftPos + layout.actionX, this.topPos + layout.actionY, layout.actionWidth, 20).build();
        this.addRenderableWidget(tapButton);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, OUTER);
        fillPanel(graphics, layout.ritualX, layout.ritualY, layout.ritualWidth, layout.ritualHeight);
        fillPanel(graphics, layout.inventoryPanelX, layout.inventoryPanelY, layout.inventoryPanelWidth, layout.inventoryPanelHeight);

        int cx = leftPos + layout.ritualCenterX;
        int cy = topPos + layout.ritualCenterY;
        drawRitualRing(graphics, cx + 8, cy + 8);
        drawSlotWell(graphics, cx, cy, PsyMixerMultiblock.SLOT_BASE);
        drawSlotWell(graphics, cx - layout.slotRadius, cy, PsyMixerMultiblock.SLOT_MATERIAL);
        drawSlotWell(graphics, cx, cy - layout.slotRadius, PsyMixerMultiblock.SLOT_CATALYST);
        drawSlotWell(graphics, cx + layout.slotRadius, cy, PsyMixerMultiblock.SLOT_STABILIZER);
        drawSlotWell(graphics, cx, cy + layout.slotRadius, PsyMixerMultiblock.SLOT_VESSEL);
        drawSlotWell(graphics, cx + layout.outputOffsetX, cy, PsyMixerMultiblock.SLOT_OUTPUT);
    }

    private void fillPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        int left = leftPos + x;
        int top = topPos + y;
        graphics.fill(left, top, left + width, top + height, PANEL_DARK);
        graphics.fill(left + 2, top + 2, left + width - 2, top + height - 2, PANEL);
        graphics.fill(left, top, left + width, top + 1, PANEL_LINE);
        graphics.fill(left, top + height - 1, left + width, top + height, PANEL_LINE);
    }

    private void drawRitualRing(GuiGraphics graphics, int cx, int cy) {
        for (int i = 0; i < 64; i++) {
            double angle = i * Math.PI * 2.0 / 64.0;
            int dx = (int) Math.round(Math.cos(angle) * layout.ringRadius);
            int dy = (int) Math.round(Math.sin(angle) * layout.ringRadius);
            graphics.fill(cx + dx - 1, cy + dy - 1, cx + dx + 1, cy + dy + 1, 0xFF6A3A4A);
        }

        if (!menu.isRunning()) {
            return;
        }

        float window = menu.getTimingWindow();
        for (int i = 0; i < 128; i++) {
            float p = i / 128.0F;
            float dist = PsyMixerRitualEngine.nearestGoldenZoneDistance(p, menu.getTargetPhase());
            if (dist <= window / 2.0F) {
                double angle = -Math.PI / 2.0 + p * Math.PI * 2.0;
                int dx = (int) Math.round(Math.cos(angle) * layout.markerRadius);
                int dy = (int) Math.round(Math.sin(angle) * layout.markerRadius);
                graphics.fill(cx + dx - 2, cy + dy - 2, cx + dx + 2, cy + dy + 2, WARN);
            }
        }

        float phase = menu.getServerPhase();
        double markerAngle = -Math.PI / 2.0 + phase * Math.PI * 2.0;
        int mx = (int) Math.round(Math.cos(markerAngle) * layout.markerRadius);
        int my = (int) Math.round(Math.sin(markerAngle) * layout.markerRadius);
        int markerColor = isInTimingWindow() ? GOOD : 0xFFFFEEAA;
        graphics.fill(cx + mx - 5, cy + my - 5, cx + mx + 5, cy + my + 5, markerColor);
        graphics.fill(cx + mx - 2, cy + my - 2, cx + mx + 2, cy + my + 2, OUTER);
    }

    private void drawSlotWell(GuiGraphics graphics, int cx, int cy, int slot) {
        boolean focused = menu.isRunning() && slot == menu.getFocusSlot();
        int border = focused ? WARN : 0xFF100407;
        graphics.fill(cx - 3, cy - 3, cx + 19, cy + 19, focused ? 0xFF1C1207 : 0xFF100407);
        graphics.fill(cx - 2, cy - 2, cx + 18, cy + 18, border);
        graphics.fill(cx, cy, cx + 16, cy + 16, 0xFF24111A);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, MUTED, false);
        //graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, MUTED, false);
        drawSideInstructions(graphics);
        drawChecklist(graphics);
        drawActiveDrugBonuses(graphics);
        drawMeters(graphics);
        drawRitualCue(graphics);
    }

    private void drawSideInstructions(GuiGraphics graphics) {
        Component status = menu.isRunning()
                ? Component.translatable("screen.mydrugs.psy_mixer.help.running_focus", Component.translatable(menu.getFocus().labelKey()))
                : Component.translatable(hasStarterItems()
                ? "screen.mydrugs.psy_mixer.help.ready"
                : "screen.mydrugs.psy_mixer.help.setup");

        int y = layout.sideY;
        graphics.drawString(font, Component.translatable("screen.mydrugs.psy_mixer.guidance"), layout.sideX, y, MUTED, false);
        y += 12;
        for (var line : font.split(status, layout.sideWidth)) {
            graphics.drawString(font, line, layout.sideX, y, TEXT, false);
            y += 10;
        }
    }

    private void drawChecklist(GuiGraphics graphics) {
        int y = layout.checklistY;
        graphics.drawString(font, Component.translatable("screen.mydrugs.psy_mixer.ingredients"), layout.sideX, y, MUTED, false);
        y += 12;
        drawChecklistLine(graphics, y, "screen.mydrugs.psy_mixer.offering", hasSlot(PsyMixerMultiblock.SLOT_BASE), true);
        drawChecklistLine(graphics, y + 10, "screen.mydrugs.psy_mixer.material", hasSlot(PsyMixerMultiblock.SLOT_MATERIAL), true);
        drawChecklistLine(graphics, y + 20, "screen.mydrugs.psy_mixer.vessel", hasSlot(PsyMixerMultiblock.SLOT_VESSEL), isSlotRequired(PsyMixerMultiblock.SLOT_VESSEL));
        drawBonusSlotLine(graphics, y + 30, "screen.mydrugs.psy_mixer.catalyst", PsyMixerMultiblock.SLOT_CATALYST);
        drawBonusSlotLine(graphics, y + 40, "screen.mydrugs.psy_mixer.stabilizer", PsyMixerMultiblock.SLOT_STABILIZER);
    }

    private enum BonusSlotState { ACTIVE, MISSING, INVALID, OPTIONAL }

    private BonusSlotState getBonusSlotState(int slot) {
        var recipes = ClientRecipesCache.getPsyMixerRecipes();
        ItemStack itemInSlot = menu.getSlot(slot).getItem();

        boolean anySupports = recipes.stream().anyMatch(recipe -> switch (slot) {
            case PsyMixerMultiblock.SLOT_CATALYST -> recipe.catalyst().isPresent();
            case PsyMixerMultiblock.SLOT_STABILIZER -> recipe.stabilizer().isPresent();
            default -> false;
        });

        if (!anySupports) {
            return itemInSlot.isEmpty() ? BonusSlotState.OPTIONAL : BonusSlotState.INVALID;
        }
        if (itemInSlot.isEmpty()) return BonusSlotState.MISSING;

        boolean isValid = recipes.stream().anyMatch(recipe -> switch (slot) {
            case PsyMixerMultiblock.SLOT_CATALYST -> recipe.catalyst().map(ing -> ing.test(itemInSlot)).orElse(false);
            case PsyMixerMultiblock.SLOT_STABILIZER -> recipe.stabilizer().map(ing -> ing.test(itemInSlot)).orElse(false);
            default -> false;
        });
        return isValid ? BonusSlotState.ACTIVE : BonusSlotState.INVALID;
    }

    private void drawBonusSlotLine(GuiGraphics graphics, int y, String labelKey, int slot) {
        BonusSlotState state = getBonusSlotState(slot);
        int color;
        Component stateText;
        switch (state) {
            case ACTIVE -> { color = GOOD; stateText = Component.translatable("screen.mydrugs.psy_mixer.bonus_active"); }
            case MISSING -> { color = WARN; stateText = Component.translatable("screen.mydrugs.psy_mixer.missing"); }
            case INVALID -> { color = BAD; stateText = Component.translatable("screen.mydrugs.psy_mixer.invalid"); }
            default -> { color = MUTED; stateText = Component.translatable("screen.mydrugs.psy_mixer.optional_short"); }
        }
        Component label = Component.translatable("screen.mydrugs.psy_mixer.checkline", Component.translatable(labelKey), stateText);
        graphics.drawString(font, label, layout.sideX, y, color, false);
    }

    private void drawChecklistLine(GuiGraphics graphics, int y, String labelKey, boolean present, boolean required) {
        int color = present ? GOOD : required ? BAD : MUTED;
        Component state = Component.translatable(present
                ? "screen.mydrugs.psy_mixer.ready"
                : required ? "screen.mydrugs.psy_mixer.missing" : "screen.mydrugs.psy_mixer.optional_short");
        Component label = Component.translatable("screen.mydrugs.psy_mixer.checkline", Component.translatable(labelKey), state);
        graphics.drawString(font, label, layout.sideX, y, color, false);
    }

    private void drawMeters(GuiGraphics graphics) {
        int y = layout.meterY;
        int resonanceWidth = Mth.clamp((int) (menu.getResonance() * layout.meterWidth), 0, layout.meterWidth);
        drawMeter(graphics, y, "screen.mydrugs.psy_mixer.resonance", resonanceWidth, 0xFF66D9FF);
        y += 13;
        if (menu.isRunning()) {
            int progressWidth = menu.getMaxProgress() > 0
                    ? Mth.clamp((int) (((float) menu.getProgress() / menu.getMaxProgress()) * layout.meterWidth), 0, layout.meterWidth)
                    : 0;
            drawMeter(graphics, y, "screen.mydrugs.psy_mixer.progress", progressWidth, 0xFFAA66CC);
            y += 13;
        }

        int instabilityWidth = Mth.clamp((int) (Math.min(1.0F, menu.getInstability()) * layout.meterWidth), 0, layout.meterWidth);
        drawMeter(graphics, y, "screen.mydrugs.psy_mixer.instability", instabilityWidth, 0xFFCC2244);
    }

    private void drawActiveDrugBonuses(GuiGraphics graphics) {
        DrugBonusClientText.drawRitualBonuses(graphics, font, 0, layout.height + 5, leftPos - 10);
    }

    private void drawMeter(GuiGraphics graphics, int y, String labelKey, int fillWidth, int fillColor) {
        graphics.drawString(font, Component.translatable(labelKey), layout.sideX, y - 1, MUTED, false);
        int barX = layout.sideX + 64;
        graphics.fill(barX, y + 2, barX + layout.meterWidth, y + 5, 0xFF21131A);
        graphics.fill(barX, y + 2, barX + fillWidth, y + 5, fillColor);
    }

    private void drawRitualCue(GuiGraphics graphics) {
        if (!menu.isRunning()) {
            return;
        }

        PsyMixerRitualJudgement judgement = menu.getLastJudgement();
        boolean showJudgement = menu.getFeedbackTicks() > 0 && judgement != PsyMixerRitualJudgement.NONE;
        Component cue = showJudgement
                ? Component.translatable(judgement.screenKey())
                : Component.translatable(isInTimingWindow()
                ? "screen.mydrugs.psy_mixer.tap_now"
                : "screen.mydrugs.psy_mixer.wait");
        int color = showJudgement ? judgementColor(judgement) : isInTimingWindow() ? GOOD : WARN;
        drawCentered(graphics, cue, layout.ritualCenterX, layout.ritualCueY, color);

        Component hits = Component.translatable(
                "screen.mydrugs.psy_mixer.hit_count_streak",
                menu.getGoodHits(),
                menu.getBadHits(),
                menu.getStreak()
        );
        drawCentered(graphics, hits, layout.ritualCenterX, layout.ritualCueY + 12, TEXT);
    }

    private int judgementColor(PsyMixerRitualJudgement judgement) {
        return switch (judgement) {
            case PERFECT -> 0xFF9BFFFF;
            case GREAT -> GOOD;
            case GOOD -> 0xFFB8F38A;
            case NEAR -> WARN;
            case MISS -> BAD;
            default -> TEXT;
        };
    }

    private boolean hasStarterItems() {
        return hasSlot(PsyMixerMultiblock.SLOT_BASE)
                && hasSlot(PsyMixerMultiblock.SLOT_MATERIAL)
                && hasSlot(PsyMixerMultiblock.SLOT_VESSEL);
    }

    private boolean hasSlot(int slot) {
        return menu.hasRitualItem(slot);
    }

    private boolean isSlotRequired(int slot) {
        var recipes = ClientRecipesCache.getPsyMixerRecipes();
        if (recipes.isEmpty()) {
            return slot == PsyMixerMultiblock.SLOT_BASE
                    || slot == PsyMixerMultiblock.SLOT_MATERIAL
                    || slot == PsyMixerMultiblock.SLOT_VESSEL
                    || slot == PsyMixerMultiblock.SLOT_CATALYST
                    || slot == PsyMixerMultiblock.SLOT_STABILIZER;
        }

        return recipes.stream().allMatch(recipe -> switch (slot) {
            case PsyMixerMultiblock.SLOT_BASE, PsyMixerMultiblock.SLOT_MATERIAL -> true;
            case PsyMixerMultiblock.SLOT_CATALYST -> recipe.catalyst().isPresent();
            case PsyMixerMultiblock.SLOT_STABILIZER -> recipe.stabilizer().isPresent();
            case PsyMixerMultiblock.SLOT_VESSEL -> recipe.vessel().isPresent();
            default -> false;
        });
    }

    private boolean isInTimingWindow() {
        float phase = menu.getServerPhase();
        float dist = PsyMixerRitualEngine.nearestGoldenZoneDistance(phase, menu.getTargetPhase());
        return dist <= menu.getTimingWindow() / 2.0F;
    }

    private void drawCentered(GuiGraphics graphics, Component text, int x, int y, int color) {
        graphics.drawString(font, text, x - font.width(text) / 2, y, color, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        updateButtons();
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (menu.isRunning()
                && menu.getRhythmInputCooldown() <= 0
                && (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            ClientPacketDistributor.sendToServer(new PsyMixerRitualInputPayload(menu.getMenuId(), menu.getServerPhase()));
            return true;
        }
        return super.keyPressed(event);
    }

    private void updateButtons() {
        if (beginButton != null) {
            beginButton.visible = !menu.isRunning();
            beginButton.active = !menu.isRunning();
        }
        if (tapButton != null) {
            boolean running = menu.isRunning();
            tapButton.visible = running;
            tapButton.active = running && menu.getRhythmInputCooldown() <= 0;
            tapButton.setMessage(Component.translatable(isInTimingWindow()
                    ? "screen.mydrugs.psy_mixer.tap_now"
                    : "screen.mydrugs.psy_mixer.hit_focus"));
        }
    }

    private static final class RitualLayout {
        final int width = 336;
        final int height = 198;
        final int titleX = 10;
        final int titleY = 8;
        final int ritualX = 10;
        final int ritualY = 22;
        final int ritualWidth = 164;
        final int ritualHeight = 80;
        final int ritualCenterX = 82;
        final int ritualCenterY = 52;
        final int ritualCueY = 86;
        final int slotRadius = 30;
        final int outputOffsetX = 64;
        final int ringRadius = 42;
        final int markerRadius = 49;
        final int sideX = 188;
        final int sideY = 20;
        final int sideWidth = 132;
        final int checklistY = 66;
        final int bonusY = 120;
        final int meterY = 135;
        final int meterWidth = 64;
        final int actionX = 236;
        final int actionY = 164;
        final int actionWidth = 84;
        final int inventoryX = 10;
        final int inventoryLabelY = 104;
        final int inventoryPanelX = 6;
        final int inventoryPanelY = 110;
        final int inventoryPanelWidth = 174;
        final int inventoryPanelHeight = 82;

        static RitualLayout create() {
            return new RitualLayout();
        }
    }
}
