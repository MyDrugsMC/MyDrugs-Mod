package org.mydrugs.mydrugs.dimension;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.dimension.block.RedlineThornBlock;
import org.mydrugs.mydrugs.dimension.block.SymbolicPlantBlock;

public final class ModInnerDimensionBlocks {
    public static final DeferredBlock<Block> LUCID_ECHO_NODE =
            ModBlocks.BLOCKS.registerBlock(
                    "lucid_echo_node",
                    Block::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
                            .strength(1.4F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 4)
            );
    public static final DeferredItem<BlockItem> LUCID_ECHO_NODE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(LUCID_ECHO_NODE);

    public static final DeferredBlock<Block> BITTER_ECHO_NODE =
            ModBlocks.BLOCKS.registerBlock(
                    "bitter_echo_node",
                    Block::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF)
                            .strength(1.6F)
                            .sound(SoundType.TUFF)
            );
    public static final DeferredItem<BlockItem> BITTER_ECHO_NODE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(BITTER_ECHO_NODE);

    public static final DeferredBlock<Block> CALMING_ECHO_NODE =
            ModBlocks.BLOCKS.registerBlock(
                    "calming_echo_node",
                    Block::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.MOSS_BLOCK)
                            .strength(0.8F)
                            .sound(SoundType.MOSS)
                            .lightLevel(state -> 2)
            );
    public static final DeferredItem<BlockItem> CALMING_ECHO_NODE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(CALMING_ECHO_NODE);

    public static final DeferredBlock<Block> PRESSED_CALM_NODE =
            ModBlocks.BLOCKS.registerBlock(
                    "pressed_calm_node",
                    Block::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CALCITE)
                            .strength(1.2F)
                            .sound(SoundType.CALCITE)
            );
    public static final DeferredItem<BlockItem> PRESSED_CALM_NODE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(PRESSED_CALM_NODE);

    public static final DeferredBlock<Block> FERMENTED_MEMORY_NODE =
            ModBlocks.BLOCKS.registerBlock(
                    "fermented_memory_node",
                    Block::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                            .strength(1.8F)
                            .sound(SoundType.DEEPSLATE)
                            .lightLevel(state -> 1)
            );
    public static final DeferredItem<BlockItem> FERMENTED_MEMORY_NODE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(FERMENTED_MEMORY_NODE);

    public static final DeferredBlock<Block> REDLINE_CRYSTAL_NODE =
            ModBlocks.BLOCKS.registerBlock(
                    "redline_crystal_node",
                    Block::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_BLOCK)
                            .strength(1.3F)
                            .sound(SoundType.AMETHYST)
                            .lightLevel(state -> 5)
            );
    public static final DeferredItem<BlockItem> REDLINE_CRYSTAL_NODE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(REDLINE_CRYSTAL_NODE);

    public static final DeferredBlock<Block> DREAM_RESIDUE_GEODE =
            ModBlocks.BLOCKS.registerBlock(
                    "dream_residue_geode",
                    Block::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SCULK)
                            .strength(1.2F)
                            .sound(SoundType.SCULK)
                            .lightLevel(state -> 4)
            );
    public static final DeferredItem<BlockItem> DREAM_RESIDUE_GEODE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(DREAM_RESIDUE_GEODE);

    public static final DeferredBlock<Block> OVERDRIVE_SLAG =
            ModBlocks.BLOCKS.registerBlock(
                    "overdrive_slag",
                    Block::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.BLACKSTONE)
                            .strength(2.0F)
                            .sound(SoundType.NETHERITE_BLOCK)
                            .lightLevel(state -> 2)
            );
    public static final DeferredItem<BlockItem> OVERDRIVE_SLAG_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(OVERDRIVE_SLAG);

    public static final DeferredBlock<Block> MYCELIAL_INSIGHT_NODE =
            ModBlocks.BLOCKS.registerBlock(
                    "mycelial_insight_node",
                    Block::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.MYCELIUM)
                            .strength(0.9F)
                            .sound(SoundType.ROOTED_DIRT)
                            .lightLevel(state -> 3)
            );
    public static final DeferredItem<BlockItem> MYCELIAL_INSIGHT_NODE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(MYCELIAL_INSIGHT_NODE);

    public static final DeferredBlock<SymbolicPlantBlock> BREATH_GRASS =
            ModBlocks.BLOCKS.registerBlock(
                    "breath_grass",
                    SymbolicPlantBlock::new,
                    props -> symbolicPlantProperties()
            );
    public static final DeferredItem<BlockItem> BREATH_GRASS_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(BREATH_GRASS);

    public static final DeferredBlock<SymbolicPlantBlock> CALMING_FERN =
            ModBlocks.BLOCKS.registerBlock(
                    "calming_fern",
                    SymbolicPlantBlock::new,
                    props -> symbolicPlantProperties()
            );
    public static final DeferredItem<BlockItem> CALMING_FERN_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(CALMING_FERN);

    public static final DeferredBlock<SymbolicPlantBlock> MEMORY_REEDS =
            ModBlocks.BLOCKS.registerBlock(
                    "memory_reeds",
                    SymbolicPlantBlock::new,
                    props -> symbolicPlantProperties()
            );
    public static final DeferredItem<BlockItem> MEMORY_REEDS_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(MEMORY_REEDS);

    public static final DeferredBlock<RedlineThornBlock> REDLINE_THORN =
            ModBlocks.BLOCKS.registerBlock(
                    "redline_thorn",
                    RedlineThornBlock::new,
                    props -> BlockBehaviour.Properties.ofFullCopy(Blocks.FERN)
                            .strength(0.0F)
                            .noCollision()
                            .replaceable()
                            .sound(SoundType.SWEET_BERRY_BUSH)
                            .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
            );
    public static final DeferredItem<BlockItem> REDLINE_THORN_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(REDLINE_THORN);

    public static final DeferredBlock<SymbolicPlantBlock> MYCELIAL_ROOT =
            ModBlocks.BLOCKS.registerBlock(
                    "mycelial_root",
                    SymbolicPlantBlock::new,
                    props -> symbolicPlantProperties()
            );
    public static final DeferredItem<BlockItem> MYCELIAL_ROOT_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(MYCELIAL_ROOT);

    private ModInnerDimensionBlocks() {
    }

    private static BlockBehaviour.Properties symbolicPlantProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.FERN)
                .strength(0.0F)
                .noCollision()
                .replaceable()
                .sound(SoundType.GRASS)
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);
    }
}
