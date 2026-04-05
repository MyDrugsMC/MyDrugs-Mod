package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.mydrugs.mydrugs.blocks.entity.AdvancedMixingVatBlockEntity;
import org.mydrugs.mydrugs.menu.AdvancedMixingVatMenu;
import org.mydrugs.mydrugs.menu.layout.AdvancedMixingVatLayout;

import java.util.ArrayList;
import java.util.List;

public class AdvancedMixingVatScreen extends AbstractContainerScreen<AdvancedMixingVatMenu> {
    public AdvancedMixingVatScreen(AdvancedMixingVatMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = AdvancedMixingVatLayout.GUI_WIDTH;
        this.imageHeight = AdvancedMixingVatLayout.GUI_HEIGHT;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF171717);
        graphics.fill(left + 4, top + 4, left + this.imageWidth - 4, top + this.imageHeight - 4, 0xFF252525);

        drawSlotFrame(graphics, AdvancedMixingVatLayout.ITEM_0_X, AdvancedMixingVatLayout.ITEM_0_Y);
        drawSlotFrame(graphics, AdvancedMixingVatLayout.ITEM_1_X, AdvancedMixingVatLayout.ITEM_1_Y);
        drawSlotFrame(graphics, AdvancedMixingVatLayout.ITEM_2_X, AdvancedMixingVatLayout.ITEM_2_Y);
        drawSlotFrame(graphics, AdvancedMixingVatLayout.ITEM_3_X, AdvancedMixingVatLayout.ITEM_3_Y);

        drawTankFrame(graphics, AdvancedMixingVatLayout.TANK_A_X, AdvancedMixingVatLayout.TANK_Y);
        drawTankFrame(graphics, AdvancedMixingVatLayout.TANK_B_X, AdvancedMixingVatLayout.TANK_Y);
        drawTankFrame(graphics, AdvancedMixingVatLayout.GAS_X, AdvancedMixingVatLayout.TANK_Y);
        drawTankFrame(graphics, AdvancedMixingVatLayout.OUTPUT_X, AdvancedMixingVatLayout.TANK_Y);

        drawTankFill(
                graphics,
                left + AdvancedMixingVatLayout.TANK_A_X,
                top + AdvancedMixingVatLayout.TANK_Y,
                this.menu.getScaledTank(this.menu.getInputATankAmount(), AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY, AdvancedMixingVatLayout.TANK_INNER_H),
                getFluidColor(this.menu.getInputAFluid())
        );

        drawTankFill(
                graphics,
                left + AdvancedMixingVatLayout.TANK_B_X,
                top + AdvancedMixingVatLayout.TANK_Y,
                this.menu.getScaledTank(this.menu.getInputBTankAmount(), AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY, AdvancedMixingVatLayout.TANK_INNER_H),
                getFluidColor(this.menu.getInputBFluid())
        );

        drawGasBar(
                graphics,
                left + AdvancedMixingVatLayout.GAS_X,
                top + AdvancedMixingVatLayout.TANK_Y,
                this.menu.getScaledTank(this.menu.getGasAmount(), (int) AdvancedMixingVatBlockEntity.GAS_TANK_CAPACITY, AdvancedMixingVatLayout.TANK_INNER_H)
        );

        drawTankFill(
                graphics,
                left + AdvancedMixingVatLayout.OUTPUT_X,
                top + AdvancedMixingVatLayout.TANK_Y,
                this.menu.getScaledTank(this.menu.getOutputTankAmount(), AdvancedMixingVatBlockEntity.OUTPUT_TANK_CAPACITY, AdvancedMixingVatLayout.TANK_INNER_H),
                getFluidColor(this.menu.getOutputFluid())
        );

