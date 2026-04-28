package org.mydrugs.mydrugs.recipes.steam_cracker;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record SteamCrackerFluidStack(ResourceLocation fluid, int amount) {
    public static final Codec<SteamCrackerFluidStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("fluid").forGetter(SteamCrackerFluidStack::fluid),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(SteamCrackerFluidStack::amount)
    ).apply(instance, SteamCrackerFluidStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SteamCrackerFluidStack> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, SteamCrackerFluidStack::fluid,
                    ByteBufCodecs.VAR_INT, SteamCrackerFluidStack::amount,
                    SteamCrackerFluidStack::new
            );
}
