package org.mydrugs.mydrugs.diary;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;
import org.mydrugs.mydrugs.progression.PsyKnowledgeManager;
import org.mydrugs.mydrugs.psyche.PsycheMapNodeCatalog;
import org.mydrugs.mydrugs.psyche.PsycheMapNodeDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Interprets authoritative player state into diary-facing clarity.
 *
 * This layer does not unlock content, mutate progression, or replace JEI/the guide.
 * It chooses a small set of readable diary hints from state that already exists.
 */
public final class DiaryClarityService {
    private static final int MAX_BREADCRUMBS = 3;
    private static final int MAX_BLOCKERS = 3;
    private static final int MAX_MEMORIES = 5;
    private static final int MAX_WARNINGS = 3;
    private static final long BLOCKER_MEMORY_TICKS = 7L * 24000L;

    private static final String ITEM_COFFEE = item("coffee_bean");
    private static final String ITEM_TOBACCO = item("tobacco_leaf");
    private static final String ITEM_CANNABIS = item("cannabis_resin");
    private static final String ITEM_MIXING_VAT = item("mixing_vat");
    private static final String ITEM_STOMP = item("stomp_crafter");
    private static final String ITEM_WIRE = item("insulated_wire");
    private static final String ITEM_LSD = item("lsd_drop");
    private static final String ITEM_METH = item("meth_powder");
    private static final String ITEM_MUSHROOM = item("magic_mushroom");
    private static final String ITEM_DIARY = item("personal_diary");
    private static final String ITEM_RECOVERY_ANCHOR = item("recovery_anchor");

    private static final List<ProgressionStage> STAGES = List.of(
            stage("start",
                    known -> !has(known, PsyKnowledgeKey.CAFFEINE),
                    "diary.mydrugs.thought.start",
                    crumb("start", "Coffee", ITEM_COFFEE)),
            stage("caffeine",
                    known -> has(known, PsyKnowledgeKey.CAFFEINE) && !has(known, PsyKnowledgeKey.NICOTINIC),
                    "diary.mydrugs.thought.caffeine.next",
                    crumb("caffeine", "Tobacco", ITEM_TOBACCO)),
            stage("nicotinic",
                    known -> has(known, PsyKnowledgeKey.NICOTINIC) && !has(known, PsyKnowledgeKey.CANNABINOID),
                    "diary.mydrugs.thought.nicotinic.next",
                    crumb("nicotinic", "Cannabis", ITEM_CANNABIS)),
            stage("cannabinoid",
                    known -> has(known, PsyKnowledgeKey.CANNABINOID) && !has(known, PsyKnowledgeKey.FERMENTED),
                    "diary.mydrugs.thought.cannabinoid.next",
                    crumb("cannabinoid", "Fermentation", ITEM_MIXING_VAT)),
            stage("fermented",
                    known -> has(known, PsyKnowledgeKey.FERMENTED) && !has(known, PsyKnowledgeKey.STEEL_PLATING),
                    "diary.mydrugs.thought.fermented.next",
                    crumb("fermented", "Hash and Steel", ITEM_STOMP)),
            stage("steel",
                    known -> has(known, PsyKnowledgeKey.STEEL_PLATING) && !has(known, PsyKnowledgeKey.STIMULANT),
                    "diary.mydrugs.thought.steel.next",
                    crumb("steel", "Machine Era", ITEM_WIRE)),
            stage("stimulant",
                    known -> has(known, PsyKnowledgeKey.STIMULANT) && !has(known, PsyKnowledgeKey.LYSERGIC),
                    "diary.mydrugs.thought.stimulant.next",
                    crumb("stimulant", "LSD", ITEM_LSD)),
            stage("lysergic",
                    known -> has(known, PsyKnowledgeKey.LYSERGIC) && !has(known, PsyKnowledgeKey.OVERCLOCKED),
                    "diary.mydrugs.thought.lysergic.next",
                    crumb("lysergic", "Meth", ITEM_METH)),
            stage("overclocked",
                    known -> has(known, PsyKnowledgeKey.OVERCLOCKED) && !has(known, PsyKnowledgeKey.MYCELIAL),
                    "diary.mydrugs.thought.overclocked.next",
                    crumb("overclocked", "Mushrooms", ITEM_MUSHROOM)),
            stage("mycelial",
                    known -> has(known, PsyKnowledgeKey.MYCELIAL),
                    "diary.mydrugs.thought.mycelial.next",
                    crumb("mycelial", "Psychotrope Resonator", ITEM_DIARY))
    );

    private DiaryClarityService() {
    }

    public static DiaryClaritySnapshot build(ServerPlayer player,
                                             PlayerDiaryAttachment diary,
                                             DiaryPlayerStateDto state,
                                             List<PsycheMapNodeDto> psycheNodes) {
        Set<PsyKnowledgeKey> known = PsyKnowledgeManager.getKnown(player);
        ProgressionStage stage = stageFor(known);
        List<DiaryWarning> warnings = buildWarnings(state);
        List<DiaryBlocker> blockers = buildBlockers(diary, player.level().getGameTime(), state);
        List<DiaryBreadcrumb> breadcrumbs = buildBreadcrumbs(stage, warnings, blockers);
        List<DiaryMemory> memories = buildMemories(psycheNodes);

        String thoughtKey = chooseThoughtKey(stage, state);
        boolean diagnosticMode = player.isCreative() || player.hasPermissions(2);
        return new DiaryClaritySnapshot(
                new DiaryThought(thoughtKey),
                breadcrumbs,
                blockers,
                memories,
                warnings,
                diagnosticMode
        );
    }

