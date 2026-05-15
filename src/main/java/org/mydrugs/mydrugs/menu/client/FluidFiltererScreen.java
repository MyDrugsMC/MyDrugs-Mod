package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.machine.manual.ManualMachineType;
import org.mydrugs.mydrugs.menu.FluidFiltererMenu;
import org.mydrugs.mydrugs.menu.client.util.DrugBonusClientText;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.FluidFiltererLayout;

import java.util.List;

public class FluidFiltererScreen extends AbstractMachineScreen<FluidFiltererMenu> {
    private boolean holdingRunButton = false;
    private InvisibleButton dumpInputButton;
    private InvisibleButton dumpOutputAButton;

    public FluidFiltererScreen(FluidFiltererMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, FluidFiltererLayout.GUI_WIDTH, FluidFiltererLayout.GUI_HEIGHT);
        this.titleLabelX = 14;
        this.titleLabelY = FluidFiltererLayout.TITLE_Y;
        this.inventoryLabelX = FluidFiltererLayout.PLAYER_INV_X;
        this.inventoryLabelY = FluidFiltererLayout.PLAYER_INV_Y - 10;
    }

    @Override
    protected boolean shouldRenderSharedEnergyBar() {
        return false;
    }

    @Override
    protected void init() {
        super.init();

        this.dumpInputButton = this.addRenderableWidget(new InvisibleButton(
                guiX(FluidFiltererLayout.DUMP_INPUT_X),
                guiY(FluidFiltererLayout.DUMP_BUTTON_Y),
                FluidFiltererLayout.DUMP_BUTTON_SIZE,
                FluidFiltererLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(FluidFiltererMenu.DUMP_INPUT_BUTTON_ID)
        ));

        this.dumpOutputAButton = this.addRenderableWidget(new InvisibleButton(
                guiX(FluidFiltererLayout.DUMP_OUTPUT_A_X),
                guiY(FluidFiltererLayout.DUMP_BUTTON_Y),
                FluidFiltererLayout.DUMP_BUTTON_SIZE,
                FluidFiltererLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(FluidFiltererMenu.DUMP_OUTPUT_A_BUTTON_ID)
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineGuiRenderer.drawFluidFilterer(
                this,
                graphics,
                new MachineGuiRenderer.FluidFiltererState(
                        MachineGuiRenderer.TankFill.live(this.menu.getInputFluid(), this.menu.getScaledInputTank(FluidFiltererLayout.TANK_INNER_H)),
                        MachineGuiRenderer.TankFill.live(this.menu.getOutputAFluid(), this.menu.getScaledOutputATank(FluidFiltererLayout.TANK_INNER_H)),
                        this.menu.getScaledProgress(FluidFiltererLayout.PROGRESS_W),
                        this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                        this.dumpOutputAButton != null && this.dumpOutputAButton.isHoveredOrFocused(),
                        this.menu.getInputTankAmount() > 0,
                        this.menu.getOutputATankAmount() > 0,
                        isHoveringBox(FluidFiltererLayout.RUN_BUTTON_X, FluidFiltererLayout.RUN_BUTTON_Y, FluidFiltererLayout.RUN_BUTTON_W, FluidFiltererLayout.RUN_BUTTON_H, mouseX, mouseY),
                        this.holdingRunButton || this.menu.isButtonHeld(),
                        this.menu.getMaxProgress() > 0 ? this.menu.getProgress() + " / " + this.menu.getMaxProgress() : null
                ),
                true
        );
        if (this.menu.hasEnergyStorage()) {
            drawExternalEnergyBar(graphics, this.menu.getEnergyStored(), this.menu.getEnergyCapacity());
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawFluidFiltererLabels(this, graphics, this.font, this.title, null);
        DrugBonusClientText.drawManualWorkBonus(graphics, this.font, -leftPos + 5, 12, ManualMachineType.FLUID_FILTERER);
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "item_input" -> List.of(
                    slotHighlight(FluidFiltererLayout.INPUT_SLOT_X, FluidFiltererLayout.INPUT_SLOT_Y),
                    slotHighlight(FluidFiltererLayout.FILTER_SLOT_X, FluidFiltererLayout.FILTER_SLOT_Y)
            );
            case "item_output" -> List.of(
                    slotHighlight(FluidFiltererLayout.OUTPUT_A_SLOT_X, FluidFiltererLayout.OUTPUT_A_SLOT_Y),
                    slotHighlight(FluidFiltererLayout.RESIDUE_SLOT_X, FluidFiltererLayout.RESIDUE_SLOT_Y)
            );
            case "fluid_input" -> List.of(
                    tankHighlight(FluidFiltererLayout.INPUT_TANK_X, FluidFiltererLayout.INPUT_TANK_Y, FluidFiltererLayout.TANK_W, FluidFiltererLayout.TANK_H),
                    slotHighlight(FluidFiltererLayout.INPUT_SLOT_X, FluidFiltererLayout.INPUT_SLOT_Y)
            );
            case "fluid_output" -> List.of(
                    tankHighlight(FluidFiltererLayout.OUTPUT_A_TANK_X, FluidFiltererLayout.OUTPUT_A_TANK_Y, FluidFiltererLayout.TANK_W, FluidFiltererLayout.TANK_H),
                    slotHighlight(FluidFiltererLayout.OUTPUT_A_SLOT_X, FluidFiltererLayout.OUTPUT_A_SLOT_Y)
            );
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.menu.hasEnergyStorage() && isHoveringBox(-18, 24, 10, 50, mouseX, mouseY)) {
            renderExternalEnergyTooltip(graphics, mouseX, mouseY, this.menu.getEnergyStored(), this.menu.getEnergyCapacity());
        } else if (isHoveringBox(FluidFiltererLayout.DUMP_INPUT_X, FluidFiltererLayout.DUMP_BUTTON_Y, FluidFiltererLayout.DUMP_BUTTON_SIZE, FluidFiltererLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.dump_input_tank"));
        } else if (isHoveringBox(FluidFiltererLayout.DUMP_OUTPUT_A_X, FluidFiltererLayout.DUMP_BUTTON_Y, FluidFiltererLayout.DUMP_BUTTON_SIZE, FluidFiltererLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.dump_output_tank"));
        } else if (isHoveringBox(FluidFiltererLayout.INPUT_TANK_X, FluidFiltererLayout.INPUT_TANK_Y, FluidFiltererLayout.TANK_W, FluidFiltererLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.input_tank"),
                    Component.literal(getFluidName(this.menu.getInputFluid())),
                    Component.literal(this.menu.getInputTankAmount() + " / " + FluidFiltererMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(FluidFiltererLayout.OUTPUT_A_TANK_X, FluidFiltererLayout.OUTPUT_A_TANK_Y, FluidFiltererLayout.TANK_W, FluidFiltererLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.output_tank"),
                    Component.literal(getFluidName(this.menu.getOutputAFluid())),
                    Component.literal(this.menu.getOutputATankAmount() + " / " + FluidFiltererMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(FluidFiltererLayout.INPUT_SLOT_X, FluidFiltererLayout.INPUT_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.input_fluid_container"));
        } else if (isHoveringBox(FluidFiltererLayout.OUTPUT_A_SLOT_X, FluidFiltererLayout.OUTPUT_A_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.output_container"));
        } else if (isHoveringBox(FluidFiltererLayout.PROGRESS_X, FluidFiltererLayout.PROGRESS_Y, FluidFiltererLayout.PROGRESS_W, FluidFiltererLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.filtering_progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        } else if (isHoveringBox(FluidFiltererLayout.RUN_BUTTON_X, FluidFiltererLayout.RUN_BUTTON_Y, FluidFiltererLayout.RUN_BUTTON_W, FluidFiltererLayout.RUN_BUTTON_H, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.hold_to_filter"));
        } else if (isHoveringBox(FluidFiltererLayout.FILTER_SLOT_X, FluidFiltererLayout.FILTER_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.filter_slot"));
        } else if (isHoveringBox(FluidFiltererLayout.RESIDUE_SLOT_X, FluidFiltererLayout.RESIDUE_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.waste_output"));
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClicked) {
        if (event.button() == 0 && isHoveringBox(
                FluidFiltererLayout.RUN_BUTTON_X,
                FluidFiltererLayout.RUN_BUTTON_Y,
                FluidFiltererLayout.RUN_BUTTON_W,
                FluidFiltererLayout.RUN_BUTTON_H,
                event.x(),
                event.y()
        )) {
            pressMenuButton(FluidFiltererMenu.RUN_BUTTON_START_ID);
            this.holdingRunButton = true;
            return true;
        }

        return super.mouseClicked(event, doubleClicked);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && this.holdingRunButton) {
            pressMenuButton(FluidFiltererMenu.RUN_BUTTON_STOP_ID);
            this.holdingRunButton = false;
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    public void removed() {
        if (this.holdingRunButton) {
            pressMenuButton(FluidFiltererMenu.RUN_BUTTON_STOP_ID);
        }
        this.holdingRunButton = false;
        super.removed();
    }

}
