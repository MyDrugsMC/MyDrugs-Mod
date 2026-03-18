package org.mydrugs.mydrugs.items;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.items.drugs.*;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MyDrugs.MODID);

    public static final DeferredItem<BlockItem> WEED_SEEDS =
            ITEMS.registerItem(
                    "weed_seeds",
                    props -> new BlockItem(ModBlocks.WEED_CROP.get(), props)
            );

    public static final DeferredItem<Item> WEED_LEAF =
            ITEMS.registerItem("weed_leaf", props ->
                    new WeedLeafItem(props.food(new FoodProperties.Builder()
                            .nutrition(0)
                            .saturationModifier(0.1f)
                            .alwaysEdible()
                            .build()
                    ), DrugId.WEED)
            );

    public static final DeferredItem<Item> WEED_POWDER =
            ITEMS.registerItem("weed_powder", prop -> new WeedPowderItem(prop, DrugId.WEED));

    public static final DeferredItem<Item> METH_SHARD =
            ITEMS.registerItem("meth_shard", prop -> new MethShardItem(prop, DrugId.METH));

    public static final DeferredItem<Item> METH_POWDER =
            ITEMS.registerItem("meth_powder", prop -> new MethPowderItem(prop, DrugId.METH));

    public static final DeferredItem<Item> LSD_BOTTLE =
            ITEMS.registerItem("lsd_bottle",
                    prop -> new LsdBottleItem(prop.craftRemainder(Items.GLASS_BOTTLE), DrugId.LSD));

    public static final DeferredItem<Item> LSD_DROP =
            ITEMS.registerItem("lsd_drop",
                    prop -> new LsdDropItem(
                            prop.food(new FoodProperties.Builder()
                                    .nutrition(0)
                                    .saturationModifier(0.0f)
                                    .alwaysEdible()
                                    .build()
            ), DrugId.LSD));

    public static final DeferredItem<Item> ERGOT =
            ITEMS.registerSimpleItem("ergot");

    public static final DeferredItem<BlockItem> PSYCHEDELIC_GRASS_BLOCK =
            ITEMS.registerSimpleBlockItem(ModBlocks.PSYCHEDELIC_GRASS_BLOCK);

    public static final DeferredItem<BlockItem> PSYCHEDELIC_MYCELIUM =
            ITEMS.registerSimpleBlockItem(ModBlocks.PSYCHEDELIC_MYCELIUM);

    public static final DeferredItem<BlockItem> RYE =
            ITEMS.registerSimpleBlockItem(ModBlocks.RYE);

    public static final DeferredItem<BlockItem> MAGIC_MUSHROOM =
            ITEMS.registerSimpleBlockItem(ModBlocks.MAGIC_MUSHROOM);

    public static final DeferredItem<BlockItem> MAGIC_MUSHROOM_BLOCK =
            ITEMS.registerSimpleBlockItem(ModBlocks.MAGIC_MUSHROOM_BLOCK);

    public static final DeferredItem<BlockItem> MAGIC_MUSHROOM_STEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.MAGIC_MUSHROOM_STEM);
}
