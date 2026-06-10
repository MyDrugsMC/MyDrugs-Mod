package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record ServerMusicUploadResultPayload(
        String uploadId, boolean success, String messageKey, String serverTrackId, String audioHash
) implements CustomPacketPayload {
    public static final Type<ServerMusicUploadResultPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "server_music_upload_result"));
    public static final StreamCodec<ByteBuf, ServerMusicUploadResultPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(36), ServerMusicUploadResultPayload::uploadId,
            ByteBufCodecs.BOOL, ServerMusicUploadResultPayload::success,
            ByteBufCodecs.stringUtf8(128), ServerMusicUploadResultPayload::messageKey,
            ByteBufCodecs.stringUtf8(128), ServerMusicUploadResultPayload::serverTrackId,
            ByteBufCodecs.stringUtf8(64), ServerMusicUploadResultPayload::audioHash,
            ServerMusicUploadResultPayload::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
