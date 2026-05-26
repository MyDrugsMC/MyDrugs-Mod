package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorBlockEntity.ResonatorState;
import org.mydrugs.mydrugs.core.drug.DrugId;
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
        drawSieveInventoryPanels(graphics, PsychotropeResonatorLayout.PLAYER_INV_X, PsychotropeResonatorLayout.PLAYER_INV_Y);

        drawSlotFrame(graphics, PsychotropeResonatorLayout.DREAM_SLOT_X, PsychotropeResonatorLayout.DREAM_SLOT_Y);
        drawSlotFrame(graphics, PsychotropeResonatorLayout.CORE_SLOT_X, PsychotropeResonatorLayout.CORE_SLOT_Y);
        drawSlotFrame(graphics, PsychotropeResonatorLayout.DIARY_SLOT_X, PsychotropeResonatorLayout.DIARY_SLOT_Y);
        drawSlotFrame(graphics, PsychotropeResonatorLayout.OUTPUT_SLOT_X, PsychotropeResonatorLayout.OUTPUT_SLOT_Y);

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
                116,
                31,
                stateColor(this.menu.state()),
                false
        );
        graphics.drawString(
                this.font,
                activeDrugLabel(),
                116,
                62,
                0xFFE8ECEF,
                false
        );
        graphics.drawString(
                this.font,
                this.menu.dimensionReady()
                        ? Component.translatable("screen.mydrugs.psychotrope_resonator.dimension_ready")
                        : Component.translatable("screen.mydrugs.psychotrope_resonator.dimension_locked"),
                116,
                74,
                this.menu.dimensionReady() ? 0xFFB58CFF : 0xFF9FA6AE,
                false
        );
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

    private int stateColor(ResonatorState state) {
        return switch (state) {
            case RESONATING, INTEGRATING, READY, DIMENSION_READY -> 0xFFCDBDFF;
            case BLOCKED, INVALID_STRUCTURE -> 0xFFFF7D7D;
            case COOLDOWN, AWAITING_CONDITIONS -> 0xFFFFD27D;
            default -> 0xFFE8ECEF;
        };
    }
}
