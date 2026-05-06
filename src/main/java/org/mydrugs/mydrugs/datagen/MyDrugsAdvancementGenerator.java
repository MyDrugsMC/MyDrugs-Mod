package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.List;

public final class MyDrugsAdvancementGenerator {
    private ModAdvancementProvider.Output output;

    public void generate(ModAdvancementProvider.Output output) {
        this.output = output;
        onboarding();
        agriculture();
        processing();
        consumptionAndKnowledge();
        recovery();
        materials();
        machines();
        fluidsAndGases();
        logistics();
        psychotrope();
        challenges();
    }

    private void onboarding() {
        root("onboarding/root", item("cannabis_leaf"));
        inventoryAny("onboarding/first_seed", "onboarding/root", item("cannabis_seeds"),
                recipes("drying_rack", "grinding_bowl", "grinding_tool"),
                item("cannabis_seeds"), item("tobacco_seeds"), item("coca_seeds"), item("rye_seeds"), item("malt_seeds"), item("opium_poppy_seeds"));
        inventoryAny("onboarding/first_harvest", "onboarding/first_seed", item("cannabis_leaf"), List.of(),
                item("cannabis_leaf"), item("tobacco_leaf"), item("coca_leaf"), item("rye"), item("malt"), item("magic_mushroom"));
        placedAny("onboarding/drying_rack", "onboarding/first_harvest", item("drying_rack"), recipes("drying_rack"), block("drying_rack"));
        inventoryAny("onboarding/grinding_started", "onboarding/drying_rack", item("grinding_tool"),
                recipes("sieve", "stomp_crafter", "clay_vat", "evaporation_tray"),
                item("grinding_tool"), item("grinding_bowl"), item("portable_grinder"));
        placedAny("onboarding/first_processing_station", "onboarding/grinding_started", item("sieve"), List.of(),
                block("sieve"), block("stomp_crafter"), block("clay_vat"), block("evaporation_tray"), block("mixing_vat"));
        inventoryAny("onboarding/support_tools", "onboarding/root", item("personal_diary"),
                recipes("personal_diary", "headphones"),
                item("personal_diary"), item("headphones"), item("herbal_tea"), item("calming_mixture"));
        inventoryAny("onboarding/first_consumable", "onboarding/first_processing_station", item("joint"), recipes("roller", "bang", "glass_bottle"),
                item("joint"), item("cigaret"), item("hash_piece"), item("cannabis_powder"), item("tobacco_handful"), item("magic_mushroom"));
    }

    private void agriculture() {
        placed("agriculture/plant_cannabis", "onboarding/first_seed", item("cannabis_seeds"), block("cannabis_crop"));
        inventory("agriculture/harvest_cannabis", "agriculture/plant_cannabis", item("cannabis_leaf"), item("cannabis_leaf"));
        placed("agriculture/plant_tobacco", "onboarding/first_seed", item("tobacco_seeds"), block("tobacco_crop"));
        inventory("agriculture/harvest_tobacco", "agriculture/plant_tobacco", item("tobacco_leaf"), item("tobacco_leaf"));
        placed("agriculture/plant_coca", "onboarding/first_seed", item("coca_seeds"), block("coca_crop"));
        inventory("agriculture/harvest_coca", "agriculture/plant_coca", item("coca_leaf"), item("coca_leaf"));
        placed("agriculture/grow_rye", "onboarding/first_seed", item("rye_seeds"), block("rye_crop"));
        placed("agriculture/grow_malt", "agriculture/grow_rye", item("malt_seeds"), block("malt_crop"));
        placed("agriculture/plant_opium_poppy", "onboarding/first_seed", item("opium_poppy_seeds"), block("opium_poppy_crop"));
        inventory("agriculture/fungal_culture", "agriculture/magic_mushroom", item("fungal_culture"), item("fungal_culture"));
        inventory("agriculture/ergot", "agriculture/grow_rye", item("ergot"), item("ergot"), true);
        inventory("agriculture/magic_mushroom", "onboarding/first_harvest", item("magic_mushroom"), item("magic_mushroom"));
        inventory("agriculture/psychedelic_biome", "agriculture/magic_mushroom", item("psychedelic_mycelium"), item("psychedelic_mycelium"), true);
    }

