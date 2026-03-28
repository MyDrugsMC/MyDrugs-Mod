package org.mydrugs.mydrugs.items;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.BangSmokingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.EatingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.JointSmokingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.SniffingStrategy;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.items.drugs.*;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MyDrugs.MODID);

    public static final DeferredItem<BlockItem> RYE_SEEDS =
            ITEMS.registerItem(
                    "rye_seeds",
                    props -> new BlockItem(ModBlocks.RYE_CROP.get(), props)
            );

    public static final DeferredItem<BlockItem> MALT_SEEDS =
            ITEMS.registerItem(
                    "malt_seeds",
                    props -> new BlockItem(ModBlocks.MALT_CROP.get(), props)
            );

    public static final DeferredItem<BlockItem> CANNABIS_SEEDS =
            ITEMS.registerItem(
                    "cannabis_seeds",
                    props -> new BlockItem(ModBlocks.CANNABIS_CROP.get(), props)
            );

    public static final DeferredItem<Item> CANNABIS_LEAF =
            ITEMS.registerItem("cannabis_leaf", props ->
                    new CannabisLeafItem(props, DrugId.WEED, new EatingStrategy())
            );

    public static final DeferredItem<Item> CANNABIS_POWDER =
            ITEMS.registerItem("cannabis_powder",
                    prop -> new CannabisPowderItem(prop, DrugId.WEED, new BangSmokingStrategy(), new JointSmokingStrategy()));

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

    public static final DeferredItem<Item> MALT = ITEMS.registerSimpleItem("malt");

    public static final DeferredItem<Item> MALT_POWDER = ITEMS.registerSimpleItem("malt_powder");

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

    public static final DeferredItem<Item> SYRINGE =
            ITEMS.registerItem("syringe", SyringeItem::new, props -> props.stacksTo(1));

    public static final DeferredItem<Item> CANNABIS_RESIN =
            ITEMS.registerSimpleItem("cannabis_resin");

    public static final DeferredItem<Item> HASH_BRICK =
            ITEMS.registerSimpleItem("hash_brick");

    public static final DeferredItem<Item> HASH_PIECE =
            ITEMS.registerItem("hash_piece", prop -> new HashPieceItem(prop, DrugId.HASH, new BangSmokingStrategy(), new JointSmokingStrategy()));

    public static final DeferredItem<Item> COCA_LEAF =
            ITEMS.registerSimpleItem("coca_leaf");

    public static final DeferredItem<Item> COCAINE_DUST =
            ITEMS.registerItem("cocaine_dust", prop -> new HashPieceItem(prop, DrugId.COCAINE, new SniffingStrategy()));

    public static final DeferredItem<Item> CRACK_SHARD =
            ITEMS.registerItem("crack_shard", prop -> new HashPieceItem(prop, DrugId.CRACK, new BangSmokingStrategy()));

    public static final DeferredItem<Item> GLASS_BOTTLE =
            ITEMS.registerItem("glass_bottle",
                    GlassBottleItem::new,
                    properties -> properties.stacksTo(1));

    public static final DeferredItem<Item> TOBACCO_BAG =
            ITEMS.registerItem("tobacco_bag", prop -> new HashPieceItem(prop, DrugId.TOBACCO, new BangSmokingStrategy()));

    public static final DeferredItem<BlockItem> ADVANCED_FURNACE_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.ADVANCED_FURNACE);

    public static final DeferredItem<BlockItem> DISTILLER_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.DISTILLER);

    public static final DeferredItem<BlockItem> MIXING_VAT =
            ITEMS.registerSimpleBlockItem(ModBlocks.MIXING_VAT);

    public static final DeferredItem<Item> FLOUR =
            ITEMS.registerSimpleItem("flour");

    public static final DeferredItem<Item> MIXING_SPATULA =
            ITEMS.registerSimpleItem("mixing_spatula");

    public static final Map<ResourceLocation, DeferredItem<SpaceFoodItem>> SPACE_FOODS_BY_BASE_ID = new LinkedHashMap<>();
    public static final Map<Item, DeferredItem<SpaceFoodItem>> SPACE_FOODS_BY_BASE_ITEM = new IdentityHashMap<>();

    static {
        registerSpaceFoods();
    }

    private static void registerSpaceFoods() {
        for (Item baseFood : BuiltInRegistries.ITEM) {
            ResourceLocation baseId = BuiltInRegistries.ITEM.getKey(baseFood);

            if (!"minecraft".equals(baseId.getNamespace())) continue;
            if (!isSupportedBaseFood(baseFood)) continue;

            String regName = "space_" + baseId.getPath();

            DeferredItem<SpaceFoodItem> holder = ITEMS.register(regName, resourceLocation -> createSpaceFood(baseFood, resourceLocation));

            SPACE_FOODS_BY_BASE_ID.put(baseId, holder);
            SPACE_FOODS_BY_BASE_ITEM.put(baseFood, holder);
        }
    }

    private static boolean isSupportedBaseFood(Item item) {
        ItemStack prototype = item.getDefaultInstance();
        return prototype.get(DataComponents.FOOD) != null
                && prototype.get(DataComponents.CONSUMABLE) != null;
    }

    private static SpaceFoodItem createSpaceFood(Item baseFood, ResourceLocation resourceLocation) {
        ItemStack prototype = baseFood.getDefaultInstance();

        Item.Properties props = new Item.Properties()
                .stacksTo(baseFood.getDefaultMaxStackSize())
                .setId(ResourceKey.create(Registries.ITEM, resourceLocation));

        copyIfPresent(prototype, props, DataComponents.FOOD);
        copyIfPresent(prototype, props, DataComponents.CONSUMABLE);

        // Important for soups / bottles / bowls when applicable
        copyIfPresent(prototype, props, DataComponents.USE_REMAINDER);

        // Optional extras you may also want to preserve
        copyIfPresent(prototype, props, DataComponents.RARITY);

        return new SpaceFoodItem(baseFood, props, DrugId.WEED);
    }

    private static <T> void copyIfPresent(ItemStack from, Item.Properties props, DataComponentType<T> type) {
        T value = from.get(type);
        if (value != null) {
            props.component(type, value);
        }
    }

    @Nullable
    public static Item getSpaceVariant(Item baseFood) {
        DeferredItem<SpaceFoodItem> holder = SPACE_FOODS_BY_BASE_ITEM.get(baseFood);
        return holder == null ? null : holder.get();
    }
}
