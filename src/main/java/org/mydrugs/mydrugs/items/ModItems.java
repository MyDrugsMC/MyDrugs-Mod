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
import org.mydrugs.mydrugs.core.drug.strategy.BangSmokingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.EatingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.JointSmokingStrategy;
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
                    new WeedLeafItem(props, DrugId.WEED, new EatingStrategy())
            );

    public static final DeferredItem<Item> WEED_POWDER =
            ITEMS.registerItem("weed_powder",
                    prop -> new WeedPowderItem(prop, DrugId.WEED, new BangSmokingStrategy(), new JointSmokingStrategy()));

    public static final DeferredItem<Item> METH_SHARD =
            ITEMS.registerItem("meth_shard", prop -> new MethShardItem(prop, DrugId.METH));

    public static final DeferredItem<Item> METH_POWDER =
            ITEMS.registerItem("meth_powder", prop -> new MethPowderItem(prop, DrugId.METH, new BangSmokingStrategy()));

    public static final DeferredItem<Item> LSD_BOTTLE =
            ITEMS.registerItem("lsd_bottle",
                    prop -> new LsdBottleItem(prop.craftRemainder(Items.GLASS_BOTTLE), DrugId.LSD, new EatingStrategy()));

    public static final DeferredItem<Item> LSD_DROP =
            ITEMS.registerItem("lsd_drop",
                    prop -> new LsdDropItem(prop, DrugId.LSD, new EatingStrategy()));

    public static final DeferredItem<Item> ERGOT =
            ITEMS.registerSimpleItem("ergot");

    public static final DeferredItem<BlockItem> PSYCHEDELIC_GRASS_BLOCK =
            ITEMS.registerSimpleBlockItem(ModBlocks.PSYCHEDELIC_GRASS_BLOCK);

    public static final DeferredItem<BlockItem> PSYCHEDELIC_MYCELIUM =
            ITEMS.registerSimpleBlockItem(ModBlocks.PSYCHEDELIC_MYCELIUM);

    public static final DeferredItem<BlockItem> RYE =
            ITEMS.registerSimpleBlockItem(ModBlocks.RYE);

    public static final DeferredItem<BlockItem> MAGIC_MUSHROOM =
            ITEMS.registerItem("magic_mushroom",
                    prop -> new MagicMushroomItem(ModBlocks.MAGIC_MUSHROOM.get(), prop, DrugId.MUSHROOMS, new EatingStrategy()));

    public static final DeferredItem<Item> MAGIC_MUSHROOM_POWDER =
            ITEMS.registerItem("magic_mushroom_powder", prop -> new MagicMushroomPowderItem(prop, DrugId.MUSHROOMS, new EatingStrategy()));


    public static final DeferredItem<BlockItem> MAGIC_MUSHROOM_BLOCK =
            ITEMS.registerSimpleBlockItem(ModBlocks.MAGIC_MUSHROOM_BLOCK);

    public static final DeferredItem<BlockItem> MAGIC_MUSHROOM_STEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.MAGIC_MUSHROOM_STEM);

    public static final DeferredItem<Item> BANG =
            ITEMS.registerItem("bang", BangItem::new);

    public static final DeferredItem<Item> GRINDING_TOOL =
            ITEMS.registerSimpleItem("grinding_tool", properties -> properties.stacksTo(1));

    public static final DeferredItem<BlockItem> GRINDING_BOWL =
            ITEMS.registerSimpleBlockItem(ModBlocks.GRINDING_BOWL);

    public static final DeferredItem<Item> PORTABLE_GRINDER =
            ITEMS.registerItem("portable_grinder", PortableGrinderItem::new);

    public static final DeferredItem<Item> CUPBOARD_PIECE =
            ITEMS.registerSimpleItem("cupboard_piece");

    public static final DeferredItem<BlockItem> STOMP_CRAFTER_ITEM =
            ITEMS.registerSimpleBlockItem("stomp_crafter", ModBlocks.STOMP_CRAFTER);

    public static final DeferredItem<Item> STOMP_PLATE =
            ITEMS.registerSimpleItem("stomp_plate");
}