    private void processing() {
        placed("processing/place_drying_rack", "onboarding/drying_rack", item("drying_rack"), block("drying_rack"));
        machine("processing/dry_first_leaf", "processing/place_drying_rack", item("dried_cannabis_leaf"), "drying_rack");
        placed("processing/craft_grinding_bowl", "onboarding/grinding_started", item("grinding_bowl"), block("grinding_bowl"));
        machine("processing/grind_first_material", "processing/craft_grinding_bowl", item("cannabis_powder"), "grinding_bowl");
        inventory("processing/portable_grinder", "processing/grind_first_material", item("portable_grinder"), item("portable_grinder"));
        placedAny("processing/craft_sieve", "processing/grind_first_material", item("sieve"), recipes("sieve"), block("sieve"));
        machine("processing/sieve_first_material", "processing/craft_sieve", item("cannabis_resin"), "sieve");
        placedAny("processing/craft_stomp_crafter", "processing/sieve_first_material", item("stomp_crafter"), recipes("stomp_crafter"), block("stomp_crafter"));
        inventoryAny("processing/stomp_plate", "processing/craft_stomp_crafter", item("stomp_plate"), recipes("stomp_plate"), item("stomp_plate"));
        machine("processing/stomp_first_recipe", "processing/craft_stomp_crafter", item("hash_brick"), "stomp_crafter");
        placedAny("processing/craft_clay_vat", "processing/stomp_first_recipe", item("clay_vat"), recipes("clay_vat"), block("clay_vat"));
        placedAny("processing/craft_evaporation_tray", "processing/craft_clay_vat", item("evaporation_tray"), recipes("evaporation_tray"), block("evaporation_tray"));
        machine("processing/evaporate_first_output", "processing/craft_evaporation_tray", item("salt_powder"), "evaporation_tray");
        inventoryAny("processing/craft_roller", "onboarding/first_consumable", item("roller"), recipes("roller"), item("roller"));
        inventory("processing/roll_first_item", "processing/craft_roller", item("joint"), item("joint"), item("cigaret"));
        inventoryAny("processing/craft_bang", "processing/roll_first_item", item("bang"), recipes("bang"), item("bang"));
        inventory("processing/craft_syringe", "processing/craft_bang", item("syringe"), item("syringe"));
        inventoryAny("processing/fill_glass_bottle", "processing/craft_evaporation_tray", item("glass_bottle"), recipes("glass_bottle"), item("glass_bottle"));
    }

    private void consumptionAndKnowledge() {
        drug("consumption/first_drug", "onboarding/first_consumable", item("cannabis_powder"), "first_drug", "Knowledge Leaves a Mark", null, null, null, false);
        drug("consumption/first_smoked", "consumption/first_drug", item("joint"), "first_smoked", null, null, "smoked", null, false);
        drug("consumption/first_eaten", "consumption/first_drug", item("magic_mushroom"), "first_eaten", null, null, "eaten", null, false);
        drug("consumption/first_sniffed", "consumption/first_drug", item("cocaine_powder"), "first_sniffed", null, null, "sniffed", null, true);
        drug("consumption/first_injected", "consumption/first_drug", item("syringe"), "first_injected", null, null, "injected", null, true);
        drug("consumption/first_bang", "consumption/first_smoked", item("bang"), "first_bang", null, null, null, "bang", false);
        drug("consumption/first_rolled_item", "processing/roll_first_item", item("joint"), "first_rolled_item", null, null, null, "rolled_item", false);
        drug("consumption/first_bottle", "processing/fill_glass_bottle", item("glass_bottle"), "first_bottle", null, null, null, "bottle", true);
        drug("knowledge/first_cannabinoid", "consumption/first_drug", item("cannabis_powder"), "first_cannabinoid", null, "cannabinoid", null, null, false);
        drug("knowledge/first_stimulant", "consumption/first_drug", item("cocaine_powder"), "first_stimulant", null, "stimulant", null, null, true);
        drug("knowledge/first_psychedelic", "consumption/first_drug", item("magic_mushroom"), "first_psychedelic", null, "psychedelic", null, null, false);
        drug("knowledge/first_depressant", "consumption/first_drug", item("glass_bottle"), "first_depressant", null, "depressant", null, null, true);
        drug("knowledge/first_nicotinic", "consumption/first_drug", item("tobacco_handful"), "first_nicotinic", null, "nicotinic", null, null, false);
        drug("knowledge/first_alcohol", "knowledge/first_depressant", item("glass_bottle"), "first_alcohol", null, null, null, null, true, "alcohol");
        drug("knowledge/first_high_value_psychotrope", "psychotrope/psychotrope_core", item("meth_shard"), "first_high_value_psychotrope", null, null, null, null, true, "meth");
        psyKnowledge("knowledge/nicotinic", "knowledge/first_nicotinic", item("psy_receptacle"), "mydrugs:nicotinic", false);
        psyKnowledge("knowledge/cannabinoid", "knowledge/nicotinic", item("cannabis_powder"), "mydrugs:cannabinoid", false);
        psyKnowledge("knowledge/fermented", "knowledge/cannabinoid", item("glass_bottle"), "mydrugs:fermented", false);
        psyKnowledge("knowledge/stimulant", "knowledge/fermented", item("cocaine_powder"), "mydrugs:stimulant", false);
        psyKnowledge("knowledge/lysergic", "knowledge/stimulant", item("lsd_drop"), "mydrugs:lysergic", false);
        psyKnowledge("knowledge/overclocked", "knowledge/lysergic", item("meth_shard"), "mydrugs:overclocked", false);
        psyKnowledge("knowledge/mycelial", "knowledge/overclocked", item("magic_mushroom"), "mydrugs:mycelial", false);
    }

