package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.mydrugs.mydrugs.menu.FluidFiltererMenu;
import org.mydrugs.mydrugs.menu.layout.FluidFiltererLayout;

import java.util.ArrayList;
import java.util.List;

public class FluidFiltererScreen extends AbstractContainerScreen<FluidFiltererMenu> {
    private boolean holdingRunButton = false;
    private InvisibleButton dumpInputButton;
    private InvisibleButton dumpOutputAButton;
    private InvisibleButton dumpOutputBButton;

    public FluidFiltererScreen(FluidFiltererMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = FluidFiltererLayout.GUI_WIDTH;
        this.imageHeight = FluidFiltererLayout.GUI_HEIGHT;
        this.titleLabelX = 14;
        this.titleLabelY = FluidFiltererLayout.TITLE_Y;
        this.inventoryLabelX = FluidFiltererLayout.PLAYER_INV_X;
        this.inventoryLabelY = FluidFiltererLayout.INVENTORY_PANEL_Y - 10;
    }

    @Override
    protected void init() {
        super.init();

        this.dumpInputButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + FluidFiltererLayout.DUMP_INPUT_X,
                this.topPos + FluidFiltererLayout.DUMP_BUTTON_Y,
                FluidFiltererLayout.DUMP_BUTTON_SIZE,
                FluidFiltererLayout.DUMP_BUTTON_SIZE,
                button -> this.onMenuButtonPressed(FluidFiltererMenu.DUMP_INPUT_BUTTON_ID)
        ));

        this.dumpOutputAButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + FluidFiltererLayout.DUMP_OUTPUT_A_X,
                this.topPos + FluidFiltererLayout.DUMP_BUTTON_Y,
                FluidFiltererLayout.DUMP_BUTTON_SIZE,
                FluidFiltererLayout.DUMP_BUTTON_SIZE,
                button -> this.onMenuButtonPressed(FluidFiltererMenu.DUMP_OUTPUT_A_BUTTON_ID)
        ));

        this.dumpOutputBButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + FluidFiltererLayout.DUMP_OUTPUT_B_X,
                this.topPos + FluidFiltererLayout.DUMP_BUTTON_Y,
                FluidFiltererLayout.DUMP_BUTTON_SIZE,
                FluidFiltererLayout.DUMP_BUTTON_SIZE,
                button -> this.onMenuButtonPressed(FluidFiltererMenu.DUMP_OUTPUT_B_BUTTON_ID)
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
                left + FluidFiltererLayout.MACHINE_PANEL_X,
                top + FluidFiltererLayout.MACHINE_PANEL_Y,
                FluidFiltererLayout.MACHINE_PANEL_W,
                FluidFiltererLayout.MACHINE_PANEL_H,
                0xFF2E3138
        );

        fillPanel(
                graphics,
                left + FluidFiltererLayout.INVENTORY_PANEL_X,
                top + FluidFiltererLayout.INVENTORY_PANEL_Y,
                FluidFiltererLayout.INVENTORY_PANEL_W,
                FluidFiltererLayout.INVENTORY_PANEL_H,
                0xFF2A2D33
        );

        drawTankFrame(graphics, FluidFiltererLayout.INPUT_TANK_X, FluidFiltererLayout.INPUT_TANK_Y);
        drawTankFrame(graphics, FluidFiltererLayout.OUTPUT_A_TANK_X, FluidFiltererLayout.OUTPUT_A_TANK_Y);
        drawTankFrame(graphics, FluidFiltererLayout.OUTPUT_B_TANK_X, FluidFiltererLayout.OUTPUT_B_TANK_Y);

        drawTankFill(
                graphics,
                left + FluidFiltererLayout.INPUT_TANK_X,
                top + FluidFiltererLayout.INPUT_TANK_Y,
                this.menu.getScaledInputTank(FluidFiltererLayout.TANK_INNER_H),
                getFluidColor(this.menu.getInputFluid())
        );

        drawTankFill(
                graphics,
                left + FluidFiltererLayout.OUTPUT_A_TANK_X,
                top + FluidFiltererLayout.OUTPUT_A_TANK_Y,
                this.menu.getScaledOutputATank(FluidFiltererLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputAFluid())
        );

        drawTankFill(
                graphics,
                left + FluidFiltererLayout.OUTPUT_B_TANK_X,
                top + FluidFiltererLayout.OUTPUT_B_TANK_Y,
                this.menu.getScaledOutputBTank(FluidFiltererLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputBFluid())
        );

        drawSlotFrame(graphics, FluidFiltererLayout.INPUT_SLOT_X, FluidFiltererLayout.INPUT_SLOT_Y);
        drawSlotFrame(graphics, FluidFiltererLayout.OUTPUT_A_SLOT_X, FluidFiltererLayout.OUTPUT_A_SLOT_Y);
        drawSlotFrame(graphics, FluidFiltererLayout.OUTPUT_B_SLOT_X, FluidFiltererLayout.OUTPUT_B_SLOT_Y);
        drawSlotFrame(graphics, FluidFiltererLayout.FILTER_SLOT_X, FluidFiltererLayout.FILTER_SLOT_Y);
        drawSlotFrame(graphics, FluidFiltererLayout.RESIDUE_SLOT_X, FluidFiltererLayout.RESIDUE_SLOT_Y);

        drawProgressBar(
                graphics,
                left + FluidFiltererLayout.PROGRESS_X,
                top + FluidFiltererLayout.PROGRESS_Y,
                this.menu.getScaledProgress(FluidFiltererLayout.PROGRESS_W)
        );

        drawDumpButton(
                graphics,
                FluidFiltererLayout.DUMP_INPUT_X,
                FluidFiltererLayout.DUMP_BUTTON_Y,
                this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                this.menu.getInputTankAmount() > 0
        );

        drawDumpButton(
                graphics,
                FluidFiltererLayout.DUMP_OUTPUT_A_X,
                FluidFiltererLayout.DUMP_BUTTON_Y,
                this.dumpOutputAButton != null && this.dumpOutputAButton.isHoveredOrFocused(),
                this.menu.getOutputATankAmount() > 0
        );

        drawDumpButton(
                graphics,
                FluidFiltererLayout.DUMP_OUTPUT_B_X,
                FluidFiltererLayout.DUMP_BUTTON_Y,
                this.dumpOutputBButton != null && this.dumpOutputBButton.isHoveredOrFocused(),
                this.menu.getOutputBTankAmount() > 0
        );

        renderRunButton(graphics, mouseX, mouseY);

        //graphics.drawCenteredString(this.font, Component.literal("FILTER"), left + FluidFiltererLayout.FILTER_SLOT_X + 8, top + 70, 0xFFD8D8D8);
        //graphics.drawCenteredString(this.font, Component.literal("WASTE"), left + FluidFiltererLayout.RESIDUE_SLOT_X + 8, top + 70, 0xFFD8D8D8);

        if (this.menu.getMaxProgress() > 0) {
            graphics.drawCenteredString(
                    this.font,
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress()),
                    left + this.imageWidth / 2,
                    top + 68,
                    0xFFE6E6E6
            );
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        //graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFC8C8C8, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHoveringBox(FluidFiltererLayout.DUMP_INPUT_X, FluidFiltererLayout.DUMP_BUTTON_Y, FluidFiltererLayout.DUMP_BUTTON_SIZE, FluidFiltererLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump input tank"));
        } else if (isHoveringBox(FluidFiltererLayout.DUMP_OUTPUT_A_X, FluidFiltererLayout.DUMP_BUTTON_Y, FluidFiltererLayout.DUMP_BUTTON_SIZE, FluidFiltererLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output tank A"));
        } else if (isHoveringBox(FluidFiltererLayout.DUMP_OUTPUT_B_X, FluidFiltererLayout.DUMP_BUTTON_Y, FluidFiltererLayout.DUMP_BUTTON_SIZE, FluidFiltererLayout.DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Dump output tank B"));
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
                    Component.literal("Output tank A"),
                    Component.literal(getFluidName(this.menu.getOutputAFluid())),
                    Component.literal(this.menu.getOutputATankAmount() + " / " + FluidFiltererMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(FluidFiltererLayout.OUTPUT_B_TANK_X, FluidFiltererLayout.OUTPUT_B_TANK_Y, FluidFiltererLayout.TANK_W, FluidFiltererLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output tank B"),
                    Component.literal(getFluidName(this.menu.getOutputBFluid())),
                    Component.literal(this.menu.getOutputBTankAmount() + " / " + FluidFiltererMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(FluidFiltererLayout.INPUT_SLOT_X, FluidFiltererLayout.INPUT_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Input fluid container"));
        } else if (isHoveringBox(FluidFiltererLayout.OUTPUT_A_SLOT_X, FluidFiltererLayout.OUTPUT_A_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Output container A"));
        } else if (isHoveringBox(FluidFiltererLayout.OUTPUT_B_SLOT_X, FluidFiltererLayout.OUTPUT_B_SLOT_Y, 18, 18, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY, Component.literal("Output container B"));
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
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, FluidFiltererMenu.RUN_BUTTON_START_ID);
                this.holdingRunButton = true;
                return true;
            }
        }
        return super.mouseClicked(event, doubleClicked);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && this.holdingRunButton) {
            if (this.minecraft != null && this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, FluidFiltererMenu.RUN_BUTTON_STOP_ID);
            }
            this.holdingRunButton = false;
            return true;
        }

        return super.mouseReleased(event);
    }

    @Override
    public void removed() {
        if (this.holdingRunButton && this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, FluidFiltererMenu.RUN_BUTTON_STOP_ID);
        }
        this.holdingRunButton = false;
        super.removed();
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

        graphics.fill(x - 1, y - 1, x + FluidFiltererLayout.TANK_W + 1, y + FluidFiltererLayout.TANK_H + 1, 0xFF7F8590);
        graphics.fill(x, y, x + FluidFiltererLayout.TANK_W, y + FluidFiltererLayout.TANK_H, 0xFF15171B);

        graphics.fill(
                x + FluidFiltererLayout.TANK_INNER_X_OFFSET,
                y + FluidFiltererLayout.TANK_INNER_Y_OFFSET,
                x + FluidFiltererLayout.TANK_INNER_X_OFFSET + FluidFiltererLayout.TANK_INNER_W,
                y + FluidFiltererLayout.TANK_INNER_Y_OFFSET + FluidFiltererLayout.TANK_INNER_H,
                0xFF090A0D
        );
    }

    private void drawTankFill(GuiGraphics graphics, int tankX, int tankY, int filledPixels, int color) {
        if (filledPixels <= 0 || (color >>> 24) == 0) {
            return;
        }

        int x1 = tankX + FluidFiltererLayout.TANK_INNER_X_OFFSET;
        int y1 = tankY + FluidFiltererLayout.TANK_INNER_Y_OFFSET;
        int x2 = x1 + FluidFiltererLayout.TANK_INNER_W;
        int y2 = y1 + FluidFiltererLayout.TANK_INNER_H;
        int fillTop = y2 - filledPixels;

        graphics.fill(x1, fillTop, x2, y2, color);

        int darker = darken(color, 0.72f);
        int lighter = lighten(color, 1.15f);

        graphics.fill(x1 + 1, fillTop, x1 + 2, y2, lighter);
        graphics.fill(x1 + 3, fillTop, x1 + 5, y2, darker);
    }

    private void drawProgressBar(GuiGraphics graphics, int x, int y, int progress) {
        graphics.fill(x - 1, y - 1, x + FluidFiltererLayout.PROGRESS_W + 1, y + FluidFiltererLayout.PROGRESS_H + 1, 0xFF767C88);
        graphics.fill(x, y, x + FluidFiltererLayout.PROGRESS_W, y + FluidFiltererLayout.PROGRESS_H, 0xFF101216);

        if (progress > 0) {
            int clamped = Math.min(FluidFiltererLayout.PROGRESS_W, progress);
            graphics.fill(x, y, x + clamped, y + FluidFiltererLayout.PROGRESS_H, 0xFF768AB8);
            graphics.fill(x, y, x + clamped, y + 2, 0xFFAAB9DB);
        }
    }

    private void drawDumpButton(GuiGraphics graphics, int localX, int localY, boolean hovered, boolean enabled) {
        int x = this.leftPos + localX;
        int y = this.topPos + localY;

        graphics.fill(x, y, x + FluidFiltererLayout.DUMP_BUTTON_SIZE, y + FluidFiltererLayout.DUMP_BUTTON_SIZE, 0xFF7C818C);
        graphics.fill(x + 1, y + 1, x + FluidFiltererLayout.DUMP_BUTTON_SIZE - 1, y + FluidFiltererLayout.DUMP_BUTTON_SIZE - 1, 0xFF181A1F);

        if (!enabled) {
            graphics.fill(x + 1, y + 1, x + FluidFiltererLayout.DUMP_BUTTON_SIZE - 1, y + FluidFiltererLayout.DUMP_BUTTON_SIZE - 1, 0x66000000);
        } else if (hovered) {
            graphics.fill(x + 1, y + 1, x + FluidFiltererLayout.DUMP_BUTTON_SIZE - 1, y + FluidFiltererLayout.DUMP_BUTTON_SIZE - 1, 0x22FFFFFF);
        }

        int cross = enabled ? 0xFFD65B5B : 0xFF6E4545;
        graphics.fill(x + 3, y + 3, x + 4, y + 9, cross);
        graphics.fill(x + 8, y + 3, x + 9, y + 9, cross);
        graphics.fill(x + 4, y + 4, x + 8, y + 5, cross);
        graphics.fill(x + 4, y + 7, x + 8, y + 8, cross);
    }

    private void renderRunButton(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = this.leftPos + FluidFiltererLayout.RUN_BUTTON_X;
        int y = this.topPos + FluidFiltererLayout.RUN_BUTTON_Y;

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

    private boolean isHoveringBox(int localX, int localY, int width, int height, double mouseX, double mouseY) {
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