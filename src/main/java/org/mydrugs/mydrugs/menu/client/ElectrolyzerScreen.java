package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.ElectrolyzerMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.ElectrolyzerLayout;

import java.util.ArrayList;
import java.util.List;

public class ElectrolyzerScreen extends AbstractMachineScreen<ElectrolyzerMenu> {
    private InvisibleButton dumpInputButton;
    private InvisibleButton dumpOutput1Button;
    private InvisibleButton dumpOutput2Button;
    private InvisibleButton dumpOutput3Button;

    public ElectrolyzerScreen(ElectrolyzerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, ElectrolyzerLayout.GUI_WIDTH, ElectrolyzerLayout.GUI_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        this.dumpInputButton = this.addRenderableWidget(new InvisibleButton(
                guiX(ElectrolyzerLayout.DUMP_INPUT_X),
                guiY(ElectrolyzerLayout.DUMP_BUTTON_Y),
                ElectrolyzerLayout.DUMP_BUTTON_SIZE,
                ElectrolyzerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(ElectrolyzerMenu.DUMP_INPUT_BUTTON_ID)
        ));

        this.dumpOutput1Button = this.addRenderableWidget(new InvisibleButton(
                guiX(ElectrolyzerLayout.DUMP_OUTPUT_1_X),
                guiY(ElectrolyzerLayout.DUMP_BUTTON_Y),
                ElectrolyzerLayout.DUMP_BUTTON_SIZE,
                ElectrolyzerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(ElectrolyzerMenu.DUMP_OUTPUT_1_BUTTON_ID)
        ));

        this.dumpOutput2Button = this.addRenderableWidget(new InvisibleButton(
                guiX(ElectrolyzerLayout.DUMP_OUTPUT_2_X),
                guiY(ElectrolyzerLayout.DUMP_BUTTON_Y),
                ElectrolyzerLayout.DUMP_BUTTON_SIZE,
                ElectrolyzerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(ElectrolyzerMenu.DUMP_OUTPUT_2_BUTTON_ID)
        ));

        this.dumpOutput3Button = this.addRenderableWidget(new InvisibleButton(
                guiX(ElectrolyzerLayout.DUMP_OUTPUT_3_X),
                guiY(ElectrolyzerLayout.DUMP_BUTTON_Y),
                ElectrolyzerLayout.DUMP_BUTTON_SIZE,
                ElectrolyzerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(ElectrolyzerMenu.DUMP_OUTPUT_3_BUTTON_ID)
        ));
    }

    private int getOutput1Color() {
        return this.menu.isOutput1GasMode()
                ? this.menu.getOutput1GasColor()
                : getFluidColor(this.menu.getOutput1Fluid());
    }

    private int getOutput2Color() {
        return this.menu.isOutput2GasMode()
                ? this.menu.getOutput2GasColor()
                : getFluidColor(this.menu.getOutput2Fluid());
    }

