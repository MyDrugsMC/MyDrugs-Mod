package org.mydrugs.mydrugs.addiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record HeadphonesStatePayload(
        boolean playing,
        String trackId,
        int libraryVersion,
        float volume,
        boolean shuffle,
        boolean repeat
) implements CustomPacketPayload {
    public static final Type<HeadphonesStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "headphones_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HeadphonesStatePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        ByteBufCodecs.BOOL.encode(buf, payload.playing());
                        ByteBufCodecs.STRING_UTF8.encode(buf, payload.trackId());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.libraryVersion());
                        ByteBufCodecs.FLOAT.encode(buf, payload.volume());
                        ByteBufCodecs.BOOL.encode(buf, payload.shuffle());
                        ByteBufCodecs.BOOL.encode(buf, payload.repeat());
                    },
                    buf -> new HeadphonesStatePayload(
                            ByteBufCodecs.BOOL.decode(buf),
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.FLOAT.decode(buf),
                            ByteBufCodecs.BOOL.decode(buf),
                            ByteBufCodecs.BOOL.decode(buf)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
