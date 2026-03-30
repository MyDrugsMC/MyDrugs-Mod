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
import org.mydrugs.mydrugs.menu.DistillerLayout;
import org.mydrugs.mydrugs.menu.DistillerMenu;

import java.util.ArrayList;
import java.util.List;

public class DistillerScreen extends AbstractContainerScreen<DistillerMenu> {
    private InvisibleButton runButton;
    private InvisibleButton dumpInputButton;
    private InvisibleButton dumpOutputAButton;
    private InvisibleButton dumpOutputBButton;

    public DistillerScreen(DistillerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = DistillerLayout.GUI_WIDTH;
        this.imageHeight = DistillerLayout.GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();

        this.runButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + DistillerLayout.RUN_BUTTON_X,
                this.topPos + DistillerLayout.RUN_BUTTON_Y,
                DistillerLayout.RUN_BUTTON_SIZE,
                DistillerLayout.RUN_BUTTON_SIZE,
                button -> this.onMenuButtonPressed(DistillerMenu.RUN_BUTTON_ID)
        ));

        this.dumpInputButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + DistillerLayout.DUMP_INPUT_X,
                this.topPos + DistillerLayout.DUMP_BUTTON_Y,
                DistillerLayout.DUMP_BUTTON_SIZE,
                DistillerLayout.DUMP_BUTTON_SIZE,
                button -> this.onMenuButtonPressed(DistillerMenu.DUMP_INPUT_BUTTON_ID)
        ));

        this.dumpOutputAButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + DistillerLayout.DUMP_OUTPUT_A_X,
                this.topPos + DistillerLayout.DUMP_BUTTON_Y,
                DistillerLayout.DUMP_BUTTON_SIZE,
                DistillerLayout.DUMP_BUTTON_SIZE,
                button -> this.onMenuButtonPressed(DistillerMenu.DUMP_OUTPUT_A_BUTTON_ID)
        ));

        this.dumpOutputBButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + DistillerLayout.DUMP_OUTPUT_B_X,
                this.topPos + DistillerLayout.DUMP_BUTTON_Y,
                DistillerLayout.DUMP_BUTTON_SIZE,
                DistillerLayout.DUMP_BUTTON_SIZE,
                button -> this.onMenuButtonPressed(DistillerMenu.DUMP_OUTPUT_B_BUTTON_ID)
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
                left + DistillerLayout.MACHINE_PANEL_X,
                top + DistillerLayout.MACHINE_PANEL_Y,
                DistillerLayout.MACHINE_PANEL_W,
                DistillerLayout.MACHINE_PANEL_H,
                0xFF2E3138
        );

        fillPanel(
                graphics,
                left + DistillerLayout.INVENTORY_PANEL_X,
                top + DistillerLayout.INVENTORY_PANEL_Y,
                DistillerLayout.INVENTORY_PANEL_W,
                DistillerLayout.INVENTORY_PANEL_H,
                0xFF2A2D33
        );

        drawTankFrame(graphics, DistillerLayout.INPUT_TANK_X, DistillerLayout.INPUT_TANK_Y);
        drawTankFrame(graphics, DistillerLayout.OUTPUT_A_TANK_X, DistillerLayout.OUTPUT_A_TANK_Y);
        drawTankFrame(graphics, DistillerLayout.OUTPUT_B_TANK_X, DistillerLayout.OUTPUT_B_TANK_Y);

        drawTankFill(
                graphics,
                left + DistillerLayout.INPUT_TANK_X,
                top + DistillerLayout.INPUT_TANK_Y,
                this.menu.getScaledInputTank(DistillerLayout.TANK_INNER_H),
                getFluidColor(this.menu.getInputFluid())
        );

        drawTankFill(
                graphics,
                left + DistillerLayout.OUTPUT_A_TANK_X,
                top + DistillerLayout.OUTPUT_A_TANK_Y,
                this.menu.getScaledOutputATank(DistillerLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputAFluid())
        );

        drawTankFill(
                graphics,
                left + DistillerLayout.OUTPUT_B_TANK_X,
                top + DistillerLayout.OUTPUT_B_TANK_Y,
                this.menu.getScaledOutputBTank(DistillerLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputBFluid())
        );

        drawSlotFrame(graphics, DistillerLayout.INPUT_SLOT_X, DistillerLayout.INPUT_SLOT_Y);
        drawSlotFrame(graphics, DistillerLayout.OUTPUT_A_SLOT_X, DistillerLayout.OUTPUT_A_SLOT_Y);
        drawSlotFrame(graphics, DistillerLayout.OUTPUT_B_SLOT_X, DistillerLayout.OUTPUT_B_SLOT_Y);

        drawProgressBar(
                graphics,
                left + DistillerLayout.PROGRESS_X,
                top + DistillerLayout.PROGRESS_Y,
                this.menu.getScaledProgress(DistillerLayout.PROGRESS_W)
        );

        drawDumpButton(
                graphics,
                DistillerLayout.DUMP_INPUT_X,
                DistillerLayout.DUMP_BUTTON_Y,
                this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                this.menu.getInputTankAmount() > 0
        );

        drawDumpButton(
                graphics,
                DistillerLayout.DUMP_OUTPUT_A_X,
                DistillerLayout.DUMP_BUTTON_Y,
                this.dumpOutputAButton != null && this.dumpOutputAButton.isHoveredOrFocused(),
                this.menu.getOutputATankAmount() > 0
        );

        drawDumpButton(
                graphics,
                DistillerLayout.DUMP_OUTPUT_B_X,
                DistillerLayout.DUMP_BUTTON_Y,
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
                left + this.imageWidth / 2,
                top + DistillerLayout.CPS_TEXT_Y,
                0xFFD8D8D8
        );

        graphics.drawCenteredString(
                this.font,
                Component.literal(this.menu.getSpeedPercent() + "% speed"),
                left + this.imageWidth / 2,
                top + DistillerLayout.SPEED_TEXT_Y,
                0xFFBEBEBE
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
//        graphics.drawCenteredString(
//                this.font,
//                this.title,
//                this.imageWidth / 2,
//                DistillerLayout.TITLE_Y,
//                0xFFEAEAEA
//        );
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

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

        graphics.fill(x - 1, y - 1, x + DistillerLayout.TANK_W + 1, y + DistillerLayout.TANK_H + 1, 0xFF7F8590);
        graphics.fill(x, y, x + DistillerLayout.TANK_W, y + DistillerLayout.TANK_H, 0xFF15171B);

        graphics.fill(
                x + DistillerLayout.TANK_INNER_X_OFFSET,
                y + DistillerLayout.TANK_INNER_Y_OFFSET,
                x + DistillerLayout.TANK_INNER_X_OFFSET + DistillerLayout.TANK_INNER_W,
                y + DistillerLayout.TANK_INNER_Y_OFFSET + DistillerLayout.TANK_INNER_H,
                0xFF090A0D
        );
    }

    private void drawTankFill(GuiGraphics graphics, int tankX, int tankY, int filledPixels, int color) {
        if (filledPixels <= 0 || (color >>> 24) == 0) {
            return;
        }

        int x1 = tankX + DistillerLayout.TANK_INNER_X_OFFSET;
        int y1 = tankY + DistillerLayout.TANK_INNER_Y_OFFSET;
        int x2 = x1 + DistillerLayout.TANK_INNER_W;
        int y2 = y1 + DistillerLayout.TANK_INNER_H;
        int fillTop = y2 - filledPixels;

        graphics.fill(x1, fillTop, x2, y2, color);

        int darker = darken(color, 0.72f);
        int lighter = lighten(color, 1.15f);

        graphics.fill(x1 + 1, fillTop, x1 + 2, y2, lighter);
        graphics.fill(x1 + 3, fillTop, x1 + 5, y2, darker);
    }

    private void drawProgressBar(GuiGraphics graphics, int x, int y, int progress) {
        graphics.fill(x - 1, y - 1, x + DistillerLayout.PROGRESS_W + 1, y + DistillerLayout.PROGRESS_H + 1, 0xFF767C88);
        graphics.fill(x, y, x + DistillerLayout.PROGRESS_W, y + DistillerLayout.PROGRESS_H, 0xFF101216);

        if (progress > 0) {
            int clamped = Math.min(DistillerLayout.PROGRESS_W, progress);
            graphics.fill(x, y, x + clamped, y + DistillerLayout.PROGRESS_H, 0xFF768AB8);
            graphics.fill(x, y, x + clamped, y + 2, 0xFFAAB9DB);
        }
    }

    private void drawDumpButton(GuiGraphics graphics, int localX, int localY, boolean hovered, boolean enabled) {
        int x = this.leftPos + localX;
        int y = this.topPos + localY;

        graphics.fill(x, y, x + DistillerLayout.DUMP_BUTTON_SIZE, y + DistillerLayout.DUMP_BUTTON_SIZE, 0xFF7C818C);
        graphics.fill(x + 1, y + 1, x + DistillerLayout.DUMP_BUTTON_SIZE - 1, y + DistillerLayout.DUMP_BUTTON_SIZE - 1, 0xFF181A1F);

        if (!enabled) {
            graphics.fill(x + 1, y + 1, x + DistillerLayout.DUMP_BUTTON_SIZE - 1, y + DistillerLayout.DUMP_BUTTON_SIZE - 1, 0x66000000);
        } else if (hovered) {
            graphics.fill(x + 1, y + 1, x + DistillerLayout.DUMP_BUTTON_SIZE - 1, y + DistillerLayout.DUMP_BUTTON_SIZE - 1, 0x22FFFFFF);
        }

        int cross = enabled ? 0xFFD65B5B : 0xFF6E4545;
        graphics.fill(x + 3, y + 3, x + 4, y + 9, cross);
        graphics.fill(x + 8, y + 3, x + 9, y + 9, cross);
        graphics.fill(x + 4, y + 4, x + 8, y + 5, cross);
        graphics.fill(x + 4, y + 7, x + 8, y + 8, cross);
    }

    private void drawReactor(GuiGraphics graphics, int localX, int localY, boolean hovered, boolean working, boolean boosted) {
        int x = this.leftPos + localX;
        int y = this.topPos + localY;
        int cx = x + DistillerLayout.RUN_BUTTON_SIZE / 2;
        int cy = y + DistillerLayout.RUN_BUTTON_SIZE / 2;

        if (hovered) {
            graphics.fill(x + 2, y + 2, x + DistillerLayout.RUN_BUTTON_SIZE - 2, y + DistillerLayout.RUN_BUTTON_SIZE - 2, 0x16FFFFFF);
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

        //drawChevron(graphics, cx - 9, cy - 17, 7, 0xFF8D96A6, true);
        //drawChevron(graphics, cx + 2, cy - 17, 7, 0xFF8D96A6, true);
    }

    private void drawChevron(GuiGraphics graphics, int x, int y, int size, int color, boolean up) {
        for (int i = 0; i < size; i++) {
            int yy = up ? y + i : y + (size - 1 - i);
            graphics.fill(x + i, yy, x + i + 1, yy + 1, color);
            graphics.fill(x + (size * 2 - 2 - i), yy, x + (size * 2 - 1 - i), yy + 1, color);
        }
    }

    private void drawCircle(GuiGraphics graphics, int cx, int cy, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    graphics.fill(cx + x, cy + y, cx + x + 1, cy + y + 1, color);
                }
            }
        }
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