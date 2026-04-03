package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.mydrugs.mydrugs.menu.BiochemicalReactorMenu;
import org.mydrugs.mydrugs.menu.layout.BiochemicalReactorLayout;

import java.util.ArrayList;
import java.util.List;

public class BiochemicalReactorScreen extends AbstractContainerScreen<BiochemicalReactorMenu> {
    private Button manualBoostButton;

    public BiochemicalReactorScreen(BiochemicalReactorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = BiochemicalReactorLayout.GUI_WIDTH;
        this.imageHeight = BiochemicalReactorLayout.GUI_HEIGHT;
        this.titleLabelX = 14;
        this.titleLabelY = 6;
        this.inventoryLabelX = 17;
        this.inventoryLabelY = 94;
    }

    @Override
    protected void init() {
        super.init();

        this.manualBoostButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + BiochemicalReactorLayout.MANUAL_BUTTON_X,
                this.topPos + BiochemicalReactorLayout.MANUAL_BUTTON_Y,
                BiochemicalReactorLayout.MANUAL_BUTTON_W,
                BiochemicalReactorLayout.MANUAL_BUTTON_H,
                button -> this.onMenuButtonPressed(BiochemicalReactorMenu.MANUAL_BOOST_BUTTON_ID)
        ));
    }

    private void onMenuButtonPressed(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    private static int getFluidColor(Fluid fluid) {
        if (fluid == Fluids.EMPTY) {
            return 0;
        }

        int color = IClientFluidTypeExtensions.of(fluid).getTintColor();
        if ((color >>> 24) == 0) {
            color |= 0xFF000000;
        }
        return color;
    }

    private static String getFluidName(Fluid fluid) {
        if (fluid == Fluids.EMPTY) {
            return "empty";
        }
        return fluid.getFluidType().getDescription().getString();
    }

    private static int darken(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        r = Math.max(0, Math.min(255, (int) (r * factor)));
        g = Math.max(0, Math.min(255, (int) (g * factor)));
        b = Math.max(0, Math.min(255, (int) (b * factor)));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lighten(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        r = Math.max(0, Math.min(255, (int) (r * factor)));
        g = Math.max(0, Math.min(255, (int) (g * factor)));
        b = Math.max(0, Math.min(255, (int) (b * factor)));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF171717);
        graphics.fill(left + 4, top + 4, left + this.imageWidth - 4, top + this.imageHeight - 4, 0xFF252525);

        fillPanel(
                graphics,
                left + BiochemicalReactorLayout.MACHINE_PANEL_X,
                top + BiochemicalReactorLayout.MACHINE_PANEL_Y,
                BiochemicalReactorLayout.MACHINE_PANEL_W,
                BiochemicalReactorLayout.MACHINE_PANEL_H,
                0xFF2E3138
        );

        fillPanel(
                graphics,
                left + BiochemicalReactorLayout.INVENTORY_PANEL_X,
                top + BiochemicalReactorLayout.INVENTORY_PANEL_Y,
                BiochemicalReactorLayout.INVENTORY_PANEL_W,
                BiochemicalReactorLayout.INVENTORY_PANEL_H,
                0xFF2A2D33
        );

        drawSlotFrame(graphics, BiochemicalReactorLayout.ERGOT_SLOT_X, BiochemicalReactorLayout.ERGOT_SLOT_Y);
        drawSlotFrame(graphics, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_X, BiochemicalReactorLayout.TRYPTOPHAN_SLOT_Y);
        drawSlotFrame(graphics, BiochemicalReactorLayout.CHARCOAL_SLOT_X, BiochemicalReactorLayout.CHARCOAL_SLOT_Y);
        drawSlotFrame(graphics, BiochemicalReactorLayout.OUTPUT_SLOT_X, BiochemicalReactorLayout.OUTPUT_SLOT_Y);

        drawProgressBar(
                graphics,
                left + BiochemicalReactorLayout.PROGRESS_X,
                top + BiochemicalReactorLayout.PROGRESS_Y,
                this.menu.getScaledProgress(BiochemicalReactorLayout.PROGRESS_W)
        );

        drawVerticalBarFrame(graphics, BiochemicalReactorLayout.HEAT_BAR_X, BiochemicalReactorLayout.HEAT_BAR_Y,
                BiochemicalReactorLayout.HEAT_BAR_W, BiochemicalReactorLayout.HEAT_BAR_H);
        drawVerticalFill(
                graphics,
                left + BiochemicalReactorLayout.HEAT_BAR_X,
                top + BiochemicalReactorLayout.HEAT_BAR_Y,
                BiochemicalReactorLayout.HEAT_BAR_INNER_X_OFFSET,
                BiochemicalReactorLayout.HEAT_BAR_INNER_Y_OFFSET,
                BiochemicalReactorLayout.HEAT_BAR_INNER_W,
                BiochemicalReactorLayout.HEAT_BAR_INNER_H,
                this.menu.getScaledHeat(BiochemicalReactorLayout.HEAT_BAR_INNER_H),
                0xFFE38D3F
        );

        drawVerticalBarFrame(graphics, BiochemicalReactorLayout.MANUAL_BAR_X, BiochemicalReactorLayout.MANUAL_BAR_Y,
                BiochemicalReactorLayout.MANUAL_BAR_W, BiochemicalReactorLayout.MANUAL_BAR_H);
        drawVerticalFill(
                graphics,
                left + BiochemicalReactorLayout.MANUAL_BAR_X,
                top + BiochemicalReactorLayout.MANUAL_BAR_Y,
                BiochemicalReactorLayout.MANUAL_BAR_INNER_X_OFFSET,
                BiochemicalReactorLayout.MANUAL_BAR_INNER_Y_OFFSET,
                BiochemicalReactorLayout.MANUAL_BAR_INNER_W,
                BiochemicalReactorLayout.MANUAL_BAR_INNER_H,
                this.menu.getScaledManualEnergy(BiochemicalReactorLayout.MANUAL_BAR_INNER_H),
                0xFF77A8E8
        );

        drawTankFrame(graphics, BiochemicalReactorLayout.OUTPUT_TANK_X, BiochemicalReactorLayout.OUTPUT_TANK_Y);
        drawTankFill(
                graphics,
                left + BiochemicalReactorLayout.OUTPUT_TANK_X,
                top + BiochemicalReactorLayout.OUTPUT_TANK_Y,
                this.menu.getScaledOutputTank(BiochemicalReactorLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputFluid())
        );

        drawManualButton(
                graphics,
                BiochemicalReactorLayout.MANUAL_BUTTON_X,
                BiochemicalReactorLayout.MANUAL_BUTTON_Y,
                this.manualBoostButton != null && this.manualBoostButton.isHoveredOrFocused()
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        //graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFD8DCE6, false);
        //graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFB5BAC5, false);

        String status = this.menu.isWorking() ? "Processing" : "Idle";
        graphics.drawString(this.font, status, 58, 22, 0xFFB5BAC5, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

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
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output Container")
            );
        }
    }

    private void fillPanel(GuiGraphics graphics, int x, int y, int width, int height, int fillColor) {
        graphics.fill(x, y, x + width, y + height, fillColor);
        drawBorder(graphics, x, y, width, height);
    }

    private void drawBorder(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + 1, 0xFF5C616B);
        graphics.fill(x, y + height - 1, x + width, y + height, 0xFF0E1014);
        graphics.fill(x, y, x + 1, y + height, 0xFF5C616B);
        graphics.fill(x + width - 1, y, x + width, y + height, 0xFF0E1014);
    }

    private void drawSlotFrame(GuiGraphics graphics, int slotX, int slotY) {
        int x = this.leftPos + slotX - 1;
        int y = this.topPos + slotY - 1;

        graphics.fill(x, y, x + 18, y + 18, 0xFF8A8F99);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF101216);
    }

    private void drawProgressBar(GuiGraphics graphics, int x, int y, int progress) {
        graphics.fill(x - 1, y - 1, x + BiochemicalReactorLayout.PROGRESS_W + 1, y + BiochemicalReactorLayout.PROGRESS_H + 1, 0xFF767C88);
        graphics.fill(x, y, x + BiochemicalReactorLayout.PROGRESS_W, y + BiochemicalReactorLayout.PROGRESS_H, 0xFF101216);

        if (progress > 0) {
            int clamped = Math.min(BiochemicalReactorLayout.PROGRESS_W, progress);
            graphics.fill(x, y, x + clamped, y + BiochemicalReactorLayout.PROGRESS_H, 0xFF768AB8);
            graphics.fill(x, y, x + clamped, y + 2, 0xFFAAB9DB);
        }
    }

    private void drawVerticalBarFrame(GuiGraphics graphics, int localX, int localY, int width, int height) {
        int x = this.leftPos + localX;
        int y = this.topPos + localY;
        graphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF767C88);
        graphics.fill(x, y, x + width, y + height, 0xFF101216);
    }

    private void drawVerticalFill(
            GuiGraphics graphics,
            int x,
            int y,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH,
            int filledPixels,
            int color
    ) {
        int x1 = x + innerOffsetX;
        int y1 = y + innerOffsetY;
        int x2 = x1 + innerW;
        int y2 = y1 + innerH;

        graphics.fill(x1, y1, x2, y2, 0xFF090A0D);

        if (filledPixels > 0) {
            int clamped = Math.min(innerH, filledPixels);
            int fillTop = y2 - clamped;

            graphics.fill(x1, fillTop, x2, y2, color);
            graphics.fill(x1, fillTop, x2, fillTop + 2, 0x22FFFFFF);
        }
    }

    private void drawTankFrame(GuiGraphics graphics, int tankX, int tankY) {
        int x = this.leftPos + tankX;
        int y = this.topPos + tankY;

        graphics.fill(x - 1, y - 1, x + BiochemicalReactorLayout.TANK_W + 1, y + BiochemicalReactorLayout.TANK_H + 1, 0xFF7F8590);
        graphics.fill(x, y, x + BiochemicalReactorLayout.TANK_W, y + BiochemicalReactorLayout.TANK_H, 0xFF15171B);

        graphics.fill(
                x + BiochemicalReactorLayout.TANK_INNER_X_OFFSET,
                y + BiochemicalReactorLayout.TANK_INNER_Y_OFFSET,
                x + BiochemicalReactorLayout.TANK_INNER_X_OFFSET + BiochemicalReactorLayout.TANK_INNER_W,
                y + BiochemicalReactorLayout.TANK_INNER_Y_OFFSET + BiochemicalReactorLayout.TANK_INNER_H,
                0xFF090A0D
        );
    }

    private void drawTankFill(GuiGraphics graphics, int tankX, int tankY, int filledPixels, int color) {
        if (filledPixels <= 0 || (color >>> 24) == 0) {
            return;
        }

        int x1 = tankX + BiochemicalReactorLayout.TANK_INNER_X_OFFSET;
        int y1 = tankY + BiochemicalReactorLayout.TANK_INNER_Y_OFFSET;
        int x2 = x1 + BiochemicalReactorLayout.TANK_INNER_W;
        int y2 = y1 + BiochemicalReactorLayout.TANK_INNER_H;
        int fillTop = y2 - filledPixels;

        graphics.fill(x1, fillTop, x2, y2, color);

        int darker = darken(color, 0.72f);
        int lighter = lighten(color, 1.15f);

        graphics.fill(x1 + 1, fillTop, x1 + 2, y2, lighter);
        graphics.fill(x1 + 3, fillTop, x1 + 5, y2, darker);
    }

    private void drawManualButton(GuiGraphics graphics, int localX, int localY, boolean hovered) {
        int x = this.leftPos + localX;
        int y = this.topPos + localY;

        graphics.fill(x, y, x + BiochemicalReactorLayout.MANUAL_BUTTON_W, y + BiochemicalReactorLayout.MANUAL_BUTTON_H, 0xFF7C818C);
        graphics.fill(x + 1, y + 1, x + BiochemicalReactorLayout.MANUAL_BUTTON_W - 1, y + BiochemicalReactorLayout.MANUAL_BUTTON_H - 1, 0xFF181A1F);

        if (hovered) {
            graphics.fill(x + 1, y + 1, x + BiochemicalReactorLayout.MANUAL_BUTTON_W - 1, y + BiochemicalReactorLayout.MANUAL_BUTTON_H - 1, 0x22FFFFFF);
        }

        graphics.drawString(this.font, "+", x + 6, y + 5, 0xFFD8DCE6, false);
    }

    private boolean isHoveringBox(int localX, int localY, int width, int height, int mouseX, int mouseY) {
        int x = this.leftPos + localX;
        int y = this.topPos + localY;
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void renderTooltipLines(GuiGraphics graphics, int mouseX, int mouseY, Component... lines) {
        List<ClientTooltipComponent> components = new ArrayList<>(lines.length);
        for (Component line : lines) {
            components.add(ClientTooltipComponent.create(line.getVisualOrderText()));
        }

        graphics.renderTooltip(
                this.font,
                components,
                mouseX,
                mouseY,
                DefaultTooltipPositioner.INSTANCE,
                null
        );
    }

    private static class InvisibleButton extends Button {
        public InvisibleButton(int x, int y, int width, int height, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        }
    }
}