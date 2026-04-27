package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.blocks.entity.AromaticExtractorBlockEntity;
import org.mydrugs.mydrugs.menu.AromaticExtractorMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
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
        MachineGuiRenderer.AromaticExtractorState state = new MachineGuiRenderer.AromaticExtractorState(
                MachineGuiRenderer.TankFill.live(this.menu.getInputFluid(), this.menu.getScaledInputTank(AromaticExtractorLayout.TANK_INNER_H)),
                MachineGuiRenderer.TankFill.live(this.menu.getCatalystFluid(), this.menu.getScaledCatalystTank(AromaticExtractorLayout.TANK_INNER_H)),
                MachineGuiRenderer.TankFill.live(this.menu.getOutputAFluid(), this.menu.getScaledOutputATank(AromaticExtractorLayout.TANK_INNER_H)),
                MachineGuiRenderer.TankFill.live(this.menu.getOutputBFluid(), this.menu.getScaledOutputBTank(AromaticExtractorLayout.TANK_INNER_H)),
                this.menu.getScaledProgress(AromaticExtractorLayout.PROGRESS_W),
                this.menu.getScaledBurnTime(AromaticExtractorLayout.FUEL_BAR_INNER_H),
                this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                this.dumpCatalystButton != null && this.dumpCatalystButton.isHoveredOrFocused(),
                this.dumpOutputAButton != null && this.dumpOutputAButton.isHoveredOrFocused(),
                this.dumpOutputBButton != null && this.dumpOutputBButton.isHoveredOrFocused(),
                this.menu.getInputTankAmount() > 0,
                this.menu.getCatalystTankAmount() > 0,
                this.menu.getOutputATankAmount() > 0,
                this.menu.getOutputBTankAmount() > 0
        );

        MachineGuiRenderer.drawAromaticExtractor(
                this,
                graphics,
                state,
                true
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, this.title, AromaticExtractorLayout.GUI_WIDTH / 2, 5, 0xFFFFFFFF);
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
