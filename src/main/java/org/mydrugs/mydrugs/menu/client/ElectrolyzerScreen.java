package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.ElectrolyzerMenu;
import org.mydrugs.mydrugs.menu.layout.ElectrolyzerLayout;

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
        drawWindow(graphics);

        drawPanel(
                graphics,
                ElectrolyzerLayout.MACHINE_PANEL_X,
                ElectrolyzerLayout.MACHINE_PANEL_Y,
                ElectrolyzerLayout.MACHINE_PANEL_W,
                ElectrolyzerLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        drawSieveInventoryPanels(
                graphics,
                ElectrolyzerLayout.PLAYER_INV_X,
                ElectrolyzerLayout.PLAYER_INV_Y
        );

        drawPanel(
                graphics,
                ElectrolyzerLayout.CENTER_PANEL_X,
                ElectrolyzerLayout.CENTER_PANEL_Y,
                ElectrolyzerLayout.CENTER_PANEL_W,
                ElectrolyzerLayout.CENTER_PANEL_H,
                0xFF262B32
        );

        drawTankFrame(
                graphics,
                ElectrolyzerLayout.INPUT_TANK_X,
                ElectrolyzerLayout.INPUT_TANK_Y,
                ElectrolyzerLayout.TANK_W,
                ElectrolyzerLayout.TANK_H,
                ElectrolyzerLayout.TANK_INNER_X_OFFSET,
                ElectrolyzerLayout.TANK_INNER_Y_OFFSET,
                ElectrolyzerLayout.TANK_INNER_W,
                ElectrolyzerLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                ElectrolyzerLayout.OUTPUT_1_TANK_X,
                ElectrolyzerLayout.OUTPUT_1_TANK_Y,
                ElectrolyzerLayout.TANK_W,
                ElectrolyzerLayout.TANK_H,
                ElectrolyzerLayout.TANK_INNER_X_OFFSET,
                ElectrolyzerLayout.TANK_INNER_Y_OFFSET,
                ElectrolyzerLayout.TANK_INNER_W,
                ElectrolyzerLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                ElectrolyzerLayout.OUTPUT_2_TANK_X,
                ElectrolyzerLayout.OUTPUT_2_TANK_Y,
                ElectrolyzerLayout.TANK_W,
                ElectrolyzerLayout.TANK_H,
                ElectrolyzerLayout.TANK_INNER_X_OFFSET,
                ElectrolyzerLayout.TANK_INNER_Y_OFFSET,
                ElectrolyzerLayout.TANK_INNER_W,
                ElectrolyzerLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                ElectrolyzerLayout.OUTPUT_3_TANK_X,
                ElectrolyzerLayout.OUTPUT_3_TANK_Y,
                ElectrolyzerLayout.TANK_W,
                ElectrolyzerLayout.TANK_H,
                ElectrolyzerLayout.TANK_INNER_X_OFFSET,
                ElectrolyzerLayout.TANK_INNER_Y_OFFSET,
                ElectrolyzerLayout.TANK_INNER_W,
                ElectrolyzerLayout.TANK_INNER_H
        );

        drawTankFillShaded(
                graphics,
                ElectrolyzerLayout.INPUT_TANK_X,
                ElectrolyzerLayout.INPUT_TANK_Y,
                ElectrolyzerLayout.TANK_INNER_X_OFFSET,
                ElectrolyzerLayout.TANK_INNER_Y_OFFSET,
                ElectrolyzerLayout.TANK_INNER_W,
                ElectrolyzerLayout.TANK_INNER_H,
                this.menu.getScaledInputTank(ElectrolyzerLayout.TANK_INNER_H),
                getFluidColor(this.menu.getInputFluid())
        );

        drawTankFillShaded(
                graphics,
                ElectrolyzerLayout.OUTPUT_1_TANK_X,
                ElectrolyzerLayout.OUTPUT_1_TANK_Y,
                ElectrolyzerLayout.TANK_INNER_X_OFFSET,
                ElectrolyzerLayout.TANK_INNER_Y_OFFSET,
                ElectrolyzerLayout.TANK_INNER_W,
                ElectrolyzerLayout.TANK_INNER_H,
                this.menu.getScaledOutput1Tank(ElectrolyzerLayout.TANK_INNER_H),
                getOutput1Color()
        );

        drawTankFillShaded(
                graphics,
                ElectrolyzerLayout.OUTPUT_2_TANK_X,
                ElectrolyzerLayout.OUTPUT_2_TANK_Y,
                ElectrolyzerLayout.TANK_INNER_X_OFFSET,
                ElectrolyzerLayout.TANK_INNER_Y_OFFSET,
                ElectrolyzerLayout.TANK_INNER_W,
                ElectrolyzerLayout.TANK_INNER_H,
                this.menu.getScaledOutput2Tank(ElectrolyzerLayout.TANK_INNER_H),
                getOutput2Color()
        );

        drawTankFillShaded(
                graphics,
                ElectrolyzerLayout.OUTPUT_3_TANK_X,
                ElectrolyzerLayout.OUTPUT_3_TANK_Y,
                ElectrolyzerLayout.TANK_INNER_X_OFFSET,
                ElectrolyzerLayout.TANK_INNER_Y_OFFSET,
                ElectrolyzerLayout.TANK_INNER_W,
                ElectrolyzerLayout.TANK_INNER_H,
                this.menu.getScaledOutput3Tank(ElectrolyzerLayout.TANK_INNER_H),
                getOutput3Color()
        );

        drawSlotFrame(graphics, ElectrolyzerLayout.INPUT_SLOT_X, ElectrolyzerLayout.INPUT_SLOT_Y);
        drawSlotFrame(graphics, ElectrolyzerLayout.OUTPUT_1_SLOT_X, ElectrolyzerLayout.OUTPUT_1_SLOT_Y);
        drawSlotFrame(graphics, ElectrolyzerLayout.OUTPUT_2_SLOT_X, ElectrolyzerLayout.OUTPUT_2_SLOT_Y);
        drawSlotFrame(graphics, ElectrolyzerLayout.OUTPUT_3_SLOT_X, ElectrolyzerLayout.OUTPUT_3_SLOT_Y);
        drawSlotFrame(graphics, ElectrolyzerLayout.FUEL_SLOT_X, ElectrolyzerLayout.FUEL_SLOT_Y);

        drawHorizontalBar(
                graphics,
                ElectrolyzerLayout.PROGRESS_X,
                ElectrolyzerLayout.PROGRESS_Y,
                ElectrolyzerLayout.PROGRESS_W,
                ElectrolyzerLayout.PROGRESS_H,
                this.menu.getScaledProgress(ElectrolyzerLayout.PROGRESS_W),
                0xFF768AB8,
                0xFFAAB9DB
        );

        drawVerticalBar(
                graphics,
                ElectrolyzerLayout.FUEL_BAR_X,
                ElectrolyzerLayout.FUEL_BAR_Y,
                ElectrolyzerLayout.FUEL_BAR_W,
                ElectrolyzerLayout.FUEL_BAR_H,
                ElectrolyzerLayout.FUEL_BAR_INNER_X_OFFSET,
                ElectrolyzerLayout.FUEL_BAR_INNER_Y_OFFSET,
                ElectrolyzerLayout.FUEL_BAR_INNER_W,
                ElectrolyzerLayout.FUEL_BAR_INNER_H,
                this.menu.getScaledBurnTime(ElectrolyzerLayout.FUEL_BAR_INNER_H),
                this.menu.isLit() ? 0xFFE38D3F : 0xFF8E6A4A,
                0xFFFFC270
        );

        drawDumpButton(
                graphics,
                ElectrolyzerLayout.DUMP_INPUT_X,
                ElectrolyzerLayout.DUMP_BUTTON_Y,
                ElectrolyzerLayout.DUMP_BUTTON_SIZE,
                this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                this.menu.getInputTankAmount() > 0
        );

        drawDumpButton(
                graphics,
                ElectrolyzerLayout.DUMP_OUTPUT_1_X,
                ElectrolyzerLayout.DUMP_BUTTON_Y,
                ElectrolyzerLayout.DUMP_BUTTON_SIZE,
                this.dumpOutput1Button != null && this.dumpOutput1Button.isHoveredOrFocused(),
                this.menu.getOutput1TankAmount() > 0
        );

        drawDumpButton(
                graphics,
                ElectrolyzerLayout.DUMP_OUTPUT_2_X,
                ElectrolyzerLayout.DUMP_BUTTON_Y,
                ElectrolyzerLayout.DUMP_BUTTON_SIZE,
                this.dumpOutput2Button != null && this.dumpOutput2Button.isHoveredOrFocused(),
                this.menu.getOutput2TankAmount() > 0
        );

        drawDumpButton(
                graphics,
                ElectrolyzerLayout.DUMP_OUTPUT_3_X,
                ElectrolyzerLayout.DUMP_BUTTON_Y,
                ElectrolyzerLayout.DUMP_BUTTON_SIZE,
                this.dumpOutput3Button != null && this.dumpOutput3Button.isHoveredOrFocused(),
                this.menu.getOutput3TankAmount() > 0
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, this.title, ElectrolyzerLayout.GUI_WIDTH / 2, ElectrolyzerLayout.MACHINE_PANEL_Y + 4, 0xFFFFFFFF);
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