package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record PersonalDiscPlaybackPayload(
        Action action,
        Source source,
        BlockPos pos,
        String trackId,
        String title,
        String artist,
        int durationMs,
        long startedGameTime
) implements CustomPacketPayload {
    private static final int MAX_TRACK_ID_LENGTH = 128;
    private static final int MAX_TITLE_LENGTH = 96;
    private static final int MAX_ARTIST_LENGTH = 96;

    public enum Action {
        START(0),
        STOP(1);

        private final int id;

        Action(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }

        public static Action byId(int id) {
            return id == STOP.id ? STOP : START;
        }
    }

    public enum Source {
        RECOVERY_JUKEBOX(0),
        VANILLA_JUKEBOX(1);

        private final int id;

        Source(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }

        public static Source byId(int id) {
            return id == VANILLA_JUKEBOX.id ? VANILLA_JUKEBOX : RECOVERY_JUKEBOX;
        }
    }

    public static final Type<PersonalDiscPlaybackPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "personal_disc_playback"));

    public static final StreamCodec<ByteBuf, PersonalDiscPlaybackPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ByteBufCodecs.VAR_INT.encode(buf, payload.action().id());
                ByteBufCodecs.VAR_INT.encode(buf, payload.source().id());
                BlockPos.STREAM_CODEC.encode(buf, payload.pos());
                ByteBufCodecs.stringUtf8(MAX_TRACK_ID_LENGTH).encode(buf, payload.trackId());
                ByteBufCodecs.stringUtf8(MAX_TITLE_LENGTH).encode(buf, payload.title());
                ByteBufCodecs.stringUtf8(MAX_ARTIST_LENGTH).encode(buf, payload.artist());
                ByteBufCodecs.VAR_INT.encode(buf, payload.durationMs());
                ByteBufCodecs.VAR_LONG.encode(buf, payload.startedGameTime());
            },
            buf -> new PersonalDiscPlaybackPayload(
                    Action.byId(ByteBufCodecs.VAR_INT.decode(buf)),
                    Source.byId(ByteBufCodecs.VAR_INT.decode(buf)),
                    BlockPos.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.stringUtf8(MAX_TRACK_ID_LENGTH).decode(buf),
                    ByteBufCodecs.stringUtf8(MAX_TITLE_LENGTH).decode(buf),
                    ByteBufCodecs.stringUtf8(MAX_ARTIST_LENGTH).decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf)
            )
    );

    public static PersonalDiscPlaybackPayload stop(Source source, BlockPos pos) {
        return new PersonalDiscPlaybackPayload(Action.STOP, source, pos, "", "", "", 0, 0L);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
