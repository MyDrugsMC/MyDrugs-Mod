package org.mydrugs.mydrugs.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.function.Supplier;

public class ModBlockTypes {
    public static final DeferredRegister<MapCodec<? extends Block>> BLOCK_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, MyDrugs.MODID);

    public static final Supplier<MapCodec<AdvancedFurnaceBlock>> ADVANCED_FURNACE_CODEC =
            BLOCK_TYPES.register("advanced_furnace", () -> Block.simpleCodec(AdvancedFurnaceBlock::new));

    public static final Supplier<MapCodec<DistillerBlock>> DISTILLER_CODEC =
            BLOCK_TYPES.register("distiller", () -> Block.simpleCodec(DistillerBlock::new));

    public static final Supplier<MapCodec<SieveBlock>> SIEVE_CODEC =
            BLOCK_TYPES.register("dryer", () -> Block.simpleCodec(SieveBlock::new));

    public static final Supplier<MapCodec<FluidFiltererBlock>> FLUID_FILTERER_CODEC =
            BLOCK_TYPES.register("fluid_filterer", () -> Block.simpleCodec(FluidFiltererBlock::new));

    public static final Supplier<MapCodec<EvaporationTrayBlock>> EVAPORATION_TRAY_CODEC =
            BLOCK_TYPES.register("evaporation_tray", () -> Block.simpleCodec(EvaporationTrayBlock::new));

    public static final Supplier<MapCodec<CentrifugeBlock>> CENTRIFUGE_CODEC =
            BLOCK_TYPES.register("centrifuge", () -> Block.simpleCodec(CentrifugeBlock::new));

    public static final Supplier<MapCodec<BTXFractionationTowerBlock>> BTX_FRACTIONATION_TOWER_CODEC =
            BLOCK_TYPES.register("btx_fractionation_tower", () -> Block.simpleCodec(BTXFractionationTowerBlock::new));

    public static final Supplier<MapCodec<AromaticExtractorBlock>> AROMATIC_EXTRACTOR_CODEC =
            BLOCK_TYPES.register("aromatic_extractor", () -> Block.simpleCodec(AromaticExtractorBlock::new));

    public static final Supplier<MapCodec<ElectrolyzerBlock>> ELECTROLYZER_CODEC =
            BLOCK_TYPES.register("electrolyzer", () -> Block.simpleCodec(ElectrolyzerBlock::new));

    public static final Supplier<MapCodec<GrowthChamberBlock>> GROWTH_CHAMBER_CODEC =
            BLOCK_TYPES.register("growth_chamber", () -> Block.simpleCodec(GrowthChamberBlock::new));

    public static final Supplier<MapCodec<GeneExtractorBlock>> GENE_EXTRACTOR_CODEC =
            BLOCK_TYPES.register("gene_extractor", () -> Block.simpleCodec(GeneExtractorBlock::new));

    public static final Supplier<MapCodec<KrisprKas9CombinatorBlock>> CRISPR_CAS9_COMBINATOR_CODEC =
            BLOCK_TYPES.register("crispr_cas9_combinator", () -> Block.simpleCodec(KrisprKas9CombinatorBlock::new));

    public static final Supplier<MapCodec<BacterialIncubatorBlock>> BACTERIAL_INCUBATOR_CODEC =
            BLOCK_TYPES.register("bacterial_incubator", () -> Block.simpleCodec(BacterialIncubatorBlock::new));

    public static final Supplier<MapCodec<HemogenicInfuserBlock>> HEMOGENIC_INFUSER_CODEC =
            BLOCK_TYPES.register("hemogenic_infuser", () -> Block.simpleCodec(HemogenicInfuserBlock::new));

    public static final Supplier<MapCodec<AutoclaveBlock>> AUTOCLAVE_CODEC =
            BLOCK_TYPES.register("autoclave", () -> Block.simpleCodec(AutoclaveBlock::new));

    public static final Supplier<MapCodec<CatalyticReformerBlock>> CATALYTIC_REFORMER_CODEC =
            BLOCK_TYPES.register("catalytic_reformer", () -> Block.simpleCodec(CatalyticReformerBlock::new));

    public static final Supplier<MapCodec<SteamCrackerBlock>> STEAM_CRACKER_CODEC =
            BLOCK_TYPES.register("steam_cracker", () -> Block.simpleCodec(SteamCrackerBlock::new));

    public static final Supplier<MapCodec<PsychotropeCoreBlock>> PSYCHOTROPE_CORE_CODEC =
            BLOCK_TYPES.register("psychotrope_core", () -> Block.simpleCodec(PsychotropeCoreBlock::new));

    public static final Supplier<MapCodec<PsychotropeComponentBlock>> PSYCHOTROPE_COMPONENT_CODEC =
            BLOCK_TYPES.register("psychotrope_component", () -> Block.simpleCodec(PsychotropeComponentBlock::new));

    private ModBlockTypes() {
    }
}
