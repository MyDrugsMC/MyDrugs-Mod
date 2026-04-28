package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.CatalyticReformerBlockEntity;
import org.mydrugs.mydrugs.menu.CatalyticReformerMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.CatalyticReformerLayout;

import java.util.ArrayList;
import java.util.List;

public class CatalyticReformerScreen extends AbstractMachineScreen<CatalyticReformerMenu> {
    private InvisibleButton dumpInput1Button;
    private InvisibleButton dumpInput2Button;
    private InvisibleButton dumpOutput1Button;
    private InvisibleButton dumpOutput2Button;
    private InvisibleButton dumpOutput3Button;

    public CatalyticReformerScreen(CatalyticReformerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, CatalyticReformerLayout.GUI_WIDTH, CatalyticReformerLayout.GUI_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        this.dumpInput1Button = this.addRenderableWidget(new InvisibleButton(
                guiX(CatalyticReformerLayout.DUMP_INPUT_1_X),
                guiY(CatalyticReformerLayout.DUMP_BUTTON_Y),
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(CatalyticReformerMenu.DUMP_INPUT_1_BUTTON_ID)
        ));

        this.dumpInput2Button = this.addRenderableWidget(new InvisibleButton(
                guiX(CatalyticReformerLayout.DUMP_INPUT_2_X),
                guiY(CatalyticReformerLayout.DUMP_BUTTON_Y),
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(CatalyticReformerMenu.DUMP_INPUT_2_BUTTON_ID)
        ));

        this.dumpOutput1Button = this.addRenderableWidget(new InvisibleButton(
                guiX(CatalyticReformerLayout.DUMP_OUTPUT_1_X),
                guiY(CatalyticReformerLayout.DUMP_BUTTON_Y),
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(CatalyticReformerMenu.DUMP_OUTPUT_1_BUTTON_ID)
        ));

        this.dumpOutput2Button = this.addRenderableWidget(new InvisibleButton(
                guiX(CatalyticReformerLayout.DUMP_OUTPUT_2_X),
                guiY(CatalyticReformerLayout.DUMP_BUTTON_Y),
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(CatalyticReformerMenu.DUMP_OUTPUT_2_BUTTON_ID)
        ));

        this.dumpOutput3Button = this.addRenderableWidget(new InvisibleButton(
                guiX(CatalyticReformerLayout.DUMP_OUTPUT_3_X),
                guiY(CatalyticReformerLayout.DUMP_BUTTON_Y),
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(CatalyticReformerMenu.DUMP_OUTPUT_3_BUTTON_ID)
        ));
    }

    private int input1Color() {
        return this.menu.isInput1GasMode()
                ? this.menu.getInput1GasColor()
                : getFluidColor(this.menu.getInput1Fluid());
    }

    private int input2Color() {
        return this.menu.isInput2GasMode()
                ? this.menu.getInput2GasColor()
                : getFluidColor(this.menu.getInput2Fluid());
    }

    private int output1Color() {
        return this.menu.isOutput1GasMode()
                ? this.menu.getOutput1GasColor()
                : getFluidColor(this.menu.getOutput1Fluid());
    }

    private int output2Color() {
        return this.menu.isOutput2GasMode()
                ? this.menu.getOutput2GasColor()
                : getFluidColor(this.menu.getOutput2Fluid());
    }

