package org.mydrugs.mydrugs.menu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.entity.AdvancedFurnaceBlockEntity;
import org.mydrugs.mydrugs.menu.AdvancedFurnaceMenu;

public class AdvancedFurnaceScreen extends AbstractMachineScreen<AdvancedFurnaceMenu> {
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
        super(menu, playerInventory, title, 176, 166);
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        blitTexture(graphics, TEXTURE, 0, 0, 0, 0, this.imageWidth, this.imageHeight, TEX_W, TEX_H);

        if (this.menu.isBurning()) {
            int flame = this.menu.getScaledBurn(FLAME_H);
            if (flame > 0) {
                blitTexture(
                        graphics,
                        TEXTURE,
                        FLAME_X,
                        FLAME_Y + (FLAME_H - flame),
                        176,
                        FLAME_H - flame,
                        FLAME_W,
                        flame,
                        TEX_W,
                        TEX_H
                );
            }
        }

        int progress = this.menu.getScaledProgress(ARROW_W);
        if (progress > 0) {
            blitTexture(graphics, TEXTURE, ARROW_X, ARROW_Y, 176, 14, progress, ARROW_H, TEX_W, TEX_H);
        }

        int tank = this.menu.getScaledTank(TANK_H);
        if (tank > 0) {
            blitTexture(
                    graphics,
                    TEXTURE,
                    TANK_X,
                    TANK_Y + (TANK_H - tank),
                    176,
                    31 + (TANK_H - tank),
                    TANK_W,
                    tank,
                    TEX_W,
                    TEX_H
            );
        }
    }

    @Override
    protected void renderExtraTooltips(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHoveringBox(TANK_X, TANK_Y, TANK_W, TANK_H, mouseX, mouseY)) {
            renderTooltipLines(
                    graphics,
                    mouseX,
                    mouseY,
                    Component.literal(this.menu.getTankAmount() + " / " + AdvancedFurnaceBlockEntity.TANK_CAPACITY + " mB")
            );
        }
    }
}