package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorFailureReason;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorBlockEntity.ResonatorState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationCoreTier;
import org.mydrugs.mydrugs.menu.PsychotropeResonatorMenu;
import org.mydrugs.mydrugs.menu.layout.PsychotropeResonatorLayout;

import java.util.ArrayList;
import java.util.List;

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
                this.leftPos + PsychotropeResonatorLayout.FAILURE_PANEL_X,
                this.topPos + bodyTop,
                this.leftPos + PsychotropeResonatorLayout.FAILURE_PANEL_X + PsychotropeResonatorLayout.FAILURE_PANEL_W,
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
        boolean hasMissing = hasMissingChecks(mask);

        PsychotropeResonatorFailureReason reason = this.menu.failureReason();
        if (reason != PsychotropeResonatorFailureReason.NONE) {
            lines.add(new ChecklistLine(Component.translatable(reason.translationKey()), 0xFFFF9F9F));
        } else if (!hasMissing) {
            lines.add(new ChecklistLine(
                    Component.translatable("screen.mydrugs.psychotrope_resonator.ready"),
                    0xFF9FE8AE));
        }

        if (candidate != null) {
            lines.add(new ChecklistLine(
                    Component.translatable("screen.mydrugs.psychotrope_resonator.candidate",
                            Component.translatable("drug.mydrugs." + candidate.serializedName())),
                    0xFFE8ECEF));
        }

        for (int bit : new int[] {
                PsychotropeResonatorBlockEntity.CHECK_KNOWLEDGE,
                PsychotropeResonatorBlockEntity.CHECK_REQUIREMENT,
                PsychotropeResonatorBlockEntity.CHECK_LOW_ADDICTION,
                PsychotropeResonatorBlockEntity.CHECK_RECOVERY,
                PsychotropeResonatorBlockEntity.CHECK_LIFETIME,
                PsychotropeResonatorBlockEntity.CHECK_MATERIAL,
                PsychotropeResonatorBlockEntity.CHECK_CORE,
                PsychotropeResonatorBlockEntity.CHECK_DIARY,
                PsychotropeResonatorBlockEntity.CHECK_ROOM,
                PsychotropeResonatorBlockEntity.CHECK_NOT_INTEGRATED
        }) {
            if ((mask & bit) != 0) {
                continue;
            }
            lines.add(new ChecklistLine(missingLineFor(bit, candidate), 0xFFFFD27D));
        }
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

    private static boolean hasMissingChecks(int mask) {
        return (mask & PsychotropeResonatorBlockEntity.CHECK_KNOWLEDGE) == 0
                || (mask & PsychotropeResonatorBlockEntity.CHECK_REQUIREMENT) == 0
                || (mask & PsychotropeResonatorBlockEntity.CHECK_LOW_ADDICTION) == 0
                || (mask & PsychotropeResonatorBlockEntity.CHECK_RECOVERY) == 0
                || (mask & PsychotropeResonatorBlockEntity.CHECK_LIFETIME) == 0
                || (mask & PsychotropeResonatorBlockEntity.CHECK_MATERIAL) == 0
                || (mask & PsychotropeResonatorBlockEntity.CHECK_CORE) == 0
                || (mask & PsychotropeResonatorBlockEntity.CHECK_DIARY) == 0
                || (mask & PsychotropeResonatorBlockEntity.CHECK_ROOM) == 0
                || (mask & PsychotropeResonatorBlockEntity.CHECK_NOT_INTEGRATED) == 0;
    }

    private static Component missingLineFor(int bit, @Nullable DrugId candidate) {
        // Tier-aware Core message: tells the player which tier of core they need to forge.
        if (bit == PsychotropeResonatorBlockEntity.CHECK_CORE && candidate != null) {
            IntegrationCoreTier required = IntegrationCoreTier.requiredFor(candidate);
            if (required != null) {
                return Component.translatable(
                        "screen.mydrugs.psychotrope_resonator.missing.core_tier",
                        Component.translatable(required.translationKey())
                );
            }
        }
        return Component.translatable("screen.mydrugs.psychotrope_resonator.missing." + bit);
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
