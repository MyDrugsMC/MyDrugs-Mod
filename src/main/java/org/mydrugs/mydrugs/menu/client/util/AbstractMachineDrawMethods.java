package org.mydrugs.mydrugs.menu.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Shared machine drawing primitives for screens and JEI recipe categories.
 *
 * The methods operate on local GUI coordinates. Implementations can override
 * {@link #drawOriginX()} and {@link #drawOriginY()} when they need to translate
 * local coordinates into absolute screen coordinates. JEI categories use the
 * default origin of 0,0.
 */
public interface AbstractMachineDrawMethods {
    int DEFAULT_PREVIEW_TANK_CAPACITY = 4000;

    int WINDOW_OUTER_BG = 0xFF171717;
    int WINDOW_INNER_BG = 0xFF252525;

    int PANEL_LIGHT_BORDER = 0xFF5C616B;
    int PANEL_DARK_BORDER = 0xFF0E1014;

    int SLOT_FRAME_COLOR = 0xFF8A8F99;
    int SLOT_INNER_COLOR = 0xFF101216;

    int TANK_FRAME_COLOR = 0xFF7F8590;
    int TANK_BODY_COLOR = 0xFF15171B;
    int TANK_INNER_COLOR = 0xFF090A0D;

    int BAR_FRAME_COLOR = 0xFF767C88;
    int BAR_BG_COLOR = 0xFF101216;
    int BAR_INNER_COLOR = 0xFF090A0D;

    int BUTTON_FRAME_COLOR = 0xFF7C818C;
    int BUTTON_FILL_COLOR = 0xFF181A1F;

    default int drawOriginX() {
        return 0;
    }

    default int drawWidth() {
        return 0;
    }

    default int drawHeight() {
        return 0;
    }

    default int drawOriginY() {
        return 0;
    }

    default int guiX(int localX) {
        return drawOriginX() + localX;
    }

    default int guiY(int localY) {
        return drawOriginY() + localY;
    }

    default int labelX(int localX) {
        return guiX(localX);
    }

    default int labelY(int localY) {
        return guiY(localY);
    }

    default int getFluidColor(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return 0;
        }

        int color = IClientFluidTypeExtensions.of(fluid).getTintColor();
        if ((color >>> 24) == 0) {
            color |= 0xFF000000;
        }
        return color;
    }

    default int getFluidColor(FluidStack stack) {
        return stack == null ? 0 : getFluidColor(stack.getFluid());
    }

    default String getFluidName(Fluid fluid) {
        return fluid == null || fluid == Fluids.EMPTY ? "empty" : fluid.getFluidType().getDescription().getString();
    }

    default int darken(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        r = Math.max(0, Math.min(255, (int) (r * factor)));
        g = Math.max(0, Math.min(255, (int) (g * factor)));
        b = Math.max(0, Math.min(255, (int) (b * factor)));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    default int lighten(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        r = Math.max(0, Math.min(255, (int) (r * factor)));
        g = Math.max(0, Math.min(255, (int) (g * factor)));
        b = Math.max(0, Math.min(255, (int) (b * factor)));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    default void drawWindow(GuiGraphics graphics) {
        drawWindow(graphics, drawWidth(), drawHeight());
    }

    default void drawWindowColored(GuiGraphics graphics, int outerColor, int innerColor) {
        drawWindow(graphics, drawWidth(), drawHeight(), outerColor, innerColor);
    }

    default void drawWindow(GuiGraphics graphics, int width, int height) {
        drawWindow(graphics, width, height, WINDOW_OUTER_BG, WINDOW_INNER_BG);
    }

    default void drawWindow(GuiGraphics graphics, int width, int height, int outerColor, int innerColor) {
        graphics.fill(guiX(0), guiY(0), guiX(width), guiY(height), outerColor);
        graphics.fill(guiX(4), guiY(4), guiX(width - 4), guiY(height - 4), innerColor);
    }

    default void drawPanel(GuiGraphics graphics, int localX, int localY, int width, int height, int fillColor) {
        drawPanel(graphics, localX, localY, width, height, fillColor, PANEL_LIGHT_BORDER, PANEL_DARK_BORDER);
    }

    default void drawPanel(
            GuiGraphics graphics,
            int localX,
            int localY,
            int width,
            int height,
            int fillColor,
            int lightBorderColor,
            int darkBorderColor
    ) {
        int x = guiX(localX);
        int y = guiY(localY);

        graphics.fill(x, y, x + width, y + height, fillColor);
        drawBorderAbsolute(graphics, x, y, width, height, lightBorderColor, darkBorderColor);
    }

    default void drawBorder(GuiGraphics graphics, int localX, int localY, int width, int height) {
        drawBorder(graphics, localX, localY, width, height, PANEL_LIGHT_BORDER, PANEL_DARK_BORDER);
    }

    default void drawBorder(
            GuiGraphics graphics,
            int localX,
            int localY,
            int width,
            int height,
            int lightBorderColor,
            int darkBorderColor
    ) {
        drawBorderAbsolute(graphics, guiX(localX), guiY(localY), width, height, lightBorderColor, darkBorderColor);
    }

    default void drawBorderAbsolute(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int lightBorderColor,
            int darkBorderColor
    ) {
        graphics.fill(x, y, x + width, y + 1, lightBorderColor);
        graphics.fill(x, y + height - 1, x + width, y + height, darkBorderColor);
        graphics.fill(x, y, x + 1, y + height, lightBorderColor);
        graphics.fill(x + width - 1, y, x + width, y + height, darkBorderColor);
    }

    default void drawSlotFrame(GuiGraphics graphics, int slotX, int slotY) {
        drawSlotFrame(graphics, slotX, slotY, SLOT_FRAME_COLOR, SLOT_INNER_COLOR);
    }

    default void drawSlotFrame(GuiGraphics graphics, int slotX, int slotY, int frameColor, int innerColor) {
        int x = guiX(slotX - 1);
        int y = guiY(slotY - 1);

        graphics.fill(x, y, x + 18, y + 18, frameColor);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, innerColor);
    }

    default void drawTankFrame(
            GuiGraphics graphics,
            int tankX,
            int tankY,
            int tankW,
            int tankH,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH
    ) {
        drawTankFrame(
                graphics,
                tankX,
                tankY,
                tankW,
                tankH,
                innerOffsetX,
                innerOffsetY,
                innerW,
                innerH,
                TANK_FRAME_COLOR,
                TANK_BODY_COLOR,
                TANK_INNER_COLOR
        );
    }

    default void drawTankFrame(
            GuiGraphics graphics,
            int tankX,
            int tankY,
            int tankW,
            int tankH,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH,
            int frameColor,
            int bodyColor,
            int innerColor
    ) {
        int x = guiX(tankX);
        int y = guiY(tankY);

        graphics.fill(x - 1, y - 1, x + tankW + 1, y + tankH + 1, frameColor);
        graphics.fill(x, y, x + tankW, y + tankH, bodyColor);
        graphics.fill(
                x + innerOffsetX,
                y + innerOffsetY,
                x + innerOffsetX + innerW,
                y + innerOffsetY + innerH,
                innerColor
        );
    }

    default void drawTankFillShaded(
            GuiGraphics graphics,
            int tankX,
            int tankY,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH,
            int filledPixels,
            int color
    ) {
        if (filledPixels <= 0 || (color >>> 24) == 0) {
            return;
        }

        int x1 = guiX(tankX + innerOffsetX);
        int y1 = guiY(tankY + innerOffsetY);
        int x2 = x1 + innerW;
        int y2 = y1 + innerH;

        int clamped = Math.min(innerH, filledPixels);
        int fillTop = y2 - clamped;

        graphics.fill(x1, fillTop, x2, y2, color);

        int lighter = lighten(color, 1.15f);
        int darker = darken(color, 0.72f);

        if (innerW >= 2) {
            graphics.fill(x1 + 1, fillTop, Math.min(x1 + 2, x2), y2, lighter);
        }
        if (innerW >= 5) {
            graphics.fill(Math.min(x1 + 3, x2), fillTop, Math.min(x1 + 5, x2), y2, darker);
        }
    }

    default void drawTankFillTopLit(
            GuiGraphics graphics,
            int tankX,
            int tankY,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH,
            int filledPixels,
            int fillColor,
            int topHighlightColor
    ) {
        if (filledPixels <= 0 || (fillColor >>> 24) == 0) {
            return;
        }

        int x1 = guiX(tankX + innerOffsetX);
        int y1 = guiY(tankY + innerOffsetY);
        int x2 = x1 + innerW;
        int y2 = y1 + innerH;

        int clamped = Math.min(innerH, filledPixels);
        int fillTop = y2 - clamped;

        graphics.fill(x1, fillTop, x2, y2, fillColor);
        graphics.fill(x1, fillTop, x2, Math.min(fillTop + 2, y2), topHighlightColor);
    }

    default int tankPreviewFilledPixels(long amount, long capacity, int innerH) {
        if (amount <= 0 || capacity <= 0 || innerH <= 0) {
            return 0;
        }
        long scaled = Math.max(1L, (amount * innerH + capacity - 1L) / capacity);
        if (scaled > innerH) {
            return innerH;
        }
        return (int) scaled;
    }

    default void drawFluidTankPreview(
            GuiGraphics graphics,
            ResourceLocation fluid,
            int amount,
            int tankX,
            int tankY,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH
    ) {
        drawFluidTankPreview(
                graphics,
                fluid,
                amount,
                DEFAULT_PREVIEW_TANK_CAPACITY,
                tankX,
                tankY,
                innerOffsetX,
                innerOffsetY,
                innerW,
                innerH
        );
    }

    default void drawFluidTankPreview(
            GuiGraphics graphics,
            ResourceLocation fluid,
            int amount,
            int capacity,
            int tankX,
            int tankY,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH
    ) {
        Fluid fluid1 = BuiltInRegistries.FLUID.getValue(fluid);
        drawFluidTankPreview(graphics, fluid1, amount, capacity, tankX, tankY, innerOffsetX, innerOffsetY, innerW, innerH);
    }

    default void drawFluidTankPreview(
            GuiGraphics graphics,
            Fluid fluid,
            int amount,
            int tankX,
            int tankY,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH
    ) {
        drawFluidTankPreview(
                graphics,
                fluid,
                amount,
                DEFAULT_PREVIEW_TANK_CAPACITY,
                tankX,
                tankY,
                innerOffsetX,
                innerOffsetY,
                innerW,
                innerH
        );
    }

    default void drawFluidTankPreview(
            GuiGraphics graphics,
            Fluid fluid,
            int amount,
            int capacity,
            int tankX,
            int tankY,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH
    ) {
        if (fluid == null || fluid == Fluids.EMPTY || amount <= 0) {
            return;
        }
        drawTankFillShaded(
                graphics,
                tankX,
                tankY,
                innerOffsetX,
                innerOffsetY,
                innerW,
                innerH,
                tankPreviewFilledPixels(amount, capacity, innerH),
                getFluidColor(fluid)
        );
    }

    default void drawGasTankPreview(
            GuiGraphics graphics,
            ResourceLocation gasId,
            long amount,
            int tankX,
            int tankY,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH
    ) {
        drawGasTankPreview(
                graphics,
                gasId,
                amount,
                DEFAULT_PREVIEW_TANK_CAPACITY,
                tankX,
                tankY,
                innerOffsetX,
                innerOffsetY,
                innerW,
                innerH
        );
    }

    default void drawGasTankPreview(
            GuiGraphics graphics,
            ResourceLocation gasId,
            long amount,
            long capacity,
            int tankX,
            int tankY,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH
    ) {
        if (gasId == null || amount <= 0) {
            return;
        }
        int color = gasColor(gasId);
        drawTankFillTopLit(
                graphics,
                tankX,
                tankY,
                innerOffsetX,
                innerOffsetY,
                innerW,
                innerH,
                tankPreviewFilledPixels(amount, capacity, innerH),
                color,
                lighten(color, 1.20f)
        );
    }

    default void drawHorizontalBar(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int filledPixels,
            int fillColor,
            int highlightColor
    ) {
        drawHorizontalBar(graphics, x, y, width, height, filledPixels, fillColor, highlightColor, BAR_FRAME_COLOR, BAR_BG_COLOR);
    }

    default void drawHorizontalBar(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int filledPixels,
            int fillColor,
            int highlightColor,
            int frameColor,
            int backgroundColor
    ) {
        int gx = guiX(x);
        int gy = guiY(y);

        graphics.fill(gx - 1, gy - 1, gx + width + 1, gy + height + 1, frameColor);
        graphics.fill(gx, gy, gx + width, gy + height, backgroundColor);

        if (filledPixels > 0) {
            int clamped = Math.min(width, filledPixels);
            graphics.fill(gx, gy, gx + clamped, gy + height, fillColor);
            graphics.fill(gx, gy, gx + clamped, Math.min(gy + 2, gy + height), highlightColor);
        }
    }

    default void drawVerticalBar(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH,
            int filledPixels,
            int fillColor,
            int highlightColor
    ) {
        drawVerticalBar(
                graphics,
                x,
                y,
                width,
                height,
                innerOffsetX,
                innerOffsetY,
                innerW,
                innerH,
                filledPixels,
                fillColor,
                highlightColor,
                BAR_FRAME_COLOR,
                BAR_BG_COLOR,
                BAR_INNER_COLOR
        );
    }

    default void drawVerticalBar(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH,
            int filledPixels,
            int fillColor,
            int highlightColor,
            int frameColor,
            int backgroundColor,
            int innerBackgroundColor
    ) {
        int gx = guiX(x);
        int gy = guiY(y);

        graphics.fill(gx - 1, gy - 1, gx + width + 1, gy + height + 1, frameColor);
        graphics.fill(gx, gy, gx + width, gy + height, backgroundColor);

        int x1 = gx + innerOffsetX;
        int y1 = gy + innerOffsetY;
        int x2 = x1 + innerW;
        int y2 = y1 + innerH;

        graphics.fill(x1, y1, x2, y2, innerBackgroundColor);

        if (filledPixels > 0) {
            int clamped = Math.min(innerH, filledPixels);
            int fillTop = y2 - clamped;
            graphics.fill(x1, fillTop, x2, y2, fillColor);
            graphics.fill(x1, fillTop, x2, Math.min(fillTop + 2, y2), highlightColor);
        }
    }

    default void drawDumpButton(GuiGraphics graphics, int localX, int localY, int size, boolean hovered, boolean enabled) {
        int x = guiX(localX);
        int y = guiY(localY);

        graphics.fill(x, y, x + size, y + size, BUTTON_FRAME_COLOR);
        graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, BUTTON_FILL_COLOR);

        if (!enabled) {
            graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, 0x66000000);
        } else if (hovered) {
            graphics.fill(x + 1, y + 1, x + size - 1, y + size - 1, 0x22FFFFFF);
        }

        int cross = enabled ? 0xFFD65B5B : 0xFF6E4545;
        graphics.fill(x + 3, y + 3, x + 4, y + 9, cross);
        graphics.fill(x + 8, y + 3, x + 9, y + 9, cross);
        graphics.fill(x + 4, y + 4, x + 8, y + 5, cross);
        graphics.fill(x + 4, y + 7, x + 8, y + 8, cross);
    }

    default void drawPlusButton(GuiGraphics graphics, int localX, int localY, int width, int height, boolean hovered) {
        int x = guiX(localX);
        int y = guiY(localY);

        graphics.fill(x, y, x + width, y + height, BUTTON_FRAME_COLOR);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, BUTTON_FILL_COLOR);

        if (hovered) {
            graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0x22FFFFFF);
        }

        graphics.drawString(Minecraft.getInstance().font, "+", x + (width / 2) - 2, y + (height / 2) - 4, 0xFFD8DCE6, false);
    }

    default void drawHoldButton(
            GuiGraphics graphics,
            int localX,
            int localY,
            int width,
            int height,
            boolean hovered,
            boolean active,
            String idleText,
            String activeText
    ) {
        int x = guiX(localX);
        int y = guiY(localY);

        int border = active ? 0xFFA8F17A : (hovered ? 0xFF94C76C : 0xFF688A50);
        int fill = active ? 0xFF56773F : (hovered ? 0xFF4A6536 : 0xFF334A26);

        graphics.fill(x, y, x + width, y + height, border);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);

        graphics.drawCenteredString(
                Minecraft.getInstance().font,
                active ? activeText : idleText,
                x + width / 2,
                y + 6,
                0xFFF3FFF0
        );
    }

    default void drawArrow(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        int midY = y + height / 2;

        int shaftH = Math.max(1, (height / 2) | 1);
        int shaftY = midY - shaftH / 2;

        int headW = Math.max(4, shaftH);
        if (width < headW + 1) {
            headW = Math.max(1, width);
        }
        int shaftW = width - headW;

        if (shaftW > 0) {
            graphics.fill(guiX(x), guiY(shaftY), guiX(x + shaftW), guiY(shaftY + shaftH), color);
        }

        int halfBase = shaftH / 2;

        if (headW == 1) {
            int xx = x + shaftW;
            graphics.fill(guiX(xx), guiY(midY - halfBase), guiX(xx + 1), guiY(midY + halfBase + 1), color);
        } else {
            for (int i = 0; i < headW; i++) {
                int half = halfBase * (headW - 1 - i) / (headW - 1);
                int xx = x + shaftW + i;
                graphics.fill(guiX(xx), guiY(midY - half), guiX(xx + 1), guiY(midY + half + 1), color);
            }
        }
    }

    default void drawCircle(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
        int cx = guiX(centerX);
        int cy = guiY(centerY);
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    graphics.fill(cx + x, cy + y, cx + x + 1, cy + y + 1, color);
                }
            }
        }
    }

    default int gasColor(ResourceLocation id) {
        int hash = id.toString().hashCode();
        int r = 90 + (hash & 0x3F);
        int g = 110 + ((hash >> 6) & 0x3F);
        int b = 140 + ((hash >> 12) & 0x3F);
        return 0xC0000000 | (r << 16) | (g << 8) | b;
    }

    default boolean isHoveringBox(
            int localX,
            int localY,
            int width,
            int height,
            double mouseX,
            double mouseY
    ) {
        int x = guiX(localX);
        int y = guiY(localY);

        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }
}
