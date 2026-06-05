package org.mydrugs.mydrugs.audit;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Bootstrap-free coverage check for ore/material tag generation. Verifies the datagen providers
 * declare the expected ore and material tags so that running datagen produces them.
 */
class OreMaterialTagCoverageTest {
    private static final Path BLOCK_TAGS = Path.of("src/main/java/org/mydrugs/mydrugs/datagen/ModBlockTagsProvider.java");
    private static final Path ITEM_TAGS = Path.of("src/main/java/org/mydrugs/mydrugs/datagen/ModItemTagsProvider.java");

    @Test
    void blockTagsCoverAllOresAndStorageBlocks() {
        String src = SourceIndex.read(BLOCK_TAGS);

        for (String ore : new String[]{
                "SULFUR_ORE", "DEEPSLATE_SULFUR_ORE", "PLATINUM_ORE", "DEEPSLATE_PLATINUM_ORE",
                "ALUMINIUM_ORE", "DEEPSLATE_ALUMINIUM_ORE", "PHOSPHATE_ORE", "DEEPSLATE_PHOSPHATE_ORE"}) {
            assertTrue(src.contains(ore), "Block tags should reference " + ore);
        }
        assertTrue(src.contains("MINEABLE_WITH_PICKAXE"), "Should populate mineable/pickaxe");
        assertTrue(src.contains("NEEDS_STONE_TOOL"), "Should populate needs_stone_tool");
        assertTrue(src.contains("NEEDS_IRON_TOOL"), "Should populate needs_iron_tool");
        assertTrue(src.contains("Tags.Blocks.ORES"), "Should populate c:ores");
        for (String sub : new String[]{"ores/sulfur", "ores/platinum", "ores/aluminium", "ores/phosphate"}) {
            assertTrue(SourceIndex.containsLiteral(src, sub), "Should populate c:" + sub);
        }
        for (String sub : new String[]{
                "storage_blocks/platinum", "storage_blocks/raw_platinum",
                "storage_blocks/aluminium", "storage_blocks/raw_aluminium"}) {
            assertTrue(SourceIndex.containsLiteral(src, sub), "Should populate c:" + sub);
        }
    }

    @Test
    void itemTagsCoverIngotsRawMaterialsDustsAndMirrors() {
        String src = SourceIndex.read(ITEM_TAGS);

        assertTrue(src.contains("Tags.Items.INGOTS"), "Should populate c:ingots");
        assertTrue(src.contains("Tags.Items.RAW_MATERIALS"), "Should populate c:raw_materials");
        assertTrue(src.contains("Tags.Items.DUSTS"), "Should populate c:dusts");
        assertTrue(src.contains("Tags.Items.ORES"), "Should populate c:ores item mirror");

        for (String sub : new String[]{
                "ingots/platinum", "ingots/aluminium",
                "raw_materials/platinum", "raw_materials/aluminium", "raw_materials/phosphorus",
                "dusts/sulfur",
                "ores/sulfur", "ores/platinum", "ores/aluminium", "ores/phosphate",
                "storage_blocks/platinum", "storage_blocks/raw_platinum",
                "storage_blocks/aluminium", "storage_blocks/raw_aluminium"}) {
            assertTrue(SourceIndex.containsLiteral(src, sub), "Item tags should populate c:" + sub);
        }
    }
}
