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
    public static final FluidEntry CUMENE;
    public static final FluidEntry CUMENE_HYDROPEROXIDE;
    public static final FluidEntry ACETONE;
    public static final FluidEntry PHENOL;
    public static final FluidEntry CHLOROACETONE;
    public static final FluidEntry METHAMPHETAMINE;
    public static final FluidEntry PHENYLACETONE;
    public static final FluidEntry LIGHT_OIL;

    static {
        METHYLAMINE = register(new FluidEntryDef("methylamine", 0xFFFFFFFF, false, null));
        CRUDE_C4_MIX = register(new FluidEntryDef("crude_c4_mix", 0xFF000000, false, null));
        BUTADIENE = register(new FluidEntryDef("butadiene", 0xFFFFFFFF, false, null));
        COKE = register(new FluidEntryDef("coke", 0xFF000000, false, null));
        BUTENES = register(new FluidEntryDef("butenes", 0xFF000000, false, null));
        STARCH_MASH = register(new FluidEntryDef("starch_mash", 0xFFB7925E, false, null));
        SWEET_MASH = register(new FluidEntryDef("sweet_mash", 0xFFD7B46E, false, null));
        WILD_YEAST = register(new FluidEntryDef("wild_yeast", 0xFFE8DFB4, false, null));
        FERMENTED_MASH = register(new FluidEntryDef("fermented_mash", 0xFF9B6A3D, false, null));

        RAW_ALCOHOL = register(new FluidEntryDef("raw_alcohol", 0x66F4FAFF, true, DrugId.ALCOHOL));
        VODKA = register(new FluidEntryDef("vodka", 0x66F8FCFF, true, DrugId.ALCOHOL));

        AMMONIAC = register(new FluidEntryDef("ammoniac", 0x66EEF8FF, false, null));
        BLOOD = register(new FluidEntryDef("blood", 0xFF7A1014, false, null));
        MURKY_EXTRACT = register(new FluidEntryDef("murky_extract", 0xFF4B5B2B, false, null));
        FILTERED_EXTRACT = register(new FluidEntryDef("filtered_extract", 0xFF88A85A, false, null));
        COAL_TAR = register(new FluidEntryDef("coal_tar", 0xFF1A120C, false, null));

        AMINO_ACID = register(new FluidEntryDef("amino_acid", 0x88FFF4D8, false, null));
        WASTE_BIOMASS = register(new FluidEntryDef("waste_biomass", 0xFF2A391C, false, null));

        LYSERGIC_ACID = register(new FluidEntryDef("lysergic_acid", 0x88FFF3D7, false, null));
        DIETHYLAMINE = register(new FluidEntryDef("diethylamine", 0x66FFFDF2, false, null));
        ERGOTAMINE = register(new FluidEntryDef("ergotamine", 0x88F6EDD8, false, null));
        LSD = register(new FluidEntryDef("lsd", 0x66FFFDF7, false, DrugId.LSD));

        HYDROCHLORIC_ACID = register(new FluidEntryDef("hydrochloric_acid", 0x55FFFDF9, false, null));
        ACYLATING_AGENT = register(new FluidEntryDef("acylating_agent", 0x88FFF0B8, false, null));
        ACTIVATED_LYSERGIC_ACID = register(new FluidEntryDef("activated_lysergic_acid", 0x88FFE1A6, false, null));
        SULFURIC_ACID = register(new FluidEntryDef("sulfuric_acid", 0x55FFFBEA, false, null));

        CRACK = register(new FluidEntryDef("crack", 0x99FFF1C2, false, null));
        WET_ACTIVATED_COAL = register(new FluidEntryDef("wet_activated_coal", 0xFF2B2B2B, false, null));

        METHANOL = register(new FluidEntryDef("methanol", 0x66F8FCFF, false, null));
        BRINE = register(new FluidEntryDef("brine", 0x66F3F9FF, false, null));

        PETROLEUM = register(new FluidEntryDef("petroleum", 0xFF2B2418, false, null));
        NAPHTHA = register(new FluidEntryDef("naphtha", 0x88FFF1B8, false, null));
        INDUSTRIAL_MIX = register(new FluidEntryDef("industrial_mix", 0xCC8B6A2F, false, null));
        REFORMATE = register(new FluidEntryDef("reformate", 0x88FFD67A, false, null));
        BTX_MIX = register(new FluidEntryDef("btx_mix", 0x88E6C27E, false, null));

        SULFOLANE = register(new FluidEntryDef("sulfolane", 0x66F9FCFF, false, null));
        SULFOLENE = register(new FluidEntryDef("sulfolene", 0x88FFF6D8, false, null));

        BENZENE = register(new FluidEntryDef("benzene", 0x66FFFDF0, false, null));
        TOLUENE = register(new FluidEntryDef("toluene", 0x66FFFDF4, false, null));
        XYLENE = register(new FluidEntryDef("xylene", 0x66FFFDF4, false, null));
        CUMENE = register(new FluidEntryDef("cumene", 0x88FFE9A8, false, null));
        CUMENE_HYDROPEROXIDE = register(new FluidEntryDef("cumene_hydroperoxide", 0x88FFF0C8, false, null));
        ACETONE = register(new FluidEntryDef("acetone", 0x66F5FBFF, false, null));
        PHENOL = register(new FluidEntryDef("phenol", 0x88FFF8D6, false, null));

        CHLOROACETONE = register(new FluidEntryDef("chloroacetone", 0xFFFFFFFF, false, null));
        PHENYLACETONE = register(new FluidEntryDef("phenylacetone", 0xFFFFFFFF, false, null));
        METHAMPHETAMINE = register(new FluidEntryDef("methamphetamine", 0xFFFFFFFF, false, null));

        LIGHT_OIL = register(new FluidEntryDef("light_oil", 0x88E6C27E, false, null));
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
