package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.entity.*;
import org.mydrugs.mydrugs.pipe.blockentity.PipeBlockEntity;

import java.util.function.Supplier;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MyDrugs.MODID);

    public static final Supplier<BlockEntityType<GrindingBowlBlockEntity>> GRINDING_BOWL =
            BLOCK_ENTITY_TYPES.register(
                    "grinding_bowl",
                    () -> new BlockEntityType<>(
                            GrindingBowlBlockEntity::new,
                            false,
                            ModBlocks.GRINDING_BOWL.get()
                    )
            );

    public static final Supplier<BlockEntityType<StompCrafterBlockEntity>> STOMP_CRAFTER =
            BLOCK_ENTITY_TYPES.register("stomp_crafter",
                    () -> new BlockEntityType<>(
                            StompCrafterBlockEntity::new,
                            false,
                            ModBlocks.STOMP_CRAFTER.get()
                    ));

    public static final Supplier<BlockEntityType<AdvancedFurnaceBlockEntity>> ADVANCED_FURNACE =
            BLOCK_ENTITY_TYPES.register(
                    "advanced_furnace",
                    () -> new BlockEntityType<>(
                            AdvancedFurnaceBlockEntity::new,
                            false,
                            ModBlocks.ADVANCED_FURNACE.get()
                    )
            );


    public static final Supplier<BlockEntityType<DistillerBlockEntity>> DISTILLER =
            BLOCK_ENTITY_TYPES.register(
                    "distiller",
                    () -> new BlockEntityType<>(
                            DistillerBlockEntity::new,
                            false,
                            ModBlocks.DISTILLER.get()
                    )
            );

    public static final Supplier<BlockEntityType<MixingVatBlockEntity>> MIXING_VAT =
            BLOCK_ENTITY_TYPES.register(
                    "mixing_vat",
                    () -> new BlockEntityType<>(MixingVatBlockEntity::new, false, ModBlocks.MIXING_VAT.get())
            );

    public static final Supplier<BlockEntityType<SieveBlockEntity>> SIEVE = BLOCK_ENTITY_TYPES.register(
            "sieve",
            () -> new BlockEntityType<>(SieveBlockEntity::new, false, ModBlocks.SIEVE.get())
    );

    public static final Supplier<BlockEntityType<FluidFiltererBlockEntity>> FLUID_FILTERER = BLOCK_ENTITY_TYPES.register(
            "fluid_filterer",
            () -> new BlockEntityType<>(
                    FluidFiltererBlockEntity::new,
                    ModBlocks.FLUID_FILTERER.get()
            )
    );

    public static final Supplier<BlockEntityType<EvaporationTrayBlockEntity>> EVAPORATION_TRAY =
            BLOCK_ENTITY_TYPES.register("evaporation_tray",
                    () -> new BlockEntityType<>(
                            EvaporationTrayBlockEntity::new,
                            ModBlocks.EVAPORATION_TRAY.get()
                    ));

    public static final Supplier<BlockEntityType<CentrifugeBlockEntity>> CENTRIFUGE =
            BLOCK_ENTITY_TYPES.register("centrifuge",
                    () -> new BlockEntityType<>(
                            CentrifugeBlockEntity::new,
                            ModBlocks.CENTRIFUGE.get()
                    ));

    public static final Supplier<BlockEntityType<BTXFractionationTowerBlockEntity>> BTX_FRACTIONATION_TOWER =
            BLOCK_ENTITY_TYPES.register("btx_fractionation_tower",
                    () -> new BlockEntityType<>(
                            BTXFractionationTowerBlockEntity::new,
                            ModBlocks.BTX_FRACTIONATION_TOWER.get()
                    ));

    public static final Supplier<BlockEntityType<AromaticExtractorBlockEntity>> AROMATIC_EXTRACTOR =
            BLOCK_ENTITY_TYPES.register("aromatic_extractor",
                    () -> new BlockEntityType<>(
                            AromaticExtractorBlockEntity::new,
                            ModBlocks.AROMATIC_EXTRACTOR.get()
                    ));

    public static final Supplier<BlockEntityType<ElectrolyzerBlockEntity>> ELECTROLYZER =
            BLOCK_ENTITY_TYPES.register("electrolyzer",
                    () -> new BlockEntityType<>(
                            ElectrolyzerBlockEntity::new,
                            ModBlocks.ELECTROLYZER.get()
                    ));

    public static final Supplier<BlockEntityType<GrowthChamberBlockEntity>> GROWTH_CHAMBER =
            BLOCK_ENTITY_TYPES.register("growth_chamber",
                    () -> new BlockEntityType<>(
                            GrowthChamberBlockEntity::new,
                            false,
                            ModBlocks.GROWTH_CHAMBER.get()
                    ));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BiochemicalReactorBlockEntity>> BIOCHEMICAL_REACTOR =
            BLOCK_ENTITY_TYPES.register(
                    "biochemical_reactor",
                    () -> new BlockEntityType<>(
                            BiochemicalReactorBlockEntity::new,
                            ModBlocks.BIOCHEMICAL_REACTOR.get()
                    )
            );

    public static final Supplier<BlockEntityType<GasTankBlockEntity>> GAS_TANK = BLOCK_ENTITY_TYPES.register(
            "gas_tank",
            () -> new BlockEntityType<>(
                    GasTankBlockEntity::new,
                    false,
                    ModBlocks.GAS_TANK.get()
            )
    );

    public static final Supplier<BlockEntityType<GasPumpBlockEntity>> GAS_PUMP = BLOCK_ENTITY_TYPES.register(
            "gas_pump",
            () -> new BlockEntityType<>(
                    GasPumpBlockEntity::new,
                    false,
                    ModBlocks.GAS_PUMP.get()
            )
    );

    public static final Supplier<BlockEntityType<FluidPumpBlockEntity>> FLUID_PUMP = BLOCK_ENTITY_TYPES.register(
            "fluid_pump",
            () -> new BlockEntityType<>(
                    FluidPumpBlockEntity::new,
                    false,
                    ModBlocks.FLUID_PUMP.get()
            )
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GasifierBlockEntity>> GASIFIER =
            BLOCK_ENTITY_TYPES.register(
                    "gasifier",
                    () -> new BlockEntityType<>(
                            GasifierBlockEntity::new,
                            false,
                            ModBlocks.GASIFIER.get()
                    )
            );

    public static final Supplier<BlockEntityType<ChemicalReactorBlockEntity>> CHEMICAL_REACTOR =
            BLOCK_ENTITY_TYPES.register("chemical_reactor",
                    () -> new BlockEntityType<>(
                            ChemicalReactorBlockEntity::new,
                            false,
                            ModBlocks.CHEMICAL_REACTOR.get()
                    ));

    public static final Supplier<BlockEntityType<AdvancedMixingVatBlockEntity>> ADVANCED_MIXING_VAT_BE =
            BLOCK_ENTITY_TYPES.register(
                    "advanced_mixing_vat",
                    () -> new BlockEntityType<>(
                            AdvancedMixingVatBlockEntity::new,
                            false,
                            ModBlocks.ADVANCED_MIXING_VAT.get()
                    )
            );



    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManualCoffeePulperBlockEntity>> MANUAL_COFFEE_PULPER =
            BLOCK_ENTITY_TYPES.register(
                    "manual_coffee_pulper",
                    () -> new BlockEntityType<>(ManualCoffeePulperBlockEntity::new, ModBlocks.MANUAL_COFFEE_PULPER.get())
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoffeeDryingMatBlockEntity>> COFFEE_DRYING_MAT =
            BLOCK_ENTITY_TYPES.register(
                    "coffee_drying_mat",
                    () -> new BlockEntityType<>(CoffeeDryingMatBlockEntity::new, ModBlocks.COFFEE_DRYING_MAT.get())
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>> DRYING_RACK =
            BLOCK_ENTITY_TYPES.register(
                    "drying_rack",
                    () -> new BlockEntityType<>(DryingRackBlockEntity::new, ModBlocks.DRYING_RACK.get())
            );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClayVatBlockEntity>> CLAY_VAT =
            BLOCK_ENTITY_TYPES.register("clay_vat",
                    () -> new BlockEntityType<>(
                            ClayVatBlockEntity::new,
                            ModBlocks.CLAY_VAT.get()
                    ));

    public static final Supplier<BlockEntityType<PsyAnvilBlockEntity>> PSY_ANVIL =
            BLOCK_ENTITY_TYPES.register("psy_anvil",
                    () -> new BlockEntityType<>(
                            PsyAnvilBlockEntity::new,
                            false,
                            ModBlocks.PSY_ANVIL.get()
                    ));

    public static final Supplier<BlockEntityType<CatalyticReformerBlockEntity>> CATALYTIC_REFORMER =
            BLOCK_ENTITY_TYPES.register("catalytic_reformer",
                    () -> new BlockEntityType<>(
                            CatalyticReformerBlockEntity::new,
                            ModBlocks.CATALYTIC_REFORMER.get()
                    ));

    public static final Supplier<BlockEntityType<SteamCrackerBlockEntity>> STEAM_CRACKER =
            BLOCK_ENTITY_TYPES.register("steam_cracker",
                    () -> new BlockEntityType<>(
                            SteamCrackerBlockEntity::new,
                            ModBlocks.STEAM_CRACKER.get()
                    ));

    public static final Supplier<BlockEntityType<PsychotropeComponentBlockEntity>> PSYCHOTROPE_COMPONENT =
            BLOCK_ENTITY_TYPES.register("psychotrope_component",
                    () -> new BlockEntityType<>(
                            PsychotropeComponentBlockEntity::new,
                            false,
                            ModBlocks.PSYCHOTROPE_COMPONENT.get()
                    ));

    public static final Supplier<BlockEntityType<PsychotropeCoreBlockEntity>> PSYCHOTROPE_CORE =
            BLOCK_ENTITY_TYPES.register("psychotrope_core",
                    () -> new BlockEntityType<>(
                            PsychotropeCoreBlockEntity::new,
                            false,
                            ModBlocks.PSYCHOTROPE_CORE.get()
                    ));

    public static final Supplier<BlockEntityType<FormedPsyMixerCoreBlockEntity>> FORMED_PSY_MIXER_CORE =
            BLOCK_ENTITY_TYPES.register("formed_psy_mixer_core",
                    () -> new BlockEntityType<>(
                            FormedPsyMixerCoreBlockEntity::new,
                            ModBlocks.FORMED_PSY_MIXER_CORE.get()
                    ));

    public static final Supplier<BlockEntityType<FormedPsyMixerPartBlockEntity>> FORMED_PSY_MIXER_PART =
            BLOCK_ENTITY_TYPES.register("formed_psy_mixer_part",
                    () -> new BlockEntityType<>(
                            FormedPsyMixerPartBlockEntity::new,
                            ModBlocks.FORMED_PSY_MIXER_PART.get()
                    ));

    public static final Supplier<BlockEntityType<PipeBlockEntity>> PIPES =
            BLOCK_ENTITY_TYPES.register("pipes",
                    () -> new BlockEntityType<>(
                            PipeBlockEntity::new,
                            false,
                            ModBlocks.BASIC_ITEM_PIPE.get(),
                            ModBlocks.FAST_ITEM_PIPE.get(),
                            ModBlocks.BASIC_FLUID_PIPE.get(),
                            ModBlocks.FAST_FLUID_PIPE.get(),
                            ModBlocks.BASIC_GAS_PIPE.get(),
                            ModBlocks.FAST_GAS_PIPE.get()
                    ));
}
