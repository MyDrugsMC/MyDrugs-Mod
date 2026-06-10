package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.recovery.music.ServerMusicLibrary;

public record ServerMusicUploadChunkPayload(String uploadId, int chunkIndex, byte[] data) implements CustomPacketPayload {
    public static final int MAX_CHUNK_SIZE = 32 * 1024;
    public static final Type<ServerMusicUploadChunkPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "server_music_upload_chunk"));
    public static final StreamCodec<ByteBuf, ServerMusicUploadChunkPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.stringUtf8(36).encode(buf, p.uploadId());
                ByteBufCodecs.VAR_INT.encode(buf, p.chunkIndex());
                ByteBufCodecs.VAR_INT.encode(buf, p.data().length);
                buf.writeBytes(p.data());
            },
            buf -> {
                String id = ByteBufCodecs.stringUtf8(36).decode(buf);
                int index = ByteBufCodecs.VAR_INT.decode(buf);
                int size = ByteBufCodecs.VAR_INT.decode(buf);
                if (size < 0 || size > MAX_CHUNK_SIZE) throw new IllegalArgumentException("Invalid music chunk size");
                byte[] data = new byte[size];
                buf.readBytes(data);
                return new ServerMusicUploadChunkPayload(id, index, data);
            }
    );
    public static void handleOnServer(ServerMusicUploadChunkPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ServerMusicLibrary.handleUploadChunk(context.player(), payload));
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
