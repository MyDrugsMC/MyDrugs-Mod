package org.mydrugs.mydrugs.audit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

final class ResourceIndex {
    static final Path MAIN = Path.of("src/main/resources");
    static final Path GENERATED = Path.of("src/generated/resources");
    private static final Pattern NAMESPACE = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern PATH = Pattern.compile("[a-z0-9_./-]+");

    private final Map<String, Entry> entries;
    private final Map<String, List<Entry>> collisions;

    private ResourceIndex(Map<String, Entry> entries, Map<String, List<Entry>> collisions) {
        this.entries = entries;
        this.collisions = collisions;
    }

    static ResourceIndex load() {
        Map<String, Entry> entries = new LinkedHashMap<>();
        Map<String, List<Entry>> collisions = new LinkedHashMap<>();
        scanRoot(MAIN, Origin.MAIN, entries, collisions);
        scanRoot(GENERATED, Origin.GENERATED, entries, collisions);
        return new ResourceIndex(Map.copyOf(entries), Map.copyOf(collisions));
    }

    boolean exists(String logicalPath) {
        return entries.containsKey(normalize(logicalPath));
    }

    Entry require(String logicalPath) {
        String normalized = normalize(logicalPath);
        Entry entry = entries.get(normalized);
        if (entry == null) {
            String hint = normalized.startsWith("assets/mydrugs/")
                    || normalized.startsWith("data/mydrugs/")
                    ? " Run '.\\gradlew.bat runData' if this is provider-owned."
                    : "";
            throw new AssertionError("Missing resource '" + normalized + "' in "
                    + MAIN + " or " + GENERATED + "." + hint);
        }
        return entry;
    }

    JsonObject object(String logicalPath) {
        JsonElement json = json(logicalPath);
        if (!json.isJsonObject()) {
            throw new AssertionError("Expected JSON object at " + logicalPath);
        }
        return json.getAsJsonObject();
    }

    JsonArray array(String logicalPath) {
        JsonElement json = json(logicalPath);
        if (!json.isJsonArray()) {
            throw new AssertionError("Expected JSON array at " + logicalPath);
        }
        return json.getAsJsonArray();
    }

    JsonElement json(String logicalPath) {
        Entry entry = require(logicalPath);
        try {
            return JsonParser.parseString(Files.readString(entry.file()));
        } catch (RuntimeException | IOException e) {
            throw new AssertionError("Invalid JSON at " + entry.file() + ": " + e.getMessage(), e);
        }
    }

    List<Entry> under(String logicalPrefix) {
        String prefix = normalize(logicalPrefix);
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }
        String finalPrefix = prefix;
        return entries.values().stream()
                .filter(entry -> entry.logicalPath().startsWith(finalPrefix))
                .sorted(Comparator.comparing(Entry::logicalPath))
                .toList();
    }

    Set<String> logicalPaths(Predicate<String> filter) {
        return entries.keySet().stream().filter(filter).collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    Map<String, List<Entry>> collisions() {
        return collisions;
    }

    Set<ResourceId> collectMyDrugsReferences(JsonElement json) {
        Set<ResourceId> references = new LinkedHashSet<>();
        collectStrings(json, value -> {
            Optional<ResourceId> id = ResourceId.parse(value, null);
            if (id.isPresent() && "mydrugs".equals(id.get().namespace())) {
                references.add(id.get());
            }
        });
        return references;
    }

    static void collectStrings(JsonElement json, java.util.function.Consumer<String> sink) {
        if (json == null || json.isJsonNull()) {
            return;
        }
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            sink.accept(json.getAsString());
        } else if (json.isJsonArray()) {
            json.getAsJsonArray().forEach(element -> collectStrings(element, sink));
        } else if (json.isJsonObject()) {
            json.getAsJsonObject().entrySet().forEach(entry -> collectStrings(entry.getValue(), sink));
        }
    }

    static String formatMissing(String heading, Collection<String> missing) {
        if (missing.isEmpty()) {
            return "";
        }
        return heading + ":\n - " + missing.stream().sorted().collect(java.util.stream.Collectors.joining("\n - "));
    }

    private static void scanRoot(
            Path root,
            Origin origin,
            Map<String, Entry> entries,
            Map<String, List<Entry>> collisions
    ) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> !path.startsWith(root.resolve(".cache")))
                    .sorted()
                    .forEach(path -> {
                        String logical = normalize(root.relativize(path).toString());
                        Entry entry = new Entry(logical, path, origin);
                        Entry previous = entries.putIfAbsent(logical, entry);
                        if (previous != null) {
                            List<Entry> duplicates = new ArrayList<>();
                            duplicates.add(previous);
                            duplicates.add(entry);
                            collisions.put(logical, List.copyOf(duplicates));
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String normalize(String path) {
        return path.replace('\\', '/').replaceAll("^/+", "");
    }

    enum Origin {
        MAIN,
        GENERATED
    }

    record Entry(String logicalPath, Path file, Origin origin) {
    }

    record ResourceId(String namespace, String path) {
        static Optional<ResourceId> parse(String value, String defaultNamespace) {
            if (value == null || value.isBlank() || value.startsWith("#")) {
                return Optional.empty();
            }
            String namespace = defaultNamespace;
            String path = value;
            int separator = value.indexOf(':');
            if (separator >= 0) {
                namespace = value.substring(0, separator);
                path = value.substring(separator + 1);
            }
            if (namespace == null || !NAMESPACE.matcher(namespace).matches() || !PATH.matcher(path).matches()) {
                return Optional.empty();
            }
            return Optional.of(new ResourceId(namespace, path));
        }

        @Override
        public String toString() {
            return namespace + ":" + path;
        }
    }
}
