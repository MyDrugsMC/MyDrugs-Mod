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
import net.minecraft.world.item.equipment.ArmorType;
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
import org.mydrugs.mydrugs.items.data.BiomeFinderTarget;
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

    public static final DeferredItem<Item> IRON_MESH =
            ITEMS.registerSimpleItem("iron_mesh");

    public static final DeferredItem<Item> CIGARET_FILTER =
            ITEMS.registerSimpleItem("cigaret_filter");

    public static final DeferredItem<Item> COCAINE_POWDER =
            ITEMS.registerItem("cocaine_powder", prop -> new CocainePowderItem(prop, DrugId.COCAINE, new SniffingStrategy()));

    public static final DeferredItem<Item> CRACK_SHARD =
            ITEMS.registerItem("crack_shard", prop -> new CrackShardItem(prop, DrugId.CRACK, new SmokingStrategy(true, false)));

    public static final DeferredItem<Item> CRACK_PLATE =
            ITEMS.registerSimpleItem("crack_plate");

    public static final DeferredItem<Item> GLASS_BOTTLE =
            ITEMS.registerItem("glass_bottle",
                    GlassBottleItem::new,
                    properties -> properties.stacksTo(16));

    public static final DeferredItem<Item> TOBACCO_HANDFUL =
            ITEMS.registerItem("tobacco_handful", prop -> new TobaccoHandfulItem(prop, DrugId.TOBACCO, new SmokingStrategy(true, true)));

    public static final DeferredItem<Item> ALOE_VERA =
            ITEMS.registerItem("aloe_vera", prop -> new BlockItem(org.mydrugs.mydrugs.blocks.crops.ModCrops.ALOE_VERA_CROP.get(), prop));

    public static final DeferredItem<Item> SOOTHING_TOBACCO_BLEND =
            ITEMS.registerItem("soothing_tobacco_blend", prop -> new TobaccoHandfulItem(prop, DrugId.TOBACCO, new SmokingStrategy(true, true)));

    public static final DeferredItem<Item> MIXED_DRUG =
            ITEMS.registerItem("mixed_drug", prop -> new MixedDrugItem(prop, new SmokingStrategy(true, true)));

    public static final DeferredItem<Item> MIXED_WEED_DRUG =
            ITEMS.registerItem("mixed_weed_drug", prop -> new MixedDrugItem(prop, new SmokingStrategy(true, true)));

    public static final DeferredItem<Item> MIXED_TOBACCO_DRUG =
            ITEMS.registerItem("mixed_tobacco_drug", prop -> new MixedDrugItem(prop, new SmokingStrategy(true, true)));

    public static final DeferredItem<Item> MIXED_LSD_DRUG =
            ITEMS.registerItem("mixed_lsd_drug", prop -> new MixedDrugItem(prop, new EatingStrategy()));

    public static final DeferredItem<Item> MIXED_MUSHROOMS_DRUG =
            ITEMS.registerItem("mixed_mushrooms_drug", prop -> new MixedDrugItem(prop, new EatingStrategy()));

    public static final DeferredItem<Item> MIXED_HASH_DRUG =
            ITEMS.registerItem("mixed_hash_drug", prop -> new MixedDrugItem(prop, new SmokingStrategy(true, true)));

    public static final DeferredItem<Item> MIXED_METH_DRUG =
            ITEMS.registerItem("mixed_meth_drug", prop -> new MixedDrugItem(prop, new SmokingStrategy(true, false)));

    public static final DeferredItem<Item> MIXED_COCAINE_DRUG =
            ITEMS.registerItem("mixed_cocaine_drug", prop -> new MixedDrugItem(prop, new SniffingStrategy()));

    public static final DeferredItem<Item> MIXED_CRACK_DRUG =
            ITEMS.registerItem("mixed_crack_drug", prop -> new MixedDrugItem(prop, new SmokingStrategy(true, false)));

    public static final DeferredItem<Item> MIXED_COFFEE_DRUG =
            ITEMS.registerItem("mixed_coffee_drug", prop -> new MixedDrugItem(prop, new org.mydrugs.mydrugs.core.drug.strategy.DrinkingStrategy()));

    public static final DeferredItem<Item> DEFIANT_SPIRIT_BOTTLE =
            ITEMS.registerItem("defiant_spirit_bottle", prop -> new MixedDrugItem(prop, new org.mydrugs.mydrugs.core.drug.strategy.DrinkingStrategy()));

    public static final DeferredItem<Item> DRUG_ANALYZER =
            ITEMS.registerItem("drug_analyzer", DrugAnalyzerItem::new);

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

    public static final DeferredItem<Item> COFFEE_CHERRIES =
            ITEMS.registerSimpleItem("coffee_cherries");

    public static final DeferredItem<Item> WET_COFFEE_BEAN =
            ITEMS.registerSimpleItem("wet_coffee_bean");

    public static final DeferredItem<Item> COFFEE_BEAN =
            ITEMS.registerSimpleItem("coffee_bean");

    public static final DeferredItem<Item> COFFEE_POWDER =
            ITEMS.registerSimpleItem("coffee_powder");

    public static final DeferredItem<Item> COFFEE_CUP =
            ITEMS.registerItem("coffee_cup", prop -> new org.mydrugs.mydrugs.items.drugs.CoffeeCupItem(prop, DrugId.COFFEE, new org.mydrugs.mydrugs.core.drug.strategy.DrinkingStrategy()));


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

    public static final DeferredItem<ProgressionGuideItem> PROGRESSION_GUIDE =
            ITEMS.registerItem("progression_guide", ProgressionGuideItem::new, props -> props.stacksTo(1));

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

    public static final DeferredItem<Item> PSY_RECEPTACLE =
            ITEMS.registerItem("psy_receptacle",
                    props -> new PsyTooltipItem(props, "tooltip.mydrugs.psy_receptacle"));

    public static final DeferredItem<Item> PSY_BLUEPRINT =
            ITEMS.registerItem("psy_blueprint", PsyBlueprintItem::new);

    public static final DeferredItem<Item> COPPER_PLATE =
            ITEMS.registerSimpleItem("copper_plate");

    public static final DeferredItem<Item> COPPER_STRAPPING =
            ITEMS.registerSimpleItem("copper_strapping");

    public static final DeferredItem<Item> WOODEN_FRAME =
            ITEMS.registerSimpleItem("wooden_frame");

    public static final DeferredItem<Item> HEAVY_IRON =
            ITEMS.registerSimpleItem("heavy_iron");

    public static final DeferredItem<Item> HEAVY_IRON_PLATE =
            ITEMS.registerSimpleItem("heavy_iron_plate");

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

    public static final DeferredItem<Item> INSULATED_WIRE =
            ITEMS.registerSimpleItem("insulated_wire");

    public static final DeferredItem<Item> CONTROL_CIRCUIT =
            ITEMS.registerSimpleItem("control_circuit");

    public static final DeferredItem<Item> ADVANCED_CONTROL_CIRCUIT =
            ITEMS.registerSimpleItem("advanced_control_circuit");

    public static final DeferredItem<Item> MYCELIAL_RESONATOR =
            ITEMS.registerItem("mycelial_resonator",
                    props -> new PsyTooltipItem(
                            props.stacksTo(1),
                            "tooltip.mydrugs.mycelial_resonator",
                            "message.mydrugs.mycelial_resonator.use"
                    ));

    public static final DeferredItem<Item> ELECTRIC_MOTOR =
            ITEMS.registerSimpleItem("electric_motor");

    public static final DeferredItem<Item> HEATING_COIL =
            ITEMS.registerSimpleItem("heating_coil");

    public static final DeferredItem<Item> CONDENSER_COIL =
            ITEMS.registerSimpleItem("condenser_coil");

    public static final DeferredItem<Item> ELECTRODE_PAIR =
            ITEMS.registerSimpleItem("electrode_pair");

    public static final DeferredItem<Item> CATALYST_BED =
            ITEMS.registerSimpleItem("catalyst_bed");

    public static final DeferredItem<Item> PACKED_COLUMN =
            ITEMS.registerSimpleItem("packed_column");

    public static final DeferredItem<Item> PIPE_JOINT =
            ITEMS.registerSimpleItem("pipe_joint");

    public static final DeferredItem<Item> PSYCHOTROPE_LENS =
            ITEMS.registerSimpleItem("psychotrope_lens");

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

    public static final DeferredItem<Item> ALUMINIUM_HELMET =
            ITEMS.registerItem("aluminium_helmet", props -> new Item(props.humanoidArmor(ModArmorMaterials.ALUMINIUM, ArmorType.HELMET)));

    public static final DeferredItem<Item> ALUMINIUM_CHESTPLATE =
            ITEMS.registerItem("aluminium_chestplate", props -> new Item(props.humanoidArmor(ModArmorMaterials.ALUMINIUM, ArmorType.CHESTPLATE)));

    public static final DeferredItem<Item> ALUMINIUM_LEGGINGS =
            ITEMS.registerItem("aluminium_leggings", props -> new Item(props.humanoidArmor(ModArmorMaterials.ALUMINIUM, ArmorType.LEGGINGS)));

    public static final DeferredItem<Item> ALUMINIUM_BOOTS =
            ITEMS.registerItem("aluminium_boots", props -> new Item(props.humanoidArmor(ModArmorMaterials.ALUMINIUM, ArmorType.BOOTS)));

    public static final DeferredItem<Item> PLATINUM_HELMET =
            ITEMS.registerItem("platinum_helmet", props -> new Item(props.humanoidArmor(ModArmorMaterials.PLATINUM, ArmorType.HELMET)));

    public static final DeferredItem<Item> PLATINUM_CHESTPLATE =
            ITEMS.registerItem("platinum_chestplate", props -> new Item(props.humanoidArmor(ModArmorMaterials.PLATINUM, ArmorType.CHESTPLATE)));

    public static final DeferredItem<Item> PLATINUM_LEGGINGS =
            ITEMS.registerItem("platinum_leggings", props -> new Item(props.humanoidArmor(ModArmorMaterials.PLATINUM, ArmorType.LEGGINGS)));

    public static final DeferredItem<Item> PLATINUM_BOOTS =
            ITEMS.registerItem("platinum_boots", props -> new Item(props.humanoidArmor(ModArmorMaterials.PLATINUM, ArmorType.BOOTS)));

    // Psy Mixer ritual ingredients
    public static final DeferredItem<Item> RITUAL_THREADS =
            ITEMS.registerSimpleItem("ritual_threads");

    public static final DeferredItem<Item> PSYCHOTROPIC_PIGMENT =
            ITEMS.registerSimpleItem("psychotropic_pigment");

    public static final DeferredItem<Item> RITUAL_RESIN =
            ITEMS.registerSimpleItem("ritual_resin");

    public static final DeferredItem<Item> UNSTABLE_RESIDUE =
            ITEMS.registerSimpleItem("unstable_residue");

    public static final DeferredItem<Item> INNER_DEMON_REMAINS =
            ITEMS.registerItem("inner_demon_remains",
                    props -> new PsyTooltipItem(props, "tooltip.mydrugs.inner_demon_remains"));

    public static final DeferredItem<Item> BRIGHTENED_CANNABIS_POWDER =
            ITEMS.registerItem("brightened_cannabis_powder",
                    prop -> new CannabisPowderItem(
                            prop, DrugId.WEED,
                            new SmokingStrategy(true, true)));

    public static final DeferredItem<VanillaBiomeFinderItem> VANILLA_BIOME_FINDER =
            ITEMS.registerItem("vanilla_biome_finder",
                    props -> new VanillaBiomeFinderItem(props.stacksTo(1)
                            .component(ModDataComponents.BIOME_FINDER_TARGET.get(),
                                    BiomeFinderTarget.EMPTY)));

    // ===== PR 3: Ritual mix ingredients =====
    public static final DeferredItem<Item> CALMING_SPORES =
            ITEMS.registerItem("calming_spores",
                    props -> new PsyTooltipItem(props, "tooltip.mydrugs.calming_spores"));

    public static final DeferredItem<Item> BITTER_NUT =
            ITEMS.registerItem("bitter_nut",
                    props -> new PsyTooltipItem(props, "tooltip.mydrugs.bitter_nut"));

    public static final DeferredItem<Item> CHARGED_SINEW =
            ITEMS.registerItem("charged_sinew",
                    props -> new PsyTooltipItem(props, "tooltip.mydrugs.charged_sinew"));

    public static final DeferredItem<Item> FRACTURED_IMPULSE =
            ITEMS.registerItem("fractured_impulse",
                    props -> new PsyTooltipItem(props, "tooltip.mydrugs.fractured_impulse"));

    public static final DeferredItem<Item> CHARGED_CORE =
            ITEMS.registerItem("charged_core",
                    props -> new PsyTooltipItem(props, "tooltip.mydrugs.charged_core"));

    public static final DeferredItem<Item> BROKEN_COURAGE =
            ITEMS.registerItem("broken_courage",
                    props -> new PsyTooltipItem(props, "tooltip.mydrugs.broken_courage"));

    public static final DeferredItem<Item> DREAMCAP_SPORES =
            ITEMS.registerItem("dreamcap_spores",
                    props -> new PsyTooltipItem(props, "tooltip.mydrugs.dreamcap_spores"));

    // ===== PR 3: Support items =====
    public static final DeferredItem<Item> COPPER_NUGGET =
            ITEMS.registerSimpleItem("copper_nugget");

    public static final DeferredItem<Item> THUNDER_BOTTLE =
            ITEMS.registerItem("thunder_bottle",
                    props -> new PsyTooltipItem(props.stacksTo(16), "tooltip.mydrugs.thunder_bottle"));

    public static final DeferredItem<Item> LIGHTNING_BOTTLE =
            ITEMS.registerItem("lightning_bottle",
                    props -> new PsyTooltipItem(props.stacksTo(16), "tooltip.mydrugs.lightning_bottle"));

    public static final DeferredItem<Item> SHROOM_HARVESTER =
            ITEMS.registerItem("shroom_harvester",
                    props -> new ShroomHarvesterItem(props.stacksTo(1).durability(59)));

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
