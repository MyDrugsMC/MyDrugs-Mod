package org.mydrugs.mydrugs.psyche;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.MyDrugs;

/**
 * Convenience server-side helpers that translate game events into psyche-map
 * node unlocks. All unlocks are idempotent (handled by {@link PsycheMapManager}).
 */
public final class PsycheMapMilestones {
    public static final ResourceLocation FIRST_DIARY_ENTRY      = id("first_diary_entry");
    public static final ResourceLocation FIRST_BAD_TRIP         = id("first_bad_trip");
    public static final ResourceLocation FIRST_RECOVERY_ANCHOR  = id("first_recovery_anchor");
    public static final ResourceLocation FIRST_THERAPIST_VISIT  = id("first_therapist_visit");
    public static final ResourceLocation FIRST_PSY_MIXER_RITUAL = id("first_psy_mixer_ritual");
    public static final ResourceLocation FIRST_RITUAL_SUCCESS   = id("first_ritual_success");
    public static final ResourceLocation FIRST_RITUAL_FAILURE   = id("first_ritual_failure");
    public static final ResourceLocation FIRST_NAMED_FORMULA    = id("first_named_formula");
    public static final ResourceLocation FIRST_MUTATION         = id("first_mutation");
    public static final ResourceLocation FIRST_INFECTION        = id("first_infection");
    public static final ResourceLocation FIRST_INNER_DEMON      = id("first_inner_demon");
    public static final ResourceLocation FIRST_DEMON_DEFEATED   = id("first_demon_defeated");
    public static final ResourceLocation FIRST_PSYCHOTROPE_ENERGY = id("first_psychotrope_energy");

    private PsycheMapMilestones() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, path);
    }

    public static void diaryEntry(ServerPlayer player)        { unlock(player, FIRST_DIARY_ENTRY,      "diary_entry"); }
    public static void badTrip(ServerPlayer player)           { unlock(player, FIRST_BAD_TRIP,         "bad_trip"); }
    public static void recoveryAnchor(ServerPlayer player)    { unlock(player, FIRST_RECOVERY_ANCHOR,  "recovery_anchor"); }
    public static void therapistVisit(ServerPlayer player)    { unlock(player, FIRST_THERAPIST_VISIT,  "therapy"); }
    public static void psyMixerRitual(ServerPlayer player)    { unlock(player, FIRST_PSY_MIXER_RITUAL, "ritual_start"); }
    public static void ritualSuccess(ServerPlayer player)     { unlock(player, FIRST_RITUAL_SUCCESS,   "ritual_success"); }
    public static void ritualFailure(ServerPlayer player)     { unlock(player, FIRST_RITUAL_FAILURE,   "ritual_failure"); }
    public static void namedFormula(ServerPlayer player)      { unlock(player, FIRST_NAMED_FORMULA,    "named_formula"); }
    public static void mutation(ServerPlayer player)          { unlock(player, FIRST_MUTATION,         "mutation"); }
    public static void infection(ServerPlayer player)         { unlock(player, FIRST_INFECTION,        "infection"); }
    public static void innerDemon(ServerPlayer player)        { unlock(player, FIRST_INNER_DEMON,      "demon_spawn"); }
    public static void demonDefeated(ServerPlayer player)     { unlock(player, FIRST_DEMON_DEFEATED,   "demon_killed"); }
    public static void psychotropeEnergy(ServerPlayer player) { unlock(player, FIRST_PSYCHOTROPE_ENERGY, "psychotrope_energy"); }

    private static void unlock(ServerPlayer player, ResourceLocation node, String trigger) {
        if (player == null) return;
        try {
            PsycheMapManager.unlock(player, node, trigger);
        } catch (Throwable ignored) {
            // unlock failures must not break gameplay
        }
    }
}
