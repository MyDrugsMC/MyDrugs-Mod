package org.mydrugs.mydrugs.client.psy_mixer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualAction;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualEngine;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualJudgement;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualQuality;

public final class PsyMixerRitualOverlay {
    private static final int PANEL = 0xAA12070A;
    private static final int LINE = 0xCC5C3344;
    private static final int TEXT = 0xFFE8D6C2;
    private static final int MUTED = 0xFFB58A96;
    private static final int GOOD = 0xFF76E08A;
    private static final int WARN = 0xFFFFD060;
    private static final int BAD = 0xFFFF5A77;

    private PsyMixerRitualOverlay() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !PsyMixerRitualClientState.isActive()) {
            return;
        }

        Font font = mc.font;
        int width = mc.getWindow().getGuiScaledWidth();
        int x = width / 2 - 96;
        int y = 18;
        graphics.fill(x, y, x + 192, y + 72, PANEL);
        graphics.fill(x, y, x + 192, y + 1, LINE);
        graphics.fill(x, y + 71, x + 192, y + 72, LINE);

        String formula = PsyMixerRitualClientState.formulaName();
        if (font.width(formula) > 176) {
            formula = font.plainSubstrByWidth(formula, 173) + "...";
        }
        graphics.drawString(font, Component.translatable("screen.mydrugs.psy_mixer.formula", formula), x + 8, y + 7, TEXT, false);
        graphics.drawString(font, Component.translatable("screen.mydrugs.psy_mixer.current_action", PsyMixerRitualClientState.actionIndex() + 1, Math.max(1, PsyMixerRitualClientState.actionCount())), x + 8, y + 18, MUTED, false);
        graphics.drawString(font, Component.translatable(PsyMixerRitualClientState.action().promptKey()), x + 8, y + 30, TEXT, false);
        graphics.drawString(font, Component.translatable(PsyMixerRitualClientState.action().hintKey()), x + 8, y + 41, MUTED, false);

        PsyMixerRitualQuality quality = PsyMixerRitualClientState.quality();
        graphics.drawString(font, Component.translatable("screen.mydrugs.psy_mixer.quality_preview", Component.translatable(quality.translationKey())), x + 8, y + 54, qualityColor(quality), false);
        graphics.drawString(font, Component.translatable("screen.mydrugs.psy_mixer.mistakes", PsyMixerRitualClientState.mistakes(), PsyMixerRitualClientState.maxMistakes()), x + 112, y + 54, PsyMixerRitualClientState.mistakes() > 0 ? WARN : GOOD, false);

        if (PsyMixerRitualClientState.feedbackTicks() > 0 && PsyMixerRitualClientState.lastJudgement() != PsyMixerRitualJudgement.NONE) {
            graphics.drawString(font, Component.translatable(PsyMixerRitualClientState.lastJudgement().screenKey()), x + 138, y + 18, judgementColor(PsyMixerRitualClientState.lastJudgement()), false);
        }

        if (PsyMixerRitualClientState.action() == PsyMixerRitualAction.TIMING_RING) {
            drawTimingRing(graphics, x + 168, y + 37);
        } else {
            int barX = x + 112;
            int barY = y + 43;
            int fill = Math.round(PsyMixerRitualClientState.actionProgress() * 64.0F);
            graphics.fill(barX, barY, barX + 64, barY + 4, 0xAA21131A);
            graphics.fill(barX, barY, barX + fill, barY + 4, WARN);
        }
    }

    private static void drawTimingRing(GuiGraphics graphics, int cx, int cy) {
        for (int i = 0; i < 40; i++) {
            double angle = i * Math.PI * 2.0 / 40.0;
            int dx = (int) Math.round(Math.cos(angle) * 16.0);
            int dy = (int) Math.round(Math.sin(angle) * 16.0);
            graphics.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, 0xFF6A3A4A);
        }
        float window = PsyMixerRitualClientState.timingWindow();
        for (int i = 0; i < 96; i++) {
            float p = i / 96.0F;
            if (PsyMixerRitualEngine.nearestGoldenZoneDistance(p, PsyMixerRitualClientState.targetPhase()) <= window / 2.0F) {
                double angle = -Math.PI / 2.0 + p * Math.PI * 2.0;
                int dx = (int) Math.round(Math.cos(angle) * 19.0);
                int dy = (int) Math.round(Math.sin(angle) * 19.0);
                graphics.fill(cx + dx - 1, cy + dy - 1, cx + dx + 2, cy + dy + 2, WARN);
            }
        }
        float phase = PsyMixerRitualClientState.phase();
        double angle = -Math.PI / 2.0 + phase * Math.PI * 2.0;
        int dx = (int) Math.round(Math.cos(angle) * 19.0);
        int dy = (int) Math.round(Math.sin(angle) * 19.0);
        graphics.fill(cx + dx - 2, cy + dy - 2, cx + dx + 3, cy + dy + 3, GOOD);
    }

    private static int qualityColor(PsyMixerRitualQuality quality) {
        return switch (quality) {
            case MASTERWORK -> 0xFF9BFFFF;
            case PERFECT -> GOOD;
            case BASE -> TEXT;
            case CRUDE -> WARN;
        };
    }

    private static int judgementColor(PsyMixerRitualJudgement judgement) {
        return switch (judgement) {
            case PERFECT -> 0xFF9BFFFF;
            case GREAT, GOOD -> GOOD;
            case NEAR -> WARN;
            case MISS -> BAD;
            default -> TEXT;
        };
    }
}
