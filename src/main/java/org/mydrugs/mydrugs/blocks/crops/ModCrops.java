package org.mydrugs.mydrugs.blocks.crops;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
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

    private static final CropSpec<RyeCropBlock> RYE = new CropSpec<>("rye_crop", "rye_seeds", RyeCropBlock::new);
    private static final CropSpec<MaltCropBlock> MALT = new CropSpec<>("malt_crop", "malt_seeds", MaltCropBlock::new);
    private static final CropSpec<TobaccoCropBlock> TOBACCO = new CropSpec<>("tobacco_crop", "tobacco_seeds", TobaccoCropBlock::new);
    private static final CropSpec<AloeVeraCropBlock> ALOE_VERA = new CropSpec<>("aloe_vera_crop", "aloe_vera_seeds", AloeVeraCropBlock::new);
    private static final CropSpec<CannabisCropBlock> CANNABIS = new CropSpec<>("cannabis_crop", "cannabis_seeds", CannabisCropBlock::new);
    private static final CropSpec<CocaCropBlock> COCA = new CropSpec<>("coca_crop", "coca_seeds", CocaCropBlock::new);
    private static final CropSpec<CoffeeCropBlock> COFFEE = new CropSpec<>("coffee_crop", "coffee_seeds", CoffeeCropBlock::new);
    private static final CropSpec<OpiumPoppyCropBlock> OPIUM_POPPY = new CropSpec<>("opium_poppy_crop", "opium_poppy_seeds", OpiumPoppyCropBlock::new);

    public static final DeferredBlock<RyeCropBlock> RYE_CROP =
            registerCropBlock(RYE);

    public static final DeferredItem<BlockItem> RYE_SEEDS =
            registerSeeds(RYE, RYE_CROP);

    public static final DeferredBlock<MaltCropBlock> MALT_CROP =
            registerCropBlock(MALT);

    public static final DeferredItem<BlockItem> MALT_SEEDS =
            registerSeeds(MALT, MALT_CROP);

    public static final DeferredBlock<TobaccoCropBlock> TOBACCO_CROP =
            registerCropBlock(TOBACCO);

    public static final DeferredItem<BlockItem> TOBACCO_SEEDS =
            registerSeeds(TOBACCO, TOBACCO_CROP);

    public static final DeferredBlock<AloeVeraCropBlock> ALOE_VERA_CROP =
            registerCropBlock(ALOE_VERA);

    public static final DeferredItem<BlockItem> ALOE_VERA_SEEDS =
            registerSeeds(ALOE_VERA, ALOE_VERA_CROP);

    public static final DeferredBlock<CannabisCropBlock> CANNABIS_CROP =
            registerCropBlock(CANNABIS);

    public static final DeferredItem<BlockItem> CANNABIS_SEEDS =
            registerSeeds(CANNABIS, CANNABIS_CROP);

    public static final DeferredBlock<CocaCropBlock> COCA_CROP =
            registerCropBlock(COCA);

    public static final DeferredItem<BlockItem> COCA_SEEDS =
            registerSeeds(COCA, COCA_CROP);


    public static final DeferredBlock<CoffeeCropBlock> COFFEE_CROP =
            registerCropBlock(COFFEE);

    public static final DeferredItem<BlockItem> COFFEE_SEEDS =
            registerSeeds(COFFEE, COFFEE_CROP);

    public static final DeferredBlock<OpiumPoppyCropBlock> OPIUM_POPPY_CROP =
            registerCropBlock(OPIUM_POPPY);

    public static final DeferredItem<BlockItem> OPIUM_POPPY_SEEDS =
            registerSeeds(OPIUM_POPPY, OPIUM_POPPY_CROP);

    private ModCrops() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    private static <T extends CropBlock> DeferredBlock<T> registerCropBlock(CropSpec<T> spec) {
        return BLOCKS.registerBlock(
                spec.cropId(),
                spec.factory(),
                () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
        );
    }

    private static DeferredItem<BlockItem> registerSeeds(CropSpec<?> spec, DeferredBlock<? extends CropBlock> crop) {
        return ITEMS.registerItem(
                spec.seedId(),
                props -> new BlockItem(crop.get(), props)
        );
    }
}