    private int output3Color() {
        return this.menu.isOutput3GasMode()
                ? this.menu.getOutput3GasColor()
                : getFluidColor(this.menu.getOutput3Fluid());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineGuiRenderer.drawCatalyticReformer(
                this,
                graphics,
                new MachineGuiRenderer.CatalyticReformerState(
                        MachineGuiRenderer.TankFill.liveColor(this.menu.getScaledInput1Tank(CatalyticReformerLayout.TANK_INNER_H), input1Color()),
                        MachineGuiRenderer.TankFill.liveColor(this.menu.getScaledInput2Tank(CatalyticReformerLayout.TANK_INNER_H), input2Color()),
                        MachineGuiRenderer.TankFill.liveColor(this.menu.getScaledOutput1Tank(CatalyticReformerLayout.TANK_INNER_H), output1Color()),
                        MachineGuiRenderer.TankFill.liveColor(this.menu.getScaledOutput2Tank(CatalyticReformerLayout.TANK_INNER_H), output2Color()),
                        MachineGuiRenderer.TankFill.liveColor(this.menu.getScaledOutput3Tank(CatalyticReformerLayout.TANK_INNER_H), output3Color()),
                        this.menu.getScaledProgress(CatalyticReformerLayout.PROGRESS_W),
                        this.dumpInput1Button != null && this.dumpInput1Button.isHoveredOrFocused(),
                        this.dumpInput2Button != null && this.dumpInput2Button.isHoveredOrFocused(),
                        this.dumpOutput1Button != null && this.dumpOutput1Button.isHoveredOrFocused(),
                        this.dumpOutput2Button != null && this.dumpOutput2Button.isHoveredOrFocused(),
                        this.dumpOutput3Button != null && this.dumpOutput3Button.isHoveredOrFocused(),
                        this.menu.getInput1Amount() > 0,
                        this.menu.getInput2Amount() > 0,
                        this.menu.getOutput1Amount() > 0,
                        this.menu.getOutput2Amount() > 0,
                        this.menu.getOutput3Amount() > 0
                ),
                true
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawCatalyticReformerLabels(this, graphics, this.font, this.title, null);
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "item_input" -> List.of(
                    slotHighlight(CatalyticReformerLayout.INPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y),
                    slotHighlight(CatalyticReformerLayout.INPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y)
            );
            case "catalyst" -> List.of(slotHighlight(CatalyticReformerLayout.CATALYST_SLOT_X, CatalyticReformerLayout.CATALYST_SLOT_Y));
            case "item_output" -> List.of(
                    slotHighlight(CatalyticReformerLayout.OUTPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y),
                    slotHighlight(CatalyticReformerLayout.OUTPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y),
                    slotHighlight(CatalyticReformerLayout.OUTPUT_3_SLOT_X, CatalyticReformerLayout.SLOT_Y)
            );
            case "fluid_input" -> inputHighlights(false);
            case "gas_input" -> inputHighlights(true);
            case "fluid_output" -> outputHighlights(false);
            case "gas_output" -> outputHighlights(true);
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    private List<TransferHighlight> inputHighlights(boolean gasMode) {
        ArrayList<TransferHighlight> highlights = new ArrayList<>();
        addTankSlotHighlight(highlights, gasMode, this.menu.isInput1GasMode(), CatalyticReformerLayout.INPUT_1_TANK_X, CatalyticReformerLayout.INPUT_1_SLOT_X);
        addTankSlotHighlight(highlights, gasMode, this.menu.isInput2GasMode(), CatalyticReformerLayout.INPUT_2_TANK_X, CatalyticReformerLayout.INPUT_2_SLOT_X);
        return highlights.isEmpty() ? allInputHighlights() : highlights;
    }

    private List<TransferHighlight> outputHighlights(boolean gasMode) {
        ArrayList<TransferHighlight> highlights = new ArrayList<>();
        addTankSlotHighlight(highlights, gasMode, this.menu.isOutput1GasMode(), CatalyticReformerLayout.OUTPUT_1_TANK_X, CatalyticReformerLayout.OUTPUT_1_SLOT_X);
        addTankSlotHighlight(highlights, gasMode, this.menu.isOutput2GasMode(), CatalyticReformerLayout.OUTPUT_2_TANK_X, CatalyticReformerLayout.OUTPUT_2_SLOT_X);
        addTankSlotHighlight(highlights, gasMode, this.menu.isOutput3GasMode(), CatalyticReformerLayout.OUTPUT_3_TANK_X, CatalyticReformerLayout.OUTPUT_3_SLOT_X);
        return highlights.isEmpty() ? allOutputHighlights() : highlights;
    }

    private static void addTankSlotHighlight(List<TransferHighlight> highlights, boolean expectedGasMode, boolean actualGasMode, int tankX, int slotX) {
        if (actualGasMode != expectedGasMode) {
            return;
        }
        highlights.add(tankHighlight(tankX, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H));
        highlights.add(slotHighlight(slotX, CatalyticReformerLayout.SLOT_Y));
    }

    private static List<TransferHighlight> allInputHighlights() {
        return List.of(
                tankHighlight(CatalyticReformerLayout.INPUT_1_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H),
                tankHighlight(CatalyticReformerLayout.INPUT_2_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H),
                slotHighlight(CatalyticReformerLayout.INPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y),
                slotHighlight(CatalyticReformerLayout.INPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y)
        );
    }

    private static List<TransferHighlight> allOutputHighlights() {
        return List.of(
                tankHighlight(CatalyticReformerLayout.OUTPUT_1_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H),
                tankHighlight(CatalyticReformerLayout.OUTPUT_2_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H),
                tankHighlight(CatalyticReformerLayout.OUTPUT_3_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H),
                slotHighlight(CatalyticReformerLayout.OUTPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y),
                slotHighlight(CatalyticReformerLayout.OUTPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y),
                slotHighlight(CatalyticReformerLayout.OUTPUT_3_SLOT_X, CatalyticReformerLayout.SLOT_Y)
        );
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(CatalyticReformerLayout.DUMP_INPUT_1_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, CatalyticReformerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump input tank 1"));
        } else if (isHoveringBox(CatalyticReformerLayout.DUMP_INPUT_2_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, CatalyticReformerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump input tank 2"));
        } else if (isHoveringBox(CatalyticReformerLayout.DUMP_OUTPUT_1_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, CatalyticReformerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output tank 1"));
        } else if (isHoveringBox(CatalyticReformerLayout.DUMP_OUTPUT_2_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, CatalyticReformerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output tank 2"));
        } else if (isHoveringBox(CatalyticReformerLayout.DUMP_OUTPUT_3_X, CatalyticReformerLayout.DUMP_BUTTON_Y, CatalyticReformerLayout.DUMP_BUTTON_SIZE, CatalyticReformerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output tank 3"));
        } else if (isHoveringBox(CatalyticReformerLayout.INPUT_1_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H, mouseX, mouseY)) {
            if (this.menu.isInput1GasMode()) {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Input gas tank 1"),
                        Component.literal(this.menu.getInput1GasName()),
                        Component.literal(this.menu.getInput1Amount() + " / " + CatalyticReformerBlockEntity.GAS_CAPACITY)
                );
            } else {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Input fluid tank 1"),
                        Component.literal(getFluidName(this.menu.getInput1Fluid())),
                        Component.literal(this.menu.getInput1Amount() + " / " + CatalyticReformerBlockEntity.FLUID_CAPACITY + " mB")
                );
            }
        } else if (isHoveringBox(CatalyticReformerLayout.INPUT_2_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H, mouseX, mouseY)) {
            if (this.menu.isInput2GasMode()) {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Input gas tank 2"),
                        Component.literal(this.menu.getInput2GasName()),
                        Component.literal(this.menu.getInput2Amount() + " / " + CatalyticReformerBlockEntity.GAS_CAPACITY)
                );
            } else {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Input fluid tank 2"),
                        Component.literal(getFluidName(this.menu.getInput2Fluid())),
                        Component.literal(this.menu.getInput2Amount() + " / " + CatalyticReformerBlockEntity.FLUID_CAPACITY + " mB")
                );
            }
        } else if (isHoveringBox(CatalyticReformerLayout.OUTPUT_1_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H, mouseX, mouseY)) {
            if (this.menu.isOutput1GasMode()) {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output gas tank 1"),
                        Component.literal(this.menu.getOutput1GasName()),
                        Component.literal(this.menu.getOutput1Amount() + " / " + CatalyticReformerBlockEntity.GAS_CAPACITY)
                );
            } else {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output fluid tank 1"),
                        Component.literal(getFluidName(this.menu.getOutput1Fluid())),
                        Component.literal(this.menu.getOutput1Amount() + " / " + CatalyticReformerBlockEntity.FLUID_CAPACITY + " mB")
                );
            }
        } else if (isHoveringBox(CatalyticReformerLayout.OUTPUT_2_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H, mouseX, mouseY)) {
            if (this.menu.isOutput2GasMode()) {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output gas tank 2"),
                        Component.literal(this.menu.getOutput2GasName()),
                        Component.literal(this.menu.getOutput2Amount() + " / " + CatalyticReformerBlockEntity.GAS_CAPACITY)
                );
            } else {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output fluid tank 2"),
                        Component.literal(getFluidName(this.menu.getOutput2Fluid())),
                        Component.literal(this.menu.getOutput2Amount() + " / " + CatalyticReformerBlockEntity.FLUID_CAPACITY + " mB")
                );
            }
        } else if (isHoveringBox(CatalyticReformerLayout.OUTPUT_3_TANK_X, CatalyticReformerLayout.TANK_Y, CatalyticReformerLayout.TANK_W, CatalyticReformerLayout.TANK_H, mouseX, mouseY)) {
            if (this.menu.isOutput3GasMode()) {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output gas tank 3"),
                        Component.literal(this.menu.getOutput3GasName()),
                        Component.literal(this.menu.getOutput3Amount() + " / " + CatalyticReformerBlockEntity.GAS_CAPACITY)
                );
            } else {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output fluid tank 3"),
                        Component.literal(getFluidName(this.menu.getOutput3Fluid())),
                        Component.literal(this.menu.getOutput3Amount() + " / " + CatalyticReformerBlockEntity.FLUID_CAPACITY + " mB")
                );
            }
        } else if (isHoveringBox(CatalyticReformerLayout.CATALYST_SLOT_X, CatalyticReformerLayout.CATALYST_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Catalyst slot"));
        } else if (isHoveringBox(CatalyticReformerLayout.PROGRESS_X, CatalyticReformerLayout.PROGRESS_Y, CatalyticReformerLayout.PROGRESS_W, CatalyticReformerLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Catalytic reforming progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        }
    }
}
