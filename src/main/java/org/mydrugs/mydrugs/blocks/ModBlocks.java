package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.crops.*;
import org.mydrugs.mydrugs.effects.addiction.block.RecoveryAnchorBlock;
import org.mydrugs.mydrugs.effects.addiction.block.TherapistDeskBlock;
import org.mydrugs.mydrugs.worldgen.ModWorldGenKeys;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(MyDrugs.MODID);

    public static final DeferredBlock<RyeCropBlock> RYE_CROP =
            BLOCKS.registerBlock(
                    "rye_crop",
                    RyeCropBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
            );

    public static final DeferredBlock<MaltCropBlock> MALT_CROP =
            BLOCKS.registerBlock(
                    "malt_crop",
                    MaltCropBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
            );

    public static final DeferredBlock<TobaccoCropBlock> TOBACCO_CROP =
            BLOCKS.registerBlock(
                    "tobacco_crop",
                    TobaccoCropBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
            );

    public static final DeferredBlock<CannabisCropBlock> CANNABIS_CROP =
            BLOCKS.registerBlock(
                    "cannabis_crop",
                    CannabisCropBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
            );

    public static final DeferredBlock<CocaCropBlock> COCA_CROP =
            BLOCKS.registerBlock(
                    "coca_crop",
                    CocaCropBlock::new,
                    () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
            );

    public static final DeferredBlock<Block> PSYCHEDELIC_MYCELIUM =
            BLOCKS.register("psychedelic_mycelium",
                    registryName
                            -> new MyceliumBlock(
                            BlockBehaviour.Properties
                                    .ofFullCopy(Blocks.MYCELIUM)
                                    .setId(ResourceKey.create(Registries.BLOCK, registryName)))
            );

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

    public static final DeferredBlock<HugeMushroomBlock> MAGIC_MUSHROOM_STEM =
            BLOCKS.registerBlock(
                    "magic_mushroom_stem",
                    HugeMushroomBlock::new,
                    prop -> BlockBehaviour.Properties.ofFullCopy(Blocks.MUSHROOM_STEM)
            );

    public static final DeferredBlock<Block> GRINDING_BOWL = BLOCKS.registerBlock(
            "grinding_bowl",
            GrindingBowlBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(1.2F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );


    public static final DeferredBlock<Block> STOMP_CRAFTER = BLOCKS.registerBlock(
            "stomp_crafter",
            StompCrafterBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(3.0F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
    );

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


    public static final DeferredBlock<Block> DISTILLER =
            BLOCKS.registerBlock("distiller",
                    DistillerBlock::new,
                    props -> props.strength(3.5f));


    public static final DeferredBlock<Block> MIXING_VAT = BLOCKS.registerBlock(
            "mixing_vat",
            MixingVatBlock::new,
            props -> props.strength(2.5f).noOcclusion()
    );

    public static final DeferredBlock<Block> FLUID_FILTERER  = BLOCKS.registerBlock(
            "fluid_filterer",
            FluidFiltererBlock::new,
            props -> props.strength(3.5f).noOcclusion()
    );


    public static final DeferredBlock<DryerBlock> DRYER = BLOCKS.registerBlock(
            "dryer",
            DryerBlock::new,
            props -> props
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );


    public static final DeferredBlock<SieveBlock> SIEVE = BLOCKS.registerBlock(
            "sieve",
            SieveBlock::new,
            prop -> prop
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );


    public static final DeferredBlock<EvaporationTrayBlock> EVAPORATION_TRAY = BLOCKS.registerBlock(
            "evaporation_tray",
            EvaporationTrayBlock::new,
            props -> props
                    .strength(1.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    public static final DeferredBlock<CentrifugeBlock> CENTRIFUGE = BLOCKS.registerBlock(
            "centrifuge",
            CentrifugeBlock::new,
            props -> props
                    .strength(1.5F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    public static final DeferredBlock<GrowthChamberBlock> GROWTH_CHAMBER = BLOCKS.registerBlock(
            "growth_chamber",
            GrowthChamberBlock::new,
            props -> props
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> BIOCHEMICAL_REACTOR = BLOCKS.register(
            "biochemical_reactor",
            registryName -> new BiochemicalReactorBlock(
                    BlockBehaviour.Properties.of()
                            .strength(3.5f)
                            .requiresCorrectToolForDrops()
                            .setId(ResourceKey.create(Registries.BLOCK, registryName))
            )
    );

    public static final DeferredBlock<Block> SALT_BLOCK = BLOCKS.registerSimpleBlock(
            "salt_block",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.CLAY)
    );

    public static final DeferredBlock<Block> SULFUR_ORE = BLOCKS.registerSimpleBlock(
            "sulfur_ore",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
    );

    public static final DeferredBlock<Block> DEEPSLATE_SULFUR_ORE = BLOCKS.registerSimpleBlock(
            "deepslate_sulfur_ore",
            props -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)
    );

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

    public static final DeferredBlock<GasifierBlock> GASIFIER = BLOCKS.registerBlock(
            "gasifier",
            GasifierBlock::new,
            props -> props
                            .strength(3.5F)
                            .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> CHEMICAL_REACTOR =
            BLOCKS.registerBlock("chemical_reactor",
                    ChemicalReactorBlock::new,
                    props -> props
                                    .strength(3.5F)
                                    .requiresCorrectToolForDrops()
                                    .sound(SoundType.METAL)
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

    public static final DeferredBlock<TherapistDeskBlock> THERAPIST_DESK =
            BLOCKS.registerBlock("therapist_desk", props -> new TherapistDeskBlock(props.strength(2.5F)));

    public static final DeferredBlock<RecoveryAnchorBlock> RECOVERY_ANCHOR =
            BLOCKS.registerBlock("recovery_anchor", props -> new RecoveryAnchorBlock(props.strength(2.0F).lightLevel(state -> 8)));

    private ModBlocks() {
    }
}
