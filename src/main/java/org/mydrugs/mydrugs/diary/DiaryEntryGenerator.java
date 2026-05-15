package org.mydrugs.mydrugs.diary;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.data.TemporaryRecoveryEffects;
import org.mydrugs.mydrugs.core.drug.dose.DoseState;
import org.mydrugs.mydrugs.addiction.manager.state.BadTripState;

import java.util.List;

/**
 * Deterministically generate an automatic diary entry from the current player state.
 *
 * The selection is NOT random: a strongest-state priority chain picks the state key,
 * and the template variant inside that state is chosen by hashing
 * (state key + day + dominant drug id + withdrawal bracket + dose state).
 * This guarantees variety without true randomness.
 */
public final class DiaryEntryGenerator {
    private DiaryEntryGenerator() {
    }

    public static DiaryEntry generate(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        long day = PlayerDiaryAttachment.currentDay(gameTime);

        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        TemporaryRecoveryEffects te = stats.temporaryEffects;

        DrugId dominantDrug = findDominantDrug(stats);
        DrugCategory dominantCategory = dominantDrug == null ? null : DrugRegistry.getCategory(dominantDrug);
        DrugAddictionStats dominantStats = dominantDrug == null ? null : stats.getDrugStats(dominantDrug);

        float stress = stats.stressLevel;
        float severeWithdrawal = peakWithdrawal(stats);
        DoseState dose = dominantStats == null ? DoseState.NORMAL : dominantStats.lastDoseState;

        boolean overdose = stats.overdoseDeathTimer >= 0
                || dose == DoseState.OVERDOSE
                || dose == DoseState.ETHYLIC_COMA;
        boolean badTrip = stats.badTrip != null && stats.badTrip.active;
        boolean veryHighDose = dose == DoseState.VERY_HIGH || dose == DoseState.VERY_DRUNK;
        boolean highDose = dose == DoseState.HIGH || dose == DoseState.DRUNK;
        boolean sleepBlocked = stats.sleepBlockedUntil > gameTime;
        boolean recoveryActive = te.hasDiaryCalm(gameTime)
                || te.hasHeadphones(gameTime)
                || te.hasCalmingMixture(gameTime)
                || te.hasSleepBonus(gameTime);

        String stateKey;
        if (overdose) stateKey = "overdose";
        else if (badTrip) stateKey = "bad_trip";
        else if (severeWithdrawal >= 70.0F) stateKey = "severe_withdrawal";
        else if (severeWithdrawal >= 35.0F) stateKey = "moderate_withdrawal";
        else if (veryHighDose) stateKey = "very_high_dose";
        else if (highDose) stateKey = "high_dose";
        else if (stress >= 0.70F) stateKey = "high_stress";
        else if (sleepBlocked) stateKey = "insomnia";
        else if (recoveryActive) stateKey = "recovery_support";
        else stateKey = "calm_day";

        int withdrawalBracket = Math.min(4, Math.round(severeWithdrawal / 25.0F));
        int doseOrdinal = dose == null ? 0 : dose.ordinal();
        String dominantId = dominantDrug == null ? "" : dominantDrug.serializedName();

        int templateIndex = deterministicIndex(stateKey, day, dominantId, withdrawalBracket, doseOrdinal,
                TEMPLATES_OF(stateKey).size());
        String body = TEMPLATES_OF(stateKey).get(templateIndex);

        String fragment = drugFragment(dominantCategory, dominantDrug, stateKey, day);
        String content = fragment.isEmpty() ? body : body + " " + fragment;

        return new DiaryEntry(day, gameTime, DiaryEntryType.AUTO, content, stateKey, dominantId);
    }

    private static int deterministicIndex(String stateKey, long day, String drug, int withdrawalBracket,
                                          int doseOrdinal, int size) {
        if (size <= 0) return 0;
        long h = 1469598103934665603L;
        h ^= stateKey.hashCode();
        h *= 1099511628211L;
        h ^= day;
        h *= 1099511628211L;
        h ^= drug.hashCode();
        h *= 1099511628211L;
        h ^= withdrawalBracket;
        h *= 1099511628211L;
        h ^= doseOrdinal;
        int v = (int) (h ^ (h >>> 32));
        v = Math.floorMod(v, size);
        return v;
    }

