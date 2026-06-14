package org.mydrugs.mydrugs.audit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

final class ModelResourceValidator {
    private final ResourceIndex resources;
    private final Set<String> visitedModels = new LinkedHashSet<>();
    private final Set<String> missingModels = new LinkedHashSet<>();
    private final Map<String, Set<String>> missingTextures = new LinkedHashMap<>();

    ModelResourceValidator(ResourceIndex resources) {
        this.resources = resources;
    }

    void validateClientItem(String logicalPath) {
        collectModelReferences(resources.json(logicalPath), logicalPath);
    }

    void validateBlockstate(String logicalPath) {
        collectModelReferences(resources.json(logicalPath), logicalPath);
    }

    void validateModel(String reference, String referencedBy) {
        ResourceIndex.ResourceId id = ResourceIndex.ResourceId.parse(reference, "minecraft").orElse(null);
        if (id == null || !"mydrugs".equals(id.namespace())) {
            return;
        }
        validateModelId(id.path(), referencedBy);
    }

    void validateAllMyDrugsModels() {
        for (ResourceIndex.Entry entry : resources.under("assets/mydrugs/models")) {
            if (!entry.logicalPath().endsWith(".json")) {
                continue;
            }
            String path = entry.logicalPath()
                    .substring("assets/mydrugs/models/".length(), entry.logicalPath().length() - ".json".length());
            validateModelId(path, entry.logicalPath());
        }
    }

    Set<String> missingModels() {
        return Set.copyOf(missingModels);
    }

    Map<String, Set<String>> missingTextures() {
        Map<String, Set<String>> copy = new LinkedHashMap<>();
        missingTextures.forEach((path, owners) -> copy.put(path, Set.copyOf(owners)));
        return Map.copyOf(copy);
    }

    private Map<String, String> validateModelId(String modelPath, String referencedBy) {
        String modelId = "mydrugs:" + modelPath;
        String logicalPath = "assets/mydrugs/models/" + modelPath + ".json";
        if (!resources.exists(logicalPath)) {
            missingModels.add(modelId + " referenced by " + referencedBy);
            return Map.of();
        }
        if (!visitedModels.add(modelId)) {
            return Map.of();
        }

        JsonObject model = resources.object(logicalPath);
        Map<String, String> textures = new LinkedHashMap<>();
        if (model.has("parent")) {
            String parent = model.get("parent").getAsString();
            ResourceIndex.ResourceId parentId = ResourceIndex.ResourceId.parse(parent, "minecraft").orElse(null);
            if (parentId != null && "mydrugs".equals(parentId.namespace())) {
                textures.putAll(validateModelId(parentId.path(), logicalPath));
            }
        }
        Map<String, String> localTextures = new LinkedHashMap<>();
        if (model.has("textures") && model.get("textures").isJsonObject()) {
            model.getAsJsonObject("textures").entrySet().forEach(entry -> {
                if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString()) {
                    localTextures.put(entry.getKey(), entry.getValue().getAsString());
                }
            });
        }
        textures.putAll(localTextures);

        // Parent textures are validated at their declaring model. Child-local values still resolve
        // through inherited variables, without multiplying one missing parent texture per child.
        for (String texture : localTextures.values()) {
            String resolved = resolveTexture(texture, textures, new LinkedHashSet<>());
            validateTexture(resolved, logicalPath);
        }
        collectSingularTextures(model, logicalPath, textures);
        return textures;
    }

    private void collectModelReferences(JsonElement node, String owner) {
        if (node == null || node.isJsonNull()) {
            return;
        }
        if (node.isJsonArray()) {
            node.getAsJsonArray().forEach(element -> collectModelReferences(element, owner));
            return;
        }
        if (!node.isJsonObject()) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : node.getAsJsonObject().entrySet()) {
            if ("model".equals(entry.getKey())
                    && entry.getValue().isJsonPrimitive()
                    && entry.getValue().getAsJsonPrimitive().isString()) {
                validateModel(entry.getValue().getAsString(), owner);
            } else {
                collectModelReferences(entry.getValue(), owner);
            }
        }
    }

    private void collectSingularTextures(JsonElement node, String owner, Map<String, String> variables) {
        if (node == null || node.isJsonNull()) {
            return;
        }
        if (node.isJsonArray()) {
            node.getAsJsonArray().forEach(element -> collectSingularTextures(element, owner, variables));
            return;
        }
        if (!node.isJsonObject()) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry : node.getAsJsonObject().entrySet()) {
            if ("texture".equals(entry.getKey())
                    && entry.getValue().isJsonPrimitive()
                    && entry.getValue().getAsJsonPrimitive().isString()) {
                validateTexture(resolveTexture(entry.getValue().getAsString(), variables, new LinkedHashSet<>()), owner);
            } else if (!"textures".equals(entry.getKey())) {
                collectSingularTextures(entry.getValue(), owner, variables);
            }
        }
    }

    private void validateTexture(String reference, String owner) {
        if (reference == null || reference.startsWith("#")) {
            return;
        }
        ResourceIndex.ResourceId id = ResourceIndex.ResourceId.parse(reference, "minecraft").orElse(null);
        if (id == null || !"mydrugs".equals(id.namespace())) {
            return;
        }
        String path = "assets/mydrugs/textures/" + id.path() + ".png";
        if (!resources.exists(path)) {
            missingTextures.computeIfAbsent(path, ignored -> new LinkedHashSet<>()).add(owner);
        }
    }

    private static String resolveTexture(String value, Map<String, String> variables, Set<String> visited) {
        if (value == null || !value.startsWith("#")) {
            return value;
        }
        String variable = value.substring(1);
        if (!visited.add(variable)) {
            return value;
        }
        return resolveTexture(variables.get(variable), variables, visited);
    }
}
