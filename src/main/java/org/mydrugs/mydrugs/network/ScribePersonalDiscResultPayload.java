package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record ScribePersonalDiscResultPayload(
        boolean success,
        String messageKey,
        String trackId,
        String title
) implements CustomPacketPayload {
    public static final Type<ScribePersonalDiscResultPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "scribe_personal_disc_result"));

    public static final StreamCodec<ByteBuf, ScribePersonalDiscResultPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ScribePersonalDiscResultPayload::success,
            ByteBufCodecs.stringUtf8(128), ScribePersonalDiscResultPayload::messageKey,
            ByteBufCodecs.stringUtf8(128), ScribePersonalDiscResultPayload::trackId,
            ByteBufCodecs.stringUtf8(96), ScribePersonalDiscResultPayload::title,
            ScribePersonalDiscResultPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
