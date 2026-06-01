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
import org.mydrugs.mydrugs.dimension.block.SmallSymbolicPlantBlock;
import org.mydrugs.mydrugs.dimension.block.SymbolicBushBlock;
import org.mydrugs.mydrugs.dimension.block.SymbolicGlowPlantBlock;
import org.mydrugs.mydrugs.dimension.block.SymbolicPlantBlock;
import org.mydrugs.mydrugs.dimension.block.SymbolicReedBlock;
import org.mydrugs.mydrugs.dimension.block.TallSymbolicPlantBlock;
import org.mydrugs.mydrugs.dimension.block.WideSymbolicPlantBlock;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

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

    public static final DeferredBlock<SmallSymbolicPlantBlock> LUCID_CLOVER =
            ModBlocks.BLOCKS.registerBlock(
                    "lucid_clover",
                    SmallSymbolicPlantBlock::new,
                    props -> symbolicPlantProperties()
            );
    public static final DeferredItem<BlockItem> LUCID_CLOVER_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(LUCID_CLOVER);

    public static final DeferredBlock<TallSymbolicPlantBlock> ASH_GRASS =
            ModBlocks.BLOCKS.registerBlock(
                    "ash_grass",
                    TallSymbolicPlantBlock::new,
                    props -> symbolicPlantProperties().sound(SoundType.NETHER_SPROUTS)
            );
    public static final DeferredItem<BlockItem> ASH_GRASS_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(ASH_GRASS);

    public static final DeferredBlock<WideSymbolicPlantBlock> MOSS_BREATH_CARPET =
            ModBlocks.BLOCKS.registerBlock(
                    "moss_breath_carpet",
                    WideSymbolicPlantBlock::new,
                    props -> symbolicPlantProperties().sound(SoundType.MOSS_CARPET)
            );
    public static final DeferredItem<BlockItem> MOSS_BREATH_CARPET_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(MOSS_BREATH_CARPET);

    public static final DeferredBlock<TallSymbolicPlantBlock> QUARTZ_NEEDLEGRASS =
            ModBlocks.BLOCKS.registerBlock(
                    "quartz_needlegrass",
                    TallSymbolicPlantBlock::new,
                    props -> symbolicPlantProperties().sound(SoundType.AMETHYST)
            );
    public static final DeferredItem<BlockItem> QUARTZ_NEEDLEGRASS_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(QUARTZ_NEEDLEGRASS);

    public static final DeferredBlock<WideSymbolicPlantBlock> MYCELIAL_THREADS =
            ModBlocks.BLOCKS.registerBlock(
                    "mycelial_threads",
                    WideSymbolicPlantBlock::new,
                    props -> symbolicPlantProperties().sound(SoundType.ROOTED_DIRT)
            );
    public static final DeferredItem<BlockItem> MYCELIAL_THREADS_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(MYCELIAL_THREADS);

    public static final DeferredBlock<SmallSymbolicPlantBlock> DREAM_ORCHID =
            ModBlocks.BLOCKS.registerBlock(
                    "dream_orchid",
                    SmallSymbolicPlantBlock::new,
                    props -> glowPlantProperties(3)
            );
    public static final DeferredItem<BlockItem> DREAM_ORCHID_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(DREAM_ORCHID);

    public static final DeferredBlock<SymbolicGlowPlantBlock> SPORE_BLOOM =
            ModBlocks.BLOCKS.registerBlock(
                    "spore_bloom",
                    SymbolicGlowPlantBlock::new,
                    props -> glowPlantProperties(5).sound(SoundType.FUNGUS)
            );
    public static final DeferredItem<BlockItem> SPORE_BLOOM_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(SPORE_BLOOM);

    public static final DeferredBlock<SmallSymbolicPlantBlock> BITTER_SPROUT =
            ModBlocks.BLOCKS.registerBlock(
                    "bitter_sprout",
                    SmallSymbolicPlantBlock::new,
                    props -> symbolicPlantProperties().sound(SoundType.NETHER_SPROUTS)
            );
    public static final DeferredItem<BlockItem> BITTER_SPROUT_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(BITTER_SPROUT);

    public static final DeferredBlock<SymbolicGlowPlantBlock> REDLINE_SPARK_BLOOM =
            ModBlocks.BLOCKS.registerBlock(
                    "redline_spark_bloom",
                    SymbolicGlowPlantBlock::new,
                    props -> glowPlantProperties(6).sound(SoundType.SWEET_BERRY_BUSH)
            );
    public static final DeferredItem<BlockItem> REDLINE_SPARK_BLOOM_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(REDLINE_SPARK_BLOOM);

    public static final DeferredBlock<SymbolicBushBlock> CALMING_BUSH =
            ModBlocks.BLOCKS.registerBlock(
                    "calming_bush",
                    SymbolicBushBlock::new,
                    props -> symbolicPlantProperties().sound(SoundType.AZALEA_LEAVES)
            );
    public static final DeferredItem<BlockItem> CALMING_BUSH_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(CALMING_BUSH);

    public static final DeferredBlock<SymbolicReedBlock> MEMORY_SEDGE =
            ModBlocks.BLOCKS.registerBlock(
                    "memory_sedge",
                    SymbolicReedBlock::new,
                    props -> symbolicPlantProperties().sound(SoundType.WET_GRASS)
            );
    public static final DeferredItem<BlockItem> MEMORY_SEDGE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(MEMORY_SEDGE);

    public static final DeferredBlock<SymbolicBushBlock> REDLINE_BRAMBLE =
            ModBlocks.BLOCKS.registerBlock(
                    "redline_bramble",
                    SymbolicBushBlock::new,
                    props -> symbolicPlantProperties().sound(SoundType.SWEET_BERRY_BUSH)
            );
    public static final DeferredItem<BlockItem> REDLINE_BRAMBLE_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(REDLINE_BRAMBLE);

    public static final DeferredBlock<SymbolicBushBlock> CRYSTAL_SHRUB =
            ModBlocks.BLOCKS.registerBlock(
                    "crystal_shrub",
                    SymbolicBushBlock::new,
                    props -> symbolicPlantProperties().sound(SoundType.AMETHYST)
            );
    public static final DeferredItem<BlockItem> CRYSTAL_SHRUB_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(CRYSTAL_SHRUB);

    public static final DeferredBlock<SymbolicBushBlock> FERMENTED_SHRUB =
            ModBlocks.BLOCKS.registerBlock(
                    "fermented_shrub",
                    SymbolicBushBlock::new,
                    props -> symbolicPlantProperties().sound(SoundType.ROOTED_DIRT)
            );
    public static final DeferredItem<BlockItem> FERMENTED_SHRUB_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(FERMENTED_SHRUB);

    public static final DeferredBlock<SymbolicReedBlock> MUD_REEDS =
            ModBlocks.BLOCKS.registerBlock(
                    "mud_reeds",
                    SymbolicReedBlock::new,
                    props -> symbolicPlantProperties().sound(SoundType.WET_GRASS)
            );
    public static final DeferredItem<BlockItem> MUD_REEDS_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(MUD_REEDS);

    public static final DeferredBlock<WideSymbolicPlantBlock> MEMORY_LOTUS =
            ModBlocks.BLOCKS.registerBlock(
                    "memory_lotus",
                    WideSymbolicPlantBlock::new,
                    props -> glowPlantProperties(2).sound(SoundType.LILY_PAD)
            );
    public static final DeferredItem<BlockItem> MEMORY_LOTUS_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(MEMORY_LOTUS);

    public static final DeferredBlock<WideSymbolicPlantBlock> BREATH_LILY =
            ModBlocks.BLOCKS.registerBlock(
                    "breath_lily",
                    WideSymbolicPlantBlock::new,
                    props -> symbolicPlantProperties().sound(SoundType.LILY_PAD)
            );
    public static final DeferredItem<BlockItem> BREATH_LILY_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(BREATH_LILY);

    public static final DeferredBlock<SymbolicGlowPlantBlock> PRISM_LOTUS =
            ModBlocks.BLOCKS.registerBlock(
                    "prism_lotus",
                    SymbolicGlowPlantBlock::new,
                    props -> glowPlantProperties(7).sound(SoundType.LILY_PAD)
            );
    public static final DeferredItem<BlockItem> PRISM_LOTUS_ITEM =
            ModBlocks.ITEMS.registerSimpleBlockItem(PRISM_LOTUS);

    private static final List<Supplier<? extends Block>> NODE_BLOCKS = List.of(
            supplier(LUCID_ECHO_NODE),
            supplier(BITTER_ECHO_NODE),
            supplier(CALMING_ECHO_NODE),
            supplier(PRESSED_CALM_NODE),
            supplier(FERMENTED_MEMORY_NODE),
            supplier(REDLINE_CRYSTAL_NODE),
            supplier(DREAM_RESIDUE_GEODE),
            supplier(OVERDRIVE_SLAG),
            supplier(MYCELIAL_INSIGHT_NODE)
    );

    private static final List<Supplier<? extends Block>> SYMBOLIC_PLANTS = List.of(
            supplier(BREATH_GRASS),
            supplier(CALMING_FERN),
            supplier(MEMORY_REEDS),
            supplier(REDLINE_THORN),
            supplier(MYCELIAL_ROOT),
            supplier(LUCID_CLOVER),
            supplier(ASH_GRASS),
            supplier(MOSS_BREATH_CARPET),
            supplier(QUARTZ_NEEDLEGRASS),
            supplier(MYCELIAL_THREADS),
            supplier(DREAM_ORCHID),
            supplier(SPORE_BLOOM),
            supplier(BITTER_SPROUT),
            supplier(REDLINE_SPARK_BLOOM),
            supplier(CALMING_BUSH),
            supplier(MEMORY_SEDGE),
            supplier(REDLINE_BRAMBLE),
            supplier(CRYSTAL_SHRUB),
            supplier(FERMENTED_SHRUB),
            supplier(MUD_REEDS),
            supplier(MEMORY_LOTUS),
            supplier(BREATH_LILY),
            supplier(PRISM_LOTUS)
    );

    private static final Set<String> SYMBOLIC_PLANT_IDS = Set.of(
            "breath_grass",
            "calming_fern",
            "memory_reeds",
            "redline_thorn",
            "mycelial_root",
            "lucid_clover",
            "ash_grass",
            "moss_breath_carpet",
            "quartz_needlegrass",
            "mycelial_threads",
            "dream_orchid",
            "spore_bloom",
            "bitter_sprout",
            "redline_spark_bloom",
            "calming_bush",
            "memory_sedge",
            "redline_bramble",
            "crystal_shrub",
            "fermented_shrub",
            "mud_reeds",
            "memory_lotus",
            "breath_lily",
            "prism_lotus"
    );

    private ModInnerDimensionBlocks() {
    }

    public static List<Supplier<? extends Block>> symbolicPlantSuppliers() {
        return SYMBOLIC_PLANTS;
    }

    public static Set<String> symbolicPlantIdsForTest() {
        return SYMBOLIC_PLANT_IDS;
    }

    public static boolean isSymbolicPlantBlock(Block block) {
        return containsBlock(SYMBOLIC_PLANTS, block);
    }

    public static boolean isGeneratedInnerBlock(Block block) {
        return isSymbolicPlantBlock(block) || containsBlock(NODE_BLOCKS, block);
    }

    private static boolean containsBlock(List<Supplier<? extends Block>> blocks, Block block) {
        for (Supplier<? extends Block> supplier : blocks) {
            if (supplier.get() == block) {
                return true;
            }
        }
        return false;
    }

    private static Supplier<? extends Block> supplier(DeferredBlock<? extends Block> block) {
        return block::get;
    }

    private static BlockBehaviour.Properties symbolicPlantProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.FERN)
                .strength(0.0F)
                .noCollision()
                .replaceable()
                .sound(SoundType.GRASS)
                .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY);
    }

    private static BlockBehaviour.Properties glowPlantProperties(int light) {
        return symbolicPlantProperties().lightLevel(state -> light);
    }
}
