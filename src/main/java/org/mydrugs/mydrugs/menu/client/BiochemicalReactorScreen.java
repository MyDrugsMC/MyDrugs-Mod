package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.menu.BiochemicalReactorMenu;
import org.mydrugs.mydrugs.menu.layout.BiochemicalReactorLayout;

public class BiochemicalReactorScreen extends AbstractMachineScreen<BiochemicalReactorMenu> {
    private InvisibleButton manualBoostButton;

    public BiochemicalReactorScreen(BiochemicalReactorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, BiochemicalReactorLayout.GUI_WIDTH, BiochemicalReactorLayout.GUI_HEIGHT);
        this.titleLabelX = 14;
        this.titleLabelY = 6;
        this.inventoryLabelX = 17;
        this.inventoryLabelY = 94;
    }

    @Override
    protected void init() {
        super.init();

        this.manualBoostButton = this.addRenderableWidget(new InvisibleButton(
                guiX(BiochemicalReactorLayout.MANUAL_BUTTON_X),
                guiY(BiochemicalReactorLayout.MANUAL_BUTTON_Y),
                BiochemicalReactorLayout.MANUAL_BUTTON_W,
                BiochemicalReactorLayout.MANUAL_BUTTON_H,
                button -> pressMenuButton(BiochemicalReactorMenu.MANUAL_BOOST_BUTTON_ID)
        ));
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        drawWindow(graphics);

        drawPanel(
                graphics,
                BiochemicalReactorLayout.MACHINE_PANEL_X,
                BiochemicalReactorLayout.MACHINE_PANEL_Y,
                BiochemicalReactorLayout.MACHINE_PANEL_W,
                BiochemicalReactorLayout.MACHINE_PANEL_H,
                0xFF323232
        );

        drawSieveInventoryPanels(
                graphics,
                BiochemicalReactorLayout.PLAYER_INV_X,
                BiochemicalReactorLayout.PLAYER_INV_Y
        );

        drawSlotFrame(graphics, BiochemicalReactorLayout.ERGOT_SLOT_X, BiochemicalReactorLayout.ERGOT_SLOT_Y);
        drawSlotFrame(graphics, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_X, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_Y);
        drawSlotFrame(graphics, BiochemicalReactorLayout.CHARCOAL_SLOT_X, BiochemicalReactorLayout.CHARCOAL_SLOT_Y);
        drawSlotFrame(graphics, BiochemicalReactorLayout.OUTPUT_SLOT_X, BiochemicalReactorLayout.OUTPUT_SLOT_Y);

        drawHorizontalBar(
                graphics,
                BiochemicalReactorLayout.PROGRESS_X,
                BiochemicalReactorLayout.PROGRESS_Y,
                BiochemicalReactorLayout.PROGRESS_W,
                BiochemicalReactorLayout.PROGRESS_H,
                this.menu.getScaledProgress(BiochemicalReactorLayout.PROGRESS_W),
                0xFF768AB8,
                0xFFAAB9DB
        );

        drawVerticalBar(
                graphics,
                BiochemicalReactorLayout.HEAT_BAR_X,
                BiochemicalReactorLayout.HEAT_BAR_Y,
                BiochemicalReactorLayout.HEAT_BAR_W,
                BiochemicalReactorLayout.HEAT_BAR_H,
                BiochemicalReactorLayout.HEAT_BAR_INNER_X_OFFSET,
                BiochemicalReactorLayout.HEAT_BAR_INNER_Y_OFFSET,
                BiochemicalReactorLayout.HEAT_BAR_INNER_W,
                BiochemicalReactorLayout.HEAT_BAR_INNER_H,
                this.menu.getScaledHeat(BiochemicalReactorLayout.HEAT_BAR_INNER_H),
                0xFFE38D3F,
                0x22FFFFFF
        );

        drawVerticalBar(
                graphics,
                BiochemicalReactorLayout.MANUAL_BAR_X,
                BiochemicalReactorLayout.MANUAL_BAR_Y,
                BiochemicalReactorLayout.MANUAL_BAR_W,
                BiochemicalReactorLayout.MANUAL_BAR_H,
                BiochemicalReactorLayout.MANUAL_BAR_INNER_X_OFFSET,
                BiochemicalReactorLayout.MANUAL_BAR_INNER_Y_OFFSET,
                BiochemicalReactorLayout.MANUAL_BAR_INNER_W,
                BiochemicalReactorLayout.MANUAL_BAR_INNER_H,
                this.menu.getScaledManualEnergy(BiochemicalReactorLayout.MANUAL_BAR_INNER_H),
                0xFF77A8E8,
                0x22FFFFFF
        );

        drawTankFrame(
                graphics,
                BiochemicalReactorLayout.OUTPUT_TANK_X,
                BiochemicalReactorLayout.OUTPUT_TANK_Y,
                BiochemicalReactorLayout.TANK_W,
                BiochemicalReactorLayout.TANK_H,
                BiochemicalReactorLayout.TANK_INNER_X_OFFSET,
                BiochemicalReactorLayout.TANK_INNER_Y_OFFSET,
                BiochemicalReactorLayout.TANK_INNER_W,
                BiochemicalReactorLayout.TANK_INNER_H
        );

        drawTankFillShaded(
                graphics,
                BiochemicalReactorLayout.OUTPUT_TANK_X,
                BiochemicalReactorLayout.OUTPUT_TANK_Y,
                BiochemicalReactorLayout.TANK_INNER_X_OFFSET,
                BiochemicalReactorLayout.TANK_INNER_Y_OFFSET,
                BiochemicalReactorLayout.TANK_INNER_W,
                BiochemicalReactorLayout.TANK_INNER_H,
                this.menu.getScaledOutputTank(BiochemicalReactorLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputFluid())
        );

        drawPlusButton(
                graphics,
                BiochemicalReactorLayout.MANUAL_BUTTON_X,
                BiochemicalReactorLayout.MANUAL_BUTTON_Y,
                BiochemicalReactorLayout.MANUAL_BUTTON_W,
                BiochemicalReactorLayout.MANUAL_BUTTON_H,
                this.manualBoostButton != null && this.manualBoostButton.isHoveredOrFocused()
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, BiochemicalReactorLayout.CHARCOAL_SLOT_X, BiochemicalReactorLayout.MACHINE_PANEL_Y + 4, 0xFFFFFFFF);
        String status = this.menu.isWorking() ? "Processing" : "Idle";
        graphics.drawString(this.font, status, 58, BiochemicalReactorLayout.PROGRESS_Y - 10, 0xFFB5BAC5, false);
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(BiochemicalReactorLayout.PROGRESS_X, BiochemicalReactorLayout.PROGRESS_Y,
                BiochemicalReactorLayout.PROGRESS_W, BiochemicalReactorLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Progress"),
                    Component.literal(this.menu.getProgressUnits() + " / " + this.menu.getMaxProgressUnits() + " units")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.HEAT_BAR_X, BiochemicalReactorLayout.HEAT_BAR_Y,
                BiochemicalReactorLayout.HEAT_BAR_W, BiochemicalReactorLayout.HEAT_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Heat"),
                    Component.literal(this.menu.getHeat() + " / " + this.menu.getMaxHeat()),
                    Component.literal("Raises processing speed")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.MANUAL_BAR_X, BiochemicalReactorLayout.MANUAL_BAR_Y,
                BiochemicalReactorLayout.MANUAL_BAR_W, BiochemicalReactorLayout.MANUAL_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Manual Energy"),
                    Component.literal(this.menu.getManualEnergy() + " / " + this.menu.getMaxManualEnergy()),
                    Component.literal("Generated by player interaction")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.OUTPUT_TANK_X, BiochemicalReactorLayout.OUTPUT_TANK_Y,
                BiochemicalReactorLayout.TANK_W, BiochemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output Tank"),
                    Component.literal(getFluidName(this.menu.getOutputFluid())),
                    Component.literal(this.menu.getOutputTankAmount() + " / " + this.menu.getOutputTankCapacity() + " mB")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.MANUAL_BUTTON_X, BiochemicalReactorLayout.MANUAL_BUTTON_Y,
                BiochemicalReactorLayout.MANUAL_BUTTON_W, BiochemicalReactorLayout.MANUAL_BUTTON_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Manual Boost"),
                    Component.literal("Click to add manual energy")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.ERGOT_SLOT_X, BiochemicalReactorLayout.ERGOT_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Ergot"),
                    Component.literal("More Ergot in this slot increases speed")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.TRYPTOPHAN_SLOT_X, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Tryptophan"));
        } else if (isHoveringBox(BiochemicalReactorLayout.CHARCOAL_SLOT_X, BiochemicalReactorLayout.CHARCOAL_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Charcoal"),
                    Component.literal("Adds heat to the reactor")
            );
        } else if (isHoveringBox(BiochemicalReactorLayout.OUTPUT_SLOT_X, BiochemicalReactorLayout.OUTPUT_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Output Container"));
        }
    }
}