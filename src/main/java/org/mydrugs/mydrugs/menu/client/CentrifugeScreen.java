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
import org.mydrugs.mydrugs.menu.CentrifugeMenu;
import org.mydrugs.mydrugs.menu.layout.CentrifugeLayout;

import java.util.ArrayList;
import java.util.List;

public class CentrifugeScreen extends AbstractContainerScreen<CentrifugeMenu> {
    private InvisibleButton dumpInputButton;
    private InvisibleButton dumpOutputAButton;
    private InvisibleButton dumpOutputBButton;

    public CentrifugeScreen(CentrifugeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = CentrifugeLayout.GUI_WIDTH;
        this.imageHeight = CentrifugeLayout.GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        this.dumpInputButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + CentrifugeLayout.DUMP_INPUT_X,
                this.topPos + CentrifugeLayout.DUMP_BUTTON_Y,
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                button -> this.onMenuButtonPressed(CentrifugeMenu.DUMP_INPUT_BUTTON_ID)
        ));

        this.dumpOutputAButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + CentrifugeLayout.DUMP_OUTPUT_A_X,
                this.topPos + CentrifugeLayout.DUMP_BUTTON_Y,
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                button -> this.onMenuButtonPressed(CentrifugeMenu.DUMP_OUTPUT_A_BUTTON_ID)
        ));

        this.dumpOutputBButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + CentrifugeLayout.DUMP_OUTPUT_B_X,
                this.topPos + CentrifugeLayout.DUMP_BUTTON_Y,
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                CentrifugeLayout.DUMP_BUTTON_SIZE,
                button -> this.onMenuButtonPressed(CentrifugeMenu.DUMP_OUTPUT_B_BUTTON_ID)
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
                left + CentrifugeLayout.MACHINE_PANEL_X,
                top + CentrifugeLayout.MACHINE_PANEL_Y,
                CentrifugeLayout.MACHINE_PANEL_W,
                CentrifugeLayout.MACHINE_PANEL_H,
                0xFF2E3138
        );

        fillPanel(
                graphics,
                left + CentrifugeLayout.INVENTORY_PANEL_X,
                top + CentrifugeLayout.INVENTORY_PANEL_Y,
                CentrifugeLayout.INVENTORY_PANEL_W,
                CentrifugeLayout.INVENTORY_PANEL_H,
                0xFF2A2D33
        );

        fillPanel(
                graphics,
                left + CentrifugeLayout.CENTER_PANEL_X,
                top + CentrifugeLayout.CENTER_PANEL_Y,
                CentrifugeLayout.CENTER_PANEL_W,
                CentrifugeLayout.CENTER_PANEL_H,
                0xFF262B32
        );

        drawTankFrame(graphics, CentrifugeLayout.INPUT_TANK_X, CentrifugeLayout.INPUT_TANK_Y);
        drawTankFrame(graphics, CentrifugeLayout.OUTPUT_A_TANK_X, CentrifugeLayout.OUTPUT_A_TANK_Y);
        drawTankFrame(graphics, CentrifugeLayout.OUTPUT_B_TANK_X, CentrifugeLayout.OUTPUT_B_TANK_Y);

        drawTankFill(
                graphics,
                left + CentrifugeLayout.INPUT_TANK_X,
                top + CentrifugeLayout.INPUT_TANK_Y,
                this.menu.getScaledInputTank(CentrifugeLayout.TANK_INNER_H),
                getFluidColor(this.menu.getInputFluid())
        );

        drawTankFill(
                graphics,
                left + CentrifugeLayout.OUTPUT_A_TANK_X,
                top + CentrifugeLayout.OUTPUT_A_TANK_Y,
                this.menu.getScaledOutputATank(CentrifugeLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputAFluid())
        );

        drawTankFill(
                graphics,
                left + CentrifugeLayout.OUTPUT_B_TANK_X,
                top + CentrifugeLayout.OUTPUT_B_TANK_Y,
                this.menu.getScaledOutputBTank(CentrifugeLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputBFluid())
        );

        drawSlotFrame(graphics, CentrifugeLayout.INPUT_SLOT_X, CentrifugeLayout.INPUT_SLOT_Y);
        drawSlotFrame(graphics, CentrifugeLayout.OUTPUT_A_SLOT_X, CentrifugeLayout.OUTPUT_A_SLOT_Y);
        drawSlotFrame(graphics, CentrifugeLayout.OUTPUT_B_SLOT_X, CentrifugeLayout.OUTPUT_B_SLOT_Y);
        drawSlotFrame(graphics, CentrifugeLayout.FUEL_SLOT_X, CentrifugeLayout.FUEL_SLOT_Y);

        drawProgressBar(
                graphics,
                left + CentrifugeLayout.PROGRESS_X,
                top + CentrifugeLayout.PROGRESS_Y,
                this.menu.getScaledProgress(CentrifugeLayout.PROGRESS_W)
        );

        drawFuelBar(
                graphics,
                left + CentrifugeLayout.FUEL_BAR_X,
                top + CentrifugeLayout.FUEL_BAR_Y,
                this.menu.getScaledBurnTime(CentrifugeLayout.FUEL_BAR_INNER_H),
                this.menu.isLit()
        );

        drawDumpButton(
                graphics,
                CentrifugeLayout.DUMP_INPUT_X,
                CentrifugeLayout.DUMP_BUTTON_Y,
                this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                this.menu.getInputTankAmount() > 0
        );

        drawDumpButton(
                graphics,
                CentrifugeLayout.DUMP_OUTPUT_A_X,
                CentrifugeLayout.DUMP_BUTTON_Y,
                this.dumpOutputAButton != null && this.dumpOutputAButton.isHoveredOrFocused(),
                this.menu.getOutputATankAmount() > 0
        );

        drawDumpButton(
                graphics,
                CentrifugeLayout.DUMP_OUTPUT_B_X,
                CentrifugeLayout.DUMP_BUTTON_Y,
                this.dumpOutputBButton != null && this.dumpOutputBButton.isHoveredOrFocused(),
                this.menu.getOutputBTankAmount() > 0
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // optional title
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

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

    private void drawTankFrame(GuiGraphics graphics, int tankX, int tankY) {
        int x = this.leftPos + tankX;
        int y = this.topPos + tankY;

        graphics.fill(x - 1, y - 1, x + CentrifugeLayout.TANK_W + 1, y + CentrifugeLayout.TANK_H + 1, 0xFF7F8590);
        graphics.fill(x, y, x + CentrifugeLayout.TANK_W, y + CentrifugeLayout.TANK_H, 0xFF15171B);

        graphics.fill(
                x + CentrifugeLayout.TANK_INNER_X_OFFSET,
                y + CentrifugeLayout.TANK_INNER_Y_OFFSET,
                x + CentrifugeLayout.TANK_INNER_X_OFFSET + CentrifugeLayout.TANK_INNER_W,
                y + CentrifugeLayout.TANK_INNER_Y_OFFSET + CentrifugeLayout.TANK_INNER_H,
                0xFF090A0D
        );
    }

    private void drawTankFill(GuiGraphics graphics, int tankX, int tankY, int filledPixels, int color) {
        if (filledPixels <= 0 || (color >>> 24) == 0) {
            return;
        }

        int x1 = tankX + CentrifugeLayout.TANK_INNER_X_OFFSET;
        int y1 = tankY + CentrifugeLayout.TANK_INNER_Y_OFFSET;
        int x2 = x1 + CentrifugeLayout.TANK_INNER_W;
        int y2 = y1 + CentrifugeLayout.TANK_INNER_H;
        int fillTop = y2 - filledPixels;

        graphics.fill(x1, fillTop, x2, y2, color);

        int darker = darken(color, 0.72f);
        int lighter = lighten(color, 1.15f);

        graphics.fill(x1 + 1, fillTop, x1 + 2, y2, lighter);
        graphics.fill(x1 + 3, fillTop, x1 + 5, y2, darker);
    }

    private void drawProgressBar(GuiGraphics graphics, int x, int y, int progress) {
        graphics.fill(x - 1, y - 1, x + CentrifugeLayout.PROGRESS_W + 1, y + CentrifugeLayout.PROGRESS_H + 1, 0xFF767C88);
        graphics.fill(x, y, x + CentrifugeLayout.PROGRESS_W, y + CentrifugeLayout.PROGRESS_H, 0xFF101216);

        if (progress > 0) {
            int clamped = Math.min(CentrifugeLayout.PROGRESS_W, progress);
            graphics.fill(x, y, x + clamped, y + CentrifugeLayout.PROGRESS_H, 0xFF768AB8);
            graphics.fill(x, y, x + clamped, y + 2, 0xFFAAB9DB);
        }
    }

    private void drawFuelBar(GuiGraphics graphics, int x, int y, int filledPixels, boolean lit) {
        graphics.fill(x - 1, y - 1, x + CentrifugeLayout.FUEL_BAR_W + 1, y + CentrifugeLayout.FUEL_BAR_H + 1, 0xFF767C88);
        graphics.fill(x, y, x + CentrifugeLayout.FUEL_BAR_W, y + CentrifugeLayout.FUEL_BAR_H, 0xFF101216);

        int x1 = x + CentrifugeLayout.FUEL_BAR_INNER_X_OFFSET;
        int y1 = y + CentrifugeLayout.FUEL_BAR_INNER_Y_OFFSET;
        int x2 = x1 + CentrifugeLayout.FUEL_BAR_INNER_W;
        int y2 = y1 + CentrifugeLayout.FUEL_BAR_INNER_H;

        graphics.fill(x1, y1, x2, y2, 0xFF090A0D);

        if (filledPixels > 0) {
            int clamped = Math.min(CentrifugeLayout.FUEL_BAR_INNER_H, filledPixels);
            int fillTop = y2 - clamped;

            graphics.fill(x1, fillTop, x2, y2, lit ? 0xFFE38D3F : 0xFF8E6A4A);
            graphics.fill(x1, fillTop, x2, fillTop + 2, 0xFFFFC270);
        }
    }

    private void drawDumpButton(GuiGraphics graphics, int localX, int localY, boolean hovered, boolean enabled) {
        int x = this.leftPos + localX;
        int y = this.topPos + localY;

        graphics.fill(x, y, x + CentrifugeLayout.DUMP_BUTTON_SIZE, y + CentrifugeLayout.DUMP_BUTTON_SIZE, 0xFF7C818C);
        graphics.fill(x + 1, y + 1, x + CentrifugeLayout.DUMP_BUTTON_SIZE - 1, y + CentrifugeLayout.DUMP_BUTTON_SIZE - 1, 0xFF181A1F);

        if (!enabled) {
            graphics.fill(x + 1, y + 1, x + CentrifugeLayout.DUMP_BUTTON_SIZE - 1, y + CentrifugeLayout.DUMP_BUTTON_SIZE - 1, 0x66000000);
        } else if (hovered) {
            graphics.fill(x + 1, y + 1, x + CentrifugeLayout.DUMP_BUTTON_SIZE - 1, y + CentrifugeLayout.DUMP_BUTTON_SIZE - 1, 0x22FFFFFF);
        }

        int cross = enabled ? 0xFFD65B5B : 0xFF6E4545;
        graphics.fill(x + 3, y + 3, x + 4, y + 9, cross);
        graphics.fill(x + 8, y + 3, x + 9, y + 9, cross);
        graphics.fill(x + 4, y + 4, x + 8, y + 5, cross);
        graphics.fill(x + 4, y + 7, x + 8, y + 8, cross);
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