package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record ServerMusicTrackInfoPayload(String serverTrackId, String audioHash, int fileSize, int chunkCount)
        implements CustomPacketPayload {
    public static final Type<ServerMusicTrackInfoPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "server_music_track_info"));
    public static final StreamCodec<ByteBuf, ServerMusicTrackInfoPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(128), ServerMusicTrackInfoPayload::serverTrackId,
            ByteBufCodecs.stringUtf8(64), ServerMusicTrackInfoPayload::audioHash,
            ByteBufCodecs.VAR_INT, ServerMusicTrackInfoPayload::fileSize,
            ByteBufCodecs.VAR_INT, ServerMusicTrackInfoPayload::chunkCount,
            ServerMusicTrackInfoPayload::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
