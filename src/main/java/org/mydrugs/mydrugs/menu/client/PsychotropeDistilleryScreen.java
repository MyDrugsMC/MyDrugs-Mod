package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.PsychotropeDistilleryMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.PsychotropeDistilleryLayout;

import java.util.List;

public final class PsychotropeDistilleryScreen extends AbstractMachineScreen<PsychotropeDistilleryMenu> {
    public PsychotropeDistilleryScreen(PsychotropeDistilleryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, PsychotropeDistilleryLayout.GUI_WIDTH, PsychotropeDistilleryLayout.GUI_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Shared rendering (also reused by the JEI category) — geometry comes from the layout
        // constants so the screen and JEI preview always match.
        MachineGuiRenderer.drawPsychotropeDistillery(
                this,
                graphics,
                new MachineGuiRenderer.PsychotropeDistilleryState(
                        this.menu.getScaledProgress(PsychotropeDistilleryLayout.PROGRESS_W),
                        this.menu.getScaledBurn(PsychotropeDistilleryLayout.BURN_W)
                ),
                true
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xFFE8ECEF, false);
        graphics.drawString(
                this.font,
                Component.translatable("screen.mydrugs.ui.inventory"),
                PsychotropeDistilleryLayout.PLAYER_INV_X,
                standardInventoryLabelY(PsychotropeDistilleryLayout.PLAYER_INV_Y),
                0xFFE8ECEF,
                false
        );
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "drug_input" -> List.of(slotHighlight(PsychotropeDistilleryLayout.DRUG_SLOT_X, PsychotropeDistilleryLayout.DRUG_SLOT_Y));
            case "reagent_input" -> List.of(slotHighlight(PsychotropeDistilleryLayout.REAGENT_SLOT_X, PsychotropeDistilleryLayout.REAGENT_SLOT_Y));
            case "fuel" -> List.of(slotHighlight(PsychotropeDistilleryLayout.FUEL_SLOT_X, PsychotropeDistilleryLayout.FUEL_SLOT_Y));
            case "extract_output" -> List.of(slotHighlight(PsychotropeDistilleryLayout.EXTRACT_SLOT_X, PsychotropeDistilleryLayout.EXTRACT_SLOT_Y));
            case "residue_output" -> List.of(slotHighlight(PsychotropeDistilleryLayout.RESIDUE_SLOT_X, PsychotropeDistilleryLayout.RESIDUE_SLOT_Y));
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(PsychotropeDistilleryLayout.PROGRESS_X, PsychotropeDistilleryLayout.PROGRESS_Y,
                PsychotropeDistilleryLayout.PROGRESS_W, PsychotropeDistilleryLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.psychotrope_distillery.progress"),
                    Component.translatable("screen.mydrugs.ui.amount", this.menu.progress(), this.menu.maxProgress())
            );
        } else if (isHoveringBox(PsychotropeDistilleryLayout.BURN_X, PsychotropeDistilleryLayout.BURN_Y,
                PsychotropeDistilleryLayout.BURN_W, PsychotropeDistilleryLayout.BURN_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.fuel_burn"),
                    Component.translatable("screen.mydrugs.ui.amount", this.menu.burnTimeRemaining(), this.menu.burnTimeTotal())
            );
        }
    }
}
