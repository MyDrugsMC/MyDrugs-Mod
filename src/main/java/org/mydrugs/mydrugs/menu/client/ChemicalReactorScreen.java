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
import net.neoforged.neoforge.fluids.FluidStack;
import org.mydrugs.mydrugs.blocks.entity.ChemicalReactorBlockEntity;
import org.mydrugs.mydrugs.menu.ChemicalReactorMenu;
import org.mydrugs.mydrugs.menu.layout.ChemicalReactorLayout;

import java.util.ArrayList;
import java.util.List;

public class ChemicalReactorScreen extends AbstractContainerScreen<ChemicalReactorMenu> {
    public ChemicalReactorScreen(ChemicalReactorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = ChemicalReactorLayout.GUI_WIDTH;
        this.imageHeight = ChemicalReactorLayout.GUI_HEIGHT;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = this.leftPos;
        int top = this.topPos;

        graphics.fill(left, top, left + this.imageWidth, top + this.imageHeight, 0xFF171717);
        graphics.fill(left + 4, top + 4, left + this.imageWidth - 4, top + this.imageHeight - 4, 0xFF252525);

        fillPanel(graphics,
                left + ChemicalReactorLayout.MACHINE_PANEL_X,
                top + ChemicalReactorLayout.MACHINE_PANEL_Y,
                ChemicalReactorLayout.MACHINE_PANEL_W,
                ChemicalReactorLayout.MACHINE_PANEL_H,
                0xFF2C3138);

        fillPanel(graphics,
                left + ChemicalReactorLayout.INVENTORY_PANEL_X,
                top + ChemicalReactorLayout.INVENTORY_PANEL_Y,
                ChemicalReactorLayout.INVENTORY_PANEL_W,
                ChemicalReactorLayout.INVENTORY_PANEL_H,
                0xFF2A2D33);

        drawTankFrame(graphics, ChemicalReactorLayout.PRIMARY_GAS_TANK_X, ChemicalReactorLayout.PRIMARY_GAS_TANK_Y);
        drawTankFrame(graphics, ChemicalReactorLayout.SECONDARY_TANK_X, ChemicalReactorLayout.SECONDARY_TANK_Y);
        drawTankFrame(graphics, ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.OUTPUT_TANK_Y);

        ChemicalReactorBlockEntity blockEntity = getBlockEntity();
        if (blockEntity != null) {
            drawGasTankFill(
                    graphics,
                    left + ChemicalReactorLayout.PRIMARY_GAS_TANK_X,
                    top + ChemicalReactorLayout.PRIMARY_GAS_TANK_Y,
                    blockEntity.getScaledPrimaryGas(ChemicalReactorLayout.TANK_INNER_H),
                    blockEntity.getPrimaryGasColor()
            );

            int secondaryPixels = blockEntity.isSecondaryFluidMode()
                    ? blockEntity.getScaledSecondaryFluid(ChemicalReactorLayout.TANK_INNER_H)
                    : blockEntity.getScaledSecondaryGas(ChemicalReactorLayout.TANK_INNER_H);

            int secondaryColor = blockEntity.isSecondaryFluidMode()
                    ? getFluidColor(blockEntity.getSecondaryFluid())
                    : blockEntity.getSecondaryGasColor();

            drawTankFill(
                    graphics,
                    left + ChemicalReactorLayout.SECONDARY_TANK_X,
                    top + ChemicalReactorLayout.SECONDARY_TANK_Y,
                    secondaryPixels,
                    secondaryColor
            );

            int outputPixels = blockEntity.isOutputFluidMode()
                    ? blockEntity.getScaledOutputFluid(ChemicalReactorLayout.TANK_INNER_H)
                    : blockEntity.getScaledOutputGas(ChemicalReactorLayout.TANK_INNER_H);

            int outputColor = blockEntity.isOutputFluidMode()
                    ? getFluidColor(blockEntity.getOutputFluid())
                    : blockEntity.getOutputGasColor();

            drawTankFill(
                    graphics,
                    left + ChemicalReactorLayout.OUTPUT_TANK_X,
                    top + ChemicalReactorLayout.OUTPUT_TANK_Y,
                    outputPixels,
                    outputColor
            );
        }

        drawSlotFrame(graphics, ChemicalReactorLayout.FUEL_SLOT_X, ChemicalReactorLayout.FUEL_SLOT_Y);

        drawProgressBar(
                graphics,
                left + ChemicalReactorLayout.PROGRESS_X,
                top + ChemicalReactorLayout.PROGRESS_Y,
                this.menu.getScaledProgress(ChemicalReactorLayout.PROGRESS_W)
        );

        drawHeatBar(
                graphics,
                left + ChemicalReactorLayout.HEAT_BAR_X,
                top + ChemicalReactorLayout.HEAT_BAR_Y,
                this.menu.getScaledHeat(ChemicalReactorLayout.HEAT_BAR_INNER_H)
        );

        drawFuelBar(
                graphics,
                left + ChemicalReactorLayout.FUEL_BAR_X,
                top + ChemicalReactorLayout.FUEL_BAR_Y,
                this.menu.getScaledBurnTime(ChemicalReactorLayout.FUEL_BAR_INNER_H),
                this.menu.isLit()
        );

        drawManualBar(
                graphics,
                left + ChemicalReactorLayout.MANUAL_BAR_X,
                top + ChemicalReactorLayout.MANUAL_BAR_Y,
                this.menu.getScaledManualEnergy(ChemicalReactorLayout.MANUAL_BAR_W)
        );
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, 8, 6, 0xE0E0E0, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        ChemicalReactorBlockEntity blockEntity = getBlockEntity();
        if (blockEntity == null) {
            return;
        }

        if (isHoveringBox(ChemicalReactorLayout.PRIMARY_GAS_TANK_X, ChemicalReactorLayout.PRIMARY_GAS_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Primary gas"),
                    Component.literal(blockEntity.getPrimaryGasName()),
                    Component.literal(blockEntity.getPrimaryGasAmount() + " / " + ChemicalReactorBlockEntity.GAS_TANK_CAPACITY)
            );
        } else if (isHoveringBox(ChemicalReactorLayout.SECONDARY_TANK_X, ChemicalReactorLayout.SECONDARY_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            if (blockEntity.isSecondaryFluidMode()) {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Secondary fluid"),
                        Component.literal(getFluidName(blockEntity.getSecondaryFluid().getFluid())),
                        Component.literal(blockEntity.getSecondaryFluidAmount() + " / " + ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY + " mB")
                );
            } else {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Secondary gas"),
                        Component.literal(blockEntity.getSecondaryGasName()),
                        Component.literal(blockEntity.getSecondaryGasAmount() + " / " + ChemicalReactorBlockEntity.GAS_TANK_CAPACITY)
                );
            }
        } else if (isHoveringBox(ChemicalReactorLayout.OUTPUT_TANK_X, ChemicalReactorLayout.OUTPUT_TANK_Y, ChemicalReactorLayout.TANK_W, ChemicalReactorLayout.TANK_H, mouseX, mouseY)) {
            if (blockEntity.isOutputFluidMode()) {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output fluid"),
                        Component.literal(getFluidName(blockEntity.getOutputFluid().getFluid())),
                        Component.literal(blockEntity.getOutputFluidAmount() + " / " + ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY + " mB")
                );
            } else {
                renderTooltipLines(
                        graphics,
                        mouseX,
                        mouseY,
                        Component.literal("Output gas"),
                        Component.literal(blockEntity.getOutputGasName()),
                        Component.literal(blockEntity.getOutputGasAmount() + " / " + ChemicalReactorBlockEntity.GAS_TANK_CAPACITY)
                );
            }
        } else if (isHoveringBox(ChemicalReactorLayout.PROGRESS_X, ChemicalReactorLayout.PROGRESS_Y, ChemicalReactorLayout.PROGRESS_W, ChemicalReactorLayout.PROGRESS_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Progress"),
                    Component.literal(this.menu.getProgress() + " / " + this.menu.getMaxProgress())
            );
        } else if (isHoveringBox(ChemicalReactorLayout.HEAT_BAR_X, ChemicalReactorLayout.HEAT_BAR_Y, ChemicalReactorLayout.HEAT_BAR_W, ChemicalReactorLayout.HEAT_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Heat"),
                    Component.literal(this.menu.getHeat() + " / " + this.menu.getMaxHeat())
            );
        } else if (isHoveringBox(ChemicalReactorLayout.FUEL_BAR_X, ChemicalReactorLayout.FUEL_BAR_Y, ChemicalReactorLayout.FUEL_BAR_W, ChemicalReactorLayout.FUEL_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Fuel burn time"),
                    Component.literal(this.menu.getBurnTimeRemaining() + " / " + this.menu.getBurnTimeTotal() + " ticks")
            );
        } else if (isHoveringBox(ChemicalReactorLayout.MANUAL_BAR_X, ChemicalReactorLayout.MANUAL_BAR_Y, ChemicalReactorLayout.MANUAL_BAR_W, ChemicalReactorLayout.MANUAL_BAR_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal("Manual boost"),
                    Component.literal(this.menu.getManualEnergy() + " / " + this.menu.getMaxManualEnergy())
            );
        }
    }

    private ChemicalReactorBlockEntity getBlockEntity() {
        if (this.minecraft == null || this.minecraft.player == null) {
            return null;
        }
        return this.menu.getBlockEntity(this.minecraft.player);
    }

    private static int getFluidColor(FluidStack stack) {
        return getFluidColor(stack.getFluid());
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

        r = Math.max(0, Math.min(255, (int) Math.min(255, r * factor)));
        g = Math.max(0, Math.min(255, (int) Math.min(255, g * factor)));
        b = Math.max(0, Math.min(255, (int) Math.min(255, b * factor)));

        return (a << 24) | (r << 16) | (g << 8) | b;
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

        graphics.fill(x - 1, y - 1, x + ChemicalReactorLayout.TANK_W + 1, y + ChemicalReactorLayout.TANK_H + 1, 0xFF7F8590);
        graphics.fill(x, y, x + ChemicalReactorLayout.TANK_W, y + ChemicalReactorLayout.TANK_H, 0xFF15171B);
        graphics.fill(
                x + ChemicalReactorLayout.TANK_INNER_X_OFFSET,
                y + ChemicalReactorLayout.TANK_INNER_Y_OFFSET,
                x + ChemicalReactorLayout.TANK_INNER_X_OFFSET + ChemicalReactorLayout.TANK_INNER_W,
                y + ChemicalReactorLayout.TANK_INNER_Y_OFFSET + ChemicalReactorLayout.TANK_INNER_H,
                0xFF090A0D
        );
    }

    private void drawGasTankFill(GuiGraphics graphics, int tankX, int tankY, int filledPixels, int color) {
        drawTankFill(graphics, tankX, tankY, filledPixels, color);
    }

    private void drawTankFill(GuiGraphics graphics, int tankX, int tankY, int filledPixels, int color) {
        if (filledPixels <= 0 || (color >>> 24) == 0) {
            return;
        }

        int x1 = tankX + ChemicalReactorLayout.TANK_INNER_X_OFFSET;
        int y1 = tankY + ChemicalReactorLayout.TANK_INNER_Y_OFFSET;
        int x2 = x1 + ChemicalReactorLayout.TANK_INNER_W;
        int y2 = y1 + ChemicalReactorLayout.TANK_INNER_H;
        int fillTop = y2 - filledPixels;

        graphics.fill(x1, fillTop, x2, y2, color);

        int darker = darken(color, 0.72f);
        int lighter = lighten(color, 1.15f);

        graphics.fill(x1 + 1, fillTop, x1 + 2, y2, lighter);
        graphics.fill(x1 + 3, fillTop, x1 + 5, y2, darker);
    }

    private void drawProgressBar(GuiGraphics graphics, int x, int y, int progress) {
        graphics.fill(x - 1, y - 1, x + ChemicalReactorLayout.PROGRESS_W + 1, y + ChemicalReactorLayout.PROGRESS_H + 1, 0xFF767C88);
        graphics.fill(x, y, x + ChemicalReactorLayout.PROGRESS_W, y + ChemicalReactorLayout.PROGRESS_H, 0xFF101216);

        if (progress > 0) {
            int clamped = Math.min(ChemicalReactorLayout.PROGRESS_W, progress);
            graphics.fill(x, y, x + clamped, y + ChemicalReactorLayout.PROGRESS_H, 0xFF85A6C9);
            graphics.fill(x, y, x + clamped, y + 2, 0xFFC6DCF2);
        }
    }

    private void drawHeatBar(GuiGraphics graphics, int x, int y, int filledPixels) {
        graphics.fill(x - 1, y - 1, x + ChemicalReactorLayout.HEAT_BAR_W + 1, y + ChemicalReactorLayout.HEAT_BAR_H + 1, 0xFF767C88);
        graphics.fill(x, y, x + ChemicalReactorLayout.HEAT_BAR_W, y + ChemicalReactorLayout.HEAT_BAR_H, 0xFF101216);

        int x1 = x + ChemicalReactorLayout.HEAT_BAR_INNER_X_OFFSET;
        int y1 = y + ChemicalReactorLayout.HEAT_BAR_INNER_Y_OFFSET;
        int x2 = x1 + ChemicalReactorLayout.HEAT_BAR_INNER_W;
        int y2 = y1 + ChemicalReactorLayout.HEAT_BAR_INNER_H;

        graphics.fill(x1, y1, x2, y2, 0xFF090A0D);

        if (filledPixels > 0) {
            int clamped = Math.min(ChemicalReactorLayout.HEAT_BAR_INNER_H, filledPixels);
            int fillTop = y2 - clamped;
            graphics.fill(x1, fillTop, x2, y2, 0xFFE35C3F);
            graphics.fill(x1, fillTop, x2, fillTop + 2, 0xFFFFB870);
        }
    }

    private void drawFuelBar(GuiGraphics graphics, int x, int y, int filledPixels, boolean lit) {
        graphics.fill(x - 1, y - 1, x + ChemicalReactorLayout.FUEL_BAR_W + 1, y + ChemicalReactorLayout.FUEL_BAR_H + 1, 0xFF767C88);
        graphics.fill(x, y, x + ChemicalReactorLayout.FUEL_BAR_W, y + ChemicalReactorLayout.FUEL_BAR_H, 0xFF101216);

        int x1 = x + ChemicalReactorLayout.FUEL_BAR_INNER_X_OFFSET;
        int y1 = y + ChemicalReactorLayout.FUEL_BAR_INNER_Y_OFFSET;
        int x2 = x1 + ChemicalReactorLayout.FUEL_BAR_INNER_W;
        int y2 = y1 + ChemicalReactorLayout.FUEL_BAR_INNER_H;

        graphics.fill(x1, y1, x2, y2, 0xFF090A0D);

        if (filledPixels > 0) {
            int clamped = Math.min(ChemicalReactorLayout.FUEL_BAR_INNER_H, filledPixels);
            int fillTop = y2 - clamped;
            graphics.fill(x1, fillTop, x2, y2, lit ? 0xFFE38D3F : 0xFF8E6A4A);
            graphics.fill(x1, fillTop, x2, fillTop + 2, 0xFFFFC270);
        }
    }

    private void drawManualBar(GuiGraphics graphics, int x, int y, int filledPixels) {
        graphics.fill(x - 1, y - 1, x + ChemicalReactorLayout.MANUAL_BAR_W + 1, y + ChemicalReactorLayout.MANUAL_BAR_H + 1, 0xFF767C88);
        graphics.fill(x, y, x + ChemicalReactorLayout.MANUAL_BAR_W, y + ChemicalReactorLayout.MANUAL_BAR_H, 0xFF101216);

        if (filledPixels > 0) {
            int clamped = Math.min(ChemicalReactorLayout.MANUAL_BAR_W, filledPixels);
            graphics.fill(x, y, x + clamped, y + ChemicalReactorLayout.MANUAL_BAR_H, 0xFF63B36D);
            graphics.fill(x, y, x + clamped, y + 1, 0xFFA8E4AF);
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