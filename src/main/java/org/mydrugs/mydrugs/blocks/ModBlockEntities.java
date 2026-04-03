package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.entity.*;

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

    public static final Supplier<BlockEntityType<DryerBlockEntity>> DRYER = BLOCK_ENTITY_TYPES.register(
            "dryer",
            () -> new BlockEntityType<>(DryerBlockEntity::new, false, ModBlocks.DRYER.get())
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
}