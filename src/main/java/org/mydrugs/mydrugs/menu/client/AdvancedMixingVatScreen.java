package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.AdvancedMixingVatBlockEntity;
import org.mydrugs.mydrugs.menu.AdvancedMixingVatMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.AdvancedMixingVatLayout;

import java.util.List;

public class AdvancedMixingVatScreen extends AbstractMachineScreen<AdvancedMixingVatMenu> {
    public AdvancedMixingVatScreen(AdvancedMixingVatMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, AdvancedMixingVatLayout.GUI_WIDTH, AdvancedMixingVatLayout.GUI_HEIGHT);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineGuiRenderer.drawAdvancedMixingVat(
                this,
                graphics,
                MachineGuiRenderer.AdvancedMixingVatState.screen(
                        this.menu.getInputAFluid(),
                        this.menu.getScaledTank(this.menu.getInputATankAmount(), AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY, AdvancedMixingVatLayout.TANK_INNER_H),
                        this.menu.getInputBFluid(),
                        this.menu.getScaledTank(this.menu.getInputBTankAmount(), AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY, AdvancedMixingVatLayout.TANK_INNER_H),
                        this.menu.getInputCFluid(),
                        this.menu.getScaledTank(this.menu.getInputCTankAmount(), AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY, AdvancedMixingVatLayout.TANK_INNER_H),
                        this.menu.getScaledTank(this.menu.getGasAmount(), (int) AdvancedMixingVatBlockEntity.GAS_TANK_CAPACITY, AdvancedMixingVatLayout.TANK_INNER_H),
                        this.menu.getOutputFluid(),
                        this.menu.getScaledTank(this.menu.getOutputTankAmount(), AdvancedMixingVatBlockEntity.OUTPUT_TANK_CAPACITY, AdvancedMixingVatLayout.TANK_INNER_H),
                        this.menu.getScaledProgress(AdvancedMixingVatLayout.PROGRESS_W)
                ),
                true
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawAdvancedMixingVatLabels(this, graphics, this.font, this.title, null);
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "item_input" -> List.of(
                    slotHighlight(AdvancedMixingVatLayout.ITEM_0_X, AdvancedMixingVatLayout.ITEM_0_Y),
                    slotHighlight(AdvancedMixingVatLayout.ITEM_1_X, AdvancedMixingVatLayout.ITEM_1_Y),
                    slotHighlight(AdvancedMixingVatLayout.ITEM_2_X, AdvancedMixingVatLayout.ITEM_2_Y),
                    slotHighlight(AdvancedMixingVatLayout.ITEM_3_X, AdvancedMixingVatLayout.ITEM_3_Y)
            );
            case "fluid_input_a" -> List.of(
                    tankHighlight(AdvancedMixingVatLayout.TANK_A_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H),
                    slotHighlight(AdvancedMixingVatLayout.TANK_A_SLOT_X, AdvancedMixingVatLayout.TANK_SLOT_Y)
            );
            case "fluid_input_b" -> List.of(
                    tankHighlight(AdvancedMixingVatLayout.TANK_B_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H),
                    slotHighlight(AdvancedMixingVatLayout.TANK_B_SLOT_X, AdvancedMixingVatLayout.TANK_SLOT_Y)
            );
            case "fluid_input_c" -> List.of(
                    tankHighlight(AdvancedMixingVatLayout.TANK_C_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H),
                    slotHighlight(AdvancedMixingVatLayout.TANK_C_SLOT_X, AdvancedMixingVatLayout.TANK_SLOT_Y)
            );
            case "gas_input" -> List.of(
                    tankHighlight(AdvancedMixingVatLayout.GAS_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H),
                    slotHighlight(AdvancedMixingVatLayout.GAS_SLOT_X, AdvancedMixingVatLayout.TANK_SLOT_Y)
            );
            case "item_output" -> List.of(slotHighlight(AdvancedMixingVatLayout.OUTPUT_SLOT_X, AdvancedMixingVatLayout.TANK_SLOT_Y));
            case "fluid_output" -> List.of(
                    tankHighlight(AdvancedMixingVatLayout.OUTPUT_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H),
                    slotHighlight(AdvancedMixingVatLayout.OUTPUT_SLOT_X, AdvancedMixingVatLayout.TANK_SLOT_Y)
            );
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(
                AdvancedMixingVatLayout.TANK_A_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.fluid_input_a"),
                    Component.literal(getFluidName(this.menu.getInputAFluid())),
                    Component.literal(this.menu.getInputATankAmount() + " / " + AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(
                AdvancedMixingVatLayout.TANK_B_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.fluid_input_b"),
                    Component.literal(getFluidName(this.menu.getInputBFluid())),
                    Component.literal(this.menu.getInputBTankAmount() + " / " + AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(
                AdvancedMixingVatLayout.TANK_C_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.fluid_input_c"),
                    Component.literal(getFluidName(this.menu.getInputCFluid())),
                    Component.literal(this.menu.getInputCTankAmount() + " / " + AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(
                AdvancedMixingVatLayout.GAS_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.gas_input"),
                    Component.literal(this.menu.getGasAmount() + " / " + AdvancedMixingVatBlockEntity.GAS_TANK_CAPACITY + " units")
            );
        } else if (isHoveringBox(
                AdvancedMixingVatLayout.OUTPUT_X,
                AdvancedMixingVatLayout.TANK_Y,
                AdvancedMixingVatLayout.TANK_W,
                AdvancedMixingVatLayout.TANK_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.fluid_output"),
                    Component.literal(getFluidName(this.menu.getOutputFluid())),
                    Component.literal(this.menu.getOutputTankAmount() + " / " + AdvancedMixingVatBlockEntity.OUTPUT_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(
                AdvancedMixingVatLayout.PROGRESS_X,
                AdvancedMixingVatLayout.PROGRESS_Y,
                AdvancedMixingVatLayout.PROGRESS_W,
                AdvancedMixingVatLayout.PROGRESS_H,
                mouseX,
                mouseY
        )) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.mixing_progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        }
    }
}
