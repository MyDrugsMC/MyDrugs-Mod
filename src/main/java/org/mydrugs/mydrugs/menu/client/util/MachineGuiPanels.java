package org.mydrugs.mydrugs.menu.client.util;

import net.minecraft.client.gui.GuiGraphics;
import org.mydrugs.mydrugs.menu.layout.AdvancedFurnaceLayout;
import org.mydrugs.mydrugs.menu.layout.StandardInventoryLayout;

final class MachineGuiPanels {
    private MachineGuiPanels() {
    }

    static void drawAdvancedFurnaceInventoryPanels(AbstractMachineDrawMethods draw, GuiGraphics graphics) {
        draw.drawPanel(
                graphics,
                AdvancedFurnaceLayout.PLAYER_INV_X,
                AdvancedFurnaceLayout.PLAYER_INV_Y,
                StandardInventoryLayout.PLAYER_INV_PANEL_W,
                StandardInventoryLayout.PLAYER_INV_PANEL_H,
                0xFF2A2D33,
                0xFF5C616B,
                0xFF0E1014
        );

        draw.drawPanel(
                graphics,
                AdvancedFurnaceLayout.PLAYER_INV_X,
                StandardInventoryLayout.hotbarPanelY(AdvancedFurnaceLayout.PLAYER_INV_Y),
                StandardInventoryLayout.HOTBAR_PANEL_W,
                StandardInventoryLayout.HOTBAR_PANEL_H,
                0xFF2A2D33,
                0xFF5C616B,
                0xFF0E1014
        );
    }

    static void drawStandardInventoryPanels(
            AbstractMachineDrawMethods draw,
            GuiGraphics graphics,
            int x,
            int y,
            int fillColor,
            int lightBorderColor,
            int darkBorderColor
    ) {
        draw.drawPanel(
                graphics,
                x,
                y,
                StandardInventoryLayout.PLAYER_INV_PANEL_W,
                StandardInventoryLayout.PLAYER_INV_PANEL_H,
                fillColor,
                lightBorderColor,
                darkBorderColor
        );

        draw.drawPanel(
                graphics,
                x,
                StandardInventoryLayout.hotbarPanelY(y),
                StandardInventoryLayout.HOTBAR_PANEL_W,
                StandardInventoryLayout.HOTBAR_PANEL_H,
                fillColor,
                lightBorderColor,
                darkBorderColor
        );
    }
}
