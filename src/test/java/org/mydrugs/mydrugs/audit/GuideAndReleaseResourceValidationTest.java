package org.mydrugs.mydrugs.audit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuideAndReleaseResourceValidationTest {
    private static final ResourceIndex RESOURCES = ResourceIndex.load();
    private static final RegistrySourceIndex REGISTRIES = RegistrySourceIndex.load();
    private static final String GUIDE = "assets/mydrugs/guide/pages.json";
    private static final Pattern BAD_TEXT = Pattern.compile(
            "(?i)\\b(todo|fixme|placeholder|lorem|dummy|test string|discord)\\b|Ã|Â|â€|â†|â€¢|�"
                    + "|(?i)\\b(fuck|shit|bitch|asshole)\\b|[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}");

    @Test
    void generatedGuideMatchesItsCanonicalMarkdownSource() {
        JsonObject expected = GuideSourceParser.parse(SourceIndex.read(
                Path.of("docs/progression_guide_pages.md")));
        assertEquals(expected, RESOURCES.object(GUIDE),
                "Guide JSON is stale. Run tools/sync_progression_guide.ps1 after editing the canonical markdown.");
    }

    @Test
    void guideReferencesResolveAndLegacyIdsAreAbsent() {
        JsonArray pages = RESOURCES.object(GUIDE).getAsJsonArray("pages");
        Set<String> titles = new LinkedHashSet<>();
        List<String> errors = new ArrayList<>();
        for (JsonElement pageElement : pages) {
            JsonObject page = pageElement.getAsJsonObject();
            String title = page.get("title").getAsString();
            if (!titles.add(title)) {
                errors.add("duplicate page title: " + title);
            }
        }
        for (JsonElement pageElement : pages) {
            JsonObject page = pageElement.getAsJsonObject();
            String pageTitle = page.get("title").getAsString();
            for (JsonElement elementValue : page.getAsJsonArray("elements")) {
                JsonObject element = elementValue.getAsJsonObject();
                String type = element.get("type").getAsString();
                if ("link".equals(type) && !titles.contains(element.get("target").getAsString())) {
                    errors.add(pageTitle + ": unresolved page link " + element.get("target").getAsString());
                } else if ("item".equals(type)) {
                    validateItem(element.get("text").getAsString(), pageTitle, errors);
                } else if ("recipe".equals(type)) {
                    String id = element.get("text").getAsString();
                    if (!recipeIds().contains(id)) {
                        errors.add(pageTitle + ": unresolved recipe " + id);
                    }
                } else if ("advancement".equals(type)) {
                    String id = element.get("text").getAsString();
                    if (!advancementIds().contains(id)) {
                        errors.add(pageTitle + ": unresolved advancement " + id);
                    }
                }
            }
        }
        String guideText = RESOURCES.json(GUIDE).toString();
        ResourceExceptions.knownLegacyIds().stream()
                .filter(guideText::contains)
                .forEach(id -> errors.add("deleted legacy id still appears: " + id));
        assertTrue(errors.isEmpty(), "Guide reference failures:\n - " + String.join("\n - ", errors));
    }

    @Test
    void releaseFacingJsonContainsNoDebugPersonalOrMojibakeText() {
        List<String> errors = new ArrayList<>();
        for (ResourceIndex.Entry entry : RESOURCES.logicalPaths(path ->
                        path.matches("assets/mydrugs/lang/[^/]+\\.json")
                                || path.matches("assets/mydrugs/guide/.+\\.json")
                                || path.matches("data/mydrugs/advancements?/.+\\.json"))
                .stream().map(RESOURCES::require).toList()) {
            inspectText(RESOURCES.json(entry.logicalPath()), entry.logicalPath(), null, errors);
        }
        assertTrue(errors.isEmpty(), "Release text failures:\n - " + String.join("\n - ", errors));
    }

    private static void inspectText(JsonElement node, String path, String key, List<String> errors) {
        if (node == null || node.isJsonNull()) {
            return;
        }
        if (node.isJsonArray()) {
            node.getAsJsonArray().forEach(value -> inspectText(value, path, key, errors));
        } else if (node.isJsonObject()) {
            node.getAsJsonObject().entrySet()
                    .forEach(entry -> inspectText(entry.getValue(), path, entry.getKey(), errors));
        } else if (node.getAsJsonPrimitive().isString()) {
            String value = node.getAsString();
            String exceptionKey = path.contains("/lang/") ? key : path + ":" + key;
            if (BAD_TEXT.matcher(value).find()
                    && !ResourceExceptions.RELEASE_TEXT_ALLOWLIST.containsKey(exceptionKey)) {
                errors.add(path + " [" + key + "]: " + value);
            }
        }
    }

    private static void validateItem(String value, String page, List<String> errors) {
        ResourceIndex.ResourceId id = ResourceIndex.ResourceId.parse(value, "minecraft").orElse(null);
        if (id == null) {
            errors.add(page + ": invalid item id " + value);
        } else if ("mydrugs".equals(id.namespace()) && !REGISTRIES.items().contains(id.path())) {
            errors.add(page + ": unregistered item " + value);
        }
    }

    private static Set<String> recipeIds() {
        Set<String> ids = new LinkedHashSet<>();
        RESOURCES.logicalPaths(path -> path.matches("data/[^/]+/recipe/.+\\.json")).forEach(path -> {
            int separator = path.indexOf("/recipe/");
            ids.add(path.substring("data/".length(), separator) + ":"
                    + path.substring(separator + "/recipe/".length(), path.length() - ".json".length()));
        });
        return ids;
    }

    private static Set<String> advancementIds() {
        Set<String> ids = new LinkedHashSet<>();
        RESOURCES.logicalPaths(path -> path.matches("data/[^/]+/advancements?/.+\\.json")).forEach(path -> {
            int singular = path.indexOf("/advancement/");
            int separator = singular >= 0 ? singular : path.indexOf("/advancements/");
            String folder = singular >= 0 ? "/advancement/" : "/advancements/";
            ids.add(path.substring("data/".length(), separator) + ":"
                    + path.substring(separator + folder.length(), path.length() - ".json".length()));
        });
        return ids;
    }
}
