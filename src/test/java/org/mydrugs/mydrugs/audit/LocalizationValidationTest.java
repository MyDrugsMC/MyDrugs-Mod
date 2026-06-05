package org.mydrugs.mydrugs.audit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validates that {@code en_us.json} is well-formed and that the creative tabs and fluid/gas metadata
 * labels added by this polish pass are localized.
 */
class LocalizationValidationTest {
    private static final Path LANG = Path.of("src/main/resources/assets/mydrugs/lang/en_us.json");

    private static final String[] CREATIVE_TAB_KEYS = {
            "itemGroup.mydrugs.main",
            "itemGroup.mydrugs.machines",
            "itemGroup.mydrugs.materials",
            "itemGroup.mydrugs.plants",
            "itemGroup.mydrugs.fluids_and_gases",
            "itemGroup.mydrugs.recovery_and_psyche",
            "itemGroup.mydrugs.food_and_consumables"
    };

    private static final String[] METADATA_KEYS = {
            "tooltip.mydrugs.gas.hazard", "tooltip.mydrugs.gas.flammable",
            "tooltip.mydrugs.fluid.hazard", "tooltip.mydrugs.fluid.role", "tooltip.mydrugs.fluid.phase",
            "hazard.mydrugs.safe", "hazard.mydrugs.irritant", "hazard.mydrugs.toxic",
            "hazard.mydrugs.corrosive", "hazard.mydrugs.flammable",
            "fluid_role.mydrugs.raw_input", "fluid_role.mydrugs.intermediate", "fluid_role.mydrugs.product",
            "fluid_role.mydrugs.byproduct", "fluid_role.mydrugs.waste",
            "fluid_phase.mydrugs.aqueous", "fluid_phase.mydrugs.oil", "fluid_phase.mydrugs.slurry",
            "fluid_phase.mydrugs.solvent", "fluid_phase.mydrugs.biological", "fluid_phase.mydrugs.alcoholic",
            "fluid_phase.mydrugs.other"
    };

    private static JsonObject lang() {
        return JsonParser.parseString(SourceIndex.read(LANG)).getAsJsonObject();
    }

    @Test
    void langIsWellFormedAndAllValuesAreStrings() {
        JsonObject lang = lang();
        assertTrue(lang.size() > 0, "en_us.json should not be empty");
        for (Map.Entry<String, com.google.gson.JsonElement> entry : lang.entrySet()) {
            assertTrue(entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString(),
                    "Localization value for '" + entry.getKey() + "' must be a string");
        }
    }

    @Test
    void creativeTabKeysExist() {
        JsonObject lang = lang();
        for (String key : CREATIVE_TAB_KEYS) {
            assertTrue(lang.has(key), "Missing creative tab localization: " + key);
        }
    }

    @Test
    void metadataLabelKeysExist() {
        JsonObject lang = lang();
        for (String key : METADATA_KEYS) {
            assertTrue(lang.has(key), "Missing metadata label localization: " + key);
        }
    }
}
