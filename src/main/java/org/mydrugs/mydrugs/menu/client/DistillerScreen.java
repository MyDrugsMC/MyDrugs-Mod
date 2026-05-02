package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.DistillerMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.DistillerLayout;

import java.util.List;

public class DistillerScreen extends AbstractMachineScreen<DistillerMenu> {
    private InvisibleButton runButton;
    private InvisibleButton dumpInputButton;
    private InvisibleButton dumpOutputAButton;
    private InvisibleButton dumpOutputBButton;

    public DistillerScreen(DistillerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, DistillerLayout.GUI_WIDTH, DistillerLayout.GUI_HEIGHT);
    }

    @Override
    protected boolean shouldRenderSharedEnergyBar() {
        return false;
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
        MachineGuiRenderer.drawDistiller(
                this,
                graphics,
                new MachineGuiRenderer.DistillerState(
                        MachineGuiRenderer.TankFill.live(this.menu.getInputFluid(), this.menu.getScaledInputTank(DistillerLayout.TANK_INNER_H)),
                        MachineGuiRenderer.TankFill.live(this.menu.getOutputAFluid(), this.menu.getScaledOutputATank(DistillerLayout.TANK_INNER_H)),
                        MachineGuiRenderer.TankFill.live(this.menu.getOutputBFluid(), this.menu.getScaledOutputBTank(DistillerLayout.TANK_INNER_H)),
                        this.menu.getScaledProgress(DistillerLayout.PROGRESS_W),
                        this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                        this.dumpOutputAButton != null && this.dumpOutputAButton.isHoveredOrFocused(),
                        this.dumpOutputBButton != null && this.dumpOutputBButton.isHoveredOrFocused(),
                        this.menu.getInputTankAmount() > 0,
                        this.menu.getOutputATankAmount() > 0,
                        this.menu.getOutputBTankAmount() > 0,
                        this.runButton != null && this.runButton.isHoveredOrFocused(),
                        this.menu.isWorking(),
                        this.menu.getClicksPerSecond() > 5,
                        this.menu.getClicksPerSecond() + " CPS",
                        this.menu.getSpeedPercent() + "% speed"
                ),
                true
        );
        if (this.menu.hasEnergyStorage()) {
            drawExternalEnergyBar(graphics, this.menu.getEnergyStored(), this.menu.getEnergyCapacity());
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawDistillerLabels(this, graphics, this.font, this.title);
    }

    @Override
    protected List<TransferHighlight> transferPortHighlights(String portIdPath) {
        return switch (portIdPath) {
            case "item_input" -> List.of(slotHighlight(DistillerLayout.INPUT_SLOT_X, DistillerLayout.INPUT_SLOT_Y));
            case "item_output" -> List.of(
                    slotHighlight(DistillerLayout.OUTPUT_A_SLOT_X, DistillerLayout.OUTPUT_A_SLOT_Y),
                    slotHighlight(DistillerLayout.OUTPUT_B_SLOT_X, DistillerLayout.OUTPUT_B_SLOT_Y)
            );
            case "fluid_input" -> List.of(
                    tankHighlight(DistillerLayout.INPUT_TANK_X, DistillerLayout.INPUT_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H),
                    slotHighlight(DistillerLayout.INPUT_SLOT_X, DistillerLayout.INPUT_SLOT_Y)
            );
            case "fluid_output" -> List.of(
                    tankHighlight(DistillerLayout.OUTPUT_A_TANK_X, DistillerLayout.OUTPUT_A_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H),
                    tankHighlight(DistillerLayout.OUTPUT_B_TANK_X, DistillerLayout.OUTPUT_B_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H),
                    slotHighlight(DistillerLayout.OUTPUT_A_SLOT_X, DistillerLayout.OUTPUT_A_SLOT_Y),
                    slotHighlight(DistillerLayout.OUTPUT_B_SLOT_X, DistillerLayout.OUTPUT_B_SLOT_Y)
            );
            case "fuel" -> List.of();
            default -> super.transferPortHighlights(portIdPath);
        };
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (this.menu.hasEnergyStorage() && isHoveringBox(-18, 24, 10, 50, mouseX, mouseY)) {
            renderExternalEnergyTooltip(graphics, mouseX, mouseY, this.menu.getEnergyStored(), this.menu.getEnergyCapacity());
        } else if (isHoveringBox(DistillerLayout.DUMP_INPUT_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, DistillerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.dump_input_tank"));
        } else if (isHoveringBox(DistillerLayout.DUMP_OUTPUT_A_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, DistillerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.dump_output_tank_a"));
        } else if (isHoveringBox(DistillerLayout.DUMP_OUTPUT_B_X, DistillerLayout.DUMP_BUTTON_Y, DistillerLayout.DUMP_BUTTON_SIZE, DistillerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.translatable("screen.mydrugs.ui.dump_output_tank_b"));
        } else if (isHoveringBox(DistillerLayout.INPUT_TANK_X, DistillerLayout.INPUT_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.input_tank"),
                    Component.literal(getFluidName(this.menu.getInputFluid())),
                    Component.literal(this.menu.getInputTankAmount() + " / " + DistillerMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(DistillerLayout.OUTPUT_A_TANK_X, DistillerLayout.OUTPUT_A_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.output_tank_a"),
                    Component.literal(getFluidName(this.menu.getOutputAFluid())),
                    Component.literal(this.menu.getOutputATankAmount() + " / " + DistillerMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(DistillerLayout.OUTPUT_B_TANK_X, DistillerLayout.OUTPUT_B_TANK_Y, DistillerLayout.TANK_W, DistillerLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.output_tank_b"),
                    Component.literal(getFluidName(this.menu.getOutputBFluid())),
                    Component.literal(this.menu.getOutputBTankAmount() + " / " + DistillerMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(DistillerLayout.PROGRESS_X, DistillerLayout.PROGRESS_Y, DistillerLayout.PROGRESS_W, DistillerLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.distillation_progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        } else if (isHoveringBox(DistillerLayout.RUN_BUTTON_X, DistillerLayout.RUN_BUTTON_Y, DistillerLayout.RUN_BUTTON_SIZE, DistillerLayout.RUN_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.translatable("screen.mydrugs.ui.run_distiller"),
                    Component.translatable("screen.mydrugs.ui.more_than_5_cps_increases_speed")
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
