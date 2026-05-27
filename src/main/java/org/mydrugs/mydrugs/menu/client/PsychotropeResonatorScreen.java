package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorFailureReason;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorBlockEntity.ResonatorState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationCoreTier;
import org.mydrugs.mydrugs.menu.PsychotropeResonatorMenu;
import org.mydrugs.mydrugs.menu.layout.PsychotropeResonatorLayout;

public final class PsychotropeResonatorScreen extends AbstractMachineScreen<PsychotropeResonatorMenu> {
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
                PsychotropeResonatorLayout.STATUS_Y + 31,
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
                PsychotropeResonatorLayout.STATUS_Y + 43,
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
        int y = PsychotropeResonatorLayout.FAILURE_PANEL_Y + 7;
        int rightEdge = PsychotropeResonatorLayout.FAILURE_PANEL_X + PsychotropeResonatorLayout.FAILURE_PANEL_W - 8;
        int bottomEdge = PsychotropeResonatorLayout.FAILURE_PANEL_Y + PsychotropeResonatorLayout.FAILURE_PANEL_H - 4;
        graphics.drawString(this.font,
                Component.translatable("screen.mydrugs.psychotrope_resonator.checklist"),
                x, y, 0xFFCDBDFF, false);
        y += 11;

        DrugId candidate = this.menu.candidateDrug();
        int mask = this.menu.checklistMask();
        boolean hasMissing = hasMissingChecks(mask);

        PsychotropeResonatorFailureReason reason = this.menu.failureReason();
        if (reason != PsychotropeResonatorFailureReason.NONE) {
            // Real prior failure: show its specific reason (e.g. ADDICTION_TOO_HIGH).
            graphics.drawString(this.font,
                    Component.translatable(reason.translationKey()),
                    x, y, 0xFFFF9F9F, false);
            y += 11;
        } else if (!hasMissing) {
            // No prior failure and no missing prerequisites: everything is ready.
            graphics.drawString(this.font,
                    Component.translatable("screen.mydrugs.psychotrope_resonator.ready"),
                    x, y, 0xFF9FE8AE, false);
            y += 11;
        }

        if (candidate != null) {
            graphics.drawString(this.font,
                    Component.translatable("screen.mydrugs.psychotrope_resonator.candidate",
                            Component.translatable("drug.mydrugs." + candidate.serializedName())),
                    x, y, 0xFFE8ECEF, false);
            y += 11;
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
            if (y + 9 > bottomEdge) {
                // Out of room. Last line shows an ellipsis so the player knows more is hidden.
                graphics.drawString(this.font,
                        Component.translatable("screen.mydrugs.psychotrope_resonator.missing.more"),
                        x, y, 0xFFFFD27D, false);
                break;
            }
            Component line = missingLineFor(bit, candidate);
            graphics.drawString(this.font, line, x, y, 0xFFFFD27D, false);
            // Trim hint: shrink if drawn line would overflow the panel width.
            if (this.font.width(line) > rightEdge - x) {
                // Width-bounded draw fallback isn't needed for Minecraft's font; line just wraps via clipping.
                // Keep this branch so we can swap in a future text-wrapping helper without restructuring.
            }
            y += 11;
        }
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
}
