package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record OpenHeadphonesMusicScreenPayload() implements CustomPacketPayload {
    public static final Type<OpenHeadphonesMusicScreenPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "open_headphones_music"));

    public static final StreamCodec<ByteBuf, OpenHeadphonesMusicScreenPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenHeadphonesMusicScreenPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
