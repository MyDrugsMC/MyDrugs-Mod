package org.mydrugs.mydrugs.effects.addiction.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record VomitOverlayPayload(float intensity) implements CustomPacketPayload {
    public static final Type<VomitOverlayPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "vomit_overlay"));

    public static final StreamCodec<ByteBuf, VomitOverlayPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            VomitOverlayPayload::intensity,
            VomitOverlayPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
