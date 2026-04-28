package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.AdvancedFurnaceBlockEntity;
import org.mydrugs.mydrugs.menu.AdvancedFurnaceMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.AdvancedFurnaceLayout;
import org.mydrugs.mydrugs.menu.layout.StandardTankLayout;

import java.util.List;

public final class AdvancedFurnaceScreen extends AbstractMachineScreen<AdvancedFurnaceMenu> {
    public AdvancedFurnaceScreen(AdvancedFurnaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, AdvancedFurnaceLayout.GUI_WIDTH, AdvancedFurnaceLayout.GUI_HEIGHT);
        this.inventoryLabelY = standardInventoryLabelY(AdvancedFurnaceLayout.PLAYER_INV_Y);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineGuiRenderer.drawAdvancedFurnace(
                this,
                graphics,
                MachineGuiRenderer.AdvancedFurnaceState.screen(
                        this.menu.getScaledProgress(AdvancedFurnaceLayout.PROGRESS_W),
                        this.menu.getScaledBurn(AdvancedFurnaceLayout.BURN_W),
                        this.menu.getTankFluid(),
                        this.menu.getScaledTank(StandardTankLayout.INNER_H)
                ),
                true
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawAdvancedFurnaceLabels(
                this,
                graphics,
                this.font,
                this.title,
                this.playerInventoryTitle,
                this.inventoryLabelY
        );
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "item_input" -> List.of(
                    slotHighlight(AdvancedFurnaceLayout.INPUT_A_X, AdvancedFurnaceLayout.INPUT_A_Y),
                    slotHighlight(AdvancedFurnaceLayout.INPUT_B_X, AdvancedFurnaceLayout.INPUT_B_Y)
            );
            case "fuel" -> List.of(slotHighlight(AdvancedFurnaceLayout.FUEL_X, AdvancedFurnaceLayout.FUEL_Y));
            case "item_output" -> List.of(
                    slotHighlight(AdvancedFurnaceLayout.OUTPUT_A_X, AdvancedFurnaceLayout.OUTPUT_A_Y),
                    slotHighlight(AdvancedFurnaceLayout.OUTPUT_B_X, AdvancedFurnaceLayout.OUTPUT_B_Y)
            );
            case "fluid_output" -> List.of(
                    tankHighlight(AdvancedFurnaceLayout.TANK_X, AdvancedFurnaceLayout.TANK_Y, StandardTankLayout.TANK_W, StandardTankLayout.TANK_H),
                    slotHighlight(AdvancedFurnaceLayout.OUTPUT_CONTAINER_X, AdvancedFurnaceLayout.OUTPUT_CONTAINER_Y)
            );
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(
                AdvancedFurnaceLayout.TANK_X,
                AdvancedFurnaceLayout.TANK_Y,
                StandardTankLayout.TANK_W,
                StandardTankLayout.TANK_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output Tank"),
                    Component.literal(getFluidName(this.menu.getTankFluid())),
                    Component.literal(this.menu.getTankAmount() + " / " + AdvancedFurnaceBlockEntity.TANK_CAPACITY + " mB")
            );
        }

        if (isHoveringBox(
                AdvancedFurnaceLayout.PROGRESS_X,
                AdvancedFurnaceLayout.PROGRESS_Y,
                AdvancedFurnaceLayout.PROGRESS_W,
                AdvancedFurnaceLayout.PROGRESS_H,
                mouseX,
                mouseY
        )) {
            renderSimpleAmountTooltip(
                    graphics,
                    mouseX,
                    mouseY,
                    "Progress",
                    this.menu.getProgress(),
                    this.menu.getMaxProgress(),
                    "ticks"
            );
        }

        if (isHoveringBox(
                AdvancedFurnaceLayout.BURN_X,
                AdvancedFurnaceLayout.BURN_Y,
                AdvancedFurnaceLayout.BURN_W,
                AdvancedFurnaceLayout.BURN_H,
                mouseX,
                mouseY
        )) {
            renderSimpleAmountTooltip(
                    graphics,
                    mouseX,
                    mouseY,
                    "Burn",
                    this.menu.getBurnTime(),
                    this.menu.getBurnDuration(),
                    "ticks"
            );
        }
    }
}
