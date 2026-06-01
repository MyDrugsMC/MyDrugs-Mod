package org.mydrugs.mydrugs.guide;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineHandbookResourceTest {
    private static final Path GUIDE_SOURCE = Path.of("docs/progression_guide_pages.md");
    private static final Path GUIDE_JSON = Path.of("src/main/resources/assets/mydrugs/guide/pages.json");
    private static final Path LANG_JSON = Path.of("src/main/resources/assets/mydrugs/lang/en_us.json");

    private static final List<String> HANDBOOK_PAGES = List.of(
            "Machine Handbook",
            "Machine Handbook - Early Processing",
            "Machine Handbook - Crude Machine Era",
            "Machine Handbook - Chemical Machine Era",
            "Machine Handbook - Psy Current Era",
            "Machine Handbook - Ritual and Integration Era"
    );

    private static final List<String> MACHINE_HEADINGS = List.of(
            "Manual Coffee Pulper",
            "Coffee Drying Mat",
            "Drying Rack",
            "Mortar",
            "Sieve",
            "Stomp Crafter",
            "Clay Vat",
            "Mixing Vat",
            "Advanced Furnace",
            "Distiller",
            "Centrifuge",
            "Fluid Filterer",
            "Evaporation Tray",
            "Advanced Mixing Vat",
            "Fluid Pump",
            "Biochemical Reactor",
            "Growth Chamber",
            "Chemical Reactor",
            "Gasifier",
            "Electrolyzer",
            "Gas Tank",
            "Gas Pump",
            "Steam Cracker",
            "Catalytic Reformer",
            "Aromatic Extractor",
            "BTX Fractionation Tower",
            "Reduction Still",
            "Psychotrope Distillery",
            "Distillate Engine",
            "Psy Anvil",
            "Psy Mixer",
            "Psychotrope Resonator",
            "Gene Extractor",
            "CRISPR-Cas9 Combinator",
            "Bacterial Incubator",
            "Hemogenic Infuser",
            "Autoclave"
    );

    @Test
    void generatedGuideContainsMachineHandbookPagesInOrder() throws IOException {
        List<String> titles = generatedPageTitles();

        int machineEra = titles.indexOf("Machine Era");
        int supportMaterials = titles.indexOf("Support Materials");
        assertTrue(machineEra >= 0, "Machine Era page should still exist");
        assertTrue(supportMaterials >= 0, "Support Materials page should still exist");

        int previous = machineEra;
        for (String page : HANDBOOK_PAGES) {
            int current = titles.indexOf(page);
            assertTrue(current > previous, page + " should appear after the previous handbook page");
            assertTrue(current < supportMaterials, page + " should stay near the Machine Era chapter");
            previous = current;
        }
    }

    @Test
    void sourceDocumentsEveryMajorMachineWithConceptualFields() throws IOException {
        String source = read(GUIDE_SOURCE);

        for (String machine : MACHINE_HEADINGS) {
            String entry = handbookEntry(source, machine);
            assertTrue(entry.contains("Era:"), machine + " should describe its era");
            assertTrue(entry.contains("Purpose:"), machine + " should describe its purpose");
            assertTrue(entry.contains("Handles:"), machine + " should describe handled categories");
            assertTrue(entry.contains("Common blockers:"), machine + " should describe common blockers");
            assertTrue(entry.contains("Automation:"), machine + " should describe automation guidance");
            assertTrue(entry.contains("See also:"), machine + " should include related guide systems");
        }
    }

    @Test
    void generatedGuideContainsEveryMachineHeading() throws IOException {
        String guideJson = read(GUIDE_JSON);

        for (String machine : MACHINE_HEADINGS) {
            assertTrue(guideJson.contains("\"text\": \"" + machine + "\""), machine + " should be generated into pages.json");
        }
    }

    @Test
    void handbookStaysConceptualInsteadOfRecipeLevel() throws IOException {
        String source = read(GUIDE_SOURCE);
        String handbook = source.substring(source.indexOf("# Machine Handbook"));

        assertFalse(handbook.contains("Recipe:"), "Handbook should not include recipe blocks");
        assertFalse(handbook.contains("Crafting recipe"), "Handbook should not duplicate crafting recipes");
        assertFalse(handbook.contains("Shaped:"), "Handbook should not encode shaped recipes");
    }

    @Test
    void statusHintLocalizationExists() throws IOException {
        String lang = read(LANG_JSON);

        assertTrue(lang.contains("\"screen.mydrugs.machine_handbook.status_hint\""));
        assertTrue(lang.contains("See Machine Handbook."));
    }

    private static List<String> generatedPageTitles() throws IOException {
        JsonObject root = JsonParser.parseString(read(GUIDE_JSON)).getAsJsonObject();
        JsonArray pages = root.getAsJsonArray("pages");
        List<String> titles = new ArrayList<>(pages.size());
        for (int i = 0; i < pages.size(); i++) {
            titles.add(pages.get(i).getAsJsonObject().get("title").getAsString());
        }
        return titles;
    }

    private static String handbookEntry(String source, String heading) {
        String marker = "## " + heading;
        int start = source.indexOf(marker);
        assertTrue(start >= 0, heading + " should have a handbook heading");

        int nextHeading = source.indexOf("\n## ", start + marker.length());
        int nextPage = source.indexOf("\n---", start + marker.length());
        int end = minPositive(nextHeading, nextPage, source.length());
        return source.substring(start, end);
    }

    private static int minPositive(int first, int second, int fallback) {
        int value = fallback;
        if (first >= 0) {
            value = Math.min(value, first);
        }
        if (second >= 0) {
            value = Math.min(value, second);
        }
        return value;
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path);
    }
}
