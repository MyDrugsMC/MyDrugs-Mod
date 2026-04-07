package org.mydrugs.mydrugs.items;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumable;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.ModSounds;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.EatingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.SmokingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.SniffingStrategy;
import org.mydrugs.mydrugs.gas.GasTankContents;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.items.drugs.*;
import org.mydrugs.mydrugs.items.rolling.RollerItem;
import org.mydrugs.mydrugs.registry.ModDataComponents;

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

    public static final DeferredItem<BlockItem> TOBACCO_SEEDS =
            ITEMS.registerItem(
                    "tobacco_seeds",
                    props -> new BlockItem(ModBlocks.TOBACCO_CROP.get(), props)
            );

    public static final DeferredItem<BlockItem> CANNABIS_SEEDS =
            ITEMS.registerItem(
                    "cannabis_seeds",
                    props -> new BlockItem(ModBlocks.CANNABIS_CROP.get(), props)
            );

    public static final DeferredItem<BlockItem> COCA_SEEDS =
            ITEMS.registerItem(
                    "coca_seeds",
                    props -> new BlockItem(ModBlocks.COCA_CROP.get(), props)
            );

    public static final DeferredItem<Item> TOBACCO_LEAF =
            ITEMS.registerSimpleItem("tobacco_leaf");

    public static final DeferredItem<Item> DRIED_TOBACCO_LEAF =
            ITEMS.registerSimpleItem("dried_tobacco_leaf");

    public static final DeferredItem<Item> CANNABIS_LEAF =
            ITEMS.registerSimpleItem("cannabis_leaf");

    public static final DeferredItem<Item> CANNABIS_POWDER =
            ITEMS.registerItem("cannabis_powder",
                    prop -> new CannabisPowderItem(prop, DrugId.WEED, new SmokingStrategy(true, true)));

    public static final DeferredItem<Item> METH_SHARD =
            ITEMS.registerItem("meth_shard", prop -> new MethShardItem(prop, DrugId.METH, null));

    public static final DeferredItem<Item> METH_POWDER =
            ITEMS.registerItem("meth_powder", prop -> new MethPowderItem(prop, DrugId.METH, new SmokingStrategy(true, false)));

    public static final DeferredItem<Item> LSD_DROP =
            ITEMS.registerItem("lsd_drop",
                    prop -> new LsdDropItem(prop, DrugId.LSD, new EatingStrategy()));

    public static final DeferredItem<Item> ERGOT =
            ITEMS.registerSimpleItem("ergot");

    public static final DeferredItem<Item> FUNGAL_FIBER =
            ITEMS.registerSimpleItem("fungal_fiber");

    public static final DeferredItem<Item> FUNGAL_CULTURE =
            ITEMS.registerSimpleItem("fungal_culture");

    public static final DeferredItem<BlockItem> PSYCHEDELIC_MYCELIUM =
            ITEMS.registerSimpleBlockItem(ModBlocks.PSYCHEDELIC_MYCELIUM);

    public static final DeferredItem<Item> RYE =
            ITEMS.registerSimpleItem("rye");

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

    public static final DeferredItem<Item> CURED_CANNABIS_LEAF =
            ITEMS.registerSimpleItem("cured_cannabis_leaf");

    public static final DeferredItem<Item> DRIED_CANNABIS_LEAF =
            ITEMS.registerSimpleItem("dried_cannabis_leaf");

    public static final DeferredItem<Item> CANNABIS_RESIN =
            ITEMS.registerSimpleItem("cannabis_resin");

    public static final DeferredItem<Item> HASH_BRICK =
            ITEMS.registerSimpleItem("hash_brick");

    public static final DeferredItem<Item> HASH_PIECE =
            ITEMS.registerItem("hash_piece", prop -> new HashPieceItem(prop, DrugId.HASH, new SmokingStrategy(true, true)));

    public static final DeferredItem<Item> COCA_LEAF =
            ITEMS.registerSimpleItem("coca_leaf");

    public static final DeferredItem<Item> DRIED_COCA_LEAF =
            ITEMS.registerSimpleItem("dried_coca_leaf");

    public static final DeferredItem<Item> COCA_PASTE =
            ITEMS.registerSimpleItem("coca_paste");

    public static final DeferredItem<Item> FILTER =
            ITEMS.registerSimpleItem("filter");

    public static final DeferredItem<Item> COCAINE_DUST =
            ITEMS.registerItem("cocaine_dust", prop -> new HashPieceItem(prop, DrugId.COCAINE, new SniffingStrategy()));

    public static final DeferredItem<Item> CRACK_SHARD =
            ITEMS.registerItem("crack_shard", prop -> new HashPieceItem(prop, DrugId.CRACK, new SmokingStrategy(true, false)));

    public static final DeferredItem<Item> GLASS_BOTTLE =
            ITEMS.registerItem("glass_bottle",
                    GlassBottleItem::new,
                    properties -> properties.stacksTo(1));

    public static final DeferredItem<Item> TOBACCO_HANDFUL =
            ITEMS.registerItem("tobacco_handful", prop -> new TobaccoHandfulItem(prop, DrugId.TOBACCO, new SmokingStrategy(true, true)));

    public static final DeferredItem<Item> CIGARETTE =
            ITEMS.registerItem("cigaret",
                    prop -> new CigaretteItem(prop, DrugId.TOBACCO, new SmokingStrategy(false, true)));

    public static final DeferredItem<Item> JOINT =
            ITEMS.registerItem("joint",
                    prop -> new JointItem(prop, null, new SmokingStrategy(false, true)));

    public static final DeferredItem<Item> ROLLER = ITEMS.registerItem("roller", RollerItem::new);

    public static final DeferredItem<BlockItem> ADVANCED_FURNACE_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.ADVANCED_FURNACE);

    public static final DeferredItem<BlockItem> DISTILLER_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.DISTILLER);

    public static final DeferredItem<BlockItem> MIXING_VAT_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.MIXING_VAT);

    public static final DeferredItem<Item> FLOUR =
            ITEMS.registerSimpleItem("flour");

    public static final DeferredItem<Item> PLANT_WASTE =
            ITEMS.registerSimpleItem("plant_waste");

    public static final DeferredItem<Item> INFECTED_RYE =
            ITEMS.registerSimpleItem("infected_rye");

    public static final DeferredItem<Item> MIXING_SPATULA =
            ITEMS.registerSimpleItem("mixing_spatula");

    public static final DeferredItem<Item> PLANT_BIOMASS =
            ITEMS.registerSimpleItem("plant_biomass");

    public static final DeferredItem<Item> ERGOTAMINE =
            ITEMS.registerSimpleItem("ergotamine");

    public static final DeferredItem<BlockItem> BIOCHEMICAL_REACTOR = ITEMS.registerSimpleBlockItem(
            ModBlocks.BIOCHEMICAL_REACTOR
    );

    public static final DeferredItem<Item> TRYPTOPHAN = ITEMS.registerSimpleItem("tryptophan");
    public static final DeferredItem<BlockItem> DRYER_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.DRYER);

    public static final DeferredItem<BlockItem> SIEVE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.SIEVE);

    public static final DeferredItem<BlockItem> FLUID_FILTERER_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.FLUID_FILTERER);

    public static final DeferredItem<Item> FLUID_FILTER = ITEMS.registerItem(
            "fluid_filter",
            prop -> new Item(prop.durability(128))
    );

    public static final DeferredItem<Item> COCAINE_PLATE =
            ITEMS.registerSimpleItem("cocaine_plate");

    public static final DeferredItem<BlockItem> EVAPORATION_TRAY_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.EVAPORATION_TRAY);

    public static final DeferredItem<BlockItem> CENTRIFUGE_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.CENTRIFUGE);

    public static final DeferredItem<BlockItem> GROWTH_CHAMBER_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.GROWTH_CHAMBER);

    public static final DeferredItem<BlockItem> SALT_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.SALT_BLOCK);

    public static final DeferredItem<BlockItem> SULFUR_ORE_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.SULFUR_ORE);

    public static final DeferredItem<BlockItem> DEEPSLATE_SULFUR_ORE_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.DEEPSLATE_SULFUR_ORE);

    public static final DeferredItem<GasTankItem> GAS_TANK_ITEM = ITEMS.register(
            "gas_tank",
            registryName -> new GasTankItem(
                    ModBlocks.GAS_TANK.get(),
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, registryName))
                            .stacksTo(1)
                            .component(ModDataComponents.GAS_TANK_CONTENTS.get(), GasTankContents.EMPTY)
            )
    );
    public static final DeferredItem<BlockItem> GAS_PUMP_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.GAS_PUMP);

    public static final DeferredItem<Item> SALT_POWDER = ITEMS.registerSimpleItem("salt_powder");
    public static final DeferredItem<Item> SULFUR_POWDER = ITEMS.registerSimpleItem("sulfur_powder");

    public static final DeferredItem<BlockItem> CHEMICAL_REACTOR_ITEM = ITEMS.registerSimpleBlockItem(
            ModBlocks.CHEMICAL_REACTOR
    );

    public static final DeferredItem<BlockItem> ADVANCED_MIXING_VAT_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.ADVANCED_MIXING_VAT);

    public static final DeferredItem<BlockItem> GASIFIER_ITEM = ITEMS.registerSimpleBlockItem(ModBlocks.GASIFIER);

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

        return new SpaceFoodItem(baseFood, props, DrugId.WEED, new EatingStrategy());
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
