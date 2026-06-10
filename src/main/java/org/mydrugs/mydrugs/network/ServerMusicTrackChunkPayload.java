package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record ServerMusicTrackChunkPayload(String audioHash, int chunkIndex, byte[] data) implements CustomPacketPayload {
    public static final Type<ServerMusicTrackChunkPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "server_music_track_chunk"));
    public static final StreamCodec<ByteBuf, ServerMusicTrackChunkPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                ByteBufCodecs.stringUtf8(64).encode(buf, p.audioHash());
                ByteBufCodecs.VAR_INT.encode(buf, p.chunkIndex());
                ByteBufCodecs.VAR_INT.encode(buf, p.data().length);
                buf.writeBytes(p.data());
            },
            buf -> {
                String hash = ByteBufCodecs.stringUtf8(64).decode(buf);
                int index = ByteBufCodecs.VAR_INT.decode(buf);
                int size = ByteBufCodecs.VAR_INT.decode(buf);
                if (size < 0 || size > ServerMusicUploadChunkPayload.MAX_CHUNK_SIZE) {
                    throw new IllegalArgumentException("Invalid music chunk size");
                }
                byte[] data = new byte[size];
                buf.readBytes(data);
                return new ServerMusicTrackChunkPayload(hash, index, data);
            }
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
