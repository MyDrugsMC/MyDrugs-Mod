package org.mydrugs.mydrugs.audit;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JeiResourceValidationTest {
    private static final Path DESCRIPTORS = Path.of(
            "src/main/java/org/mydrugs/mydrugs/client/compat/JeiCategoryDescriptors.java");
    private static final Path CACHE = Path.of(
            "src/main/java/org/mydrugs/mydrugs/client/compat/ClientRecipesCache.java");
    private static final Pattern DESCRIPTOR = Pattern.compile(
            "descriptor\\(\"([a-z0-9_]+)\",\\s*\"([^\"]+)\",\\s*"
                    + "([A-Za-z0-9_]+)::new,\\s*\\3\\.TYPE,\\s*\"([A-Za-z0-9_]+)\",\\s*"
                    + "(ModBlocks|ModItems)\\.class,\\s*([^\\)]*)\\)",
            Pattern.DOTALL);
    private static final Pattern STRING = Pattern.compile("\"([A-Z0-9_]+)\"");
    private static final Pattern JEI_TYPE_ID = Pattern.compile(
            "fromNamespaceAndPath\\(MyDrugs\\.MODID,\\s*\"([a-z0-9_]+)\"\\)");

    @Test
    void descriptorTableCoversEveryRecipeTypeAndItsDependencies() {
        RegistrySourceIndex registries = RegistrySourceIndex.load();
        JsonObject lang = ResourceIndex.load().object("assets/mydrugs/lang/en_us.json");
        String source = SourceIndex.read(DESCRIPTORS);
        String cacheSource = SourceIndex.read(CACHE);
        Matcher matcher = DESCRIPTOR.matcher(source);
        Set<String> recipeTypes = new LinkedHashSet<>();
        List<String> errors = new ArrayList<>();
        Map<String, String> intentionalJeiIds = Map.of(
                "btx_fractionation", "btx_fractionation_tower",
                "fluid_filtering", "fluid_filterer"
        );

        int descriptors = 0;
        while (matcher.find()) {
            descriptors++;
            String recipeType = matcher.group(1);
            String titleKey = matcher.group(2);
            String category = matcher.group(3);
            String cacheMethod = matcher.group(4);
            String owner = matcher.group(5);
            String catalystSource = matcher.group(6);
            recipeTypes.add(recipeType);

            if (!lang.has(titleKey)) {
                errors.add(recipeType + ": missing JEI title translation " + titleKey);
            }
            if (!cacheSource.contains(" " + cacheMethod + "()")) {
                errors.add(recipeType + ": cache method does not exist: " + cacheMethod);
            }
            Matcher catalysts = STRING.matcher(catalystSource);
            int catalystCount = 0;
            String ownerSource = SourceIndex.read(Path.of(
                    "src/main/java/org/mydrugs/mydrugs/" + ("ModItems".equals(owner) ? "items/ModItems.java" : "blocks/ModBlocks.java")));
            while (catalysts.find()) {
                catalystCount++;
                if (!ownerSource.contains(catalysts.group(1))) {
                    errors.add(recipeType + ": catalyst field not found: " + owner + "." + catalysts.group(1));
                }
            }
            if (catalystCount == 0) {
                errors.add(recipeType + ": no catalyst/icon field configured");
            }

            String categorySource = SourceIndex.read(Path.of(
                    "src/main/java/org/mydrugs/mydrugs/client/compat/" + category + ".java"));
            Matcher typeId = JEI_TYPE_ID.matcher(categorySource);
            if (!typeId.find()) {
                errors.add(recipeType + ": category has no statically resolvable JEI type ID");
            } else {
                String expected = intentionalJeiIds.getOrDefault(recipeType, recipeType);
                if (!expected.equals(typeId.group(1))) {
                    errors.add(recipeType + ": JEI type is " + typeId.group(1) + ", expected " + expected);
                }
            }
        }
        assertEquals(25, descriptors, "Expected one JEI descriptor per custom recipe type");
        assertEquals(registries.recipeTypes(), recipeTypes,
                "Every registered custom recipe type must have exactly one JEI descriptor");
        assertTrue(errors.isEmpty(), "JEI contract failures:\n - " + String.join("\n - ", errors));
    }
}
