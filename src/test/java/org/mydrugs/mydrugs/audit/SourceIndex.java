package org.mydrugs.mydrugs.audit;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads the mod's Java sources into a single in-memory string so bootstrap-free audits can assert
 * the presence/absence of registration literals without loading any Minecraft classes.
 */
final class SourceIndex {
    static final Path MAIN_JAVA = Path.of("src/main/java");

    private static volatile String cachedAllSources;

    private SourceIndex() {
    }

    static String allSources() {
        String local = cachedAllSources;
        if (local == null) {
            local = readTree(MAIN_JAVA);
            cachedAllSources = local;
        }
        return local;
    }

    static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static boolean containsLiteral(String haystack, String id) {
        return haystack.contains("\"" + id + "\"");
    }

    static List<Path> javaFiles() {
        try (Stream<Path> paths = Files.walk(MAIN_JAVA)) {
            return paths.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String readTree(Path root) {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                    .filter(p -> p.toString().endsWith(".java"))
                    .map(SourceIndex::read)
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
