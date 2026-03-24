package org.mydrugs.mydrugs.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.items.bottle.BottleFluidContent;
import org.mydrugs.mydrugs.items.data.BloodSample;
import org.mydrugs.mydrugs.fluids.BottleLiquid;

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

    private ModDataComponents() {}
}
