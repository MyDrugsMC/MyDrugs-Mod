package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;

/**
 * Pure, side-effect-free logic for the Psy Mixer soft-lock recovery rituals.
 *
 * <p>Kept free of any Minecraft types on purpose so it can be unit-tested on a plain JVM. The
 * Minecraft-coupled wrapper {@link PsyMixerRecoveryRitual} classifies {@code ItemStack}s into
 * {@link SlotKind}s and applies the outcome; everything decision-related lives here.
 *
 * <p>Design invariant: a valid recovery loadout is made only of a single drug plus four filler
 * items. {@link SlotKind#RECEPTACLE} and {@link SlotKind#WIRE} are explicitly rejected, so a
 * recovery route can never require the very item it is meant to recover.
 */
public final class RecoveryRitualLogic {
    /** Largest share of the success chance that experience levels alone can contribute. */
    public static final double MAX_XP_CHANCE = 0.50D;
    /** Success-chance bonus for a drug not in the explicit table. */
    public static final double DEFAULT_DRUG_BONUS = 0.15D;

    /** Recognised contents of one of the five Psy Mixer input slots. */
    public enum SlotKind {
        EMPTY,
        VINE,
        COPPER_INGOT,
        DRUG,
        RECEPTACLE,
        WIRE,
        OTHER
    }

    /** Which recovery a loadout resolves to. */
    public enum RecoveryKind {
        RECEPTACLE,
        WIRE
    }

    private RecoveryRitualLogic() {
    }

    /**
     * Classifies a five-slot loadout. Returns {@code null} when the slots are not a valid recovery
     * loadout (wrong counts, empty slots, or — deliberately — any slot holding the locked item).
     */
    public static @Nullable RecoveryKind classify(List<SlotKind> slots) {
        if (slots == null || slots.size() != 5) {
            return null;
        }
        int vines = 0;
        int copper = 0;
        int drugs = 0;
        for (SlotKind kind : slots) {
            switch (kind) {
                case VINE -> vines++;
                case COPPER_INGOT -> copper++;
                case DRUG -> drugs++;
                default -> {
                    // EMPTY, OTHER, and — critically — RECEPTACLE/WIRE make this not a recovery loadout.
                    return null;
                }
            }
        }
        if (drugs != 1) {
            return null;
        }
        if (vines == 4 && copper == 0) {
            return RecoveryKind.RECEPTACLE;
        }
        if (copper == 4 && vines == 0) {
            return RecoveryKind.WIRE;
        }
        return null;
    }

    /**
     * Success chance for a recovery ritual. Experience contributes from 0% at 0 levels up to
     * {@link #MAX_XP_CHANCE} at {@code maxLevels}; the drug bonus is added on top; the total is
     * clamped to [0, 1].
     */
    public static double successChance(int xpLevels, int maxLevels, double drugBonus) {
        if (maxLevels <= 0) {
            return clamp01(drugBonus);
        }
        int effectiveLevels = Math.max(0, Math.min(xpLevels, maxLevels));
        double xpChance = (double) effectiveLevels / maxLevels * MAX_XP_CHANCE;
        return clamp01(xpChance + drugBonus);
    }

    /** Experience levels consumed by a ritual: all the player has, capped at {@code maxLevels}. */
    public static int levelsConsumed(int xpLevels, int maxLevels) {
        return Math.max(0, Math.min(xpLevels, Math.max(0, maxLevels)));
    }

    /** Per-drug success-chance bonus. Unlisted drugs use {@link #DEFAULT_DRUG_BONUS}. */
    public static double drugBonus(@Nullable DrugId drug) {
        if (drug == null) {
            return DEFAULT_DRUG_BONUS;
        }
        return switch (drug) {
            case COFFEE -> 0.05D;
            case TOBACCO -> 0.10D;
            case WEED -> 0.15D;
            case ALCOHOL -> 0.25D;
            case HASH -> 0.30D;
            case COCAINE, CRACK -> 0.40D;
            case LSD -> 0.45D;
            case METH -> 0.50D;
            default -> DEFAULT_DRUG_BONUS;
        };
    }

    private static double clamp01(double value) {
        if (value < 0.0D) {
            return 0.0D;
        }
        return Math.min(value, 1.0D);
    }
}
