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

    public static void firstDimensionEntry(ServerPlayer player) {
        append(player, null, "dimension.first_entry",
                "I stepped into the inner world for the first time. It is small and bare, but it is mine. " +
                        "Whatever I integrate out there will shape this place.");
    }

    public static void dimensionExpanded(ServerPlayer player, DrugId drug) {
        append(player, drug, "dimension.expanded",
                "The inner world grew today, fed by " + drugName(drug) + ". A new ring of ground I never put there. " +
                        "I am learning that what I make peace with becomes the world I get to live in.");
    }

    private static String drugName(DrugId drug) {
        if (drug == null) {
            return "it";
        }
        String s = drug.serializedName();
        return s.isEmpty() ? "it" : s.replace('_', ' ');
    }

    private static void append(ServerPlayer player, DrugId drug, String sourceKey, String content) {
        if (player == null) {
            return;
        }
        PlayerDiaryAttachment diary = player.getData(ModAttachments.PLAYER_DIARY);
        if (diary == null) {
            return;
        }
        long gameTime = player.level().getGameTime();
        String cleaned = PlayerDiaryAttachment.sanitizeCustomContent(content);
        if (cleaned.isEmpty()) {
            return;
        }
        diary.append(new DiaryEntry(
                PlayerDiaryAttachment.currentDay(gameTime),
                gameTime,
                DiaryEntryType.AUTO,
                cleaned,
                sourceKey,
                drug == null ? "" : drug.serializedName()
        ));
    }
}
