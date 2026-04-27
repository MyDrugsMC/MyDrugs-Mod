package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.BTXFractionationTowerMenu;
import org.mydrugs.mydrugs.menu.client.util.MachineGuiRenderer;
import org.mydrugs.mydrugs.menu.layout.BTXFractionationTowerLayout;

public class BTXFractionationTowerScreen extends AbstractMachineScreen<BTXFractionationTowerMenu> {
    private InvisibleButton dumpInputButton;
    private InvisibleButton dumpBenzeneButton;
    private InvisibleButton dumpTolueneButton;
    private InvisibleButton dumpXyleneButton;

    public BTXFractionationTowerScreen(BTXFractionationTowerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, BTXFractionationTowerLayout.GUI_WIDTH, BTXFractionationTowerLayout.GUI_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        this.dumpInputButton = this.addRenderableWidget(new InvisibleButton(
                guiX(BTXFractionationTowerLayout.DUMP_INPUT_X),
                guiY(BTXFractionationTowerLayout.DUMP_BUTTON_Y),
                BTXFractionationTowerLayout.DUMP_BUTTON_SIZE,
                BTXFractionationTowerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(BTXFractionationTowerMenu.DUMP_INPUT_BUTTON_ID)
        ));

        this.dumpBenzeneButton = this.addRenderableWidget(new InvisibleButton(
                guiX(BTXFractionationTowerLayout.DUMP_BENZENE_X),
                guiY(BTXFractionationTowerLayout.DUMP_BUTTON_Y),
                BTXFractionationTowerLayout.DUMP_BUTTON_SIZE,
                BTXFractionationTowerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(BTXFractionationTowerMenu.DUMP_BENZENE_BUTTON_ID)
        ));

        this.dumpTolueneButton = this.addRenderableWidget(new InvisibleButton(
                guiX(BTXFractionationTowerLayout.DUMP_TOLUENE_X),
                guiY(BTXFractionationTowerLayout.DUMP_BUTTON_Y),
                BTXFractionationTowerLayout.DUMP_BUTTON_SIZE,
                BTXFractionationTowerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(BTXFractionationTowerMenu.DUMP_TOLUENE_BUTTON_ID)
        ));

        this.dumpXyleneButton = this.addRenderableWidget(new InvisibleButton(
                guiX(BTXFractionationTowerLayout.DUMP_XYLENE_X),
                guiY(BTXFractionationTowerLayout.DUMP_BUTTON_Y),
                BTXFractionationTowerLayout.DUMP_BUTTON_SIZE,
                BTXFractionationTowerLayout.DUMP_BUTTON_SIZE,
                button -> pressMenuButton(BTXFractionationTowerMenu.DUMP_XYLENE_BUTTON_ID)
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MachineGuiRenderer.drawBTXFractionationTower(
                this,
                graphics,
                new MachineGuiRenderer.BTXFractionationTowerState(
                        MachineGuiRenderer.TankFill.live(this.menu.getInputFluid(), this.menu.getScaledInputTank(BTXFractionationTowerLayout.TANK_INNER_H)),
                        MachineGuiRenderer.TankFill.live(this.menu.getBenzeneFluid(), this.menu.getScaledBenzeneTank(BTXFractionationTowerLayout.TANK_INNER_H)),
                        MachineGuiRenderer.TankFill.live(this.menu.getTolueneFluid(), this.menu.getScaledTolueneTank(BTXFractionationTowerLayout.TANK_INNER_H)),
                        MachineGuiRenderer.TankFill.live(this.menu.getXyleneFluid(), this.menu.getScaledXyleneTank(BTXFractionationTowerLayout.TANK_INNER_H)),
                        this.menu.getScaledProgress(BTXFractionationTowerLayout.PROGRESS_W),
                        this.menu.getScaledBurnTime(BTXFractionationTowerLayout.FUEL_BAR_INNER_H),
                        this.menu.isLit() ? 0xFFE38D3F : 0xFF8E6A4A,
                        this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                        this.dumpBenzeneButton != null && this.dumpBenzeneButton.isHoveredOrFocused(),
                        this.dumpTolueneButton != null && this.dumpTolueneButton.isHoveredOrFocused(),
                        this.dumpXyleneButton != null && this.dumpXyleneButton.isHoveredOrFocused(),
                        this.menu.getInputTankAmount() > 0,
                        this.menu.getBenzeneTankAmount() > 0,
                        this.menu.getTolueneTankAmount() > 0,
                        this.menu.getXyleneTankAmount() > 0
                ),
                true
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        MachineGuiRenderer.drawBTXFractionationTowerLabels(this, graphics, this.font, this.title, null);
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(BTXFractionationTowerLayout.DUMP_INPUT_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump input tank"));
        } else if (isHoveringBox(BTXFractionationTowerLayout.DUMP_BENZENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump benzene tank"));
        } else if (isHoveringBox(BTXFractionationTowerLayout.DUMP_TOLUENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump toluene tank"));
        } else if (isHoveringBox(BTXFractionationTowerLayout.DUMP_XYLENE_X, BTXFractionationTowerLayout.DUMP_BUTTON_Y, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, BTXFractionationTowerLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump xylene tank"));
        } else if (isHoveringBox(BTXFractionationTowerLayout.INPUT_TANK_X, BTXFractionationTowerLayout.INPUT_TANK_Y, BTXFractionationTowerLayout.TANK_W, BTXFractionationTowerLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("BTX Mix input tank"),
                    Component.literal(getFluidName(this.menu.getInputFluid())),
                    Component.literal(this.menu.getInputTankAmount() + " / " + BTXFractionationTowerMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(BTXFractionationTowerLayout.BENZENE_TANK_X, BTXFractionationTowerLayout.BENZENE_TANK_Y, BTXFractionationTowerLayout.TANK_W, BTXFractionationTowerLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Benzene output tank"),
                    Component.literal(getFluidName(this.menu.getBenzeneFluid())),
                    Component.literal(this.menu.getBenzeneTankAmount() + " / " + BTXFractionationTowerMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(BTXFractionationTowerLayout.TOLUENE_TANK_X, BTXFractionationTowerLayout.TOLUENE_TANK_Y, BTXFractionationTowerLayout.TANK_W, BTXFractionationTowerLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Toluene output tank"),
                    Component.literal(getFluidName(this.menu.getTolueneFluid())),
                    Component.literal(this.menu.getTolueneTankAmount() + " / " + BTXFractionationTowerMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(BTXFractionationTowerLayout.XYLENE_TANK_X, BTXFractionationTowerLayout.XYLENE_TANK_Y, BTXFractionationTowerLayout.TANK_W, BTXFractionationTowerLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Xylene output tank"),
                    Component.literal(getFluidName(this.menu.getXyleneFluid())),
                    Component.literal(this.menu.getXyleneTankAmount() + " / " + BTXFractionationTowerMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(BTXFractionationTowerLayout.PROGRESS_X, BTXFractionationTowerLayout.PROGRESS_Y, BTXFractionationTowerLayout.PROGRESS_W, BTXFractionationTowerLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Fractionation progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        } else if (isHoveringBox(BTXFractionationTowerLayout.FUEL_BAR_X, BTXFractionationTowerLayout.FUEL_BAR_Y, BTXFractionationTowerLayout.FUEL_BAR_W, BTXFractionationTowerLayout.FUEL_BAR_H, mouseX, mouseY)) {
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
