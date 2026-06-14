package org.mydrugs.mydrugs.audit;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class RegistrySourceIndex {
    private static final Pattern DIRECT_BLOCK = Pattern.compile(
            "(?:BLOCKS|ModBlocks\\.BLOCKS|FLUID_BLOCKS)\\s*\\.\\s*register(?:SimpleBlock|Block)?\\s*\\(\\s*\"([a-z0-9_./-]+)\"");
    private static final Pattern DIRECT_ITEM = Pattern.compile(
            "(?:ITEMS|ModItems\\.ITEMS|ModBlocks\\.ITEMS|FLUID_ITEMS)\\s*\\.\\s*register(?:SimpleItem|Item)?\\s*\\(\\s*\"([a-z0-9_./-]+)\"");
    private static final Pattern ITEM_SPEC = Pattern.compile("new\\s+ItemSpec(?:<[^>]*>)?\\s*\\(\\s*\"([a-z0-9_./-]+)\"");
    private static final Pattern MACHINE_SPEC = Pattern.compile("new\\s+MachineSpec(?:<[^>]*>)?\\s*\\(\\s*\"([a-z0-9_./-]+)\"");
    private static final Pattern CROP_SPEC = Pattern.compile(
            "new\\s+CropSpec(?:<[^>]*>)?\\s*\\(\\s*\"([a-z0-9_./-]+)\"\\s*,\\s*\"([a-z0-9_./-]+)\"");
    private static final Pattern FLUID_SPEC = Pattern.compile("new\\s+FluidSpec\\s*\\(\\s*\"([a-z0-9_./-]+)\"");
    private static final Pattern GAS_SPEC = Pattern.compile("new\\s+GasSpec\\s*\\(\\s*\"([a-z0-9_./-]+)\"");
    private static final Pattern KNOWLEDGE = Pattern.compile("PsyKnowledgeKey\\s+\\w+\\s*=\\s*create\\(\\s*\"([a-z0-9_./-]+)\"");
    private static final Pattern RECIPE_REGISTER = Pattern.compile(
            "(?:RECIPE_TYPES|RECIPE_SERIALIZERS)\\.register\\(\\s*\"([a-z0-9_./-]+)\"");
    private static final Pattern MACHINE_BLOCK_ITEM = Pattern.compile("registerMachineBlockItem\\(\\s*\"([a-z0-9_./-]+)\"");
    private static final Pattern PIPE_BLOCK = Pattern.compile(
            "register\\(\\s*\"([a-z0-9_./-]+)\"\\s*,\\s*PipeResourceKind\\.");
    private static final Pattern DECLARED_BLOCK = Pattern.compile(
            "DeferredBlock<[^>]+>\\s+(\\w+)\\s*=\\s*(?:ModBlocks\\.)?BLOCKS\\.register(?:SimpleBlock|Block)?\\(\\s*\"([a-z0-9_./-]+)\"");
    private static final Pattern SIMPLE_BLOCK_ITEM = Pattern.compile("registerSimpleBlockItem\\(\\s*(\\w+)\\s*\\)");

    private final Set<String> items;
    private final Set<String> blocks;
    private final Set<String> blockItems;
    private final Set<String> fluids;
    private final Set<String> gases;
    private final Set<String> knowledge;
    private final Set<String> recipeTypes;
    private final Set<String> recipeSerializers;

    private RegistrySourceIndex(
            Set<String> items,
            Set<String> blocks,
            Set<String> blockItems,
            Set<String> fluids,
            Set<String> gases,
            Set<String> knowledge,
            Set<String> recipeTypes,
            Set<String> recipeSerializers
    ) {
        this.items = Set.copyOf(items);
        this.blocks = Set.copyOf(blocks);
        this.blockItems = Set.copyOf(blockItems);
        this.fluids = Set.copyOf(fluids);
        this.gases = Set.copyOf(gases);
        this.knowledge = Set.copyOf(knowledge);
        this.recipeTypes = Set.copyOf(recipeTypes);
        this.recipeSerializers = Set.copyOf(recipeSerializers);
    }

    static RegistrySourceIndex load() {
        String sources = SourceIndex.allSources();
        Set<String> blocks = matches(DIRECT_BLOCK, sources, 1);
        blocks.addAll(matches(MACHINE_SPEC, sources, 1));
        blocks.addAll(matches(PIPE_BLOCK, sources, 1));

        Set<String> items = matches(DIRECT_ITEM, sources, 1);
        items.addAll(matches(ITEM_SPEC, sources, 1));
        items.addAll(matches(MACHINE_BLOCK_ITEM, sources, 1));
        // Space foods are registered by iterating the vanilla item registry. Their deterministic
        // generated client definitions are the bootstrap-free snapshot of the resulting IDs.
        ResourceIndex.load().under("assets/mydrugs/items").stream()
                .map(ResourceIndex.Entry::logicalPath)
                .map(path -> path.substring(path.lastIndexOf('/') + 1, path.length() - ".json".length()))
                .filter(id -> id.startsWith("space_"))
                .forEach(items::add);

        Matcher crops = CROP_SPEC.matcher(sources);
        while (crops.find()) {
            blocks.add(crops.group(1));
            items.add(crops.group(2));
        }

        Set<String> fluids = matches(FLUID_SPEC, sources, 1);
        blocks.addAll(fluids);
        fluids.stream().map(id -> id + "_bucket").forEach(items::add);

        Set<String> blockItems = new LinkedHashSet<>(matches(MACHINE_BLOCK_ITEM, sources, 1));
        Matcher declaredBlocks = DECLARED_BLOCK.matcher(sources);
        java.util.Map<String, String> fields = new java.util.LinkedHashMap<>();
        while (declaredBlocks.find()) {
            fields.put(declaredBlocks.group(1), declaredBlocks.group(2));
        }
        Matcher simpleBlockItems = SIMPLE_BLOCK_ITEM.matcher(sources);
        while (simpleBlockItems.find()) {
            String id = fields.get(simpleBlockItems.group(1));
            if (id != null) {
                blockItems.add(id);
                items.add(id);
            }
        }
        // Helper-based pipe registrations always create matching simple block items.
        blockItems.addAll(matches(PIPE_BLOCK, sources, 1));
        items.addAll(blockItems);

        String typeSource = SourceIndex.read(java.nio.file.Path.of(
                "src/main/java/org/mydrugs/mydrugs/recipes/ModRecipeTypes.java"));
        String serializerSource = SourceIndex.read(java.nio.file.Path.of(
                "src/main/java/org/mydrugs/mydrugs/recipes/ModRecipeSerializers.java"));

        return new RegistrySourceIndex(
                items,
                blocks,
                blockItems,
                fluids,
                matches(GAS_SPEC, sources, 1),
                matches(KNOWLEDGE, sources, 1),
                matches(RECIPE_REGISTER, typeSource, 1),
                matches(RECIPE_REGISTER, serializerSource, 1)
        );
    }

    Set<String> items() {
        return items;
    }

    Set<String> blocks() {
        return blocks;
    }

    Set<String> blockItems() {
        return blockItems;
    }

    Set<String> fluids() {
        return fluids;
    }

    Set<String> gases() {
        return gases;
    }

    Set<String> knowledge() {
        return knowledge;
    }

    Set<String> recipeTypes() {
        return recipeTypes;
    }

    Set<String> recipeSerializers() {
        return recipeSerializers;
    }

    private static Set<String> matches(Pattern pattern, String source, int group) {
        Set<String> values = new LinkedHashSet<>();
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            values.add(matcher.group(group));
        }
        return values;
    }
}
