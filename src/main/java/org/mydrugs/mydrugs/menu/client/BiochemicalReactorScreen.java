package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.BiochemicalReactorMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.BiochemicalReactorLayout;

import java.util.List;

public class BiochemicalReactorScreen extends AbstractMachineScreen<BiochemicalReactorMenu> {
    private InvisibleButton manualBoostButton;

    public BiochemicalReactorScreen(BiochemicalReactorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, BiochemicalReactorLayout.GUI_WIDTH, BiochemicalReactorLayout.GUI_HEIGHT);
        this.titleLabelX = 14;
        this.titleLabelY = 6;
        this.inventoryLabelX = BiochemicalReactorLayout.PLAYER_INV_X;
        this.inventoryLabelY = standardInventoryLabelY(BiochemicalReactorLayout.PLAYER_INV_Y);
    }

    @Override
    protected void init() {
        super.init();

        this.manualBoostButton = this.addRenderableWidget(new InvisibleButton(
                guiX(BiochemicalReactorLayout.MANUAL_BUTTON_X),
                guiY(BiochemicalReactorLayout.MANUAL_BUTTON_Y),
                BiochemicalReactorLayout.MANUAL_BUTTON_W,
                BiochemicalReactorLayout.MANUAL_BUTTON_H,
                button -> pressMenuButton(BiochemicalReactorMenu.MANUAL_BOOST_BUTTON_ID)
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineGuiRenderer.drawBiochemicalReactor(
                this,
                graphics,
                MachineGuiRenderer.BiochemicalReactorState.screen(
                        this.menu.getScaledProgress(BiochemicalReactorLayout.PROGRESS_W),
                        this.menu.getScaledHeat(BiochemicalReactorLayout.HEAT_BAR_INNER_H),
                        this.menu.getScaledManualEnergy(BiochemicalReactorLayout.MANUAL_BAR_INNER_H),
                        this.menu.getOutputFluid(),
                        this.menu.getScaledOutputTank(BiochemicalReactorLayout.TANK_INNER_H),
                        this.manualBoostButton != null && this.manualBoostButton.isHoveredOrFocused(),
                        this.menu.isWorking() ? "Processing" : "Idle"
                ),
                true
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawBiochemicalReactorLabels(
                this,
                graphics,
                this.font,
                this.title,
                this.menu.isWorking() ? "Processing" : "Idle"
        );
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "item_input" -> List.of(
                    slotHighlight(BiochemicalReactorLayout.ERGOT_SLOT_X, BiochemicalReactorLayout.ERGOT_SLOT_Y),
                    slotHighlight(BiochemicalReactorLayout.TRYPTOPHAN_SLOT_X, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_Y),
                    slotHighlight(BiochemicalReactorLayout.CHARCOAL_SLOT_X, BiochemicalReactorLayout.CHARCOAL_SLOT_Y)
            );
            case "item_output" -> List.of(slotHighlight(BiochemicalReactorLayout.OUTPUT_SLOT_X, BiochemicalReactorLayout.OUTPUT_SLOT_Y));
            case "fluid_output" -> List.of(
                    tankHighlight(BiochemicalReactorLayout.OUTPUT_TANK_X, BiochemicalReactorLayout.OUTPUT_TANK_Y, BiochemicalReactorLayout.TANK_W, BiochemicalReactorLayout.TANK_H),
                    slotHighlight(BiochemicalReactorLayout.OUTPUT_SLOT_X, BiochemicalReactorLayout.OUTPUT_SLOT_Y)
            );
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(BiochemicalReactorLayout.PROGRESS_X, BiochemicalReactorLayout.PROGRESS_Y,
                BiochemicalReactorLayout.PROGRESS_W, BiochemicalReactorLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.progress"),
                    Component.literal(this.menu.getProgressUnits() + " / " + this.menu.getMaxProgressUnits() + " units")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.HEAT_BAR_X, BiochemicalReactorLayout.HEAT_BAR_Y,
                BiochemicalReactorLayout.HEAT_BAR_W, BiochemicalReactorLayout.HEAT_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.heat"),
                    Component.literal(this.menu.getHeat() + " / " + this.menu.getMaxHeat()),
                    Component.translatable("screen.mydrugs.ui.raises_processing_speed")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.MANUAL_BAR_X, BiochemicalReactorLayout.MANUAL_BAR_Y,
                BiochemicalReactorLayout.MANUAL_BAR_W, BiochemicalReactorLayout.MANUAL_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.manual_energy"),
                    Component.literal(this.menu.getManualEnergy() + " / " + this.menu.getMaxManualEnergy()),
                    Component.translatable("screen.mydrugs.ui.generated_by_player_interaction")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.OUTPUT_TANK_X, BiochemicalReactorLayout.OUTPUT_TANK_Y,
                BiochemicalReactorLayout.TANK_W, BiochemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.output_tank"),
                    Component.literal(getFluidName(this.menu.getOutputFluid())),
                    Component.literal(this.menu.getOutputTankAmount() + " / " + this.menu.getOutputTankCapacity() + " mB")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.MANUAL_BUTTON_X, BiochemicalReactorLayout.MANUAL_BUTTON_Y,
                BiochemicalReactorLayout.MANUAL_BUTTON_W, BiochemicalReactorLayout.MANUAL_BUTTON_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.manual_boost"),
                    Component.translatable("screen.mydrugs.ui.click_to_add_manual_energy")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.ERGOT_SLOT_X, BiochemicalReactorLayout.ERGOT_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.ergot"),
                    Component.translatable("screen.mydrugs.ui.more_ergot_in_this_slot_increases_speed")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.TRYPTOPHAN_SLOT_X, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.tryptophan"));
        } else if (isHoveringBox(BiochemicalReactorLayout.CHARCOAL_SLOT_X, BiochemicalReactorLayout.CHARCOAL_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.charcoal"),
                    Component.translatable("screen.mydrugs.ui.adds_heat_to_the_reactor")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.OUTPUT_SLOT_X, BiochemicalReactorLayout.OUTPUT_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.output_container"));
        }
    }
}
