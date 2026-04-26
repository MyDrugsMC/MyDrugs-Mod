package org.mydrugs.mydrugs.fluids;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, MyDrugs.MODID);

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, MyDrugs.MODID);

    public static final DeferredRegister.Blocks FLUID_BLOCKS =
            DeferredRegister.createBlocks(MyDrugs.MODID);

    public static final DeferredRegister.Items FLUID_ITEMS =
            DeferredRegister.createItems(MyDrugs.MODID);

    public static final Map<String, FluidEntry> ALL = new LinkedHashMap<>();

    public static final FluidEntry STARCH_MASH;
    public static final FluidEntry SWEET_MASH;
    public static final FluidEntry WILD_YEAST;
    public static final FluidEntry FERMENTED_MASH;
    public static final FluidEntry RAW_ALCOHOL;
    public static final FluidEntry VODKA;
    public static final FluidEntry AMMONIAC;
    public static final FluidEntry BLOOD;
    public static final FluidEntry MURKY_EXTRACT;
    public static final FluidEntry FILTERED_EXTRACT;
    public static final FluidEntry COAL_TAR;
    public static final FluidEntry AMINO_ACID;
    public static final FluidEntry WASTE_BIOMASS;
    public static final FluidEntry LYSERGIC_ACID;
    public static final FluidEntry DIETHYLAMINE;
    public static final FluidEntry ERGOTAMINE;
    public static final FluidEntry LSD;
    public static final FluidEntry HYDROCHLORIC_ACID;
    public static final FluidEntry ACYLATING_AGENT;
    public static final FluidEntry ACTIVATED_LYSERGIC_ACID;
    public static final FluidEntry SULFURIC_ACID;
    public static final FluidEntry CRACK;
    public static final FluidEntry WET_ACTIVATED_COAL;
    public static final FluidEntry METHANOL;
    public static final FluidEntry METHYLAMINE;
    public static final FluidEntry BRINE;
    public static final FluidEntry CRUDE_C4_MIX;
    public static final FluidEntry BUTADIENE;
    public static final FluidEntry PETROLEUM;
    public static final FluidEntry NAPHTHA;
    public static final FluidEntry INDUSTRIAL_MIX;
    public static final FluidEntry REFORMATE;
    public static final FluidEntry BTX_MIX;
    public static final FluidEntry SULFOLANE;
    public static final FluidEntry COKE;
    public static final FluidEntry BUTENES;
    public static final FluidEntry SULFOLENE;
    public static final FluidEntry BENZENE;
    public static final FluidEntry TOLUENE;
    public static final FluidEntry XYLENE;

    static {
        STARCH_MASH = register(new FluidEntryDef("starch_mash", 0xFFD2B07A, false, null));
        SWEET_MASH = register(new FluidEntryDef("sweet_mash", 0xFFE2C070, false, null));
        WILD_YEAST = register(new FluidEntryDef("wild_yeast", 0xFFDDD9A6, false, null));
        FERMENTED_MASH = register(new FluidEntryDef("fermented_mash", 0xFFB98E57, false, null));
        RAW_ALCOHOL = register(new FluidEntryDef("raw_alcohol", 0xCCF2F2FF, true, DrugId.ALCOHOL));
        VODKA = register(new FluidEntryDef("vodka", 0xCCEAF6FF, true, DrugId.ALCOHOL));
        AMMONIAC = register(new FluidEntryDef("ammoniac", 0xFFCFE17A, false, null));
        BLOOD = register(new FluidEntryDef("blood", 0xFF8E1B1B, false, null));
        MURKY_EXTRACT = register(new FluidEntryDef("murky_extract", 0xFF17960C, false, null));
        FILTERED_EXTRACT = register(new FluidEntryDef("filtered_extract", 0xFF92F78D, false, null));
        COAL_TAR = register(new FluidEntryDef("coal_tar", 0xFF000000, false, null));
        AMINO_ACID = register(new FluidEntryDef("amino_acid", 0xFFDDDDAA, false, null));
        WASTE_BIOMASS = register(new FluidEntryDef("waste_biomass", 0xFF035700, false, null));
        LYSERGIC_ACID = register(new FluidEntryDef("lysergic_acid", 0xFFFFFFFF, false, null));
        DIETHYLAMINE = register(new FluidEntryDef("diethylamine", 0xFFFEFFD1, false, null));
        ERGOTAMINE = register(new FluidEntryDef("ergotamine", 0xFFFFFFFF, false, null));
        LSD = register(new FluidEntryDef("lsd", 0xFFFFFFFF, true, DrugId.LSD));
        HYDROCHLORIC_ACID = register(new FluidEntryDef("hydrochloric_acid", 0xFFFFFFFF, false, null));
        ACYLATING_AGENT = register(new FluidEntryDef("acylating_agent", 0xFFFFFFFF, false, null));
        ACTIVATED_LYSERGIC_ACID = register(new FluidEntryDef("activated_lysergic_acid", 0xFFFFFFFF, false, null));
        SULFURIC_ACID = register(new FluidEntryDef("sulfuric_acid", 0xFFD6E86A, false, null));
        CRACK = register(new FluidEntryDef("crack", 0xFFCFE17A, false, null));
        WET_ACTIVATED_COAL = register(new FluidEntryDef("wet_activated_coal", 0xFF000000, false, null));
        METHANOL = register(new FluidEntryDef("methanol", 0xFFFFFFFF, false, null));
        METHYLAMINE = register(new FluidEntryDef("methylamine", 0xFFFFFFFF, false, null));
        BRINE = register(new FluidEntryDef("brine", 0xFFFFFFFF, true, null));
        CRUDE_C4_MIX = register(new FluidEntryDef("crude_c4_mix", 0xFF000000, false, null));
        BUTADIENE = register(new FluidEntryDef("butadiene", 0xFFFFFFFF, false, null));
        PETROLEUM = register(new FluidEntryDef("petroleum", 0xFF000000, false, null));
        NAPHTHA = register(new FluidEntryDef("naphtha", 0xFF000000, false, null));
        INDUSTRIAL_MIX = register(new FluidEntryDef("industrial_mix", 0xFF000000, false, null));
        REFORMATE = register(new FluidEntryDef("reformate", 0xFFFFFFFF, false, null));
        BTX_MIX = register(new FluidEntryDef("btx_mix", 0xFF2E2B24, false, null));
        SULFOLANE = register(new FluidEntryDef("sulfolane", 0xFFFFFFFF, false, null));
        COKE = register(new FluidEntryDef("coke", 0xFF000000, false, null));
        BUTENES = register(new FluidEntryDef("butenes", 0xFF000000, false, null));
        SULFOLENE = register(new FluidEntryDef("sulfolene", 0xFFFFFFFF, false, null));
        BENZENE = register(new FluidEntryDef("benzene", 0xFFFFFFFF, false, null));
        TOLUENE = register(new FluidEntryDef("toluene", 0xFFFFFFFF, false, null));
        XYLENE = register(new FluidEntryDef("xylene", 0xFFFFFFFF, false, null));
    }

    private ModFluids() {
    }

    private static FluidEntry register(FluidEntryDef def) {
        FluidEntry entry = new FluidEntry(def.name(), def.tint());

        entry.setType(FLUID_TYPES.register(
                def.name() + "_type",
                () -> new DrugTintedFluidType(FluidType.Properties.create(), def.drinkable(), def.drugId())
        ));

        BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(
                entry.type(),
                () -> entry.source().get(),
                () -> entry.flowing().get()
        )
                .bucket(() -> entry.bucket().get())
                .block(() -> entry.block().get())
                .slopeFindDistance(2)
                .levelDecreasePerBlock(1);

        entry.setSource(FLUIDS.register(
                def.name(),
                () -> new BaseFlowingFluid.Source(properties)
        ));

        entry.setFlowing(FLUIDS.register(
                "flowing_" + def.name(),
                () -> new BaseFlowingFluid.Flowing(properties)
        ));

        entry.setBlock(FLUID_BLOCKS.registerBlock(
                def.name(),
                props -> new LiquidBlock(
                        entry.source().get(),
                        props
                                .noCollision()
                                .replaceable()
                                .strength(100.0F)
                                .noLootTable()
                )
        ));

        entry.setBucket(FLUID_ITEMS.registerItem(
                def.name() + "_bucket",
                props -> new BucketItem(
                        entry.source().get(),
                        props
                                .craftRemainder(Items.BUCKET)
                                .stacksTo(1)
                )
        ));

        ALL.put(def.name(), entry);
        return entry;
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, path);
    }
}