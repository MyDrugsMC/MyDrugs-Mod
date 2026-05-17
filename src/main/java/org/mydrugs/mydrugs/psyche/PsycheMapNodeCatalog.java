package org.mydrugs.mydrugs.psyche;

import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Static catalog of every psyche-map node. The map renderer uses these
 * coordinates as <em>world</em> coordinates (origin at 0,0); the screen
 * applies pan + zoom on top.
 *
 * <p>Knowledge node ids preserve the existing {@link org.mydrugs.mydrugs.progression.PsyKnowledgeKey}
 * paths so v1 saves keep working.</p>
 */
public final class PsycheMapNodeCatalog {

    public static final class Entry {
        public final String nodeId;
        public final String titleKey;
        public final String captionKey;
        public final String iconItemId;
        public final int x;
        public final int y;
        public final List<String> parents;
        public final boolean hiddenUntilUnlocked;

        Entry(String nodeId, String titleKey, String captionKey, String iconItemId,
              int x, int y, List<String> parents, boolean hiddenUntilUnlocked) {
            this.nodeId = nodeId;
            this.titleKey = titleKey;
            this.captionKey = captionKey;
            this.iconItemId = iconItemId;
            this.x = x;
            this.y = y;
            this.parents = parents;
            this.hiddenUntilUnlocked = hiddenUntilUnlocked;
        }

        public ResourceLocation idAsResourceLocation() {
            ResourceLocation rl = ResourceLocation.tryParse(nodeId);
            return rl != null ? rl : ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, nodeId.replace('/', '_'));
        }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();
    private static final Map<String, Entry> BY_ID = new LinkedHashMap<>();

    private PsycheMapNodeCatalog() {
    }

    public static List<Entry> all() {
        return Collections.unmodifiableList(ENTRIES);
    }

    public static Entry byId(String nodeId) {
        return BY_ID.get(nodeId);
    }

    public static int totalNodes() {
        return ENTRIES.size();
    }

    private static void add(String id, String title, String caption, String icon,
                            int x, int y, List<String> parents, boolean hidden) {
        Entry e = new Entry(id, title, caption, icon, x, y, parents, hidden);
        ENTRIES.add(e);
        BY_ID.put(id, e);
    }

    private static String titleKeyFor(String id) {
        return "psyche.mydrugs.node." + id.replace('/', '.') + ".title";
    }

    private static String captionKeyFor(String id) {
        return "psyche.mydrugs.node." + id.replace('/', '.') + ".caption";
    }

    private static String know(String path) {
        // PsyKnowledgeKey ids are mydrugs:<path>; the map node id mirrors that
        // for v1 compat by being just the raw mydrugs:<path> ResourceLocation.
        return MyDrugs.MODID + ":" + path;
    }

    private static List<String> p(String... s) {
        return List.of(s);
    }