    private static String chooseThoughtKey(ProgressionStage stage, DiaryPlayerStateDto state) {
        if (state.overdoseTimerTicks() > 0) {
            return "diary.mydrugs.thought.recovery.overdose";
        }
        if (state.badTripActive()) {
            return "diary.mydrugs.thought.recovery.bad_trip";
        }
        if (state.globalSeverity() >= 0.70F || state.stress() >= 0.85F) {
            return "diary.mydrugs.thought.recovery.severe";
        }
        return stage.thoughtKey();
    }

    private static List<DiaryBreadcrumb> buildBreadcrumbs(ProgressionStage stage,
                                                          List<DiaryWarning> warnings,
                                                          List<DiaryBlocker> blockers) {
        List<DiaryBreadcrumb> out = new ArrayList<>(MAX_BREADCRUMBS);
        if (!warnings.isEmpty()) {
            out.add(new DiaryBreadcrumb(
                    "diary.mydrugs.breadcrumb.recovery.text",
                    "diary.mydrugs.breadcrumb.recovery.clear",
                    "diary.mydrugs.breadcrumb.recovery.explicit",
                    "Recovery Basics",
                    ITEM_RECOVERY_ANCHOR,
                    DiarySpoilerLevel.CLEAR
            ));
        }
        if (!blockers.isEmpty()) {
            out.add(new DiaryBreadcrumb(
                    "diary.mydrugs.breadcrumb.blocker.text",
                    "diary.mydrugs.breadcrumb.blocker.clear",
                    "diary.mydrugs.breadcrumb.blocker.explicit",
                    "When You Are Stuck",
                    ITEM_DIARY,
                    DiarySpoilerLevel.CLEAR
            ));
        }
        out.addAll(stage.breadcrumbs());
        if (out.size() > MAX_BREADCRUMBS) {
            return List.copyOf(out.subList(0, MAX_BREADCRUMBS));
        }
        return List.copyOf(out);
    }

    private static List<DiaryWarning> buildWarnings(DiaryPlayerStateDto state) {
        List<DiaryWarning> warnings = new ArrayList<>(MAX_WARNINGS);
        if (state.overdoseTimerTicks() > 0) {
            warnings.add(new DiaryWarning(
                    "diary.mydrugs.warning.overdose.text",
                    "diary.mydrugs.warning.overdose.clear",
                    ITEM_RECOVERY_ANCHOR,
                    3
            ));
        }
        if (state.badTripActive()) {
            warnings.add(new DiaryWarning(
                    "diary.mydrugs.warning.bad_trip.text",
                    "diary.mydrugs.warning.bad_trip.clear",
                    ITEM_DIARY,
                    state.badTripSeverity() >= 0.70F ? 3 : 2
            ));
        }
        if (state.globalSeverity() >= 0.70F) {
            warnings.add(new DiaryWarning(
                    "diary.mydrugs.warning.withdrawal.text",
                    "diary.mydrugs.warning.withdrawal.clear",
                    ITEM_RECOVERY_ANCHOR,
                    2
            ));
        }
        if (state.stress() >= 0.80F) {
            warnings.add(new DiaryWarning(
                    "diary.mydrugs.warning.stress.text",
                    "diary.mydrugs.warning.stress.clear",
                    ITEM_DIARY,
                    2
            ));
        }
        if (state.sleepBlocked()) {
            warnings.add(new DiaryWarning(
                    "diary.mydrugs.warning.sleep.text",
                    "diary.mydrugs.warning.sleep.clear",
                    ITEM_RECOVERY_ANCHOR,
                    1
            ));
        }
        if (warnings.size() > MAX_WARNINGS) {
            return List.copyOf(warnings.subList(0, MAX_WARNINGS));
        }
        return List.copyOf(warnings);
    }

    private static List<DiaryBlocker> buildBlockers(PlayerDiaryAttachment diary,
                                                    long gameTime,
                                                    DiaryPlayerStateDto state) {
        List<DiaryBlocker> blockers = new ArrayList<>(MAX_BLOCKERS);
        for (PlayerDiaryAttachment.RecentBlocker recent : diary.getRecentBlockers()) {
            if (gameTime - recent.lastSeenGameTime() > BLOCKER_MEMORY_TICKS) {
                continue;
            }
            blockers.add(blockerFor(recent));
            if (blockers.size() >= MAX_BLOCKERS) {
                break;
            }
        }
        if (blockers.isEmpty() && (state.badTripActive() || state.globalSeverity() >= 0.70F || state.stress() >= 0.85F)) {
            blockers.add(new DiaryBlocker(
                    DiaryBlockerTypes.BODY_TOO_LOUD,
                    "diary.mydrugs.blocker.body_too_loud.text",
                    "diary.mydrugs.blocker.body_too_loud.clear",
                    "diary.mydrugs.blocker.body_too_loud.explicit",
                    1,
                    gameTime,
                    DiarySpoilerLevel.CLEAR
            ));
        }
        return List.copyOf(blockers);
    }

