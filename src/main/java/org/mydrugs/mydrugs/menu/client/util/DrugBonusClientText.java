package org.mydrugs.mydrugs.menu.client.util;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.client.AddictionClientState;

public final class DrugBonusClientText {
    private static final int GOOD = 0xFF76E08A;
    private static final int MUTED = 0xFFB58A96;
    private static final int PSYCHEDELIC = 0xFFB98CFF;

    private DrugBonusClientText() {
    }

    public static void drawManualWorkBonus(GuiGraphics graphics, Font font, int x, int y) {
        float manual = effect(EffectType.MANUAL_WORK_SPEED);
        float focus = effect(EffectType.FOCUS);
        float precision = effect(EffectType.PRECISION);
        float adrenaline = effect(EffectType.ADRENALINE_SURGE);
        float bonus = manual + focus * 0.35F + precision * 0.12F + adrenaline * 0.65F;
        if (bonus <= 0.01F) {
            return;
        }

        Component label = Component.translatable(manualWorkKey(manual, precision, adrenaline), Math.round(bonus * 100.0F));
        graphics.drawString(font, label, x, y, GOOD, false);
    }

    public static void drawRitualBonuses(GuiGraphics graphics, Font font, int x, int y, int width) {
        Component first = primaryRitualBonus();
        if (first == null) {
            return;
        }

        graphics.drawString(font, first, x, y, first.getString().contains("Insight") ? PSYCHEDELIC : GOOD, false);

        Component second = secondaryRitualBonus();
        if (second != null) {
            graphics.drawString(font, fit(font, second, width), x, y + 10, MUTED, false);
        }
    }

    private static Component primaryRitualBonus() {
        float psychedelicFocus = effect(EffectType.RITUAL_FOCUS);
        if (psychedelicFocus >= 1.5F) {
            return Component.translatable("screen.mydrugs.bonus.psychedelic_insight");
        }

        if (effect(EffectType.RITUAL_STABILITY) > 0.01F) {
            return Component.translatable("screen.mydrugs.bonus.cannabis_calm");
        }

        if (effect(EffectType.PRECISION) > 0.01F || psychedelicFocus > 0.01F) {
            return Component.translatable("screen.mydrugs.bonus.ritual_focus");
        }

        float manual = effect(EffectType.MANUAL_WORK_SPEED);
        if (manual > 0.01F) {
            return Component.translatable(manualWorkKey(manual, 0.0F, effect(EffectType.ADRENALINE_SURGE)), Math.round(manual * 100.0F));
        }

        return null;
    }

    private static Component secondaryRitualBonus() {
        if (effect(EffectType.RITUAL_STABILITY) > 0.01F && effect(EffectType.RITUAL_FOCUS) > 0.01F) {
            return Component.translatable("screen.mydrugs.bonus.cannabis_calm");
        }
        if (effect(EffectType.PRECISION) > 0.01F && effect(EffectType.RITUAL_STABILITY) > 0.01F) {
            return Component.translatable("screen.mydrugs.bonus.ritual_focus");
        }
        return null;
    }

    private static String manualWorkKey(float manual, float precision, float adrenaline) {
        if (adrenaline > 0.05F) {
            return "screen.mydrugs.bonus.adrenaline_hands";
        }
        if (manual >= 0.45F) {
            return "screen.mydrugs.bonus.stimulant_rush";
        }
        if (precision > 0.01F && manual <= 0.01F) {
            return "screen.mydrugs.bonus.tobacco_precision";
        }
        return "screen.mydrugs.bonus.coffee_focus";
    }

    private static Component fit(Font font, Component component, int width) {
        return font.width(component) <= width
                ? component
                : Component.literal(font.plainSubstrByWidth(component.getString(), width - font.width("...")) + "...");
    }

    private static float effect(EffectType type) {
        return AddictionClientState.getEffectIntensity(type);
    }
}