    private int getOutput3Color() {
        return this.menu.isOutput3GasMode()
                ? this.menu.getOutput3GasColor()
                : getFluidColor(this.menu.getOutput3Fluid());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineGuiRenderer.drawElectrolyzer(
                this,
                graphics,
                new MachineGuiRenderer.ElectrolyzerState(
                        MachineGuiRenderer.TankFill.live(this.menu.getInputFluid(), this.menu.getScaledInputTank(ElectrolyzerLayout.TANK_INNER_H)),
                        MachineGuiRenderer.TankFill.liveColor(this.menu.getScaledOutput1Tank(ElectrolyzerLayout.TANK_INNER_H), getOutput1Color()),
                        MachineGuiRenderer.TankFill.liveColor(this.menu.getScaledOutput2Tank(ElectrolyzerLayout.TANK_INNER_H), getOutput2Color()),
                        MachineGuiRenderer.TankFill.liveColor(this.menu.getScaledOutput3Tank(ElectrolyzerLayout.TANK_INNER_H), getOutput3Color()),
                        this.menu.getScaledProgress(ElectrolyzerLayout.PROGRESS_W),
                        this.menu.getScaledBurnTime(ElectrolyzerLayout.FUEL_BAR_INNER_H),
                        this.menu.isLit() ? 0xFFE38D3F : 0xFF8E6A4A,
                        this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                        this.dumpOutput1Button != null && this.dumpOutput1Button.isHoveredOrFocused(),
                        this.dumpOutput2Button != null && this.dumpOutput2Button.isHoveredOrFocused(),
                        this.dumpOutput3Button != null && this.dumpOutput3Button.isHoveredOrFocused(),
                        this.menu.getInputTankAmount() > 0,
                        this.menu.getOutput1TankAmount() > 0,
                        this.menu.getOutput2TankAmount() > 0,
                        this.menu.getOutput3TankAmount() > 0
                ),
                true
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawElectrolyzerLabels(this, graphics, this.font, this.title);
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "item_input" -> List.of(
                    slotHighlight(ElectrolyzerLayout.INPUT_SLOT_X, ElectrolyzerLayout.INPUT_SLOT_Y),
                    slotHighlight(ElectrolyzerLayout.FUEL_SLOT_X, ElectrolyzerLayout.FUEL_SLOT_Y)
            );
            case "item_output" -> List.of(
                    slotHighlight(ElectrolyzerLayout.OUTPUT_1_SLOT_X, ElectrolyzerLayout.OUTPUT_1_SLOT_Y),
                    slotHighlight(ElectrolyzerLayout.OUTPUT_2_SLOT_X, ElectrolyzerLayout.OUTPUT_2_SLOT_Y),
                    slotHighlight(ElectrolyzerLayout.OUTPUT_3_SLOT_X, ElectrolyzerLayout.OUTPUT_3_SLOT_Y)
            );
            case "fluid_input" -> List.of(
                    tankHighlight(ElectrolyzerLayout.INPUT_TANK_X, ElectrolyzerLayout.INPUT_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H),
                    slotHighlight(ElectrolyzerLayout.INPUT_SLOT_X, ElectrolyzerLayout.INPUT_SLOT_Y)
            );
            case "fluid_output" -> outputHighlights(false);
            case "gas_output" -> outputHighlights(true);
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    private List<TransferHighlight> outputHighlights(boolean gasMode) {
        ArrayList<TransferHighlight> highlights = new ArrayList<>();
        addOutputHighlight(highlights, gasMode, this.menu.isOutput1GasMode(), ElectrolyzerLayout.OUTPUT_1_TANK_X, ElectrolyzerLayout.OUTPUT_1_TANK_Y, ElectrolyzerLayout.OUTPUT_1_SLOT_X, ElectrolyzerLayout.OUTPUT_1_SLOT_Y);
        addOutputHighlight(highlights, gasMode, this.menu.isOutput2GasMode(), ElectrolyzerLayout.OUTPUT_2_TANK_X, ElectrolyzerLayout.OUTPUT_2_TANK_Y, ElectrolyzerLayout.OUTPUT_2_SLOT_X, ElectrolyzerLayout.OUTPUT_2_SLOT_Y);
        addOutputHighlight(highlights, gasMode, this.menu.isOutput3GasMode(), ElectrolyzerLayout.OUTPUT_3_TANK_X, ElectrolyzerLayout.OUTPUT_3_TANK_Y, ElectrolyzerLayout.OUTPUT_3_SLOT_X, ElectrolyzerLayout.OUTPUT_3_SLOT_Y);
        return highlights.isEmpty() ? allOutputHighlights() : highlights;
    }

    private static void addOutputHighlight(List<TransferHighlight> highlights, boolean expectedGasMode, boolean actualGasMode, int tankX, int tankY, int slotX, int slotY) {
        if (actualGasMode != expectedGasMode) {
            return;
        }
        highlights.add(tankHighlight(tankX, tankY, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H));
        highlights.add(slotHighlight(slotX, slotY));
    }

    private static List<TransferHighlight> allOutputHighlights() {
        return List.of(
                tankHighlight(ElectrolyzerLayout.OUTPUT_1_TANK_X, ElectrolyzerLayout.OUTPUT_1_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H),
                tankHighlight(ElectrolyzerLayout.OUTPUT_2_TANK_X, ElectrolyzerLayout.OUTPUT_2_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H),
                tankHighlight(ElectrolyzerLayout.OUTPUT_3_TANK_X, ElectrolyzerLayout.OUTPUT_3_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H),
                slotHighlight(ElectrolyzerLayout.OUTPUT_1_SLOT_X, ElectrolyzerLayout.OUTPUT_1_SLOT_Y),
                slotHighlight(ElectrolyzerLayout.OUTPUT_2_SLOT_X, ElectrolyzerLayout.OUTPUT_2_SLOT_Y),
                slotHighlight(ElectrolyzerLayout.OUTPUT_3_SLOT_X, ElectrolyzerLayout.OUTPUT_3_SLOT_Y)
        );
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(ElectrolyzerLayout.DUMP_INPUT_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, ElectrolyzerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump input tank"));
        } else if (isHoveringBox(ElectrolyzerLayout.DUMP_OUTPUT_1_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, ElectrolyzerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output tank 1"));
        } else if (isHoveringBox(ElectrolyzerLayout.DUMP_OUTPUT_2_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, ElectrolyzerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output tank 2"));
        } else if (isHoveringBox(ElectrolyzerLayout.DUMP_OUTPUT_3_X, ElectrolyzerLayout.DUMP_BUTTON_Y, ElectrolyzerLayout.DUMP_BUTTON_SIZE, ElectrolyzerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output tank 3"));
        } else if (isHoveringBox(ElectrolyzerLayout.INPUT_TANK_X, ElectrolyzerLayout.INPUT_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Input tank"),
                    Component.literal(getFluidName(this.menu.getInputFluid())),
                    Component.literal(this.menu.getInputTankAmount() + " / " + ElectrolyzerMenu.FLUID_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(ElectrolyzerLayout.OUTPUT_1_TANK_X, ElectrolyzerLayout.OUTPUT_1_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, mouseX, mouseY)) {
            if (this.menu.isOutput1GasMode()) {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output gas tank 1"),
                        Component.literal(this.menu.getOutput1GasName()),
                        Component.literal(this.menu.getOutput1TankAmount() + " / " + ElectrolyzerMenu.GAS_TANK_CAPACITY)
                );
            } else {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output fluid tank 1"),
                        Component.literal(getFluidName(this.menu.getOutput1Fluid())),
                        Component.literal(this.menu.getOutput1TankAmount() + " / " + ElectrolyzerMenu.FLUID_TANK_CAPACITY + " mB")
                );
            }
        } else if (isHoveringBox(ElectrolyzerLayout.OUTPUT_2_TANK_X, ElectrolyzerLayout.OUTPUT_2_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, mouseX, mouseY)) {
            if (this.menu.isOutput2GasMode()) {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output gas tank 2"),
                        Component.literal(this.menu.getOutput2GasName()),
                        Component.literal(this.menu.getOutput2TankAmount() + " / " + ElectrolyzerMenu.GAS_TANK_CAPACITY)
                );
            } else {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output fluid tank 2"),
                        Component.literal(getFluidName(this.menu.getOutput2Fluid())),
                        Component.literal(this.menu.getOutput2TankAmount() + " / " + ElectrolyzerMenu.FLUID_TANK_CAPACITY + " mB")
                );
            }
        } else if (isHoveringBox(ElectrolyzerLayout.OUTPUT_3_TANK_X, ElectrolyzerLayout.OUTPUT_3_TANK_Y, ElectrolyzerLayout.TANK_W, ElectrolyzerLayout.TANK_H, mouseX, mouseY)) {
            if (this.menu.isOutput3GasMode()) {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output gas tank 3"),
                        Component.literal(this.menu.getOutput3GasName()),
                        Component.literal(this.menu.getOutput3TankAmount() + " / " + ElectrolyzerMenu.GAS_TANK_CAPACITY)
                );
            } else {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output fluid tank 3"),
                        Component.literal(getFluidName(this.menu.getOutput3Fluid())),
                        Component.literal(this.menu.getOutput3TankAmount() + " / " + ElectrolyzerMenu.FLUID_TANK_CAPACITY + " mB")
                );
            }
        } else if (isHoveringBox(ElectrolyzerLayout.PROGRESS_X, ElectrolyzerLayout.PROGRESS_Y, ElectrolyzerLayout.PROGRESS_W, ElectrolyzerLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Electrolyzer progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        } else if (isHoveringBox(ElectrolyzerLayout.FUEL_BAR_X, ElectrolyzerLayout.FUEL_BAR_Y, ElectrolyzerLayout.FUEL_BAR_W, ElectrolyzerLayout.FUEL_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Fuel burn time"),
                    Component.literal(this.menu.getBurnTimeRemaining() + " / " + this.menu.getBurnTimeTotal() + " ticks")
            );
        }
    }
}
