package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.GasifierMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.GasifierLayout;

public class GasifierScreen extends AbstractMachineScreen<GasifierMenu> {
    private static final int GAS_COLOR = 0xFF9FC75E;
    private static final int GAS_HIGHLIGHT = 0xFFDDF3AF;

    private static final int PROGRESS_COLOR = 0xFF88B85D;
    private static final int PROGRESS_HIGHLIGHT = 0xFFD8EEA9;

    private static final int FUEL_COLOR = 0xFFE3A44B;
    private static final int FUEL_HIGHLIGHT = 0xFFFFD28E;

    public GasifierScreen(GasifierMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, GasifierLayout.GUI_WIDTH, GasifierLayout.GUI_HEIGHT);
        this.inventoryLabelY = GasifierLayout.PLAYER_INV_Y - 10;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineGuiRenderer.drawGasifier(
                this,
                graphics,
                new MachineGuiRenderer.GasifierState(
                        this.menu.getScaledFuel(GasifierLayout.FUEL_BAR_INNER_H),
                        this.menu.getScaledProgress(GasifierLayout.PROGRESS_W),
                        MachineGuiRenderer.TankFill.liveColor(this.menu.getScaledGasTank(GasifierLayout.TANK_INNER_H), GAS_COLOR)
                ),
                true
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawGasifierLabels(this, graphics, this.font, this.title, this.playerInventoryTitle, this.inventoryLabelY, null);
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(GasifierLayout.OUTPUT_TANK_X, GasifierLayout.OUTPUT_TANK_Y, GasifierLayout.TANK_W, GasifierLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output gas tank"),
                    this.menu.getGasName(),
                    Component.literal(this.menu.getGasAmount() + " / " + GasifierMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(GasifierLayout.PROGRESS_X, GasifierLayout.PROGRESS_Y, GasifierLayout.PROGRESS_W, GasifierLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Gasification progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        } else if (isHoveringBox(GasifierLayout.FUEL_BAR_X, GasifierLayout.FUEL_BAR_Y, GasifierLayout.FUEL_BAR_W, GasifierLayout.FUEL_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Fuel burn"),
                    this.menu.isLit() ? Component.literal("Burning") : Component.literal("Idle"),
                    Component.literal(this.menu.getFuelLeft() + " / " + this.menu.getFuelTotal() + " ticks")
            );
        } else if (isHoveringBox(GasifierLayout.FUEL_SLOT_X, GasifierLayout.FUEL_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Fuel slot"),
                    Component.literal("Accepts furnace fuels")
            );
        } else if (isHoveringBox(GasifierLayout.EXPORT_SLOT_X, GasifierLayout.EXPORT_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Gas Tank link slot"),
                    Component.literal("Insert a Gas Tank block item"),
                    Component.literal("Exports to the placed tank in front")
            );
        }
    }
}
