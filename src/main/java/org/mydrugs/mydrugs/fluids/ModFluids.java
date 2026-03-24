package org.mydrugs.mydrugs.fluids;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.LinkedHashMap;
import java.util.List;
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

    public static final List<FluidEntryDef> DEFINITIONS = List.of(
            new FluidEntryDef("starch_mash",    0xFFD2B07A),
            new FluidEntryDef("sweet_mash",     0xFFE2C070),
            new FluidEntryDef("wild_yeast",     0xFFDDD9A6),
            new FluidEntryDef("fermented_mash", 0xFFB98E57),
            new FluidEntryDef("raw_alcohol",    0xCCF2F2FF),
            new FluidEntryDef("vodka",          0xCCEAF6FF)
    );

    public static final Map<String, FluidEntry> ALL = new LinkedHashMap<>();

    public static final FluidEntry STARCH_MASH;
    public static final FluidEntry SWEET_MASH;
    public static final FluidEntry WILD_YEAST;
    public static final FluidEntry FERMENTED_MASH;
    public static final FluidEntry RAW_ALCOHOL;
    public static final FluidEntry VODKA;

    static {
        STARCH_MASH    = register(new FluidEntryDef("starch_mash",    0xFFD2B07A));
        SWEET_MASH     = register(new FluidEntryDef("sweet_mash",     0xFFE2C070));
        WILD_YEAST     = register(new FluidEntryDef("wild_yeast",     0xFFDDD9A6));
        FERMENTED_MASH = register(new FluidEntryDef("fermented_mash", 0xFFB98E57));
        RAW_ALCOHOL    = register(new FluidEntryDef("raw_alcohol",    0xCCF2F2FF));
        VODKA          = register(new FluidEntryDef("vodka",          0xCCEAF6FF));
    }

    private ModFluids() {
    }

    private static FluidEntry register(FluidEntryDef def) {
        FluidEntry entry = new FluidEntry(def.name(), def.tint());

        entry.setType(FLUID_TYPES.register(
                def.name() + "_type",
                () -> new SimpleTintedFluidType(
                        FluidType.Properties.create()
                )
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