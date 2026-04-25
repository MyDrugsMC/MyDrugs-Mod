package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.AromaticExtractorBlockEntity;
import org.mydrugs.mydrugs.menu.AromaticExtractorMenu;
import org.mydrugs.mydrugs.menu.layout.AromaticExtractorLayout;

public class AromaticExtractorScreen extends AbstractMachineScreen<AromaticExtractorMenu> {
    private InvisibleButton dumpInputButton;
    private InvisibleButton dumpCatalystButton;
    private InvisibleButton dumpOutputAButton;
    private InvisibleButton dumpOutputBButton;

    public AromaticExtractorScreen(AromaticExtractorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, AromaticExtractorLayout.GUI_WIDTH, AromaticExtractorLayout.GUI_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        this.dumpInputButton = this.addRenderableWidget(new InvisibleButton(
                guiX(AromaticExtractorLayout.DUMP_INPUT_X),
                guiY(AromaticExtractorLayout.DUMP_BUTTON_Y),
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(AromaticExtractorMenu.DUMP_INPUT_BUTTON_ID)
        ));

        this.dumpCatalystButton = this.addRenderableWidget(new InvisibleButton(
                guiX(AromaticExtractorLayout.DUMP_CATALYST_X),
                guiY(AromaticExtractorLayout.DUMP_BUTTON_Y),
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(AromaticExtractorMenu.DUMP_CATALYST_BUTTON_ID)
        ));

        this.dumpOutputAButton = this.addRenderableWidget(new InvisibleButton(
                guiX(AromaticExtractorLayout.DUMP_OUTPUT_A_X),
                guiY(AromaticExtractorLayout.DUMP_BUTTON_Y),
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(AromaticExtractorMenu.DUMP_OUTPUT_A_BUTTON_ID)
        ));

        this.dumpOutputBButton = this.addRenderableWidget(new InvisibleButton(
                guiX(AromaticExtractorLayout.DUMP_OUTPUT_B_X),
                guiY(AromaticExtractorLayout.DUMP_BUTTON_Y),
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(AromaticExtractorMenu.DUMP_OUTPUT_B_BUTTON_ID)
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);

        drawPanel(
                graphics,
                AromaticExtractorLayout.MACHINE_PANEL_X,
                AromaticExtractorLayout.MACHINE_PANEL_Y,
                AromaticExtractorLayout.MACHINE_PANEL_W,
                AromaticExtractorLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        drawSieveInventoryPanels(
                graphics,
                AromaticExtractorLayout.PLAYER_INV_X,
                AromaticExtractorLayout.PLAYER_INV_Y
        );

        drawPanel(
                graphics,
                AromaticExtractorLayout.CENTER_PANEL_X,
                AromaticExtractorLayout.CENTER_PANEL_Y,
                AromaticExtractorLayout.CENTER_PANEL_W,
                AromaticExtractorLayout.CENTER_PANEL_H,
                0xFF262B32
        );

        drawTankFrame(
                graphics,
                AromaticExtractorLayout.INPUT_TANK_X,
                AromaticExtractorLayout.INPUT_TANK_Y,
                AromaticExtractorLayout.TANK_W,
                AromaticExtractorLayout.TANK_H,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                AromaticExtractorLayout.CATALYST_TANK_X,
                AromaticExtractorLayout.CATALYST_TANK_Y,
                AromaticExtractorLayout.TANK_W,
                AromaticExtractorLayout.TANK_H,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                AromaticExtractorLayout.OUTPUT_A_TANK_X,
                AromaticExtractorLayout.OUTPUT_A_TANK_Y,
                AromaticExtractorLayout.TANK_W,
                AromaticExtractorLayout.TANK_H,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                AromaticExtractorLayout.OUTPUT_B_TANK_X,
                AromaticExtractorLayout.OUTPUT_B_TANK_Y,
                AromaticExtractorLayout.TANK_W,
                AromaticExtractorLayout.TANK_H,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H
        );

        drawTankFillShaded(
                graphics,
                AromaticExtractorLayout.INPUT_TANK_X,
                AromaticExtractorLayout.INPUT_TANK_Y,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H,
                this.menu.getScaledInputTank(AromaticExtractorLayout.TANK_INNER_H),
                getFluidColor(this.menu.getInputFluid())
        );

        drawTankFillShaded(
                graphics,
                AromaticExtractorLayout.CATALYST_TANK_X,
                AromaticExtractorLayout.CATALYST_TANK_Y,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H,
                this.menu.getScaledCatalystTank(AromaticExtractorLayout.TANK_INNER_H),
                getFluidColor(this.menu.getCatalystFluid())
        );

        drawTankFillShaded(
                graphics,
                AromaticExtractorLayout.OUTPUT_A_TANK_X,
                AromaticExtractorLayout.OUTPUT_A_TANK_Y,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H,
                this.menu.getScaledOutputATank(AromaticExtractorLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputAFluid())
        );

        drawTankFillShaded(
                graphics,
                AromaticExtractorLayout.OUTPUT_B_TANK_X,
                AromaticExtractorLayout.OUTPUT_B_TANK_Y,
                AromaticExtractorLayout.TANK_INNER_X_OFFSET,
                AromaticExtractorLayout.TANK_INNER_Y_OFFSET,
                AromaticExtractorLayout.TANK_INNER_W,
                AromaticExtractorLayout.TANK_INNER_H,
                this.menu.getScaledOutputBTank(AromaticExtractorLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputBFluid())
        );

        drawSlotFrame(graphics, AromaticExtractorLayout.INPUT_SLOT_X, AromaticExtractorLayout.INPUT_SLOT_Y);
        drawSlotFrame(graphics, AromaticExtractorLayout.CATALYST_SLOT_X, AromaticExtractorLayout.CATALYST_SLOT_Y);
        drawSlotFrame(graphics, AromaticExtractorLayout.OUTPUT_A_SLOT_X, AromaticExtractorLayout.OUTPUT_A_SLOT_Y);
        drawSlotFrame(graphics, AromaticExtractorLayout.OUTPUT_B_SLOT_X, AromaticExtractorLayout.OUTPUT_B_SLOT_Y);
        drawSlotFrame(graphics, AromaticExtractorLayout.FUEL_SLOT_X, AromaticExtractorLayout.FUEL_SLOT_Y);

        drawHorizontalBar(
                graphics,
                AromaticExtractorLayout.PROGRESS_X,
                AromaticExtractorLayout.PROGRESS_Y,
                AromaticExtractorLayout.PROGRESS_W,
                AromaticExtractorLayout.PROGRESS_H,
                this.menu.getScaledProgress(AromaticExtractorLayout.PROGRESS_W),
                0xFFB8865F,
                0xFFFFD0A6
        );

        drawVerticalBar(
                graphics,
                AromaticExtractorLayout.FUEL_BAR_X,
                AromaticExtractorLayout.FUEL_BAR_Y,
                AromaticExtractorLayout.FUEL_BAR_W,
                AromaticExtractorLayout.FUEL_BAR_H,
                AromaticExtractorLayout.FUEL_BAR_INNER_X_OFFSET,
                AromaticExtractorLayout.FUEL_BAR_INNER_Y_OFFSET,
                AromaticExtractorLayout.FUEL_BAR_INNER_W,
                AromaticExtractorLayout.FUEL_BAR_INNER_H,
                this.menu.getScaledBurnTime(AromaticExtractorLayout.FUEL_BAR_INNER_H),
                this.menu.isLit() ? 0xFFE38D3F : 0xFF8E6A4A,
                0xFFFFC270
        );

        drawDumpButton(
                graphics,
                AromaticExtractorLayout.DUMP_INPUT_X,
                AromaticExtractorLayout.DUMP_BUTTON_Y,
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                this.menu.getInputTankAmount() > 0
        );

        drawDumpButton(
                graphics,
                AromaticExtractorLayout.DUMP_CATALYST_X,
                AromaticExtractorLayout.DUMP_BUTTON_Y,
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                this.dumpCatalystButton != null && this.dumpCatalystButton.isHoveredOrFocused(),
                this.menu.getCatalystTankAmount() > 0
        );

        drawDumpButton(
                graphics,
                AromaticExtractorLayout.DUMP_OUTPUT_A_X,
                AromaticExtractorLayout.DUMP_BUTTON_Y,
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                this.dumpOutputAButton != null && this.dumpOutputAButton.isHoveredOrFocused(),
                this.menu.getOutputATankAmount() > 0
        );

        drawDumpButton(
                graphics,
                AromaticExtractorLayout.DUMP_OUTPUT_B_X,
                AromaticExtractorLayout.DUMP_BUTTON_Y,
                AromaticExtractorLayout.DUMP_BUTTON_SIZE,
                this.dumpOutputBButton != null && this.dumpOutputBButton.isHoveredOrFocused(),
                this.menu.getOutputBTankAmount() > 0
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, this.title, AromaticExtractorLayout.GUI_WIDTH / 2, AromaticExtractorLayout.MACHINE_PANEL_Y + 4, 0xFFFFFFFF);
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(AromaticExtractorLayout.DUMP_INPUT_X, AromaticExtractorLayout.DUMP_BUTTON_Y, AromaticExtractorLayout.DUMP_BUTTON_SIZE, AromaticExtractorLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump input tank"));
        } else if (isHoveringBox(AromaticExtractorLayout.DUMP_CATALYST_X, AromaticExtractorLayout.DUMP_BUTTON_Y, AromaticExtractorLayout.DUMP_BUTTON_SIZE, AromaticExtractorLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump catalyst tank"));
        } else if (isHoveringBox(AromaticExtractorLayout.DUMP_OUTPUT_A_X, AromaticExtractorLayout.DUMP_BUTTON_Y, AromaticExtractorLayout.DUMP_BUTTON_SIZE, AromaticExtractorLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output A tank"));
        } else if (isHoveringBox(AromaticExtractorLayout.DUMP_OUTPUT_B_X, AromaticExtractorLayout.DUMP_BUTTON_Y, AromaticExtractorLayout.DUMP_BUTTON_SIZE, AromaticExtractorLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output B tank"));
        } else if (isHoveringBox(AromaticExtractorLayout.INPUT_TANK_X, AromaticExtractorLayout.INPUT_TANK_Y, AromaticExtractorLayout.TANK_W, AromaticExtractorLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Recipe input tank"),
                    Component.literal(getFluidName(this.menu.getInputFluid())),
                    Component.literal(this.menu.getInputTankAmount() + " / " + AromaticExtractorMenu.INPUT_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(AromaticExtractorLayout.CATALYST_TANK_X, AromaticExtractorLayout.CATALYST_TANK_Y, AromaticExtractorLayout.TANK_W, AromaticExtractorLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Catalyst tank"),
                    Component.literal("Catalyst fluid is not consumed"),
                    Component.literal("Minimum: " + AromaticExtractorBlockEntity.MIN_CATALYST_AMOUNT + " mB"),
                    Component.literal("Speed: " + this.menu.getCatalystSpeedPercent() + "%"),
                    Component.literal(getFluidName(this.menu.getCatalystFluid())),
                    Component.literal(this.menu.getCatalystTankAmount() + " / " + AromaticExtractorMenu.CATALYST_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(AromaticExtractorLayout.OUTPUT_A_TANK_X, AromaticExtractorLayout.OUTPUT_A_TANK_Y, AromaticExtractorLayout.TANK_W, AromaticExtractorLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output A tank"),
                    Component.literal(getFluidName(this.menu.getOutputAFluid())),
                    Component.literal(this.menu.getOutputATankAmount() + " / " + AromaticExtractorMenu.OUTPUT_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(AromaticExtractorLayout.OUTPUT_B_TANK_X, AromaticExtractorLayout.OUTPUT_B_TANK_Y, AromaticExtractorLayout.TANK_W, AromaticExtractorLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output B tank"),
                    Component.literal(getFluidName(this.menu.getOutputBFluid())),
                    Component.literal(this.menu.getOutputBTankAmount() + " / " + AromaticExtractorMenu.OUTPUT_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(AromaticExtractorLayout.PROGRESS_X, AromaticExtractorLayout.PROGRESS_Y, AromaticExtractorLayout.PROGRESS_W, AromaticExtractorLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Extraction progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        } else if (isHoveringBox(AromaticExtractorLayout.FUEL_BAR_X, AromaticExtractorLayout.FUEL_BAR_Y, AromaticExtractorLayout.FUEL_BAR_W, AromaticExtractorLayout.FUEL_BAR_H, mouseX, mouseY)) {
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
