package org.mydrugs.mydrugs.client.compat.gas;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class GasIngredientRenderer implements IIngredientRenderer<GasJeiIngredient> {
    private static final int BORDER = 0xFF657084;
    private static final int BACKGROUND = 0xFF0C1016;

    @Override
    public void render(GuiGraphics guiGraphics, GasJeiIngredient ingredient) {
        int x = 0;
        int y = 0;
        int w = 16;
        int h = 16;

        guiGraphics.fill(x, y, x + w, y + h, BORDER);
        guiGraphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, BACKGROUND);

        int innerX = x + 2;
        int innerY = y + 2;
        int innerW = w - 4;
        int innerH = h - 4;

        float ratio = Math.min(1.0f, ingredient.amount() / 1000.0f);
        ratio = Math.max(0.18f, ratio);

        int fillHeight = Mth.clamp((int) (innerH * ratio), 1, innerH);

        int gasColor = GasJeiUtil.color(ingredient.id());

        // top-down fill, so it looks like "light gas"
        guiGraphics.fill(innerX, innerY, innerX + innerW, innerY + fillHeight, gasColor);

        // subtle bands
        for (int yy = innerY + 1; yy < innerY + fillHeight; yy += 3) {
            guiGraphics.fill(innerX, yy, innerX + innerW, Math.min(yy + 1, innerY + fillHeight), 0x30FFFFFF);
        }

        // little top highlight
        guiGraphics.fill(innerX, innerY, innerX + innerW, innerY + 1, 0x55FFFFFF);

        // tiny amount marker
        var font = Minecraft.getInstance().font;
        String text = ingredient.amount() >= 1000 ? "1k+" : Long.toString(ingredient.amount());
        if (font.width(text) <= 14) {
            guiGraphics.drawString(font, text, 8 - font.width(text) / 2, 17 - font.lineHeight, 0xFFE5E7EB, false);
        }
    }

    @Override
    public List<Component> getTooltip(GasJeiIngredient ingredient, TooltipFlag tooltipFlag) {
        return GasJeiUtil.tooltip(ingredient, tooltipFlag.isAdvanced());
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }
}