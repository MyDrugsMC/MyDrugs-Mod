package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.recovery.music.ServerMusicLibrary;

public record ServerMusicUploadFinishPayload(String uploadId) implements CustomPacketPayload {
    public static final Type<ServerMusicUploadFinishPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "server_music_upload_finish"));
    public static final StreamCodec<ByteBuf, ServerMusicUploadFinishPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.stringUtf8(36), ServerMusicUploadFinishPayload::uploadId,
                    ServerMusicUploadFinishPayload::new);
    public static void handleOnServer(ServerMusicUploadFinishPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ServerMusicLibrary.handleUploadFinish(context.player(), payload));
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
