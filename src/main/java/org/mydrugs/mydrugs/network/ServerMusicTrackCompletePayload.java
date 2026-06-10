package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record ServerMusicTrackCompletePayload(String serverTrackId, String audioHash) implements CustomPacketPayload {
    public static final Type<ServerMusicTrackCompletePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "server_music_track_complete"));
    public static final StreamCodec<ByteBuf, ServerMusicTrackCompletePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(128), ServerMusicTrackCompletePayload::serverTrackId,
            ByteBufCodecs.stringUtf8(64), ServerMusicTrackCompletePayload::audioHash,
            ServerMusicTrackCompletePayload::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