    @Nullable
    private static DrugId findDominantDrug(PlayerAddictionStats stats) {
        DrugId best = null;
        float bestScore = -1.0F;
        for (DrugId id : stats.getTrackedDrugIds()) {
            DrugAddictionStats ds = stats.getDrugStats(id);
            if (ds == null) continue;
            float score = ds.currentDose() * 2.0F + ds.baseWithdrawalMeter * 1.2F + ds.addictionValue * 0.6F;
            if (score > bestScore) {
                bestScore = score;
                best = id;
            }
        }
        return best;
    }

    private static float peakWithdrawal(PlayerAddictionStats stats) {
        float peak = 0.0F;
        for (DrugId id : stats.getTrackedDrugIds()) {
            DrugAddictionStats ds = stats.getDrugStats(id);
            if (ds == null) continue;
            peak = Math.max(peak, ds.baseWithdrawalMeter);
        }
        return peak;
    }

    private static String drugFragment(@Nullable DrugCategory category, @Nullable DrugId drug, String stateKey, long day) {
        if (category == null) return "";
        List<String> list = CATEGORY_FRAGMENTS.getOrDefault(category, List.of());
        if (list.isEmpty()) return "";
        int idx = deterministicIndex("frag_" + stateKey, day,
                drug == null ? "" : drug.serializedName(), 0, category.ordinal(), list.size());
        return list.get(idx);
    }

    private static List<String> TEMPLATES_OF(String key) {
        return switch (key) {
            case "overdose" -> OVERDOSE;
            case "bad_trip" -> BAD_TRIP;
            case "severe_withdrawal" -> SEVERE_WITHDRAWAL;
            case "moderate_withdrawal" -> MODERATE_WITHDRAWAL;
            case "very_high_dose" -> VERY_HIGH_DOSE;
            case "high_dose" -> HIGH_DOSE;
            case "high_stress" -> HIGH_STRESS;
            case "insomnia" -> INSOMNIA;
            case "recovery_support" -> RECOVERY_SUPPORT;
            default -> CALM_DAY;
        };
    }

    private static final List<String> OVERDOSE = List.of(
            "I came too close today. My body stopped feeling like a place I could trust. I am writing this because I am still here.",
            "Something went wrong inside me today. It was not dramatic from the outside, but it felt like my body was counting down.",
            "I felt my heartbeat get strange and far away. I do not want this page to be my last one."
    );
    private static final List<String> BAD_TRIP = List.of(
            "Reality did not stay still today. I tried to hold onto simple things: the page, my breath, the fact that this will pass.",
            "My thoughts turned against me for a while. The world became too loud, too meaningful, too sharp.",
            "The shapes did not stay where I put them today. I am writing this slowly to remind myself the page does."
    );
    private static final List<String> SEVERE_WITHDRAWAL = List.of(
            "My body kept asking for something I did not want to give it. Every quiet moment felt like an argument.",
            "Today was not just craving. It was pressure under the skin, like my own nerves were trying to negotiate.",
            "I could feel the absence everywhere. The day had weight that I had to carry around."
    );
    private static final List<String> MODERATE_WITHDRAWAL = List.of(
            "Something in me kept pulling backward today. I could still move, but everything cost more effort.",
            "The lack was there, not screaming, but present enough to shape the day.",
            "It was the kind of day where everything took an extra second of will to start."
    );
    private static final List<String> VERY_HIGH_DOSE = List.of(
            "I am too far in to call this casual. The page is the steadiest thing in the room right now.",
            "I am not sober enough to pretend this is nothing. The page helps me name it before it names me.",
            "I keep losing track of small things. Writing the date helped. Writing my name would help too."
    );
    private static final List<String> HIGH_DOSE = List.of(
            "I can feel the dose changing the way the world reaches me. Part of me wants to call it control. Another part knows better.",
            "Things are louder than they should be. Or maybe I am quieter. Either way, the gap is noticeable.",
            "The edges of the room are friendly today, but I do not entirely trust that."
    );
    private static final List<String> HIGH_STRESS = List.of(
            "My stress was high today. I kept reacting like danger was nearby, even when nothing was happening.",
            "The pressure in me had nowhere to go, so I put some of it here.",
            "I felt my shoulders all day. That is usually a sign I forgot to put them down."
    );
    private static final List<String> INSOMNIA = List.of(
            "Sleep would not come easily. The night stretched out, and my thoughts filled the empty space.",
            "I was tired, but not peaceful. My body wanted rest and my mind refused to unlock the door.",
            "The dark hours kept their own time today. I lived them more than I slept them."
    );
    private static final List<String> RECOVERY_SUPPORT = List.of(
            "Something helped today. Not everything is fixed, but the noise became a little less absolute.",
            "I found a small support and used it. That matters, even if it does not feel heroic.",
            "I let the kindness in for a few minutes. It cost me nothing and changed the shape of the hour."
    );
    private static final List<String> CALM_DAY = List.of(
            "Today was quieter than usual. I am trying to notice that without waiting for it to collapse.",
            "Nothing huge happened, and maybe that is worth writing down too.",
            "I felt close to ordinary today. The word \"ordinary\" tasted like rest."
    );

