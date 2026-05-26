package org.mydrugs.mydrugs.menu.client.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.menu.layout.*;

final class MachineTankRenderer {
    private MachineTankRenderer() {
    }

    static void drawAdvancedFurnaceTank(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            MachineGuiRenderer.TankFill fill
    ) {
        drawTankFill(
                draw,
                graphics,
                fill,
                AdvancedFurnaceLayout.TANK_X,
                AdvancedFurnaceLayout.TANK_Y,
                StandardTankLayout.INNER_X,
                StandardTankLayout.INNER_Y,
                StandardTankLayout.INNER_W,
                StandardTankLayout.INNER_H
        );
    }

    static void drawTankFill(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            MachineGuiRenderer.TankFill fill,
            int tankX,
            int tankY,
            int innerOffsetX,
            int innerOffsetY,
            int innerW,
            int innerH
    ) {
        if (isEmpty(fill)) {
            return;
        }

        if (fill.filledPixels() >= 0) {
            int color = fill.color() != 0 ? fill.color() : draw.getFluidColor(fill.fluid());
            if (fill.topLit()) {
                draw.drawTankFillTopLit(
                        graphics,
                        tankX,
                        tankY,
                        innerOffsetX,
                        innerOffsetY,
                        innerW,
                        innerH,
                        fill.filledPixels(),
                        color,
                        draw.lighten(color, 1.20f)
                );
            } else {
                draw.drawTankFillShaded(
                        graphics,
                        tankX,
                        tankY,
                        innerOffsetX,
                        innerOffsetY,
                        innerW,
                        innerH,
                        fill.filledPixels(),
                        color
                );
            }
            return;
        }

        if (fill.gasId() != null) {
            draw.drawGasTankPreview(
                    graphics,
                    fill.gasId(),
                    fill.amount(),
                    fill.capacity(),
                    tankX,
                    tankY,
                    innerOffsetX,
                    innerOffsetY,
                    innerW,
                    innerH
            );
            return;
        }

        draw.drawFluidTankPreview(
                graphics,
                fill.fluid(),
                (int) fill.amount(),
                (int) fill.capacity(),
                tankX,
                tankY,
                innerOffsetX,
                innerOffsetY,
                innerW,
                innerH
        );
    }

    static void drawCatalyticTank(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            int tankX,
            MachineGuiRenderer.TankFill fill
    ) {
        draw.drawTankFrame(
                graphics,
                tankX,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_W,
                CatalyticReformerLayout.TANK_H,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H
        );
        drawTankFill(
                draw,
                graphics,
                fill,
                tankX,
                CatalyticReformerLayout.TANK_Y,
                CatalyticReformerLayout.TANK_INNER_X_OFFSET,
                CatalyticReformerLayout.TANK_INNER_Y_OFFSET,
                CatalyticReformerLayout.TANK_INNER_W,
                CatalyticReformerLayout.TANK_INNER_H
        );
    }

    static void drawSteamCrackerTank(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            int tankX,
            MachineGuiRenderer.TankFill fill
    ) {
        draw.drawTankFrame(
                graphics,
                tankX,
                SteamCrackerLayout.TANK_Y,
                SteamCrackerLayout.TANK_W,
                SteamCrackerLayout.TANK_H,
                StandardTankLayout.INNER_X,
                StandardTankLayout.INNER_Y,
                StandardTankLayout.INNER_W,
                SteamCrackerLayout.TANK_INNER_H
        );
        drawTankFill(
                draw,
                graphics,
                fill,
                tankX,
                SteamCrackerLayout.TANK_Y,
                StandardTankLayout.INNER_X,
                StandardTankLayout.INNER_Y,
                StandardTankLayout.INNER_W,
                SteamCrackerLayout.TANK_INNER_H
        );
    }

    static void drawBtxTank(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            int tankX,
            int tankY,
            MachineGuiRenderer.TankFill fill
    ) {
        draw.drawTankFrame(
                graphics,
                tankX,
                tankY,
                BTXFractionationTowerLayout.TANK_W,
                BTXFractionationTowerLayout.TANK_H,
                BTXFractionationTowerLayout.TANK_INNER_X_OFFSET,
                BTXFractionationTowerLayout.TANK_INNER_Y_OFFSET,
                BTXFractionationTowerLayout.TANK_INNER_W,
                BTXFractionationTowerLayout.TANK_INNER_H
        );
        drawTankFill(
                draw,
                graphics,
                fill,
                tankX,
                tankY,
                BTXFractionationTowerLayout.TANK_INNER_X_OFFSET,
                BTXFractionationTowerLayout.TANK_INNER_Y_OFFSET,
                BTXFractionationTowerLayout.TANK_INNER_W,
                BTXFractionationTowerLayout.TANK_INNER_H
        );
    }

    private static boolean isEmpty(MachineGuiRenderer.TankFill fill) {
        return fill == null
                || ((fill.fluid() == null || fill.fluid() == Fluids.EMPTY)
                && fill.gasId() == null
                && fill.color() == 0);
    }
}
