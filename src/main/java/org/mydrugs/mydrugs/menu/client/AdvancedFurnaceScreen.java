package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.entity.AdvancedFurnaceBlockEntity;
import org.mydrugs.mydrugs.menu.AdvancedFurnaceMenu;

import java.util.List;

public class AdvancedFurnaceScreen extends AbstractContainerScreen<AdvancedFurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "textures/gui/container/advanced_furnace.png");

    private static final int TEX_W = 256;
    private static final int TEX_H = 256;

    private static final int FLAME_X = 54;
    private static final int FLAME_Y = 36;
    private static final int FLAME_W = 14;
    private static final int FLAME_H = 14;

    private static final int ARROW_X = 84;
    private static final int ARROW_Y = 35;
    private static final int ARROW_W = 24;
    private static final int ARROW_H = 17;

    private static final int TANK_X = 152;
    private static final int TANK_Y = 17;
    private static final int TANK_W = 8;
    private static final int TANK_H = 54;

    public AdvancedFurnaceScreen(AdvancedFurnaceMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // Full background
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x, y,
                0, 0,
                this.imageWidth, this.imageHeight,
                TEX_W, TEX_H
        );

        // Flame: bottom -> top
        if (this.menu.isBurning()) {
            int flame = this.menu.getScaledBurn(FLAME_H);
            if (flame > 0) {
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        TEXTURE,
                        x + FLAME_X,
                        y + FLAME_Y + (FLAME_H - flame),
                        176, FLAME_H - flame,
                        FLAME_W, flame,
                        TEX_W, TEX_H
                );
            }
        }

        // Arrow: left -> right
        int progress = this.menu.getScaledProgress(ARROW_W);
        if (progress > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    x + ARROW_X,
                    y + ARROW_Y,
                    176, 14,
                    progress, ARROW_H,
                    TEX_W, TEX_H
            );
        }

        // Tank: bottom -> top
        int tank = this.menu.getScaledTank(TANK_H);
        if (tank > 0) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    TEXTURE,
                    x + TANK_X,
                    y + TANK_Y + (TANK_H - tank),
                    176, 31 + (TANK_H - tank),
                    TANK_W, tank,
                    TEX_W, TEX_H
            );
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        if (isHoveringTank(mouseX, mouseY)) {
            graphics.renderTooltip(
                    this.font,
                    List.of(Component.literal(this.menu.getTankAmount() + " / " + AdvancedFurnaceBlockEntity.TANK_CAPACITY + " mB")
                                    .getVisualOrderText())
                            .stream()
                            .map(net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent::create)
                            .toList(),
                    mouseX,
                    mouseY,
                    net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE,
                    null
            );
        }
    }

    private boolean isHoveringTank(int mouseX, int mouseY) {
        int x = this.leftPos + TANK_X;
        int y = this.topPos + TANK_Y;
        return mouseX >= x && mouseX < x + TANK_W && mouseY >= y && mouseY < y + TANK_H;
    }
}