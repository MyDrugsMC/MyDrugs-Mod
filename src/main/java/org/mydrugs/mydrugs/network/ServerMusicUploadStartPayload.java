package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.recovery.music.ServerMusicLibrary;

public record ServerMusicUploadStartPayload(
        String uploadId, String localTrackId, String title, String artist,
        int durationMs, int fileSize, String sha256
) implements CustomPacketPayload {
    public static final Type<ServerMusicUploadStartPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "server_music_upload_start"));
    public static final StreamCodec<ByteBuf, ServerMusicUploadStartPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(36), ServerMusicUploadStartPayload::uploadId,
            ByteBufCodecs.stringUtf8(128), ServerMusicUploadStartPayload::localTrackId,
            ByteBufCodecs.stringUtf8(96), ServerMusicUploadStartPayload::title,
            ByteBufCodecs.stringUtf8(96), ServerMusicUploadStartPayload::artist,
            ByteBufCodecs.VAR_INT, ServerMusicUploadStartPayload::durationMs,
            ByteBufCodecs.VAR_INT, ServerMusicUploadStartPayload::fileSize,
            ByteBufCodecs.stringUtf8(64), ServerMusicUploadStartPayload::sha256,
            ServerMusicUploadStartPayload::new
    );
    public static void handleOnServer(ServerMusicUploadStartPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ServerMusicLibrary.handleUploadStart(context.player(), payload));
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