    static {
        // -------- Knowledge spine (zigzag main line) --------
        String k_caf  = know("caffeine");
        String k_nic  = know("nicotinic");
        String k_can  = know("cannabinoid");
        String k_fer  = know("fermented");
        String k_sti  = know("stimulant");
        String k_lys  = know("lysergic");
        String k_ovc  = know("overclocked");
        String k_myc  = know("mycelial");
        String k_stl  = know("steel_plating");
        String paper = "minecraft:paper";
        String psyIcon = MyDrugs.MODID + ":psy_receptacle";

        add(k_caf, titleKeyFor("knowledge/caffeine"),  captionKeyFor("knowledge/caffeine"),
                MyDrugs.MODID + ":coffee_bean",  0,   -45, p(),                  false);
        add(k_nic, titleKeyFor("knowledge/nicotinic"), captionKeyFor("knowledge/nicotinic"),
                MyDrugs.MODID + ":tobacco_leaf", 95,  45,  p(k_caf),             false);
        add(k_can, titleKeyFor("knowledge/cannabinoid"), captionKeyFor("knowledge/cannabinoid"),
                psyIcon,                         190, -45, p(k_nic),             false);
        add(k_fer, titleKeyFor("knowledge/fermented"), captionKeyFor("knowledge/fermented"),
                MyDrugs.MODID + ":insulated_wire", 285, 45, p(k_can),            false);
        add(k_sti, titleKeyFor("knowledge/stimulant"), captionKeyFor("knowledge/stimulant"),
                psyIcon,                         380, -45, p(k_fer),             false);
        add(k_lys, titleKeyFor("knowledge/lysergic"),  captionKeyFor("knowledge/lysergic"),
                MyDrugs.MODID + ":lsd_drop",     475, 45,  p(k_sti),             false);
        add(k_ovc, titleKeyFor("knowledge/overclocked"), captionKeyFor("knowledge/overclocked"),
                psyIcon,                         570, -45, p(k_lys),             false);
        add(k_myc, titleKeyFor("knowledge/mycelial"),  captionKeyFor("knowledge/mycelial"),
                MyDrugs.MODID + ":magic_mushroom", 665, 45, p(k_ovc),            false);
        add(k_stl, titleKeyFor("knowledge/steel_plating"), captionKeyFor("knowledge/steel_plating"),
                "minecraft:iron_ingot",          760, -45, p(k_myc),             false);

        // -------- Milestone branches --------
        String diaryId      = MyDrugs.MODID + ":first_diary_entry";
        String badTripId    = MyDrugs.MODID + ":first_bad_trip";
        String anchorId     = MyDrugs.MODID + ":first_recovery_anchor";
        String therapyId    = MyDrugs.MODID + ":first_therapist_visit";
        String ritualId     = MyDrugs.MODID + ":first_psy_mixer_ritual";
        String ritualOkId   = MyDrugs.MODID + ":first_ritual_success";
        String ritualFailId = MyDrugs.MODID + ":first_ritual_failure";
        String formulaId    = MyDrugs.MODID + ":first_named_formula";
        String mutationId   = MyDrugs.MODID + ":first_mutation";
        String infectionId  = MyDrugs.MODID + ":first_infection";
        String demonId      = MyDrugs.MODID + ":first_inner_demon";
        String demonKillId  = MyDrugs.MODID + ":first_demon_defeated";
        String pscEnergyId  = MyDrugs.MODID + ":first_psychotrope_energy";

        add(diaryId, titleKeyFor("first_diary_entry"), captionKeyFor("first_diary_entry"),
                MyDrugs.MODID + ":personal_diary",     -100, 30,   p(),                  false);
        add(badTripId, titleKeyFor("first_bad_trip"), captionKeyFor("first_bad_trip"),
                MyDrugs.MODID + ":broken_courage",      380, -150, p(k_sti),             true);
        add(anchorId, titleKeyFor("first_recovery_anchor"), captionKeyFor("first_recovery_anchor"),
                MyDrugs.MODID + ":recovery_anchor",     190, 145,  p(k_can),             false);
        add(therapyId, titleKeyFor("first_therapist_visit"), captionKeyFor("first_therapist_visit"),
                MyDrugs.MODID + ":therapist_desk",      190, 230,  p(anchorId),          false);
        add(ritualId, titleKeyFor("first_psy_mixer_ritual"), captionKeyFor("first_psy_mixer_ritual"),
                psyIcon,                                475, 155,  p(k_lys),             false);
        add(ritualOkId, titleKeyFor("first_ritual_success"), captionKeyFor("first_ritual_success"),
                paper,                                  570, 170,  p(ritualId),          false);
        add(ritualFailId, titleKeyFor("first_ritual_failure"), captionKeyFor("first_ritual_failure"),
                MyDrugs.MODID + ":unstable_residue",    665, 170,  p(ritualId),          false);
        add(formulaId, titleKeyFor("first_named_formula"), captionKeyFor("first_named_formula"),
                paper,                                  760, 170,  p(ritualOkId),        false);
        add(mutationId, titleKeyFor("first_mutation"), captionKeyFor("first_mutation"),
                paper,                                  665, -150, p(k_myc),             true);
        add(infectionId, titleKeyFor("first_infection"), captionKeyFor("first_infection"),
                paper,                                  475, -150, p(k_lys),             true);
        add(demonId, titleKeyFor("first_inner_demon"), captionKeyFor("first_inner_demon"),
                paper,                                  380, 155,  p(badTripId),         true);
        add(demonKillId, titleKeyFor("first_demon_defeated"), captionKeyFor("first_demon_defeated"),
                MyDrugs.MODID + ":fractured_impulse",   380, 235,  p(demonId),           true);
        add(pscEnergyId, titleKeyFor("first_psychotrope_energy"), captionKeyFor("first_psychotrope_energy"),
                psyIcon,                                570, -150, p(k_ovc),             false);
    }
}
