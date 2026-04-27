package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.CentrifugeMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
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
        MachineGuiRenderer.drawCentrifuge(
                this,
                graphics,
                new MachineGuiRenderer.CentrifugeState(
                        MachineGuiRenderer.TankFill.live(this.menu.getInputFluid(), this.menu.getScaledInputTank(CentrifugeLayout.TANK_INNER_H)),
                        MachineGuiRenderer.TankFill.live(this.menu.getOutputAFluid(), this.menu.getScaledOutputATank(CentrifugeLayout.TANK_INNER_H)),
                        MachineGuiRenderer.TankFill.live(this.menu.getOutputBFluid(), this.menu.getScaledOutputBTank(CentrifugeLayout.TANK_INNER_H)),
                        this.menu.getScaledProgress(CentrifugeLayout.PROGRESS_W),
                        this.menu.getScaledBurnTime(CentrifugeLayout.FUEL_BAR_INNER_H),
                        this.menu.isLit() ? 0xFFE38D3F : 0xFF8E6A4A,
                        this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                        this.dumpOutputAButton != null && this.dumpOutputAButton.isHoveredOrFocused(),
                        this.dumpOutputBButton != null && this.dumpOutputBButton.isHoveredOrFocused(),
                        this.menu.getInputTankAmount() > 0,
                        this.menu.getOutputATankAmount() > 0,
                        this.menu.getOutputBTankAmount() > 0
                ),
                true
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawCentrifugeLabels(this, graphics, this.font, this.title);
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
