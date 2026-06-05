package org.mydrugs.mydrugs.audit;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bootstrap-free machine descriptor audit: every machine that already has systems must be described
 * in {@link org.mydrugs.mydrugs.blocks.ModMachineContent}, have its block registered, and carry the
 * expected localization key.
 */
class MachineDescriptorConsistencyTest {
    private static final Path BLOCKS = Path.of("src/main/java/org/mydrugs/mydrugs/blocks/ModBlocks.java");
    private static final Path CONTENT = Path.of("src/main/java/org/mydrugs/mydrugs/blocks/ModMachineContent.java");
    private static final Path LANG = Path.of("src/main/resources/assets/mydrugs/lang/en_us.json");

    /** machine id -> backing block id (usually identical; psy_mixer is formed from its core block). */
    private static final Map<String, String> MACHINES = new LinkedHashMap<>();

    static {
        for (String id : new String[]{
                "advanced_furnace", "distiller", "mixing_vat", "sieve", "fluid_filterer",
                "evaporation_tray", "centrifuge", "btx_fractionation_tower", "aromatic_extractor",
                "electrolyzer", "growth_chamber", "gene_extractor", "crispr_cas9_combinator",
                "bacterial_incubator", "hemogenic_infuser", "autoclave", "biochemical_reactor",
                "gasifier", "chemical_reactor", "advanced_mixing_vat", "catalytic_reformer",
                "steam_cracker", "psychotrope_distillery", "psy_anvil"}) {
            MACHINES.put(id, id);
        }
        MACHINES.put("psy_mixer", "formed_psy_mixer_core");
    }

    @Test
    void everyMachineIsDescribedRegisteredAndLocalized() {
        String content = SourceIndex.read(CONTENT);
        String blocks = SourceIndex.read(BLOCKS);
        String lang = SourceIndex.read(LANG);

        for (Map.Entry<String, String> entry : MACHINES.entrySet()) {
            String machineId = entry.getKey();
            String blockId = entry.getValue();

            assertTrue(SourceIndex.containsLiteral(content, machineId),
                    "Machine '" + machineId + "' is missing from ModMachineContent.");
            assertTrue(SourceIndex.containsLiteral(blocks, blockId),
                    "Machine '" + machineId + "' block '" + blockId + "' is not registered in ModBlocks.");
            assertTrue(lang.contains("\"block.mydrugs." + blockId + "\""),
                    "Machine '" + machineId + "' is missing localization key block.mydrugs." + blockId);
        }
    }
}
