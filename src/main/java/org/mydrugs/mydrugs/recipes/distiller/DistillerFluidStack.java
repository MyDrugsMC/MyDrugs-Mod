package org.mydrugs.mydrugs.recipes.distiller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public record DistillerFluidStack(ResourceLocation fluid, int amount) {
    public static final Codec<DistillerFluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(DistillerFluidStack::fluid),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(DistillerFluidStack::amount)
    ).apply(instance, DistillerFluidStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DistillerFluidStack> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, DistillerFluidStack::fluid,
                    ByteBufCodecs.VAR_INT, DistillerFluidStack::amount,
                    DistillerFluidStack::new
            );

    public FluidStack toFluidStack() {
        Fluid fluidFluid = BuiltInRegistries.FLUID.getValue(fluid);
        return new FluidStack(fluidFluid, amount);
    }
}