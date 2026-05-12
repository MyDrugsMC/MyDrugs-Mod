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
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.PipeTier;
import org.mydrugs.mydrugs.pipe.block.PipeBlock;
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

    public static final DeferredItem<BlockItem> STOMP_PLATE_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(STOMP_PLATE_BLOCK);


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
            props -> props.strength(3.0F).noOcclusion()
    );

    public static final DeferredBlock<GasPumpBlock> GAS_PUMP = BLOCKS.registerBlock(
            "gas_pump",
            GasPumpBlock::new,
            props -> props.strength(2.0F)
    );

    public static final DeferredItem<BlockItem> GAS_PUMP_ITEM = ITEMS.registerSimpleBlockItem(GAS_PUMP);

    public static final DeferredBlock<FluidPumpBlock> FLUID_PUMP = BLOCKS.registerBlock(
            "fluid_pump",
            FluidPumpBlock::new,
            props -> props.strength(2.5F).sound(SoundType.METAL).noOcclusion()
    );

    public static final DeferredItem<BlockItem> FLUID_PUMP_ITEM = ITEMS.registerSimpleBlockItem(FLUID_PUMP);

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


    public static final DeferredBlock<ManualCoffeePulperBlock> MANUAL_COFFEE_PULPER = BLOCKS.registerBlock(
            "manual_coffee_pulper",
            ManualCoffeePulperBlock::new,
            props -> props.strength(1.5F).sound(SoundType.WOOD).noOcclusion()
    );

    public static final DeferredItem<BlockItem> MANUAL_COFFEE_PULPER_ITEM =
            ITEMS.registerSimpleBlockItem(MANUAL_COFFEE_PULPER);

    public static final DeferredBlock<CoffeeDryingMatBlock> COFFEE_DRYING_MAT = BLOCKS.registerBlock(
            "coffee_drying_mat",
            CoffeeDryingMatBlock::new,
            props -> props.strength(0.4F).sound(SoundType.WOOL).noOcclusion()
    );

    public static final DeferredItem<BlockItem> COFFEE_DRYING_MAT_ITEM =
            ITEMS.registerSimpleBlockItem(COFFEE_DRYING_MAT);

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

    public static final DeferredBlock<PsyAnvilBlock> PSY_ANVIL = BLOCKS.registerBlock(
            "psy_anvil",
            PsyAnvilBlock::new,
            props -> props
                    .strength(4.0F)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.ANVIL)
                    .noOcclusion()
    );

    public static final DeferredItem<BlockItem> PSY_ANVIL_ITEM = ITEMS.registerSimpleBlockItem(PSY_ANVIL);

    public static final DeferredBlock<VomitSplashBlock> VOMIT_SPLASH = BLOCKS.registerBlock(
            "vomit_splash",
            VomitSplashBlock::new,
            props -> props
                    .strength(0.1F)
                    .sound(SoundType.SLIME_BLOCK)
                    .noCollision()
                    .noOcclusion()
                    .replaceable()
                    .noLootTable()
    );

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

    public static final DeferredBlock<PipeBlock> BASIC_ITEM_PIPE = BLOCKS.registerBlock(
            "basic_item_pipe",
            props -> new PipeBlock(props.noOcclusion(), PipeResourceKind.ITEM, PipeTier.BASIC),
            props -> props.strength(0.8F).sound(SoundType.METAL)
    );

    public static final DeferredItem<BlockItem> BASIC_ITEM_PIPE_ITEM = ITEMS.registerSimpleBlockItem(BASIC_ITEM_PIPE);

    public static final DeferredBlock<PipeBlock> FAST_ITEM_PIPE = BLOCKS.registerBlock(
            "fast_item_pipe",
            props -> new PipeBlock(props.noOcclusion(), PipeResourceKind.ITEM, PipeTier.FAST),
            props -> props.strength(1.0F).sound(SoundType.METAL)
    );

    public static final DeferredItem<BlockItem> FAST_ITEM_PIPE_ITEM = ITEMS.registerSimpleBlockItem(FAST_ITEM_PIPE);

    public static final DeferredBlock<PipeBlock> BASIC_FLUID_PIPE = BLOCKS.registerBlock(
            "basic_fluid_pipe",
            props -> new PipeBlock(props.noOcclusion(), PipeResourceKind.FLUID, PipeTier.BASIC),
            props -> props.strength(0.8F).sound(SoundType.METAL)
    );

    public static final DeferredItem<BlockItem> BASIC_FLUID_PIPE_ITEM = ITEMS.registerSimpleBlockItem(BASIC_FLUID_PIPE);

    public static final DeferredBlock<PipeBlock> FAST_FLUID_PIPE = BLOCKS.registerBlock(
            "fast_fluid_pipe",
            props -> new PipeBlock(props.noOcclusion(), PipeResourceKind.FLUID, PipeTier.FAST),
            props -> props.strength(1.0F).sound(SoundType.METAL)
    );

    public static final DeferredItem<BlockItem> FAST_FLUID_PIPE_ITEM = ITEMS.registerSimpleBlockItem(FAST_FLUID_PIPE);

    public static final DeferredBlock<PipeBlock> BASIC_GAS_PIPE = BLOCKS.registerBlock(
            "basic_gas_pipe",
            props -> new PipeBlock(props.noOcclusion(), PipeResourceKind.GAS, PipeTier.BASIC),
            props -> props.strength(0.8F).sound(SoundType.METAL)
    );

    public static final DeferredItem<BlockItem> BASIC_GAS_PIPE_ITEM = ITEMS.registerSimpleBlockItem(BASIC_GAS_PIPE);

    public static final DeferredBlock<PipeBlock> FAST_GAS_PIPE = BLOCKS.registerBlock(
            "fast_gas_pipe",
            props -> new PipeBlock(props.noOcclusion(), PipeResourceKind.GAS, PipeTier.FAST),
            props -> props.strength(1.0F).sound(SoundType.METAL)
    );

    public static final DeferredItem<BlockItem> FAST_GAS_PIPE_ITEM = ITEMS.registerSimpleBlockItem(FAST_GAS_PIPE);


    public static final DeferredBlock<CatalyticReformerBlock> CATALYTIC_REFORMER = BLOCKS.registerBlock(
            "catalytic_reformer",
            CatalyticReformerBlock::new,
            props -> props
                    .strength(3.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    public static final DeferredItem<BlockItem> CATALYTIC_REFORMER_ITEM = ITEMS.registerSimpleBlockItem(CATALYTIC_REFORMER);

    public static final DeferredBlock<SteamCrackerBlock> STEAM_CRACKER = BLOCKS.registerBlock(
            "steam_cracker",
            SteamCrackerBlock::new,
            props -> props
                    .strength(3.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    public static final DeferredItem<BlockItem> STEAM_CRACKER_ITEM = ITEMS.registerSimpleBlockItem(STEAM_CRACKER);

    public static final DeferredBlock<PsychotropeComponentBlock> PSYCHOTROPE_COMPONENT = BLOCKS.registerBlock(
            "psychotrope_component",
            PsychotropeComponentBlock::new,
            props -> props
                    .strength(3.5F)
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 4)
    );

    public static final DeferredItem<BlockItem> PSYCHOTROPE_COMPONENT_ITEM =
            ITEMS.registerSimpleBlockItem(PSYCHOTROPE_COMPONENT);

    public static final DeferredBlock<PsychotropeCoreBlock> PSYCHOTROPE_CORE = BLOCKS.registerBlock(
            "psychotrope_core",
            PsychotropeCoreBlock::new,
            props -> props
                    .strength(5.0F)
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 8)
    );

    public static final DeferredItem<BlockItem> PSYCHOTROPE_CORE_ITEM =
            ITEMS.registerSimpleBlockItem(PSYCHOTROPE_CORE);

    public static final DeferredBlock<Block> RAW_PLATINUM_BLOCK = BLOCKS.registerSimpleBlock(
            "raw_platinum_block",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).requiresCorrectToolForDrops()
    );

    public static final DeferredItem<BlockItem> RAW_PLATINUM_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(RAW_PLATINUM_BLOCK);

    public static final DeferredBlock<Block> PLATINUM_BLOCK = BLOCKS.registerSimpleBlock(
            "platinum_block",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_BLOCK).requiresCorrectToolForDrops()
    );

    public static final DeferredItem<BlockItem> PLATINUM_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(PLATINUM_BLOCK);

    public static final DeferredBlock<Block> RAW_ALUMINIUM_BLOCK = BLOCKS.registerSimpleBlock(
            "raw_aluminium_block",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK).requiresCorrectToolForDrops()
    );

    public static final DeferredItem<BlockItem> RAW_ALUMINIUM_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(RAW_ALUMINIUM_BLOCK);

    public static final DeferredBlock<Block> ALUMINIUM_BLOCK = BLOCKS.registerSimpleBlock(
            "aluminium_block",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops()
    );

    public static final DeferredItem<BlockItem> ALUMINIUM_BLOCK_ITEM =
            ITEMS.registerSimpleBlockItem(ALUMINIUM_BLOCK);

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

    public static final DeferredBlock<Block> ALUMINIUM_ORE = BLOCKS.registerSimpleBlock(
            "aluminium_ore",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
    );

    public static final DeferredItem<BlockItem> ALUMINIUM_ORE_ITEM =
            ITEMS.registerSimpleBlockItem(ALUMINIUM_ORE);

    public static final DeferredBlock<Block> DEEPSLATE_ALUMINIUM_ORE = BLOCKS.registerSimpleBlock(
            "deepslate_aluminium_ore",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)
    );

    public static final DeferredItem<BlockItem> DEEPSLATE_ALUMINIUM_ORE_ITEM =
            ITEMS.registerSimpleBlockItem(DEEPSLATE_ALUMINIUM_ORE);

    // Psy Mixer ritual blocks
    public static final DeferredBlock<Block> WOVEN_VINE_FRAME = BLOCKS.registerBlock(
            "woven_vine_frame",
            Block::new,
            props -> props.strength(1.5F).sound(SoundType.WOOD).noOcclusion()
    );
    public static final DeferredItem<BlockItem> WOVEN_VINE_FRAME_ITEM = ITEMS.registerSimpleBlockItem(WOVEN_VINE_FRAME);

    public static final DeferredBlock<Block> RITUAL_BARK = BLOCKS.registerBlock(
            "ritual_bark",
            Block::new,
            props -> props.strength(2.0F).sound(SoundType.WOOD)
    );
    public static final DeferredItem<BlockItem> RITUAL_BARK_ITEM = ITEMS.registerSimpleBlockItem(RITUAL_BARK);

    public static final DeferredBlock<Block> CHARCOAL_GLYPH_BLOCK = BLOCKS.registerBlock(
            "charcoal_glyph_block",
            Block::new,
            props -> props.strength(2.0F).sound(SoundType.STONE).lightLevel(state -> 3)
    );
    public static final DeferredItem<BlockItem> CHARCOAL_GLYPH_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(CHARCOAL_GLYPH_BLOCK);

    public static final DeferredBlock<PaintedClayBowlBlock> PAINTED_CLAY_BOWL = BLOCKS.registerBlock(
            "painted_clay_bowl",
            PaintedClayBowlBlock::new,
            props -> props.strength(0.8F).sound(SoundType.MUD_BRICKS).noOcclusion()
    );
    public static final DeferredItem<BlockItem> PAINTED_CLAY_BOWL_ITEM = ITEMS.registerSimpleBlockItem(PAINTED_CLAY_BOWL);

    public static final DeferredBlock<HangingVineBundleBlock> HANGING_VINE_BUNDLE = BLOCKS.registerBlock(
            "hanging_vine_bundle",
            HangingVineBundleBlock::new,
            props -> props.strength(0.4F).sound(SoundType.GRASS).noOcclusion()
    );
    public static final DeferredItem<BlockItem> HANGING_VINE_BUNDLE_ITEM = ITEMS.registerSimpleBlockItem(HANGING_VINE_BUNDLE);

    public static final DeferredBlock<Block> MYCELIAL_PADDING = BLOCKS.registerBlock(
            "mycelial_padding",
            Block::new,
            props -> props.strength(0.6F).sound(SoundType.WOOL)
    );
    public static final DeferredItem<BlockItem> MYCELIAL_PADDING_ITEM = ITEMS.registerSimpleBlockItem(MYCELIAL_PADDING);

    public static final DeferredBlock<FormedPsyMixerCoreBlock> FORMED_PSY_MIXER_CORE = BLOCKS.registerBlock(
            "formed_psy_mixer_core",
            FormedPsyMixerCoreBlock::new,
            props -> props.strength(2.5F).sound(SoundType.MUD_BRICKS).noOcclusion().noLootTable()
    );

    public static final DeferredBlock<FormedPsyMixerPartBlock> FORMED_PSY_MIXER_PART = BLOCKS.registerBlock(
            "formed_psy_mixer_part",
            FormedPsyMixerPartBlock::new,
            props -> props.strength(2.0F).sound(SoundType.WOOD).noOcclusion().noLootTable()
    );

    // Cocaine preparation: in-world powder pile / rail. No creative item form.
    public static final DeferredBlock<CocainePowderPileBlock> COCAINE_POWDER_PILE = BLOCKS.registerBlock(
            "cocaine_powder_pile",
            CocainePowderPileBlock::new,
            props -> props
                    .strength(0.0F)
                    .sound(SoundType.SAND)
                    .noOcclusion()
                    .noLootTable()
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
    );

    // ===== PR 3 worldgen blocks =====
    // Bitter Nut Bush: worldgen-only, non-replantable, random-tick regrowth. No BlockItem on purpose.
    public static final DeferredBlock<BitterNutBushBlock> BITTER_NUT_BUSH = BLOCKS.registerBlock(
            "bitter_nut_bush",
            BitterNutBushBlock::new,
            props -> props
                    .strength(0.0F)
                    .randomTicks()
                    .noCollision()
                    .sound(SoundType.SWEET_BERRY_BUSH)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
    );

    // Third Eye Petal: rare flower from worldgen, also obtainable as item.
    public static final DeferredBlock<ThirdEyePetalBlock> THIRD_EYE_PETAL = BLOCKS.registerBlock(
            "third_eye_petal",
            ThirdEyePetalBlock::new,
            props -> props
                    .strength(0.0F)
                    .noCollision()
                    .sound(SoundType.GRASS)
                    .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
    );

    public static final DeferredItem<BlockItem> THIRD_EYE_PETAL_ITEM =
            ITEMS.registerSimpleBlockItem(THIRD_EYE_PETAL);

    private ModBlocks() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        BLOCKS.register(bus);
    }
}
