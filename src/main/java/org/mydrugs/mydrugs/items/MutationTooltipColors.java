package org.mydrugs.mydrugs.items;

import net.minecraft.ChatFormatting;

public final class MutationTooltipColors {
    private MutationTooltipColors() {
    }

    public static ChatFormatting value(float value) {
        if (value < 0.20F) return ChatFormatting.DARK_GRAY;
        if (value < 0.40F) return ChatFormatting.WHITE;
        if (value < 0.60F) return ChatFormatting.GREEN;
        if (value < 0.80F) return ChatFormatting.AQUA;
        return ChatFormatting.LIGHT_PURPLE;
    }

    public static ChatFormatting risk(float value) {
        if (value < 0.25F) return ChatFormatting.GREEN;
        if (value < 0.50F) return ChatFormatting.YELLOW;
        return ChatFormatting.RED;
    }
}
