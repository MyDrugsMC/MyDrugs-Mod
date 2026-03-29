package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.menu.DistillerMenu;

import java.util.ArrayList;
import java.util.List;

public class DistillerScreen extends AbstractContainerScreen<DistillerMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/gui/container/distiller.png");

    private static final int TEXTURE_W = 196;
    private static final int TEXTURE_H = 194;

    private static final int INPUT_TANK_X = 22;
    private static final int INPUT_TANK_Y = 22;
    private static final int OUTPUT_A_TANK_X = 142;
    private static final int OUTPUT_A_TANK_Y = 22;
    private static final int OUTPUT_B_TANK_X = 166;
    private static final int OUTPUT_B_TANK_Y = 22;

    private static final int TANK_W = 17;
    private static final int TANK_H = 58;

    private static final int TANK_INNER_X_OFFSET = 2;
    private static final int TANK_INNER_Y_OFFSET = 2;
    private static final int TANK_INNER_W = 13;
    private static final int TANK_INNER_H = 54;

    private static final int PROGRESS_X = 66;
    private static final int PROGRESS_Y = 28;
    private static final int PROGRESS_W = 56;
    private static final int PROGRESS_H = 10;

    private static final int RUN_BUTTON_X = 72;
    private static final int RUN_BUTTON_Y = 36;
    private static final int RUN_BUTTON_SIZE = 44;

    private static final int DUMP_BUTTON_SIZE = 12;
    private static final int DUMP_INPUT_X = 24;
    private static final int DUMP_OUTPUT_A_X = 144;
    private static final int DUMP_OUTPUT_B_X = 168;
    private static final int DUMP_BUTTON_Y = 8;

    private InvisibleButton runButton;
    private InvisibleButton dumpInputButton;
    private InvisibleButton dumpOutputAButton;
    private InvisibleButton dumpOutputBButton;

    public DistillerScreen(DistillerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = TEXTURE_W;
        this.imageHeight = TEXTURE_H;
        this.inventoryLabelX = 16;
        this.inventoryLabelY = 96;
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
    protected void init() {
        super.init();

        this.runButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + RUN_BUTTON_X,
                this.topPos + RUN_BUTTON_Y,
                RUN_BUTTON_SIZE,
                RUN_BUTTON_SIZE,
                button -> onMenuButtonPressed(DistillerMenu.RUN_BUTTON_ID)
        ));

        this.dumpInputButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + DUMP_INPUT_X,
                this.topPos + DUMP_BUTTON_Y,
                DUMP_BUTTON_SIZE,
                DUMP_BUTTON_SIZE,
                button -> onMenuButtonPressed(DistillerMenu.DUMP_INPUT_BUTTON_ID)
        ));

        this.dumpOutputAButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + DUMP_OUTPUT_A_X,
                this.topPos + DUMP_BUTTON_Y,
                DUMP_BUTTON_SIZE,
                DUMP_BUTTON_SIZE,
                button -> onMenuButtonPressed(DistillerMenu.DUMP_OUTPUT_A_BUTTON_ID)
        ));

        this.dumpOutputBButton = this.addRenderableWidget(new InvisibleButton(
                this.leftPos + DUMP_OUTPUT_B_X,
                this.topPos + DUMP_BUTTON_Y,
                DUMP_BUTTON_SIZE,
                DUMP_BUTTON_SIZE,
                button -> onMenuButtonPressed(DistillerMenu.DUMP_OUTPUT_B_BUTTON_ID)
        ));
    }

    private void onMenuButtonPressed(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, TEXTURE_W, TEXTURE_H);

        drawTankFill(
                graphics,
                x + INPUT_TANK_X,
                y + INPUT_TANK_Y,
                this.menu.getScaledInputTank(TANK_INNER_H),
                getFluidColor(this.menu.getInputFluid())
        );

        drawTankFill(
                graphics,
                x + OUTPUT_A_TANK_X,
                y + OUTPUT_A_TANK_Y,
                this.menu.getScaledOutputATank(TANK_INNER_H),
                getFluidColor(this.menu.getOutputAFluid())
        );

        drawTankFill(
                graphics,
                x + OUTPUT_B_TANK_X,
                y + OUTPUT_B_TANK_Y,
                this.menu.getScaledOutputBTank(TANK_INNER_H),
                getFluidColor(this.menu.getOutputBFluid())
        );

        drawProgress(graphics, x + PROGRESS_X, y + PROGRESS_Y, this.menu.getScaledProgress(PROGRESS_W));

        drawRunButtonOverlay(
                graphics,
                x + RUN_BUTTON_X,
                y + RUN_BUTTON_Y,
                this.runButton != null && this.runButton.isHoveredOrFocused(),
                this.menu.getClicksPerSecond() > 5
        );

        drawDumpButtonOverlay(
                graphics,
                x + DUMP_INPUT_X,
                y + DUMP_BUTTON_Y,
                this.dumpInputButton != null && this.dumpInputButton.isHoveredOrFocused(),
                this.menu.getInputTankAmount() > 0
        );

        drawDumpButtonOverlay(
                graphics,
                x + DUMP_OUTPUT_A_X,
                y + DUMP_BUTTON_Y,
                this.dumpOutputAButton != null && this.dumpOutputAButton.isHoveredOrFocused(),
                this.menu.getOutputATankAmount() > 0
        );

        drawDumpButtonOverlay(
                graphics,
                x + DUMP_OUTPUT_B_X,
                y + DUMP_BUTTON_Y,
                this.dumpOutputBButton != null && this.dumpOutputBButton.isHoveredOrFocused(),
                this.menu.getOutputBTankAmount() > 0
        );

        graphics.drawCenteredString(
                this.font,
                Component.literal(this.menu.getClicksPerSecond() + " CPS"),
                x + 98,
                y + 78,
                0xFFD8D8D8
        );

        graphics.drawCenteredString(
                this.font,
                Component.literal(this.menu.getSpeedPercent() + "% speed"),
                x + 98,
                y + 88,
                0xFFBEBEBE
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, this.title, this.imageWidth / 2, 17, 0xFFEDEDED);
        //graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFD0D0D0, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHoveringBox(DUMP_INPUT_X, DUMP_BUTTON_Y, DUMP_BUTTON_SIZE, DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY,
                    Component.literal("Dump input tank"));
        } else if (isHoveringBox(DUMP_OUTPUT_A_X, DUMP_BUTTON_Y, DUMP_BUTTON_SIZE, DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY,
                    Component.literal("Dump output tank A"));
        } else if (isHoveringBox(DUMP_OUTPUT_B_X, DUMP_BUTTON_Y, DUMP_BUTTON_SIZE, DUMP_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(graphics, mouseX, mouseY,
                    Component.literal("Dump output tank B"));
        } else if (isHoveringBox(INPUT_TANK_X, INPUT_TANK_Y, TANK_W, TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Input tank"),
                    Component.literal(getFluidName(this.menu.getInputFluid())),
                    Component.literal(this.menu.getInputTankAmount() + " / " + DistillerMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(OUTPUT_A_TANK_X, OUTPUT_A_TANK_Y, TANK_W, TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output tank A"),
                    Component.literal(getFluidName(this.menu.getOutputAFluid())),
                    Component.literal(this.menu.getOutputATankAmount() + " / " + DistillerMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(OUTPUT_B_TANK_X, OUTPUT_B_TANK_Y, TANK_W, TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Output tank B"),
                    Component.literal(getFluidName(this.menu.getOutputBFluid())),
                    Component.literal(this.menu.getOutputBTankAmount() + " / " + DistillerMenu.TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(PROGRESS_X, PROGRESS_Y, PROGRESS_W, PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Distillation progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        } else if (isHoveringBox(RUN_BUTTON_X, RUN_BUTTON_Y, RUN_BUTTON_SIZE, RUN_BUTTON_SIZE, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Run distiller"),
                    Component.literal("More than 5 CPS increases speed")
            );
        }
    }

    private void drawTankFill(GuiGraphics graphics, int tankX, int tankY, int filledPixels, int color) {
        if (filledPixels <= 0 || (color >>> 24) == 0) {
            return;
        }

        int x1 = tankX + TANK_INNER_X_OFFSET;
        int y1 = tankY + TANK_INNER_Y_OFFSET;
        int x2 = x1 + TANK_INNER_W;
        int y2 = y1 + TANK_INNER_H;
        int fillTop = y2 - filledPixels;

        graphics.fill(x1, fillTop, x2, y2, color);

        int darker = darken(color, 0.72f);
        graphics.fill(x1 + 3, fillTop, x1 + 5, y2, darker);

        int lighter = lighten(color, 1.15f);
        graphics.fill(x1 + 1, fillTop, x1 + 2, y2, lighter);
    }

    private void drawProgress(GuiGraphics graphics, int x, int y, int progress) {
        if (progress <= 0) {
            return;
        }

        int clamped = Math.min(PROGRESS_W, progress);
        graphics.fill(x, y, x + clamped, y + PROGRESS_H, 0xFF7E8FB8);
        graphics.fill(x, y, x + clamped, y + 2, 0xFFAAB9DB);
    }

    private void drawRunButtonOverlay(GuiGraphics graphics, int x, int y, boolean hovered, boolean active) {
        if (active) {
            graphics.fill(x + 8, y + 8, x + 36, y + 36, 0x33FF6A5A);
        }

        if (hovered) {
            graphics.fill(x + 2, y + 2, x + RUN_BUTTON_SIZE - 2, y + RUN_BUTTON_SIZE - 2, 0x22FFFFFF);
        }
    }

    private void drawDumpButtonOverlay(GuiGraphics graphics, int x, int y, boolean hovered, boolean enabled) {
        if (!enabled) {
            graphics.fill(x + 1, y + 1, x + DUMP_BUTTON_SIZE - 1, y + DUMP_BUTTON_SIZE - 1, 0x66000000);
        } else if (hovered) {
            graphics.fill(x + 1, y + 1, x + DUMP_BUTTON_SIZE - 1, y + DUMP_BUTTON_SIZE - 1, 0x22FFFFFF);
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