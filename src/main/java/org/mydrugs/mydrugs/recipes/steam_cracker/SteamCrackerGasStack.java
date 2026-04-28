package org.mydrugs.mydrugs.recipes.steam_cracker;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record SteamCrackerGasStack(ResourceLocation gas, int amount) {
    public static final Codec<SteamCrackerGasStack> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("gas").forGetter(SteamCrackerGasStack::gas),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(SteamCrackerGasStack::amount)
    ).apply(instance, SteamCrackerGasStack::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SteamCrackerGasStack> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, SteamCrackerGasStack::gas,
                    ByteBufCodecs.VAR_INT, SteamCrackerGasStack::amount,
                    SteamCrackerGasStack::new
            );
}
