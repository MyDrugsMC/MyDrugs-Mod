package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.effects.addiction.block.RecoveryAnchorBlock;
import org.mydrugs.mydrugs.effects.addiction.block.TherapistDeskBlock;
import org.mydrugs.mydrugs.worldgen.ModWorldGenKeys;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(MyDrugs.MODID);

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MyDrugs.MODID);

    public static final DeferredBlock<Block> PSYCHEDELIC_MYCELIUM =
            BLOCKS.register("psychedelic_mycelium",
                    registryName
                            -> new MyceliumBlock(
                            BlockBehaviour.Properties
                                    .ofFullCopy(Blocks.MYCELIUM)
                                    .setId(ResourceKey.create(Registries.BLOCK, registryName)))
            );

    public static final DeferredItem<BlockItem> PSYCHEDELIC_MYCELIUM_ITEM =
            ITEMS.registerSimpleBlockItem(PSYCHEDELIC_MYCELIUM);

    public static final DeferredBlock<MushroomBlock> MAGIC_MUSHROOM =
            BLOCKS.register("magic_mushroom", registryName ->
                    // In some mappings/setups the constructor order may appear reversed in your IDE.
                    // If that happens, swap the two arguments.
                    new MagicMushroomBlock(
                            BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM)
                                    .setId(ResourceKey.create(Registries.BLOCK, registryName)),
                            ModWorldGenKeys.HUGE_MAGIC_MUSHROOM
                    )
            );

    public static final DeferredBlock<HugeMushroomBlock> MAGIC_MUSHROOM_BLOCK =
            BLOCKS.registerBlock(
                    "magic_mushroom_block",
                    HugeMushroomBlock::new,
                    prop -> BlockBehaviour.Properties.ofFullCopy(Blocks.BROWN_MUSHROOM_BLOCK)
            );

    public static final DeferredItem<BlockItem> MAGIC_MUSHROOM_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(MAGIC_MUSHROOM_BLOCK);

    public static final DeferredBlock<HugeMushroomBlock> MAGIC_MUSHROOM_STEM =
            BLOCKS.registerBlock(
                    "magic_mushroom_stem",
                    HugeMushroomBlock::new,
                    prop -> BlockBehaviour.Properties.ofFullCopy(Blocks.MUSHROOM_STEM)
            );

    public static final DeferredItem<BlockItem> MAGIC_MUSHROOM_STEM_ITEM =
            ITEMS.registerSimpleBlockItem(MAGIC_MUSHROOM_STEM);

    public static final DeferredBlock<Block> GRINDING_BOWL = BLOCKS.registerBlock(
            "grinding_bowl",
            GrindingBowlBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(1.2F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    public static final DeferredItem<BlockItem> GRINDING_BOWL_ITEM =
            ITEMS.registerSimpleBlockItem(GRINDING_BOWL);


    public static final DeferredBlock<Block> STOMP_CRAFTER = BLOCKS.registerBlock(
            "stomp_crafter",
            StompCrafterBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(3.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
    );

    public static final DeferredItem<BlockItem> STOMP_CRAFTER_ITEM =
            ITEMS.registerSimpleBlockItem("stomp_crafter", STOMP_CRAFTER);

    public static final DeferredBlock<Block> STOMP_PLATE_BLOCK =
            BLOCKS.registerBlock(
                    "stomp_plate_block",
                    Block::new,
                    prop -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()
            );


    public static final DeferredBlock<Block> ADVANCED_FURNACE =
            BLOCKS.registerBlock("advanced_furnace",
                    AdvancedFurnaceBlock::new,
                    props -> props.strength(3.5f));


    public static final DeferredItem<BlockItem> ADVANCED_FURNACE_ITEM =
            ITEMS.registerSimpleBlockItem(ADVANCED_FURNACE);


    public static final DeferredBlock<Block> DISTILLER =
            BLOCKS.registerBlock("distiller",
                    DistillerBlock::new,
                    props -> props.strength(3.5f));

    public static final DeferredItem<BlockItem> DISTILLER_ITEM =
            ITEMS.registerSimpleBlockItem(DISTILLER);

    public static final DeferredBlock<Block> MIXING_VAT = BLOCKS.registerBlock(
            "mixing_vat",
            MixingVatBlock::new,
            props -> props.strength(2.5f).noOcclusion()
    );

    public static final DeferredItem<BlockItem> MIXING_VAT_ITEM =
            ITEMS.registerSimpleBlockItem(ModBlocks.MIXING_VAT);

    public static final DeferredBlock<Block> FLUID_FILTERER = BLOCKS.registerBlock(
            "fluid_filterer",
            FluidFiltererBlock::new,
            props -> props.strength(3.5f).noOcclusion()
    );

    public static final DeferredItem<BlockItem> FLUID_FILTERER_ITEM = ITEMS.registerSimpleBlockItem(FLUID_FILTERER);

    public static final DeferredBlock<SieveBlock> SIEVE = BLOCKS.registerBlock(
            "sieve",
            SieveBlock::new,
            prop -> prop
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    public static final DeferredItem<BlockItem> SIEVE_ITEM = ITEMS.registerSimpleBlockItem(SIEVE);

    public static final DeferredBlock<EvaporationTrayBlock> EVAPORATION_TRAY = BLOCKS.registerBlock(
            "evaporation_tray",
            EvaporationTrayBlock::new,
            props -> props
                    .strength(1.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    public static final DeferredItem<BlockItem> EVAPORATION_TRAY_ITEM = ITEMS.registerSimpleBlockItem(EVAPORATION_TRAY);

    public static final DeferredBlock<CentrifugeBlock> CENTRIFUGE = BLOCKS.registerBlock(
            "centrifuge",
            CentrifugeBlock::new,
            props -> props
                    .strength(1.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    public static final DeferredItem<BlockItem> CENTRIFUGE_ITEM = ITEMS.registerSimpleBlockItem(CENTRIFUGE);

    public static final DeferredBlock<BTXFractionationTowerBlock> BTX_FRACTIONATION_TOWER = BLOCKS.registerBlock(
            "btx_fractionation_tower",
            BTXFractionationTowerBlock::new,
            props -> props
                    .strength(3.5F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
    );

    public static final DeferredItem<BlockItem> BTX_FRACTIONATION_TOWER_ITEM =
            ITEMS.registerSimpleBlockItem(BTX_FRACTIONATION_TOWER);

    public static final DeferredBlock<AromaticExtractorBlock> AROMATIC_EXTRACTOR = BLOCKS.registerBlock(
            "aromatic_extractor",
            AromaticExtractorBlock::new,
            props -> props
                    .strength(3.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    public static final DeferredItem<BlockItem> AROMATIC_EXTRACTOR_ITEM = ITEMS.registerSimpleBlockItem(AROMATIC_EXTRACTOR);

    public static final DeferredBlock<ElectrolyzerBlock> ELECTROLYZER = BLOCKS.registerBlock(
            "electrolyzer",
            ElectrolyzerBlock::new,
            props -> props
                    .strength(1.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    public static final DeferredItem<BlockItem> ELECTROLYZER_ITEM = ITEMS.registerSimpleBlockItem(ELECTROLYZER);

    public static final DeferredBlock<GrowthChamberBlock> GROWTH_CHAMBER = BLOCKS.registerBlock(
            "growth_chamber",
            GrowthChamberBlock::new,
            props -> props
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredItem<BlockItem> GROWTH_CHAMBER_ITEM = ITEMS.registerSimpleBlockItem(GROWTH_CHAMBER);

    public static final DeferredBlock<Block> BIOCHEMICAL_REACTOR = BLOCKS.register(
            "biochemical_reactor",
            registryName -> new BiochemicalReactorBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
            )
    );

    public static final DeferredItem<BlockItem> BIOCHEMICAL_REACTOR_ITEM = ITEMS.registerSimpleBlockItem(
            BIOCHEMICAL_REACTOR
    );

    public static final DeferredBlock<Block> SALT_BLOCK = BLOCKS.registerSimpleBlock(
            "salt_block",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.CLAY)
    );

    public static final DeferredItem<BlockItem> SALT_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(SALT_BLOCK);

    public static final DeferredBlock<Block> SULFUR_ORE = BLOCKS.registerSimpleBlock(
            "sulfur_ore",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
    );

    public static final DeferredItem<BlockItem> SULFUR_ORE_ITEM =
            ITEMS.registerSimpleBlockItem(SULFUR_ORE);

    public static final DeferredBlock<Block> DEEPSLATE_SULFUR_ORE = BLOCKS.registerSimpleBlock(
            "deepslate_sulfur_ore",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)
    );

    public static final DeferredItem<BlockItem> DEEPSLATE_SULFUR_ORE_ITEM =
            ITEMS.registerSimpleBlockItem(DEEPSLATE_SULFUR_ORE);

    public static final DeferredBlock<GasTankBlock> GAS_TANK = BLOCKS.registerBlock(
            "gas_tank",
            GasTankBlock::new,
            props -> props.strength(3.0F)
    );

    public static final DeferredBlock<GasPumpBlock> GAS_PUMP = BLOCKS.registerBlock(
            "gas_pump",
            GasPumpBlock::new,
            props -> props.strength(2.0F)
    );

    public static final DeferredItem<BlockItem> GAS_PUMP_ITEM = ITEMS.registerSimpleBlockItem(GAS_PUMP);

    public static final DeferredBlock<GasifierBlock> GASIFIER = BLOCKS.registerBlock(
            "gasifier",
            GasifierBlock::new,
            props -> props
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredItem<BlockItem> GASIFIER_ITEM = ITEMS.registerSimpleBlockItem(GASIFIER);

    public static final DeferredBlock<Block> CHEMICAL_REACTOR =
            BLOCKS.registerBlock("chemical_reactor",
                    ChemicalReactorBlock::new,
                    props -> props
                            .strength(3.5F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)
            );

    public static final DeferredItem<BlockItem> CHEMICAL_REACTOR_ITEM = ITEMS.registerSimpleBlockItem(
            CHEMICAL_REACTOR
    );

    public static final DeferredBlock<AdvancedMixingVatBlock> ADVANCED_MIXING_VAT =
            BLOCKS.registerBlock(
                    "advanced_mixing_vat",
                    AdvancedMixingVatBlock::new,
                    props -> props
                            .strength(3.5F)
                            .requiresCorrectToolForDrops()
                            .sound(SoundType.METAL)

            );

    public static final DeferredItem<BlockItem> ADVANCED_MIXING_VAT_ITEM = ITEMS.registerSimpleBlockItem(ADVANCED_MIXING_VAT);

    public static final DeferredBlock<TherapistDeskBlock> THERAPIST_DESK =
            BLOCKS.registerBlock("therapist_desk", props -> new TherapistDeskBlock(props.strength(2.5F)));

    public static final DeferredItem<BlockItem> THERAPIST_DESK_ITEM =
            ITEMS.registerSimpleBlockItem(THERAPIST_DESK);

    public static final DeferredBlock<RecoveryAnchorBlock> RECOVERY_ANCHOR =
            BLOCKS.registerBlock("recovery_anchor", props -> new RecoveryAnchorBlock(props.strength(2.0F).lightLevel(state -> 8)));

    public static final DeferredItem<BlockItem> RECOVERY_ANCHOR_ITEM =
            ITEMS.registerSimpleBlockItem(RECOVERY_ANCHOR);

    public static final DeferredBlock<DryingRackBlock> DRYING_RACK = BLOCKS.registerBlock(
            "drying_rack",
            props -> new DryingRackBlock(
                    props
                            .strength(1.5F)
                            .noOcclusion()
            )
    );

    public static final DeferredItem<BlockItem> DRYING_RACK_ITEM =
            ITEMS.registerSimpleBlockItem(DRYING_RACK);

    public static final DeferredBlock<Block> CLAY_VAT = BLOCKS.registerBlock("clay_vat",
            properties -> new ClayVatBlock(
                   properties
                            .strength(1.2F)
                            .sound(SoundType.MUD_BRICKS)
                            .noOcclusion()
            )
    );

    public static final DeferredItem<BlockItem> CLAY_VAT_ITEM =
            ITEMS.registerSimpleBlockItem(CLAY_VAT);

//    public static final DeferredBlock<PsyAnvilBlock> PSY_ANVIL = BLOCKS.registerBlock(
//            "psy_anvil",
//            PsyAnvilBlock::new,
//            props -> props
//                    .strength(4.0F)
//                    .requiresCorrectToolForDrops()
//                    .sound(SoundType.ANVIL)
//                    .noOcclusion()
//    );
//
//    public static final DeferredItem<BlockItem> PSY_ANVIL_ITEM = ITEMS.registerSimpleBlockItem(PSY_ANVIL);

    public static final DeferredBlock<Block> TREATED_PLANKS = BLOCKS.registerBlock(
            "treated_planks",
            Block::new,
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
    );

    public static final DeferredItem<BlockItem> TREATED_PLANKS_ITEM = ITEMS.registerSimpleBlockItem(TREATED_PLANKS);

    public static final DeferredBlock<Block> MECHANICAL_FRAME = BLOCKS.registerBlock(
            "mechanical_frame",
            Block::new,
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
    );

    public static final DeferredItem<BlockItem> MECHANICAL_FRAME_ITEM = ITEMS.registerSimpleBlockItem(MECHANICAL_FRAME);


    public static final DeferredBlock<CatalyticReformerBlock> CATALYTIC_REFORMER = BLOCKS.registerBlock(
            "catalytic_reformer",
            CatalyticReformerBlock::new,
            props -> props
                    .strength(3.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    public static final DeferredItem<BlockItem> CATALYTIC_REFORMER_ITEM = ITEMS.registerSimpleBlockItem(CATALYTIC_REFORMER);

    public static final DeferredBlock<Block> PLATINUM_ORE = BLOCKS.registerSimpleBlock(
            "platinum_ore",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE)
    );

    public static final DeferredItem<BlockItem> PLATINUM_ORE_ITEM =
            ITEMS.registerSimpleBlockItem(PLATINUM_ORE);

    public static final DeferredBlock<Block> DEEPSLATE_PLATINUM_ORE = BLOCKS.registerSimpleBlock(
            "deepslate_platinum_ore",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_DIAMOND_ORE)
    );

    public static final DeferredItem<BlockItem> DEEPSLATE_PLATINUM_ORE_ITEM =
            ITEMS.registerSimpleBlockItem(DEEPSLATE_PLATINUM_ORE);

    private ModBlocks() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        BLOCKS.register(bus);
    }
}
