package org.mydrugs.mydrugs.psyche;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Static catalog of knowledge, milestone, and curated integration nodes on the Psyche Map. */
public final class PsycheMapNodeCatalog {

    public static final class Entry {
        public final String nodeId;
        public final @Nullable DrugId integrationDrugId;
        public final String knowledgeNodeId;
        public final String titleKey;
        public final String captionKey;
        public final String iconItemId;
        public final int x;
        public final int y;
        public final List<String> parents;
        public final boolean hiddenUntilUnlocked;

        Entry(String nodeId, @Nullable DrugId integrationDrugId, String knowledgeNodeId,
              String titleKey, String captionKey, String iconItemId,
              int x, int y, List<String> parents, boolean hiddenUntilUnlocked) {
            this.nodeId = nodeId;
            this.integrationDrugId = integrationDrugId;
            this.knowledgeNodeId = knowledgeNodeId;
            this.titleKey = titleKey;
            this.captionKey = captionKey;
            this.iconItemId = iconItemId;
            this.x = x;
            this.y = y;
            this.parents = parents;
            this.hiddenUntilUnlocked = hiddenUntilUnlocked;
        }

        public ResourceLocation idAsResourceLocation() {
            ResourceLocation parsed = ResourceLocation.tryParse(nodeId);
            return parsed != null
                    ? parsed
                    : ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, nodeId.replace('/', '_'));
        }

        public boolean isIntegrationNode() {
            return integrationDrugId != null;
        }

        public String drugTranslationKey() {
            return integrationDrugId == null
                    ? ""
                    : "drug.mydrugs." + integrationDrugId.serializedName();
        }

        public String knowledgeTranslationKey() {
            if (integrationDrugId == null) {
                return "";
            }
            PsyKnowledgeKey knowledge = CuratedDrugChain.stageKnowledge(integrationDrugId);
            return knowledge == null ? "" : knowledge.translationKey();
        }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final Map<String, Entry> BY_ID = new LinkedHashMap<>();
    private static final Map<DrugId, Entry> BY_INTEGRATION_DRUG = new EnumMap<>(DrugId.class);

    private PsycheMapNodeCatalog() {
    }

    public static List<Entry> all() {
        return Collections.unmodifiableList(ENTRIES);
    }

    public static Entry byId(String nodeId) {
        return BY_ID.get(nodeId);
    }

    public static Entry byDrug(DrugId drugId) {
        return BY_INTEGRATION_DRUG.get(drugId);
    }

    public static int totalNodes() {
        return ENTRIES.size();
    }

    private static void add(String id, String title, String caption, String icon,
                            int x, int y, List<String> parents, boolean hidden) {
        add(new Entry(id, null, "", title, caption, icon, x, y, parents, hidden));
    }

