package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.DistillerMenu;
import org.mydrugs.mydrugs.menu.layout.ChemicalReactorLayout;
import org.mydrugs.mydrugs.menu.layout.DistillerLayout;

public class DistillerScreen extends AbstractMachineScreen<DistillerMenu> {
    private InvisibleButton runButton;
    private InvisibleButton dumpInputButton;
    private InvisibleButton dumpOutputAButton;
    private InvisibleButton dumpOutputBButton;

    public DistillerScreen(DistillerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, DistillerLayout.GUI_WIDTH, DistillerLayout.GUI_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        this.runButton = this.addRenderableWidget(new InvisibleButton(
                guiX(DistillerLayout.RUN_BUTTON_X),
                guiY(DistillerLayout.RUN_BUTTON_Y),
                DistillerLayout.RUN_BUTTON_SIZE,
                DistillerLayout.RUN_BUTTON_SIZE,
                button -> pressMenuButton(DistillerMenu.RUN_BUTTON_ID)
        ));

        this.dumpInputButton = this.addRenderableWidget(new InvisibleButton(
                guiX(DistillerLayout.DUMP_INPUT_X),
                guiY(DistillerLayout.DUMP_BUTTON_Y),
                DistillerLayout.DUMP_BUTTON_SIZE,
                DistillerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(DistillerMenu.DUMP_INPUT_BUTTON_ID)
        ));

        this.dumpOutputAButton = this.addRenderableWidget(new InvisibleButton(
                guiX(DistillerLayout.DUMP_OUTPUT_A_X),
                guiY(DistillerLayout.DUMP_BUTTON_Y),
                DistillerLayout.DUMP_BUTTON_SIZE,
                DistillerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(DistillerMenu.DUMP_OUTPUT_A_BUTTON_ID)
        ));

        this.dumpOutputBButton = this.addRenderableWidget(new InvisibleButton(
                guiX(DistillerLayout.DUMP_OUTPUT_B_X),
                guiY(DistillerLayout.DUMP_BUTTON_Y),
                DistillerLayout.DUMP_BUTTON_SIZE,
                DistillerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(DistillerMenu.DUMP_OUTPUT_B_BUTTON_ID)
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);

        drawPanel(
                graphics,
                DistillerLayout.MACHINE_PANEL_X,
                DistillerLayout.MACHINE_PANEL_Y,
                DistillerLayout.MACHINE_PANEL_W,
                DistillerLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        drawSieveInventoryPanels(
                graphics,
                DistillerLayout.PLAYER_INV_X,
                DistillerLayout.PLAYER_INV_Y
        );

        drawTankFrame(
                graphics,
                DistillerLayout.INPUT_TANK_X,
                DistillerLayout.INPUT_TANK_Y,
                DistillerLayout.TANK_W,
                DistillerLayout.TANK_H,
                DistillerLayout.TANK_INNER_X_OFFSET,
                DistillerLayout.TANK_INNER_Y_OFFSET,
                DistillerLayout.TANK_INNER_W,
                DistillerLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                DistillerLayout.OUTPUT_A_TANK_X,
                DistillerLayout.OUTPUT_A_TANK_Y,
                DistillerLayout.TANK_W,
                DistillerLayout.TANK_H,
                DistillerLayout.TANK_INNER_X_OFFSET,
                DistillerLayout.TANK_INNER_Y_OFFSET,
                DistillerLayout.TANK_INNER_W,
                DistillerLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                DistillerLayout.OUTPUT_B_TANK_X,
                DistillerLayout.OUTPUT_B_TANK_Y,
                DistillerLayout.TANK_W,
                DistillerLayout.TANK_H,
                DistillerLayout.TANK_INNER_X_OFFSET,
                DistillerLayout.TANK_INNER_Y_OFFSET,
                DistillerLayout.TANK_INNER_W,
                DistillerLayout.TANK_INNER_H
        );

        drawTankFillShaded(
                graphics,
                DistillerLayout.INPUT_TANK_X,
                DistillerLayout.INPUT_TANK_Y,
                DistillerLayout.TANK_INNER_X_OFFSET,
                DistillerLayout.TANK_INNER_Y_OFFSET,
                DistillerLayout.TANK_INNER_W,
                DistillerLayout.TANK_INNER_H,
                this.menu.getScaledInputTank(DistillerLayout.TANK_INNER_H),
                getFluidColor(this.menu.getInputFluid())
        );

        drawTankFillShaded(
                graphics,
                DistillerLayout.OUTPUT_A_TANK_X,
                DistillerLayout.OUTPUT_A_TANK_Y,
                DistillerLayout.TANK_INNER_X_OFFSET,
                DistillerLayout.TANK_INNER_Y_OFFSET,
                DistillerLayout.TANK_INNER_W,
                DistillerLayout.TANK_INNER_H,
                this.menu.getScaledOutputATank(DistillerLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputAFluid())
        );

        drawTankFillShaded(
                graphics,
                DistillerLayout.OUTPUT_B_TANK_X,
                DistillerLayout.OUTPUT_B_TANK_Y,
                DistillerLayout.TANK_INNER_X_OFFSET,
                DistillerLayout.TANK_INNER_Y_OFFSET,
                DistillerLayout.TANK_INNER_W,
                DistillerLayout.TANK_INNER_H,
                this.menu.getScaledOutputBTank(DistillerLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputBFluid())
        );

        drawSlotFrame(graphics, DistillerLayout.INPUT_SLOT_X, DistillerLayout.INPUT_SLOT_Y);
        drawSlotFrame(graphics, DistillerLayout.OUTPUT_A_SLOT_X, DistillerLayout.OUTPUT_A_SLOT_Y);
        drawSlotFrame(graphics, DistillerLayout.OUTPUT_B_SLOT_X, DistillerLayout.OUTPUT_B_SLOT_Y);

        drawHorizontalBar(
                graphics,
                DistillerLayout.PROGRESS_X,
                DistillerLayout.PROGRESS_Y,
                DistillerLayout.PROGRESS_W,
                DistillerLayout.PROGRESS_H,
                this.menu.getScaledProgress(DistillerLayout.PROGRESS_W),
                0xFF768AB8,
                0xFFAAB9DB
        );

        drawDumpButton(
                graphics,
                DistillerLayout.DUMP_INPUT_X,
                DistillerLayout.DUMP_BUTTON_Y,
                DistillerLayout.DUMP_BUTTON_SIZE,
                this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                this.menu.getInputTankAmount() > 0
        );

        drawDumpButton(
                graphics,
                DistillerLayout.DUMP_OUTPUT_A_X,
                DistillerLayout.DUMP_BUTTON_Y,
                DistillerLayout.DUMP_BUTTON_SIZE,
                this.dumpOutputAButton != null && this.dumpOutputAButton.isHoveredOrFocused(),
                this.menu.getOutputATankAmount() > 0
        );

        drawDumpButton(
                graphics,
                DistillerLayout.DUMP_OUTPUT_B_X,
                DistillerLayout.DUMP_BUTTON_Y,
                DistillerLayout.DUMP_BUTTON_SIZE,
                this.dumpOutputBButton != null && this.dumpOutputBButton.isHoveredOrFocused(),
                this.menu.getOutputBTankAmount() > 0
        );

        drawReactor(
                graphics,
                DistillerLayout.RUN_BUTTON_X,
                DistillerLayout.RUN_BUTTON_Y,
                this.runButton != null && this.runButton.isHoveredOrFocused(),
                this.menu.isWorking(),
                this.menu.getClicksPerSecond() > 5
        );

        graphics.drawCenteredString(
                this.font,
                Component.literal(this.menu.getClicksPerSecond() + " CPS"),
                guiX(DistillerLayout.RUN_BUTTON_X + DistillerLayout.RUN_BUTTON_SIZE / 2),
                guiY(DistillerLayout.CPS_TEXT_Y),
                0xFFD8D8D8
        );

        graphics.drawCenteredString(
                this.font,
                Component.literal(this.menu.getSpeedPercent() + "% speed"),
                guiX(DistillerLayout.RUN_BUTTON_X + DistillerLayout.RUN_BUTTON_SIZE / 2),
                guiY(DistillerLayout.SPEED_TEXT_Y),
                0xFFBEBEBE
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, this.title, ChemicalReactorLayout.GUI_WIDTH / 2, 5, 0xFFFFFFFF);
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(DistillerLayout.DUMP_INPUT_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, DistillerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump input tank"));
        } else if (isHoveringBox(DistillerLayout.DUMP_OUTPUT_A_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, DistillerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output tank A"));
        } else if (isHoveringBox(DistillerLayout.DUMP_OUTPUT_B_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, DistillerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output tank B"));
        } else if (isHoveringBox(DistillerLayout.INPUT_TANK_X, DistillerLayout.INPUT_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Input tank"),
                    Component.literal(getFluidName(this.menu.getInputFluid())),
                    Component.literal(this.menu.getInputTankAmount() + " / " + DistillerMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(DistillerLayout.OUTPUT_A_TANK_X, DistillerLayout.OUTPUT_A_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output tank A"),
                    Component.literal(getFluidName(this.menu.getOutputAFluid())),
                    Component.literal(this.menu.getOutputATankAmount() + " / " + DistillerMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(DistillerLayout.OUTPUT_B_TANK_X, DistillerLayout.OUTPUT_B_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output tank B"),
                    Component.literal(getFluidName(this.menu.getOutputBFluid())),
                    Component.literal(this.menu.getOutputBTankAmount() + " / " + DistillerMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(DistillerLayout.PROGRESS_X, DistillerLayout.PROGRESS_Y, DistillerLayout.PROGRESS_W, DistillerLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Distillation progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        } else if (isHoveringBox(DistillerLayout.RUN_BUTTON_X, DistillerLayout.RUN_BUTTON_Y, DistillerLayout.RUN_BUTTON_SIZE, DistillerLayout.RUN_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Run distiller"),
                    Component.literal("More than 5 CPS increases speed")
            );
        }
    }

    private void drawReactor(GuiGraphics graphics, int localX, int localY, boolean hovered, boolean working, boolean boosted) {
        int x = localX;
        int y = localY;
        int cx = x + DistillerLayout.RUN_BUTTON_SIZE / 2;
        int cy = y + DistillerLayout.RUN_BUTTON_SIZE / 2;

        if (hovered) {
            graphics.fill(guiX(x + 2), guiY(y + 2), guiX(x + DistillerLayout.RUN_BUTTON_SIZE - 2), guiY(y + DistillerLayout.RUN_BUTTON_SIZE - 2), 0x16FFFFFF);
        }

        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS + 2, 0xFF818793);
        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS, 0xFF20242B);
        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS - 3, 0xFF9FA7B4);
        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS - 5, 0xFF3E4652);

        int coreColor = boosted
                ? 0xFF6FD6FF
                : working
                ? 0xFFE8E8E8
                : 0xFF90959E;

        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_INNER_RADIUS, 0xFF2D333B);
        drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_CORE_RADIUS, coreColor);

        if (boosted) {
            drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_CORE_RADIUS + 3, 0x336FD6FF);
        }
    }
}