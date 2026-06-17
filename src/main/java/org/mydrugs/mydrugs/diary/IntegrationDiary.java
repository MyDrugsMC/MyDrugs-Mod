package org.mydrugs.mydrugs.diary;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.core.drug.DrugId;

/**
 * Narrative diary beats for the integration arc and the Inner Dimension (Phase H.2).
 *
 * Each helper appends one automatic {@link DiaryEntry} to the player's diary using the same
 * append pattern as {@link org.mydrugs.mydrugs.progression.PsyKnowledgeManager}: small, deterministic
 * snippets the player can re-read in the personal diary to retrace the integration arc.
 */
public final class IntegrationDiary {
    private IntegrationDiary() {
    }

    public static void firstEligible(ServerPlayer player, DrugId drug) {
        append(player, drug, "integration.eligible",
                "Something in me has settled. I survived " + drugName(drug) +
                        ". For the first time, I can imagine being free of it without pretending I never used it. " +
                        "I will go to the Resonator when I am ready.");
    }

    public static void integrated(ServerPlayer player, DrugId drug) {
        append(player, drug, "integration.integrated",
                "I integrated " + drugName(drug) + " today. The craving is not gone, but it is no longer in charge. " +
                        "Something it gave me, the part that was useful, is mine to keep now. The body still remembers.");
    }

    public static void firstIntegrationCore(ServerPlayer player) {
        append(player, DrugId.COFFEE, "integration.core_first",
                "Coffee pushed me to the edge of useful speed, then recovery made me account for it. " +
                        "The core it left behind feels like a way to bind readiness into something permanent.");
    }

    public static void dreamAligned(ServerPlayer player) {
        append(player, DrugId.LSD, "dimension.dream_aligned",
                "The dream has coordinates now. It still asks for integration, not escape.");
    }

    public static void recoveryResonance(ServerPlayer player) {
        append(player, null, "integration.recovery_resonance",
                "The room held. The craving had somewhere to go besides back into me.");
    }

    public static boolean firstDimensionEntry(ServerPlayer player) {
        return appendGlobal(player, null, "dimension.first_entry",
                "I stepped into the inner world. The Self Anchor waits at the center, and every path away from it " +
                        "belongs to something I have integrated. I should begin there.");
    }

    public static void dimensionExpanded(ServerPlayer player, DrugId drug) {
        append(player, drug, "dimension.expanded",
                "The inner world grew today, fed by " + drugName(drug) + ". A new ring of ground I never put there. " +
                        "I am learning that what I make peace with becomes the world I get to live in.");
    }

    public static boolean firstRegionDiscovered(ServerPlayer player, DrugId drug) {
        return appendGlobal(player, drug, "dimension.first_region",
                "The " + drugName(drug) + " region is awake now. A path from the Self Anchor points toward its " +
                        "landmark. I should follow it and see what the place asks of me.");
    }

    public static boolean firstTrialStarted(ServerPlayer player, DrugId drug) {
        return appendGlobal(player, drug, "dimension.first_trial_started",
                "The landmark is not decoration. It is waiting for an answer made through action, not explanation. " +
                        "I should read its shape carefully and respond without rushing.");
    }

    public static boolean trialCompleted(ServerPlayer player, DrugId drug) {
        return append(player, drug, "dimension.trial_completed",
                "The " + drugName(drug) + " trial answered me. Its sigil should be awake at the Self Anchor now. " +
                        "I need to return to the center and see what changed.");
    }

    public static boolean firstSigilAwakened(ServerPlayer player, DrugId drug) {
        return appendGlobal(player, drug, "dimension.first_sigil",
                "The first sigil is lit. The ring around the Self Anchor is a map of work completed, not merely " +
                        "places discovered. Other sealed things may recognize it.");
    }

    public static boolean firstLockedVaultFound(ServerPlayer player) {
        return appendGlobal(player, null, "dimension.first_locked_vault",
                "I found a vault that would not open. It did not feel broken. It felt selective. The sigils at the " +
                        "Self Anchor may be the language these ruins remember.");
    }

    public static boolean firstVaultOpened(ServerPlayer player) {
        return appendGlobal(player, null, "dimension.first_vault_opened",
                "A vault opened for one of the sigils I carry. The ruins are not random after all. Progress at the " +
                        "center changes what the outer regions are willing to reveal.");
    }

    public static boolean firstSkyShardReached(ServerPlayer player) {
        return appendGlobal(player, null, "dimension.first_sky_shard",
                "I reached one of the fragments above the island. The ground was never the whole map. Some memories " +
                        "only become visible when I build toward them.");
    }

    public static boolean firstScarHealed(ServerPlayer player) {
        return appendGlobal(player, null, "dimension.first_scar_healed",
                "The scar softened. Calming growth gave the place another pattern to follow, and the pressure around " +
                        "it receded. This world can remember how to breathe.");
    }

    public static boolean allSigilsCompleted(ServerPlayer player) {
        return appendGlobal(player, null, "dimension.all_sigils",
                "All nine sigils answer from the center. The ring feels less like a boundary now and more like a key. " +
                        "I should return to the Self Anchor.");
    }

    public static boolean spiralCourtOpened(ServerPlayer player) {
        return appendGlobal(player, null, "dimension.spiral_court_opened",
                "The Spiral Court opened beneath the Self Anchor. Every region has left a place inside it. The way " +
                        "down is waiting, but it does not feel like escape.");
    }

    public static boolean spiralCourtCompleted(ServerPlayer player) {
        return appendGlobal(player, null, "dimension.spiral_court_completed",
                "The court answered. Nothing outside me vanished, and nothing inside me became simple. Still, the " +
                        "whole island feels quieter. Freedom was never the same thing as leaving.");
    }

    private static String drugName(DrugId drug) {
        if (drug == null) {
            return "it";
        }
        String s = drug.serializedName();
        return s.isEmpty() ? "it" : s.replace('_', ' ');
    }

    private static boolean append(ServerPlayer player, DrugId drug, String sourceKey, String content) {
        return append(player, drug, sourceKey, content, false);
    }

    private static boolean appendGlobal(ServerPlayer player, DrugId drug, String sourceKey, String content) {
        return append(player, drug, sourceKey, content, true);
    }

    private static boolean append(
            ServerPlayer player,
            DrugId drug,
            String sourceKey,
            String content,
            boolean sourceOnly
    ) {
        if (player == null) {
            return false;
        }
        PlayerDiaryAttachment diary = player.getData(ModAttachments.PLAYER_DIARY);
        if (diary == null) {
            return false;
        }
        long gameTime = player.level().getGameTime();
        String cleaned = PlayerDiaryAttachment.sanitizeCustomContent(content);
        if (cleaned == null || cleaned.isEmpty()) {
            return false;
        }
        DiaryEntry entry = new DiaryEntry(
                PlayerDiaryAttachment.currentDay(gameTime),
                gameTime,
                DiaryEntryType.AUTO,
                cleaned,
                sourceKey,
                drug == null ? "" : drug.serializedName()
        );
        return sourceOnly ? diary.appendIfSourceAbsent(entry) : diary.appendIfAbsent(entry);
    }
}