    private static final java.util.EnumMap<DrugCategory, List<String>> CATEGORY_FRAGMENTS = new java.util.EnumMap<>(DrugCategory.class);
    static {
        CATEGORY_FRAGMENTS.put(DrugCategory.CANNABINOID, List.of(
                "The haze made things softer, but not necessarily clearer.",
                "The smoke sat behind my eyes for a while, gentle and slow."
        ));
        CATEGORY_FRAGMENTS.put(DrugCategory.CAFFEINE, List.of(
                "The energy felt borrowed, and my hands knew it.",
                "Coffee carried me a long way today, then asked for interest."
        ));
        CATEGORY_FRAGMENTS.put(DrugCategory.NICOTINIC, List.of(
                "The habit asked quietly, which somehow made it harder to ignore.",
                "Tobacco kept finding my fingers even when I was not thinking about it."
        ));
        CATEGORY_FRAGMENTS.put(DrugCategory.DEPRESSANT, List.of(
                "The courage felt temporary, like warmth from a match.",
                "Alcohol blurred the rough edges, and a few important ones too."
        ));
        CATEGORY_FRAGMENTS.put(DrugCategory.STIMULANT, List.of(
                "Speed made movement easier, but peace harder.",
                "I went faster than the day. The day caught up later."
        ));
        CATEGORY_FRAGMENTS.put(DrugCategory.PSYCHEDELIC, List.of(
                "The visions had weight, but not every meaning was trustworthy.",
                "I saw more than I could carry, and tried to keep only what felt true."
        ));
        CATEGORY_FRAGMENTS.put(DrugCategory.OPIOID, List.of(
                "The quiet promised relief, but I know promises can become chains.",
                "The warmth was real, and so was the room I had to come back to."
        ));
        CATEGORY_FRAGMENTS.put(DrugCategory.SEDATIVE, List.of(
                "Numbness lowered the volume, but it did not answer the question.",
                "I traded sharpness for stillness. I am not sure the exchange was fair."
        ));
        CATEGORY_FRAGMENTS.put(DrugCategory.DISSOCIATIVE, List.of(
                "Distance from myself felt safe for a moment, then strange.",
                "I watched my body do things from the next room over."
        ));
        CATEGORY_FRAGMENTS.put(DrugCategory.EMPATHOGEN, List.of(
                "Connection felt close enough to touch, and that scared me too.",
                "I loved everyone for an hour. Coming down made me wonder if it counted."
        ));
        CATEGORY_FRAGMENTS.put(DrugCategory.INHALANT, List.of(
                "A short escape still leaves a trace.",
                "The brief brightness paid itself back in dull edges."
        ));
        CATEGORY_FRAGMENTS.put(DrugCategory.DELIRIANT, List.of(
                "The line between thought and image stopped behaving today.",
                "Things I knew were not there refused to be polite about it."
        ));
    }
}
