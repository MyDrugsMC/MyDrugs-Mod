package org.mydrugs.mydrugs.effects.addiction.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.effects.addiction.client.AddictionClientState;
import org.mydrugs.mydrugs.effects.addiction.config.SymptomFlags;
import org.mydrugs.mydrugs.effects.addiction.dose.DosePath;
import org.mydrugs.mydrugs.effects.addiction.dose.DoseState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AddictionHudRenderer {
    private static final int PANEL_X = 10;
    private static final int PANEL_Y = 10;
    private static final int PANEL_WIDTH = 166;
    private static final int PANEL_PADDING = 6;
    private static final int BAR_WIDTH = PANEL_WIDTH - (PANEL_PADDING * 2);
    private static final int BAR_HEIGHT = 6;
    private static final int BADGE_HEIGHT = 12;
    private static final int BADGE_GAP = 4;
    private static final int ROW_GAP = 5;

    private AddictionHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null
                || mc.options.hideGui
                || !Config.CLIENT.showAddictionHud.get()
                || !AddictionClientState.shouldRenderHud()) {
            return;
        }

        Font font = mc.font;
        List<Badge> statusBadges = new ArrayList<>();
        boolean compact = Config.CLIENT.compactAddictionHud.get();
        List<Badge> recoveryBadges = compact ? List.of() : buildRecoveryBadges();
        List<Badge> symptomBadges = compact ? List.of() : buildSymptomBadges();

        Badge dangerBadge = buildDangerBadge();
        if (dangerBadge != null) {
            statusBadges.add(dangerBadge);
        }
        if (AddictionClientState.isSleepBlocked()) {
            statusBadges.add(new Badge(Component.translatable("mydrugs.hud.badge.insomnia"), 0xAA5B4A82, 0xFFE4DAFF));
        }
        if (AddictionClientState.hasOverdoseTimer()) {
            statusBadges.add(new Badge(Component.translatable("mydrugs.hud.badge.critical"), 0xAAC0392B, 0xFFFFE5E0));
        }

        Component sourceLine = buildSourceLine();
        int totalHeight = PANEL_PADDING;
        totalHeight += 10 + BAR_HEIGHT + ROW_GAP;
        totalHeight += 10 + BAR_HEIGHT + ROW_GAP;
        totalHeight += 10;

        if (!statusBadges.isEmpty()) {
            totalHeight += measureBadgeRows(font, statusBadges, BAR_WIDTH) * BADGE_HEIGHT;
            totalHeight += ROW_GAP;
        }
        if (!recoveryBadges.isEmpty()) {
            totalHeight += measureBadgeRows(font, recoveryBadges, BAR_WIDTH) * BADGE_HEIGHT;
            totalHeight += ROW_GAP;
        }
        if (!symptomBadges.isEmpty()) {
            totalHeight += measureBadgeRows(font, symptomBadges, BAR_WIDTH) * BADGE_HEIGHT;
            totalHeight += ROW_GAP;
        }
        totalHeight += PANEL_PADDING - ROW_GAP;

        int x = PANEL_X;
        int y = PANEL_Y;
        int contentX = x + PANEL_PADDING;
        int contentY = y + PANEL_PADDING;

        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + totalHeight, 0x9A0C0C10);
        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + 1, 0x66FFFFFF);
        guiGraphics.fill(x, y + totalHeight - 1, x + PANEL_WIDTH, y + totalHeight, 0x44000000);
        guiGraphics.fill(x, y, x + 1, y + totalHeight, 0x44000000);
        guiGraphics.fill(x + PANEL_WIDTH - 1, y, x + PANEL_WIDTH, y + totalHeight, 0x44000000);

        drawMeter(guiGraphics, font, Component.translatable("mydrugs.hud.withdrawal"), contentX, contentY,
                AddictionClientState.globalSeverity, 0xAA6E2323, 0xFFD65E5E);
        contentY += 10 + BAR_HEIGHT + ROW_GAP;

        drawMeter(guiGraphics, font, Component.translatable("mydrugs.hud.stress"), contentX, contentY,
                AddictionClientState.stressLevel, 0xAA2D3C66, 0xFF8AA8FF);
        contentY += 10 + BAR_HEIGHT + ROW_GAP;

        if (AddictionClientState.hasDangerousDoseState()) {
            guiGraphics.drawString(font, sourceLine, contentX, contentY, 0xFFF0F0F0, false);
            contentY += 10 + ROW_GAP;
        }

        if (!statusBadges.isEmpty()) {
            contentY = drawBadges(guiGraphics, font, statusBadges, contentX, contentY, BAR_WIDTH);
            contentY += ROW_GAP;
        }
        if (!recoveryBadges.isEmpty()) {
            contentY = drawBadges(guiGraphics, font, recoveryBadges, contentX, contentY, BAR_WIDTH);
            contentY += ROW_GAP;
        }
        if (!symptomBadges.isEmpty()) {
            drawBadges(guiGraphics, font, symptomBadges, contentX, contentY, BAR_WIDTH);
        }
    }

    private static void drawMeter(GuiGraphics guiGraphics,
                                  Font font,
                                  Component label,
                                  int x,
                                  int y,
                                  float value,
                                  int backgroundColor,
                                  int fillColor) {
        int clampedFill = Math.round(Mth.clamp(value, 0.0F, 1.0F) * BAR_WIDTH);
        guiGraphics.drawString(font, label, x, y, 0xFFEDEDED, false);
        int barY = y + 10;
        guiGraphics.fill(x, barY, x + BAR_WIDTH, barY + BAR_HEIGHT, 0x66000000);
        if (clampedFill > 0) {
            guiGraphics.fill(x, barY, x + clampedFill, barY + BAR_HEIGHT, fillColor);
        }
        guiGraphics.fill(x, barY, x + BAR_WIDTH, barY + 1, 0x55FFFFFF);
        guiGraphics.fill(x, barY + BAR_HEIGHT - 1, x + BAR_WIDTH, barY + BAR_HEIGHT, 0x55000000);
        guiGraphics.fill(x, barY, x + 1, barY + BAR_HEIGHT, backgroundColor);
        guiGraphics.fill(x + BAR_WIDTH - 1, barY, x + BAR_WIDTH, barY + BAR_HEIGHT, backgroundColor);
    }

    private static Component buildSourceLine() {
        DrugId dominantDrug = AddictionClientState.getDominantDrugIdEnum();
        DrugCategory dominantCategory = AddictionClientState.getDominantCategoryEnum();

        if (dominantDrug != null) {
            return Component.translatable(
                    "mydrugs.hud.source.value",
                    Component.translatable(drugKey(dominantDrug)),
                    Component.translatable(categoryKey(dominantCategory))
            );
        }

        return Component.translatable(
                "mydrugs.hud.source.category_only",
                Component.translatable(categoryKey(dominantCategory))
        );
    }

    private static @Nullable Badge buildDangerBadge() {
        DrugCategory doseCategory = AddictionClientState.getDisplayedDoseCategory();
        DosePath path = DosePath.of(doseCategory);
        if (path == DosePath.NONE && !AddictionClientState.hasOverdoseTimer()) {
            return null;
        }

        DoseState state = AddictionClientState.getDominantDoseState();
        if (state == DoseState.NORMAL && !AddictionClientState.hasAnyDose() && !AddictionClientState.hasOverdoseTimer()) {
            return null;
        }

        Component text = Component.translatable("mydrugs.hud.badge.dose", Component.translatable(doseStateKey(path, state)));
        return switch (state) {
            case OVERDOSE, ETHYLIC_COMA -> new Badge(text, 0xAAC53B2D, 0xFFFFECE7);
            case VERY_HIGH, VERY_DRUNK -> new Badge(text, 0xAAA45B1F, 0xFFFFF1D8);
            case HIGH, DRUNK -> new Badge(text, 0xAA7A6522, 0xFFFFF6E0);
            case NORMAL -> new Badge(text, 0xAA3B434E, 0xFFE9EEF6);
        };
    }

    private static List<Badge> buildRecoveryBadges() {
        List<Badge> badges = new ArrayList<>();
        if (AddictionClientState.isInSafeZone()) {
            badges.add(new Badge(Component.translatable("mydrugs.hud.badge.safe_zone"), 0xAA224B34, 0xFFE7FFF0));
        }
        if (AddictionClientState.hasDiaryCalm()) {
            badges.add(new Badge(Component.translatable("mydrugs.hud.badge.diary"), 0xAA2F5A4B, 0xFFE8FFF7));
        }
        if (AddictionClientState.hasCalmingMixture()) {
            badges.add(new Badge(Component.translatable("mydrugs.hud.badge.calming_mixture"), 0xAA45604A, 0xFFEFFFF0));
        }
        if (AddictionClientState.hasHeadphonesCalm()) {
            badges.add(new Badge(Component.translatable("mydrugs.hud.badge.headphones"), 0xAA254B64, 0xFFE8F7FF));
        }
        if (AddictionClientState.hasSleepBonus()) {
            badges.add(new Badge(Component.translatable("mydrugs.hud.badge.sleep_bonus"), 0xAA3F4A7C, 0xFFF0F2FF));
        }
        return badges;
    }

    private static List<Badge> buildSymptomBadges() {
        List<Badge> badges = new ArrayList<>();
        addSymptomBadge(badges, SymptomFlags.CONFUSION, "confusion");
        addSymptomBadge(badges, SymptomFlags.FRAGILITY, "fragility");
        addSymptomBadge(badges, SymptomFlags.VISION, "vision");
        addSymptomBadge(badges, SymptomFlags.HALLUCINATION, "hallucination");
        addSymptomBadge(badges, SymptomFlags.STRESS, "stress");
        addSymptomBadge(badges, SymptomFlags.DISSOCIATION, "dissociation");
        addSymptomBadge(badges, SymptomFlags.FATIGUE, "fatigue");
        addSymptomBadge(badges, SymptomFlags.INTRUSIVE_THOUGHTS, "intrusive_thoughts");
        if (AddictionClientState.hasInsomniaSymptom()) {
            badges.add(new Badge(Component.translatable("mydrugs.hud.symptom.insomnia"), 0xAA4D4F69, 0xFFF1F1FF));
        }
        return badges;
    }

    private static void addSymptomBadge(List<Badge> badges, int symptomFlag, String keySuffix) {
        if (AddictionClientState.has(symptomFlag)) {
            badges.add(new Badge(Component.translatable("mydrugs.hud.symptom." + keySuffix), 0xAA44464F, 0xFFF2F2F2));
        }
    }

    private static int measureBadgeRows(Font font, List<Badge> badges, int maxWidth) {
        int rows = 1;
        int lineWidth = 0;

        for (Badge badge : badges) {
            int badgeWidth = badgeWidth(font, badge);
            if (lineWidth > 0 && lineWidth + BADGE_GAP + badgeWidth > maxWidth) {
                rows++;
                lineWidth = badgeWidth;
            } else {
                lineWidth += lineWidth == 0 ? badgeWidth : BADGE_GAP + badgeWidth;
            }
        }

        return rows;
    }

    private static int drawBadges(GuiGraphics guiGraphics,
                                  Font font,
                                  List<Badge> badges,
                                  int startX,
                                  int startY,
                                  int maxWidth) {
        int x = startX;
        int y = startY;
        int lineWidth = 0;

        for (Badge badge : badges) {
            int badgeWidth = badgeWidth(font, badge);
            if (lineWidth > 0 && lineWidth + BADGE_GAP + badgeWidth > maxWidth) {
                y += BADGE_HEIGHT;
                x = startX;
                lineWidth = 0;
            }

            drawBadge(guiGraphics, font, badge, x, y, badgeWidth);
            x += badgeWidth + BADGE_GAP;
            lineWidth += lineWidth == 0 ? badgeWidth : badgeWidth + BADGE_GAP;
        }

        return y + BADGE_HEIGHT;
    }

    private static void drawBadge(GuiGraphics guiGraphics, Font font, Badge badge, int x, int y, int width) {
        guiGraphics.fill(x, y, x + width, y + BADGE_HEIGHT - 1, badge.backgroundColor());
        guiGraphics.fill(x, y, x + width, y + 1, 0x30FFFFFF);
        guiGraphics.fill(x, y + BADGE_HEIGHT - 2, x + width, y + BADGE_HEIGHT - 1, 0x40000000);
        guiGraphics.drawString(font, badge.text(), x + 4, y + 2, badge.textColor(), false);
    }

    private static int badgeWidth(Font font, Badge badge) {
        return font.width(badge.text()) + 8;
    }

    private static String drugKey(DrugId id) {
        return "mydrugs.addiction.drug." + id.name().toLowerCase(Locale.ROOT);
    }

    private static String categoryKey(DrugCategory category) {
        return "mydrugs.addiction.category." + category.name().toLowerCase(Locale.ROOT);
    }

    private static String doseStateKey(DosePath path, DoseState state) {
        String suffix = switch (state) {
            case NORMAL -> "normal";
            case HIGH -> "high";
            case VERY_HIGH -> "very_high";
            case OVERDOSE -> "overdose_risk";
            case DRUNK -> "drunk";
            case VERY_DRUNK -> "very_drunk";
            case ETHYLIC_COMA -> "ethylic_coma";
        };
        return path == DosePath.ALCOHOL
                ? "mydrugs.hud.dose.alcohol." + suffix
                : "mydrugs.hud.dose.drug." + suffix;
    }

    private record Badge(Component text, int backgroundColor, int textColor) {
    }
}
