package org.mydrugs.mydrugs.menu.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.mydrugs.mydrugs.menu.layout.AdvancedMixingVatLayout;
import org.mydrugs.mydrugs.menu.layout.DistillerLayout;
import org.mydrugs.mydrugs.menu.layout.SieveLayout;

final class MachineGuiWidgets {
    private MachineGuiWidgets() {
    }

    static void drawSlotCount(AbstractMachineDrawMethods draw, GuiGraphics graphics, int slotX, int slotY, int count) {
        if (count <= 1) {
            return;
        }
        graphics.drawString(Minecraft.getInstance().font, "x" + count, draw.guiX(slotX + 9), draw.guiY(slotY + 10), 0xFFF0D57A, false);
    }

    static int[] advancedMixingVatItemX() {
        return new int[]{
                AdvancedMixingVatLayout.ITEM_0_X,
                AdvancedMixingVatLayout.ITEM_1_X,
                AdvancedMixingVatLayout.ITEM_2_X,
                AdvancedMixingVatLayout.ITEM_3_X
        };
    }

    static int[] advancedMixingVatItemY() {
        return new int[]{
                AdvancedMixingVatLayout.ITEM_0_Y,
                AdvancedMixingVatLayout.ITEM_1_Y,
                AdvancedMixingVatLayout.ITEM_2_Y,
                AdvancedMixingVatLayout.ITEM_3_Y
        };
    }

    static int[] advancedMixingVatTankX() {
        return new int[]{
                AdvancedMixingVatLayout.TANK_A_X,
                AdvancedMixingVatLayout.TANK_B_X,
                AdvancedMixingVatLayout.TANK_C_X,
                AdvancedMixingVatLayout.GAS_X,
                AdvancedMixingVatLayout.OUTPUT_X
        };
    }

    static int[] advancedMixingVatTankSlotX() {
        return new int[]{
                AdvancedMixingVatLayout.TANK_A_SLOT_X,
                AdvancedMixingVatLayout.TANK_B_SLOT_X,
                AdvancedMixingVatLayout.TANK_C_SLOT_X,
                AdvancedMixingVatLayout.GAS_SLOT_X,
                AdvancedMixingVatLayout.OUTPUT_SLOT_X
        };
    }

    static void drawDistillerReactor(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            boolean hovered,
            boolean working,
            boolean boosted
    ) {
        int x = DistillerLayout.RUN_BUTTON_X;
        int y = DistillerLayout.RUN_BUTTON_Y;
        int cx = x + DistillerLayout.RUN_BUTTON_SIZE / 2;
        int cy = y + DistillerLayout.RUN_BUTTON_SIZE / 2;

        if (hovered) {
            graphics.fill(draw.guiX(x + 2), draw.guiY(y + 2), draw.guiX(x + DistillerLayout.RUN_BUTTON_SIZE - 2), draw.guiY(y + DistillerLayout.RUN_BUTTON_SIZE - 2), 0x16FFFFFF);
        }

        draw.drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS + 2, 0xFF818793);
        draw.drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS, 0xFF20242B);
        draw.drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS - 3, 0xFF9FA7B4);
        draw.drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_OUTER_RADIUS - 5, 0xFF3E4652);

        int coreColor = boosted ? 0xFF6FD6FF : working ? 0xFFE8E8E8 : 0xFF90959E;
        draw.drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_INNER_RADIUS, 0xFF2D333B);
        draw.drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_CORE_RADIUS, coreColor);

        if (boosted) {
            draw.drawCircle(graphics, cx, cy, DistillerLayout.REACTOR_CORE_RADIUS + 3, 0x336FD6FF);
        }
    }

    static void drawSieveWidget(AbstractMachineDrawMethods draw, GuiGraphics graphics, int knobCenterY) {
        int trackLeft = draw.guiX(SieveLayout.WIDGET_X);
        int trackTop = draw.guiY(SieveLayout.WIDGET_Y);

        graphics.fill(trackLeft - 2, trackTop - 2, trackLeft + SieveLayout.WIDGET_W + 2, trackTop + SieveLayout.WIDGET_H + 2, 0xFF5A5A5A);
        graphics.fill(trackLeft - 1, trackTop - 1, trackLeft + SieveLayout.WIDGET_W + 1, trackTop + SieveLayout.WIDGET_H + 1, 0xFF161616);
        graphics.fill(trackLeft + 7, trackTop + 2, trackLeft + 11, trackTop + SieveLayout.WIDGET_H - 2, 0xFF090909);
        graphics.fill(trackLeft + 5, trackTop + 1, trackLeft + 13, trackTop + 3, 0xFF727272);
        graphics.fill(trackLeft + 5, trackTop + SieveLayout.WIDGET_H - 3, trackLeft + 13, trackTop + SieveLayout.WIDGET_H - 1, 0xFF0E0E0E);

        int centerX = SieveLayout.WIDGET_X + SieveLayout.WIDGET_W / 2;
        draw.drawCircle(graphics, centerX, knobCenterY, 6, 0xFFBABABA);
        draw.drawCircle(graphics, centerX, knobCenterY, 5, 0xFF3B3B3B);
        draw.drawCircle(graphics, centerX - 1, knobCenterY - 1, 1, 0xFFE8E8E8);
    }
}
