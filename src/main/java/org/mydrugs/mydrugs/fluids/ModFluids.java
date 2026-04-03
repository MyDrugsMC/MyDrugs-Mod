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
    public static final FluidEntry TRYPTOPHAN;
    public static final FluidEntry WASTE_BIOMASS;
    public static final FluidEntry LYSERGIC_ACID; //Diéthylamine
    public static final FluidEntry DIETHYLAMINE;
    public static final FluidEntry ERGOTAMINE;


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
        TRYPTOPHAN = register(new FluidEntryDef("tryptophan", 0xC3EBE8, false, null));
        WASTE_BIOMASS = register(new FluidEntryDef("waste_biomass", 0xFF035700, false, null));
        LYSERGIC_ACID = register(new FluidEntryDef("lysergic_acid", 0xFFFFFFFF, false, null));
        DIETHYLAMINE = register(new FluidEntryDef("diethylamine", 0xFFFEFFD1, false, null));
        ERGOTAMINE = register(new FluidEntryDef("ergotamine", 0xFFFFFFFF, false, null));
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