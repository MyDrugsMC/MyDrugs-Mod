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
import org.mydrugs.mydrugs.menu.client.util.AbstractMachineDrawMethods;
import org.mydrugs.mydrugs.menu.layout.StandardInventoryLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all custom machine screens.
 * <p>
 * All primitive machine drawing now comes from {@link AbstractMachineDrawMethods}.
 * JEI recipe categories use the same abstraction, so changing shared window,
 * panel, slot, tank, bar, button, arrow, color, or fluid/gas drawing in that
 * abstraction automatically updates both real screens and recipe GUIs.
 */
public abstract class AbstractMachineScreen<T extends AbstractContainerMenu>
        extends AbstractContainerScreen<T>
        implements AbstractMachineDrawMethods {
    protected static final int SIEVE_INV_PANEL_FILL = 0xFF2C2C2C;
    protected static final int SIEVE_INV_PANEL_LIGHT_BORDER = 0xFF595959;
    protected static final int SIEVE_INV_PANEL_DARK_BORDER = 0xFF101010;

    protected AbstractMachineScreen(T menu, Inventory playerInventory, Component title, int imageWidth, int imageHeight) {
        super(menu, playerInventory, title);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    public int drawOriginX() {
        return this.leftPos;
    }

    @Override
    public int drawOriginY() {
        return this.topPos;
    }

    @Override
    public int drawWidth() {
        return this.imageWidth;
    }

    @Override
    public int drawHeight() {
        return this.imageHeight;
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

    protected void pressMenuButton(int buttonId) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
        }
    }

    /**
     * Generic textured blit helper.
     * Useful for texture-based screens.
     */
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

    protected static class InvisibleButton extends Button {
        public InvisibleButton(int x, int y, int width, int height, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // Intentionally empty: only used for click handling.
        }
    }
}
