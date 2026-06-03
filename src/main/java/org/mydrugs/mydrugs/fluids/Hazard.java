package org.mydrugs.mydrugs.fluids;

import net.minecraft.ChatFormatting;

/**
 * Shared hazard classification for fluids and gases. Display/metadata only; no gameplay effect yet.
 */
public enum Hazard {
    SAFE,
    IRRITANT,
    TOXIC,
    CORROSIVE,
    FLAMMABLE;

    public String translationKey() {
        return "hazard.mydrugs." + name().toLowerCase(java.util.Locale.ROOT);
    }

    public ChatFormatting color() {
        return switch (this) {
            case SAFE -> ChatFormatting.GRAY;
            case IRRITANT -> ChatFormatting.YELLOW;
            case TOXIC -> ChatFormatting.DARK_GREEN;
            case CORROSIVE -> ChatFormatting.DARK_AQUA;
            case FLAMMABLE -> ChatFormatting.GOLD;
        };
    }
}