    private void recovery() {
        inventory("recovery/personal_diary", "onboarding/support_tools", item("personal_diary"), item("personal_diary"));
        recoveryAction("recovery/use_personal_diary", "recovery/personal_diary", item("personal_diary"), "personal_diary");
        inventory("recovery/headphones", "recovery/personal_diary", item("headphones"), item("headphones"));
        recoveryAction("recovery/use_headphones", "recovery/headphones", item("headphones"), "headphones");
        inventory("recovery/herbal_tea", "recovery/personal_diary", item("herbal_tea"), item("herbal_tea"));
        inventory("recovery/calming_mixture", "recovery/herbal_tea", item("calming_mixture"), item("calming_mixture"));
        inventory("recovery/sleeping_aid", "recovery/calming_mixture", item("sleeping_aid"), item("sleeping_aid"));
        inventory("recovery/overdose_antidote", "recovery/calming_mixture", item("overdose_antidote"), item("overdose_antidote"), true);
        placedAny("recovery/therapist_desk", "recovery/headphones", item("therapist_desk"), recipes("therapist_desk"), block("therapist_desk"));
        recoveryAction("recovery/therapy_used", "recovery/therapist_desk", item("therapist_desk"), "therapy");
        placedAny("recovery/recovery_anchor", "recovery/therapist_desk", item("recovery_anchor"), recipes("recovery_anchor"), block("recovery_anchor"));
        recoveryAction("recovery/safe_zone", "recovery/recovery_anchor", item("recovery_anchor"), "safe_zone");
        recoveryAction("recovery/overdose_survived", "recovery/overdose_antidote", item("overdose_antidote"), "overdose_antidote", true);
    }

    private void materials() {
        inventoryAny("materials/raw_aluminium", "onboarding/first_processing_station", item("raw_aluminium"), List.of(), item("raw_aluminium"), item("aluminium_ore"), item("deepslate_aluminium_ore"));
        inventory("materials/aluminium_ingot", "materials/raw_aluminium", item("aluminium_ingot"), item("aluminium_ingot"));
        inventoryAny("materials/raw_platinum", "materials/aluminium_ingot", item("raw_platinum"), List.of(), item("raw_platinum"), item("platinum_ore"), item("deepslate_platinum_ore"));
        inventory("materials/platinum_ingot", "materials/raw_platinum", item("platinum_ingot"), item("platinum_ingot"));
        inventoryAny("materials/sulfur", "materials/raw_aluminium", item("sulfur_powder"), List.of(), item("sulfur_powder"), item("sulfur_ore"), item("deepslate_sulfur_ore"));
        inventory("materials/salt", "materials/sulfur", item("salt_powder"), item("salt_powder"));
        inventory("materials/steel_blend", "materials/aluminium_ingot", item("steel_blend"), item("steel_blend"));
        inventory("materials/steel_ingot", "materials/steel_blend", item("steel_ingot"), item("steel_ingot"));
        inventory("materials/thick_glass", "materials/steel_ingot", item("thick_glass"), item("thick_glass"));
        inventory("materials/control_circuit", "materials/aluminium_ingot", item("control_circuit"), item("control_circuit"));
        inventory("materials/advanced_control_circuit", "materials/control_circuit", item("advanced_control_circuit"), item("advanced_control_circuit"));
    }

