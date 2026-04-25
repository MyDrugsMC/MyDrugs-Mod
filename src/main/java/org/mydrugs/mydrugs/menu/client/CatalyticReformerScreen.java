package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.CatalyticReformerBlockEntity;
import org.mydrugs.mydrugs.menu.CatalyticReformerMenu;
import org.mydrugs.mydrugs.menu.layout.CatalyticReformerLayout;

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
        drawWindow(graphics);

        drawPanel(
                graphics,
                CatalyticReformerLayout.MACHINE_PANEL_X,
                CatalyticReformerLayout.MACHINE_PANEL_Y,
                CatalyticReformerLayout.MACHINE_PANEL_W,
                CatalyticReformerLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        drawSieveInventoryPanels(
                graphics,
                CatalyticReformerLayout.PLAYER_INV_X,
                CatalyticReformerLayout.PLAYER_INV_Y
        );

        drawPanel(
                graphics,
                CatalyticReformerLayout.CENTER_PANEL_X,
                CatalyticReformerLayout.CENTER_PANEL_Y,
                CatalyticReformerLayout.CENTER_PANEL_W,
                CatalyticReformerLayout.CENTER_PANEL_H,
                0xFF262B32
        );

        drawTankFrame(
                graphics,
                CatalyticReformerLayout.INPUT_1_TANK_X,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_W,
                CatalyticReformerLayout.TANK_H,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H
        );

        drawTankFrame(
                graphics,
                CatalyticReformerLayout.INPUT_2_TANK_X,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_W,
                CatalyticReformerLayout.TANK_H,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H
        );

        drawTankFrame(
                graphics,
                CatalyticReformerLayout.OUTPUT_1_TANK_X,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_W,
                CatalyticReformerLayout.TANK_H,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H
        );

        drawTankFrame(
                graphics,
                CatalyticReformerLayout.OUTPUT_2_TANK_X,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_W,
                CatalyticReformerLayout.TANK_H,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H
        );

        drawTankFrame(
                graphics,
                CatalyticReformerLayout.OUTPUT_3_TANK_X,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_W,
                CatalyticReformerLayout.TANK_H,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H
        );

        drawTankFillShaded(
                graphics,
                CatalyticReformerLayout.INPUT_1_TANK_X,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H,
                this.menu.getScaledInput1Tank(CatalyticReformerLayout.TANK_INNER_H),
                input1Color()
        );

        drawTankFillShaded(
                graphics,
                CatalyticReformerLayout.INPUT_2_TANK_X,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H,
                this.menu.getScaledInput2Tank(CatalyticReformerLayout.TANK_INNER_H),
                input2Color()
        );

        drawTankFillShaded(
                graphics,
                CatalyticReformerLayout.OUTPUT_1_TANK_X,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H,
                this.menu.getScaledOutput1Tank(CatalyticReformerLayout.TANK_INNER_H),
                output1Color()
        );

        drawTankFillShaded(
                graphics,
                CatalyticReformerLayout.OUTPUT_2_TANK_X,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H,
                this.menu.getScaledOutput2Tank(CatalyticReformerLayout.TANK_INNER_H),
                output2Color()
        );

        drawTankFillShaded(
                graphics,
                CatalyticReformerLayout.OUTPUT_3_TANK_X,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H,
                this.menu.getScaledOutput3Tank(CatalyticReformerLayout.TANK_INNER_H),
                output3Color()
        );

        drawSlotFrame(graphics, CatalyticReformerLayout.INPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        drawSlotFrame(graphics, CatalyticReformerLayout.INPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        drawSlotFrame(graphics, CatalyticReformerLayout.OUTPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        drawSlotFrame(graphics, CatalyticReformerLayout.OUTPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        drawSlotFrame(graphics, CatalyticReformerLayout.OUTPUT_3_SLOT_X, CatalyticReformerLayout.SLOT_Y);
        drawSlotFrame(graphics, CatalyticReformerLayout.CATALYST_SLOT_X, CatalyticReformerLayout.CATALYST_SLOT_Y);

        drawHorizontalBar(
                graphics,
                CatalyticReformerLayout.PROGRESS_X,
                CatalyticReformerLayout.PROGRESS_Y,
                CatalyticReformerLayout.PROGRESS_W,
                CatalyticReformerLayout.PROGRESS_H,
                this.menu.getScaledProgress(CatalyticReformerLayout.PROGRESS_W),
                0xFF768AB8,
                0xFFAAB9DB
        );

        drawDumpButton(
                graphics,
                CatalyticReformerLayout.DUMP_INPUT_1_X,
                CatalyticReformerLayout.DUMP_BUTTON_Y,
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                this.dumpInput1Button != null && this.dumpInput1Button.isHoveredOrFocused(),
                this.menu.getInput1Amount() > 0
        );

        drawDumpButton(
                graphics,
                CatalyticReformerLayout.DUMP_INPUT_2_X,
                CatalyticReformerLayout.DUMP_BUTTON_Y,
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                this.dumpInput2Button != null && this.dumpInput2Button.isHoveredOrFocused(),
                this.menu.getInput2Amount() > 0
        );

        drawDumpButton(
                graphics,
                CatalyticReformerLayout.DUMP_OUTPUT_1_X,
                CatalyticReformerLayout.DUMP_BUTTON_Y,
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                this.dumpOutput1Button != null && this.dumpOutput1Button.isHoveredOrFocused(),
                this.menu.getOutput1Amount() > 0
        );

        drawDumpButton(
                graphics,
                CatalyticReformerLayout.DUMP_OUTPUT_2_X,
                CatalyticReformerLayout.DUMP_BUTTON_Y,
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                this.dumpOutput2Button != null && this.dumpOutput2Button.isHoveredOrFocused(),
                this.menu.getOutput2Amount() > 0
        );

        drawDumpButton(
                graphics,
                CatalyticReformerLayout.DUMP_OUTPUT_3_X,
                CatalyticReformerLayout.DUMP_BUTTON_Y,
                CatalyticReformerLayout.DUMP_BUTTON_SIZE,
                this.dumpOutput3Button != null && this.dumpOutput3Button.isHoveredOrFocused(),
                this.menu.getOutput3Amount() > 0
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, this.title, CatalyticReformerLayout.GUI_WIDTH / 2, CatalyticReformerLayout.MACHINE_PANEL_Y + 4, 0xFFFFFFFF);
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