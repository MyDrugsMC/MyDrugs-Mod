package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.blocks.entity.StillhouseBurnerBlockEntity;
import org.mydrugs.mydrugs.menu.StillhouseBurnerMenu;
import org.mydrugs.mydrugs.menu.layout.StillhouseBurnerLayout;

import java.util.List;

public final class StillhouseBurnerScreen extends AbstractMachineScreen<StillhouseBurnerMenu> {
    public StillhouseBurnerScreen(StillhouseBurnerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, StillhouseBurnerLayout.GUI_WIDTH, StillhouseBurnerLayout.GUI_HEIGHT);
    }

    @Override
    protected boolean shouldRenderSharedEnergyBar() {
        return false;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);
        drawSieveInventoryPanels(graphics, StillhouseBurnerLayout.PLAYER_INV_X, StillhouseBurnerLayout.PLAYER_INV_Y);
        drawSlotFrame(graphics, StillhouseBurnerLayout.FUEL_SLOT_X, StillhouseBurnerLayout.FUEL_SLOT_Y);
        drawVerticalBar(
                graphics,
                StillhouseBurnerLayout.FUEL_BAR_X,
                StillhouseBurnerLayout.FUEL_BAR_Y,
                StillhouseBurnerLayout.FUEL_BAR_W,
                StillhouseBurnerLayout.FUEL_BAR_H,
                2,
                2,
                StillhouseBurnerLayout.FUEL_BAR_W - 4,
                StillhouseBurnerLayout.FUEL_BAR_H - 4,
                this.menu.scaledFuel(StillhouseBurnerLayout.FUEL_BAR_H),
                0xFFB77C34,
                0xFFFFCF7A
        );
        drawHorizontalBar(
                graphics,
                StillhouseBurnerLayout.CURRENT_BAR_X,
                StillhouseBurnerLayout.CURRENT_BAR_Y,
                StillhouseBurnerLayout.CURRENT_BAR_W,
                StillhouseBurnerLayout.CURRENT_BAR_H,
                this.menu.scaledCurrent(StillhouseBurnerLayout.CURRENT_BAR_W),
                0xFF8E7CFF,
                0xFFCFC7FF
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xFFE8ECEF, false);
        graphics.drawString(this.font, Component.translatable("screen.mydrugs.stillhouse_burner.fuel"), StillhouseBurnerLayout.FUEL_BAR_X - 1, StillhouseBurnerLayout.FUEL_BAR_Y - 10, 0xFFFFCF7A, false);
        Component fuel = this.menu.fuelFluid() == Fluids.EMPTY
                ? Component.translatable("screen.mydrugs.ui.empty")
                : this.menu.fuelFluid().getFluidType().getDescription();
        graphics.drawString(this.font, fuel, StillhouseBurnerLayout.STATUS_X, StillhouseBurnerLayout.STATUS_Y, 0xFFE8ECEF, false);
        graphics.drawString(this.font, Component.translatable("screen.mydrugs.stillhouse_burner.current", this.menu.currentStored(), this.menu.currentCapacity()), StillhouseBurnerLayout.STATUS_X, StillhouseBurnerLayout.STATUS_Y + 11, 0xFFD5C9FF, false);
        graphics.drawString(this.font, Component.translatable("screen.mydrugs.stillhouse_burner.rate", this.menu.generationRate()), StillhouseBurnerLayout.STATUS_X, StillhouseBurnerLayout.STATUS_Y + 22, 0xFFE8ECEF, false);
        graphics.drawString(this.font, Component.translatable("screen.mydrugs.stillhouse_burner.targets", this.menu.validTargets()), StillhouseBurnerLayout.STATUS_X, StillhouseBurnerLayout.STATUS_Y + 33, 0xFFE8ECEF, false);
        graphics.drawString(this.font, Component.translatable("screen.mydrugs.ui.inventory"), StillhouseBurnerLayout.PLAYER_INV_X, standardInventoryLabelY(StillhouseBurnerLayout.PLAYER_INV_Y), 0xFFE8ECEF, false);
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "stillhouse_fuel" -> List.of(
                    tankHighlight(StillhouseBurnerLayout.FUEL_BAR_X, StillhouseBurnerLayout.FUEL_BAR_Y, StillhouseBurnerLayout.FUEL_BAR_W, StillhouseBurnerLayout.FUEL_BAR_H),
                    slotHighlight(StillhouseBurnerLayout.FUEL_SLOT_X, StillhouseBurnerLayout.FUEL_SLOT_Y)
            );
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(StillhouseBurnerLayout.FUEL_BAR_X, StillhouseBurnerLayout.FUEL_BAR_Y, StillhouseBurnerLayout.FUEL_BAR_W, StillhouseBurnerLayout.FUEL_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.stillhouse_burner.fuel_tank"),
                    this.menu.fuelFluid() == Fluids.EMPTY
                            ? Component.translatable("screen.mydrugs.ui.empty")
                            : this.menu.fuelFluid().getFluidType().getDescription(),
                    Component.translatable("screen.mydrugs.ui.amount_unit", this.menu.fuelAmount(), StillhouseBurnerBlockEntity.FUEL_CAPACITY_MB, "mB"),
                    Component.translatable("screen.mydrugs.stillhouse_burner.accepted_fuels"),
                    Component.translatable("screen.mydrugs.stillhouse_burner.fuel_value", this.menu.fuelPcPerMb())
            );
        } else if (isHoveringBox(StillhouseBurnerLayout.FUEL_SLOT_X, StillhouseBurnerLayout.FUEL_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.stillhouse_burner.fuel_slot"),
                    Component.translatable("screen.mydrugs.stillhouse_burner.fuel_slot_hint"),
                    Component.translatable("screen.mydrugs.stillhouse_burner.accepted_fuels")
            );
        } else if (isHoveringBox(StillhouseBurnerLayout.CURRENT_BAR_X, StillhouseBurnerLayout.CURRENT_BAR_Y, StillhouseBurnerLayout.CURRENT_BAR_W, StillhouseBurnerLayout.CURRENT_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.stillhouse_burner.current_buffer"),
                    Component.translatable("screen.mydrugs.ui.amount_unit", this.menu.currentStored(), this.menu.currentCapacity(), "PC"),
                    Component.translatable("screen.mydrugs.stillhouse_burner.rate", this.menu.generationRate()),
                    Component.translatable("screen.mydrugs.stillhouse_burner.targets_hint", this.menu.validTargets())
            );
        }
    }
}
