package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record SieveShakePayload(int menuId, float impulse) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SieveShakePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "sieve_shake"));

    public static final StreamCodec<ByteBuf, SieveShakePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SieveShakePayload::menuId,
                    ByteBufCodecs.FLOAT, SieveShakePayload::impulse,
                    SieveShakePayload::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}