    private static void addIntegration(
            DrugId drugId,
            String icon,
            int x,
            int y,
            String knowledgeNodeId
    ) {
        String path = drugId.serializedName();
        Entry entry = new Entry(
                ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, path).toString(),
                drugId,
                knowledgeNodeId,
                "psyche.mydrugs.node.integration." + path + ".title",
                "psyche.mydrugs.node.integration." + path + ".caption",
                icon,
                x,
                y,
                List.of(knowledgeNodeId),
                true
        );
        add(entry);
        BY_INTEGRATION_DRUG.put(drugId, entry);
    }

    private static void add(Entry entry) {
        ENTRIES.add(entry);
        BY_ID.put(entry.nodeId, entry);
    }

    private static String titleKeyFor(String id) {
        return "psyche.mydrugs.node." + id.replace('/', '.') + ".title";
    }

    private static String captionKeyFor(String id) {
        return "psyche.mydrugs.node." + id.replace('/', '.') + ".caption";
    }

    private static String know(String path) {
        return MyDrugs.MODID + ":" + path;
    }

    private static String item(String path) {
        return MyDrugs.MODID + ":" + path;
    }

    private static List<String> p(String... values) {
        return List.of(values);
    }

    static {
        String kCaf = know("caffeine");
        String kNic = know("nicotinic");
        String kCan = know("cannabinoid");
        String kFer = know("fermented");
        String kSti = know("stimulant");
        String kLys = know("lysergic");
        String kOvc = know("overclocked");
        String kMyc = know("mycelial");
        String kStl = know("steel_plating");
        String paper = "minecraft:paper";
        String psyIcon = item("psy_receptacle");

        // Knowledge spine. IDs and layout are preserved for existing saves.
        add(kCaf, titleKeyFor("knowledge/caffeine"), captionKeyFor("knowledge/caffeine"),
                item("coffee_bean"), 0, -45, p(), false);
        add(kNic, titleKeyFor("knowledge/nicotinic"), captionKeyFor("knowledge/nicotinic"),
                item("tobacco_leaf"), 95, 45, p(kCaf), false);
        add(kCan, titleKeyFor("knowledge/cannabinoid"), captionKeyFor("knowledge/cannabinoid"),
                psyIcon, 190, -45, p(kNic), false);
        add(kFer, titleKeyFor("knowledge/fermented"), captionKeyFor("knowledge/fermented"),
                item("insulated_wire"), 285, 45, p(kCan), false);
        add(kSti, titleKeyFor("knowledge/stimulant"), captionKeyFor("knowledge/stimulant"),
                psyIcon, 380, -45, p(kFer), false);
        add(kLys, titleKeyFor("knowledge/lysergic"), captionKeyFor("knowledge/lysergic"),
                item("lsd_drop"), 475, 45, p(kSti), false);
        add(kOvc, titleKeyFor("knowledge/overclocked"), captionKeyFor("knowledge/overclocked"),
                psyIcon, 570, -45, p(kLys), false);
        add(kMyc, titleKeyFor("knowledge/mycelial"), captionKeyFor("knowledge/mycelial"),
                item("magic_mushroom"), 665, 45, p(kOvc), false);
        add(kStl, titleKeyFor("knowledge/steel_plating"), captionKeyFor("knowledge/steel_plating"),
                "minecraft:iron_ingot", 760, -45, p(kMyc), false);

        // Existing milestone branches.
        String diaryId = item("first_diary_entry");
        String badTripId = item("first_bad_trip");
        String anchorId = item("first_recovery_anchor");
        String sanctuaryId = item("first_sanctuary");
        String therapyId = item("first_therapist_visit");
        String ritualId = item("first_psy_mixer_ritual");
        String ritualOkId = item("first_ritual_success");
        String ritualFailId = item("first_ritual_failure");
        String formulaId = item("first_named_formula");
        String mutationId = item("first_mutation");
        String infectionId = item("first_infection");
        String demonId = item("first_inner_demon");
        String demonKillId = item("first_demon_defeated");
        String psyEnergyId = item("first_psychotrope_energy");

        add(diaryId, titleKeyFor("first_diary_entry"), captionKeyFor("first_diary_entry"),
                item("personal_diary"), -100, 30, p(), false);
        add(badTripId, titleKeyFor("first_bad_trip"), captionKeyFor("first_bad_trip"),
                item("broken_courage"), 380, -150, p(kSti), true);
        add(anchorId, titleKeyFor("first_recovery_anchor"), captionKeyFor("first_recovery_anchor"),
                item("recovery_anchor"), 190, 145, p(kCan), false);
        add(sanctuaryId, titleKeyFor("first_sanctuary"), captionKeyFor("first_sanctuary"),
                item("recovery_anchor"), 95, 230, p(anchorId), true);
        add(therapyId, titleKeyFor("first_therapist_visit"), captionKeyFor("first_therapist_visit"),
                item("therapist_desk"), 190, 230, p(anchorId), false);
        add(ritualId, titleKeyFor("first_psy_mixer_ritual"), captionKeyFor("first_psy_mixer_ritual"),
                psyIcon, 475, 155, p(kLys), false);
        add(ritualOkId, titleKeyFor("first_ritual_success"), captionKeyFor("first_ritual_success"),
                paper, 570, 170, p(ritualId), false);
        add(ritualFailId, titleKeyFor("first_ritual_failure"), captionKeyFor("first_ritual_failure"),
                item("unstable_residue"), 665, 170, p(ritualId), false);
        add(formulaId, titleKeyFor("first_named_formula"), captionKeyFor("first_named_formula"),
                paper, 760, 170, p(ritualOkId), false);
        add(mutationId, titleKeyFor("first_mutation"), captionKeyFor("first_mutation"),
                paper, 665, -150, p(kMyc), true);
        add(infectionId, titleKeyFor("first_infection"), captionKeyFor("first_infection"),
                paper, 475, -150, p(kLys), true);
        add(demonId, titleKeyFor("first_inner_demon"), captionKeyFor("first_inner_demon"),
                paper, 380, 155, p(badTripId), true);
        add(demonKillId, titleKeyFor("first_demon_defeated"), captionKeyFor("first_demon_defeated"),
                item("fractured_impulse"), 380, 235, p(demonId), true);
        add(psyEnergyId, titleKeyFor("first_psychotrope_energy"), captionKeyFor("first_psychotrope_energy"),
                psyIcon, 570, -150, p(kOvc), false);

        // Integration nodes are children of each curated drug's corresponding knowledge node.
        addIntegration(DrugId.COFFEE, item("lucid_extract"), 0, -300, kCaf);
        addIntegration(DrugId.TOBACCO, item("bitter_residue"), 95, 330, kNic);
        addIntegration(DrugId.WEED, item("calming_resin"), 190, -300, kCan);
        addIntegration(DrugId.HASH, item("pressed_calm"), 760, -300, kStl);
        addIntegration(DrugId.ALCOHOL, item("fermented_memory"), 285, 330, kFer);
        addIntegration(DrugId.COCAINE, item("redline_fuel"), 380, -300, kSti);
        addIntegration(DrugId.LSD, item("dream_residue"), 475, 330, kLys);
        addIntegration(DrugId.METH, item("overdrive_fuel"), 570, -300, kOvc);
        addIntegration(DrugId.MUSHROOMS, item("mycelial_insight"), 665, 330, kMyc);
    }
}
