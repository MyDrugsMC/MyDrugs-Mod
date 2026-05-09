package org.mydrugs.mydrugs.items.data;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.ritual.MixedDrugData;
import org.mydrugs.mydrugs.gas.GasTankContents;
import org.mydrugs.mydrugs.items.bottle.BottleFluidContent;
import org.mydrugs.mydrugs.items.data.BloodSample;
import org.mydrugs.mydrugs.items.rolling.RolledDrugContent;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterConfig;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MyDrugs.MODID);

    public static final Supplier<DataComponentType<Boolean>> FILLED =
            DATA_COMPONENTS.registerComponentType(
                    "filled",
                    builder -> builder
                            .persistent(Codec.BOOL)
                            .networkSynchronized(ByteBufCodecs.BOOL)
            );

    public static final Supplier<DataComponentType<BloodSample>> BLOOD_SAMPLE =
            DATA_COMPONENTS.registerComponentType(
                    "blood_sample",
                    builder -> builder
                            .persistent(BloodSample.CODEC)
                            .networkSynchronized(BloodSample.STREAM_CODEC)
            );

    public static final Supplier<DataComponentType<Integer>> BLOOD_AMOUNT =
            DATA_COMPONENTS.registerComponentType(
                    "blood_amount",
                    builder -> builder
                            .persistent(Codec.intRange(0, 100))
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
            );

    public static final Supplier<DataComponentType<BottleFluidContent>> BOTTLE_CONTENT =
            DATA_COMPONENTS.register("bottle_content", () ->
                    DataComponentType.<BottleFluidContent>builder()
                            .persistent(BottleFluidContent.CODEC)
                            .networkSynchronized(BottleFluidContent.STREAM_CODEC)
                            .cacheEncoding()
                            .build()
            );

    public static final Supplier<DataComponentType<RolledDrugContent>> ROLLED_CONTENT =
            DATA_COMPONENTS.registerComponentType(
                    "rolled_content",
                    builder -> builder
                            .persistent(RolledDrugContent.CODEC)
                            .networkSynchronized(RolledDrugContent.STREAM_CODEC)
            );

    public static final Supplier<DataComponentType<GasTankContents>> GAS_TANK_CONTENTS =
            DATA_COMPONENTS.registerComponentType(
                    "gas_tank_contents",
                    builder -> builder
                            .persistent(GasTankContents.CODEC)
                            .networkSynchronized(GasTankContents.STREAM_CODEC)
            );

    public static final Supplier<DataComponentType<BiomeFinderTarget>> BIOME_FINDER_TARGET =
            DATA_COMPONENTS.registerComponentType(
                    "biome_finder_target",
                    builder -> builder
                            .persistent(BiomeFinderTarget.CODEC)
                            .networkSynchronized(BiomeFinderTarget.STREAM_CODEC)
            );

    public static final Supplier<DataComponentType<PipeFilterConfig>> PIPE_FILTER_CONFIG =
            DATA_COMPONENTS.registerComponentType(
                    "pipe_filter_config",
                    builder -> builder.persistent(PipeFilterConfig.CODEC).cacheEncoding()
            );

    public static final Supplier<DataComponentType<MixedDrugData>> MIXED_DRUG_DATA =
            DATA_COMPONENTS.registerComponentType(
                    "mixed_drug_data",
                    builder -> builder
                            .persistent(MixedDrugData.CODEC)
                            .networkSynchronized(MixedDrugData.STREAM_CODEC)
            );

    private ModDataComponents() {
    }
}