    private void machines() {
        placedAny("machines/advanced_furnace", "materials/steel_ingot", item("advanced_furnace"), recipes("advanced_furnace"), block("advanced_furnace"));
        placedAny("machines/mixing_vat", "processing/craft_clay_vat", item("mixing_vat"), recipes("mixing_vat"), block("mixing_vat"));
        placedAny("machines/distiller", "machines/mixing_vat", item("distiller"), recipes("distiller"), block("distiller"));
        placedAny("machines/fluid_filterer", "machines/distiller", item("fluid_filterer"), recipes("fluid_filterer"), block("fluid_filterer"));
        placedAny("machines/centrifuge", "machines/fluid_filterer", item("centrifuge"), recipes("centrifuge"), block("centrifuge"));
        placedAny("machines/chemical_reactor", "machines/centrifuge", item("chemical_reactor"), recipes("chemical_reactor"), block("chemical_reactor"));
        placedAny("machines/advanced_mixing_vat", "machines/chemical_reactor", item("advanced_mixing_vat"), recipes("advanced_mixing_vat"), true, block("advanced_mixing_vat"));
        placedAny("machines/electrolyzer", "machines/chemical_reactor", item("electrolyzer"), recipes("electrolyzer"), true, block("electrolyzer"));
        placedAny("machines/gasifier", "machines/electrolyzer", item("gasifier"), recipes("gasifier"), true, block("gasifier"));
        placedAny("machines/steam_cracker", "machines/gasifier", item("steam_cracker"), recipes("steam_cracker"), true, block("steam_cracker"));
        placedAny("machines/aromatic_extractor", "machines/steam_cracker", item("aromatic_extractor"), recipes("aromatic_extractor"), true, block("aromatic_extractor"));
        placedAny("machines/btx_fractionation_tower", "machines/aromatic_extractor", item("btx_fractionation_tower"), recipes("btx_fractionation_tower"), true, block("btx_fractionation_tower"));
        placedAny("machines/catalytic_reformer", "machines/btx_fractionation_tower", item("catalytic_reformer"), recipes("catalytic_reformer"), true, block("catalytic_reformer"));
        placedAny("machines/growth_chamber", "agriculture/fungal_culture", item("growth_chamber"), recipes("growth_chamber"), block("growth_chamber"));
        placedAny("machines/biochemical_reactor", "machines/growth_chamber", item("biochemical_reactor"), recipes("biochemical_reactor"), true, block("biochemical_reactor"));
        inventoryAll("machines/all_basic_machines", "machines/chemical_reactor", item("chemical_reactor"), "goal",
                item("advanced_furnace"), item("mixing_vat"), item("distiller"), item("fluid_filterer"), item("centrifuge"), item("chemical_reactor"));
        inventoryAll("machines/all_advanced_machines", "machines/catalytic_reformer", item("catalytic_reformer"), "challenge",
                item("advanced_mixing_vat"), item("electrolyzer"), item("gasifier"), item("steam_cracker"), item("aromatic_extractor"), item("btx_fractionation_tower"), item("catalytic_reformer"), item("growth_chamber"), item("biochemical_reactor"));
    }

    private void fluidsAndGases() {
        inventory("fluids/first_custom_fluid", "machines/mixing_vat", item("glass_bottle"), item("glass_bottle"));
        machine("fluids/distill_first_fluid", "machines/distiller", item("distiller"), "distiller");
        inventoryAny("fluids/use_fluid_filter", "machines/fluid_filterer", item("fluid_filter"), recipes("fluid_filter"), item("fluid_filter"));
        machine("fluids/industrial_fluid_chain", "machines/catalytic_reformer", item("btx_fractionation_tower"), "btx_fractionation_tower", true);
        machine("gases/first_gas", "machines/gasifier", item("gasifier"), "gasifier", true);
        inventoryAny("gases/gas_tank", "gases/first_gas", item("gas_tank"), recipes("gas_tank"), item("gas_tank"));
        placedAny("gases/gas_pump", "gases/gas_tank", item("gas_pump"), recipes("gas_pump"), block("gas_pump"));
        machine("gases/process_first_gas", "machines/electrolyzer", item("electrolyzer"), "electrolyzer", true);
        inventory("gases/toxic_gas_handling", "gases/process_first_gas", item("gas_tank"), item("gas_tank"), true);
    }

