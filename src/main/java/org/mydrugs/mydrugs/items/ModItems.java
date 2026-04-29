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
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.EatingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.SmokingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.SniffingStrategy;
import org.mydrugs.mydrugs.effects.addiction.item.*;
import org.mydrugs.mydrugs.energy.AutomationUpgradeItem;
import org.mydrugs.mydrugs.energy.EnergyUpgradeItem;
import org.mydrugs.mydrugs.gas.GasTankContents;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.items.drugs.*;
import org.mydrugs.mydrugs.items.rolling.RollerItem;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterConfig;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterUpgradeItem;
import org.mydrugs.mydrugs.pipe.item.PipeWrenchItem;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferUpgradeItem;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MyDrugs.MODID);

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

    public static final DeferredItem<Item> RYE =
            ITEMS.registerSimpleItem("rye");

    public static final DeferredItem<Item> MALT = ITEMS.registerSimpleItem("malt");

    public static final DeferredItem<Item> MALT_POWDER = ITEMS.registerSimpleItem("malt_powder");

    public static final DeferredItem<BlockItem> MAGIC_MUSHROOM =
            ITEMS.registerItem("magic_mushroom",
                    prop -> new MagicMushroomItem(ModBlocks.MAGIC_MUSHROOM.get(), prop, DrugId.MUSHROOMS, new EatingStrategy()));

    public static final DeferredItem<Item> MAGIC_MUSHROOM_POWDER =
            ITEMS.registerItem("magic_mushroom_powder", prop -> new MagicMushroomPowderItem(prop, DrugId.MUSHROOMS, new EatingStrategy()));

    public static final DeferredItem<Item> BANG =
            ITEMS.registerItem("bang", BangItem::new);

    public static final DeferredItem<Item> GRINDING_TOOL =
            ITEMS.registerSimpleItem("grinding_tool", properties -> properties.stacksTo(1));

    public static final DeferredItem<Item> PORTABLE_GRINDER =
            ITEMS.registerItem("portable_grinder", PortableGrinderItem::new);

    public static final DeferredItem<Item> CUPBOARD_PIECE =
            ITEMS.registerSimpleItem("cupboard_piece");

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
            ITEMS.registerItem("cocaine_dust", prop -> new CocaineDustItem(prop, DrugId.COCAINE, new SniffingStrategy()));

    public static final DeferredItem<Item> CRACK_SHARD =
            ITEMS.registerItem("crack_shard", prop -> new CrackShardItem(prop, DrugId.CRACK, new SmokingStrategy(true, false)));

    public static final DeferredItem<Item> CRACK_PLATE =
            ITEMS.registerSimpleItem("crack_plate");

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

    public static final DeferredItem<Item> TRYPTOPHAN = ITEMS.registerSimpleItem("tryptophan");

    public static final DeferredItem<Item> FLUID_FILTER = ITEMS.registerItem(
            "fluid_filter",
            prop -> new Item(prop.durability(128))
    );

    public static final DeferredItem<Item> COCAINE_PLATE =
            ITEMS.registerSimpleItem("cocaine_plate");

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

    public static final DeferredItem<Item> SALT_POWDER = ITEMS.registerSimpleItem("salt_powder");

    public static final DeferredItem<Item> SULFUR_POWDER = ITEMS.registerSimpleItem("sulfur_powder");

    public static final DeferredItem<PersonalDiaryItem> PERSONAL_DIARY =
            ITEMS.registerItem("personal_diary", PersonalDiaryItem::new);

    public static final DeferredItem<HeadphonesItem> HEADPHONES =
            ITEMS.registerItem("headphones", HeadphonesItem::new);

    public static final DeferredItem<HerbalTeaItem> HERBAL_TEA =
            ITEMS.registerItem("herbal_tea", HerbalTeaItem::new);

    public static final DeferredItem<CalmingMixtureItem> CALMING_MIXTURE =
            ITEMS.registerItem("calming_mixture", CalmingMixtureItem::new);

    public static final DeferredItem<SleepingAidItem> SLEEPING_AID =
            ITEMS.registerItem("sleeping_aid", SleepingAidItem::new);

    public static final DeferredItem<OverdoseAntidoteItem> OVERDOSE_ANTIDOTE =
            ITEMS.registerItem("overdose_antidote", OverdoseAntidoteItem::new);

    public static final DeferredItem<Item> RESIN =
            ITEMS.registerSimpleItem("resin");

    public static final DeferredItem<Item> STONE_HAMMER =
            ITEMS.registerSimpleItem("stone_hammer", props -> props.stacksTo(1).durability(20));

    public static final DeferredItem<Item> COPPER_PLATE =
            ITEMS.registerSimpleItem("copper_plate");

    public static final DeferredItem<Item> COPPER_STRAPPING =
            ITEMS.registerSimpleItem("copper_strapping");

    public static final DeferredItem<Item> WOODEN_FRAME =
            ITEMS.registerSimpleItem("wooden_frame");

    public static final DeferredItem<Item> HEAVY_IRON =
            ITEMS.registerSimpleItem("heavy_iron");

    public static final DeferredItem<Item> IRON_AXLE =
            ITEMS.registerSimpleItem("iron_axle");

    public static final DeferredItem<Item> IRON_HAMMER =
            ITEMS.registerSimpleItem("iron_hammer", props -> props.stacksTo(1).durability(192));

    public static final DeferredItem<Item> COAL_DUST =
            ITEMS.registerSimpleItem("coal_dust");

    public static final DeferredItem<Item> ACTIVATED_COAL =
            ITEMS.registerSimpleItem("activated_coal");

    public static final DeferredItem<Item> POROUS_CLAY =
            ITEMS.registerSimpleItem("porous_clay");

    public static final DeferredItem<Item> POROUS_CERAMIC =
            ITEMS.registerSimpleItem("porous_ceramic");

    public static final DeferredItem<Item> GLASS_TUBE =
            ITEMS.registerSimpleItem("glass_tube");

    public static final DeferredItem<Item> HAND_CRANK =
            ITEMS.registerSimpleItem("hand_crank");

    public static final DeferredItem<Item> FILTER_BOX =
            ITEMS.registerSimpleItem("filter_box");

    public static final DeferredItem<Item> ROTOR =
            ITEMS.registerSimpleItem("rotor");

    public static final DeferredItem<Item> RAW_THICK_GLASS =
            ITEMS.registerSimpleItem("raw_thick_glass");

    public static final DeferredItem<Item> THICK_GLASS =
            ITEMS.registerSimpleItem("thick_glass");

    public static final DeferredItem<Item> SOFT_SEAL =
            ITEMS.registerSimpleItem("soft_seal");

    public static final DeferredItem<Item> WATERING_CONNECTION =
            ITEMS.registerSimpleItem("watering_connection");

    public static final DeferredItem<Item> STEEL_BLEND =
            ITEMS.registerSimpleItem("steel_blend");

    public static final DeferredItem<Item> STEEL_INGOT =
            ITEMS.registerSimpleItem("steel_ingot");

    public static final DeferredItem<Item> STEEL_HAMMER =
            ITEMS.registerSimpleItem("steel_hammer", props -> props.stacksTo(1).durability(384));

    public static final DeferredItem<Item> PIPE_WRENCH =
            ITEMS.registerItem("pipe_wrench", PipeWrenchItem::new, props -> props.stacksTo(1).durability(256));

    public static final DeferredItem<Item> PIPE_FILTER_UPGRADE =
            ITEMS.registerItem(
                    "pipe_filter_upgrade",
                    PipeFilterUpgradeItem::new,
                    props -> props.stacksTo(1).component(
                            ModDataComponents.PIPE_FILTER_CONFIG.get(),
                            PipeFilterConfig.empty(PipeResourceKind.ITEM)
                    )
            );

    public static final DeferredItem<Item> MACHINE_TRANSFER_UPGRADE =
            ITEMS.registerItem("machine_transfer_upgrade", MachineTransferUpgradeItem::new, props -> props.stacksTo(1));

    public static final DeferredItem<Item> ENERGY_UPGRADE =
            ITEMS.registerItem("energy_upgrade", EnergyUpgradeItem::new, props -> props.stacksTo(1));

    public static final DeferredItem<Item> AUTOMATION_UPGRADE =
            ITEMS.registerItem("automation_upgrade", AutomationUpgradeItem::new, props -> props.stacksTo(1));

    public static final DeferredItem<Item> STEEL_PLATE =
            ITEMS.registerSimpleItem("steel_plate");

    public static final DeferredItem<Item> RAW_RUBBER =
            ITEMS.registerSimpleItem("raw_rubber");

    public static final DeferredItem<Item> RUBBER =
            ITEMS.registerSimpleItem("rubber");

    public static final DeferredItem<Item> REFRACTORY_MIX =
            ITEMS.registerSimpleItem("refractory_mix");

    public static final DeferredItem<Item> REFRACTORY_BRICK =
            ITEMS.registerSimpleItem("refractory_brick");

    public static final DeferredItem<Item> TIGHT_SEAL =
            ITEMS.registerSimpleItem("tight_seal");

    public static final DeferredItem<Item> REINFORCED_CASING =
            ITEMS.registerSimpleItem("reinforced_casing");

    public static final DeferredItem<Item> AGITATOR =
            ITEMS.registerSimpleItem("agitator");

    public static final DeferredItem<Item> HEAT_LINING =
            ITEMS.registerSimpleItem("heat_lining");

    public static final DeferredItem<Item> COPPER_TUBE =
            ITEMS.registerSimpleItem("copper_tube");

    public static final DeferredItem<Item> PRESSURE_SEAL =
            ITEMS.registerSimpleItem("pressure_seal");

    public static final DeferredItem<Item> VALVE =
            ITEMS.registerSimpleItem("valve");

    public static final DeferredItem<Item> TANK_WALL =
            ITEMS.registerSimpleItem("tank_wall");

    public static final DeferredItem<Item> PRESSURE_CASING =
            ITEMS.registerSimpleItem("pressure_casing");

    public static final DeferredItem<Item> MEMBRANE =
            ITEMS.registerSimpleItem("membrane");

    public static final DeferredItem<Item> PUMP_HEAD =
            ITEMS.registerSimpleItem("pump_head");

    public static final DeferredItem<Item> INJECTOR_NOZZLE =
            ITEMS.registerSimpleItem("injector_nozzle");

    public static final DeferredItem<Item> REACTION_CORE =
            ITEMS.registerSimpleItem("reaction_core");

    public static final DeferredItem<Item> CUP = ITEMS.registerSimpleItem("cup");

    public static final DeferredItem<Item> RAW_PLATINUM =
            ITEMS.registerSimpleItem("raw_platinum");

    public static final DeferredItem<Item> PLATINUM_INGOT =
            ITEMS.registerSimpleItem("platinum_ingot");

    public static final DeferredItem<Item> RAW_ALUMINIUM =
            ITEMS.registerSimpleItem("raw_aluminium");

    public static final DeferredItem<Item> ALUMINIUM_INGOT =
            ITEMS.registerSimpleItem("aluminium_ingot");

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
