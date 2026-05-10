package org.mydrugs.mydrugs.effects.addiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record BadTripScreamerPayload(int durationTicks, float intensity) implements CustomPacketPayload {
    public static final Type<BadTripScreamerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "bad_trip_screamer"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BadTripScreamerPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, BadTripScreamerPayload::durationTicks,
                    ByteBufCodecs.FLOAT, BadTripScreamerPayload::intensity,
                    BadTripScreamerPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
