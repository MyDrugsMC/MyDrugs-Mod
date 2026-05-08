package org.mydrugs.mydrugs.blocks.crops;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;

public class ModCrops {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MyDrugs.MODID);

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(MyDrugs.MODID);

    public static final DeferredBlock<RyeCropBlock> RYE_CROP =
            BLOCKS.registerBlock(
                    "rye_crop",
                    RyeCropBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
            );

    public static final DeferredItem<BlockItem> RYE_SEEDS =
            ITEMS.registerItem(
                    "rye_seeds",
                    props -> new BlockItem(RYE_CROP.get(), props)
            );

    public static final DeferredBlock<MaltCropBlock> MALT_CROP =
            BLOCKS.registerBlock(
                    "malt_crop",
                    MaltCropBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
            );

    public static final DeferredItem<BlockItem> MALT_SEEDS =
            ITEMS.registerItem(
                    "malt_seeds",
                    props -> new BlockItem(MALT_CROP.get(), props)
            );

    public static final DeferredBlock<TobaccoCropBlock> TOBACCO_CROP =
            BLOCKS.registerBlock(
                    "tobacco_crop",
                    TobaccoCropBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
            );

    public static final DeferredItem<BlockItem> TOBACCO_SEEDS =
            ITEMS.registerItem(
                    "tobacco_seeds",
                    props -> new BlockItem(TOBACCO_CROP.get(), props)
            );

    public static final DeferredBlock<AloeVeraCropBlock> ALOE_VERA_CROP =
            BLOCKS.registerBlock(
                    "aloe_vera_crop",
                    AloeVeraCropBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
            );

    public static final DeferredBlock<CannabisCropBlock> CANNABIS_CROP =
            BLOCKS.registerBlock(
                    "cannabis_crop",
                    CannabisCropBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
            );

    public static final DeferredItem<BlockItem> CANNABIS_SEEDS =
            ITEMS.registerItem(
                    "cannabis_seeds",
                    props -> new BlockItem(CANNABIS_CROP.get(), props)
            );

    public static final DeferredBlock<CocaCropBlock> COCA_CROP =
            BLOCKS.registerBlock(
                    "coca_crop",
                    CocaCropBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
            );

    public static final DeferredItem<BlockItem> COCA_SEEDS =
            ITEMS.registerItem(
                    "coca_seeds",
                    props -> new BlockItem(COCA_CROP.get(), props)
            );


    public static final DeferredBlock<CoffeeCropBlock> COFFEE_CROP =
            BLOCKS.registerBlock(
                    "coffee_crop",
                    CoffeeCropBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
            );

    public static final DeferredItem<BlockItem> COFFEE_SEEDS =
            ITEMS.registerItem(
                    "coffee_seeds",
                    props -> new BlockItem(COFFEE_CROP.get(), props)
            );

    public static final DeferredBlock<OpiumPoppyCropBlock> OPIUM_POPPY_CROP =
            BLOCKS.registerBlock(
                    "opium_poppy_crop",
                    OpiumPoppyCropBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
            );

    public static final DeferredItem<BlockItem> OPIUM_POPPY_SEEDS =
            ITEMS.registerItem(
                    "opium_poppy_seeds",
                    props -> new BlockItem(OPIUM_POPPY_CROP.get(), props)
            );

    private ModCrops() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }
}
