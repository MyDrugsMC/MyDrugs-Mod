package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.worldgen.ModWorldGenKeys;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(MyDrugs.MODID);

    public static final DeferredBlock<WeedCropBlock> WEED_CROP =
            BLOCKS.registerBlock(
                    "weed_crop",
                    WeedCropBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
            );

    public static final DeferredBlock<RyeBlock> RYE =
            BLOCKS.registerBlock(
                    "rye",
                    RyeBlock::new,
                    properties -> properties.noCollision().instabreak().sound(SoundType.GRASS)
            );

    public static final DeferredBlock<PsychedelicGrassBlock> PSYCHEDELIC_GRASS_BLOCK =
            BLOCKS.registerBlock(
                    "psychedelic_grass",
                    PsychedelicGrassBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK));

    public static final DeferredBlock<Block> PSYCHEDELIC_MYCELIUM =
            BLOCKS.register("psychedelic_mycelium",
                    registryName
                            -> new MyceliumBlock(
                                    BlockBehaviour.Properties
                                            .ofFullCopy(Blocks.MYCELIUM)
                                            .setId(ResourceKey.create(Registries.BLOCK, registryName)))
            );

    public static final DeferredBlock<MushroomBlock> MAGIC_MUSHROOM =
            BLOCKS.register("magic_mushroom", registryName ->
                    // In some mappings/setups the constructor order may appear reversed in your IDE.
                    // If that happens, swap the two arguments.
                    new MushroomBlock(
                            ModWorldGenKeys.HUGE_MAGIC_MUSHROOM,
                            BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM)
                                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    )
            );

    public static final DeferredBlock<HugeMushroomBlock> MAGIC_MUSHROOM_BLOCK =
            BLOCKS.registerBlock(
                    "magic_mushroom_block",
                    HugeMushroomBlock::new,
                    prop -> BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM_BLOCK)
            );

    public static final DeferredBlock<HugeMushroomBlock> MAGIC_MUSHROOM_STEM =
            BLOCKS.registerBlock(
                    "magic_mushroom_stem",
                    HugeMushroomBlock::new,
                    prop -> BlockBehaviour.Properties.ofFullCopy(Blocks.MUSHROOM_STEM)
            );

    private ModBlocks() {}
}
