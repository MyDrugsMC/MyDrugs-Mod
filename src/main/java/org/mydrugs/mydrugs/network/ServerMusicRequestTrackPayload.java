package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.recovery.music.ServerMusicLibrary;

public record ServerMusicRequestTrackPayload(String serverTrackId, String audioHash) implements CustomPacketPayload {
    public static final Type<ServerMusicRequestTrackPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "server_music_request_track"));
    public static final StreamCodec<ByteBuf, ServerMusicRequestTrackPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.stringUtf8(128), ServerMusicRequestTrackPayload::serverTrackId,
            ByteBufCodecs.stringUtf8(64), ServerMusicRequestTrackPayload::audioHash,
            ServerMusicRequestTrackPayload::new
    );
    public static void handleOnServer(ServerMusicRequestTrackPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ServerMusicLibrary.handleTrackRequest(context.player(), payload));
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