        drawProgressBar(
                graphics,
                left + AdvancedMixingVatLayout.PROGRESS_X,
                top + AdvancedMixingVatLayout.PROGRESS_Y,
                this.menu.getScaledProgress(AdvancedMixingVatLayout.PROGRESS_W)
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xCFCFCF, false);
        graphics.drawString(this.font, Component.literal("No heat required"), 52, 90, 0x8AA0B5, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHoveringBox(AdvancedMixingVatLayout.TANK_A_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Fluid Input A"),
                    Component.literal(getFluidName(this.menu.getInputAFluid())),
                    Component.literal(this.menu.getInputATankAmount() + " / " + AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(AdvancedMixingVatLayout.TANK_B_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Fluid Input B"),
                    Component.literal(getFluidName(this.menu.getInputBFluid())),
                    Component.literal(this.menu.getInputBTankAmount() + " / " + AdvancedMixingVatBlockEntity.INPUT_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(AdvancedMixingVatLayout.GAS_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Gas Input"),
                    Component.literal(this.menu.getGasAmount() + " / " + AdvancedMixingVatBlockEntity.GAS_TANK_CAPACITY + " units")
            );
        } else if (isHoveringBox(AdvancedMixingVatLayout.OUTPUT_X, AdvancedMixingVatLayout.TANK_Y, AdvancedMixingVatLayout.TANK_W, AdvancedMixingVatLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Fluid Output"),
                    Component.literal(getFluidName(this.menu.getOutputFluid())),
                    Component.literal(this.menu.getOutputTankAmount() + " / " + AdvancedMixingVatBlockEntity.OUTPUT_TANK_CAPACITY + " mB")
            );
        } else if (isHoveringBox(AdvancedMixingVatLayout.PROGRESS_X, AdvancedMixingVatLayout.PROGRESS_Y, AdvancedMixingVatLayout.PROGRESS_W, AdvancedMixingVatLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Mixing Progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
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
        return fluid == Fluids.EMPTY ? "empty" : fluid.getFluidType().getDescription().getString();
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

    private void drawSlotFrame(GuiGraphics graphics, int slotX, int slotY) {
        int x = this.leftPos + slotX - 1;
        int y = this.topPos + slotY - 1;

        graphics.fill(x, y, x + 18, y + 18, 0xFF8A8F99);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF101216);
    }

    private void drawTankFrame(GuiGraphics graphics, int tankX, int tankY) {
        int x = this.leftPos + tankX;
        int y = this.topPos + tankY;

        graphics.fill(x - 1, y - 1, x + AdvancedMixingVatLayout.TANK_W + 1, y + AdvancedMixingVatLayout.TANK_H + 1, 0xFF7F8590);
        graphics.fill(x, y, x + AdvancedMixingVatLayout.TANK_W, y + AdvancedMixingVatLayout.TANK_H, 0xFF15171B);

        graphics.fill(
                x + AdvancedMixingVatLayout.TANK_INNER_X_OFFSET,
                y + AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET,
                x + AdvancedMixingVatLayout.TANK_INNER_X_OFFSET + AdvancedMixingVatLayout.TANK_INNER_W,
                y + AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET + AdvancedMixingVatLayout.TANK_INNER_H,
                0xFF090A0D
        );
    }

    private void drawTankFill(GuiGraphics graphics, int tankX, int tankY, int filledPixels, int color) {
        if (filledPixels <= 0 || (color >>> 24) == 0) {
            return;
        }

        int x1 = tankX + AdvancedMixingVatLayout.TANK_INNER_X_OFFSET;
        int y1 = tankY + AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET;
        int x2 = x1 + AdvancedMixingVatLayout.TANK_INNER_W;
        int y2 = y1 + AdvancedMixingVatLayout.TANK_INNER_H;
        int fillTop = y2 - filledPixels;

        graphics.fill(x1, fillTop, x2, y2, color);

        int darker = darken(color, 0.72f);
        int lighter = lighten(color, 1.15f);
        graphics.fill(x1 + 1, fillTop, x1 + 2, y2, lighter);
        graphics.fill(x1 + 3, fillTop, x1 + 5, y2, darker);
    }

    private void drawGasBar(GuiGraphics graphics, int tankX, int tankY, int filledPixels) {
        int x1 = tankX + AdvancedMixingVatLayout.TANK_INNER_X_OFFSET;
        int y1 = tankY + AdvancedMixingVatLayout.TANK_INNER_Y_OFFSET;
        int x2 = x1 + AdvancedMixingVatLayout.TANK_INNER_W;
        int y2 = y1 + AdvancedMixingVatLayout.TANK_INNER_H;

        if (filledPixels > 0) {
            int fillTop = y2 - filledPixels;
            graphics.fill(x1, fillTop, x2, y2, 0xFF9BC4D8);
            graphics.fill(x1, fillTop, x2, fillTop + 2, 0xFFD4EEF7);
        }
    }

    private void drawProgressBar(GuiGraphics graphics, int x, int y, int progress) {
        graphics.fill(x - 1, y - 1, x + AdvancedMixingVatLayout.PROGRESS_W + 1, y + AdvancedMixingVatLayout.PROGRESS_H + 1, 0xFF767C88);
        graphics.fill(x, y, x + AdvancedMixingVatLayout.PROGRESS_W, y + AdvancedMixingVatLayout.PROGRESS_H, 0xFF101216);

        if (progress > 0) {
            int clamped = Math.min(AdvancedMixingVatLayout.PROGRESS_W, progress);
            graphics.fill(x, y, x + clamped, y + AdvancedMixingVatLayout.PROGRESS_H, 0xFF768AB8);
            graphics.fill(x, y, x + clamped, y + 2, 0xFFAAB9DB);
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
}