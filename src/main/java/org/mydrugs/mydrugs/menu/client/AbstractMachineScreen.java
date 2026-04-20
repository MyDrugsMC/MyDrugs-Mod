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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.mydrugs.mydrugs.menu.layout.StandardInventoryLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all custom machine screens.
 * <p>
 * Goal:
 * - keep render()/tooltip/button plumbing in one place
 * - centralize panel/slot/tank/bar drawing
 * - reduce future screen classes to mostly layout + logic
 * <p>
 * This class does NOT force one exact renderBg() style because some screens are
 * texture-based (AdvancedFurnace) and others are fully drawn with fill().
 */
public abstract class AbstractMachineScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    // ---------------------------
    // Shared theme colors
    // ---------------------------
    protected static final int WINDOW_OUTER_BG = 0xFF171717;
    protected static final int WINDOW_INNER_BG = 0xFF252525;

    protected static final int PANEL_LIGHT_BORDER = 0xFF5C616B;
    protected static final int PANEL_DARK_BORDER = 0xFF0E1014;

    protected static final int SLOT_FRAME_COLOR = 0xFF8A8F99;
    protected static final int SLOT_INNER_COLOR = 0xFF101216;

    protected static final int TANK_FRAME_COLOR = 0xFF7F8590;
    protected static final int TANK_BODY_COLOR = 0xFF15171B;
    protected static final int TANK_INNER_COLOR = 0xFF090A0D;

    protected static final int BAR_FRAME_COLOR = 0xFF767C88;
    protected static final int BAR_BG_COLOR = 0xFF101216;
    protected static final int BAR_INNER_COLOR = 0xFF090A0D;

    protected static final int BUTTON_FRAME_COLOR = 0xFF7C818C;
    protected static final int BUTTON_FILL_COLOR = 0xFF181A1F;

    protected static final int SIEVE_INV_PANEL_FILL = 0xFF2C2C2C;
    protected static final int SIEVE_INV_PANEL_LIGHT_BORDER = 0xFF595959;
    protected static final int SIEVE_INV_PANEL_DARK_BORDER = 0xFF101010;

    protected AbstractMachineScreen(T menu, Inventory playerInventory, Component title, int imageWidth, int imageHeight) {
        super(menu, playerInventory, title);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    // -------------------------------------------------
    // Color / fluid helpers
    // -------------------------------------------------
    protected static int getFluidColor(FluidStack stack) {
        return getFluidColor(stack.getFluid());
    }

    protected static int getFluidColor(Fluid fluid) {
        if (fluid == Fluids.EMPTY) {
            return 0;
        }

        int color = IClientFluidTypeExtensions.of(fluid).getTintColor();
        if ((color >>> 24) == 0) {
            color |= 0xFF000000;
        }
        return color;
    }

    protected static String getFluidName(Fluid fluid) {
        return fluid == Fluids.EMPTY ? "empty" : fluid.getFluidType().getDescription().getString();
    }

    protected static int darken(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        r = Math.max(0, Math.min(255, (int) (r * factor)));
        g = Math.max(0, Math.min(255, (int) (g * factor)));
        b = Math.max(0, Math.min(255, (int) (b * factor)));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    protected static int lighten(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        r = Math.max(0, Math.min(255, (int) (r * factor)));
        g = Math.max(0, Math.min(255, (int) (g * factor)));
        b = Math.max(0, Math.min(255, (int) (b * factor)));

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    protected void drawSieveInventoryPanels(GuiGraphics graphics, int inventoryPanelX, int inventoryPanelY) {
        drawPanel(
                graphics,
                inventoryPanelX,
                inventoryPanelY,
                StandardInventoryLayout.PLAYER_INV_PANEL_W,
                StandardInventoryLayout.PLAYER_INV_PANEL_H,
                SIEVE_INV_PANEL_FILL,
                SIEVE_INV_PANEL_LIGHT_BORDER,
                SIEVE_INV_PANEL_DARK_BORDER
        );

        drawPanel(
                graphics,
                inventoryPanelX,
                StandardInventoryLayout.hotbarPanelY(inventoryPanelY),
                StandardInventoryLayout.HOTBAR_PANEL_W,
                StandardInventoryLayout.HOTBAR_PANEL_H,
                SIEVE_INV_PANEL_FILL,
                SIEVE_INV_PANEL_LIGHT_BORDER,
                SIEVE_INV_PANEL_DARK_BORDER
        );
    }

    protected int standardInventoryLabelY(int inventoryPanelY) {
        return StandardInventoryLayout.inventoryLabelY(inventoryPanelY);
    }

    // -------------------------------------------------
    // Shared render flow
    // -------------------------------------------------
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        this.renderExtraTooltips(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * Child classes override this for custom non-item tooltips.
     * Keeps render() identical across screens.
     */
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    // -------------------------------------------------
    // Menu button helper
    // -------------------------------------------------
    protected void pressMenuButton(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    // -------------------------------------------------
    // Coordinate helpers
    // -------------------------------------------------
    protected int guiX(int localX) {
        return this.leftPos + localX;
    }

    protected int guiY(int localY) {
        return this.topPos + localY;
    }

    // -------------------------------------------------
    // Generic textured blit helper
    // Useful for texture-based screens like AdvancedFurnace
    // -------------------------------------------------
    protected void blitTexture(
            GuiGraphics graphics,
            ResourceLocation texture,
            int localX,
            int localY,
            int u,
            int v,
            int width,
            int height,
            int texW,
            int texH
    ) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                guiX(localX),
                guiY(localY),
                u,
                v,
                width,
                height,
                texW,
                texH
        );
    }

    // -------------------------------------------------
    // Window + panels
    // -------------------------------------------------
    protected void drawWindow(GuiGraphics graphics) {
        drawWindow(graphics, WINDOW_OUTER_BG, WINDOW_INNER_BG);
    }

    protected void drawWindow(GuiGraphics graphics, int outerColor, int innerColor) {
        graphics.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, outerColor);
        graphics.fill(this.leftPos + 4, this.topPos + 4, this.leftPos + this.imageWidth - 4, this.topPos + this.imageHeight - 4, innerColor);
    }

    protected void drawPanel(GuiGraphics graphics, int localX, int localY, int width, int height, int fillColor) {
        drawPanel(graphics, localX, localY, width, height, fillColor, PANEL_LIGHT_BORDER, PANEL_DARK_BORDER);
    }

    protected void drawPanel(
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

    protected void drawBorder(GuiGraphics graphics, int localX, int localY, int width, int height) {
        drawBorder(graphics, localX, localY, width, height, PANEL_LIGHT_BORDER, PANEL_DARK_BORDER);
    }

    protected void drawBorder(
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

    protected void drawBorderAbsolute(
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

    // -------------------------------------------------
    // Slots
    // -------------------------------------------------
    protected void drawSlotFrame(GuiGraphics graphics, int slotX, int slotY) {
        drawSlotFrame(graphics, slotX, slotY, SLOT_FRAME_COLOR, SLOT_INNER_COLOR);
    }

    protected void drawSlotFrame(GuiGraphics graphics, int slotX, int slotY, int frameColor, int innerColor) {
        int x = guiX(slotX - 1);
        int y = guiY(slotY - 1);

        graphics.fill(x, y, x + 18, y + 18, frameColor);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, innerColor);
    }

    // -------------------------------------------------
    // Tanks
    // -------------------------------------------------
    protected void drawTankFrame(
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

    protected void drawTankFrame(
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

    /**
     * Standard shaded tank fill used by most of your screens.
     */
    protected void drawTankFillShaded(
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

    /**
     * Alternate tank fill style for simpler water/gas fills.
     */
    protected void drawTankFillTopLit(
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

    // -------------------------------------------------
    // Horizontal bars
    // -------------------------------------------------
    protected void drawHorizontalBar(
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

    protected void drawHorizontalBar(
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

    // -------------------------------------------------
    // Vertical bars with inner area
    // -------------------------------------------------
    protected void drawVerticalBar(
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

    protected void drawVerticalBar(
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

    // -------------------------------------------------
    // Buttons
    // -------------------------------------------------
    protected void drawDumpButton(GuiGraphics graphics, int localX, int localY, int size, boolean hovered, boolean enabled) {
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

    protected void drawPlusButton(GuiGraphics graphics, int localX, int localY, int width, int height, boolean hovered) {
        int x = guiX(localX);
        int y = guiY(localY);

        graphics.fill(x, y, x + width, y + height, BUTTON_FRAME_COLOR);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, BUTTON_FILL_COLOR);

        if (hovered) {
            graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0x22FFFFFF);
        }

        graphics.drawString(this.font, "+", x + (width / 2) - 2, y + (height / 2) - 4, 0xFFD8DCE6, false);
    }

    protected void drawHoldButton(
            GuiGraphics graphics,
            int localX,
            int localY,
            int width,
            int height,
            boolean hovered,
            boolean active,
            Component idleText,
            Component activeText
    ) {
        int x = guiX(localX);
        int y = guiY(localY);

        int border = active ? 0xFFA8F17A : (hovered ? 0xFF94C76C : 0xFF688A50);
        int fill = active ? 0xFF56773F : (hovered ? 0xFF4A6536 : 0xFF334A26);

        graphics.fill(x, y, x + width, y + height, border);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);

        graphics.drawCenteredString(
                this.font,
                active ? activeText : idleText,
                x + width / 2,
                y + 6,
                0xFFF3FFF0
        );
    }

    // -------------------------------------------------
    // Generic shapes
    // -------------------------------------------------
    protected void drawCircle(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    graphics.fill(centerX + x, centerY + y, centerX + x + 1, centerY + y + 1, color);
                }
            }
        }
    }

    protected void drawCircleLocal(GuiGraphics graphics, int localCenterX, int localCenterY, int radius, int color) {
        drawCircle(graphics, guiX(localCenterX), guiY(localCenterY), radius, color);
    }

    protected void drawChevron(GuiGraphics graphics, int x, int y, int size, int color, boolean up) {
        for (int i = 0; i < size; i++) {
            int yy = up ? y + i : y + (size - 1 - i);
            graphics.fill(x + i, yy, x + i + 1, yy + 1, color);
            graphics.fill(x + (size * 2 - 2 - i), yy, x + (size * 2 - 1 - i), yy + 1, color);
        }
    }

    // -------------------------------------------------
    // Hover helpers
    // -------------------------------------------------
    protected boolean isHoveringBox(int localX, int localY, int width, int height, int mouseX, int mouseY) {
        int x = guiX(localX);
        int y = guiY(localY);
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    protected boolean isHoveringBox(int localX, int localY, int width, int height, double mouseX, double mouseY) {
        int x = guiX(localX);
        int y = guiY(localY);
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    // -------------------------------------------------
    // Tooltip helpers
    // -------------------------------------------------
    protected void renderTooltipLines(GuiGraphics graphics, int mouseX, int mouseY, Component... lines) {
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

    protected void renderFluidTooltip(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            String title,
            Fluid fluid,
            int amount,
            int capacity
    ) {
        renderTooltipLines(
                graphics,
                mouseX,
                mouseY,
                Component.literal(title),
                Component.literal(getFluidName(fluid)),
                Component.literal(amount + " / " + capacity + " mB")
        );
    }

    protected void renderSimpleAmountTooltip(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            String title,
            int amount,
            int capacity,
            String unit
    ) {
        renderTooltipLines(
                graphics,
                mouseX,
                mouseY,
                Component.literal(title),
                Component.literal(amount + " / " + capacity + " " + unit)
        );
    }

    // -------------------------------------------------
    // Shared invisible button
    // -------------------------------------------------
    protected static class InvisibleButton extends Button {
        public InvisibleButton(int x, int y, int width, int height, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // Intentionally empty: only used for click handling
        }
    }
}