    private void logistics() {
        inventoryAny("logistics/basic_item_pipe", "materials/control_circuit", item("basic_item_pipe"), recipes("basic_item_pipe"), item("basic_item_pipe"));
        inventoryAny("logistics/basic_fluid_pipe", "logistics/basic_item_pipe", item("basic_fluid_pipe"), recipes("basic_fluid_pipe"), item("basic_fluid_pipe"));
        inventoryAny("logistics/basic_gas_pipe", "logistics/basic_fluid_pipe", item("basic_gas_pipe"), recipes("basic_gas_pipe"), item("basic_gas_pipe"));
        inventoryAny("logistics/fast_pipe", "logistics/basic_gas_pipe", item("fast_item_pipe"), recipes("fast_item_pipe", "fast_fluid_pipe", "fast_gas_pipe"), item("fast_item_pipe"), item("fast_fluid_pipe"), item("fast_gas_pipe"));
        inventory("logistics/pipe_wrench", "logistics/basic_item_pipe", item("pipe_wrench"), item("pipe_wrench"));
        inventory("logistics/filter_upgrade", "logistics/pipe_wrench", item("pipe_filter_upgrade"), item("pipe_filter_upgrade"));
        inventory("logistics/machine_transfer_upgrade", "logistics/filter_upgrade", item("machine_transfer_upgrade"), item("machine_transfer_upgrade"));
        inventoryAny("logistics/automation_upgrade", "logistics/machine_transfer_upgrade", item("automation_upgrade"), recipes("automation_upgrade"), item("automation_upgrade"));
        inventoryAny("logistics/energy_upgrade", "logistics/automation_upgrade", item("energy_upgrade"), recipes("energy_upgrade"), item("energy_upgrade"));
        placedAny("logistics/fluid_pump", "logistics/basic_fluid_pipe", item("fluid_pump"), recipes("fluid_pump"), block("fluid_pump"));
    }

    private void psychotrope() {
        inventoryAny("psy_anvil/obtain_receptacle", "knowledge/nicotinic", item("psy_receptacle"), recipes("psy_anvil"), item("psy_receptacle"));
        placedAny("psy_anvil/craft_psy_anvil", "psy_anvil/obtain_receptacle", item("psy_anvil"), recipes("copper_plate", "heavy_iron", "heavy_iron_plate", "insulated_wire", "advanced_control_circuit", "mycelial_resonator"), block("psy_anvil"));
        psyAnvilCraft("psy_anvil/shape_copper_plate", "knowledge/cannabinoid", item("copper_plate"), "mydrugs:copper_plate");
        psyAnvilCraft("psy_anvil/shape_heavy_iron", "knowledge/fermented", item("heavy_iron"), "mydrugs:heavy_iron");
        psyAnvilCraft("psy_anvil/shape_insulated_wire", "knowledge/stimulant", item("insulated_wire"), "mydrugs:insulated_wire");
        psyAnvilCraft("psy_anvil/shape_advanced_control_circuit", "knowledge/lysergic", item("advanced_control_circuit"), "mydrugs:advanced_control_circuit");
        psyAnvilCraft("psy_anvil/build_resonator", "knowledge/overclocked", item("mycelial_resonator"), "mydrugs:mycelial_resonator");
        inventoryAny("psychotrope/psychotrope_lens", "materials/advanced_control_circuit", item("psychotrope_lens"), recipes("psychotrope_lens"), item("psychotrope_lens"));
        placedAny("psychotrope/psychotrope_component", "psychotrope/psychotrope_lens", item("psychotrope_component"), recipes("psychotrope_component"), block("psychotrope_component"));
        placedAny("psychotrope/psychotrope_core", "psychotrope/psychotrope_component", item("psychotrope_core"), recipes("psychotrope_core"), block("psychotrope_core"));
        psychotrope("psychotrope/multiblock_formed", "psychotrope/psychotrope_core", item("psychotrope_core"), "multiblock_formed", false, 0);
        psychotrope("psychotrope/first_energy", "psychotrope/multiblock_formed", item("psychotrope_lens"), "energy_generated", false, 1);
        psychotrope("psychotrope/energy_threshold_1", "psychotrope/first_energy", item("energy_upgrade"), "energy_generated", true, 1000);
        psychotrope("psychotrope/energy_threshold_2", "psychotrope/energy_threshold_1", item("automation_upgrade"), "energy_generated", true, 10000);
        psychotrope("psychotrope/power_machine", "psychotrope/first_energy", item("energy_upgrade"), "powered_machine", false, 0);
        drug("psychotrope/high_value_input", "psychotrope/first_energy", item("meth_shard"), "high_value_input", null, null, null, "psychotrope", true);
    }

