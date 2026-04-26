package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.FluidFiltererMenu;
import org.mydrugs.mydrugs.menu.layout.ElectrolyzerLayout;
import org.mydrugs.mydrugs.menu.layout.FluidFiltererLayout;

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
        drawWindow(graphics);

        drawPanel(
                graphics,
                FluidFiltererLayout.MACHINE_PANEL_X,
                FluidFiltererLayout.MACHINE_PANEL_Y,
                FluidFiltererLayout.MACHINE_PANEL_W,
                FluidFiltererLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        drawSieveInventoryPanels(
                graphics,
                FluidFiltererLayout.PLAYER_INV_X,
                FluidFiltererLayout.PLAYER_INV_Y
        );

        drawTankFrame(
                graphics,
                FluidFiltererLayout.INPUT_TANK_X,
                FluidFiltererLayout.INPUT_TANK_Y,
                FluidFiltererLayout.TANK_W,
                FluidFiltererLayout.TANK_H,
                FluidFiltererLayout.TANK_INNER_X_OFFSET,
                FluidFiltererLayout.TANK_INNER_Y_OFFSET,
                FluidFiltererLayout.TANK_INNER_W,
                FluidFiltererLayout.TANK_INNER_H
        );
        drawTankFrame(
                graphics,
                FluidFiltererLayout.OUTPUT_A_TANK_X,
                FluidFiltererLayout.OUTPUT_A_TANK_Y,
                FluidFiltererLayout.TANK_W,
                FluidFiltererLayout.TANK_H,
                FluidFiltererLayout.TANK_INNER_X_OFFSET,
                FluidFiltererLayout.TANK_INNER_Y_OFFSET,
                FluidFiltererLayout.TANK_INNER_W,
                FluidFiltererLayout.TANK_INNER_H
        );

        drawTankFillShaded(
                graphics,
                FluidFiltererLayout.INPUT_TANK_X,
                FluidFiltererLayout.INPUT_TANK_Y,
                FluidFiltererLayout.TANK_INNER_X_OFFSET,
                FluidFiltererLayout.TANK_INNER_Y_OFFSET,
                FluidFiltererLayout.TANK_INNER_W,
                FluidFiltererLayout.TANK_INNER_H,
                this.menu.getScaledInputTank(FluidFiltererLayout.TANK_INNER_H),
                getFluidColor(this.menu.getInputFluid())
        );

        drawTankFillShaded(
                graphics,
                FluidFiltererLayout.OUTPUT_A_TANK_X,
                FluidFiltererLayout.OUTPUT_A_TANK_Y,
                FluidFiltererLayout.TANK_INNER_X_OFFSET,
                FluidFiltererLayout.TANK_INNER_Y_OFFSET,
                FluidFiltererLayout.TANK_INNER_W,
                FluidFiltererLayout.TANK_INNER_H,
                this.menu.getScaledOutputATank(FluidFiltererLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputAFluid())
        );

        drawSlotFrame(graphics, FluidFiltererLayout.INPUT_SLOT_X, FluidFiltererLayout.INPUT_SLOT_Y);
        drawSlotFrame(graphics, FluidFiltererLayout.OUTPUT_A_SLOT_X, FluidFiltererLayout.OUTPUT_A_SLOT_Y);
        drawSlotFrame(graphics, FluidFiltererLayout.FILTER_SLOT_X, FluidFiltererLayout.FILTER_SLOT_Y);
        drawSlotFrame(graphics, FluidFiltererLayout.RESIDUE_SLOT_X, FluidFiltererLayout.RESIDUE_SLOT_Y);

        drawHorizontalBar(
                graphics,
                FluidFiltererLayout.PROGRESS_X,
                FluidFiltererLayout.PROGRESS_Y,
                FluidFiltererLayout.PROGRESS_W,
                FluidFiltererLayout.PROGRESS_H,
                this.menu.getScaledProgress(FluidFiltererLayout.PROGRESS_W),
                0xFF768AB8,
                0xFFAAB9DB
        );

        drawDumpButton(
                graphics,
                FluidFiltererLayout.DUMP_INPUT_X,
                FluidFiltererLayout.DUMP_BUTTON_Y,
                FluidFiltererLayout.DUMP_BUTTON_SIZE,
                this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                this.menu.getInputTankAmount() > 0
        );

        drawDumpButton(
                graphics,
                FluidFiltererLayout.DUMP_OUTPUT_A_X,
                FluidFiltererLayout.DUMP_BUTTON_Y,
                FluidFiltererLayout.DUMP_BUTTON_SIZE,
                this.dumpOutputAButton != null && this.dumpOutputAButton.isHoveredOrFocused(),
                this.menu.getOutputATankAmount() > 0
        );

        renderRunButton(graphics, mouseX, mouseY);

        if (this.menu.getMaxProgress() > 0) {
            graphics.drawCenteredString(
                    this.font,
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress()),
                    guiX(this.imageWidth / 2),
                    guiY(FluidFiltererLayout.PROGRESS_TEXT_Y),
                    0xFFE6E6E6
            );
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, this.title, FluidFiltererLayout.GUI_WIDTH / 2, 5, 0xFFFFFFFF);
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(FluidFiltererLayout.DUMP_INPUT_X, FluidFiltererLayout.DUMP_BUTTON_Y, FluidFiltererLayout.DUMP_BUTTON_SIZE, FluidFiltererLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump input tank"));
        } else if (isHoveringBox(FluidFiltererLayout.DUMP_OUTPUT_A_X, FluidFiltererLayout.DUMP_BUTTON_Y, FluidFiltererLayout.DUMP_BUTTON_SIZE, FluidFiltererLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output tank"));
        } else if (isHoveringBox(FluidFiltererLayout.INPUT_TANK_X, FluidFiltererLayout.INPUT_TANK_Y, FluidFiltererLayout.TANK_W, FluidFiltererLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Input tank"),
                    Component.literal(getFluidName(this.menu.getInputFluid())),
                    Component.literal(this.menu.getInputTankAmount() + " / " + FluidFiltererMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(FluidFiltererLayout.OUTPUT_A_TANK_X, FluidFiltererLayout.OUTPUT_A_TANK_Y, FluidFiltererLayout.TANK_W, FluidFiltererLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output tank"),
                    Component.literal(getFluidName(this.menu.getOutputAFluid())),
                    Component.literal(this.menu.getOutputATankAmount() + " / " + FluidFiltererMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(FluidFiltererLayout.INPUT_SLOT_X, FluidFiltererLayout.INPUT_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Input fluid container"));
        } else if (isHoveringBox(FluidFiltererLayout.OUTPUT_A_SLOT_X, FluidFiltererLayout.OUTPUT_A_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Output container"));
        } else if (isHoveringBox(FluidFiltererLayout.PROGRESS_X, FluidFiltererLayout.PROGRESS_Y, FluidFiltererLayout.PROGRESS_W, FluidFiltererLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Filtering progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        } else if (isHoveringBox(FluidFiltererLayout.RUN_BUTTON_X, FluidFiltererLayout.RUN_BUTTON_Y, FluidFiltererLayout.RUN_BUTTON_W, FluidFiltererLayout.RUN_BUTTON_H, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Hold to filter"));
        } else if (isHoveringBox(FluidFiltererLayout.FILTER_SLOT_X, FluidFiltererLayout.FILTER_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Filter slot"));
        } else if (isHoveringBox(FluidFiltererLayout.RESIDUE_SLOT_X, FluidFiltererLayout.RESIDUE_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Waste output"));
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

    private void renderRunButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = guiX(FluidFiltererLayout.RUN_BUTTON_X);
        int y = guiY(FluidFiltererLayout.RUN_BUTTON_Y);

        boolean hovered = isHoveringBox(
                FluidFiltererLayout.RUN_BUTTON_X,
                FluidFiltererLayout.RUN_BUTTON_Y,
                FluidFiltererLayout.RUN_BUTTON_W,
                FluidFiltererLayout.RUN_BUTTON_H,
                mouseX,
                mouseY
        );

        boolean active = this.holdingRunButton || this.menu.isButtonHeld();

        int border = active ? 0xFFA8F17A : (hovered ? 0xFF94C76C : 0xFF688A50);
        int fill = active ? 0xFF56773F : (hovered ? 0xFF4A6536 : 0xFF334A26);

        graphics.fill(x, y, x + FluidFiltererLayout.RUN_BUTTON_W, y + FluidFiltererLayout.RUN_BUTTON_H, border);
        graphics.fill(x + 1, y + 1, x + FluidFiltererLayout.RUN_BUTTON_W - 1, y + FluidFiltererLayout.RUN_BUTTON_H - 1, fill);

        graphics.drawCenteredString(
                this.font,
                active ? Component.literal("FILTERING...") : Component.literal("HOLD"),
                x + FluidFiltererLayout.RUN_BUTTON_W / 2,
                y + 6,
                0xFFF3FFF0
        );
    }
}