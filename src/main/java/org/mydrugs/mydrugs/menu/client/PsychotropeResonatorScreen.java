package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorFailureReason;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorBlockEntity.ResonatorState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationCoreTier;
import org.mydrugs.mydrugs.core.drug.integration.IntegratedTrait;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationRequirementProfile;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationRequirements;
import org.mydrugs.mydrugs.menu.PsychotropeResonatorMenu;
import org.mydrugs.mydrugs.menu.layout.PsychotropeResonatorLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PsychotropeResonatorScreen extends AbstractMachineScreen<PsychotropeResonatorMenu> {
    private int checklistScroll;

    public PsychotropeResonatorScreen(PsychotropeResonatorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, PsychotropeResonatorLayout.GUI_WIDTH, PsychotropeResonatorLayout.GUI_HEIGHT);
    }

    @Override
    protected boolean shouldShowTransferConfigButton() {
        return false;
    }

    @Override
    protected boolean shouldRenderSharedEnergyBar() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        addRitualButton(
                PsychotropeResonatorLayout.DREAM_BUTTON_X,
                PsychotropeResonatorMenu.DREAM_ALIGNMENT_BUTTON_ID,
                "screen.mydrugs.psychotrope_resonator.button.dream_alignment",
                "screen.mydrugs.psychotrope_resonator.tooltip.dream_alignment"
        );
        addRitualButton(
                PsychotropeResonatorLayout.INTEGRATION_BUTTON_X,
                PsychotropeResonatorMenu.INTEGRATION_BUTTON_ID,
                "screen.mydrugs.psychotrope_resonator.button.integration",
                "screen.mydrugs.psychotrope_resonator.tooltip.integration"
        );
        addRitualButton(
                PsychotropeResonatorLayout.RECOVERY_BUTTON_X,
                PsychotropeResonatorMenu.RECOVERY_RESONANCE_BUTTON_ID,
                "screen.mydrugs.psychotrope_resonator.button.recovery",
                "screen.mydrugs.psychotrope_resonator.tooltip.recovery"
        );
        addRitualButton(
                PsychotropeResonatorLayout.DIMENSION_BUTTON_X,
                PsychotropeResonatorMenu.OPEN_DIMENSION_BUTTON_ID,
                "screen.mydrugs.psychotrope_resonator.button.dimension",
                "screen.mydrugs.psychotrope_resonator.tooltip.dimension"
        );
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);
        drawPanel(
                graphics,
                PsychotropeResonatorLayout.MACHINE_PANEL_X,
                PsychotropeResonatorLayout.MACHINE_PANEL_Y,
                PsychotropeResonatorLayout.MACHINE_PANEL_W,
                PsychotropeResonatorLayout.MACHINE_PANEL_H,
                0xFF1E1B25
        );
        drawPanel(
                graphics,
                PsychotropeResonatorLayout.FAILURE_PANEL_X,
                PsychotropeResonatorLayout.FAILURE_PANEL_Y,
                PsychotropeResonatorLayout.FAILURE_PANEL_W,
                PsychotropeResonatorLayout.FAILURE_PANEL_H,
                0xFF17141D
        );
        drawSieveInventoryPanels(graphics, PsychotropeResonatorLayout.PLAYER_INV_X, PsychotropeResonatorLayout.PLAYER_INV_Y);

        drawSlotFrame(graphics, PsychotropeResonatorLayout.MATERIAL_SLOT_X, PsychotropeResonatorLayout.MATERIAL_SLOT_Y);
        drawSlotFrame(graphics, PsychotropeResonatorLayout.CORE_SLOT_X, PsychotropeResonatorLayout.CORE_SLOT_Y);
        drawSlotFrame(graphics, PsychotropeResonatorLayout.DIARY_SLOT_X, PsychotropeResonatorLayout.DIARY_SLOT_Y);

        drawHorizontalBar(
                graphics,
                PsychotropeResonatorLayout.PROGRESS_BAR_X,
                PsychotropeResonatorLayout.PROGRESS_BAR_Y,
                PsychotropeResonatorLayout.PROGRESS_BAR_W,
                PsychotropeResonatorLayout.PROGRESS_BAR_H,
                this.menu.getScaledProgress(PsychotropeResonatorLayout.PROGRESS_BAR_W),
                0xFF9A6DFF,
                0xFFD9CAFF
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xFFE8ECEF, false);
        graphics.drawString(
                this.font,
                Component.translatable(this.menu.state().translationKey()),
                PsychotropeResonatorLayout.STATUS_X,
                PsychotropeResonatorLayout.STATUS_Y,
                stateColor(this.menu.state()),
                false
        );
        graphics.drawString(
                this.font,
                activeDrugLabel(),
                PsychotropeResonatorLayout.STATUS_X,
                PsychotropeResonatorLayout.STATUS_Y + PsychotropeResonatorLayout.STATUS_DRUG_Y_OFFSET,
                0xFFE8ECEF,
                false
        );
        graphics.drawString(
                this.font,
                this.menu.dimensionReady()
                        ? (this.menu.canOpenDimension()
                        ? Component.translatable("screen.mydrugs.psychotrope_resonator.dimension_ready")
                        : Component.translatable("screen.mydrugs.psychotrope_resonator.dimension_aligned"))
                        : Component.translatable("screen.mydrugs.psychotrope_resonator.dimension_locked"),
                PsychotropeResonatorLayout.STATUS_X,
                PsychotropeResonatorLayout.STATUS_Y + PsychotropeResonatorLayout.STATUS_DIMENSION_Y_OFFSET,
                this.menu.canOpenDimension() ? 0xFFB58CFF : 0xFF9FA6AE,
                false
        );
        renderFailurePanel(graphics);
        graphics.drawString(
                this.font,
                Component.translatable("screen.mydrugs.ui.inventory"),
                PsychotropeResonatorLayout.PLAYER_INV_X,
                standardInventoryLabelY(PsychotropeResonatorLayout.PLAYER_INV_Y),
                0xFFE8ECEF,
                false
        );
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(PsychotropeResonatorLayout.PROGRESS_BAR_X, PsychotropeResonatorLayout.PROGRESS_BAR_Y,
                PsychotropeResonatorLayout.PROGRESS_BAR_W, PsychotropeResonatorLayout.PROGRESS_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.psychotrope_resonator.progress"),
                    Component.translatable("screen.mydrugs.ui.amount", this.menu.progress(), this.menu.maxProgress())
            );
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isOverFailurePanel(mouseX, mouseY)) {
            int delta = (int) -Math.signum(scrollY); // wheel up (scrollY > 0) -> scroll up (offset--)
            if (delta != 0) {
                checklistScroll = Mth.clamp(checklistScroll + delta, 0, maxChecklistScroll());
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean isOverFailurePanel(double mouseX, double mouseY) {
        int x = this.leftPos + PsychotropeResonatorLayout.FAILURE_PANEL_X;
        int y = this.topPos + PsychotropeResonatorLayout.FAILURE_PANEL_Y;
        return mouseX >= x && mouseX < x + PsychotropeResonatorLayout.FAILURE_PANEL_W
                && mouseY >= y && mouseY < y + PsychotropeResonatorLayout.FAILURE_PANEL_H;
    }

    private void addRitualButton(int x, int id, String labelKey, String tooltipKey) {
        this.addRenderableWidget(Button.builder(
                        Component.translatable(labelKey),
                        button -> pressMenuButton(id)
                )
                .bounds(guiX(x), guiY(PsychotropeResonatorLayout.BUTTON_Y),
                        PsychotropeResonatorLayout.BUTTON_W, PsychotropeResonatorLayout.BUTTON_H)
                .tooltip(Tooltip.create(Component.translatable(tooltipKey)))
                .build());
    }

    private Component activeDrugLabel() {
        DrugId drug = this.menu.activeIntegrationDrug();
        if (drug == null) {
            drug = this.menu.candidateDrug();
        }
        if (drug == null) {
            return Component.translatable("screen.mydrugs.psychotrope_resonator.no_active_drug");
        }
        return Component.translatable(
                "screen.mydrugs.psychotrope_resonator.active_drug",
                Component.translatable("drug.mydrugs." + drug.serializedName())
        );
    }

    private void renderFailurePanel(GuiGraphics graphics) {
        int x = PsychotropeResonatorLayout.FAILURE_PANEL_X + 8;
        int headerY = PsychotropeResonatorLayout.FAILURE_PANEL_Y + 7;

        graphics.drawString(this.font,
                Component.translatable("screen.mydrugs.psychotrope_resonator.checklist"),
                x, headerY, 0xFFCDBDFF, false);

        List<ChecklistLine> lines = buildChecklistLines();
        int lineHeight = PsychotropeResonatorLayout.FAILURE_PANEL_LINE_HEIGHT;
        int bodyTop = headerY + PsychotropeResonatorLayout.FAILURE_PANEL_HEADER_H;
        int bodyBottom = PsychotropeResonatorLayout.FAILURE_PANEL_Y
                + PsychotropeResonatorLayout.FAILURE_PANEL_H
                - PsychotropeResonatorLayout.FAILURE_PANEL_BOTTOM_PAD;
        int visibleLines = Math.max(0, (bodyBottom - bodyTop) / lineHeight);
        int maxScroll = Math.max(0, lines.size() - visibleLines);
        checklistScroll = Mth.clamp(checklistScroll, 0, maxScroll);

        // Clip lines to the body so partially-visible text gets cut at the panel edges.
        graphics.enableScissor(
                PsychotropeResonatorLayout.FAILURE_PANEL_X,
                this.topPos + bodyTop,
                PsychotropeResonatorLayout.FAILURE_PANEL_X + PsychotropeResonatorLayout.FAILURE_PANEL_W,
                this.topPos + bodyBottom
        );
        int drawY = bodyTop;
        for (int i = checklistScroll; i < lines.size(); i++) {
            if (drawY >= bodyBottom) break;
            ChecklistLine line = lines.get(i);
            graphics.drawString(this.font, line.text(), x, drawY, line.color(), false);
            drawY += lineHeight;
        }
        graphics.disableScissor();

        // Scroll indicators in the panel's right gutter.
        int gutterX = PsychotropeResonatorLayout.FAILURE_PANEL_X + PsychotropeResonatorLayout.FAILURE_PANEL_W - 9;
        if (checklistScroll > 0) {
            graphics.drawString(this.font, "▲", gutterX, bodyTop, 0xFFCDBDFF, false);
        }
        if (checklistScroll < maxScroll) {
            graphics.drawString(this.font, "▼", gutterX, bodyBottom - 7, 0xFFCDBDFF, false);
        }
    }

    private List<ChecklistLine> buildChecklistLines() {
        List<ChecklistLine> lines = new ArrayList<>();
        DrugId candidate = this.menu.candidateDrug();
        int mask = this.menu.checklistMask();

        PsychotropeResonatorFailureReason reason = this.menu.failureReason();
        if (reason != PsychotropeResonatorFailureReason.NONE) {
            lines.add(new ChecklistLine(Component.translatable(reason.translationKey()), 0xFFFF9F9F));
        } else if (candidate != null && allChecksMet(mask)) {
            lines.add(new ChecklistLine(
                    Component.translatable("screen.mydrugs.psychotrope_resonator.ready"),
                    0xFF9FE8AE));
        }

        if (candidate == null) {
            return lines;
        }

        lines.add(new ChecklistLine(
                Component.translatable("screen.mydrugs.psychotrope_resonator.candidate",
                        Component.translatable("drug.mydrugs." + candidate.serializedName())),
                0xFFE8ECEF));
        IntegratedTrait trait = IntegratedTrait.bySource(candidate);
        if (trait != null) {
            lines.add(new ChecklistLine(Component.translatable(
                    "screen.mydrugs.psychotrope_resonator.trait_preview",
                    Component.translatable(trait.translationKey())), 0xFFCDBDFF));
            lines.add(new ChecklistLine(Component.translatable(trait.rewardKey()), 0xFFE8ECEF));
            lines.add(new ChecklistLine(Component.translatable(
                    "screen.mydrugs.psychotrope_resonator.permanent_warning"), 0xFFFFD38A));
        }
        lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_KNOWLEDGE,
                Component.translatable("screen.mydrugs.psychotrope_resonator.check.knowledge")));

        IntegrationRequirementProfile profile = IntegrationRequirements.profile(candidate);
        if (profile != null && profile.usesCleanDoseStreak()) {
            lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_REQUIREMENT,
                    Component.translatable("screen.mydrugs.psychotrope_resonator.check.clean_doses",
                            this.menu.cleanDoseStreak(), this.menu.cleanDoseStreakRequired())));
            lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_SPACING, spacingLine()));
            lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_REFLECTION,
                    Component.translatable("screen.mydrugs.psychotrope_resonator.check.reflection",
                            this.menu.psychedelicReflections(), this.menu.psychedelicReflectionsRequired())));
            lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_SAFE_PSYCH_USE,
                    Component.translatable("screen.mydrugs.psychotrope_resonator.check.safe_use",
                            this.menu.safePsychedelicUses(), this.menu.safePsychedelicUsesRequired())));
            lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_NO_RECENT_BAD_TRIP, badTripLine()));
        } else {
            lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_REQUIREMENT,
                    Component.translatable("screen.mydrugs.psychotrope_resonator.check.peak",
                            formatTenth(this.menu.peakCurrentTenth()), formatTenth(this.menu.peakRequiredTenth()))));
            lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_LOW_ADDICTION,
                    Component.translatable("screen.mydrugs.psychotrope_resonator.check.current_addiction",
                            formatTenth(this.menu.addictionCurrentTenth()), formatTenth(this.menu.addictionMaxTenth()))));
            lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_LIFETIME,
                    Component.translatable("screen.mydrugs.psychotrope_resonator.check.lifetime",
                            formatTenth(this.menu.lifetimeDoseTenth()), formatTenth(this.menu.lifetimeDoseRequiredTenth()))));
            lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_RECOVERY,
                    Component.translatable("screen.mydrugs.psychotrope_resonator.check.recovery",
                            this.menu.recoveryCurrentPercent(), this.menu.recoveryRequiredPercent())));
        }

        lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_MATERIAL,
                Component.translatable("screen.mydrugs.psychotrope_resonator.check.material")));
        lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_CORE,
                Component.translatable("screen.mydrugs.psychotrope_resonator.check.core",
                        tierName(this.menu.requiredCoreRank()), tierName(this.menu.slottedCoreRank()))));
        lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_DIARY,
                Component.translatable("screen.mydrugs.psychotrope_resonator.check.diary")));
        lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_ROOM,
                Component.translatable("screen.mydrugs.psychotrope_resonator.check.room")));
        lines.add(checkLine(mask, PsychotropeResonatorBlockEntity.CHECK_NOT_INTEGRATED,
                Component.translatable("screen.mydrugs.psychotrope_resonator.check.not_integrated")));
        return lines;
    }

    private int maxChecklistScroll() {
        int lineHeight = PsychotropeResonatorLayout.FAILURE_PANEL_LINE_HEIGHT;
        int bodyTop = PsychotropeResonatorLayout.FAILURE_PANEL_Y + 7 + PsychotropeResonatorLayout.FAILURE_PANEL_HEADER_H;
        int bodyBottom = PsychotropeResonatorLayout.FAILURE_PANEL_Y
                + PsychotropeResonatorLayout.FAILURE_PANEL_H
                - PsychotropeResonatorLayout.FAILURE_PANEL_BOTTOM_PAD;
        int visibleLines = Math.max(0, (bodyBottom - bodyTop) / lineHeight);
        return Math.max(0, buildChecklistLines().size() - visibleLines);
    }

    private static boolean allChecksMet(int mask) {
        int required = PsychotropeResonatorBlockEntity.CHECK_KNOWLEDGE
                | PsychotropeResonatorBlockEntity.CHECK_REQUIREMENT
                | PsychotropeResonatorBlockEntity.CHECK_LOW_ADDICTION
                | PsychotropeResonatorBlockEntity.CHECK_RECOVERY
                | PsychotropeResonatorBlockEntity.CHECK_LIFETIME
                | PsychotropeResonatorBlockEntity.CHECK_MATERIAL
                | PsychotropeResonatorBlockEntity.CHECK_CORE
                | PsychotropeResonatorBlockEntity.CHECK_DIARY
                | PsychotropeResonatorBlockEntity.CHECK_ROOM
                | PsychotropeResonatorBlockEntity.CHECK_NOT_INTEGRATED
                | PsychotropeResonatorBlockEntity.CHECK_REFLECTION
                | PsychotropeResonatorBlockEntity.CHECK_SAFE_PSYCH_USE
                | PsychotropeResonatorBlockEntity.CHECK_NO_RECENT_BAD_TRIP
                | PsychotropeResonatorBlockEntity.CHECK_SPACING;
        return (mask & required) == required;
    }

    private ChecklistLine checkLine(int mask, int bit, Component detail) {
        boolean ok = (mask & bit) != 0;
        return new ChecklistLine(
                Component.translatable(ok
                                ? "screen.mydrugs.psychotrope_resonator.line.ok"
                                : "screen.mydrugs.psychotrope_resonator.line.missing",
                        detail),
                ok ? 0xFF9FE8AE : 0xFFFFD27D
        );
    }

    private Component spacingLine() {
        int ticks = this.menu.cleanSpacingRemainingTicks();
        if (ticks <= 0) {
            return Component.translatable("screen.mydrugs.psychotrope_resonator.check.spacing_ready");
        }
        return Component.translatable("screen.mydrugs.psychotrope_resonator.check.spacing_wait", formatDuration(ticks));
    }

    private Component badTripLine() {
        int ticks = this.menu.recentBadTripRemainingTicks();
        if (ticks <= 0) {
            return Component.translatable("screen.mydrugs.psychotrope_resonator.check.bad_trip_clear");
        }
        return Component.translatable("screen.mydrugs.psychotrope_resonator.check.bad_trip_wait", formatDuration(ticks));
    }

    private static Component tierName(int rank) {
        IntegrationCoreTier tier = IntegrationCoreTier.byRank(rank);
        return tier == null
                ? Component.translatable("screen.mydrugs.psychotrope_resonator.core.none")
                : Component.translatable(tier.translationKey());
    }

    private static String formatTenth(int tenth) {
        return String.format(Locale.ROOT, "%.1f", tenth / 10.0F);
    }

    private static String formatDuration(int ticks) {
        int seconds = Math.max(1, (ticks + 19) / 20);
        if (seconds < 90) {
            return seconds + "s";
        }
        int minutes = Math.max(1, (seconds + 59) / 60);
        return minutes + "m";
    }

    private int stateColor(ResonatorState state) {
        return switch (state) {
            case RESONATING, INTEGRATING, READY, DIMENSION_READY -> 0xFFCDBDFF;
            case BLOCKED, INVALID_STRUCTURE -> 0xFFFF7D7D;
            case COOLDOWN, AWAITING_CONDITIONS -> 0xFFFFD27D;
            default -> 0xFFE8ECEF;
        };
    }

    private record ChecklistLine(Component text, int color) {
    }
}