    private void challenges() {
        inventoryAll("challenges/complete_crop_branch", "agriculture/psychedelic_biome", item("rye"), "goal",
                item("cannabis_leaf"), item("tobacco_leaf"), item("coca_leaf"), item("rye"), item("malt"), item("magic_mushroom"));
        inventoryAll("challenges/complete_recovery_branch", "recovery/safe_zone", item("recovery_anchor"), "goal",
                item("personal_diary"), item("headphones"), item("herbal_tea"), item("calming_mixture"), item("sleeping_aid"), item("overdose_antidote"), item("recovery_anchor"));
        inventoryAll("challenges/complete_logistics_branch", "logistics/energy_upgrade", item("automation_upgrade"), "goal",
                item("basic_item_pipe"), item("basic_fluid_pipe"), item("basic_gas_pipe"), item("pipe_wrench"), item("pipe_filter_upgrade"), item("machine_transfer_upgrade"), item("automation_upgrade"), item("energy_upgrade"));
        inventoryAll("challenges/complete_machine_branch", "machines/all_advanced_machines", item("catalytic_reformer"), "challenge",
                item("advanced_furnace"), item("mixing_vat"), item("distiller"), item("fluid_filterer"), item("centrifuge"), item("chemical_reactor"), item("advanced_mixing_vat"), item("electrolyzer"), item("gasifier"), item("steam_cracker"), item("aromatic_extractor"), item("btx_fractionation_tower"), item("catalytic_reformer"), item("growth_chamber"), item("biochemical_reactor"));
        drugAllCategories("challenges/consume_all_categories", "knowledge/first_nicotinic", item("joint"));
        psychotrope("challenges/complete_psychotrope_branch", "psychotrope/energy_threshold_2", item("psychotrope_core"), "powered_machine", true, 0);
        inventoryAll("challenges/deep_production", "machines/catalytic_reformer", item("meth_shard"), "challenge",
                item("hash_piece"), item("cocaine_powder"), item("meth_shard"), item("magic_mushroom_powder"));
        inventoryAll("challenges/full_mod_progression", "challenges/complete_machine_branch", item("psychotrope_core"), "challenge",
                item("recovery_anchor"), item("psychotrope_core"), item("catalytic_reformer"), item("automation_upgrade"), item("joint"));
    }

    private void root(String path, String icon) {
        JsonObject advancement = base(path, null, icon, "task", false, false, false);
        JsonObject criteria = new JsonObject();
        criteria.add("tick", criterion("minecraft:tick", null));
        advancement.add("criteria", criteria);
        output.accept(ModAdvancementIds.id(path), advancement);
    }

    private void inventory(String path, String parent, String icon, String item) {
        inventory(path, parent, icon, item, false);
    }

    private void inventory(String path, String parent, String icon, String item, boolean hidden) {
        inventoryAny(path, parent, icon, List.of(), hidden, item);
    }

    private void inventory(String path, String parent, String icon, String itemA, String itemB) {
        inventoryAny(path, parent, icon, List.of(), itemA, itemB);
    }

    private void inventoryAny(String path, String parent, String icon, List<String> rewards, String... items) {
        inventoryAny(path, parent, icon, rewards, false, items);
    }

    private void inventoryAny(String path, String parent, String icon, List<String> rewards, boolean hidden, String... items) {
        JsonObject advancement = base(path, parent, icon, "task", hidden, true, false);
        JsonObject criteria = new JsonObject();
        JsonArray group = new JsonArray();
        for (int i = 0; i < items.length; i++) {
            String name = "has_" + i;
            criteria.add(name, inventoryCriterion(items[i]));
            group.add(name);
        }
        advancement.add("criteria", criteria);
        advancement.add("requirements", requirements(List.of(group)));
        rewards(advancement, rewards);
        output.accept(ModAdvancementIds.id(path), advancement);
    }

