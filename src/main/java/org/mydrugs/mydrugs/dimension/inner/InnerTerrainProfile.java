package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public record InnerTerrainProfile(
        DrugId drugId,
        Supplier<BlockState> surface,
        Supplier<BlockState> subsurface,
        Supplier<BlockState> deep,
        Supplier<BlockState> accent,
        Supplier<? extends Block> nodeBlock,
        List<Supplier<? extends Block>> plantBlocks,
        int heightBias,
        double silhouetteScale,
        double ridgeScale,
        double scarCarve,
        Supplier<BlockState> scar,
        Supplier<BlockState> path
) {
    private static final Map<DrugId, InnerTerrainProfile> PROFILES = Map.ofEntries(
            Map.entry(DrugId.COFFEE, new InnerTerrainProfile(
                    DrugId.COFFEE,
                    () -> Blocks.GRASS_BLOCK.defaultBlockState(),
                    () -> Blocks.DIRT.defaultBlockState(),
                    () -> Blocks.STONE.defaultBlockState(),
                    () -> Blocks.BOOKSHELF.defaultBlockState(),
                    lucidNode(),
                    List.of(breathGrass()),
                    -5,
                    0.65D,
                    0.55D,
                    0.75D,
                    () -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
                    () -> Blocks.DIRT_PATH.defaultBlockState()
            )),
            Map.entry(DrugId.TOBACCO, new InnerTerrainProfile(
                    DrugId.TOBACCO,
                    () -> Blocks.TUFF.defaultBlockState(),
                    () -> Blocks.STONE_BRICKS.defaultBlockState(),
                    () -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
                    () -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
                    bitterNode(),
                    List.of(breathGrass()),
                    -1,
                    1.15D,
                    1.35D,
                    1.15D,
                    () -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
                    () -> Blocks.STONE_BRICKS.defaultBlockState()
            )),
            Map.entry(DrugId.WEED, new InnerTerrainProfile(
                    DrugId.WEED,
                    () -> Blocks.MOSS_BLOCK.defaultBlockState(),
                    () -> Blocks.ROOTED_DIRT.defaultBlockState(),
                    () -> Blocks.DIRT.defaultBlockState(),
                    () -> Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
                    calmingNode(),
                    List.of(calmingFern(), breathGrass()),
                    -4,
                    0.45D,
                    0.45D,
                    0.55D,
                    () -> Blocks.ROOTED_DIRT.defaultBlockState(),
                    () -> Blocks.MOSS_BLOCK.defaultBlockState()
            )),
            Map.entry(DrugId.HASH, new InnerTerrainProfile(
                    DrugId.HASH,
                    () -> Blocks.CALCITE.defaultBlockState(),
                    () -> Blocks.AMETHYST_BLOCK.defaultBlockState(),
                    () -> Blocks.SMOOTH_BASALT.defaultBlockState(),
                    () -> Blocks.AMETHYST_BLOCK.defaultBlockState(),
                    pressedNode(),
                    List.of(calmingFern()),
                    5,
                    0.9D,
                    1.1D,
                    0.85D,
                    () -> Blocks.SMOOTH_BASALT.defaultBlockState(),
                    () -> Blocks.CALCITE.defaultBlockState()
            )),
            Map.entry(DrugId.ALCOHOL, new InnerTerrainProfile(
                    DrugId.ALCOHOL,
                    () -> Blocks.MUD.defaultBlockState(),
                    () -> Blocks.DEEPSLATE.defaultBlockState(),
                    () -> Blocks.DEEPSLATE_TILES.defaultBlockState(),
                    () -> Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState(),
                    memoryNode(),
                    List.of(memoryReeds()),
                    -10,
                    0.7D,
                    0.75D,
                    1.0D,
                    () -> Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState(),
                    () -> Blocks.MUD.defaultBlockState()
            )),
            Map.entry(DrugId.COCAINE, new InnerTerrainProfile(
                    DrugId.COCAINE,
                    () -> Blocks.SMOOTH_QUARTZ.defaultBlockState(),
                    () -> Blocks.WHITE_CONCRETE.defaultBlockState(),
                    () -> Blocks.QUARTZ_BLOCK.defaultBlockState(),
                    () -> Blocks.REDSTONE_BLOCK.defaultBlockState(),
                    redlineNode(),
                    List.of(redlineThorn()),
                    5,
                    1.2D,
                    1.55D,
                    1.35D,
                    () -> Blocks.REDSTONE_BLOCK.defaultBlockState(),
                    () -> Blocks.SMOOTH_QUARTZ.defaultBlockState()
            )),
            Map.entry(DrugId.LSD, new InnerTerrainProfile(
                    DrugId.LSD,
                    () -> Blocks.PRISMARINE.defaultBlockState(),
                    () -> Blocks.CALCITE.defaultBlockState(),
                    () -> Blocks.SCULK.defaultBlockState(),
                    () -> Blocks.SEA_LANTERN.defaultBlockState(),
                    dreamNode(),
                    List.of(breathGrass()),
                    16,
                    1.35D,
                    1.25D,
                    0.7D,
                    () -> Blocks.TINTED_GLASS.defaultBlockState(),
                    () -> Blocks.PRISMARINE.defaultBlockState()
            )),
            Map.entry(DrugId.METH, new InnerTerrainProfile(
                    DrugId.METH,
                    () -> Blocks.BLACKSTONE.defaultBlockState(),
                    () -> Blocks.BASALT.defaultBlockState(),
                    () -> Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState(),
                    () -> Blocks.MAGMA_BLOCK.defaultBlockState(),
                    overdriveNode(),
                    List.of(redlineThorn()),
                    3,
                    1.35D,
                    1.7D,
                    1.45D,
                    () -> Blocks.MAGMA_BLOCK.defaultBlockState(),
                    () -> Blocks.POLISHED_BLACKSTONE.defaultBlockState()
            )),
            Map.entry(DrugId.MUSHROOMS, new InnerTerrainProfile(
                    DrugId.MUSHROOMS,
                    () -> Blocks.MYCELIUM.defaultBlockState(),
                    () -> Blocks.MUSHROOM_STEM.defaultBlockState(),
                    () -> Blocks.ROOTED_DIRT.defaultBlockState(),
                    () -> Blocks.RED_MUSHROOM_BLOCK.defaultBlockState(),
                    mycelialNode(),
                    List.of(mycelialRoot(), calmingFern()),
                    0,
                    0.8D,
                    0.8D,
                    0.65D,
                    () -> Blocks.ROOTED_DIRT.defaultBlockState(),
                    () -> Blocks.ROOTED_DIRT.defaultBlockState()
            ))
    );

    public InnerTerrainProfile {
        plantBlocks = List.copyOf(plantBlocks);
    }

    public static boolean hasProfile(DrugId drugId) {
        return PROFILES.containsKey(drugId);
    }

    public static InnerTerrainProfile forDrug(DrugId drugId) {
        InnerTerrainProfile profile = PROFILES.get(drugId);
        return profile == null ? PROFILES.get(DrugId.COFFEE) : profile;
    }

    public BlockState surfaceBlock() {
        return surface.get();
    }

    public BlockState subsurfaceBlock() {
        return subsurface.get();
    }

    public BlockState deepBlock() {
        return deep.get();
    }

    public BlockState accentBlock() {
        return accent.get();
    }

    public BlockState scarBlock() {
        return scar.get();
    }

    public BlockState pathBlock() {
        return path.get();
    }

    public BlockState nodeState() {
        return nodeBlock.get().defaultBlockState();
    }

    public List<BlockState> plantStates() {
        return plantBlocks.stream()
                .map(Supplier::get)
                .map(Block::defaultBlockState)
                .toList();
    }

    private static Supplier<? extends Block> lucidNode() {
        return () -> ModInnerDimensionBlocks.LUCID_ECHO_NODE.get();
    }

    private static Supplier<? extends Block> bitterNode() {
        return () -> ModInnerDimensionBlocks.BITTER_ECHO_NODE.get();
    }

    private static Supplier<? extends Block> calmingNode() {
        return () -> ModInnerDimensionBlocks.CALMING_ECHO_NODE.get();
    }

    private static Supplier<? extends Block> pressedNode() {
        return () -> ModInnerDimensionBlocks.PRESSED_CALM_NODE.get();
    }

    private static Supplier<? extends Block> memoryNode() {
        return () -> ModInnerDimensionBlocks.FERMENTED_MEMORY_NODE.get();
    }

    private static Supplier<? extends Block> redlineNode() {
        return () -> ModInnerDimensionBlocks.REDLINE_CRYSTAL_NODE.get();
    }

    private static Supplier<? extends Block> dreamNode() {
        return () -> ModInnerDimensionBlocks.DREAM_RESIDUE_GEODE.get();
    }

    private static Supplier<? extends Block> overdriveNode() {
        return () -> ModInnerDimensionBlocks.OVERDRIVE_SLAG.get();
    }

    private static Supplier<? extends Block> mycelialNode() {
        return () -> ModInnerDimensionBlocks.MYCELIAL_INSIGHT_NODE.get();
    }

    private static Supplier<? extends Block> breathGrass() {
        return () -> ModInnerDimensionBlocks.BREATH_GRASS.get();
    }

    private static Supplier<? extends Block> calmingFern() {
        return () -> ModInnerDimensionBlocks.CALMING_FERN.get();
    }

    private static Supplier<? extends Block> memoryReeds() {
        return () -> ModInnerDimensionBlocks.MEMORY_REEDS.get();
    }

    private static Supplier<? extends Block> redlineThorn() {
        return () -> ModInnerDimensionBlocks.REDLINE_THORN.get();
    }

    private static Supplier<? extends Block> mycelialRoot() {
        return () -> ModInnerDimensionBlocks.MYCELIAL_ROOT.get();
    }
}
