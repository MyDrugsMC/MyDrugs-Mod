package org.mydrugs.mydrugs.audit;

import java.util.Map;
import java.util.Set;

final class ResourceExceptions {
    private ResourceExceptions() {
    }

    static final Map<String, String> ITEMS_WITHOUT_CLIENT_DEFINITIONS = Map.of();
    static final Map<String, String> ITEMS_WITHOUT_TRANSLATIONS = Map.of();
    static final Map<String, String> BLOCKS_WITHOUT_TRANSLATIONS = Map.of();

    static final Map<String, String> BLOCKS_WITHOUT_ITEMS = Map.ofEntries(
            Map.entry("aloe_vera_bush", "Wild worldgen bush drops aloe and seeds; it has no obtainable bush item."),
            Map.entry("cocaine_powder_pile", "Transient in-world powder pile with no item form."),
            Map.entry("formed_psy_mixer_core", "Formed multiblock internal, never obtainable as an item."),
            Map.entry("formed_psy_mixer_part", "Formed multiblock internal, never obtainable as an item."),
            Map.entry("vomit_splash", "Transient effect block with no item form.")
    );

    static final Map<String, String> BLOCKS_WITHOUT_LOOT = Map.ofEntries(
            Map.entry("cocaine_powder_pile", "Transient powder pile explicitly uses noLootTable()."),
            Map.entry("formed_psy_mixer_core", "Formed multiblock internal explicitly uses noLootTable()."),
            Map.entry("formed_psy_mixer_part", "Formed multiblock internal explicitly uses noLootTable()."),
            Map.entry("vomit_splash", "Transient effect block explicitly uses noLootTable().")
    );

    static final Map<String, String> RELEASE_TEXT_ALLOWLIST = Map.of(
            "screen.mydrugs.diary.write_placeholder",
            "Legitimate editable-text placeholder prompt shown inside the diary input field."
    );

    static Set<String> knownLegacyIds() {
        return Set.of("mydrugs:dryer", "mydrugs:psychotrope_core", "mydrugs:psychotrope_component");
    }
}