    private void inventoryAll(String path, String parent, String icon, String frame, String... items) {
        JsonObject advancement = base(path, parent, icon, frame, "challenge".equals(frame), true, "challenge".equals(frame));
        JsonObject criteria = new JsonObject();
        List<JsonArray> groups = new java.util.ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            String name = "has_" + i;
            criteria.add(name, inventoryCriterion(items[i]));
            JsonArray group = new JsonArray();
            group.add(name);
            groups.add(group);
        }
        advancement.add("criteria", criteria);
        advancement.add("requirements", requirements(groups));
        output.accept(ModAdvancementIds.id(path), advancement);
    }

    private void placed(String path, String parent, String icon, String block) {
        placed(path, parent, icon, block, false);
    }

    private void placed(String path, String parent, String icon, String block, boolean hidden) {
        placedAny(path, parent, icon, List.of(), hidden, block);
    }

    private void placedAny(String path, String parent, String icon, List<String> rewards, String... blocks) {
        placedAny(path, parent, icon, rewards, false, blocks);
    }

    private void placedAny(String path, String parent, String icon, List<String> rewards, boolean hidden, String... blocks) {
        JsonObject advancement = base(path, parent, icon, "task", hidden, true, false);
        JsonObject criteria = new JsonObject();
        JsonArray group = new JsonArray();
        for (int i = 0; i < blocks.length; i++) {
            String name = "placed_" + i;
            criteria.add(name, placedBlockCriterion(blocks[i]));
            group.add(name);
        }
        advancement.add("criteria", criteria);
        advancement.add("requirements", requirements(List.of(group)));
        rewards(advancement, rewards);
        output.accept(ModAdvancementIds.id(path), advancement);
    }

    private void machine(String path, String parent, String icon, String machine) {
        machine(path, parent, icon, machine, false);
    }

    private void machine(String path, String parent, String icon, String machine, boolean hidden) {
        JsonObject conditions = new JsonObject();
        conditions.addProperty("machine", block(machine));
        custom(path, parent, icon, "mydrugs:machine_recipe_completed", conditions, hidden, "task");
    }

    private void recoveryAction(String path, String parent, String icon, String action) {
        recoveryAction(path, parent, icon, action, false);
    }

    private void recoveryAction(String path, String parent, String icon, String action, boolean hidden) {
        JsonObject conditions = new JsonObject();
        conditions.addProperty("action", action);
        custom(path, parent, icon, "mydrugs:recovery_action", conditions, hidden, "task");
    }

    private void drug(String path, String parent, String icon, String criterionName, @Nullable String ignoredTitle,
                      @Nullable String category, @Nullable String route, @Nullable String source, boolean hidden) {
        drug(path, parent, icon, criterionName, ignoredTitle, category, route, source, hidden, null);
    }

    private void drug(String path, String parent, String icon, String criterionName, @Nullable String ignoredTitle,
                      @Nullable String category, @Nullable String route, @Nullable String source, boolean hidden,
                      @Nullable String drug) {
        JsonObject advancement = base(path, parent, icon, "task", hidden, true, false);
        JsonObject conditions = new JsonObject();
        if (drug != null) conditions.addProperty("drug", drug);
        if (category != null) conditions.addProperty("category", category);
        if (route != null) conditions.addProperty("route", route);
        if (source != null) conditions.addProperty("source", source);
        JsonObject criteria = new JsonObject();
        criteria.add(criterionName, criterion("mydrugs:drug_consumed", conditions));
        advancement.add("criteria", criteria);
        output.accept(ModAdvancementIds.id(path), advancement);
    }

    private void drugAllCategories(String path, String parent, String icon) {
        JsonObject advancement = base(path, parent, icon, "challenge", true, true, true);
        JsonObject criteria = new JsonObject();
        List<JsonArray> groups = new java.util.ArrayList<>();
        String[] categories = {"cannabinoid", "stimulant", "psychedelic", "depressant", "nicotinic"};
        for (String category : categories) {
            JsonObject conditions = new JsonObject();
            conditions.addProperty("category", category);
            criteria.add(category, criterion("mydrugs:drug_consumed", conditions));
            JsonArray group = new JsonArray();
            group.add(category);
            groups.add(group);
        }
        advancement.add("criteria", criteria);
        advancement.add("requirements", requirements(groups));
        output.accept(ModAdvancementIds.id(path), advancement);
    }

    private void psyKnowledge(String path, String parent, String icon, String knowledge, boolean hidden) {
        JsonObject conditions = new JsonObject();
        conditions.addProperty("knowledge", knowledge);
        custom(path, parent, icon, "mydrugs:psy_knowledge_unlocked", conditions, hidden, "task");
    }

    private void psyAnvilCraft(String path, String parent, String icon, String resultItem) {
        JsonObject conditions = new JsonObject();
        conditions.addProperty("machine", block("psy_anvil"));
        conditions.addProperty("result_item", resultItem);
        custom(path, parent, icon, "mydrugs:machine_recipe_completed", conditions, false, "task");
    }

    private void psychotrope(String path, String parent, String icon, String event, boolean hidden, int threshold) {
        JsonObject conditions = new JsonObject();
        conditions.addProperty("event", event);
        if (threshold > 0) {
            conditions.addProperty("threshold", threshold);
        }
        custom(path, parent, icon, "mydrugs:psychotrope_energy", conditions, hidden, hidden ? "goal" : "task");
    }

    private void custom(String path, String parent, String icon, String trigger, JsonObject conditions, boolean hidden, String frame) {
        JsonObject advancement = base(path, parent, icon, frame, hidden, true, "challenge".equals(frame));
        JsonObject criteria = new JsonObject();
        criteria.add("event", criterion(trigger, conditions));
        advancement.add("criteria", criteria);
        output.accept(ModAdvancementIds.id(path), advancement);
    }

    private JsonObject base(String path, @Nullable String parent, String icon, String frame, boolean hidden,
                            boolean showToast, boolean announce) {
        JsonObject root = new JsonObject();
        if (parent != null) {
            root.addProperty("parent", MyDrugs.MODID + ":" + parent);
        }
        JsonObject display = new JsonObject();
        JsonObject iconObject = new JsonObject();
        iconObject.addProperty("id", icon);
        display.add("icon", iconObject);
        display.add("title", translate(ModAdvancementIds.translationKey(path, "title")));
        display.add("description", translate(ModAdvancementIds.translationKey(path, "description")));
        if (parent == null) {
            display.addProperty("background", "minecraft:textures/block/dirt.png");
        }
        display.addProperty("frame", frame);
        display.addProperty("show_toast", showToast);
        display.addProperty("announce_to_chat", announce);
        display.addProperty("hidden", hidden);
        root.add("display", display);
        return root;
    }

    private JsonObject inventoryCriterion(String item) {
        JsonObject itemPredicate = new JsonObject();
        itemPredicate.addProperty("items", item);
        JsonArray items = new JsonArray();
        items.add(itemPredicate);
        JsonObject conditions = new JsonObject();
        conditions.add("items", items);
        return criterion("minecraft:inventory_changed", conditions);
    }

    private JsonObject placedBlockCriterion(String block) {
        JsonObject blockPredicate = new JsonObject();
        blockPredicate.addProperty("condition", "minecraft:block_state_property");
        blockPredicate.addProperty("block", block);
        JsonArray location = new JsonArray();
        location.add(blockPredicate);
        JsonObject conditions = new JsonObject();
        conditions.add("location", location);
        return criterion("minecraft:placed_block", conditions);
    }

    private JsonObject criterion(String trigger, @Nullable JsonObject conditions) {
        JsonObject criterion = new JsonObject();
        criterion.addProperty("trigger", trigger);
        if (conditions != null && !conditions.isEmpty()) {
            criterion.add("conditions", conditions);
        }
        return criterion;
    }

    private JsonObject translate(String key) {
        JsonObject object = new JsonObject();
        object.addProperty("translate", key);
        return object;
    }

    private JsonArray requirements(List<JsonArray> groups) {
        JsonArray requirements = new JsonArray();
        for (JsonArray group : groups) {
            requirements.add(group);
        }
        return requirements;
    }

    private void rewards(JsonObject advancement, List<String> recipePaths) {
        if (recipePaths.isEmpty()) {
            return;
        }
        JsonObject rewards = new JsonObject();
        JsonArray recipes = new JsonArray();
        for (String path : recipePaths) {
            recipes.add(MyDrugs.MODID + ":" + path);
        }
        rewards.add("recipes", recipes);
        advancement.add("rewards", rewards);
    }

    private List<String> recipes(String... paths) {
        return List.of(paths);
    }

    private String item(String path) {
        return MyDrugs.MODID + ":" + path;
    }

    private String block(String path) {
        return MyDrugs.MODID + ":" + path;
    }

}