    private static DiaryBlocker blockerFor(PlayerDiaryAttachment.RecentBlocker recent) {
        String type = recent.type();
        String base = switch (type) {
            case DiaryBlockerTypes.MUSHROOM_GATE -> "mushroom_gate";
            case DiaryBlockerTypes.MACHINE_GENERIC -> "machine_generic";
            case DiaryBlockerTypes.BODY_TOO_LOUD -> "body_too_loud";
            default -> "knowledge_gate";
        };
        DiarySpoilerLevel level = recent.count() >= 3 ? DiarySpoilerLevel.EXPLICIT : DiarySpoilerLevel.CLEAR;
        return new DiaryBlocker(
                type,
                "diary.mydrugs.blocker." + base + ".text",
                "diary.mydrugs.blocker." + base + ".clear",
                "diary.mydrugs.blocker." + base + ".explicit",
                recent.count(),
                recent.lastSeenGameTime(),
                level
        );
    }

    private static List<DiaryMemory> buildMemories(List<PsycheMapNodeDto> nodes) {
        List<PsycheMapNodeDto> sorted = new ArrayList<>(nodes);
        sorted.sort(Comparator.comparingLong(PsycheMapNodeDto::unlockedAtGameTime).reversed());
        List<DiaryMemory> memories = new ArrayList<>(MAX_MEMORIES);
        for (PsycheMapNodeDto node : sorted) {
            PsycheMapNodeCatalog.Entry entry = PsycheMapNodeCatalog.byId(node.nodeId());
            if (entry == null) {
                continue;
            }
            memories.add(new DiaryMemory(
                    node.unlockedDay(),
                    entry.captionKey,
                    entry.titleKey,
                    node.nodeId(),
                    guideTargetFor(node.nodeId()),
                    entry.iconItemId
            ));
            if (memories.size() >= MAX_MEMORIES) {
                break;
            }
        }
        return List.copyOf(memories);
    }

    private static String guideTargetFor(String nodeId) {
        if (nodeId == null || nodeId.isBlank()) {
            return "";
        }
        if (nodeId.endsWith(":caffeine")) return "Coffee";
        if (nodeId.endsWith(":nicotinic")) return "Tobacco";
        if (nodeId.endsWith(":cannabinoid")) return "Cannabis";
        if (nodeId.endsWith(":fermented")) return "Fermentation";
        if (nodeId.endsWith(":steel_plating")) return "Hash and Steel";
        if (nodeId.endsWith(":stimulant")) return "Coca";
        if (nodeId.endsWith(":lysergic")) return "LSD";
        if (nodeId.endsWith(":overclocked")) return "Meth";
        if (nodeId.endsWith(":mycelial")) return "Mushrooms";
        if (nodeId.endsWith(":first_recovery_anchor") || nodeId.endsWith(":first_sanctuary")) return "Recovery Sanctuary";
        if (nodeId.endsWith(":first_bad_trip") || nodeId.endsWith(":first_inner_demon")) return "Bad Trips and Integration";
        if (nodeId.endsWith(":first_psy_mixer_ritual") || nodeId.endsWith(":first_ritual_success")
                || nodeId.endsWith(":first_ritual_failure") || nodeId.endsWith(":first_named_formula")) {
            return "Psy Mixer";
        }
        return "The Psyche Map";
    }

    private static ProgressionStage stageFor(Set<PsyKnowledgeKey> known) {
        for (ProgressionStage stage : STAGES) {
            if (stage.matches(known)) {
                return stage;
            }
        }
        return STAGES.getFirst();
    }

    private static ProgressionStage stage(String id,
                                          Predicate<Set<PsyKnowledgeKey>> predicate,
                                          String thoughtKey,
                                          DiaryBreadcrumb breadcrumb) {
        return new ProgressionStage(id, predicate, thoughtKey, List.of(breadcrumb));
    }

    private static DiaryBreadcrumb crumb(String id, String guideTarget, String iconItemId) {
        return new DiaryBreadcrumb(
                "diary.mydrugs.breadcrumb." + id + ".text",
                "diary.mydrugs.breadcrumb." + id + ".clear",
                "diary.mydrugs.breadcrumb." + id + ".explicit",
                guideTarget,
                iconItemId,
                DiarySpoilerLevel.VAGUE
        );
    }

    private static boolean has(Set<PsyKnowledgeKey> known, PsyKnowledgeKey key) {
        return known.contains(key);
    }

    private static String item(String path) {
        return MyDrugs.MODID + ":" + path;
    }

    private record ProgressionStage(
            String id,
            Predicate<Set<PsyKnowledgeKey>> predicate,
            String thoughtKey,
            List<DiaryBreadcrumb> breadcrumbs
    ) {
        boolean matches(Set<PsyKnowledgeKey> known) {
            return predicate.test(known);
        }
    }
}
