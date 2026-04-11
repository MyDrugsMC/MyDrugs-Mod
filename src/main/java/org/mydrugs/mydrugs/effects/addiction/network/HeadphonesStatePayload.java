package org.mydrugs.mydrugs.effects.addiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record HeadphonesStatePayload(
        boolean enabled,
        int trackNonce
) implements CustomPacketPayload {
    public static final Type<HeadphonesStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "headphones_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HeadphonesStatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, HeadphonesStatePayload::enabled,
                    ByteBufCodecs.VAR_INT, HeadphonesStatePayload::trackNonce,
                    HeadphonesStatePayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}