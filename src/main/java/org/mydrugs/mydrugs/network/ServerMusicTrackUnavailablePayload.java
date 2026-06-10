package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record ServerMusicTrackUnavailablePayload(String serverTrackId, String audioHash, String messageKey)
        implements CustomPacketPayload {
    public static final Type<ServerMusicTrackUnavailablePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "server_music_track_unavailable"));
    public static final StreamCodec<ByteBuf, ServerMusicTrackUnavailablePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(128), ServerMusicTrackUnavailablePayload::serverTrackId,
            ByteBufCodecs.stringUtf8(64), ServerMusicTrackUnavailablePayload::audioHash,
            ByteBufCodecs.stringUtf8(128), ServerMusicTrackUnavailablePayload::messageKey,
            ServerMusicTrackUnavailablePayload::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
