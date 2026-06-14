package org.mydrugs.mydrugs.audit;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatagenProviderContractValidationTest {
    private static final Path DATAGEN = Path.of("src/main/java/org/mydrugs/mydrugs/datagen");

    @Test
    void allCustomStableWritesUseTheSharedDuplicateGuard() {
        List<String> bypasses = new ArrayList<>();
        SourceIndex.javaFiles().stream()
                .filter(path -> path.startsWith(DATAGEN))
                .filter(path -> !path.endsWith("DatagenOutputGuard.java"))
                .forEach(path -> {
                    String source = SourceIndex.read(path);
                    if (source.contains("DataProvider.saveStable(")) {
                        bypasses.add(path.toString());
                    }
                });
        assertTrue(bypasses.isEmpty(),
                "Custom datagen providers bypass DatagenOutputGuard:\n - " + String.join("\n - ", bypasses));
    }

    @Test
    void simpleBlockProviderHasNoDuplicateLiteralGenerationCalls() {
        String source = SourceIndex.read(DATAGEN.resolve("ModSimpleBlockAssetProvider.java"));
        String runBody = source.substring(source.indexOf("public CompletableFuture<?> run"),
                source.indexOf("private void saveCubeAllBlock"));
        Pattern call = Pattern.compile("(save[A-Za-z0-9_]+)\\(futures,\\s*cachedOutput,\\s*\"([a-z0-9_]+)\"");
        Matcher matcher = call.matcher(runBody);
        Set<String> seen = new HashSet<>();
        List<String> duplicates = new ArrayList<>();
        while (matcher.find()) {
            String outputOwner = matcher.group(1) + ":" + matcher.group(2);
            if (!seen.add(outputOwner)) {
                duplicates.add(outputOwner);
            }
        }
        assertTrue(duplicates.isEmpty(),
                "Duplicate literal generation calls in ModSimpleBlockAssetProvider: " + duplicates);
    }
}
