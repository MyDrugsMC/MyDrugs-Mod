package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.CentrifugeMenu;
import org.mydrugs.mydrugs.menu.layout.CentrifugeLayout;

public class CentrifugeScreen extends AbstractMachineScreen<CentrifugeMenu> {
    private InvisibleButton dumpInputButton;
    private InvisibleButton dumpOutputAButton;
    private InvisibleButton dumpOutputBButton;

    public CentrifugeScreen(CentrifugeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, CentrifugeLayout.GUI_WIDTH, CentrifugeLayout.GUI_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        this.dumpInputButton = this.addRenderableWidget(new InvisibleButton(
                guiX(CentrifugeLayout.DUMP_INPUT_X),
                guiY(CentrifugeLayout.DUMP_BUTTON_Y),
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(CentrifugeMenu.DUMP_INPUT_BUTTON_ID)
        ));

        this.dumpOutputAButton = this.addRenderableWidget(new InvisibleButton(
                guiX(CentrifugeLayout.DUMP_OUTPUT_A_X),
                guiY(CentrifugeLayout.DUMP_BUTTON_Y),
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(CentrifugeMenu.DUMP_OUTPUT_A_BUTTON_ID)
        ));

        this.dumpOutputBButton = this.addRenderableWidget(new InvisibleButton(
                guiX(CentrifugeLayout.DUMP_OUTPUT_B_X),
                guiY(CentrifugeLayout.DUMP_BUTTON_Y),
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(CentrifugeMenu.DUMP_OUTPUT_B_BUTTON_ID)
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);

        drawPanel(
                graphics,
                CentrifugeLayout.MACHINE_PANEL_X,
                CentrifugeLayout.MACHINE_PANEL_Y,
                CentrifugeLayout.MACHINE_PANEL_W,
                CentrifugeLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        drawSieveInventoryPanels(
                graphics,
                CentrifugeLayout.PLAYER_INV_X,
                CentrifugeLayout.PLAYER_INV_Y
        );

        drawPanel(
                graphics,
                CentrifugeLayout.CENTER_PANEL_X,
                CentrifugeLayout.CENTER_PANEL_Y,
                CentrifugeLayout.CENTER_PANEL_W,
                CentrifugeLayout.CENTER_PANEL_H,
                0xFF262B32
        );

        drawTankFrame(
                graphics,
                CentrifugeLayout.INPUT_TANK_X,
                CentrifugeLayout.INPUT_TANK_Y,
                CentrifugeLayout.TANK_W,
                CentrifugeLayout.TANK_H,
                CentrifugeLayout.TANK_INNER_X_OFFSET,
                CentrifugeLayout.TANK_INNER_Y_OFFSET,
                CentrifugeLayout.TANK_INNER_W,
                CentrifugeLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                CentrifugeLayout.OUTPUT_A_TANK_X,
                CentrifugeLayout.OUTPUT_A_TANK_Y,
                CentrifugeLayout.TANK_W,
                CentrifugeLayout.TANK_H,
                CentrifugeLayout.TANK_INNER_X_OFFSET,
                CentrifugeLayout.TANK_INNER_Y_OFFSET,
                CentrifugeLayout.TANK_INNER_W,
                CentrifugeLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                CentrifugeLayout.OUTPUT_B_TANK_X,
                CentrifugeLayout.OUTPUT_B_TANK_Y,
                CentrifugeLayout.TANK_W,
                CentrifugeLayout.TANK_H,
                CentrifugeLayout.TANK_INNER_X_OFFSET,
                CentrifugeLayout.TANK_INNER_Y_OFFSET,
                CentrifugeLayout.TANK_INNER_W,
                CentrifugeLayout.TANK_INNER_H
        );

        drawTankFillShaded(
                graphics,
                CentrifugeLayout.INPUT_TANK_X,
                CentrifugeLayout.INPUT_TANK_Y,
                CentrifugeLayout.TANK_INNER_X_OFFSET,
                CentrifugeLayout.TANK_INNER_Y_OFFSET,
                CentrifugeLayout.TANK_INNER_W,
                CentrifugeLayout.TANK_INNER_H,
                this.menu.getScaledInputTank(CentrifugeLayout.TANK_INNER_H),
                getFluidColor(this.menu.getInputFluid())
        );

        drawTankFillShaded(
                graphics,
                CentrifugeLayout.OUTPUT_A_TANK_X,
                CentrifugeLayout.OUTPUT_A_TANK_Y,
                CentrifugeLayout.TANK_INNER_X_OFFSET,
                CentrifugeLayout.TANK_INNER_Y_OFFSET,
                CentrifugeLayout.TANK_INNER_W,
                CentrifugeLayout.TANK_INNER_H,
                this.menu.getScaledOutputATank(CentrifugeLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputAFluid())
        );

        drawTankFillShaded(
                graphics,
                CentrifugeLayout.OUTPUT_B_TANK_X,
                CentrifugeLayout.OUTPUT_B_TANK_Y,
                CentrifugeLayout.TANK_INNER_X_OFFSET,
                CentrifugeLayout.TANK_INNER_Y_OFFSET,
                CentrifugeLayout.TANK_INNER_W,
                CentrifugeLayout.TANK_INNER_H,
                this.menu.getScaledOutputBTank(CentrifugeLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputBFluid())
        );

        drawSlotFrame(graphics, CentrifugeLayout.INPUT_SLOT_X, CentrifugeLayout.INPUT_SLOT_Y);
        drawSlotFrame(graphics, CentrifugeLayout.OUTPUT_A_SLOT_X, CentrifugeLayout.OUTPUT_A_SLOT_Y);
        drawSlotFrame(graphics, CentrifugeLayout.OUTPUT_B_SLOT_X, CentrifugeLayout.OUTPUT_B_SLOT_Y);
        drawSlotFrame(graphics, CentrifugeLayout.FUEL_SLOT_X, CentrifugeLayout.FUEL_SLOT_Y);

        drawHorizontalBar(
                graphics,
                CentrifugeLayout.PROGRESS_X,
                CentrifugeLayout.PROGRESS_Y,
                CentrifugeLayout.PROGRESS_W,
                CentrifugeLayout.PROGRESS_H,
                this.menu.getScaledProgress(CentrifugeLayout.PROGRESS_W),
                0xFF768AB8,
                0xFFAAB9DB
        );

        drawVerticalBar(
                graphics,
                CentrifugeLayout.FUEL_BAR_X,
                CentrifugeLayout.FUEL_BAR_Y,
                CentrifugeLayout.FUEL_BAR_W,
                CentrifugeLayout.FUEL_BAR_H,
                CentrifugeLayout.FUEL_BAR_INNER_X_OFFSET,
                CentrifugeLayout.FUEL_BAR_INNER_Y_OFFSET,
                CentrifugeLayout.FUEL_BAR_INNER_W,
                CentrifugeLayout.FUEL_BAR_INNER_H,
                this.menu.getScaledBurnTime(CentrifugeLayout.FUEL_BAR_INNER_H),
                this.menu.isLit() ? 0xFFE38D3F : 0xFF8E6A4A,
                0xFFFFC270
        );

        drawDumpButton(
                graphics,
                CentrifugeLayout.DUMP_INPUT_X,
                CentrifugeLayout.DUMP_BUTTON_Y,
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                this.menu.getInputTankAmount() > 0
        );

        drawDumpButton(
                graphics,
                CentrifugeLayout.DUMP_OUTPUT_A_X,
                CentrifugeLayout.DUMP_BUTTON_Y,
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                this.dumpOutputAButton != null && this.dumpOutputAButton.isHoveredOrFocused(),
                this.menu.getOutputATankAmount() > 0
        );

        drawDumpButton(
                graphics,
                CentrifugeLayout.DUMP_OUTPUT_B_X,
                CentrifugeLayout.DUMP_BUTTON_Y,
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                this.dumpOutputBButton != null && this.dumpOutputBButton.isHoveredOrFocused(),
                this.menu.getOutputBTankAmount() > 0
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(CentrifugeLayout.DUMP_INPUT_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, CentrifugeLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump input tank"));
        } else if (isHoveringBox(CentrifugeLayout.DUMP_OUTPUT_A_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, CentrifugeLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output tank A"));
        } else if (isHoveringBox(CentrifugeLayout.DUMP_OUTPUT_B_X, CentrifugeLayout.DUMP_BUTTON_Y, CentrifugeLayout.DUMP_BUTTON_SIZE, CentrifugeLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output tank B"));
        } else if (isHoveringBox(CentrifugeLayout.INPUT_TANK_X, CentrifugeLayout.INPUT_TANK_Y, CentrifugeLayout.TANK_W, CentrifugeLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Input tank"),
                    Component.literal(getFluidName(this.menu.getInputFluid())),
                    Component.literal(this.menu.getInputTankAmount() + " / " + CentrifugeMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(CentrifugeLayout.OUTPUT_A_TANK_X, CentrifugeLayout.OUTPUT_A_TANK_Y, CentrifugeLayout.TANK_W, CentrifugeLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output tank A"),
                    Component.literal(getFluidName(this.menu.getOutputAFluid())),
                    Component.literal(this.menu.getOutputATankAmount() + " / " + CentrifugeMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(CentrifugeLayout.OUTPUT_B_TANK_X, CentrifugeLayout.OUTPUT_B_TANK_Y, CentrifugeLayout.TANK_W, CentrifugeLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output tank B"),
                    Component.literal(getFluidName(this.menu.getOutputBFluid())),
                    Component.literal(this.menu.getOutputBTankAmount() + " / " + CentrifugeMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(CentrifugeLayout.PROGRESS_X, CentrifugeLayout.PROGRESS_Y, CentrifugeLayout.PROGRESS_W, CentrifugeLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Centrifuge progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        } else if (isHoveringBox(CentrifugeLayout.FUEL_BAR_X, CentrifugeLayout.FUEL_BAR_Y, CentrifugeLayout.FUEL_BAR_W, CentrifugeLayout.FUEL_BAR_H, mouseX, mouseY)) {
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