package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record OpenDiscScriberScreenPayload(BlockPos scriberPos) implements CustomPacketPayload {
    public static final Type<OpenDiscScriberScreenPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "open_disc_scriber"));

    public static final StreamCodec<ByteBuf, OpenDiscScriberScreenPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> BlockPos.STREAM_CODEC.encode(buf, payload.scriberPos()),
            buf -> new OpenDiscScriberScreenPayload(BlockPos.STREAM_CODEC.decode(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
